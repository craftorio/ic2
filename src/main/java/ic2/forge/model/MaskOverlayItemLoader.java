package ic2.forge.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryLoader;

public final class MaskOverlayItemLoader implements IGeometryLoader<Ic2Model>
{
	@Override
	public Ic2Model read(JsonObject obj, JsonDeserializationContext context)
	{
		ResourceLocation base = ResourceLocation.parse(obj.get("base").getAsString());
		ResourceLocation mask = ResourceLocation.parse(obj.get("mask").getAsString());
		boolean scaleOverlay = !obj.has("scale_overlay") || obj.get("scale_overlay").getAsBoolean();
		float offset = obj.has("offset") ? obj.get("offset").getAsFloat() : 0.001f;
		return new MaskOverlayItemModel(base, mask, scaleOverlay, offset);
	}
}
