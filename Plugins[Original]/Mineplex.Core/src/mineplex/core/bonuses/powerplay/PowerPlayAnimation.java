package mineplex.core.bonuses.powerplay;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PowerPlayAnimation
{

	private List<ItemStack> _animationItems;
	private Player _player;

	public PowerPlayAnimation(Player player, List<ItemStack> animationItems)
	{
		_animationItems = animationItems;
		_player = player;
	}

	public List<ItemStack> getAnimationItems()
	{
		return _animationItems;
	}

	public Player getPlayer()
	{
		return _player;
	}
}
