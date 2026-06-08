package ic2.data.loot_tables.generator;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import ic2.core.loot.Ic2BlockNbtProvider;

import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds.Ints;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer.Builder;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.CopyNameFunction;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.CopyNameFunction.NameSource;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

public abstract class Ic2BlockLootTableGenerator implements Consumer<BiConsumer<ResourceLocation, net.minecraft.world.level.storage.loot.LootTable.Builder>>
{
	private final Map<ResourceLocation, net.minecraft.world.level.storage.loot.LootTable.Builder> lootTables = Maps.newHashMap();
	private static final net.minecraft.world.level.storage.loot.predicates.LootItemCondition.Builder WITH_SILK_TOUCH = MatchTool.toolMatches(
		net.minecraft.advancements.critereon.ItemPredicate.Builder.item().hasEnchantment(new EnchantmentPredicate(Enchantments.SILK_TOUCH, Ints.atLeast(1)))
	);
	private static final net.minecraft.world.level.storage.loot.predicates.LootItemCondition.Builder WITHOUT_SILK_TOUCH = WITH_SILK_TOUCH.invert();
	private static final net.minecraft.world.level.storage.loot.predicates.LootItemCondition.Builder WITH_SHEARS = MatchTool.toolMatches(
		net.minecraft.advancements.critereon.ItemPredicate.Builder.item().of(new ItemLike[] { Items.SHEARS })
	);
	private static final net.minecraft.world.level.storage.loot.predicates.LootItemCondition.Builder WITH_SILK_TOUCH_OR_SHEARS = WITH_SHEARS.or(
		WITH_SILK_TOUCH
	);
	private static final net.minecraft.world.level.storage.loot.predicates.LootItemCondition.Builder WITHOUT_SILK_TOUCH_NOR_SHEARS = WITH_SILK_TOUCH_OR_SHEARS.invert();
	private static final float[] LEAVES_STICK_DROP_CHANCE = new float[] { 0.02F, 0.022222223F, 0.025F, 0.033333335F, 0.1F };
	private static final float[] SAPLING_DROP_CHANCE = new float[] { 0.05F, 0.0625F, 0.083333336F, 0.1F };
	private static final Set<Item> EXPLOSION_IMMUNE = Stream.of(
			Blocks.DRAGON_EGG,
			Blocks.BEACON,
			Blocks.CONDUIT,
			Blocks.SKELETON_SKULL,
			Blocks.WITHER_SKELETON_SKULL,
			Blocks.PLAYER_HEAD,
			Blocks.ZOMBIE_HEAD,
			Blocks.CREEPER_HEAD,
			Blocks.DRAGON_HEAD,
			Blocks.SHULKER_BOX,
			Blocks.BLACK_SHULKER_BOX,
			Blocks.BLUE_SHULKER_BOX,
			Blocks.BROWN_SHULKER_BOX,
			Blocks.CYAN_SHULKER_BOX,
			Blocks.GRAY_SHULKER_BOX,
			Blocks.GREEN_SHULKER_BOX,
			Blocks.LIGHT_BLUE_SHULKER_BOX,
			Blocks.LIGHT_GRAY_SHULKER_BOX,
			Blocks.LIME_SHULKER_BOX,
			Blocks.MAGENTA_SHULKER_BOX,
			Blocks.ORANGE_SHULKER_BOX,
			Blocks.PINK_SHULKER_BOX,
			Blocks.PURPLE_SHULKER_BOX,
			Blocks.RED_SHULKER_BOX,
			Blocks.WHITE_SHULKER_BOX,
			Blocks.YELLOW_SHULKER_BOX
		)
		.map(ItemLike::asItem)
		.collect(ImmutableSet.toImmutableSet());

	public Ic2BlockLootTableGenerator appendDrop(Block block)
	{
		this.addDrop(block);
		return this;
	}

	protected static <T extends FunctionUserBuilder<T>> T applyExplosionDecay(ItemLike drop, FunctionUserBuilder<T> builder)
	{
		return (T) (!EXPLOSION_IMMUNE.contains(drop.asItem()) ? builder.apply(ApplyExplosionDecay.explosionDecay()) : builder.unwrap());
	}

	protected static <T extends ConditionUserBuilder<T>> T addSurvivesExplosionCondition(ItemLike drop, ConditionUserBuilder<T> builder)
	{
		return (T) (!EXPLOSION_IMMUNE.contains(drop.asItem()) ? builder.when(ExplosionCondition.survivesExplosion()) : builder.unwrap());
	}

