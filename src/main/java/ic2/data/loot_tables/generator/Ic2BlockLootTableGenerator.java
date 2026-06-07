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
	private static final net.minecraft.world.level.storage.loot.predicates.LootItemCondition.Builder WITH_SILK_TOUCH = MatchTool.m_81997_(
		net.minecraft.advancements.critereon.ItemPredicate.Builder.m_45068_().m_45071_(new EnchantmentPredicate(Enchantments.f_44985_, Ints.m_55386_(1)))
	);
	private static final net.minecraft.world.level.storage.loot.predicates.LootItemCondition.Builder WITHOUT_SILK_TOUCH = WITH_SILK_TOUCH.m_81807_();
	private static final net.minecraft.world.level.storage.loot.predicates.LootItemCondition.Builder WITH_SHEARS = MatchTool.m_81997_(
		net.minecraft.advancements.critereon.ItemPredicate.Builder.m_45068_().m_151445_(new ItemLike[] { Items.f_42574_ })
	);
	private static final net.minecraft.world.level.storage.loot.predicates.LootItemCondition.Builder WITH_SILK_TOUCH_OR_SHEARS = WITH_SHEARS.m_7818_(
		WITH_SILK_TOUCH
	);
	private static final net.minecraft.world.level.storage.loot.predicates.LootItemCondition.Builder WITHOUT_SILK_TOUCH_NOR_SHEARS = WITH_SILK_TOUCH_OR_SHEARS.m_81807_();
	private static final float[] LEAVES_STICK_DROP_CHANCE = new float[] { 0.02F, 0.022222223F, 0.025F, 0.033333335F, 0.1F };
	private static final float[] SAPLING_DROP_CHANCE = new float[] { 0.05F, 0.0625F, 0.083333336F, 0.1F };
	private static final Set<Item> EXPLOSION_IMMUNE = Stream.of(
			Blocks.f_50260_,
			Blocks.f_50273_,
			Blocks.f_50569_,
			Blocks.f_50310_,
			Blocks.f_50312_,
			Blocks.f_50316_,
			Blocks.f_50314_,
			Blocks.f_50318_,
			Blocks.f_50320_,
			Blocks.f_50456_,
			Blocks.f_50525_,
			Blocks.f_50521_,
			Blocks.f_50522_,
			Blocks.f_50466_,
			Blocks.f_50464_,
			Blocks.f_50523_,
			Blocks.f_50460_,
			Blocks.f_50465_,
			Blocks.f_50462_,
			Blocks.f_50459_,
			Blocks.f_50458_,
			Blocks.f_50463_,
			Blocks.f_50520_,
			Blocks.f_50524_,
			Blocks.f_50457_,
			Blocks.f_50461_
		)
		.map(ItemLike::m_5456_)
		.collect(ImmutableSet.toImmutableSet());

	public Ic2BlockLootTableGenerator appendDrop(Block block)
	{
		this.addDrop(block);
		return this;
	}

	protected static <T extends FunctionUserBuilder<T>> T applyExplosionDecay(ItemLike drop, FunctionUserBuilder<T> builder)
	{
		return (T) (!EXPLOSION_IMMUNE.contains(drop.m_5456_()) ? builder.m_79078_(ApplyExplosionDecay.m_80037_()) : builder.m_79073_());
	}

	protected static <T extends ConditionUserBuilder<T>> T addSurvivesExplosionCondition(ItemLike drop, ConditionUserBuilder<T> builder)
	{
		return (T) (!EXPLOSION_IMMUNE.contains(drop.m_5456_()) ? builder.m_79080_(ExplosionCondition.m_81661_()) : builder.m_79073_());
	}

	protected static net.minecraft.world.level.storage.loot.LootTable.Builder drops(ItemLike drop)
	{
		return LootTable.m_79147_()
			.m_79161_(addSurvivesExplosionCondition(drop, LootPool.m_79043_().m_165133_(ConstantValue.m_165692_(1.0F)).m_79076_(LootItem.m_79579_(drop))));
	}

	protected static net.minecraft.world.level.storage.loot.LootTable.Builder drops(
		Block drop, net.minecraft.world.level.storage.loot.predicates.LootItemCondition.Builder conditionBuilder, Builder<?> child
	)
	{
		return LootTable.m_79147_()
			.m_79161_(
				LootPool.m_79043_()
					.m_165133_(ConstantValue.m_165692_(1.0F))
					.m_79076_(
						((net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer.Builder) LootItem.m_79579_(drop).m_79080_(conditionBuilder))
							.m_7170_(child)
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
		return dropsWithSilkTouch(dropWithSilkTouch, addSurvivesExplosionCondition(dropWithSilkTouch, LootItem.m_79579_(drop)));
	}

	protected static net.minecraft.world.level.storage.loot.LootTable.Builder drops(ItemLike drop, NumberProvider count)
	{
		return LootTable.m_79147_()
			.m_79161_(
				LootPool.m_79043_()
					.m_165133_(ConstantValue.m_165692_(1.0F))
					.m_79076_(applyExplosionDecay(drop, LootItem.m_79579_(drop).m_79078_(SetItemCountFunction.m_165412_(count))))
			);
	}

	protected static net.minecraft.world.level.storage.loot.LootTable.Builder drops(Block dropWithSilkTouch, ItemLike drop, NumberProvider count)
	{
		return dropsWithSilkTouch(
			dropWithSilkTouch, applyExplosionDecay(dropWithSilkTouch, LootItem.m_79579_(drop).m_79078_(SetItemCountFunction.m_165412_(count)))
		);
	}

	protected static net.minecraft.world.level.storage.loot.LootTable.Builder dropsWithSilkTouch(ItemLike drop)
	{
		return LootTable.m_79147_()
			.m_79161_(LootPool.m_79043_().m_79080_(WITH_SILK_TOUCH).m_165133_(ConstantValue.m_165692_(1.0F)).m_79076_(LootItem.m_79579_(drop)));
	}

	protected static net.minecraft.world.level.storage.loot.LootTable.Builder dropsWithNbt(Block drop)
	{
		return LootTable.m_79147_()
			.m_79161_(
				addSurvivesExplosionCondition(
					drop,
					LootPool.m_79043_()
						.m_165133_(ConstantValue.m_165692_(1.0F))
						.m_79076_(
							LootItem.m_79579_(drop)
								.m_79078_(CopyNameFunction.m_80187_(NameSource.BLOCK_ENTITY))
								.m_79078_(
									CopyNbtFunction.m_165180_(Ic2BlockNbtProvider.BLOCK_NBT)
										.m_80279_("Lock", "BlockEntityTag.Lock")
										.m_80279_("LootTable", "BlockEntityTag.LootTable")
										.m_80279_("LootTableSeed", "BlockEntityTag.LootTableSeed")
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
		this.lootTables.put(block.m_60589_(), lootTable);
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
