package ai_server_cafe.gui;

import ai_server_cafe.gui.interfaces.IContainerCafe;
import ai_server_cafe.gui.interfaces.IGraphicalComponent;
import ai_server_cafe.util.interfaces.IFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameWindow extends JFrame {
    private VisionArea visionArea;
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
        this.componentMap.put(VisionArea.class, new VisionArea((int)(this.getHeight() * 12.0 / 9.0), this.getHeight()));
        for (Map.Entry<Class<? extends IContainerCafe>, Component> entry : this.componentMap.entrySet())
            this.getContentPane().add(entry.getValue());
    }


    /**
     * add contents, this method must call on init.
     * @param contents
     * @param clazz
     */
    public void addContents(List<Container> contents, Class<?> clazz) {
        if (clazz == null) {
            for (Container c : contents)
                this.getContentPane().add(c);
        } else if (clazz == VisionArea.class) {
            for (Container c : contents)
                this.visionArea.add(c);
        }
    }

    /**
     * add graphical contents, this method must call on loop.
     * @param components
     * @param parent
     */
    public void setGraphicalComponent(List<IGraphicalComponent> components, Class<? extends IContainerCafe> parent) {
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
}
