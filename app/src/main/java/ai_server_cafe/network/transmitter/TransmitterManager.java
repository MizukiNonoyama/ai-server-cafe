package ai_server_cafe.network.transmitter;

import ai_server_cafe.network.radio.EnumRadioType;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public final class TransmitterManager {
    private static TransmitterManager instance = null;
    private final List<EnumRadioType> transmitters;

    private TransmitterManager() {
        this.transmitters = new ArrayList<>();
    }

    @Nonnull
    public List<AbstractTransmitter> getTransmitters() {
        List<AbstractTransmitter> transmitterList = new ArrayList<>();
        synchronized (this.transmitters) {
            for (EnumRadioType ert : this.transmitters) {
                if (ert == null) continue;
                try {
                    Object o = ert.getTransmitterClass().getMethod("getInstance").invoke(null);
                    if (o instanceof AbstractTransmitter) {
                        transmitterList.add((AbstractTransmitter) o);
                    }
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return transmitterList;
    }

    public void setTransmitters(int[] values) {
        for (AbstractTransmitter at : this.getTransmitters()) {
            at.terminate();
        }
        synchronized (this.transmitters) {
            this.transmitters.clear();
            for (int value : values) {
                this.transmitters.add(EnumRadioType.getFromId(value));
            }
        }
        for (AbstractTransmitter at : this.getTransmitters()) {
            at.startWith(at.getType().getTransmitterPort(), at.getType().getTransmitterAddress(), at.getType().getTransmitterIfAddress());
        }
    }

    public static TransmitterManager getInstance() {
        if (instance == null) {
            instance = new TransmitterManager();
        }
        return instance;
    }
}
