package ai_server_cafe.updater;

import ai_server_cafe.config.ConfigManager;
import ai_server_cafe.filter.kalman.FilterFreeRobot;
import ai_server_cafe.model.FilteredRobot;
import ai_server_cafe.model.RawRobot;
import ai_server_cafe.network.proto.ssl.vision.VisionDetection;
import ai_server_cafe.util.TeamColor;
import ai_server_cafe.util.interfaces.IFuncParam1;
import ai_server_cafe.util.interfaces.IFuncParam2;
import ai_server_cafe.util.interfaces.InterfaceHelper;
import ai_server_cafe.util.math.MathHelper;
import org.apache.commons.math3.util.Pair;

import javax.annotation.Nonnull;
import java.util.*;

/**
 *  UpdaterWorldからのアクセスのみOK
 */
public class UpdaterRobot extends AbstractFilteredUpdater<FilteredRobot, RawRobot> {
    private final TeamColor color;
    private final int id;
    private Map<Integer, VisionDetection.Robot> rawRobotMap;

    public UpdaterRobot(TeamColor color, int id, boolean initInvert) {
        super(new FilteredRobot(), initInvert);
        this.color = color;
        this.id = id;
        this.rawRobotMap = new HashMap<>();
        this.value.setLost(true);
        this.setDefaultFilter(FilterFreeRobot.class, ConfigManager.getInstance().getConfig().lostDuration);
    }

    synchronized public boolean isVisible() {
        return !this.value.isLost();
    }

    public int getId() {
        return this.id;
    }

    public TeamColor getColor() {
        return this.color;
    }

    synchronized public void update(@Nonnull VisionDetection.Frame detection) {
        // カメラID
        int cameraId = detection.getCameraId();
        // キャプチャされた時間
        double capturedTime = detection.getTCapture();

        // 保持している生データを更新する
        Optional<VisionDetection.Robot> rawRobotOpt = Optional.empty();
        if (this.color.isYellow()) {
            rawRobotOpt = detection.getRobotsYellowList().stream().filter(InterfaceHelper.getPredicate(new IFuncParam1<Boolean, VisionDetection.Robot>() {
                @Override
                public Boolean function(VisionDetection.Robot robot) {
                    return robot.getRobotId() == UpdaterRobot.this.id;
                }
            })).findAny();
        } else {
            rawRobotOpt = detection.getRobotsBlueList().stream().filter(InterfaceHelper.getPredicate(new IFuncParam1<Boolean, VisionDetection.Robot>() {
                @Override
                public Boolean function(VisionDetection.Robot robot) {
                    return robot.getRobotId() == UpdaterRobot.this.id;
                }
            })).findAny();
        }

        if (rawRobotOpt.isPresent()) {
            this.rawRobotMap.put(cameraId, rawRobotOpt.get());
        } else {
            this.rawRobotMap.remove(cameraId);
        }
        // 最もconfidenceの高い要素を選択して値の更新を行う
        Optional<Pair<Integer, VisionDetection.Robot>> reliableOpt = InterfaceHelper.makePairList(this.rawRobotMap).stream().max(InterfaceHelper.getComparator(new IFuncParam2<Boolean, Pair<Integer, VisionDetection.Robot>, Pair<Integer, VisionDetection.Robot>>() {
            @Override
            public Boolean function(Pair<Integer, VisionDetection.Robot> integerRobotPair, Pair<Integer, VisionDetection.Robot> integerRobotPair2) {
                return integerRobotPair.getValue().getConfidence() <= integerRobotPair.getValue().getConfidence();
            }
        }));
        if (reliableOpt.isPresent()) {
            Pair<Integer, VisionDetection.Robot> pair = reliableOpt.get();
            //System.out.println(pair.getKey());
            // カメラIDが一致していたら値の更新を行う
            // (現在のカメラで新たに検出された or
            // 現在のカメラで検出された値のほうがconfidenceが高かった)
            if (pair.getKey() == cameraId) {
                FilteredRobot rawValue = new FilteredRobot();
                rawValue.setX(pair.getValue().getX());
                rawValue.setY(pair.getValue().getY());
                rawValue.setTheta(pair.getValue().getOrientation());
                rawValue = MathHelper.invert(rawValue, this.invert);

                // 2つのFilterが設定されておらず, かつfilter_initializer_が設定されていたら
                // filter_initializer_でFilterを初期化する
                if (this.initializeFilterFunc.isPresent() && !this.filterSame.isPresent() && !this.filterManual.isPresent()) {
                    this.filterSame = Optional.of(this.initializeFilterFunc.get().function());
                }

                if (this.filterSame.isPresent()) {
                    Optional<FilteredRobot> ofr = this.filterSame.get().updateRaw(Optional.of(rawValue.getRaw()), capturedTime);
                    if (ofr.isPresent()) {
                        this.value = ofr.get();
                        this.value.setLost(false);
                    } else {
                        this.value.setLost(true);
                    }
                } else if(this.filterManual.isPresent()) {
                    this.filterManual.get().updateRaw(Optional.of(rawValue.getRaw()), capturedTime);
                } else {
                    this.value = rawValue;
                    this.value.setLost(false);
                }
            }
            // カメラIDが一致しないときは更新しない
            // (現在のカメラで検出されたがconfidenceが低かった or 現在のカメラで検出されなかった)
        } else {
            // ロスト
            if (this.filterSame.isPresent()) {
                Optional<FilteredRobot> ofr = this.filterSame.get().updateRaw(Optional.empty(), capturedTime);
                if (ofr.isPresent()) {
                    this.value = ofr.get();
                    this.value.setLost(false);
                } else {
                    this.value.setLost(true);
                }
            } else if(this.filterManual.isPresent()) {
                this.filterManual.get().updateRaw(Optional.empty(), capturedTime);
            } else {
                this.value.setLost(true);
            }
        }
    }
}
