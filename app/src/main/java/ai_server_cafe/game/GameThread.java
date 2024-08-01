package ai_server_cafe.game;

import ai_server_cafe.Main;
import ai_server_cafe.config.ConfigManager;
import ai_server_cafe.network.transmitter.GrSimTransmitter;
import ai_server_cafe.network.transmitter.KIKSTransmitter;
import ai_server_cafe.updater.WorldUpdater;
import ai_server_cafe.util.EnumKickType;
import ai_server_cafe.util.SendCommand;
import ai_server_cafe.util.TeamColor;
import ai_server_cafe.util.TimeHelper;
import ai_server_cafe.util.thread.AbstractLoopThreadCafe;
import org.apache.commons.math3.util.Pair;

import java.util.Arrays;

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
        if (nowDate - lastDate >= ConfigManager.CYCLE) {
            try {
                SendCommand command = new SendCommand(10, TeamColor.BLUE, new Pair<EnumKickType, Integer>(EnumKickType.NONE, 0), 0, 0, 0,0);
                KIKSTransmitter.getInstance().sendCommand(Arrays.asList(new SendCommand[] {command}));
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
}
