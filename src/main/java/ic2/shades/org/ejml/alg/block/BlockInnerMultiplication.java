// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.block;

public class BlockInnerMultiplication
{
    public static void blockMultPlus(final double[] dataA, final double[] dataB, final double[] dataC, final int indexA, final int indexB, final int indexC, final int heightA, final int widthA, final int widthC) {
        int a = indexA;
        for (int rowC = indexC, i = 0; i < heightA; ++i, rowC += widthC) {
            int b = indexB;
            final int endC = rowC + widthC;
            final int endA = a + widthA;
            while (a != endA) {
                final double valA = dataA[a++];
                int n;
                for (int c = rowC; c != endC; n = c++, dataC[n] += valA * dataB[b++]) {}
            }
        }
    }
    
    public static void blockMultPlusTransA(final double[] dataA, final double[] dataB, final double[] dataC, final int indexA, final int indexB, final int indexC, final int heightA, final int widthA, final int widthC) {
        for (int rowC = indexC, i = 0; i < widthA; ++i, rowC += widthC) {
            int colA = i + indexA;
            final int endA = colA + widthA * heightA;
            int b = indexB;
            while (colA != endA) {
                final double valA = dataA[colA];
                int c = rowC;
                int n;
                for (int endB = b + widthC; b != endB; dataC[n] += valA * dataB[b++]) {
                    n = c++;
                }
                colA += widthA;
            }
        }
    }
    
    public static void blockMultPlusTransB(final double[] dataA, final double[] dataB, final double[] dataC, final int indexA, final int indexB, final int indexC, final int heightA, final int widthA, final int widthC) {
        for (int i = 0; i < heightA; ++i) {
            for (int j = 0; j < widthC; ++j) {
                double val = 0.0;
                for (int k = 0; k < widthA; ++k) {
                    val += dataA[i * widthA + k + indexA] * dataB[j * widthA + k + indexB];
                }
                final int n = i * widthC + j + indexC;
                dataC[n] += val;
            }
        }
    }
    
    public static void blockMultMinus(final double[] dataA, final double[] dataB, final double[] dataC, final int indexA, final int indexB, final int indexC, final int heightA, final int widthA, final int widthC) {
        int a = indexA;
        for (int rowC = indexC, i = 0; i < heightA; ++i, rowC += widthC) {
            int b = indexB;
            final int endC = rowC + widthC;
            final int endA = a + widthA;
            while (a != endA) {
                final double valA = dataA[a++];
                int n;
                for (int c = rowC; c != endC; n = c++, dataC[n] -= valA * dataB[b++]) {}
            }
        }
    }
    
    public static void blockMultMinusTransA(final double[] dataA, final double[] dataB, final double[] dataC, final int indexA, final int indexB, final int indexC, final int heightA, final int widthA, final int widthC) {
        for (int rowC = indexC, i = 0; i < widthA; ++i, rowC += widthC) {
            int colA = i + indexA;
            final int endA = colA + widthA * heightA;
            int b = indexB;
            while (colA != endA) {
                final double valA = dataA[colA];
                int c = rowC;
                int n;
                for (int endB = b + widthC; b != endB; dataC[n] -= valA * dataB[b++]) {
                    n = c++;
                }
                colA += widthA;
            }
        }
    }
    
    public static void blockMultMinusTransB(final double[] dataA, final double[] dataB, final double[] dataC, final int indexA, final int indexB, final int indexC, final int heightA, final int widthA, final int widthC) {
        for (int i = 0; i < heightA; ++i) {
            for (int j = 0; j < widthC; ++j) {
                double val = 0.0;
                for (int k = 0; k < widthA; ++k) {
                    val += dataA[i * widthA + k + indexA] * dataB[j * widthA + k + indexB];
                }
                final int n = i * widthC + j + indexC;
                dataC[n] -= val;
            }
        }
    }
    
    public static void blockMultSet(final double[] dataA, final double[] dataB, final double[] dataC, final int indexA, final int indexB, final int indexC, final int heightA, final int widthA, final int widthC) {
        int a = indexA;
        for (int rowC = indexC, i = 0; i < heightA; ++i, rowC += widthC) {
            int b = indexB;
            final int endC = rowC + widthC;
            final int endA = a + widthA;
            while (a != endA) {
                final double valA = dataA[a++];
                int c = rowC;
                if (b == indexB) {
                    while (c != endC) {
                        dataC[c++] = valA * dataB[b++];
                    }
                }
                else {
                    while (c != endC) {
                        final int n = c++;
                        dataC[n] += valA * dataB[b++];
                    }
                }
            }
        }
    }
    
    public static void blockMultSetTransA(final double[] dataA, final double[] dataB, final double[] dataC, final int indexA, final int indexB, final int indexC, final int heightA, final int widthA, final int widthC) {
        for (int rowC = indexC, i = 0; i < widthA; ++i, rowC += widthC) {
            int colA = i + indexA;
            final int endA = colA + widthA * heightA;
            int b = indexB;
            while (colA != endA) {
                final double valA = dataA[colA];
                int c = rowC;
                final int endB = b + widthC;
                if (b == indexB) {
                    while (b != endB) {
                        dataC[c++] = valA * dataB[b++];
                    }
                }
                else {
                    while (b != endB) {
                        final int n = c++;
                        dataC[n] += valA * dataB[b++];
                    }
                }
                colA += widthA;
            }
        }
    }
    
