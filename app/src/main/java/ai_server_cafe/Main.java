package ai_server_cafe;

import ai_server_cafe.config.Config;
import ai_server_cafe.config.ConfigManager;
import ai_server_cafe.game.GameThread;
import ai_server_cafe.gui.GuiThread;
import ai_server_cafe.network.receiver.VisionReceiver;
import ai_server_cafe.network.transmitter.GrSimTransmitter;
import ai_server_cafe.network.transmitter.KIKSTransmitter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
	public String getGreeting() {
		return "Hello World!";
	}
	
	public static void main(String[] args) {
		Config config = ConfigManager.getInstance().getConfig();
		VisionReceiver.getInstance().startWith(config.visionPort, config.visionAddress, config.visionInterfaceAddress);
		GrSimTransmitter.getInstance().startWith(config.grSimPort, config.grSimAddress, config.grSimInterfaceAddress);
		KIKSTransmitter.getInstance().startWith(config.transmitterPort, config.transmitterAddress, config.transmitterInterfaceAddress);
		GuiThread.getInstance().start();
		GuiThread.getInstance().setVisible(true);
		GameThread.getInstance().start();
    }

	public static void exit(int exitCode) {
		VisionReceiver.getInstance().terminate();
		GuiThread.getInstance().terminate();
		GameThread.getInstance().terminate();
		GrSimTransmitter.getInstance().terminate();
		KIKSTransmitter.getInstance().terminate();
		ConfigManager.getInstance().save();
        try {
            Thread.sleep(30);
        } catch (InterruptedException e) {
            // Do nothing
        }
        Logger log = LogManager.getLogger("system");
		log.info("Terminated with exit code : {}", exitCode);
		System.exit(exitCode);
	}
}
