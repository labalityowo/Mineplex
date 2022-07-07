package nautilus.game.arcade.kit.perks;

import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.recharge.Recharge;

import nautilus.game.arcade.kit.Perk;

public class PerkQuickshot extends Perk
{
	private final String _name;
	private final double _power;
	private final long _recharge;
	private final boolean _useArrow;

	public PerkQuickshot(String name, double power, long recharge)
	{
		this(name, power, recharge, false);
	}

	public PerkQuickshot(String name, double power, long recharge, boolean useArrow)
	{
		super("Quickshot", new String[]
				{
						C.cYellow + "Left-Click" + C.cGray + " with Bow to " + C.cGreen + name
				});

		_name = name;
		_power = power;
		_recharge = recharge;
		_useArrow = useArrow;
	}

	@EventHandler
	public void Leap(PlayerInteractEvent event)
	{
		if (event.isCancelled())
			return;

		if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK)
			return;

		if (UtilBlock.usable(event.getClickedBlock()))
			return;

		if (event.getPlayer().getItemInHand() == null)
			return;

		if (event.getPlayer().getItemInHand().getType() != Material.BOW)
			return;

		Player player = event.getPlayer();

		if (!Kit.HasKit(player))
			return;

		if (_useArrow && !player.getInventory().contains(Material.ARROW, 1))
		{
			player.sendMessage(F.main("Game", "You need an " + F.name("Arrow") + " to use " + F.name(GetName()) + "."));
			return;
		}

		if (!Recharge.Instance.use(player, _name, _recharge, true, true))
			return;

		UtilInv.remove(player, Material.ARROW, (byte) 0, 1);

		Arrow arrow = player.launchProjectile(Arrow.class);
		arrow.setVelocity(player.getLocation().getDirection().multiply(_power));

		UtilPlayer.message(player, F.main("Skill", "You used " + F.skill(_name) + "."));
	}
}
