package ai_server_cafe.filter.kalman;

import ai_server_cafe.filter.AbstractFilterSame;
import ai_server_cafe.filter.kalman.detail.*;
import ai_server_cafe.model.FilteredRobot;
import ai_server_cafe.model.RawRobot;
import ai_server_cafe.util.interfaces.IFunction;
import ai_server_cafe.util.math.MathHelper;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationConvention;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import javax.annotation.Nonnull;
import java.util.Optional;

public class FilterFreeRobot extends AbstractFilterSame<FilteredRobot, RawRobot> {
    private final double lostDuration;
    private double prevTime;
    private double prevObserveTime;
    // 更新がなければ前回の値
    private Optional<FilteredRobot> robot;
    private final LinerSystem.Observation observation;
    private Estimation estimation;

    public FilterFreeRobot(double lostDuration) {
        this.lostDuration = lostDuration;
        this.observation = new LinerSystem.Observation(6, 3, MathHelper.makeIdentity(6, 3),
                MathHelper.makeDiagonal(new double[] {Probability.toVariance(5.0), Probability.toVariance(5.0),
                Probability.toVariance(Math.toRadians(5.0))}));
        this.prevTime = 0.0;
        this.prevObserveTime = 0.0;
    }

    @Nonnull
    public static NonLinerSystem.State makeStateModel(double dt) {
        IFunction<RealMatrix> transitionFunc = new IFunction<RealMatrix>() {
            @Override
            @Nonnull
            public RealMatrix function(@Nonnull Object... args) {
                assert(args.length > 0);
                assert(args[0] instanceof RealMatrix);
                assert(((RealMatrix)args[0]).getColumnDimension() == 1);
                assert(((RealMatrix)args[0]).getRowDimension() == 6);
                RealMatrix state = (RealMatrix)args[0];
                final double[] stateData = state.getColumn(0);
                Vector3D r = new Vector3D(stateData[0], stateData[1], stateData[2]);
                Vector3D v = new Vector3D(stateData[3], stateData[4], stateData[5]);
                double nextTheta = r.getZ() + dt * v.getZ();
                Rotation rot = new Rotation(MathHelper.AXIS_Z, nextTheta, RotationConvention.VECTOR_OPERATOR);
                r = r.add(rot.applyTo(v.scalarMultiply(dt)));
                return MathHelper.makeVectorMatrix(new double[] {r.getX(), r.getY(), r.getZ(), v.getX(), v.getY(), v.getZ()});
            }
        };
        // システムモデルの加速度誤差の標準偏差[mm/(s^2)]
        double sigma = 6000.0;
        // システムモデルの加速度誤差の標準偏差[rad/(s^2)]
        double sigmaAlpha = 6000.0 * Math.toRadians(1.0);
        RealMatrix covariance = Probability.toCovarianceMatrix(MathHelper.makeVectorMatrix(new double[] {
                0.5 * sigma * dt * dt, // (1/2)*σ*dt^2
                0.5 * sigma * dt * dt,
                0.5 * sigmaAlpha * dt * dt,  // (1/2)*σ*dt^2
                sigma * dt, // σ*dt
                sigma * dt, // σ*dt
                sigmaAlpha * dt
        }),3);
        return new NonLinerSystem.State(covariance.getRowDimension(), transitionFunc, covariance);
    }

