package mineplex.game.clans.items.legendaries;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.core.ClaimLocation;
import mineplex.game.clans.core.repository.ClanTerritory;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class MagneticMaul extends LegendaryItem
{
	private static final double PULL_RANGE = 10d;
	
	private double _power;
	private double _heat;
	
	public MagneticMaul()
	{
		super("Magnetic Maul", UtilText.splitLinesToArray(new String[]
		{
			C.cWhite + "For centuries, warlords used this hammer to control their subjects. This brutal weapon allows you to pull your enemies towards you with magnetic force!",
			" ",
			"#" + C.cWhite + "Deals " + C.cYellow + "8 Damage" + C.cWhite + " with attack",
			"#" + C.cYellow + "Right-Click" + C.cWhite + " to use " + C.cGreen + "Magnetism"
		}, LineFormat.LORE), Material.RECORD_5);
	}
	
	@Override
	public void update(Player wielder)
	{
		Location loc = wielder.getLocation();
		ClanTerritory territory = ClansManager.getInstance().getClaimMap().get(ClaimLocation.of(loc.getChunk()));
		if (territory != null && territory.isSafe(loc))
		{
			return;
		}
		if (ClansManager.getInstance().hasTimer(wielder))
		{
			UtilPlayer.message(wielder, F.main("Clans", "You are not allowed to use the Magnetic Maul whilst protected from PvP. Run " + F.elem("/pvp") + " to enable PvP!"));
			return;
		}
		
		if (isHoldingRightClick())
		{
			if (canPull())
			{
				pullEntities(wielder);
			}
		}
		else
		{
			addPower(Math.max(0, 0.65 - (_heat / 150)));
			
			if (_heat > 0)
			{
				_heat--;
			}
		}
		
		UtilTextBottom.displayProgress(_power / 80., wielder);
	}


	@Override
	public void onAttack(CustomDamageEvent event, Player wielder)
	{
		event.AddKnockback("Magnetic Blade", -0.5d); // Pull players with negative knockback
		event.AddMod("Magnetic Maul", 7);
		log("Negative knockback!");
	}

	private void pullEntities(Player player)
	{
		Vector direction = player.getLocation().getDirection().normalize().multiply(10.0d);
		Location target = player.getEyeLocation().add(direction);
		
		double targetDistance = player.getLocation().distance(target);
		
		for (LivingEntity entity : player.getWorld().getLivingEntities())
		{				
			if (entity.getEntityId() == player.getEntityId())
			{
				continue; // Skip pulling self
			}
			if (entity.getPassenger() != null && entity.getPassenger().getEntityId() == player.getEntityId())
			{
				continue;
			}
			if (UtilEnt.hasFlag(entity, "LegendaryAbility.IgnoreMe"))
			{
				continue;
			}
			
			double otherDistance = player.getLocation().distance(entity.getLocation());
			double otherTargetDistance = target.distance(entity.getLocation());
			
			// If player is in-front of us and within pulling range
			if (otherTargetDistance < targetDistance && otherDistance <= PULL_RANGE)
			{
				// If entity is in safe zone, don't allow pulling of that entity.
				if (ClansManager.getInstance().getClaimMap().containsKey(ClaimLocation.of(entity.getLocation().getChunk())))
				{
					if (ClansManager.getInstance().getClaimMap().get(ClaimLocation.of(entity.getLocation().getChunk())).isSafe(entity.getLocation()))
					{
						continue;
					}
				}
				
				UtilAction.velocity(entity, UtilAlg.getTrajectory(entity, player), 0.3, false, 0, 0, 1, true);
			}
		}
		
		// Do Particles
        for (int i = 0; i < 6; i++)
        {
			Vector random = new Vector(Math.random() * 4 - 2, Math.random() * 4 - 2, Math.random() * 4 - 2);

			Location origin = player.getLocation().add(0, 1.3, 0);
			origin.add(player.getLocation().getDirection().multiply(10));
			origin.add(random);

			Vector vel = UtilAlg.getTrajectory(origin, player.getLocation().add(0, 1.3, 0));
			vel.multiply(7);

			UtilParticle.PlayParticle(ParticleType.MAGIC_CRIT,
							origin,
							(float)vel.getX(),
							(float)vel.getY(),
							(float)vel.getZ(),
							1, 0, ViewDist.LONG, UtilServer.getPlayers());
        }
		
		removePower(1.75);
		_heat++;
	}
	
	private void addPower(double power)
	{
		_power = UtilMath.clamp(_power + power, 0, 80);
	}
	
	private void removePower(double power)
	{
		_power = UtilMath.clamp(_power - power, 0, 80);
	}
	
	private boolean canPull()
	{
		return _power >= 10 && _heat < 70;
	}
}