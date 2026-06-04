// TODO(Ravel): Failed to fully resolve file: null cannot be cast to non-null type com.intellij.psi.PsiJavaCodeReferenceElement
// TODO(Ravel): Failed to fully resolve file: null cannot be cast to non-null type com.intellij.psi.PsiJavaCodeReferenceElement
// TODO(Ravel): Failed to fully resolve file: null cannot be cast to non-null type com.intellij.psi.PsiJavaCodeReferenceElement
// TODO(Ravel): Failed to fully resolve file: null cannot be cast to non-null type com.intellij.psi.PsiJavaCodeReferenceElement
// TODO(Ravel): Failed to fully resolve file: null cannot be cast to non-null type com.intellij.psi.PsiJavaCodeReferenceElement
// TODO(Ravel): Failed to fully resolve file: null cannot be cast to non-null type com.intellij.psi.PsiJavaCodeReferenceElement
package ic2.core.block.transport.items;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import ic2.core.block.state.Ic2BlockState;
import ic2.core.block.transport.TileEntityFluidPipe;
import ic2.core.block.transport.TileEntityPipe;
import ic2.core.model.AbstractModel;
import ic2.core.model.BasicBakedBlockModel;
import ic2.core.model.ISpecialParticleModel;
import ic2.core.model.ModelUtil;
import ic2.core.model.VdUtil;
import ic2.core.util.Util;
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
import net.minecraft.client.particle.Particle;
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
public class PipeModel extends AbstractModel implements ISpecialParticleModel {
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
    ret.put(new ResourceLocation("ic2", "blocks/transport/pipe_side"), null);
    StringBuilder name = new StringBuilder();
    name.append("blocks/transport/");
    name.append("pipe");
    int reset0 = name.length();
    for (PipeSize size : PipeSize.values()) {
      name.append('_');
      name.append(size.name());
      ret.put(new ResourceLocation("ic2", name.toString()), null);
      name.setLength(reset0);
    } 
    return ret;
  }
  
  private static ResourceLocation getTextureLocation(PipeSize size) {
    String loc = "blocks/transport/pipe_" + size.getName();
    return new ResourceLocation("ic2", loc);
  }
  
  private static ResourceLocation getSideTextureLocation() {
    String loc = "blocks/transport/pipe_side";
    return new ResourceLocation("ic2", loc);
  }
  
  public List<BakedQuad> func_188616_a(IBlockState rawState, EnumFacing side, long rand) {
    if (!(rawState instanceof Ic2BlockState.Ic2BlockStateInstance))
      return ModelUtil.getMissingModel().func_188616_a(rawState, side, rand); 
    Ic2BlockState.Ic2BlockStateInstance state = (Ic2BlockState.Ic2BlockStateInstance)rawState;
    if (!state.hasValue(TileEntityFluidPipe.renderStateProperty))
      return ModelUtil.getMissingModel().func_188616_a((IBlockState)state, side, rand); 
    TileEntityPipe.PipeRenderState prop = (TileEntityPipe.PipeRenderState)state.getValue(TileEntityFluidPipe.renderStateProperty);
    try {
      return ((IBakedModel)this.modelCache.get(prop)).func_188616_a((IBlockState)state, side, rand);
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    } 
  }
  
  private IBakedModel generateModel(TileEntityPipe.PipeRenderState prop) {
    PipeType type = prop.type;
    int color = 0xFF000000 | ((byte)type.blue & 0xFF) << 16 | ((byte)type.green & 0xFF) << 8 | (byte)type.red & 0xFF;
    float th = prop.size.thickness;
    float sp = (1.0F - th) / 2.0F;
    EnumFacing pFacing = EnumFacing.values()[prop.facing];
    List[] arrayOfList = new List[EnumFacing.field_82609_l.length];
    for (int i = 0; i < arrayOfList.length; i++)
      arrayOfList[i] = new ArrayList(); 
    List<BakedQuad> generalQuads = new ArrayList<>();
    TextureAtlasSprite sideSprite = this.textures.get(getSideTextureLocation());
    TextureAtlasSprite sizeSprite = this.textures.get(getTextureLocation(prop.size));
    int totalConnections = Integer.bitCount(prop.connectivity);
    if (totalConnections == 0) {
      float zS = sp, yS = zS, xS = yS;
      float zE = sp + th, yE = zE, xE = yE;
      VdUtil.addCuboid(xS, yS, zS, xE, yE, zE, color, Util.allFacings, sizeSprite, arrayOfList, generalQuads);
    } else if (totalConnections == 1) {
      EnumFacing connected = EnumFacing.field_82609_l[Integer.numberOfTrailingZeros(prop.connectivity)];
      for (EnumFacing facing : EnumFacing.field_82609_l) {
        float zS = sp, yS = zS, xS = yS;
        float zE = sp + th, yE = zE, xE = yE;
        if (facing == connected) {
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
          VdUtil.addCuboid(xS, yS, zS, xE, yE, zE, color, EnumSet.of(facing), sizeSprite, arrayOfList, generalQuads);
          VdUtil.addCuboid(xS, yS, zS, xE, yE, zE, color, EnumSet.complementOf((EnumSet)EnumSet.of(facing, facing.func_176734_d())), sideSprite, arrayOfList, generalQuads);
        } else if (facing == connected.func_176734_d()) {
          VdUtil.addCuboid(xS, yS, zS, xE, yE, zE, color, EnumSet.of(facing), sizeSprite, arrayOfList, generalQuads);
        } else {
          VdUtil.addCuboid(xS, yS, zS, xE, yE, zE, color, EnumSet.of(facing), sideSprite, arrayOfList, generalQuads);
        } 
      } 
    } else {
      for (EnumFacing facing : EnumFacing.field_82609_l) {
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
          VdUtil.addCuboid(xS, yS, zS, xE, yE, zE, color, EnumSet.of(facing), sizeSprite, arrayOfList, generalQuads);
          VdUtil.addCuboid(xS, yS, zS, xE, yE, zE, color, EnumSet.complementOf((EnumSet)EnumSet.of(facing, facing.func_176734_d())), sideSprite, arrayOfList, generalQuads);
        } else {
          VdUtil.addCuboid(xS, yS, zS, xE, yE, zE, color, EnumSet.of(facing), sideSprite, arrayOfList, generalQuads);
        } 
      } 
    } 
    float cs = 1.0F;
    float ch = 0.1F;
    for (EnumFacing facing : EnumFacing.field_82609_l) {
      boolean hasCover = ((prop.covers & 1 << facing.ordinal()) != 0);
      float zS = 0.0F, yS = zS, xS = yS;
      float zE = 1.0F, yE = zE, xE = yE;
      if (hasCover) {
        switch (facing) {
          case DOWN:
            yS = 0.0F;
            yE = ch;
            break;
          case UP:
            yS = cs - ch;
            yE = 1.0F;
            break;
          case NORTH:
            zS = 0.0F;
            zE = ch;
            break;
          case SOUTH:
            zS = cs - ch;
            zE = 1.0F;
            break;
          case WEST:
            xS = 0.0F;
            xE = ch;
            break;
          case EAST:
            xS = cs - ch;
            xE = 1.0F;
            break;
          default:
            throw new RuntimeException();
        } 
        VdUtil.addCuboid(xS, yS, zS, xE, yE, zE, color, Util.allFacings, sideSprite, arrayOfList, generalQuads);
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
    return (IBakedModel)new BasicBakedBlockModel(arrayOfList, generalQuads, sizeSprite);
  }
  
  public void onReload() {
    this.modelCache.invalidateAll();
  }
  
  public TextureAtlasSprite func_177554_e() {
    return this.textures.get(getSideTextureLocation());
  }
  
  public boolean needsEnhancing(IBlockState state) {
    return true;
  }
  
  public void enhanceParticle(Particle particle, Ic2BlockState.Ic2BlockStateInstance state) {
    if (state.hasValue(TileEntityPipe.renderStateProperty)) {
      TileEntityPipe.PipeRenderState prop = (TileEntityPipe.PipeRenderState)state.getValue(TileEntityPipe.renderStateProperty);
      particle.func_70538_b(prop.type.red / 255.0F, prop.type.green / 255.0F, prop.type.blue / 255.0F);
    } 
  }
  
  private final Map<ResourceLocation, TextureAtlasSprite> textures = generateTextureLocations();
  
  private final LoadingCache<TileEntityPipe.PipeRenderState, IBakedModel> modelCache = CacheBuilder.newBuilder()
    .maximumSize(256L)
    .expireAfterAccess(5L, TimeUnit.MINUTES)
    .build(new CacheLoader<TileEntityPipe.PipeRenderState, IBakedModel>() {
        public IBakedModel load(TileEntityPipe.PipeRenderState key) throws Exception {
          return PipeModel.this.generateModel(key);
        }
      });
}
