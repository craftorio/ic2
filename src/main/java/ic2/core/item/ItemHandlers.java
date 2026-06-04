package ic2.core.item;

import ic2.api.recipe.Recipes;
import ic2.core.IC2;
import ic2.core.IC2Potion;
import ic2.core.block.BlockSheet;
import ic2.core.block.state.IIdProvider;
import ic2.core.item.armor.ItemArmorHazmat;
import ic2.core.item.type.CellType;
import ic2.core.item.type.IRadioactiveItemType;
import ic2.core.item.upgrade.ItemUpgradeModule;
import ic2.core.ref.BlockName;
import ic2.core.ref.FluidName;
import ic2.core.ref.ItemName;
import ic2.core.ref.TeBlock;
import ic2.core.util.LiquidUtil;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;

public class ItemHandlers {
  public static ItemMulti.IItemRightClickHandler cfPowderApply = new ItemMulti.IItemRightClickHandler() {
      public ActionResult<ItemStack> onRightClick(ItemStack stack, EntityPlayer player, EnumHand hand) {
        RayTraceResult position = Util.traceBlocks(player, true);
        if (position == null)
          return new ActionResult(EnumActionResult.PASS, stack); 
        if (position.typeOfHit == RayTraceResult.Type.BLOCK) {
          World world = player.getEntityWorld();
          if (!world.canMineBlockBody(player, position.getBlockPos()))
            return new ActionResult(EnumActionResult.FAIL, stack); 
          if (world.getBlockState(position.getBlockPos()).getBlock() == Blocks.WATER) {
            stack = StackUtil.decSize(stack);
            world.setBlockState(position.getBlockPos(), FluidName.construction_foam.getInstance().getBlock().getDefaultState());
            new ActionResult(EnumActionResult.SUCCESS, stack);
          } 
        } 
        return new ActionResult(EnumActionResult.FAIL, stack);
      }
    };
  
  public static ItemMulti.IItemRightClickHandler scrapBoxUnpack = new ItemMulti.IItemRightClickHandler() {
      public ActionResult<ItemStack> onRightClick(ItemStack stack, EntityPlayer player, EnumHand hand) {
        if (!(player.getEntityWorld()).isRemote) {
          ItemStack drop = Recipes.scrapboxDrops.getDrop(stack, false);
          if (drop != null && player
            .dropItem(drop, false) != null && !player.capabilities.isCreativeMode) {
            stack = StackUtil.decSize(stack);
            return new ActionResult(EnumActionResult.SUCCESS, stack);
          } 
        } 
        return new ActionResult(EnumActionResult.PASS, stack);
      }
    };
  
