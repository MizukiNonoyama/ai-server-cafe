package ai_server_cafe;

import ai_server_cafe.network.receiver.VisionReceiver;

public class Main {
	public String getGreeting() {
		return "Hello World!";
	}
	
	public static void main(String[] args) {
		System.out.println(new Main().getGreeting());
		System.out.println("Hello, World");
		VisionReceiver.getInstance().start();
		int i = 0;
		long last_date = System.nanoTime();
		while(true) {
			long nowDate = System.nanoTime();
			if(VisionReceiver.getInstance().isUpdated()) {
				System.out.println((double)(nowDate - last_date) * 1e-9);
				VisionReceiver.getInstance().setUpdated(false);
				i++;
				last_date = nowDate;
			}
			
			if(i > 1000) {
				VisionReceiver.getInstance().setTerminate(true);
				break;
			}
		}
	}
}
