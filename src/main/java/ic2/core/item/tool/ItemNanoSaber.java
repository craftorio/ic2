package ic2.core.item.tool;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import ic2.api.item.ElectricItem;
import ic2.core.IC2;
import ic2.core.audio.PositionSpec;
import ic2.core.item.ItemIC2;
import ic2.core.item.armor.ItemArmorNanoSuit;
import ic2.core.item.armor.ItemArmorQuantumSuit;
import ic2.core.ref.ItemName;
import ic2.core.slot.ArmorSlot;
import ic2.core.util.StackUtil;
import java.util.EnumSet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemNanoSaber extends ItemElectricTool {
   public static int ticker = 0;
   private int soundTicker = 0;

   public ItemNanoSaber() {
      super(ItemName.nano_saber, 10, HarvestLevel.Diamond, EnumSet.of(ToolClass.Sword));
      this.maxCharge = 160000;
      this.transferLimit = 500;
      this.tier = 3;
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void registerModels(final ItemName name) {
      String activeSuffix = "active";
      ModelLoader.setCustomMeshDefinition(this, new ItemMeshDefinition() {
         public ModelResourceLocation getModelLocation(ItemStack stack) {
            return ItemIC2.getModelLocation(name, ItemNanoSaber.isActive(stack) ? "active" : null);
         }
      });
      ModelBakery.registerItemVariants(this, new ResourceLocation[]{ItemIC2.getModelLocation(name, null)});
      ModelBakery.registerItemVariants(this, new ResourceLocation[]{ItemIC2.getModelLocation(name, "active")});
   }

   @Override
   public float getDestroySpeed(ItemStack stack, IBlockState state) {
      if (isActive(stack)) {
         this.soundTicker++;
         if (IC2.platform.isRendering() && this.soundTicker % 4 == 0) {
            IC2.platform.playSoundSp(this.getRandomSwingSound(), 1.0F, 1.0F);
         }

         return state.getBlock() == Blocks.WEB ? 50.0F : 4.0F;
      } else {
         return 1.0F;
      }
   }

   public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {
      if (slot != EntityEquipmentSlot.MAINHAND) {
         return super.getAttributeModifiers(slot, stack);
      }

      int dmg = 4;
      if (ElectricItem.manager.canUse(stack, 400.0) && isActive(stack)) {
         dmg = 20;
      }

      Multimap<String, AttributeModifier> ret = HashMultimap.create();
      ret.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Tool modifier", this.attackSpeed, 0));
      ret.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(Item.ATTACK_DAMAGE_MODIFIER, "Tool modifier", dmg, 0));
      return ret;
   }

   @Override
   public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase source) {
      if (!isActive(stack)) {
         return true;
      }

      if (IC2.platform.isSimulating()) {
         drainSaber(stack, 400.0, source);
         if (!(source instanceof EntityPlayerMP) || !(target instanceof EntityPlayer) || ((EntityPlayerMP)source).canAttackPlayer((EntityPlayer)target)) {
            for (EntityEquipmentSlot slot : ArmorSlot.getAll()) {
               if (!ElectricItem.manager.canUse(stack, 2000.0)) {
                  break;
               }

               ItemStack armor = target.getItemStackFromSlot(slot);
               if (armor != null) {
                  double amount = 0.0;
                  if (armor.getItem() instanceof ItemArmorNanoSuit) {
                     amount = 48000.0;
                  } else if (armor.getItem() instanceof ItemArmorQuantumSuit) {
                     amount = 300000.0;
                  }

                  if (amount > 0.0) {
                     ElectricItem.manager.discharge(armor, amount, this.tier, true, false, false);
                     if (!ElectricItem.manager.canUse(armor, 1.0)) {
                        target.setItemStackToSlot(slot, null);
                     }

                     drainSaber(stack, 2000.0, source);
                  }
               }
            }
         }
      }

      if (IC2.platform.isRendering()) {
         IC2.platform.playSoundSp(this.getRandomSwingSound(), 1.0F, 1.0F);
      }

      return true;
   }

   public String getRandomSwingSound() {
      switch (IC2.random.nextInt(3)) {
         case 1:
            return "Tools/Nanosabre/NanosabreSwing2.ogg";
         case 2:
            return "Tools/Nanosabre/NanosabreSwing3.ogg";
         default:
            return "Tools/Nanosabre/NanosabreSwing1.ogg";
      }
   }

   public boolean canDestroyBlockInCreative(World world, BlockPos pos, ItemStack stack, EntityPlayer player) {
      return false;
   }

   public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, EntityPlayer player) {
      if (isActive(stack)) {
         drainSaber(stack, 80.0, player);
      }

      return false;
   }

   public boolean isFull3D() {
      return true;
   }

   public static void drainSaber(ItemStack stack, double amount, EntityLivingBase entity) {
      if (!ElectricItem.manager.use(stack, amount, entity)) {
         NBTTagCompound nbt = StackUtil.getOrCreateNbtData(stack);
         setActive(nbt, false);
      }
   }

   @Override
   public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
      ItemStack stack = StackUtil.get(player, hand);
      if (world.isRemote) {
         return new ActionResult(EnumActionResult.PASS, stack);
      } else {
         NBTTagCompound nbt = StackUtil.getOrCreateNbtData(stack);
         if (isActive(nbt)) {
            setActive(nbt, false);
            return new ActionResult(EnumActionResult.SUCCESS, stack);
         } else if (ElectricItem.manager.canUse(stack, 16.0)) {
            setActive(nbt, true);
            return new ActionResult(EnumActionResult.SUCCESS, stack);
         } else {
            return super.onItemRightClick(world, player, hand);
         }
      }
   }

   @Override
   public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean par5) {
      super.onUpdate(stack, world, entity, slot, par5 && isActive(stack));
      if (isActive(stack)) {
         if (ticker % 16 == 0 && entity instanceof EntityPlayerMP) {
            if (slot < 9) {
               drainSaber(stack, 64.0, (EntityPlayer)entity);
            } else if (ticker % 64 == 0) {
               drainSaber(stack, 16.0, (EntityPlayer)entity);
            }
         }
      }
   }

   @SideOnly(Side.CLIENT)
   @Override
   public EnumRarity getRarity(ItemStack stack) {
      return EnumRarity.UNCOMMON;
   }

   private static boolean isActive(ItemStack stack) {
      NBTTagCompound nbt = StackUtil.getOrCreateNbtData(stack);
      return isActive(nbt);
   }

   private static boolean isActive(NBTTagCompound nbt) {
      return nbt.getBoolean("active");
   }

   private static void setActive(NBTTagCompound nbt, boolean active) {
      nbt.setBoolean("active", active);
   }

   public boolean onEntitySwing(EntityLivingBase entity, ItemStack stack) {
      if (IC2.platform.isRendering() && isActive(stack)) {
         IC2.audioManager.playOnce(entity, PositionSpec.Hand, this.getRandomSwingSound(), true, IC2.audioManager.getDefaultVolume());
      }

      return false;
   }

   @Override
   protected String getIdleSound(EntityLivingBase player, ItemStack stack) {
      return "Tools/Nanosabre/NanosabreIdle.ogg";
   }

   @Override
   protected String getStartSound(EntityLivingBase player, ItemStack stack) {
      return "Tools/Nanosabre/NanosabrePowerup.ogg";
   }
}
