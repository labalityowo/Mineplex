package nautilus.game.arcade.game.games.wizards;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilServer;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.lifetimes.Lifetime;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game.GameState;

import nautilus.game.arcade.game.GameComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class WizardSpellMenu extends GameComponent<Wizards>
{
	private WizardSpellMenuShop _wizardShop;
	private ItemStack _wizardSpells = new ItemBuilder(Material.ENCHANTED_BOOK).setTitle(C.cGold + "Wizard Spells")
			.addLore(C.cGray + "Right click with this to view the spells").build();

	public WizardSpellMenu(Wizards wizards)
	{
		super(wizards);
		_wizardShop = new WizardSpellMenuShop(this, wizards.getArcadeManager().GetClients(), wizards.getArcadeManager()
				.GetDonation(), wizards);
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event)
	{
		if (getGame().GetState() == GameState.Recruit || getGame().GetState() == GameState.Live)
		{
			event.getPlayer().getInventory().setItem(0, _wizardSpells);
		}
	}

	@EventHandler
	public void onDeath(final PlayerDeathEvent event)
	{
		Bukkit.getScheduler().scheduleSyncDelayedTask(UtilServer.getPlugin(), new Runnable()
		{
			public void run()
			{
				if (getGame().IsLive())
				{
					event.getEntity().getInventory().setItem(0, _wizardSpells);
				}
			}
		});
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void Observer(PlayerCommandPreprocessEvent event)
	{
		if (event.getMessage().equalsIgnoreCase("/spec"))
		{
			if (!getGame().IsAlive(event.getPlayer())
					&& !UtilInv.contains(event.getPlayer(), _wizardSpells.getType(), (byte) 0, 1))
			{
				event.getPlayer().getInventory().setItem(0, _wizardSpells);
			}
		}
	}

	@EventHandler
	public void onGameStateChange(GameStateChangeEvent event)
	{
		if (event.GetState() == GameState.Recruit)
		{
			for (Player player : UtilServer.GetPlayers())
			{
				player.getInventory().setItem(0, _wizardSpells);
			}
		} 
		else if(event.GetState() == GameState.Prepare)
		{
			for(Player player : UtilServer.GetPlayers())
			{
				if (!getGame().IsAlive(player))
				{
					player.getInventory().setItem(0, _wizardSpells);
				}
			}
		}
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent event)
	{
		if (event.getAction() != Action.PHYSICAL && event.getAction().name().contains("RIGHT")
				&& (!getGame().IsLive() || !getGame().IsAlive(event.getPlayer())))
		{

			ItemStack item = event.getItem();

			if (item != null && item.isSimilar(_wizardSpells))
			{

				_wizardShop.attemptShopOpen(event.getPlayer());
			}
		}

		if (getGame().IsLive() && getGame().IsAlive(event.getPlayer()))
		{
			Player p = event.getPlayer();

			if (p.getInventory().getHeldItemSlot() < getGame().getWizard(p).getWandsOwned())
			{
				if (event.getAction().name().contains("RIGHT"))
				{
					if (p.getInventory().getHeldItemSlot() < 5)
					{
						if (event.getClickedBlock() == null || !(event.getClickedBlock().getState() instanceof InventoryHolder))
						{
							_wizardShop.attemptShopOpen(p);
						}
					}
				}
			}
		}
	}
}
