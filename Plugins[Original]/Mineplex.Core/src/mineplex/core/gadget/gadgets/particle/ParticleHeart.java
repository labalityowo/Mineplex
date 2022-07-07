package mineplex.core.gadget.gadgets.particle;

import mineplex.core.common.util.UtilMath;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.ParticleGadget;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class ParticleHeart extends ParticleGadget
{

	public ParticleHeart(GadgetManager manager)
	{
		super(manager, "Cupid's Love",
		        UtilText.splitLineToArray(C.cGray + "Share the love you feel in your heart with everybody near you!", LineFormat.LORE), -2,
		        Material.APPLE, (byte) 0, "I Heart You", "Cupids Love", "Heartfelt Halo");
	}

	@Override
	public void playParticle(Player player, UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTEST) return;

		if (Manager.isMoving(player))
		{
			if(getSet() == null || !getSet().isActive(player)) return;
			
			UtilParticle.playParticleFor(player, ParticleType.HEART, player.getLocation().add(0, 1.2, 0), null, 0, 1, ViewDist.NORMAL);
		}
		else
		{
			UtilParticle.playParticleFor(player, ParticleType.HEART, UtilMath.gauss(player.getLocation(), 1, 3, 1).add(0, 1.2, 0), null, 0, 1, ViewDist.NORMAL);
		}
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		if (_active.remove(player)) UtilPlayer.message(player, F.main("Gadget", "You unsummoned " + F.elem(getName()) + "."));
	}

	@EventHandler
	public void quit(PlayerQuitEvent event)
	{
	}
	
}
