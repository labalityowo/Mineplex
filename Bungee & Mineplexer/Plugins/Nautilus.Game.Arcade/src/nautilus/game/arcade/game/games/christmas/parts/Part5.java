package nautilus.game.arcade.game.games.christmas.parts;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowman;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.christmas.Christmas;
import nautilus.game.arcade.game.games.christmas.ChristmasAudio;
import nautilus.game.arcade.game.games.christmas.content.BossFloor;
import nautilus.game.arcade.game.games.christmas.content.BossMobs;
import nautilus.game.arcade.game.games.christmas.content.BossSnowmanPattern;
import nautilus.game.arcade.game.games.christmas.content.PumpkinKing;

public class Part5 extends Part
{
	private ArrayList<Location> _spawn;
	private ArrayList<Location> _floor;
	private ArrayList<Location> _playerSpawns;
	private ArrayList<Location> _hurt;
	private ArrayList<Location> _mobs;
	private ArrayList<Location> _snowmenA;
	private ArrayList<Location> _snowmenB;
	private ArrayList<Location> _glass;

	private PumpkinKing _boss;
	private BossSnowmanPattern _bossSnowmen;
	private BossFloor _bossFloor;
	private BossMobs _bossMob;
	
	private long _bossDamageDelay = 0;

	private int _state = 0;
	private long _stateTime = 0;
	private int _stateHealthMax = 6;
	private int _stateHealth = 6;
	
	private boolean _a = false;
	private boolean _b = false;
	private boolean _c = false;
	private boolean _d = false;
	private boolean _e = false;
	private boolean _f = false;
	private boolean _g = false;
	private boolean _h = false;
	
	private long _dialogueDelay = 0;
	private long _delayTime = 4000;


	public Part5(Christmas host, Location sleigh, Location[] presents, 
			ArrayList<Location> snowmenA, ArrayList<Location> snowmenB, ArrayList<Location> mobs, 
			ArrayList<Location> floor, ArrayList<Location> playerSpawns, ArrayList<Location> hurt, ArrayList<Location> spawn, ArrayList<Location> glass) 
	{
		super(host, sleigh, presents);

		_floor = floor;
		_playerSpawns = playerSpawns;
		_spawn = spawn;
		_mobs = mobs;
		_snowmenA = snowmenA;
		_snowmenB = snowmenB;
		_glass = glass;
		_hurt = hurt;
		
		for (Location loc : _glass)
			loc.getBlock().setType(Material.GLASS);
		
		for (Location loc : _spawn)
			loc.getBlock().setType(Material.AIR);
		
		for (Location loc : _playerSpawns)
			loc.getBlock().setType(Material.AIR);

		for (Location loc : hurt)
			loc.getBlock().setType(Material.AIR);

		for (Location loc : _mobs)
			loc.getBlock().setType(Material.AIR);

		for (Location loc : _snowmenA)
			loc.getBlock().setType(Material.AIR);

		for (Location loc : _snowmenB)
			loc.getBlock().setType(Material.AIR);
	}

	@Override
	public void Activate() 
	{
	        Host.ReachedEnding = true;
		_bossSnowmen = new BossSnowmanPattern(this, _snowmenA, _snowmenB, GetSleighWaypoint());
		_bossFloor = new BossFloor(this, _floor);
		_bossMob = new BossMobs(this, _mobs);
	}

	@Override
	public boolean CanFinish()
	{
		return (_boss != null && _boss.IsDead());
	}

	public int GetState() 
	{
		return _state;
	}
	
	public long GetStateTime()
	{
		return _stateTime;
	}
	
