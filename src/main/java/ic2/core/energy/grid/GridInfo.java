// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.energy.grid;

public class GridInfo
{
    public final int id;
    public final int nodeCount;
    public final int complexNodeCount;
    public final int minX;
    public final int minY;
    public final int minZ;
    public final int maxX;
    public final int maxY;
    public final int maxZ;
    
    public GridInfo(final int id, final int nodeCount, final int complexNodeCount, final int minX, final int minY, final int minZ, final int maxX, final int maxY, final int maxZ) {
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
