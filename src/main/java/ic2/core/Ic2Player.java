package ic2.core;

import com.google.common.base.Charsets;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayerFactory;

public class Ic2Player {
   public static EntityPlayer get(World world) {
      return world instanceof WorldServer ? FakePlayerFactory.get((WorldServer)world, getGameProfile(world.provider.getDimension())) : null;
   }

   private static GameProfile getGameProfile(int dim) {
      String name = "[IC2 " + dim + "]";
      UUID uuid = UUID.nameUUIDFromBytes(name.getBytes(Charsets.UTF_8));
      return new GameProfile(uuid, name);
   }
}