	@EventHandler
	public void UpdateIntro(UpdateEvent event) 
	{
		if (event.getType() != UpdateType.FAST)
			return;

		if (!_a)
		{
			if (UtilMath.offset(Host.GetSleigh().GetLocation(), GetSleighWaypoint()) > 1)
				return;
			
			_a = true;
			_dialogueDelay = System.currentTimeMillis();
			Host.SantaSay("WHAT IS THIS?! Whose castle is this?!", ChristmasAudio.BANTER_A);	
		}	
		else if (_a && !_b && UtilTime.elapsed(_dialogueDelay, _delayTime))
		{
			_b = true;
			_dialogueDelay = System.currentTimeMillis();
			Host.BossSay("????????", "I will destroy Christmas! Not even your pathetic friends can save you now!", ChristmasAudio.BANTER_B);	
		}
		else if (_b && !_c && UtilTime.elapsed(_dialogueDelay, _delayTime + 2400))
		{
			_c = true;
			_dialogueDelay = System.currentTimeMillis();
			Host.SantaSay("WHO ARE YOU?!", ChristmasAudio.BANTER_C);	
		}
		else if (_c && !_d && UtilTime.elapsed(_dialogueDelay, _delayTime - 1800))
		{
			_d = true;
			_dialogueDelay = System.currentTimeMillis();
			
			Host.BossSay("????????", "It is me... THE PUMPKIN KING!", ChristmasAudio.BANTER_D);	
			
			//Start Battle
			_boss = new PumpkinKing(this, _spawn.get(0), _floor);
		}
		else if (_d && !_e && UtilTime.elapsed(_dialogueDelay, _delayTime + 1000))
		{
			_e = true;
			_dialogueDelay = System.currentTimeMillis();
			Host.BossSay("Pumpkin King", "Revenge will be mine! You will all die!", ChristmasAudio.BANTER_E);	
		}
		else if (_e && !_f && UtilTime.elapsed(_dialogueDelay, _delayTime + 1000))
		{
			_f = true;
			_dialogueDelay = System.currentTimeMillis();
			Host.SantaSay("My friends beat you before, they'll do it again!", ChristmasAudio.BANTER_F);	
		}
		else if (_f && !_g && UtilTime.elapsed(_dialogueDelay, _delayTime + 500))
		{
			_g = true;
			_dialogueDelay = System.currentTimeMillis();
			
			Host.SantaSay("Prepare for battle!", ChristmasAudio.BANTER_G);	
			
			//Teleport
			for (int i=0 ; i<Host.GetPlayers(true).size() ; i++)
			{
				final Player player = Host.GetPlayers(true).get(i);
				final int index = i;
				
				Bukkit.getServer().getScheduler().runTaskLater(Host.Manager.getPlugin(), new Runnable()
				{
					@Override
					public void run()
					{
						player.leaveVehicle();
						
						player.teleport(_playerSpawns.get(index%_playerSpawns.size()));
						player.playSound(player.getLocation(), Sound.ZOMBIE_UNFECT, 2f, 1f);
					}
				}, i*2);
			}
		}
		else if (_g && !_h && UtilTime.elapsed(_dialogueDelay, _delayTime - 1000))
		{
			_h = true;
			_dialogueDelay = System.currentTimeMillis();
			Host.BossSay("Pumpkin King", "More like... PREPARE TO DIE!", ChristmasAudio.BANTER_H);	
		}
	}

	@EventHandler
	public void ElementUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		if (!_h)
			return;

		if (_bossSnowmen != null)
			_bossSnowmen.Update();

		if (_bossFloor != null)
			_bossFloor.Update();
		
