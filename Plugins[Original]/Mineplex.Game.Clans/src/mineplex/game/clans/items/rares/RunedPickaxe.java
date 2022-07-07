package mineplex.game.clans.items.rares;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTime;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.core.repository.ClanTerritory;
import mineplex.game.clans.items.PlayerGear;

public class RunedPickaxe extends RareItem
{
	private long _instamineEnabled;
	private boolean _enabled;
	
	static
	{
		UtilServer.RegisterEvents(new Listener()
		{
			@EventHandler
			public void update(UpdateEvent event)
			{
				if (event.getType() != UpdateType.TICK)
				{
					return;
				}
				
				UtilServer.getPlayersCollection().forEach(player ->
				{
					PlayerGear gear = ClansManager.getInstance().getGearManager().getPlayerGear(player);
					
					if (!(gear.getWeapon() instanceof RunedPickaxe))
					{
						return;
					}

					RunedPickaxe pick = (RunedPickaxe) gear.getWeapon();
					
					if (pick._enabled && !UtilTime.elapsed(pick._instamineEnabled, 12000))
					{
						UtilTextBottom.displayProgress("Instant mine", (((double) (((double) System.currentTimeMillis()) - ((double) pick._instamineEnabled))) / 12000D), null, true, player);
					}
					
					if (pick._enabled && (System.currentTimeMillis() - pick._instamineEnabled) >= 12000)
					{
						Recharge.Instance.use(player, "Instant Mine", 15 * 1000, true, true);
						
						pick._enabled = false;
					}
					
					if (!pick._enabled)
					{
						player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 20 * 2, 100));
					}
				});
			}
			
			@EventHandler
			public void onSwap(PlayerItemHeldEvent event)
			{
				if (event.getPlayer().hasPotionEffect(PotionEffectType.FAST_DIGGING))
				{
					ItemStack previous = event.getPlayer().getInventory().getItem(event.getPreviousSlot());
					if (previous != null && previous.getType() == Material.RECORD_7)
					{
						event.getPlayer().removePotionEffect(PotionEffectType.FAST_DIGGING);
					}
				}
			}
			
			@EventHandler(priority = EventPriority.LOWEST)
			public void blockDamage(BlockDamageEvent event)
			{
				PlayerGear gear = ClansManager.getInstance().getGearManager().getPlayerGear(event.getPlayer());
				
				if (!(gear.getWeapon() instanceof RunedPickaxe))
				{
					return;
				}
				
				RunedPickaxe pick = (RunedPickaxe) gear.getWeapon();
				
				String playerClan =  ClansManager.getInstance().getClanUtility().getClanByPlayer(event.getPlayer()) == null ? null :  ClansManager.getInstance().getClanUtility().getClanByPlayer(event.getPlayer()).getName();
				
				ClanTerritory territory =  ClansManager.getInstance().getClanUtility().getClaim(event.getBlock().getLocation());
				
				if (territory != null && !territory.Owner.equals(playerClan))
					return;
				
				if (event.getBlock().getType() == Material.BEDROCK || event.getBlock().getType() == Material.BARRIER)
					return;
				
				if (ClansManager.getInstance().getNetherManager().getNetherWorld().equals(event.getBlock().getWorld()))
					return;
				
				if (ClansManager.getInstance().getWorldEvent().getRaidManager().isInRaid(event.getBlock().getLocation()))
					return;
				
				if (ClansManager.getInstance().getBlockRestore().contains(event.getBlock()))
					return;
				
				if (ClansManager.getInstance().getSiegeManager().getOutpostManager().getOutposts().stream().filter(outpost ->
				{
					return UtilAlg.inBoundingBox(event.getBlock().getLocation(), outpost.getBoundsBlockBreak().getLeft(), outpost.getBoundsBlockBreak().getRight());
				}).findAny().isPresent())
					return;
				
				if (!UtilTime.elapsed(pick._instamineEnabled, 12000))
				{
					event.getBlock().breakNaturally();
					
					event.getBlock().getWorld().playEffect(event.getBlock().getLocation(), Effect.TILE_BREAK, event.getBlock().getTypeId(), 10);
					
					event.getPlayer().playSound(event.getBlock().getLocation(), Sound.LAVA_POP, 1.f, 1.f);
				}
			}
			
			@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
			public void blockBreak(BlockBreakEvent event)
			{
				PlayerGear gear = ClansManager.getInstance().getGearManager().getPlayerGear(event.getPlayer());
				
				if (!(gear.getWeapon() instanceof RunedPickaxe))
				{
					return;
				}
				
				RunedPickaxe pick = (RunedPickaxe) gear.getWeapon();
				
				if (ClansManager.getInstance().getNetherManager().getNetherWorld().equals(event.getBlock().getWorld()))
					return;
				
				if (ClansManager.getInstance().getBlockRestore().contains(event.getBlock()))
					return;
				
				if (ClansManager.getInstance().getWorldEvent().getRaidManager().isInRaid(event.getBlock().getLocation()))
					return;
				
				if (!pick._enabled)
				{
					event.setCancelled(true);
					event.getBlock().breakNaturally();
				}
			}
		});
	}
	
	public RunedPickaxe()
	{
		super("Runed Pickaxe", UtilText.splitLinesToArray(new String[]
				{
					"What an interesting design this pickaxe seems to have!",
					C.cYellow + "Right-Click" + C.cWhite + " to use " + F.elem("Instant mine") + "."
				}, LineFormat.LORE), Material.RECORD_7);
	}
	
	public void onInteract(PlayerInteractEvent event)
	{
		if (!UtilEvent.isAction(event, ActionType.R))
		{
			return;
		}
		
		if (Recharge.Instance.usable(event.getPlayer(), "Instant Mine", true) && UtilTime.elapsed(_instamineEnabled, 15000))
		{
			UtilTextMiddle.display("", "Instant mine enabled for " + F.elem("12 Seconds"), 20, 80, 20, event.getPlayer());
			_instamineEnabled = System.currentTimeMillis();
			_enabled = true;
		}
		
		super.onInteract(event);
	}	
}