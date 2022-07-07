package nautilus.game.arcade.game.games.christmas.content;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftCreature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Snowman;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.recharge.Recharge;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.christmas.Christmas;
import nautilus.game.arcade.game.games.christmas.ChristmasAudio;
import net.minecraft.server.v1_8_R3.EntityCreature;

public class SnowmanBoss 
{
	private Christmas Host;
	private Location _spawn;

	private ArrayList<SnowmanMinion> _minions;
	private IronGolem _heart;
	private boolean _sword = false;
	private boolean _joke = true;

	public SnowmanBoss(Christmas host, Location spawn)
	{
		Host = host;
		_spawn = spawn;

		_minions = new ArrayList<SnowmanMinion>();

		//Spawn
		Host.CreatureAllowOverride = true;

		for (int i=0 ; i<14 ; i++)
			_minions.add(new SnowmanMinion(_spawn.getWorld().spawn(_spawn, Snowman.class)));

		_heart = _spawn.getWorld().spawn(_spawn, IronGolem.class);
		_heart.setMaxHealth(1400);
		_heart.setHealth(1400);
		UtilEnt.vegetate(_heart);

		Host.CreatureAllowOverride = false;

		//Stack
		Entity base = null;
		for (SnowmanMinion ent : _minions)
		{
			if (base != null)
				base.setPassenger(ent.Ent);

			base = ent.Ent;
		}

		base.setPassenger(_heart);
	}
	
	public void UpdateSnowball()
	{
		if (Host.GetPlayers(true).isEmpty())
			return;
		
		//Count Size
		int size = 0;
		Entity bottom = _heart;
		while (bottom.getVehicle() != null)
		{
			bottom = bottom.getVehicle();
			size++;
		}
		if (size < 3)
			return;
		
		//Throw Snowballs
		Entity ent = _heart;
		while (ent.getVehicle() != null)
		{
			ent = ent.getVehicle();
			
			if (Math.random() > 0.01 * Host.GetPlayers(true).size())
				continue;
			
			Player target = UtilAlg.Random(Host.GetPlayers(true));
			
			Vector dir = UtilAlg.getTrajectory(ent, target);
			dir.multiply(2);
			
			Snowball ball = ent.getWorld().spawn(ent.getLocation().add(0, 1, 0).add(dir), Snowball.class);
			
			ball.setShooter((LivingEntity) ent);
			ball.setVelocity(dir.add(new Vector(0, Math.min(0.6, UtilMath.offset2d(target, ent)/150), 0)));
		}
	}

	public void UpdateMove()
	{
		if (_heart != null)
		{
			//Orbit
			double speed = 20d;
			double oX = Math.sin(_heart.getTicksLived()/speed) * 8;
			double oY = 0;
			double oZ = Math.cos(_heart.getTicksLived()/speed) * 8;
			Location loc = _spawn.clone().add(oX, oY, oZ);

			Entity bottomEnt = _heart;
			while (bottomEnt.getVehicle() != null)
				bottomEnt = bottomEnt.getVehicle();
			
			float rate = 2f;
			if (_heart.getVehicle() == null)
				rate = 1f;
			
			UtilEnt.CreatureMoveFast(bottomEnt, loc, rate);
		}

		for (SnowmanMinion minion : _minions)
		{
			if (minion.Ent.getVehicle() != null)
				continue;
			
			if (minion.Ent.getPassenger() != null)
				continue;

			//Return to Stack
			if (minion.CanStack())
			{
				EntityCreature ec = ((CraftCreature)minion.Ent).getHandle();
				ec.getControllerMove().a(_heart.getLocation().getX(), minion.Ent.getLocation().getY(), _heart.getLocation().getZ(), 2f);
			}
			//Harass Players
			else
			{
				if ((minion.Target == null || !minion.Target.isValid() || !Host.IsAlive(minion.Target)) && !Host.GetPlayers(true).isEmpty())
				{
					List<Player> valid = new ArrayList<Player>();
					for (Player player : Host.GetPlayers(true))
						if (UtilMath.offset(player, minion.Ent) <= 25 && player.getLocation().getBlockY() >= minion.Ent.getLocation().getBlockY())
							valid.add(player);
					
					minion.Target = UtilAlg.Random(valid);
				}
				
				if (minion.Target == null)
					continue;

				//Move
				UtilEnt.CreatureMoveFast(minion.Ent, minion.Target.getLocation(), 1.6f);

				//Bump
				if (UtilMath.offset(minion.Ent, minion.Target) < 1.5)
				{
					if (!Recharge.Instance.usable(minion.Target, "Snowman Hit"))
						return;

					UtilAction.velocity(minion.Target, UtilAlg.getTrajectory(minion.Ent, minion.Target), 0.8, false, 0, 0.3, 1.2, true);
					Recharge.Instance.useForce(minion.Target, "Snowman Hit", 1000);

					//Damage Event
					Host.Manager.GetDamage().NewDamageEvent(minion.Target, minion.Ent, null, 
							DamageCause.ENTITY_ATTACK, 6, false, false, false,
							null, null);
				}
			}	
		}

		//Clean
		if (_heart != null && !_heart.isValid())
		{
			for (SnowmanMinion minion : _minions)
			{
				minion.Ent.getWorld().playEffect(minion.Ent.getLocation(), Effect.STEP_SOUND, 80);
				minion.Ent.remove();
			}

			_minions.clear();
		}
	}

	public void UpdateCombine()
	{
		Entity bottomEnt = _heart;
		while (bottomEnt.getVehicle() != null)
			bottomEnt = bottomEnt.getVehicle();
		
		for (SnowmanMinion minion : _minions)
		{
			if (bottomEnt.equals(minion.Ent))
				continue;
			
			if (minion.Ent.getVehicle() != null)
				continue;
			
			if (!minion.CanStack())
				continue;
			
			if (UtilMath.offset(minion.Ent, bottomEnt) < 2)
			{
				minion.Ent.setPassenger(bottomEnt);
				
				if (!_joke)
				{
					int size = 0;
					Entity ent = _heart;
					while (ent.getVehicle() != null)
					{
						ent = ent.getVehicle();
						size++;
					}
					
					if (size >= 8)
					{
						_joke = true;
						Host.SantaSay("Shoot the...", ChristmasAudio.P3_JOKE);
					}
				
				}
				
				return;
			}
		}
	}

	public void Damage(CustomDamageEvent event)
	{
		if (_heart == null)
			return;
		
		if (event.GetDamageeEntity().equals(_heart))
		{
			event.SetKnockback(false);
		}
		
		if (event.GetCause() != DamageCause.PROJECTILE)
			return;
		
		if (_heart.getVehicle() == null)
			return;
		
		//Stack Shatter
		if (event.GetDamageeEntity().equals(_heart))
		{
			event.SetCancelled("Ranged Damage");
			
			if (!_sword)
			{
				_sword = true;
				Host.SantaSay("Good! Now kill it with your swords!", ChristmasAudio.P3_USE_SWORD);
			}
				
			
			Entity cur = _heart;

			while (cur.getVehicle() != null)
			{
				Entity past = cur;

				cur = cur.getVehicle();
				cur.eject();

				past.setVelocity(new Vector((Math.random() - 0.5)*2, (Math.random())*1, (Math.random() - 0.5)*2));
			}

			for (SnowmanMinion minion : _minions)
				minion.StackDelay = System.currentTimeMillis();

			return;
		}
	}

	public boolean IsDead()
	{
		return !_heart.isValid();
	}

	public double GetHealth() 
	{
		return _heart.getHealth()/_heart.getMaxHealth();
	}
}
