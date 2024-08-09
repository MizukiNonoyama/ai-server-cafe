package ai_server_cafe.filter.kalman;

import ai_server_cafe.filter.AbstractFilterManual;
import ai_server_cafe.filter.kalman.detail.*;
import ai_server_cafe.model.FilteredRobot;
import ai_server_cafe.model.RawRobot;
import ai_server_cafe.util.interfaces.IFuncParam2;
import ai_server_cafe.util.math.MathHelper;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationConvention;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import javax.annotation.Nonnull;
import java.util.*;

public class FilterControlledRobot extends AbstractFilterManual<FilteredRobot, RawRobot> {
    // 前回呼び出しのシステム時刻
    private double prevTime;
    // カメラが情報を更新した時刻
    private double receiveTime;
    // 前回観測値を使った時間
    private double prevObserveTime;

    // コントローラの制御周期
    private double controlCycle;
    // delay mod controlCycle
    private double modDelay;

    // 見えなくなってからロストさせるまでの時間
    private final double lostDuration;

    // 観測空間モデル
    private LinerSystem.Observation obsModel;

    // 目標速度のバッファ (ロボット基準)
    private List<Vector3D> targetV;
    // バッファの大きさ
    private final int bufferSize;

    // むだ時間補償の概略図
    //
    //                 |<--    delay     -->|
    //                 |                    |
    //                 |  cycle             |
    //              -->|        |<--        |
    //               mod_delay_ |           |
    //              -->|  |<--  |           |
    // received time:  *--------#--------o--------o--------o--------o
    // control time :  ---$--------+--------+--------+--------+--------+
    // target_v_    :   at(0)    at(1)    at(2)    at(3)
    //
    // カルマンフィルタでは、観測値と同じ時刻におけるシステムの状態を予測する必要がある。
    // 観測値が図中 `#` の時刻に観測された場合、システムの状態を予測するために
    // `*` 地点の推定結果と、区間 [`*`, `#`] における制御入力を用いる。
    // 今回は制御入力を加速度 `(目標速度 - 現在速度)/dt` としているため、
    // 区間 [`*`, `$`] における制御入力は `(target_v_.at(0) - `*`地点の速度)/mod_delay_` 、
    // 区間 [`$`, `#`] における制御入力は `(target_v_.at(1) - `$`地点の速度)/(cycle-mod_delay_)`
    // となる。

    // 前回の値
    private Optional<FilteredRobot> robot;
    //観測した情報
    private Optional<RawRobot> rawValue;

    // 推定値
    //   - 位置: フィールド基準
    //   - 速度: ロボット基準
    private Estimation estimation;

    /// @brief       コンストラクタ
    /// @param wf               値を書き込むための関数
    /// @param cycle            制御周期
    /// @param delay            ロボットがキャプチャされてから制御されるまでの時間
    /// @param lost_duration    ロスト時に補完する時間
    public FilterControlledRobot(double cycle, double delay, double lostDuration) {
        super();
        this.lostDuration = lostDuration;
        this.controlCycle = cycle;
        this.modDelay = MathHelper.mod(delay, cycle);
        this.prevTime = 0.0;
        this.prevObserveTime = 0.0;
        this.receiveTime = 0.0;
        this.obsModel = new LinerSystem.Observation(6, 3,
                // 推定した位置をそのまま観測値とする
                MathHelper.makeIdentity(3, 6),
                MathHelper.makeDiagonal(new double[] {Probability.toVariance(5.0), Probability.toVariance(5.0),
                        Probability.toVariance(Math.toRadians(5.0))}));
        this.bufferSize = (int)(delay / cycle) + 2;
        this.targetV = new ArrayList<>();
        for (int i = 0; i < this.bufferSize; i++) {
            this.targetV.addLast(Vector3D.ZERO);
        }
        this.estimation = new Estimation(6, MathHelper.makeVectorMatrix(new double[]
                {0.0, 0.0, 0.0, 0.0, 0.0, 0.0}
        ), MathHelper.makeFill(6, 6, 0.0));
        this.robot = Optional.empty();
    }

