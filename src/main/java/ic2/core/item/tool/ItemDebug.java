// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tool;

import ic2.core.item.InfiniteElectricItemManager;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.math.Vec3i;
import net.minecraft.item.Item;
import net.minecraft.block.state.BlockStateContainer;
import java.lang.reflect.Modifier;
import net.minecraftforge.fluids.FluidTank;
import java.util.List;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Collection;
import java.lang.reflect.Array;
import java.util.ArrayList;
import ic2.api.crops.CropCard;
import java.util.Iterator;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.nbt.NBTTagCompound;
import java.io.UnsupportedEncodingException;
import ic2.core.network.NetworkManager;
import net.minecraft.entity.player.EntityPlayerMP;
import java.io.IOException;
import ic2.core.util.LogCategory;
import java.io.FileOutputStream;
import java.io.FileDescriptor;
import net.minecraft.util.ITickable;
import ic2.api.energy.EnergyNet;
import ic2.core.crop.TileEntityCrop;
import ic2.core.block.personal.IPersonalBlock;
import ic2.api.reactor.IReactor;
import ic2.api.tile.IEnergyStorage;
import ic2.core.block.generator.tileentity.TileEntityBaseGenerator;
import ic2.core.block.comp.Redstone;
import ic2.core.block.comp.Energy;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.block.TileEntityBlock;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.RayTraceResult;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;
import ic2.api.item.IDebuggable;
import ic2.core.IC2;
import ic2.core.util.StackUtil;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.creativetab.CreativeTabs;
import ic2.core.util.Util;
import ic2.core.ref.ItemName;
import ic2.api.item.IElectricItemManager;
import ic2.api.item.IBoxable;
import ic2.api.item.ISpecialElectricItem;
import ic2.core.item.ItemIC2;

public class ItemDebug extends ItemIC2 implements ISpecialElectricItem, IBoxable
{
    private static IElectricItemManager manager;
    
    public ItemDebug() {
        super(ItemName.debug_item);
        this.setHasSubtypes(false);
        if (!Util.inDev()) {
            this.setCreativeTab((CreativeTabs)null);
        }
    }
    
