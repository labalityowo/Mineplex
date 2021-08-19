package nautilus.game.arcade.kit.perks;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleFlightEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilTime.TimeUnit;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.events.PerkDoubleJumpEvent;
import nautilus.game.arcade.kit.Perk;

public class PerkDoubleJump extends Perk
{

	private double _power;
	private double _heightMax;
	private boolean _control;
	private long _recharge;
	private boolean _displayForce;

	private final Set<Player> _disabled = new HashSet<>();

	public PerkDoubleJump(String name)
	{
		this(name, 0, 0, false);
	}

	public PerkDoubleJump(String name, double power, double heightLimit, boolean control)
	{
		super(name, new String[]
				{
				C.cYellow + "Tap Jump Twice" + C.cGray + " to " + C.cGreen + name,
				});

		_power = power;
		_heightMax = heightLimit;
		_control = control;
		_recharge = 0;
		_displayForce = false;
	}

	public PerkDoubleJump(String name, double power, double heightLimit, boolean control, long recharge, boolean displayForce)
	{
		super(name, new String[]
				{
				C.cYellow + "Tap Jump Twice" + C.cGray + " to " + C.cGreen + name,
				"Cooldown " + C.cGreen + UtilTime.convertString(recharge, 0, TimeUnit.SECONDS) + C.cGray + "."
				});

		_power = power;
		_heightMax = heightLimit;
		_control = control;
		_recharge = recharge;
		_displayForce = displayForce;
	}

	public PerkDoubleJump(String name, String[] description, double power, double heightLimit, boolean control, long recharge, boolean displayForce)
	{
		super(name, description);

		_power = power;
		_heightMax = heightLimit;
		_control = control;
		_recharge = recharge;
		_displayForce = displayForce;
	}

	@Override
	public void setupValues()
	{
		_power = getPerkDouble("Power", _power);
		_heightMax = getPerkDouble("Height Limit", _heightMax);
		_control = getPerkBoolean("Control", _control);
	}

	@EventHandler
	public void FlightHop(PlayerToggleFlightEvent event)
	{
		Player player = event.getPlayer();

		if (!hasPerk(player) || _disabled.contains(player) || Manager.isSpectator(player) || player.getGameMode() == GameMode.CREATIVE)
		{
			return;
		}

		event.setCancelled(true);
		player.setFlying(false);

		//Disable Flight
		player.setAllowFlight(false);

		PerkDoubleJumpEvent doubleJumpEvent = new PerkDoubleJumpEvent(player, _power, _heightMax, _control);
		UtilServer.CallEvent(doubleJumpEvent);

		if (doubleJumpEvent.isCancelled())
		{
			return;
		}

		//Velocity
		if (doubleJumpEvent.isControlled())
		{
			UtilAction.velocity(player, doubleJumpEvent.getPower(), 0.2, doubleJumpEvent.getHeightMax(), true);
		}
		else
		{
			UtilAction.velocity(player, player.getLocation().getDirection(), doubleJumpEvent.getPower(), true, doubleJumpEvent.getPower(), 0, doubleJumpEvent.getHeightMax(), true);
		}

		//Sound
		player.getWorld().playEffect(player.getLocation(), Effect.BLAZE_SHOOT, 0, 16);

		//Recharge
		if (_recharge > 0)
		{
			Recharge.Instance.useForce(player, GetName(), _recharge);

			if (_displayForce)
			{
				Recharge.Instance.setDisplayForce(player, GetName(), true);
			}
		}
		else
		{
			Recharge.Instance.useForce(player, GetName(), 50);
		}
	}

    @Override
    public void unregisteredEvents()
    {
        _disabled.clear();
    }

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		for (Player player : UtilServer.getPlayersCollection())
		{
			if (!hasPerk(player) || UtilPlayer.isSpectator(player) || !Recharge.Instance.usable(player, GetName()))
			{
				continue;
			}

//			Block block = player.getLocation().getBlock();

			if (UtilEnt.onBlock(player))
//			if (player.isOnGround() || UtilBlock.solid(block.getRelative(BlockFace.DOWN)) && UtilBlock.solid(block))
			{
				player.setAllowFlight(true);
			}
		}
	}

	public void disableForPlayer(Player player)
	{
		_disabled.add(player);
	}

	public void enableForPlayer(Player player)
	{
		_disabled.remove(player);
	}

}