    /// @brief       オブザーバの状態更新
    /// @param vx    制御入力 (x 軸方向の速度)
    /// @param vy    制御入力 (y 軸方向の速度)
    /// @return      オブザーバを通したロボットの情報
    synchronized public void updateObserver(@Nonnull Object... args) {
        if (args.length != 3)
            throw new IllegalArgumentException("args.length=" + args.length + " is not 3");
        for (int i = 0; i < 3; i++) {
            if (args[i] instanceof Double)
                throw new IllegalArgumentException("args[" + i + "] is not an instance of Double");
        }
        double vx = (double)args[0];
        double vy = (double)args[1];
        double omega = (double)args[2];
        this.targetV.addLast(MathHelper.applyRotation(new Vector3D(vx, vy, omega), -this.estimation.getState().getEntry(2, 0), MathHelper.AXIS_Z));
        this.targetV.removeFirst();

        // 受信間隔 [s]
        double receptionInterval = this.receiveTime - this.prevTime;

        // 非常に短い間隔で受信していたら直前の値を返す
        // (ゼロ除算の原因になるので)
        if (MathHelper.isEpsilon(receptionInterval)) {
            this.write(this.robot);
            return;
        }

        // 前回受信時から制御入力が切り替わるまでを予測
        double dtLast = Math.min(this.modDelay, receptionInterval);
        if (!MathHelper.isEpsilon(dtLast)) {
            RealMatrix input = toInput(this.estimation.getState(), this.targetV.get(0), dtLast);
            NonLinerSystem.State stateModel = makeStateModel(dtLast);
            RealMatrix jacobian = getStateJacobian(this.estimation.getState(), input, dtLast);
            this.estimation = ExtendedKalman.predict(this.estimation, input, stateModel, jacobian);
        }

        // 制御入力が切り替わってから現在までを予測
        double dtNow = receptionInterval - dtLast;
        if (!MathHelper.isEpsilon(dtNow)) {
            RealMatrix input    = toInput(this.estimation.getState(), this.targetV.get(1), this.controlCycle);
            NonLinerSystem.State stateModel  = makeStateModel(dtNow);
            RealMatrix jacobian = getStateJacobian(this.estimation.getState(), input, dtNow);
            this.estimation = ExtendedKalman.predict(this.estimation, input, stateModel, jacobian);
        }

        // 観測値を持っているか
        if (this.rawValue.isPresent()) {
            double theta = this.rawValue.get().getTheta();
            // 角度差が180度を超えないように修正
            theta = this.estimation.getState().getEntry(2, 0) +
                    MathHelper.wrapPI(theta - this.estimation.getState().getEntry(2, 0));

            // 補正
            this.estimation = LinerKalman.correct(this.estimation, MathHelper.makeVectorMatrix(new double[] {
                    this.rawValue.get().getX(),
                    this.rawValue.get().getY(),
                    theta
            }), this.obsModel);
            this.prevObserveTime = this.receiveTime;
        }

        // ロスト
        if (this.receiveTime - this.prevObserveTime > this.lostDuration) {
            this.prevTime = 0.0;
            this.prevObserveTime = 0.0;
            this.robot = Optional.empty();
            this.write(this.robot);
            return;
        }

        // 時間更新
        this.prevTime = this.receiveTime;

        // 正規化
        this.estimation.getState().setEntry(2, 0,
                MathHelper.wrapPI(this.estimation.getState().getEntry(2, 0)));

        // 結果
        RealMatrix s = this.estimation.getState();
        double x = s.getEntry(0, 0);
        double y = s.getEntry(1, 0);
        double theta = s.getEntry(2, 0);
        Vector3D vField = MathHelper.applyRotation(new Vector3D(s.getEntry(3, 0),
                        s.getEntry(4, 0), s.getEntry(5, 0)),
                theta, MathHelper.AXIS_Z);
        FilteredRobot r = new FilteredRobot();
        r.setX(x);
        r.setY(y);
        r.setTheta(theta);
        r.setVx(vField.getX());
        r.setVy(vField.getY());
        r.setOmega(vField.getZ());
        // 予測関数の設定
        double modDelayLocal = this.modDelay;
        double controlCycleLocal = this.controlCycle;
        List<Vector3D> targetVLocal = new ArrayList<>();
        for (Vector3D vec : this.targetV) {
            targetVLocal.addLast(new Vector3D(vec.getX(), vec.getY(), vec.getZ()));
        }
        r.setEstimator(new IFuncParam2<Optional<FilteredRobot>, FilteredRobot, Double>() {
            @Override
            public Optional<FilteredRobot> function(FilteredRobot filteredRobot, Double t) {
                if (MathHelper.isEpsilon(Math.abs(t))) {
                    return Optional.of(r);
                }
                Vector3D velRobot = MathHelper.applyRotation(new Vector3D(r.getVx(), r.getVy(), r.getOmega()),
                        -r.getTheta(), MathHelper.AXIS_Z);
                RealMatrix s = MathHelper.makeVectorMatrix(new double[] {
                        r.getX(),
                        r.getY(),
                        r.getTheta(),
                        velRobot.getX(),
                        velRobot.getY(),
                        velRobot.getZ()
                });
                // どこまで推定したか
                double estimatedTime = 0.0;
                double dt0 = Math.min(modDelayLocal, t);
                // 前回受信時から制御入力が切り替わるまでを予測
                if (!MathHelper.isEpsilon(dt0)) {
                    s = getNextState(s, toInput(s, targetVLocal.get(0), dt0), dt0);
                    estimatedTime += dt0;
                }
                // 既知の制御入力を使って予測
                for (int i = 0; i < targetVLocal.size() && estimatedTime < t; i++) {
                    double deltaT = Math.min(controlCycleLocal, t - estimatedTime);
                    s = getNextState(s, toInput(s, targetVLocal.get(i), controlCycleLocal), deltaT);
                    estimatedTime += deltaT;
                }
                double x = s.getEntry(0, 0);
                double y = s.getEntry(1, 0);
                double theta = s.getEntry(2, 0);
                double vx = s.getEntry(3, 0);
                double vy = s.getEntry(4, 0);
                double omega = s.getEntry(5, 0);

                // 惰性で動いていると仮定
                // 正確に予測するため、積分をつかって計算
                double dt = t - estimatedTime;
                if (dt >= 0) {
                    // x, y
                    Vector2D vel2d = new Vector2D(vx, vy);
                    if (MathHelper.isEpsilon(Math.abs(omega))) {
                        // 回転がなければそのままフィールド基準に変換して足し合わせる
                        Vector2D dPos2d = MathHelper.applyRotation2D(vel2d, theta).scalarMultiply(dt);
                        x += dPos2d.getX();
                        y += dPos2d.getY();
                    } else {
                        // 回転があれば回転行列の積分
                        // ∫{Rot(ωt)}dt=(Rot(ωt-π/2))/ω
                        Vector2D integral = (MathHelper.applyRotation2D(vel2d, dt * omega - MathHelper.HALF_PI)
                                .subtract(MathHelper.applyRotation2D(vel2d, -MathHelper.HALF_PI)))
                                .scalarMultiply(1.0 / omega);
                        // フィールド基準に変換してから足す
                        Vector2D dPos2d = MathHelper.applyRotation2D(integral, theta);
                        x += dPos2d.getX();
                        y += dPos2d.getY();
                    }

                    // θ
                    theta += dt * omega;
                }

                // 正規化
                theta = MathHelper.wrapPI(theta);

                // フィールド基準の速度
                Vector3D vField = MathHelper.applyRotation(new Vector3D(vx, vy, omega), theta, MathHelper.AXIS_Z);
                // 返り値
                FilteredRobot result = new FilteredRobot();
                result.setX(x);
                result.setY(y);
                result.setTheta(theta);
                result.setVx(vField.getX());
                result.setVy(vField.getY());
                result.setOmega(vField.getZ());
                return Optional.of(result);
            }
        });

        this.robot = r.getStateAfter(0.075);
        this.write(this.robot);
    }

