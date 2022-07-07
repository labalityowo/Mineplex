package nautilus.game.arcade.kit.perks;

import java.util.HashMap;
import java.util.HashSet;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.event.PerkDestructorBlockEvent;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class PerkDestructor extends Perk
{
	private boolean _enabled;
	
	private int _spawnRate;
	private int _max;
	
	private long _fallTime;

	private double _damage;

	private HashSet<String> _preparing = new HashSet<String>();
	private HashMap<Block, Long> _blocks = new HashMap<Block, Long>();

	public PerkDestructor(int spawnRate, int max, long fallTime, double damage, boolean enabled)
	{
		super("Seismic Charge", new String[]
				{
						C.cGray + "Receive 1 Seismic Charge every " + spawnRate + " seconds. Maximum of " + max + ".",
						C.cYellow + "Right-Click" + C.cGray + " with Seismic Charge to " + C.cGreen + "Throw Seismic Charge",
						enabled ? "" : C.cGray + "You will not receive them until bridges drop",
				});

		_spawnRate = spawnRate;
		_max = max;
		_fallTime = fallTime;
		_damage = damage;

		_enabled = enabled;
	}

	public void Apply(Player player)
	{
		Recharge.Instance.use(player, GetName(), _spawnRate * 1000, false, false);
	}

	@EventHandler
	public void spawn(UpdateEvent event)
	{
		if (!_enabled)
			return;
		
		if (event.getType() != UpdateType.FAST)
			return;

		for (Player cur : UtilServer.getPlayers())
		{
			if (!Kit.HasKit(cur))
				continue;

			if (!Manager.GetGame().IsAlive(cur))
				continue;

			if (!Recharge.Instance.use(cur, GetName(), _spawnRate * 1000, false, false))
				continue;

			if (UtilInv.contains(cur, "Seismic Charge", Material.ENDER_PEARL, (byte) 0, _max))
				continue;

			//Add
			cur.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.ENDER_PEARL, (byte) 0, 1, F.item("Seismic Charge")));

			cur.playSound(cur.getLocation(), Sound.ITEM_PICKUP, 2f, 1f);
		}
	}

	@EventHandler
	public void drop(PlayerDropItemEvent event)
	{
		if (event.isCancelled())
			return;
		
		if (!UtilInv.IsItem(event.getItemDrop().getItemStack(), "Seismic Charge", Material.ENDER_PEARL, (byte) 0))
			return;

		//Cancel
		event.setCancelled(true);

		//Inform
		UtilPlayer.message(event.getPlayer(), F.main(GetName(), "You cannot drop " + F.item("Seismic Charge") + "."));
	}

	@EventHandler
	public void deathRemove(PlayerDeathEvent event)
	{
		HashSet<org.bukkit.inventory.ItemStack> remove = new HashSet<org.bukkit.inventory.ItemStack>();

		for (org.bukkit.inventory.ItemStack item : event.getDrops())
			if (UtilInv.IsItem(item, Material.ENDER_PEARL, (byte) 0))
				remove.add(item);

		for (org.bukkit.inventory.ItemStack item : remove)
			event.getDrops().remove(item);
	}

	@EventHandler
	public void invClick(InventoryClickEvent event)
	{
		UtilInv.DisallowMovementOf(event, "Seismic Charge", Material.ENDER_PEARL, (byte) 0, true);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void preThrowItem(PlayerInteractEvent event)
	{
		if (!UtilEvent.isAction(event, ActionType.R))
			return;
		
		if (!Manager.GetGame().IsLive())
			return;
		
		if (!Manager.IsAlive(event.getPlayer()))
			return;
		
		if (!UtilInv.IsItem(event.getItem(), "Seismic Charge", Material.ENDER_PEARL, (byte) 0))
			return;
		
		_preparing.add(event.getPlayer().getName());
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void throwItem(ProjectileLaunchEvent event)
	{
		if (Manager.GetGame() == null || !Manager.GetGame().IsLive())
			return;
		
		if (!(event.getEntity() instanceof EnderPearl))
			return;
		
		if (!(event.getEntity().getShooter() instanceof Player))
			return;

		Player player = (Player) event.getEntity().getShooter();

		if (!Manager.IsAlive(player))
			return;

		if (_preparing.contains(player.getName()))
		{
			_preparing.remove(player.getName());
			event.getEntity().setMetadata("Destructor", new FixedMetadataValue(Manager.getPlugin(), 1));

			event.getEntity().setVelocity(event.getEntity().getVelocity().multiply(0.7));
		}		
	}

	@EventHandler
	public void collide(ProjectileHitEvent event)
	{
		if (!Manager.GetGame().IsLive())
			return;

		Projectile proj = event.getEntity();

		if (!(proj.getShooter() instanceof Player))
			return;

		Player player = (Player) proj.getShooter();

		if (!Manager.IsAlive(player))
			return;

		if (!Kit.HasKit(player))
			return;
		
		if (!proj.hasMetadata("Destructor"))
			return;

		for (Player hit : UtilPlayer.getNearby(proj.getLocation(), 3))
		{
			if (player.equals(hit))
			{
				continue;
			}

			Manager.GetDamage().NewDamageEvent(hit, player, proj, EntityDamageEvent.DamageCause.CUSTOM, _damage, true, true, true, proj.getName(), GetName());
		}

		for (Block block : UtilBlock.getInRadius(proj.getLocation(), 4).keySet())
		{
			if (block.getType() == Material.AIR || block.getType() == Material.BEDROCK || block.getType() == Material.BARRIER || block.isLiquid())
			{
				continue;
			}
			
			//Event
			PerkDestructorBlockEvent blockEvent = new PerkDestructorBlockEvent(player, block);
			UtilServer.getServer().getPluginManager().callEvent(blockEvent);
			
			if (!blockEvent.isCancelled())
			{
				_blocks.put(block, System.currentTimeMillis());

				block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getType());
			}
		}

		UtilParticle.PlayParticle(ParticleType.LARGE_EXPLODE, proj.getLocation(), 0f, 0f, 0f, 0f, 1, ViewDist.MAX, UtilServer.getPlayers());

		proj.getWorld().playSound(proj.getLocation(), Sound.EXPLODE, 1f, 0.5f);
		proj.getWorld().playSound(proj.getLocation(), Sound.FIREWORK_TWINKLE, 2f, 0.5f);
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void fall(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		Block lowest = null;
		int lowestY = 0;
		
		for (Block block : _blocks.keySet())
		{	
			if (!UtilTime.elapsed(_blocks.get(block), _fallTime))
				continue;
			
			if (lowest == null || block.getY() < lowestY)
			{
				lowest = block;
				lowestY = block.getY();
			}
		}
		
		if (lowest != null)
		{
			Block down = lowest.getRelative(BlockFace.DOWN);
			
			if (lowest.getType() != Material.AIR && (UtilBlock.airFoliage(down) || down.isLiquid()))
			{
				lowest.getWorld().playEffect(lowest.getLocation(), Effect.STEP_SOUND, lowest.getType());
				
				Material type = lowest.getType();
				byte data = lowest.getData();
				lowest.setType(Material.AIR);
				
				lowest.getWorld().spawnFallingBlock(lowest.getLocation().add(0.5, 0.5, 0.5), type, data);
			}
			
			_blocks.remove(lowest);
		}
	}

	@EventHandler
	public void pearlTeleport(PlayerTeleportEvent event)
	{
		if (!Manager.GetGame().IsLive())
		{
			return;
		}

		if (event.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL)
		{
			return;
		}

		if (!Kit.HasKit(event.getPlayer()))
		{
			return;
		}

		event.setCancelled(true);
	}

	public void setEnabled(boolean var)
	{
		_enabled = var;
	}
}
