package ai_server_cafe.network.transmitter;

import ai_server_cafe.network.proto.ssl.grsim.GrsimCommand;
import ai_server_cafe.network.proto.ssl.grsim.GrsimPacket;
import ai_server_cafe.util.EnumKickType;
import ai_server_cafe.util.SendCommand;
import ai_server_cafe.util.TimeHelper;
import ai_server_cafe.util.math.KickConverter;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public final class GrSimTransmitter extends UDPMulticastTransmitter {
    private static GrSimTransmitter instance = null;

    private GrSimTransmitter() {
        super("grSim transmitter");
    }

    synchronized public void sendCommand(@Nonnull List<SendCommand> commandList) {
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
                .setIsteamyellow(true).addAllRobotCommands(robotCommands).build();
        GrsimPacket.Packet packet = GrsimPacket.Packet.newBuilder().mergeCommands(commands).build();
        this.setTransmitData(packet.toByteArray());
    }

    @Override
    protected int getTimeout() {
        return 10;
    }

    public static GrSimTransmitter getInstance() {
        if (instance == null) {
            instance = new GrSimTransmitter();
        }
        return instance;
    }
}
