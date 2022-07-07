package nautilus.game.arcade.game.games.smash.perks.villager;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilItem;
import mineplex.core.recharge.Recharge;
import mineplex.core.recharge.RechargedEvent;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.events.PerkDoubleJumpEvent;
import nautilus.game.arcade.game.games.smash.events.SmashActivateEvent;
import nautilus.game.arcade.game.games.smash.kits.KitVillager;
import nautilus.game.arcade.game.games.smash.kits.KitVillager.VillagerType;
import nautilus.game.arcade.game.games.smash.perks.SmashPerk;

public class PerkArts extends SmashPerk
{

	private final Map<Player, ArtData> _data = new HashMap<>();

	private int _duration;
	private int _cooldown;

	private double _attackDamageMod;
	private float _attackSpeedMod;
	private double _attackKBMod;
	private double _attackDoubleJumpMod;

	private float _defenseDamageMod;
	private float _defenseSpeedMod;
	private double _defenseKBMod;
	private double _defenseDoubleJumpMod;

	private float _speedDamageMod;
	private float _speedSpeedMod;
	private double _speedKBMod;
	private double _speedDoubleJumpMod;

	public PerkArts()
	{
		super("Villager Arts", new String[]
				{
						C.cYellow + "Right-Click" + C.cGray + " Spade to cycle through " + C.cGreen + "Villager Arts",
						C.cYellow + "Drop" + C.cGray + " to activate the selected " + C.cGreen + "Villager Art",
				});
	}

	@Override
	public void setupValues()
	{
		_duration = getPerkTime("Duration");
		_cooldown = getPerkTime("Cooldown");

		_attackDamageMod = getPerkDouble("Attack.Damage Mod");
		_attackSpeedMod = getPerkFloat("Attack.Speed Mod");
		_attackKBMod = getPerkPercentage("Attack.Knockback Mod");
		_attackDoubleJumpMod = getPerkDouble("Attack.Double Jump Mod");

		_defenseDamageMod = getPerkFloat("Defense.Damage Mod");
		_defenseSpeedMod = getPerkFloat("Defense.Speed Mod");
		_defenseKBMod = getPerkPercentage("Defense.Knockback Mod");
		_defenseDoubleJumpMod = getPerkDouble("Defense.Double Jump Mod");

		_speedDamageMod = getPerkFloat("Speed.Damage Mod");
		_speedSpeedMod = getPerkFloat("Speed.Speed Mod");
		_speedKBMod = getPerkPercentage("Speed.Knockback Mod");
		_speedDoubleJumpMod = getPerkDouble("Speed.Double Jump Mod");
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void playerInteract(PlayerInteractEvent event)
	{
		if (event.isCancelled() || !UtilEvent.isAction(event, ActionType.R) || UtilBlock.usable(event.getClickedBlock()))
		{
			return;
		}

		Player player = event.getPlayer();
		ItemStack itemStack = player.getItemInHand();

		if (!UtilItem.isSpade(itemStack) || !hasPerk(player) || !Recharge.Instance.use(player, GetName(), 100, false, false))
		{
			return;
		}

		event.setCancelled(true);

		KitVillager kit = (KitVillager) Kit;
		VillagerType newType = kit.get(player).getNext();
		boolean active = !Recharge.Instance.usable(player, newType.getName());

		kit.set(player, newType);
		player.getInventory().setItem(KitVillager.ART_ACTIVE_SLOT, kit.getArtItem(newType, active));
		player.getInventory().setItem(KitVillager.ART_VISUAL_SLOT, kit.getArtVisualItem(newType, active));
		player.updateInventory();
	}

	@EventHandler
	public void artRecharge(RechargedEvent event)
	{
		Player player = event.GetPlayer();
		String ability = event.GetAbility();

		if (!Manager.GetGame().InProgress() || !hasPerk(player) || isSuperActive(player))
		{
			return;
		}

		KitVillager kit = (KitVillager) Kit;
		ItemStack itemStack = player.getInventory().getItem(KitVillager.ART_ACTIVE_SLOT);

		if (itemStack == null || itemStack.getItemMeta() == null || !itemStack.getItemMeta().getDisplayName().contains(ability))
		{
			return;
		}

		VillagerType type = kit.get(player);

		player.getInventory().setItem(KitVillager.ART_ACTIVE_SLOT, kit.getArtItem(type, false));
		player.getInventory().setItem(KitVillager.ART_VISUAL_SLOT, kit.getArtVisualItem(type, false));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void playerDropItem(PlayerDropItemEvent event)
	{
		Player player = event.getPlayer();
		ItemStack itemStack = event.getItemDrop().getItemStack();

		if (!hasPerk(player) || itemStack == null || itemStack.getType() == Material.BED)
		{
			return;
		}

		KitVillager kit = (KitVillager) Kit;
		VillagerType type = kit.get(player);

		if (!Recharge.Instance.use(player, type.getName(), _cooldown, true, false))
		{
			return;
		}

		ArtData previousData = _data.remove(player);

		if (previousData != null)
		{
			VillagerType oldType = previousData.Type;
			player.sendMessage(F.main("Game", "You deactivated the " + F.name(oldType.getChatColour() + oldType.getName()) + " Art."));
		}

		player.sendMessage(F.main("Game", "You activated the " + F.name(type.getChatColour() + type.getName()) + " Art."));
		player.setExp(0.99F);
		player.getInventory().setItem(KitVillager.ART_ACTIVE_SLOT, kit.getArtItem(type, true));
		player.getInventory().setItem(KitVillager.ART_VISUAL_SLOT, kit.getArtVisualItem(player, type));
		kit.giveArmour(player, true);
		kit.updateDisguise(player, type.getProfession());
		UtilFirework.playFirework(player.getLocation().add(0, 1, 0), FireworkEffect
				.builder()
				.with(Type.BALL)
				.withColor(type.getColour())
				.withFade(Color.WHITE)
				.withFlicker()
				.build()
		);
		reset(player);

		switch (type)
		{
			case ATTACK:
				player.setWalkSpeed(player.getWalkSpeed() + _attackSpeedMod);
				break;
			case DEFENSE:
				player.setWalkSpeed(player.getWalkSpeed() + _defenseSpeedMod);
				break;
			case SPEED:
				player.setWalkSpeed(player.getWalkSpeed() + _speedSpeedMod);
				break;
		}

		if (UtilItem.isSpade(itemStack))
		{
			event.setCancelled(false);
			event.getItemDrop().remove();
		}

		_data.put(player, new ArtData(type));
	}

	@EventHandler
	public void updateExp(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		_data.keySet().removeIf(player ->
		{
			ArtData data = _data.get(player);

			if (!player.isOnline() || !hasPerk(player))
			{
				return true;
			}

			if (player.getExp() == 0)
			{
				KitVillager kit = (KitVillager) Kit;

				reset(player);
				kit.giveArmour(player, false);
				kit.updateDisguise(player, Profession.FARMER);
				player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 0.5F);
				player.sendMessage(F.main("Game", "Your " + F.name(data.Type.getChatColour() + data.Type.getName()) + " Art ended."));
				return true;
			}

			long time = System.currentTimeMillis() - data.Start;

			if (time > _duration - 2000 && data.Sounds == 0 || time > _duration - 1000 && data.Sounds == 1)
			{
				player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 0.3F);
				data.Sounds++;
			}

			player.setExp(Math.max(1 - time / (float) _duration, 0));
			return false;
		});
	}

