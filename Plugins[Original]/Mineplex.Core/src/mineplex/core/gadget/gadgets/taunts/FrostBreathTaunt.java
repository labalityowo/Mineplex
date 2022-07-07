package mineplex.core.gadget.gadgets.taunts;

import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.TauntGadget;
import mineplex.core.gadget.util.CostConstants;
import mineplex.core.particleeffects.SpiralEffect;
import mineplex.core.recharge.Recharge;

public class FrostBreathTaunt extends TauntGadget
{

	private static final long COOLDOWN = TimeUnit.SECONDS.toMillis(20);
	private static final int TICKS = 80;
	private static final double MAX_RADIUS = 1.3;
	private static final ItemStack[] TO_DROP =
			{
					new ItemStack(Material.ICE),
					new ItemStack(Material.SNOW_BALL),
					new ItemStack(Material.PACKED_ICE)
			};

	public FrostBreathTaunt(GadgetManager manager)
	{
		super(manager, "Frost Breath Taunt",
				UtilText.splitLineToArray(C.cGray + "Send shivers down the spines of your enemies with this chilling taunt.", LineFormat.LORE),
				CostConstants.FOUND_IN_GINGERBREAD_CHESTS, Material.PACKED_ICE, (byte) 0);
	}

	@Override
	public boolean onStart(Player player)
	{
		if (!Recharge.Instance.use(player, getName(), COOLDOWN, true, false, "Cosmetics"))
		{
			return false;
		}

		Player other = UtilPlayer.getPlayerInSight(player, 64, true);

		if (other != null)
		{
			Bukkit.broadcastMessage(F.main(Manager.getName(), F.name(other.getName()) + " felt shivers down their spine."));
		}

		Location location = player.getLocation().add(0, 1.5, 0);
		location.add(location.getDirection());

		location.getWorld().playSound(location, Sound.ENDERDRAGON_GROWL, 1, 1);

		new SpiralEffect(TICKS, MAX_RADIUS, location)
		{
			@Override
			public void playParticle(Location location)
			{
				UtilParticle.PlayParticleToAll(ParticleType.SNOW_SHOVEL, location, null, 0, 1, ViewDist.NORMAL);
				UtilParticle.PlayParticleToAll(ParticleType.DRIP_WATER, location, null, 0, 1, ViewDist.NORMAL);

				if (Math.random() < 0.2)
				{
					UtilItem.dropItem(UtilMath.randomElement(TO_DROP), location, true, false, 10, false);
				}
			}
		}.start();

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
