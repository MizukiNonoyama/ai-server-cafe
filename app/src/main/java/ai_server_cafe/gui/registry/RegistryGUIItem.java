package ai_server_cafe.gui.registry;

import ai_server_cafe.config.Config;
import ai_server_cafe.config.ConfigManager;
import ai_server_cafe.gui.ConfigArea;
import ai_server_cafe.gui.GameWindow;
import ai_server_cafe.gui.GuiThread;
import ai_server_cafe.gui.VisionArea;
import ai_server_cafe.gui.component.SwitchBoxCafe;
import ai_server_cafe.gui.interfaces.IGraphicalComponent;
import ai_server_cafe.gui.item.CircleCafe;
import ai_server_cafe.gui.item.LineCafe;
import ai_server_cafe.gui.item.RectCafe;
import ai_server_cafe.model.Field;
import ai_server_cafe.updater.WorldUpdater;
import ai_server_cafe.util.TeamColor;
import ai_server_cafe.util.gui.ColorHelper;
import ai_server_cafe.util.interfaces.IFunction;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class RegistryGUIItem {
    public static void registerContents(GameWindow window) {
        Config config = ConfigManager.getInstance().getConfig();
        // Panelを追加
        window.addContent(new VisionArea((int)(window.getHeight() * 12.0 / 9.0), window.getHeight()), null);
        window.addContent(new ConfigArea((int)(window.getHeight() * 12.0 / 9.0), 0, 300, 960), null);

        // Switch等を追加
        List<Component> components1 = new ArrayList<>();
        IFunction<Void> onSwitchVision = new IFunction<Void>() {
            @Override
            public Void function(Object... args) {
                if(args.length == 1 && args[0] instanceof Boolean) {
                    ConfigManager.getInstance().getConfig().isVisionAreaVisible = (Boolean) args[0];
                    ConfigManager.getInstance().markDirty();
                }
                return null;
            }
        };
        components1.add(new SwitchBoxCafe("", "", config.isVisionAreaVisible, 20, 30, 30, ColorHelper.SWITCH_GRAY, ColorHelper.ARCHIVE_SKY, ColorHelper.LINE_WHITE, onSwitchVision));
        IFunction<Void> onSwitchTeamColor = new IFunction<Void>() {
            @Override
            public Void function(Object... args) {
                if(args.length == 1 && args[0] instanceof Boolean) {
                    ConfigManager.getInstance().getConfig().teamColor = ((Boolean) args[0]) ? TeamColor.BLUE.getId() : TeamColor.YELLOW.getId();
                    ConfigManager.getInstance().markDirty();
                }
                return null;
            }
        };
        components1.add(new SwitchBoxCafe("", "", config.teamColor == TeamColor.BLUE.getId(), 20, 90, 100, ColorHelper.ROBOT_YELLOW, ColorHelper.ROBOT_BLUE, ColorHelper.LINE_WHITE, onSwitchTeamColor));
        window.addContents(components1, ConfigArea.class);
    }

    public static void registerGraphicalContents() {
        // Init draw
        // LinkedHashMapを使っているので登録順に表示される component idを予めここで定義しておく Backgroundに表示するものは中身も書いておいてよい
        Field field = WorldUpdater.getInstance().getField();
        List<IGraphicalComponent> components = new ArrayList<>();
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

        GuiThread.getInstance().addAllGraphicalComponents(VisionArea.class, components);
    }
}
