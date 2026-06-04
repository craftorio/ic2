package ic2.core.network;

import ic2.api.network.ClientModifiable;
import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.api.network.INetworkDataProvider;
import ic2.api.network.INetworkItemEventListener;
import ic2.api.network.INetworkManager;
import ic2.core.ContainerBase;
import ic2.core.ExplosionIC2;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.WorldData;
import ic2.core.block.ITeBlock;
import ic2.core.block.TileEntityBlock;
import ic2.core.item.IHandHeldInventory;
import ic2.core.util.LogCategory;
import ic2.core.util.ReflectionUtil;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.DeflaterOutputStream;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

public class NetworkManager implements INetworkManager {
  public NetworkManager() {
    if (channel == null)
      channel = NetworkRegistry.INSTANCE.newEventDrivenChannel("ic2"); 
    channel.register(this);
  }
  
  protected boolean isClient() {
    return false;
  }
  
  public void onTickEnd(WorldData worldData) {
    try {
      TeUpdate.send(worldData, this);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } 
  }
  
  public final void sendPlayerItemData(EntityPlayer player, int slot, Object... data) {
    GrowingBuffer buffer = new GrowingBuffer(256);
    try {
      SubPacketType.PlayerItemData.writeTo(buffer);
      buffer.writeByte(slot);
      DataEncoder.encode(buffer, ((ItemStack)player.inventory.field_70462_a.get(slot)).getItem(), false);
      buffer.writeVarInt(data.length);
      for (Object o : data)
        DataEncoder.encode(buffer, o); 
    } catch (IOException e) {
      throw new RuntimeException(e);
    } 
    buffer.flip();
    if (!isClient()) {
      sendPacket(buffer, true, (EntityPlayerMP)player);
    } else {
      sendPacket(buffer);
    } 
  }
  
  public final void updateTileEntityField(TileEntity te, String field) {
    if (!isClient()) {
      getTeUpdateData(te).addGlobalField(field);
    } else if (getClientModifiableField(te.getClass(), field) == null) {
      IC2.log.warn(LogCategory.Network, "Field update for %s failed.", new Object[] { te });
    } else {
      GrowingBuffer buffer = new GrowingBuffer(64);
      try {
        SubPacketType.TileEntityData.writeTo(buffer);
        DataEncoder.encode(buffer, te, false);
        writeFieldData(te, field, buffer);
      } catch (IOException e) {
        throw new RuntimeException(e);
      } 
      buffer.flip();
      sendPacket(buffer);
    } 
  }
  
  private Field getClientModifiableField(Class<?> cls, String fieldName) {
    Field field = ReflectionUtil.getFieldRecursive(cls, fieldName);
    if (field == null) {
      IC2.log.warn(LogCategory.Network, "Can't find field %s in %s.", new Object[] { fieldName, cls.getName() });
      return null;
    } 
    if (field.getAnnotation(ClientModifiable.class) == null) {
      IC2.log.warn(LogCategory.Network, "The field %s in %s is not modifiable.", new Object[] { fieldName, cls.getName() });
      return null;
    } 
    return field;
  }
  
  private static TeUpdateDataServer getTeUpdateData(TileEntity te) {
    assert IC2.platform.isSimulating();
    if (te == null)
      throw new NullPointerException(); 
    WorldData worldData = WorldData.get(te.getWorld());
    TeUpdateDataServer ret = (TeUpdateDataServer)worldData.tesToUpdate.get(te);
    if (ret == null) {
      ret = new TeUpdateDataServer();
      worldData.tesToUpdate.put(te, ret);
    } 
    return ret;
  }
  
  public final void updateTileEntityFieldTo(TileEntity te, String field, EntityPlayerMP player) {
    assert !isClient();
    getTeUpdateData(te).addPlayerField(field, player);
  }
  
