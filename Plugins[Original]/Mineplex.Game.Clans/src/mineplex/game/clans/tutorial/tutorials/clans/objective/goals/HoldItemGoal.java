package mineplex.game.clans.tutorial.tutorials.clans.objective.goals;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.common.DefaultHashMap;
import mineplex.core.common.util.EnclosedObject;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.tutorial.objective.Objective;
import mineplex.game.clans.tutorial.objective.ObjectiveGoal;

public class HoldItemGoal extends ObjectiveGoal<Objective<?, ?>>
{
	private DefaultHashMap<String, EnclosedObject<Integer>> _ticksHeld = new DefaultHashMap<>(key -> new EnclosedObject<>(0));
	private Material _material;
	private long _holdTicks;

	public HoldItemGoal(Objective<?, ?> objective, Material material, String name, String description, String helpText, int startDelay, long holdTicks)
	{
		super(objective, name, description, helpText, null);

		_material = material;
		_holdTicks = holdTicks;
//		setStartMessageDelay(startDelay);
	}

	public HoldItemGoal(Objective<?, ?> objective, Material material, String name, String description, String helpText, long holdTicks)
	{
		this(objective, material, name, description, helpText, 120, holdTicks);
	}

	@Override
	protected void customStart(Player player)
	{
		
	}

	@Override
	protected void customFinish(Player player)
	{
		_ticksHeld.remove(player.getName());
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
				if (player.getItemInHand() == null || player.getItemInHand().getType() != _material)
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