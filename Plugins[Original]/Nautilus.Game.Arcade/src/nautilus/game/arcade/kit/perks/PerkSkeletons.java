package nautilus.game.arcade.kit.perks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftSkeleton;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.events.PlayerStateChangeEvent;
import nautilus.game.arcade.game.GameTeam.PlayerState;
import nautilus.game.arcade.game.games.survivalgames.kit.necroinventory.NecroInventoryMenu;
import nautilus.game.arcade.kit.Perk;

public class PerkSkeletons extends Perk
{
	public static class MinionSpawnEvent extends PlayerEvent
	{
		private static final HandlerList handlers = new HandlerList();

		public static HandlerList getHandlerList()
		{
			return handlers;
		}

		private final PerkSkeletons _perkSkeletons;

		MinionSpawnEvent(Player who, PerkSkeletons perkSkeletons)
		{
			super(who);

			_perkSkeletons = perkSkeletons;
		}

		@Override
		public HandlerList getHandlers()
		{
			return getHandlerList();
		}

		public PerkSkeletons getPerkSkeletons()
		{
			return _perkSkeletons;
		}
	}

	private final Map<Player, List<Skeleton>> _minions = new HashMap<>();
	private final NecroInventoryMenu _menu;

	private boolean _name;
	private int _maxDist = 8;
	private static final int MAX_SPAWN_RADIUS = 5;

	public PerkSkeletons(boolean name)
	{
		super("Skeleton Minons", new String[]
				{
						C.cGray + "Killing an opponent summons a skeletal minion."
				});
		_name = name;
		_menu = new NecroInventoryMenu(this);
	}

	@Override
	public void registeredEvents()
	{
		_menu.activate();
	}

	@Override
	public void unregisteredEvents()
	{
		_menu.deactivate();
	}

	/**
	 * Find whether a given location is a valid
	 * Skeleton spawnpoint.
	 *
	 * A location is valid if it meets the following
	 * criteria:
	 * - The block at and 1 y-value above are both air
	 * @param location - The location at which you want to spawn a skeleton.
	 * @return Whether the location is valid or not
	 */
	private boolean isValidSpawn(Location location)
	{
		return location.getBlock().getType() == Material.AIR
				&& location.clone().add(0, 1, 0).getBlock().getType() == Material.AIR;
	}

	private Location getMinionLocation(Location deathLocation)
	{
		if (isValidSpawn(deathLocation))
		{
			return deathLocation;
		}

		/*
		  Get all blocks within the max spawn radius,
		  where each block is at the same Y-level as
		  the death location,
		  filter locations to those which are valid spawns,
		  then sort them in ascending order of distance
		  to the original death location.
		 */
		List<Block> nearbyBlocks = UtilBlock.getBlocksInRadius(deathLocation, MAX_SPAWN_RADIUS, deathLocation.getBlockY())
				.stream()
				.filter(b -> isValidSpawn(b.getLocation()))
				.sorted(Comparator.comparingDouble(a -> UtilMath.offsetSquared(a.getLocation(), deathLocation)))
				.collect(Collectors.toCollection(LinkedList::new));

		if (nearbyBlocks.size() > 0)
		{
			return nearbyBlocks.get(0).getLocation();
		}
		else
		{
			Location spawnLocation = deathLocation.clone();

			while (!isValidSpawn(spawnLocation) && spawnLocation.getY() < 256)
			{
				spawnLocation = spawnLocation.add(0, 1, 0);
			}

			return spawnLocation;
		}
	}

	@EventHandler
	public void MinionSpawn(CombatDeathEvent event)
	{
		if (event.GetLog().GetKiller() == null)
			return;

		if (!(event.GetEvent().getEntity() instanceof Player))
			return;

		Player killer = UtilPlayer.searchExact(event.GetLog().GetKiller().GetName());
		if (killer == null)
			return;

		if (!Kit.HasKit(killer))
			return;

		Player killed = (Player) event.GetEvent().getEntity();

		Manager.GetGame().CreatureAllowOverride = true;

		Skeleton skel = killed.getWorld().spawn(getMinionLocation(killed.getLocation()), Skeleton.class);

		Manager.GetGame().CreatureAllowOverride = false;

		UtilEnt.removeGoalSelectors(skel);

		skel.setMaxHealth(36);
		skel.setHealth(skel.getMaxHealth());

		ItemStack inHand = killed.getItemInHand();

		if (inHand != null && inHand.getType() == Material.MAP)
		{
			inHand = null;
		}

		skel.getEquipment().setItemInHand(inHand);
		skel.getEquipment().setHelmet(killed.getInventory().getHelmet());
		skel.getEquipment().setChestplate(killed.getInventory().getChestplate());
		skel.getEquipment().setLeggings(killed.getInventory().getLeggings());
		skel.getEquipment().setBoots(killed.getInventory().getBoots());

		event.GetEvent().getDrops().remove(killed.getItemInHand());
		event.GetEvent().getDrops().remove(killed.getInventory().getHelmet());
		event.GetEvent().getDrops().remove(killed.getInventory().getChestplate());
		event.GetEvent().getDrops().remove(killed.getInventory().getLeggings());
		event.GetEvent().getDrops().remove(killed.getInventory().getBoots());

		skel.getEquipment().setItemInHandDropChance(1f);
		skel.getEquipment().setHelmetDropChance(1f);
		skel.getEquipment().setChestplateDropChance(1f);
		skel.getEquipment().setLeggingsDropChance(1f);
		skel.getEquipment().setBootsDropChance(1f);

		if (_name)
		{
			skel.setCustomName("Skeletal " + UtilEnt.getName(event.GetEvent().getEntity()));
			skel.setCustomNameVisible(true);
		}

		_minions.computeIfAbsent(killer, k -> new ArrayList<>()).add(skel);

		killer.playSound(killer.getLocation(), Sound.SKELETON_HURT, 1f, 1f);

		UtilServer.CallEvent(new MinionSpawnEvent(killer, this));
	}

