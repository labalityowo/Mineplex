package mineplex.minecraft.game.core.boss.broodmother.attacks;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilServer;
import mineplex.minecraft.game.core.boss.BossAbility;
import mineplex.minecraft.game.core.boss.broodmother.SpiderCreature;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Spider;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class SpiderPoisonBarrage extends BossAbility<SpiderCreature, Spider>
{
	private int _poisonAmount = UtilMath.r(20) + 30;
	private int _tick;
	private float _yawToFace;

	public SpiderPoisonBarrage(SpiderCreature creature)
	{
		super(creature);
	}

	@Override
	public boolean canMove()
	{
		return _poisonAmount <= 0;
	}

	@Override
	public boolean inProgress()
	{
		return _poisonAmount > 0;
	}

	@Override
	public boolean hasFinished()
	{
		return _poisonAmount <= 0;
	}

	@Override
	public void setFinished()
	{
	}

	@Override
	public int getCooldown()
	{
		return 10;
	}

	/*@Override
	public Player getTarget()
	{
		return getTarget(1, 30);
	}*/

	@Override
	public void tick()
	{
		if (_tick == 0)
		{
			Player target = getTarget();

			if (target != null)
			{
				UtilEnt.CreatureLook(getEntity(), target);

				Vector vec = UtilAlg.getTrajectory(getEntity(), target);

				_yawToFace = UtilAlg.GetYaw(vec);
			}
		}

		{
			Location loc = getEntity().getEyeLocation().add(getLocation().getDirection().setY(0).normalize().multiply(0.4));

			UtilParticle.PlayParticle(ParticleType.ICON_CRACK.getParticle(Material.SLIME_BALL, 0), loc, 0.3F, 0.3F, 0.3F, 0, 30,
					ViewDist.NORMAL, UtilServer.getPlayers());
		}

		if (_tick > 30)
		{
			Player target = getTarget();

			if (target != null)
			{
				Vector vec = UtilAlg.getTrajectory(getEntity(), target);

				_yawToFace = UtilAlg.GetYaw(vec);
			}

			float currentYaw = getLocation().getYaw();

			if ((int) currentYaw != (int) _yawToFace)
			{
				// How much should the yaw change if we go either direction, would it be faster if we subtracted or added?
				float added = ((_yawToFace + 720) - currentYaw) % 360;
				float subtracted = /*-*/((currentYaw + 720) - _yawToFace) % 360;

				float diff = Math.max(-10, Math.min(10, added > subtracted ? -subtracted : added));

				float newYaw = (currentYaw + diff + 720) % 360;

				UtilEnt.CreatureLook(getEntity(), 0, newYaw);

				System.out.print("Current yaw: " + currentYaw + ", Yaw to face: " + _yawToFace + ", New Yaw: " + newYaw
						+ ", Add: " + added + ", Subtracted: " + subtracted);
			}

			if (_tick % 2 == 0)
			{
				Vector vec = getLocation().getDirection();

				vec.setX(vec.getX() + UtilMath.rr(0.09, true));
				vec.setZ(vec.getZ() + UtilMath.rr(0.09, true));

				vec.setY(0).normalize().multiply(1 + UtilMath.rr(0.2, true));
				vec.setY(0.35 + UtilMath.rr(0.3, false));

				Item item = getLocation().getWorld()
						.dropItem(getLocation().add(0, 0.5, 0), new ItemStack(Material.SLIME_BALL));

				item.setVelocity(vec);

				new SpiderPoison(getBoss(), item);

				getLocation().getWorld().playSound(getLocation(), Sound.CREEPER_HISS, 2, 0F);

				_poisonAmount--;
			}
		}

		_tick++;
	}
}