    public EnumActionResult onItemUseFirst(final EntityPlayer player, final World world, BlockPos pos, final EnumFacing side, final float hitX, final float hitY, final float hitZ, final EnumHand hand) {
        final NBTTagCompound nbtData = StackUtil.getOrCreateNbtData(StackUtil.get(player, hand));
        int modeIdx = nbtData.getInteger("mode");
        if (modeIdx < 0 || modeIdx >= Mode.modes.length) {
            modeIdx = 0;
        }
        Mode mode = Mode.modes[modeIdx];
        if (IC2.keyboard.isModeSwitchKeyDown(player)) {
            if (!world.isRemote) {
                mode = Mode.modes[(mode.ordinal() + 1) % Mode.modes.length];
                nbtData.setInteger("mode", mode.ordinal());
                IC2.platform.messagePlayer(player, "Debug Item Mode: " + mode.getName(), new Object[0]);
                return EnumActionResult.SUCCESS;
            }
            return EnumActionResult.PASS;
        }
        else {
            final TileEntity tileentity = world.getTileEntity(pos);
            if (!(tileentity instanceof IDebuggable)) {
                final ByteArrayOutputStream consoleBuffer = new ByteArrayOutputStream();
                final PrintStream console = new PrintStream(consoleBuffer);
                final ByteArrayOutputStream chatBuffer = new ByteArrayOutputStream();
                final PrintStream chat = new PrintStream(chatBuffer);
                switch (mode) {
                    case InterfacesFields:
                    case InterfacesFieldsRetrace: {
                        RayTraceResult position;
                        if (mode == Mode.InterfacesFields) {
                            position = new RayTraceResult(RayTraceResult.Type.BLOCK, new Vec3d((double)hitX, (double)hitY, (double)hitX), side, pos);
                        }
                        else {
                            position = this.rayTrace(world, player, true);
                            if (position == null) {
                                return EnumActionResult.PASS;
                            }
                            final RayTraceResult entityPosition = Util.traceEntities(player, position.hitVec, true);
                            if (entityPosition != null) {
                                position = entityPosition;
                            }
                        }
                        String plat;
                        if (FMLCommonHandler.instance().getSide().isClient()) {
                            if (!world.isRemote) {
                                plat = "sp server";
                            }
                            else if (player.getServer() == null) {
                                plat = "mp client";
                            }
                            else {
                                plat = "sp client";
                            }
                        }
                        else {
                            plat = "mp server";
                        }
                        if (position.typeOfHit == RayTraceResult.Type.BLOCK) {
                            pos = position.getBlockPos();
                            final IBlockState state = world.getBlockState(pos);
                            final Block block = state.getBlock();
                            final TileEntity te = world.getTileEntity(pos);
                            String message = String.format("[%s] block state: %s%nname: %s%ncls: %s%nte: %s", plat, state.getActualState((IBlockAccess)world, pos), block.getUnlocalizedName(), block.getClass().getName(), te);
                            chat.println(message);
                            console.println(message);
                            if (te != null) {
                                message = "[" + plat + "] interfaces:";
                                Class<?> c = te.getClass();
                                do {
                                    for (final Class<?> i : c.getInterfaces()) {
                                        message = message + " " + i.getName();
                                    }
                                    c = c.getSuperclass();
                                } while (c != null);
                                chat.println(message);
                                console.println(message);
                            }
                            console.println("block fields:");
                            dumpObjectFields(console, block);
                            if (te != null) {
                                console.println();
                                console.println("tile entity fields:");
                                dumpObjectFields(console, te);
                            }
                            break;
                        }
                        if (position.typeOfHit == RayTraceResult.Type.ENTITY) {
                            String message2 = "[" + plat + "] entity: " + position.entityHit;
                            chat.println(message2);
                            console.println(message2);
                            if (position.entityHit instanceof EntityItem) {
                                final ItemStack entStack = ((EntityItem)position.entityHit).getItem();
                                final String name = Util.getName(entStack.getItem()).toString();
                                message2 = "[" + plat + "] item id: " + name + " meta: " + entStack.getItemDamage() + " size: " + StackUtil.getSize(entStack) + " name: " + entStack.getUnlocalizedName();
                                chat.println(message2);
                                console.println(message2);
                                console.println("NBT: " + entStack.getTagCompound());
                            }
                            break;
                        }
                        return EnumActionResult.PASS;
                    }
                    case TileData: {
                        if (world.isRemote) {
                            return EnumActionResult.PASS;
                        }
                        final TileEntity tileEntity = world.getTileEntity(pos);
                        if (tileEntity instanceof TileEntityBlock) {
                            final TileEntityBlock te2 = (TileEntityBlock)tileEntity;
                            chat.println("Block: Active=" + te2.getActive() + " Facing=" + te2.getFacing());
                            for (final TileEntityComponent comp : te2.getComponents()) {
                                if (comp instanceof Energy) {
                                    final Energy energy = (Energy)comp;
                                    chat.printf("Energy: %.2f / %.2f%n", energy.getEnergy(), energy.getCapacity());
                                }
                                else {
                                    if (!(comp instanceof Redstone)) {
                                        continue;
                                    }
                                    final Redstone redstone = (Redstone)comp;
                                    chat.printf("Redstone: %d%n", redstone.getRedstoneInput());
                                }
                            }
                        }
                        if (tileEntity instanceof TileEntityBaseGenerator) {
                            final TileEntityBaseGenerator te3 = (TileEntityBaseGenerator)tileEntity;
                            chat.println("BaseGen: Fuel=" + te3.fuel);
                        }
                        if (tileEntity instanceof IEnergyStorage) {
                            final IEnergyStorage te4 = (IEnergyStorage)tileEntity;
                            chat.println("EnergyStorage: Stored=" + te4.getStored());
                        }
                        if (tileEntity instanceof IReactor) {
                            final IReactor te5 = (IReactor)tileEntity;
                            chat.println("Reactor: Heat=" + te5.getHeat() + " MaxHeat=" + te5.getMaxHeat() + " HEM=" + te5.getHeatEffectModifier() + " Output=" + te5.getReactorEnergyOutput());
                        }
                        if (tileEntity instanceof IPersonalBlock) {
                            final IPersonalBlock te6 = (IPersonalBlock)tileEntity;
                            chat.println("PersonalBlock: CanAccess=" + te6.permitsAccess(player.getGameProfile()));
                        }
                        if (tileEntity instanceof TileEntityCrop) {
                            final TileEntityCrop te7 = (TileEntityCrop)tileEntity;
                            final CropCard crop = te7.getCrop();
                            final String id = (crop != null) ? (crop.getOwner() + ":" + crop.getId()) : "none";
                            chat.printf("Crop: Crop=%s Size=%d Growth=%d Gain=%d Resistance=%d Nutrients=%d Water=%d GrowthPoints=%d%n Cross=%b", id, te7.getCurrentSize(), te7.getStatGrowth(), te7.getStatGain(), te7.getStatResistance(), te7.getStorageNutrients(), te7.getStorageWater(), te7.getGrowthPoints(), te7.isCrossingBase());
                            break;
                        }
                        break;
                    }
                    case EnergyNet: {
                        if (world.isRemote) {
                            return EnumActionResult.PASS;
                        }
                        if (!EnergyNet.instance.dumpDebugInfo(world, pos, console, chat)) {
                            return EnumActionResult.PASS;
                        }
                        break;
                    }
                    case Accelerate:
                    case AccelerateX100: {
                        if (world.isRemote) {
                            return EnumActionResult.PASS;
                        }
                        TileEntity te8 = world.getTileEntity(pos);
                        final int count = (mode == Mode.Accelerate) ? 1000 : 100000;
                        if (te8 == null) {
                            final IBlockState state = world.getBlockState(pos);
                            if (state.getBlock().getTickRandomly()) {
                                chat.println("Running" + count + " ticks on " + state.getBlock() + "(" + pos + ").");
                                int j;
                                for (j = 0; j < count && world.getBlockState(pos) == state; ++j) {
                                    state.getBlock().randomTick(world, pos, state, ItemDebug.itemRand);
                                }
                                if (j != count) {
                                    chat.println("Ran " + j + " ticks before a state change.");
                                }
                            }
                            break;
                        }
                        if (te8 instanceof ITickable) {
                            ITickable tickable = (ITickable)te8;
                            chat.println("Running " + count + " ticks on " + te8 + ".");
                            int changes = 0;
                            int interruptCount = -1;
                            for (int k = 0; k < count; ++k) {
                                if (te8.isInvalid()) {
                                    ++changes;
                                    te8 = world.getTileEntity(pos);
                                    if (!(te8 instanceof ITickable) || te8.isInvalid()) {
                                        interruptCount = k;
                                        break;
                                    }
                                    tickable = (ITickable)te8;
                                }
                                tickable.update();
                            }
                            if (changes > 0) {
                                if (interruptCount != -1) {
                                    chat.println("The tile entity changed " + changes + " time(s), interrupted after " + interruptCount + " updates.");
                                }
                                else {
                                    chat.println("The tile entity changed " + changes + " time(s).");
                                }
                            }
                            break;
                        }
                        break;
                    }
                }
                console.flush();
                chat.flush();
                if (world.isRemote) {
                    try {
                        consoleBuffer.writeTo(new FileOutputStream(FileDescriptor.out));
                    }
                    catch (final IOException e) {
                        IC2.log.warn(LogCategory.Item, e, "Stdout write failed.");
                    }
                    for (final String line : chatBuffer.toString().split("[\\r\\n]+")) {
                        IC2.platform.messagePlayer(player, line, new Object[0]);
                    }
                }
                else if (player instanceof EntityPlayerMP) {
                    try {
                        IC2.network.get(true).sendConsole((EntityPlayerMP)player, consoleBuffer.toString("UTF-8"));
                        IC2.network.get(true).sendChat((EntityPlayerMP)player, chatBuffer.toString("UTF-8"));
                    }
                    catch (final UnsupportedEncodingException e2) {
                        IC2.log.warn(LogCategory.Item, e2, "String encoding failed.");
                    }
                }
                return world.isRemote ? EnumActionResult.PASS : EnumActionResult.SUCCESS;
            }
            if (world.isRemote) {
                return EnumActionResult.PASS;
            }
            final IDebuggable dbg = (IDebuggable)tileentity;
            if (dbg.isDebuggable() && !world.isRemote) {
                IC2.platform.messagePlayer(player, dbg.getDebugText(), new Object[0]);
            }
            return world.isRemote ? EnumActionResult.PASS : EnumActionResult.SUCCESS;
        }
    }
    
