package mineplex.core.achievement.leveling.rewards;

import org.bukkit.entity.Player;

import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.C;
import mineplex.core.gadget.types.Gadget;

public class LevelGadgetReward implements LevelReward
{

	private final Gadget _gadget;

	public LevelGadgetReward(Gadget gadget)
	{
		_gadget = gadget;
	}

	@Override
	public void claim(Player player)
	{
		DONATION.purchaseUnknownSalesPackage(player, _gadget.getName(), GlobalCurrency.TREASURE_SHARD, 0, true, null);
	}

	@Override
	public String getDescription()
	{
		return C.cGreen + _gadget.getName() + C.cWhite + " (" + C.cGreen + _gadget.getGadgetType().getCategoryType() + C.cWhite + ")";
	}
}
