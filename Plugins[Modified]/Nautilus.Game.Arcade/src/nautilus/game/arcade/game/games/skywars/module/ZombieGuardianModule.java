package nautilus.game.arcade.game.games.skywars.module;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.modules.Module;
import nautilus.game.arcade.game.modules.chest.ChestLootModule;

public class ZombieGuardianModule extends Module
{

	private static final ItemStack[] ARMOUR =
			{
					new ItemBuilder(Material.GOLD_BOOTS).setUnbreakable(true).build(),
					new ItemBuilder(Material.GOLD_LEGGINGS).setUnbreakable(true).build(),
					new ItemBuilder(Material.GOLD_CHESTPLATE).setUnbreakable(true).build(),
					new ItemBuilder(Material.GOLD_HELMET).setUnbreakable(true).build(),
			};
	private static final PotionEffect FIRE_RESISTANCE = new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, false, false);
	private static final int HEALTH = 15;
	private static final int MAX_OFFSET_SQUARED = 64;

	private final List<Location> _spawns;
	private final Map<Zombie, Location> _zombies;

	public ZombieGuardianModule()
	{
		_spawns = new ArrayList<>();
		_zombies = new HashMap<>();
	}

	@Override
	public void cleanup()
	{
		_zombies.clear();
		_spawns.clear();
	}

	public ZombieGuardianModule addSpawns(List<Location> spawns)
	{
		_spawns.addAll(spawns);
		return this;
	}

	@EventHandler
	public void spawnZombies(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Prepare)
		{
			return;
		}

		getGame().CreatureAllowOverride = true;

		for (Location location : _spawns)
		{
			Zombie zombie = location.getWorld().spawn(location, Zombie.class);
			zombie.setRemoveWhenFarAway(false);
			zombie.setCustomName(C.cDRed + "Zombie Guardian");
			zombie.setCustomNameVisible(true);
			zombie.setMaxHealth(HEALTH);
			zombie.setHealth(HEALTH);
			zombie.addPotionEffect(FIRE_RESISTANCE);

			EntityEquipment equipment = zombie.getEquipment();
			equipment.setArmorContents(ARMOUR);
			equipment.setHelmetDropChance(0);
			equipment.setChestplateDropChance(0);
			equipment.setLeggingsDropChance(0);
			equipment.setBootsDropChance(0);

			_zombies.put(zombie, location);
		}

		getGame().CreatureAllowOverride = false;
	}

	@EventHandler
	public void zombieTarget(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}

		_zombies.keySet().removeIf(zombie -> !zombie.isValid());

		_zombies.forEach((zombie, location) ->
		{
			if (zombie.getTarget() == null || UtilMath.offsetSquared(zombie.getLocation(), location) > MAX_OFFSET_SQUARED)
			{
				zombie.setTarget(null);
				UtilEnt.CreatureMove(zombie, location, 1);
			}
		});
	}

	@EventHandler
	public void zombieTarget(EntityTargetLivingEntityEvent event)
	{
		if (event.getTarget() == null || !_zombies.containsKey(event.getEntity()))
		{
			return;
		}

		Zombie zombie = (Zombie) event.getEntity();
		Location location = _zombies.get(zombie);

		if (UtilMath.offsetSquared(event.getTarget().getLocation(), location) > MAX_OFFSET_SQUARED)
		{
			event.setCancelled(true);
			zombie.setTarget(null);
		}
	}

	@EventHandler
	public void zombieCombust(EntityCombustEvent event)
	{
		if (_zombies.containsKey(event.getEntity()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void zombieDamage(CustomDamageEvent event)
	{
		if (event.GetCause() != DamageCause.ENTITY_EXPLOSION || !_zombies.containsKey(event.GetDamageeEntity()))
		{
			return;
		}

		event.AddMod("Blast Protection", -event.GetDamage() + 8);
		event.SetIgnoreArmor(true);
	}

	@EventHandler
	public void zombieDeath(EntityDeathEvent event)
	{
		Entity entity = event.getEntity();

		if (!_zombies.containsKey(entity))
		{
			return;
		}

		Zombie zombie = (Zombie) entity;
		ChestLootModule lootModule = getGame().getModule(ChestLootModule.class);
		ItemStack randomItem = lootModule.getRandomItem("Middle");

		event.getDrops().clear();

		if (randomItem != null)
		{
			event.getDrops().add(randomItem);
		}

		if (zombie.getKiller() != null)
		{
			getGame().AddStat(zombie.getKiller(), "ZombieKills", 1, false, false);
		}
	}
}
