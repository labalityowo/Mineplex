package mineplex.core.gadget.set;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerVelocityEvent;

import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.arrowtrail.ArrowTrailStorm;
import mineplex.core.gadget.gadgets.death.DeathStorm;
import mineplex.core.gadget.gadgets.doublejump.DoubleJumpStorm;
import mineplex.core.gadget.gadgets.particle.ParticleRain;
import mineplex.core.gadget.types.GadgetSet;

public class SetHowlingWinds extends GadgetSet
{

	public SetHowlingWinds(GadgetManager manager)
	{
		super(manager, "Howling Winds", "The winds carry you further. Any velocity you have is doubled (In Lobbies Only)",
				manager.getGadget(ArrowTrailStorm.class),
				manager.getGadget(DeathStorm.class),
				manager.getGadget(DoubleJumpStorm.class),
				manager.getGadget(ParticleRain.class));
	}

	@EventHandler
	public void playerVelocity(PlayerVelocityEvent event)
	{
		if (Manager.isGameLive())
		{
			return;
		}

		Player player = event.getPlayer();

		if (!isActive(player))
		{
			return;
		}

		event.getVelocity().multiply(2);
	}

}
