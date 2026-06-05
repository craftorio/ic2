package ic2.core.energy.grid;

public class GridInfo {
   public final int id;
   public final int nodeCount;
   public final int complexNodeCount;
   public final int minX;
   public final int minY;
   public final int minZ;
   public final int maxX;
   public final int maxY;
   public final int maxZ;

   public GridInfo(int id, int nodeCount, int complexNodeCount, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
      this.id = id;
      this.nodeCount = nodeCount;
      this.complexNodeCount = complexNodeCount;
      this.minX = minX;
      this.minY = minY;
      this.minZ = minZ;
      this.maxX = maxX;
      this.maxY = maxY;
      this.maxZ = maxZ;
   }
}
