package ai_server_cafe.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;

public class ConfigManager {
    private static ConfigManager instance = null;
    public static final double CYCLE = 1.0 / 60.0;
    public static final double GRAPHIC_CYCLE = 1.0 / 60.0;
    private Config config;
    private boolean isDirty = false;
    private Logger logger = LogManager.getLogger("config manager");

    private ConfigManager() {
        this.load();
    }

    synchronized public void load() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            File configDir = new File("config");
            if (!configDir.exists() || !configDir.isDirectory()) {
                configDir.mkdir();
            }
            File configFile = new File("config/config.json");
            boolean exist = !configFile.createNewFile();
            if (!exist) {
                logger.info("created new config file at {}", configFile.getAbsolutePath());
                this.config = new Config();
                JsonWriter writer = new JsonWriter(new BufferedWriter(new FileWriter(configFile)));
                writer.setIndent("  ");
                gson.toJson(gson.toJsonTree(this.config), writer);
                writer.close();
            } else {
                JsonReader reader = new JsonReader(new BufferedReader(new FileReader(configFile)));
                this.config = gson.fromJson(reader, Config.class);
                reader.close();
            }
        } catch(IOException exception) {
            exception.getStackTrace();
        }
    }

    synchronized public void save() {
        if (this.isDirty) {
            // save
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try {
                File configDir = new File("config");
                if (!configDir.exists() || !configDir.isDirectory()) {
                    configDir.mkdir();
                }
                File configFile = new File("config/config.json");
                configFile.createNewFile();
                JsonWriter writer = new JsonWriter(new BufferedWriter(new FileWriter(configFile)));
                writer.setIndent("  ");
                gson.toJson(gson.toJsonTree(this.config), writer);
                writer.close();
                this.isDirty = false;
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    public synchronized void markDirty() {
        this.isDirty = true;
    }

    public synchronized Config getConfig() {
        if (this.config == null) {
            this.load();
        }
        return this.config;
    }
}
