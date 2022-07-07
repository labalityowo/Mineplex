package mineplex.game.clans.clans.banners;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniPlugin;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.recharge.Recharge;
import mineplex.game.clans.clans.ClanInfo;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.banners.command.BannerCommand;
import mineplex.game.clans.core.repository.ClanTerritory;
import net.minecraft.server.v1_8_R3.MinecraftServer;

/**
 * Manager class for cosmetic clans banners
 */
public class BannerManager extends MiniPlugin
{
	public enum Perm implements Permission
	{
		BANNER_COMMAND,
		BANNER_ACCESS,
	}

	public final Map<String, ClanBanner> LoadedBanners = new HashMap<>();
	private final BlockFace[] _radial = { BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST, BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST };
	private BannerRepository _repo;
	
	public BannerManager(JavaPlugin plugin)
	{
		super("Clan Banners", plugin);
		_repo = new BannerRepository(plugin, this);
		
		addCommand(new BannerCommand(this));
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{
		PermissionGroup.PLAYER.setPermission(Perm.BANNER_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.BANNER_ACCESS, true, true);
	}
	
	/**
	 * Checks what type of banner unlock a player has
	 * @param player The player to check
	 * @return The type of banner unlock a player has
	 */
	public int getBannerUnlockLevel(Player player)
	{
		int level = 0;
		if (ClansManager.getInstance().getDonationManager().Get(player).ownsUnknownSalesPackage("Clan Banner Usage"))
		{
			level = 1;
		}
		if (ClansManager.getInstance().getDonationManager().Get(player).ownsUnknownSalesPackage("Clan Banner Editor"))
		{
			level = 2;
		}
		if (ClansManager.getInstance().getClientManager().Get(player).hasPermission(Perm.BANNER_ACCESS))
		{
			level = 2;
		}
		return level;
	}
	
	/**
	 * Loads a banner for a clan
	 * @param clan The clan whose banner to load
	 */
	public void loadBanner(ClanInfo clan)
	{
		_repo.loadBanner(LoadedBanners, clan);
	}
	
	/**
	* Loads all banners for this clans server
	* @param manager The Clans Manager instance triggering this load
	*/
	public void loadBanners(ClansManager manager)
	{
		_repo.loadBanners(LoadedBanners, manager);
	}
	
	/**
	 * Saves a banner to the database
	 * @param banner The banner to save
	 */
	public void saveBanner(ClanBanner banner)
	{
		_repo.saveBanner(banner);
	}
	
	/**
	 * Deletes a clan's banner
	 * @param clan The clan whose banner to delete
	 */
	public void deleteBanner(ClanInfo clan)
	{
		_repo.deleteBanner(clan);
		LoadedBanners.remove(clan.getName());
	}
	
	/**
	 * Deletes a clan banner
	 * @param banner The banner to delete
	 */
	public void deleteBanner(ClanBanner banner)
	{
		deleteBanner(banner.getClan());
	}
	
	/**
	 * Places a clans banner for a player
	 * @param placing The player who placed the banner
	 * @param banner The banner to place
	 */
	public void placeBanner(Player placing, ClanBanner banner)
	{
		Block block = placing.getLocation().getBlock();
		BlockPlaceEvent event = new BlockPlaceEvent(block, block.getState(), block, placing.getItemInHand(), placing, true);
		Bukkit.getPluginManager().callEvent(event);
		if (!event.isCancelled())
		{
			ClanTerritory claim = ClansManager.getInstance().getClanUtility().getClaim(block.getLocation());
			if (claim != null && !claim.Owner.equals(banner.getClan().getName()))
			{
				UtilPlayer.message(placing, F.main("Clans", "You cannot place your Clan Banner there."));
				return;
			}
			ClansManager.getInstance().getBlockRestore().restore(block);
			if (block.getType() == Material.AIR && UtilBlock.fullSolid(block.getRelative(BlockFace.DOWN)))
			{
				if (!Recharge.Instance.use(placing, "Place Banner", 30000, true, false))
				{
					return;
				}
				block.setType(Material.STANDING_BANNER);
				Banner state = (Banner) block.getState();
				state.setBaseColor(banner.getBaseColor());
				state.setPatterns(((BannerMeta)banner.getBanner().getItemMeta()).getPatterns());
				org.bukkit.material.Banner data = (org.bukkit.material.Banner) state.getData();
				try
				{
					data.setFacingDirection(_radial[Math.round(placing.getLocation().getYaw() / 45f) & 0x7]);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				state.setData(data);
				state.update();
			}
			else
			{
				UtilPlayer.message(placing, F.main("Clans", "You cannot place your Clan Banner there."));
				return;
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBreak(BlockBreakEvent event)
	{
		if (event.getBlock().getType() == Material.STANDING_BANNER || event.getBlock().getType() == Material.WALL_BANNER)
		{
			event.setCancelled(true);
			event.getBlock().setType(Material.AIR);
		}
	}
	
	@EventHandler
	public void onDropBanner(BlockPhysicsEvent event)
	{
		if (event.getBlock().getType() == Material.STANDING_BANNER || event.getBlock().getType() == Material.WALL_BANNER)
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onCraftBanner(PrepareItemCraftEvent event)
	{
		if (event.getInventory().getResult() == null || event.getInventory().getResult().getType() != Material.BANNER)
		{
			return;
		}
		
		event.getInventory().setResult(null);
	}
	
	@EventHandler
	public void onCraftBanner(CraftItemEvent event)
	{
		if (event.getInventory().getResult() == null || event.getInventory().getResult().getType() != Material.BANNER)
		{
			return;
		}
		
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onJoinWithBanner(PlayerJoinEvent event)
	{
		if (MinecraftServer.getServer().recentTps[0] < 19)
		{
			return;
		}
		runSyncLater(() ->
		{
			for (ItemStack item : event.getPlayer().getInventory().getContents())
			{
				if (item != null && item.getType() == Material.BANNER)
				{
					event.getPlayer().getInventory().remove(item);
				}
			}
		}, 20);
	}
	
	@EventHandler
	public void onPickupBanner(PlayerPickupItemEvent event)
	{
		if (event.getItem().getItemStack().getType() == Material.BANNER)
		{
			event.setCancelled(true);
			event.getItem().remove();
		}
	}
	
	@EventHandler
	public void onPickupBanner(PlayerDropItemEvent event)
	{
		if (event.getItemDrop().getItemStack().getType() == Material.BANNER)
		{
			event.getItemDrop().remove();
		}
	}
}