	protected static net.minecraft.world.level.storage.loot.LootTable.Builder drops(ItemLike drop)
	{
		return LootTable.lootTable()
			.withPool(addSurvivesExplosionCondition(drop, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(drop))));
	}

	protected static net.minecraft.world.level.storage.loot.LootTable.Builder drops(
		Block drop, net.minecraft.world.level.storage.loot.predicates.LootItemCondition.Builder conditionBuilder, Builder<?> child
	)
	{
		return LootTable.lootTable()
			.withPool(
				LootPool.lootPool()
					.setRolls(ConstantValue.exactly(1.0F))
					.add(
						((net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer.Builder) LootItem.lootTableItem(drop).when(conditionBuilder))
							.otherwise(child)
					)
			);
	}

	protected static net.minecraft.world.level.storage.loot.LootTable.Builder dropsWithSilkTouch(Block drop, Builder<?> child)
	{
		return drops(drop, WITH_SILK_TOUCH, child);
	}

	protected static net.minecraft.world.level.storage.loot.LootTable.Builder dropsWithShears(Block drop, Builder<?> child)
	{
		return drops(drop, WITH_SHEARS, child);
	}

	protected static net.minecraft.world.level.storage.loot.LootTable.Builder dropsWithSilkTouchOrShears(Block drop, Builder<?> child)
	{
		return drops(drop, WITH_SILK_TOUCH_OR_SHEARS, child);
	}

	protected static net.minecraft.world.level.storage.loot.LootTable.Builder drops(Block dropWithSilkTouch, ItemLike drop)
	{
		return dropsWithSilkTouch(dropWithSilkTouch, addSurvivesExplosionCondition(dropWithSilkTouch, LootItem.lootTableItem(drop)));
	}

	protected static net.minecraft.world.level.storage.loot.LootTable.Builder drops(ItemLike drop, NumberProvider count)
	{
		return LootTable.lootTable()
			.withPool(
				LootPool.lootPool()
					.setRolls(ConstantValue.exactly(1.0F))
					.add(applyExplosionDecay(drop, LootItem.lootTableItem(drop).apply(SetItemCountFunction.setCount(count))))
			);
	}

	protected static net.minecraft.world.level.storage.loot.LootTable.Builder drops(Block dropWithSilkTouch, ItemLike drop, NumberProvider count)
	{
		return dropsWithSilkTouch(
			dropWithSilkTouch, applyExplosionDecay(dropWithSilkTouch, LootItem.lootTableItem(drop).apply(SetItemCountFunction.setCount(count)))
		);
	}

	protected static net.minecraft.world.level.storage.loot.LootTable.Builder dropsWithSilkTouch(ItemLike drop)
	{
		return LootTable.lootTable()
			.withPool(LootPool.lootPool().when(WITH_SILK_TOUCH).setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(drop)));
	}

	protected static net.minecraft.world.level.storage.loot.LootTable.Builder dropsWithNbt(Block drop)
	{
		return LootTable.lootTable()
			.withPool(
				addSurvivesExplosionCondition(
					drop,
					LootPool.lootPool()
						.setRolls(ConstantValue.exactly(1.0F))
						.add(
							LootItem.lootTableItem(drop)
								.apply(CopyNameFunction.copyName(NameSource.BLOCK_ENTITY))
								.apply(
									CopyNbtFunction.copyData(Ic2BlockNbtProvider.BLOCK_NBT)
										.copy("Lock", "BlockEntityTag.Lock")
										.copy("LootTable", "BlockEntityTag.LootTable")
										.copy("LootTableSeed", "BlockEntityTag.LootTableSeed")
								)
						)
				)
			);
	}

	public void addDrop(Block block)
	{
		this.addDrop(block, block);
	}

	public void addDrop(Block block, ItemLike drop)
	{
		this.addDrop(block, drops(drop));
	}

	private void addDrop(Block block, net.minecraft.world.level.storage.loot.LootTable.Builder lootTable)
	{
		this.lootTables.put(block.getLootTable(), lootTable);
	}

	public void addDropWithSilkTouch(Block block)
	{
		this.addDropWithSilkTouch(block, block);
	}

	public void addDropWithSilkTouch(Block block, Block drop)
	{
		this.addDrop(block, dropsWithSilkTouch(drop));
	}

	public void addDropWithNbt(Block block)
	{
		this.addDrop(block, dropsWithNbt(block));
	}

	public abstract Ic2BlockLootTableGenerator build();

	public void accept(BiConsumer<ResourceLocation, net.minecraft.world.level.storage.loot.LootTable.Builder> biConsumer)
	{
		this.lootTables.forEach(biConsumer);
	}
}
