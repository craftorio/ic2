// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.model;

import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import java.util.List;
import net.minecraft.util.EnumFacing;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import java.util.function.Function;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraftforge.common.model.IModelState;
import java.util.Collections;
import net.minecraft.util.ResourceLocation;
import java.util.Collection;
import net.minecraft.client.renderer.block.model.IBakedModel;

public abstract class AbstractModel implements IReloadableModel, IBakedModel
{
    public Collection<ResourceLocation> getDependencies() {
        return (Collection<ResourceLocation>)Collections.emptyList();
    }
    
    public Collection<ResourceLocation> getTextures() {
        return (Collection<ResourceLocation>)Collections.emptyList();
    }
    
    public IBakedModel bake(final IModelState state, final VertexFormat format, final Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        return (IBakedModel)this;
    }
    
    public IModelState getDefaultState() {
        return (IModelState)TRSRTransformation.identity();
    }
    
    public List<BakedQuad> getQuads(final IBlockState state, final EnumFacing side, final long rand) {
        throw new UnsupportedOperationException();
    }
    
    public boolean isAmbientOcclusion() {
        return true;
    }
    
    public boolean isGui3d() {
        return false;
    }
    
    public boolean isBuiltInRenderer() {
        return false;
    }
    
    public TextureAtlasSprite getParticleTexture() {
        return Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
    }
    
    public ItemCameraTransforms getItemCameraTransforms() {
        return ItemCameraTransforms.DEFAULT;
    }
    
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.NONE;
    }
}
