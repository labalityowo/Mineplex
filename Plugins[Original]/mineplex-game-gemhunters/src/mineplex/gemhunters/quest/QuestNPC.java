package mineplex.gemhunters.quest;

import mineplex.core.common.util.C;
import mineplex.core.menu.Menu;
import mineplex.gemhunters.util.SimpleNPC;
import org.bukkit.Location;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class QuestNPC extends SimpleNPC
{

	private Menu<?> _questMenu;
	
	public QuestNPC(QuestModule quest, Location spawn, Menu<?> menu)
	{
		super(quest.getPlugin(), spawn, Villager.class, C.cYellowB + "Quest Master", null);
				
		_questMenu = menu;
		_entity.setMetadata("quest_npc", new FixedMetadataValue(quest.getPlugin(), true));
		
		Villager villager = (Villager) _entity;
		
		villager.setProfession(Profession.LIBRARIAN);
	}

	@Override
	@EventHandler(priority = EventPriority.HIGH)
	public void npcClick(PlayerInteractEntityEvent event)
	{
		if (event.isCancelled() || !event.getRightClicked().equals(_entity))
		{
			return;
		}
		
		event.setCancelled(true);
		
		_questMenu.open(event.getPlayer());
	}
	
}
