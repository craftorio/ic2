// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tool;

import net.minecraft.util.EnumFacing;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import ic2.core.util.StackUtil;
import ic2.core.model.ModelUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import net.minecraft.item.ItemStack;
import net.minecraft.client.renderer.block.model.IBakedModel;
import java.util.List;
import java.util.Collections;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.util.ResourceLocation;
import ic2.core.model.MaskOverlayModel;

public class RenderObscurator extends MaskOverlayModel
{
    private static final ResourceLocation baseModelLoc;
    private static final ResourceLocation maskTextureLoc;
    private final ItemOverrideList overrideHandler;
    
    public RenderObscurator() {
        super(RenderObscurator.baseModelLoc, RenderObscurator.maskTextureLoc, true, 0.001f);
        this.overrideHandler = new ItemOverrideList((List)Collections.emptyList()) {
            public IBakedModel handleItemState(final IBakedModel originalModel, final ItemStack stack, final World world, final EntityLivingBase entity) {
                if (stack == null) {
                    return ModelUtil.getMissingModel();
                }
                final NBTTagCompound nbt = StackUtil.getOrCreateNbtData(stack);
                final IBlockState state = ItemObscurator.getState(nbt);
                final EnumFacing side = ItemObscurator.getSide(nbt);
                final int[] colorMultipliers = ItemObscurator.getColorMultipliers(nbt);
                final ItemObscurator.ObscuredRenderInfo renderInfo;
                if (state == null || side == null || (renderInfo = ItemObscurator.getRenderInfo(state, side)) == null || colorMultipliers == null || colorMultipliers.length * 4 != renderInfo.uvs.length) {
                    return MaskOverlayModel.this.get();
                }
                return MaskOverlayModel.this.get(renderInfo.uvs, colorMultipliers);
            }
        };
    }
    
    @Override
    public ItemOverrideList getOverrides() {
        return this.overrideHandler;
    }
    
    static {
        baseModelLoc = new ResourceLocation("ic2", "item/tool/electric/obscurator_raw");
        maskTextureLoc = new ResourceLocation("ic2", "textures/items/tool/electric/obscurator_mask.png");
    }
}
