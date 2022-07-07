package nautilus.game.arcade.game.games.skywars.kits.perks;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilInv;
import mineplex.core.mission.MissionTrackerType;

import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.games.skywars.TeamSkywars;
import nautilus.game.arcade.kit.Perk;

public abstract class SkywarsPerk extends Perk
{

	final ItemStack _itemStack;
	private final Set<Player> _used;

	SkywarsPerk(String name, ItemStack itemStack)
	{
		super(name);

		_itemStack = itemStack;
		_used = new HashSet<>();
	}

	public abstract void onUseItem(Player player);

	protected boolean isTeamDamage(Player player1, Player player2)
	{
		Game game = Manager.GetGame();
		return game instanceof TeamSkywars && game.GetTeam(player1).equals(game.GetTeam(player2));
	}

	@Override
	public void unregisteredEvents()
	{
		_used.clear();
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void playerInteract(PlayerInteractEvent event)
	{
		if (event.isCancelled() || !UtilEvent.isAction(event, ActionType.R))
		{
			return;
		}

		Player player = event.getPlayer();
		ItemStack itemStack = player.getItemInHand();
		Block block = event.getClickedBlock();

		if (!hasPerk(player) || itemStack == null || !itemStack.isSimilar(_itemStack) || UtilBlock.usable(block))
		{
			return;
		}

		event.setCancelled(true);
		_used.add(player);
		onUseItem(player);
	}

	@EventHandler
	public void blockPlace(BlockPlaceEvent event)
	{
		if (hasPerk(event.getPlayer()) && _itemStack.isSimilar(event.getItemInHand()))
		{
			event.getPlayer().sendMessage(F.main("Game", "You cannot place your " + F.name("Skill Item") + "."));
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void playerDropItem(PlayerDropItemEvent event)
	{
		if (hasPerk(event.getPlayer()) && event.getItemDrop().getItemStack().isSimilar(_itemStack))
		{
			event.getPlayer().sendMessage(F.main("Game", "You cannot drop your " + F.name("Skill Item") + "."));
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void disallowMovement(InventoryClickEvent event)
	{
		if (hasPerk((Player) event.getWhoClicked()))
		{
			UtilInv.DisallowMovementOf(event, _itemStack.getItemMeta().getDisplayName(), _itemStack.getType(), _itemStack.getData().getData(), true);
		}
	}

	@EventHandler
	public void playerDeath(PlayerDeathEvent event)
	{
		event.getDrops().removeIf(itemStack -> itemStack.isSimilar(_itemStack));
		_used.remove(event.getEntity());
	}

	@EventHandler
	public void end(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.End)
		{
			return;
		}

		for (Player player : event.GetGame().getWinners())
		{
			if (hasPerk(player) && !_used.contains(player))
			{
				Manager.getMissionsManager().incrementProgress(player, 1, MissionTrackerType.SW_NO_ABILITIES, event.GetGame().GetType().getDisplay(), null);
			}
		}
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		_used.remove(event.getPlayer());
	}
}
