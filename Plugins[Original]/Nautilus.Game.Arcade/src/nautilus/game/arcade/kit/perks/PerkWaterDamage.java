package nautilus.game.arcade.kit.perks;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.kit.Perk;

public class PerkWaterDamage extends Perk
{
	private double _damage;
	private long _time;
	
	public PerkWaterDamage(double damage, double time)
	{
		super("(Not) Water Bender", new String[]
				{
				"You are deathly afraid of water.",
				"Entering water deals " + C.cYellow + "2 Damage" + C.cGray + " every " + C.cGreen + "1 Second"
				});
		
		_damage = damage;
		_time = (long) (time * 1000);
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (!Manager.GetGame().IsLive())
			return;
		
		for (Player player : Manager.GetGame().GetPlayers(true)){
			if (!Kit.HasKit(player))
				continue;
			
			if (!Recharge.Instance.use(player, "Not Water Bender Damage", _time, false, false))
				continue;
			
			if (UtilEnt.isInWater(player))
				Manager.GetDamage().NewDamageEvent(player, null, null, DamageCause.DROWNING, _damage, false, false, true, "Water", "Water Hating Kit");
		}
	}
}
