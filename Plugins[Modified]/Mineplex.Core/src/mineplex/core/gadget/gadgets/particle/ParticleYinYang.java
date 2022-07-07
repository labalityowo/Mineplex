package mineplex.core.gadget.gadgets.particle;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.MathHelper;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.particles.ColoredParticle;
import mineplex.core.common.util.particles.DustSpellColor;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.ParticleGadget;
import mineplex.core.gadget.util.CostConstants;
import mineplex.core.recharge.Recharge;
import mineplex.core.recharge.RechargeData;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class ParticleYinYang extends ParticleGadget
{

	public ParticleYinYang(GadgetManager manager)
	{
		super(manager, "Yin and Yang",
				UtilText.splitLineToArray(ChatColor.GRAY + "Achieve the balance of the universe and watch it spin beneath your feet.", LineFormat.LORE),
				CostConstants.FOUND_IN_TREASURE_CHESTS, Material.RECORD_9, (byte) 0);
	}

	@Override
	public void playParticle(Player player, UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK) return;

		float step = (float) (Math.PI / 30);
		step *= event.getTick() % 60;

		float x = MathHelper.sin(step) * 0.3f;
		float y = 0;
		float z = MathHelper.cos(step) * 0.3f;

		Location loc = player.getLocation().add(0, 0.2, 0);
		boolean setBonus = getSet().isActive(player);
		RechargeData r = Recharge.Instance.Get(player).get(getName());

		if (Manager.isMoving(player))
		{
			long time = 50;
			if (r != null)
			{
				time = Math.min(1000, r.GetRemaining() + 200);
			}
			Recharge.Instance.useForce(player, getName(), time);
		}

		if (r != null)
		{
			float d = r.GetRemaining() / 1000f;
			y = 0.3f * d;
			x *= 0.1 + ((1 - d) * 0.9);
			z *= 0.1 + ((1 - d) * 0.9);
		}

		UtilParticle.playParticleFor(player, ParticleType.FIREWORKS_SPARK, loc, new Vector(x, y, z), 1, 0, ViewDist.NORMAL);
		UtilParticle.playParticleFor(player, ParticleType.FIREWORKS_SPARK, loc, new Vector(-x, y, -z), 1, 0, ViewDist.NORMAL);

		if (y == 0)
		{
			int a = 2;

			double x2 = Math.sin(step + Math.PI / 2);
			double z2 = Math.cos(step + Math.PI / 2);

			Vector v = new Vector(x2, 0, z2).multiply(1.5);
			DustSpellColor color = new DustSpellColor(Color.fromRGB(UtilMath.r(256), UtilMath.r(256), UtilMath.r(256)));

			if (setBonus)
			{
				new ColoredParticle(ParticleType.RED_DUST, color, loc.clone().add(v))
						.display();
				new ColoredParticle(ParticleType.RED_DUST, color, loc.clone().subtract(v))
						.display();
			}
			else
			{
				UtilParticle.playParticleFor(player, ParticleType.SMOKE, loc.clone().add(v), null, 0, 2, ViewDist.NORMAL);
				UtilParticle.playParticleFor(player, ParticleType.SMOKE, loc.clone().subtract(v), null, 0, 2, ViewDist.NORMAL);
			}
		}

	}

}
