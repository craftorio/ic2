package ic2.core.crop;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import ic2.api.crops.CropCard;
import ic2.api.crops.Crops;
import ic2.core.block.state.Ic2BlockState;
import ic2.core.model.AbstractModel;
import ic2.core.model.BasicBakedBlockModel;
import ic2.core.model.VdUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CropModel extends AbstractModel {
  public Collection<ResourceLocation> getTextures() {
    if (textures.isEmpty()) {
      IC2Crops.needsToPost = false;
      MinecraftForge.EVENT_BUS.post((Event)new Crops.CropRegisterEvent());
      for (CropCard crop : Crops.instance.getCrops()) {
        for (ResourceLocation aux : crop.getTexturesLocation())
          textures.put(aux, null); 
      } 
      textures.put(STICK, null);
      textures.put(UPGRADED_STICK, null);
    } 
    return textures.keySet();
  }
  
  public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
    for (Map.Entry<ResourceLocation, TextureAtlasSprite> entry : textures.entrySet())
      entry.setValue(bakedTextureGetter.apply(entry.getKey())); 
    return (IBakedModel)this;
  }
  
  private static ResourceLocation getTextureLocation(CropCard crop, int currentSize) {
    return crop.getTexturesLocation().get(currentSize - 1);
  }
  
  public List<BakedQuad> func_188616_a(IBlockState rawState, EnumFacing side, long rand) {
    TileEntityCrop.CropRenderState prop;
    Ic2BlockState.Ic2BlockStateInstance state;
    if (rawState instanceof Ic2BlockState.Ic2BlockStateInstance && (state = (Ic2BlockState.Ic2BlockStateInstance)rawState).hasValue(TileEntityCrop.renderStateProperty)) {
      prop = (TileEntityCrop.CropRenderState)state.getValue(TileEntityCrop.renderStateProperty);
    } else {
      prop = new TileEntityCrop.CropRenderState(null, 0, false);
    } 
    try {
      return ((IBakedModel)this.modelCache.get(prop)).func_188616_a(rawState, side, rand);
    } catch (Exception error) {
      throw new RuntimeException(error);
    } 
  }
  
  IBakedModel generateModel(TileEntityCrop.CropRenderState prop) {
    List[] arrayOfList = new List[EnumFacing.field_176754_o.length];
    for (int index = 0; index < arrayOfList.length; ) {
      arrayOfList[index] = new ArrayList();
      index++;
    } 
    List<BakedQuad> generalQuads = new ArrayList<>();
    TextureAtlasSprite cropSprite = textures.computeIfAbsent(getTextureLocation(prop.crop, prop.size), MISSING);
    for (EnumFacing facing : EnumFacing.field_176754_o) {
      int offsetX = facing.func_82601_c();
      int offsetZ = facing.func_82599_e();
      float x = Math.abs(offsetX) * (0.5F + offsetX * 0.25F);
      float z = Math.abs(offsetZ) * (0.5F + offsetZ * 0.25F);
      float xS = (offsetX == 0) ? 0.0F : x;
      float xE = (offsetX == 0) ? 1.0F : x;
      float zS = (offsetZ == 0) ? 0.0F : z;
      float zE = (offsetZ == 0) ? 1.0F : z;
      VdUtil.addFlippedCuboidWithYOffset(xS, 0.001F, zS, xE, 1.0F, zE, -1, EnumSet.of(facing), cropSprite, arrayOfList, generalQuads, -0.0625F);
      VdUtil.addFlippedCuboidWithYOffset(xS, 0.001F, zS, xE, 1.0F, zE, -1, EnumSet.of(facing.func_176734_d()), cropSprite, arrayOfList, generalQuads, -0.0625F);
    } 
    int used = 0;
    for (int i = 0; i < arrayOfList.length; i++) {
      if (arrayOfList[i].isEmpty()) {
        arrayOfList[i] = Collections.emptyList();
      } else {
        used++;
      } 
    } 
    if (used == 0)
      arrayOfList = null; 
    if (generalQuads.isEmpty())
      generalQuads = Collections.emptyList(); 
    return (IBakedModel)new BasicBakedBlockModel(arrayOfList, generalQuads, cropSprite);
  }
  
  IBakedModel generateStickModel(boolean crosscrop) {
    List[] arrayOfList = new List[EnumFacing.field_176754_o.length];
    for (int index = 0; index < arrayOfList.length; ) {
      arrayOfList[index] = new ArrayList();
      index++;
    } 
    List<BakedQuad> generalQuads = new ArrayList<>();
    TextureAtlasSprite stickSprite = textures.get(STICK);
    TextureAtlasSprite upgradedStickSprite = textures.get(UPGRADED_STICK);
    for (EnumFacing facing : EnumFacing.field_176754_o) {
      int offsetX = facing.func_82601_c();
      int offsetZ = facing.func_82599_e();
      float x = Math.abs(offsetX) * (0.5F + offsetX * 0.25F);
      float z = Math.abs(offsetZ) * (0.5F + offsetZ * 0.25F);
      float xS = (offsetX == 0) ? 0.0F : x;
      float xE = (offsetX == 0) ? 1.0F : x;
      float zS = (offsetZ == 0) ? 0.0F : z;
      float zE = (offsetZ == 0) ? 1.0F : z;
      if (!crosscrop) {
        VdUtil.addFlippedCuboidWithYOffset(xS, 0.001F, zS, xE, 1.0F, zE, -1, EnumSet.of(facing), stickSprite, arrayOfList, generalQuads, -0.0625F);
        VdUtil.addFlippedCuboidWithYOffset(xS, 0.001F, zS, xE, 1.0F, zE, -1, EnumSet.of(facing.func_176734_d()), stickSprite, arrayOfList, generalQuads, -0.0625F);
      } else {
        VdUtil.addFlippedCuboidWithYOffset(xS, 0.001F, zS, xE, 1.0F, zE, -1, EnumSet.of(facing), upgradedStickSprite, arrayOfList, generalQuads, -0.0625F);
        VdUtil.addFlippedCuboidWithYOffset(xS, 0.001F, zS, xE, 1.0F, zE, -1, EnumSet.of(facing.func_176734_d()), upgradedStickSprite, arrayOfList, generalQuads, -0.0625F);
      } 
    } 
    int used = 0;
    for (int i = 0; i < arrayOfList.length; i++) {
      if (arrayOfList[i].isEmpty()) {
        arrayOfList[i] = Collections.emptyList();
      } else {
        used++;
      } 
    } 
    if (used == 0)
      arrayOfList = null; 
    if (generalQuads.isEmpty())
      generalQuads = Collections.emptyList(); 
    return (IBakedModel)new BasicBakedBlockModel(arrayOfList, generalQuads, stickSprite);
  }
  
  public TextureAtlasSprite func_177554_e() {
    return textures.get(STICK);
  }
  
  public void onReload() {
    this.modelCache.invalidateAll();
  }
  
  private static final ResourceLocation STICK = new ResourceLocation("ic2", "blocks/crop/stick");
  
  private static final ResourceLocation UPGRADED_STICK = new ResourceLocation("ic2", "blocks/crop/stick_upgraded");
  
  private static final Function<ResourceLocation, TextureAtlasSprite> MISSING = location -> Minecraft.func_71410_x().func_147117_R().func_174944_f();
  
  static final Map<ResourceLocation, TextureAtlasSprite> textures = new HashMap<>();
  
  private final LoadingCache<TileEntityCrop.CropRenderState, IBakedModel> modelCache = CacheBuilder.newBuilder()
    .maximumSize(256L)
    .expireAfterAccess(5L, TimeUnit.MINUTES)
    .build(new CacheLoader<TileEntityCrop.CropRenderState, IBakedModel>() {
        public IBakedModel load(TileEntityCrop.CropRenderState key) throws Exception {
          if (key.crop == null || key.size <= 0)
            return CropModel.this.generateStickModel(key.crosscrop); 
          return CropModel.this.generateModel(key);
        }
      });
}
