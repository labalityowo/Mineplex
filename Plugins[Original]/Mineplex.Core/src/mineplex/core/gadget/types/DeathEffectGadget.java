package mineplex.core.gadget.types;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.blood.BloodEvent;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.gadget.GadgetManager;

public abstract class DeathEffectGadget extends Gadget
{

	public DeathEffectGadget(GadgetManager manager, String name, String[] desc, int cost, Material mat, byte data, String... altNames)
	{
		super(manager, GadgetType.DEATH, name, desc, cost, mat, data, 1, altNames);
	}

	public boolean shouldDisplay(Player player)
	{
		return player != null && !UtilPlayer.isSpectator(player) && !Manager.hideParticles() && isActive(player);
	}
	
	@EventHandler
	public void callDeath(BloodEvent event)
	{
		if (!shouldDisplay(event.getPlayer()))
		{
			return;
		}

		onBlood(event.getPlayer(), event);
	}
	
	public abstract void onBlood(Player player, BloodEvent event);
	
}
