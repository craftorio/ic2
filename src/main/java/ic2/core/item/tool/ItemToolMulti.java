package ic2.core.item.tool;

import ic2.api.item.ICustomDamageItem;
import ic2.core.block.state.EnumProperty;
import ic2.core.block.state.IIdProvider;
import ic2.core.item.ItemIC2;
import ic2.core.item.ItemMulti;
import ic2.core.item.ItemToolIC2;
import ic2.core.ref.IMultiItem;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemToolMulti<T extends Enum<T> & IIdProvider> extends ItemToolIC2 implements IMultiItem<T>, ICustomDamageItem
{
	protected final EnumProperty<T> typeProperty;
	private final Map<T, ItemMulti.IItemRightClickHandler> rightClickHandlers = new IdentityHashMap<>();
	private final Map<T, ItemMulti.IItemUseHandler> useHandlers = new IdentityHashMap<>();
	private final Map<T, ItemMulti.IItemUpdateHandler> updateHandlers = new IdentityHashMap<>();
	private final Map<T, EnumRarity> rarityFilter = new IdentityHashMap<>();

	public static <T extends Enum<T> & IIdProvider> ItemToolMulti<T> create(
		ItemName name, Class<T> typeClass, float damage, float speed, HarvestLevel harvestLevel, Set<? extends IToolClass> toolClasses, Set<Block> mineableBlocks
	)
	{
		EnumProperty<T> typeProperty = new EnumProperty<>("type", typeClass);
		if (typeProperty.getAllowedValues().size() > 32767)
		{
			throw new IllegalArgumentException("Too many values to fit in a short for " + typeClass);
		} else
		{
			return new ItemToolMulti<>(name, typeProperty, damage, speed, harvestLevel, toolClasses, mineableBlocks);
		}
	}

	private ItemToolMulti(
		ItemName name,
		EnumProperty<T> typeProperty,
		float damage,
		float speed,
		HarvestLevel harvestLevel,
		Set<? extends IToolClass> toolClasses,
		Set<Block> mineableBlocks
	)
	{
		super(name, damage, speed, harvestLevel, toolClasses, mineableBlocks);
		this.typeProperty = typeProperty;
		this.setHasSubtypes(true);
	}

	protected ItemToolMulti(ItemName name, Class<T> typeClass, HarvestLevel harvestLevel, Set<? extends IToolClass> toolClasses)
	{
		this(name, typeClass, harvestLevel, toolClasses, new HashSet<>());
	}

	protected ItemToolMulti(ItemName name, Class<T> typeClass, HarvestLevel harvestLevel, Set<? extends IToolClass> toolClasses, Set<Block> mineableBlocks)
	{
		this(name, typeClass, 0.0F, 0.0F, harvestLevel, toolClasses, mineableBlocks);
	}

	protected ItemToolMulti(
		ItemName name, Class<T> typeClass, float damage, float speed, HarvestLevel harvestLevel, Set<? extends IToolClass> toolClasses, Set<Block> mineableBlocks
	)
	{
		this(name, new EnumProperty<>("type", typeClass), damage, speed, harvestLevel, toolClasses, mineableBlocks);
	}

	@Override
	public final String getUnlocalizedName(ItemStack stack)
	{
		T type = this.getType(stack);
		return type == null ? super.getUnlocalizedName(stack) : super.getUnlocalizedName(stack) + "." + type.getName();
	}

	public final void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems)
	{
		if (this.isInCreativeTab(tab))
		{
			for (T type : this.typeProperty.getShownValues())
			{
				subItems.add(this.getItemStackUnchecked(type));
			}
		}
	}

	@Override
	public EnumRarity getRarity(ItemStack stack)
	{
		EnumRarity rarity = this.rarityFilter.get(this.getType(stack));
		return rarity != null ? rarity : super.getRarity(stack);
	}

	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
	{
		ItemStack stack = StackUtil.get(player, hand);
		T type = this.getType(stack);
		if (type == null)
		{
			return new ActionResult(EnumActionResult.PASS, stack);
		}

		ItemMulti.IItemRightClickHandler handler = this.rightClickHandlers.get(type);
		return handler == null ? new ActionResult(EnumActionResult.PASS, stack) : handler.onRightClick(stack, player, hand);
	}

	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		ItemStack stack = StackUtil.get(player, hand);
		T type = this.getType(stack);
		if (type == null)
		{
			return EnumActionResult.PASS;
		}

		ItemMulti.IItemUseHandler handler = this.useHandlers.get(type);
		return handler == null ? EnumActionResult.PASS : handler.onUse(stack, player, pos, hand, side);
	}

	public void onUpdate(ItemStack stack, World world, Entity entity, int slotIndex, boolean isCurrentItem)
	{
		T type = this.getType(stack);
		if (type != null)
		{
			ItemMulti.IItemUpdateHandler handler = this.updateHandlers.get(type);
			if (handler != null)
			{
				handler.onUpdate(stack, world, entity, slotIndex, isCurrentItem);
			}
		}
	}

	public boolean showDurabilityBar(ItemStack stack)
	{
		return true;
	}

	public double getDurabilityForDisplay(ItemStack stack)
	{
		return (double) this.getCustomDamage(stack) / this.getMaxCustomDamage(stack);
	}

	public boolean isDamageable()
	{
		return true;
	}

	public boolean isDamaged(ItemStack stack)
	{
		return this.getCustomDamage(stack) > 0;
	}

	public int getDamage(ItemStack stack)
	{
		return this.getCustomDamage(stack);
	}

	public int getMaxDamage(ItemStack stack)
	{
		return this.getMaxCustomDamage(stack);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerModels(ItemName name)
	{
		for (T type : this.typeProperty.getAllowedValues())
		{
			ItemIC2.registerModel(this, type.getId(), name, type.getModelName());
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public final int getItemColor(ItemStack stack, int tintIndex)
	{
		T type = this.getType(stack);
		return type == null ? super.getItemColor(stack, tintIndex) : type.getColor();
	}

	public ItemStack getItemStack(T type)
	{
		if (!this.typeProperty.getAllowedValues().contains(type))
		{
			throw new IllegalArgumentException("Invalid property value " + type + " for property " + this.typeProperty);
		} else
		{
			return this.getItemStackUnchecked(type);
		}
	}

	@Override
	public ItemStack getItemStack(String variant)
	{
		T type = this.typeProperty.getValue(variant);
		if (type == null)
		{
			throw new IllegalArgumentException("Invalid variant " + variant + " for " + this);
		} else
		{
			return this.getItemStackUnchecked(type);
		}
	}

	@Override
	public String getVariant(ItemStack stack)
	{
		if (stack == null)
		{
			throw new NullPointerException("The stack cannot be null");
		} else if (stack.getItem() != this)
		{
			throw new IllegalArgumentException("The stack " + stack + " does not match " + this);
		} else
		{
			T type = this.getType(stack);
			if (type == null)
			{
				throw new IllegalArgumentException("The stack " + stack + " does not reference any valid subtype");
			} else
			{
				return type.getName();
			}
		}
	}

	@Override
	public Set<T> getAllTypes()
	{
		return EnumSet.allOf(this.typeProperty.getValueClass());
	}

	@Override
	public int getCustomDamage(ItemStack stack)
	{
		if (!stack.hasTagCompound())
		{
			return 0;
		}

		NBTTagCompound data = stack.getTagCompound();
		assert data != null;
		return data.hasKey("durability") ? data.getInteger("durability") : 0;
	}

	@Override
	public int getMaxCustomDamage(ItemStack stack)
	{
		if (!stack.hasTagCompound())
		{
			return 0;
		}

		NBTTagCompound data = stack.getTagCompound();
		assert data != null;
		return data.hasKey("maxDurability") ? data.getInteger("maxDurability") : 0;
	}

	@Override
	public void setCustomDamage(ItemStack stack, int damage)
	{
		NBTTagCompound data = StackUtil.getOrCreateNbtData(stack);
		data.setInteger("durability", damage);
	}

	@Override
	public boolean applyCustomDamage(ItemStack stack, int damage, EntityLivingBase source)
	{
		this.setCustomDamage(stack, this.getCustomDamage(stack) + damage);
		return true;
	}

	public final T getType(ItemStack stack)
	{
		return this.typeProperty.getValue(stack.getMetadata());
	}

	public void setRightClickHandler(T type, ItemMulti.IItemRightClickHandler handler)
	{
		if (type == null)
		{
			for (T cType : this.typeProperty.getAllowedValues())
			{
				this.setRightClickHandler(cType, handler);
			}
		} else
		{
			this.rightClickHandlers.put(type, handler);
		}
	}

	public void setUseHandler(T type, ItemMulti.IItemUseHandler handler)
	{
		if (type == null)
		{
			for (T cType : this.typeProperty.getAllowedValues())
			{
				this.setUseHandler(cType, handler);
			}
		} else
		{
			this.useHandlers.put(type, handler);
		}
	}

	public void setUpdateHandler(T type, ItemMulti.IItemUpdateHandler handler)
	{
		if (type == null)
		{
			for (T cType : this.typeProperty.getAllowedValues())
			{
				this.setUpdateHandler(cType, handler);
			}
		} else
		{
			this.updateHandlers.put(type, handler);
		}
	}

	public void setRarity(T type, EnumRarity rarity)
	{
		if (type == null)
		{
			this.setRarity(rarity);
		} else
		{
			this.rarityFilter.put(type, rarity);
		}
	}

	private ItemStack getItemStackUnchecked(T type)
	{
		return new ItemStack(this, 1, type.getId());
	}
}
