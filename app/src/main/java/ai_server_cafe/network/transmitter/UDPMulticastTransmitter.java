package ai_server_cafe.network.transmitter;

import ai_server_cafe.Main;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;

public abstract class UDPMulticastTransmitter extends AbstractTransmitter {
    protected MulticastSocket socket = null;
    private byte[] sendBuf;
    private boolean waitingSend;
    protected InetAddress hostAddress;
    protected int port;

    protected UDPMulticastTransmitter(String name) {
        super(name);
        this.waitingSend = false;
    }

    public void startWith(int port, String hostAddress, String interfaceAddress) {
        boolean startFlag = true;
        try {
            this.socket = new MulticastSocket();
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
                ni = NetworkInterface.getByInetAddress(InetAddress.getByName(interfaceAddress));
            } catch (SocketException | UnknownHostException e) {
                this.logger.error("Interface address [%s] is not exist", interfaceAddress);
                startFlag = false;
            }
        }
        try {
            this.hostAddress = InetAddress.getByName(hostAddress);
            this.port = port;
            if (ni != null)
                this.socket.setOption(StandardSocketOptions.IP_MULTICAST_IF, ni);
        } catch (IOException e) {
            this.logger.error("Multicast socket cannot join group with : [port : {}, host : {}]", port, hostAddress);
            startFlag = false;
        }
        if(startFlag) super.startWith(port, hostAddress, interfaceAddress);
        else this.logger.warn("This thread will not start with error");
    }

    @Override
    synchronized protected void loop() {
        try {
            if (this.waitingSend) {
                this.socket.setSendBufferSize(this.sendBuf.length);
                final DatagramPacket packet = new DatagramPacket(this.sendBuf, this.sendBuf.length, this.hostAddress, this.port);
                this.socket.send(packet);
            }
            this.waitingSend = false;
        } catch (SocketTimeoutException exception) {
            this.logger.warn("UDP multicast could not receive in {} milliseconds", this.getTimeout());
        } catch (IOException e) {
            this.logger.error("UDP multicast could not receive with IOException");
        } catch (Exception e) {
            e.printStackTrace();
            Main.exit(1);
        }
    }

    @Override
    synchronized protected void onTerminate() {
        if (this.socket != null) this.socket.close();
        super.onTerminate();
    }

    @Override
    protected void init() {
        int timeout = this.getTimeout();
        if (timeout > 0) {
            try {
                this.socket.setSoTimeout(timeout);
            } catch (SocketException e) {
                this.logger.warn("Transmitter timeout couldn't set : {} milliseconds", timeout);
            }
        }

        try {
            this.socket.setOption(StandardSocketOptions.IP_MULTICAST_LOOP, (Boolean)false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // millisecond
    protected abstract int getTimeout();

    protected void setTransmitData(final byte[] data) {
        if (this.waitingSend) return;
        this.sendBuf = Arrays.copyOfRange(data, 0, data.length);;
        this.waitingSend = true;
    }
}
