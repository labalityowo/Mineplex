package mineplex.hub.hubgame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.donation.DonationManager;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.event.GadgetEnableEvent;
import mineplex.core.gadget.types.GadgetType;
import mineplex.core.newnpc.NPC;
import mineplex.core.newnpc.NewNPCManager;
import mineplex.core.newnpc.event.NPCInteractEvent;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.hub.HubManager;
import mineplex.hub.hubgame.CycledGame.GameState;
import mineplex.hub.hubgame.event.HubGamePlayerDeathEvent;
import mineplex.hub.hubgame.ui.HubGameShop;
import mineplex.hub.player.HubPlayerManager;

@ReflectivelyCreateMiniPlugin
public class HubGameManager extends MiniPlugin
{

	private static final int MIN_PLAYERS_COUNTDOWN = 20;
	private static final int MAX_PLAYERS_COUNTDOWN = 5;
	private static final int PREPARE_COUNTDOWN = 5;
	private static final String HEADER_FOOTER = C.cDGreen + C.Strike + "================================================";

	public static String getHeaderFooter()
	{
		return HEADER_FOOTER;
	}

	private final CoreClientManager _clientManager;
	private final DonationManager _donationManager;
	private final GadgetManager _gadgetManager;
	private final HubManager _hubManager;
	private final HubPlayerManager _hotbarManager;
	private final NewNPCManager _npcManager;

	private final HubGameShop _shop;
	private final List<HubGame> _games;
	private final Location _teleport;

	private HubGameManager()
	{
		super("Hub Games");

		_clientManager = require(CoreClientManager.class);
		_donationManager = require(DonationManager.class);
		_gadgetManager = require(GadgetManager.class);
		_hubManager = require(HubManager.class);
		_hotbarManager = require(HubPlayerManager.class);
		_npcManager = require(NewNPCManager.class);

		_shop = new HubGameShop(this, _clientManager, _donationManager);
		_games = new ArrayList<>();
		_teleport = _hubManager.getWorldData().getSpongeLocation("TELEPORT " + _moduleName);
		UtilAlg.lookAtNearest(_teleport, _hubManager.getLookAt());

		runSyncLater(this::spawnNPCs, 50);
	}

	public void addGame(HubGame game)
	{
		_games.add(game);
	}

	private void spawnNPCs()
	{
		_games.forEach(game -> _npcManager.spawnNPCs(game.getGameType().name(), game::setNpc));
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		_games.forEach(game -> game.onCleanupPlayer(event.getPlayer()));
	}

	@EventHandler
	public void npcInteract(NPCInteractEvent event)
	{
		if (event.isCancelled() || !Recharge.Instance.use(event.getPlayer(), "Hub Game NPC Interact", 1000, false, false))
		{
			return;
		}

		NPC npc = event.getNpc();

		for (HubGame game : _games)
		{
			if (!npc.getMetadata().equals(game.getGameType().name()))
			{
				continue;
			}

			Player player = event.getPlayer();

			if (game instanceof CycledGame)
			{
				CycledGame cycledGame = (CycledGame) game;
				List<Player> queuedPlayers = cycledGame.getQueuedPlayers();

				if (queuedPlayers.contains(player))
				{
					leaveQueue(cycledGame, player, false);
				}
				else
				{
					joinQueue(cycledGame, player);
				}
			}

			return;
		}
	}

	private void disableGadgets(Player player, GadgetType type)
	{
		_gadgetManager.getGadgets(type).forEach(gadget -> gadget.disable(player));
	}

	public void joinQueue(CycledGame game, Player player)
	{
		for (HubGame other : _games)
		{
			if (other instanceof CycledGame && ((CycledGame) other).getQueuedPlayers().remove(player))
			{
				leaveQueue((CycledGame) other, player, true);
			}
		}

		clearPlayer(player);

		disableGadgets(player, GadgetType.MORPH);
		disableGadgets(player, GadgetType.MOUNT);
		disableGadgets(player, GadgetType.COSTUME);
		disableGadgets(player, GadgetType.ITEM);
		disableGadgets(player, GadgetType.BALLOON);
		disableGadgets(player, GadgetType.FLAG);
		disableGadgets(player, GadgetType.HAT);

		player.sendMessage(F.main(_moduleName, "You have joined the queue for " + F.name(getGameName(game)) + "."));
		game.getQueuedPlayers().add(player);
		game.onPlayerQueue(player);
		informQueuePosition(game, player);
	}

