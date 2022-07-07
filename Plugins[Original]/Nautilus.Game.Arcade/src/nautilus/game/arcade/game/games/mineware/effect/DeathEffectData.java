package nautilus.game.arcade.game.games.mineware.effect;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import mineplex.core.hologram.Hologram;

/**
 * This class holds player specific objects regarding a triggered death effect.
 */
public class DeathEffectData
{
	private Player _player;
	private Location _death;
	private List<Item> _foodItems = new ArrayList<>();
	private ArmorStand _chickenHead;
	private Hologram _hologram;

	public DeathEffectData(Player player, Location death)
	{
		_player = player;
		_death = death;
	}
	
	public boolean isChickenHead(ArmorStand armorStand)
	{
		return _chickenHead.equals(armorStand);
	}

	public void addFoodItems(List<Item> foodItems)
	{
		_foodItems = foodItems;
	}

	public boolean hasDroppedFoodItems()
	{
		return !_foodItems.isEmpty();
	}

	public void setChickenHead(ArmorStand chickenHead)
	{
		_chickenHead = chickenHead;
	}

	public boolean hasFinished()
	{
		return _foodItems.isEmpty() && _chickenHead.isDead();
	}

	public void setHologram(Hologram hologram)
	{
		_hologram = hologram;
	}

	public Player getPlayer()
	{
		return _player;
	}

	public Location getLocation()
	{
		return _death;
	}

	public List<Item> getFoodItems()
	{
		return _foodItems;
	}

	public ArmorStand getChickenHead()
	{
		return _chickenHead;
	}

	public Hologram getHologram()
	{
		return _hologram;
	}
}
