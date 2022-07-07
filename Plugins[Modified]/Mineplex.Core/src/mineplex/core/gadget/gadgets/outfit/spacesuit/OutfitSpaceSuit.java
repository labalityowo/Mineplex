package mineplex.core.gadget.gadgets.outfit.spacesuit;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.OutfitGadget;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class OutfitSpaceSuit extends OutfitGadget
{
	public OutfitSpaceSuit(GadgetManager manager, String name, 
			int cost, ArmorSlot slot, Material mat, byte data)
	{
		super(manager, name, 
				UtilText.splitLineToArray(C.cGray + "Designed specifically for combat in deep space for a sport known as \'Gravity\'", LineFormat.LORE), 
				cost, slot, mat, data);
	}
	
	@EventHandler
	public void setBonus(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;
		
		if (getSlot() != ArmorSlot.HELMET)
			return;
		
		for (Player player : UtilServer.getPlayers())
			if (getSet() != null && getSet().isActive(player))
				player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 39, 7, true, false), true);
	}
}
