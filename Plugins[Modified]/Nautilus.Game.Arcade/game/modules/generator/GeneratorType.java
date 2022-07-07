package nautilus.game.arcade.game.modules.generator;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilFirework;

public class GeneratorType
{

	private final ItemStack _itemStack;
	private final long _spawnRate;
	private final String _name;
	private final ChatColor _colour;
	private final boolean _flashName;
	private final FireworkEffect _effect;

	public GeneratorType(ItemStack itemStack, long spawnRate, String name, ChatColor chatColour, Color bukkitColour, boolean flashName)
	{
		_itemStack = itemStack;
		_spawnRate = spawnRate;
		_name = name;
		_colour = chatColour;
		_flashName = flashName;
		_effect = FireworkEffect.builder()
				.with(Type.BURST)
				.withColor(bukkitColour)
				.build();
	}

	final void collect(Generator generator, Player player)
	{
		playEffect(generator);
		MapUtil.QuickChangeBlockAt(generator.getBlock().getLocation(), Material.IRON_BLOCK);
		collect(player);
	}

	final ArmorStand spawnHolder(Generator generator)
	{
		Location location = generator.getLocation();
		ArmorStand holder = location.getWorld().spawn(location, ArmorStand.class);

		holder.setGravity(false);
		holder.setVisible(false);
		holder.setHelmet(_itemStack);
		holder.setRemoveWhenFarAway(false);
		UtilEnt.setTickWhenFarAway(holder, true);

		playEffect(generator);
		MapUtil.QuickChangeBlockAt(generator.getBlock().getLocation(), Material.GOLD_BLOCK);

		return holder;
	}

	private void playEffect(Generator generator)
	{
		Location location = generator.getLocation();
		UtilFirework.playFirework(location, _effect);
	}

	public void collect(Player player)
	{
		player.getInventory().addItem(_itemStack);
	}

	public ItemStack getItemStack()
	{
		return _itemStack;
	}

	public String getName()
	{
		return _name;
	}

	public long getSpawnRate()
	{
		return _spawnRate;
	}

	public ChatColor getColour()
	{
		return _colour;
	}

	public boolean isFlashName()
	{
		return _flashName;
	}
}
