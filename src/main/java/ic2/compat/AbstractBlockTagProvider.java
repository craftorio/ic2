package ic2.compat;

import ic2.forge.FmlMod;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;

public abstract class AbstractBlockTagProvider extends BlockTagsProvider
{
	public AbstractBlockTagProvider(DataGenerator arg)
	{
		super(arg, "ic2", FmlMod.existingFileHelper);
	}
}
