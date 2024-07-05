package ai_server_cafe.network.receiver;

import ai_server_cafe.util.thread.AbstractLoopThreadCafe;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;

public abstract class UDPMulticastReceiver extends AbstractLoopThreadCafe {
    private MulticastSocket socket = null;

    protected UDPMulticastReceiver(String name) {
        super(name);
    }

    @Override
    @Deprecated
    public void start() {}

    public void startWith(int port, String hostAddress, String interfaceAddress) {
        boolean startFlag = true;
        try {
            this.socket = new MulticastSocket(new InetSocketAddress(port));
        } catch (IOException e) {
            this.logger.error("Multicast socket cannot bind this port : {}", port);
            startFlag = false;
        }
        NetworkInterface ni = null;
        try {
            ni = NetworkInterface.getNetworkInterfaces().nextElement();
        } catch (SocketException e) {
            this.logger.error("No interface address");
            startFlag = false;
        }
        if(!interfaceAddress.isEmpty()) {
            try {
                ni = NetworkInterface.getByName(interfaceAddress);
            } catch (SocketException e) {
                this.logger.error("Interface address [%s] is not exist", interfaceAddress);
                startFlag = false;
            }
        }
        try {
            this.socket.joinGroup(new InetSocketAddress(hostAddress, port), ni);
        } catch (IOException e) {
            this.logger.error("Multicast socket cannot join group with : [port : {}, host : {}]", port, hostAddress);
            startFlag = false;
        }
        if(startFlag) super.start();
        else this.logger.warn("This thread will not start with error");
    }

    @Override
    protected void loop() {
        try {
            byte[] buf = new byte[this.socket.getReceiveBufferSize()];
            final DatagramPacket packet = new DatagramPacket(buf, buf.length);
            this.socket.receive(packet);
            final byte[] packetData = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
            this.onReceive(packetData);
        } catch (SocketTimeoutException exception) {
            this.logger.warn("UDP multicast could not receive in {} milliseconds", this.getTimeout());
        } catch (IOException e) {
            this.logger.error("UDP multicast could not receive with IOException");
        } catch (Exception e) {
            this.logger.error("Unknown error occurred");
        }
    }

    @Override
    protected void onTerminate() {
        this.socket.close();
        super.onTerminate();
    }

    @Override
    protected void init() {
        int timeout = this.getTimeout();
        if (timeout > 0) {
            try {
                this.socket.setSoTimeout(timeout);
            } catch (SocketException e) {
                this.logger.warn("Receiver timeout couldn't set : {} milliseconds", timeout);
            }
        }
    }

    // millisecond
    protected abstract int getTimeout();

    protected abstract void onReceive(final byte[] data);
}
