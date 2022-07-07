package nautilus.game.arcade.managers;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import mineplex.core.bonuses.event.CarlSpinnerEvent;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.core.common.util.UtilTextTop;
import mineplex.core.common.util.UtilTime;
import mineplex.core.disguise.disguises.DisguiseBase;
import mineplex.core.disguise.disguises.DisguiseLiving;
import mineplex.core.disguise.disguises.DisguiseWither;
import mineplex.core.gadget.event.GadgetEnableEvent;
import mineplex.core.gadget.gadgets.morph.MorphWither;
import mineplex.core.gadget.gadgets.mount.types.MountDragon;
import mineplex.core.gadget.types.Gadget;
import mineplex.core.gadget.types.GadgetType;
import mineplex.core.gadget.types.WinEffectGadget;
import mineplex.core.updater.RestartReason;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.RestartServerEvent;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.condition.Condition.ConditionType;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.events.GamePrepareCountdownCommence;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.events.PlayerPrepareTeleportEvent;
import nautilus.game.arcade.events.PlayerStateChangeEvent;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.gametutorial.TutorialPhase;
import nautilus.game.arcade.gametutorial.TutorialText;

public class GameManager implements Listener
{

	final ArcadeManager Manager;
	private int _colorId = 0;

	public GameManager(ArcadeManager manager)
	{
		Manager = manager;

		Manager.getPluginManager().registerEvents(this, Manager.getPlugin());
	}

	@EventHandler
	public void displayIP(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTEST)
		{
			return;
		}

		Game game = Manager.GetGame();

