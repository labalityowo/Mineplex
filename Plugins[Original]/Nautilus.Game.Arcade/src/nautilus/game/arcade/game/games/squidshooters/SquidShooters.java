package nautilus.game.arcade.game.games.squidshooters;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.mineplex.anticheat.checks.move.Glide;
import com.mineplex.anticheat.checks.move.HeadRoll;
import com.mineplex.anticheat.checks.move.Speed;

import mineplex.core.Managers;
import mineplex.core.antihack.AntiHack;
import mineplex.core.common.Pair;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.disguise.disguises.DisguiseSquid;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.combat.CombatComponent;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.events.PlayerPrepareTeleportEvent;
import nautilus.game.arcade.game.SoloGame;
import nautilus.game.arcade.game.games.squidshooters.kit.KitRetroSquid;
import nautilus.game.arcade.kit.Kit;

public class SquidShooters extends SoloGame
{

	private static final int KILLS_TO_WIN = 20;
	private static final String[] DESCRIPTION =
			{
					"Hold " + C.cYellow + "Sneak" + C.Reset + " to swim.",
					"Stay in the Water.",
					"You cannot swim when hit.",
					"First squid to " + C.cYellow + KILLS_TO_WIN + C.Reset + " kills wins."
			};

	private final Map<Player, Integer> _kills = new HashMap<>();
	private final Set<Player> _inAir = new HashSet<>();

	public SquidShooters(ArcadeManager manager)
	{
		super(manager, GameType.SquidShooters, new Kit[]
				{
						new KitRetroSquid(manager),
				}, DESCRIPTION);

		PrepareFreeze = false;
		DamageTeamSelf = true;
		DeathOut = false;
		NightVision = true;
		WorldTimeSet = 6000;

		AntiHack antiHack = Managers.get(AntiHack.class);
		antiHack.addIgnoredCheck(Speed.class);
		antiHack.addIgnoredCheck(Glide.class);
		antiHack.addIgnoredCheck(HeadRoll.class);
	}

	@Override
	@EventHandler
	public void ScoreboardUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST || !InProgress())
		{
			return;
		}

		Scoreboard.writeNewLine();

		Scoreboard.writeGroup(_kills.keySet(), player ->
		{
			Integer kills = _kills.get(player);
			return kills == null ? null : Pair.create(C.cGreen + player.getName(), kills);
		}, true);

		Scoreboard.draw();
	}

	@EventHandler
	public void playerTeleportIn(PlayerPrepareTeleportEvent event)
	{
		Player player = event.GetPlayer();

		DisguiseSquid disguise = new DisguiseSquid(player);
		disguise.setName(getPlayersTeam().GetColor() + player.getName());
		disguise.setCustomNameVisible(true);
		Manager.GetDisguise().disguise(disguise);
	}

	@EventHandler
	public void prepare(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Prepare)
		{
			return;
		}

		Manager.runSyncTimer(new BukkitRunnable()
		{
			int line = 0;

			@Override
			public void run()
			{
				UtilTextMiddle.display(null, DESCRIPTION[line], 10, 50, 10, UtilServer.getPlayers());

				if (++line == DESCRIPTION.length)
				{
					cancel();
				}
			}
		}, 20, 50);
	}

	@EventHandler
	public void updateMovement(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK || !IsLive())
		{
			return;
		}

		for (Player player : GetPlayers(true))
		{
			if (!player.isSneaking() || player.hasPotionEffect(PotionEffectType.SLOW))
			{
				continue;
			}

			Vector direction = player.getLocation().getDirection();
			boolean inAirPast = _inAir.contains(player);
			boolean inAirNow = !UtilEnt.isInWater(player);

			// Entering water
			if (inAirPast && !inAirNow)
			{
				_inAir.remove(player);
			}
			// Leaving water
			else if (!inAirPast && inAirNow)
			{
				_inAir.add(player);
			}
			// Already in air
			else if (inAirNow)
			{
				continue;
			}
			// Already in water
			else
			{
				direction.multiply(0.5);
			}

			// If players are sneaking on the edge of a block and looking down (positive pitch).
			// They won't have downwards velocity applied to them. So for one tick we give them a
			// slight vertical velocity to fix this.
			if (direction.getY() < 0)
			{
				Pair<Location, Location> box = UtilEnt.getSideStandingBox(player);
				Location min = box.getLeft(), max = box.getRight();

				outerLoop:
				for (int x = min.getBlockX(); x <= max.getBlockX(); x++)
				{
					for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++)
					{
						Block block = player.getLocation().add(x, -0.5, z).getBlock();

						if (UtilBlock.solid(block))
						{
							direction.setY(0.01);
							break outerLoop;
						}
					}
				}
			}

			UtilAction.velocity(player, direction);
		}
	}

	@EventHandler
	public void updateHunger(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST || !InProgress())
		{
			return;
		}

		for (Player player : GetPlayers(true))
		{
			if (UtilEnt.isInWater(player))
			{
				UtilPlayer.hunger(player, 2);
			}
			else
			{
				UtilPlayer.hunger(player, -2);

				if (player.getFoodLevel() == 0)
				{
					Manager.GetDamage().NewDamageEvent(player, null, null, DamageCause.SUFFOCATION, 2, false, true, true, GetName(), "Suffocation");
				}
			}

			if (!player.hasPotionEffect(PotionEffectType.WATER_BREATHING))
			{
				player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, Integer.MAX_VALUE, 0, false, false));
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void playerDeath(CombatDeathEvent event)
	{
		CombatComponent killer = event.GetLog().GetKiller();

		if (killer == null)
		{
			return;
		}

		Player killerPlayer = UtilPlayer.searchExact(killer.getUniqueIdOfEntity());

		if (killerPlayer == null)
		{
			return;
		}

		_kills.compute(killerPlayer, (k, v) -> _kills.getOrDefault(k, 0) + 1);
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();

		_kills.remove(player);
		_inAir.remove(player);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void damage(CustomDamageEvent event)
	{
		if (!IsLive() || event.IsCancelled())
		{
			return;
		}

		if (event.GetCause() == DamageCause.ENTITY_ATTACK)
		{
			event.SetCancelled("Squid Melee Attack");
			return;
		}

		Player damagee = event.GetDamageePlayer();

		if (damagee != null)
		{
			damagee.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 10, 0, false, false), true);
		}
	}

	@Override
	public void EndCheck()
	{
		if (!IsLive())
		{
			return;
		}

		List<Player> alive = GetPlayers(true);

		if (alive.isEmpty())
		{
			SetState(GameState.End);
			return;
		}

		boolean end = alive.size() == 1;

		for (int kills : _kills.values())
		{
			if (kills >= KILLS_TO_WIN)
			{
				end = true;
				break;
			}
		}

		if (!end)
		{
			return;
		}

		List<Player> places = alive.stream()
				.sorted(Comparator.comparing(player -> _kills.getOrDefault(player, 0)).reversed())
				.collect(Collectors.toList());

		places.forEach(player -> AddGems(player, 10, "Participation", false, false));

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

		AnnounceEnd(places);
		SetState(GameState.End);
	}
}