		if (_bossMob != null)
			_bossMob.Update();
	}

	public void NextState()
	{
		_state++;
		_stateTime = System.currentTimeMillis();
		
		_stateHealth = _stateHealthMax;
		
		_bossSnowmen.SetActive(false, 0);
		_bossFloor.SetActive(false, 0);
		_bossMob.SetActive(false, 0);
		
		if (_state > 7)
		{
			_boss.Die();
			
			_bossSnowmen.Clean();
			_bossFloor.Restore();
			_bossMob.Clean();
			
			for (Location loc : _glass)
			{
				loc.getWorld().playEffect(loc, Effect.STEP_SOUND, 20);
				loc.getBlock().setType(Material.AIR);
			}
		}
			
	}
	
	@EventHandler
	public void HurtPlayer(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;
		
		for (Player player : Host.GetPlayers(true))
		{
			for (Location loc : _hurt)
			{
				if (UtilMath.offset(player.getLocation(), loc) < 1.5)
				{
					player.damage(2);
					UtilAction.velocity(player, UtilAlg.getTrajectory2d(player.getLocation(), _spawn.get(0)), 1, true, 0.6, 0, 1, true);
				}
			}
		}
	}
	
	@EventHandler
	public void StateUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		if (_boss == null || _boss.IsDead())
			return;
		
		if (!_h)
			return;
		
		//Health
		SetObjectiveText("Defeat the Pumpkin King!", 
				((8 * _stateHealthMax) - (_state * _stateHealthMax) - (_stateHealthMax - _stateHealth))/(8d * _stateHealthMax));

		if (_bossFloor.ShouldBossMove())
		{
			_boss.MoveUpdate();
			_boss.TNTUpdate();
		}
		else
		{
			_boss.StayIdle();
		}
		
		//TNT Only
		if (_state == 0)
		{
		
		}	
		//Floor (Easy)
		else if (_state == 1)
		{
			_bossFloor.SetActive(true, 0);
		}
		//Mobs (Easy)
		else if (_state == 2)
		{
			_bossMob.SetActive(true, 0);
		}
		//Snowmen (Easy) 
		else if (_state == 3)
		{
			_bossSnowmen.SetActive(true, 0);
		}
		//Floor (Medium)
		else if (_state == 4)
		{
			_bossFloor.SetActive(true, 1);
		}
		//Mobs (Medium)
		else if (_state == 5)
		{
			_bossMob.SetActive(true, 1);
		}
		//Snowmen (Medium)
		else if (_state == 6)
		{
			_bossSnowmen.SetActive(true, 1);
		}
		//Floor + Mobs + Snowmen (Easy)
		else if (_state == 7)
		{
			_bossSnowmen.SetActive(true, 1);
			_bossMob.SetActive(true, 1);
			_bossFloor.SetActive(true, 1);
		}
	}
	
	@EventHandler
	public void Skip(PlayerCommandPreprocessEvent event)
	{
		if (event.getMessage().equals("/boss"))
			if (event.getPlayer().isOp())
			{
				event.setCancelled(true);

				NextState();
			}
				
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void TNTExplosion(EntityExplodeEvent event)
	{
		/*
		for (Block block : event.blockList())
		{
			if (block.getType() == Material.WOOL)
			{
				Host.Manager.GetBlockRestore().Add(block, 0, (byte)0, (long) (1500 + Math.random()*1000));
			}
		}
		*/
		
		event.blockList().clear();
	}
	
	@EventHandler
	public void Damage(CustomDamageEvent event)
	{
		if (event.isCancelled())
			return;
		
		if (_boss == null)
			return;
		
		if (!_boss.GetEntity().equals(event.GetDamageeEntity()))
			return;
		
		event.SetCancelled("Boss Damage");
		
		//Knockback
		Player damager = event.GetDamagerPlayer(false);
		if (damager == null)
			return;
		
		if (event.GetCause() == DamageCause.ENTITY_ATTACK)
			UtilAction.velocity(damager, UtilAlg.getTrajectory(event.GetDamageeEntity(), damager), 1, true, 0.6, 0, 1, true);
		
		if (!Host.IsAlive(damager))
			return;
		
		if (!UtilTime.elapsed(_bossDamageDelay, 400))
			return;
		
		_bossDamageDelay = System.currentTimeMillis();
		
		event.GetDamageeEntity().playEffect(EntityEffect.HURT);
		
		_stateHealth--;
		
		//Damage Sound
		_boss.GetEntity().getWorld().playSound(_boss.GetEntity().getLocation(), Sound.ENDERDRAGON_GROWL, 0.5f, 2f);
		
		
		if (_stateHealth <= 0)
		{
			NextState();
			
			_boss.GetEntity().getWorld().playSound(_boss.GetEntity().getLocation(), Sound.ENDERDRAGON_GROWL, 1f, 0.5f);
		}
	}

	public PumpkinKing GetBoss() 
	{
		return _boss;
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void DamageCancel(CustomDamageEvent event)
	{
		if (!(event.GetDamageeEntity() instanceof Snowman))
			return;
	
		if (event.GetCause() == DamageCause.LAVA)
			return;
		
		event.SetCancelled("Snowman Damage");
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void DamageCancel(BlockBurnEvent event)
	{
		event.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void DamageCancel(BlockIgniteEvent event)
	{
		event.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void IgniteCancel(EntityCombustEvent event)
	{
		event.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void FireDamageIncrease(CustomDamageEvent event)
	{
		if (!(event.GetDamageeEntity() instanceof Player))
			return;
	
		if (event.GetCause() != DamageCause.FIRE)
			return;
		
		event.AddMod("Part 5", "Fire Damage", 4, false);
	}
}
