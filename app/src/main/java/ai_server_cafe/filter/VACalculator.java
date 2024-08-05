package ai_server_cafe.filter;

import ai_server_cafe.model.FilteredRobot;
import ai_server_cafe.model.RawRobot;
import ai_server_cafe.util.math.MathHelper;

import java.util.Optional;

public class VACalculator extends AbstractFilterSame<FilteredRobot, RawRobot> {
    private double prevTime;
    private boolean hasVelocity;
    private boolean hasAcceleration;
    private Optional<FilteredRobot> prevValue;

    @Override
    public Optional<FilteredRobot> updateRaw(Optional<RawRobot> rawValue, double updateTime) {
        if (!rawValue.isPresent()) {
            // 再び見えるようになった時に変な値が計算されないように prev_time_ を初期化する
            prevTime = 0.0;
            hasVelocity = false;
            hasAcceleration = false;
            return Optional.empty();
        }

        FilteredRobot result = new FilteredRobot(rawValue.get());

        if (prevTime == 0.0 || !prevValue.isPresent()) {
            // 初めてupdateが呼ばれたときは速度加速度を計算しない
            result.setVx(0);
            result.setVy(0);
            result.setOmega(0);
            result.setAx(0);
            result.setAy(0);
            result.setAlpha(0);
            hasVelocity = false;
            hasAcceleration = false;
        } else {
            // 前回呼ばれたときからの経過時間
            final double dt = updateTime - prevTime;
            // 非常に短い間隔でupdateが呼び出されたら直前の値を返す
            // (ゼロ除算の原因になるので)
            if (Math.abs(dt) < Double.MIN_VALUE) return prevValue;

            // 速度の計算
            result.setVx((result.getX() - prevValue.get().getX()) / dt);
            result.setVy((result.getY() - prevValue.get().getY()) / dt);

            // 角速度の計算
            // 境界で大きな値になるのを防ぐために, 偏差がpi以下かそうでないかで処理を変える
            // https://github.com/kiksworks/ai-server/pull/63#pullrequestreview-29453425
            final double dTheta = MathHelper.wrap2PI(result.getTheta()) - MathHelper.wrap2PI(prevValue.get().getTheta());
            if (Math.abs(dTheta) < MathHelper.PI) {
                result.setOmega(dTheta / dt);
            } else {
                final double dTheta2 =
                        MathHelper.wrapPI(result.getTheta()) - MathHelper.wrapPI(prevValue.get().getTheta());
                result.setOmega(dTheta2 / dt);
            }

            // 加速度の計算
            if (hasVelocity) {
                result.setAx((result.getVx() - prevValue.get().getVx()) / dt);
                result.setAy((result.getVy() - prevValue.get().getVy()) / dt);
                result.setAlpha((result.getOmega() - prevValue.get().getOmega()) / dt);

                if (hasAcceleration) {
                    result.setJx((result.getAx() - prevValue.get().getAx()) / dt);
                    result.setJy((result.getAy() - prevValue.get().getAy()) / dt);
                    result.setZeta((result.getAlpha() - prevValue.get().getAlpha()) / dt);
                }
                hasAcceleration = true;
            }

            hasVelocity = true;
        }

        prevValue = Optional.of(result);
        prevTime  = updateTime;
        return Optional.of(result);
    }
}
