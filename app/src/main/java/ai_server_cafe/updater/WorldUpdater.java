package ai_server_cafe.updater;

import ai_server_cafe.model.Field;
import ai_server_cafe.network.proto.ssl.vision.VisionWrapper;

public final class WorldUpdater {
    private static WorldUpdater instance = null;

    private Object ob;
    private boolean flagGeometry;
    private Field field;

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

    synchronized public void update(VisionWrapper.Packet packet) {
        this.flagGeometry = packet.hasGeometry();
    }

    synchronized public boolean hasGeometry() {
        return this.flagGeometry;
    }

    synchronized public Field getField() {
        return this.field;
    }
}
