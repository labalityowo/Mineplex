package mineplex.core.gadget.gadgets.morph;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilText;
import mineplex.core.disguise.disguises.DisguiseCow;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.morph.managers.UtilMorph;
import mineplex.core.gadget.types.MorphGadget;
import mineplex.core.recharge.Recharge;

public class MorphCow extends MorphGadget
{	
	public MorphCow(GadgetManager manager)
	{
		super(manager, "Cow Morph", UtilText.splitLinesToArray(new String[] 
				{
				C.cGray + "How now brown cow?",
				C.blankLine,
				"#" + C.cWhite + "Left Click to use Moo",
				}, LineFormat.LORE),
				6000,
				Material.LEATHER, (byte)0);
	}

	@Override
	public void enableCustom(final Player player, boolean message)
	{
		this.applyArmor(player, message);

		DisguiseCow disguise = new DisguiseCow(player);
		UtilMorph.disguise(player, disguise, Manager);
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		this.removeArmor(player);
		UtilMorph.undisguise(player, Manager.getDisguiseManager());
	}

	@EventHandler
	public void Audio(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();

		if (!isActive(player))
			return;

		if (!UtilEvent.isAction(event, ActionType.L))
			return;
		
		if (!Recharge.Instance.use(player, getName(), 2500, false, false, "Cosmetics"))
			return;
		
		player.getWorld().playSound(player.getLocation(), Sound.COW_IDLE, 1f, 1f);
		
	}
}