		if ((game != null && !game.InProgress()) || Manager.GetGameCreationManager().getVotingManager().isVoteInProgress())
		{
			ChatColor colour;

			switch (_colorId)
			{
				case 1:
					colour = ChatColor.YELLOW;
					break;
				case 2:
					colour = ChatColor.GREEN;
					break;
				case 3:
					colour = ChatColor.AQUA;
					break;
				default:
					colour = ChatColor.RED;
					break;
			}

			_colorId = (_colorId + 1) % 4;

			String text = colour + C.Bold + "US.MINEPLEX.COM       EU.MINEPLEX.COM";
			double health = 1;

			if (game != null && game.GetCountdown() >= 0 && game.GetCountdown() <= 10)
			{
				health = 1 - game.GetCountdown() / 10D;
			}

			//Display IP
			UtilTextTop.displayProgress(text, health, UtilServer.getPlayers());

			for (Entity pet : Manager.getCosmeticManager().getPetManager().getPets())
			{
				if (pet instanceof LivingEntity)
				{
					DisguiseBase disguise = Manager.GetDisguise().getDisguise((LivingEntity) pet);

					if (disguise instanceof DisguiseWither)
					{
						((DisguiseWither) disguise).setName(text);
						((DisguiseLiving) disguise).setHealth((float) Math.max(0.1, 300 * health));
						disguise.resendMetadata();
					}
				}
			}

			//Name Dragons Appropriately
			for (Gadget mount : Manager.getCosmeticManager().getGadgetManager().getGadgets(GadgetType.MOUNT))
			{
				if (mount instanceof MountDragon)
				{
					((MountDragon) mount).SetName(text);
					((MountDragon) mount).setHealthPercent(health);
				}
			}

			for (Gadget gadget : Manager.getCosmeticManager().getGadgetManager().getGadgets(GadgetType.MORPH))
			{
				if (gadget instanceof MorphWither)
				{
					((MorphWither) gadget).setWitherData(text, health);
				}
			}
		}
	}

	public boolean isInTutorial(boolean checkForTimer)
	{
		if (Manager.GetGame() == null || Manager.GetGame().GetState() != GameState.Prepare)
			return false;

		Game game = Manager.GetGame();

		boolean tutorialSet = false;
		for (GameTeam team : game.GetTeamList())
		{
			if (team.getTutorial() != null)
				tutorialSet = true;
		}
		if (!tutorialSet)
		{
			game.addTutorials();
		}
		boolean finished = true;
		if (game.EnableTutorials)
		{
			for (Player player : UtilServer.getPlayers())
			{
				if (player.getWorld() == Manager.GetLobby().getSpawn().getWorld())
					return true;
			}
			long prepTime = 0;
			for (GameTeam team : game.GetTeamList())
			{
				long timeUsage = 0;
				if (team.getTutorial() != null)
				{
					if (!team.getTutorial().hasStarted())
					{
						team.getTutorial().setTeam(team);
						team.getTutorial().start();
						timeUsage = team.getTutorial().StartAfterTutorial;
						timeUsage = timeUsage + (team.getTutorial().TimeBetweenPhase * team.getTutorial().getPhases().length);
						if (team.getTutorial().CustomEnding)
							timeUsage = timeUsage + team.getTutorial().CustomEndingTime;

						for (TutorialPhase phase : team.getTutorial().getPhases())
						{
							for (TutorialText text : phase.getText())
							{
								timeUsage = timeUsage + (text.getStayTime() * 50);
							}
						}
					}
					if (!team.getTutorial().hasEnded())
					{
						finished = false;
						if (checkForTimer)
						{
							if (team.getTutorial().ShowPrepareTimer)
								finished = true;
							else
								finished = false;
						}
					}
				}
				if (prepTime <= timeUsage)
					prepTime = timeUsage;
			}
			if (prepTime > 0)
				Manager.GetGame().PrepareTime = prepTime;

			return !finished;
		}
		return false;
	}

	@EventHandler
	public void DisplayPrepareTime(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		Game game = Manager.GetGame();

		if (game == null || game.GetState() != GameState.Prepare)
		{
			return;
		}

		if (isInTutorial(true))
		{
			return;
		}

		double percentage = (double) (System.currentTimeMillis() - game.GetStateTime()) / game.PrepareTime;

		UtilTextBottom.displayProgress("Game Start", percentage, UtilTime.MakeStr(Math.max(0, game.PrepareTime - (System.currentTimeMillis() - game.GetStateTime()))), UtilServer.getPlayers());
	}

	@EventHandler
	public void updateGameTutorials(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		Game game = Manager.GetGame();

		if (game == null || game.GetState() != GameState.Prepare)
		{
			return;
		}

		if (game.EnableTutorials)
		{
			for (GameTeam team : game.GetTeamList())
			{
				if (team.getTutorial() != null)
				{
					if (!team.getTutorial().hasEnded() && team.getTutorial().hasStarted())
					{
						team.getTutorial().onTick(team.getTutorial().tick());
					}
				}
			}
		}
	}

	@EventHandler
	public void StateUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		Game game = Manager.GetGame();

		if (game == null)
		{
			return;
		}

		switch (game.GetState())
		{
			case Loading:
				if (UtilTime.elapsed(game.GetStateTime(), 30000))
				{
					System.out.println("Game Load Expired.");
					game.SetState(GameState.Dead);
				}
				break;
			case Recruit:
				int validPlayers = Manager.getValidPlayersForGameStart().size();

				//Stop Countdown!
				if (game.GetCountdown() != -1 && validPlayers < Manager.GetPlayerMin() && !game.GetCountdownForce())
				{
					game.SetCountdown(-1);
					Manager.GetLobby().displayWaiting(false);
				}

				if (Manager.IsGameAutoStart())
				{
					if (validPlayers >= Manager.GetPlayerFull())
					{
						StateCountdown(game, 10, false);
					}
					else if (validPlayers >= Manager.GetPlayerFull() * 0.75D)
					{
						StateCountdown(game, 30, false);
					}
					else if (validPlayers >= Manager.GetPlayerMin())
					{
						StateCountdown(game, 60, false);
					}
					else if (game.GetCountdown() != -1)
					{
						StateCountdown(game, -1, false);
					}
				}
				else if (game.GetCountdown() != -1)
				{
					StateCountdown(game, -1, false);
				}
				break;
			case Prepare:
				if (isInTutorial(false))
				{
					return;
				}

				if (game.CanStartPrepareCountdown())
				{
					if (UtilTime.elapsed(game.GetStateTime(), game.PrepareTime))
					{
						int players = game.GetPlayers(true).size();

						if (game.PlaySoundGameStart)
						{
							for (Player player : UtilServer.getPlayersCollection())
							{
								player.playSound(player.getLocation(), Sound.NOTE_PLING, 2f, 2f);
							}
						}

						if (players < 2)
						{
							game.Announce(C.cWhiteB + game.GetName() + " ended, not enough players!");
							game.SetState(GameState.Dead);
						}
						else
						{
							game.SetState(GameState.Live);
						}
					}
					else
					{
						for (Player player : UtilServer.getPlayersCollection())
						{
							player.playSound(player.getLocation(), Sound.NOTE_STICKS, 1f, 1f);
						}
					}
				}
				break;
			case Live:
			{
				if (game.GameTimeout != -1 && UtilTime.elapsed(game.GetStateTime(), game.GameTimeout) && Manager.IsGameTimeout())
				{
					game.HandleTimeout();
				}

				break;
			}
			case End:
			{
				WinEffectGadget winEffectGadget = game.WinEffectManager.getWinEffect();

				if (UtilTime.elapsed(game.GetStateTime(), winEffectGadget == null ? 10000 : winEffectGadget.getLength()))
				{
					game.WinEffectManager.end();
					game.SetState(GameState.Dead);
				}
				break;
			}
		}
	}

	public void StateCountdown(Game game, int timer, boolean force)
	{
		if (Manager.GetGameHostManager().isPrivateServer() && Manager.GetGameHostManager().isVoteInProgress())
			return;

		if ((game.GetCountdown() <= 5 && game.GetCountdown() >= 0) || timer <= 5)
		{
			Manager.getTitles().forceDisable();
		}

		//Disabling Cosmetics
		if (game.GetCountdown() <= 5 && game.GetCountdown() >= 0 && game.GadgetsDisabled)
		{
			if (Manager.getCosmeticManager().isShowingInterface())
			{
				Manager.getCosmeticManager().setActive(false);
				Manager.getCosmeticManager().disableItemsForGame();
			}
		}

		if (force)
			game.SetCountdownForce(true);

		//Initialise Countdown
		if (force)
			game.SetCountdownForce(true);

		//Start  Timer
		if (game.GetCountdown() == -1)
			game.SetCountdown(timer + 1);

		//Decrease Timer
		if (game.GetCountdown() > timer + 1 && timer != -1)
			game.SetCountdown(timer + 1);

		//Countdown--
		if (game.GetCountdown() > 0)
			game.SetCountdown(game.GetCountdown() - 1);

		//Inform Countdown
		if (game.GetCountdown() > 0)
		{
			Manager.GetLobby().writeGameLine("starting in " + game.GetCountdown() + "...", 3, 159, (byte) 13);
		}
		else
		{
			Manager.GetLobby().writeGameLine("game in progress", 3, 159, (byte) 13);
		}

		if (game.GetCountdown() > 0 && game.GetCountdown() <= 10)
		{
			for (Player player : UtilServer.getPlayersCollection())
			{
				player.playSound(player.getLocation(), Sound.NOTE_PLING, 1f, 1f);
			}
		}

		//Countdown Ended
		if (game.GetCountdown() == 0)
		{
			game.SetState(GameState.Prepare);
		}
	}

	@EventHandler
	public void gadgetDisable(GadgetEnableEvent event)
	{
		Game game = Manager.GetGame();

		if (game != null && game.GetCountdown() <= 5 && game.GetCountdown() >= 0 && game.GadgetsDisabled)
		{
			event.setCancelled(true);
			event.setShowMessage(false);
		}
	}

	@EventHandler
	public void restartServerCheck(RestartServerEvent event)
	{
		if (Manager.GetGame() == null || Manager.GetGame().inLobby() || event.getReason() == RestartReason.SINGLE_COMMAND)
		{
			return;
		}

		event.setCancelled(true);
	}

	@EventHandler
	public void KitRegister(GameStateChangeEvent event)
	{
		if (event.GetState() != event.GetGame().KitRegisterState)
			return;

		event.GetGame().RegisterKits();
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void KitDeregister(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.End)
			return;

		event.GetGame().DeregisterKits();
		event.GetGame().deRegisterStats();
	}

	@EventHandler
	public void ScoreboardTitle(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTEST)
			return;

		Game game = Manager.GetGame();
		if (game == null) return;

		if (!game.UseCustomScoreboard)
		{
			game.GetScoreboard().updateTitle();
		}
	}

	@EventHandler
	public void TeamQueueSizeUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		Game game = Manager.GetGame();

		if (game == null)
		{
			return;
		}

		for (GameTeam team : game.GetTeamList())
		{
			if (team.GetTeamEntity() == null)
			{
				continue;
			}

			String amount;

			if (Manager.IsTeamBalance())
			{
				amount = game.getTeamModule().getPlayersQueued(team) + " Queued";
			}
			else
			{
				amount = team.GetSize() + " Players";
			}

			team.GetTeamEntity().setCustomName(team.GetFormattedName() + " Team" + C.Reset + "  " + amount);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void PlayerPrepare(GameStateChangeEvent event)
	{
		final Game game = event.GetGame();

		if (event.GetState() != GameState.Prepare)
			return;

		// Sir, I'll handle this.
		if (!game.Prepare)
			return;

		final ArrayList<Player> players = game.GetPlayers(true);

		//Prepare Players
		for (int i = 0; i < players.size(); i++)
		{
			final Player player = players.get(i);

			final GameTeam team = game.GetTeam(player);

			UtilServer.getServer().getScheduler().runTaskLater(Manager.getPlugin(), new Runnable()
			{
				public void run()
				{
					//Teleport
					if (game.SpawnTeleport)
						team.SpawnTeleport(player);

					game.addPlayerInTime(player);

					Manager.Clear(player);
					UtilInv.Clear(player);

					game.ValidateKit(player, game.GetTeam(player));

					if (game.GetKit(player) != null)
						game.GetKit(player).ApplyKit(player);

					//Event
					PlayerPrepareTeleportEvent playerStateEvent = new PlayerPrepareTeleportEvent(game, player);
					UtilServer.getServer().getPluginManager().callEvent(playerStateEvent);
				}
			}, i * game.TickPerTeleport);
		}

		if (game.PrepareAutoAnnounce)
		{
			//Announce Game after every player is TP'd in
			UtilServer.getServer().getScheduler().runTaskLater(Manager.getPlugin(), new Runnable()
			{
				public void run()
				{
					game.AnnounceGame();
					game.StartPrepareCountdown();

					//Event
					GamePrepareCountdownCommence event = new GamePrepareCountdownCommence(game);
					UtilServer.getServer().getPluginManager().callEvent(event);
				}
			}, players.size() * game.TickPerTeleport);
		}

		//Spectators Move
		for (Player player : UtilServer.getPlayers())
		{
			if (Manager.GetGame().IsAlive(player))
				continue;

			Manager.addSpectator(player, true);
		}
	}

	@EventHandler
	public void PlayerTeleportOut(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Dead)
			return;

		final Player[] players = UtilServer.getPlayers();

		//Prepare Players
		for (int i = 0; i < players.length; i++)
		{
			final Player player = players[i];

			UtilServer.getServer().getScheduler().runTaskLater(Manager.getPlugin(), new Runnable()
			{
				public void run()
				{
					Manager.Clear(player);
					UtilInv.Clear(player);

					Manager.GetCondition().EndCondition(player, ConditionType.CLOAK, "Spectator");

					player.eject();
					player.leaveVehicle();
					player.teleport(Manager.GetLobby().getSpawn());
				}
			}, i);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void EndUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		Game game = Manager.GetGame();
		if (game == null) return;

		game.EndCheck();
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void EndStateChange(PlayerStateChangeEvent event)
	{
		event.GetGame().EndCheck();
	}

	@EventHandler
	public void carlSpinnerCancel(CarlSpinnerEvent event)
	{
		Game game = Manager.GetGame();
		if (game == null) return;

		if (game.GetCountdown() > 0 || !game.inLobby())
		{
			UtilPlayer.message(event.getPlayer(), F.main("Carl", "You can't use my spinner at the moment!"));
			event.setCancelled(true);
		}
	}
}
