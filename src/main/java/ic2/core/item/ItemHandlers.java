// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item;

import ic2.core.item.type.CellType;
import net.minecraftforge.fluids.FluidRegistry;
import ic2.core.util.LiquidUtil;
import ic2.core.item.upgrade.ItemUpgradeModule;
import ic2.core.IC2;
import net.minecraft.tileentity.TileEntity;
import ic2.core.block.reactor.tileentity.TileEntityNuclearReactorElectric;
import net.minecraft.item.Item;
import ic2.core.IC2Potion;
import ic2.core.item.armor.ItemArmorHazmat;
import ic2.core.item.type.IRadioactiveItemType;
import ic2.core.block.BlockSheet;
import ic2.core.ref.BlockName;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.BlockPistonBase;
import ic2.api.recipe.Recipes;
import ic2.core.ref.FluidName;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.RayTraceResult;
import ic2.core.util.Util;
import net.minecraft.util.ActionResult;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;
import ic2.core.util.StackUtil;
import net.minecraft.util.SoundCategory;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.world.IBlockAccess;
import ic2.core.ref.ItemName;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Block;
import ic2.core.ref.TeBlock;

public class ItemHandlers
{
    public static ItemMulti.IItemRightClickHandler cfPowderApply;
    public static ItemMulti.IItemRightClickHandler scrapBoxUnpack;
    public static ItemMulti.IItemUseHandler resinUse;
    public static ItemMulti.IItemUpdateHandler radioactiveUpdate;
    public static TeBlock.ITePlaceHandler reactorChamberPlace;
    public static ItemMulti.IItemRightClickHandler openAdvancedUpgradeGUI;
    public static ItemMulti.IItemUseHandler emptyCellFill;
    
    public static ItemMulti.IItemUseHandler getFluidPlacer(final Block type) {
        return new ItemMulti.IItemUseHandler() {
            @Override
            public EnumActionResult onUse(final ItemStack stack, final EntityPlayer player, BlockPos pos, final EnumHand hand, final EnumFacing side) {
                assert stack.getItem() == ItemName.misc_resource.getInstance();
                final World world = player.getEntityWorld();
                if (!world.getBlockState(pos).getBlock().isReplaceable((IBlockAccess)world, pos)) {
                    pos = pos.offset(side);
                }
                if (player.canPlayerEdit(pos, side, stack) && world.mayPlace(type, pos, false, side, (Entity)null)) {
                    final IBlockState placedState = type.getStateForPlacement(world, pos, side, 0.0f, 0.0f, 0.0f, 0, (EntityLivingBase)player, hand);
                    world.setBlockState(pos, placedState);
                    final SoundType sound = placedState.getBlock().getSoundType(placedState, world, pos, (Entity)player);
                    world.playSound(player, pos, sound.getPlaceSound(), SoundCategory.BLOCKS, (sound.getVolume() + 1.0f) / 2.0f, sound.getPitch() * 0.8f);
                    StackUtil.consumeOrError(player, hand, 1);
                    return EnumActionResult.SUCCESS;
                }
                return EnumActionResult.FAIL;
            }
        };
    }
    
