package ai_server_cafe.updater;

import ai_server_cafe.config.ConfigManager;
import ai_server_cafe.filter.AbstractFilter;
import ai_server_cafe.filter.AbstractFilterManual;
import ai_server_cafe.filter.AbstractFilterSame;
import ai_server_cafe.filter.kalman.FilterBall;
import ai_server_cafe.model.*;
import ai_server_cafe.network.proto.ssl.vision.VisionDetection;
import ai_server_cafe.network.proto.ssl.vision.VisionWrapper;
import ai_server_cafe.util.TeamColor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.*;

public final class UpdaterWorld {
    private static UpdaterWorld instance = null;
    private boolean invert;
    private UpdaterBall updaterBall;
    private Object ob;
    private Field field;
    private List<RawBall> rawBallList;
    private List<RawRobot> rawRobots;
    private List<FilteredRobot> filteredRobots;
    private List<IntegratedRobot> robots;
    private double systemTime;
    private int visionPerSec;
    private Map<Integer, RawRobot> blueRawRobots;
    private Map<Integer, UpdaterRobot> updaterBlueRobotMap;
    private Map<Integer, UpdaterRobot> updaterYellowRobotMap;
    private final Logger logger = LogManager.getLogger("world updater");

    private UpdaterWorld() {
        this.field = new Field();
        this.rawBallList = new ArrayList<>();
        this.blueRawRobots = new HashMap<>();
        this.updaterBlueRobotMap = new HashMap<>();
        this.updaterYellowRobotMap = new HashMap<>();
        this.invert = false;
        this.updaterBall = new UpdaterBall(false);
        double lostDuration = ConfigManager.getInstance().getConfig().lostDuration;
        this.updaterBall.setFilterSame(new FilterBall(lostDuration));
    }

    public static UpdaterWorld getInstance() {
        if (instance == null) {
            instance = new UpdaterWorld();
        }
        return instance;
    }

    synchronized public void update(@Nonnull VisionWrapper.Packet packet, int perSec) {
        this.visionPerSec = perSec;
        // update field
        if (packet.hasGeometry()) {
            if (packet.getGeometry().getField().hasFieldWidth()) this.field.setGameHeight(packet.getGeometry().getField().getFieldWidth());
            if (packet.getGeometry().getField().hasFieldLength()) this.field.setGameWidth(packet.getGeometry().getField().getFieldLength());
            if (packet.getGeometry().getField().hasPenaltyAreaWidth()) this.field.setPenaltyWidth(packet.getGeometry().getField().getPenaltyAreaWidth());
            if (packet.getGeometry().getField().hasPenaltyAreaDepth()) this.field.setPenaltyLength(packet.getGeometry().getField().getPenaltyAreaDepth());
            if (packet.getGeometry().getField().hasGoalDepth()) this.field.setGoalLength(packet.getGeometry().getField().getGoalDepth());
            if (packet.getGeometry().getField().hasGoalWidth()) this.field.setGoalWidth(packet.getGeometry().getField().getGoalWidth());
        }

        if (!packet.getDetection().getBallsList().isEmpty()) this.rawBallList.clear();
        for (VisionDetection.Ball detectedBall : packet.getDetection().getBallsList()) {
            this.rawBallList.add(new RawBall(detectedBall.getX(), detectedBall.getY(), detectedBall.getZ()));
        }
        // update ball
        this.updaterBall.update(packet.getDetection());

        // update robot
        for (VisionDetection.Robot robot : packet.getDetection().getRobotsBlueList()) {
            int id = robot.getRobotId();
            if (!this.updaterBlueRobotMap.containsKey(id)) {
                this.updaterBlueRobotMap.put(id, new UpdaterRobot(TeamColor.BLUE, id, this.invert));
            }
        }
        for (VisionDetection.Robot robot : packet.getDetection().getRobotsYellowList()) {
            int id = robot.getRobotId();
            if (!this.updaterYellowRobotMap.containsKey(id)) {
                this.updaterYellowRobotMap.put(id, new UpdaterRobot(TeamColor.YELLOW, id, this.invert));
            }
        }
        for (Map.Entry<Integer, UpdaterRobot> entry : this.updaterBlueRobotMap.entrySet()) {
            entry.getValue().update(packet.getDetection());
        }
        for (Map.Entry<Integer, UpdaterRobot> entry : this.updaterYellowRobotMap.entrySet()) {
            entry.getValue().update(packet.getDetection());
        }

        for (int i = 0; i < 11; i++) {
            try {
                VisionDetection.Robot blue = packet.getDetection().getRobotsBlue(i);
                this.blueRawRobots.put(blue.getRobotId(), new RawRobot(blue.getX(), blue.getY(), blue.getOrientation()));
            } catch (IndexOutOfBoundsException e) {
                // DO NOTHING
                // Only robot is not visible
            }
        }
    }