    @Nonnull
    public static RealMatrix getStateJacobian(@Nonnull RealMatrix state, double dt) {
        assert(state.getRowDimension() == 6);
        assert(state.getColumnDimension() == 1);
        final double[] stateData = state.getColumn(0);
        Vector3D r = new Vector3D(new double[] {stateData[0], stateData[1], stateData[2]});
        // v
        Vector3D v = new Vector3D(new double[] {stateData[3], stateData[4], stateData[5]});
        RealMatrix result = MathHelper.makeIdentity(state.getRowDimension(), state.getRowDimension());
        // θ+dθ=θ+dt*ω
        double nextTheta = r.getZ() + dt * v.getZ();
        Rotation rot = new Rotation(MathHelper.AXIS_Z, nextTheta, RotationConvention.VECTOR_OPERATOR);
        RealMatrix rotMatrix = MatrixUtils.createRealMatrix(rot.getMatrix());
        // I  dt*Rot(θ+dt*ω)
        // O               I
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                result.setEntry(i, j + 3, dt * rotMatrix.getEntry(i, j));
            }
        }
        Rotation rot1 = new Rotation(MathHelper.AXIS_Z, nextTheta + MathHelper.HALF_PI, RotationConvention.VECTOR_OPERATOR);
        // 回転行列の微分による項
        // d(p_xy+Rot(θ+dt*ω)*dt*v_xy)/dω=dt^2*Rot(θ+dt*ω+π/2)*v_xy
        Vector3D rotated = rot1.applyTo(v.scalarMultiply(dt * dt));
        result.setEntry(0, 5, rotated.getX());
        result.setEntry(1, 5, rotated.getY());
        return result;
    }

    @Override
    public Optional<FilteredRobot> updateRaw(Optional<RawRobot> rawValue, double updateTime) {
        // 初期化前
        if (this.prevTime == 0.0) {
            if (rawValue.isPresent()) {
                this.prevTime        = updateTime;
                this.prevObserveTime = updateTime;

                // 観測値で初期化
                this.robot = Optional.of(new FilteredRobot(rawValue.get()));
                RealMatrix state = MathHelper.makeVectorMatrix(new double[] {
                        rawValue.get().getX(),
                        rawValue.get().getY(),
                        rawValue.get().getTheta(),
                        0.0, 0.0, 0.0
                });
                // 初期値の誤差[mm] は大きめに
                RealMatrix covariance = MathHelper.makeDiagonal(new double[] {
                        Probability.toVariance(3000.0),
                        Probability.toVariance(3000.0),
                        Probability.toVariance(MathHelper.PI),
                        Probability.toVariance(3000.0),
                        Probability.toVariance(3000.0),
                        Probability.toVariance(MathHelper.PI),
                });

                this.estimation = new Estimation(6, state, covariance);
            }
            return this.robot;
        }

        // 前回呼び出しからの経過時刻[s]
        double dt = updateTime - prevTime;
        // 非常に短い間隔でupdateが呼び出されたら直前の値を返す
        // (ゼロ除算の原因になるので)
        if (MathHelper.isEpsilon(dt)) {
            return this.robot;
        }

        // 推定
        NonLinerSystem.State stateModel  = makeStateModel(dt);
        RealMatrix jacobian = getStateJacobian(this.estimation.getState(), dt);
        // ゼロ入力を仮定
        RealMatrix input = MathHelper.makeFill(6, 1, 0.0);
        this.estimation = ExtendedKalman.predict(this.estimation, input, stateModel, jacobian);

        // 観測値を持っているか
        if (rawValue.isPresent()) {
            Vector3D rawP = new Vector3D(rawValue.get().getX(), rawValue.get().getY(), rawValue.get().getTheta());
            // 角度差が180度を超えないように修正
            Vector3D p = new Vector3D(rawP.getX(), rawP.getY(), this.estimation.getState().getEntry(2,0) +
                    MathHelper.wrapPI(rawP.getZ() - this.estimation.getState().getEntry(2,0)));
            // 補正
            this.estimation        = LinerKalman.correct(this.estimation, MathHelper.makeVectorMatrix(new double[] {p.getX(), p.getY(), p.getZ()}), this.observation);
            this.prevObserveTime = updateTime;
        }

        // ロスト
        if (updateTime - this.prevObserveTime > this.lostDuration) {
            this.robot             = Optional.empty();
            this.prevTime         = 0.0;
            this.prevObserveTime = 0.0;
            return this.robot;
        }

        // 時間更新
        this.prevTime = updateTime;

        // 正規化
        this.estimation.getState().setEntry(2, 0, MathHelper.wrapPI(this.estimation.getState().getEntry(2,0)));
        // 発散を防止するため、角速度を現実的な範囲に抑える
        double omega_max = 10.0 * MathHelper.TWO_PI;
        this.estimation.getState().setEntry(5, 0, Math.clamp(this.estimation.getState().getEntry(5, 0), -omega_max, omega_max));

        // 結果
        double[] s = this.estimation.getState().getColumn(0);
        Vector3D vField = (new Rotation(MathHelper.AXIS_Z, s[2], RotationConvention.VECTOR_OPERATOR)).applyTo(
                new Vector3D(s[3], s[4], s[5])
        );
        FilteredRobot r = getFilteredRobot(s, vField);

        this.robot = r.getStateAfter(0.080);
        return this.robot;
    }

    @Nonnull
    private static FilteredRobot getFilteredRobot(@Nonnull double[] s, @Nonnull Vector3D vField) {
        FilteredRobot r = new FilteredRobot();
        r.setX(s[0]);
        r.setY(s[1]);
        r.setTheta(s[2]);
        r.setVx(vField.getX());
        r.setVy(vField.getY());
        r.setOmega(vField.getZ());
        // 予測関数を設定
        r.setEstimator(new IFunction<Optional<FilteredRobot>>() {
            @Override
            public Optional<FilteredRobot> function(Object... args) {
                assert(args.length > 1);
                assert(args[0] instanceof FilteredRobot);
                assert(args[1] instanceof Double);
                final FilteredRobot fr = (FilteredRobot) args[0];
                final double t = (double) args[1];
                // 初期値を設定
                // 位置: フィールド基準
                Vector3D rawP = fr.positionXYTheta();
                // 速度: ロボット基準
                Vector3D rawV = (new Rotation(MathHelper.AXIS_Z, -fr.getTheta(), RotationConvention.VECTOR_OPERATOR)).applyTo(fr.velocityXYTheta());
                // 惰性で動いていると仮定
                // 正確に予測するため、積分をつかって計算
                // x, y
                double x = rawP.getX();
                double y = rawP.getY();
                double theta = rawP.getZ();
                Vector2D raw2v = new Vector2D(rawV.getX(), rawV.getY());
                if (MathHelper.isEpsilon(rawV.getZ())) {
                    // 回転がなければそのままフィールド基準に変換して足し合わせる
                    x += t * MathHelper.applyRotation2D(raw2v, theta).getX();
                    y += t * MathHelper.applyRotation2D(raw2v, theta).getY();
                } else {
                    // 回転があれば回転行列の積分
                    // ∫{Rot(ωt)}dt=(Rot(ωt-π/2))/ω
                    Vector2D integral = (MathHelper.applyRotation2D(raw2v, t * rawV.getZ() - MathHelper.HALF_PI)
                            .subtract(MathHelper.applyRotation2D(raw2v, -MathHelper.HALF_PI))).scalarMultiply(1.0 / rawV.getZ());
                    // フィールド基準に変換してから足し合わせる
                    x += MathHelper.applyRotation2D(integral, theta).getX();
                    y += MathHelper.applyRotation2D(integral, theta).getY();
                }

                // 角度
                theta += t * rawV.getZ();
                theta = MathHelper.wrapPI(theta);

                // フィールド基準の速度
                Vector3D v_field1 = (new Rotation(MathHelper.AXIS_Z, theta, RotationConvention.VECTOR_OPERATOR)).applyTo(rawV);

                // 返り値
                FilteredRobot result = new FilteredRobot();
                result.setX(x);
                result.setY(y);
                result.setTheta(theta);
                result.setVx(v_field1.getX());
                result.setVy(v_field1.getY());
                result.setOmega(v_field1.getZ());
                return Optional.of(result);
            }
        });
        return r;
    }
}