  public final void sendComponentUpdate(TileEntityBlock te, String componentName, EntityPlayerMP player, GrowingBuffer data) {
    assert !isClient();
    if (player.getEntityWorld() != te.getWorld())
      throw new IllegalArgumentException("mismatched world (te " + te.getWorld() + ", player " + player.getEntityWorld() + ")"); 
    GrowingBuffer buffer = new GrowingBuffer(64);
    try {
      SubPacketType.TileEntityBlockComponent.writeTo(buffer);
      DataEncoder.encode(buffer, te, false);
      buffer.writeString(componentName);
      buffer.writeVarInt(data.available());
      data.writeTo(buffer);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } 
    buffer.flip();
    sendPacket(buffer, true, player);
  }
  
  public final void initiateTileEntityEvent(TileEntity te, int event, boolean limitRange) {
    assert !isClient();
    if ((te.getWorld()).field_73010_i.isEmpty())
      return; 
    GrowingBuffer buffer = new GrowingBuffer(32);
    try {
      SubPacketType.TileEntityEvent.writeTo(buffer);
      DataEncoder.encode(buffer, te, false);
      buffer.writeInt(event);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } 
    buffer.flip();
    for (EntityPlayerMP target : getPlayersInRange(te.getWorld(), te.getPos(), new ArrayList())) {
      if (limitRange) {
        int dX = (int)(te.getPos().getX() + 0.5D - target.posX);
        int dZ = (int)(te.getPos().getZ() + 0.5D - target.posZ);
        if (dX * dX + dZ * dZ > 400)
          continue; 
      } 
      sendPacket(buffer, false, target);
    } 
  }
  
  public final void initiateItemEvent(EntityPlayer player, ItemStack stack, int event, boolean limitRange) {
    if (StackUtil.isEmpty(stack))
      throw new NullPointerException("invalid stack: " + StackUtil.toStringSafe(stack)); 
    assert !isClient();
    GrowingBuffer buffer = new GrowingBuffer(256);
    try {
      SubPacketType.ItemEvent.writeTo(buffer);
      DataEncoder.encode(buffer, player.func_146103_bH(), false);
      DataEncoder.encode(buffer, stack, false);
      buffer.writeInt(event);
    } catch (Exception e) {
      throw new RuntimeException(e);
    } 
    buffer.flip();
    for (EntityPlayerMP target : getPlayersInRange(player.getEntityWorld(), player.func_180425_c(), new ArrayList())) {
      if (limitRange) {
        int dX = (int)(player.posX - target.posX);
        int dZ = (int)(player.posZ - target.posZ);
        if (dX * dX + dZ * dZ > 400)
          continue; 
      } 
      sendPacket(buffer, false, target);
    } 
  }
  
  public void initiateClientItemEvent(ItemStack stack, int event) {
    assert false;
  }
  
  public void initiateClientTileEntityEvent(TileEntity te, int event) {
    assert false;
  }
  
  public void initiateRpc(int id, Class<? extends IRpcProvider<?>> provider, Object[] args) {
    assert false;
  }
  
  public void requestGUI(IHasGui inventory) {
    assert false;
  }
  
  public final void initiateGuiDisplay(EntityPlayerMP player, IHasGui inventory, int windowId) {
    initiateGuiDisplay(player, inventory, windowId, null);
  }
  
