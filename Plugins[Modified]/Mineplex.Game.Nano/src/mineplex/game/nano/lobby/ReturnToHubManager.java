package mineplex.game.nano.lobby;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.command.CommandBase;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.portal.GenericServer;
import mineplex.core.portal.Intent;
import mineplex.core.portal.Portal;
import mineplex.core.recharge.Recharge;
import mineplex.game.nano.GameManager;
import mineplex.game.nano.game.Game;
import mineplex.game.nano.game.Game.GameState;
import mineplex.game.nano.game.event.GameStateChangeEvent;
import mineplex.game.nano.game.event.PlayerStateChangeEvent;

@ReflectivelyCreateMiniPlugin
public class ReturnToHubManager extends GameManager
{

	public enum Perm implements Permission
	{
		COMMANDS
	}

	private static final ItemStack HUB_CLOCK = new ItemBuilder(Material.WATCH)
			.setTitle(C.cGreen + "Cash Out")
			.addLore("", "Click to return to the hub and", "receive all your rewards from this game!")
			.build();
	private static final ItemStack FAVOURITE_STAR = new ItemBuilder(Material.NETHER_STAR)
			.setTitle(C.cPurple + "Favorite Game")
			.addLore("", "Click to open the favorite", "games menu!")
			.build();

	private ReturnToHubManager()
	{
		super("Return To Hub");

		generatePermissions();
	}

	private void generatePermissions()
	{
		PermissionGroup.PLAYER.setPermission(Perm.COMMANDS, true, true);
	}

	@Override
	public void addCommands()
	{
		addCommand(new CommandBase<ReturnToHubManager>(this, Perm.COMMANDS, "hub", "lobby", "leave")
		{
			@Override
			public void Execute(Player caller, String[] args)
			{
				sendToHub(caller);
			}
		});
		addCommand(new CommandBase<ReturnToHubManager>(this, Perm.COMMANDS, "favorite", "fav")
		{
			@Override
			public void Execute(Player caller, String[] args)
			{
				openShop(caller);
			}
		});
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void playerJoin(PlayerJoinEvent event)
	{
		Game game = _manager.getGame();
		Player player = event.getPlayer();

		if (game != null && game.isAlive(player))
		{
			return;
		}

		giveItems(player);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void playerOut(PlayerStateChangeEvent event)
	{
		if (event.isAlive())
		{
			return;
		}

		Player player = event.getPlayer();

		runSyncLater(() ->
		{
			if (player.isOnline() && !_manager.getGame().isAlive(player))
			{
				giveItems(player);
			}
		}, 20);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void prepare(GameStateChangeEvent event)
	{
		if (event.getState() != GameState.Prepare)
		{
			return;
		}

		for (Player player : _manager.getSpectators())
		{
			giveItems(player);
		}
	}

	@EventHandler
	public void playerInteract(PlayerInteractEvent event)
	{
		if (!UtilEvent.isAction(event, ActionType.R))
		{
			return;
		}

		Player player = event.getPlayer();
		ItemStack itemStack = player.getItemInHand();

		if (HUB_CLOCK.equals(itemStack))
		{
			sendToHub(player);
		}
		else if (FAVOURITE_STAR.equals(itemStack))
		{
			openShop(player);
		}
	}

	public void giveItems(Player player)
	{
		PlayerInventory inventory = player.getInventory();

		inventory.setItem(8, HUB_CLOCK);
		_manager.getCosmeticManager().giveInterfaceItem(player);
		inventory.setItem(6, FAVOURITE_STAR);
	}

	private void sendToHub(Player player)
	{
		if (!Recharge.Instance.use(player, "Return To Hub", 1000, false, false))
		{
			return;
		}

		Portal.getInstance().sendPlayerToGenericServer(player, GenericServer.HUB, Intent.PLAYER_REQUEST);
	}

	private void openShop(Player player)
	{
		if (!Recharge.Instance.use(player, "Open Favourite", 1000, false, false))
		{
			return;
		}

		_manager.getFavourite().getShop().attemptShopOpen(player);
	}
}
