package nautilus.game.arcade.kit.perks;

import java.util.HashMap;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilTime;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.kit.Perk;

public class PerkSquidSwim extends Perk 
{
	private HashMap<Player, Long> _push = new HashMap<Player, Long>();
	private HashMap<Player, Long> _active = new HashMap<Player, Long>();

	public PerkSquidSwim() 
	{
		super("Swimming", new String[] 
				{
				C.cYellow + "Tap Crouch" + C.cGray + " to use " + C.cGreen + "Squid Thrust",
				C.cYellow + "Hold Crouch" + C.cGray + " to use " + C.cGreen + "Squid Swim"
				});
	}

	@EventHandler
	public void EnergyUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Player player : Manager.GetGame().GetPlayers(true))
		{
			if (!Kit.HasKit(player))
				continue;

			player.setExp((float) Math.min(0.999, player.getExp()+0.007));
		}
	}

	@EventHandler
	public void Use(PlayerToggleSneakEvent event)
	{
		if (event.isCancelled())
			return;

		Player player = event.getPlayer();

		//Store Push
		if (!player.isSneaking())
		{
			_push.put(event.getPlayer(), System.currentTimeMillis());
			return;
		}
		
		if (!_push.containsKey(player) || UtilTime.elapsed(_push.get(player), 500))
			return;
	
		if (!Kit.HasKit(player))
			return;

		if (!player.getLocation().getBlock().isLiquid())
			return;

		if (player.getExp() < 0.5)
			return;

		if (!Recharge.Instance.use(player, GetName(), 500, false, false))
			return;

		player.setExp(player.getExp() - 0.5f);

		//Velocity
		UtilAction.velocity(player, 0.9, 0.2, 2, false);

		//Sound
		player.getWorld().playSound(player.getLocation(), Sound.SPLASH, 0.5f, 0.75f);

		_active.put(player, System.currentTimeMillis());
	}

	@EventHandler
	public void Reuse(UpdateEvent event) 
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Player player : Manager.GetGame().GetPlayers(true))
		{
			if (!player.getLocation().getBlock().isLiquid())
				continue;

			//Fast Thrust
			if (_active.containsKey(player))
			{
				if (!UtilTime.elapsed(_active.get(player), 200))
				{
					UtilAction.velocity(player, 1, 0.1, 2, false);
					continue;
				}
				else
				{
					_active.remove(player);
				}
			}
			
			//Swim
			if (player.isSneaking())
			{
				UtilAction.velocity(player, 0.5, 0, 2, false);
			}
		}
	}
}
