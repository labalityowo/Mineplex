package mineplex.game.clans.items.legendaries;

import java.lang.reflect.Field;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.core.common.util.UtilTime;
import mineplex.core.recharge.Recharge;
import mineplex.game.clans.items.GearManager;
import mineplex.game.clans.items.PlayerGear;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import net.minecraft.server.v1_8_R3.PlayerConnection;

public class WindBlade extends LegendaryItem
{
	private static final Field G_FIELD;

	static
	{
		try
		{
			G_FIELD = PlayerConnection.class.getDeclaredField("g");
			G_FIELD.setAccessible(true);
		}
		catch (ReflectiveOperationException exception)
		{
			throw new RuntimeException("Could not reflectively access field", exception);
		}
		
		UtilServer.RegisterEvents(new Listener()
		{
			@EventHandler
			public void onFall(CustomDamageEvent event)
			{
				if (event.GetDamageePlayer() != null && event.GetCause() == EntityDamageEvent.DamageCause.FALL)
				{
					PlayerGear playerGear = GearManager.getInstance().getPlayerGear(event.GetDamageePlayer());
					if (playerGear.getWeapon() instanceof WindBlade)
					{
						event.SetCancelled("Wind Blade No Fall Damage");
					}
				}
			}
		});
	}

	private static final double FLIGHT_VELOCITY = 0.75d;

	private double _power;
	private double _burnoutThreshold;

	private int _messageTimer;

	public WindBlade()
	{
		super("Wind Blade", new String[]
		{
			C.cWhite + "Long ago, a race of cloud dwellers",
			C.cWhite + "terrorized the skies. A remnant of",
			C.cWhite + "their tyranny, this airy blade is",
			C.cWhite + "the last surviving memorium from",
			C.cWhite + "their final battle against the Titans.",
			" ",
			C.cWhite + "Deals " + C.cYellow + "7 Damage" + C.cWhite + " with attack",
			C.cYellow + "Right-Click" + C.cWhite + " to use " + C.cGreen + "Flight",
		}, Material.GREEN_RECORD);
	}

	@Override
	public void update(Player wielder)
	{
		long burnoutRemaining = -1L;

		if (Recharge.Instance.Get(wielder) != null && Recharge.Instance.Get(wielder).containsKey("clans_legendary_windblade_burnout"))
		{
			burnoutRemaining = Recharge.Instance.Get(wielder).get("clans_legendary_windblade_burnout").GetRemaining();
		}

		// Check if player is attempting to fly and activate
		if (isHoldingRightClick())
		{
			if (canPropel(wielder))
			{
				wielder.setFallDistance(0f);

				if (burnoutRemaining > 0)
				{
					_messageTimer++;

					if (_messageTimer % 4 == 0)
					{
						UtilParticle.PlayParticle(ParticleType.SMOKE, wielder.getLocation(), 0.f, 0.f, 0.f, .1f, 1, ViewDist.NORMAL);
						wielder.playSound(wielder.getLocation(), Sound.FIZZ, .5f, 1.f);

						removePower(0.15);

						_burnoutThreshold = 0;
					}

					if (_messageTimer % 5 == 0)
					{
						UtilPlayer.message(wielder, F.main("Wind Blade", "Flight power damaged whilst scraping the ground! Repairs will be finish in " + F.time(UtilTime.MakeStr(burnoutRemaining)) + "."));
					}

					if (_messageTimer % 10 == 0)
					{
						wielder.playSound(wielder.getLocation(), Sound.ANVIL_USE, .5f, 1.5f);
					}

					return;
				}

				removePower(UtilEnt.isGrounded(wielder) ? 1.17 : .88);

				propelPlayer(wielder);
				UtilParticle.PlayParticle(ParticleType.EXPLODE, wielder.getLocation().add(0, 1, 0), 0, 0, 0, .1f, 3, ViewDist.NORMAL);

				wielder.playSound(wielder.getLocation(), Sound.FIRE, .25f, 1.75f);
			}

			if (UtilEnt.isGrounded(wielder))
			{
				_burnoutThreshold++;
				wielder.playSound(wielder.getLocation(), Sound.NOTE_STICKS, .75f, 2.f);
			}
			else
			{
				_burnoutThreshold = 0;
			}
		}
		else
		{
			_burnoutThreshold = UtilMath.clamp(_burnoutThreshold - .5, 0, _burnoutThreshold);
		}

		if (UtilEnt.isGrounded(wielder, wielder.getLocation()) || UtilEnt.isGrounded(wielder, wielder.getLocation().subtract(0, 1, 0)))
		{
			addPower(0.65);
		}

		if (_burnoutThreshold > 15 && burnoutRemaining <= 0)
		{
			Recharge.Instance.use(wielder, "clans_legendary_windblade_burnout", 2500, false, false);
		}

		UtilTextBottom.displayProgress(UtilMath.clamp(_power, .0, 80.) / 80., wielder);
	}

	@Override
	public void onAttack(CustomDamageEvent event, Player wielder)
	{
		event.AddMod("Wind Blade", 6);
	}

	private void propelPlayer(Player player)
	{
		Vector direction = player.getLocation().getDirection().normalize();
		direction.multiply(FLIGHT_VELOCITY);

		player.setVelocity(direction);

		PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
		try
		{
			G_FIELD.set(connection, 0);
		}
		catch (IllegalAccessException e)
		{
			new RuntimeException("Could not update g field", e).printStackTrace();
		}
	}

	private boolean canPropel(Player player)
	{
		return _power > 0 && !UtilItem.isLiquid(player.getLocation().getBlock().getType());
	}

	private void addPower(double power)
	{
		_power = UtilMath.clamp(_power + power, -20, 80);
	}

	private void removePower(double power)
	{
		_power = UtilMath.clamp(_power - power, -20, 80);
	}
}