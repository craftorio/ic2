package ic2.compat;

import ic2.forge.FmlMod;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;

public abstract class AbstractItemTagProvider extends ItemTagsProvider
{
	public AbstractItemTagProvider(DataGenerator root, BlockTagsProvider blockTagsProvider)
	{
		super(root, blockTagsProvider, "ic2", FmlMod.existingFileHelper);
	}
}
