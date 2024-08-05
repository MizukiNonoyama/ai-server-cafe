package ai_server_cafe.filter.kalman.detail;

import org.apache.commons.math3.linear.RealMatrix;

import javax.annotation.Nonnull;

public class Estimation {
    private final RealMatrix state;
    private final RealMatrix covariance;
    private final int stateDim;

    public Estimation(int stateDim, @Nonnull RealMatrix state, @Nonnull RealMatrix covariance) {
        assert(covariance.getRowDimension() == covariance.getColumnDimension());
        assert(state.getRowDimension() == covariance.getColumnDimension());
        assert(state.getColumnDimension() == 1);
        assert(state.getRowDimension() == stateDim);
        this.state = state;
        this.covariance = covariance;
        this.stateDim = stateDim;
    }

    public RealMatrix getState() {
        return this.state;
    }

    public RealMatrix getCovariance() {
        return this.covariance;
    }

    public int getStateDim() {
        return this.stateDim;
    }
}
