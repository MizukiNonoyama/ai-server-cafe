package ai_server_cafe.gui.item;

import ai_server_cafe.gui.interfaces.AbstractGraphicalComponent;
import ai_server_cafe.gui.interfaces.IGraphicalComponent;

import java.awt.*;

public class RectCafe extends AbstractGraphicalComponent {
    private final double startX;
    private final double startY;
    private final double width;
    private final double height;
    private final float stroke;
    private final boolean isFill;

    public RectCafe(String id, double startX, double startY, double width, double height, Color color, float stroke, boolean isFill) {
        super(id, color);
        this.startX = startX;
        this.startY = startY;
        this.width = width;
        this.height = height;
        this.stroke = stroke;
        this.isFill = isFill;
    }

    public RectCafe(String id, double startX, double startY, double width, double height, Color color, float stroke) {
        super(id, color);
        this.startX = startX;
        this.startY = startY;
        this.width = width;
        this.height = height;
        this.stroke = stroke;
        this.isFill = false;
    }

    public RectCafe(String id, double startX, double startY, double width, double height, Color color) {
        super(id, color);
        this.startX = startX;
        this.startY = startY;
        this.width = width;
        this.height = height;
        this.stroke = 0.0F;
        this.isFill = true;
    }

    @Override
    public void paint2D(Graphics2D graphics) {
        graphics.setStroke(new BasicStroke(this.stroke));
        if(isFill) {
            graphics.fillRect((int)this.startX, (int)this.startY, (int)this.width, (int)this.height);
        } else {
            graphics.drawRect((int)this.startX, (int)this.startY, (int)this.width, (int)this.height);
        }
    }
}