  public final void initiateGuiDisplay(EntityPlayerMP player, IHasGui inventory, int windowId, Integer ID) {
    assert !isClient();
    try {
      GrowingBuffer buffer = new GrowingBuffer(32);
      SubPacketType.GuiDisplay.writeTo(buffer);
      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      boolean isAdmin = server.func_184103_al().func_152596_g(player.func_146103_bH());
      buffer.writeBoolean(isAdmin);
      if (inventory instanceof TileEntity) {
        TileEntity te = (TileEntity)inventory;
        buffer.writeByte(0);
        DataEncoder.encode(buffer, te, false);
      } else if (player.inventory.getCurrentItem() != null && player.inventory.getCurrentItem().getItem() instanceof IHandHeldInventory) {
        buffer.writeByte(1);
        buffer.writeInt(player.inventory.field_70461_c);
        handleSubData(buffer, player.inventory.getCurrentItem(), ID);
      } else if (player.func_184592_cb() != null && player.func_184592_cb().getItem() instanceof IHandHeldInventory) {
        buffer.writeByte(1);
        buffer.writeInt(-1);
        handleSubData(buffer, player.func_184592_cb(), ID);
      } else {
        IC2.platform.displayError("An unknown GUI type was attempted to be displayed.\nThis could happen due to corrupted data from a player or a bug.\n\n(Technical information: " + inventory + ")", new Object[0]);
      } 
      buffer.writeInt(windowId);
      buffer.flip();
      sendPacket(buffer, true, player);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } 
  }
  
  private final void handleSubData(GrowingBuffer buffer, ItemStack stack, Integer ID) {
    boolean subInv = (ID != null && stack.getItem() instanceof ic2.core.item.IHandHeldSubInventory);
    buffer.writeBoolean(subInv);
    if (subInv)
      buffer.writeShort(ID.intValue()); 
  }
  
  public final void sendInitialData(TileEntity te, EntityPlayerMP player) {
    assert !isClient();
    if (te instanceof INetworkDataProvider) {
      TeUpdateDataServer updateData = getTeUpdateData(te);
      for (String field : ((INetworkDataProvider)te).getNetworkedFields())
        updateData.addPlayerField(field, player); 
    } 
  }
  
  public final void sendInitialData(TileEntity te) {
    assert !isClient();
    if (te instanceof INetworkDataProvider) {
      TeUpdateDataServer updateData = getTeUpdateData(te);
      List<String> fields = ((INetworkDataProvider)te).getNetworkedFields();
      for (String field : fields)
        updateData.addGlobalField(field); 
      if (TeUpdate.debug)
        IC2.log.info(LogCategory.Network, "Sending initial TE data for %s (%s).", new Object[] { Util.formatPosition(te), fields }); 
    } 
  }
  
  public final void sendChat(EntityPlayerMP player, String message) {
    assert !isClient();
    GrowingBuffer buffer = new GrowingBuffer(message.length() * 2);
    buffer.writeString(message);
    buffer.flip();
    sendLargePacket(player, 1, buffer);
  }
  
  public final void sendConsole(EntityPlayerMP player, String message) {
    assert !isClient();
    GrowingBuffer buffer = new GrowingBuffer(message.length() * 2);
    buffer.writeString(message);
    buffer.flip();
    sendLargePacket(player, 2, buffer);
  }
  
  public final void sendContainerFields(ContainerBase<?> container, String... fieldNames) {
    for (String fieldName : fieldNames)
      sendContainerField(container, fieldName); 
  }
  
  public final void sendContainerField(ContainerBase<?> container, String fieldName) {
    if (isClient() && 
      getClientModifiableField(container.getClass(), fieldName) == null) {
      IC2.log.warn(LogCategory.Network, "Field update for %s failed.", new Object[] { container });
      return;
    } 
    GrowingBuffer buffer = new GrowingBuffer(256);
    try {
      SubPacketType.ContainerData.writeTo(buffer);
      buffer.writeInt(container.field_75152_c);
      writeFieldData(container, fieldName, buffer);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } 
    buffer.flip();
    if (!isClient()) {
      for (IContainerListener listener : container.getListeners()) {
        if (listener instanceof EntityPlayerMP)
          sendPacket(buffer, false, (EntityPlayerMP)listener); 
      } 
    } else {
      sendPacket(buffer);
    } 
  }
  
