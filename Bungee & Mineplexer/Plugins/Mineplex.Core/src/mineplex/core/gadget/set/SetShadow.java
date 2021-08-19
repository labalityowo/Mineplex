package mineplex.core.gadget.set;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.arrowtrail.ArrowTrailShadow;
import mineplex.core.gadget.gadgets.death.DeathShadow;
import mineplex.core.gadget.gadgets.doublejump.DoubleJumpShadow;
import mineplex.core.gadget.gadgets.particle.ParticleFoot;
import mineplex.core.gadget.types.GadgetSet;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class SetShadow extends GadgetSet
{

	private static final PotionEffect POTION_EFFECT = new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false);

	public SetShadow(GadgetManager manager)
	{
		super(manager, "Shadow", "Standing still causes you to be completely invisible (In Lobbies Only)",
				manager.getGadget(ArrowTrailShadow.class),
				manager.getGadget(DeathShadow.class),
				manager.getGadget(DoubleJumpShadow.class),
				manager.getGadget(ParticleFoot.class));
	}

	@Override
	public void customDisable(Player player)
	{
		super.customDisable(player);

		player.removePotionEffect(PotionEffectType.INVISIBILITY);
	}

	@EventHandler
	public void updateMovement(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC || Manager.isGameLive())
		{
			return;
		}

		for (Player player : _active)
		{
			if (Manager.isMoving(player))
			{
				player.removePotionEffect(PotionEffectType.INVISIBILITY);
			}
			else
			{
				if (player.addPotionEffect(POTION_EFFECT))
				{
					UtilParticle.PlayParticleToAll(ParticleType.LARGE_SMOKE, player.getLocation().add(0, 0.5, 0), 0.6F, 0.6F, 0.6F, 0, 20, ViewDist.NORMAL);
				}
				else
				{
					UtilParticle.PlayParticleToAll(ParticleType.LARGE_SMOKE, player.getLocation().add(0, 0.2, 0), null, 0, 1, ViewDist.NORMAL);
				}
			}
		}
	}
}
