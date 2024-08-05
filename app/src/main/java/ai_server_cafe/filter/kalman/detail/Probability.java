package ai_server_cafe.filter.kalman.detail;

import com.google.common.math.Quantiles;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import javax.annotation.Nonnull;

public class Probability {
    public static boolean canTrust(double n, double x, double per) {
        ChiSquaredDistribution csd = new ChiSquaredDistribution(n);
        return csd.cumulativeProbability(x) <= per;
    }

    public static double toVariance(double std) {
        return std * std;
    }

    @Nonnull
    public static RealMatrix toCovarianceMatrix(@Nonnull RealMatrix deviation, int blockSize) {
        assert(blockSize > 0);
        assert(deviation.getColumnDimension() == 1);
        assert(deviation.getRowDimension() > 0);
        assert(deviation.getRowDimension() % blockSize == 0);

        // deviation = [d00,d01,d02,  d10,d11,d12,  d20,d21,d22]^T の形式で与えられた情報を
        //
        //   d00*d00         0         0 | d00*d10         0         0 | d00*d20         0         0
        //         0   d01*d01         0 |       0   d01*d11         0 |       0   d01*d21         0
        //         0         0   d02*d02 |       0         0   d02*d12 |       0         0   d02*d22
        // ------------------------------|-----------------------------|----------------------------
        //   d10*d00         0         0 | d10*d10         0         0 | d10*d20         0         0
        //         0   d11*d01         0 |       0   d11*d11         0 |       0   d11*d21         0
        //         0         0   d12*d02 |       0         0   d12*d12 |       0         0   d12*d22
        // ------------------------------|-----------------------------|----------------------------
        //   d20*d00         0         0 | d20*d10         0         0 | d20*d20         0         0
        //         0   d21*d01         0 |       0   d21*d11         0 |       0   d21*d21         0
        //         0         0   d22*d02 |       0         0   d22*d12 |       0         0   d22*d22
        // に変換

        RealMatrix result = MatrixUtils.createRealMatrix(deviation.getRowDimension(), deviation.getRowDimension());

        for (int row = 0; row < deviation.getRowDimension(); ++row) {
            final double deviation_row = deviation.getRow(row)[0];
            final int row_mod       = row % blockSize;

            for (int col = 0; col < deviation.getRowDimension(); ++col) {
                result.setEntry(row, col, (col % blockSize == row_mod) ? deviation_row * deviation.getRow(col)[0] : 0.0);
            }
        }

        return result;
    }
}
