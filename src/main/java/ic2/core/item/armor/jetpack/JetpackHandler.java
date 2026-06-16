package ic2.core.item.armor.jetpack;

import ic2.api.item.ElectricItem;
import ic2.api.item.IBackupElectricItemManager;
import ic2.api.item.IElectricItem;
import ic2.core.init.Localization;
import ic2.core.ref.Ic2Items;
import ic2.core.util.ReflectionUtil;
import ic2.core.util.StackUtil;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;

public class JetpackHandler implements IBackupElectricItemManager
{
	private static final Map<Player, ItemStack> playerArmorBuffer = new WeakHashMap<>();
	@OnlyIn(Dist.CLIENT)
	private static LayerJetpackOverride render;
	@OnlyIn(Dist.CLIENT)
	private static Field renderLayers;
	private boolean internalHandlesCheck = false;
	static final ItemStack jetpack = new ItemStack(Ic2Items.JETPACK_ELECTRIC);
	public static JetpackHandler instance;

	private JetpackHandler()
	{
		MinecraftForge.EVENT_BUS.register(this);
		ElectricItem.registerBackupManager(this);
	}

	public static void init()
	{
		instance = new JetpackHandler();
	}

	public static void onPlayerTick(Player player)
	{
		ItemStack stack = player.getItemBySlot(EquipmentSlot.CHEST);
		if (hasJetpack(stack))
		{
			JetpackLogic.onArmorTick(player.level(), player, stack, getJetpack(stack));
		}
		else
		{
			JetpackLogic.stopJetpackSound(player);
		}
	}

	public static void setJetpackAttached(ItemStack stack, boolean value)
	{
		if (StackUtil.isEmpty(stack))
		{
			return;
		}

		if (!value)
		{
			if (!stack.hasTag())
			{
				return;
			}

			stack.getTag().remove("hasIC2Jetpack");
			if (stack.getTag().isEmpty())
			{
				stack.setTag(null);
			}
		}
		else if (Mob.getEquipmentSlotForItem(stack) == EquipmentSlot.CHEST)
		{
			StackUtil.getOrCreateNbtData(stack).putBoolean("hasIC2Jetpack", true);
		}
	}

	public static boolean hasJetpackAttached(ItemStack stack)
	{
		return !StackUtil.isEmpty(stack)
			&& Mob.getEquipmentSlotForItem(stack) == EquipmentSlot.CHEST
			&& stack.hasTag()
			&& stack.getTag().getBoolean("hasIC2Jetpack");
	}

	public static boolean hasJetpack(ItemStack stack)
	{
		return !StackUtil.isEmpty(stack) && (hasJetpackAttached(stack) || stack.getItem() instanceof IJetpack);
	}

	public static IJetpack getJetpack(ItemStack stack)
	{
		assert hasJetpack(stack);
		return stack.getItem() instanceof IJetpack ? (IJetpack) stack.getItem() : (IJetpack) jetpack.getItem();
	}

	public static double getTransferLimit()
	{
		return ((IElectricItem) jetpack.getItem()).getTransferLimit(jetpack);
	}

	@Override
	public double charge(ItemStack stack, double amount, int tier, boolean ignoreTransferLimit, boolean simulate)
	{
		if (this.getTier(stack) > tier)
		{
			return 0.0;
		}

		if (!ignoreTransferLimit)
		{
			amount = Math.min(amount, getTransferLimit());
		}

		double charge = stack.hasTag() ? stack.getTag().getDouble("charge") : 0.0;
		amount = Math.min(amount, this.getMaxCharge(stack) - charge);
		if (!simulate)
		{
			StackUtil.getOrCreateNbtData(stack).putDouble("charge", charge + amount);
		}

		return amount;
	}

	@Override
	public double discharge(ItemStack stack, double amount, int tier, boolean ignoreTransferLimit, boolean externally, boolean simulate)
	{
		if (!externally && this.getTier(stack) <= tier && stack.hasTag())
		{
			if (!ignoreTransferLimit)
			{
				amount = Math.min(amount, getTransferLimit());
			}

			double charge = stack.getTag().getDouble("charge");
			amount = Math.min(amount, charge);
			if (!simulate)
			{
				charge -= amount;
				if (charge == 0.0)
				{
					stack.getTag().remove("charge");
					if (stack.getTag().isEmpty())
					{
						stack.setTag(null);
					}
				}
				else
				{
					stack.getTag().putDouble("charge", charge);
				}
			}

			return amount;
		}
		else
		{
			return 0.0;
		}
	}

