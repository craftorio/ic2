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
            world.func_175656_a(position.getBlockPos(), FluidName.construction_foam.getInstance().getBlock().getDefaultState());
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
            .func_71019_a(drop, false) != null && !player.field_71075_bZ.field_75098_d) {
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
        if (state.getBlock() == Blocks.field_150331_J && state.func_177229_b((IProperty)BlockPistonBase.field_176387_N) == side) {
          IBlockState newState = Blocks.field_150320_F.getDefaultState().func_177226_a((IProperty)BlockPistonBase.field_176387_N, (Comparable)side).func_177226_a((IProperty)BlockPistonBase.field_176320_b, state.func_177229_b((IProperty)BlockPistonBase.field_176320_b));
          world.func_180501_a(pos, newState, 3);
          if (!player.field_71075_bZ.field_75098_d)
            StackUtil.consumeOrError(player, hand, 1); 
          return EnumActionResult.SUCCESS;
        } 
        if (side != EnumFacing.UP)
          return EnumActionResult.PASS; 
        pos = pos.up();
        if (!state.getBlock().isAir(state, (IBlockAccess)world, pos) || 
          !BlockName.sheet.getInstance().func_176198_a(world, pos, side))
          return EnumActionResult.PASS; 
        world.func_175656_a(pos, BlockName.sheet.getBlockState((IIdProvider)BlockSheet.SheetType.resin));
        if (!player.field_71075_bZ.field_75098_d)
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
          if (!world.getBlockState(pos).getBlock().func_176200_f((IBlockAccess)world, pos))
            pos = pos.offset(side); 
          if (player.func_175151_a(pos, side, stack) && world.func_190527_a(type, pos, false, side, null)) {
            IBlockState placedState = type.getStateForPlacement(world, pos, side, 0.0F, 0.0F, 0.0F, 0, (EntityLivingBase)player, hand);
            world.func_175656_a(pos, placedState);
            SoundType sound = placedState.getBlock().getSoundType(placedState, world, pos, (Entity)player);
            world.func_184133_a(player, pos, sound.func_185841_e(), SoundCategory.BLOCKS, (sound.func_185843_a() + 1.0F) / 2.0F, sound.func_185847_b() * 0.8F);
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
          if (!player.func_175151_a(pos, position.field_178784_b, player.func_184586_b(hand)))
            return EnumActionResult.FAIL; 
          LiquidUtil.LiquidData data = LiquidUtil.getLiquid(world, pos);
          if (data != null && data.isSource) {
            if (data.liquid == FluidRegistry.WATER && StackUtil.storeInventoryItem(ItemName.cell.getItemStack((Enum)CellType.water), player, true)) {
              world.func_175698_g(pos);
              StackUtil.consumeOrError(player, hand, 1);
              StackUtil.storeInventoryItem(ItemName.cell.getItemStack((Enum)CellType.water), player, false);
              return EnumActionResult.SUCCESS;
            } 
            if (data.liquid == FluidRegistry.LAVA && StackUtil.storeInventoryItem(ItemName.cell.getItemStack((Enum)CellType.lava), player, true)) {
              world.func_175698_g(pos);
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
