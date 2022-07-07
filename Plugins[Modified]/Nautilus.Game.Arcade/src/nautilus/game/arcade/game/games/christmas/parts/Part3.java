package nautilus.game.arcade.game.games.christmas.parts;

import java.util.ArrayList;
import java.util.Iterator;

import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilServer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.christmas.Christmas;
import nautilus.game.arcade.game.games.christmas.ChristmasAudio;
import nautilus.game.arcade.game.games.christmas.content.IceMaze;
import nautilus.game.arcade.game.games.christmas.content.SnowmanBoss;
import nautilus.game.arcade.game.games.christmas.content.SnowmanMaze;
import nautilus.game.arcade.game.games.christmas.content.SnowmanWaveA;
import nautilus.game.arcade.game.games.christmas.content.SnowmanWaveB;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Snowman;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class Part3 extends Part
{
	private ArrayList<Location> _snowmenMaze;
	private ArrayList<Location> _snowmenMazeArea;
	
	private ArrayList<Location> _iceMaze;
	private ArrayList<Location> _iceMazeExits;
	
	private ArrayList<Location> _boss;
	private ArrayList<Location> _bridge;
	
	private SnowmanBoss _snowmanBoss;
	
	private IceMaze _maze;
	private SnowmanMaze _snowmen;
	
	//private SnowmanWaveA _waveA;
	//private SnowmanWaveB _waveB;
	
	private boolean _a = false; 
	
	public Part3(Christmas host, Location sleigh, Location[] presents, 
			ArrayList<Location> snowmenA, ArrayList<Location> snowmenAreaA,
			ArrayList<Location> maze, ArrayList<Location> mazeExits,
			ArrayList<Location> boss, ArrayList<Location> bridge) 
	{
		super(host, sleigh, presents);
		
		_snowmenMaze = snowmenA;
		_snowmenMazeArea = snowmenAreaA;
		
		_iceMaze = maze;
		_iceMazeExits = mazeExits;
		
		_boss = boss;
		_bridge = bridge;
	}

	@Override
	public void Activate()
	{
		
		_snowmen = new SnowmanMaze(Host, _snowmenMaze, _snowmenMazeArea, GetPresents());
		_maze = new IceMaze(Host, _iceMaze, _iceMazeExits, GetPresents());
		
		//_waveA = new SnowmanWaveA(Host, _snowmenMaze, GetSleighWaypoint(), GetPresents());
		//_waveB = new SnowmanWaveB(Host, _maze, GetSleighWaypoint(), GetPresents());
	}
	
	private void UpdateBridge() 
	{
		if (_bridge.isEmpty())
			return;
		
		int lowest = 1000;
		
		for (Location loc : _bridge)
			if (loc.getBlockZ() < lowest)
				lowest = loc.getBlockZ();
		
		Iterator<Location> gateIterator = _bridge.iterator();
		
		boolean sound = true;
		
		while (gateIterator.hasNext())
		{
			Location loc = gateIterator.next();
			
			if (loc.getBlockZ() == lowest)
			{
				byte color = 14;
				if (lowest % 6 == 1)	color = 1;
				else if (lowest % 6 == 2)	color = 4;
				else if (lowest % 6 == 3)	color = 5;
				else if (lowest % 6 == 4)	color = 3;
				else if (lowest % 6 == 5)	color = 2;
				
				loc.getBlock().setTypeIdAndData(35, color, false);
				loc.getWorld().playEffect(loc, Effect.STEP_SOUND, 35);
				gateIterator.remove();
				
				if (sound)
				{
					loc.getWorld().playSound(loc, Sound.ZOMBIE_UNFECT, 3f, 1f);
					sound = false;
				}
			}
		}
	}
	
	@EventHandler
	public void Update(UpdateEvent event)
	{
		if (event.getType() == UpdateType.FASTER)
			UpdateBridge();
		
		if (event.getType() == UpdateType.TICK)
			if (_snowmanBoss != null)
				_snowmanBoss.UpdateMove();
		
		if (event.getType() == UpdateType.FASTEST)
			if (_snowmanBoss != null)
				_snowmanBoss.UpdateCombine();
		
		if (event.getType() == UpdateType.TICK)
			if (_snowmanBoss != null)
				_snowmanBoss.UpdateSnowball();
		
		if (event.getType() == UpdateType.TICK)
			if (_snowmanBoss != null)
			{
				SetObjectiveText("Kill the Snow Monster", _snowmanBoss.GetHealth());
			}
				
		if (event.getType() == UpdateType.TICK)
			if (_snowmen != null)
				_snowmen.Update();
		
		if (event.getType() == UpdateType.TICK)
			if (_maze != null)
				_maze.Update();
		
		if (event.getType() == UpdateType.FAST)
			if (_snowmanBoss == null)
				if (HasPresents())
				{
					_snowmanBoss = new SnowmanBoss(Host, _boss.get(0));
					Host.SantaSay("WATCH OUT! It's some kind of Snow Monster!", ChristmasAudio.P3_BOSS_INTRO);	
					
					UtilServer.getServer().getScheduler().scheduleSyncDelayedTask(Host.Manager.getPlugin(), new Runnable()
					{
						public void run()
						{
							Host.SantaSay("Shoot the Iron Golem with your bow!", ChristmasAudio.P3_SHOOT_HEART);	
						}
					}, 80);

				}	
	}
	
	@EventHandler
	public void Damage(CustomDamageEvent event)
	{
		if (_snowmanBoss != null)
			_snowmanBoss.Damage(event);
	}
	
	@EventHandler
	public void UpdateIntro(UpdateEvent event) 
	{
		if (event.getType() != UpdateType.FAST)
			return;

		if (!_a)
		{
			if (UtilMath.offset(Host.GetSleigh().GetLocation(), GetSleighWaypoint()) > 10)
				return;
			
			_a = true;
			
			Host.SantaSay("Collect those presents, I'll try to open the gate!", ChristmasAudio.P3_GET_PRESENTS);	
			SetObjectivePresents();
		}	
	}

	@Override
	public boolean CanFinish() 
	{
		return (_snowmanBoss != null && _snowmanBoss.IsDead() && HasPresents());
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void DamageCancel(CustomDamageEvent event)
	{
		if (!(event.GetDamageeEntity() instanceof Snowman))
			return;
		
		if (_boss == null || event.GetDamageeEntity().getPassenger() == null)
		{
			event.SetCancelled("Snowman Wave Cancel");	
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void SnowballDamage(CustomDamageEvent event)
	{
		if (event.GetProjectile() == null)
			return;
		
		if (!(event.GetProjectile() instanceof Snowball))
			return;
		
		if (event.GetDamageePlayer() != null)
			event.AddMod("Christmas Part 3", "Snowball", 2, false);
		else
			event.SetCancelled("Snowball vs Mobs");
	}
}