    private static void dumpObjectFields(final PrintStream ps, final Object o) {
        Class<?> fieldDeclaringClass = o.getClass();
        do {
            final Field[] declaredFields;
            final Field[] fields = declaredFields = fieldDeclaringClass.getDeclaredFields();
            for (final Field field : declaredFields) {
                Label_0577: {
                    if ((field.getModifiers() & 0x8) != 0x0) {
                        if (fieldDeclaringClass == Block.class) {
                            break Label_0577;
                        }
                        if (fieldDeclaringClass == TileEntity.class) {
                            break Label_0577;
                        }
                    }
                    final boolean accessible = field.isAccessible();
                    field.setAccessible(true);
                    Object value;
                    try {
                        value = field.get(o);
                    }
                    catch (final IllegalAccessException e) {
                        value = "<can't access>";
                    }
                    ps.println(field.getName() + " class: " + fieldDeclaringClass.getName() + " type: " + field.getType());
                    ps.printf("  identity hash: %x hash: %x modifiers: %x%n", System.identityHashCode(value), (value == null) ? 0 : value.hashCode(), field.getModifiers());
                    if (value != null && field.getType().isArray()) {
                        final List<Object> array = new ArrayList<Object>();
                        for (int i = 0; i < Array.getLength(value); ++i) {
                            array.add(Array.get(value, i));
                        }
                        value = array;
                    }
                    if (value instanceof Iterable) {
                        ps.println("  values (" + ((value instanceof Collection) ? Integer.valueOf(((Collection)value).size()) : "?") + "):");
                        int j = 0;
                        for (final Object o2 : (Iterable)value) {
                            ps.print("    [" + j++ + "] ");
                            dumpValueString(o2, field, "      ", ps);
                        }
                    }
                    else if (value instanceof Map) {
                        ps.println("  values (" + ((Map)value).size() + "):");
                        for (final Map.Entry<?, ?> entry : ((Map)value).entrySet()) {
                            ps.print("    " + entry.getKey() + ": ");
                            dumpValueString(entry.getValue(), field, "      ", ps);
                        }
                    }
                    else {
                        ps.print("  value: ");
                        dumpValueString(value, field, "    ", ps);
                    }
                    field.setAccessible(accessible);
                }
            }
            fieldDeclaringClass = fieldDeclaringClass.getSuperclass();
        } while (fieldDeclaringClass != null);
    }
    
