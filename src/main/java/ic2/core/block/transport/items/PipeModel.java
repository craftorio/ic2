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
import java.util.Map.Entry;
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
   private final Map<ResourceLocation, TextureAtlasSprite> textures = generateTextureLocations();
   private final LoadingCache<TileEntityPipe.PipeRenderState, IBakedModel> modelCache = CacheBuilder.newBuilder()
      .maximumSize(256L)
      .expireAfterAccess(5L, TimeUnit.MINUTES)
      .build(new CacheLoader<TileEntityPipe.PipeRenderState, IBakedModel>() {
         public IBakedModel load(TileEntityPipe.PipeRenderState key) throws Exception {
            return PipeModel.this.generateModel(key);
         }
      });

   @Override
   public Collection<ResourceLocation> getTextures() {
      return this.textures.keySet();
   }

   @Override
   public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
      for (Entry<ResourceLocation, TextureAtlasSprite> entry : this.textures.entrySet()) {
         entry.setValue(bakedTextureGetter.apply(entry.getKey()));
      }

      return this;
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

   @Override
   public List<BakedQuad> getQuads(IBlockState rawState, EnumFacing side, long rand) {
      if (!(rawState instanceof Ic2BlockState.Ic2BlockStateInstance)) {
         return ModelUtil.getMissingModel().getQuads(rawState, side, rand);
      }

      Ic2BlockState.Ic2BlockStateInstance state = (Ic2BlockState.Ic2BlockStateInstance)rawState;
      if (!state.hasValue(TileEntityFluidPipe.renderStateProperty)) {
         return ModelUtil.getMissingModel().getQuads(state, side, rand);
      }

      TileEntityPipe.PipeRenderState prop = state.getValue(TileEntityFluidPipe.renderStateProperty);

      try {
         return ((IBakedModel)this.modelCache.get(prop)).getQuads(state, side, rand);
      } catch (ExecutionException e) {
         throw new RuntimeException(e);
      }
   }

   private IBakedModel generateModel(TileEntityPipe.PipeRenderState prop) {
      PipeType type = prop.type;
      int color = 0xFF000000 | ((byte)type.blue & 255) << 16 | ((byte)type.green & 255) << 8 | (byte)type.red & 255;
      float th = prop.size.thickness;
      float sp = (1.0F - th) / 2.0F;
      EnumFacing pFacing = EnumFacing.values()[prop.facing];
      List<BakedQuad>[] faceQuads = new List[EnumFacing.VALUES.length];

      for (int i = 0; i < faceQuads.length; i++) {
         faceQuads[i] = new ArrayList<>();
      }

      List<BakedQuad> generalQuads = new ArrayList<>();
      TextureAtlasSprite sideSprite = this.textures.get(getSideTextureLocation());
      TextureAtlasSprite sizeSprite = this.textures.get(getTextureLocation(prop.size));
      int totalConnections = Integer.bitCount(prop.connectivity);
      if (totalConnections == 0) {
         float zS = sp;
         float yS = sp;
         float xS = sp;
         float yE;
         float zE;
         float xE = yE = zE = sp + th;
         VdUtil.addCuboid(xS, yS, zS, xE, yE, zE, color, Util.allFacings, sizeSprite, faceQuads, generalQuads);
      } else if (totalConnections == 1) {
         EnumFacing connected = EnumFacing.VALUES[Integer.numberOfTrailingZeros(prop.connectivity)];

         for (EnumFacing facing : EnumFacing.VALUES) {
            float zS = sp;
            float yS = sp;
            float xS = sp;
            float yE;
            float zE;
            float xE = yE = zE = sp + th;
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

               VdUtil.addCuboid(xS, yS, zS, xE, yE, zE, color, EnumSet.of(facing), sizeSprite, faceQuads, generalQuads);
               VdUtil.addCuboid(
                  xS, yS, zS, xE, yE, zE, color, EnumSet.complementOf(EnumSet.of(facing, facing.getOpposite())), sideSprite, faceQuads, generalQuads
               );
            } else if (facing == connected.getOpposite()) {
               VdUtil.addCuboid(xS, yS, zS, xE, yE, zE, color, EnumSet.of(facing), sizeSprite, faceQuads, generalQuads);
            } else {
               VdUtil.addCuboid(xS, yS, zS, xE, yE, zE, color, EnumSet.of(facing), sideSprite, faceQuads, generalQuads);
            }
         }
      } else {
         for (EnumFacing facing : EnumFacing.VALUES) {
            boolean hasConnection = (prop.connectivity & 1 << facing.ordinal()) != 0;
            float zS = sp;
            float yS = sp;
            float xS = sp;
            float yE;
            float zE;
            float xE = yE = zE = sp + th;
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

               VdUtil.addCuboid(xS, yS, zS, xE, yE, zE, color, EnumSet.of(facing), sizeSprite, faceQuads, generalQuads);
               VdUtil.addCuboid(
                  xS, yS, zS, xE, yE, zE, color, EnumSet.complementOf(EnumSet.of(facing, facing.getOpposite())), sideSprite, faceQuads, generalQuads
               );
            } else {
               VdUtil.addCuboid(xS, yS, zS, xE, yE, zE, color, EnumSet.of(facing), sideSprite, faceQuads, generalQuads);
            }
         }
      }

      float cs = 1.0F;
      float ch = 0.1F;

      for (EnumFacing facing : EnumFacing.VALUES) {
         boolean hasCover = (prop.covers & 1 << facing.ordinal()) != 0;
         float zS = 0.0F;
         float yS = 0.0F;
         float xS = 0.0F;
         float zE = 1.0F;
         float yE = 1.0F;
         float xE = 1.0F;
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

            VdUtil.addCuboid(xS, yS, zS, xE, yE, zE, color, Util.allFacings, sideSprite, faceQuads, generalQuads);
         }
      }

      int used = 0;

      for (int i = 0; i < faceQuads.length; i++) {
         if (faceQuads[i].isEmpty()) {
            faceQuads[i] = Collections.emptyList();
         } else {
            used++;
         }
      }

      if (used == 0) {
         faceQuads = null;
      }

      if (generalQuads.isEmpty()) {
         generalQuads = Collections.emptyList();
      }

      return new BasicBakedBlockModel(faceQuads, generalQuads, sizeSprite);
   }

   @Override
   public void onReload() {
      this.modelCache.invalidateAll();
   }

   @Override
   public TextureAtlasSprite getParticleTexture() {
      return this.textures.get(getSideTextureLocation());
   }

   @Override
   public boolean needsEnhancing(IBlockState state) {
      return true;
   }

   @Override
   public void enhanceParticle(Particle particle, Ic2BlockState.Ic2BlockStateInstance state) {
      if (state.hasValue(TileEntityPipe.renderStateProperty)) {
         TileEntityPipe.PipeRenderState prop = state.getValue(TileEntityPipe.renderStateProperty);
         particle.setRBGColorF(prop.type.red / 255.0F, prop.type.green / 255.0F, prop.type.blue / 255.0F);
      }
   }
}
