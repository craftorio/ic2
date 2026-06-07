package ic2.forge.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryLoader;

public final class BeModelLoader implements IGeometryLoader<Ic2Model>
{
	public Ic2Model read(JsonObject obj, JsonDeserializationContext context)
	{
		ResourceLocation id = ResourceLocation.fromNamespaceAndPath(obj.get("id").getAsString());
		return new DynamicBeModelForge(id);
	}
}
