package ai_server_cafe.filter.kalman;

import ai_server_cafe.filter.AbstractFilterSame;
import ai_server_cafe.filter.kalman.detail.Estimation;
import ai_server_cafe.filter.kalman.detail.LinerKalman;
import ai_server_cafe.filter.kalman.detail.LinerSystem;
import ai_server_cafe.filter.kalman.detail.Probability;
import ai_server_cafe.model.FilteredBall;
import ai_server_cafe.model.RawBall;
import ai_server_cafe.util.interfaces.IFuncParam2;
import ai_server_cafe.util.math.MathHelper;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.linear.RealMatrix;

import javax.annotation.Nonnull;
import java.util.Optional;

public class FilterBall extends AbstractFilterSame<FilteredBall, RawBall>  {
    // ボールの半径[m]
    public static final double BALL_RADIUS = 42.67 / 2000;
    // ボールの断面積 [m^2]
    public static final double BALL_CROSS_SECTIONAL = MathHelper.PI * BALL_RADIUS * BALL_RADIUS;
    // ボールの重さ[kg]
    public static final double BALL_WEIGHT = 45.93 / 1000;
    // 床とボールの摩擦係数
    public static final double FRIC_COEF = 0.04;
    // 重力加速度[m・s^-2]
    public static final double GRAVITY = 9.8;
    // 空気の密度[kg/(m^3)]
    public static final double AIR_DENSITY = 1.2041;
    // 空気の粘度[Pa・s]
    public static final double AIR_VISCOSITY = 1.822e-5;

    // 前回呼び出された時刻
    private double prevTime;
    // 前回観測値を使った時間
    private double prevObserveTime;
    // ロストから復帰した時刻
    private double appearTime;
    // 見えなくなってからロストさせるまでの時間
    private final double lostDuration;

    // 観測空間モデル
    private final LinerSystem.Observation obsModel;
    // 前回の値
    private Optional<FilteredBall> ball;
    private Estimation estimation;

    public FilterBall(double lostDuration) {
        this.lostDuration = lostDuration;
        this.prevTime = 0.0;
        this.prevObserveTime = 0.0;
        this.appearTime = 0.0;
        this.obsModel = new LinerSystem.Observation(4, 2, MathHelper.makeIdentity(2, 4),
                MathHelper.makeIdentity(2, 2).scalarMultiply(Probability.toVariance(5.0)));
        this.ball = Optional.empty();
        this.estimation = new Estimation(4, MathHelper.makeVectorMatrix(new double[]
                {0.0, 0.0, 0.0, 0.0}
        ), MathHelper.makeFill(4, 4, 0.0));
    }

    @Override
    public Optional<FilteredBall> updateRaw(Optional<RawBall> rawValue, double updateTime) {
        // 初期化前
        if (this.prevTime == 0.0) {
            if (!rawValue.isPresent() || this.appearTime == 0.0) {
                this.appearTime = updateTime;
            }
            if (rawValue.isPresent() && updateTime - this.appearTime > 0.05) {
                this.prevTime         = updateTime;
                this.prevObserveTime = updateTime;
                // 観測値で初期化
                this.ball = Optional.of(new FilteredBall(rawValue.get()));
                RealMatrix state = MathHelper.makeVectorMatrix(new double[] {
                        this.ball.get().position().getX(),
                        this.ball.get().position().getY(),
                        0.0,
                        0.0
                });
                // 初期値の誤差[mm] は大きめに
                RealMatrix covariance = MathHelper.makeIdentity(4, 4)
                        .scalarMultiply(Probability.toVariance(3000.0));
                this.estimation = new Estimation(4, state, covariance);
            }
            return this.ball;
        }

        // 前回呼び出しからの経過時刻[s]
        final double dt = updateTime - this.prevTime;
        // 非常に短い間隔でupdateが呼び出されたら直前の値を返す
        // (ゼロ除算の原因になるので)
        if (MathHelper.isEpsilon(dt)) {
            return this.ball;
        }

        // 状態空間モデル

        // I  dt*I
        // O     I
        RealMatrix t = MathHelper.makeIdentity(4, 4);
        t.setEntry(0, 2, dt);
        t.setEntry(1, 3, dt);
        // 0.5*dt^2*I     O
        //          O  dt*I
        RealMatrix i = MathHelper.makeDiagonal(new double[] {
                0.5 * dt * dt,
                0.5 * dt * dt,
                dt,
                dt
        });
        // システムモデルの加速度誤差の標準偏差[mm/(s^2)]
        final double sigma = 4000.0;
        RealMatrix c = Probability.toCovarianceMatrix(
                MathHelper.makeVectorMatrix(new double[] {
                        0.5 * sigma * dt * dt, // (1/2)*σ*dt^2
                        0.5 * sigma * dt * dt,
                        sigma * dt, //         σ*dt
                        sigma * dt
                }), 2
        );
        LinerSystem.State stateModel = new LinerSystem.State(4, t, i , c);
        Vector2D vecA = calcA(new Vector2D(
                this.estimation.getState().getEntry(2, 0),
                this.estimation.getState().getEntry(3, 0)), dt);
        // 推定
        this.estimation = LinerKalman.predict(
                this.estimation, MathHelper.makeVectorMatrix(new double[] {
                        vecA.getX(),
                        vecA.getY(),
                        vecA.getX(),
                        vecA.getY()
                }),
                stateModel);

        // 観測値を持っているか
        if (rawValue.isPresent()) {
            Vector2D ballP = new Vector2D(rawValue.get().getX(), rawValue.get().getY());
            RealMatrix ballPMatrix = MathHelper.makeVectorMatrix(new double[] {ballP.getX(), ballP.getY()});

            if ( // 観測値が信頼できるか
                    LinerKalman.canTrust(ballPMatrix,
                            this.estimation, this.obsModel, 0.995) ||
                    // 補正なしになってから時間が経っているか
                    updateTime - this.prevObserveTime > 0.05) {
                // 補正
                this.estimation        = LinerKalman.correct(this.estimation, ballPMatrix, this.obsModel);
                this.prevObserveTime = updateTime;
            }
        }

        // ロスト
        if (updateTime - this.prevObserveTime > this.lostDuration) {
            this.ball              = Optional.empty();
            this.prevTime         = 0.0;
            this.prevObserveTime = 0.0;
            return this.ball;
        }

        // 時間更新
        this.prevTime = updateTime;

        // 戻り値
        FilteredBall b = new FilteredBall();

        // 推定値を入れる
        RealMatrix state = this.estimation.getState();
        b.setX(state.getEntry(0, 0));
        b.setY(state.getEntry(1, 0));
        b.setVx(state.getEntry(2, 0));
        b.setVy(state.getEntry(3, 0));
        // 予測関数を設定
        b.setEstimator(getEstimator());
        this.ball = b.getStateAfter(0.080);
        return this.ball;
    }

