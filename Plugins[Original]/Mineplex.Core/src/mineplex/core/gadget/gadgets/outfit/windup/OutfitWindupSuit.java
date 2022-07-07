package mineplex.core.gadget.gadgets.outfit.windup;

import java.time.Month;
import java.time.YearMonth;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.set.suits.SetWindup;
import mineplex.core.gadget.types.OutfitGadget;
import mineplex.core.gadget.util.CostConstants;
import mineplex.core.itemstack.ItemBuilder;

class OutfitWindupSuit extends OutfitGadget
{

	private static final String[] DESCRIPTION = UtilText.splitLineToArray(C.cGray + "A suit of leather that winds up as you walk around. Once fully charged they say you gain unbelievable swiftness...", LineFormat.LORE);

	OutfitWindupSuit(GadgetManager manager, String name, ArmorSlot slot, Material mat)
	{
		super(manager, SetWindup.NAME + " " + name, DESCRIPTION, CostConstants.POWERPLAY_BONUS, slot, mat, (byte) 0);

		setColor(SetWindup.COLOUR);
		setDisplayItem(new ItemBuilder(mat)
				.setColor(SetWindup.COLOUR)
				.build());
	}

	@Override
	public boolean ownsGadget(Player player)
	{
		return super.ownsGadget(player) ||  Manager.getDonationManager().Get(player).ownsUnknownSalesPackage(SetWindup.NAME);
	}
}