	public void leaveQueue(CycledGame game, Player player, boolean forAnother)
	{
		if (!forAnother)
		{
			_hotbarManager.giveHotbar(player);
		}

		player.sendMessage(F.main(_moduleName, "You have left the queue for " + F.name(getGameName(game)) + "."));
		game.getQueuedPlayers().remove(player);
		game.onPlayerLeaveQueue(player);
	}

	private void informQueuePosition(CycledGame game, Player player)
	{
		int position = game.getQueuedPlayers().indexOf(player);

		if (position == -1)
		{
			return;
		}

		player.sendMessage(F.main(_moduleName, "Your position in the queue for " +
				F.name(getGameName(game)) + " is " +
				F.elem((position + 1)) + "/" +
				F.elem(game.getQueuedPlayers().size()) +
				"!"));
	}

	@EventHandler
	public void updateLobby(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		for (HubGame game : _games)
		{
			if (!(game instanceof CycledGame))
			{
				continue;
			}

			CycledGame cycledGame = (CycledGame) game;
			HubGameType gameType = game.getGameType();

			if (cycledGame.getGameState() != GameState.Waiting)
			{
				continue;
			}

			int countdown = cycledGame.getCountdown();
			int queuedSize = cycledGame.getQueuedPlayers().size();

			// Initial start check
			if (countdown == -1 && queuedSize >= gameType.getMinPlayers())
			{
				int waitTime = MIN_PLAYERS_COUNTDOWN;

				if (queuedSize >= gameType.getMaxPlayers())
				{
					waitTime = MAX_PLAYERS_COUNTDOWN;
				}

				cycledGame.setCountdown(waitTime);

				for (Player player : cycledGame.getQueuedPlayers())
				{
					player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 0.7F);
				}
			}
			else if (countdown != -1 && isEnoughPlayers(cycledGame))
			{
				// Start Game
				if (countdown == 0 && isEnoughPlayers(cycledGame))
				{
					List<Player> players = cycledGame.getNextPlayers();

					cycledGame.getAlivePlayers().addAll(players);
					cycledGame.getAllPlayers().addAll(players);
					cycledGame.getQueuedPlayers().removeAll(players);

					cycledGame.setCountdown(PREPARE_COUNTDOWN);
					cycledGame.setState(GameState.Prepare);

					for (Player player : cycledGame.getQueuedPlayers())
					{
						informQueuePosition(cycledGame, player);
					}
				}
				// Countdown
				else if (countdown > 0)
				{
					cycledGame.setCountdown(countdown - 1);

					if (countdown < 10 || countdown % 10 == 0)
					{
						for (Player player : cycledGame.getNextPlayers())
						{
							player.playSound(player.getLocation(), Sound.CLICK, 1, 0.6F);
							player.sendMessage(F.main(_moduleName, F.elem(countdown) + " second" + (countdown == 1 ? "" : "s") + " until the game starts."));
							UtilTextMiddle.display("", C.cGreen + countdown, 0, 30, 0, player);
						}
					}
				}
			}
		}
	}

	// Ensure that prepare is run before the lobby update
	@EventHandler(priority = EventPriority.LOW)
	public void updatePrepare(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		for (HubGame game : _games)
		{
			if (!(game instanceof CycledGame))
			{
				continue;
			}

			CycledGame cycledGame = (CycledGame) game;

			if (cycledGame.getGameState() != GameState.Prepare)
			{
				continue;
			}

			int countdown = cycledGame.getCountdown();

			if (countdown == 0)
			{
				for (Player player : cycledGame.getAllPlayers())
				{
					player.playSound(player.getLocation(), Sound.NOTE_PLING, 1, 1.2F);
				}

				cycledGame.setState(GameState.Live);
			}
			else if (countdown <= 3)
			{
				for (Player player : cycledGame.getAllPlayers())
				{
					player.playSound(player.getLocation(), Sound.CLICK, 1, 0.4F);
					UtilTextMiddle.display("", C.cRed + countdown, 0, 30, 0, player);
				}
			}

			cycledGame.setCountdown(countdown - 1);
		}
	}

	@EventHandler
	public void updateEnd(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}

		for (HubGame game : _games)
		{
			if (!(game instanceof CycledGame))
			{
				continue;
			}

			CycledGame cycledGame = (CycledGame) game;

			if (cycledGame.isLive() && cycledGame.endCheck())
			{
				for (Player player : cycledGame.getAlivePlayers())
				{
					cycledGame.onPlayerDeath(player, true);
				}

				Collections.reverse(cycledGame.getPlaces());

				cycledGame.onEnd();
				cycledGame.setState(GameState.End);

				cycledGame.onCleanup();
				cycledGame.setState(GameState.Waiting);
			}
		}
	}

	@EventHandler
	public void gadgetEnable(GadgetEnableEvent event)
	{
		cancelIfPlaying(event.getPlayer(), event);
	}

	@EventHandler
	public void inventoryOpen(InventoryOpenEvent event)
	{
		if (event.getInventory().equals(event.getPlayer().getInventory()))
		{
			return;
		}

		cancelIfPlaying((Player) event.getPlayer(), event);
	}

	private void cancelIfPlaying(Player player, Cancellable event)
	{
		for (HubGame game : _games)
		{
			if (!(game instanceof CycledGame))
			{
				continue;
			}

			CycledGame cycledGame = (CycledGame) game;

			if (cycledGame.getQueuedPlayers().contains(player) || cycledGame.isAlive(player))
			{
				event.setCancelled(true);
				return;
			}
		}
	}

	private boolean isEnoughPlayers(CycledGame game)
	{
		boolean enough = game.getQueuedPlayers().size() >= game.getGameType().getMinPlayers();

		if (!enough)
		{
			game.setCountdown(-1);

			for (Player player : game.getQueuedPlayers())
			{
				player.sendMessage(F.main(_moduleName, "Not enough players."));
				UtilTextMiddle.display("", C.cRed + "Not enough players", 0, 30, 0, player);
			}
		}

		return enough;
	}

	@EventHandler
	public void playerDeath(HubGamePlayerDeathEvent event)
	{
		Player player = event.getPlayer();
		CycledGame game = event.getGame();

		if (!event.isEnding())
		{
			game.getAlivePlayers().remove(player);
		}

		game.getPlaces().add(player);
		clearPlayer(player);
		player.teleport(game.getSpawn());

		runSyncLater(() -> _hotbarManager.giveHotbar(player), 20);
	}

	private void clearPlayer(Player player)
	{
		UtilPlayer.clearPotionEffects(player);
		UtilPlayer.clearInventory(player);
		player.leaveVehicle();
		player.eject();
		player.setFireTicks(0);
		player.setHealth(player.getMaxHealth());
	}

	public boolean inQueue(Player player)
	{
		return _games.stream()
				.anyMatch(game -> game instanceof CycledGame && ((CycledGame) game).getQueuedPlayers().contains(player));
	}

	public String getGameHeader(CycledGame game)
	{
		return C.cWhiteB + "Game" + C.cYellow + " - " + C.cWhiteB + getGameName(game);
	}

	private String getGameName(CycledGame game)
	{
		return game.getGameType().getName();
	}

	public HubManager getHubManager()
	{
		return _hubManager;
	}

	public CoreClientManager getClientManager()
	{
		return _clientManager;
	}

	public DonationManager getDonationManager()
	{
		return _donationManager;
	}

	public HubGameShop getShop()
	{
		return _shop;
	}

	public Location getTeleport()
	{
		return _teleport;
	}
}
