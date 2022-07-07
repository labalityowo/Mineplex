package mineplex.core.gadget.gadgets.death;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.blood.BloodEvent;
import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.DeathEffectGadget;

public class DeathPinataBurst extends DeathEffectGadget
{
	
	private byte[] _data = new byte[]{1,2,4,5,6,9,10,11,12,13,14,15};

	public DeathPinataBurst(GadgetManager manager)
	{
		super(manager, "Pinata Burst",
				UtilText.splitLineToArray(C.cGray + "Kill 'em with kindness and " + C.cPurple + "candy!", LineFormat.LORE),
				-2, Material.FIREWORK, (byte)0);
	}
	
	@Override
	public void onBlood(Player player, BloodEvent event)
	{
		event.setCancelled(true);
		for(byte d : _data)
		{
			ItemStack item = new ItemStack(Material.INK_SACK, 1, (short) 0, d);
			UtilItem.dropItem(item, event.getLocation(), true, false, 40, false);
		}
	}

}
