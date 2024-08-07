package ai_server_cafe.updater;

import ai_server_cafe.model.FilteredBall;
import ai_server_cafe.model.RawBall;
import ai_server_cafe.network.proto.ssl.vision.VisionDetection;
import ai_server_cafe.util.interfaces.IFuncParam2;
import ai_server_cafe.util.math.MathHelper;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;


public class UpdaterBall extends AbstractFilteredUpdater<FilteredBall, RawBall> {
    // 各カメラで検出されたボールの生データ
    private Map<Integer, VisionDetection.Ball> rawBallMap;

    public UpdaterBall() {
        super(new FilteredBall());
        this.rawBallMap = new HashMap<>();
    }

    synchronized public void update(@Nonnull VisionDetection.Frame detection) {
        final int cameraId = detection.getCameraId();
        final double captureTime = detection.getTCapture();
        // 検出されたボールの中から, 最も前回と近い値を選択候補に登録する
        // FIXME:
        // 現在の実装は, フィールドにボールが1つしかないと仮定している
        // 1つのカメラで複数のボールが検出された場合, 意図しないデータが選択される可能性がある
        FilteredBall ballNext = MathHelper.invert(this.value.getStateAfter(0.016).orElseGet(new Supplier<FilteredBall>() {
            @Override
            public FilteredBall get() {
                return UpdaterBall.this.value;
            }
        }), this.invert);
        List<VisionDetection.Ball> balls = detection.getBallsList();
        Optional<VisionDetection.Ball> candidate = balls.stream().min(MathHelper.getComparator(
                new IFuncParam2<Boolean, VisionDetection.Ball, VisionDetection.Ball>() {
            @Override
            public Boolean function(VisionDetection.Ball ball, VisionDetection.Ball ball2) {
                return MathHelper.distance2D(ball, ballNext) < MathHelper.distance2D(ball2, ballNext);
            }
        }));
        if (candidate.isPresent()) {
            this.rawBallMap.put(cameraId, candidate.get());
        } else {
            this.rawBallMap.remove(cameraId);
        }
        // 候補の中から, 最も前回と近いボールを求める
        Optional<VisionDetection.Ball> reliable = this.rawBallMap.values().stream().min(
                MathHelper.getComparator(new IFuncParam2<Boolean, VisionDetection.Ball, VisionDetection.Ball>() {
            @Override
            public Boolean function(VisionDetection.Ball ball, VisionDetection.Ball ball2) {
                return MathHelper.distance2D(ball, ballNext) < MathHelper.distance2D(ball2, ballNext);
            }
        }));
        if (reliable.isPresent()) {
            Optional<Integer> oCId = MathHelper.getKey(this.rawBallMap, reliable.get());
            if (oCId.isPresent() && oCId.get() == cameraId) {
                FilteredBall rawFb = new FilteredBall();
                rawFb.setX(reliable.get().getX());
                rawFb.setY(reliable.get().getY());
                rawFb.setZ(reliable.get().getZ());
                rawFb = MathHelper.invert(rawFb, this.invert);
                if (this.filterSame.isPresent()) {
                    // filterSame が設定されていたらFilterを通した値を使う
                    Optional<FilteredBall> fb = this.filterSame.get().updateRaw(Optional.of(rawFb.getRaw()), captureTime);
                    if (fb.isPresent()) {
                        this.value = fb.get();
                    } else {
                        this.value.setLost(true);
                    }
                } else if (this.filterManual.isPresent()) {
                    // filterManual が設定されていたら観測値を通知する
                    this.filterManual.get().updateRaw(Optional.of(rawFb.getRaw()), captureTime);
                } else {
                    // Filterが登録されていない場合はそのままの値を使う
                    this.value = rawFb;
                    this.value.setLost(false);
                }
            }
        } else if(this.filterSame.isPresent()) {
            Optional<FilteredBall> ofb = this.filterSame.get().updateRaw(Optional.empty(), captureTime);
            if (ofb.isPresent()) {
                this.value = ofb.get();
            }
            this.value.setLost(true);
        } else if(this.filterManual.isPresent()) {
            this.filterManual.get().updateRaw(Optional.empty(), captureTime);
        } else {
            this.value.setLost(true);
        }
    }
}
