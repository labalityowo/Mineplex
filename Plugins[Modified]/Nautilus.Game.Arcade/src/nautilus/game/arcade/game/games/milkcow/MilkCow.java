package nautilus.game.arcade.game.games.milkcow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

import mineplex.core.Managers;
import mineplex.core.common.Pair;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.visibility.VisibilityManager;
import mineplex.minecraft.game.core.combat.CombatComponent;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.SoloGame;
import nautilus.game.arcade.game.games.milkcow.kits.KitCow;
import nautilus.game.arcade.game.games.milkcow.kits.KitFarmerJump;
import nautilus.game.arcade.game.modules.compass.CompassModule;
import nautilus.game.arcade.game.team.selectors.RatioSelector;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.NullKit;

public class MilkCow extends SoloGame
{

	private static final int SCORE_TO_WIN = 15;
	private static final int SCORE_DEATH = 2;
	private static final String[] DESCRIPTION =
			{
					C.cGreen + "Farmers" + C.Reset + " get " + C.cYellow + "1" + C.Reset + " point for drinking milk.",
					C.cRed + "Cows" + C.Reset + " defend your herd from the farmers.",
					"",
					C.cYellow + "Everyone" + C.Reset + " loses " + C.cYellow + SCORE_DEATH + C.Reset + " points for dying.",
					"First player to " + C.cYellow + SCORE_TO_WIN + C.Reset + " points wins!"
			};

	private GameTeam _farmers;
	private GameTeam _cows;

	private final Map<EntityType, List<Location>> _animals;
	private final Map<Player, Integer> _scores;
	private final Set<Cow> _herd;
	private final Objective _objective;

	public MilkCow(ArcadeManager manager)
	{
		super(manager, GameType.MilkCow, new Kit[]
				{
						new KitFarmerJump(manager),
						new NullKit(manager),
						new KitCow(manager),
				}, DESCRIPTION);

		_animals = new HashMap<>(3);
		_scores = new HashMap<>();
		_herd = new HashSet<>();

		DeathOut = false;

		new CompassModule()
				.setGiveCompassToAlive(true)
				.register(this);

		_objective = Scoreboard.getScoreboard().registerNewObjective("Milk", "dummy");
		_objective.setDisplaySlot(DisplaySlot.BELOW_NAME);

		registerChatStats(
				Kills,
				Deaths
		);
	}

	@Override
	public void ParseData()
	{
		_animals.put(EntityType.CHICKEN, WorldData.GetDataLocs("WHITE"));
		_animals.put(EntityType.PIG, WorldData.GetDataLocs("PINK"));
		_animals.put(EntityType.VILLAGER, WorldData.GetDataLocs("PURPLE"));
	}

	@Override
	public void RestrictKits()
	{
		for (Kit kit : GetKits())
		{
			for (GameTeam team : GetTeamList())
			{
				if (team.GetColor() == ChatColor.RED)
				{
					if (kit.GetName().contains("Farm"))
					{
						team.GetRestrictedKits().add(kit);
					}
				}
				else
				{
					if (kit.GetName().contains("Cow"))
					{
						team.GetRestrictedKits().add(kit);
					}
				}
			}
		}
	}

	@Override
	@EventHandler
	public void CustomTeamGeneration(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Recruit)
		{
			return;
		}

		_farmers = GetTeamList().get(0);
		_farmers.SetName("Farmers");

		_cows = new GameTeam(this, "Cow", ChatColor.RED, _farmers.GetSpawns());
		AddTeam(_cows);

		_teamSelector = new RatioSelector(_cows, 0.2);