	@EventHandler
	public void TargetCancel(EntityTargetEvent event)
	{
		if (!(event.getTarget() instanceof Player) || !(event.getEntity() instanceof Skeleton))
		{
			return;
		}

		if (_minions.containsKey(event.getTarget()) && _minions.get(event.getTarget()).contains(event.getEntity()))
			event.setCancelled(true);

		for (Player player : _minions.keySet())
		{
			for (Skeleton skel : _minions.get(player))
			{
				if (event.getEntity().equals(skel))
				{
					// Cancel targeting when the skeleton
					// needs to return home
					if (UtilMath.offset(skel, player) > _maxDist)
					{
						event.setCancelled(true);
					}

					if (!Manager.canHurt(player, (Player) event.getTarget()))
					{
						event.setCancelled(true);
					}
				}
			}
		}
	}

	@EventHandler
	public void MinionUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		for (Player player : _minions.keySet())
		{
			Iterator<Skeleton> skelIterator = _minions.get(player).iterator();

			while (skelIterator.hasNext())
			{
				Skeleton skel = skelIterator.next();

				//Dead
				if (!skel.isValid())
				{
					skelIterator.remove();
					continue;
				}

				double range = 4;

				//If it's targeting another player
				if (skel.getTarget() != null || ((CraftSkeleton) skel).getHandle().getGoalTarget() != null)
				{
					range = _maxDist;
				}

				//Return to Owner
				if (UtilMath.offset(skel, player) > range)
				{
					float speed = 1.25f;
					if (player.isSprinting())
						speed = 1.75f;

					//Move
					Location target = skel.getLocation().add(UtilAlg.getTrajectory(skel, player).multiply(3));

					skel.setTarget(null);
					UtilEnt.CreatureMove(skel, target, speed);
				}

				// If it's VERY far from the player
				if  (UtilMath.offset(skel, player) > (range * 5))
				{
					skel.teleport(player.getLocation());
				}
			}
		}
	}

	@EventHandler
	public void Heal(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		for (List<Skeleton> skels : _minions.values())
		{
			for (Skeleton skel : skels)
			{
				if (skel.getHealth() > 0)
					skel.setHealth(Math.min(skel.getMaxHealth(), skel.getHealth() + 1));
			}
		}
	}

	public boolean IsMinion(Entity ent)
	{
		for (List<Skeleton> skels : _minions.values())
		{
			for (Skeleton skel : skels)
			{
				if (ent.equals(skel))
				{
					return true;
				}
			}
		}

		return false;
	}

	@EventHandler
	public void Combust(EntityCombustEvent event)
	{
		if (IsMinion(event.getEntity()))
			event.setCancelled(true);
	}

	@EventHandler
	public void Damage(CustomDamageEvent event)
	{
		if (event.GetDamagerEntity(true) == null)
			return;

		if (!IsMinion(event.GetDamagerEntity(true)))
			return;

		double damage = 4;

		if (event.GetDamagerEntity(true) instanceof Skeleton)
		{
			Skeleton skel = (Skeleton) event.GetDamagerEntity(true);

			if (skel.getEquipment().getItemInHand() != null)
			{
				if (skel.getEquipment().getItemInHand().getType() == Material.STONE_SWORD) damage = 5;
				else if (skel.getEquipment().getItemInHand().getType() == Material.IRON_SWORD) damage = 6;
				else if (skel.getEquipment().getItemInHand().getType() == Material.GOLD_SWORD) damage = 6;
				else if (skel.getEquipment().getItemInHand().getType() == Material.DIAMOND_SWORD) damage = 7;

				else if (skel.getEquipment().getItemInHand().getType() == Material.IRON_AXE) damage = 5;
				else if (skel.getEquipment().getItemInHand().getType() == Material.GOLD_AXE) damage = 5;
				else if (skel.getEquipment().getItemInHand().getType() == Material.DIAMOND_AXE) damage = 6;
			}
		}

		if (event.GetProjectile() != null)
			damage = 6;

		event.AddMod("Skeleton Minion", "Negate", -event.GetDamageInitial(), false);
		event.AddMod("Skeleton Minion", "Damage", damage, false);
	}

	@EventHandler
	public void outOfGame(PlayerStateChangeEvent event)
	{
		if (!Manager.GetGame().IsLive())
			return;
		
		if (event.GetState() == PlayerState.OUT)
		{
			despawnSkels(event.GetPlayer());
		}
	}
	
	@EventHandler
	public void PlayerDeath(PlayerDeathEvent event)
	{
		despawnSkels(event.getEntity());
	}
	
	private void despawnSkels(Player player)
	{
		List<Skeleton> skels = _minions.remove(player);

		if (skels == null)
			return;

		for (Skeleton skel : skels)
			skel.remove();

		skels.clear();
	}

	public List<Skeleton> getSkeletons(Player player)
	{
		return _minions.get(player);
	}
}
