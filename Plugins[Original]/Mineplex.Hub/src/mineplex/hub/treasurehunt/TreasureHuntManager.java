package mineplex.hub.treasurehunt;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;

import mineplex.core.MiniClientPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.ILoginProcessor;
import mineplex.core.common.util.F;
import mineplex.core.hologram.HologramManager;
import mineplex.core.inventory.InventoryManager;
import mineplex.core.titles.tracks.TrackManager;
import mineplex.core.world.MineplexWorld;
import mineplex.hub.HubManager;
import mineplex.hub.treasurehunt.types.NewHubTreasureHunt;

@ReflectivelyCreateMiniPlugin
public class TreasureHuntManager extends MiniClientPlugin<Set<Integer>>
{

	private final CoreClientManager _clientManager;
	private final HologramManager _hologramManager;
	private final InventoryManager _inventoryManager;
	private final TrackManager _trackManager;

	private final TreasureHuntRepository _repository;
	private TreasureHunt _treasureHunt;

	private TreasureHuntManager()
	{
		super("Treasure Hunt");

		_clientManager = require(CoreClientManager.class);
		_hologramManager = require(HologramManager.class);
		_inventoryManager = require(InventoryManager.class);
		_trackManager = require(TrackManager.class);

		_repository = new TreasureHuntRepository();

		setupTreasureHunt();
	}

	private void setupTreasureHunt()
	{
		MineplexWorld worldData = require(HubManager.class).getWorldData();
		Map<Block, Integer> treasure = new HashMap<>();
		_treasureHunt = new NewHubTreasureHunt(this, treasure);

		worldData.getSpongeLocations().forEach((key, locations) ->
		{
			if (!key.startsWith("TH"))
			{
				return;
			}

			Block block = locations.get(0).getBlock();
			int id = Integer.parseInt(key.split(" ")[1]);
			treasure.put(block, id);
			_treasureHunt.createTreasure(block, id);
		});

		_clientManager.addStoredProcedureLoginProcessor(new ILoginProcessor()
		{
			@Override
			public String getName()
			{
				return _moduleName;
			}

			@Override
			public void processLoginResultSet(String playerName, UUID uuid, int accountId, ResultSet resultSet) throws SQLException
			{
				Set<Integer> found = Get(uuid);

				while (resultSet.next())
				{
					found.add(resultSet.getInt("treasureId"));
				}
			}

			@Override
			public String getQuery(int accountId, String uuid, String name)
			{
				return "SELECT treasureId FROM accountTreasureHunt WHERE accountId=" + accountId + ";";
			}
		});
	}

	@Override
	protected Set<Integer> addPlayer(UUID uuid)
	{
		return new HashSet<>();
	}

	@EventHandler
	public void interactBlock(PlayerInteractEvent event)
	{
		Block block = event.getClickedBlock();

		if (block == null || _treasureHunt == null)
		{
			return;
		}

		Player player = event.getPlayer();
		Set<Integer> found = Get(player);

		for (Entry<Block, Integer> entry : _treasureHunt.getTreasure().entrySet())
		{
			Block treasureBlock = entry.getKey();
			int id = entry.getValue();

			if (!treasureBlock.equals(block))
			{
				continue;
			}

			if (!found.add(id))
			{
				player.sendMessage(F.main(_moduleName, "You've already found this treasure. You have found " + F.count(found.size()) + "/" + F.count(_treasureHunt.getTreasure().size()) + "."));
				continue;
			}

			player.sendMessage(F.main(_moduleName, "You found a piece of treasure! You have found " + F.count(found.size()) + "/" + F.count(_treasureHunt.getTreasure().size()) + "."));
			_treasureHunt.onTreasureFind(player, block, found);
			runAsync(() -> _repository.saveTreasure(_clientManager.getAccountId(player), id));
			return;
		}
	}

	public HologramManager getHologramManager()
	{
		return _hologramManager;
	}

	public InventoryManager getInventoryManager()
	{
		return _inventoryManager;
	}

	public TrackManager getTrackManager()
	{
		return _trackManager;
	}
}
