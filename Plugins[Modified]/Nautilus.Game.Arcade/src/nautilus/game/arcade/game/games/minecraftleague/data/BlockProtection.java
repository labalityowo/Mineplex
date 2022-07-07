package nautilus.game.arcade.game.games.minecraftleague.data;

import java.util.ArrayList;
import java.util.List;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import nautilus.game.arcade.game.games.minecraftleague.MinecraftLeague;

import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class BlockProtection
{
	private MinecraftLeague _host;
	private Player _player;
	private List<Block> _blocks = new ArrayList<Block>();
	
	public BlockProtection(MinecraftLeague host, Player player)
	{
		_host = host;
		_player = player;
	}
	
	public Player getOwner()
	{
		return _player;
	}
	
	public boolean hasBlock(Block block)
	{
		for (Block b : _blocks)
		{
			if (ProtectionUtil.isSameBlock(block, b))
				return true;
		}
		return false;
	}
	
	public boolean isLockedTo(Player opening, Block block, boolean ignoreTeam)
	{
		if (!hasBlock(block))
			return false;
		if (_host.GetTeam(_player).GetColor() != _host.GetTeam(opening).GetColor())
			return ignoreTeam;
		if (opening.getName().equalsIgnoreCase(_player.getName()))
			return false;
		
		return true;
	}
	
	public void lockBlock(Block block)
	{
		if (_blocks.size() > 5)
			return;
		
		_blocks.add(block);
		_player.playSound(_player.getLocation(), Sound.ANVIL_USE, 5, 1);
		UtilPlayer.message(_player, F.main("Game", "You have locked this block to your team! Right-click it while sneaking to unlock it!"));
	}
	
	public boolean unlockBlock(Block block)
	{
		if (_blocks.remove(block))
		{
			_player.playSound(_player.getLocation(), Sound.ANVIL_USE, 5, 1);
			UtilPlayer.message(_player, F.main("Game", "You have unlocked this block!"));
			return true;
		}
		return false;
	}
}
