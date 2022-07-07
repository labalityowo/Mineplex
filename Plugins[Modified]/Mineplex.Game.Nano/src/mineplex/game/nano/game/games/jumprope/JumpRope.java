package mineplex.game.nano.game.games.jumprope;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.nano.NanoManager;
import mineplex.game.nano.game.GameType;
import mineplex.game.nano.game.SoloGame;
import mineplex.game.nano.game.event.GameStateChangeEvent;
import mineplex.game.nano.game.event.PlayerGameRespawnEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class JumpRope extends SoloGame
{

	private double _minY, _maxHeight;
	private double _zDiff;
	private double _theta, _thetaDelta = Math.PI / 40;
	private Location _mid;

	public JumpRope(NanoManager manager)
	{
		super(manager, GameType.JUMP_ROPE, new String[]
				{
						C.cGreen + "Jump" + C.Reset + " over the particle rope!",
						C.cRed + "Don't" + C.Reset + " get hit.",
						"This game requires " + C.cPurple + "Particles" + C.Reset + " to be enabled!",
						C.cYellow + "Last player" + C.Reset + " standing wins!"
				});

		_prepareComponent.setPrepareFreeze(false);

		_playerComponent.setHideParticles(true);

		_damageComponent
				.setPvp(false)
				.setFall(false);

		_endComponent.setTimeout(TimeUnit.SECONDS.toMillis(100));

		_scoreboardComponent.setSetupSettingsConsumer((player, team, scoreboardTeam) -> scoreboardTeam.setNameTagVisibility(NameTagVisibility.NEVER));
	}

	@Override
	protected void parseData()
	{
		List<Location> ropePoints = _mineplexWorld.getIronLocations("RED");

		Location a = ropePoints.get(0);
		Location b = ropePoints.get(1);

		_zDiff = Math.abs(a.getZ() - b.getZ()) / 2;

		_minY = _playersTeam.getSpawn().getBlockY();
		_maxHeight = a.getY() - _minY;
		_minY += 0.1;

		_mid = UtilAlg.getAverageLocation(Arrays.asList(a, b));
	}

	@Override
	public void disable()
	{
	}

	@EventHandler
	public void respawn(PlayerGameRespawnEvent event)
	{
		Player player = event.getPlayer();

		player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 3, false, false));
		player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));
	}

	@EventHandler
	public void live(GameStateChangeEvent event)
	{
		if (event.getState() != GameState.Live)
		{
			return;
		}

		for (Player player : getAllPlayers())
		{
			if (player.getLocation().getBlock().isLiquid())
			{
				player.teleport(_playersTeam.getSpawn());
				player.setFireTicks(0);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void damage(CustomDamageEvent event)
	{
		event.AddMod(getGameType().getName(), "The Rope", 500, true);
	}

	@EventHandler
	public void updateRope(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK || !isLive())
		{
			return;
		}

		double xD = _maxHeight * Math.cos(_theta);
		double yD = _maxHeight * Math.sin(_theta);
		double yR = _maxHeight * Math.sin(_theta - Math.PI / 8);

		Location mid = _mid.clone().add(xD, yD - 0.2, 0);

		for (double z = -_zDiff; z <= _zDiff; z++)
		{
			mid.add(0, 0.3, z);

			UtilParticle.PlayParticleToAll(ParticleType.EXPLODE, mid, null, 0, 1, ViewDist.NORMAL);

			mid.subtract(0, 0.3, z);
		}

		if (_mid.getY() + yR <= _minY)
		{
			for (Player player : getAlivePlayers())
			{
				if (!UtilEnt.onBlock(player) || !Recharge.Instance.use(player, "Hit by Rope", 500, false, false))
				{
					continue;
				}

				player.playEffect(EntityEffect.HURT);
				UtilAction.velocity(player, new Vector(1, 1, 0));
			}
		}

		_theta += _thetaDelta;

		if (_theta > 2 * Math.PI)
		{
			_theta = 0;
			_thetaDelta *= 1.05;
		}
	}
}
