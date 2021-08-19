package mineplex.core.gadget.types;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.chat.LevelPrefixType;
import mineplex.core.gadget.util.CostConstants;

public class LevelPrefixGadget extends Gadget
{

	private final LevelPrefixType _prefixType;

	public LevelPrefixGadget(GadgetManager manager, LevelPrefixType prefixType)
	{
		super(manager, GadgetType.LEVEL_PREFIX, prefixType.getName() + " Level Color", new String[]
				{
						C.cGray + "Changes the color of your level",
						C.cGray + "in chat to " + prefixType.getChatColor() + prefixType.getName() + C.cGray + "."
				}, CostConstants.LEVEL_REWARDS, Material.INK_SACK, prefixType.getDyeColor().getDyeData());

		_prefixType = prefixType;
	}

	@Override
	public void enableCustom(Player player, boolean message)
	{
		Manager.removeGadgetType(player, getGadgetType(), this);
		_active.add(player);

		if (message)
		{
			player.sendMessage(F.main(Manager.getName(), "You changed your level color to " + _prefixType.getChatColor() + _prefixType.getName() + C.cGray + "."));
		}
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		_active.remove(player);
	}

	@Override
	public boolean ownsGadget(Player player)
	{
		return Manager.getAchievementManager().getMineplexLevelNumber(player, false) >= _prefixType.getUnlockAt();
	}

	public LevelPrefixType getPrefixType()
	{
		return _prefixType;
	}
}
