package ai_server_cafe.model;

import ai_server_cafe.util.interfaces.IFunction;

import javax.annotation.Nonnull;
import java.util.Optional;

public class FilteredRobot extends AbstractFilteredObject<RawRobot> {
    protected Optional<IFunction<Optional<FilteredRobot>>> estimator;

    public FilteredRobot() {
        super();
        this.estimator = Optional.empty();
    }

    @Override
    public FilteredRobot copy() {
        FilteredRobot fr = new FilteredRobot();
        fr.estimator = this.estimator;
        fr.lost = this.lost;
        fr.x = this.x;
        fr.y = this.y;
        fr.z = this.z;
        fr.vx = this.vx;
        fr.vy = this.vy;
        fr.vz = this.vz;
        fr.ax = this.ax;
        fr.ay = this.ay;
        fr.az = this.az;
        fr.jx = this.jx;
        fr.jy = this.jy;
        fr.jz = this.jz;
        fr.theta = this.theta;
        fr.omega = this.omega;
        fr.alpha = this.alpha;
        fr.zeta = this.zeta;
        return fr;
    }

    public FilteredRobot(@Nonnull RawRobot rawRobot) {
        this();
        this.x = rawRobot.getX();
        this.y = rawRobot.getY();
        this.theta = rawRobot.getTheta();
    }

    @Override
    public RawRobot getRaw() {
        return new RawRobot(this.x, this.y, this.theta);
    }


    public void setEstimator(IFunction<Optional<FilteredRobot>> estimator) {
        this.estimator = Optional.of(estimator);
    }

    public boolean hasEstimator() {
        return this.estimator.isPresent();
    }

    public Optional<IFunction<Optional<FilteredRobot>>> getEstimator() {
        return this.estimator;
    }

    public Optional<FilteredRobot> getStateAfter(double offset) {
        if (this.hasEstimator()) {
            return this.getEstimator().get().function(this, offset);
        }
        return Optional.empty();
    }
}
