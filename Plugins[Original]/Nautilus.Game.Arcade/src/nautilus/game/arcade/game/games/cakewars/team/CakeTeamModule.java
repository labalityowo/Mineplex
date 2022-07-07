package nautilus.game.arcade.game.games.cakewars.team;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTime;
import mineplex.core.hologram.Hologram;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.mission.MissionTrackerType;
import mineplex.core.preferences.Preference;
import mineplex.core.preferences.PreferencesManager;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;

import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.events.PlayerDeathOutEvent;
import nautilus.game.arcade.events.PlayerKitGiveEvent;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.cakewars.CakeModule;
import nautilus.game.arcade.game.games.cakewars.CakeWars;
import nautilus.game.arcade.game.games.cakewars.event.CakeRotEvent;
import nautilus.game.arcade.game.games.cakewars.event.CakeWarsEatCakeEvent;
import nautilus.game.arcade.game.games.cakewars.shop.CakeItem;
import nautilus.game.arcade.game.games.cakewars.shop.CakeShopModule;
import nautilus.game.arcade.game.games.cakewars.shop.trap.CakeTrapItem;
import nautilus.game.arcade.game.games.cakewars.shop.trap.CakeTrapItem.TrapTrigger;
import nautilus.game.arcade.game.modules.compass.CompassModule;
import nautilus.game.arcade.game.modules.rejoin.PlayerRejoinGameEvent;
import nautilus.game.arcade.world.WorldData;

public class CakeTeamModule extends CakeModule
{

	private static final int HOLOGRAM_VIEW_SQUARED = 16;
	private static final long CAKE_ROT_TIME = TimeUnit.MINUTES.toMillis(20);
	private static final long CAKE_WARNING_TIME = TimeUnit.MINUTES.toMillis(5);
	private static final long TRAP_COOLDOWN = TimeUnit.SECONDS.toMillis(10);
	private static final ItemStack[] STARTING_ITEMS =
			{
					new ItemBuilder(Material.WOOD_SWORD)
							.setUnbreakable(true)
							.build()
			};

	private final Map<GameTeam, CakeTeam> _teams;

	private Player _lastPlayer;
	private boolean _samePlayer;

	private boolean _announcedWarning;
	private boolean _cakesRotten;

	public CakeTeamModule(CakeWars game)
	{
		super(game);

		_teams = new HashMap<>();
	}

	@Override
	public void cleanup()
	{
		_teams.clear();
	}

	@EventHandler
	public void prepare(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Prepare)
		{
			return;
		}

		WorldData worldData = _game.WorldData;
		List<Location> edgeHolograms = worldData.GetDataLocs("BLACK");
		List<Location> shopHolograms = worldData.GetDataLocs("GRAY");
		List<Location> chestLocations = worldData.GetCustomLocs(String.valueOf(Material.CHEST.getId()));

