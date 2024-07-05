package ai_server_cafe.gui;

import ai_server_cafe.gui.interfaces.AbstractPanelCafe;
import ai_server_cafe.model.Field;
import ai_server_cafe.updater.WorldUpdater;
import java.awt.*;
import java.awt.geom.AffineTransform;

public class VisionArea extends AbstractPanelCafe {
    private boolean isRotated;

    public VisionArea(int width, int height) {
        super(0, 0, width, height);
        this.isRotated = false;
    }

    @Override
    public void initPaint(Graphics2D graphics2D) {
        Field field = WorldUpdater.getInstance().getField();
        double drawMaxWidth = field.getCarpetWidth();
        double drawMaxHeight = field.getCarpetHeight();
        this.isRotated = this.getWidth() < this.getHeight();
        AffineTransform affineTransform = graphics2D.getTransform();
        double rate = Math.min((this.isRotated ? (double)this.getWidth() : (double)this.getHeight()) / drawMaxHeight,
                (this.isRotated ? (double)this.getHeight() : (double)this.getWidth()) / drawMaxWidth);
        affineTransform.translate(this.getWidth() / 2.0 , this.getHeight() / 2.0);
        affineTransform.rotate(this.isRotated ? 0.5 * Math.PI : 0.0, 0.0, 0.0);
        affineTransform.scale(rate, -rate);
        graphics2D.setTransform(affineTransform);
    }
}
