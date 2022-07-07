package mineplex.hub.treasurehunt;

import java.util.Map;
import java.util.Set;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public abstract class TreasureHunt
{

	protected final TreasureHuntManager _manager;
	private final Map<Block, Integer> _treasure;

	public TreasureHunt(TreasureHuntManager manager, Map<Block, Integer> treasure)
	{
		_manager = manager;
		_treasure = treasure;
	}

	public abstract void createTreasure(Block block, int id);

	public abstract void onTreasureFind(Player player, Block block, Set<Integer> found);

	protected Map<Block, Integer> getTreasure()
	{
		return _treasure;
	}
}
