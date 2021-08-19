package mineplex.core.gadget.gadgets.death;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.blood.BloodEvent;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.banner.CountryFlag;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.DeathEffectGadget;

public class DeathFreedom extends DeathEffectGadget
{

	public DeathFreedom(GadgetManager manager)
	{
		super(manager, "Price of Freedom", UtilText.splitLineToArray(UtilText.colorWords("Freedom isn't always free, Soldier.",
				ChatColor.RED, ChatColor.WHITE, ChatColor.BLUE), LineFormat.LORE),
				-8, Material.WOOL, (byte) 0);
		setDisplayItem(CountryFlag.USA.getBanner());
	}

	@Override
	public void onBlood(Player player, BloodEvent event)
	{
		event.setCancelled(true);

		event.setItem(Material.INK_SACK, (byte) 15);

		Location loc = event.getLocation();
		UtilFirework.playFreedomFirework(loc);
	}

}
