package ai_server_cafe.config;

public class Config {
    private static Config instance = null;
    private boolean visionVisible;
    public static final double CYCLE = 1.0 / 60.0;
    public static final double GRAPHIC_CYCLE = 1.0 / 60.0;
    private String visionAddress = "224.5.23.2";
    private String visionIfAddress = "10.22.254.149";

    private Config() {
        this.visionVisible = false;
    }

    synchronized public void setVisionVisible(boolean value) {
        this.visionVisible = value;
    }

    synchronized public boolean load() {
        return false;
    }

    synchronized public boolean save() {
        return false;
    }

    synchronized public boolean isVisibleVision() {
        return this.visionVisible;
    }

    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }
}
