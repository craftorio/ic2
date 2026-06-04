// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.dense.misc;

import ic2.shades.org.ejml.data.RowD1Matrix64F;

public class UnrolledDeterminantFromMinor
{
    public static final int MAX = 6;
    
    public static double det(final RowD1Matrix64F mat) {
        switch (mat.numRows) {
            case 2: {
                return det2(mat);
            }
            case 3: {
                return det3(mat);
            }
            case 4: {
                return det4(mat);
            }
            case 5: {
                return det5(mat);
            }
            case 6: {
                return det6(mat);
            }
            default: {
                throw new IllegalArgumentException("Not supported");
            }
        }
    }
    
    public static double det2(final RowD1Matrix64F mat) {
        final double[] m = mat.data;
        return m[0] * m[3] - m[1] * m[2];
    }
    
    public static double det3(final RowD1Matrix64F mat) {
        final double[] m = mat.data;
        final double a11 = m[0];
        final double a12 = m[1];
        final double a13 = m[2];
        final double a14 = m[3];
        final double a15 = m[4];
        final double a16 = m[5];
        final double a17 = m[6];
        final double a18 = m[7];
        final double a19 = m[8];
        final double a20 = a11 * (a15 * a19 - a16 * a18);
        final double b = a12 * (a14 * a19 - a16 * a17);
        final double c = a13 * (a14 * a18 - a17 * a15);
        return a20 - b + c;
    }
    
    public static double det4(final RowD1Matrix64F mat) {
        final double[] data = mat.data;
        double a11 = data[5];
        double a12 = data[6];
        double a13 = data[7];
        double a14 = data[9];
        double a15 = data[10];
        double a16 = data[11];
        double a17 = data[13];
        double a18 = data[14];
        double a19 = data[15];
        double ret = 0.0;
        ret += data[0] * (a11 * (a15 * a19 - a16 * a18) - a12 * (a14 * a19 - a16 * a17) + a13 * (a14 * a18 - a15 * a17));
        a11 = data[4];
        a14 = data[8];
        a17 = data[12];
        ret -= data[1] * (a11 * (a15 * a19 - a16 * a18) - a12 * (a14 * a19 - a16 * a17) + a13 * (a14 * a18 - a15 * a17));
        a12 = data[5];
        a15 = data[9];
        a18 = data[13];
        ret += data[2] * (a11 * (a15 * a19 - a16 * a18) - a12 * (a14 * a19 - a16 * a17) + a13 * (a14 * a18 - a15 * a17));
        a13 = data[6];
        a16 = data[10];
        a19 = data[14];
        ret -= data[3] * (a11 * (a15 * a19 - a16 * a18) - a12 * (a14 * a19 - a16 * a17) + a13 * (a14 * a18 - a15 * a17));
        return ret;
    }
    
