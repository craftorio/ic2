// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.apihelper;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import ic2.core.block.KineticGeneratorRenderer;
import ic2.api.tile.IRotorProvider;
import net.minecraft.tileentity.TileEntity;
import ic2.api.tile.RotorRegistry;
import ic2.api.network.NetworkHelper;
import ic2.core.IC2;
import ic2.api.network.INetworkManager;
import ic2.api.item.IItemAPI;
import ic2.api.item.IC2Items;
import ic2.api.info.Info;
import ic2.core.IC2DamageSource;

public class ApiHelper
{
    public static void preload() {
        Info.DMG_ELECTRIC = IC2DamageSource.electricity;
        Info.DMG_NUKE_EXPLOSION = IC2DamageSource.nuke;
        Info.DMG_RADIATION = IC2DamageSource.radiation;
        IC2Items.setInstance(new ItemAPI());
        NetworkHelper.setInstance(IC2.network.get(true), IC2.network.get(false));
        if (IC2.platform.isRendering()) {
            RotorRegistry.setInstance(new RotorRegistry.IRotorRegistry() {
                @Override
                public <T extends TileEntity & IRotorProvider> void registerRotorProvider(final Class<T> clazz) {
                    ClientRegistry.bindTileEntitySpecialRenderer((Class)clazz, (TileEntitySpecialRenderer)new KineticGeneratorRenderer<Object>());
                }
            });
        }
    }
}
