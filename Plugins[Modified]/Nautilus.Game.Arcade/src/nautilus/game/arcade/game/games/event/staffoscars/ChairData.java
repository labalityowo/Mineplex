package nautilus.game.arcade.game.games.event.staffoscars;

import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

public class ChairData
{

	private Player _player;
	private Block _block;
	private ArmorStand _stand;

	public ChairData(Player player, Block block)
	{
		_player = player;
		_block = block;
		_stand = block.getWorld().spawn(block.getLocation().add(0.5, -0.4, 0.5), ArmorStand.class);

		_stand.setGravity(false);
		_stand.setSmall(true);
		_stand.setVisible(false);
		_stand.setPassenger(player);
	}

	public Player getPlayer()
	{
		return _player;
	}

	public Block getBlock()
	{
		return _block;
	}

	public ArmorStand getStand()
	{
		return _stand;
	}

}
