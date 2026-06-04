// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tool;

import ic2.core.block.state.IIdProvider;
import net.minecraft.entity.EntityLivingBase;
import ic2.core.init.Localization;
import net.minecraft.init.Items;
import net.minecraft.client.util.ITooltipFlag;
import java.util.List;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.util.NonNullList;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import com.google.common.collect.UnmodifiableIterator;
import net.minecraft.block.BlockStainedGlassPane;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.block.BlockColored;
import net.minecraft.init.Blocks;
import net.minecraft.block.properties.IProperty;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import ic2.core.audio.PositionSpec;
import ic2.core.IC2;
import ic2.core.util.StackUtil;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.util.Util;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.Iterator;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.util.ResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraftforge.common.MinecraftForge;
import ic2.core.ref.ItemName;
import ic2.core.block.state.EnumProperty;
import ic2.api.item.IBoxable;
import ic2.core.util.Ic2Color;
import ic2.core.ref.IMultiItem;
import ic2.core.item.ItemIC2;

public class ItemToolPainter extends ItemIC2 implements IMultiItem<Ic2Color>, IBoxable
{
    private static final EnumProperty<Ic2Color> typeProperty;
    private static final int maxDamage = 32;
    
    public ItemToolPainter() {
        super(ItemName.painter);
        this.setMaxDamage(31);
        this.setMaxStackSize(1);
        this.setHasSubtypes(true);
        MinecraftForge.EVENT_BUS.register((Object)this);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public void registerModels(final ItemName name) {
        ModelLoader.setCustomMeshDefinition((Item)this, (ItemMeshDefinition)new ItemMeshDefinition() {
            public ModelResourceLocation getModelLocation(final ItemStack stack) {
                final Ic2Color color = ItemToolPainter.this.getColor(stack);
                return ItemIC2.getModelLocation(name, (color != null) ? color.getName() : null);
            }
        });
        ModelBakery.registerItemVariants((Item)this, new ResourceLocation[] { (ResourceLocation)ItemIC2.getModelLocation(name, null) });
        for (final Ic2Color type : ItemToolPainter.typeProperty.getAllowedValues()) {
            ModelBakery.registerItemVariants((Item)this, new ResourceLocation[] { (ResourceLocation)ItemIC2.getModelLocation(name, type.getName()) });
        }
    }
    
    public int getDamage(final ItemStack stack) {
        final int rawDamage = super.getDamage(stack);
        if (rawDamage == 0) {
            return 0;
        }
        return (rawDamage - 1) / Ic2Color.values.length;
    }
    
    public boolean isDamaged(final ItemStack stack) {
        return this.getDamage(stack) > 0;
    }
    
    public void setDamage(final ItemStack stack, final int damage) {
        final int oldRawDamage = super.getDamage(stack);
        if (oldRawDamage == 0) {
            return;
        }
        final int oldDamage = this.getDamage(stack);
        final int newDamage = Util.limit(damage, 0, 32);
        super.setDamage(stack, oldRawDamage + (newDamage - oldDamage) * Ic2Color.values.length);
    }
    
    public int getMetadata(final ItemStack stack) {
        final int rawDamage = super.getDamage(stack);
        if (rawDamage == 0 || rawDamage == 32767) {
            return rawDamage;
        }
        return (rawDamage - 1) % Ic2Color.values.length + 1;
    }
    
    public Ic2Color getColor(final ItemStack stack) {
        final int meta = this.getMetadata(stack);
        if (meta == 0) {
            return null;
        }
        return Ic2Color.values[meta - 1];
    }
    
    public EnumActionResult onItemUseFirst(final EntityPlayer player, final World world, final BlockPos pos, final EnumFacing side, final float hitX, final float hitY, final float hitZ, final EnumHand hand) {
        final ItemStack stack = StackUtil.get(player, hand);
        final Ic2Color color = this.getColor(stack);
        if (color == null) {
            return EnumActionResult.PASS;
        }
        final IBlockState state = world.getBlockState(pos);
        final Block block = state.getBlock();
        if (block.recolorBlock(world, pos, side, color.mcColor) || this.colorBlock(world, pos, block, state, color.mcColor)) {
            this.damagePainter(player, hand, color);
            if (world.isRemote) {
                IC2.audioManager.playOnce(player, PositionSpec.Hand, "Tools/Painter.ogg", true, IC2.audioManager.getDefaultVolume());
            }
            return world.isRemote ? EnumActionResult.PASS : EnumActionResult.SUCCESS;
        }
        return EnumActionResult.PASS;
    }
    
    private boolean colorBlock(final World world, final BlockPos pos, final Block block, final IBlockState state, final EnumDyeColor newColor) {
        for (final IProperty<?> property : state.getProperties().keySet()) {
            if (property.getValueClass() == EnumDyeColor.class) {
                final IProperty<EnumDyeColor> typedProperty = (IProperty<EnumDyeColor>)property;
                final EnumDyeColor oldColor = (EnumDyeColor)state.getValue((IProperty)typedProperty);
                if (oldColor == newColor || !typedProperty.getAllowedValues().contains(newColor)) {
                    return false;
                }
                world.setBlockState(pos, state.withProperty((IProperty)typedProperty, (Comparable)newColor));
                return true;
            }
        }
        if (block == Blocks.HARDENED_CLAY) {
            world.setBlockState(pos, Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty((IProperty)BlockColored.COLOR, (Comparable)newColor));
            return true;
        }
        if (block == Blocks.GLASS) {
            world.setBlockState(pos, Blocks.STAINED_GLASS.getDefaultState().withProperty((IProperty)BlockStainedGlass.COLOR, (Comparable)newColor));
            return true;
        }
        if (block == Blocks.GLASS_PANE) {
            world.setBlockState(pos, Blocks.STAINED_GLASS_PANE.getDefaultState().withProperty((IProperty)BlockStainedGlassPane.COLOR, (Comparable)newColor));
            return true;
        }
        return false;
    }
    
    @SubscribeEvent
    public void onEntityInteract(final PlayerInteractEvent.EntityInteract event) {
        final EntityPlayer player = event.getEntityPlayer();
        if (player.getEntityWorld().isRemote) {
            return;
        }
        final Entity entity = event.getEntity();
        final ItemStack stack = player.getActiveItemStack();
        if (StackUtil.isEmpty(stack) || stack.getItem() != this) {
            return;
        }
        final Ic2Color color = this.getColor(stack);
        if (color == null) {
            return;
        }
        if (entity instanceof EntitySheep) {
            final EntitySheep sheep = (EntitySheep)entity;
            if (sheep.getFleeceColor() != color.mcColor) {
                ((EntitySheep)entity).setFleeceColor(color.mcColor);
                this.damagePainter(player, event.getHand(), color);
                event.setCanceled(true);
            }
        }
    }
    
    public ActionResult<ItemStack> onItemRightClick(final World world, final EntityPlayer player, final EnumHand hand) {
        final ItemStack stack = StackUtil.get(player, hand);
        if (!world.isRemote && IC2.keyboard.isModeSwitchKeyDown(player)) {
            final NBTTagCompound nbtData = StackUtil.getOrCreateNbtData(stack);
            final boolean newValue = !nbtData.getBoolean("autoRefill");
            nbtData.setBoolean("autoRefill", newValue);
            if (newValue) {
                IC2.platform.messagePlayer(player, "Painter automatic refill mode enabled", new Object[0]);
            }
            else {
                IC2.platform.messagePlayer(player, "Painter automatic refill mode disabled", new Object[0]);
            }
            return (ActionResult<ItemStack>)new ActionResult(EnumActionResult.SUCCESS, (Object)stack);
        }
        return (ActionResult<ItemStack>)new ActionResult(EnumActionResult.PASS, (Object)stack);
    }
    
    @Override
    public String getUnlocalizedName(final ItemStack stack) {
        final Ic2Color color = this.getColor(stack);
        if (color == null) {
            return this.getUnlocalizedName();
        }
        return this.getUnlocalizedName() + "." + color.getName();
    }
    
    public final void getSubItems(final CreativeTabs tab, final NonNullList<ItemStack> subItems) {
        if (!this.isInCreativeTab(tab)) {
            return;
        }
        subItems.add((Object)this.getItemStackUnchecked(null));
        for (final Ic2Color type : ItemToolPainter.typeProperty.getAllowedValues()) {
            subItems.add((Object)this.getItemStackUnchecked(type));
        }
    }
    
    @Override
    public Set<Ic2Color> getAllTypes() {
        return EnumSet.allOf(Ic2Color.class);
    }
    
    @SideOnly(Side.CLIENT)
    public void addInformation(final ItemStack stack, final World world, final List<String> tooltip, final ITooltipFlag advanced) {
        final Ic2Color color = this.getColor(stack);
        if (color == null) {
            return;
        }
        final ItemStack dyeStack = new ItemStack(Items.DYE, 1, color.mcColor.getDyeDamage());
        tooltip.add(Localization.translate(Items.DYE.getUnlocalizedName(dyeStack) + ".name"));
    }
    
    public void damagePainter(final EntityPlayer player, final EnumHand hand, final Ic2Color color) {
        assert color != null;
        final ItemStack stack = StackUtil.get(player, hand);
        if (stack.getItemDamage() >= stack.getMaxDamage()) {
            final NBTTagCompound nbtData = StackUtil.getOrCreateNbtData(stack);
            if (nbtData.getBoolean("autoRefill") && StackUtil.consumeFromPlayerInventory(player, StackUtil.oreDict(color.oreDictDyeName), 1, false)) {
                this.setDamage(stack, 0);
            }
            else {
                super.setDamage(stack, 0);
            }
        }
        else {
            stack.damageItem(1, (EntityLivingBase)player);
        }
    }
    
    @Override
    public ItemStack getItemStack(final Ic2Color type) {
        if (type != null && !ItemToolPainter.typeProperty.getAllowedValues().contains(type)) {
            throw new IllegalArgumentException("invalid property value " + type + " for property " + ItemToolPainter.typeProperty);
        }
        return this.getItemStackUnchecked(type);
    }
    
    private ItemStack getItemStackUnchecked(final Ic2Color type) {
        if (type == null) {
            return new ItemStack((Item)this);
        }
        return new ItemStack((Item)this, 1, 1 + type.getId());
    }
    
    @Override
    public ItemStack getItemStack(final String variant) {
        Ic2Color type;
        if (variant != null && !variant.isEmpty()) {
            type = ItemToolPainter.typeProperty.getValue(variant);
            if (type == null) {
                throw new IllegalArgumentException("invalid variant " + variant + " for " + this);
            }
        }
        else {
            type = null;
        }
        return this.getItemStackUnchecked(type);
    }
    
    @Override
    public String getVariant(final ItemStack stack) {
        if (stack == null) {
            throw new NullPointerException("null stack");
        }
        if (stack.getItem() != this) {
            throw new IllegalArgumentException("The stack " + stack + " doesn't match " + this);
        }
        final Ic2Color color = this.getColor(stack);
        if (color == null) {
            return null;
        }
        return color.getName();
    }
    
    @Override
    public boolean canBeStoredInToolbox(final ItemStack itemstack) {
        return true;
    }
    
    static {
        typeProperty = new EnumProperty<Ic2Color>("type", Ic2Color.class);
    }
}
