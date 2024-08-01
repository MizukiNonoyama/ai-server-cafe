package ai_server_cafe.gui.interfaces;

import java.awt.*;
import java.util.List;

public interface IContainerCafe {
    /**
     * set graphical contents. should be called at loop.
     */
    public void setGraphicalContents(List<IGraphicalComponent> graphicalContents);

    /**
     * set action contents. should be called at init.
      */
    public void addActionContents(List<Component> component);

    public void onResize(int newX, int newY);
}
