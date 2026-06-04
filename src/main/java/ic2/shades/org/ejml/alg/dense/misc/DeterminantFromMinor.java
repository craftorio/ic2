// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.dense.misc;

import ic2.shades.org.ejml.data.RowD1Matrix64F;
import ic2.shades.org.ejml.data.DenseMatrix64F;

public class DeterminantFromMinor
{
    private int width;
    private int minWidth;
    private int[] levelIndexes;
    private double[] levelResults;
    private int[] levelRemoved;
    private int[] open;
    private int numOpen;
    private DenseMatrix64F tempMat;
    private boolean dirty;
    
    public DeterminantFromMinor(final int width) {
        this(width, 5);
    }
    
    public DeterminantFromMinor(final int width, int minWidth) {
        this.dirty = false;
        if (minWidth > 5 || minWidth < 2) {
            throw new IllegalArgumentException("No direct function for that width");
        }
        if (width < minWidth) {
            minWidth = width;
        }
        this.minWidth = minWidth;
        this.width = width;
        final int numLevels = width - (minWidth - 2);
        this.levelResults = new double[numLevels];
        this.levelRemoved = new int[numLevels];
        this.levelIndexes = new int[numLevels];
        this.open = new int[width];
        this.tempMat = new DenseMatrix64F(minWidth - 1, minWidth - 1);
    }
    
    public double compute(final RowD1Matrix64F mat) {
        if (this.width != mat.numCols || this.width != mat.numRows) {
            throw new RuntimeException("Unexpected matrix dimension");
        }
        this.initStructures();
        int level = 0;
        while (true) {
            final int levelWidth = this.width - level;
            final int levelIndex = this.levelIndexes[level];
            if (levelIndex == levelWidth) {
                if (level == 0) {
                    break;
                }
                final int prevLevelIndex = this.levelIndexes[level - 1]++;
                final double val = mat.get((level - 1) * this.width + this.levelRemoved[level - 1]);
                if (prevLevelIndex % 2 == 0) {
                    final double[] levelResults = this.levelResults;
                    final int n = level - 1;
                    levelResults[n] += val * this.levelResults[level];
                }
                else {
                    final double[] levelResults2 = this.levelResults;
                    final int n2 = level - 1;
                    levelResults2[n2] -= val * this.levelResults[level];
                }
                this.putIntoOpen(level - 1);
                this.levelResults[level] = 0.0;
                this.levelIndexes[level] = 0;
                --level;
            }
            else {
                final int excluded = this.openRemove(levelIndex);
                this.levelRemoved[level] = excluded;
                if (levelWidth == this.minWidth) {
                    this.createMinor(mat);
                    double subresult = mat.get(level * this.width + this.levelRemoved[level]);
                    subresult *= UnrolledDeterminantFromMinor.det(this.tempMat);
                    if (levelIndex % 2 == 0) {
                        final double[] levelResults3 = this.levelResults;
                        final int n3 = level;
                        levelResults3[n3] += subresult;
                    }
                    else {
                        final double[] levelResults4 = this.levelResults;
                        final int n4 = level;
                        levelResults4[n4] -= subresult;
                    }
                    this.putIntoOpen(level);
                    final int[] levelIndexes = this.levelIndexes;
                    final int n5 = level;
                    ++levelIndexes[n5];
                }
                else {
                    ++level;
                }
            }
        }
        return this.levelResults[0];
    }
    
    private void initStructures() {
        for (int i = 0; i < this.width; ++i) {
            this.open[i] = i;
        }
        this.numOpen = this.width;
        if (this.dirty) {
            for (int i = 0; i < this.levelIndexes.length; ++i) {
                this.levelIndexes[i] = 0;
                this.levelResults[i] = 0.0;
                this.levelRemoved[i] = 0;
            }
        }
        this.dirty = true;
    }
    
    private int openRemove(final int where) {
        final int val = this.open[where];
        System.arraycopy(this.open, where + 1, this.open, where, this.numOpen - where - 1);
        --this.numOpen;
        return val;
    }
    
    private void openAdd(final int where, final int val) {
        for (int i = this.numOpen; i > where; --i) {
            this.open[i] = this.open[i - 1];
        }
        ++this.numOpen;
        this.open[where] = val;
    }
    
    private void openAdd(final int val) {
        this.open[this.numOpen++] = val;
    }
    
    private void putIntoOpen(final int level) {
        boolean added = false;
        for (int i = 0; i < this.numOpen; ++i) {
            if (this.open[i] > this.levelRemoved[level]) {
                added = true;
                this.openAdd(i, this.levelRemoved[level]);
                break;
            }
        }
        if (!added) {
            this.openAdd(this.levelRemoved[level]);
        }
    }
    
    private void createMinor(final RowD1Matrix64F mat) {
        final int w = this.minWidth - 1;
        final int firstRow = (this.width - w) * this.width;
        for (int i = 0; i < this.numOpen; ++i) {
            final int col = this.open[i];
            int srcIndex = firstRow + col;
            int dstIndex = i;
            for (int j = 0; j < w; ++j) {
                this.tempMat.set(dstIndex, mat.get(srcIndex));
                dstIndex += w;
                srcIndex += this.width;
            }
        }
    }
}
