package ic2.core.item.tool;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import ic2.api.item.ElectricItem;
import ic2.core.IC2;
import ic2.core.IHitSoundOverride;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemElectricToolChainsaw extends ItemElectricTool implements IHitSoundOverride {
  public ItemElectricToolChainsaw() {
    super(ItemName.chainsaw, 100, HarvestLevel.Iron, EnumSet.of(ToolClass.Axe, ToolClass.Sword, ToolClass.Shears));
    this.maxCharge = 30000;
    this.transferLimit = 100;
    this.tier = 1;
    this.field_77864_a = 12.0F;
    MinecraftForge.EVENT_BUS.register(this);
  }
  
  public ActionResult<ItemStack> func_77659_a(World world, EntityPlayer player, EnumHand hand) {
    if (world.isRemote)
      return super.func_77659_a(world, player, hand); 
    if (IC2.keyboard.isModeSwitchKeyDown(player)) {
      NBTTagCompound compoundTag = StackUtil.getOrCreateNbtData(StackUtil.get(player, hand));
      if (compoundTag.func_74767_n("disableShear")) {
        compoundTag.func_74757_a("disableShear", false);
        IC2.platform.messagePlayer(player, "ic2.tooltip.mode", new Object[] { "ic2.tooltip.mode.normal" });
      } else {
        compoundTag.func_74757_a("disableShear", true);
        IC2.platform.messagePlayer(player, "ic2.tooltip.mode", new Object[] { "ic2.tooltip.mode.noShear" });
      } 
    } 
    return super.func_77659_a(world, player, hand);
  }
  
  public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {
    if (slot != EntityEquipmentSlot.MAINHAND)
      return super.getAttributeModifiers(slot, stack); 
    HashMultimap hashMultimap = HashMultimap.create();
    if (ElectricItem.manager.canUse(stack, this.operationEnergyCost)) {
      hashMultimap.put(SharedMonsterAttributes.field_188790_f.func_111108_a(), new AttributeModifier(field_185050_h, "Tool modifier", this.field_185065_c, 0));
      hashMultimap.put(SharedMonsterAttributes.field_111264_e.func_111108_a(), new AttributeModifier(Item.field_111210_e, "Tool modifier", 9.0D, 0));
    } 
    return (Multimap<String, AttributeModifier>)hashMultimap;
  }
  
  public boolean func_77644_a(ItemStack itemstack, EntityLivingBase entityliving, EntityLivingBase attacker) {
    ElectricItem.manager.use(itemstack, this.operationEnergyCost, attacker);
    if (attacker instanceof EntityPlayer && entityliving instanceof net.minecraft.entity.monster.EntityCreeper && entityliving.func_110143_aJ() <= 0.0F)
      IC2.achievements.issueAchievement((EntityPlayer)attacker, "killCreeperChainsaw"); 
    return true;
  }
  
  @SubscribeEvent
  public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
    if (!IC2.platform.isSimulating())
      return; 
    Entity entity = event.getTarget();
    EntityPlayer player = event.getEntityPlayer();
    ItemStack itemstack = player.field_71071_by.func_70301_a(player.field_71071_by.field_70461_c);
    if (itemstack != null && itemstack.getItem() == this && entity instanceof IShearable && 
      !StackUtil.getOrCreateNbtData(itemstack).func_74767_n("disableShear") && ElectricItem.manager
      .use(itemstack, this.operationEnergyCost, (EntityLivingBase)player)) {
      IShearable target = (IShearable)entity;
      World world = entity.func_130014_f_();
      BlockPos pos = new BlockPos(entity.field_70165_t, entity.field_70163_u, entity.field_70161_v);
      if (target.isShearable(itemstack, (IBlockAccess)world, pos)) {
        List<ItemStack> drops = target.onSheared(itemstack, (IBlockAccess)world, pos, 
            EnchantmentHelper.func_77506_a(Enchantments.field_185308_t, itemstack));
        for (ItemStack stack : drops) {
          EntityItem ent = entity.func_70099_a(stack, 1.0F);
          ent.field_70181_x += (field_77697_d.nextFloat() * 0.05F);
          ent.field_70159_w += ((field_77697_d.nextFloat() - field_77697_d.nextFloat()) * 0.1F);
          ent.field_70179_y += ((field_77697_d.nextFloat() - field_77697_d.nextFloat()) * 0.1F);
        } 
      } 
    } 
  }
  
  public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, EntityPlayer player) {
    if (!IC2.platform.isSimulating())
      return false; 
    if (StackUtil.getOrCreateNbtData(itemstack).func_74767_n("disableShear"))
      return false; 
    World world = player.func_130014_f_();
    IBlockState state = world.func_180495_p(pos);
    Block block = state.func_177230_c();
    if (block instanceof IShearable) {
      IShearable target = (IShearable)block;
      if (target.isShearable(itemstack, (IBlockAccess)world, pos) && ElectricItem.manager
        .use(itemstack, this.operationEnergyCost, (EntityLivingBase)player)) {
        List<ItemStack> drops = target.onSheared(itemstack, (IBlockAccess)world, pos, 
            EnchantmentHelper.func_77506_a(Enchantments.field_185308_t, itemstack));
        for (ItemStack stack : drops)
          StackUtil.dropAsEntity(world, pos, stack); 
        player.func_71064_a(StatList.func_188055_a(block), 1);
        world.func_180501_a(pos, Blocks.field_150350_a.func_176223_P(), 11);
        return true;
      } 
    } 
    return false;
  }
  
  @SideOnly(Side.CLIENT)
  public String getHitSoundForBlock(EntityPlayerSP player, World world, BlockPos pos, ItemStack stack) {
    return null;
  }
  
  @SideOnly(Side.CLIENT)
  public String getBreakSoundForBlock(EntityPlayerSP player, World world, BlockPos pos, ItemStack stack) {
    return null;
  }
  
  protected String getIdleSound(EntityLivingBase player, ItemStack stack) {
    return "Tools/Chainsaw/ChainsawIdle.ogg";
  }
  
  protected String getStopSound(EntityLivingBase player, ItemStack stack) {
    return "Tools/Chainsaw/ChainsawStop.ogg";
  }
}