    public static double det5(final RowD1Matrix64F mat) {
        final double[] data = mat.data;
        double a11 = data[6];
        double a12 = data[7];
        double a13 = data[8];
        double a14 = data[9];
        double a15 = data[11];
        double a16 = data[12];
        double a17 = data[13];
        double a18 = data[14];
        double a19 = data[16];
        double a20 = data[17];
        double a21 = data[18];
        double a22 = data[19];
        double a23 = data[21];
        double a24 = data[22];
        double a25 = data[23];
        double a26 = data[24];
        double ret = 0.0;
        ret += data[0] * (a11 * (a16 * (a21 * a26 - a22 * a25) - a17 * (a20 * a26 - a22 * a24) + a18 * (a20 * a25 - a21 * a24)) - a12 * (a15 * (a21 * a26 - a22 * a25) - a17 * (a19 * a26 - a22 * a23) + a18 * (a19 * a25 - a21 * a23)) + a13 * (a15 * (a20 * a26 - a22 * a24) - a16 * (a19 * a26 - a22 * a23) + a18 * (a19 * a24 - a20 * a23)) - a14 * (a15 * (a20 * a25 - a21 * a24) - a16 * (a19 * a25 - a21 * a23) + a17 * (a19 * a24 - a20 * a23)));
        a11 = data[5];
        a15 = data[10];
        a19 = data[15];
        a23 = data[20];
        ret -= data[1] * (a11 * (a16 * (a21 * a26 - a22 * a25) - a17 * (a20 * a26 - a22 * a24) + a18 * (a20 * a25 - a21 * a24)) - a12 * (a15 * (a21 * a26 - a22 * a25) - a17 * (a19 * a26 - a22 * a23) + a18 * (a19 * a25 - a21 * a23)) + a13 * (a15 * (a20 * a26 - a22 * a24) - a16 * (a19 * a26 - a22 * a23) + a18 * (a19 * a24 - a20 * a23)) - a14 * (a15 * (a20 * a25 - a21 * a24) - a16 * (a19 * a25 - a21 * a23) + a17 * (a19 * a24 - a20 * a23)));
        a12 = data[6];
        a16 = data[11];
        a20 = data[16];
        a24 = data[21];
        ret += data[2] * (a11 * (a16 * (a21 * a26 - a22 * a25) - a17 * (a20 * a26 - a22 * a24) + a18 * (a20 * a25 - a21 * a24)) - a12 * (a15 * (a21 * a26 - a22 * a25) - a17 * (a19 * a26 - a22 * a23) + a18 * (a19 * a25 - a21 * a23)) + a13 * (a15 * (a20 * a26 - a22 * a24) - a16 * (a19 * a26 - a22 * a23) + a18 * (a19 * a24 - a20 * a23)) - a14 * (a15 * (a20 * a25 - a21 * a24) - a16 * (a19 * a25 - a21 * a23) + a17 * (a19 * a24 - a20 * a23)));
        a13 = data[7];
        a17 = data[12];
        a21 = data[17];
        a25 = data[22];
        ret -= data[3] * (a11 * (a16 * (a21 * a26 - a22 * a25) - a17 * (a20 * a26 - a22 * a24) + a18 * (a20 * a25 - a21 * a24)) - a12 * (a15 * (a21 * a26 - a22 * a25) - a17 * (a19 * a26 - a22 * a23) + a18 * (a19 * a25 - a21 * a23)) + a13 * (a15 * (a20 * a26 - a22 * a24) - a16 * (a19 * a26 - a22 * a23) + a18 * (a19 * a24 - a20 * a23)) - a14 * (a15 * (a20 * a25 - a21 * a24) - a16 * (a19 * a25 - a21 * a23) + a17 * (a19 * a24 - a20 * a23)));
        a14 = data[8];
        a18 = data[13];
        a22 = data[18];
        a26 = data[23];
        ret += data[4] * (a11 * (a16 * (a21 * a26 - a22 * a25) - a17 * (a20 * a26 - a22 * a24) + a18 * (a20 * a25 - a21 * a24)) - a12 * (a15 * (a21 * a26 - a22 * a25) - a17 * (a19 * a26 - a22 * a23) + a18 * (a19 * a25 - a21 * a23)) + a13 * (a15 * (a20 * a26 - a22 * a24) - a16 * (a19 * a26 - a22 * a23) + a18 * (a19 * a24 - a20 * a23)) - a14 * (a15 * (a20 * a25 - a21 * a24) - a16 * (a19 * a25 - a21 * a23) + a17 * (a19 * a24 - a20 * a23)));
        return ret;
    }
    
