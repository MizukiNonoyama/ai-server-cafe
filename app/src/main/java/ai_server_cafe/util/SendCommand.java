package ai_server_cafe.util;

import org.apache.commons.math3.util.Pair;

public class SendCommand {
    private final int id;
    private final TeamColor color;
    private final Pair<EnumKickType, Integer> kickFlag;
    private final int dribble;
    private final double vx;
    private final double vy;
    private final double omega;

    public SendCommand(int id, TeamColor color, Pair<EnumKickType, Integer> kickFlag, int dribble, double vx, double vy, double omega) {
        this.id = id;
        this.color = color;
        this.kickFlag = kickFlag;
        this.dribble = dribble;
        this.vx = vx;
        this.vy = vy;
        this.omega = omega;
    }

    public int getId() {
        return this.id;
    }

    public TeamColor getColor() {
        return this.color;
    }

    public double getVx() {
        return this.vx;
    }

    public double getVy() {
        return this.vy;
    }

    public double getOmega() {
        return this.omega;
    }

    public Pair<EnumKickType, Integer> getKickFlag() {
        return this.kickFlag;
    }

    public int getDribble() {
        return this.dribble;
    }
}
