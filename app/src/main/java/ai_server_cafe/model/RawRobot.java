package ai_server_cafe.model;

public class RawRobot {
    private double x;
    private double y;
    private double theta;

    public RawRobot() {
        this.x = 0;
        this.y = 0;
        this.theta = 0;
    }

    public RawRobot(double x, double y, double theta) {
        this.x = x;
        this.y = y;
        this.theta = theta;
    }

    public double getX() {
        return x;
    }

    public double getTheta() {
        return theta;
    }

    public double getY() {
        return y;
    }
}
