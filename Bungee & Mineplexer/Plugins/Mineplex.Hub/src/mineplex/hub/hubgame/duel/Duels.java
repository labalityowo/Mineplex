package mineplex.hub.hubgame.duel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.mission.MissionManager;
import mineplex.core.mission.MissionTrackerType;
import mineplex.hub.hubgame.CycledGame;
import mineplex.hub.hubgame.HubGameManager;
import mineplex.hub.hubgame.HubGameType;
import mineplex.hub.hubgame.common.damage.DamageComponent;
import mineplex.hub.hubgame.common.general.DoubleJumpComponent;
import mineplex.hub.hubgame.common.general.GameDescriptionComponent;
import mineplex.hub.hubgame.common.general.GameTimeoutComponent;
import mineplex.hub.hubgame.common.general.InventoryEditComponent;
import mineplex.hub.hubgame.common.general.MissionsComponent;
import mineplex.hub.hubgame.common.general.PlayerGameModeComponent;
import mineplex.hub.hubgame.common.general.PrepareFreezeComponent;
import mineplex.hub.hubgame.common.general.SingleWinnerComponent;
import mineplex.hub.hubgame.common.map.BlockRecorderComponent;
import mineplex.hub.hubgame.common.map.PreventNonAlivePlayersComponent;
import mineplex.hub.hubgame.common.map.TeleportIntoMapComponent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class Duels extends CycledGame
{

	private static final ItemStack[] ITEMS = {
			new ItemBuilder(Material.DIAMOND_SWORD)
					.setUnbreakable(true)
					.addEnchantment(Enchantment.DAMAGE_ALL, 2)
					.build(),
			new ItemBuilder(Material.BOW)
					.setUnbreakable(true)
					.addEnchantment(Enchantment.ARROW_DAMAGE, 2)
					.build(),
			new ItemBuilder(Material.FISHING_ROD)
					.setUnbreakable(true)
					.build(),
			new ItemStack(Material.GOLDEN_APPLE, 6),
			new ItemBuilder(Material.DIAMOND_AXE)
					.setUnbreakable(true)
					.build(),
			new ItemStack(Material.WOOD, 64),
			new ItemStack(Material.ARROW, 16),
	};
	private static final ItemStack[] ARMOUR = {
			new ItemBuilder(Material.IRON_BOOTS)
					.setUnbreakable(true)
					.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2)
					.build(),
			new ItemBuilder(Material.DIAMOND_LEGGINGS)
					.setUnbreakable(true)
					.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2)
					.build(),
			new ItemBuilder(Material.DIAMOND_CHESTPLATE)
					.setUnbreakable(true)
					.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2)
					.build(),
			new ItemBuilder(Material.DIAMOND_HELMET)
					.setUnbreakable(true)
					.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2)
					.build(),
	};

	private final List<Player> _notDamaged;
	private long _lastStart;

	public Duels(HubGameManager manager)
	{
		super(manager, HubGameType.DUELS);

		_notDamaged = new ArrayList<>(getGameType().getMaxPlayers());

		registerComponent(new DamageComponent(this));
		registerComponent(new GameDescriptionComponent(this, player ->
		{
			for (Player other : getAlivePlayers())
			{
				if (!player.equals(other))
				{
					return C.cWhiteB + "Opponent " + C.cRedB + other.getName();
				}
			}

			return null;
		}));
		registerComponent(new TeleportIntoMapComponent(this, _worldData.getIronLocations("YELLOW")));
		registerComponent(new PrepareFreezeComponent(this));
		registerComponent(new InventoryEditComponent(this));
		registerComponent(new DoubleJumpComponent(this));
		registerComponent(new GameTimeoutComponent(this, TimeUnit.MINUTES.toMillis(2)));
		registerComponent(new SingleWinnerComponent(this));
		registerComponent(new PlayerGameModeComponent(this, GameMode.SURVIVAL));
		registerComponent(new MissionsComponent(this));

		List<Location> corners = _worldData.getIronLocations("LIME");
		Location a = corners.get(0);
		Location b = corners.get(1);

		registerComponent(new BlockRecorderComponent(this, a, b));
		registerComponent(new PreventNonAlivePlayersComponent(this, a, b));
	}

	@Override
	public void onPrepare()
	{
		_lastStart = System.currentTimeMillis();
		_notDamaged.clear();

		for (Player player : getAlivePlayers())
		{
			player.getInventory().addItem(ITEMS);
			player.getInventory().setArmorContents(ARMOUR);
			_notDamaged.add(player);
		}
	}

	@Override
	public void onEnd()
	{
		MissionManager manager = getManager().getHubManager().getMissionManager();
		long since = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - _lastStart);

		for (Player player : getAlivePlayers())
		{
			manager.incrementProgress(player, 1, MissionTrackerType.LOBBY_GLD_QUICK, null, null, (int) since);

			if (_notDamaged.contains(player))
			{
				manager.incrementProgress(player, 1, MissionTrackerType.LOBBY_GLD_NO_DAMAGE, null, null);
			}
		}

		_notDamaged.clear();
	}

	@Override
	public boolean endCheck()
	{
		return getAlivePlayers().size() <= 1;
	}

	@EventHandler
	public void healthRegain(EntityRegainHealthEvent event)
	{
		if (event.getRegainReason() != RegainReason.SATIATED || !(event.getEntity() instanceof Player))
		{
			return;
		}

		Player player = (Player) event.getEntity();

		if (isAlive(player))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void damage(CustomDamageEvent event)
	{
		Player damagee = event.GetDamageePlayer();

		if (damagee == null)
		{
			return;
		}

		_notDamaged.remove(damagee);
	}
}
