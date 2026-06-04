package ic2.core.item.tool;

import ic2.api.event.RetextureEvent;
import ic2.api.item.ElectricItem;
import ic2.core.IC2;
import ic2.core.block.state.BlockStateUtil;
import ic2.core.item.BaseElectricItem;
import ic2.core.model.ModelUtil;
import ic2.core.network.IPlayerItemDataListener;
import ic2.core.network.NetworkManager;
import ic2.core.ref.ItemName;
import ic2.core.util.LogCategory;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import ic2.core.util.Vector3;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;

public class ItemObscurator extends BaseElectricItem implements IPlayerItemDataListener {
  private final int scanOperationCost = 20000;
  
  private final int printOperationCost = 5000;
  
  public ItemObscurator() {
    super(ItemName.obscurator, 100000.0D, 250.0D, 2);
    func_77656_e(27);
    func_77625_d(1);
    setNoRepair();
  }
  
  public List<String> getHudInfo(ItemStack stack, boolean advanced) {
    List<String> info = new LinkedList<>();
    info.add(ElectricItem.manager.getToolTip(stack));
    return info;
  }
  
  public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
    ItemStack stack = StackUtil.get(player, hand);
    if (!player.func_70093_af() && !world.isRemote && ElectricItem.manager.canUse(stack, 5000.0D)) {
      NBTTagCompound nbt = StackUtil.getOrCreateNbtData(stack);
      IBlockState refState;
      EnumFacing refSide;
      int[] colorMultipliers;
      if ((refState = getState(nbt)) == null || (
        refSide = getSide(nbt)) == null || (
        colorMultipliers = getColorMultipliers(nbt)) == null) {
        clear(nbt);
        return EnumActionResult.PASS;
      } 
      IBlockState state = world.getBlockState(pos);
      RetextureEvent event = new RetextureEvent(world, pos, state, side, player, refState, getVariant(nbt), refSide, colorMultipliers);
      MinecraftForge.EVENT_BUS.post((Event)event);
      if (event.applied) {
        ElectricItem.manager.use(stack, 5000.0D, (EntityLivingBase)player);
        return EnumActionResult.SUCCESS;
      } 
      return EnumActionResult.PASS;
    } 
    if (player.func_70093_af() && world.isRemote && ElectricItem.manager.canUse(stack, 20000.0D))
      return scanBlock(stack, player, world, pos, side) ? EnumActionResult.SUCCESS : EnumActionResult.PASS; 
    return EnumActionResult.PASS;
  }
  
  private boolean scanBlock(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side) {
    assert world.isRemote;
    IBlockState state = Util.getBlockState((IBlockAccess)world, pos);
    if (state.getBlock().isAir(state, (IBlockAccess)world, pos))
      return false; 
    ObscuredRenderInfo renderInfo = getRenderInfo(state, side);
    if (renderInfo == null)
      return false; 
    String variant = ModelUtil.getVariant(state);
    int[] colorMultipliers = new int[renderInfo.tints.length];
    for (int i = 0; i < renderInfo.tints.length; i++)
      colorMultipliers[i] = IC2.platform.getColorMultiplier(state, (IBlockAccess)world, pos, renderInfo.tints[i]); 
    NBTTagCompound nbt = StackUtil.getOrCreateNbtData(stack);
    if (getState(nbt) != state || 
      !variant.equals(getVariant(nbt)) || 
      getSide(nbt) != side || 
      !Arrays.equals(getColorMultipliers(nbt), colorMultipliers)) {
      ((NetworkManager)IC2.network.get(false)).sendPlayerItemData(player, player.inventory.field_70461_c, new Object[] { state
            .getBlock(), variant, side, colorMultipliers });
      return true;
    } 
    return false;
  }
  
  public void onPlayerItemNetworkData(EntityPlayer player, int slot, Object... data) {
    if (!(data[0] instanceof Block))
      return; 
    if (!(data[1] instanceof String))
      return; 
    if (!(data[2] instanceof Integer))
      return; 
    if (!(data[3] instanceof int[]))
      return; 
    ItemStack stack = (ItemStack)player.inventory.field_70462_a.get(slot);
    if (!ElectricItem.manager.use(stack, 20000.0D, (EntityLivingBase)player))
      return; 
    NBTTagCompound nbt = StackUtil.getOrCreateNbtData(stack);
    setState(nbt, (Block)data[0], (String)data[1]);
    setSide(nbt, ((Integer)data[2]).intValue());
    setColorMultipliers(nbt, (int[])data[3]);
  }
  
  public static IBlockState getState(NBTTagCompound nbt) {
    String blockName = nbt.func_74779_i("refBlock");
    if (blockName.isEmpty())
      return null; 
    Block block = Util.getBlock(blockName);
    if (block == null)
      return null; 
    String variant = getVariant(nbt);
    return BlockStateUtil.getState(block, variant);
  }
  
  public static String getVariant(NBTTagCompound nbt) {
    return nbt.func_74779_i("refVariant");
  }
  
  private static void setState(NBTTagCompound nbt, Block block, String variant) {
    nbt.func_74778_a("refBlock", Util.getName(block).toString());
    nbt.func_74778_a("refVariant", variant);
  }
  
  public static EnumFacing getSide(NBTTagCompound nbt) {
    int ordinal = nbt.func_74771_c("refSide");
    if (ordinal < 0 || ordinal >= EnumFacing.field_82609_l.length)
      return null; 
    return EnumFacing.field_82609_l[ordinal];
  }
  
  private static void setSide(NBTTagCompound nbt, int side) {
    nbt.func_74774_a("refSide", (byte)side);
  }
  
  public static int[] getColorMultipliers(NBTTagCompound nbt) {
    int[] ret = nbt.func_74759_k("refColorMuls");
    return (ret.length == 0) ? null : internColorMultipliers(ret);
  }
  
  public static void setColorMultipliers(NBTTagCompound nbt, int[] colorMultipliers) {
    if (colorMultipliers.length == 0)
      throw new IllegalArgumentException(); 
    nbt.func_74783_a("refColorMuls", colorMultipliers);
  }
  
  private static void clear(NBTTagCompound nbt) {
    nbt.func_82580_o("refBlock");
    nbt.func_82580_o("refVariant");
    nbt.func_82580_o("refSide");
    nbt.func_82580_o("refColorMul");
  }
  
  public static ObscuredRenderInfo getRenderInfo(IBlockState state, EnumFacing side) {
    Block block = state.getBlock();
    if (block.func_180664_k() == BlockRenderLayer.TRANSLUCENT)
      return null; 
    IBakedModel model = ModelUtil.getBlockModel(state);
    if (model == null)
      return null; 
    List<BakedQuad> faceQuads = model.func_188616_a(state, side, 0L);
    if (faceQuads.isEmpty())
      return null; 
    float[] uvs = new float[faceQuads.size() * 4];
    int uvsOffset = 0;
    int[] tints = new int[faceQuads.size()];
    TextureAtlasSprite[] sprites = new TextureAtlasSprite[faceQuads.size()];
    ExtractingVertexConsumer testConsumer = testConsumers.get();
    for (BakedQuad faceQuad : faceQuads) {
      testConsumer.setTexture(faceQuad.func_187508_a());
      try {
        faceQuad.pipe(testConsumer);
      } catch (Throwable t) {
        IC2.log.warn(LogCategory.General, t, "Can't retrieve face data");
        return null;
      } finally {
        testConsumer.reset();
      } 
      float[] positions = testConsumer.positions;
      int dx = side.getFrontOffsetX();
      int dy = side.getFrontOffsetY();
      int dz = side.getFrontOffsetZ();
      int xS = (dx + 1) / 2;
      int yS = (dy + 1) / 2;
      int zS = (dz + 1) / 2;
      int vertices = 4;
      int positionElements = 3;
      int firstVertex = -1;
      for (int v = 0; v < 4; v++) {
        int vo = v * 3;
        if (Util.isSimilar(positions[vo + 0], xS) && 
          Util.isSimilar(positions[vo + 1], yS) && 
          Util.isSimilar(positions[vo + 2], zS)) {
          firstVertex = v;
          break;
        } 
      } 
      if (firstVertex == -1)
        continue; 
      Vector3 v1 = new Vector3((positions[3] - positions[0]), (positions[4] - positions[1]), (positions[5] - positions[2]));
      if (!Util.isSimilar(v1.lengthSquared(), 1.0D))
        continue; 
      Vector3 v4 = new Vector3((positions[9] - positions[0]), (positions[10] - positions[1]), (positions[11] - positions[2]));
      if (!Util.isSimilar(v4.lengthSquared(), 1.0D))
        continue; 
      Vector3 v3 = new Vector3((positions[9] - positions[6]), (positions[10] - positions[7]), (positions[11] - positions[8]));
      if (!Util.isSimilar(v3.copy().add(v1).lengthSquared(), 0.0D))
        continue; 
      Vector3 normal = v1.copy().cross(v4);
      if (!Util.isSimilar(normal.copy().sub(dx, dy, dz).lengthSquared(), 0.0D))
        continue; 
      tints[uvsOffset / 4] = testConsumer.tint;
      sprites[uvsOffset / 4] = testConsumer.sprite;
      uvs[uvsOffset++] = testConsumer.uvs[firstVertex * 2];
      uvs[uvsOffset++] = testConsumer.uvs[firstVertex * 2 + 1];
      uvs[uvsOffset++] = testConsumer.uvs[(firstVertex + 2) % 4 * 2];
      uvs[uvsOffset++] = testConsumer.uvs[(firstVertex + 2) % 4 * 2 + 1];
    } 
    if (uvsOffset == 0)
      return null; 
    if (uvsOffset < uvs.length) {
      uvs = Arrays.copyOf(uvs, uvsOffset);
      tints = Arrays.copyOf(tints, uvsOffset / 4);
    } 
    tints = internTints(tints);
    return new ObscuredRenderInfo(uvs, tints, sprites);
  }
  
  public static int[] internTints(int[] tints) {
    if (tints.length == 1) {
      if (tints[0] == noTint[0])
        return noTint; 
      if (tints[0] == zeroTint[0])
        return zeroTint; 
    } 
    return tints;
  }
  
  public static int[] internColorMultipliers(int[] colorMultipliers) {
    if (colorMultipliers.length == 1) {
      if (colorMultipliers[0] == defaultColorMultiplier[0])
        return defaultColorMultiplier; 
      if (colorMultipliers[0] == colorMultiplierOpaqueWhite[0])
        return colorMultiplierOpaqueWhite; 
    } 
    return colorMultipliers;
  }
  
  public static class ObscuredRenderInfo {
    public final float[] uvs;
    
    public final int[] tints;
    
    public final TextureAtlasSprite[] sprites;
    
    private ObscuredRenderInfo(float[] uvs, int[] tints, TextureAtlasSprite[] sprites) {
      this.uvs = uvs;
      this.tints = tints;
      this.sprites = sprites;
    }
  }
  
  private static class ExtractingVertexConsumer implements IVertexConsumer {
    public VertexFormat getVertexFormat() {
      return DefaultVertexFormats.field_181707_g;
    }
    
    public void setQuadTint(int tint) {
      this.tint = tint;
    }
    
    public void setQuadOrientation(EnumFacing orientation) {}
    
    public void setApplyDiffuseLighting(boolean diffuse) {}
    
    public void put(int element, float... data) {
      if (element == 0) {
        this.positions[this.posIdx++] = data[0];
        this.positions[this.posIdx++] = data[1];
        this.positions[this.posIdx++] = data[2];
      } else if (element == 1) {
        this.uvs[this.uvIdx++] = data[0];
        this.uvs[this.uvIdx++] = data[1];
      } else {
        throw new IllegalStateException("invalid element: " + element);
      } 
    }
    
    public void setTexture(TextureAtlasSprite texture) {
      this.sprite = texture;
    }
    
    public void reset() {
      this.posIdx = 0;
      this.uvIdx = 0;
      this.tint = -1;
      this.sprite = null;
    }
    
    private final float[] positions = new float[12];
    
    private int posIdx;
    
    private final float[] uvs = new float[8];
    
    private int uvIdx;
    
    private int tint = -1;
    
    private TextureAtlasSprite sprite;
    
    private ExtractingVertexConsumer() {}
  }
  
  private static ThreadLocal<ExtractingVertexConsumer> testConsumers = new ThreadLocal<ExtractingVertexConsumer>() {
      protected ItemObscurator.ExtractingVertexConsumer initialValue() {
        return new ItemObscurator.ExtractingVertexConsumer();
      }
    };
  
  private static final int[] noTint = new int[] { -1 };
  
  private static final int[] zeroTint = new int[] { 0 };
  
  private static final int[] defaultColorMultiplier = new int[] { 16777215 };
  
  private static final int[] colorMultiplierOpaqueWhite = new int[] { -1 };
}