  public static ItemMulti.IItemUseHandler resinUse = new ItemMulti.IItemUseHandler() {
      public EnumActionResult onUse(ItemStack stack, EntityPlayer player, BlockPos pos, EnumHand hand, EnumFacing side) {
        World world = player.getEntityWorld();
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() == Blocks.PISTON && state.getValue((IProperty)BlockPistonBase.FACING) == side) {
          IBlockState newState = Blocks.STICKY_PISTON.getDefaultState().withProperty((IProperty)BlockPistonBase.FACING, (Comparable)side).withProperty((IProperty)BlockPistonBase.EXTENDED, state.getValue((IProperty)BlockPistonBase.EXTENDED));
          world.setBlockState(pos, newState, 3);
          if (!player.capabilities.isCreativeMode)
            StackUtil.consumeOrError(player, hand, 1); 
          return EnumActionResult.SUCCESS;
        } 
        if (side != EnumFacing.UP)
          return EnumActionResult.PASS; 
        pos = pos.up();
        if (!state.getBlock().isAir(state, (IBlockAccess)world, pos) || 
          !BlockName.sheet.getInstance().canPlaceBlockOnSide(world, pos, side))
          return EnumActionResult.PASS; 
        world.setBlockState(pos, BlockName.sheet.getBlockState((IIdProvider)BlockSheet.SheetType.resin));
        if (!player.capabilities.isCreativeMode)
          StackUtil.consumeOrError(player, hand, 1); 
        return EnumActionResult.PASS;
      }
    };
  
  public static ItemMulti.IItemUpdateHandler radioactiveUpdate = new ItemMulti.IItemUpdateHandler() {
      public void onUpdate(ItemStack stack, World world, Entity rawEntity, int slotIndex, boolean isCurrentItem) {
        Item item = stack.getItem();
        if (item == null || !(item instanceof ItemMulti))
          return; 
        Object rawType = ((ItemMulti)item).getType(stack);
        if (!(rawType instanceof IRadioactiveItemType))
          return; 
        IRadioactiveItemType type = (IRadioactiveItemType)rawType;
        if (!(rawEntity instanceof EntityLivingBase))
          return; 
        EntityLivingBase entity = (EntityLivingBase)rawEntity;
        if (ItemArmorHazmat.hasCompleteHazmat(entity))
          return; 
        IC2Potion.radiation.applyTo(entity, type.getRadiationDuration() * 20, type.getRadiationAmplifier());
      }
    };
  
  public static TeBlock.ITePlaceHandler reactorChamberPlace = new TeBlock.ITePlaceHandler() {
      public boolean canReplace(World world, BlockPos pos, EnumFacing side, ItemStack stack) {
        int count = 0;
        for (EnumFacing dir : EnumFacing.VALUES) {
          TileEntity te = world.getTileEntity(pos.offset(dir));
          if (te instanceof ic2.core.block.reactor.tileentity.TileEntityNuclearReactorElectric)
            count++; 
        } 
        return (count == 1);
      }
    };
  
  public static ItemMulti.IItemRightClickHandler openAdvancedUpgradeGUI = new ItemMulti.IItemRightClickHandler() {
      public ActionResult<ItemStack> onRightClick(ItemStack stack, EntityPlayer player, EnumHand hand) {
        assert stack.getItem() == ItemName.upgrade.getInstance();
        if (IC2.platform.isSimulating())
          IC2.platform.launchGui(player, ((ItemUpgradeModule)stack.getItem()).getInventory(player, stack)); 
        return new ActionResult(EnumActionResult.SUCCESS, stack);
      }
    };
  
  public static ItemMulti.IItemUseHandler getFluidPlacer(final Block type) {
    return new ItemMulti.IItemUseHandler() {
        public EnumActionResult onUse(ItemStack stack, EntityPlayer player, BlockPos pos, EnumHand hand, EnumFacing side) {
          assert stack.getItem() == ItemName.misc_resource.getInstance();
          World world = player.getEntityWorld();
          if (!world.getBlockState(pos).getBlock().isReplaceable((IBlockAccess)world, pos))
            pos = pos.offset(side); 
          if (player.canPlayerEdit(pos, side, stack) && world.mayPlace(type, pos, false, side, null)) {
            IBlockState placedState = type.getStateForPlacement(world, pos, side, 0.0F, 0.0F, 0.0F, 0, (EntityLivingBase)player, hand);
            world.setBlockState(pos, placedState);
            SoundType sound = placedState.getBlock().getSoundType(placedState, world, pos, (Entity)player);
            world.playSound(player, pos, sound.getPlaceSound(), SoundCategory.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
            StackUtil.consumeOrError(player, hand, 1);
            return EnumActionResult.SUCCESS;
          } 
          return EnumActionResult.FAIL;
        }
      };
  }
  
  public static ItemMulti.IItemUseHandler emptyCellFill = new ItemMulti.IItemUseHandler() {
      public EnumActionResult onUse(ItemStack stack, EntityPlayer player, BlockPos pos, EnumHand hand, EnumFacing side) {
        assert stack.getItem() == ItemName.cell.getInstance();
        World world = player.getEntityWorld();
        RayTraceResult position = Util.traceBlocks(player, true);
        if (position == null)
          return EnumActionResult.FAIL; 
        if (position.typeOfHit == RayTraceResult.Type.BLOCK) {
          pos = position.getBlockPos();
          if (!world.canMineBlockBody(player, pos))
            return EnumActionResult.FAIL; 
          if (!player.canPlayerEdit(pos, position.sideHit, player.getHeldItem(hand)))
            return EnumActionResult.FAIL; 
          LiquidUtil.LiquidData data = LiquidUtil.getLiquid(world, pos);
          if (data != null && data.isSource) {
            if (data.liquid == FluidRegistry.WATER && StackUtil.storeInventoryItem(ItemName.cell.getItemStack((Enum)CellType.water), player, true)) {
              world.setBlockToAir(pos);
              StackUtil.consumeOrError(player, hand, 1);
              StackUtil.storeInventoryItem(ItemName.cell.getItemStack((Enum)CellType.water), player, false);
              return EnumActionResult.SUCCESS;
            } 
            if (data.liquid == FluidRegistry.LAVA && StackUtil.storeInventoryItem(ItemName.cell.getItemStack((Enum)CellType.lava), player, true)) {
              world.setBlockToAir(pos);
              StackUtil.consumeOrError(player, hand, 1);
              StackUtil.storeInventoryItem(ItemName.cell.getItemStack((Enum)CellType.lava), player, false);
              return EnumActionResult.SUCCESS;
            } 
          } 
        } 
        return EnumActionResult.PASS;
      }
    };
}
