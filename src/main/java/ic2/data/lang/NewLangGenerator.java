package ic2.data.lang;

import ic2.core.IC2;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Items;
import it.unimi.dsi.fastutil.Pair;

import java.util.List;

import net.minecraft.core.Registry;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class NewLangGenerator
{
	private final String lang;
	private final List<Pair<String, String>> langPairList;

	public NewLangGenerator(String lang, List<Pair<String, String>> langPairList)
	{
		this.lang = lang;
		this.langPairList = langPairList;
	}

	public void generate()
	{
		switch (this.lang)
		{
			case "zh_cn":
				this.appendBlockText(Ic2Blocks.STRIPPED_RUBBER_WOOD, "去皮橡胶木")
					.appendBlockText(Ic2Blocks.STRIPPED_RUBBER_LOG, "去皮橡胶原木")
					.appendBlockText(Ic2Blocks.RUBBER_WOOD, "橡胶木")
					.appendBlockText(Ic2Blocks.RUBBER_BUTTON, "橡胶木按钮")
					.appendBlockText(Ic2Blocks.RUBBER_DOOR, "橡胶木门")
					.appendBlockText(Ic2Blocks.RUBBER_FENCE, "橡胶木栅栏")
					.appendBlockText(Ic2Blocks.RUBBER_PLANKS, "橡胶木板")
					.appendBlockText(Ic2Blocks.RUBBER_SIGN, "橡胶木告示牌")
					.appendBlockText(Ic2Blocks.RUBBER_FENCE_GATE, "橡胶木栅栏门")
					.appendBlockText(Ic2Blocks.RUBBER_SLAB, "橡胶木台阶")
					.appendBlockText(Ic2Blocks.RUBBER_STAIRS, "橡胶木楼梯")
					.appendBlockText(Ic2Blocks.RUBBER_TRAPDOOR, "橡胶木活板门")
					.appendBlockText(Ic2Blocks.RUBBER_PRESSURE_PLATE, "橡胶木压力板")
					.appendBlockText(Ic2Blocks.DEEPSLATE_LEAD_ORE, "深层铅矿石")
					.appendBlockText(Ic2Blocks.DEEPSLATE_TIN_ORE, "深层锡矿石")
					.appendBlockText(Ic2Blocks.DEEPSLATE_URANIUM_ORE, "深层铀矿石")
					.appendBlockText(Ic2Blocks.RAW_LEAD_BLOCK, "粗铅块")
					.appendBlockText(Ic2Blocks.RAW_TIN_BLOCK, "粗锡块")
					.appendBlockText(Ic2Blocks.RAW_URANIUM_BLOCK, "粗铀块")
					.appendItemText(Ic2Items.RAW_LEAD, "粗铅")
					.appendItemText(Ic2Items.RAW_TIN, "粗锡")
					.appendItemText(Ic2Items.RAW_URANIUM, "粗铀")
					.appendItemGroupText(IC2.tabIc2General, "工业2常规")
					.appendItemGroupText(IC2.tabIc2GeneratorsAndWiring, "工业2发电机与线路")
					.appendItemGroupText(IC2.tabIc2Machines, "工业2机器")
					.appendItemGroupText(IC2.tabIc2Farming, "工业2农业")
					.appendItemGroupText(IC2.tabIc2ToolsAndUtilities, "工业2工具与实用物品")
					.appendItemGroupText(IC2.tabIc2Combat, "工业2战斗用品")
					.appendItemGroupText(IC2.tabIc2Materials, "工业2原材料")
					.appendItemGroupText(IC2.tabIc2Reactor, "工业2反应堆");
				break;
			case "en_us":
				this.appendBlockText(Ic2Blocks.STRIPPED_RUBBER_WOOD, "Stripped Rubber Wood")
					.appendBlockText(Ic2Blocks.STRIPPED_RUBBER_LOG, "Stripped Rubber Log")
					.appendBlockText(Ic2Blocks.RUBBER_WOOD, "Rubber Wood")
					.appendBlockText(Ic2Blocks.RUBBER_BUTTON, "Rubber Button")
					.appendBlockText(Ic2Blocks.RUBBER_DOOR, "Rubber Door")
					.appendBlockText(Ic2Blocks.RUBBER_FENCE, "Rubber Fence")
					.appendBlockText(Ic2Blocks.RUBBER_PLANKS, "Rubber Planks")
					.appendBlockText(Ic2Blocks.RUBBER_SIGN, "Rubber Sign")
					.appendBlockText(Ic2Blocks.RUBBER_FENCE_GATE, "Rubber Fence Gate")
					.appendBlockText(Ic2Blocks.RUBBER_SLAB, "Rubber Slab")
					.appendBlockText(Ic2Blocks.RUBBER_STAIRS, "Rubber Stairs")
					.appendBlockText(Ic2Blocks.RUBBER_TRAPDOOR, "Rubber Trapdoor")
					.appendBlockText(Ic2Blocks.RUBBER_PRESSURE_PLATE, "Rubber Pressure Plate")
					.appendBlockText(Ic2Blocks.DEEPSLATE_LEAD_ORE, "Deepslate Lead Ore")
					.appendBlockText(Ic2Blocks.DEEPSLATE_TIN_ORE, "Deepslate Tin Ore")
					.appendBlockText(Ic2Blocks.DEEPSLATE_URANIUM_ORE, "Deepslate Uranium Ore")
					.appendBlockText(Ic2Blocks.RAW_LEAD_BLOCK, "Raw Lead Block")
					.appendBlockText(Ic2Blocks.RAW_TIN_BLOCK, "Raw Tin Block")
					.appendBlockText(Ic2Blocks.RAW_URANIUM_BLOCK, "Raw Uranium Block")
					.appendItemText(Ic2Items.RAW_LEAD, "Raw Lead")
					.appendItemText(Ic2Items.RAW_TIN, "Raw Tin")
					.appendItemText(Ic2Items.RAW_URANIUM, "Raw Uranium")
					.appendItemGroupText(IC2.tabIc2General, "IC2 General")
					.appendItemGroupText(IC2.tabIc2GeneratorsAndWiring, "IC2 Generators And Wiring")
					.appendItemGroupText(IC2.tabIc2Machines, "IC2 Machines")
					.appendItemGroupText(IC2.tabIc2Farming, "IC2 Farming")
					.appendItemGroupText(IC2.tabIc2ToolsAndUtilities, "IC2 Tools And Utilities")
					.appendItemGroupText(IC2.tabIc2Combat, "IC2 Combat")
					.appendItemGroupText(IC2.tabIc2Materials, "IC2 Materials")
					.appendItemGroupText(IC2.tabIc2Reactor, "IC2 Reactor");
		}
	}

	public boolean overrideOldTranslation(String oldKey, String newKey, String translation)
	{
		if (this.lang.equals("zh_cn"))
		{
			return switch (oldKey)
			{
				case "rubber_wood" -> this.overrideLang(newKey, "橡胶原木");
				case "leaves.rubber" -> this.overrideLang(newKey, "橡胶树叶");
				case "sapling.rubber" -> this.overrideLang(newKey, "橡胶树苗");
				default -> false;
			};
		} else
		{
			return false;
		}
	}

	private boolean overrideLang(String key, String text)
	{
		this.append(key, text);
		return true;
	}

	private NewLangGenerator append(String key, String text)
	{
		this.langPairList.add(Pair.of(key, text));
		return this;
	}

	private NewLangGenerator appendBlockText(Block block, String text)
	{
		String blockKey = Registry.BLOCK.getKey(block).m_214298_();
		return this.append("block." + blockKey, text);
	}

	private NewLangGenerator appendItemText(Item item, String text)
	{
		String itemKey = Registry.f_122827_.getKey(item).m_214298_();
		return this.append("item." + itemKey, text);
	}

	private NewLangGenerator appendItemGroupText(CreativeModeTab group, String text)
	{
		String groupKey = group.m_40783_();
		return this.append("itemGroup." + groupKey, text);
	}
}
