package mineplex.core.npc.event;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class NpcInteractEntityEvent extends NpcEvent
{
	private Player _player;
	
	public NpcInteractEntityEvent(LivingEntity npc, Player player)
	{
		super(npc);
		
		_player = player;
	}
	
	public Player getPlayer()
	{
		return _player;
	}
}
