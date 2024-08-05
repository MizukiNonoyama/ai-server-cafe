package ai_server_cafe.updater;

import ai_server_cafe.gui.GuiThread;
import ai_server_cafe.gui.VisionArea;
import ai_server_cafe.gui.interfaces.IGraphicalComponent;
import ai_server_cafe.gui.item.CircleCafe;
import ai_server_cafe.gui.item.LineCafe;
import ai_server_cafe.gui.item.NoneCafe;
import ai_server_cafe.gui.item.RectCafe;
import ai_server_cafe.gui.registry.RegistryGUIItem;
import ai_server_cafe.model.*;
import ai_server_cafe.network.proto.ssl.vision.VisionDetection;
import ai_server_cafe.network.proto.ssl.vision.VisionWrapper;
import ai_server_cafe.util.TimeHelper;
import ai_server_cafe.util.gui.ColorHelper;

import java.util.ArrayList;
import java.util.List;

public final class WorldUpdater {
    private static WorldUpdater instance = null;

    private Object ob;
    private boolean flagGeometry;
    private Field field;
    private List<RawBall> rawBallList;
    private List<RawRobot> rawRobots;
    private List<FilteredRobot> filteredRobots;
    private List<IntegratedRobot> robots;
    private double systemTime;
    private int visionPerSec;

    private WorldUpdater() {
        this.flagGeometry = false;
        this.field = new Field();
        this.rawBallList = new ArrayList<>();
    }

    public static WorldUpdater getInstance() {
        if (instance == null) {
            instance = new WorldUpdater();
        }
        return instance;
    }

    synchronized public void update(VisionWrapper.Packet packet, int perSec) {

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
            this.rawBallList.add(new RawBall(detectedBall.getX(), detectedBall.getY()));
        }
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
}