  public final void sendContainerEvent(ContainerBase<?> container, String event) {
    GrowingBuffer buffer = new GrowingBuffer(64);
    SubPacketType.ContainerEvent.writeTo(buffer);
    buffer.writeInt(container.field_75152_c);
    buffer.writeString(event);
    buffer.flip();
    if (!isClient()) {
      for (IContainerListener listener : container.getListeners()) {
        if (listener instanceof EntityPlayerMP)
          sendPacket(buffer, false, (EntityPlayerMP)listener); 
      } 
    } else {
      sendPacket(buffer);
    } 
  }
  
  public final void sendHandHeldInvField(ContainerBase<?> container, String fieldName) {
    if (!(container.base instanceof ic2.core.item.tool.HandHeldInventory)) {
      IC2.log.warn(LogCategory.Network, "Invalid container (%s) sent for field update.", new Object[] { container });
      return;
    } 
    if (isClient() && getClientModifiableField(container.base.getClass(), fieldName) == null) {
      IC2.log.warn(LogCategory.Network, "Field update for %s failed.", new Object[] { container });
      return;
    } 
    GrowingBuffer buffer = new GrowingBuffer(256);
    try {
      SubPacketType.HandHeldInvData.writeTo(buffer);
      buffer.writeInt(container.field_75152_c);
      writeFieldData(container.base, fieldName, buffer);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } 
    buffer.flip();
    if (!isClient()) {
      for (IContainerListener listener : container.getListeners()) {
        if (listener instanceof EntityPlayerMP)
          sendPacket(buffer, false, (EntityPlayerMP)listener); 
      } 
    } else {
      sendPacket(buffer);
    } 
  }
  
  public final void initiateTeblockLandEffect(World world, double x, double y, double z, int count, ITeBlock teBlock) {
    initiateTeblockLandEffect(world, null, x, y, z, count, teBlock);
  }
  
  public final void initiateTeblockLandEffect(World world, BlockPos pos, double x, double y, double z, int count, ITeBlock teBlock) {
    assert !isClient();
    GrowingBuffer buffer = new GrowingBuffer(64);
    try {
      SubPacketType.TileEntityBlockLandEffect.writeTo(buffer);
      DataEncoder.encode(buffer, world, false);
      if (pos != null) {
        buffer.writeBoolean(true);
        DataEncoder.encode(buffer, pos, false);
      } else {
        buffer.writeBoolean(false);
      } 
      buffer.writeDouble(x);
      buffer.writeDouble(y);
      buffer.writeDouble(z);
      buffer.writeInt(count);
      buffer.writeString(teBlock.getName());
    } catch (IOException e) {
      throw new RuntimeException(e);
    } 
    buffer.flip();
    for (EntityPlayer player : world.field_73010_i) {
      if (!(player instanceof EntityPlayerMP))
        continue; 
      double distance = player.func_70092_e(x, y, z);
      if (distance <= 1024.0D)
        sendPacket(buffer, false, (EntityPlayerMP)player); 
    } 
  }
  
  public final void initiateTeblockRunEffect(World world, Entity entity, ITeBlock teBlock) {
    initiateTeblockRunEffect(world, null, entity, teBlock);
  }
  
  public final void initiateTeblockRunEffect(World world, BlockPos pos, Entity entity, ITeBlock teBlock) {
    assert !isClient();
    GrowingBuffer buffer = new GrowingBuffer(64);
    try {
      SubPacketType.TileEntityBlockRunEffect.writeTo(buffer);
      DataEncoder.encode(buffer, world, false);
      if (pos != null) {
        buffer.writeBoolean(true);
        DataEncoder.encode(buffer, pos, false);
      } else {
        buffer.writeBoolean(false);
      } 
      buffer.writeDouble(entity.posX + (IC2.random.nextFloat() - 0.5D) * entity.field_70130_N);
      buffer.writeDouble((entity.func_174813_aQ()).field_72338_b + 0.1D);
      buffer.writeDouble(entity.posZ + (IC2.random.nextFloat() - 0.5D) * entity.field_70130_N);
      buffer.writeDouble(-entity.motionX * 4.0D);
      buffer.writeDouble(-entity.motionZ * 4.0D);
      buffer.writeString(teBlock.getName());
    } catch (IOException e) {
      throw new RuntimeException(e);
    } 
    buffer.flip();
    for (EntityPlayer player : world.field_73010_i) {
      if (!(player instanceof EntityPlayerMP))
        continue; 
      double distance = player.func_70092_e(entity.posX, entity.posY, entity.posZ);
      if (distance <= 1024.0D)
        sendPacket(buffer, false, (EntityPlayerMP)player); 
    } 
  }
  
