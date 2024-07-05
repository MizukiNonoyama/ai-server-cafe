package ai_server_cafe.gui.interfaces;

import javax.swing.*;

public abstract class AbstractSwitchCafe extends AbstractButton {
    private final String label0;
    private final String label1;

    public AbstractSwitchCafe(String label0, String label1) {
        this.label0 = label0;
        this.label1 = label1;
    }
}
