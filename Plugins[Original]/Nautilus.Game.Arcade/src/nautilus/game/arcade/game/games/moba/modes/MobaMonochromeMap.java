package nautilus.game.arcade.game.games.moba.modes;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTime;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.utils.UtilVariant;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.moba.Moba;
import nautilus.game.arcade.game.games.moba.buff.BuffManager;
import nautilus.game.arcade.game.games.moba.buff.buffs.BuffPumpkinKing;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class MobaMonochromeMap extends MobaMap
{

	private static final long START_TIME = TimeUnit.MINUTES.toMillis(5);
	private static final long ACTIVE_TIME = TimeUnit.SECONDS.toMillis(30);
	private static final ItemStack IN_HAND = new ItemStack(Material.STONE_SWORD);
	private static final ItemStack BUFF_HELMET = new ItemBuilder(Material.SKULL_ITEM, (byte) 1).build();

	private final Set<LivingEntity> _skeletons;
	private final Map<GameTeam, Integer> _killedSkeletons;

	private boolean _active;
	private long _lastStart;

	public MobaMonochromeMap(Moba host)
	{
		super(host);

		_skeletons = new HashSet<>();
		_killedSkeletons = new HashMap<>();
	}

	@EventHandler
	public void live(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Live)
		{
			return;
		}

		_lastStart = System.currentTimeMillis();
	}

	@EventHandler
	public void updateStart(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST || !_host.IsLive() || _active || !UtilTime.elapsed(_lastStart, START_TIME))
		{
			return;
		}

		_lastStart = System.currentTimeMillis();
		_active = true;

		UtilTextMiddle.display(C.cRedB + "Wither Skeletons", "Have Spawned!", 10, 40, 10);
		_host.Announce(F.main("Game", F.elem("Wither Skeletons") + " have spawned! The team that kills the most within " + F.time("30 seconds") + " receives a buff!"), false);

		for (Player player : Bukkit.getOnlinePlayers())
		{
			player.playSound(player.getLocation(), Sound.WITHER_SPAWN, 1, 0.4F);
		}

		_host.CreatureAllowOverride = true;

		for (Location location : _host.WorldData.GetDataLocs("BLACK"))
		{
			Skeleton skeleton = UtilVariant.spawnWitherSkeleton(location);
			skeleton.getEquipment().setItemInHand(IN_HAND);
			skeleton.setCustomName(C.Bold + "Wither Skeleton");
			skeleton.setCustomNameVisible(true);

			_skeletons.add(skeleton);
		}

		_host.CreatureAllowOverride = false;

		for (GameTeam team : _host.GetTeamList())
		{
			_killedSkeletons.put(team, 0);
		}
	}

	@EventHandler
	public void updateEnd(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST || !_host.IsLive() || !_active || !UtilTime.elapsed(_lastStart, ACTIVE_TIME))
		{
			return;
		}

		GameTeam red = _host.GetTeam(ChatColor.RED);
		int redKills = _killedSkeletons.get(red);
		GameTeam blue = _host.GetTeam(ChatColor.AQUA);
		int blueKills = _killedSkeletons.get(blue);
		List<GameTeam> winners;

		// Draw
		if (redKills == blueKills)
		{
			winners = Arrays.asList(red, blue);
		}
		// Red win
		else if (redKills > blueKills)
		{
			winners = Collections.singletonList(red);
		}
		// Blue win
		else
		{
			winners = Collections.singletonList(blue);
		}

		if (winners.size() == 1)
		{
			GameTeam winner = winners.get(0);

			_host.Announce(F.main("Game", F.name(winner.GetFormattedName()) + " killed the most " + F.elem("Wither Skeletons") + ". They have been given the buff!"), false);
			UtilTextMiddle.display("", winner.GetFormattedName() + C.cWhite + " killed the most " + F.elem("Wither Skeletons"), 10, 40, 10);
		}
		else
		{
			_host.Announce(F.main("Game", F.elem(C.Bold + "Draw") + "! No one was given the buff!"), false);
			UtilTextMiddle.display("", C.cYellowB + "Draw" + C.cWhite + "! No one was given the buff!", 10, 40, 10);
			cleanup();
			return;
		}

		// Give the team members the buff
		BuffManager buffManager = _host.getBuffManager();
		winners.forEach(team ->
		{
			for (Player teamMember : team.GetPlayers(true))
			{
				buffManager.apply(new BuffPumpkinKing(_host, teamMember, BUFF_HELMET));
			}
		});

		cleanup();
	}

	private void cleanup()
	{
		_skeletons.forEach(entity ->
		{
			if (!entity.isDead())
			{
				UtilParticle.PlayParticleToAll(ParticleType.CLOUD, entity.getLocation().add(0, 1.5, 0), 0.5F, 1, 0.5F, 0.001F, 15, ViewDist.LONG);
			}

			entity.remove();
		});
		_skeletons.clear();
		_killedSkeletons.clear();

		_active = false;
	}

	@EventHandler
	public void entityDeath(EntityDeathEvent event)
	{
		LivingEntity entity = event.getEntity();

		if (_skeletons.remove(entity))
		{
			Player player = entity.getKiller();

			if (player == null)
			{
				return;
			}

			GameTeam team = _host.GetTeam(player);

			if (team == null)
			{
				return;
			}

			event.getDrops().clear();
			event.setDroppedExp(0);
			_killedSkeletons.put(team, _killedSkeletons.get(team) + 1);
			player.sendMessage(F.main("Game", "You killed a " + F.name("Wither Skeleton") + "!"));
		}
	}

	@EventHandler
	public void entityCombust(EntityCombustEvent event)
	{
		if (_skeletons.contains(event.getEntity()))
		{
			event.setCancelled(true);
		}
	}
}
