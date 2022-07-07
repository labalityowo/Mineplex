package mineplex.hub.player;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.achievement.Achievement;
import mineplex.core.achievement.AchievementData;
import mineplex.core.achievement.AchievementManager;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.cosmetic.CosmeticManager;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.stats.event.PlayerStatsLoadedEvent;
import mineplex.core.titles.Titles;
import mineplex.hub.HubManager;

@ReflectivelyCreateMiniPlugin
public class HubPlayerManager extends MiniPlugin
{

	private static final ItemStack GAME_MENU = new ItemBuilder(Material.COMPASS)
			.setTitle(C.cGreen + "Quick Compass")
			.addLore("Click to open the Quick Compass.")
			.build();
	private static final ItemStack LOBBY_MENU = new ItemBuilder(Material.WATCH)
			.setTitle(C.cGreen + "Lobby Selector")
			.addLore("Click to open the Lobby Selector.")
			.build();

	private final AchievementManager _achievementManager;
	private final CosmeticManager _cosmeticManager;
	private final HubManager _hubManager;
	private final Titles _titles;

	private HubPlayerManager()
	{
		super("Hub Player");

		_achievementManager = require(AchievementManager.class);
		_cosmeticManager = require(CosmeticManager.class);
		_hubManager = require(HubManager.class);
		_titles = require(Titles.class);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void playerJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();

		event.setJoinMessage(null);

		player.teleport(_hubManager.GetSpawn());
		player.setHealth(player.getMaxHealth());
		player.setGameMode(GameMode.ADVENTURE);

		giveHotbar(player);
	}

	@EventHandler
	public void statsLoad(PlayerStatsLoadedEvent event)
	{
		Player player = event.getPlayer();

		AchievementData data = _achievementManager.get(player, Achievement.GLOBAL_MINEPLEX_LEVEL);
		float levelPercentage = (float) data.getExpRemainder() / data.getExpNextLevel();

		player.setLevel(data.getLevel());
		player.setExp(Math.max(0, Math.min(0.999F, levelPercentage)));
	}

	public void giveHotbar(Player player)
	{
		PlayerInventory inventory = player.getInventory();

		UtilPlayer.clearInventory(player);

		inventory.setItem(0, GAME_MENU);
		inventory.setItem(1, LOBBY_MENU);

		_cosmeticManager.giveInterfaceItem(player);

		_titles.giveBook(player, false);
	}
}
