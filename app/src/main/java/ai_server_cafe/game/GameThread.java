package ai_server_cafe.game;

import ai_server_cafe.Main;
import ai_server_cafe.config.Config;
import ai_server_cafe.config.ConfigManager;
import ai_server_cafe.model.Command;
import ai_server_cafe.model.IntegratedCommand;
import ai_server_cafe.network.transmitter.RobotDriver;
import ai_server_cafe.util.EnumKickType;
import ai_server_cafe.model.SendCommand;
import ai_server_cafe.util.TeamColor;
import ai_server_cafe.util.TimeHelper;
import ai_server_cafe.util.thread.AbstractLoopThreadCafe;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.List;

public final class GameThread extends AbstractLoopThreadCafe {
    private static GameThread instance = null;
    private double lastDate;
    private double systemStartedTime;

    /**
     * DO NOT USE THIS CONSTRUCTOR!!!
     * USE GameThread#getInstance()
      */
    private GameThread() {
        super("game_runner");
        this.systemStartedTime = 0;
    }

    @Override
    protected void loop() {
        double nowDate = TimeHelper.now();
        ConfigManager configManager = ConfigManager.getInstance();
        Config config = configManager.getConfig();
        if (nowDate - lastDate >= config.getCycleTime()) {
            //this.logger.info(nowDate - this.lastDate);
            try {
                List<SendCommand> commands = new ArrayList<>();
                if (configManager.isStart()) {
                    // Captain etc.
                } else {
                    commands.addAll(getHaltCommands(config.getTeamColor(), config.activeRobots));
                }
                for(SendCommand command : commands)
                    RobotDriver.getInstance().updateCommand(command.getId(), command.getColor(), new Command());
            } catch(Exception e) {
                e.printStackTrace();
                Main.exit(1);
            }
            lastDate = nowDate;
        }
    }

    @Override
    protected void init() {
        this.lastDate = TimeHelper.now();
        this.systemStartedTime = TimeHelper.now();
    }

    public static GameThread getInstance() {
        if (instance == null) {
            instance = new GameThread();
        }
        return instance;
    }

    synchronized public double getSystemStartedTime() {
        return this.systemStartedTime;
    }

    public static List<SendCommand> getHaltCommands(TeamColor color, int[] activeRobots) {
        List<SendCommand> commandList = new ArrayList<>();
        for (int id : activeRobots) {
            commandList.add(new SendCommand(id, color, new Pair<EnumKickType, Integer>(EnumKickType.NONE, 0), 0, 0, 0,0));
        }
        return commandList;
    }
}
