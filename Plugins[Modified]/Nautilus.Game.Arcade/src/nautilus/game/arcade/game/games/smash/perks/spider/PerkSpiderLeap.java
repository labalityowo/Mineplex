package nautilus.game.arcade.game.games.smash.perks.spider;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.kit.Perk;

public class PerkSpiderLeap extends Perk
{

	private float _energyTick;
	private float _energyJump;

	private Set<UUID> _secondJump = new HashSet<>();
	private Set<UUID> _finalJump = new HashSet<>();

	public PerkSpiderLeap()
	{
		super("Spider Leap", new String[] { C.cYellow + "Tap Jump Twice" + C.cGray + " to " + C.cGreen + "Spider Leap", C.cYellow + "Hold Crouch" + C.cGray + " to " + C.cGreen + "Wall Climb", C.cWhite
				+ "Wall Climb requires Energy (Experience Bar)." });
	}

	@Override
	public void setupValues()
	{
		_energyTick = getPerkFloat("Energy Per Tick");
		_energyJump = getPerkFloat("Energy Per Jump");
	}

	@EventHandler
	public void WallClimb(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		for (Player player : UtilServer.getPlayers())
		{
			if (UtilPlayer.isSpectator(player) || !hasPerk(player))
			{
				continue;
			}

			if (!player.isSneaking())
			{
				if (UtilEnt.onBlock(player))
				{
					player.setExp(Math.min(0.999F, player.getExp() + _energyTick));
					_secondJump.remove(player.getUniqueId());
				}
				continue;
			}

			player.setExp(Math.max(0, player.getExp() - _energyTick));

			if (player.getExp() <= 0)
			{
				continue;
			}

			if (player.getExp() >= _energyJump)
			{
				_finalJump.remove(player.getUniqueId());
			}

			if (!Recharge.Instance.usable(player, GetName()))
			{
				continue;
			}

			for (Block block : UtilBlock.getSurrounding(player.getLocation().getBlock(), true))
			{
				if (!UtilBlock.airFoliage(block) && !block.isLiquid())
				{
					UtilAction.velocity(player, new Vector(0, 0.2, 0));

					if (!_secondJump.contains(player.getUniqueId()))
					{
						player.setAllowFlight(true);
						_secondJump.add(player.getUniqueId());
					}
				}
			}
		}
	}

	@EventHandler
	public void FlightHop(PlayerToggleFlightEvent event)
	{
		Player player = event.getPlayer();

		if (UtilPlayer.isSpectator(player) || !hasPerk(player))
		{
			return;
		}

		event.setCancelled(true);
		player.setFlying(false);

		// Disable Flight
		player.setAllowFlight(false);

		if (player.getExp() < _energyJump)
		{
			if (!_finalJump.contains(player.getUniqueId()))
			{
				_finalJump.add(player.getUniqueId());
			}
			else
			{
				return;
			}
		}

		// Velocity
		UtilAction.velocity(player, 1.0, 0.2, 1.0, true);

		// Energy
		//player.setExp(Math.max(0, player.getExp() - _energyJump));

		// Sound
		player.getWorld().playSound(player.getLocation(), Sound.SPIDER_IDLE, 1f, 1.5f);

		Recharge.Instance.use(player, GetName(), 500, false, false);
	}

	@EventHandler
	public void FlightUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		for (Player player : UtilServer.getPlayers())
		{
			if (UtilPlayer.isSpectator(player) || !hasPerk(player))
			{
				continue;
			}

			if (UtilEnt.onBlock(player))
			{
				player.setAllowFlight(true);
			}
		}
	}
}
