package ai_server_cafe.gui;

import java.util.Optional;

public class GuiThread extends Thread {
    private static Optional<GuiThread> instance = Optional.empty();
    ThreadLocal<Boolean> requiredRepaint;

    public GuiThread() {
        
    }

    public void run() {

        if (requiredRepaint.get()) {
            // repaint

            requiredRepaint.set(false);
        }
    }

    synchronized public static GuiThread instance() {
        if (instance.isEmpty()) {
            instance = Optional.of(new GuiThread());
        }
        return instance.get();
    }

    synchronized public void repaint() {
        requiredRepaint.set(true);
    }
}