    private static void dumpValueString(final Object o, final Field parentField, final String prefix, final PrintStream out) {
        if (o == null) {
            out.println("<null>");
            return;
        }
        String ret;
        if (o.getClass().isArray()) {
            ret = "";
            for (int i = 0; i < Array.getLength(o); ++i) {
                final Object val = Array.get(o, i);
                String valStr;
                if (val == null) {
                    valStr = "<null>";
                }
                else {
                    valStr = val.toString();
                    if (valStr.length() > 32) {
                        valStr = valStr.substring(0, 20) + "... (" + (valStr.length() - 20) + " more)";
                    }
                }
                ret = ret + " [" + i + "] " + valStr;
            }
        }
        else {
            ret = o.toString();
        }
        if (ret.length() > 100) {
            ret = ret.substring(0, 90) + "... (" + (ret.length() - 90) + " more)";
        }
        out.println(ret);
        if (o instanceof FluidTank && IC2.platform.isSimulating()) {
            System.out.println();
        }
        if (Modifier.isStatic(parentField.getModifiers()) || parentField.isSynthetic() || o.getClass().isArray() || o.getClass().isEnum() || o.getClass().isPrimitive() || o instanceof Iterable || o instanceof Class || o instanceof String) {
            return;
        }
        if (o instanceof World) {
            out.println(prefix + " dim: " + ((World)o).provider.getDimension());
        }
        else if (!(o instanceof BlockStateContainer) && !(o instanceof Block) && !(o instanceof TileEntity) && !(o instanceof Item) && !(o instanceof ItemStack) && !(o instanceof Vec3i) && !(o instanceof Vec3d) && !(o instanceof NBTBase) && !o.getClass().getName().startsWith("java.")) {
            for (Class<?> fieldDeclaringClass = o.getClass(); fieldDeclaringClass != null && fieldDeclaringClass != Object.class; fieldDeclaringClass = fieldDeclaringClass.getSuperclass()) {
                for (final Field field : fieldDeclaringClass.getDeclaredFields()) {
                    if (!field.isSynthetic()) {
                        if (!Modifier.isStatic(field.getModifiers())) {
                            Object val2;
                            try {
                                field.setAccessible(true);
                                val2 = field.get(o);
                            }
                            catch (final Exception e) {
                                val2 = "<can't access>";
                            }
                            String valStr2;
                            if (val2 == o) {
                                valStr2 = "<parent>";
                            }
                            else {
                                valStr2 = toStringLimited(val2, 100);
                            }
                            out.println(prefix + field.getName() + ": " + valStr2);
                        }
                    }
                }
            }
        }
    }
    
    private static String toStringLimited(final Object o, int limit) {
        if (o == null) {
            return "<null>";
        }
        final int extra = 12;
        limit = Math.max(limit, 12);
        final String ret = o.toString();
        if (ret.length() > limit) {
            final int newLimit = limit - 12;
            return ret.substring(0, newLimit) + "... (" + (ret.length() - newLimit) + " more)";
        }
        return ret;
    }
    
    @Override
    public IElectricItemManager getManager(final ItemStack stack) {
        if (ItemDebug.manager == null) {
            ItemDebug.manager = new InfiniteElectricItemManager();
        }
        return ItemDebug.manager;
    }
    
    @Override
    public boolean canBeStoredInToolbox(final ItemStack itemstack) {
        return true;
    }
    
    static {
        ItemDebug.manager = null;
    }
    
    private enum Mode
    {
        InterfacesFields("Interfaces and Fields"), 
        InterfacesFieldsRetrace("Interfaces and Fields (liquid/entity)"), 
        TileData("Tile Data"), 
        EnergyNet("Energy Net"), 
        Accelerate("Accelerate"), 
        AccelerateX100("Accelerate x100");
        
        static final Mode[] modes;
        private final String name;
        
        private Mode(final String name) {
            this.name = name;
        }
        
        String getName() {
            return this.name;
        }
        
        static {
            modes = values();
        }
    }
}
