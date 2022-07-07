package mineplex.game.clans.tutorial.tutorials.clans.objective.goals.fields;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.common.DefaultHashMap;
import mineplex.core.common.util.EnclosedObject;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.tutorial.objective.ObjectiveGoal;
import mineplex.game.clans.tutorial.tutorials.clans.objective.FieldsObjective;

public class IdentifyFieldsGoal extends ObjectiveGoal<FieldsObjective>
{
	private DefaultHashMap<String, EnclosedObject<Integer>> _ticksHeld = new DefaultHashMap<>(key -> new EnclosedObject<>(0));
	
	public IdentifyFieldsGoal(FieldsObjective objective)
	{
		super(objective, "Identify The Fields", "By looking at your map, identify where the Fields are");
	}

	@Override
	protected void customStart(Player player)
	{
	}
	
	@Override
	protected void customFinish(Player player)
	{
		
	}
	
	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}
		
		getActivePlayers().forEach(uuid ->
		{
			Player player = Bukkit.getPlayer(uuid);
			
			if (player != null && player.isOnline())
			{
				if (player.getItemInHand() == null || player.getItemInHand().getType() != Material.MAP)
				{
					return;
				}
				
				_ticksHeld.get(player.getName()).Set(_ticksHeld.get(player.getName()).Get() + 1);
				
				if (_ticksHeld.get(player.getName()).Get() >= 80)
				{
					finish(player);
				}
			}
		});
	}
}