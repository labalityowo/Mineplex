package nautilus.game.arcade.game.games.halloween2016.creatures;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Giant;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilText;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.halloween2016.Halloween2016;

public class MobGiant extends CryptBreaker<Giant>
{
	
	//Instant destroy crypt
	public static final int CRYPT_DAMAGE = 100000;
	public static final int CRYPT_DAMAGE_COOLDOWN = 1;
	public static final float SPEED = 0.5f;
	
	private static double DAMAGE = 9000;
	private static double KNOCKBACK = 8;
	private static double HEALTH = 200;
	
	private static float EXTRA_EXPLOSION_DAMAGE = 10;
	
	private boolean _cryptDestroyed = false;
	
	private Zombie _pathDummy;
	
	private Player _target;

	public MobGiant(Halloween2016 game, Location loc)
	{
		super(game, null, Giant.class, loc, CRYPT_DAMAGE, CRYPT_DAMAGE_COOLDOWN, SPEED);
		
		_customCryptRange = 3;
	}
	
	@Override
	public void SpawnCustom(Giant ent)
	{
		_pathDummy = ent.getWorld().spawn(ent.getLocation(), Zombie.class);
		
		super.Host.Manager.GetCondition().Factory().Invisible("Cloak", _pathDummy, _pathDummy, 999999, 0, false, false, false);
		_pathDummy.setCustomNameVisible(true);
		
		ent.setMaxHealth(HEALTH);
		ent.setHealth(ent.getMaxHealth());
		
		UtilEnt.setBoundingBox(_pathDummy, 0, 0);
		UtilEnt.vegetate(_pathDummy, true);
		UtilEnt.setStepHeight(_pathDummy, 1);
		
		//Prevent other mobs from pushing the giant
		UtilEnt.ghost(_pathDummy, true, true);
		
		_pathDummy.setRemoveWhenFarAway(false);
		UtilEnt.setTickWhenFarAway(_pathDummy, true);
		
		addEntityPart(_pathDummy);
	}
	
	@Override
	public void Update(UpdateEvent event)
	{
		if(event.getType() == UpdateType.TICK)
		{
			move();
			updateHealthBar();
		}
		
		if(!_cryptDestroyed && _crypt.isDestroyed())
		{
			SetTarget(null);
			_cryptDestroyed = true;
		}
		
	}
	
	private Player getRandomPlayer()
	{
		List<Player> players = Host.GetPlayers(true);
		return players.get(UtilMath.r(players.size()));
	}
	
	private void move()
	{
		if(_target != null)
		{
			if(!_target.isOnline() || !_target.isValid() || !Host.GetPlayers(true).contains(_target))
			{
				_target = null;
			}
		}
		
		Location target = GetTarget();
		if(target == null && _target == null)
		{
			if(_crypt.isDestroyed())
			{
				_target = getRandomPlayer();
			}
			else
			{
				target = getClosestDoor();
				
			}
			SetTarget(target);
		}
		if(_target != null)
		{
			target = _target.getLocation();
		}
		UtilEnt.CreatureMove(_pathDummy, target, SPEED);
		GetEntity().teleport(_pathDummy);
		
		if(!_crypt.isDestroyed())
		{
			if(getClosestDoor().distanceSquared(GetEntity().getLocation()) <= _customCryptRange*_customCryptRange)
			{
				if(!_crypt.tryDamage(GetEntity(), _cryptDamage, _cryptDamageRate)) return;
				
				GetEntity().getWorld().playSound(GetEntity().getLocation(), Sound.ZOMBIE_WOODBREAK, 1, 0.7f + UtilMath.random.nextFloat() * 0.5f);
				swingArms();
			}
		}
		
		boolean hit = false;
		
		for(Player p : getInsideBoundingBox())
		{
			Host.getArcadeManager().GetDamage().NewDamageEvent(p, GetEntity(), null, GetEntity().getLocation(), DamageCause.ENTITY_ATTACK, DAMAGE, true, false, false, "Giant", "Giant Damage", false);
			hit = true;
		}
		if(hit)
		{
			swingArms();
			GetEntity().getWorld().playSound(GetEntity().getLocation(), Sound.ZOMBIE_IDLE, 2, 0.5f);
		}
	}
	
	@Override
	public void Move()
	{}
	
	@Override
	public void Damage(CustomDamageEvent event)
	{
		if(_pathDummy.equals(event.GetDamageeEntity()))
		{
			event.SetCancelled("Invalid Entity");
			return;
		}
		if(event.GetDamageeEntity().equals(GetEntity()))
		{
			event.SetKnockback(false);
			
			if(event.GetCause() == DamageCause.SUFFOCATION)
			{
				event.SetCancelled("Invalid Giant Damage");
				return;
			}
			else if(event.GetCause() == DamageCause.ENTITY_EXPLOSION)
			{
				event.AddMod("Explosion", EXTRA_EXPLOSION_DAMAGE);
			}
		}
		
		if(!GetEntity().equals(event.GetDamagerEntity(false))) return;
		
		event.SetKnockback(true);
		event.AddKnockback("Giant Knockback", KNOCKBACK);
	}
	
	public void updateHealthBar()
	{
		_pathDummy.setCustomName(UtilText.getProgress(C.cGreen, GetEntity().getHealth()/GetEntity().getMaxHealth(), C.cGray, false));
	}
	

}
