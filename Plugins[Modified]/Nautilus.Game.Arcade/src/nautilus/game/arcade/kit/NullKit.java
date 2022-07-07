package nautilus.game.arcade.kit;

import org.bukkit.entity.Player;

import mineplex.core.game.kit.GameKit;

import nautilus.game.arcade.ArcadeManager;

public class NullKit extends Kit
{

	public NullKit(ArcadeManager manager)
	{
		super(manager, GameKit.NULL_SPACER);
	}

	@Override
	public void GiveItems(Player player) 
	{
	}

}