    synchronized public Optional<FilteredRobot> updateRaw(Optional<RawRobot> value,
                       double updateTime) {
        // 初期化時
        if (this.prevTime == 0.0 && value.isPresent()) {
            FilteredRobot robot = new FilteredRobot(value.get());
            // 観測値で初期化
            this.robot = Optional.of(robot);
            RealMatrix state = MathHelper.makeVectorMatrix(new double[] {
                    robot.getX(),
                    robot.getY(),
                    robot.getTheta(),
                    0.0,
                    0.0,
                    0.0
            });
            RealMatrix covariance = MathHelper.makeDiagonal(new double[] {
                    Probability.toVariance(3000.0),
                    Probability.toVariance(3000.0),
                    Probability.toVariance(MathHelper.PI),
                    Probability.toVariance(3000.0),
                    Probability.toVariance(3000.0),
                    Probability.toVariance(MathHelper.PI)
            });
            this.estimation = new Estimation(6, state, covariance);
            this.write(Optional.of(robot));
            this.prevTime         = updateTime;
            this.prevObserveTime = updateTime;
            // 最初は制御入力なし
            this.targetV.clear();
            for (int i = 0; i < this.bufferSize; i++) {
                this.targetV.addLast(Vector3D.ZERO);
            }
        }
        this.receiveTime = updateTime;
        this.rawValue = value;
        return Optional.empty();
    }

