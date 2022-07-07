package mineplex.core.cosmetic;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.Managers;
import mineplex.core.MiniPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.boosters.BoosterManager;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilGear;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilServer;
import mineplex.core.cosmetic.ui.CosmeticShop;
import mineplex.core.donation.DonationManager;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.morph.MorphDinnerbone;
import mineplex.core.gadget.gadgets.outfit.OutfitTeam;
import mineplex.core.gadget.types.Gadget;
import mineplex.core.gadget.types.GadgetType;
import mineplex.core.inventory.InventoryManager;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.pet.PetManager;
import mineplex.core.punish.Punish;
import mineplex.core.treasure.TreasureManager;
import mineplex.core.twofactor.TwoFactorAuth;

public class CosmeticManager extends MiniPlugin
{
	private final TwoFactorAuth _twofactor = Managers.require(TwoFactorAuth.class);
	private final InventoryManager _inventoryManager;
	private final GadgetManager _gadgetManager;
	private final PetManager _petManager;
	private final TreasureManager _treasureManager;
	private final BoosterManager _boosterManager;
	private final Punish _punish;

	private CosmeticShop _shop;
	
	private boolean _showInterface = true;
	private int _interfaceSlot = 4;

	public CosmeticManager(JavaPlugin plugin, CoreClientManager clientManager, DonationManager donationManager,
						   InventoryManager inventoryManager, GadgetManager gadgetManager, PetManager petManager,
						   TreasureManager treasureManager, BoosterManager boosterManager, Punish punish)
	{
		super("Cosmetic Manager", plugin);
		
		_inventoryManager = inventoryManager;
		_gadgetManager = gadgetManager;
		_petManager = petManager;
		_treasureManager = treasureManager;
		_boosterManager = boosterManager;
		_punish = punish;

		_shop = new CosmeticShop(this, clientManager, donationManager, _moduleName);
	}

	public void showInterface(boolean showInterface)
	{
		boolean changed = _showInterface == showInterface;
		
		_showInterface = showInterface;
		
		if (changed)
		{
			for (Player player : Bukkit.getOnlinePlayers())
			{
				if (_showInterface)
					player.getInventory().setItem(_interfaceSlot, ItemStackFactory.Instance.CreateStack(Material.CHEST, (byte)0, 1, ChatColor.RESET + C.cGreen + "Cosmetic Menu"));
				else
					player.getInventory().setItem(_interfaceSlot, null);
			}
		}
	}
	
	@EventHandler 
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		if (!_showInterface)
			return;
				
		giveInterfaceItem(event.getPlayer());
	}
	
	public void giveInterfaceItem(Player player)
	{
		if (!UtilGear.isMat(player.getInventory().getItem(_interfaceSlot), Material.CHEST))
		{
			player.getInventory().setItem(_interfaceSlot, ItemStackFactory.Instance.CreateStack(Material.CHEST, (byte)0, 1, ChatColor.RESET + C.cGreen + "Cosmetic Menu"));
			
			_gadgetManager.redisplayActiveItem(player);
			
			UtilInv.Update(player);
		}
	}
	
	@EventHandler
	public void orderThatChest(final PlayerDropItemEvent event)
	{
		if (!_showInterface)
			return;
		
		if (event.getItemDrop().getItemStack().getType() == Material.CHEST)
		{
			Bukkit.getScheduler().scheduleSyncDelayedTask(getPlugin(), new Runnable()
			{
				public void run()
				{
					if (event.getPlayer().isOnline())
					{
						event.getPlayer().getInventory().remove(Material.CHEST);
						event.getPlayer().getInventory().setItem(_interfaceSlot, ItemStackFactory.Instance.CreateStack(Material.CHEST, (byte)0, 1, ChatColor.RESET + C.cGreen + "Cosmetic Menu"));
						event.getPlayer().updateInventory();
					}
				}
			});
		}
	}
	
	@EventHandler
	public void openShop(PlayerInteractEvent event)
	{
		if (_twofactor.isAuthenticating(event.getPlayer()) || !_showInterface)
			return;
		
		if (event.hasItem() && event.getItem().getType() == Material.CHEST)
		{
			event.setCancelled(true);

			_shop.attemptShopOpen(event.getPlayer());
		}
	}

	// Allows player to open cosmetic shop while carrying armor stand
	// Also calls PlayerInteractEvent to open other menus
	@EventHandler
	public void openShop(PlayerInteractAtEntityEvent event)
	{
		if (!_showInterface)
			return;

		Player player = event.getPlayer();

		if (!(_gadgetManager.getActive(player, GadgetType.MORPH) instanceof MorphDinnerbone))
			return;

		if (!event.getRightClicked().getType().equals(EntityType.ARMOR_STAND))
			return;

		Block block = event.getRightClicked().getLocation().getBlock();
		Action action = Action.RIGHT_CLICK_AIR;
		BlockFace blockFace = BlockFace.SOUTH;
		ItemStack item = player.getItemInHand();
		PlayerInteractEvent playerInteractEvent = new PlayerInteractEvent(player, action, item, block, blockFace);
		Bukkit.getPluginManager().callEvent(playerInteractEvent);
	}

	public GadgetManager getGadgetManager()
	{
		return _gadgetManager;
	}
	
	public PetManager getPetManager()
	{
		return _petManager;
	}

	public InventoryManager getInventoryManager()
	{
		return _inventoryManager;
	}

	public void setInterfaceSlot(int i)
	{
		_interfaceSlot = i;
		
		_gadgetManager.setActiveItemSlot(i-1);
	}

	public void setActive(boolean showInterface)
	{
		_showInterface = showInterface;

		if (!showInterface)
		{
			for (Player player : UtilServer.getPlayers())
			{
				if (player.getOpenInventory().getTopInventory().getHolder() != player)
				{
					player.closeInventory();
				}
			}
		}
	}

	public void disableItemsForGame()
	{
		_gadgetManager.disableAll();
		_petManager.disableAll();
	}

	public void setHideParticles(boolean b)
	{
		_gadgetManager.setHideParticles(b);
	}

	public boolean isShowingInterface()
	{
		return _showInterface;
	}

	public TreasureManager getTreasureManager()
	{
		return _treasureManager;
	}

	public BoosterManager getBoosterManager()
	{
		return _boosterManager;
	}

	public Punish getPunishManager()
	{
		return _punish;
	}

	public CosmeticShop getShop()
	{
		return _shop;
	}

	public void disableTeamArmor()
	{
		for (Gadget gadget : getGadgetManager().getGadgets(GadgetType.COSTUME))
		{
			if (gadget instanceof OutfitTeam)
			{
				((OutfitTeam)gadget).setEnabled(false);
			}
		}
	}
}