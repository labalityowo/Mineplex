package nautilus.game.arcade.game.games.moba;

import nautilus.game.arcade.game.games.moba.kit.HeroKit;
import org.bukkit.entity.Player;

public class MobaPlayer
{

	private final Player _player;
	private MobaRole _role;
	private HeroKit _kit;

	public MobaPlayer(Player player)
	{
		_player = player;
	}

	public Player getPlayer()
	{
		return _player;
	}

	public void setRole(MobaRole role)
	{
		_role = role;
	}

	public MobaRole getRole()
	{
		return _role;
	}

	public void setKit(HeroKit kit)
	{
		_role = kit.getRole();
		_kit = kit;
	}

	public HeroKit getKit()
	{
		return _kit;
	}
}
