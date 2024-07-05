package ai_server_cafe.game;

import ai_server_cafe.config.Config;
import ai_server_cafe.updater.WorldUpdater;
import ai_server_cafe.util.TimeHelper;
import ai_server_cafe.util.thread.AbstractLoopThreadCafe;

public final class GameThread extends AbstractLoopThreadCafe {
    private static GameThread instance = null;
    private int i;
    private double lastDate;

    /**
     * DO NOT USE THIS CONSTRUCTOR!!!
     * USE GameThread#getInstance()
      */
    private GameThread() {
        super("game_runner");
    }

    @Override
    protected void loop() {
        double nowDate = TimeHelper.now();
        if (nowDate - lastDate >= Config.CYCLE) {

            if(WorldUpdater.getInstance().hasGeometry()) this.logger.info("has geometry");
            i++;
            lastDate = nowDate;
        }

        if(i > 1000) {
            this.terminate();
        }
    }

    @Override
    protected void init() {
        this.i = 0;
        this.lastDate = TimeHelper.now();
    }

    public static GameThread getInstance() {
        if (instance == null) {
            instance = new GameThread();
        }
        return instance;
    }
}