  final void sendLargePacket(EntityPlayerMP player, int id, GrowingBuffer data) {
    boolean lastPacket;
    GrowingBuffer buffer = new GrowingBuffer(16384);
    buffer.writeShort(0);
    try {
      DeflaterOutputStream deflate = new DeflaterOutputStream(buffer);
      data.writeTo(deflate);
      deflate.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    } 
    buffer.flip();
    boolean firstPacket = true;
    do {
      lastPacket = (buffer.available() <= 32766);
      if (!firstPacket)
        buffer.skipBytes(-2); 
      SubPacketType.LargePacket.writeTo(buffer);
      int state = 0;
      if (firstPacket)
        state |= 0x1; 
      if (lastPacket)
        state |= 0x2; 
      state |= id << 2;
      buffer.write(state);
      buffer.skipBytes(-2);
      if (lastPacket) {
        sendPacket(buffer, true, player);
        assert !buffer.hasAvailable();
      } else {
        sendPacket(buffer.copy(32766), true, player);
      } 
      firstPacket = false;
    } while (!lastPacket);
  }
  
  @SubscribeEvent
  public void onPacket(FMLNetworkEvent.ServerCustomPacketEvent event) {
    if (getClass() == NetworkManager.class) {
      try {
        onPacketData(GrowingBuffer.wrap(event.getPacket().payload()), 
            (EntityPlayer)((NetHandlerPlayServer)event.getHandler()).field_147369_b);
      } catch (Throwable t) {
        IC2.log.warn(LogCategory.Network, t, "Network read failed");
        throw new RuntimeException(t);
      } 
      event.getPacket().payload().release();
    } 
  }
  
  private void onPacketData(GrowingBuffer is, final EntityPlayer player) throws IOException {
    final ItemStack stack;
    final int keyState;
    final Object teDeferred;
    final boolean hand;
    final int event;
    final Object teDeferred;
    if (!is.hasAvailable())
      return; 
    SubPacketType packetType = SubPacketType.read(is, true);
    if (packetType == null)
      return; 
    switch (packetType) {
      case ItemEvent:
        stack = DataEncoder.<ItemStack>decode(is, ItemStack.class);
        event = is.readInt();
        if (stack.getItem() instanceof INetworkItemEventListener)
          IC2.platform.requestTick(true, new Runnable() {
                public void run() {
                  ((INetworkItemEventListener)stack.getItem()).onNetworkEvent(stack, player, event);
                }
              }); 
        return;
      case KeyUpdate:
        keyState = is.readInt();
        IC2.platform.requestTick(true, new Runnable() {
              public void run() {
                IC2.keyboard.processKeyUpdate(player, keyState);
              }
            });
        return;
      case TileEntityEvent:
        teDeferred = DataEncoder.decodeDeferred(is, TileEntity.class);
        event = is.readInt();
        IC2.platform.requestTick(true, new Runnable() {
              public void run() {
                TileEntity te = DataEncoder.<TileEntity>getValue(teDeferred);
                if (te instanceof INetworkClientTileEntityEventListener)
                  ((INetworkClientTileEntityEventListener)te).onNetworkEvent(player, event); 
              }
            });
        return;
      case RequestGUI:
        hand = is.readBoolean();
        object1 = hand ? null : DataEncoder.decodeDeferred(is, TileEntity.class);
        IC2.platform.requestTick(true, new Runnable() {
              private IHasGui tryFindGUI(ItemStack stack) {
                if (!StackUtil.isEmpty(stack) && stack.getItem() instanceof IHandHeldInventory)
                  return ((IHandHeldInventory)stack.getItem()).getInventory(player, stack); 
                return null;
              }
              
              public void run() {
                if (hand) {
                  for (ItemStack stack : player.func_184214_aD()) {
                    IHasGui gui = tryFindGUI(stack);
                    if (gui != null) {
                      IC2.platform.launchGui(player, gui);
                      break;
                    } 
                  } 
                } else {
                  TileEntity te = DataEncoder.<TileEntity>getValue(teDeferred);
                  if (te instanceof IHasGui)
                    IC2.platform.launchGui(player, (IHasGui)te); 
                } 
              }
            });
        return;
      case Rpc:
        RpcHandler.processRpcRequest(is, (EntityPlayerMP)player);
        return;
    } 
    onCommonPacketData(packetType, true, is, player);
  }
  
