package nautilus.game.arcade.game.games.cakewars.island;

import java.util.List;

import org.bukkit.Location;

public class CakeIsland
{

	private final List<Location> _blocks;

	private long _chestOpen;
	private boolean _crumbing;

	CakeIsland(List<Location> blocks)
	{
		_blocks = blocks;
	}

	public List<Location> getBlocks()
	{
		return _blocks;
	}

	public void setChestOpen()
	{
		_chestOpen = System.currentTimeMillis();
	}

	public long getChestOpen()
	{
		return _chestOpen;
	}

	public void setCrumbing(boolean crumbing)
	{
		_crumbing = crumbing;
	}

	public boolean isCrumbing()
	{
		return _crumbing;
	}
}