		RestrictKits();
	}

	@EventHandler
	public void spawnAnimals(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Live)
		{
			return;
		}

		CreatureAllowOverride = true;

		_animals.forEach((entityType, locations) ->
		{
			locations.forEach(location ->
			{
				Entity entity = location.getWorld().spawnEntity(location, entityType);

				if (entity instanceof Ageable)
				{
					Ageable ageable = (Ageable) entity;
					ageable.setBaby();
					ageable.setAgeLock(true);
				}
				else
				{
					entity.setCustomName("Bob");
					entity.setCustomNameVisible(true);
				}
			});
		});

		CreatureAllowOverride = false;
	}

	@EventHandler
	public void updateHerd(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC || !IsLive())
		{
			return;
		}

		List<Player> aliveCows = _cows.GetPlayers(true);

		if (aliveCows.isEmpty())
		{
			return;
		}

		Location target = aliveCows.get(0).getLocation();

		_herd.removeIf(cow ->
		{
			if (!cow.isValid())
			{
				return true;
			}

			UtilEnt.CreatureMove(cow, target, 1.4F);
			return false;
		});

		while (_herd.size() < 5)
		{
			CreatureAllowOverride = true;

			Cow cow = target.getWorld().spawn(target, Cow.class);

			_herd.add(cow);

			CreatureAllowOverride = false;
		}
	}

	@EventHandler
	public void cowDamage(CustomDamageEvent event)
	{
		if (event.GetDamageeEntity() instanceof Creature || _cows.HasPlayer(event.GetDamageePlayer()))
		{
			event.SetCancelled("Cow Immunity");
		}
	}

	@EventHandler
	public void updateFairTeams(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST || !IsLive())
		{
			return;
		}

		if (_cows.GetPlayers(true).isEmpty() && !_farmers.GetPlayers(true).isEmpty())
		{
			Player player = UtilAlg.Random(_farmers.GetPlayers(true));
			setCow(player);
		}
	}

	private void setCow(Player player)
	{
		SetPlayerTeam(player, _cows, true);

		//Refresh
		VisibilityManager vm = Managers.require(VisibilityManager.class);
		Bukkit.getOnlinePlayers().forEach(pl -> vm.refreshVisibility(pl, player));

		//Turn off flight
		player.setAllowFlight(false);
		player.setFlying(false);

		AddGems(player, 10, "Forced Cow", false, false);

		Announce(F.main("Game", F.elem(_farmers.GetColor() + player.getName()) + " has become " + F.elem(_cows.GetColor() + GetKit(player).GetName()) + "."));

		player.getWorld().strikeLightningEffect(player.getLocation());
	}

	@EventHandler
	public void getMilk(PlayerInteractEntityEvent event)
	{
		Player player = event.getPlayer();
		ItemStack itemStack = player.getItemInHand();

		if (itemStack == null || itemStack.getType() != Material.BUCKET || !(event.getRightClicked() instanceof Player))
		{
			return;
		}

		Player cow = (Player) event.getRightClicked();

		if (!_cows.HasPlayer(cow))
		{
			return;
		}

		event.setCancelled(true);
		player.setItemInHand(new ItemStack(Material.MILK_BUCKET));
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void bucketFill(PlayerBucketFillEvent event)
	{
		if (!InProgress() || event.getBlockClicked() == null || !event.getBlockClicked().isLiquid())
		{
			return;
		}

		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void drinkMilk(PlayerItemConsumeEvent event)
	{
		if (!IsLive() || event.getItem().getType() != Material.MILK_BUCKET)
		{
			return;
		}

		Player player = event.getPlayer();

		incrementScore(player, 1);
		UtilPlayer.health(player, 2);

		player.getWorld().playSound(player.getLocation(), Sound.BURP, 2, 1);

		AddGems(player, 0.5, "Milk Drunk", true, true);
	}

	@EventHandler
	public void loseMilk(PlayerDeathEvent event)
	{
		UtilServer.CallEvent(new MilkRemoveEvent(event.getEntity(), SCORE_DEATH));
	}

	@EventHandler
	public void killFarmer(CombatDeathEvent event)
	{
		CombatComponent killer = event.GetLog().GetKiller();

		if (killer == null)
		{
			return;
		}

		Player player = UtilPlayer.searchExact(killer.getUniqueIdOfEntity());

		if (player == null)
		{
			return;
		}

		incrementScore(player, 1);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void handleLostMilk(MilkRemoveEvent event)
	{
		incrementScore(event.getPlayer(), -event.getMilkToRemove());
	}

	private void incrementScore(Player player, int score)
	{
		int newScore = getScore(player) + score;

		if (newScore < 0)
		{
			newScore = 0;
		}

		_objective.getScore(player.getName()).setScore(newScore);
		_scores.put(player, newScore);

		if (newScore >= SCORE_TO_WIN)
		{
			End();
		}
	}

	private int getScore(Player player)
	{
		return _scores.getOrDefault(player, 0);
	}

	private List<Player> getPlacements()
	{
		return _scores.entrySet().stream()
				// Reserved natural ordering
				.sorted((o1, o2) -> o2.getValue().compareTo(o1.getValue()))
				.map(Entry::getKey)
				.collect(Collectors.toList());
	}

	private void End()
	{
		List<Player> places = getPlacements();

		//Award Gems
		if (!places.isEmpty())
		{
			AddGems(places.get(0), 20, "1st Place", false, false);
		}

		if (places.size() > 1)
		{
			AddGems(places.get(1), 15, "2nd Place", false, false);
		}

		if (places.size() > 2)
		{
			AddGems(places.get(2), 10, "3rd Place", false, false);
		}

		//Participation
		for (Player player : places)
		{
			if (player.isOnline())
			{
				AddGems(player, 10, "Participation", false, false);
			}
		}

		AnnounceEnd(places);
		SetState(GameState.End);
	}

	@Override
	public void EndCheck()
	{
		if (!IsLive())
		{
			return;
		}

		if (_cows.GetPlayers(true).isEmpty())
		{
			End();
		}
		else if (_farmers.GetPlayers(true).isEmpty())
		{
			_cows.GetPlayers(true).forEach(player -> incrementScore(player, SCORE_TO_WIN));
			End();
		}
	}

	@Override
	public List<Player> getWinners()
	{
		if (GetState().ordinal() >= GameState.End.ordinal())
		{
			Player winner = getPlacements().get(0);

			if (winner == null)
			{
				return Collections.emptyList();
			}
			else
			{
				return Collections.singletonList(winner);
			}
		}

		return null;
	}

	@Override
	public List<Player> getLosers()
	{
		List<Player> losers = new ArrayList<>(GetPlayers(false));
		losers.removeAll(getWinners());
		return losers;
	}

	@Override
	@EventHandler
	public void ScoreboardUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}

		Scoreboard.writeNewLine();

		Scoreboard.writeGroup(GetPlayers(true), player ->
		{
			GameTeam team = GetTeam(player);

			if (team == null)
			{
				return null;
			}

			return Pair.create(team.GetColor() + player.getName(), getScore(player));
		}, true);

		Scoreboard.draw();
	}
}
