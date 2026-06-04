// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tool;

import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import java.util.Iterator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import ic2.core.util.Vector3;
import ic2.core.util.LogCategory;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockRenderLayer;
import ic2.core.block.state.BlockStateUtil;
import net.minecraft.block.Block;
import ic2.core.network.NetworkManager;
import java.util.Arrays;
import ic2.core.IC2;
import ic2.core.model.ModelUtil;
import net.minecraft.world.IBlockAccess;
import ic2.core.util.Util;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.common.MinecraftForge;
import ic2.api.event.RetextureEvent;
import ic2.core.util.StackUtil;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;
import ic2.api.item.ElectricItem;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.item.ItemStack;
import ic2.core.ref.ItemName;
import ic2.core.network.IPlayerItemDataListener;
import ic2.core.item.BaseElectricItem;

public class ItemObscurator extends BaseElectricItem implements IPlayerItemDataListener
{
    private final int scanOperationCost = 20000;
    private final int printOperationCost = 5000;
    private static ThreadLocal<ExtractingVertexConsumer> testConsumers;
    private static final int[] noTint;
    private static final int[] zeroTint;
    private static final int[] defaultColorMultiplier;
    private static final int[] colorMultiplierOpaqueWhite;
    
    public ItemObscurator() {
        super(ItemName.obscurator, 100000.0, 250.0, 2);
        this.setMaxDamage(27);
        this.setMaxStackSize(1);
        this.setNoRepair();
    }
    
    @Override
    public List<String> getHudInfo(final ItemStack stack, final boolean advanced) {
        final List<String> info = new LinkedList<String>();
        info.add(ElectricItem.manager.getToolTip(stack));
        return info;
    }
    
    public EnumActionResult onItemUseFirst(final EntityPlayer player, final World world, final BlockPos pos, final EnumFacing side, final float hitX, final float hitY, final float hitZ, final EnumHand hand) {
        final ItemStack stack = StackUtil.get(player, hand);
        if (!player.isSneaking() && !world.isRemote && ElectricItem.manager.canUse(stack, 5000.0)) {
            final NBTTagCompound nbt = StackUtil.getOrCreateNbtData(stack);
            final IBlockState refState;
            final EnumFacing refSide;
            final int[] colorMultipliers;
            if ((refState = getState(nbt)) == null || (refSide = getSide(nbt)) == null || (colorMultipliers = getColorMultipliers(nbt)) == null) {
                clear(nbt);
                return EnumActionResult.PASS;
            }
            final IBlockState state = world.getBlockState(pos);
            final RetextureEvent event = new RetextureEvent(world, pos, state, side, player, refState, getVariant(nbt), refSide, colorMultipliers);
            MinecraftForge.EVENT_BUS.post((Event)event);
            if (event.applied) {
                ElectricItem.manager.use(stack, 5000.0, (EntityLivingBase)player);
                return EnumActionResult.SUCCESS;
            }
            return EnumActionResult.PASS;
        }
        else {
            if (player.isSneaking() && world.isRemote && ElectricItem.manager.canUse(stack, 20000.0)) {
                return this.scanBlock(stack, player, world, pos, side) ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
            }
            return EnumActionResult.PASS;
        }
    }
    
    private boolean scanBlock(final ItemStack stack, final EntityPlayer player, final World world, final BlockPos pos, final EnumFacing side) {
        assert world.isRemote;
        final IBlockState state = Util.getBlockState((IBlockAccess)world, pos);
        if (state.getBlock().isAir(state, (IBlockAccess)world, pos)) {
            return false;
        }
        final ObscuredRenderInfo renderInfo = getRenderInfo(state, side);
        if (renderInfo == null) {
            return false;
        }
        final String variant = ModelUtil.getVariant(state);
        final int[] colorMultipliers = new int[renderInfo.tints.length];
        for (int i = 0; i < renderInfo.tints.length; ++i) {
            colorMultipliers[i] = IC2.platform.getColorMultiplier(state, (IBlockAccess)world, pos, renderInfo.tints[i]);
        }
        final NBTTagCompound nbt = StackUtil.getOrCreateNbtData(stack);
        if (getState(nbt) != state || !variant.equals(getVariant(nbt)) || getSide(nbt) != side || !Arrays.equals(getColorMultipliers(nbt), colorMultipliers)) {
            IC2.network.get(false).sendPlayerItemData(player, player.inventory.currentItem, state.getBlock(), variant, side, colorMultipliers);
            return true;
        }
        return false;
    }
    
