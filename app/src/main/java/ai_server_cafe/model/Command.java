package ai_server_cafe.model;

import ai_server_cafe.util.EnumKickType;
import ai_server_cafe.util.TeamColor;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.util.Pair;

import java.util.Optional;

/**
 * ロボット用コマンド フィールド基準
 */
public class Command {
    protected Optional<Vector2D> targetPos;
    protected Vector2D targetVel;
    protected Optional<Double> targetTheta;
    protected double targetOmega;
    protected int dribble;
    protected Pair<EnumKickType, Integer> kickFlag;

    public Command() {
        this.targetPos = Optional.empty();
        this.targetVel = Vector2D.ZERO;
        this.targetTheta = Optional.empty();
        this.targetOmega = 0.0;
        this.dribble = 0;
        this.kickFlag = new Pair<>(EnumKickType.NONE, 0);
    }

    public Command setTargetPosition(Vector2D targetPos) {
        this.targetPos = Optional.of(targetPos);
        return this;
    }

    public Command setTargetVel(Vector2D targetVel) {
        this.targetVel = targetVel;
        return this;
    }

    public Command setTargetTheta(double theta) {
        this.targetTheta = Optional.of(theta);
        return this;
    }

    public Command setTargetOmega(double omega) {
        this.targetOmega = omega;
        return this;
    }

    public Command setDribble(int dribble) {
        this.dribble = dribble;
        return this;
    }

    public Command setKickFlag(Pair<EnumKickType, Integer> kickFlag) {
        this.kickFlag = kickFlag;
        return this;
    }

    public Command setKickFlag(EnumKickType type, int value) {
        this.kickFlag = new Pair<>(type, value);
        return this;
    }

    public Optional<Vector2D> getTargetPos() {
        return this.targetPos;
    }

    public Vector2D getTargetVel() {
        return this.targetVel;
    }

    public Optional<Double> getTargetTheta() {
        return this.targetTheta;
    }

    public double getTargetOmega() {
        return this.targetOmega;
    }

    public double getDribble() {
        return this.dribble;
    }

    public Pair<EnumKickType, Integer> getKickFlag() {
        return this.kickFlag;
    }

    @Override
    public Command clone() {
        Command command = new Command();
        if (this.targetPos.isPresent()) {
            command.setTargetPosition(new Vector2D(this.targetPos.get().getX(), this.targetPos.get().getY()));
        }
        if (this.targetTheta.isPresent()) {
            command.setTargetTheta(this.targetTheta.get());
        }
        command.setDribble(this.dribble);
        command.setTargetOmega(this.targetOmega);
        command.setKickFlag(this.kickFlag.getKey(), this.kickFlag.getValue());
        command.setTargetVel(new Vector2D(this.targetVel.getX(), this.targetVel.getY()));
        return command;
    }
}