    /**
     * FilterControlledRobot用のSendCommandの更新
      */
    synchronized public void updateSendCommand(@Nonnull TeamColor color, int id, SendCommand command) {
        Optional<FilteredRobot> robotOptional = Optional.empty();
        if (color.isYellow()) {
            if (this.updaterYellowRobotMap.containsKey(id)) {
                robotOptional = Optional.of(this.updaterYellowRobotMap.get(id).getValue());
            }
        } else {
            if (this.updaterBlueRobotMap.containsKey(id)) {
                robotOptional = Optional.of(this.updaterBlueRobotMap.get(id).getValue());
            }
        }
        if (robotOptional.isPresent() && !robotOptional.get().isLost()) {
            FilteredRobot robot = robotOptional.get();
            double st = Math.sin(robot.getTheta());
            double ct = Math.cos(robot.getTheta());
            double vxf = ct * command.getVx() - st * command.getVy();
            double vyf = st * command.getVx() + ct * command.getVy();
            double omega = command.getOmega();

            if (color.isYellow()) {
                if (this.updaterYellowRobotMap.containsKey(id)) {
                    if (this.updaterYellowRobotMap.get(id).filterManual.isPresent()) {
                        this.updaterYellowRobotMap.get(id).filterManual.get().updateObserver(vxf, vyf, omega);
                    }
                }
            } else {
                if (this.updaterBlueRobotMap.containsKey(id)) {
                    if (this.updaterBlueRobotMap.get(id).filterManual.isPresent()) {
                        this.updaterBlueRobotMap.get(id).filterManual.get().updateObserver(vxf, vyf, omega);
                    }
                }
            }
        }
    }

    synchronized public void clearAllFilterRobot(@Nonnull TeamColor color, int id) {
        if (color.isYellow()) {
            if (!this.updaterYellowRobotMap.containsKey(id)) {
                this.updaterYellowRobotMap.put(id, new UpdaterRobot(color, id, this.invert));
            }
            this.updaterYellowRobotMap.get(id).clearAllFilters();
        } else {
            if (!this.updaterBlueRobotMap.containsKey(id)) {
                this.updaterBlueRobotMap.put(id, new UpdaterRobot(color, id, this.invert));
            }
            this.updaterBlueRobotMap.get(id).clearAllFilters();
        }
    }

