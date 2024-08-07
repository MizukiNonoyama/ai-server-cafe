package ai_server_cafe.updater;

import ai_server_cafe.config.ConfigManager;
import ai_server_cafe.filter.kalman.FilterBall;
import ai_server_cafe.gui.registry.RegistryGUIItem;
import ai_server_cafe.model.*;
import ai_server_cafe.network.proto.ssl.vision.VisionDetection;
import ai_server_cafe.network.proto.ssl.vision.VisionWrapper;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public final class UpdaterWorld {
    private static UpdaterWorld instance = null;
    private UpdaterBall updaterBall;
    private Object ob;
    private boolean flagGeometry;
    private Field field;
    private List<RawBall> rawBallList;
    private List<RawRobot> rawRobots;
    private List<FilteredRobot> filteredRobots;
    private List<IntegratedRobot> robots;
    private double systemTime;
    private int visionPerSec;

    private UpdaterWorld() {
        this.flagGeometry = false;
        this.field = new Field();
        this.rawBallList = new ArrayList<>();
        this.updaterBall = new UpdaterBall();
        double lostDuration = ConfigManager.getInstance().getConfig().lostDuration;
        this.updaterBall.setFilterSame(new FilterBall(lostDuration));
    }

    public static UpdaterWorld getInstance() {
        if (instance == null) {
            instance = new UpdaterWorld();
        }
        return instance;
    }

    synchronized public void update(@Nonnull VisionWrapper.Packet packet, int perSec) {
        this.visionPerSec = perSec;
        this.flagGeometry = packet.hasGeometry();
        if (packet.hasGeometry()) {
            this.field.setGameHeight(packet.getGeometry().getField().getFieldWidth());
            this.field.setGameWidth(packet.getGeometry().getField().getFieldLength());
            this.field.setPenaltyWidth(packet.getGeometry().getField().getPenaltyAreaWidth());
            this.field.setPenaltyLength(packet.getGeometry().getField().getPenaltyAreaDepth());
            this.field.setGoalLength(packet.getGeometry().getField().getGoalDepth());
            this.field.setGoalWidth(packet.getGeometry().getField().getGoalWidth());

            RegistryGUIItem.updateFieldStaticGraphicalContents(this.field);
        }

        if (!packet.getDetection().getBallsList().isEmpty()) this.rawBallList.clear();
        for (VisionDetection.Ball detectedBall : packet.getDetection().getBallsList()) {
            this.rawBallList.add(new RawBall(detectedBall.getX(), detectedBall.getY(), detectedBall.getZ()));
        }
        this.updaterBall.update(packet.getDetection());
    }

    synchronized public boolean hasGeometry() {
        return this.flagGeometry;
    }

    synchronized public Field getField() {
        return this.field;
    }

    synchronized public List<RawBall> getRawBallList() {
        return this.rawBallList;
    }

    synchronized public int getVisionPerSec() {
        return this.visionPerSec;
    }

    synchronized public FilteredBall getBall() {
        return this.updaterBall.getValue().copy();
    }
}
