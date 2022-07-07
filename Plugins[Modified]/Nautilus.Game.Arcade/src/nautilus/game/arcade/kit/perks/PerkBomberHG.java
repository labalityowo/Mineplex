package nautilus.game.arcade.kit.perks;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.kit.Perk;

public class PerkBomberHG extends Perk
{
	private int _spawnRate;
	private int _max;
	
	public PerkBomberHG(int spawnRate, int max)
	{
		super("Explosives", new String[] 
				{
				C.cGray + "Receive 1 TNT every " + spawnRate + " seconds. Maximum of " + max + ".",
				});
		
		_spawnRate = spawnRate;
		_max = max;
	}
	
	public void Apply(Player player) 
	{
		Recharge.Instance.use(player, GetName(), _spawnRate*1000, false, false);
	}
	
	@EventHandler
	public void TNTSpawn(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		for (Player cur : UtilServer.getPlayers())
		{
			if (!Kit.HasKit(cur))
				continue;
			
			if (!hasPerk(cur))
				continue;
			
			if (!Manager.GetGame().IsAlive(cur))
				continue;

			if (!Recharge.Instance.use(cur, GetName(), _spawnRate*1000, false, false))
				continue;

			if (UtilInv.contains(cur, Material.TNT, (byte)0, _max))
				continue;
			
			if (UtilPlayer.isSpectator(cur))
				continue;

			//Add
			cur.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.TNT, (byte)0, 1, F.item("Throwable TNT")));

			cur.playSound(cur.getLocation(), Sound.ITEM_PICKUP, 2f, 1f);
		}
	}

	@EventHandler
	public void TNTDrop(PlayerDropItemEvent event)
	{
		if (event.isCancelled())
			return;
		
		if (!Kit.HasKit(event.getPlayer()))
			return;
		
		if (!hasPerk(event.getPlayer()))
			return;

		if (event.getItemDrop().getItemStack().getType() != Material.TNT)
		{
			return;
		}

		//Cancel
		event.setCancelled(true);

		//Inform
		UtilPlayer.message(event.getPlayer(), F.main(GetName(), "You cannot drop " + F.item("Throwable TNT") + "."));
	}

	@EventHandler
	public void TNTInvClick(InventoryClickEvent event)
	{	
		boolean clickInContainer = event.getClickedInventory() != null && !(event.getClickedInventory().getHolder() instanceof Player);
		
		if (clickInContainer)
		{
			return;
		}
		
		if (!(event.getWhoClicked() instanceof Player))
			return;
		
		if (!Kit.HasKit((Player)event.getWhoClicked()))
			return;
		
		if (!hasPerk((Player)event.getWhoClicked()))
			return;
		
		UtilInv.DisallowMovementOf(event, "Throwable TNT", Material.TNT, (byte) 0, true);
	}

	@EventHandler
	public void TNTDeathRemove(PlayerDeathEvent event)
	{	
		Set<ItemStack> remove = new HashSet<>();

		for (ItemStack item : event.getDrops())
			if (UtilInv.IsItem(item, Material.TNT, (byte)0))
				remove.add(item);

		for (ItemStack item : remove)
			event.getDrops().remove(item);
	}
}
