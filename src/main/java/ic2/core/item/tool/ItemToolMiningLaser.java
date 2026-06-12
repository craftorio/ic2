package ic2.core.item.tool;

import ic2.api.network.INetworkItemEventListener;
import ic2.core.IC2;
import ic2.core.entity.LaserBulletEntity;
import ic2.core.init.Localization;
import ic2.core.item.PriorityUsableItem;
import ic2.core.ref.Ic2SoundEvents;
import ic2.core.util.KeyboardClient;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import ic2.core.util.Vector3;

import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class ItemToolMiningLaser extends ItemElectricTool implements INetworkItemEventListener, PriorityUsableItem
{
	public ItemToolMiningLaser(Properties settings)
	{
		super(settings, 100);
		this.maxCharge = 300000;
		this.transferLimit = 512;
		this.tier = 3;
	}

	@Override
	public boolean isBarVisible(ItemStack stack)
	{
		return !stack.getHoverName().getString().equals("ic2:tab_icon");
	}

	@Override
	public void appendHoverText(ItemStack stack, Level world, List<Component> list, TooltipFlag par4)
	{
		super.appendHoverText(stack, world, list, par4);
		CompoundTag nbtData = StackUtil.getOrCreateNbtData(stack);
		String mode;
		switch (nbtData.getInt("laser_setting"))
		{
			case 0:
				mode = Localization.translate("item.ic2.mining_laser.tooltip.mode.mining");
				break;
			case 1:
				mode = Localization.translate("item.ic2.mining_laser.tooltip.mode.lowFocus");
				break;
			case 2:
				mode = Localization.translate("item.ic2.mining_laser.tooltip.mode.longRange");
				break;
			case 3:
				mode = Localization.translate("item.ic2.mining_laser.tooltip.mode.horizontal");
				break;
			case 4:
				mode = Localization.translate("item.ic2.mining_laser.tooltip.mode.superHeat");
				break;
			case 5:
				mode = Localization.translate("item.ic2.mining_laser.tooltip.mode.scatter");
				break;
			case 6:
				mode = Localization.translate("item.ic2.mining_laser.tooltip.mode.explosive");
				break;
			case 7:
				mode = Localization.translate("item.ic2.mining_laser.tooltip.mode.3x3");
				break;
			default:
				return;
		}

		list.add(Component.translatable("item.ic2.mining_laser.tooltip.mode", mode));
		list.add(Component.translatable("item.ic2.tooltip.mode.switch", KeyboardClient.modeSwitchKey.getKey().getDisplayName(), Minecraft.getInstance().options.keyUse.getKey().getDisplayName()));
	}

	@Override
	public List<String> getHudInfo(ItemStack stack, boolean advanced)
	{
		CompoundTag nbtData = StackUtil.getOrCreateNbtData(stack);
		String mode = Localization.translate(getModeString(nbtData.getInt("laser_setting")));
		List<String> info = new LinkedList<>(super.getHudInfo(stack, advanced));
		info.add(Localization.translate("item.ic2.mining_laser.tooltip.mode", mode));
		return info;
	}

	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand)
	{
		ItemStack stack = StackUtil.get(player, hand);
		if (!IC2.sideProxy.isSimulating())
		{
			return new InteractionResultHolder<>(InteractionResult.PASS, stack);
		}

		CompoundTag nbtData = StackUtil.getOrCreateNbtData(stack);
		int laserSetting = nbtData.getInt("laser_setting");
		if (IC2.keyboard.isModeSwitchKeyDown(player))
		{
			laserSetting = (laserSetting + 1) % 8;
			nbtData.putInt("laser_setting", laserSetting);
			IC2.sideProxy.messagePlayer(player, "item.ic2.mining_laser.tooltip.mode", getModeString(laserSetting));
		} else
		{
			int consume = new int[] { 1250, 100, 5000, 0, 2500, 10000, 5000, 7500 }[laserSetting];
			if (!this.consumeEnergy(stack, consume, player))
			{
				return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
			}

			switch (laserSetting)
			{
				case 0:
					if (this.shootLaser(world, player, Float.POSITIVE_INFINITY, 5.0F, Integer.MAX_VALUE, false, false))
					{
						IC2.network.get(true).initiateItemEvent(player, stack, 0, true);
					}
					break;
				case 1:
					if (this.shootLaser(world, player, 4.0F, 5.0F, 1, false, false))
					{
						IC2.network.get(true).initiateItemEvent(player, stack, 1, true);
					}
					break;
				case 2:
					if (this.shootLaser(world, player, Float.POSITIVE_INFINITY, 20.0F, Integer.MAX_VALUE, false, false))
					{
						IC2.network.get(true).initiateItemEvent(player, stack, 2, true);
					}
				case 3:
				case 7:
				case 4:
					if (this.shootLaser(world, player, Float.POSITIVE_INFINITY, 8.0F, Integer.MAX_VALUE, false, true))
					{
						IC2.network.get(true).initiateItemEvent(player, stack, 4, true);
					}
					break;
				case 5:
					Vector3 look = Util.getLook(player);
					Vector3 right = look.copy().cross(Vector3.UP);
					if (right.lengthSquared() < 1.0E-4)
					{
						double angle = Math.toRadians(player.getYRot()) - (Math.PI / 2);
						right.set(Math.sin(angle), 0.0, -Math.cos(angle));
					} else
					{
						right.normalize();
					}

					Vector3 up = right.copy().cross(look);
					look.scale(8.0);

					for (int r = -2; r <= 2; r++)
					{
						for (int u = -2; u <= 2; u++)
						{
							Vector3 dir = look.copy().addScaled(right, r).addScaled(up, u).normalize();
							this.shootLaser(world, dir, player, Float.POSITIVE_INFINITY, 12.0F, Integer.MAX_VALUE, false, false);
						}
					}

					IC2.network.get(true).initiateItemEvent(player, stack, 5, true);
					break;
				case 6:
					if (this.shootLaser(world, player, Float.POSITIVE_INFINITY, 12.0F, Integer.MAX_VALUE, true, false))
					{
						IC2.network.get(true).initiateItemEvent(player, stack, 6, true);
					}
				default:
					break;
			}
		}

		return super.use(world, player, hand);
	}

	@Override
	public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context)
	{
		Level world = context.getLevel();
		Player player = context.getPlayer();
		BlockPos pos = context.getClickedPos();
		if (world.isClientSide)
		{
			return InteractionResult.PASS;
		}

		if (player == null)
		{
			return InteractionResult.PASS;
		}

		CompoundTag nbtData = StackUtil.getOrCreateNbtData(stack);
		if (!IC2.keyboard.isModeSwitchKeyDown(player) && (nbtData.getInt("laser_setting") == 3 || nbtData.getInt("laser_setting") == 7))
		{
			Vector3 dir = Util.getLook(player);
			double angle = dir.dot(Vector3.UP);
			if (Math.abs(angle) < 1.0 / Math.sqrt(2.0))
			{
				if (this.consumeEnergy(stack, 3000.0, player))
				{
					dir.y = 0.0;
					dir.normalize();
					Vector3 start = Util.getEyePosition(player);
					start.y = pos.getY() + 0.5;
					adjustStartPos(start, dir);
					if (nbtData.getInt("laser_setting") == 3)
					{
						if (this.shootLaser(world, start, dir, player, Float.POSITIVE_INFINITY, 5.0F, Integer.MAX_VALUE, false, false))
						{
							IC2.network.get(true).initiateItemEvent(player, stack, 3, true);
						}
					} else if (nbtData.getInt("laser_setting") == 7 && this.shootLaser(world, start, dir, player, Float.POSITIVE_INFINITY, 5.0F, Integer.MAX_VALUE, false, false))
					{
						this.shootLaser(world, new Vector3(start.x, start.y - 1.0, start.z), dir, player, Float.POSITIVE_INFINITY, 5.0F, Integer.MAX_VALUE, false, false);
						this.shootLaser(world, new Vector3(start.x, start.y + 1.0, start.z), dir, player, Float.POSITIVE_INFINITY, 5.0F, Integer.MAX_VALUE, false, false);
						if (player.getDirection().equals(Direction.SOUTH) || player.getDirection().equals(Direction.NORTH))
						{
							this.shootLaser(world, new Vector3(start.x - 1.0, start.y, start.z), dir, player, Float.POSITIVE_INFINITY, 5.0F, Integer.MAX_VALUE, false, false);
							this.shootLaser(world, new Vector3(start.x + 1.0, start.y, start.z), dir, player, Float.POSITIVE_INFINITY, 5.0F, Integer.MAX_VALUE, false, false);
							this.shootLaser(world, new Vector3(start.x - 1.0, start.y - 1.0, start.z), dir, player, Float.POSITIVE_INFINITY, 5.0F, Integer.MAX_VALUE, false, false);
							this.shootLaser(world, new Vector3(start.x + 1.0, start.y - 1.0, start.z), dir, player, Float.POSITIVE_INFINITY, 5.0F, Integer.MAX_VALUE, false, false);
							this.shootLaser(world, new Vector3(start.x - 1.0, start.y + 1.0, start.z), dir, player, Float.POSITIVE_INFINITY, 5.0F, Integer.MAX_VALUE, false, false);
							this.shootLaser(world, new Vector3(start.x + 1.0, start.y + 1.0, start.z), dir, player, Float.POSITIVE_INFINITY, 5.0F, Integer.MAX_VALUE, false, false);
						}

						if (player.getDirection().equals(Direction.EAST) || player.getDirection().equals(Direction.WEST))
						{
							this.shootLaser(world, new Vector3(start.x, start.y, start.z - 1.0), dir, player, Float.POSITIVE_INFINITY, 5.0F, Integer.MAX_VALUE, false, false);
							this.shootLaser(world, new Vector3(start.x, start.y, start.z + 1.0), dir, player, Float.POSITIVE_INFINITY, 5.0F, Integer.MAX_VALUE, false, false);
							this.shootLaser(world, new Vector3(start.x, start.y - 1.0, start.z - 1.0), dir, player, Float.POSITIVE_INFINITY, 5.0F, Integer.MAX_VALUE, false, false);
							this.shootLaser(world, new Vector3(start.x, start.y - 1.0, start.z + 1.0), dir, player, Float.POSITIVE_INFINITY, 5.0F, Integer.MAX_VALUE, false, false);
							this.shootLaser(world, new Vector3(start.x, start.y + 1.0, start.z - 1.0), dir, player, Float.POSITIVE_INFINITY, 5.0F, Integer.MAX_VALUE, false, false);
							this.shootLaser(world, new Vector3(start.x, start.y + 1.0, start.z + 1.0), dir, player, Float.POSITIVE_INFINITY, 5.0F, Integer.MAX_VALUE, false, false);
						}

						IC2.network.get(true).initiateItemEvent(player, stack, 7, true);
					}
				}
			} else if (nbtData.getInt("laser_setting") == 7)
			{
				if (this.consumeEnergy(stack, 3000.0, player))
				{
					dir.x = 0.0;
					dir.z = 0.0;
					dir.normalize();
					Vector3 start = Util.getEyePosition(player);
					start.x = pos.getX() + 0.5;
					start.z = pos.getZ() + 0.5;
					adjustStartPos(start, dir);
					if (this.shootLaser(world, start, dir, player, Float.POSITIVE_INFINITY, 5.0F, Integer.MAX_VALUE, false, false))
					{
						this.shootLaser(world, new Vector3(start.x + 1.0, start.y, start.z), dir, player, Float.POSITIVE_INFINITY, 5.0F, Integer.MAX_VALUE, false, false);
						this.shootLaser(world, new Vector3(start.x - 1.0, start.y, start.z), dir, player, Float.POSITIVE_INFINITY, 5.0F, Integer.MAX_VALUE, false, false);
						this.shootLaser(world, new Vector3(start.x + 1.0, start.y, start.z + 1.0), dir, player, Float.POSITIVE_INFINITY, 5.0F, Integer.MAX_VALUE, false, false);
						this.shootLaser(world, new Vector3(start.x - 1.0, start.y, start.z - 1.0), dir, player, Float.POSITIVE_INFINITY, 5.0F, Integer.MAX_VALUE, false, false);
						this.shootLaser(world, new Vector3(start.x + 1.0, start.y, start.z - 1.0), dir, player, Float.POSITIVE_INFINITY, 5.0F, Integer.MAX_VALUE, false, false);
						this.shootLaser(world, new Vector3(start.x - 1.0, start.y, start.z + 1.0), dir, player, Float.POSITIVE_INFINITY, 5.0F, Integer.MAX_VALUE, false, false);
						this.shootLaser(world, new Vector3(start.x, start.y, start.z + 1.0), dir, player, Float.POSITIVE_INFINITY, 5.0F, Integer.MAX_VALUE, false, false);
						this.shootLaser(world, new Vector3(start.x, start.y, start.z - 1.0), dir, player, Float.POSITIVE_INFINITY, 5.0F, Integer.MAX_VALUE, false, false);
						IC2.network.get(true).initiateItemEvent(player, stack, 7, true);
					}
				}
			} else
			{
				IC2.sideProxy.messagePlayer(player, "Mining laser aiming angle too steep");
			}
		}

		return InteractionResult.FAIL;
	}

	private static Vector3 adjustStartPos(Vector3 pos, Vector3 dir)
	{
		return pos.addScaled(dir, 0.2);
	}

	private void setLaserVelocity(Projectile laser, Entity shooter, Vector3 direction)
	{
		laser.shoot(direction.x, direction.y, direction.z, (float) 3.0, (float) 1.0);
		Vec3 vec3d = shooter.getDeltaMovement();
		laser.setDeltaMovement(laser.getDeltaMovement().add(vec3d.x, shooter.onGround() ? 0.0 : vec3d.y, vec3d.z));
	}

	public boolean shootLaser(Level world, LivingEntity owner, float range, float power, int blockBreaks, boolean explosive, boolean smelt)
	{
		Vector3 dir = Util.getLook(owner);
		return this.shootLaser(world, dir, owner, range, power, blockBreaks, explosive, smelt);
	}

	public boolean shootLaser(Level world, Vector3 dir, LivingEntity owner, float range, float power, int blockBreaks, boolean explosive, boolean smelt)
	{
		Vector3 start = adjustStartPos(Util.getEyePosition(owner), dir);
		return this.shootLaser(world, start, dir, owner, range, power, blockBreaks, explosive, smelt);
	}

	public boolean shootLaser(Level world, Vector3 start, Vector3 dir, LivingEntity owner, float range, float power, int blockBreaks, boolean explosive, boolean smelt)
	{
		LaserBulletEntity entity = new LaserBulletEntity(world, start, dir, owner, range, power, blockBreaks, explosive);
		entity.init(owner, range, power, blockBreaks, explosive, smelt, true);
		this.setLaserVelocity(entity, owner, dir);
		world.addFreshEntity(entity);
		return true;
	}

	public @NotNull Rarity getRarity(@NotNull ItemStack stack)
	{
		return Rarity.UNCOMMON;
	}

	private void playShotSound(Player player, SoundEvent soundEvent)
	{
		player.playNotifySound(soundEvent, SoundSource.PLAYERS, 1.0F, 1.0F);
	}

	@Override
	public void onNetworkEvent(ItemStack stack, Player player, int event)
	{
		switch (event)
		{
			case 0:
			case 3:
			case 4:
				this.playShotSound(player, Ic2SoundEvents.ITEM_LASER_SHOOT);
				break;
			case 1:
				this.playShotSound(player, Ic2SoundEvents.ITEM_LASER_LOW_FOCUS);
				break;
			case 2:
				this.playShotSound(player, Ic2SoundEvents.ITEM_LASER_LONG_RANGE);
				break;
			case 5:
			case 7:
				this.playShotSound(player, Ic2SoundEvents.ITEM_LASER_SCATTER);
				break;
			case 6:
				this.playShotSound(player, Ic2SoundEvents.ITEM_LASER_EXPLOSIVE);
		}
	}

	private static String getModeString(int mode)
	{
		return switch (mode)
		{
			case 0 -> "item.ic2.mining_laser.tooltip.mode.mining";
			case 1 -> "item.ic2.mining_laser.tooltip.mode.lowFocus";
			case 2 -> "item.ic2.mining_laser.tooltip.mode.longRange";
			case 3 -> "item.ic2.mining_laser.tooltip.mode.horizontal";
			case 4 -> "item.ic2.mining_laser.tooltip.mode.superHeat";
			case 5 -> "item.ic2.mining_laser.tooltip.mode.scatter";
			case 6 -> "item.ic2.mining_laser.tooltip.mode.explosive";
			case 7 -> "item.ic2.mining_laser.tooltip.mode.3x3";
			default -> throw new NoSuchElementException("No such mode: " + mode);
		};
	}
}
