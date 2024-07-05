package ai_server_cafe.gui.interfaces;

import ai_server_cafe.config.Config;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractPanelCafe extends JPanel implements IContainerCafe {
    private final List<IGraphicalComponent> components;

    public AbstractPanelCafe(int x, int y, int width, int height) {
        super();
        this.setBounds(x, y, width, height);
        this.components = new ArrayList<IGraphicalComponent>();
    }

    @Override
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (graphics instanceof Graphics2D && this.isVisibleConfig()) {
            this.initPaint((Graphics2D) graphics);
            for (IGraphicalComponent gc : this.components) {
                gc.paint((Graphics2D) graphics);
            }
        }
    }

    public void setGraphicalContents(List<IGraphicalComponent> graphicalContents) {
        this.components.clear();
        this.components.addAll(graphicalContents);
    }

    public abstract void initPaint(Graphics2D graphics2D);

    @Override
    public void addActionContents(List<Component> component) {
        for (Component c : component)
            this.add(c);
    }

    public abstract boolean isVisibleConfig();
}