		_game.GetTeamList().forEach(team ->
				{
					if (!team.IsTeamAlive())
					{
						return;
					}

					Location average = _game.getAverageLocation(team);

					_teams.put(team,
							new CakeTeam(
									_game,
									team,
									UtilAlg.findClosest(
											average,
											edgeHolograms
									),
									UtilAlg.findClosest(
											average,
											shopHolograms
									),
									UtilAlg.findClosest(
											average,
											chestLocations
									),
									worldData.GetCustomLocs("GEN " + team.GetName().toUpperCase()).get(0)
							)
					);
				}
		);
	}

	@EventHandler
	public void playerRejoin(PlayerRejoinGameEvent event)
	{
		CakeTeam cakeTeam = getCakeTeam(event.getPlayerGameInfo().getTeam());

		if (cakeTeam == null || !cakeTeam.canRespawn())
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void updateHologramVisibility(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST || !_game.InProgress())
		{
			return;
		}

		_teams.forEach((team, cakeTeam) ->
		{
			updateVisibility(cakeTeam, cakeTeam.getTipHolograms(), true);
			updateVisibility(cakeTeam, cakeTeam.getOtherHolograms(), false);
		});
	}

	private void updateVisibility(CakeTeam cakeTeam, List<Hologram> holograms, boolean checkPreference)
	{
		GameTeam team = cakeTeam.getGameTeam();
		List<Player> players = team.GetPlayers(true);
		PreferencesManager preferences = _game.getArcadeManager().getPreferences();

		holograms.forEach(hologram ->
		{
			if (!hologram.isInUse())
			{
				return;
			}

			Location location = hologram.getLocation();

			for (Player player : players)
			{
				if (!player.isOnline() || !team.IsAlive(player) || UtilMath.offsetSquared(player.getLocation(), location) < HOLOGRAM_VIEW_SQUARED || (checkPreference && !preferences.get(player).isActive(Preference.GAME_TIPS)))
				{
					hologram.removePlayer(player);
				}
				else
				{
					hologram.addPlayer(player);
				}
			}
		});
	}

	@EventHandler
	public void cakeInteract(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		Block block = event.getClickedBlock();

		if (UtilPlayer.isSpectator(player) || block == null || block.getType() != Material.CAKE_BLOCK)
		{
			return;
		}

		event.setCancelled(true);

		GameTeam playerTeam = _game.GetTeam(player);

		if (playerTeam == null)
		{
			return;
		}

		ItemStack itemStack = player.getItemInHand();

		_teams.forEach((team, cakeTeam) ->
		{
			if (!block.equals(cakeTeam.getCake().getBlock()))
			{
				return;
			}

			if (team.equals(playerTeam))
			{
				if (itemStack != null && itemStack.getType() != Material.AIR && player.isSneaking())
				{
					event.setCancelled(false);
					return;
				}

				player.sendMessage(F.main("Game", "You cannot eat your own cake!"));
				return;
			}

			if (!Recharge.Instance.use(player, "Eat Cake", 500, false, false))
			{
				return;
			}

			boolean anyAir = false;

			for (Block nearby : UtilBlock.getSurrounding(block, false))
			{
				if (nearby.getType() == Material.AIR)
				{
					anyAir = true;
				}
			}

			if (!anyAir)
			{
				return;
			}

			CakeShopModule module = _game.getCakeShopModule();

			module.getOwnedItems(team).removeIf(item ->
			{
				if (!(item instanceof CakeTrapItem))
				{
					return false;
				}

				CakeTrapItem trapItem = (CakeTrapItem) item;

				if (trapItem.getTrapTrigger() != TrapTrigger.CAKE_INTERACT || !Recharge.Instance.use(player, "Trap", TRAP_COOLDOWN, false, false))
				{
					return false;
				}

				triggerTrap(player, team, cakeTeam, trapItem);
				return true;
			});

			_game.AddGems(player, 1, "Cake Bites", true, true);
			_game.AddStat(player, "Bites", 1, false, false);
			_game.getArcadeManager().getMissionsManager().incrementProgress(player, 1, MissionTrackerType.CW_EAT_SLICE, _game.GetType().getDisplay(), null);

			byte data = block.getData();

			if (data == 0)
			{
				_lastPlayer = player;
				_samePlayer = true;
			}
			else if (!player.equals(_lastPlayer))
			{
				_samePlayer = false;
			}

			Location location = block.getLocation();

			block.getWorld().playEffect(location, Effect.STEP_SOUND, Material.CAKE_BLOCK);

			if (data < 6)
			{
				block.getWorld().playSound(location, Sound.EAT, 1, 1);
				block.setData((byte) (data + 1));
			}
			else
			{
				if (_samePlayer)
				{
					_game.AddStat(player, "EatWholeCake", 1, true, false);
				}

				UtilServer.CallEvent(new CakeWarsEatCakeEvent(player, cakeTeam));

				block.setType(Material.AIR);
				cakeTeam.getCakeHologram().stop();

				_game.Announce(F.main("Game", F.name(team.GetFormattedName()) + "'s Cake was eaten by " + F.name(playerTeam.GetColor() + player.getName()) + "! They can no longer respawn."));
				UtilTextMiddle.display(team.GetColor() + "CAKE EATEN", "You can no longer respawn", 10, 30, 10, team.GetPlayers(true).toArray(new Player[0]));
			}
		});
	}

	@EventHandler
	public void updateIslandTraps(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}

		CakeShopModule module = _game.getCakeShopModule();

		_teams.forEach((team, cakeTeam) ->
		{
			if (!cakeTeam.canRespawn())
			{
				return;
			}

			module.getOwnedItems(team).removeIf(item ->
			{
				if (!(item instanceof CakeTrapItem))
				{
					return false;
				}

				CakeTrapItem trapItem = (CakeTrapItem) item;

				if (trapItem.getTrapTrigger() != TrapTrigger.CAKE_NEAR)
				{
					return false;
				}

				for (Player player : UtilPlayer.getNearby(cakeTeam.getCake(), 3))
				{
					if (!team.HasPlayer(player) && Recharge.Instance.use(player, "Trap", TRAP_COOLDOWN, false, false))
					{
						triggerTrap(player, team, cakeTeam, trapItem);
						return true;
					}
				}

				return false;
			});
		});
	}

	private void triggerTrap(Player player, GameTeam team, CakeTeam cakeTeam, CakeTrapItem trapItem)
	{
		Player damager = null;

		for (Player teamMember : team.GetPlayers(true))
		{
			Set<CakeItem> items = _game.getCakeShopModule().getOwnedItems(teamMember);
			Iterator<CakeItem> iterator = items.iterator();

			while (iterator.hasNext())
			{
				if (iterator.next().equals(trapItem))
				{
					damager = teamMember;
					iterator.remove();
				}
			}
		}

		_game.getArcadeManager().GetDamage().NewDamageEvent(player, damager, null, DamageCause.CUSTOM, 2, false, true, false, "Trap", trapItem.getName());
		trapItem.onTrapTrigger(player, cakeTeam.getCake());
		UtilTextMiddle.display(team.GetColor() + "TRAP SET OFF", "One of your traps has been set off!", 5, 20, 5, team.GetPlayers(true).toArray(new Player[0]));
		UtilTextMiddle.display("", C.cRedB + "TRAPPED", 5, 20, 5, player);
	}

	@EventHandler
	public void playerDeathOut(PlayerDeathOutEvent event)
	{
		GameTeam team = _game.GetTeam(event.GetPlayer());

		if (team == null)
		{
			return;
		}
		else if (!_teams.get(team).canRespawn())
		{
			if (team.GetPlayers(true).size() == 1)
			{
				_game.Announce(F.main("Game", "The " + F.name(team.GetFormattedName()) + " team has been eliminated!"));
			}

			Player killer = event.GetPlayer().getKiller();

			if (killer != null)
			{
				_game.AddGems(killer, 2, "Final Kills", true, true);
				_game.AddStat(killer, "FinalKills", 1, false, false);
			}

			return;
		}

		event.setCancelled(true);
	}

	@EventHandler
	public void chestInteract(PlayerInteractEvent event)
	{
		if (!UtilEvent.isAction(event, ActionType.R_BLOCK))
		{
			return;
		}

		Player player = event.getPlayer();
		Location block = event.getClickedBlock().getLocation();

		if (UtilPlayer.isSpectator(player))
		{
			return;
		}

		GameTeam team = _game.GetTeam(player);

		for (CakeTeam cakeTeam : _teams.values())
		{
			if (!cakeTeam.canRespawn() || !cakeTeam.getChest().equals(block) || team.equals(cakeTeam.getGameTeam()))
			{
				continue;
			}

			event.setCancelled(true);
			player.sendMessage(F.main("Game", "You cannot open another team's chest while their Cake hasn't been eaten."));
			return;
		}
	}

	@EventHandler
	public void kitGiveItems(PlayerKitGiveEvent event)
	{
		if (!_game.InProgress())
		{
			return;
		}

		Player player = event.getPlayer();
		PlayerInventory inventory = player.getInventory();
		Color colour = _game.GetTeam(player).GetColorBase();

		inventory.addItem(STARTING_ITEMS);
		inventory.setArmorContents(new ItemStack[]
				{
						createColouredArmour(Material.LEATHER_BOOTS, colour),
						createColouredArmour(Material.LEATHER_LEGGINGS, colour),
						createColouredArmour(Material.LEATHER_CHESTPLATE, colour),
						createColouredArmour(Material.LEATHER_HELMET, colour),
				});
	}

	private ItemStack createColouredArmour(Material material, Color colour)
	{
		return new ItemBuilder(material)
				.setColor(colour)
				.setUnbreakable(true)
				.build();
	}

	@EventHandler
	public void playerDropItem(PlayerDropItemEvent event)
	{
		if (!_game.IsLive())
		{
			return;
		}

		ItemStack itemStack = event.getItemDrop().getItemStack();

		if (UtilItem.isLeatherProduct(itemStack))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void playerQuit(PlayerQuitEvent event)
	{
		GameTeam team = _game.GetTeam(event.getPlayer());

		if (team == null || team.GetPlayers(true).size() > 1)
		{
			return;
		}

		CakeTeam cakeTeam = _teams.get(team);

		if (cakeTeam == null || !cakeTeam.canRespawn())
		{
			return;
		}

		MapUtil.QuickChangeBlockAt(cakeTeam.getCake(), Material.AIR);
		_game.Announce(F.main("Game", F.name(team.GetFormattedName()) + "'s Cake has been eaten! All their players have quit."));
	}

	@EventHandler
	public void playerDeath(CombatDeathEvent event)
	{
		if (!_game.IsLive())
		{
			return;
		}

		Player player = event.GetEvent().getEntity();
		GameTeam team = _game.GetTeam(player);

		if (team == null)
		{
			return;
		}

		CakeTeam cakeTeam = _teams.get(team);

		if (cakeTeam.canRespawn())
		{
			Player killer = player.getKiller();
			GameTeam killerTeam = _game.GetTeam(killer);

			event.getPlayersToInform().removeIf(other -> (killerTeam == null || !killerTeam.HasPlayer(other)) && !team.HasPlayer(other));
		}
		else
		{
			event.setSuffix(C.cAquaB + " ELIMINATION");
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void updateRotHologram(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST || !_game.IsLive())
		{
			return;
		}

		String cakeRotString = getCakeRotString();

		if (cakeRotString != null)
		{
			String finalCakeRotString = C.cRed + cakeRotString + " until Cake Rot";

			_teams.values().forEach(cakeTeam ->
			{
				if (!cakeTeam.canRespawn())
				{
					cakeTeam.getCakeHologram().stop();
					return;
				}

				cakeTeam.getCakeHologram().setText(finalCakeRotString);
			});
		}
	}

	public String getCakeRotString()
	{
		long time = Math.max(0, _game.GetStateTime() + CAKE_ROT_TIME - System.currentTimeMillis());

		if (time > CAKE_WARNING_TIME)
		{
			return null;
		}
		else if (!_announcedWarning)
		{
			_announcedWarning = true;
			_game.Announce(F.main("Game", "Cakes will rot in " + F.time(UtilTime.MakeStr(CAKE_WARNING_TIME)) + "!"));
		}
		else if (time == 0)
		{
			if (!_cakesRotten)
			{
				_cakesRotten = true;
				_game.Announce(F.main("Game", "All Cakes have rotted away. No one can respawn!"));

				_teams.values().forEach(cakeTeam ->
				{
					if (cakeTeam.canRespawn())
					{
						MapUtil.QuickChangeBlockAt(cakeTeam.getCake(), Material.AIR);
					}

					cakeTeam.getCakeHologram().stop();
				});

				_game.getModule(CompassModule.class)
						.setGiveCompassToAlive(true);

				UtilServer.CallEvent(new CakeRotEvent());
			}

			return null;
		}

		return UtilTime.MakeStr(time);
	}

	public boolean hasCakeRot()
	{
		return UtilTime.elapsed(_game.GetStateTime(), CAKE_ROT_TIME);
	}

	public CakeTeam getCakeTeam(GameTeam team)
	{
		return _teams.get(team);
	}

	public Map<GameTeam, CakeTeam> getCakeTeams()
	{
		return _teams;
	}
}
