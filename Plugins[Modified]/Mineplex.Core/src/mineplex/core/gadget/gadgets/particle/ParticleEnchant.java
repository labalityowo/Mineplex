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

public class ParticleEnchant extends ParticleGadget
{
	public ParticleEnchant(GadgetManager manager)
	{
		super(manager, "Enchanted",
		        UtilText.splitLineToArray(
		                C.cGray + "The wisdom of the universe suddenly finds you extremely attractive, and wants to \'enchant\' you.",
		                LineFormat.LORE),
		        -2, Material.BOOK, (byte) 0);
	}

	@Override
	public void playParticle(Player player, UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK) return;

		if (Manager.isMoving(player))
		{
			UtilParticle.PlayParticle(ParticleType.ENCHANTMENT_TABLE, player.getLocation().add(0, 1, 0), 0.2f, 0.2f, 0.2f, 0, 4, ViewDist.NORMAL,
			        UtilServer.getPlayers());
		}
		else
		{
			UtilParticle.PlayParticle(ParticleType.ENCHANTMENT_TABLE, player.getLocation().add(0, 1.4, 0), 0f, 0f, 0f, 1, 4, ViewDist.NORMAL,
			        UtilServer.getPlayers());
		}
	}
}
