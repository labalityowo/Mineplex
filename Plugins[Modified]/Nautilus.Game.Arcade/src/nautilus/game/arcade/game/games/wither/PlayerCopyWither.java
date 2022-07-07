package nautilus.game.arcade.game.games.wither;

import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilEnt;
import nautilus.game.arcade.game.Game;

public class PlayerCopyWither 
{

	private final Skeleton _ent;
	private final Player _owner;
	
	PlayerCopyWither(Game host, Player owner, ChatColor nameColor)
	{
		_owner = owner;

		host.CreatureAllowOverride = true;
		_ent = owner.getWorld().spawn(owner.getLocation(), Skeleton.class);
		host.CreatureAllowOverride = false;
		
		UtilEnt.ghost(_ent, true, false);
		UtilEnt.vegetate(_ent, true);
		
		//Armor
		_ent.getEquipment().setArmorContents(owner.getInventory().getArmorContents());
		
		_ent.setCustomName(C.cWhite + C.Bold + C.Scramble + "XX" + ChatColor.RESET + " " + nameColor + owner.getName() + " " + C.cWhite + C.Bold + C.Scramble + "XX");
		_ent.setCustomNameVisible(true);
	}

	public LivingEntity GetEntity()
	{
		return _ent;
	}

	public Player GetPlayer() 
	{
		return _owner;
	}

}
