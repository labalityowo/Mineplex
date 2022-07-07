package mineplex.game.clans.tutorial.tutorials.clans.objective.goals.shops;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.game.clans.clans.event.ClansPlayerBuyItemEvent;
import mineplex.game.clans.clans.event.ClansShopAddButtonEvent;
import mineplex.game.clans.tutorial.objective.Objective;
import mineplex.game.clans.tutorial.objective.ObjectiveGoal;
import org.bukkit.event.EventPriority;

public class PurchaseGoal extends ObjectiveGoal
{
	private Material _material;

	public PurchaseGoal(Objective objective, Material material, String name, String description)
	{
		super(objective, name, description);
		_material = material;

		setDisplayStartMessage(false);
		setDisplayFinishMessage(false);
	}

	public PurchaseGoal(Objective objective, Material material, String name, String description,
						String helpText)
	{
		super(objective, name, description, helpText, null);
		_material = material;

		setDisplayStartMessage(false);
		setDisplayFinishMessage(false);
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
	public void button(ClansShopAddButtonEvent event)
	{
		if (contains(event.getPlayer()) && event.getMaterial() == _material)
		{
			event.setBuyPrice(0);
		}
	}

	@EventHandler (priority = EventPriority.HIGH)
	public void buy(final ClansPlayerBuyItemEvent event)
	{
		if (contains(event.getPlayer()) && event.getItem().getType() == _material)
		{
			event.setCancelled(false);
			finish(event.getPlayer());
		}
	}
}
