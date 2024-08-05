package ai_server_cafe.filter.kalman.detail;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import javax.annotation.Nonnull;

public class LinerKalman {
    /// @brief 推定値の共分散行列を観測空間に射影
/// @param est      推定値
/// @param model    観測モデル
    public static RealMatrix mappedEstimatedCovariance(
    @Nonnull Estimation est,
    @Nonnull LinerSystem.Observation model) {
        return model.getMapping().multiply(est.getCovariance()).multiply(model.getMapping().transpose());
    }

    /// @brief 予測した値を返す
/// @param est            推定値
/// @param u              制御入力
/// @param model
// 状態モデル
    @Nonnull
    public static Estimation predict(@Nonnull Estimation est,
             @Nonnull RealMatrix u,
             @Nonnull LinerSystem.State model) {
        // 将来の状態を予測  x_(t|t-1) = A * x_(t-1|t-1) + B * u
        // 誤差共分散を予測  P_(t|t-1) = A * P_(t-1|t-1) * A.transpose() + Q
        assert(u.getColumnDimension() == 1);
        assert(u.getRowDimension() == est.getStateDim());
        assert(model.getStateDim() == est.getStateDim());
        return new Estimation(est.getStateDim(),
                model.getTransition().multiply(est.getState()).add(model.getInput().multiply(u)),
                model.getTransition().multiply(est.getCovariance()).multiply(model.getTransition().transpose()).add(model.getCovariance()));
    }

    /// @brief 観測値で補正をかけた値を返す
/// @param est      推定値
/// @param o        観測値
/// @param model    観測モデル
    @Nonnull
    public static Estimation correct(@Nonnull Estimation est,
             @Nonnull RealMatrix o,
             @Nonnull LinerSystem.Observation model) {
        assert(o.getColumnDimension() == 1);
        assert(o.getRowDimension() == model.getObsDim());
        assert(est.getStateDim() == model.getStateDim());
        // 観測残差    e = z - H * x_(t|t-1)
        final RealMatrix err = o.subtract(model.getMapping().multiply(est.getState()));
        // 観測残差の共分散    S = R + H * P_(t|t-1) * H.transpose()
        final RealMatrix S = model.getCovariance().add(mappedEstimatedCovariance(est, model));
        // カルマンゲイン    K = P_(t|t-1) * H.transpose() * S^(-1)
        final RealMatrix K = est.getCovariance().multiply(model.getMapping().transpose()).multiply(MatrixUtils.inverse(S));

        return new Estimation(est.getStateDim(),// 推定状態    x_(t|t) = x_(t|t-1) + K * e
                est.getState().add(K.multiply(err)),
                // 誤差の共分散    P_(t|t) = (I - K * H) * P_(t|t-1)
                est.getCovariance().subtract(K.multiply(model.getMapping()).multiply(est.getCovariance())));
    }

    /// @brief  観測値が信頼できるか
/// @param o           観測値
/// @param est         推定値
/// @param model       観測モデル
/// @param confidence  観測値を信頼する確率
    public static boolean canTrust(@Nonnull RealMatrix o,
               @Nonnull Estimation est,
               @Nonnull LinerSystem.Observation model,
                   double confidence) {
        assert(o.getColumnDimension() == 1);
        assert(model.getObsDim() == o.getRowDimension());
        assert(est.getStateDim() == model.getStateDim());
        // 観測残差    e = z - H * x_(t|t-1)
        final RealMatrix err = o.subtract(model.getMapping().multiply(est.getState()));

        // マハラノビス距離の二乗値
        // (xp - xz)' * P^(-1) * (xp - xz) = (xp - xz)・(P^(-1) * (xp - xz))
        // 転置を使った実装にすると err が 1x1 列だった場合に戻り地の型がスカラーでなくなってしまう
        // ドット積を使った実装にすることでそれを回避する
        final double squared_eps = err.transpose().multiply(MatrixUtils.inverse(mappedEstimatedCovariance(est, model)).multiply(err)).getEntry(0,0);
        return Probability.canTrust(est.getStateDim(), squared_eps, confidence);
    }

}