    @Override
    public void onPlayerItemNetworkData(final EntityPlayer player, final int slot, final Object... data) {
        if (!(data[0] instanceof Block)) {
            return;
        }
        if (!(data[1] instanceof String)) {
            return;
        }
        if (!(data[2] instanceof Integer)) {
            return;
        }
        if (!(data[3] instanceof int[])) {
            return;
        }
        final ItemStack stack = (ItemStack)player.inventory.mainInventory.get(slot);
        if (!ElectricItem.manager.use(stack, 20000.0, (EntityLivingBase)player)) {
            return;
        }
        final NBTTagCompound nbt = StackUtil.getOrCreateNbtData(stack);
        setState(nbt, (Block)data[0], (String)data[1]);
        setSide(nbt, (int)data[2]);
        setColorMultipliers(nbt, (int[])data[3]);
    }
    
    public static IBlockState getState(final NBTTagCompound nbt) {
        final String blockName = nbt.getString("refBlock");
        if (blockName.isEmpty()) {
            return null;
        }
        final Block block = Util.getBlock(blockName);
        if (block == null) {
            return null;
        }
        final String variant = getVariant(nbt);
        return BlockStateUtil.getState(block, variant);
    }
    
    public static String getVariant(final NBTTagCompound nbt) {
        return nbt.getString("refVariant");
    }
    
    private static void setState(final NBTTagCompound nbt, final Block block, final String variant) {
        nbt.setString("refBlock", Util.getName(block).toString());
        nbt.setString("refVariant", variant);
    }
    
    public static EnumFacing getSide(final NBTTagCompound nbt) {
        final int ordinal = nbt.getByte("refSide");
        if (ordinal < 0 || ordinal >= EnumFacing.VALUES.length) {
            return null;
        }
        return EnumFacing.VALUES[ordinal];
    }
    
    private static void setSide(final NBTTagCompound nbt, final int side) {
        nbt.setByte("refSide", (byte)side);
    }
    
    public static int[] getColorMultipliers(final NBTTagCompound nbt) {
        final int[] ret = nbt.getIntArray("refColorMuls");
        return (int[])((ret.length == 0) ? null : internColorMultipliers(ret));
    }
    
    public static void setColorMultipliers(final NBTTagCompound nbt, final int[] colorMultipliers) {
        if (colorMultipliers.length == 0) {
            throw new IllegalArgumentException();
        }
        nbt.setIntArray("refColorMuls", colorMultipliers);
    }
    
    private static void clear(final NBTTagCompound nbt) {
        nbt.removeTag("refBlock");
        nbt.removeTag("refVariant");
        nbt.removeTag("refSide");
        nbt.removeTag("refColorMul");
    }
    
    public static ObscuredRenderInfo getRenderInfo(final IBlockState state, final EnumFacing side) {
        final Block block = state.getBlock();
        if (block.getBlockLayer() == BlockRenderLayer.TRANSLUCENT) {
            return null;
        }
        final IBakedModel model = ModelUtil.getBlockModel(state);
        if (model == null) {
            return null;
        }
        final List<BakedQuad> faceQuads = model.getQuads(state, side, 0L);
        if (faceQuads.isEmpty()) {
            return null;
        }
        float[] uvs = new float[faceQuads.size() * 4];
        int uvsOffset = 0;
        int[] tints = new int[faceQuads.size()];
        final TextureAtlasSprite[] sprites = new TextureAtlasSprite[faceQuads.size()];
        final ExtractingVertexConsumer testConsumer = ItemObscurator.testConsumers.get();
        for (final BakedQuad faceQuad : faceQuads) {
            testConsumer.setTexture(faceQuad.getSprite());
            try {
                faceQuad.pipe((IVertexConsumer)testConsumer);
            }
            catch (final Throwable t) {
                IC2.log.warn(LogCategory.General, t, "Can't retrieve face data");
                return null;
            }
            finally {
                testConsumer.reset();
            }
            final float[] positions = testConsumer.positions;
            final int dx = side.getFrontOffsetX();
            final int dy = side.getFrontOffsetY();
            final int dz = side.getFrontOffsetZ();
            final int xS = (dx + 1) / 2;
            final int yS = (dy + 1) / 2;
            final int zS = (dz + 1) / 2;
            final int vertices = 4;
            final int positionElements = 3;
            int firstVertex = -1;
            for (int v = 0; v < 4; ++v) {
                final int vo = v * 3;
                if (Util.isSimilar(positions[vo + 0], (float)xS) && Util.isSimilar(positions[vo + 1], (float)yS) && Util.isSimilar(positions[vo + 2], (float)zS)) {
                    firstVertex = v;
                    break;
                }
            }
            if (firstVertex == -1) {
                continue;
            }
            final Vector3 v2 = new Vector3(positions[3] - positions[0], positions[4] - positions[1], positions[5] - positions[2]);
            if (!Util.isSimilar(v2.lengthSquared(), 1.0)) {
                continue;
            }
            final Vector3 v3 = new Vector3(positions[9] - positions[0], positions[10] - positions[1], positions[11] - positions[2]);
            if (!Util.isSimilar(v3.lengthSquared(), 1.0)) {
                continue;
            }
            final Vector3 v4 = new Vector3(positions[9] - positions[6], positions[10] - positions[7], positions[11] - positions[8]);
            if (!Util.isSimilar(v4.copy().add(v2).lengthSquared(), 0.0)) {
                continue;
            }
            final Vector3 normal = v2.copy().cross(v3);
            if (!Util.isSimilar(normal.copy().sub(dx, dy, dz).lengthSquared(), 0.0)) {
                continue;
            }
            tints[uvsOffset / 4] = testConsumer.tint;
            sprites[uvsOffset / 4] = testConsumer.sprite;
            uvs[uvsOffset++] = testConsumer.uvs[firstVertex * 2];
            uvs[uvsOffset++] = testConsumer.uvs[firstVertex * 2 + 1];
            uvs[uvsOffset++] = testConsumer.uvs[(firstVertex + 2) % 4 * 2];
            uvs[uvsOffset++] = testConsumer.uvs[(firstVertex + 2) % 4 * 2 + 1];
        }
        if (uvsOffset == 0) {
            return null;
        }
        if (uvsOffset < uvs.length) {
            uvs = Arrays.copyOf(uvs, uvsOffset);
            tints = Arrays.copyOf(tints, uvsOffset / 4);
        }
        tints = internTints(tints);
        return new ObscuredRenderInfo(uvs, tints, sprites);
    }
    