  protected void onCommonPacketData(SubPacketType packetType, boolean simulating, GrowingBuffer is, final EntityPlayer player) throws IOException {
    final int slot, windowId;
    final Object teDeferred;
    final Item item;
    final String fieldName, event, fieldName;
    int dataCount;
    final Object value, subData[];
    int i;
    switch (packetType) {
      case PlayerItemData:
        slot = is.readByte();
        item = DataEncoder.<Item>decode(is, Item.class);
        dataCount = is.readVarInt();
        subData = new Object[dataCount];
        for (i = 0; i < dataCount; i++)
          subData[i] = DataEncoder.decode(is); 
        if (slot >= 0 && slot < 9)
          IC2.platform.requestTick(simulating, new Runnable() {
                public void run() {
                  for (int i = 0; i < subData.length; i++)
                    subData[i] = DataEncoder.getValue(subData[i]); 
                  ItemStack stack = (ItemStack)player.inventory.field_70462_a.get(slot);
                  if (!StackUtil.isEmpty(stack) && stack.getItem() == item && 
                    item instanceof IPlayerItemDataListener)
                    ((IPlayerItemDataListener)item).onPlayerItemNetworkData(player, slot, subData); 
                }
              }); 
        return;
      case ContainerData:
        windowId = is.readInt();
        str1 = is.readString();
        value = DataEncoder.decode(is);
        IC2.platform.requestTick(simulating, new Runnable() {
              public void run() {
                if (player.field_71070_bA instanceof ContainerBase && player.field_71070_bA.field_75152_c == windowId && (NetworkManager.this
                  
                  .isClient() || NetworkManager.this.getClientModifiableField(player.field_71070_bA.getClass(), fieldName) != null))
                  ReflectionUtil.setValueRecursive(player.field_71070_bA, fieldName, DataEncoder.getValue(value)); 
              }
            });
        return;
      case ContainerEvent:
        windowId = is.readInt();
        event = is.readString();
        IC2.platform.requestTick(simulating, new Runnable() {
              public void run() {
                if (player.field_71070_bA instanceof ContainerBase && player.field_71070_bA.field_75152_c == windowId)
                  ((ContainerBase)player.field_71070_bA).onContainerEvent(event); 
              }
            });
        return;
      case HandHeldInvData:
        windowId = is.readInt();
        fieldName = is.readString();
        value = DataEncoder.decode(is);
        IC2.platform.requestTick(simulating, new Runnable() {
              public void run() {
                if (player.field_71070_bA instanceof ContainerBase && player.field_71070_bA.field_75152_c == windowId) {
                  ContainerBase<?> container = (ContainerBase)player.field_71070_bA;
                  if (container.base instanceof ic2.core.item.tool.HandHeldInventory && (NetworkManager.this
                    .isClient() || NetworkManager.this.getClientModifiableField(container.base.getClass(), fieldName) != null))
                    ReflectionUtil.setValueRecursive(container.base, fieldName, DataEncoder.getValue(value)); 
                } 
              }
            });
        return;
      case TileEntityData:
        teDeferred = DataEncoder.decodeDeferred(is, TileEntity.class);
        fieldName = is.readString();
        value = DataEncoder.decode(is);
        IC2.platform.requestTick(simulating, new Runnable() {
              public void run() {
                TileEntity te = DataEncoder.<TileEntity>getValue(teDeferred);
                if (te != null && (NetworkManager.this
                  .isClient() || NetworkManager.this.getClientModifiableField(te.getClass(), fieldName) != null))
                  ReflectionUtil.setValueRecursive(te, fieldName, DataEncoder.getValue(value)); 
              }
            });
        return;
    } 
    IC2.log.warn(LogCategory.Network, "Unhandled packet type: %s", new Object[] { packetType.name() });
  }
  
