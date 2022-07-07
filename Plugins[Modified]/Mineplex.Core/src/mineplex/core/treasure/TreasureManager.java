package mineplex.core.treasure;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.blockrestore.BlockRestore;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilServer;
import mineplex.core.disguise.DisguiseManager;
import mineplex.core.donation.DonationManager;
import mineplex.core.gadget.event.GadgetBlockEvent;
import mineplex.core.gadget.event.GadgetSelectLocationEvent;
import mineplex.core.inventory.InventoryManager;
import mineplex.core.recharge.Recharge;
import mineplex.core.treasure.reward.TreasureRewardManager;
import mineplex.core.treasure.types.AncientTreasure;
import mineplex.core.treasure.types.ChristmasTreasure;
import mineplex.core.treasure.types.FreedomTreasure;
import mineplex.core.treasure.types.GingerbreadTreasure;
import mineplex.core.treasure.types.HauntedTreasure;
import mineplex.core.treasure.types.IlluminatedTreasure;
import mineplex.core.treasure.types.LoveTreasure;
import mineplex.core.treasure.types.MOBATreasure;
import mineplex.core.treasure.types.MinestrikeTreasure;
import mineplex.core.treasure.types.MythicalTreasure;
import mineplex.core.treasure.types.OldTreasure;
import mineplex.core.treasure.types.OmegaTreasure;
import mineplex.core.treasure.types.SpringTreasure;
import mineplex.core.treasure.types.StPatricksTreasure;
import mineplex.core.treasure.types.ThankfulTreasure;
import mineplex.core.treasure.types.Treasure;
import mineplex.core.treasure.types.TrickOrTreatTreasure2016;
import mineplex.core.treasure.types.TrickOrTreatTreasure2017;

@ReflectivelyCreateMiniPlugin
public class TreasureManager extends MiniPlugin
{

	private final BlockRestore _blockRestore;
	private final CoreClientManager _clientManager;
	private final DisguiseManager _disguiseManager;
	private final DonationManager _donationManager;
	private final InventoryManager _inventoryManager;
	private final TreasureRewardManager _rewardManager;

	private final List<Treasure> _treasures;
	private final List<TreasureLocation> _treasureLocations;

	private TreasureManager()
	{
		super("Treasure");

		_blockRestore = require(BlockRestore.class);
		_clientManager = require(CoreClientManager.class);
		_disguiseManager = require(DisguiseManager.class);
		_donationManager = require(DonationManager.class);
		_inventoryManager = require(InventoryManager.class);
		_rewardManager = require(TreasureRewardManager.class);

		_treasures = new ArrayList<>();
		_treasureLocations = new ArrayList<>(5);

		populateTreasureTypes();
	}

	private void populateTreasureTypes()
	{
		addTreasureType(new OldTreasure());
		addTreasureType(new AncientTreasure());
		addTreasureType(new MythicalTreasure());
		addTreasureType(new IlluminatedTreasure());
		addTreasureType(new OmegaTreasure());
		addTreasureType(new MinestrikeTreasure());
		addTreasureType(new MOBATreasure());
		addTreasureType(new GingerbreadTreasure());
		addTreasureType(new TrickOrTreatTreasure2017());
		addTreasureType(new TrickOrTreatTreasure2016());
		addTreasureType(new ThankfulTreasure());
		addTreasureType(new StPatricksTreasure());
		addTreasureType(new HauntedTreasure());
		addTreasureType(new LoveTreasure());
		addTreasureType(new ChristmasTreasure());
		addTreasureType(new SpringTreasure());
		addTreasureType(new FreedomTreasure());
	}

	private void addTreasureType(Treasure treasure)
	{
		_treasures.add(treasure);
	}

	public void addTreasureLocation(Location location)
	{
		TreasureLocation treasureLocation = new TreasureLocation(this, location.subtract(0, 1, 0));
		UtilServer.RegisterEvents(treasureLocation);
		_treasureLocations.add(treasureLocation);
	}

	public TreasureLocation getOpenTreasureLocation()
	{
		return _treasureLocations.stream()
				.filter(treasureLocation -> !treasureLocation.inUse())
				.findAny()
				.orElse(null);
	}

	public boolean isOpeningTreasure(Player player)
	{
		return _treasureLocations.stream()
				.anyMatch(treasureLocation -> treasureLocation.inUse() && treasureLocation.getSession().getPlayer().equals(player));
	}

	public int getChestsToOpen(Player player)
	{
		int chests = 0;

		for (Treasure treasure : _treasures)
		{
			chests += getChestsToOpen(player, treasure);
		}

		return chests;
	}

	public int getChestsToOpen(Player player, Treasure treasure)
	{
		return _inventoryManager.Get(player).getItemCount(treasure.getTreasureType().getItemName());
	}

	public void purchase(Consumer<Integer> callback, Player player, Treasure treasure, int amount)
	{
		if (!Recharge.Instance.use(player, "Buy Treasure Chest", 1000, false, false))
		{
			return;
		}

		int ownedItems = _rewardManager.getOwnedItems(player, treasure);
		int totalItems = _rewardManager.getTotalItems(treasure);

		if (ownedItems == totalItems)
		{
			player.sendMessage(F.main(_moduleName, "Sorry, it seems that you already have all the items for this chest!"));
			return;
		}

		callback.accept(amount);
	}

	public void giveTreasure(Player player, Treasure treasure, int amount)
	{
		_inventoryManager.addItemToInventory(null, player, treasure.getTreasureType().getItemName(), amount);
	}

	@EventHandler
	public void gadgetSelectBlock(GadgetBlockEvent event)
	{
		event.getBlocks().removeIf(block -> isTooClose(block.getLocation()));
	}

	@EventHandler
	public void gadgetSelectLocation(GadgetSelectLocationEvent event)
	{
		if (isTooClose(event.getLocation()))
		{
			event.setCancelled(true);
		}
	}

	private boolean isTooClose(Location gadgetLocation)
	{
		for (TreasureLocation location : _treasureLocations)
		{
			if (UtilMath.offsetSquared(location.getChest(), gadgetLocation) < 25)
			{
				return true;
			}
		}

		return false;
	}

	public BlockRestore getBlockRestore()
	{
		return _blockRestore;
	}

	public CoreClientManager getClientManager()
	{
		return _clientManager;
	}

	public DisguiseManager getDisguiseManager()
	{
		return _disguiseManager;
	}

	public DonationManager getDonationManager()
	{
		return _donationManager;
	}

	public InventoryManager getInventoryManager()
	{
		return _inventoryManager;
	}

	public TreasureRewardManager getRewardManager()
	{
		return _rewardManager;
	}

	public List<Treasure> getTreasures()
	{
		return _treasures;
	}
}
