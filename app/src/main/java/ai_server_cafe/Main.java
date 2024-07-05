package ai_server_cafe;

import ai_server_cafe.game.GameThread;
import ai_server_cafe.gui.GuiThread;
import ai_server_cafe.network.receiver.VisionReceiver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
	public String getGreeting() {
		return "Hello World!";
	}
	
	public static void main(String[] args) {
		VisionReceiver.getInstance().startWith(10556, "224.5.23.2", "10.22.254.149");
		GuiThread.getInstance().start();
		GuiThread.getInstance().setVisible(true);
		GameThread.getInstance().start();
    }

	public static void exit(int exitCode) {
		VisionReceiver.getInstance().terminate();
		GuiThread.getInstance().terminate();
		GameThread.getInstance().terminate();
        try {
            Thread.sleep(30);
        } catch (InterruptedException e) {
            // Do nothing
        }
        Logger log = LogManager.getLogger("system");
		log.info("Terminated with code : {}", exitCode);
		System.exit(exitCode);
	}
}
