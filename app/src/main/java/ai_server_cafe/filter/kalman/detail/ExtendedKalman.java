package ai_server_cafe.filter.kalman.detail;

import org.apache.commons.math3.linear.RealMatrix;

import javax.annotation.Nonnull;

public class ExtendedKalman {
    @Nonnull
    public static Estimation predict(@Nonnull Estimation est,
             @Nonnull RealMatrix u,
             @Nonnull NonLinerSystem.State model,
             @Nonnull RealMatrix jacobian) {
        assert(est.getStateDim() == model.getStateDim());
        assert(est.getStateDim() == u.getRowDimension());
        assert(u.getColumnDimension() == 1);
        assert(jacobian.getRowDimension() == jacobian.getColumnDimension());
        assert(jacobian.getRowDimension() == est.getStateDim());
        return new Estimation(est.getStateDim(),
                // 将来の状態を予測  x_(t|t-1) = f(x_(t-1|t-1), u_k)
                model.getTransitionFunc().function(est.getState(), u),
                // 誤差共分散を予測  P_(t|t-1) = F_k * P_(t-1|t-1) * F_k' + Q_k
                jacobian.multiply(est.getCovariance()).multiply(jacobian.transpose()).add(model.getCovariance()));
    }
}
