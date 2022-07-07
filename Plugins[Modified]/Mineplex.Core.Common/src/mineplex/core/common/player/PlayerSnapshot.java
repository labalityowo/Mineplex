package mineplex.core.common.player;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class PlayerSnapshot
{
	private Location _location;
	private ItemStack[] _armor;
	private ItemStack[] _items;
	private double _health;
	private int _hunger;
	private float _saturation;
	private float _exhaustion;
	private int _experience;
	private Vector _velocity;

	private PlayerSnapshot(Location location, ItemStack[] armor, ItemStack[] items, double health, int hunger, float saturation, float exhaustion, int experience, Vector velocity)
	{
		_location = location;
		_armor = armor;
		_items = items;
		_health = health;
		_hunger = hunger;
		_saturation = saturation;
		_exhaustion = exhaustion;
		_experience = experience;
		_velocity = velocity;
	}

	public void applySnapshot(Player player)
	{
		player.teleport(_location);
		player.getInventory().setArmorContents(_armor);
		player.getInventory().setContents(_items);
		player.setHealth(_health);
		player.setFoodLevel(_hunger);
		player.setSaturation(_saturation);
		player.setExhaustion(_exhaustion);
		player.setVelocity(_velocity);
		player.setTotalExperience(_experience);
	}

	public static PlayerSnapshot getSnapshot(Player player)
	{
		Location location = player.getLocation();

		// Inventory
		ItemStack[] bArmor = player.getInventory().getArmorContents();
		ItemStack[] armor = new ItemStack[bArmor.length];

		for (int i = 0; i < bArmor.length; i++)
		{
			armor[i] = bArmor[i];
		}

		ItemStack[] bItems = player.getInventory().getContents();
		ItemStack[] items = new ItemStack[bItems.length];

		for (int i = 0; i < bItems.length; i++)
		{
			items[i] = bItems[i];
		}

		// Health
		double health = player.getHealth();
		int hunger = player.getFoodLevel();
		float saturation = player.getSaturation();
		float exhaustion = player.getExhaustion();
		int experience = player.getTotalExperience();

		Vector velocity = player.getVelocity();

		return new PlayerSnapshot(location, armor, items, health, hunger, saturation, exhaustion, experience, velocity);
	}
}
