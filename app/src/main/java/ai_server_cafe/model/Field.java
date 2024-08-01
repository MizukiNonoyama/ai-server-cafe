package ai_server_cafe.model;

public class Field {
    private double width = 12000.0;
    private double height = 9000.0;
    private final double fieldMargin = 700.0;
    private final double gameMargin = 300.0;
    private final double centerCircleRadius = 500.0;
    private double penaltyWidth = 3600.0;
    private double penaltyLength = 1800.0;
    private double goalLength = 200.0;
    private double goalWidth = 1800.0;

    public void setGameWidth(double value) {
        this.width = value;
    }

    public void setGameHeight(double value) {
        this.height = value;
    }

    public void setPenaltyWidth(double penaltyWidth) {
        this.penaltyWidth = penaltyWidth;
    }

    public void setPenaltyLength(double penaltyLength) {
        this.penaltyLength = penaltyLength;
    }

    public void setGoalLength(double goalLength) {
        this.goalLength = goalLength;
    }

    public void setGoalWidth(double goalWidth) {
        this.goalWidth = goalWidth;
    }

    public double getGameWidth() {
        return this.width;
    }

    public double getGameHeight() {
        return this.height;
    }

    public double getCarpetWidth() {
        return this.width + 2.0 * fieldMargin;
    }

    public double getCarpetHeight() {
        return this.height + 2.0 * fieldMargin;
    }

    public double getFieldWidth() {
        return this.width + 2.0 * gameMargin;
    }

    public double getFieldHeight() {
        return this.height + 2.0 * gameMargin;
    }

    public double getCenterCircle() {
        return this.centerCircleRadius;
    }

    public double getPenaltyWidth() {
        return this.penaltyWidth;
    }

    public double getPenaltyLength() {
        return this.penaltyLength;
    }

    public double getGoalLength() {
        return this.goalLength;
    }

    public double getGoalWidth() {
        return this.goalWidth;
    }
}
