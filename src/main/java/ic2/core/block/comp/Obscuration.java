// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.comp;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.tileentity.TileEntity;
import ic2.core.ref.BlockName;
import ic2.api.event.RetextureEvent;
import net.minecraftforge.common.MinecraftForge;
import java.util.Arrays;
import net.minecraft.nbt.NBTBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.Block;
import ic2.core.item.tool.ItemObscurator;
import ic2.core.block.state.BlockStateUtil;
import ic2.core.util.Util;
import net.minecraft.util.EnumFacing;
import net.minecraft.nbt.NBTTagCompound;
import ic2.core.block.TileEntityBlock;

public class Obscuration extends TileEntityComponent
{
    private final Runnable changeHandler;
    private ObscurationData[] dataMap;
    
    public Obscuration(final TileEntityBlock parent, final Runnable changeHandler) {
        super(parent);
        this.changeHandler = changeHandler;
    }
    
    @Override
    public void readFromNbt(final NBTTagCompound nbt) {
        if (nbt.hasNoTags()) {
            return;
        }
        for (final EnumFacing facing : EnumFacing.VALUES) {
            if (nbt.hasKey(facing.getName(), 10)) {
                final NBTTagCompound cNbt = nbt.getCompoundTag(facing.getName());
                final Block block = Util.getBlock(cNbt.getString("block"));
                if (block != null) {
                    final String variant = cNbt.getString("variant");
                    final IBlockState state = BlockStateUtil.getState(block, variant);
                    if (state != null) {
                        final int rawSide = cNbt.getByte("side");
                        if (rawSide >= 0) {
                            if (rawSide < EnumFacing.VALUES.length) {
                                final EnumFacing side = EnumFacing.VALUES[rawSide];
                                final int[] colorMultipliers = ItemObscurator.internColorMultipliers(cNbt.getIntArray("colorMuls"));
                                final ObscurationData data = new ObscurationData(state, variant, side, colorMultipliers);
                                if (this.dataMap == null) {
                                    this.dataMap = new ObscurationData[EnumFacing.VALUES.length];
                                }
                                this.dataMap[facing.ordinal()] = data.intern();
                            }
                        }
                    }
                }
            }
        }
    }
    
    @Override
    public NBTTagCompound writeToNbt() {
        if (this.dataMap == null) {
            return null;
        }
        final NBTTagCompound ret = new NBTTagCompound();
        for (final EnumFacing facing : EnumFacing.VALUES) {
            final ObscurationData data = this.dataMap[facing.ordinal()];
            if (data != null) {
                final NBTTagCompound nbt = new NBTTagCompound();
                nbt.setString("block", Util.getName(data.state.getBlock()).toString());
                nbt.setString("variant", data.variant);
                nbt.setByte("side", (byte)data.side.ordinal());
                nbt.setIntArray("colorMuls", data.colorMultipliers);
                ret.setTag(facing.getName(), (NBTBase)nbt);
            }
        }
        return ret;
    }
    
    public boolean applyObscuration(final EnumFacing side, final ObscurationData data) {
        if (this.dataMap != null && data.equals(this.dataMap[side.ordinal()])) {
            return false;
        }
        if (this.dataMap == null) {
            this.dataMap = new ObscurationData[EnumFacing.VALUES.length];
        }
        this.dataMap[side.ordinal()] = data.intern();
        this.changeHandler.run();
        return true;
    }
    
    public void clear() {
        this.dataMap = null;
        this.changeHandler.run();
    }
    
    public boolean hasObscuration() {
        return this.dataMap != null;
    }
    
    public ObscurationData[] getRenderState() {
        if (this.dataMap == null) {
            return null;
        }
        return Arrays.copyOf(this.dataMap, this.dataMap.length);
    }
    
    public static class ObscurationComponentEventHandler
    {
        public static void init() {
            new ObscurationComponentEventHandler();
        }
        
        private ObscurationComponentEventHandler() {
            MinecraftForge.EVENT_BUS.register((Object)this);
        }
        
        @SubscribeEvent
        public void onObscuration(final RetextureEvent event) {
            if (event.state.getBlock() != BlockName.te.getInstance()) {
                return;
            }
            final TileEntity teRaw = event.getWorld().getTileEntity(event.pos);
            if (!(teRaw instanceof TileEntityBlock)) {
                return;
            }
            final Obscuration obscuration = ((TileEntityBlock)teRaw).getComponent(Obscuration.class);
            if (obscuration == null) {
                return;
            }
            final ObscurationData data = new ObscurationData(event.refState, event.refVariant, event.refSide, event.refColorMultipliers);
            if (obscuration.applyObscuration(event.side, data)) {
                event.setCanceled(event.applied = true);
            }
        }
    }
    
    public static class ObscurationData
    {
        public final IBlockState state;
        public final String variant;
        public final EnumFacing side;
        public final int[] colorMultipliers;
        
        public ObscurationData(final IBlockState state, final String variant, final EnumFacing side, final int[] colorMultipliers) {
            this.state = state;
            this.variant = variant;
            this.side = side;
            this.colorMultipliers = colorMultipliers;
        }
        
        @Override
        public boolean equals(final Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof ObscurationData)) {
                return false;
            }
            final ObscurationData o = (ObscurationData)obj;
            return o.state.equals(this.state) && o.variant.equals(this.variant) && o.side == this.side && Arrays.equals(o.colorMultipliers, this.colorMultipliers);
        }
        
        @Override
        public int hashCode() {
            return (this.state.hashCode() * 7 + this.side.ordinal()) * 23;
        }
        
        public ObscurationData intern() {
            return this;
        }
    }
}
