package ic2.forge.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import ic2.core.block.wiring.CableFoam;
import ic2.core.block.wiring.CableType;
import net.minecraftforge.client.model.geometry.IGeometryLoader;

public final class CableModelLoader implements IGeometryLoader<Ic2Model>
{
	public Ic2Model read(JsonObject obj, JsonDeserializationContext context)
	{
		CableType type = CableType.valueOf(obj.get("type").getAsString());
		int insulation = obj.get("insulation").getAsInt();
		CableFoam foam = CableFoam.get(obj.get("foam").getAsString());
		boolean active = obj.get("active").getAsBoolean();
		return new DynamicCableModelForge(type, insulation, foam, active);
	}
}
