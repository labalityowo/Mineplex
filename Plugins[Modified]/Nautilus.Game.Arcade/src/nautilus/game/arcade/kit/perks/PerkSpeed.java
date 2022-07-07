package nautilus.game.arcade.kit.perks;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.C;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.updater.UpdateType;
import nautilus.game.arcade.kit.Perk;

public class PerkSpeed extends Perk
{
	private int _level;
	
	public PerkSpeed(int level) 
	{
		super("Speed", new String[] 
				{ 
				C.cGray + "Permanent Speed " + (level+1),
				});
		
		_level = level;
	}
		
	@EventHandler
	public void DigSpeed(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOW)
			return;
		
		if (Manager.GetGame() == null)
			return;
			
		for (Player player : Manager.GetGame().GetPlayers(true))
		{
			if (!hasPerk(player))
				continue;
			
			Manager.GetCondition().Factory().Speed(GetName(), player, player, 8, _level, false, false, true);
		}
	}
}
