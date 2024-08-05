package ai_server_cafe.model;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class FieldObject {
    // 初期値は0
    protected double x = 0;
    protected double y = 0;
    protected double z = 0;
    protected double vx = 0;
    protected double vy = 0;
    protected double vz = 0;
    protected double ax = 0;
    protected double ay = 0;
    protected double az = 0;
    protected double jx = 0;
    protected double jy = 0;
    protected double jz = 0;
    protected double theta = 0;
    protected double omega = 0;
    protected double alpha = 0;
    protected double zeta = 0;

    public void setJx(double jx) {
        this.jx = jx;
    }

    public void setJy(double jy) {
        this.jy = jy;
    }

    public void setJz(double jz) {
        this.jz = jz;
    }

    public void setZeta(double zeta) {
        this.zeta = zeta;
    }

    public void setAx(double ax) {
        this.ax = ax;
    }

    public void setAy(double ay) {
        this.ay = ay;
    }

    public void setAz(double az) {
        this.az = az;
    }

    public void setVx(double vx) {
        this.vx = vx;
    }

    public void setVy(double vy) {
        this.vy = vy;
    }

    public void setVz(double vz) {
        this.vz = vz;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public void setOmega(double omega) {
        this.omega = omega;
    }

    public void setTheta(double theta) {
        this.theta = theta;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public double getVx() {
        return vx;
    }

    public double getVy() {
        return vy;
    }

    public double getVz() {
        return vz;
    }

    public double getAx() {
        return ax;
    }

    public double getAy() {
        return ay;
    }

    public double getAz() {
        return az;
    }

    public double getTheta() {
        return theta;
    }

    public double getOmega() {
        return omega;
    }

    public double getAlpha() {
        return alpha;
    }

    public double getJx() {
        return this.jx;
    }

    public double getJy() {
        return this.jy;
    }

    public double getJz() {
        return this.jz;
    }

    public double getZeta() {
        return this.zeta;
    }

    public Vector3D positionXYTheta() {
        return new Vector3D(this.x, this.y, this.theta);
    }

    public Vector3D velocityXYTheta() {
        return new Vector3D(this.vx, this.vy, this.omega);
    }
}
