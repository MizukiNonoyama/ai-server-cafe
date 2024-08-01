package ai_server_cafe.gui;

import ai_server_cafe.gui.interfaces.AbstractPanelCafe;
import ai_server_cafe.gui.interfaces.IGraphicalComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.List;

public class ConfigArea extends AbstractPanelCafe {

    public ConfigArea(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    public void initPaint(Graphics2D graphics2D) {
    }

    @Override
    public boolean isVisibleConfig() {
        return true;
    }

    @Override
    public void onResize(int newX, int newY) {

    }
}
