package nautilus.game.arcade.game.games.moba.shop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.gadget.gadgets.gamemodifiers.moba.shopmorph.ShopMorphGadget;
import mineplex.core.game.GameDisplay;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.combat.CombatComponent;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;
import mineplex.minecraft.game.core.condition.events.ConditionApplyEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.events.PlayerGameRespawnEvent;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.moba.Moba;
import nautilus.game.arcade.game.games.moba.MobaPlayer;
import nautilus.game.arcade.game.games.moba.MobaRole;
import nautilus.game.arcade.game.games.moba.kit.AmmoGiveEvent;
import nautilus.game.arcade.game.games.moba.kit.CooldownCalculateEvent;
import nautilus.game.arcade.game.games.moba.kit.HeroKit;
import nautilus.game.arcade.game.games.moba.kit.hp.MobaHPRegenEvent;
import nautilus.game.arcade.game.games.moba.shop.assassin.MobaAssassinShop;
import nautilus.game.arcade.game.games.moba.shop.hunter.MobaHunterShop;
import nautilus.game.arcade.game.games.moba.shop.mage.MobaMageShop;
import nautilus.game.arcade.game.games.moba.shop.warrior.MobaWarriorShop;
import nautilus.game.arcade.game.games.moba.util.MobaConstants;

public class MobaShop implements Listener
{

	private static final String NPC_NAME = C.cGoldB + "GOLD UPGRADES";
	static String getNPCName()
	{
		return NPC_NAME;
	}

	private final Moba _host;
	private final Map<Player, MobaShopNPC> _entities;
	private final Map<MobaRole, MobaShopMenu> _roleMenus;
	private final Map<Player, List<MobaItem>> _upgrades;

	public MobaShop(Moba host)
	{
		_host = host;
		_entities = new HashMap<>(8);
		_roleMenus = new HashMap<>(4);
		_upgrades = new HashMap<>();

		// Create menus
		_roleMenus.put(MobaRole.ASSASSIN, new MobaAssassinShop(host, this));
		_roleMenus.put(MobaRole.WARRIOR, new MobaWarriorShop(host, this));
		_roleMenus.put(MobaRole.HUNTER, new MobaHunterShop(host, this));
		_roleMenus.put(MobaRole.MAGE, new MobaMageShop(host, this));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void spawnNPC(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Prepare)
		{
			return;
		}

		// Only spawn the NPCs once all players have been loaded into the world.
		_host.getArcadeManager().runSyncLater(this::spawnShopNPCs, _host.GetPlayers(true).size() * _host.TickPerTeleport + 10);
	}

	private void spawnShopNPCs()
	{
		ArrayList<Location> locations = _host.WorldData.GetDataLocs(MobaConstants.SHOP);

		_host.CreatureAllowOverride = true;
		for (GameTeam team : _host.GetTeamList())
		{
			Location location = UtilAlg.findClosest(team.GetSpawn(), locations);
			location.setYaw(UtilAlg.GetYaw(UtilAlg.getTrajectory(location, _host.GetSpectatorLocation())));
			location.setPitch(0);

			for (Player player : team.GetPlayers(true))
			{
				ShopMorphGadget gadget = (ShopMorphGadget) _host.getArcadeManager().getCosmeticManager().getGadgetManager().getGameCosmeticManager().getActiveCosmetic(
						player,
						GameDisplay.MOBA,
						"Shop Morph"
				);

				MobaShopNPC npc = new MobaShopNPC(this, player, location, gadget);

				_entities.put(player, npc);
			}
		}
		_host.CreatureAllowOverride = false;
	}

	@EventHandler
	public void cleanup(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.End && event.GetState() != GameState.Dead)
		{
			return;
		}

