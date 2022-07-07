package mineplex.game.nano.game.components.player;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import mineplex.game.nano.game.Game;
import mineplex.game.nano.game.Game.GameState;
import mineplex.game.nano.game.GameComponent;
import mineplex.game.nano.game.event.PlayerGameRespawnEvent;

public class GiveItemComponent extends GameComponent<Game>
{

	private ItemStack[] _items, _armour;

	public GiveItemComponent(Game game)
	{
		super(game, GameState.Prepare, GameState.Live);
	}

	@Override
	public void disable()
	{
		_items = null;
		_armour = null;
	}

	public GiveItemComponent setItems(ItemStack[] items)
	{
		_items = items;
		return this;
	}

	public GiveItemComponent setArmour(ItemStack[] armour)
	{
		_armour = armour;
		return this;
	}

	@EventHandler
	public void respawn(PlayerGameRespawnEvent event)
	{
		giveItems(event.getPlayer());
	}

	public void giveItems(Player player)
	{
		if (_items != null)
		{
			player.getInventory().clear();
			player.getInventory().addItem(_items);
		}

		if (_armour != null)
		{
			player.getInventory().setArmorContents(_armour);
		}
	}
}