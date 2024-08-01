package ai_server_cafe.gui;

import ai_server_cafe.gui.interfaces.IContainerCafe;
import ai_server_cafe.gui.interfaces.IGraphicalComponent;
import ai_server_cafe.util.interfaces.IFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;
import java.util.List;

public class GameWindow extends JFrame {
    private final Logger logger;
    private final Map<Class<? extends IContainerCafe>, Component> componentMap;

    public GameWindow(String title, int defaultWidth, int defaultHeight, IFunction<Void> onExit) {
        super(title);
        this.logger = LogManager.getLogger("game_window");
        this.setLayout(null);
        this.setBounds(100, 100, defaultWidth, defaultHeight);
        this.setResizable(true);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onExit.function(e);
            }
        });
        this.componentMap = new HashMap<>();
        /*this.componentMap.put(VisionArea.class, new VisionArea((int)(this.getHeight() * 12.0 / 9.0), this.getHeight()));
        this.componentMap.put(ConfigArea.class, new ConfigArea((int)(this.getHeight() * 12.0 / 9.0), 0, 120, 960));
        for (Map.Entry<Class<? extends IContainerCafe>, Component> entry : this.componentMap.entrySet())
            this.getContentPane().add(entry.getValue());
        */
        this.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent componentEvent) {
                onResize(componentEvent.getComponent().getWidth(), componentEvent.getComponent().getHeight());
            }});
    }


    /**
     * add contents, this method must call on init.
     * @param contents
     * @param parent
     */
    public void addContents(List<Component> contents, Class<? extends IContainerCafe> parent) {
        if (parent == null) {
            for (Component c : contents) {
                if (c instanceof IContainerCafe) {
                    IContainerCafe containerCafe = (IContainerCafe)c;
                    this.componentMap.put(containerCafe.getClass(), c);
                    this.getContentPane().add(this.componentMap.get(containerCafe.getClass()));
                } else {
                    this.getContentPane().add(c);
                }
            }
        } else {
            if(this.componentMap.containsKey(parent)) {
                if (this.componentMap.get(parent) instanceof IContainerCafe) {
                    ((IContainerCafe)this.componentMap.get(parent)).addActionContents(contents);
                }
            }
        }
    }

    public void addContent(Component content, Class<? extends IContainerCafe> parent) {
        if (parent == null) {
            if (content instanceof IContainerCafe) {
                IContainerCafe containerCafe = (IContainerCafe)content;
                this.componentMap.put(containerCafe.getClass(), content);
                this.getContentPane().add(this.componentMap.get(containerCafe.getClass()));
            } else {
                this.getContentPane().add(content);
            }
        } else {
            if(this.componentMap.containsKey(parent)) {
                if (this.componentMap.get(parent) instanceof IContainerCafe) {
                    ((IContainerCafe)this.componentMap.get(parent)).addActionContents(Arrays.asList(content));
                }
            }
        }
    }

    /**
     * add graphical contents, this method must call on loop.
     * @param components
     * @param parent
     */
    public void setGraphicalComponents(List<IGraphicalComponent> components, Class<? extends IContainerCafe> parent) {
        if (parent == null) {
            this.logger.info("Parent was null");
        } else {
            if(this.componentMap.containsKey(parent)) {
                if (this.componentMap.get(parent) instanceof IContainerCafe) {
                    ((IContainerCafe)this.componentMap.get(parent)).setGraphicalContents(components);
                }
            }
        }
    }

    public void onResize(int newWidth, int newHeight) {
        this.logger.info("{}, {}", newWidth, newHeight);
        for (Map.Entry<Class<? extends IContainerCafe>, Component> entry : this.componentMap.entrySet()) {
            if (entry.getValue() instanceof IContainerCafe) {
                ((IContainerCafe)entry.getValue()).onResize(newWidth, newHeight);
            }
        }
    }
}
