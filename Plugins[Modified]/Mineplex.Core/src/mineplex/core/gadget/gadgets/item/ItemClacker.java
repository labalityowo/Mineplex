package mineplex.core.gadget.gadgets.item;

import java.time.Month;
import java.time.YearMonth;
import java.util.concurrent.TimeUnit;

import org.bukkit.Color;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilShapes;
import mineplex.core.common.util.particles.ColoredParticle;
import mineplex.core.common.util.particles.DustSpellColor;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.ItemGadget;
import mineplex.core.gadget.util.CostConstants;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.recharge.Recharge;

public class ItemClacker extends ItemGadget
{

	private static final int SWITCH_TICKS = 60, MAX_TICKS = 200;
	private static final int RADIUS = 2;
	private static final double DELTA_THETA = Math.PI / 13;
	private static final DustSpellColor COLOUR = new DustSpellColor(Color.YELLOW);

	public ItemClacker(GadgetManager manager)
	{
		super(manager, "Clacker Boomerang", new String[]
				{
						C.cGray + "An ancient fighting style involving",
						C.cGray + "a pair of Clackers. It's said that a true",
						C.cGray + "master of the art can turn them",
						C.cGray + "into boomerangs."
				}, CostConstants.POWERPLAY_BONUS, Material.STAINED_CLAY, (byte) 10, TimeUnit.SECONDS.toMillis(10), null);
		
		Free = false;
		setPPCYearMonth(YearMonth.of(2018, Month.JULY));
	}

	@Override
	public void ActivateCustom(Player player)
	{
		Location location = player.getLocation().subtract(0, 0.5, 0);
		location.add(location.getDirection());

		player.sendMessage(F.main(Manager.getName(), "You threw your " + F.name(getName()) + "."));

		if (Math.random() < 0.01)
		{
			player.sendMessage(F.main(Manager.getName(), "Is this some sort of JoJo's reference?"));
		}

		Clacker clacker = new Clacker(player, location);

		Manager.runSyncTimer(new BukkitRunnable()
		{
			@Override
			public void run()
			{
				if (clacker.update())
				{
					clacker.remove();
					cancel();
				}
			}
		}, 0, 1);
	}

	private class Clacker
	{

		private final Player _player;
		private final Location _location;
		private final ArmorStand _a, _b;

		private Vector _direction;
		private double _theta;
		private int _ticks;
		private boolean _backwards;

		Clacker(Player player, Location location)
		{
			_player = player;
			_location = location;
			_direction = location.getDirection().multiply(0.5);

			_a = location.getWorld().spawn(location, ArmorStand.class);
			_b = location.getWorld().spawn(location, ArmorStand.class);

			setupStand(_a);
			setupStand(_b);
		}

		private void setupStand(ArmorStand stand)
		{
			stand.setVisible(false);
			stand.setGravity(false);
			stand.setHelmet(new ItemBuilder(getDisplayMaterial(), getDisplayData())
					.build());
		}

		boolean update()
		{
			if (!_player.isOnline() || !_player.getWorld().equals(_location.getWorld()))
			{
				return true;
			}

			double x = RADIUS * Math.cos(_theta);
			double z = RADIUS * Math.sin(_theta);

			Location aLocation = _location.clone().add(x, 0, z);
			Location bLocation = _location.clone().subtract(x, 0, z);

			if (!_backwards && (UtilBlock.solid(aLocation.getBlock().getRelative(BlockFace.UP, 2)) || UtilBlock.solid(bLocation.getBlock().getRelative(BlockFace.UP, 2))))
			{
				_backwards = true;
			}

			UtilEnt.setPosition(_a, aLocation);
			UtilEnt.setPosition(_b, bLocation);

			UtilPlayer.getInRadius(_location, RADIUS + 1).forEach((player, scale) ->
			{
				if (_player.equals(player) || !Recharge.Instance.use(player, "Hit By " + getName(), 500, false, false) || !Manager.selectEntity(ItemClacker.this, player))
				{
					return;
				}

				Location location = player.getLocation();
				Vector direction = UtilAlg.getTrajectory(_location, location).multiply(1.5);
				direction.setY(0.7 + (Math.random() / 4));

				UtilAction.velocity(player, direction);

				UtilParticle.PlayParticleToAll(ParticleType.FIREWORKS_SPARK, location.add(0, 1, 0), 0.6F, 0.6F, 0.6F, 0.01F, 10, ViewDist.NORMAL);
				player.getWorld().playSound(location, Sound.NOTE_PLING, 1.5F, (float) (0.5 + Math.random() / 5));
				player.playEffect(EntityEffect.HURT);
				player.sendMessage(F.main(Manager.getName(), "You were hit by " + F.name(_player.getName()) + "'s " + F.name(getName()) + "."));
				_player.sendMessage(F.main(Manager.getName(), "You hit " + F.name(player.getName()) + " with your " + F.name(getName()) + "."));
			});

			if (_ticks % 10 == 0)
			{
				_location.getWorld().playSound(_location, Sound.ENDERDRAGON_WINGS, 1.5F, 0.2F);
			}

			_theta += DELTA_THETA;

			if (_backwards)
			{
				_direction.add(UtilAlg.getTrajectory(_location, _player.getLocation()).multiply(0.2));
				_location.add(_direction.multiply(0.5));
				_direction.normalize();

				if (_ticks++ == MAX_TICKS)
				{
					return true;
				}
				else if (UtilMath.offsetSquared(_location, _player.getLocation()) < 4)
				{
					_player.sendMessage(F.main(Manager.getName(), "You caught your " + F.name(getName()) + "."));
					return true;
				}
			}
			else
			{
				_location.add(_direction);

				if (_ticks++ == SWITCH_TICKS)
				{
					_backwards = true;
					_direction = _direction.multiply(-1);
				}
			}

			aLocation.add(0, 1.7, 0);
			bLocation.add(0, 1.7, 0);

			for (Location location : UtilShapes.getLinesLimitedPoints(aLocation, bLocation, RADIUS * 5))
			{
				new ColoredParticle(ParticleType.RED_DUST, COLOUR, location)
						.display(ViewDist.LONG);
			}

			return false;
		}

		void remove()
		{
			_a.remove();
			_b.remove();
		}

	}
}
