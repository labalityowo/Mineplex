package mineplex.minecraft.game.core.damage.compatibility;

import mineplex.core.Managers;
import mineplex.core.newnpc.NewNPCManager;
import mineplex.core.npc.NpcManager;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class NpcProtectListener implements Listener
{
	private NpcManager _npcManager;
	
	public NpcProtectListener(NpcManager npcManager)
	{
		_npcManager = npcManager;
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void CustomDamage(CustomDamageEvent event)
	{
    	if (_npcManager.isNpc(event.GetDamageeEntity()))
    	{
    		event.SetCancelled("NPC");
    	}
    	else
		{
			// This is bad but /shrug
			NewNPCManager manager = Managers.get(NewNPCManager.class);

			if (manager != null && manager.isNPC(event.GetDamageeEntity()))
			{
				event.SetCancelled("New NPC");
			}
		}
	}
}
