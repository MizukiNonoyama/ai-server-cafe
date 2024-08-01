package ai_server_cafe.updater;

import ai_server_cafe.model.Field;
import ai_server_cafe.network.proto.ssl.vision.VisionWrapper;
import ai_server_cafe.util.TimeHelper;

public final class WorldUpdater {
    private static WorldUpdater instance = null;

    private Object ob;
    private boolean flagGeometry;
    private Field field;
    private double systemTime;
    private int visionPerSec;

    private WorldUpdater() {
        this.flagGeometry = false;
        this.field = new Field();
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
        }
    }

    synchronized public boolean hasGeometry() {
        return this.flagGeometry;
    }

    synchronized public Field getField() {
        return this.field;
    }

    synchronized public int getVisionPerSec() {
        return this.visionPerSec;
    }
}
