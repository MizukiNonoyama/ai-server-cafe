package ai_server_cafe.util.math;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import javax.annotation.Nonnull;

public class MathHelper {
    public static final double TWO_PI = 2.0 * Math.PI;
    public static final double HALF_PI = 0.5 * Math.PI;
    public static final double PI = Math.PI;

    public static final Vector3D AXIS_X = new Vector3D(1.0, 0.0, 0.0);
    public static final Vector3D AXIS_Y = new Vector3D(0.0, 1.0, 0.0);
    public static final Vector3D AXIS_Z = new Vector3D(0.0, 0.0, 1.0);

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

    @Nonnull
    public static Vector2D applyRotation2D(@Nonnull Vector2D vector2D, double radian) {
        Rotation rot = new Rotation(MathHelper.AXIS_Z, radian);
        Vector3D vector3D = rot.applyTo(new Vector3D(vector2D.getX(), vector2D.getY(), 0.0));
        return new Vector2D(vector3D.getX(), vector3D.getY());
    }

    @Nonnull
    public static RealMatrix makeIdentity(int rows, int columns) {
        RealMatrix matrix = MatrixUtils.createRealMatrix(rows, columns);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; i++) {
                matrix.setEntry(i, j, i == j ? 1.0 : 0.0);
            }
        }
        return matrix;
    }

    @Nonnull
    public static RealMatrix makeDiagonal(@Nonnull double[] values) {
        RealMatrix matrix = MathHelper.makeFill(values.length, values.length, 0.0);
        for (int i = 0; i < values.length; i++) {
            matrix.setEntry(i, i, values[i]);
        }
        return matrix;
    }

    @Nonnull
    public static RealMatrix makeVectorMatrix(@Nonnull double[] values) {
        RealMatrix matrix = MatrixUtils.createRealMatrix(values.length, 1);
        for (int i = 0; i < values.length; i++) {
            matrix.setEntry(i, 0, values[i]);
        }
        return matrix;
    }

    public static RealMatrix makeFill(int rows, int columns, double filler) {
        RealMatrix matrix = MatrixUtils.createRealMatrix(rows, columns);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; i++) {
                matrix.setEntry(i, j, filler);
            }
        }
        return matrix;
    }
}
