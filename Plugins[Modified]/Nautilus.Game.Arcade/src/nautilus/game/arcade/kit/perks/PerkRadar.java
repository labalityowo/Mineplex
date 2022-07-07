package nautilus.game.arcade.kit.perks;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilGear;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.kit.Perk;

public class PerkRadar extends Perk
{
	private HashMap<Player, Long> _lastTick = new HashMap<Player, Long>();
	
	public PerkRadar() 
	{
		super("Radar Scanner", new String[] 
				{
				C.cYellow + "Hold Compass" + C.cGray + " to use " + C.cGreen + "Radar Scanner",
				"Ticks get faster when you are near a Hider!"
				});
	}

	@EventHandler
	public void Update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		for (Player player : UtilServer.getPlayers())
		{
			if (!UtilGear.isMat(player.getItemInHand(), Material.COMPASS))
				continue;
			
			if (!Kit.HasKit(player) || !Manager.IsAlive(player))
				continue;
			
			double closest = 999;
			for (Player other : UtilServer.getPlayers())
			{
				if (other.equals(player))
					continue;
				
				if (!Manager.IsAlive(other))
					continue;
				
				if (Manager.GetColor(other) != ChatColor.AQUA)
					continue;
				
				double dist = UtilMath.offset(other, player);
				if (dist < closest)
					closest = dist;
			}
			
			double scale = Math.max(0, Math.min(1, (closest - 3) / 10d));
			
			//Bi-Directional Cooldown
			if (_lastTick.containsKey(player) && !UtilTime.elapsed(_lastTick.get(player), (long)(2000 * scale)) && !Recharge.Instance.usable(player, "Radar"))
				return;
			
			_lastTick.put(player, System.currentTimeMillis());
			Recharge.Instance.useForce(player, "Radar", (long)(2000 * scale));
			
			player.getWorld().playSound(player.getLocation(), Sound.NOTE_STICKS, 1f, (float)(2 - 1*scale));
			
			player.setCompassTarget(player.getLocation().add(Math.random()*10 - 5, 0, Math.random()*10 - 5));
		}
	}
}
