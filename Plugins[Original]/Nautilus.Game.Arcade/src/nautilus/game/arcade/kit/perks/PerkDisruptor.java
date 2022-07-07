package nautilus.game.arcade.kit.perks;

import java.util.HashMap;

import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilServer;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.updater.UpdateType;
import nautilus.game.arcade.kit.Perk;

public class PerkDisruptor extends Perk
{
	private HashMap<Entity, Player> _tntMap = new HashMap<Entity, Player>();
	
	private int _spawnRate;
	private int _max;
	
	public PerkDisruptor(int spawnRate, int max) 
	{
		super("Bomber", new String[] 
				{
				C.cGray + "Receive 1 Disruptor every " + spawnRate + " seconds. Maximum of " + max + ".",
				C.cYellow + "Click" + C.cGray + " with TNT to " + C.cGreen + "Place Disruptor"
				});
		
		_spawnRate = spawnRate;
		_max = max;
	}
	
	public void Apply(Player player) 
	{
		Recharge.Instance.use(player, GetName(), _spawnRate*1000, false, false);
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

			if (!Recharge.Instance.use(cur, GetName(), _spawnRate*1000, false, true))
				continue;

			if (UtilInv.contains(cur, Material.TNT, (byte)0, _max))
				continue;

			//Add
			cur.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.TNT, (byte)0, 1, F.item("Disruptor")));

			cur.playSound(cur.getLocation(), Sound.ITEM_PICKUP, 2f, 1f);
		}
	}
	
	@EventHandler
	public void Place(PlayerInteractEvent event)
	{
		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK &&
			event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_AIR)
			return;
		
		Player player = event.getPlayer();
		
		if (!UtilInv.IsItem(player.getItemInHand(), Material.TNT, (byte)0))
			return;
				
		if (!Kit.HasKit(player))
			return;
		
		event.setCancelled(true);
		
		UtilInv.remove(player, Material.TNT, (byte)0, 1);
		UtilInv.Update(player);
		
		Item item = player.getWorld().dropItem(player.getEyeLocation(), ItemStackFactory.Instance.CreateStack(Material.TNT));
		item.setVelocity(new Vector(0,0,0));
		
		_tntMap.put(item, player);
	}
	
	@EventHandler
	public void Explode(PlayerPickupItemEvent event)
	{
		if (!_tntMap.containsKey(event.getItem()))
			return;
		
		event.setCancelled(true);
		
		if (!Manager.GetGame().IsAlive(event.getPlayer()))
			return;
		
		if (event.getItem().getTicksLived() < 40)
			return;
		
		if (UtilMath.offset(event.getItem(), event.getPlayer()) > 2)
			return;
		
		//Dont Hit Self
		if (event.getPlayer().equals(_tntMap.get(event.getItem())))
			return;
		
		_tntMap.remove(event.getItem());
		event.getItem().remove();
		
		UtilAction.velocity(event.getPlayer(), new Vector(0, 0.5, 0));
		event.getPlayer().getWorld().playSound(event.getPlayer().getLocation(), Sound.EXPLODE, 1f, 2f);
		event.getPlayer().playEffect(EntityEffect.HURT);
		
		event.getPlayer().setSprinting(false);
	}
}
