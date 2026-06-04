// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.init;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.world.storage.loot.LootPool;
import ic2.core.util.LogCategory;
import ic2.core.IC2;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.common.MinecraftForge;

public class Ic2Loot
{
    public static void init() {
        new Ic2Loot();
    }
    
    private Ic2Loot() {
        MinecraftForge.EVENT_BUS.register((Object)this);
    }
    
    @SubscribeEvent
    public void onLootTableLoad(final LootTableLoadEvent event) {
        try {
            if (!event.getName().getResourceDomain().equals("minecraft")) {
                return;
            }
            if (this.getClass().getResource("/assets/ic2/loot_tables/" + event.getName().getResourcePath() + ".json") == null) {
                return;
            }
            final LootTable table = event.getLootTableManager().getLootTableFromLocation(new ResourceLocation("ic2", event.getName().getResourcePath()));
            if (table == null || table == LootTable.EMPTY_LOOT_TABLE) {
                return;
            }
            final LootPool pool = table.getPool("ic2");
            if (pool == null) {
                return;
            }
            event.getTable().addPool(pool);
        }
        catch (final Throwable t) {
            IC2.log.warn(LogCategory.General, t, "Error loading loot table %s.", event.getName().getResourcePath());
        }
    }
}
