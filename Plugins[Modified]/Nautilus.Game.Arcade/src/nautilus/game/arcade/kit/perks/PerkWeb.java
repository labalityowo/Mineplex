package nautilus.game.arcade.kit.perks;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilServer;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.projectile.IThrown;
import mineplex.core.projectile.ProjectileUser;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.updater.UpdateType;
import nautilus.game.arcade.kit.Perk;

public class PerkWeb extends Perk implements IThrown
{
	private int _spawnRate;
	private int _max;
	
	public PerkWeb(int spawnRate, int max) 
	{
		super("Bomber", new String[] 
				{
				C.cGray + "Receive 1 Web every " + spawnRate + " seconds. Maximum of " + max + ".",
				C.cYellow + "Click" + C.cGray + " with Web to " + C.cGreen + "Throw Web"
				});
		
		_spawnRate = spawnRate;
		_max = max;
	}
	
	@EventHandler
	public void Spawn(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		for (Player cur : UtilServer.getPlayers())
		{
			if (!Kit.HasKit(cur))
				continue;
			
			if (!Manager.GetGame().IsAlive(cur))
				continue;

			if (!Recharge.Instance.use(cur, GetName(), _spawnRate*1000, false, false))
				continue;

			if (UtilInv.contains(cur, Material.WEB, (byte)0, _max))
				continue;

			//Add
			cur.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.WEB));
		}
	}

	@EventHandler
	public void Throw(PlayerInteractEvent event)
	{
		if (event.getPlayer().getItemInHand() == null)
			return;
		
		if (event.getPlayer().getItemInHand().getType() == Material.WEB)
		{
			if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK &&
				event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
				return;
		}
		else if (event.getPlayer().getItemInHand().getType().toString().contains("_AXE"))
		{
			if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
				return;	
		}
		else
		{
			return;
		}
		
		Player player = event.getPlayer();
		
		if (!Kit.HasKit(player))
			return;
		
		event.setCancelled(true);
		
		UtilInv.remove(player, Material.WEB, (byte)0, 1);
		UtilInv.Update(player);
		
		org.bukkit.entity.Item ent = player.getWorld().dropItem(player.getEyeLocation(), ItemStackFactory.Instance.CreateStack(Material.WEB));
		UtilAction.velocity(ent, player.getLocation().getDirection(), 0.8, false, 0, 0.2, 10, false);
		Manager.GetProjectile().AddThrow(ent, player, this, -1, true, true, true, false, 0.5f);
	}
	
	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data) 
	{
		if (target != null)
		{
			if (target instanceof Player)
			{
				if (!Manager.GetGame().IsAlive((Player)target))
				{
					return;
				}
			}
		}
		
		Web(data);
	}

	@Override
	public void Idle(ProjectileUser data) 
	{
		Web(data);
	}

	@Override
	public void Expire(ProjectileUser data) 
	{
		Web(data);
	}
	
	@Override
	public void ChunkUnload(ProjectileUser data)
	{
		data.getThrown().remove();
	}
	
	public void Web(ProjectileUser data)
	{
		Location loc = data.getThrown().getLocation();
		data.getThrown().remove();
		
		Manager.GetBlockRestore().add(loc.getBlock(), 30, (byte) 0, 4000);
	}
}