	@EventHandler
	public void playerDeath(PlayerDeathEvent event)
	{
		_data.keySet().removeIf(player -> event.getEntity().equals(player));
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void damage(CustomDamageEvent event)
	{
		if (event.isCancelled() || event.GetCause() != DamageCause.ENTITY_ATTACK)
		{
			return;
		}

		Player damager = event.GetDamagerPlayer(false);
		String key = "Knockback Multiplier";
		Double value = event.GetKnockback().get(key);

		if (damager == null || value == null)
		{
			return;
		}

		if (isSuperActive(damager))
		{
			LivingEntity damagee = event.GetDamageeEntity();
			damagee.getWorld().playEffect(damagee.getLocation().add(0, 1, 0), Effect.STEP_SOUND, Material.IRON_BLOCK);

			event.AddMod("Perfection", _attackDamageMod);
			return;
		}
		else if (event.GetDamageePlayer() != null && isSuperActive(event.GetDamageePlayer()))
		{
			event.GetKnockback().put(key, value + _defenseKBMod);
			return;
		}

		ArtData data = _data.get(damager);

		if (data == null)
		{
			return;
		}

		switch (data.Type)
		{
			case ATTACK:
				event.AddMod(data.Type.getName(), _attackDamageMod);
				value += _attackKBMod;
				break;
			case DEFENSE:
				event.AddMod(data.Type.getName(), _defenseDamageMod);
				value += _defenseKBMod;
				break;
			case SPEED:
				event.AddMod(data.Type.getName(), _speedDamageMod);
				value += _speedKBMod;
				break;
		}

		event.GetKnockback().put(key, value);
	}

	@EventHandler
	public void doubleJump(PerkDoubleJumpEvent event)
	{
		Player player = event.getPlayer();

		if (!hasPerk(player))
		{
			return;
		}

		double increase = 0;

		if (isSuperActive(player))
		{
			increase = _speedDoubleJumpMod;
			event.setControl(true);
		}
		else
		{
			ArtData data = _data.get(player);

			if (data == null)
			{
				return;
			}

			switch (data.Type)
			{
				case ATTACK:
					increase = _attackDoubleJumpMod;
					break;
				case DEFENSE:
					increase = _defenseDoubleJumpMod;
					break;
				case SPEED:
					increase = _speedDoubleJumpMod;
					event.setControl(true);
					break;
			}
		}

		event.setPower(event.getPower() + increase);
	}

	private void reset(Player player)
	{
		player.setWalkSpeed(0.2F);
	}

	@EventHandler
	public void smashActivate(SmashActivateEvent event)
	{
		Player player = event.getPlayer();

		if (!hasPerk(player))
		{
			return;
		}

		_data.remove(player);
		reset(player);
		player.setWalkSpeed(player.getWalkSpeed() + _speedSpeedMod);
		KitVillager kit = (KitVillager) Kit;
		player.getInventory().setItem(KitVillager.ART_ACTIVE_SLOT, kit.getArtItem(player, true));
	}

	public VillagerType getActiveArt(Player player)
	{
		ArtData data = _data.get(player);
		return data == null ? null : data.Type;
	}

	private class ArtData
	{
		VillagerType Type;
		long Start;
		int Sounds;

		ArtData(VillagerType type)
		{
			Type = type;
			Start = System.currentTimeMillis();
		}
	}
}
