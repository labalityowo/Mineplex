package mineplex.core.gadget.types;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

/**
 * A wrapper for particle trail gadgets
 */

public abstract class ParticleGadget extends Gadget
{
	/**
	 * @param manager The normal gadget manager
	 * @param name The display name of the particle trail
	 * @param desc The display description of the particle trail
	 * @param cost The shard cost of the particle trail
	 * @param mat The Material type used to display this particle trail in GUIs
	 * @param data The data used to display this particle trail in GUIs
	 * @param altNames Alternative packet names
	 */
	public ParticleGadget(GadgetManager manager, String name, String[] desc, int cost, Material mat, byte data, String...altNames) 
	{
		super(manager, GadgetType.PARTICLE, name, desc, cost, mat, data, 1, altNames);
	}

	@Override
	public void enableCustom(Player player, boolean message)
	{
		Manager.removeGadgetType(player, GadgetType.PARTICLE, this);
		_active.add(player);
		startEffect(player);
		if (message)
			UtilPlayer.message(player, F.main("Gadget", "You summoned " + F.elem(getName()) + "."));
	}
	
	@Override
	public void disableCustom(Player player, boolean message)
	{
		if (_active.remove(player))
		{
			stopEffect(player);
			UtilPlayer.message(player, F.main("Gadget", "You unsummoned " + F.elem(getName()) + "."));
		}
	}
	
	private boolean shouldDisplay(Player player)
	{
		return !UtilPlayer.isSpectator(player) && !Manager.hideParticles();
	}
	
	@EventHandler
	public void Caller(UpdateEvent event)
	{
		for (Player player : getActive())
		{
			if (!shouldDisplay(player))
				continue;
			playParticle(player, event);
		}
	}
	
	/**
	 * Called every time time {@link UpdateType} is called, used to display this particle trail to the provided player
	 * No need to check if the particle is valid for display for this player.
	 * @param player The player which the trail should be displayed to.
	 * @param event The update event
	 */
	public abstract void playParticle(Player player, UpdateEvent event);

	/**
	 * Called when the effect starts
	 * @param player The player that activated it
	 */
	public void startEffect(Player player){}

	/**
	 * Called when the effect ends
	 * @param player The player that disabled it
	 */
	public void stopEffect(Player player){}
}
