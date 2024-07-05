package ai_server_cafe.gui;

import ai_server_cafe.Main;
import ai_server_cafe.config.Config;
import ai_server_cafe.gui.interfaces.IContainerCafe;
import ai_server_cafe.gui.interfaces.IGraphicalComponent;
import ai_server_cafe.gui.item.CircleCafe;
import ai_server_cafe.gui.item.LineCafe;
import ai_server_cafe.gui.item.RectCafe;
import ai_server_cafe.model.Field;
import ai_server_cafe.updater.WorldUpdater;
import ai_server_cafe.util.TimeHelper;
import ai_server_cafe.util.gui.ColorHelper;
import ai_server_cafe.util.interfaces.IFunction;
import ai_server_cafe.util.thread.AbstractLoopThreadCafe;
import org.checkerframework.checker.units.qual.A;

import java.util.*;

public final class GuiThread extends AbstractLoopThreadCafe {
    private static GuiThread instance = null;
    private static boolean updating = false;
    private double lastDate;
    private boolean isVisible;
    private Map<Class<? extends IContainerCafe>, LinkedHashMap<String, IGraphicalComponent>> graphicalComponentsMap;
    private GameWindow window;
    private GuiThread() {
        super("gui_thread");
        this.isVisible = false;
        this.lastDate = TimeHelper.now();
        this.graphicalComponentsMap = new HashMap<Class<? extends IContainerCafe>, LinkedHashMap<String, IGraphicalComponent>>();
    }

    public static GuiThread getInstance() {
        if (instance == null) {
            instance = new GuiThread();
        }
        return instance;
    }

    synchronized public void setVisible(boolean value) {
        this.isVisible = value;
    }

    synchronized public void setGraphicalComponents(Class<? extends IContainerCafe> parent, List<IGraphicalComponent> components) {
        this.graphicalComponentsMap.put(parent, new LinkedHashMap<>());
        for(IGraphicalComponent component : components) {
            this.graphicalComponentsMap.get(parent).put(component.getId(), component);
        }
    }

    synchronized public void addAllGraphicalComponents(Class<? extends IContainerCafe> parent, List<IGraphicalComponent> components) {
        if (!this.graphicalComponentsMap.containsKey(parent)) {
            this.graphicalComponentsMap.put(parent, new LinkedHashMap<>());
        }
        for(IGraphicalComponent component : components) {
            this.graphicalComponentsMap.get(parent).put(component.getId(), component);
        }
    }

    /**
     * Not recommended
     * @param parent
     * @param component
     */
    synchronized public void addGraphicalComponents(Class<? extends IContainerCafe> parent, IGraphicalComponent component) {
        if (!this.graphicalComponentsMap.containsKey(parent)) {
            this.graphicalComponentsMap.put(parent, new LinkedHashMap<>());
        }
        this.graphicalComponentsMap.get(parent).put(component.getId(), component);
    }

    @Override
    protected void loop() {
        double nowDate = TimeHelper.now();
        if (nowDate - lastDate >= Config.GRAPHIC_CYCLE) {
            for (Map.Entry<Class<? extends IContainerCafe>, LinkedHashMap<String, IGraphicalComponent>> entry : this.graphicalComponentsMap.entrySet()) {
                List<IGraphicalComponent> list = new ArrayList<>(entry.getValue().values());
                this.window.setGraphicalComponent(list , entry.getKey());
            }
            logger.info("repaint");
            this.window.repaint();
            this.lastDate = nowDate;
        }
    }

    @Override
    protected void init() {
        IFunction<Void> onExit = new IFunction<Void>() {
            @Override
            public Void function(Object... args) {
                Main.exit(0);
                return null;
            }
        };
        this.window = new GameWindow("AI Server Cafe", 1280, 720, onExit);
        this.window.setVisible(this.isVisible);

        // Init field draw
        // LinkedHashMapを使っているので登録順に表示される component idを予めここで定義しておく Backgroundに表示するものは中身も書いておいてよい
        Field field = WorldUpdater.getInstance().getField();
        List<IGraphicalComponent> components = new ArrayList<>();
        components.add(new RectCafe("background", -field.getCarpetWidth() / 2.0, -field.getCarpetHeight() / 2.0, field.getCarpetWidth(), field.getCarpetHeight(), ColorHelper.FIELD_GREEN));
        components.add(new LineCafe("goalToGoalLine", -field.getGameWidth() / 2.0, 0.0, field.getGameWidth() / 2.0, 0.0, ColorHelper.LINE_WHITE, 4.0F));
        components.add(new LineCafe("centerLine", 0.0, -field.getGameHeight() / 2.0, 0.0, field.getGameHeight() / 2.0, ColorHelper.LINE_WHITE, 4.0F));
        components.add(new RectCafe("fieldLine", -field.getGameWidth() / 2.0, -field.getGameHeight() / 2.0, field.getGameWidth(), field.getGameHeight(), ColorHelper.LINE_WHITE, 4.0F));
        components.add(new RectCafe("outsideLine", -field.getFieldWidth() / 2.0, -field.getFieldHeight() / 2.0, field.getFieldWidth(), field.getFieldHeight(), ColorHelper.WALL_RED, 8.0F));
        components.add(new CircleCafe("centerCircle", ColorHelper.LINE_WHITE, 0.0, 0.0, field.getCenterCircle(), false, 4.0F));
        components.add(new RectCafe("ourPenalty", -field.getGameWidth() / 2.0, -field.getPenaltyWidth() / 2.0, field.getPenaltyLength(), field.getPenaltyWidth(), ColorHelper.LINE_WHITE, 4.0F));
        components.add(new RectCafe("oppositePenalty", field.getGameWidth() / 2.0 - field.getPenaltyLength(), -field.getPenaltyWidth() / 2.0, field.getPenaltyLength(), field.getPenaltyWidth(), ColorHelper.LINE_WHITE, 4.0F));
        components.add(new RectCafe("ourGoal", -field.getGameWidth() / 2.0 - field.getGoalLength(), -field.getGoalWidth() / 2.0, field.getGoalLength(), field.getGoalWidth(), ColorHelper.LINE_WHITE, 4.0F));
        components.add(new RectCafe("oppositeGoal", field.getGameWidth() / 2.0, -field.getGoalWidth() / 2.0, field.getGoalLength(), field.getGoalWidth(), ColorHelper.LINE_WHITE, 4.0F));

        this.addAllGraphicalComponents(VisionArea.class, components);
    }
}
