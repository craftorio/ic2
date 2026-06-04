package ic2.core.util;

import ic2.core.IC2;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

public class RotationUtil {
  public static EnumFacing rotateByRay(RayTraceResult ray) {
    assert ray.typeOfHit == RayTraceResult.Type.BLOCK;
    Vec3d hit = ray.field_72307_f;
    BlockPos pos = ray.getBlockPos();
    return rotateByHit(ray.field_178784_b, (float)hit.field_72450_a - pos.getX(), (float)hit.field_72448_b - pos.getY(), (float)hit.field_72449_c - pos.getZ());
  }
  
  public static EnumFacing rotateByHit(EnumFacing facingHit, float hitX, float hitY, float hitZ) {
    switch (facingHit) {
      case DOWN:
        if (hitX <= 0.25F) {
          if (hitZ > 0.25F && hitZ < 0.75F)
            return EnumFacing.WEST; 
          return EnumFacing.UP;
        } 
        if (hitX > 0.25F && hitX < 0.75F) {
          if (hitZ <= 0.25F)
            return EnumFacing.NORTH; 
          if (hitZ >= 0.75F)
            return EnumFacing.SOUTH; 
          return EnumFacing.DOWN;
        } 
        if (hitX >= 0.75F) {
          if (hitZ > 0.25F && hitZ < 0.75F)
            return EnumFacing.EAST; 
          return EnumFacing.UP;
        } 
        break;
      case UP:
        if (hitX <= 0.25F) {
          if (hitZ > 0.25F && hitZ < 0.75F)
            return EnumFacing.WEST; 
          return EnumFacing.DOWN;
        } 
        if (hitX > 0.25F && hitX < 0.75F) {
          if (hitZ <= 0.25F)
            return EnumFacing.NORTH; 
          if (hitZ >= 0.75F)
            return EnumFacing.SOUTH; 
          return EnumFacing.UP;
        } 
        if (hitX >= 0.75F) {
          if (hitZ > 0.25F && hitZ < 0.75F)
            return EnumFacing.EAST; 
          return EnumFacing.DOWN;
        } 
        break;
      case NORTH:
        if (hitX <= 0.25F) {
          if (hitY > 0.25F && hitY < 0.75F)
            return EnumFacing.WEST; 
          return EnumFacing.SOUTH;
        } 
        if (hitX > 0.25F && hitX < 0.75F) {
          if (hitY <= 0.25F)
            return EnumFacing.DOWN; 
          if (hitY >= 0.75F)
            return EnumFacing.UP; 
          return EnumFacing.NORTH;
        } 
        if (hitX >= 0.75F) {
          if (hitY > 0.25F && hitY < 0.75F)
            return EnumFacing.EAST; 
          return EnumFacing.SOUTH;
        } 
        break;
      case SOUTH:
        if (hitX <= 0.25F) {
          if (hitY > 0.25F && hitY < 0.75F)
            return EnumFacing.WEST; 
          return EnumFacing.NORTH;
        } 
        if (hitX > 0.25F && hitX < 0.75F) {
          if (hitY <= 0.25F)
            return EnumFacing.DOWN; 
          if (hitY >= 0.75F)
            return EnumFacing.UP; 
          return EnumFacing.SOUTH;
        } 
        if (hitX >= 0.75F) {
          if (hitY > 0.25F && hitY < 0.75F)
            return EnumFacing.EAST; 
          return EnumFacing.NORTH;
        } 
        break;
      case WEST:
        if (hitZ <= 0.25F) {
          if (hitY > 0.25F && hitY < 0.75F)
            return EnumFacing.NORTH; 
          return EnumFacing.EAST;
        } 
        if (hitZ > 0.25F && hitZ < 0.75F) {
          if (hitY <= 0.25F)
            return EnumFacing.DOWN; 
          if (hitY >= 0.75F)
            return EnumFacing.UP; 
          return EnumFacing.WEST;
        } 
        if (hitZ >= 0.75F) {
          if (hitY > 0.25F && hitY < 0.75F)
            return EnumFacing.SOUTH; 
          return EnumFacing.EAST;
        } 
        break;
      case EAST:
        if (hitZ <= 0.25F) {
          if (hitY > 0.25F && hitY < 0.75F)
            return EnumFacing.NORTH; 
          return EnumFacing.WEST;
        } 
        if (hitZ > 0.25F && hitZ < 0.75F) {
          if (hitY <= 0.25F)
            return EnumFacing.DOWN; 
          if (hitY >= 0.75F)
            return EnumFacing.UP; 
          return EnumFacing.EAST;
        } 
        if (hitZ >= 0.75F) {
          if (hitY > 0.25F && hitY < 0.75F)
            return EnumFacing.SOUTH; 
          return EnumFacing.WEST;
        } 
        break;
    } 
    return facingHit;
  }
  
  public static int[] shuffledFacings() {
    int[] ordinals = { 0, 1, 2, 3, 4, 5 };
    for (int i = ordinals.length - 1; i > 0; i--) {
      int index = IC2.random.nextInt(i + 1);
      if (index != i) {
        ordinals[index] = ordinals[index] ^ ordinals[i];
        ordinals[i] = ordinals[i] ^ ordinals[index];
        ordinals[index] = ordinals[index] ^ ordinals[i];
      } 
    } 
    return ordinals;
  }
}
