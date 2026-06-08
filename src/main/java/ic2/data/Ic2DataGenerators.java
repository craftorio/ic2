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

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;

public class Ic2DataGenerators
{
	public static void setup(DataGenerator generator)
	{
		generator.addProvider(true, new Ic2ItemTagProvider(generator));
		generator.addProvider(true, new Ic2BlockTagProvider(generator));
		generator.addProvider(true, new BlastFurnaceRecipeProvider(generator));
		generator.addProvider(true, new BlockCutterRecipeProvider(generator));
		generator.addProvider(true, new CannerRecipeProvider(generator));
		generator.addProvider(true, new CentrifugeRecipeProvider(generator));
		generator.addProvider(true, new CompressorRecipeProvider(generator));
		generator.addProvider(true, new ExtractorRecipeProvider(generator));
		generator.addProvider(true, new MaceratorRecipeProvider(generator));
		generator.addProvider(true, new MatterFabricatorRecipeProvider(generator));
		generator.addProvider(true, new MetalFormerRecipeProvider(generator));
		generator.addProvider(true, new OreWasherRecipeProvider(generator));
		generator.addProvider(true, new ShapedRecipeProvider(generator));
		generator.addProvider(true, new ShapelessRecipeProvider(generator));
	}

	public static void saveJsonPreserveOrder(Gson gson, CachedOutput writer, JsonElement jsonElement, Path path) throws IOException
	{
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		HashingOutputStream hashingOutputStream = new HashingOutputStream(Hashing.sha1(), byteArrayOutputStream);
		Writer writer2 = new OutputStreamWriter(hashingOutputStream, StandardCharsets.UTF_8);
		writer2.write(gson.toJson(jsonElement));
		writer2.close();
		writer.writeIfNeeded(path, byteArrayOutputStream.toByteArray(), hashingOutputStream.hash());
	}
}
