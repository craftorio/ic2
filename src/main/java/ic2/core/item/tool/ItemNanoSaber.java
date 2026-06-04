// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tool;

import ic2.core.audio.PositionSpec;
import net.minecraft.item.EnumRarity;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.nbt.NBTTagCompound;
import ic2.core.util.StackUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import java.util.Iterator;
import ic2.core.item.armor.ItemArmorQuantumSuit;
import ic2.core.item.armor.ItemArmorNanoSuit;
import ic2.core.slot.ArmorSlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import com.google.common.collect.HashMultimap;
import ic2.api.item.ElectricItem;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import com.google.common.collect.Multimap;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.init.Blocks;
import ic2.core.IC2;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.util.ResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import ic2.core.item.ItemIC2;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.client.renderer.ItemMeshDefinition;
import java.util.Set;
import java.util.EnumSet;
import ic2.core.ref.ItemName;

public class ItemNanoSaber extends ItemElectricTool
{
    public static int ticker;
    private int soundTicker;
    
    public ItemNanoSaber() {
        super(ItemName.nano_saber, 10, HarvestLevel.Diamond, EnumSet.of(ToolClass.Sword));
        this.soundTicker = 0;
        this.maxCharge = 160000;
        this.transferLimit = 500;
        this.tier = 3;
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public void registerModels(final ItemName name) {
        final String activeSuffix = "active";
        ModelLoader.setCustomMeshDefinition((Item)this, (ItemMeshDefinition)new ItemMeshDefinition() {
            public ModelResourceLocation getModelLocation(final ItemStack stack) {
                return ItemIC2.getModelLocation(name, isActive(stack) ? "active" : null);
            }
        });
        ModelBakery.registerItemVariants((Item)this, new ResourceLocation[] { (ResourceLocation)ItemIC2.getModelLocation(name, null) });
        ModelBakery.registerItemVariants((Item)this, new ResourceLocation[] { (ResourceLocation)ItemIC2.getModelLocation(name, "active") });
    }
    
    @Override
    public float getDestroySpeed(final ItemStack stack, final IBlockState state) {
        if (isActive(stack)) {
            ++this.soundTicker;
            if (IC2.platform.isRendering() && this.soundTicker % 4 == 0) {
                IC2.platform.playSoundSp(this.getRandomSwingSound(), 1.0f, 1.0f);
            }
            return (state.getBlock() == Blocks.WEB) ? 50.0f : 4.0f;
        }
        return 1.0f;
    }
    
    public Multimap<String, AttributeModifier> getAttributeModifiers(final EntityEquipmentSlot slot, final ItemStack stack) {
        if (slot != EntityEquipmentSlot.MAINHAND) {
            return (Multimap<String, AttributeModifier>)super.getAttributeModifiers(slot, stack);
        }
        int dmg = 4;
        if (ElectricItem.manager.canUse(stack, 400.0) && isActive(stack)) {
            dmg = 20;
        }
        final Multimap<String, AttributeModifier> ret = (Multimap<String, AttributeModifier>)HashMultimap.create();
        ret.put((Object)SharedMonsterAttributes.ATTACK_SPEED.getName(), (Object)new AttributeModifier(ItemNanoSaber.ATTACK_SPEED_MODIFIER, "Tool modifier", (double)this.attackSpeed, 0));
        ret.put((Object)SharedMonsterAttributes.ATTACK_DAMAGE.getName(), (Object)new AttributeModifier(Item.ATTACK_DAMAGE_MODIFIER, "Tool modifier", (double)dmg, 0));
        return ret;
    }
    
    @Override
    public boolean hitEntity(final ItemStack stack, final EntityLivingBase target, final EntityLivingBase source) {
        if (!isActive(stack)) {
            return true;
        }
        if (IC2.platform.isSimulating()) {
            drainSaber(stack, 400.0, source);
            if (!(source instanceof EntityPlayerMP) || !(target instanceof EntityPlayer) || ((EntityPlayerMP)source).canAttackPlayer((EntityPlayer)target)) {
                for (final EntityEquipmentSlot slot : ArmorSlot.getAll()) {
                    if (!ElectricItem.manager.canUse(stack, 2000.0)) {
                        break;
                    }
                    final ItemStack armor = target.getItemStackFromSlot(slot);
                    if (armor == null) {
                        continue;
                    }
                    double amount = 0.0;
                    if (armor.getItem() instanceof ItemArmorNanoSuit) {
                        amount = 48000.0;
                    }
                    else if (armor.getItem() instanceof ItemArmorQuantumSuit) {
                        amount = 300000.0;
                    }
                    if (amount <= 0.0) {
                        continue;
                    }
                    ElectricItem.manager.discharge(armor, amount, this.tier, true, false, false);
                    if (!ElectricItem.manager.canUse(armor, 1.0)) {
                        target.setItemStackToSlot(slot, (ItemStack)null);
                    }
                    drainSaber(stack, 2000.0, source);
                }
            }
        }
        if (IC2.platform.isRendering()) {
            IC2.platform.playSoundSp(this.getRandomSwingSound(), 1.0f, 1.0f);
        }
        return true;
    }
    
    public String getRandomSwingSound() {
        switch (IC2.random.nextInt(3)) {
            default: {
                return "Tools/Nanosabre/NanosabreSwing1.ogg";
            }
            case 1: {
                return "Tools/Nanosabre/NanosabreSwing2.ogg";
            }
            case 2: {
                return "Tools/Nanosabre/NanosabreSwing3.ogg";
            }
        }
    }
    
    public boolean canDestroyBlockInCreative(final World world, final BlockPos pos, final ItemStack stack, final EntityPlayer player) {
        return false;
    }
    
    public boolean onBlockStartBreak(final ItemStack stack, final BlockPos pos, final EntityPlayer player) {
        if (isActive(stack)) {
            drainSaber(stack, 80.0, (EntityLivingBase)player);
        }
        return false;
    }
    
    public boolean isFull3D() {
        return true;
    }
    
    public static void drainSaber(final ItemStack stack, final double amount, final EntityLivingBase entity) {
        if (!ElectricItem.manager.use(stack, amount, entity)) {
            final NBTTagCompound nbt = StackUtil.getOrCreateNbtData(stack);
            setActive(nbt, false);
        }
    }
    
    @Override
    public ActionResult<ItemStack> onItemRightClick(final World world, final EntityPlayer player, final EnumHand hand) {
        final ItemStack stack = StackUtil.get(player, hand);
        if (world.isRemote) {
            return (ActionResult<ItemStack>)new ActionResult(EnumActionResult.PASS, (Object)stack);
        }
        final NBTTagCompound nbt = StackUtil.getOrCreateNbtData(stack);
        if (isActive(nbt)) {
            setActive(nbt, false);
            return (ActionResult<ItemStack>)new ActionResult(EnumActionResult.SUCCESS, (Object)stack);
        }
        if (ElectricItem.manager.canUse(stack, 16.0)) {
            setActive(nbt, true);
            return (ActionResult<ItemStack>)new ActionResult(EnumActionResult.SUCCESS, (Object)stack);
        }
        return super.onItemRightClick(world, player, hand);
    }
    
    @Override
    public void onUpdate(final ItemStack stack, final World world, final Entity entity, final int slot, final boolean par5) {
        super.onUpdate(stack, world, entity, slot, par5 && isActive(stack));
        if (!isActive(stack)) {
            return;
        }
        if (ItemNanoSaber.ticker % 16 == 0 && entity instanceof EntityPlayerMP) {
            if (slot < 9) {
                drainSaber(stack, 64.0, (EntityLivingBase)entity);
            }
            else if (ItemNanoSaber.ticker % 64 == 0) {
                drainSaber(stack, 16.0, (EntityLivingBase)entity);
            }
        }
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public EnumRarity getRarity(final ItemStack stack) {
        return EnumRarity.UNCOMMON;
    }
    
    private static boolean isActive(final ItemStack stack) {
        final NBTTagCompound nbt = StackUtil.getOrCreateNbtData(stack);
        return isActive(nbt);
    }
    
    private static boolean isActive(final NBTTagCompound nbt) {
        return nbt.getBoolean("active");
    }
    
    private static void setActive(final NBTTagCompound nbt, final boolean active) {
        nbt.setBoolean("active", active);
    }
    
    public boolean onEntitySwing(final EntityLivingBase entity, final ItemStack stack) {
        if (IC2.platform.isRendering() && isActive(stack)) {
            IC2.audioManager.playOnce(entity, PositionSpec.Hand, this.getRandomSwingSound(), true, IC2.audioManager.getDefaultVolume());
        }
        return false;
    }
    
    @Override
    protected String getIdleSound(final EntityLivingBase player, final ItemStack stack) {
        return "Tools/Nanosabre/NanosabreIdle.ogg";
    }
    
    @Override
    protected String getStartSound(final EntityLivingBase player, final ItemStack stack) {
        return "Tools/Nanosabre/NanosabrePowerup.ogg";
    }
    
    static {
        ItemNanoSaber.ticker = 0;
    }
}
