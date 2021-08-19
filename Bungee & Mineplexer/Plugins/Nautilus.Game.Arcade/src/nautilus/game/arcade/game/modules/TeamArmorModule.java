package nautilus.game.arcade.game.modules;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilItem;
import mineplex.core.itemstack.ItemBuilder;

import nautilus.game.arcade.events.PlayerKitGiveEvent;
import nautilus.game.arcade.game.GameTeam;

/**
 * This module handles setting armor for team games such as Micro Battles. It allows you to set the player's armor
 * to be the team's color. It also allows you to add an chestplate to players' hotbar representing their team.
 *
 * These events are listening at MONITOR because they (should) be strict enough that it should not be possible to
 * interfere with any other listeners
 */
public class TeamArmorModule extends Module
{
	private boolean _giveTeamArmor = false;
	private boolean _giveHotbarItem = false;

	private Set<String> _hotbarNames = new HashSet<>();
	private Set<String> _armorNames = new HashSet<>();

	public TeamArmorModule giveTeamArmor()
	{
		_giveTeamArmor = true;
		return this;
	}

	public TeamArmorModule giveHotbarItem()
	{
		_giveHotbarItem = true;
		return this;
	}

	public TeamArmorModule dontGiveTeamArmor()
	{
		_giveTeamArmor = false;
		return this;
	}

	public TeamArmorModule dontGiveHotbarItem()
	{
		_giveHotbarItem = false;
		return this;
	}

	@EventHandler
	public void giveArmor(PlayerKitGiveEvent event)
	{
		apply(event.getPlayer());
	}
	
	public void apply(Player player)
	{
		GameTeam gameTeam = getGame().GetTeam(player);
		if (gameTeam == null)
			return;

		Color color = gameTeam.GetColorBase();

		if (_giveTeamArmor)
		{
			String itemName = gameTeam.GetColor() + C.Bold + "Team Armor";
			_armorNames.add(itemName);
			player.getInventory().setHelmet(new ItemBuilder(Material.LEATHER_HELMET).setColor(color).setTitle(itemName).setUnbreakable(true).build());
			player.getInventory().setChestplate(new ItemBuilder(Material.LEATHER_CHESTPLATE).setColor(color).setTitle(itemName).setUnbreakable(true).build());
			player.getInventory().setLeggings(new ItemBuilder(Material.LEATHER_LEGGINGS).setColor(color).setTitle(itemName).setUnbreakable(true).build());
			player.getInventory().setBoots(new ItemBuilder(Material.LEATHER_BOOTS).setColor(color).setTitle(itemName).setUnbreakable(true).build());
		}

		if (_giveHotbarItem && getGame().InProgress())
		{
			String teamName = gameTeam.GetFormattedName();
			_hotbarNames.add(teamName);
			player.getInventory().setItem(8, new ItemBuilder(Material.LEATHER_CHESTPLATE).setColor(color).setTitle(teamName).setUnbreakable(true).build());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void disallowDrop(PlayerDropItemEvent event)
	{
		if (!_giveTeamArmor && !_giveHotbarItem)
			return;

		if (!_hotbarNames.contains(UtilItem.getDisplayName(event.getItemDrop().getItemStack())))
			return;

		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void disallowEquip(PlayerInteractEvent event)
	{
		if (!_giveHotbarItem)
			return;

		if (!UtilEvent.isAction(event, UtilEvent.ActionType.R))
			return;

		if (!_hotbarNames.contains(UtilItem.getDisplayName(event.getItem())))
			return;

		event.setCancelled(true);

		getGame().getArcadeManager().runSyncLater(() ->
		{
			event.getPlayer().updateInventory();
		}, 1L);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void disallowMoveHotbar(InventoryClickEvent event)
	{
		if (!_giveHotbarItem)
			return;

		if (!getGame().InProgress())
			return;

		if (!UtilInv.shouldCancelEvent(event, item -> _hotbarNames.contains(UtilItem.getDisplayName(item))))
			return;

		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void disallowMoveArmor(InventoryClickEvent event)
	{
		if (!_giveTeamArmor)
			return;

		if (!getGame().InProgress())
			return;

		if (!UtilInv.shouldCancelEvent(event, item -> _armorNames.contains(UtilItem.getDisplayName(item))))
			return;

		event.setCancelled(true);
	}
}
