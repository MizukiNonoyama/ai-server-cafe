package ai_server_cafe.filter.kalman.detail;

import org.apache.commons.math3.linear.RealMatrix;

import javax.annotation.Nonnull;

public class LinerSystem {
    public static class State {
        private final RealMatrix transition;
        private final RealMatrix input;
        private final RealMatrix covariance;
        private final int stateDim;

        public State(int stateDim, @Nonnull RealMatrix transition, @Nonnull RealMatrix input, @Nonnull RealMatrix covariance) {
            assert(transition.getRowDimension() == stateDim);
            assert(transition.getRowDimension() == transition.getColumnDimension());
            assert(input.getRowDimension() == input.getColumnDimension());
            assert(covariance.getRowDimension() == covariance.getColumnDimension());
            assert(transition.getRowDimension() == input.getRowDimension());
            assert(transition.getRowDimension() == covariance.getRowDimension());
            this.transition = transition;
            this.input = input;
            this.covariance = covariance;
            this.stateDim = stateDim;
        }

        public RealMatrix getTransition() {
            return this.transition;
        }

        public RealMatrix getInput() {
            return this.input;
        }

        public RealMatrix getCovariance() {
            return this.covariance;
        }

        public int getStateDim() {
            return this.stateDim;
        }
    }

    public static class Observation {
        private final RealMatrix mapping;
        private final RealMatrix covariance;
        private final int stateDim;
        private final int obsDim;

        public Observation(int stateDim, int obsDim, @Nonnull RealMatrix mapping, @Nonnull RealMatrix covariance) {
            assert(mapping.getColumnDimension() == stateDim);
            assert(mapping.getRowDimension() == obsDim);
            assert(covariance.getRowDimension() == covariance.getColumnDimension());
            assert(covariance.getRowDimension() == obsDim);
            this.mapping = mapping;
            this.covariance = covariance;
            this.stateDim = stateDim;
            this.obsDim = obsDim;
        }

        public RealMatrix getMapping() {
            return this.mapping;
        }

        public RealMatrix getCovariance() {
            return this.covariance;
        }

        public int getStateDim() {
            return this.stateDim;
        }

        public int getObsDim() {
            return this.obsDim;
        }
    }
}
