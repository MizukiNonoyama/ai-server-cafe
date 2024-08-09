package ai_server_cafe.util.math;

import ai_server_cafe.model.FieldObject;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationConvention;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.util.Pair;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;

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
        Rotation rot = new Rotation(MathHelper.AXIS_Z, radian, RotationConvention.VECTOR_OPERATOR);
        Vector3D vector3D = rot.applyTo(new Vector3D(vector2D.getX(), vector2D.getY(), 0.0));
        return new Vector2D(vector3D.getX(), vector3D.getY());
    }

    @Nonnull
    public static Vector3D applyRotation(@Nonnull Vector3D vector3D, double radian, Vector3D axis) {
        Rotation rot = new Rotation(axis, radian, RotationConvention.VECTOR_OPERATOR);
        return rot.applyTo(vector3D);
    }

    @Nonnull
    public static RealMatrix makeIdentity(int rows, int columns) {
        RealMatrix matrix = MatrixUtils.createRealMatrix(rows, columns);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
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
        matrix.setColumn(0, values);
        return matrix;
    }

    @Nonnull
    public static RealMatrix makeFill(int rows, int columns, double filler) {
        RealMatrix matrix = MatrixUtils.createRealMatrix(rows, columns);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                matrix.setEntry(i, j, filler);
            }
        }
        return matrix;
    }

    public static boolean isEpsilon(double delta) {
        return Math.abs(delta) < Double.MIN_VALUE;
    }

    public static boolean isInfinity(double value) {
        return Double.POSITIVE_INFINITY == value || Double.NEGATIVE_INFINITY == value;
    }

    public static boolean isNaN(double value) {
        return Double.isNaN(value);
    }

    @SuppressWarnings("unchecked")
    public static <T extends FieldObject> T invert(T t, boolean inverse) {
        try {
            if (inverse) {
                FieldObject invert = t.invert();
                if(t.getClass().isInstance(invert)) {
                    return (T) invert;
                } else {
                    throw new ClassCastException(invert.getClass().getName() +
                            " is not instance of " + t.getClass().getName());
                }
            }
            FieldObject copy = t.copy();
            if(t.getClass().isInstance(copy)) {
                return (T) copy;
            } else {
                throw new ClassCastException(copy.getClass().getName() +
                        " is not instance of " + t.getClass().getName());
            }
        } catch(ClassCastException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Nonnull
    public static Vector2D position2D(@Nonnull Object o) {
        try {
            Object x = (o.getClass().getMethod("getX").invoke(o));
            Object y = (o.getClass().getMethod("getY").invoke(o));
            double dx = x instanceof Float ? (double)(float)x : (double)x;
            double dy = y instanceof Float ? (double)(float)y : (double)y;
            return new Vector2D(dx, dy);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * zが存在するオブジェクトに対して x, y, z のベクトルを返す
     * @param o
     * @return
     */
    @Nonnull
    public static Vector3D position3D(@Nonnull Object o) {
        try {
            Object x = (o.getClass().getMethod("getX").invoke(o));
            Object y = (o.getClass().getMethod("getY").invoke(o));
            Object z = (o.getClass().getMethod("getZ").invoke(o));
            double dx = x instanceof Float ? (double)(float)x : (double)x;
            double dy = y instanceof Float ? (double)(float)y : (double)y;
            double dz = z instanceof Float ? (double)(float)z : (double)z;
            return new Vector3D(dx, dy, dz);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Nonnull
    public static Pair<Vector2D, Double> position2DTheta(@Nonnull Object o) {
        try {
            Object x = o.getClass().getMethod("getX").invoke(o);
            Object y = o.getClass().getMethod("getY").invoke(o);
            Object theta = o.getClass().getMethod("getTheta").invoke(o);
            double dx = x instanceof Float ? (double)(float)x : (double)x;
            double dy = y instanceof Float ? (double)(float)y : (double)y;
            double dTheta = theta instanceof Float ? (double)(float)theta : (double)theta;
            return new Pair<>(new Vector2D(dx, dy), dTheta);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static double distance2D(@Nonnull Object o1, @Nonnull Object o2) {
        return position2D(o1).subtract(position2D(o2)).getNorm();
    }

    public static double distance3D(@Nonnull Object o1, @Nonnull Object o2) {
        return position3D(o1).subtract(position3D(o2)).getNorm();
    }

    @Nonnull
    public static Vector2D velocity2D(@Nonnull Object o) {
        try {
            Object x = o.getClass().getMethod("getVx").invoke(o);
            Object y = o.getClass().getMethod("getVy").invoke(o);
            double dx = x instanceof Float ? (double)(float)x : (double)x;
            double dy = y instanceof Float ? (double)(float)y : (double)y;
            return new Vector2D(dx, dy);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * vzが存在するオブジェクトに対して vx, vy, vz のベクトルを返す
     * @param o
     * @return
     */
    @Nonnull
    public static Vector3D velocity3D(@Nonnull Object o) {
        try {
            Object x = (o.getClass().getMethod("getVx").invoke(o));
            Object y = (o.getClass().getMethod("getVy").invoke(o));
            Object z = (o.getClass().getMethod("getVz").invoke(o));
            double dx = x instanceof Float ? (double)(float)x : (double)x;
            double dy = y instanceof Float ? (double)(float)y : (double)y;
            double dz = z instanceof Float ? (double)(float)z : (double)z;
            return new Vector3D(dx, dy, dz);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
