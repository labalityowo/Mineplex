package nautilus.game.arcade.game.games.cakewars.item;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.UtilBlock;

import nautilus.game.arcade.game.games.cakewars.CakeWars;
import nautilus.game.arcade.game.games.cakewars.team.CakeTeam;

public abstract class CakeSpecialItem
{

	protected final CakeWars _game;
	private final ItemStack _itemStack;
	private final String _name;
	private final long _cooldown;

	public CakeSpecialItem(CakeWars game, ItemStack itemStack)
	{
		this(game, itemStack, "CakeSpecialItem", 0);
	}

	public CakeSpecialItem(CakeWars game, ItemStack itemStack, String name, long cooldown)
	{
		_game = game;
		_itemStack = itemStack;
		_name = name;
		_cooldown = cooldown;
	}

	protected abstract boolean onClick(PlayerInteractEvent event, CakeTeam cakeTeam);

	protected void setup()
	{
	}

	protected void cleanup()
	{
	}

	protected boolean isInvalidBlock(Block block)
	{
		Location location = block.getLocation();
		return !UtilBlock.airFoliage(block) || _game.getCapturePointModule().isOnPoint(location) || _game.getCakeShopModule().isNearShop(location) || _game.getCakeSpawnerModule().isNearSpawner(block) || _game.isNearSpawn(block);
	}

	public ItemStack getItemStack()
	{
		return _itemStack;
	}

	public String getName()
	{
		return _name;
	}

	public long getCooldown()
	{
		return _cooldown;
	}
}
