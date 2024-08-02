package ai_server_cafe.network.transmitter;

import ai_server_cafe.network.proto.ssl.grsim.GrsimCommand;
import ai_server_cafe.network.proto.ssl.grsim.GrsimPacket;
import ai_server_cafe.network.radio.EnumRadioType;
import ai_server_cafe.util.EnumKickType;
import ai_server_cafe.util.SendCommand;
import ai_server_cafe.util.TeamColor;
import ai_server_cafe.util.TimeHelper;
import ai_server_cafe.util.math.KickConverter;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public final class GrSimTransmitter extends UDPMulticastTransmitter {
    private static GrSimTransmitter instance = null;

    private GrSimTransmitter() {
        super("grSim transmitter");
    }

    @Override
    synchronized public void sendCommands(@Nonnull List<SendCommand> commandList) {
        List<GrsimCommand.Command> robotCommands = new ArrayList<>();
        for (SendCommand c : commandList) {
            double theta = c.getKickFlag().getKey() == EnumKickType.CHIP ? Math.PI / 3.0 : 0.0;
            GrsimCommand.Command command = GrsimCommand.Command.newBuilder()
                    .setId(c.getId())
                    .setSpinner(c.getDribble() > 0)
                    .setKickspeedx(c.getKickFlag().getKey() == EnumKickType.NONE ? 0.0F
                            : (float) (KickConverter.toSpeed(c.getId(), c.getKickFlag()) * Math.cos(theta) / 1000.0))
                    .setKickspeedz((float) (KickConverter.toSpeed(c.getId(), c.getKickFlag()) * Math.sin(theta) / 1000.0))
                    .setVelangular((float)c.getOmega())
                    .setVelnormal((float)(c.getVy() / 1000.0))
                    .setVeltangent((float)(c.getVx() / 1000.0))
                    .setWheelsspeed(false).build();
            robotCommands.add(command);
        }
        GrsimCommand.Commands commands = GrsimPacket.Packet.newBuilder().getCommandsBuilder().setTimestamp(TimeHelper.now())
                .setIsteamyellow(!commandList.isEmpty() && commandList.getFirst() != null && commandList.getFirst().getColor() == TeamColor.YELLOW).addAllRobotCommands(robotCommands).build();
        GrsimPacket.Packet grSimPacket = GrsimPacket.Packet.newBuilder().mergeCommands(commands).build();
        final byte[] sendBuf = grSimPacket.toByteArray();
        try {
            this.socket.setSendBufferSize(sendBuf.length);
            final DatagramPacket packet = new DatagramPacket(sendBuf, sendBuf.length, this.hostAddress, this.port);
            this.socket.send(packet);
        } catch (SocketException e) {
            this.logger.error("socket error occurred");
        } catch (IOException e) {
            this.logger.error("socket io error occurred");
        }
    }

    @Override
    synchronized public void sendCommand(@Nonnull SendCommand c) {
        double theta = c.getKickFlag().getKey() == EnumKickType.CHIP ? Math.PI / 3.0 : 0.0;
        GrsimCommand.Command command = GrsimCommand.Command.newBuilder()
                .setId(c.getId())
                .setSpinner(c.getDribble() > 0)
                .setKickspeedx(c.getKickFlag().getKey() == EnumKickType.NONE ? 0.0F
                        : (float) (KickConverter.toSpeed(c.getId(), c.getKickFlag()) * Math.cos(theta) / 1000.0))
                .setKickspeedz((float) (KickConverter.toSpeed(c.getId(), c.getKickFlag()) * Math.sin(theta) / 1000.0))
                .setVelangular((float)c.getOmega())
                .setVelnormal((float)(c.getVy() / 1000.0))
                .setVeltangent((float)(c.getVx() / 1000.0))
                .setWheelsspeed(false).build();
        GrsimCommand.Commands commands = GrsimPacket.Packet.newBuilder().getCommandsBuilder().setTimestamp(TimeHelper.now())
                .setIsteamyellow(c.getColor() == TeamColor.YELLOW).addRobotCommands(command).build();
        GrsimPacket.Packet grSimPacket = GrsimPacket.Packet.newBuilder().mergeCommands(commands).build();
        final byte[] sendBuf = grSimPacket.toByteArray();
        try {
            this.socket.setSendBufferSize(sendBuf.length);
            final DatagramPacket packet = new DatagramPacket(sendBuf, sendBuf.length, this.hostAddress, this.port);
            this.socket.send(packet);
        } catch (SocketException e) {
            this.logger.error("socket error occurred at single command sender");
        } catch (IOException e) {
            this.logger.error("socket io error occurred at single command sender");
        }
    }

    @Override
    public EnumRadioType getType() {
        return EnumRadioType.GR_SIM;
    }

    @Override
    protected int getTimeout() {
        return 10;
    }

    public static GrSimTransmitter getInstance() {
        if (instance == null) {
            instance = new GrSimTransmitter();
            //instance.logger.info("new instance");
        }
        return instance;
    }
}
