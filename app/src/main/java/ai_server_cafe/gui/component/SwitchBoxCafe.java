package ai_server_cafe.gui.component;

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

    public SwitchBoxCafe(String label0, String label1, boolean valueDefault, int size, int x, int y) {
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
        super.setSelected(value);
    }

    public
}
