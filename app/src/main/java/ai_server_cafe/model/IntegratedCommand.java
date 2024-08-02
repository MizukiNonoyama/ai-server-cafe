package ai_server_cafe.model;

import ai_server_cafe.util.TeamColor;

public class IntegratedCommand {
    private final Command command;
    private final int id;
    private final TeamColor color;

    public IntegratedCommand(int id, TeamColor color, Command command) {
        this.id = id;
        this.color = color;
        this.command = command;
    }

    public int getId() {
        return this.id;
    }

    public Command getCommand() {
        return this.command;
    }

    public TeamColor getColor() {
        return this.color;
    }
}