    public static void blockMultSetTransB(final double[] dataA, final double[] dataB, final double[] dataC, final int indexA, final int indexB, final int indexC, final int heightA, final int widthA, final int widthC) {
        for (int i = 0; i < heightA; ++i) {
            for (int j = 0; j < widthC; ++j) {
                double val = 0.0;
                for (int k = 0; k < widthA; ++k) {
                    val += dataA[i * widthA + k + indexA] * dataB[j * widthA + k + indexB];
                }
                dataC[i * widthC + j + indexC] = val;
            }
        }
    }
    
    public static void blockMultPlus(final double alpha, final double[] dataA, final double[] dataB, final double[] dataC, final int indexA, final int indexB, final int indexC, final int heightA, final int widthA, final int widthC) {
        int a = indexA;
        for (int rowC = indexC, i = 0; i < heightA; ++i, rowC += widthC) {
            int b = indexB;
            final int endC = rowC + widthC;
            final int endA = a + widthA;
            while (a != endA) {
                final double valA = alpha * dataA[a++];
                int n;
                for (int c = rowC; c != endC; n = c++, dataC[n] += valA * dataB[b++]) {}
            }
        }
    }
    
    public static void blockMultPlusTransA(final double alpha, final double[] dataA, final double[] dataB, final double[] dataC, final int indexA, final int indexB, final int indexC, final int heightA, final int widthA, final int widthC) {
        for (int rowC = indexC, i = 0; i < widthA; ++i, rowC += widthC) {
            int colA = i + indexA;
            final int endA = colA + widthA * heightA;
            int b = indexB;
            while (colA != endA) {
                final double valA = alpha * dataA[colA];
                int c = rowC;
                int n;
                for (int endB = b + widthC; b != endB; dataC[n] += valA * dataB[b++]) {
                    n = c++;
                }
                colA += widthA;
            }
        }
    }
    
    public static void blockMultPlusTransB(final double alpha, final double[] dataA, final double[] dataB, final double[] dataC, final int indexA, final int indexB, final int indexC, final int heightA, final int widthA, final int widthC) {
        for (int i = 0; i < heightA; ++i) {
            for (int j = 0; j < widthC; ++j) {
                double val = 0.0;
                for (int k = 0; k < widthA; ++k) {
                    val += dataA[i * widthA + k + indexA] * dataB[j * widthA + k + indexB];
                }
                final int n = i * widthC + j + indexC;
                dataC[n] += alpha * val;
            }
        }
    }
    
    public static void blockMultSet(final double alpha, final double[] dataA, final double[] dataB, final double[] dataC, final int indexA, final int indexB, final int indexC, final int heightA, final int widthA, final int widthC) {
        int a = indexA;
        for (int rowC = indexC, i = 0; i < heightA; ++i, rowC += widthC) {
            int b = indexB;
            final int endC = rowC + widthC;
            final int endA = a + widthA;
            while (a != endA) {
                final double valA = alpha * dataA[a++];
                int c = rowC;
                if (b == indexB) {
                    while (c != endC) {
                        dataC[c++] = valA * dataB[b++];
                    }
                }
                else {
                    while (c != endC) {
                        final int n = c++;
                        dataC[n] += valA * dataB[b++];
                    }
                }
            }
        }
    }
    
    public static void blockMultSetTransA(final double alpha, final double[] dataA, final double[] dataB, final double[] dataC, final int indexA, final int indexB, final int indexC, final int heightA, final int widthA, final int widthC) {
        for (int rowC = indexC, i = 0; i < widthA; ++i, rowC += widthC) {
            int colA = i + indexA;
            final int endA = colA + widthA * heightA;
            int b = indexB;
            while (colA != endA) {
                final double valA = alpha * dataA[colA];
                int c = rowC;
                final int endB = b + widthC;
                if (b == indexB) {
                    while (b != endB) {
                        dataC[c++] = valA * dataB[b++];
                    }
                }
                else {
                    while (b != endB) {
                        final int n = c++;
                        dataC[n] += valA * dataB[b++];
                    }
                }
                colA += widthA;
            }
        }
    }
    
    public static void blockMultSetTransB(final double alpha, final double[] dataA, final double[] dataB, final double[] dataC, final int indexA, final int indexB, final int indexC, final int heightA, final int widthA, final int widthC) {
        for (int i = 0; i < heightA; ++i) {
            for (int j = 0; j < widthC; ++j) {
                double val = 0.0;
                for (int k = 0; k < widthA; ++k) {
                    val += dataA[i * widthA + k + indexA] * dataB[j * widthA + k + indexB];
                }
                dataC[i * widthC + j + indexC] = alpha * val;
            }
        }
    }
}
