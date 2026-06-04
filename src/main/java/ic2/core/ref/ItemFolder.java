// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.ref;

public enum ItemFolder
{
    root((String)null), 
    armor, 
    battery, 
    bcTrigger, 
    boat, 
    brewing, 
    cable, 
    cell, 
    crafting, 
    crop, 
    pipe, 
    reactor, 
    reactorFuelRod("reactor/fuel_rod"), 
    resource, 
    resourceCasing("resource/casing"), 
    resourceCrushed("resource/crushed"), 
    resourceDust("resource/dust"), 
    resourceIngot("resource/ingot"), 
    resourceNuclear("resource/nuclear"), 
    resourcePlate("resource/plate"), 
    resourcePurified("resource/purified"), 
    rotor, 
    tfbp, 
    tool, 
    toolElectric("tool/electric"), 
    toolPainter("tool/painter"), 
    turnable, 
    upgrade;
    
    final String path;
    
    private ItemFolder() {
        this.path = this.name();
    }
    
    private ItemFolder(final String path) {
        this.path = path;
    }
}
