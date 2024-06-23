package ai_server_cafe.network.receiver;

import ai_server_cafe.network.proto.RawVisionData;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.Optional;

public class VisionReceiver {
	private static RawVisionData visionData = new RawVisionData();
	private static boolean isUpdated = false;
	private static Optional<VisionReceiver> instance = Optional.empty();
	private static boolean isTerminated = false;
	
	public void run() {
		try {	
			MulticastSocket socket = new MulticastSocket(new InetSocketAddress(10556));
			socket.joinGroup(new InetSocketAddress("224.5.23.2", 10556), NetworkInterface.getByName("10.22.249.114"));
			Thread.sleep(10, 0);
			while(true) {
				try {
					final DatagramPacket packet = new DatagramPacket(new byte[25565], 25565);
					//socket.setSoTimeout(500);
					socket.receive(packet);
					visionData = new RawVisionData("" + packet.getLength());
					this.setUpdated(true);
				} catch (Exception e) {
					// Skip anyway
				}
				
				if(isTerminated) {
					socket.close();
					break;
				}
			}
		} catch (Exception e) {
			System.out.println("e");
		}
	}
	
	synchronized public RawVisionData getVision() {
		return visionData;
	}
	
	synchronized public boolean isUpdated() {
		return isUpdated;
	}
	
	synchronized public void setTerminate(boolean value) {
		isTerminated = value;
	}
	
	synchronized public void setUpdated(boolean value) {
		isUpdated = value;
	}
	
	synchronized public static VisionReceiver getInstance() {
        if (instance.isEmpty()) {
            instance = Optional.of(new VisionReceiver());
        }
        return instance.get();
    }
}