		_entities.forEach((player, npc) -> _host.getArcadeManager().getPacketHandler().removePacketHandler(npc));
	}

	public void openShop(Player player)
	{
		if (!_host.IsLive())
		{
			return;
		}

		MobaPlayer mobaPlayer = _host.getMobaData(player);

		if (mobaPlayer == null)
		{
			return;
		}

		MobaShopMenu menu = _roleMenus.get(mobaPlayer.getRole());

		if (menu == null)
		{
			player.sendMessage(F.main("Game", "There isn't an upgrade shop for that kit yet."));
			return;
		}

		menu.open(player);
	}

	@EventHandler
	public void npcDamage(CustomDamageEvent event)
	{
		for (MobaShopNPC npc : _entities.values())
		{
			if (npc.getStand().equals(event.GetDamageeEntity()))
			{
				npc.getStand().setFireTicks(0);
				event.SetCancelled("Shop NPC");
				return;
			}
		}
	}

	public void purchaseItem(Player player, MobaItem item)
	{
		List<MobaItem> owned = _upgrades.get(player);
		MobaShopCategory category = getCategory(item);

		if (category == null)
		{
			return;
		}

		if (!category.isAllowingMultiple())
		{
			owned.removeIf(previousItem -> getCategory(previousItem) == category);
		}

		player.sendMessage(F.main("Game", "Purchased " + F.greenElem(item.getItem().getItemMeta().getDisplayName()) + "."));
		_host.getGoldManager().removeGold(player, item.getCost());
		owned.add(item);

		// The respawn event needs to be called here so that effects like "Total Health Increase" will work straight away, instead of after the next respawn,
		// Prevents infinite speed
		double currentHealth = player.getHealth();
		player.setWalkSpeed(0.2F);
		player.setMaxHealth(20);

		PlayerGameRespawnEvent fakeEvent = new PlayerGameRespawnEvent(_host, player);

		for (MobaItem ownedItem : owned)
		{
			if (ownedItem.getEffects() != null)
			{
				ownedItem.getEffects().forEach(effect -> effect.onRespawn(fakeEvent, true));
			}
		}

		HeroKit kit = (HeroKit) _host.GetKit(player);

		// If we aren't tracking purchases then after we give the item remove it.
		if (category.isTrackingPurchases())
		{
			kit.GiveItems(player);
		}
		else
		{
			kit.giveConsumables(player);
			owned.remove(item);
		}

		player.setHealth(Math.min(currentHealth, player.getMaxHealth()));
	}

	public boolean ownsItem(Player player, MobaItem item)
	{
		return _upgrades.get(player).contains(item);
	}

	public List<MobaItem> getOwnedItems(Player player)
	{
		_upgrades.putIfAbsent(player, new ArrayList<>());
		return _upgrades.get(player);
	}

	private MobaShopCategory getCategory(MobaItem item)
	{
		for (MobaShopMenu menu : _roleMenus.values())
		{
			for (MobaShopCategory category : menu.getCategories())
			{
				if (category.getItems().contains(item))
				{
					return category;
				}
			}
		}

		return null;
	}

	public void clearPurchases(Player player)
	{
		_upgrades.put(player, new ArrayList<>());
	}

	@EventHandler
	public void playerDeath(PlayerDeathEvent event)
	{
		List<MobaItem> owned = _upgrades.get(event.getEntity());

		owned.removeIf(item ->
		{
			MobaShopCategory category = getCategory(item);

			return category == null || category.isDroppingOnDeath();
		});
	}

	/*
		Allow players to access the shop through an item
	 */

	@EventHandler
	public void interactShopItem(PlayerInteractEvent event)
	{
		if (event.getItem() == null || event.getItem().getType() != Material.GOLD_INGOT)
		{
			return;
		}

		openShop(event.getPlayer());
	}

	/*
		Remove empty potions
	 */
	@EventHandler
	public void removeEmptyPotions(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}

		for (Player player : _host.GetPlayers(true))
		{
			player.getInventory().remove(Material.GLASS_BOTTLE);
		}
	}

	/*
		Handle MobaItem events
	 */

	@EventHandler
	public void ammoGive(AmmoGiveEvent event)
	{
		Player player = event.getPlayer();
		List<MobaItem> items = _upgrades.get(player);

		if (items == null)
		{
			return;
		}

		for (MobaItem item : items)
		{
			if (item.getEffects() == null)
			{
				continue;
			}

			for (MobaItemEffect effect : item.getEffects())
			{
				effect.onAmmoGive(event);
			}
		}
	}

	@EventHandler
	public void cooldownCheck(CooldownCalculateEvent event)
	{
		Player player = event.getPlayer();
		List<MobaItem> items = _upgrades.get(player);

		if (items == null)
		{
			return;
		}

		for (MobaItem item : items)
		{
			if (item.getEffects() == null)
			{
				continue;
			}

			for (MobaItemEffect effect : item.getEffects())
			{
				effect.onCooldownCheck(event);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void damage(CustomDamageEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}

		Player damagee = event.GetDamageePlayer();
		Player damager = event.GetDamagerPlayer(true);

		if (damagee == null || damager == null)
		{
			return;
		}

		List<MobaItem> items = _upgrades.get(damager);

		if (items == null)
		{
			return;
		}

		for (MobaItem item : items)
		{
			if (item.getEffects() == null)
			{
				continue;
			}

			for (MobaItemEffect effect : item.getEffects())
			{
				effect.onDamage(event);
			}
		}
	}


	@EventHandler
	public void combatDeath(CombatDeathEvent event)
	{
		CombatComponent component = event.GetLog().GetKiller();

		if (component == null || !component.IsPlayer() || !(event.GetEvent().getEntity() instanceof Player))
		{
			return;
		}

		Player killed = (Player) event.GetEvent().getEntity();
		Player killer = UtilPlayer.searchExact(component.getUniqueIdOfEntity());

		if (killer == null)
		{
			return;
		}

		List<MobaItem> items = _upgrades.get(killer);

		if (items == null)
		{
			return;
		}

		for (MobaItem item : items)
		{
			if (item.getEffects() == null)
			{
				continue;
			}

			for (MobaItemEffect effect : item.getEffects())
			{
				effect.onDeath(killed, killer);
			}
		}
	}

	@EventHandler
	public void hpRegeneration(MobaHPRegenEvent event)
	{
		Player player = event.getPlayer();
		List<MobaItem> items = _upgrades.get(player);

		if (items == null)
		{
			return;
		}

		for (MobaItem item : items)
		{
			if (item.getEffects() == null)
			{
				continue;
			}

			for (MobaItemEffect effect : item.getEffects())
			{
				effect.onHPRegen(event);
			}
		}
	}

	@EventHandler
	public void hpOther(MobaHPRegenEvent event)
	{
		if (event.getSource() == null)
		{
			return;
		}

		List<MobaItem> items = _upgrades.get(event.getSource());

		if (items == null)
		{
			return;
		}

		for (MobaItem item : items)
		{
			if (item.getEffects() == null)
			{
				continue;
			}

			for (MobaItemEffect effect : item.getEffects())
			{
				effect.onHPRegenOthers(event);
			}
		}
	}

	@EventHandler
	public void repawn(PlayerGameRespawnEvent event)
	{
		Player player = event.GetPlayer();
		List<MobaItem> items = _upgrades.get(player);

		if (items == null)
		{
			return;
		}

		for (MobaItem item : items)
		{
			if (item.getEffects() == null)
			{
				continue;
			}

			for (MobaItemEffect effect : item.getEffects())
			{
				effect.onRespawn(event, false);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void conditionApply(ConditionApplyEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}

		LivingEntity entity = event.GetCondition().GetEnt();

		if (!(entity instanceof Player))
		{
			return;
		}

		Player player = (Player) entity;
		List<MobaItem> items = _upgrades.get(player);

		if (items == null)
		{
			return;
		}

		for (MobaItem item : items)
		{
			if (item.getEffects() == null)
			{
				continue;
			}

			for (MobaItemEffect effect : item.getEffects())
			{
				effect.onCondition(event);
			}
		}
	}

	public Moba getHost()
	{
		return _host;
	}
}
