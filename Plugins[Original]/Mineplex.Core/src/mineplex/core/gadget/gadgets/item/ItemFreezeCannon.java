package mineplex.core.gadget.gadgets.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.Ammo;
import mineplex.core.gadget.types.ItemGadget;
import mineplex.core.projectile.IThrown;
import mineplex.core.projectile.ProjectileUser;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class ItemFreezeCannon extends ItemGadget implements IThrown
{

	private HashMap<Player, Long> _playerMap = new HashMap<>();
	
	public ItemFreezeCannon(GadgetManager manager)
	{
		super(manager, "Freeze Cannon", 
				UtilText.splitLineToArray(C.cWhite + "Let someone cool off inside their very own ice cube!", LineFormat.LORE),
				-1, 
				Material.ICE, (byte) 0, 8000, new Ammo("Freeze Cannon", "100 Ice Blocks", Material.ICE, (byte) 0, new String[]
				{
						C.cWhite + "100 Ice Blocks for you to launch!"
				}, -1, 1));
	}

	@Override
	public void ActivateCustom(Player player)
	{
		Item item = player.getWorld().dropItem(player.getEyeLocation().add(player.getLocation().getDirection()), new ItemStack(Material.ICE));
		UtilAction.velocity(item, player.getLocation().getDirection(), 
				1, false, 0, 0.2, 10, false);

		Manager.getProjectileManager().AddThrow(item, player, this, 5000, true, true, true, true, 
				null, 1f, 1f, null, null, 0, UpdateType.TICK, 0.5f);

		//Inform
		UtilPlayer.message(player, F.main("Skill", "You used " + F.skill(getName()) + "."));

		//Effect
		item.getWorld().playSound(item.getLocation(), Sound.EXPLODE, 0.5f, 0.5f);
	}
	
	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data)
	{
		if (target instanceof Player && Manager.selectEntity(this, target))
		{
			Player p = (Player) target;
			
			int ticks = 4 * 20;
			if(isClear(p.getLocation()))
			{
				Manager.getBlockRestore().add(p.getLocation().getBlock(), Material.STAINED_GLASS.getId(), (byte)5, 0, (byte)0, ticks*50);
				Manager.getBlockRestore().add(p.getLocation().getBlock().getRelative(BlockFace.UP), Material.STAINED_GLASS.getId(), (byte)5, 0, (byte)0, ticks*50);
				if(p.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR)
				{
					Manager.getBlockRestore().add(p.getLocation().getBlock().getRelative(BlockFace.DOWN), Material.STAINED_GLASS.getId(), (byte)5, 0, (byte)0, ticks*50);
				}
				p.setWalkSpeed(0);
				_playerMap.put(p, System.currentTimeMillis() + (ticks * 50));
				p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, ticks, 250, true, false));
				p.teleport(target.getLocation().getBlock().getLocation().add(0.5, 0, 0.5));
				Recharge.Instance.useForce(p, "Double Jump", ticks*50);
				p.setAllowFlight(false);
				p.getWorld().playSound(p.getLocation(), Sound.FIZZ, 0.25f, 0.75f);
			}

			//Effect
			target.playEffect(EntityEffect.HURT);
		}
		
		smash(data.getThrown());
	}
	
	@EventHandler
	public void onMove(PlayerMoveEvent event)
	{
		if(!_playerMap.containsKey(event.getPlayer())) return;
		
		if(event.getFrom().distanceSquared(event.getTo()) > 0.3)
		{
			event.setCancelled(true);
		}
	}
	
	public boolean isClear(Location loc)
	{
		Block b = loc.getBlock();
		ArrayList<Block> blocks = UtilBlock.getInBoundingBox(b.getLocation().subtract(1, 0, 1), b.getLocation().add(1, 1, 1));
		for(Block test : blocks)
		{
			if(test.getType() == Material.PORTAL) return false;
		}
		if(b.getType() != Material.AIR) return false;
		if(b.getRelative(BlockFace.UP).getType() != Material.AIR) return false;
		
		return true;
	}
	
	@EventHandler
	public void cleanup(UpdateEvent event)
	{
		if(event.getType() == UpdateType.FAST)
		{
			for(Iterator<Player> it = _playerMap.keySet().iterator(); it.hasNext();)
			{
				Player p = it.next();
				Long time = _playerMap.get(p);
				if(time == null) 
				{
					p.setWalkSpeed(0.2f);
					p.setAllowFlight(false);
					it.remove();
					continue;
				}
				if(time < System.currentTimeMillis())
				{
					p.setWalkSpeed(0.2f);
					p.setAllowFlight(false);
					it.remove();
					continue;
				}
			}
		}
		if(event.getType() == UpdateType.TICK)
		{
			for(Player p : _playerMap.keySet())
			{
				Long time = _playerMap.get(p);
				if(time <= System.currentTimeMillis()) continue;
				
				for(Player op : UtilServer.getPlayers())
				{
					if(op.equals(p)) continue;
					op.sendBlockChange(p.getLocation(), Material.ICE, (byte) 0);
					op.sendBlockChange(p.getLocation().add(0, 1, 0), Material.ICE, (byte) 0);
				}
			}
		}
	}

	@Override
	public void Idle(ProjectileUser data)
	{
		smash(data.getThrown());
	}

	@Override
	public void Expire(ProjectileUser data)
	{
		smash(data.getThrown());
	}
	
	@Override
	public void ChunkUnload(ProjectileUser data)
	{
		data.getThrown().remove();
	}
	
	public void smash(Entity ent)
	{	
		//Effect
		ent.getWorld().playEffect(ent.getLocation(), Effect.STEP_SOUND, Material.ICE);
		
		//Remove
		ent.remove();
	}
}