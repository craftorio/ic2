package ic2.forge.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraftforge.client.model.geometry.IGeometryLoader;

public final class WallModelLoader implements IGeometryLoader<Ic2Model>
{
	@Override
	public Ic2Model read(JsonObject obj, JsonDeserializationContext context)
	{
		return new WallModelForge();
	}
}