    static {
        ItemHandlers.cfPowderApply = new ItemMulti.IItemRightClickHandler() {
            @Override
            public ActionResult<ItemStack> onRightClick(ItemStack stack, final EntityPlayer player, final EnumHand hand) {
                final RayTraceResult position = Util.traceBlocks(player, true);
                if (position == null) {
                    return (ActionResult<ItemStack>)new ActionResult(EnumActionResult.PASS, (Object)stack);
                }
                if (position.typeOfHit == RayTraceResult.Type.BLOCK) {
                    final World world = player.getEntityWorld();
                    if (!world.canMineBlockBody(player, position.getBlockPos())) {
                        return (ActionResult<ItemStack>)new ActionResult(EnumActionResult.FAIL, (Object)stack);
                    }
                    if (world.getBlockState(position.getBlockPos()).getBlock() == Blocks.WATER) {
                        stack = StackUtil.decSize(stack);
                        world.setBlockState(position.getBlockPos(), FluidName.construction_foam.getInstance().getBlock().getDefaultState());
                        new ActionResult(EnumActionResult.SUCCESS, (Object)stack);
                    }
                }
                return (ActionResult<ItemStack>)new ActionResult(EnumActionResult.FAIL, (Object)stack);
            }
        };
        ItemHandlers.scrapBoxUnpack = new ItemMulti.IItemRightClickHandler() {
            @Override
            public ActionResult<ItemStack> onRightClick(ItemStack stack, final EntityPlayer player, final EnumHand hand) {
                if (!player.getEntityWorld().isRemote) {
                    final ItemStack drop = Recipes.scrapboxDrops.getDrop(stack, false);
                    if (drop != null && player.dropItem(drop, false) != null && !player.capabilities.isCreativeMode) {
                        stack = StackUtil.decSize(stack);
                        return (ActionResult<ItemStack>)new ActionResult(EnumActionResult.SUCCESS, (Object)stack);
                    }
                }
                return (ActionResult<ItemStack>)new ActionResult(EnumActionResult.PASS, (Object)stack);
            }
        };
        ItemHandlers.resinUse = new ItemMulti.IItemUseHandler() {
            @Override
            public EnumActionResult onUse(final ItemStack stack, final EntityPlayer player, BlockPos pos, final EnumHand hand, final EnumFacing side) {
                final World world = player.getEntityWorld();
                final IBlockState state = world.getBlockState(pos);
                if (state.getBlock() == Blocks.PISTON && state.getValue((IProperty)BlockPistonBase.FACING) == side) {
                    final IBlockState newState = Blocks.STICKY_PISTON.getDefaultState().withProperty((IProperty)BlockPistonBase.FACING, (Comparable)side).withProperty((IProperty)BlockPistonBase.EXTENDED, state.getValue((IProperty)BlockPistonBase.EXTENDED));
                    world.setBlockState(pos, newState, 3);
                    if (!player.capabilities.isCreativeMode) {
                        StackUtil.consumeOrError(player, hand, 1);
                    }
                    return EnumActionResult.SUCCESS;
                }
                if (side != EnumFacing.UP) {
                    return EnumActionResult.PASS;
                }
                pos = pos.up();
                if (!state.getBlock().isAir(state, (IBlockAccess)world, pos) || !BlockName.sheet.getInstance().canPlaceBlockOnSide(world, pos, side)) {
                    return EnumActionResult.PASS;
                }
                world.setBlockState(pos, BlockName.sheet.getBlockState(BlockSheet.SheetType.resin));
                if (!player.capabilities.isCreativeMode) {
                    StackUtil.consumeOrError(player, hand, 1);
                }
                return EnumActionResult.PASS;
            }
        };
        ItemHandlers.radioactiveUpdate = new ItemMulti.IItemUpdateHandler() {
            @Override
            public void onUpdate(final ItemStack stack, final World world, final Entity rawEntity, final int slotIndex, final boolean isCurrentItem) {
                final Item item = stack.getItem();
                if (item == null || !(item instanceof ItemMulti)) {
                    return;
                }
                final Object rawType = ((ItemMulti)item).getType(stack);
                if (!(rawType instanceof IRadioactiveItemType)) {
                    return;
                }
                final IRadioactiveItemType type = (IRadioactiveItemType)rawType;
                if (!(rawEntity instanceof EntityLivingBase)) {
                    return;
                }
                final EntityLivingBase entity = (EntityLivingBase)rawEntity;
                if (ItemArmorHazmat.hasCompleteHazmat(entity)) {
                    return;
                }
                IC2Potion.radiation.applyTo(entity, type.getRadiationDuration() * 20, type.getRadiationAmplifier());
            }
        };
        ItemHandlers.reactorChamberPlace = new TeBlock.ITePlaceHandler() {
            @Override
            public boolean canReplace(final World world, final BlockPos pos, final EnumFacing side, final ItemStack stack) {
                int count = 0;
                for (final EnumFacing dir : EnumFacing.VALUES) {
                    final TileEntity te = world.getTileEntity(pos.offset(dir));
                    if (te instanceof TileEntityNuclearReactorElectric) {
                        ++count;
                    }
                }
                return count == 1;
            }
        };
        ItemHandlers.openAdvancedUpgradeGUI = new ItemMulti.IItemRightClickHandler() {
            @Override
            public ActionResult<ItemStack> onRightClick(final ItemStack stack, final EntityPlayer player, final EnumHand hand) {
                assert stack.getItem() == ItemName.upgrade.getInstance();
                if (IC2.platform.isSimulating()) {
                    IC2.platform.launchGui(player, ((ItemUpgradeModule)stack.getItem()).getInventory(player, stack));
                }
                return (ActionResult<ItemStack>)new ActionResult(EnumActionResult.SUCCESS, (Object)stack);
            }
        };
        ItemHandlers.emptyCellFill = new ItemMulti.IItemUseHandler() {
            @Override
            public EnumActionResult onUse(final ItemStack stack, final EntityPlayer player, BlockPos pos, final EnumHand hand, final EnumFacing side) {
                assert stack.getItem() == ItemName.cell.getInstance();
                final World world = player.getEntityWorld();
                final RayTraceResult position = Util.traceBlocks(player, true);
                if (position == null) {
                    return EnumActionResult.FAIL;
                }
                if (position.typeOfHit == RayTraceResult.Type.BLOCK) {
                    pos = position.getBlockPos();
                    if (!world.canMineBlockBody(player, pos)) {
                        return EnumActionResult.FAIL;
                    }
                    if (!player.canPlayerEdit(pos, position.sideHit, player.getHeldItem(hand))) {
                        return EnumActionResult.FAIL;
                    }
                    final LiquidUtil.LiquidData data = LiquidUtil.getLiquid(world, pos);
                    if (data != null && data.isSource) {
                        if (data.liquid == FluidRegistry.WATER && StackUtil.storeInventoryItem(ItemName.cell.getItemStack(CellType.water), player, true)) {
                            world.setBlockToAir(pos);
                            StackUtil.consumeOrError(player, hand, 1);
                            StackUtil.storeInventoryItem(ItemName.cell.getItemStack(CellType.water), player, false);
                            return EnumActionResult.SUCCESS;
                        }
                        if (data.liquid == FluidRegistry.LAVA && StackUtil.storeInventoryItem(ItemName.cell.getItemStack(CellType.lava), player, true)) {
                            world.setBlockToAir(pos);
                            StackUtil.consumeOrError(player, hand, 1);
                            StackUtil.storeInventoryItem(ItemName.cell.getItemStack(CellType.lava), player, false);
                            return EnumActionResult.SUCCESS;
                        }
                    }
                }
                return EnumActionResult.PASS;
            }
        };
    }
}
