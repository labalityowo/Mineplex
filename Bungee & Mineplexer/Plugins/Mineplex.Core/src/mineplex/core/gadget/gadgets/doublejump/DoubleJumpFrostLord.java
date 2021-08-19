package mineplex.core.gadget.gadgets.doublejump;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilServer;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.DoubleJumpEffectGadget;

public class DoubleJumpFrostLord extends DoubleJumpEffectGadget
{
	public DoubleJumpFrostLord(GadgetManager manager)
	{
		super(manager, "Gust of the Frost Lord", 
				UtilText.splitLineToArray(C.cGray + "Listen, the Frost Lord is a very proper individual. Stop making fart jokes!", LineFormat.LORE),
				-3,
				Material.SNOW_BALL, (byte)0, "Frost Lord");
	}

	@Override
	public void doEffect(Player player)
	{
		UtilParticle.PlayParticle(ParticleType.SNOW_SHOVEL, player.getLocation(), 0f, 0f, 0f, 0.6f, 100,
				ViewDist.LONGER, UtilServer.getPlayers());
	}
}
