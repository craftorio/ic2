package ic2.core.block.generator.tileentity;

import ic2.core.block.comp.Fluids;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlotConsumableLiquidByTank;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.init.MainConfig;
import ic2.core.network.GuiSynced;
import ic2.core.util.ConfigUtil;
import net.minecraftforge.fluids.FluidEvent;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

public class TileEntityGeoGenerator extends TileEntityBaseGenerator
{
	private static final int fluidPerTick = 2;
	public final InvSlotConsumableLiquid fluidSlot;
	public final InvSlotOutput outputSlot;
	@GuiSynced
	protected final FluidTank fluidTank;
	protected final Fluids fluids = this.addComponent(new Fluids(this));

	public TileEntityGeoGenerator()
	{
		super(20.0, 1, 2400);
		this.fluidTank = this.fluids.addTankInsert("fluid", 8000, Fluids.fluidPredicate(FluidRegistry.LAVA));
		this.production = Math.round(20.0F * ConfigUtil.getFloat(MainConfig.get(), "balance/energy/generator/geothermal"));
		this.fluidSlot = new InvSlotConsumableLiquidByTank(
			this, "fluidSlot", InvSlot.Access.I, 1, InvSlot.InvSide.ANY, InvSlotConsumableLiquid.OpType.Drain, this.fluidTank
		);
		this.outputSlot = new InvSlotOutput(this, "output", 1);
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		if (this.fluidSlot.processIntoTank(this.fluidTank, this.outputSlot))
		{
			this.markDirty();
		}
	}

	@Override
	public boolean gainFuel()
	{
		boolean dirty = false;
		FluidStack ret = this.fluidTank.drainInternal(2, false);
		if (ret != null && ret.amount >= 2)
		{
			this.fluidTank.drainInternal(2, true);
			this.fuel++;
			dirty = true;
		}

		return dirty;
	}

	@Override
	public String getOperationSoundFile()
	{
		return "Generators/GeothermalLoop.ogg";
	}

	@Override
	protected void onBlockBreak()
	{
		// $VF: Couldn't be decompiled
		// Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
		// java.lang.RuntimeException: Constructor net/minecraftforge/fluids/FluidEvent$FluidSpilledEvent.<init>(Lnet/minecraftforge/fluids/FluidStack;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V not found
		//   at org.jetbrains.java.decompiler.modules.decompiler.exps.ExprUtil.getSyntheticParametersMask(ExprUtil.java:49)
		//   at org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent.appendParamList(InvocationExprent.java:982)
		//   at org.jetbrains.java.decompiler.modules.decompiler.exps.NewExprent.toJava(NewExprent.java:462)
		//   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.getCastedExprent(ExprProcessor.java:1054)
		//   at org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent.appendParamList(InvocationExprent.java:1151)
		//   at org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent.toJava(InvocationExprent.java:921)
		//   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.listToJava(ExprProcessor.java:925)
		//   at org.jetbrains.java.decompiler.modules.decompiler.stats.BasicBlockStatement.toJava(BasicBlockStatement.java:87)
		//   at org.jetbrains.java.decompiler.modules.decompiler.stats.RootStatement.toJava(RootStatement.java:36)
		//   at org.jetbrains.java.decompiler.main.ClassWriter.writeMethod(ClassWriter.java:1351)
		//
		// Bytecode:
		// 00: aload 0
		// 01: invokespecial ic2/core/block/generator/tileentity/TileEntityBaseGenerator.onBlockBreak ()V
		// 04: new net/minecraftforge/fluids/FluidEvent$FluidSpilledEvent
		// 07: dup
		// 08: new net/minecraftforge/fluids/FluidStack
		// 0b: dup
		// 0c: getstatic net/minecraftforge/fluids/FluidRegistry.LAVA Lnet/minecraftforge/fluids/Fluid;
		// 0f: sipush 1000
		// 12: invokespecial net/minecraftforge/fluids/FluidStack.<init> (Lnet/minecraftforge/fluids/Fluid;I)V
		// 15: aload 0
		// 16: invokevirtual ic2/core/block/generator/tileentity/TileEntityGeoGenerator.getWorld ()Lnet/minecraft/world/World;
		// 19: aload 0
		// 1a: getfield ic2/core/block/generator/tileentity/TileEntityGeoGenerator.pos Lnet/minecraft/util/math/BlockPos;
		// 1d: invokespecial net/minecraftforge/fluids/FluidEvent$FluidSpilledEvent.<init> (Lnet/minecraftforge/fluids/FluidStack;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V
		// 20: invokestatic net/minecraftforge/fluids/FluidEvent.fireEvent (Lnet/minecraftforge/fluids/FluidEvent;)V
		// 23: return
	}
}
