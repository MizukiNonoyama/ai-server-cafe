package ai_server_cafe.config;

import com.google.gson.stream.JsonWriter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Config {
    private static Config instance = null;
    private boolean visionVisible;
    public static final double CYCLE = 1.0 / 60.0;
    public static final double GRAPHIC_CYCLE = 1.0 / 60.0;
    private String visionAddress = "224.5.23.2";
    private String visionIfAddress = "10.22.254.149";
    private boolean isDirty = false;

    private Config() {
        this.visionVisible = true;
        this.load();
    }

    synchronized public boolean load() {
        try {
            File configFile = new File("config/config.json");
            boolean exist = !configFile.createNewFile();
            if (!exist) {
                // write new file
                //JsonWriter writer = new JsonWriter();
            }
            //load

        } catch(IOException exception) {

        }
        return false;
    }

    synchronized public boolean save() {
        if (this.isDirty) {
            // save
            this.isDirty = false;
        }
        return false;
    }

    synchronized public void setVisionVisible(boolean value) {
        this.visionVisible = value;
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

    public void markDirty() {
        this.isDirty = true;
    }
}
