package ai_server_cafe.updater;

import ai_server_cafe.gui.GuiThread;
import ai_server_cafe.gui.VisionArea;
import ai_server_cafe.gui.interfaces.IGraphicalComponent;
import ai_server_cafe.gui.item.CircleCafe;
import ai_server_cafe.gui.item.LineCafe;
import ai_server_cafe.gui.item.NoneCafe;
import ai_server_cafe.gui.item.RectCafe;
import ai_server_cafe.model.Field;
import ai_server_cafe.model.RawBall;
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
        List<IGraphicalComponent> components = new ArrayList<>();
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

        if (!packet.getDetection().getBallsList().isEmpty()) this.rawBallList.clear();
        for (VisionDetection.Ball detectedBall : packet.getDetection().getBallsList()) {
            this.rawBallList.add(new RawBall(detectedBall.getX(), detectedBall.getY()));
        }

        // Drawing update
        // TODO Gui threadに書くべき
        components.add(new RectCafe("background", -field.getCarpetWidth() / 2.0, -field.getCarpetHeight() / 2.0, field.getCarpetWidth(), field.getCarpetHeight(), ColorHelper.SCREEN_BLACK));
        components.add(new LineCafe("goalToGoalLine", -field.getGameWidth() / 2.0, 0.0, field.getGameWidth() / 2.0, 0.0, ColorHelper.LINE_WHITE, 4.0F));
        components.add(new LineCafe("centerLine", 0.0, -field.getGameHeight() / 2.0, 0.0, field.getGameHeight() / 2.0, ColorHelper.LINE_WHITE, 4.0F));
        components.add(new RectCafe("fieldLine", -field.getGameWidth() / 2.0, -field.getGameHeight() / 2.0, field.getGameWidth(), field.getGameHeight(), ColorHelper.LINE_WHITE, 4.0F));
        components.add(new RectCafe("outsideLine", -field.getFieldWidth() / 2.0, -field.getFieldHeight() / 2.0, field.getFieldWidth(), field.getFieldHeight(), ColorHelper.WALL_RED, 8.0F));
        components.add(new CircleCafe("centerCircle", ColorHelper.LINE_WHITE, 0.0, 0.0, field.getCenterCircle(), false, 4.0F));
        components.add(new RectCafe("ourPenalty", -field.getGameWidth() / 2.0, -field.getPenaltyWidth() / 2.0, field.getPenaltyLength(), field.getPenaltyWidth(), ColorHelper.LINE_WHITE, 4.0F));
        components.add(new RectCafe("oppositePenalty", field.getGameWidth() / 2.0 - field.getPenaltyLength(), -field.getPenaltyWidth() / 2.0, field.getPenaltyLength(), field.getPenaltyWidth(), ColorHelper.LINE_WHITE, 4.0F));
        components.add(new RectCafe("ourGoal", -field.getGameWidth() / 2.0 - field.getGoalLength(), -field.getGoalWidth() / 2.0, field.getGoalLength(), field.getGoalWidth(), ColorHelper.LINE_WHITE, 4.0F));
        components.add(new RectCafe("oppositeGoal", field.getGameWidth() / 2.0, -field.getGoalWidth() / 2.0, field.getGoalLength(), field.getGoalWidth(), ColorHelper.LINE_WHITE, 4.0F));
        if (this.rawBallList.isEmpty()) {
            components.add(new CircleCafe("ball", ColorHelper.BALL_ORANGE, 0.0, 0.0, 21, true, 4.0F));
        } else {
            components.add(new CircleCafe("ball", ColorHelper.BALL_ORANGE, this.rawBallList.getFirst().getX(),
                    this.rawBallList.getFirst().getY(), 21, true, 4.0F));
        }

        GuiThread.getInstance().addAllGraphicalComponents(VisionArea.class, components);
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