	@Override
	public double getCharge(ItemStack stack)
	{
		return this.discharge(stack, Double.MAX_VALUE, Integer.MAX_VALUE, true, false, true);
	}

	@Override
	public double getStackCharge(ItemStack stack)
	{
		return this.getCharge(stack);
	}

	@Override
	public double getMaxCharge(ItemStack stack)
	{
		return ElectricItem.manager.getMaxCharge(jetpack.copy());
	}

	@Override
	public boolean canUse(ItemStack stack, double amount)
	{
		return ElectricItem.rawManager.canUse(stack, amount);
	}

	@Override
	public boolean use(ItemStack stack, double amount, LivingEntity entity)
	{
		return ElectricItem.rawManager.use(stack, amount, entity);
	}

	@Override
	public void chargeFromArmor(ItemStack stack, LivingEntity entity)
	{
	}

	@Override
	public String getToolTip(ItemStack stack)
	{
		return ElectricItem.rawManager.getToolTip(stack);
	}

	@Override
	public int getTier(ItemStack stack)
	{
		return ElectricItem.manager.getTier(jetpack.copy());
	}

	@Override
	public synchronized boolean handles(ItemStack stack)
	{
		if (this.internalHandlesCheck)
		{
			return false;
		}

		this.internalHandlesCheck = true;
		boolean handle = hasJetpackAttached(stack) && ElectricItem.manager.getMaxCharge(stack) <= 0.0;
		this.internalHandlesCheck = false;
		return handle;
	}

	@SubscribeEvent
	public void tick(TickEvent.PlayerTickEvent event)
	{
		if (event.phase == TickEvent.Phase.START)
		{
			if (playerArmorBuffer.containsKey(event.player))
			{
				ItemStack stack = event.player.getItemBySlot(EquipmentSlot.CHEST);
				ItemStack lastStack = playerArmorBuffer.get(event.player);
				if (!StackUtil.isEmpty(lastStack) && hasJetpackAttached(lastStack) && StackUtil.isEmpty(stack))
				{
					ItemStack newJetpack = jetpack.copy();
					double oldCharge = ElectricItem.manager.getCharge(lastStack);
					ElectricItem.manager.charge(newJetpack, oldCharge, Integer.MAX_VALUE, true, false);
					event.player.setItemSlot(EquipmentSlot.CHEST, newJetpack);
				}

				playerArmorBuffer.remove(event.player);
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void tooltip(ItemTooltipEvent event)
	{
		if (hasJetpackAttached(event.getItemStack()))
		{
			event.getToolTip().add(net.minecraft.network.chat.Component.literal(ChatFormatting.YELLOW + Localization.translate("ic2.jetpackAttached")));
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
	public void livingAttack(LivingAttackEvent event)
	{
		if (event.getEntity() instanceof Player player && event.getSource() != null && !event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY))
		{
			ItemStack currentArmor = player.getItemBySlot(EquipmentSlot.CHEST);
			if (hasJetpackAttached(currentArmor))
			{
				playerArmorBuffer.put(player, currentArmor);
			}
		}
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void render(RenderLivingEvent.Pre<LivingEntity, ?> event)
	{
		LivingEntity entity = event.getEntity();
		if (hasJetpackAttached(entity.getItemBySlot(EquipmentSlot.CHEST)))
		{
			if (render == null)
			{
				render = new LayerJetpackOverride((RenderLayerParent) event.getRenderer());
				renderLayers = ReflectionUtil.getField(LivingEntityRenderer.class, "layers", "f_115291_");
			}

			event.getRenderer().addLayer((RenderLayer) render);
		}
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	@SuppressWarnings("unchecked")
	public void renderPost(RenderLivingEvent.Post<LivingEntity, ?> event)
	{
		if (render != null)
		{
			((List<RenderLayer<?, ?>>) ReflectionUtil.getFieldValue(renderLayers, event.getRenderer())).remove(render);
		}
	}
}