    synchronized public void clearFilterRobot(@Nonnull TeamColor color, int id) {
        if (color.isYellow()) {
            if (!this.updaterYellowRobotMap.containsKey(id)) {
                this.updaterYellowRobotMap.put(id, new UpdaterRobot(color, id, this.invert));
            }
            this.updaterYellowRobotMap.get(id).clearFilters();
        } else {
            if (!this.updaterBlueRobotMap.containsKey(id)) {
                this.updaterBlueRobotMap.put(id, new UpdaterRobot(color, id, this.invert));
            }
            this.updaterBlueRobotMap.get(id).clearFilters();
        }
    }
    /**
     * コンフィグが変更されたタイミングで呼び出す
     * @param color
     * @param id
     * @param filter
     */
    synchronized public void setFilterRobot(@Nonnull TeamColor color, int id, AbstractFilter<FilteredRobot, RawRobot> filter) {
        if (color.isYellow()) {
            if (!this.updaterYellowRobotMap.containsKey(id)) {
                this.updaterYellowRobotMap.put(id, new UpdaterRobot(color, id, this.invert));
            }
            this.updaterYellowRobotMap.get(id).clearFilters();
            if (filter instanceof AbstractFilterSame<FilteredRobot, RawRobot>) {
                this.updaterYellowRobotMap.get(id).setFilterSame((AbstractFilterSame<FilteredRobot, RawRobot>) filter);
            } else if(filter instanceof AbstractFilterManual<FilteredRobot, RawRobot>) {
                this.updaterYellowRobotMap.get(id).setFilterManual((AbstractFilterManual<FilteredRobot, RawRobot>) filter);
            }
        } else {
            if (!this.updaterBlueRobotMap.containsKey(id)) {
                this.updaterBlueRobotMap.put(id, new UpdaterRobot(color, id, this.invert));
            }
            this.updaterBlueRobotMap.get(id).clearFilters();
            if (filter instanceof AbstractFilterSame<FilteredRobot, RawRobot>) {
                this.updaterBlueRobotMap.get(id).setFilterSame((AbstractFilterSame<FilteredRobot, RawRobot>) filter);
            } else if(filter instanceof AbstractFilterManual<FilteredRobot, RawRobot>) {
                this.updaterBlueRobotMap.get(id).setFilterManual((AbstractFilterManual<FilteredRobot, RawRobot>) filter);
            }
        }
    }

    synchronized public void setFilterBall(AbstractFilter<FilteredBall, RawBall> filter) {
        this.updaterBall.clearFilters();
        if (filter instanceof AbstractFilterSame<FilteredBall, RawBall>) {
            this.updaterBall.setFilterSame((AbstractFilterSame<FilteredBall, RawBall>) filter);
        } else if(filter instanceof AbstractFilterManual<FilteredBall, RawBall>) {
            this.updaterBall.setFilterManual((AbstractFilterManual<FilteredBall, RawBall>) filter);
        }
    }

    synchronized public void clearFilterBall() {
        this.updaterBall.clearFilters();
    }

    synchronized public void setInvert(boolean value) {
        this.invert = value;
        this.updaterBall.setInvert(this.invert);
        for (UpdaterRobot updaterRobot : this.updaterBlueRobotMap.values()) {
            updaterRobot.setInvert(this.invert);
        }
        for (UpdaterRobot updaterRobot : this.updaterYellowRobotMap.values()) {
            updaterRobot.setInvert(this.invert);
        }
    }

    synchronized public Field getField() {
        return this.field;
    }

    synchronized public List<RawBall> getRawBallList() {
        return this.rawBallList;
    }

    synchronized public int getVisionPerSec() {
        return this.visionPerSec;
    }

    synchronized public FilteredBall getBall() {
        return this.updaterBall.getValue().copy();
    }

    synchronized public Map<Integer, RawRobot> getBlueRawRobots() {
        return this.blueRawRobots;
    }

    @Nonnull
    synchronized public Map<Integer, FilteredRobot> getBlueFilteredRobots() {
        Map<Integer, FilteredRobot> map = new HashMap<>();
        for (Map.Entry<Integer, UpdaterRobot> entry : this.updaterBlueRobotMap.entrySet()) {
            if (entry.getValue().isVisible()) {
                map.put(entry.getKey(), entry.getValue().getValue());
            }
        }
        return map;
    }

    @Nonnull
    synchronized public Map<Integer, FilteredRobot> getYellowFilteredRobots() {
        Map<Integer, FilteredRobot> map = new HashMap<>();
        for (Map.Entry<Integer, UpdaterRobot> entry : this.updaterYellowRobotMap.entrySet()) {
            if (entry.getValue().isVisible()) {
                map.put(entry.getKey(), entry.getValue().getValue());
            }
        }
        return map;
    }
}
