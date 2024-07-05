package ai_server_cafe.gui.component;

import ai_server_cafe.util.interfaces.IFunction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SwitchBoxCafe extends AbstractButton {
    private final String label0;
    private final String label1;
    private final int size;
    private String text;
    private int count;
    private final Color backgroundFalse;
    private final Color backgroundTrue;
    private final Color button;
    private final IFunction<Void> onSwitch;

    public SwitchBoxCafe(String label0, String label1, boolean valueDefault, int size, int x, int y, Color backgroundFalse, Color backgroundTrue, Color button, IFunction<Void> onSwitch) {
        this.label0 = label0;
        this.label1 = label1;
        this.setModel(new DefaultButtonModel());
        this.setSelected(valueDefault);
        this.setLayout(null);
        this.setBounds(x, y, 2 * size, size);
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (new Rectangle(getPreferredSize()).contains(e.getPoint())) {
                    setSelected(!isSelected());
                }
            }
        });
        this.size = size;
        this.count = 0;
        this.backgroundFalse = backgroundFalse;
        this.backgroundTrue = backgroundTrue;
        this.button = button;
        this.onSwitch = onSwitch;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(2 * this.size, this.size);
    }

    @Override
    public void setText(String text) {
        super.setText(text);
    }

    @Override
    public int getHeight() {

        return getPreferredSize().height;
    }

    @Override
    public int getWidth() {
        return getPreferredSize().width;
    }

    @Override
    public void setSelected(boolean value) {
        this.count = 10;
        if (value) {
            setText(this.label1);
        } else {
            setText(this.label0);
        }
        if (this.onSwitch != null) {
            this.onSwitch.function(value);
        }
        super.setSelected(value);
    }

    @Override
    public void paintComponent(Graphics graphics) {
        if (this.isSelected()) {
            graphics.setColor(this.backgroundTrue);
        } else {
            graphics.setColor(this.backgroundFalse);
        }
        graphics.fillOval(0, 0, this.size, this.size);
        graphics.fillOval(this.size, 0, this.size, this.size);
        graphics.fillRect(this.size / 2, 0, this.size, this.size);
        graphics.setColor(this.button);
        int offset = 0;
        if (this.isSelected()) {
            offset = this.size;
        }
        graphics.fillOval(offset + (int)(0.1 * this.size), (int)(0.1 * this.size), (int)(0.8 * this.size), (int)(0.8 * this.size));
    }
}