    /// @brief 次の状態を予測する
    /// @param s        現在の状態
    /// @param input    制御入力
    /// @param dt       現在から次までの時間
    @Nonnull
    private static RealMatrix getNextState(@Nonnull RealMatrix s,
                                        @Nonnull RealMatrix input, double dt) {
        // v
        Vector3D v = new Vector3D(s.getEntry(3, 0), s.getEntry(4, 0), s.getEntry(5, 0));
        double x = s.getEntry(0, 0);
        double y = s.getEntry(1, 0);
        double theta = s.getEntry(2, 0);
        Vector3D r = new Vector3D(x, y, theta);
        Vector3D ir = new Vector3D(input.getEntry(0, 0), input.getEntry(1, 0), input.getEntry(2, 0));
        Vector3D iv = new Vector3D(input.getEntry(3, 0), input.getEntry(4, 0), input.getEntry(5, 0));
        // θ+dθ=θ+dt*ω+α*dt^2/2
        double nextTheta = theta + dt * v.getZ() + 0.5 * dt * dt * ir.getZ();
        Vector3D nextR = r.add(MathHelper.applyRotation(v.scalarMultiply(0.5 * dt),
                theta, MathHelper.AXIS_Z)).add(MathHelper.applyRotation((v.add(ir.scalarMultiply(dt))).scalarMultiply(0.5 * dt),
                nextTheta, MathHelper.AXIS_Z));
        Vector3D nextV = v.add(iv.scalarMultiply(dt));
        return MathHelper.makeVectorMatrix(new double[] {
                nextR.getX(),
                nextR.getY(),
                nextR.getZ(),
                nextV.getX(),
                nextV.getY(),
                nextV.getZ(),
        });
    }

