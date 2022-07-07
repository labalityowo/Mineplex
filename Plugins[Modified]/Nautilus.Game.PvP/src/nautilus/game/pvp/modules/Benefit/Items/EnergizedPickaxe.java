package nautilus.game.pvp.modules.Benefit.Items;

import java.util.HashSet;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import mineplex.core.CurrencyType;
import mineplex.core.common.util.F;
import mineplex.core.energy.Energy;
import nautilus.game.pvp.modules.Benefit.BenefitManager;

public class EnergizedPickaxe extends BenefitItem
{
	private HashSet<String> _active;
	private Energy _energy;
	
	public EnergizedPickaxe(BenefitManager plugin, Energy energy, Material material)
	{
		super(plugin, "Energized Pickaxe", material, ChatColor.RESET + "Available to Emerald or Diamond Ranks only.", ChatColor.BLACK + "", ChatColor.RESET + "" + ChatColor.GRAY + "Hold Diamond Pickaxe.", ChatColor.RESET + "" + ChatColor.GRAY + "Right-click to enable instant break mode.", ChatColor.RESET + "" + ChatColor.GRAY + "Consumes 12 Energy per block.");
		
		_energy = energy;
		_active = new HashSet<String>();
	}
	
	@Override
	public void Sold(Player player, CurrencyType currencyType)
	{
		// Nothing
	}
	
	@EventHandler
	public void OnPlayerInteract(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		
		if (event.getItem() != null && event.getItem().getType() == Material.DIAMOND_PICKAXE && Plugin.PlayerOwnsMe(getName(), player))
		{
			if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) && !_active.contains(player.getName()))
			{
				_active.add(player.getName());
				player.sendMessage(F.main(getName(), getName() + " activated."));
				return;
			}
		}
	}
	
	@EventHandler
	public void OnPlayerItemHeldChange(PlayerItemHeldEvent event)
	{
		if (_active.contains(event.getPlayer().getName()) && event.getPlayer().getItemInHand() != null && event.getPlayer().getItemInHand().getType() == Material.DIAMOND_PICKAXE)
		{
			Deactivate(event.getPlayer());
		}
	}
	
	@EventHandler
	public void OnPlayerOpenInventory(InventoryClickEvent event)
	{
		if (_active.contains(event.getWhoClicked().getName()) && event.getWhoClicked().getItemInHand() != null && event.getWhoClicked().getItemInHand().getType() != Material.DIAMOND_PICKAXE)
		{
			Deactivate((Player)event.getWhoClicked());
		}
	}
	
	@EventHandler
	public void OnPlayerDropItem(PlayerDropItemEvent event)
	{
		if (_active.contains(event.getPlayer().getName()) && event.getItemDrop().getItemStack().getType() == Material.DIAMOND_PICKAXE)
		{
			Deactivate(event.getPlayer());
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void OnBlockDamageEvent(BlockDamageEvent event)
	{
		if (_active.contains(event.getPlayer().getName()))
		{
			if (event.getBlock().getType() != Material.BEDROCK)
				event.setInstaBreak(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void OnBlockBreakEvent(BlockBreakEvent event)
	{
		if (event.isCancelled())
			return;
		
		Player player = event.getPlayer();
		
		if (_active.contains(player.getName()))
		{
			if (!_energy.use(player, Plugin.getName(), 12, true, false))
			{
				_active.remove(player.getName());
				player.sendMessage(F.main(getName(), getName() + " deactivated."));
				event.setCancelled(true);
				return;
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void OnEntityDeath(EntityDeathEvent event)
	{
		if (event.getEntityType() == EntityType.PLAYER)
		{
			_active.remove(((Player)event.getEntity()).getName());
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void OnPlayerQuit(PlayerQuitEvent event)
	{
		_active.remove(event.getPlayer().getName());
	}
	
	private void Deactivate(Player player)
	{
		_active.remove(player.getName());
		player.sendMessage(F.main(getName(), getName() + " deactivated."));
	}
}
