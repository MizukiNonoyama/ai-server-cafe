package ai_server_cafe.network.radio;

import ai_server_cafe.config.Config;
import ai_server_cafe.config.ConfigManager;
import ai_server_cafe.network.transmitter.GrSimTransmitter;
import ai_server_cafe.network.transmitter.AbstractTransmitter;
import ai_server_cafe.network.transmitter.KIKSTransmitter;

public enum EnumRadioType {
    KIKS(0, "kiks", KIKSTransmitter.class),
    GR_SIM(1, "grsim", GrSimTransmitter.class);

    private int id;
    private Class<? extends AbstractTransmitter> clazz;
    private String name;

    EnumRadioType(int id, String name, Class<? extends AbstractTransmitter> clazz) {
        this.id = id;
        this.name = name;
        this.clazz = clazz;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public Class<? extends AbstractTransmitter> getTransmitterClass() {
        return this.clazz;
    }

    public static EnumRadioType getFromId(int id) {
        for (EnumRadioType ert : EnumRadioType.values()) {
            if (ert.getId() == id) {
                return ert;
            }
        }
        return null;
    }

    public String getTransmitterAddress() {
        Config config = ConfigManager.getInstance().getConfig();
        switch (this.id) {
            case 0:
                return config.transmitterAddress;
            case 1:
                return config.grSimAddress;
            default:
                return "";
        }
    }

    public String getTransmitterIfAddress() {
        Config config = ConfigManager.getInstance().getConfig();
        switch (this.id) {
            case 0:
                return config.transmitterInterfaceAddress;
            case 1:
                return config.grSimInterfaceAddress;
            default:
                return "";
        }
    }

    public int getTransmitterPort() {
        Config config = ConfigManager.getInstance().getConfig();
        switch (this.id) {
            case 0:
                return config.transmitterPort;
            case 1:
                return config.grSimPort;
            default:
                return 0;
        }
    }
}
