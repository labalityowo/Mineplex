package nautilus.game.arcade.game.games.cakewars.shop;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.Managers;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.particles.ColoredParticle;
import mineplex.core.common.util.particles.DustSpellColor;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.newnpc.NPC;
import mineplex.core.newnpc.NewNPCManager;
import mineplex.core.newnpc.SimpleNPC;
import mineplex.core.newnpc.event.NPCInteractEvent;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.cakewars.CakeModule;
import nautilus.game.arcade.game.games.cakewars.CakeWars;
import nautilus.game.arcade.game.games.cakewars.ui.CakeResourcePage;
import nautilus.game.arcade.game.games.cakewars.ui.CakeResourceShop;

public class CakeShopModule extends CakeModule
{

	private static final int MIN_BLOCK_PLACE_DIST_SQUARED = 9;
	public static final ItemStack ENDER_PEARL = new ItemBuilder(Material.ENDER_PEARL)
			.setTitle(C.cPurpleB + "Ender Pearl")
			.addLore("", "Warning! Ender Pearls have a", C.cRed + "7 second" + C.cGray + " cooldown between uses.")
			.build();
	static int getHealingStationRadius(int level)
	{
		return 5 + (3 * (level - 1));
	}

	private final NewNPCManager _manager;
	private final Map<NPC, CakeResource> _npcs;
	private final CakeResourceShop _shop;
	private final Map<CakeResource, List<CakeItem>> _items;
	private final Map<UUID, Set<CakeItem>> _ownedItems;
	private final Map<GameTeam, Set<CakeItem>> _ownedTeamItems;

	public CakeShopModule(CakeWars game)
	{
		super(game);

		_manager = Managers.require(NewNPCManager.class);
		_npcs = new HashMap<>();
		_shop = new CakeResourceShop(game.getArcadeManager());
		_items = new HashMap<>(CakeResource.values().length);
		_ownedItems = new HashMap<>();
		_ownedTeamItems = new HashMap<>(8);

		_items.put(CakeResource.BRICK, game.generateItems(CakeResource.BRICK));
		_items.put(CakeResource.EMERALD, game.generateItems(CakeResource.EMERALD));
		_items.put(CakeResource.STAR, game.generateItems(CakeResource.STAR));
	}

	@Override
	public void cleanup()
	{
		_npcs.keySet().forEach(_manager::deleteNPC);
		_npcs.clear();
		_items.clear();
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void prepare(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Prepare)
		{
			return;
		}

		_game.CreatureAllowOverride = true;

		_game.getCakeTeamModule().getCakeTeams().forEach((team, cakeTeam) ->
		{
			String teamName = team.GetName().toUpperCase();

			for (CakeResource resource : CakeResource.values())
			{
				Location location = _game.WorldData.GetCustomLocs("SHOP " + teamName + " " + resource.name()).get(0);
				location.setYaw(UtilAlg.GetYaw(UtilAlg.getTrajectory2d(location, cakeTeam.getShop())));
				NPC npc = SimpleNPC.of(location, Villager.class, "CAKE_NPC");

				_manager.addNPC(npc);

				MapUtil.QuickChangeBlockAt(location, Material.BARRIER);
				MapUtil.QuickChangeBlockAt(location.clone().add(0, 1, 0), Material.BARRIER);

				_npcs.put(npc, resource);

				LivingEntity entity = npc.getEntity();

				entity.setCustomName(resource.getChatColor() + C.Bold + resource.getName() + " Shop");
				entity.setCustomNameVisible(true);
			}

			_ownedTeamItems.put(team, new HashSet<>());
		});

		_game.CreatureAllowOverride = false;
	}

	@EventHandler
	public void npcInteract(PlayerInteractEvent event)
	{
		if (!_game.IsLive())
		{
			return;
		}

		Block block = event.getClickedBlock();
		Player player = event.getPlayer();

		if (block == null || block.getType() != Material.BARRIER)
		{
			return;
		}

		event.setCancelled(true);
		openShop(player, event.getAction(), block.getLocation());
	}

	// Why do we need this? 1.8 clients sometimes ignore the block, and instead say they hit the entity.
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void entityInteract(NPCInteractEvent event)
	{
		openShop(event.getPlayer(), event.isLeftClick() ? Action.LEFT_CLICK_BLOCK : Action.RIGHT_CLICK_BLOCK, event.getNpc().getEntity().getLocation());
	}

