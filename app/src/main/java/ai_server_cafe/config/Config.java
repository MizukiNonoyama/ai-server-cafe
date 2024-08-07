package ai_server_cafe.config;

import ai_server_cafe.util.TeamColor;

public class Config {
    public boolean isVisionAreaVisible;
    public String visionAddress;
    public String visionInterfaceAddress;
    public int visionPort;
    public String transmitterAddress;
    public String transmitterInterfaceAddress;
    public int transmitterPort;
    public String grSimAddress;
    public String grSimInterfaceAddress;
    public int grSimPort;
    public String refBoxAddress;
    public String refBoxInterfaceAddress;
    public int refBoxPort;
    public int teamColor;
    public int[] activeRobots;
    public boolean autoReboot;
    public boolean isStart;
    public double fps;
    public double cyclePS;
    public double sendPS;
    public int[] radioTypes;
    public double lostDuration;

    public Config() {
        this.isVisionAreaVisible = true;
        this.visionAddress = "224.5.23.2";
        this.visionInterfaceAddress = "10.22.254.149";
        this.visionPort = 10556;
        this.transmitterAddress = "224.5.23.4";
        this.transmitterInterfaceAddress = "10.22.254.149";
        this.transmitterPort = 10004;
        this.grSimAddress = "127.0.0.1";
        this.grSimInterfaceAddress = "10.22.254.149";
        this.grSimPort = 20011;
        this.refBoxAddress = "224.5.23.1";
        this.refBoxInterfaceAddress = "10.22.254.149";
        this.refBoxPort = 10003;
        this.teamColor = TeamColor.YELLOW.getId();
        this.activeRobots = new int[] {0,1,2,3,4,5,6,7,8,9,10};
        this.autoReboot = false;
        this.isStart = false;
        this.fps = 60.0;
        this.cyclePS = 60.0;
        this.sendPS = 60.0;
        this.radioTypes = new int[] {1};
        this.lostDuration = 2.0;
    }

    public TeamColor getTeamColor() {
        return TeamColor.getColor(this.teamColor);
    }

    public double getSendCycleTime() {
        return 1.0 / this.sendPS;
    }

    public double getCycleTime() {
        return 1.0 / this.cyclePS;
    }

    public double getFrameTime() {
        return 1.0 / this.fps;
    }
}
