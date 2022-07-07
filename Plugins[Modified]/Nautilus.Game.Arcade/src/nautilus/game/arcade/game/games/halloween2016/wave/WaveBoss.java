package nautilus.game.arcade.game.games.halloween2016.wave;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.game.games.halloween.creatures.CreatureBase;
import nautilus.game.arcade.game.games.halloween2016.Halloween2016;
import nautilus.game.arcade.game.games.halloween2016.creatures.MobPrinceGuard;
import nautilus.game.arcade.game.games.halloween2016.creatures.MobPumpkinPrince;

public class WaveBoss extends WaveBase implements Listener
{
	private MobPumpkinPrince _prince;
	private Halloween2016 Host;
	
	private Skeleton _pumpkinKing;
	
	private List<MobPrinceGuard> _guards = new ArrayList<>();
	private int _guardsCount = 4;
	private int _guardsCountQueue = _guardsCount;
	private double _nextPrinceHealthStageForNewGuards;
	
	private int _tick = 0;
	
	private Fireball _fireball;
	
	private final static float KING_SPEED = 1.7f;
	private final static float PRINCE_SPEED = 1.3f;
	
	private Location _princeTarget;

	public WaveBoss(Halloween2016 host)
	{
		super(host, "Boss Fight", 0, host.getMobSpawns(), null);
		Host = host;
	}

	@Override
	public void Spawn(int tick)
	{
		tick = _tick;
		_tick++;
		if(tick == 0)
		{
			UtilServer.RegisterEvents(this);
			
			for(Player p : Host.GetPlayers(true))
			{
				p.teleport(Host.getCryptView());
			}
			Host.lockAllPlayers(Host.getCryptView());
			
			for(CreatureBase<?> c : Host.getMobs())
			{
				c.remove();
			}
		}
		else if(tick == 4)
		{
			for(CreatureBase<?> c : Host.getMobs())
			{
				c.remove();
			}
			for(Entity e : Host.WorldData.World.getEntities())
			{
				if((e instanceof LivingEntity) && !(e instanceof Player))
				{
					e.remove();
				}
			}
		}
		else if(tick == 5)
		{
			
			_princeTarget = Host.getPrinceTargetInfrontOfCrypt();
			
			_prince = new MobPumpkinPrince(Host, Host.getPrinceSpawn());
			_prince.setAI(false);
			Host.AddCreature(_prince);
			
			_nextPrinceHealthStageForNewGuards = _prince.GetEntity().getHealth()-200;
		}
		else if(tick < 20 * 5)
		{
			UtilEnt.CreatureMove(_prince.getHorse(), _princeTarget, PRINCE_SPEED);
		}
		else if(tick == 20 * 6)
		{
			Vector diff = Host.getInfrontOfDoorTargets().get(1).clone().add(0, 1, 0).subtract(_prince.GetEntity().getEyeLocation()).toVector();
			UtilEnt.CreatureLook(_prince.GetEntity(), UtilAlg.GetPitch(diff), UtilAlg.GetYaw(diff));
			_fireball = _prince.GetEntity().launchProjectile(Fireball.class);
		}
		else if(tick == 20 * 11)
		{
			Host.Announce("", false);
			Host.Announce("", false);
			say("Pumpkin King", "I am free! Free at last!");
		}
		if(tick == 20 * 12)
		{
			say("Pumpkin King", "Protect me my son!");
		}
		if(tick > 20 * 13 && tick < 20 * 18)
		{
			UtilEnt.CreatureMove(_pumpkinKing, Host.getGiantSpawn(), KING_SPEED);
			
		}
		else if(tick == 20 * 18)
		{
			_pumpkinKing.remove();

			Host.unlockAllPlayers();
			_prince.setAI(true);
			for(MobPrinceGuard guard : _guards)
			{
				guard.setAI(true);
			}
			
			Host.Objective = "Eliminate the Guards";
			Host.Announce(C.cGreen + C.Bold + "Kill the Prince's Guards first!", false);
		}
		
		
		
		
		
		
		
		
		if(tick > 20 * 18){
			boolean empt = _guards.isEmpty();
			for(Iterator<MobPrinceGuard> it = _guards.iterator(); it.hasNext();)
			{
				MobPrinceGuard g = it.next();
				if(!g.GetEntity().isValid()) it.remove();
			}
			if(empt != _guards.isEmpty() && !empt)
			{
				Host.Objective = "Defeat the Pumpkin Prince";
				Host.Announce(C.cGreen + C.Bold + "All the Pumpking Prince's guards are dead!", false);
				Host.Announce(C.cGreen + C.Bold + "Kill him while he is unprotected!", true);
			}
			
			_prince.setInvulnerable(!_guards.isEmpty());
			
			if(_prince.GetEntity().getHealth() < _nextPrinceHealthStageForNewGuards)
			{
				Host.Objective = "Eliminate the Guards";
				Host.Announce(C.cGreen + C.Bold + "The Pumpkin Prince's guards are back to protect him", false);
				Host.Announce(C.cGreen + C.Bold + "Kill them first before attacking the Prince!", true);
				
				
				_prince.setInvulnerable(true);
				_nextPrinceHealthStageForNewGuards -= 200;
				_guardsCountQueue = _guardsCount;
			}
			
			if(tick % 20 * 20 == 0)
			{
				if(_guardsCountQueue > 0)
				{
					MobPrinceGuard guard = new MobPrinceGuard(Host, Host.getGiantSpawn(), _prince);
					_guards.add(guard);
					Host.AddCreature(guard);
					_guardsCountQueue--;
				}
			}
		}	
	}
	
	public void say(String role, String msg)
	{
		Host.Announce("\n" + C.cGold + C.Bold + role + ": " + C.cGray + msg + "\n");
		UtilTextMiddle.display(C.cGold + msg, null, 0, 40, 0);
	}
	
	@Override
	public boolean CanEnd()
	{
		boolean r = _prince != null && _prince.isDead();
		if(r)
		{
			end();
		}
		return r;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onExplode(EntityExplodeEvent event)
	{
		if(event.getEntity().equals(_fireball))
		{
			event.setCancelled(true);
			event.blockList().clear();
			Host.getCrypt().setHealth(0);
			
			
			Host.CreatureAllowOverride = true;
			_pumpkinKing = GetSpawn().getWorld().spawn(Host.getInfrontOfCrypt(), Skeleton.class);
			Host.CreatureAllowOverride = false;
			
			_pumpkinKing.setSkeletonType(SkeletonType.WITHER);
			_pumpkinKing.getEquipment().setHelmet(new ItemStack(Material.PUMPKIN));
			
			_pumpkinKing.setCustomName(C.cYellow + C.Bold + "Pumpking King");
			_pumpkinKing.setCustomNameVisible(true);
			
			UtilEnt.vegetate(_pumpkinKing);
			
			_pumpkinKing.getWorld().strikeLightningEffect(_pumpkinKing.getLocation());
		}
	}
	
	public void end()
	{
		UtilServer.Unregister(this);
		
		Host.Announce("\n" + C.cYellow + C.Bold + "The Pumpkin Prince has been defeated!\nThe World is safe... for tonight.\n");
	}
	
	@EventHandler
	public void onDamage(CustomDamageEvent event)
	{ 
		if(event.GetDamageeEntity().equals(_pumpkinKing))
		{
			event.SetCancelled("Invincible Statue");
		}
	}

}