  public void initiateKeyUpdate(int keyState) {}
  
  public void sendLoginData() {}
  
  public final void initiateExplosionEffect(World world, Vec3d pos, ExplosionIC2.Type type) {
    assert !isClient();
    try {
      GrowingBuffer buffer = new GrowingBuffer(32);
      SubPacketType.ExplosionEffect.writeTo(buffer);
      DataEncoder.encode(buffer, world, false);
      DataEncoder.encode(buffer, pos, false);
      DataEncoder.encode(buffer, type, false);
      buffer.flip();
      for (Object obj : world.field_73010_i) {
        if (!(obj instanceof EntityPlayerMP))
          continue; 
        EntityPlayerMP player = (EntityPlayerMP)obj;
        if (player.func_70092_e(pos.field_72450_a, pos.field_72448_b, pos.field_72449_c) < 128.0D)
          sendPacket(buffer, false, player); 
      } 
    } catch (IOException e) {
      throw new RuntimeException(e);
    } 
  }
  
  protected final void sendPacket(GrowingBuffer buffer) {
    if (!isClient()) {
      channel.sendToAll(makePacket(buffer, true));
    } else {
      channel.sendToServer(makePacket(buffer, true));
    } 
  }
  
  protected final void sendPacket(GrowingBuffer buffer, boolean advancePos, EntityPlayerMP player) {
    assert !isClient();
    channel.sendTo(makePacket(buffer, advancePos), player);
  }
  
  static <T extends Collection<EntityPlayerMP>> T getPlayersInRange(World world, BlockPos pos, T result) {
    if (!(world instanceof WorldServer))
      return result; 
    PlayerChunkMap playerManager = ((WorldServer)world).func_184164_w();
    PlayerChunkMapEntry instance = playerManager.func_187301_b(pos.getX() >> 4, pos.getZ() >> 4);
    if (instance == null)
      return result; 
    result.addAll((Collection)ReflectionUtil.getFieldValue(playerInstancePlayers, instance));
    return result;
  }
  
  private static Field playerInstancePlayers = ReflectionUtil.getField(PlayerChunkMapEntry.class, List.class);
  
  private static FMLEventChannel channel;
  
  private static final int maxPacketDataLength = 32766;
  
  public static final String channelName = "ic2";
  
  static void writeFieldData(Object object, String fieldName, GrowingBuffer out) throws IOException {
    int pos = fieldName.indexOf('=');
    if (pos != -1) {
      out.writeString(fieldName.substring(0, pos));
      DataEncoder.encode(out, fieldName.substring(pos + 1));
    } else {
      out.writeString(fieldName);
      try {
        DataEncoder.encode(out, ReflectionUtil.getValueRecursive(object, fieldName));
      } catch (NoSuchFieldException e) {
        throw new RuntimeException("Can't find field " + fieldName + " in " + object.getClass().getName(), e);
      } 
    } 
  }
  
  private static FMLProxyPacket makePacket(GrowingBuffer buffer, boolean advancePos) {
    return new FMLProxyPacket(new PacketBuffer(buffer.toByteBuf(advancePos)), "ic2");
  }
}
