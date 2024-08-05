package ai_server_cafe.model;

import ai_server_cafe.util.TeamColor;

public class IntegratedRobot {
    private final Command command;
    private final int id;
    private final TeamColor color;
    private FilteredRobot robot;

    public IntegratedRobot(int id, TeamColor color, FilteredRobot robot, Command command) {
        this.id = id;
        this.color = color;
        this.command = command;
        this.robot = robot;
    }

    public FilteredRobot getRobot() {
        return this.robot;
    }

    public boolean isLost() {
        return false;
    }

    public void setRobot(FilteredRobot robot) {
        this.robot = robot;
    }

    public Command getCommand() {
        return this.command.clone();
    }
}
