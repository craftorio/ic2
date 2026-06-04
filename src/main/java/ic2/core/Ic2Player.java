// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core;

import java.util.UUID;
import com.google.common.base.Charsets;
import com.mojang.authlib.GameProfile;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraft.world.WorldServer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class Ic2Player
{
    public static EntityPlayer get(final World world) {
        if (world instanceof WorldServer) {
            return (EntityPlayer)FakePlayerFactory.get((WorldServer)world, getGameProfile(world.provider.getDimension()));
        }
        return null;
    }
    
    private static GameProfile getGameProfile(final int dim) {
        final String name = "[IC2 " + dim + "]";
        final UUID uuid = UUID.nameUUIDFromBytes(name.getBytes(Charsets.UTF_8));
        return new GameProfile(uuid, name);
    }
}
