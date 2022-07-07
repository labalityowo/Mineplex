package nautilus.game.pvp.worldevent.creature;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.projectile.IThrown;
import mineplex.core.projectile.ProjectileUser;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.updater.UpdateType;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilTime;
import nautilus.game.pvp.worldevent.EventBase;
import nautilus.game.pvp.worldevent.EventMobBoss;

public class BroodMother extends EventMobBoss implements IThrown
{
	private HashMap<Block, Long> _eggs = new HashMap<Block, Long>();
	private long _eggLast = System.currentTimeMillis();
	private int _eggSpawns = 0;

	private long _webLast = System.currentTimeMillis();
	private int _webSpawns = 0;
	private int _webMax = 80;
	
	private int _eggItems = 0;
	private int _eggItemMax = 20;

	private long _stateLast = System.currentTimeMillis();

	//States
	//0 Normal
	//1 Eggs
	//2 Webs

	public BroodMother(EventBase event, Location location) 
	{
		super(event, location, "Brood Mother", true, 800, EntityType.SPIDER);
		
		 _minionsMax = 80;
	}

	@EventHandler
	public void Heal(UpdateEvent event)
	{
		if (GetEntity() == null)
			return;

		if (event.getType() != UpdateType.SEC)
			return;

		ModifyHealth(1);
	}

	@Override
	public void Die()
	{
		Event.Manager.Blood().Effects(GetEntity().getEyeLocation(), 50, 0.8, 
				Sound.SPIDER_DEATH, 2f, 0.5f, Material.BONE, (byte)0, false);
		Loot();
		Remove();	
	}
	
	@Override
	public void Remove()
	{
		if (GetEntity() != null)
			GetEntity().remove();

		Event.CreatureDeregister(this);
		
		for (Block cur : _eggs.keySet())
			cur.setTypeId(0);
	}

	@Override
	public void Loot() 
	{
		Event.Manager.Loot().DropLoot(GetEntity().getEyeLocation(), 40, 40, 0.2f, 0.05f, 3d);
	}
	
	@Override
	public void DistanceAction() 
	{
		// TODO Auto-generated method stub
	}

	@EventHandler
	public void StateSwitch(UpdateEvent event)
	{
		if (_state != 0)
			return;

		if (event.getType() != UpdateType.SEC)
			return;

		if (!UtilTime.elapsed(_stateLast, 12000))
			return;
		
		if (Math.random() > 0.5 || _minions.size() + _eggs.size() < 6)
		{
			if (_minions.size() + _eggs.size() < 6 || UtilTime.elapsed(_eggLast, 30000))
			{
				_eggLast = System.currentTimeMillis();
				_state = 1;
				_eggSpawns = GetMinionsMax();
				((Creature)GetEntity()).setTarget(null);
			}
		}
		else
		{
			if (UtilTime.elapsed(_webLast, 30000))
			{
				_webLast = System.currentTimeMillis();
				_state = 2;
				_webSpawns = Scale(_webMax);
				((Creature)GetEntity()).setTarget(null);
			}
		}
	}
	
	private int Scale(int a)
	{
		return (int) (a * (0.1 + (0.9 - 0.9 * (GetHealthCur() / GetHealthMax()))));
	}

	private int GetMinionsMax() 
	{
		int max = Scale(_minionsMax);
		max -= _minions.size();

		return max;
	}

	@EventHandler
	public void WebSpawn(UpdateEvent event)
	{
		if (_state != 2)
			return;

		if (event.getType() != UpdateType.FASTEST)
			return;

		if (GetEntity() == null)
			return;

		if (_webSpawns <= 0)
		{
			_state = 0;
			_stateLast = System.currentTimeMillis();
			return;
		}

		_webSpawns--;

		//Item
		Item item = GetEntity().getWorld().dropItem(GetEntity().getLocation().add(0, 0.5, 0), 
				ItemStackFactory.Instance.CreateStack(Material.WEB));

		//Velocity
		UtilAction.velocity(item, new Vector(Math.random()-0.5, 0, Math.random()-0.5).normalize(), 
				Math.random() * 0.4 + 0.2, false, 0, Math.random() * 0.6 + 0.4, 10, false);

		//Sound
		item.getWorld().playSound(item.getLocation(), Sound.BREATH, 0.5f, 0.2f);

		//Thrown
		Event.Manager.Throw().AddThrow(item, GetEntity(), this, 
				-1, false, false, true, 
				null, 2f, 0.5f, 
				null, 0, UpdateType.TICK, 1.5d);
	}

