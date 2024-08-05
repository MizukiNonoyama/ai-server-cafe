package ai_server_cafe.filter.kalman.detail;

import ai_server_cafe.util.interfaces.IFunction;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import javax.annotation.Nonnull;

public class NonLinerSystem {
    public static class State {
        private final IFunction<RealMatrix> transition;
        private final RealMatrix covariance;
        private final int stateDim;

        public State(int stateDim, IFunction<RealMatrix> transition, @Nonnull RealMatrix covariance) {
            assert (covariance.getRowDimension() == covariance.getColumnDimension());
            assert (covariance.getRowDimension() == stateDim);
            this.transition = transition;
            this.covariance = covariance;
            this.stateDim = stateDim;
        }

        public IFunction<RealMatrix> getTransitionFunc() {
            return this.transition;
        }

        public RealMatrix getCovariance() {
            return this.covariance;
        }

        public int getStateDim() {
            return this.stateDim;
        }
    }
}
