package mineplex.core.gadget.gadgets.outfit.windup;

import java.time.Month;
import java.time.YearMonth;

import org.bukkit.Material;

import mineplex.core.gadget.GadgetManager;

public class OutfitWindupHelmet extends OutfitWindupSuit
{

	public OutfitWindupHelmet(GadgetManager manager)
	{
		super(manager, "Helmet", ArmorSlot.HELMET, Material.LEATHER_HELMET);

		setPPCYearMonth(YearMonth.of(2018, Month.MAY));
	}
}
