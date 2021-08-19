package mineplex.core.gadget.gadgets.taunts;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.TauntGadget;
import mineplex.core.particleeffects.EffectLocation;
import mineplex.core.particleeffects.RainbowTauntEffect;
import mineplex.core.recharge.Recharge;

public class RainbowTaunt extends TauntGadget
{

	private static final int COOLDOWN = 30000;
	private static final int PVP_COOLDOWN = 10000;

	public RainbowTaunt(GadgetManager manager)
	{
		super(manager, "Rainbow Taunt", UtilText.splitLinesToArray(new String[]
						{
								C.cGray + "At the end of every Rainbow they say there is a pot of Gold.",
								"",
								C.cWhite + "Use /taunt in game to use this taunt.",
								C.cGreen + "Can be used while in PvP!"
						},
				LineFormat.LORE), -18, Material.WOOL, (byte) 11);
		setCanPlayWithPvp(false);
		setPvpCooldown(PVP_COOLDOWN);
		setShouldPlay(false);
	}

	@Override
	public boolean onStart(Player player)
	{
		if (!Recharge.Instance.use(player, getName(), COOLDOWN, true, false, "Cosmetics"))
		{
			return false;
		}

		Vector dir = player.getLocation().getDirection();
		Vector sideA = dir.clone().setX(-dir.getZ()).setZ(dir.getX());
		Vector sideB = dir.clone().setX(dir.getZ()).setZ(-dir.getX());

		Location start = player.getLocation().clone().add(sideA.multiply(4).toLocation(player.getWorld()));
		Location end = player.getLocation().clone().add(sideB.multiply(4).toLocation(player.getWorld()));

		RainbowTauntEffect rainbowTauntEffect = new RainbowTauntEffect(start);
		rainbowTauntEffect.setTargetLocation(new EffectLocation(end));
		rainbowTauntEffect.start();

		return true;
	}

	@Override
	public void onPlay(Player player)
	{

	}

	@Override
	public void onFinish(Player player)
	{

	}

}
