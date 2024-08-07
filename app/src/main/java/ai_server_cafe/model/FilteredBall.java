package ai_server_cafe.model;

import ai_server_cafe.util.interfaces.IFunction;

import javax.annotation.Nonnull;
import java.util.Optional;

public class FilteredBall extends AbstractFilteredObject<RawBall> {
    protected Optional<IFunction<Optional<FilteredBall>>> estimator;

    public FilteredBall() {
        super();
        this.estimator = Optional.empty();
    }

    @Override
    public FilteredBall copy() {
        FilteredBall fb = new FilteredBall();
        fb.estimator = this.estimator;
        fb.lost = this.lost;
        fb.x = this.x;
        fb.y = this.y;
        fb.z = this.z;
        fb.vx = this.vx;
        fb.vy = this.vy;
        fb.vz = this.vz;
        fb.ax = this.ax;
        fb.ay = this.ay;
        fb.az = this.az;
        fb.jx = this.jx;
        fb.jy = this.jy;
        fb.jz = this.jz;
        fb.theta = this.theta;
        fb.omega = this.omega;
        fb.alpha = this.alpha;
        fb.zeta = this.zeta;
        return fb;
    }

    /**
     * @param rawBall
     */
    public FilteredBall(@Nonnull RawBall rawBall) {
        this();
        this.x = rawBall.getX();
        this.y = rawBall.getY();
        this.z = rawBall.getZ();
    }

    @Override
    public RawBall getRaw() {
        return new RawBall(this.x, this.y, this.z);
    }

    public void setEstimator(IFunction<Optional<FilteredBall>> estimator) {
        this.estimator = Optional.of(estimator);
    }

    public boolean hasEstimator() {
        return this.estimator.isPresent();
    }

    public Optional<IFunction<Optional<FilteredBall>>> getEstimator() {
        return this.estimator;
    }

    public Optional<FilteredBall> getStateAfter(double offset) {
        if (this.hasEstimator()) {
            return this.getEstimator().get().function(this, offset);
        }
        return Optional.empty();
    }
}
