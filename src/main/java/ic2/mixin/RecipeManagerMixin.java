package ic2.mixin;

import com.google.gson.JsonObject;
import ic2.core.IC2;
import ic2.core.util.LogCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// We use this to check THE FUCKING CREATE MOD is incompatible
@Mixin(RecipeManager.class)
public class RecipeManagerMixin
{
	@Inject(method = "fromJson(Lnet/minecraft/resources/ResourceLocation;Lcom/google/gson/JsonObject;)Lnet/minecraft/world/item/crafting/Recipe;", at = @At("HEAD"))
	private static void logRecipeId(ResourceLocation p_44046_, com.google.gson.JsonObject p_44047_, CallbackInfoReturnable<Recipe<?>> cir)
	{
		IC2.log.debug(LogCategory.Recipe, "[IC2 Recipe Debug] Loading recipe: %s", p_44046_);
	}
	
	@Inject(method = "fromJson(Lnet/minecraft/resources/ResourceLocation;Lcom/google/gson/JsonObject;Lnet/minecraftforge/common/crafting/conditions/ICondition$IContext;)Lnet/minecraft/world/item/crafting/Recipe;", at = @At("HEAD"))
	private static void logRecipeId(ResourceLocation p_44046_, JsonObject p_44047_, ICondition.IContext context, CallbackInfoReturnable<Recipe<?>> cir)
	{
		IC2.log.info(LogCategory.Recipe, "[IC2 Recipe Debug] Loading recipe: %s", p_44046_);
	}
}
