package mineplex.minecraft.game.classcombat.Skill.Brute;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilGear;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.projectile.IThrown;
import mineplex.core.projectile.ProjectileUser;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.classcombat.Skill.SkillCharge;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;
import mineplex.minecraft.game.classcombat.Skill.event.BlockTossExpireEvent;
import mineplex.minecraft.game.classcombat.Skill.event.BlockTossLandEvent;
import mineplex.minecraft.game.classcombat.Skill.event.SkillEvent;
import mineplex.minecraft.game.classcombat.Skill.event.SkillTriggerEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class BlockToss extends SkillCharge implements IThrown
{
	private HashMap<Player, FallingBlock> _holding = new HashMap<Player, FallingBlock>();
	private HashMap<FallingBlock, Player> _falling = new HashMap<FallingBlock, Player>();

	private Material[] _blacklist = new Material[]
			{
				Material.TNT,
				Material.IRON_DOOR,
				Material.IRON_DOOR_BLOCK,
				Material.WOOD_DOOR,
				Material.WOODEN_DOOR,
				Material.ENCHANTMENT_TABLE,
				Material.CHEST,
				Material.FURNACE,
				Material.BURNING_FURNACE,
				Material.WORKBENCH,
				Material.WATER,
				Material.STATIONARY_WATER,
				Material.LAVA,
				Material.STATIONARY_LAVA,
				Material.STONE_PLATE,
				Material.WOOD_PLATE,
				Material.GOLD_PLATE,
				Material.IRON_PLATE,
				Material.STONE_BUTTON,
				Material.WOOD_BUTTON,
				Material.LEVER,
				Material.BARRIER,
			};

	public BlockToss(SkillFactory skills, String name, ClassType classType, SkillType skillType, int cost, int levels) 
	{
		super(skills, name, classType, skillType, cost, levels,
				0.01f, 0.005f);

		SetDesc(new String[] 
				{
				"Hold Block to pick up a block,",
				"Release Block to throw it,",
				"dealing up to #6#1 damage.",
				"",
				GetChargeString(),
				});
	}
	
	@Override
	public String GetRechargeString()
	{
		return "Recharge: " + "1.5 Seconds";
	}

	@EventHandler
	public void Grab(PlayerInteractEvent event)
	{	 	
		Player player = event.getPlayer();

		if (!UtilEvent.isAction(event, ActionType.R_BLOCK))
			return;

		if (!UtilGear.isSword(player.getItemInHand()))
			return;
		
		//Check Allowed
		SkillTriggerEvent trigger = new SkillTriggerEvent(player, GetName(), GetClassType());
		Bukkit.getServer().getPluginManager().callEvent(trigger);
		
		if (trigger.IsCancelled())
			return;
		
		if (_holding.containsKey(player))
			return;

		//Level
		int level = getLevel(player);
		if (level == 0)		return;
		
		//Recharge
		if (!Recharge.Instance.usable(player, GetName()))
			return;
		
		//Water
		if (isInWater(player))
		{
			UtilPlayer.message(player, F.main("Skill", "You cannot use " + F.skill(GetName()) + " in water."));
			return;
		}
	
		Block grab = event.getClickedBlock();

		//Blacklist
		for (Material mat : _blacklist)
			if (mat == grab.getType())
			{
				UtilPlayer.message(player, F.main("Game", "You cannot grab this block."));
				return;
			}
		//Usable
		if (UtilBlock.usable(grab))
			return;

		//Door and Banner
		if (grab.getRelative(BlockFace.UP).getTypeId() == 64 || grab.getRelative(BlockFace.UP).getTypeId() == 71 || grab.getRelative(BlockFace.UP).getType() == Material.STANDING_BANNER)
		{
			UtilPlayer.message(player, F.main(GetName(), "You cannot grab this block."));
			return;
		}

		// Ladder and beacon grabs
		if (grab.getType() == Material.LADDER || grab.getType() == Material.BEACON  || grab.getType() == Material.WEB || grab.getType() == Material.STANDING_BANNER || grab.getType() == Material.WALL_BANNER)		
		{
			UtilPlayer.message(player, F.main(GetName(), "You cannot grab this block."));
			return;
		}

		//TrapDoor or ladder
		for (int x = -1; x <= 1; x++)
		{
		    for (int z = -1; z <= 1; z++)
		    {
		        if (x != z && (z == 0 || x == 0))
		        {
		            Block block = grab.getRelative(x, 0, z);
		            
		            if (block.getType() == Material.TRAP_DOOR || block.getType() == Material.LADDER)
		            {
		                UtilPlayer.message(player, F.main(GetName(), "You cannot grab this block."));
		                return;
		            }
		        }
		    }
		}
		
		if (Factory.BlockRestore().contains(grab))
		{
			UtilPlayer.message(player, F.main(GetName(), "You cannot grab this block."));
			return;
		}

		//Block to Item
		FallingBlock block  = player.getWorld().spawnFallingBlock(player.getEyeLocation(), event.getClickedBlock().getType(), event.getClickedBlock().getData());
		block.setDropItem(false);

		//Action
		player.eject();
		player.setPassenger(block);
		_holding.put(player, block);
		_falling.put(block, player);

		//Effect
		player.getWorld().playEffect(event.getClickedBlock().getLocation(), Effect.STEP_SOUND, block.getMaterial().getId());
	}
	
	@EventHandler
	public void Damage(EntityDamageByEntityEvent event)
	{
		Entity vehicle = event.getEntity().getVehicle();
		
		if (_holding.containsKey(vehicle))
		{
			Player attacker = (Player) event.getDamager();
			
			//Forward Damage
			Factory.Damage().NewDamageEvent((Player) vehicle, attacker, Factory.Damage().GetProjectile(event),
					event.getCause(), event.getDamage(),  true, false, false, null, null, event.isCancelled());
		}
	}
	
	@EventHandler
	public void Throw(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		HashSet<Player> voidSet = new HashSet<Player>();
		HashSet<Player> throwSet = new HashSet<Player>();

		for (Player cur : _holding.keySet())
		{
			if (cur.getPassenger() == null)
			{
				voidSet.add(cur);
				continue;	
			}

			if (_holding.get(cur).getVehicle() == null)
			{
				voidSet.add(cur);
				continue;	
			}

			if (!_holding.get(cur).getVehicle().equals(cur))
			{
				voidSet.add(cur);
				continue;	
			}

			//Throw
			if (!cur.isBlocking())
				throwSet.add(cur);

			//Charged Tick
			Charge(cur);
		}

		for (Player cur : voidSet)
		{
			FallingBlock block = _holding.remove(cur);
			_charge.remove(cur);
			block.remove();
		}

		for (Player cur : throwSet)
		{
			Recharge.Instance.recharge(cur, GetName());
			Recharge.Instance.use(cur, GetName(), 1500, false, true);
			
			FallingBlock block = _holding.remove(cur);
			float charge = _charge.remove(cur);
			
			//Throw 
			cur.eject();
			double mult = Math.max(0.4, charge * 2);
			
			Material mat = Material.getMaterial(block.getBlockId());
			
			//Action
			UtilAction.velocity(block, cur.getLocation().getDirection(), mult, false, 0, 0, 1, true);
			Factory.Projectile().AddThrow(block, cur, this, -1, true, true, true, true, 
					null, 0, 0, null, 0, UpdateType.FASTEST, 1.2f);
			
			// Generic Event
			UtilServer.getServer().getPluginManager().callEvent(new SkillEvent(cur, GetName(), ClassType.Brute));
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void Knockback(CustomDamageEvent event)
	{
		Player damager = event.GetDamagerPlayer(true);
		if (damager == null)	return;
		
		if (event.GetReason() == null || !event.GetReason().equals(GetName()))
			return;
		
		event.AddKnockback(GetName(), 1.5d);
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data) 
	{
		if (target == null)
			return;

		int level = getLevel(data.getThrower());
		
		//Damage Event
		Factory.Damage().NewDamageEvent(target, data.getThrower(), null,
				DamageCause.CUSTOM, data.getThrown().getVelocity().length() * (3 + 0.6 * level), true, true, false,
				UtilEnt.getName(data.getThrower()), GetName());

		//Block to Item
		if (data.getThrown() instanceof FallingBlock)
		{
			FallingBlock thrown = (FallingBlock) data.getThrown();

			FallingBlock newThrown  = data.getThrown().getWorld().spawnFallingBlock(data.getThrown().getLocation(), thrown.getMaterial(), thrown.getBlockData());
			newThrown.setDropItem(false);

			//Remove Old
			_falling.remove(thrown);
			thrown.remove();

			//Add New
			if (data.getThrower() instanceof Player)
				_falling.put(newThrown, (Player)data.getThrower());
		}
	}

	@Override
	public void Idle(ProjectileUser data) 
	{

	}

	@Override
	public void Expire(ProjectileUser data) 
	{

	}	

	@EventHandler
	public void blockSolidify(EntityChangeBlockEvent event)
	{
		if (_falling.containsKey(event.getEntity()))
		{
			event.setCancelled(true);
			
			createBlock((FallingBlock)event.getEntity(), event.getBlock());
			
			_falling.remove(event.getEntity());
		}
	}
	
	@EventHandler
	public void CreateBlock(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		Iterator<FallingBlock> fallIter = _falling.keySet().iterator();
		
		while (fallIter.hasNext())
		{
			FallingBlock fall = fallIter.next();
			
			if (!fall.isDead() && fall.isValid())
				continue;
			
			fallIter.remove();

			Block block = fall.getLocation().getBlock();

			// Call and trigger block expiry event
			BlockTossExpireEvent expireEvent = new BlockTossExpireEvent(block);
			Bukkit.getServer().getPluginManager().callEvent(expireEvent);

			if (!expireEvent.isCancelled()) createBlock(fall, block);
		}
	}
	
	public void createBlock(FallingBlock fall, Block block)
	{
		if (!UtilBlock.airFoliage(block))
			return;
		
		int id = fall.getBlockId();
		
		if (id == 12)	id = Material.SANDSTONE.getId();
		if (id == 13)	id = Material.STONE.getId();
		
		//Block Replace
		Factory.BlockRestore().add(block, id, (byte)0, 10000);

		//Effect
		block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getTypeId());
	}

	@EventHandler
	public void onBlockLand(EntityChangeBlockEvent event)
	{
		if (event.getEntity() instanceof FallingBlock)
		{
			if (!event.getBlock().getType().isSolid())	// Falling block is landing and turning block from air to type
			{
				BlockTossLandEvent landEvent = new BlockTossLandEvent(event.getBlock(), ((FallingBlock) event.getEntity()));
				Bukkit.getServer().getPluginManager().callEvent(landEvent);

				if (landEvent.isCancelled())
				{
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void ItemSpawn(ItemSpawnEvent event)
	{
		int id = event.getEntity().getItemStack().getTypeId();

		if (
				id != 1 &&
						id != 2 &&
						id != 3 &&
						id != 4 &&
						id != 12 &&
						id != 13 &&
						id != 80)
			return;

		for (FallingBlock block : _falling.keySet())
			if (UtilMath.offset(event.getEntity().getLocation(), block.getLocation()) < 1)
				event.setCancelled(true);
	}
	
	@EventHandler
	public void expireUnload(ChunkUnloadEvent event)
	{
		Iterator<Entry<FallingBlock, Player>> iterator = _falling.entrySet().iterator();
		while (iterator.hasNext())
		{
			Entry<FallingBlock, Player> entry = iterator.next();
			FallingBlock key = entry.getKey();
			
			if (key.getLocation().getChunk().equals(event.getChunk()))
			{
				key.remove();
				iterator.remove();
			}
		}
	}
	
	@Override
	public void ChunkUnload(ProjectileUser data)
	{
		data.getThrown().remove();
	}

	@Override
	public void Reset(Player player) 
	{
		if (_holding.containsKey(player))
		{
			player.eject();
		}
		
		_holding.remove(player);
		_charge.remove(player);
	}
}
