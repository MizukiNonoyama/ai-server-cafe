package ai_server_cafe.network.receiver;

import ai_server_cafe.network.proto.ssl.vision.VisionWrapper;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.Arrays;
import java.util.Optional;

public class VisionReceiver extends Thread {
	private static RawVisionData visionData = new RawVisionData();
	private static boolean isUpdated = false;
	private static Optional<VisionReceiver> instance = Optional.empty();
	private static boolean isTerminated = false;
	private static final int BUFFER_SIZE = 128;
	
	public void run() {
		try {	
			MulticastSocket socket = new MulticastSocket(new InetSocketAddress(10556));
			socket.joinGroup(new InetSocketAddress("224.5.23.2", 10556), NetworkInterface.getByName("10.22.254.149"));
			Thread.sleep(10, 0);
			while(true) {
				try {
					byte[] buf = new byte[socket.getReceiveBufferSize()];
					final DatagramPacket packet = new DatagramPacket(buf, buf.length);
					//socket.setSoTimeout(500);
					socket.receive(packet);
					final byte[] packetData = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
					visionData = new RawVisionData("" + packet.getLength());
					try {
						VisionWrapper.Packet packet1 =  VisionWrapper.Packet.parseFrom(packetData);
						if (packet1.hasGeometry()) {
							System.out.println("true");
						}
					} catch (Exception e) {
						System.out.println("parse Error");
					}
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
