package ic2.core.item.tool;

import ic2.api.crops.CropCard;
import ic2.api.energy.EnergyNet;
import ic2.api.item.IBoxable;
import ic2.api.item.IDebuggable;
import ic2.api.item.IElectricItemManager;
import ic2.api.item.ISpecialElectricItem;
import ic2.api.reactor.IReactor;
import ic2.api.tile.IEnergyStorage;
import ic2.core.IC2;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.comp.Energy;
import ic2.core.block.comp.Redstone;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.block.generator.tileentity.TileEntityBaseGenerator;
import ic2.core.block.personal.IPersonalBlock;
import ic2.core.crop.TileEntityCrop;
import ic2.core.item.InfiniteElectricItemManager;
import ic2.core.item.ItemIC2;
import ic2.core.network.NetworkManager;
import ic2.core.ref.ItemName;
import ic2.core.util.LogCategory;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class ItemDebug extends ItemIC2 implements ISpecialElectricItem, IBoxable {
  public ItemDebug() {
    super(ItemName.debug_item);
    func_77627_a(false);
    if (!Util.inDev())
      func_77637_a(null); 
  }
  
  public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
    RayTraceResult position;
    TileEntity tileEntity, te;
    String plat;
    int count;
    NBTTagCompound nbtData = StackUtil.getOrCreateNbtData(StackUtil.get(player, hand));
    int modeIdx = nbtData.func_74762_e("mode");
    if (modeIdx < 0 || modeIdx >= Mode.modes.length)
      modeIdx = 0; 
    Mode mode = Mode.modes[modeIdx];
    if (IC2.keyboard.isModeSwitchKeyDown(player)) {
      if (!world.field_72995_K) {
        mode = Mode.modes[(mode.ordinal() + 1) % Mode.modes.length];
        nbtData.func_74768_a("mode", mode.ordinal());
        IC2.platform.messagePlayer(player, "Debug Item Mode: " + mode.getName(), new Object[0]);
        return EnumActionResult.SUCCESS;
      } 
      return EnumActionResult.PASS;
    } 
    TileEntity tileentity = world.func_175625_s(pos);
    if (tileentity instanceof IDebuggable) {
      if (world.field_72995_K)
        return EnumActionResult.PASS; 
      IDebuggable dbg = (IDebuggable)tileentity;
      if (dbg.isDebuggable() && 
        !world.field_72995_K)
        IC2.platform.messagePlayer(player, dbg.getDebugText(), new Object[0]); 
      return world.field_72995_K ? EnumActionResult.PASS : EnumActionResult.SUCCESS;
    } 
    ByteArrayOutputStream consoleBuffer = new ByteArrayOutputStream();
    PrintStream console = new PrintStream(consoleBuffer);
    ByteArrayOutputStream chatBuffer = new ByteArrayOutputStream();
    PrintStream chat = new PrintStream(chatBuffer);
    switch (mode) {
      case InterfacesFields:
      case InterfacesFieldsRetrace:
        if (mode == Mode.InterfacesFields) {
          position = new RayTraceResult(RayTraceResult.Type.BLOCK, new Vec3d(hitX, hitY, hitX), side, pos);
        } else {
          position = func_77621_a(world, player, true);
          if (position == null)
            return EnumActionResult.PASS; 
          RayTraceResult entityPosition = Util.traceEntities(player, position.field_72307_f, true);
          if (entityPosition != null)
            position = entityPosition; 
        } 
        if (FMLCommonHandler.instance().getSide().isClient()) {
          if (!world.field_72995_K) {
            plat = "sp server";
          } else if (player.func_184102_h() == null) {
            plat = "mp client";
          } else {
            plat = "sp client";
          } 
        } else {
          plat = "mp server";
        } 
        if (position.field_72313_a == RayTraceResult.Type.BLOCK) {
          pos = position.func_178782_a();
          IBlockState state = world.func_180495_p(pos);
          Block block = state.func_177230_c();
          TileEntity tileEntity1 = world.func_175625_s(pos);
          String message = String.format("[%s] block state: %s%nname: %s%ncls: %s%nte: %s", new Object[] { plat, state
                .func_185899_b((IBlockAccess)world, pos), block.func_149739_a(), block.getClass().getName(), tileEntity1 });
          chat.println(message);
          console.println(message);
          if (tileEntity1 != null) {
            message = "[" + plat + "] interfaces:";
            Class<?> c = tileEntity1.getClass();
            do {
              for (Class<?> i : c.getInterfaces())
                message = message + " " + i.getName(); 
              c = c.getSuperclass();
            } while (c != null);
            chat.println(message);
            console.println(message);
          } 
          console.println("block fields:");
          dumpObjectFields(console, block);
          if (tileEntity1 != null) {
            console.println();
            console.println("tile entity fields:");
            dumpObjectFields(console, tileEntity1);
          } 
          break;
        } 
        if (position.field_72313_a == RayTraceResult.Type.ENTITY) {
          String message = "[" + plat + "] entity: " + position.field_72308_g;
          chat.println(message);
          console.println(message);
          if (position.field_72308_g instanceof EntityItem) {
            ItemStack entStack = ((EntityItem)position.field_72308_g).func_92059_d();
            String name = Util.getName(entStack.func_77973_b()).toString();
            message = "[" + plat + "] item id: " + name + " meta: " + entStack.func_77952_i() + " size: " + StackUtil.getSize(entStack) + " name: " + entStack.func_77977_a();
            chat.println(message);
            console.println(message);
            console.println("NBT: " + entStack.func_77978_p());
          } 
          break;
        } 
        return EnumActionResult.PASS;
      case TileData:
        if (world.field_72995_K)
          return EnumActionResult.PASS; 
        tileEntity = world.func_175625_s(pos);
        if (tileEntity instanceof TileEntityBlock) {
          TileEntityBlock tileEntityBlock = (TileEntityBlock)tileEntity;
          chat.println("Block: Active=" + tileEntityBlock.getActive() + " Facing=" + tileEntityBlock.getFacing());
          for (TileEntityComponent comp : tileEntityBlock.getComponents()) {
            if (comp instanceof Energy) {
              Energy energy = (Energy)comp;
              chat.printf("Energy: %.2f / %.2f%n", new Object[] { Double.valueOf(energy.getEnergy()), Double.valueOf(energy.getCapacity()) });
              continue;
            } 
            if (comp instanceof Redstone) {
              Redstone redstone = (Redstone)comp;
              chat.printf("Redstone: %d%n", new Object[] { Integer.valueOf(redstone.getRedstoneInput()) });
            } 
          } 
        } 
        if (tileEntity instanceof TileEntityBaseGenerator) {
          TileEntityBaseGenerator tileEntityBaseGenerator = (TileEntityBaseGenerator)tileEntity;
          chat.println("BaseGen: Fuel=" + tileEntityBaseGenerator.fuel);
        } 
        if (tileEntity instanceof IEnergyStorage) {
          IEnergyStorage iEnergyStorage = (IEnergyStorage)tileEntity;
          chat.println("EnergyStorage: Stored=" + iEnergyStorage.getStored());
        } 
        if (tileEntity instanceof IReactor) {
          IReactor iReactor = (IReactor)tileEntity;
          chat.println("Reactor: Heat=" + iReactor.getHeat() + " MaxHeat=" + iReactor.getMaxHeat() + " HEM=" + iReactor.getHeatEffectModifier() + " Output=" + iReactor.getReactorEnergyOutput());
        } 
        if (tileEntity instanceof IPersonalBlock) {
          IPersonalBlock iPersonalBlock = (IPersonalBlock)tileEntity;
          chat.println("PersonalBlock: CanAccess=" + iPersonalBlock.permitsAccess(player.func_146103_bH()));
        } 
        if (tileEntity instanceof TileEntityCrop) {
          TileEntityCrop tileEntityCrop = (TileEntityCrop)tileEntity;
          CropCard crop = tileEntityCrop.getCrop();
          String id = (crop != null) ? (crop.getOwner() + ":" + crop.getId()) : "none";
          chat.printf("Crop: Crop=%s Size=%d Growth=%d Gain=%d Resistance=%d Nutrients=%d Water=%d GrowthPoints=%d%n Cross=%b", new Object[] { id, 
                
                Integer.valueOf(tileEntityCrop.getCurrentSize()), 
                Integer.valueOf(tileEntityCrop.getStatGrowth()), Integer.valueOf(tileEntityCrop.getStatGain()), Integer.valueOf(tileEntityCrop.getStatResistance()), 
                Integer.valueOf(tileEntityCrop.getStorageNutrients()), Integer.valueOf(tileEntityCrop.getStorageWater()), 
                Integer.valueOf(tileEntityCrop.getGrowthPoints()), 
                Boolean.valueOf(tileEntityCrop.isCrossingBase()) });
        } 
        break;
      case EnergyNet:
        if (world.field_72995_K)
          return EnumActionResult.PASS; 
        if (!EnergyNet.instance.dumpDebugInfo(world, pos, console, chat))
          return EnumActionResult.PASS; 
        break;
      case Accelerate:
      case AccelerateX100:
        if (world.field_72995_K)
          return EnumActionResult.PASS; 
        te = world.func_175625_s(pos);
        count = (mode == Mode.Accelerate) ? 1000 : 100000;
        if (te == null) {
          IBlockState state = world.func_180495_p(pos);
          if (state.func_177230_c().func_149653_t()) {
            chat.println("Running" + count + " ticks on " + state.func_177230_c() + "(" + pos + ").");
            int i;
            for (i = 0; i < count && world.func_180495_p(pos) == state; i++)
              state.func_177230_c().func_180645_a(world, pos, state, field_77697_d); 
            if (i != count)
              chat.println("Ran " + i + " ticks before a state change."); 
          } 
          break;
        } 
        if (te instanceof ITickable) {
          ITickable tickable = (ITickable)te;
          chat.println("Running " + count + " ticks on " + te + ".");
          int changes = 0;
          int interruptCount = -1;
          for (int i = 0; i < count; i++) {
            if (te.func_145837_r()) {
              changes++;
              te = world.func_175625_s(pos);
              if (!(te instanceof ITickable) || te.func_145837_r()) {
                interruptCount = i;
                break;
              } 
              tickable = (ITickable)te;
            } 
            tickable.func_73660_a();
          } 
          if (changes > 0) {
            if (interruptCount != -1) {
              chat.println("The tile entity changed " + changes + " time(s), interrupted after " + interruptCount + " updates.");
              break;
            } 
            chat.println("The tile entity changed " + changes + " time(s).");
          } 
        } 
        break;
    } 
    console.flush();
    chat.flush();
    if (world.field_72995_K) {
      try {
        consoleBuffer.writeTo(new FileOutputStream(FileDescriptor.out));
      } catch (IOException e) {
        IC2.log.warn(LogCategory.Item, e, "Stdout write failed.");
      } 
      for (String line : chatBuffer.toString().split("[\\r\\n]+"))
        IC2.platform.messagePlayer(player, line, new Object[0]); 
    } else if (player instanceof EntityPlayerMP) {
      try {
        ((NetworkManager)IC2.network.get(true)).sendConsole((EntityPlayerMP)player, consoleBuffer.toString("UTF-8"));
        ((NetworkManager)IC2.network.get(true)).sendChat((EntityPlayerMP)player, chatBuffer.toString("UTF-8"));
      } catch (UnsupportedEncodingException e) {
        IC2.log.warn(LogCategory.Item, e, "String encoding failed.");
      } 
    } 
    return world.field_72995_K ? EnumActionResult.PASS : EnumActionResult.SUCCESS;
  }
  
  private static void dumpObjectFields(PrintStream ps, Object o) {
    Class<?> fieldDeclaringClass = o.getClass();
    do {
      Field[] fields = fieldDeclaringClass.getDeclaredFields();
      for (Field field : fields) {
        if ((field.getModifiers() & 0x8) == 0 || (
          fieldDeclaringClass != Block.class && fieldDeclaringClass != TileEntity.class)) {
          Object<Object> value;
          boolean accessible = field.isAccessible();
          field.setAccessible(true);
          try {
            value = (Object<Object>)field.get(o);
          } catch (IllegalAccessException e) {
            value = (Object<Object>)"<can't access>";
          } 
          ps.println(field.getName() + " class: " + fieldDeclaringClass.getName() + " type: " + field.getType());
          ps.printf("  identity hash: %x hash: %x modifiers: %x%n", new Object[] { Integer.valueOf(System.identityHashCode(value)), Integer.valueOf((value == null) ? 0 : value.hashCode()), Integer.valueOf(field.getModifiers()) });
          if (value != null && field.getType().isArray()) {
            List<Object> array = new ArrayList();
            for (int i = 0; i < Array.getLength(value); i++)
              array.add(Array.get(value, i)); 
            value = (Object<Object>)array;
          } 
          if (value instanceof Iterable) {
            ps.println("  values (" + ((value instanceof Collection) ? (String)Integer.valueOf(((Collection)value).size()) : "?") + "):");
            int i = 0;
            for (Object o2 : value) {
              ps.print("    [" + i++ + "] ");
              dumpValueString(o2, field, "      ", ps);
            } 
          } else if (value instanceof Map) {
            ps.println("  values (" + ((Map)value).size() + "):");
            for (Map.Entry<?, ?> entry : (Iterable<Map.Entry<?, ?>>)((Map)value).entrySet()) {
              ps.print("    " + entry.getKey() + ": ");
              dumpValueString(entry.getValue(), field, "      ", ps);
            } 
          } else {
            ps.print("  value: ");
            dumpValueString(value, field, "    ", ps);
          } 
          field.setAccessible(accessible);
        } 
      } 
      fieldDeclaringClass = fieldDeclaringClass.getSuperclass();
    } while (fieldDeclaringClass != null);
  }
  
  private static void dumpValueString(Object o, Field parentField, String prefix, PrintStream out) {
    String ret;
    if (o == null) {
      out.println("<null>");
      return;
    } 
    if (o.getClass().isArray()) {
      ret = "";
      for (int i = 0; i < Array.getLength(o); i++) {
        String valStr;
        Object val = Array.get(o, i);
        if (val == null) {
          valStr = "<null>";
        } else {
          valStr = val.toString();
          if (valStr.length() > 32)
            valStr = valStr.substring(0, 20) + "... (" + (valStr.length() - 20) + " more)"; 
        } 
        ret = ret + " [" + i + "] " + valStr;
      } 
    } else {
      ret = o.toString();
    } 
    if (ret.length() > 100)
      ret = ret.substring(0, 90) + "... (" + (ret.length() - 90) + " more)"; 
    out.println(ret);
    if (o instanceof net.minecraftforge.fluids.FluidTank && IC2.platform.isSimulating())
      System.out.println(); 
    if (Modifier.isStatic(parentField.getModifiers()) || parentField
      .isSynthetic() || o
      .getClass().isArray() || o
      .getClass().isEnum() || o
      .getClass().isPrimitive() || o instanceof Iterable || o instanceof Class || o instanceof String)
      return; 
    if (o instanceof World) {
      out.println(prefix + " dim: " + ((World)o).field_73011_w.getDimension());
    } else if (!(o instanceof net.minecraft.block.state.BlockStateContainer) && !(o instanceof Block) && !(o instanceof TileEntity) && !(o instanceof net.minecraft.item.Item) && !(o instanceof ItemStack) && !(o instanceof net.minecraft.util.math.Vec3i) && !(o instanceof Vec3d) && !(o instanceof net.minecraft.nbt.NBTBase) && 
      !o.getClass().getName().startsWith("java.")) {
      Class<?> fieldDeclaringClass = o.getClass();
      while (fieldDeclaringClass != null && fieldDeclaringClass != Object.class) {
        for (Field field : fieldDeclaringClass.getDeclaredFields()) {
          if (!field.isSynthetic() && !Modifier.isStatic(field.getModifiers())) {
            Object val;
            String valStr;
            try {
              field.setAccessible(true);
              val = field.get(o);
            } catch (Exception e) {
              val = "<can't access>";
            } 
            if (val == o) {
              valStr = "<parent>";
            } else {
              valStr = toStringLimited(val, 100);
            } 
            out.println(prefix + field.getName() + ": " + valStr);
          } 
        } 
        fieldDeclaringClass = fieldDeclaringClass.getSuperclass();
      } 
    } 
  }
  
  private static String toStringLimited(Object o, int limit) {
    if (o == null)
      return "<null>"; 
    int extra = 12;
    limit = Math.max(limit, 12);
    String ret = o.toString();
    if (ret.length() > limit) {
      int newLimit = limit - 12;
      return ret.substring(0, newLimit) + "... (" + (ret.length() - newLimit) + " more)";
    } 
    return ret;
  }
  
  public IElectricItemManager getManager(ItemStack stack) {
    if (manager == null)
      manager = (IElectricItemManager)new InfiniteElectricItemManager(); 
    return manager;
  }
  
  public boolean canBeStoredInToolbox(ItemStack itemstack) {
    return true;
  }
  
  private enum Mode {
    InterfacesFields("Interfaces and Fields"),
    InterfacesFieldsRetrace("Interfaces and Fields (liquid/entity)"),
    TileData("Tile Data"),
    EnergyNet("Energy Net"),
    Accelerate("Accelerate"),
    AccelerateX100("Accelerate x100");
    
    static final Mode[] modes = values();
    
    private final String name;
    
    Mode(String name) {
      this.name = name;
    }
    
    String getName() {
      return this.name;
    }
    
    static {
    
    }
  }
  
  private static IElectricItemManager manager = null;
}