    public static int[] internTints(final int[] tints) {
        if (tints.length == 1) {
            if (tints[0] == ItemObscurator.noTint[0]) {
                return ItemObscurator.noTint;
            }
            if (tints[0] == ItemObscurator.zeroTint[0]) {
                return ItemObscurator.zeroTint;
            }
        }
        return tints;
    }
    
    public static int[] internColorMultipliers(final int[] colorMultipliers) {
        if (colorMultipliers.length == 1) {
            if (colorMultipliers[0] == ItemObscurator.defaultColorMultiplier[0]) {
                return ItemObscurator.defaultColorMultiplier;
            }
            if (colorMultipliers[0] == ItemObscurator.colorMultiplierOpaqueWhite[0]) {
                return ItemObscurator.colorMultiplierOpaqueWhite;
            }
        }
        return colorMultipliers;
    }
    
    static {
        ItemObscurator.testConsumers = new ThreadLocal<ExtractingVertexConsumer>() {
            @Override
            protected ExtractingVertexConsumer initialValue() {
                return new ExtractingVertexConsumer();
            }
        };
        noTint = new int[] { -1 };
        zeroTint = new int[] { 0 };
        defaultColorMultiplier = new int[] { 16777215 };
        colorMultiplierOpaqueWhite = new int[] { -1 };
    }
    
    public static class ObscuredRenderInfo
    {
        public final float[] uvs;
        public final int[] tints;
        public final TextureAtlasSprite[] sprites;
        
        private ObscuredRenderInfo(final float[] uvs, final int[] tints, final TextureAtlasSprite[] sprites) {
            this.uvs = uvs;
            this.tints = tints;
            this.sprites = sprites;
        }
    }
    
    private static class ExtractingVertexConsumer implements IVertexConsumer
    {
        private final float[] positions;
        private int posIdx;
        private final float[] uvs;
        private int uvIdx;
        private int tint;
        private TextureAtlasSprite sprite;
        
        private ExtractingVertexConsumer() {
            this.positions = new float[12];
            this.uvs = new float[8];
            this.tint = -1;
        }
        
        public VertexFormat getVertexFormat() {
            return DefaultVertexFormats.POSITION_TEX;
        }
        
        public void setQuadTint(final int tint) {
            this.tint = tint;
        }
        
        public void setQuadOrientation(final EnumFacing orientation) {
        }
        
        public void setApplyDiffuseLighting(final boolean diffuse) {
        }
        
        public void put(final int element, final float... data) {
            if (element == 0) {
                this.positions[this.posIdx++] = data[0];
                this.positions[this.posIdx++] = data[1];
                this.positions[this.posIdx++] = data[2];
            }
            else {
                if (element != 1) {
                    throw new IllegalStateException("invalid element: " + element);
                }
                this.uvs[this.uvIdx++] = data[0];
                this.uvs[this.uvIdx++] = data[1];
            }
        }
        
        public void setTexture(final TextureAtlasSprite texture) {
            this.sprite = texture;
        }
        
        public void reset() {
            this.posIdx = 0;
            this.uvIdx = 0;
            this.tint = -1;
            this.sprite = null;
        }
    }
}
