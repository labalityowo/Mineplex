package mineplex.core.gadget.gadgets.particle;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.ParticleGadget;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class ParticleEmerald extends ParticleGadget
{

	public ParticleEmerald(GadgetManager manager)
	{
		super(manager, "Emerald Twirl",
		        UtilText.splitLineToArray(C.cGreen + "With these sparkles, you can sparkle while sparkle with CaptainSparklez!", LineFormat.LORE),
		        -2, Material.EMERALD, (byte) 0, "Green Ring");
	}

	public void playParticle(Player player, UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK) return;

		int tick = player.getTicksLived();
		
		float x = (float) (Math.sin(tick / 7d) * 1f);
		float z = (float) (Math.cos(tick / 7d) * 1f);
		float y = (float) (Math.cos(tick / 17d) * 1f + 1f);

		UtilParticle.PlayParticle(ParticleType.HAPPY_VILLAGER, player.getLocation().add(x, y, z), 0f, 0f, 0f, 0, 1, ViewDist.NORMAL,
		        UtilServer.getPlayers());
		
		if(getSet() == null || !getSet().isActive(player)) return;
		
		if(Manager.isMoving(player))
		{
			UtilParticle.PlayParticle(ParticleType.HAPPY_VILLAGER, player.getLocation().add(0, 0.2, 0), 0.3f, 0f, 0.3f, 0, 2, ViewDist.NORMAL,
			        UtilServer.getPlayers());
		}
	}
}