    public static double det6(final RowD1Matrix64F mat) {
        final double[] data = mat.data;
        double a11 = data[7];
        double a12 = data[8];
        double a13 = data[9];
        double a14 = data[10];
        double a15 = data[11];
        double a16 = data[13];
        double a17 = data[14];
        double a18 = data[15];
        double a19 = data[16];
        double a20 = data[17];
        double a21 = data[19];
        double a22 = data[20];
        double a23 = data[21];
        double a24 = data[22];
        double a25 = data[23];
        double a26 = data[25];
        double a27 = data[26];
        double a28 = data[27];
        double a29 = data[28];
        double a30 = data[29];
        double a31 = data[31];
        double a32 = data[32];
        double a33 = data[33];
        double a34 = data[34];
        double a35 = data[35];
        double ret = 0.0;
        ret += data[0] * (a11 * (a17 * (a23 * (a29 * a35 - a30 * a34) - a24 * (a28 * a35 - a30 * a33) + a25 * (a28 * a34 - a29 * a33)) - a18 * (a22 * (a29 * a35 - a30 * a34) - a24 * (a27 * a35 - a30 * a32) + a25 * (a27 * a34 - a29 * a32)) + a19 * (a22 * (a28 * a35 - a30 * a33) - a23 * (a27 * a35 - a30 * a32) + a25 * (a27 * a33 - a28 * a32)) - a20 * (a22 * (a28 * a34 - a29 * a33) - a23 * (a27 * a34 - a29 * a32) + a24 * (a27 * a33 - a28 * a32))) - a12 * (a16 * (a23 * (a29 * a35 - a30 * a34) - a24 * (a28 * a35 - a30 * a33) + a25 * (a28 * a34 - a29 * a33)) - a18 * (a21 * (a29 * a35 - a30 * a34) - a24 * (a26 * a35 - a30 * a31) + a25 * (a26 * a34 - a29 * a31)) + a19 * (a21 * (a28 * a35 - a30 * a33) - a23 * (a26 * a35 - a30 * a31) + a25 * (a26 * a33 - a28 * a31)) - a20 * (a21 * (a28 * a34 - a29 * a33) - a23 * (a26 * a34 - a29 * a31) + a24 * (a26 * a33 - a28 * a31))) + a13 * (a16 * (a22 * (a29 * a35 - a30 * a34) - a24 * (a27 * a35 - a30 * a32) + a25 * (a27 * a34 - a29 * a32)) - a17 * (a21 * (a29 * a35 - a30 * a34) - a24 * (a26 * a35 - a30 * a31) + a25 * (a26 * a34 - a29 * a31)) + a19 * (a21 * (a27 * a35 - a30 * a32) - a22 * (a26 * a35 - a30 * a31) + a25 * (a26 * a32 - a27 * a31)) - a20 * (a21 * (a27 * a34 - a29 * a32) - a22 * (a26 * a34 - a29 * a31) + a24 * (a26 * a32 - a27 * a31))) - a14 * (a16 * (a22 * (a28 * a35 - a30 * a33) - a23 * (a27 * a35 - a30 * a32) + a25 * (a27 * a33 - a28 * a32)) - a17 * (a21 * (a28 * a35 - a30 * a33) - a23 * (a26 * a35 - a30 * a31) + a25 * (a26 * a33 - a28 * a31)) + a18 * (a21 * (a27 * a35 - a30 * a32) - a22 * (a26 * a35 - a30 * a31) + a25 * (a26 * a32 - a27 * a31)) - a20 * (a21 * (a27 * a33 - a28 * a32) - a22 * (a26 * a33 - a28 * a31) + a23 * (a26 * a32 - a27 * a31))) + a15 * (a16 * (a22 * (a28 * a34 - a29 * a33) - a23 * (a27 * a34 - a29 * a32) + a24 * (a27 * a33 - a28 * a32)) - a17 * (a21 * (a28 * a34 - a29 * a33) - a23 * (a26 * a34 - a29 * a31) + a24 * (a26 * a33 - a28 * a31)) + a18 * (a21 * (a27 * a34 - a29 * a32) - a22 * (a26 * a34 - a29 * a31) + a24 * (a26 * a32 - a27 * a31)) - a19 * (a21 * (a27 * a33 - a28 * a32) - a22 * (a26 * a33 - a28 * a31) + a23 * (a26 * a32 - a27 * a31))));
        a11 = data[6];
        a16 = data[12];
        a21 = data[18];
        a26 = data[24];
        a31 = data[30];
        ret -= data[1] * (a11 * (a17 * (a23 * (a29 * a35 - a30 * a34) - a24 * (a28 * a35 - a30 * a33) + a25 * (a28 * a34 - a29 * a33)) - a18 * (a22 * (a29 * a35 - a30 * a34) - a24 * (a27 * a35 - a30 * a32) + a25 * (a27 * a34 - a29 * a32)) + a19 * (a22 * (a28 * a35 - a30 * a33) - a23 * (a27 * a35 - a30 * a32) + a25 * (a27 * a33 - a28 * a32)) - a20 * (a22 * (a28 * a34 - a29 * a33) - a23 * (a27 * a34 - a29 * a32) + a24 * (a27 * a33 - a28 * a32))) - a12 * (a16 * (a23 * (a29 * a35 - a30 * a34) - a24 * (a28 * a35 - a30 * a33) + a25 * (a28 * a34 - a29 * a33)) - a18 * (a21 * (a29 * a35 - a30 * a34) - a24 * (a26 * a35 - a30 * a31) + a25 * (a26 * a34 - a29 * a31)) + a19 * (a21 * (a28 * a35 - a30 * a33) - a23 * (a26 * a35 - a30 * a31) + a25 * (a26 * a33 - a28 * a31)) - a20 * (a21 * (a28 * a34 - a29 * a33) - a23 * (a26 * a34 - a29 * a31) + a24 * (a26 * a33 - a28 * a31))) + a13 * (a16 * (a22 * (a29 * a35 - a30 * a34) - a24 * (a27 * a35 - a30 * a32) + a25 * (a27 * a34 - a29 * a32)) - a17 * (a21 * (a29 * a35 - a30 * a34) - a24 * (a26 * a35 - a30 * a31) + a25 * (a26 * a34 - a29 * a31)) + a19 * (a21 * (a27 * a35 - a30 * a32) - a22 * (a26 * a35 - a30 * a31) + a25 * (a26 * a32 - a27 * a31)) - a20 * (a21 * (a27 * a34 - a29 * a32) - a22 * (a26 * a34 - a29 * a31) + a24 * (a26 * a32 - a27 * a31))) - a14 * (a16 * (a22 * (a28 * a35 - a30 * a33) - a23 * (a27 * a35 - a30 * a32) + a25 * (a27 * a33 - a28 * a32)) - a17 * (a21 * (a28 * a35 - a30 * a33) - a23 * (a26 * a35 - a30 * a31) + a25 * (a26 * a33 - a28 * a31)) + a18 * (a21 * (a27 * a35 - a30 * a32) - a22 * (a26 * a35 - a30 * a31) + a25 * (a26 * a32 - a27 * a31)) - a20 * (a21 * (a27 * a33 - a28 * a32) - a22 * (a26 * a33 - a28 * a31) + a23 * (a26 * a32 - a27 * a31))) + a15 * (a16 * (a22 * (a28 * a34 - a29 * a33) - a23 * (a27 * a34 - a29 * a32) + a24 * (a27 * a33 - a28 * a32)) - a17 * (a21 * (a28 * a34 - a29 * a33) - a23 * (a26 * a34 - a29 * a31) + a24 * (a26 * a33 - a28 * a31)) + a18 * (a21 * (a27 * a34 - a29 * a32) - a22 * (a26 * a34 - a29 * a31) + a24 * (a26 * a32 - a27 * a31)) - a19 * (a21 * (a27 * a33 - a28 * a32) - a22 * (a26 * a33 - a28 * a31) + a23 * (a26 * a32 - a27 * a31))));
        a12 = data[7];
        a17 = data[13];
        a22 = data[19];
        a27 = data[25];
        a32 = data[31];
        ret += data[2] * (a11 * (a17 * (a23 * (a29 * a35 - a30 * a34) - a24 * (a28 * a35 - a30 * a33) + a25 * (a28 * a34 - a29 * a33)) - a18 * (a22 * (a29 * a35 - a30 * a34) - a24 * (a27 * a35 - a30 * a32) + a25 * (a27 * a34 - a29 * a32)) + a19 * (a22 * (a28 * a35 - a30 * a33) - a23 * (a27 * a35 - a30 * a32) + a25 * (a27 * a33 - a28 * a32)) - a20 * (a22 * (a28 * a34 - a29 * a33) - a23 * (a27 * a34 - a29 * a32) + a24 * (a27 * a33 - a28 * a32))) - a12 * (a16 * (a23 * (a29 * a35 - a30 * a34) - a24 * (a28 * a35 - a30 * a33) + a25 * (a28 * a34 - a29 * a33)) - a18 * (a21 * (a29 * a35 - a30 * a34) - a24 * (a26 * a35 - a30 * a31) + a25 * (a26 * a34 - a29 * a31)) + a19 * (a21 * (a28 * a35 - a30 * a33) - a23 * (a26 * a35 - a30 * a31) + a25 * (a26 * a33 - a28 * a31)) - a20 * (a21 * (a28 * a34 - a29 * a33) - a23 * (a26 * a34 - a29 * a31) + a24 * (a26 * a33 - a28 * a31))) + a13 * (a16 * (a22 * (a29 * a35 - a30 * a34) - a24 * (a27 * a35 - a30 * a32) + a25 * (a27 * a34 - a29 * a32)) - a17 * (a21 * (a29 * a35 - a30 * a34) - a24 * (a26 * a35 - a30 * a31) + a25 * (a26 * a34 - a29 * a31)) + a19 * (a21 * (a27 * a35 - a30 * a32) - a22 * (a26 * a35 - a30 * a31) + a25 * (a26 * a32 - a27 * a31)) - a20 * (a21 * (a27 * a34 - a29 * a32) - a22 * (a26 * a34 - a29 * a31) + a24 * (a26 * a32 - a27 * a31))) - a14 * (a16 * (a22 * (a28 * a35 - a30 * a33) - a23 * (a27 * a35 - a30 * a32) + a25 * (a27 * a33 - a28 * a32)) - a17 * (a21 * (a28 * a35 - a30 * a33) - a23 * (a26 * a35 - a30 * a31) + a25 * (a26 * a33 - a28 * a31)) + a18 * (a21 * (a27 * a35 - a30 * a32) - a22 * (a26 * a35 - a30 * a31) + a25 * (a26 * a32 - a27 * a31)) - a20 * (a21 * (a27 * a33 - a28 * a32) - a22 * (a26 * a33 - a28 * a31) + a23 * (a26 * a32 - a27 * a31))) + a15 * (a16 * (a22 * (a28 * a34 - a29 * a33) - a23 * (a27 * a34 - a29 * a32) + a24 * (a27 * a33 - a28 * a32)) - a17 * (a21 * (a28 * a34 - a29 * a33) - a23 * (a26 * a34 - a29 * a31) + a24 * (a26 * a33 - a28 * a31)) + a18 * (a21 * (a27 * a34 - a29 * a32) - a22 * (a26 * a34 - a29 * a31) + a24 * (a26 * a32 - a27 * a31)) - a19 * (a21 * (a27 * a33 - a28 * a32) - a22 * (a26 * a33 - a28 * a31) + a23 * (a26 * a32 - a27 * a31))));
        a13 = data[8];
        a18 = data[14];
        a23 = data[20];
        a28 = data[26];
        a33 = data[32];
        ret -= data[3] * (a11 * (a17 * (a23 * (a29 * a35 - a30 * a34) - a24 * (a28 * a35 - a30 * a33) + a25 * (a28 * a34 - a29 * a33)) - a18 * (a22 * (a29 * a35 - a30 * a34) - a24 * (a27 * a35 - a30 * a32) + a25 * (a27 * a34 - a29 * a32)) + a19 * (a22 * (a28 * a35 - a30 * a33) - a23 * (a27 * a35 - a30 * a32) + a25 * (a27 * a33 - a28 * a32)) - a20 * (a22 * (a28 * a34 - a29 * a33) - a23 * (a27 * a34 - a29 * a32) + a24 * (a27 * a33 - a28 * a32))) - a12 * (a16 * (a23 * (a29 * a35 - a30 * a34) - a24 * (a28 * a35 - a30 * a33) + a25 * (a28 * a34 - a29 * a33)) - a18 * (a21 * (a29 * a35 - a30 * a34) - a24 * (a26 * a35 - a30 * a31) + a25 * (a26 * a34 - a29 * a31)) + a19 * (a21 * (a28 * a35 - a30 * a33) - a23 * (a26 * a35 - a30 * a31) + a25 * (a26 * a33 - a28 * a31)) - a20 * (a21 * (a28 * a34 - a29 * a33) - a23 * (a26 * a34 - a29 * a31) + a24 * (a26 * a33 - a28 * a31))) + a13 * (a16 * (a22 * (a29 * a35 - a30 * a34) - a24 * (a27 * a35 - a30 * a32) + a25 * (a27 * a34 - a29 * a32)) - a17 * (a21 * (a29 * a35 - a30 * a34) - a24 * (a26 * a35 - a30 * a31) + a25 * (a26 * a34 - a29 * a31)) + a19 * (a21 * (a27 * a35 - a30 * a32) - a22 * (a26 * a35 - a30 * a31) + a25 * (a26 * a32 - a27 * a31)) - a20 * (a21 * (a27 * a34 - a29 * a32) - a22 * (a26 * a34 - a29 * a31) + a24 * (a26 * a32 - a27 * a31))) - a14 * (a16 * (a22 * (a28 * a35 - a30 * a33) - a23 * (a27 * a35 - a30 * a32) + a25 * (a27 * a33 - a28 * a32)) - a17 * (a21 * (a28 * a35 - a30 * a33) - a23 * (a26 * a35 - a30 * a31) + a25 * (a26 * a33 - a28 * a31)) + a18 * (a21 * (a27 * a35 - a30 * a32) - a22 * (a26 * a35 - a30 * a31) + a25 * (a26 * a32 - a27 * a31)) - a20 * (a21 * (a27 * a33 - a28 * a32) - a22 * (a26 * a33 - a28 * a31) + a23 * (a26 * a32 - a27 * a31))) + a15 * (a16 * (a22 * (a28 * a34 - a29 * a33) - a23 * (a27 * a34 - a29 * a32) + a24 * (a27 * a33 - a28 * a32)) - a17 * (a21 * (a28 * a34 - a29 * a33) - a23 * (a26 * a34 - a29 * a31) + a24 * (a26 * a33 - a28 * a31)) + a18 * (a21 * (a27 * a34 - a29 * a32) - a22 * (a26 * a34 - a29 * a31) + a24 * (a26 * a32 - a27 * a31)) - a19 * (a21 * (a27 * a33 - a28 * a32) - a22 * (a26 * a33 - a28 * a31) + a23 * (a26 * a32 - a27 * a31))));
        a14 = data[9];
        a19 = data[15];
        a24 = data[21];
        a29 = data[27];
        a34 = data[33];
        ret += data[4] * (a11 * (a17 * (a23 * (a29 * a35 - a30 * a34) - a24 * (a28 * a35 - a30 * a33) + a25 * (a28 * a34 - a29 * a33)) - a18 * (a22 * (a29 * a35 - a30 * a34) - a24 * (a27 * a35 - a30 * a32) + a25 * (a27 * a34 - a29 * a32)) + a19 * (a22 * (a28 * a35 - a30 * a33) - a23 * (a27 * a35 - a30 * a32) + a25 * (a27 * a33 - a28 * a32)) - a20 * (a22 * (a28 * a34 - a29 * a33) - a23 * (a27 * a34 - a29 * a32) + a24 * (a27 * a33 - a28 * a32))) - a12 * (a16 * (a23 * (a29 * a35 - a30 * a34) - a24 * (a28 * a35 - a30 * a33) + a25 * (a28 * a34 - a29 * a33)) - a18 * (a21 * (a29 * a35 - a30 * a34) - a24 * (a26 * a35 - a30 * a31) + a25 * (a26 * a34 - a29 * a31)) + a19 * (a21 * (a28 * a35 - a30 * a33) - a23 * (a26 * a35 - a30 * a31) + a25 * (a26 * a33 - a28 * a31)) - a20 * (a21 * (a28 * a34 - a29 * a33) - a23 * (a26 * a34 - a29 * a31) + a24 * (a26 * a33 - a28 * a31))) + a13 * (a16 * (a22 * (a29 * a35 - a30 * a34) - a24 * (a27 * a35 - a30 * a32) + a25 * (a27 * a34 - a29 * a32)) - a17 * (a21 * (a29 * a35 - a30 * a34) - a24 * (a26 * a35 - a30 * a31) + a25 * (a26 * a34 - a29 * a31)) + a19 * (a21 * (a27 * a35 - a30 * a32) - a22 * (a26 * a35 - a30 * a31) + a25 * (a26 * a32 - a27 * a31)) - a20 * (a21 * (a27 * a34 - a29 * a32) - a22 * (a26 * a34 - a29 * a31) + a24 * (a26 * a32 - a27 * a31))) - a14 * (a16 * (a22 * (a28 * a35 - a30 * a33) - a23 * (a27 * a35 - a30 * a32) + a25 * (a27 * a33 - a28 * a32)) - a17 * (a21 * (a28 * a35 - a30 * a33) - a23 * (a26 * a35 - a30 * a31) + a25 * (a26 * a33 - a28 * a31)) + a18 * (a21 * (a27 * a35 - a30 * a32) - a22 * (a26 * a35 - a30 * a31) + a25 * (a26 * a32 - a27 * a31)) - a20 * (a21 * (a27 * a33 - a28 * a32) - a22 * (a26 * a33 - a28 * a31) + a23 * (a26 * a32 - a27 * a31))) + a15 * (a16 * (a22 * (a28 * a34 - a29 * a33) - a23 * (a27 * a34 - a29 * a32) + a24 * (a27 * a33 - a28 * a32)) - a17 * (a21 * (a28 * a34 - a29 * a33) - a23 * (a26 * a34 - a29 * a31) + a24 * (a26 * a33 - a28 * a31)) + a18 * (a21 * (a27 * a34 - a29 * a32) - a22 * (a26 * a34 - a29 * a31) + a24 * (a26 * a32 - a27 * a31)) - a19 * (a21 * (a27 * a33 - a28 * a32) - a22 * (a26 * a33 - a28 * a31) + a23 * (a26 * a32 - a27 * a31))));
        a15 = data[10];
        a20 = data[16];
        a25 = data[22];
        a30 = data[28];
        a35 = data[34];
        ret -= data[5] * (a11 * (a17 * (a23 * (a29 * a35 - a30 * a34) - a24 * (a28 * a35 - a30 * a33) + a25 * (a28 * a34 - a29 * a33)) - a18 * (a22 * (a29 * a35 - a30 * a34) - a24 * (a27 * a35 - a30 * a32) + a25 * (a27 * a34 - a29 * a32)) + a19 * (a22 * (a28 * a35 - a30 * a33) - a23 * (a27 * a35 - a30 * a32) + a25 * (a27 * a33 - a28 * a32)) - a20 * (a22 * (a28 * a34 - a29 * a33) - a23 * (a27 * a34 - a29 * a32) + a24 * (a27 * a33 - a28 * a32))) - a12 * (a16 * (a23 * (a29 * a35 - a30 * a34) - a24 * (a28 * a35 - a30 * a33) + a25 * (a28 * a34 - a29 * a33)) - a18 * (a21 * (a29 * a35 - a30 * a34) - a24 * (a26 * a35 - a30 * a31) + a25 * (a26 * a34 - a29 * a31)) + a19 * (a21 * (a28 * a35 - a30 * a33) - a23 * (a26 * a35 - a30 * a31) + a25 * (a26 * a33 - a28 * a31)) - a20 * (a21 * (a28 * a34 - a29 * a33) - a23 * (a26 * a34 - a29 * a31) + a24 * (a26 * a33 - a28 * a31))) + a13 * (a16 * (a22 * (a29 * a35 - a30 * a34) - a24 * (a27 * a35 - a30 * a32) + a25 * (a27 * a34 - a29 * a32)) - a17 * (a21 * (a29 * a35 - a30 * a34) - a24 * (a26 * a35 - a30 * a31) + a25 * (a26 * a34 - a29 * a31)) + a19 * (a21 * (a27 * a35 - a30 * a32) - a22 * (a26 * a35 - a30 * a31) + a25 * (a26 * a32 - a27 * a31)) - a20 * (a21 * (a27 * a34 - a29 * a32) - a22 * (a26 * a34 - a29 * a31) + a24 * (a26 * a32 - a27 * a31))) - a14 * (a16 * (a22 * (a28 * a35 - a30 * a33) - a23 * (a27 * a35 - a30 * a32) + a25 * (a27 * a33 - a28 * a32)) - a17 * (a21 * (a28 * a35 - a30 * a33) - a23 * (a26 * a35 - a30 * a31) + a25 * (a26 * a33 - a28 * a31)) + a18 * (a21 * (a27 * a35 - a30 * a32) - a22 * (a26 * a35 - a30 * a31) + a25 * (a26 * a32 - a27 * a31)) - a20 * (a21 * (a27 * a33 - a28 * a32) - a22 * (a26 * a33 - a28 * a31) + a23 * (a26 * a32 - a27 * a31))) + a15 * (a16 * (a22 * (a28 * a34 - a29 * a33) - a23 * (a27 * a34 - a29 * a32) + a24 * (a27 * a33 - a28 * a32)) - a17 * (a21 * (a28 * a34 - a29 * a33) - a23 * (a26 * a34 - a29 * a31) + a24 * (a26 * a33 - a28 * a31)) + a18 * (a21 * (a27 * a34 - a29 * a32) - a22 * (a26 * a34 - a29 * a31) + a24 * (a26 * a32 - a27 * a31)) - a19 * (a21 * (a27 * a33 - a28 * a32) - a22 * (a26 * a33 - a28 * a31) + a23 * (a26 * a32 - a27 * a31))));
        return ret;
    }
}
