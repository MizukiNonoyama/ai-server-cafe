package ai_server_cafe.gui;

import ai_server_cafe.Main;
import ai_server_cafe.config.ConfigManager;
import ai_server_cafe.gui.interfaces.IContainerCafe;
import ai_server_cafe.gui.interfaces.IGraphicalComponent;
import ai_server_cafe.gui.registry.RegistryGUIItem;
import ai_server_cafe.util.EnumReceivedType;
import ai_server_cafe.util.TimeHelper;
import ai_server_cafe.util.interfaces.IFunction;
import ai_server_cafe.util.thread.AbstractLoopThreadCafe;
import com.google.gson.Gson;

import java.sql.Time;
import java.util.*;
import java.util.List;

public final class GuiThread extends AbstractLoopThreadCafe {
    private static GuiThread instance = null;
    private static boolean updating = false;
    private double lastDate;
    private double serverStartingTime;
    private boolean isVisible;
    private Map<Class<? extends IContainerCafe>, LinkedHashMap<String, IGraphicalComponent>> graphicalComponentsMap;
    private Map<EnumReceivedType, Integer> receivedPacketCounts;
    private Map<EnumReceivedType, Integer> receivedPacketPerSec;
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

    synchronized public void receivePacket(EnumReceivedType type, int count) {
        if (this.receivedPacketCounts.containsKey(type)) {
            this.receivedPacketCounts.put(type, count);
        } else {
            this.receivedPacketCounts.put(type, this.receivedPacketCounts.get(type) + count);
        }

        if (this.receivedPacketPerSec.containsKey(type)) {
            this.receivedPacketPerSec.put(type, count);
        } else {
            this.receivedPacketPerSec.put(type, this.receivedPacketPerSec.get(type) + count);
        }
    }

    @Override
    protected void loop() {
        double nowDate = TimeHelper.now();
        if (nowDate - lastDate >= ConfigManager.GRAPHIC_CYCLE) {
            synchronized (this) {
                for (Map.Entry<Class<? extends IContainerCafe>, LinkedHashMap<String, IGraphicalComponent>> entry : this.graphicalComponentsMap.entrySet()) {
                    List<IGraphicalComponent> list = new ArrayList<>(entry.getValue().values());
                    this.window.setGraphicalComponents(list , entry.getKey());
                }
            }
            this.window.repaint();
            this.lastDate = nowDate;
        }
    }

    @Override
    protected void init() {
        this.serverStartingTime = TimeHelper.now();

        IFunction<Void> onExit = new IFunction<Void>() {
            @Override
            public Void function(Object... args) {
                Main.exit(0);
                return null;
            }
        };
        this.window = new GameWindow("AI Server Cafe", 1280, 720, onExit);
        this.window.setVisible(this.isVisible);

        this.receivedPacketCounts = new HashMap<>();
        this.receivedPacketPerSec = new HashMap<>();

        RegistryGUIItem.registerContents(this.window);
        RegistryGUIItem.registerGraphicalContents();
    }
}
