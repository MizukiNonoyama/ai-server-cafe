package ai_server_cafe.gui.interfaces;

import java.awt.*;

public abstract class AbstractGraphicalComponent implements IGraphicalComponent {
    protected final Color color;
    protected final String id;
    public AbstractGraphicalComponent(String id, Color color) {
        this.color = color;
        this.id = id;
    }

    public void paint(Graphics2D graphics) {
        graphics.setColor(this.color);
        this.paint2D(graphics);
    }

    public String getId() {
        return this.id;
    }

    public abstract void paint2D(Graphics2D graphics);
}
