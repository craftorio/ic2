// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.util;

import ic2.core.IC2;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;

public class RotationUtil
{
    public static EnumFacing rotateByRay(final RayTraceResult ray) {
        assert ray.typeOfHit == RayTraceResult.Type.BLOCK;
        final Vec3d hit = ray.hitVec;
        final BlockPos pos = ray.getBlockPos();
        return rotateByHit(ray.sideHit, (float)hit.x - pos.getX(), (float)hit.y - pos.getY(), (float)hit.z - pos.getZ());
    }
    
    public static EnumFacing rotateByHit(final EnumFacing facingHit, final float hitX, final float hitY, final float hitZ) {
        switch (facingHit) {
            case DOWN: {
                if (hitX <= 0.25f) {
                    if (hitZ > 0.25f && hitZ < 0.75f) {
                        return EnumFacing.WEST;
                    }
                    return EnumFacing.UP;
                }
                else if (hitX > 0.25f && hitX < 0.75f) {
                    if (hitZ <= 0.25f) {
                        return EnumFacing.NORTH;
                    }
                    if (hitZ >= 0.75f) {
                        return EnumFacing.SOUTH;
                    }
                    return EnumFacing.DOWN;
                }
                else {
                    if (hitX < 0.75f) {
                        break;
                    }
                    if (hitZ > 0.25f && hitZ < 0.75f) {
                        return EnumFacing.EAST;
                    }
                    return EnumFacing.UP;
                }
                break;
            }
            case UP: {
                if (hitX <= 0.25f) {
                    if (hitZ > 0.25f && hitZ < 0.75f) {
                        return EnumFacing.WEST;
                    }
                    return EnumFacing.DOWN;
                }
                else if (hitX > 0.25f && hitX < 0.75f) {
                    if (hitZ <= 0.25f) {
                        return EnumFacing.NORTH;
                    }
                    if (hitZ >= 0.75f) {
                        return EnumFacing.SOUTH;
                    }
                    return EnumFacing.UP;
                }
                else {
                    if (hitX < 0.75f) {
                        break;
                    }
                    if (hitZ > 0.25f && hitZ < 0.75f) {
                        return EnumFacing.EAST;
                    }
                    return EnumFacing.DOWN;
                }
                break;
            }
            case NORTH: {
                if (hitX <= 0.25f) {
                    if (hitY > 0.25f && hitY < 0.75f) {
                        return EnumFacing.WEST;
                    }
                    return EnumFacing.SOUTH;
                }
                else if (hitX > 0.25f && hitX < 0.75f) {
                    if (hitY <= 0.25f) {
                        return EnumFacing.DOWN;
                    }
                    if (hitY >= 0.75f) {
                        return EnumFacing.UP;
                    }
                    return EnumFacing.NORTH;
                }
                else {
                    if (hitX < 0.75f) {
                        break;
                    }
                    if (hitY > 0.25f && hitY < 0.75f) {
                        return EnumFacing.EAST;
                    }
                    return EnumFacing.SOUTH;
                }
                break;
            }
            case SOUTH: {
                if (hitX <= 0.25f) {
                    if (hitY > 0.25f && hitY < 0.75f) {
                        return EnumFacing.WEST;
                    }
                    return EnumFacing.NORTH;
                }
                else if (hitX > 0.25f && hitX < 0.75f) {
                    if (hitY <= 0.25f) {
                        return EnumFacing.DOWN;
                    }
                    if (hitY >= 0.75f) {
                        return EnumFacing.UP;
                    }
                    return EnumFacing.SOUTH;
                }
                else {
                    if (hitX < 0.75f) {
                        break;
                    }
                    if (hitY > 0.25f && hitY < 0.75f) {
                        return EnumFacing.EAST;
                    }
                    return EnumFacing.NORTH;
                }
                break;
            }
            case WEST: {
                if (hitZ <= 0.25f) {
                    if (hitY > 0.25f && hitY < 0.75f) {
                        return EnumFacing.NORTH;
                    }
                    return EnumFacing.EAST;
                }
                else if (hitZ > 0.25f && hitZ < 0.75f) {
                    if (hitY <= 0.25f) {
                        return EnumFacing.DOWN;
                    }
                    if (hitY >= 0.75f) {
                        return EnumFacing.UP;
                    }
                    return EnumFacing.WEST;
                }
                else {
                    if (hitZ < 0.75f) {
                        break;
                    }
                    if (hitY > 0.25f && hitY < 0.75f) {
                        return EnumFacing.SOUTH;
                    }
                    return EnumFacing.EAST;
                }
                break;
            }
            case EAST: {
                if (hitZ <= 0.25f) {
                    if (hitY > 0.25f && hitY < 0.75f) {
                        return EnumFacing.NORTH;
                    }
                    return EnumFacing.WEST;
                }
                else if (hitZ > 0.25f && hitZ < 0.75f) {
                    if (hitY <= 0.25f) {
                        return EnumFacing.DOWN;
                    }
                    if (hitY >= 0.75f) {
                        return EnumFacing.UP;
                    }
                    return EnumFacing.EAST;
                }
                else {
                    if (hitZ < 0.75f) {
                        break;
                    }
                    if (hitY > 0.25f && hitY < 0.75f) {
                        return EnumFacing.SOUTH;
                    }
                    return EnumFacing.WEST;
                }
                break;
            }
        }
        return facingHit;
    }
    
    public static int[] shuffledFacings() {
        final int[] ordinals = { 0, 1, 2, 3, 4, 5 };
        for (int i = ordinals.length - 1; i > 0; --i) {
            final int index = IC2.random.nextInt(i + 1);
            if (index != i) {
                final int[] array = ordinals;
                final int n = index;
                array[n] ^= ordinals[i];
                final int[] array2 = ordinals;
                final int n2 = i;
                array2[n2] ^= ordinals[index];
                final int[] array3 = ordinals;
                final int n3 = index;
                array3[n3] ^= ordinals[i];
            }
        }
        return ordinals;
    }
}
