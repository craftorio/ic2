package ic2.data;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import ic2.data.recipe.BlastFurnaceRecipeProvider;
import ic2.data.recipe.BlockCutterRecipeProvider;
import ic2.data.recipe.CannerRecipeProvider;
import ic2.data.recipe.CentrifugeRecipeProvider;
import ic2.data.recipe.CompressorRecipeProvider;
import ic2.data.recipe.ExtractorRecipeProvider;
import ic2.data.recipe.MaceratorRecipeProvider;
import ic2.data.recipe.MatterFabricatorRecipeProvider;
import ic2.data.recipe.MetalFormerRecipeProvider;
import ic2.data.recipe.OreWasherRecipeProvider;
import ic2.data.recipe.ShapedRecipeProvider;
import ic2.data.recipe.ShapelessRecipeProvider;
import ic2.data.tag.Ic2BlockTagProvider;
import ic2.data.tag.Ic2ItemTagProvider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;

public class Ic2DataGenerators
{
	public static void setup(DataGenerator generator, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper)
	{
		PackOutput output = generator.getPackOutput();
		Ic2BlockTagProvider blockTags = new Ic2BlockTagProvider(output, lookupProvider, existingFileHelper);
		generator.addProvider(true, blockTags);
		generator.addProvider(true, new Ic2ItemTagProvider(output, lookupProvider, blockTags.contentsGetter(), existingFileHelper));
		generator.addProvider(true, new BlastFurnaceRecipeProvider(output));
		generator.addProvider(true, new BlockCutterRecipeProvider(output));
		generator.addProvider(true, new CannerRecipeProvider(output));
		generator.addProvider(true, new CentrifugeRecipeProvider(output));
		generator.addProvider(true, new CompressorRecipeProvider(output));
		generator.addProvider(true, new ExtractorRecipeProvider(output));
		generator.addProvider(true, new MaceratorRecipeProvider(output));
		generator.addProvider(true, new MatterFabricatorRecipeProvider(output));
		generator.addProvider(true, new MetalFormerRecipeProvider(output));
		generator.addProvider(true, new OreWasherRecipeProvider(output));
		generator.addProvider(true, new ShapedRecipeProvider(output));
		generator.addProvider(true, new ShapelessRecipeProvider(output));
	}

	public static void saveJsonPreserveOrder(Gson gson, CachedOutput writer, JsonElement jsonElement, Path path) throws IOException
	{
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		HashingOutputStream hashingOutputStream = new HashingOutputStream(Hashing.sha256(), byteArrayOutputStream);
		Writer writer2 = new OutputStreamWriter(hashingOutputStream, StandardCharsets.UTF_8);
		writer2.write(gson.toJson(jsonElement));
		writer2.close();
		writer.writeIfNeeded(path, byteArrayOutputStream.toByteArray(), hashingOutputStream.hash());
	}
}
