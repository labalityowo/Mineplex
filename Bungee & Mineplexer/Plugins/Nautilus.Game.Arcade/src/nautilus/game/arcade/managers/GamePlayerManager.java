package nautilus.game.arcade.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTabTitle;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.game.kit.upgrade.KitStatLog;
import mineplex.core.party.PartyManager;
import mineplex.core.recharge.Recharge;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.events.PlayerKitApplyEvent;
import nautilus.game.arcade.events.PlayerStateChangeEvent;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.GameTeam;

public class GamePlayerManager implements Listener
{

	public static final String FOOTER = "Visit " + C.cGreen + "www.mineplex.com" + C.Reset + " for News, Forums and Shop";
	public static String getHeader(Game game)
	{
		return C.cGoldB + (game == null ? "Waiting For Players" : game.GetType().GetLobbyName() + (game.GetMode() == null ? "" : C.cWhiteB + " - " + C.cYellowB + game.GetMode()));
	}

	final ArcadeManager Manager;
	
	private static final int TEAMMATE_MESSAGE_DELAY = 40;
	
	public GamePlayerManager(ArcadeManager manager)
	{
		Manager = manager;

		Manager.getPluginManager().registerEvents(this, Manager.getPlugin());
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void PlayerDeath(CombatDeathEvent event)
	{
		//Don't actually die
		event.GetEvent().getEntity().setHealth(event.GetEvent().getEntity().getMaxHealth());

		//Dont display message
		if (Manager.GetGame() != null)
			event.SetBroadcastType(Manager.GetGame().GetDeathMessageType());

		//Colors
		if (event.GetLog().GetKiller() != null)
		{
			Player player = UtilPlayer.searchExact(event.GetLog().GetKiller().GetName());
			if (player != null)
				event.GetLog().SetKillerColor(Manager.GetColor(player)+"");
		}

		Player player = event.GetEvent().getEntity();
		event.GetLog().SetKilledColor(Manager.GetColor(player) + "");
	}

	@EventHandler
	public void PlayerJoin(PlayerJoinEvent event)
	{
		final Player player = event.getPlayer();

		Game game = Manager.GetGame();

		//Player List
		UtilTabTitle.setHeaderAndFooter(player, getHeader(game), FOOTER);

		//Lobby Spawn
		if (game == null || !game.InProgress())
		{
			Manager.Clear(player);
			Manager.getScoreboardManager().getScoreboards().entrySet().stream().filter(ent -> Bukkit.getPlayer(ent.getKey()) != null).forEach(ent -> Bukkit.getPlayer(ent.getKey()).setScoreboard(ent.getValue().getHandle()));
			player.teleport(Manager.GetLobby().getSpawn());
			//Load default kit for this game
			if (game != null && game.GetType().name().toLowerCase().contains("champions"))
			{
				return;
			}
			player.getInventory().setItem(PartyManager.INTERFACE_SLOT, PartyManager.INTERFACE_ITEM);
			return;
		}

		//Game Spawn
		if (game.IsAlive(player))
		{
			Location loc = game.GetLocationStore().remove(player.getName());
			if (loc != null && !loc.getWorld().getName().equalsIgnoreCase("world"))
			{
				player.teleport(loc);
			}
			else
			{
				Manager.Clear(player);
				player.teleport(Manager.GetGame().GetTeam(player).GetSpawn());
			}
		}
		else
		{
			Manager.Clear(player);
			Manager.addSpectator(player, true);
			UtilPlayer.message(player, F.main("Game", game.GetName() + " is in progress, please wait for next game!"));
		}

		if (!game.UseCustomScoreboard)
		{
			player.setScoreboard(Manager.GetGame().GetScoreboard().getScoreboard());
		}
	}

	@EventHandler
	public void PlayerRespawn(PlayerRespawnEvent event)
	{
		if (Manager.GetGame() == null || !Manager.GetGame().InProgress())
		{
			event.setRespawnLocation(Manager.GetLobby().getSpawn());
			return;
		}

		Player player = event.getPlayer();

		if (Manager.GetGame().IsAlive(player))
		{
			event.setRespawnLocation(Manager.GetGame().GetTeam(player).GetSpawn());
		}
		else
		{
			Manager.addSpectator(player, true);

			event.setRespawnLocation(Manager.GetGame().GetSpectatorLocation());
		}
	}
	
	@EventHandler
	public void PlayerStateChange(PlayerStateChangeEvent event)
	{
		Recharge.Instance.useForce(event.GetPlayer(), "Return to Hub", 4000);
	}
	
	@EventHandler
	public void DisallowCreativeClick(InventoryClickEvent event)
	{
		if (Manager.GetGame() == null || !Manager.GetGame().InProgress() || Manager.GetGameHostManager().isEventServer() || Manager.GetGame().GetType() == GameType.Build || Manager.GetGame().GetType() == GameType.BuildMavericks)
			return;
		
		if ((event.getInventory().getType() == InventoryType.CREATIVE || event.getInventory().getType() == InventoryType.PLAYER) && !event.getWhoClicked().isOp())
		{
			event.setCancelled(true);
			event.getWhoClicked().closeInventory();
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void TeamInteract(PlayerInteractEntityEvent event)
	{
		if (event.getRightClicked() == null)
			return;

		Player player = event.getPlayer();

		GameTeam team = Manager.GetLobby().GetClickedTeam(event.getRightClicked());

		if (team == null)
			return;

		//Observer
		if (Manager.IsObserver(player))
		{
			UtilPlayer.message(player, F.main("Game", "Spectators cannot partake in games."));
			return;
		}
		
		TeamClick(player, team);
	}

	@EventHandler
	public void TeamDamage(CustomDamageEvent event)
	{
		Player player = event.GetDamagerPlayer(false);
		if (player == null)		return;

		LivingEntity target = event.GetDamageeEntity();

		GameTeam team = Manager.GetLobby().GetClickedTeam(target);

		if (team == null)
			return;
		
		//Observer
		if (Manager.IsObserver(player))
		{
			UtilPlayer.message(player, F.main("Game", "Spectators cannot partake in games."));
			return;
		}
		
		TeamClick(player, team);
	}

	public void TeamClick(Player player, GameTeam team)
	{
		Game game = Manager.GetGame();

		if (game == null || game.GetState() != GameState.Recruit || !game.HasTeam(team))
		{
			return;
		}

		game.getTeamModule().addPlayerQueue(player, team);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void TeleportCommand(PlayerCommandPreprocessEvent event)
	{
		Game game = Manager.GetGame();
		if (game == null)
			return;

		Player player = event.getPlayer();

		if (!event.getMessage().toLowerCase().startsWith("/spec"))
			return;

		if (!game.InProgress())
		{
			return;
		}

		event.setCancelled(true);

		if (game.IsAlive(player) || !Manager.isSpectator(player))
		{
			UtilPlayer.message(player, F.main("Game", "Only Spectators can use this command."));
			return;
		}

		String[] tokens = event.getMessage().split(" ");

		if (tokens.length != 2)
		{
			UtilPlayer.message(player, F.main("Game", "Invalid Input. " + F.elem("/spec <Name>") + "."));
			return;
		}

		Player target = UtilPlayer.searchOnline(player, tokens[1], true);
		if (target != null)
		{
			if (Manager.isVanished(target))
			{
				UtilPlayer.messageSearchOnlineResult(player, tokens[1], 0);
				return;
			}

			UtilPlayer.message(player, F.main("Game", "You teleported to " + F.name(target.getName()) + "."));
			player.teleport(target);
		}
	}
	
	@EventHandler
	public void sendTeammateDetails(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Prepare)
		{
			return;
		}
		
		Game game = Manager.GetGame();
		
		if (!game.ShowTeammateMessage || game.GetTeamList().size() == 1)
		{
			return;
		}
		
		Manager.runSyncLater(() ->
		{
			for (Player player : game.GetPlayers(true))
			{									
				GameTeam team = game.GetTeam(player);
				Player bestTeamMember = null;
				
				if (team == null)
				{
					continue;
				}
				
				for (Player teamMember : team.GetPlayers(true))
				{
					if (player.equals(teamMember))
					{
						continue;
					}
					
					bestTeamMember = teamMember;
				}
				
				if (bestTeamMember == null)
				{
					UtilTextMiddle.display(C.cRedB + "No one", "You don\'t have a teammate :(", 10, 50, 10, player);
					continue;
				}
				
				UtilTextMiddle.display(null, team.GetColor() + bestTeamMember.getName() + " is your teammate", 10, 50, 10, player);
			}
			
		}, TEAMMATE_MESSAGE_DELAY);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void kitApply(PlayerKitApplyEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}

		Player player = event.getPlayer();
		Manager.getMineplexGameManager().getKitStatLog()
				.computeIfAbsent(player, k -> new KitStatLog())
				.getKitsUsed().add(event.getKit().getGameKit());
	}
}