    /// @brief 状態空間モデルを作成する
    /// @param dt       現在から次までの時間 [s]
    @Nonnull
    private static NonLinerSystem.State makeStateModel(double dt) {
        IFuncParam2<RealMatrix, RealMatrix, RealMatrix> transitionFunc = new IFuncParam2<RealMatrix, RealMatrix, RealMatrix>() {
            @Override
            @Nonnull
            public RealMatrix function(RealMatrix realMatrix, RealMatrix realMatrix2) {
                return getNextState(realMatrix, realMatrix2, dt);
            }
        };
        // システムモデルの加速度誤差の標準偏差[mm/(s^2)]
        double sigma = 3000.0;
        double sigmaAlpha = Math.toRadians(3000.0);
        RealMatrix covariance = Probability.toCovarianceMatrix(MathHelper.makeVectorMatrix(new double[] {
                0.5 * sigma * dt * dt,
                0.5 * sigma * dt * dt,
                0.5 * sigmaAlpha * dt * dt, // (1/2)*σ*dt^2
                sigma * dt,
                sigma * dt,
                sigmaAlpha * dt // σ*dt
        }), 3);
        return new NonLinerSystem.State(6, transitionFunc, covariance);
    }

    /// @brief 状態空間モデルのヤコビアンを計算する
    /// @param s        現在の状態
    /// @param input    制御入力
    /// @param dt       現在から次までの時間 [s]
    @Nonnull
    private static RealMatrix getStateJacobian(@Nonnull RealMatrix s,
                                            @Nonnull RealMatrix input, double dt) {
        // v
        Vector3D v = new Vector3D(s.getEntry(3, 0), s.getEntry(4, 0), s.getEntry(5, 0));
        double x = s.getEntry(0, 0);
        double y = s.getEntry(1, 0);
        double theta = s.getEntry(2, 0);
        Vector3D r = new Vector3D(x, y, theta);
        Vector3D ir = new Vector3D(input.getEntry(0, 0), input.getEntry(1, 0), input.getEntry(2, 0));
        // θ+dθ=θ+dt*ω+α*dt^2/2
        double nextTheta = theta + dt * v.getZ() + 0.5 * dt * dt * ir.getZ();
        // I  dt/2*{Rot(θ) + Rot(θ+dθ)}
        // O                        I
        RealMatrix result = MathHelper.makeIdentity(6, 6);
        RealMatrix rot0 = MatrixUtils.createRealMatrix(new Rotation(MathHelper.AXIS_Z, theta, RotationConvention.VECTOR_OPERATOR).getMatrix());
        RealMatrix rot1 = MatrixUtils.createRealMatrix(new Rotation(MathHelper.AXIS_Z, nextTheta,
                RotationConvention.VECTOR_OPERATOR).getMatrix());
        RealMatrix rotTerm = (rot0.add(rot1)).scalarMultiply(0.5 * dt);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                result.setEntry(i, j + 3, dt * rotTerm.getEntry(i, j));
            }
        }
        // 回転行列の微分による項
        // t^2/2*Rot(θ+dt*ω+α*dt^2/2+π/2)*(v_xy+dt*a_xy)
        Vector3D vec3d = MathHelper.applyRotation((r.add(ir.scalarMultiply(dt))).scalarMultiply(0.5 * dt * dt),
                nextTheta + MathHelper.HALF_PI, MathHelper.AXIS_Z);
        result.setEntry(0, 5, vec3d.getX());
        result.setEntry(1, 5, vec3d.getY());
        return result;
    }

    // 制御入力を計算する
    /// @param s        現在の状態
    /// @param targetV        目標速度
    /// @param dt       現在から目標速度までにかかる時間 [s]
    @Nonnull
    private static RealMatrix toInput(@Nonnull RealMatrix s, @Nonnull Vector3D targetV, double dt) {
        Vector3D stateV = new Vector3D(s.getEntry(3, 0), s.getEntry(4, 0),
                s.getEntry(5, 0));
        return MathHelper.makeVectorMatrix(new double[] {
                (targetV.getX() - stateV.getX()) / dt,
                (targetV.getY() - stateV.getY()) / dt,
                (targetV.getZ() - stateV.getZ()) / dt,
                (targetV.getX() - stateV.getX()) / dt,
                (targetV.getY() - stateV.getY()) / dt,
                (targetV.getZ() - stateV.getZ()) / dt
        });
    }
}
