package mineplex.core.gadget.types;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.gamemodifiers.GameCosmeticCategory;

public class GameModifierGadget extends Gadget
{

	private final GameCosmeticCategory _category;

	public GameModifierGadget(GadgetManager manager, GameCosmeticCategory category, String name, String[] desc, int cost, Material mat, byte data, String... alternativePackageNames)
	{
		super(manager, GadgetType.GAME_MODIFIER, name, desc, cost, mat, data, 1, alternativePackageNames);

		_category = category;
	}

	@Override
	public void enableCustom(Player player, boolean message)
	{
		if (_active.add(player) && message)
		{
			UtilPlayer.message(player, F.main("Gadget", "You enabled " + F.name(getName()) + " (" + F.name(_category.getCategoryName()) +  ")."));
		}
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		if (_active.remove(player) && message)
		{
			UtilPlayer.message(player, F.main(Manager.getName(), "You disabled " + F.name(getName()) + " (" + F.name(_category.getCategoryName()) +  ")."));
		}
	}

	public GameCosmeticCategory getCategory()
	{
		return _category;
	}
}