    @Nonnull
    public static Vector2D calcA(@Nonnull Vector2D ballV, double dt) {
        double v = ballV.getNorm();
        double cd = calcDragCoefficient(v);
        // 速度がゼロになるように加速度を設定する
        if (cd == Double.POSITIVE_INFINITY)
            return ballV.scalarMultiply(- 1.0 / dt);
        // 空気抵抗 [N]
        double airResistance =
                0.5 * AIR_DENSITY * BALL_CROSS_SECTIONAL * cd * (1.0e-3 * v) * (1.0e-3 * v);
        // 摩擦による加速度 -μ*g[mm・s^-2]
        double aFriction = -FRIC_COEF * GRAVITY * 1000.0;
        // 空気抵抗による加速度 -Fd/m [mm・s^-2]
        double aAir = -airResistance / BALL_WEIGHT * 1000.0;
        // ボールに加わる加速度 [mm・s^-2]
        double a = aFriction + aAir;
        if (a >= -v / dt) {
            return ballV.normalize().scalarMultiply(a);
        } else {
            // 速度がゼロになるように加速度を設定する
            return ballV.scalarMultiply(- 1.0 / dt);
        }
    }

    public static double calcDragCoefficient(double v) {
        // 特性長さ [m]
        final double l = 2.0 * BALL_RADIUS;
        // レイノルズ数 Re
        final double re = (AIR_DENSITY * l / AIR_VISCOSITY) * (1.0e-3 * v);
        if (MathHelper.isEpsilon(re)) {
            return Double.POSITIVE_INFINITY;
        }

        // (参考: Morrison, Faith A. An Introduction to Fluid Mechanics . Cambridge University
        // Press, 2013.)
        return 24.0 / re + 2.6 * (re / 5.0) / (1.0 + Math.pow(re / 5.0, 1.52)) +
                0.411 * Math.pow(re / 2.63e5, -7.94) / (1 + Math.pow(re / 2.63e5, -8)) +
                0.25 * (re / 1.0e6) / (1.0 + re / 1.0e6);
    }

    @Nonnull
    public static IFuncParam2<Optional<FilteredBall>, FilteredBall, Double> getEstimator() {
        return new IFuncParam2<Optional<FilteredBall>, FilteredBall, Double>() {
            @Override
            public Optional<FilteredBall> function(FilteredBall fb, Double t) {
                return estimate(fb, t);
            }
        };
    }

    @Nonnull
    public static Optional<FilteredBall> estimate(@Nonnull FilteredBall b,
                                               double t) {
        // 初期速度 [mm/s] = 現在値
        Vector2D v0 = b.velocity();
        // 速度ゼロなら現在値を返す
        // (ゼロ除算の原因になるので)
        if (MathHelper.isEpsilon(v0.getNorm())) {
            return Optional.of(b);
        }

        // 抗力係数 Cd
        double cd = calcDragCoefficient(v0.getNorm());
        // 空気抵抗の係数
        if (cd == Double.POSITIVE_INFINITY)
            return Optional.of(b);
        double airCoeff = 0.5 / BALL_WEIGHT * AIR_DENSITY * BALL_CROSS_SECTIONAL * cd;
        // 転がり抵抗による加速度 [m/s]
        double aFriction = FRIC_COEF * GRAVITY;

        double theta0 = Math.atan(Math.sqrt(airCoeff / aFriction) * 1.0e-3 * v0.getNorm());
        double theta  = Math.max(-Math.sqrt(airCoeff * aFriction) * t + theta0, 0.0);

        // 速度 [mm/s]
        Vector2D v = v0.normalize().scalarMultiply(Math.sqrt(aFriction / airCoeff) * Math.tan(theta) * 1000.0);
        // 位置 [mm]
        Vector2D p =
                b.position().add(v0.normalize().scalarMultiply(Math.log(Math.cos(theta) / Math.cos(theta0)) / airCoeff * 1000.0));
        FilteredBall result = new FilteredBall();
        result.setX(p.getX());
        result.setY(p.getY());
        result.setVx(v.getX());
        result.setVy(v.getY());
        return Optional.of(result);
    };
}
