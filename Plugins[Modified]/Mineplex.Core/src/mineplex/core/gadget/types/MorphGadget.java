package mineplex.core.gadget.types;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.gadget.GadgetManager;

public abstract class MorphGadget extends Gadget
{

	public MorphGadget(GadgetManager manager, String name, String[] desc, int cost, Material mat, byte data) 
	{
		super(manager, GadgetType.MORPH, name, desc, cost, mat, data);
	}
	
	public void applyArmor(Player player, boolean message)
	{
		Manager.removeGadgetType(player, GadgetType.MORPH, this);
		Manager.removeGadgetType(player, GadgetType.COSTUME, this);
		Manager.removeGadgetType(player, GadgetType.FLAG, this);
		
		_active.add(player);

		if (message)
			UtilPlayer.message(player, F.main("Gadget", "You morphed into " + F.elem(getName()) + "."));
	}
	
	public void removeArmor(Player player)
	{
		if (_active.remove(player))
			UtilPlayer.message(player, F.main("Gadget", "You unmorphed from " + F.elem(getName()) + "."));
	}
	
	@EventHandler
	public void playerDeath(PlayerDeathEvent event)
	{
		disable(event.getEntity());
	}
}
