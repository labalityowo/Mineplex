package mineplex.game.clans.clans.murder;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniClientPlugin;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.ClanInfo;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.shop.ClansShopItem;
import mineplex.minecraft.game.core.combat.CombatComponent;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;

public class MurderManager extends MiniClientPlugin<ClientMurder>
{
	private final List<Material> weapons = Arrays.asList(Material.WOOD_SWORD, Material.STONE_SWORD, Material.IRON_SWORD, Material.DIAMOND_SWORD, Material.WOOD_AXE, Material.STONE_AXE, Material.IRON_AXE, Material.DIAMOND_AXE, Material.BOW);
	
	private final List<Material> armor = Arrays.asList(Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS, Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS, Material.GOLD_HELMET, Material.GOLD_CHESTPLATE, Material.GOLD_LEGGINGS, Material.GOLD_BOOTS, Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS, Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS);
	
	private ClansManager _clansManager;
	
	public MurderManager(JavaPlugin plugin, ClansManager clansManager)
	{
		super("Murder", plugin);
		
		_clansManager = clansManager;
	}
	
	@EventHandler
	public void onCombatDeath(CombatDeathEvent event)
	{
		if (event.GetEvent().getEntity() instanceof Player)
		{
			Player deadPlayer = ((Player) event.GetEvent().getEntity());
			refreshClient(deadPlayer);
			Location location = deadPlayer.getLocation();
			
			CombatComponent combatKiller = event.GetLog().GetKiller();
			if (combatKiller != null && combatKiller.IsPlayer())
			{
				Player killer = UtilPlayer.searchExact(combatKiller.GetName());
				if (killer != null)
				{
					refreshClient(deadPlayer);
					refreshClient(killer);
					if (canMurderOccur(killer, deadPlayer, location) && isWeakling(deadPlayer) && !isWeakling(killer))
					{
						// Was a murder
						event.setKilledWord("murdered");
						ClanInfo killerClan = _clansManager.getClan(killer);
						
						if (killerClan != null) killerClan.addMurder(1);
					}
				}
			}
		}
	}
	
	@EventHandler (ignoreCancelled = true)
	public void onPickup(PlayerPickupItemEvent event)
	{
		refreshClient(event.getPlayer());
	}
	
	@EventHandler
	public void onDrop(PlayerDropItemEvent event)
	{
		refreshClient(event.getPlayer());
	}
	
	@EventHandler
	public void onInventoryEvent(InventoryMoveItemEvent event)
	{
		Inventory source = event.getSource();
		if (source.getHolder() instanceof Player)
		{
			refreshClient(((Player) source.getHolder()));
		}
		
		Inventory dest = event.getDestination();
		if (dest.getHolder() instanceof Player)
		{
			refreshClient(((Player) dest.getHolder()));
		}
	}
	
	@EventHandler
	public void onRespawn(PlayerRespawnEvent event)
	{
		refreshClient(event.getPlayer());
	}
	
	@EventHandler
	public void particle(UpdateEvent event)
	{
		if (event.getType() == UpdateType.SLOW)
		{
			for (Player player : UtilServer.getPlayers())
			{
				refreshClient(player);
			}
		}
		
		if (event.getType() == UpdateType.SEC)
		{
			for (Player player : UtilServer.getPlayers())
			{
				int inventoryValue = getInventoryValue(player);
				
				if (isWeakling(player))
				{
					for (Player other : UtilServer.getPlayers())
					{
						if (canMurderOccur(other, player, other.getLocation()) && inventoryValue <= 2000)
						{
							UtilParticle.PlayParticle(UtilParticle.ParticleType.ANGRY_VILLAGER, player.getEyeLocation().add(0, 0.25, 0), 0, 0, 0, 0, 1, UtilParticle.ViewDist.NORMAL, other);
						}
					}
				}
			}
		}
	}
	
	public boolean isWeakling(Player player)
	{
		return Get(player).isWeakling();
	}
	
	public int getInventoryValue(Player player)
	{
		return Get(player).getInventoryValue();
	}
	
	private int calculateInventoryValue(Player player)
	{
		int value = 0;
		
		// +4 for armor contents
		for (int i = 0; i < player.getInventory().getSize() + 4; i++)
		{
			ItemStack stack = player.getInventory().getItem(i);
			
			if (stack == null)
			{
				continue;
			}
			
			ClansShopItem item = ClansShopItem.getByItem(stack.getType(), stack.getDurability());
			
			if (item != null)
			{
				value += item.getSellPrice(stack.getAmount());
			}
		}
		
		return value;
	}
	
	private boolean canMurderOccur(Player killer, Player victim, Location location)
	{
		if (killer.equals(victim)) return false;
		if (isWeakling(killer)) return false;
		if (victim.isDead()) return false;
		if (_clansManager.getClanUtility().getClaim(location) != null) return false;
		
		return true;
	}
	
	private void refreshClient(Player player)
	{
		boolean weakling = true;
		PlayerInventory inventory = player.getInventory();
		
		// Add 4 for armor contents
		int inventorySize = inventory.getSize() + 4;
		for (int i = 0; i < inventorySize; i++)
		{
			Material type = inventory.getItem(i) != null ? inventory.getItem(i).getType() : null;
			
			weakling = !(weapons.contains(type) || armor.contains(type));
			
			if (!weakling) break;
		}
		
		Get(player).setIsWeakling(weakling);
		Get(player).setInventoryValue(calculateInventoryValue(player));
	}
	
	@Override
	protected ClientMurder addPlayer(UUID uuid)
	{
		return new ClientMurder();
	}
}
