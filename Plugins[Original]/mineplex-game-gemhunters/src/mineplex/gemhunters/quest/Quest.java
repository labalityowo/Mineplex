package mineplex.gemhunters.quest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import mineplex.core.Managers;
import mineplex.core.account.CoreClientManager;
import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilServer;
import mineplex.core.donation.DonationManager;
import mineplex.core.inventory.InventoryManager;
import mineplex.gemhunters.economy.EconomyModule;
import mineplex.gemhunters.world.WorldDataModule;

public abstract class Quest implements Listener
{

	private final int _id;
	private final String _name;
	private final String _description;
	private final int _startCost;
	private final int _completeReward;

	protected final QuestModule _quest;
	protected final CoreClientManager _clientManager;
	protected final DonationManager _donation;
	protected final EconomyModule _economy;
	protected final InventoryManager _inventory;
	protected final WorldDataModule _worldData;

	private final Map<UUID, Integer> _counter;

	public Quest(int id, String name, String description, int startCost, int completeReward)
	{
		_id = id;
		_name = name;
		_description = description;
		_startCost = startCost;
		_completeReward = completeReward;

		_quest = Managers.require(QuestModule.class);
		_clientManager = Managers.require(CoreClientManager.class);
		_donation = Managers.require(DonationManager.class);
		_economy = Managers.require(EconomyModule.class);
		_inventory = Managers.require(InventoryManager.class);
		_worldData = Managers.require(WorldDataModule.class);

		_counter = new HashMap<>();

		UtilServer.RegisterEvents(this);
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		remove(event.getPlayer());
	}

	public void transfer(Player from, Player to)
	{
		// If the player has already been progressing this quest and is
		// further than the other don't bother transferring their data.
		if (get(to) >= get(from))
		{
			return;
		}

		set(to, get(from));
	}

	public void set(Player player, int amount)
	{
		_counter.put(player.getUniqueId(), amount);
	}

	public int get(Player player)
	{
		return _counter.getOrDefault(player.getUniqueId(), 0);
	}

	public int getAndIncrement(Player player, int amount)
	{
		int newAmount = get(player) + amount;
		_counter.put(player.getUniqueId(), newAmount);
		_quest.updateQuestItem(this, player);

		return newAmount;
	}

	public void remove(Player player)
	{
		_counter.remove(player.getUniqueId());
	}

	public void onStart(Player player)
	{
	}

	public void onReward(Player player)
	{
		if (_completeReward > 0)
		{
			_economy.addToStore(player, "Completing " + F.elem(_name), _completeReward);
		}
		
		remove(player);
		_quest.completeQuest(this, player);
	}

	public boolean isActive(Player player)
	{
		return _quest.isActive(this, player);
	}

	public float getProgress(Player player)
	{
		return 0;
	}

	public int getGoal()
	{
		return 1;
	}

	public final int getId()
	{
		return _id;
	}

	public final String getName()
	{
		return _name;
	}

	public final String getDescription()
	{
		return _description;
	}

	public final int getStartCost()
	{
		return _startCost;
	}

	public final int getCompleteReward()
	{
		return _completeReward;
	}
	
	public String getRewardString()
	{
		return F.currency(GlobalCurrency.GEM, _completeReward);
	}
}