	private void openShop(Player player, Action action, Location location)
	{
		for (Entry<NPC, CakeResource> entry : _npcs.entrySet())
		{
			Location npcLocation = entry.getKey().getEntity().getLocation();

			if (location.getBlockX() != npcLocation.getBlockX() || location.getBlockZ() != npcLocation.getBlockZ())
			{
				continue;
			}

			if (UtilPlayer.isSpectator(player) || (action == Action.LEFT_CLICK_BLOCK && UtilItem.isSword(player.getItemInHand())) || !Recharge.Instance.use(player, "Interact Shop", 500, false, false))
			{
				return;
			}

			CakeResourcePage page = _game.getShopPage(entry.getValue(), player);

			page.refresh();
			_shop.openPageForPlayer(player, page);
			return;
		}
	}

	@EventHandler
	public void blockPlace(BlockPlaceEvent event)
	{
		if (!_game.IsLive() || UtilPlayer.isSpectator(event.getPlayer()))
		{
			return;
		}

		Location location = event.getBlock().getLocation();

		if (isNearShop(location))
		{
			event.setCancelled(true);
			event.getPlayer().sendMessage(F.main("Game", "You cannot place blocks that close to the Shop."));
		}
	}

	@EventHandler
	public void updatePassiveUpgrades(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC || !_game.IsLive())
		{
			return;
		}

		_game.getCakeTeamModule().getCakeTeams().forEach((team, cakeTeam) ->
		{
			List<Player> alive = team.GetPlayers(true);

			cakeTeam.getUpgrades().forEach((item, level) ->
			{
				if (_game.getCakeTeamModule().hasCakeRot() && item == CakeNetherItem.REGENERATION)
				{
					return;
				}

				if (level > 0)
				{
					alive.forEach(player -> item.apply(player, level, cakeTeam.getCake()));
				}
			});
		});
	}

	@EventHandler
	public void updateHealingParticles(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER || !_game.IsLive())
		{
			return;
		}

		_game.getCakeTeamModule().getCakeTeams().forEach((team, cakeTeam) ->
		{
			int level = cakeTeam.getUpgrades().getOrDefault(CakeNetherItem.REGENERATION, 0);

			if (level == 0 || !cakeTeam.canRespawn())
			{
				return;
			}

			Location location = cakeTeam.getCake().clone();
			DustSpellColor color = new DustSpellColor(team.GetColorBase());
			int radius = getHealingStationRadius(level);
			double deltaTheta = Math.PI / (27 - (7 * level));

			for (double theta = 0; theta < 2 * Math.PI; theta += deltaTheta)
			{
				double x = radius * Math.cos(theta), z = radius * Math.sin(theta);

				location.add(x, 0, z);

				new ColoredParticle(ParticleType.RED_DUST, color, location)
						.display();

				location.subtract(x, 0, z);
			}
		});
	}

	@EventHandler
	public void playerDeath(PlayerDeathEvent event)
	{
		onDeath(event.getEntity());
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		onDeath(event.getPlayer());
	}

	private void onDeath(Player player)
	{
		Set<CakeItem> items = getOwnedItems(player);

		if (!_game.getCakePlayerModule().isUsingRuneOfHolding(player))
		{
			items.removeIf(item -> !item.getItemType().isOnePerTeam());
		}
	}

	public boolean ownsItem(Player player, CakeItem item)
	{
		return getOwnedItems(player).contains(item);
	}

	public boolean ownsItem(GameTeam team, CakeItem item)
	{
		return _ownedTeamItems.get(team).contains(item);
	}

	public Set<CakeItem> getOwnedItems(Player player)
	{
		return _ownedItems.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>());
	}

	public Set<CakeItem> getOwnedItems(GameTeam team)
	{
		return _ownedTeamItems.get(team);
	}

	public boolean isNearShop(Location location)
	{
		for (NPC npc : _npcs.keySet())
		{
			if (UtilMath.offsetSquared(location, npc.getEntity().getLocation()) < MIN_BLOCK_PLACE_DIST_SQUARED)
			{
				return true;
			}
		}

		return false;
	}

	public CakeResourceShop getShop()
	{
		return _shop;
	}

	public Map<CakeResource, List<CakeItem>> getItems()
	{
		return _items;
	}
}
