package ic2.shades.org.ejml.alg.dense.misc;

import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.data.RowD1Matrix64F;

public class DeterminantFromMinor {
   private int width;
   private int minWidth;
   private int[] levelIndexes;
   private double[] levelResults;
   private int[] levelRemoved;
   private int[] open;
   private int numOpen;
   private DenseMatrix64F tempMat;
   private boolean dirty = false;

   public DeterminantFromMinor(int width) {
      this(width, 5);
   }

   public DeterminantFromMinor(int width, int minWidth) {
      if (minWidth <= 5 && minWidth >= 2) {
         if (width < minWidth) {
            minWidth = width;
         }

         this.minWidth = minWidth;
         this.width = width;
         int numLevels = width - (minWidth - 2);
         this.levelResults = new double[numLevels];
         this.levelRemoved = new int[numLevels];
         this.levelIndexes = new int[numLevels];
         this.open = new int[width];
         this.tempMat = new DenseMatrix64F(minWidth - 1, minWidth - 1);
      } else {
         throw new IllegalArgumentException("No direct function for that width");
      }
   }

   public double compute(RowD1Matrix64F mat) {
      if (this.width == mat.numCols && this.width == mat.numRows) {
         this.initStructures();
         int level = 0;

         while (true) {
            int levelWidth = this.width - level;
            int levelIndex = this.levelIndexes[level];
            if (levelIndex == levelWidth) {
               if (level == 0) {
                  return this.levelResults[0];
               }

               int prevLevelIndex = this.levelIndexes[level - 1]++;
               double val = mat.get((level - 1) * this.width + this.levelRemoved[level - 1]);
               if (prevLevelIndex % 2 == 0) {
                  this.levelResults[level - 1] = this.levelResults[level - 1] + val * this.levelResults[level];
               } else {
                  this.levelResults[level - 1] = this.levelResults[level - 1] - val * this.levelResults[level];
               }

               this.putIntoOpen(level - 1);
               this.levelResults[level] = 0.0;
               this.levelIndexes[level] = 0;
               level--;
            } else {
               int excluded = this.openRemove(levelIndex);
               this.levelRemoved[level] = excluded;
               if (levelWidth == this.minWidth) {
                  this.createMinor(mat);
                  double subresult = mat.get(level * this.width + this.levelRemoved[level]);
                  subresult *= UnrolledDeterminantFromMinor.det(this.tempMat);
                  if (levelIndex % 2 == 0) {
                     this.levelResults[level] = this.levelResults[level] + subresult;
                  } else {
                     this.levelResults[level] = this.levelResults[level] - subresult;
                  }

                  this.putIntoOpen(level);
                  this.levelIndexes[level]++;
               } else {
                  level++;
               }
            }
         }
      } else {
         throw new RuntimeException("Unexpected matrix dimension");
      }
   }

   private void initStructures() {
      int i = 0;

      while (i < this.width) {
         this.open[i] = i++;
      }

      this.numOpen = this.width;
      if (this.dirty) {
         for (int ix = 0; ix < this.levelIndexes.length; ix++) {
            this.levelIndexes[ix] = 0;
            this.levelResults[ix] = 0.0;
            this.levelRemoved[ix] = 0;
         }
      }

      this.dirty = true;
   }

   private int openRemove(int where) {
      int val = this.open[where];
      System.arraycopy(this.open, where + 1, this.open, where, this.numOpen - where - 1);
      this.numOpen--;
      return val;
   }

   private void openAdd(int where, int val) {
      for (int i = this.numOpen; i > where; i--) {
         this.open[i] = this.open[i - 1];
      }

      this.numOpen++;
      this.open[where] = val;
   }

   private void openAdd(int val) {
      this.open[this.numOpen++] = val;
   }

   private void putIntoOpen(int level) {
      boolean added = false;

      for (int i = 0; i < this.numOpen; i++) {
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

   private void createMinor(RowD1Matrix64F mat) {
      int w = this.minWidth - 1;
      int firstRow = (this.width - w) * this.width;

      for (int i = 0; i < this.numOpen; i++) {
         int col = this.open[i];
         int srcIndex = firstRow + col;
         int dstIndex = i;

         for (int j = 0; j < w; j++) {
            this.tempMat.set(dstIndex, mat.get(srcIndex));
            dstIndex += w;
            srcIndex += this.width;
         }
      }
   }
}
