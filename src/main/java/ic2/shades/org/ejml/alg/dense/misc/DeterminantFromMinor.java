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
    if (minWidth > 5 || minWidth < 2)
      throw new IllegalArgumentException("No direct function for that width"); 
    if (width < minWidth)
      minWidth = width; 
    this.minWidth = minWidth;
    this.width = width;
    int numLevels = width - minWidth - 2;
    this.levelResults = new double[numLevels];
    this.levelRemoved = new int[numLevels];
    this.levelIndexes = new int[numLevels];
    this.open = new int[width];
    this.tempMat = new DenseMatrix64F(minWidth - 1, minWidth - 1);
  }
  
  public double compute(RowD1Matrix64F mat) {
    if (this.width != mat.numCols || this.width != mat.numRows)
      throw new RuntimeException("Unexpected matrix dimension"); 
    initStructures();
    int level = 0;
    while (true) {
      int levelWidth = this.width - level;
      int levelIndex = this.levelIndexes[level];
      if (levelIndex == levelWidth) {
        if (level == 0)
          return this.levelResults[0]; 
        this.levelIndexes[level - 1] = this.levelIndexes[level - 1] + 1;
        int prevLevelIndex = this.levelIndexes[level - 1];
        double val = mat.get((level - 1) * this.width + this.levelRemoved[level - 1]);
        if (prevLevelIndex % 2 == 0) {
          this.levelResults[level - 1] = this.levelResults[level - 1] + val * this.levelResults[level];
        } else {
          this.levelResults[level - 1] = this.levelResults[level - 1] - val * this.levelResults[level];
        } 
        putIntoOpen(level - 1);
        this.levelResults[level] = 0.0D;
        this.levelIndexes[level] = 0;
        level--;
        continue;
      } 
      int excluded = openRemove(levelIndex);
      this.levelRemoved[level] = excluded;
      if (levelWidth == this.minWidth) {
        createMinor(mat);
        double subresult = mat.get(level * this.width + this.levelRemoved[level]);
        subresult *= UnrolledDeterminantFromMinor.det((RowD1Matrix64F)this.tempMat);
        if (levelIndex % 2 == 0) {
          this.levelResults[level] = this.levelResults[level] + subresult;
        } else {
          this.levelResults[level] = this.levelResults[level] - subresult;
        } 
        putIntoOpen(level);
        this.levelIndexes[level] = this.levelIndexes[level] + 1;
        continue;
      } 
      level++;
    } 
  }
  
  private void initStructures() {
    int i;
    for (i = 0; i < this.width; i++)
      this.open[i] = i; 
    this.numOpen = this.width;
    if (this.dirty)
      for (i = 0; i < this.levelIndexes.length; i++) {
        this.levelIndexes[i] = 0;
        this.levelResults[i] = 0.0D;
        this.levelRemoved[i] = 0;
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
    for (int i = this.numOpen; i > where; i--)
      this.open[i] = this.open[i - 1]; 
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
        openAdd(i, this.levelRemoved[level]);
        break;
      } 
    } 
    if (!added)
      openAdd(this.levelRemoved[level]); 
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
