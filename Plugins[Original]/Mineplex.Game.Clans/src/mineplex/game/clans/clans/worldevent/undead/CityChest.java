package mineplex.game.clans.clans.worldevent.undead;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;

import mineplex.game.clans.clans.ClansManager;

public class CityChest
{
	private final boolean _enabled;
	private final Block _block;
	private boolean _opened;
	
	public CityChest(Block block, boolean enabled)
	{
		_block = block;
		_enabled = enabled;
		_opened = false;
		
		if (!enabled)
		{
			_block.setType(Material.AIR);
		}
	}
	
	public boolean isEnabled()
	{
		return _enabled;
	}
	
	public boolean isOpen()
	{
		return _opened;
	}
	
	@SuppressWarnings("deprecation")
	public void open()
	{
		_block.setType(Material.AIR);
		_block.getWorld().playEffect(_block.getLocation(), Effect.STEP_SOUND, Material.ENDER_CHEST.getId());
		ClansManager.getInstance().getLootManager().dropUndeadCity(_block.getLocation().add(0.5, 0, 0.5));
		_opened = true;
	}
	
	public void revert()
	{
		_block.setType(Material.ENDER_CHEST);
	}
}