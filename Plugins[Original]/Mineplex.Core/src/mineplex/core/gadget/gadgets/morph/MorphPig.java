package mineplex.core.gadget.gadgets.morph;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilText;
import mineplex.core.disguise.disguises.DisguisePig;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.morph.managers.UtilMorph;
import mineplex.core.gadget.types.MorphGadget;
import mineplex.core.gadget.util.CostConstants;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class MorphPig extends MorphGadget
{

	private final Set<Player> _double = new HashSet<>();

	public MorphPig(GadgetManager manager)
	{
		super(manager, "Pig Morph", UtilText.splitLinesToArray(new String[]
						{
								C.cGray + "Oink. Oink. Oink.... Oink?",
								C.blankLine,
								"#" + C.cWhite + "Left Click to use Oink",
								"#" + C.cWhite + "Collide to use Pig Bounce",
						}, LineFormat.LORE),
				CostConstants.UNLOCKED_WITH_ULTRA,
				Material.PORK, (byte) 0);
	}

	@Override
	public void enableCustom(final Player player, boolean message)
	{
		this.applyArmor(player, message);

		DisguisePig disguise = new DisguisePig(player);
		UtilMorph.disguise(player, disguise, Manager);
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		this.removeArmor(player);
		UtilMorph.undisguise(player, Manager.getDisguiseManager());
		_double.remove(player);
	}

	@EventHandler
	public void Snort(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();

		if (!isActive(player))
			return;

		if (!UtilEvent.isAction(event, ActionType.L))
			return;

		if (!Recharge.Instance.use(player, getName(), 400, false, false, "Cosmetics"))
			return;

		player.getWorld().playSound(player.getLocation(), Sound.PIG_IDLE, 1f, (float) (0.75 + Math.random() * 0.5));

	}

	@EventHandler
	public void UltraOwner(PlayerJoinEvent event)
	{
		if (Manager.getClientManager().Get(event.getPlayer()).hasPermission(GadgetManager.Perm.ULTRA_MORPH))
		{
			Manager.getDonationManager().Get(event.getPlayer()).addOwnedUnknownSalesPackage(getName());
		}
	}

	@EventHandler
	public void Collide(PlayerToggleFlightEvent event)
	{
		Player player =	event.getPlayer();

		_double.add(player);
		Recharge.Instance.useForce(player, getName() + " Double Jump", 200);
	}

	@EventHandler
	public void Collide(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		for (Player player : getActive())
		{
			//Grounded
			if (UtilEnt.isGrounded(player) && Recharge.Instance.usable(player, getName() + " Double Jump"))
			{
				_double.remove(player);
			}

			double range = 1;

			if (_double.contains(player))
			{
				range++;
			}

			if (player.getVehicle() != null || !Recharge.Instance.usable(player, getName() + " Collide"))
			{
				continue;
			}

			for (Player other : UtilServer.getPlayers())
			{
				if (other.equals(player) || other.getVehicle() != null || Recharge.Instance.usable(other, getName() + " Collide") || UtilMath.offsetSquared(player, other) > range || !Manager.selectEntity(this, other))
				{
					continue;
				}

				//Cooldown
				Recharge.Instance.useForce(other, getName() + " Collide", 200);
				Recharge.Instance.useForce(player, getName() + " Collide", 200);

				double power = 0.4;
				double height = 0.1;
				if (player.isSprinting())
				{
					power = 0.6;
					height = 0.2;
				}
				else if (_double.contains(player))
				{
					power = 1;
					height = 0.3;
				}

				//Velocity
				UtilAction.velocity(player, UtilAlg.getTrajectory2d(other, player), power, false, 0, height, 1, true);
				UtilAction.velocity(other, UtilAlg.getTrajectory2d(player, other), power, false, 0, height, 1, true);

				//Sound
				if (_double.contains(player))
				{
					player.getWorld().playSound(player.getLocation(), Sound.PIG_DEATH, (float) (0.8 + Math.random() * 0.4), (float) (0.8 + Math.random() * 0.4));
				}
				else
				{
					player.getWorld().playSound(player.getLocation(), Sound.PIG_IDLE, 1f, (float) (1.5 + Math.random() * 0.5));
				}
			}
		}
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		_double.remove(event.getPlayer());
	}
}