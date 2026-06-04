package ic2.core.block.wiring;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import ic2.core.block.BlockFoam;
import ic2.core.block.TileEntityWall;
import ic2.core.block.comp.Obscuration;
import ic2.core.block.state.IIdProvider;
import ic2.core.block.state.Ic2BlockState;
import ic2.core.model.AbstractModel;
import ic2.core.model.BasicBakedBlockModel;
import ic2.core.model.ISpecialParticleModel;
import ic2.core.model.ModelUtil;
import ic2.core.model.VdUtil;
import ic2.core.ref.BlockName;
import ic2.core.ref.TeBlock;
import ic2.core.util.Ic2Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CableModel extends AbstractModel implements ISpecialParticleModel {
  public Collection<ResourceLocation> getTextures() {
    return this.textures.keySet();
  }
  
  public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
    for (Map.Entry<ResourceLocation, TextureAtlasSprite> entry : this.textures.entrySet())
      entry.setValue(bakedTextureGetter.apply(entry.getKey())); 
    return (IBakedModel)this;
  }
  
  private static Map<ResourceLocation, TextureAtlasSprite> generateTextureLocations() {
    Map<ResourceLocation, TextureAtlasSprite> ret = new HashMap<>();
    StringBuilder name = new StringBuilder();
    name.append("blocks/wiring/cable/");
    int reset0 = name.length();
    for (CableType type : CableType.values) {
      name.append(type.name());
      name.append("_cable");
      int reset1 = name.length();
      for (int insulation = 0; insulation <= type.maxInsulation; insulation++) {
        if (type.maxInsulation != 0) {
          name.append('_');
          name.append(insulation);
        } 
        if (insulation >= type.minColoredInsulation) {
          name.append('_');
          int reset2 = name.length();
          for (Ic2Color color : Ic2Color.values) {
            name.append(color.name());
            ret.put(new ResourceLocation("ic2", name.toString()), null);
            name.setLength(reset2);
          } 
        } else {
          ret.put(new ResourceLocation("ic2", name.toString()), null);
          if (type == CableType.splitter || type == CableType.detector)
            ret.put(new ResourceLocation("ic2", name.toString() + "_active"), null); 
        } 
        name.setLength(reset1);
      } 
      name.setLength(reset0);
    } 
    return ret;
  }
  
  private static ResourceLocation getTextureLocation(CableType type, int insulation, Ic2Color color, boolean active) {
    String loc = "blocks/wiring/cable/" + type.getName(insulation, color);
    if (active)
      loc = loc + "_active"; 
    return new ResourceLocation("ic2", loc);
  }
  
  public List<BakedQuad> func_188616_a(IBlockState rawState, EnumFacing side, long rand) {
    if (!(rawState instanceof Ic2BlockState.Ic2BlockStateInstance))
      return ModelUtil.getMissingModel().func_188616_a(rawState, side, rand); 
    Ic2BlockState.Ic2BlockStateInstance state = (Ic2BlockState.Ic2BlockStateInstance)rawState;
    if (!state.hasValue(TileEntityCable.renderStateProperty))
      return ModelUtil.getMissingModel().func_188616_a((IBlockState)state, side, rand); 
    TileEntityCable.CableRenderState prop = (TileEntityCable.CableRenderState)state.getValue(TileEntityCable.renderStateProperty);
    if (prop.foam == CableFoam.Soft)
      return ModelUtil.getBlockModel(BlockName.foam.getBlockState((IIdProvider)BlockFoam.FoamType.normal)).func_188616_a((IBlockState)state, side, rand); 
    if (prop.foam == CableFoam.Hardened) {
      TileEntityWall.WallRenderState wallProp = (TileEntityWall.WallRenderState)state.getValue(TileEntityWall.renderStateProperty);
      if (wallProp == null)
        return ModelUtil.getMissingModel().func_188616_a((IBlockState)state, side, rand); 
      if (wallProp.obscurations == null)
        return ModelUtil.getBlockModel(BlockName.wall.getBlockState((IIdProvider)wallProp.color)).func_188616_a((IBlockState)state, side, rand); 
      IBakedModel model = ModelUtil.getBlockModel(BlockName.te.getBlockState((IIdProvider)TeBlock.wall));
      return model.func_188616_a((IBlockState)state, side, rand);
    } 
    try {
      return ((IBakedModel)this.modelCache.get(prop)).func_188616_a((IBlockState)state, side, rand);
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    } 
  }
  
  private IBakedModel generateModel(TileEntityCable.CableRenderState prop) {
    float th = prop.type.thickness + (prop.insulation * 2) * 0.0625F;
    float sp = (1.0F - th) / 2.0F;
    List[] arrayOfList = new List[EnumFacing.VALUES.length];
    for (int i = 0; i < arrayOfList.length; i++)
      arrayOfList[i] = new ArrayList(); 
    List<BakedQuad> generalQuads = new ArrayList<>();
    TextureAtlasSprite sprite = this.textures.get(getTextureLocation(prop.type, prop.insulation, prop.color, prop.active));
    for (EnumFacing facing : EnumFacing.VALUES) {
      boolean hasConnection = ((prop.connectivity & 1 << facing.ordinal()) != 0);
      float zS = sp, yS = zS, xS = yS;
      float zE = sp + th, yE = zE, xE = yE;
      if (hasConnection) {
        switch (facing) {
          case DOWN:
            yS = 0.0F;
            yE = sp;
            break;
          case UP:
            yS = sp + th;
            yE = 1.0F;
            break;
          case NORTH:
            zS = 0.0F;
            zE = sp;
            break;
          case SOUTH:
            zS = sp + th;
            zE = 1.0F;
            break;
          case WEST:
            xS = 0.0F;
            xE = sp;
            break;
          case EAST:
            xS = sp + th;
            xE = 1.0F;
            break;
          default:
            throw new RuntimeException();
        } 
        VdUtil.addCuboid(xS, yS, zS, xE, yE, zE, EnumSet.complementOf((EnumSet)EnumSet.of(facing.getOpposite())), sprite, arrayOfList, generalQuads);
      } else {
        VdUtil.addCuboid(xS, yS, zS, xE, yE, zE, EnumSet.of(facing), sprite, arrayOfList, generalQuads);
      } 
    } 
    int used = 0;
    for (int j = 0; j < arrayOfList.length; j++) {
      if (arrayOfList[j].isEmpty()) {
        arrayOfList[j] = Collections.emptyList();
      } else {
        used++;
      } 
    } 
    if (used == 0)
      arrayOfList = null; 
    if (generalQuads.isEmpty())
      generalQuads = Collections.emptyList(); 
    return (IBakedModel)new BasicBakedBlockModel(arrayOfList, generalQuads, sprite);
  }
  
  public void onReload() {
    this.modelCache.invalidateAll();
  }
  
  public boolean needsEnhancing(IBlockState state) {
    return true;
  }
  
  public TextureAtlasSprite getParticleTexture(Ic2BlockState.Ic2BlockStateInstance state) {
    if (!state.hasValue(TileEntityCable.renderStateProperty))
      return ModelUtil.getMissingModel().func_177554_e(); 
    TileEntityCable.CableRenderState prop = (TileEntityCable.CableRenderState)state.getValue(TileEntityCable.renderStateProperty);
    if (prop.foam == CableFoam.Soft)
      return ModelUtil.getBlockModel(BlockName.foam.getBlockState((IIdProvider)BlockFoam.FoamType.normal)).func_177554_e(); 
    if (prop.foam == CableFoam.Hardened) {
      TileEntityWall.WallRenderState wallProp = (TileEntityWall.WallRenderState)state.getValue(TileEntityWall.renderStateProperty);
      if (wallProp == null)
        return ModelUtil.getMissingModel().func_177554_e(); 
      if (wallProp.obscurations == null)
        return ModelUtil.getBlockModel(BlockName.wall.getBlockState((IIdProvider)wallProp.color)).func_177554_e(); 
      Obscuration.ObscurationData data = wallProp.obscurations[EnumFacing.UP.ordinal()];
      if (data == null)
        return ModelUtil.getBlockModel(BlockName.wall.getBlockState((IIdProvider)wallProp.color)).func_177554_e(); 
      return ModelUtil.getBlockModel(data.state).func_177554_e();
    } 
    return this.textures.get(getTextureLocation(prop.type, prop.insulation, prop.color, prop.active));
  }
  
  private final Map<ResourceLocation, TextureAtlasSprite> textures = generateTextureLocations();
  
  private final LoadingCache<TileEntityCable.CableRenderState, IBakedModel> modelCache = CacheBuilder.newBuilder()
    .maximumSize(256L)
    .expireAfterAccess(5L, TimeUnit.MINUTES)
    .build(new CacheLoader<TileEntityCable.CableRenderState, IBakedModel>() {
        public IBakedModel load(TileEntityCable.CableRenderState key) throws Exception {
          return CableModel.this.generateModel(key);
        }
      });
}
