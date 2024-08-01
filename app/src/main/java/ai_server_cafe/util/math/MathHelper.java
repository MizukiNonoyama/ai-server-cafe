package ai_server_cafe.util.math;

public class MathHelper {
    public static final double TWO_PI = 2.0 * Math.PI;
    public static final double HALF_PI = 0.5 * Math.PI;

    public static double wrap2PI(double theta) {
        double wrapped = MathHelper.mod(theta, TWO_PI);
        if (wrapped < 0) {
            wrapped += TWO_PI;
        }
        return wrapped;
    }

    public static double wrapPI(double theta) {
        double wrapped = MathHelper.mod(theta, TWO_PI);
        if (wrapped > Math.PI) {
            wrapped -= TWO_PI;
        } else if (wrapped <= -Math.PI) {
            wrapped += TWO_PI;
        }
        return wrapped;
    }


    /**
     * @param a double
     * @param b double
     * @return double a % b
     */
    public static double mod(double a, double b) {
        return a - (int)(a / b) * b;
    }
}
