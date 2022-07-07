package nautilus.game.arcade.kit;

import org.bukkit.entity.Player;

import mineplex.core.game.kit.GameKit;
import mineplex.core.game.kit.upgrade.LinearUpgradeTree;

import nautilus.game.arcade.ArcadeManager;

public abstract class LinearUpgradeKit extends Kit
{

	private final Perk[][] _perks;

	public LinearUpgradeKit(ArcadeManager manager, GameKit gameKit, Perk[]... perks)
	{
		super(manager, gameKit);

		_perks = perks;

		for (int i = 0; i < perks.length; i++)
		{
			Perk[] arrayOfPerks = perks[i];

			for (Perk perk : arrayOfPerks)
			{
				perk.SetHost(this);
				perk.setUpgradeLevel(i);
			}
		}
	}

	public Perk[][] getPerks()
	{
		return _perks;
	}

	public int getLevel(Player player)
	{
		return LinearUpgradeTree.getLevel(getXp(player));
	}

	public int getXp(Player player)
	{
		return LinearUpgradeTree.getXp(Manager.getMineplexGameManager(), player, getGameKit());
	}

	public int getUpgradeLevel(Player player)
	{
		return LinearUpgradeTree.getUpgradeLevel(Manager.getMineplexGameManager(), player, getGameKit());
	}
}