	@EventHandler
	public void EggSpawn(UpdateEvent event)
	{
		if (_state != 1)
			return;

		if (event.getType() != UpdateType.FASTEST)
			return;

		if (GetEntity() == null)
			return;

		if (_eggSpawns <= 0)
		{
			_state = 0;
			_stateLast = System.currentTimeMillis();
			return;
		}
		
		if (_eggItems >= _eggItemMax)
			return;

		_eggSpawns--;

		//Item
		Item item = GetEntity().getWorld().dropItem(GetEntity().getLocation().add(0, 0.5, 0), 
				ItemStackFactory.Instance.CreateStack(Material.DRAGON_EGG));

		//Velocity
		UtilAction.velocity(item, new Vector(Math.random()-0.5, 0, Math.random()-0.5).normalize(), 
				Math.random() * 0.4 + 0.2, false, 0, Math.random() * 0.6 + 0.4, 10, false);

		//Sound
		item.getWorld().playSound(item.getLocation(), Sound.CHICKEN_EGG_POP, 1f, 1f);

		//Thrown
		Event.Manager.Throw().AddThrow(item, GetEntity(), this, 
				-1, false, false, true, 
				null, 2f, 0.5f, 
				null, 0, UpdateType.TICK, 1.5d);
		
		_eggItems++;
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data) 
	{
		//Null
	}

	@Override
	public void Idle(ProjectileUser data) 
	{
		BlockForm(data);
	}

	@Override
	public void Expire(ProjectileUser data) 
	{
		BlockForm(data);
	}

	public void BlockForm(ProjectileUser data)
	{
		Material mat = ((Item)data.GetThrown()).getItemStack().getType();

		if (mat == Material.DRAGON_EGG)
		{
			Block egg = data.GetThrown().getLocation().getBlock();

			if (egg.getRelative(BlockFace.DOWN).getType() != Material.DRAGON_EGG &&
				egg.getRelative(BlockFace.DOWN).getType() != Material.WEB)
			{
				egg.setType(Material.DRAGON_EGG);
				_eggs.put(egg, System.currentTimeMillis());
			}
			
			_eggItems--;
		}
		else if (mat == Material.WEB)
		{
			Block web = data.GetThrown().getLocation().getBlock();
			
			if (web.getType() != Material.WATER && web.getType() != Material.STATIONARY_WATER)
				Event.Manager.BlockRestore().Add(web, 30, (byte)0, Scale(40000));
		}

		data.GetThrown().remove();
	}

	@EventHandler
	public void EggHatch(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		if (_eggs.isEmpty())
			return;

		HashSet<Block> hatch = new HashSet<Block>();

		for (Block block : _eggs.keySet())
			if (Math.random() > 0.98)
				if (UtilTime.elapsed(_eggs.get(block), 12000))
					hatch.add(block);

		for (Block block : hatch)
		{
			_eggs.remove(block);
			block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getTypeId());
			block.setTypeId(0);
			Event.CreatureRegister(new Broodling(Event, block.getLocation().add(0.5, 0.5, 0.5), this));
		}
	}

	@EventHandler
	public void EggCrush(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Player cur : GetEntity().getWorld().getPlayers())
		{
			Block below = cur.getLocation().getBlock().getRelative(BlockFace.DOWN);
			if (below.getType() != Material.DRAGON_EGG)
				continue;

			if (!_eggs.containsKey(below))
				continue;

			_eggs.remove(below);
			below.setTypeId(0);
			below.getWorld().playEffect(below.getLocation(), Effect.STEP_SOUND, 122);

			UtilAction.velocity(cur, cur.getLocation().getDirection(), 0.3, true, 0.3, 0, 10, true);
		}
	}

	@EventHandler
	public void EggHit(PlayerInteractEvent event)
	{
		if (event.getClickedBlock() == null)
			return;

		if (!_eggs.containsKey(event.getClickedBlock()))
			return;

		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void TargetCancel(EntityTargetEvent event)
	{	
		if (!event.getEntity().equals(GetEntity()))
			return;

		if (_state == 0 && (GetHealthCur() / GetHealthMax()) < 0.5)
			return;

		event.setCancelled(true);
	}	
}
