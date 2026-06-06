package ic2.core.item.tool;

import ic2.core.IC2;
import ic2.core.init.BlocksItems;
import ic2.core.init.Localization;
import ic2.core.item.ItemIC2;
import ic2.core.ref.IItemModelProvider;
import ic2.core.ref.ItemName;
import ic2.core.util.Util;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class Ic2Hoe extends ItemHoe implements IItemModelProvider
{
	private final Object repairMaterial = "ingotBronze";

	public Ic2Hoe(ToolMaterial material)
	{
		super(material);
		this.setUnlocalizedName(ItemName.bronze_hoe.name());
		this.setCreativeTab(IC2.tabIC2);
		BlocksItems.registerItem(this, IC2.getIdentifier(ItemName.bronze_hoe.name()));
		ItemName.bronze_hoe.setInstance(this);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerModels(ItemName name)
	{
		ItemIC2.registerModel(this, 0, name, null);
	}

	public String getUnlocalizedName()
	{
		return "ic2." + super.getUnlocalizedName().substring(5);
	}

	public String getUnlocalizedName(ItemStack stack)
	{
		return this.getUnlocalizedName();
	}

	public String getUnlocalizedNameInefficiently(ItemStack stack)
	{
		return this.getUnlocalizedName(stack);
	}

	public String getItemStackDisplayName(ItemStack stack)
	{
		return Localization.translate(this.getUnlocalizedName(stack));
	}

	public boolean getIsRepairable(ItemStack stack1, ItemStack stack2)
	{
		return stack2 != null && Util.matchesOD(stack2, this.repairMaterial);
	}
}
