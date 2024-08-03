package ai_server_cafe.network.transmitter;

import ai_server_cafe.config.Config;
import ai_server_cafe.config.ConfigManager;
import ai_server_cafe.model.Command;
import ai_server_cafe.model.IntegratedCommand;
import ai_server_cafe.model.IntegratedRobot;
import ai_server_cafe.util.EnumKickType;
import ai_server_cafe.util.SendCommand;
import ai_server_cafe.util.TeamColor;
import ai_server_cafe.util.TimeHelper;
import ai_server_cafe.util.thread.AbstractLoopThreadCafe;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class RobotDriver extends AbstractLoopThreadCafe {
    private static RobotDriver instance = null;
    private final List<IntegratedCommand> commands;
    private double lastTime;

    private RobotDriver() {
        super("robot driver");
        this.commands = new ArrayList<>();
    }

    public void updateCommand(int id, TeamColor color, Command command) {
        synchronized (this.commands) {
            this.commands.removeIf(command1 -> command1.getColor() == color && command1.getId() == id);
            this.commands.add(new IntegratedCommand(id, color, command));
        }
    }

    @Override
    protected void loop() {
        Config config = ConfigManager.getInstance().getConfig();
        double nowDate = TimeHelper.now();
        if (nowDate - this.lastTime >= config.getSendCycleTime()) {
            synchronized (this.commands) {
                // WorldUpdater -> IntegratedRobot && !isLost && containsActiveRobots
                // Controller
                // SendCommand
                SendCommand sendCommand = new SendCommand(10, TeamColor.BLUE, new Pair<>(EnumKickType.CHIP, 100),5,100,100,0);
                for (AbstractTransmitter at : TransmitterManager.getInstance().getTransmitters()) {
                    at.sendCommand(sendCommand);
                }
            }
            this.lastTime = TimeHelper.now();
        }
    }

    @Override
    protected void init() {
        this.lastTime = TimeHelper.now();
    }

    public static RobotDriver getInstance() {
        if (instance == null) {
            instance = new RobotDriver();
        }
        return instance;
    }
}
