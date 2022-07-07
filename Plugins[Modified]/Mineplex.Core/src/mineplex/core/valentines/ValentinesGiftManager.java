package mineplex.core.valentines;

import java.util.Iterator;
import java.util.LinkedList;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.common.util.Callback;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.inventory.InventoryManager;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.reward.RewardData;
import mineplex.core.reward.RewardType;
import mineplex.core.stats.StatsManager;
import mineplex.core.treasure.reward.TreasureRewardManager;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.valentines.event.AttemptGiftEvent;
import net.md_5.bungee.api.ChatColor;

public class ValentinesGiftManager extends MiniPlugin
{
	private final String ITEM_NAME = "Valentines Gift";
	private final Material ITEM_MATERIAL = Material.RED_ROSE;

	private CoreClientManager _clientManager;
	private TreasureRewardManager _rewardManager;
	private InventoryManager _inventoryManager;
	private GadgetManager _gadgetManager;
	private StatsManager _statsManager;

	private ValentinesGiftRepository _repository;
	private LinkedList<GiftEffect> _effects;

	public ValentinesGiftManager(JavaPlugin plugin, CoreClientManager clientManager, TreasureRewardManager rewardManager, InventoryManager inventoryManager, GadgetManager gadgetManager, StatsManager statsManager)
	{
		super("Valentines", plugin);

		_clientManager = clientManager;
		_rewardManager = rewardManager;
		_inventoryManager = inventoryManager;
		_gadgetManager = gadgetManager;
		_statsManager = statsManager;

		_repository = new ValentinesGiftRepository(plugin);
		_effects = new LinkedList<>();
	}

	@EventHandler
	public void onAttemptGift(AttemptGiftEvent event)
	{
		Player from = event.getFrom();
		Player to = event.getTo();

		final int fromId = _clientManager.getAccountId(from);
		final int toId = _clientManager.getAccountId(to);

		runAsync(() -> {
			final boolean complete = _repository.giveGift(fromId, toId);
//			final boolean complete = true;
			runSync(() -> giveGift(from, to, complete));
		});
	}

	/**
	 * I apologize for this mess. Love, Shaun
	 */
	private void giveGift(Player from, Player to, boolean success)
	{
//		if (success)
//		{
//			_inventoryManager.addItemToInventory(new Callback<Boolean>()
//			{
//				@Override
//				public void run(Boolean data)
//				{
//					if (data)
//					{
//						_rewardManager.nextReward(to, RewardPool.Type.VALENTINES_GIFT, null, false, RewardType.VALENTINES_GIFT, false).giveReward(RewardType.VALENTINES_GIFT, to, new Callback<RewardData>()
//						{
//							@Override
//							public void run(RewardData toData)
//							{
//								String toGift = ChatColor.stripColor(toData.getFriendlyName());
//
//								_rewardManager.nextReward(from, RewardPool.Type.VALENTINES_GIFT, null, false, RewardType.VALENTINES_GIFT, false).giveReward(RewardType.VALENTINES_GIFT, from, new Callback<RewardData>()
//								{
//									@Override
//									public void run(RewardData fromData)
//									{
//										String fromGift = ChatColor.stripColor(fromData.getFriendlyName());
//										_effects.add(new GiftEffect(from, to, fromGift, toGift, UtilAlg.getMidpoint(to.getLocation(), from.getLocation())));
//
//										incrementStat(from, to);
//									}
//								});
//							}
//						});
//
//						from.getInventory().setItem(_gadgetManager.getActiveItemSlot(), ItemStackFactory.Instance.CreateStack(ITEM_MATERIAL, (byte) 0, 1, F.item(_inventoryManager.Get(from).getItemCount(ITEM_NAME) + " " + ITEM_NAME)));
//					}
//					else
//					{
//						UtilPlayer.message(from, F.main("Gift", "Error giving gift! Please try again"));
//					}
//				}
//			}, from, ITEM_NAME, -1);
//		}
//		else
//		{
//			UtilPlayer.message(from, F.main("Gift", "Spread the love! You have already gifted " + F.name(to.getName())));
//		}
	}

	@EventHandler
	public void updateEffects(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		Iterator<GiftEffect> iterator = _effects.iterator();

		while (iterator.hasNext())
		{
			GiftEffect effect = iterator.next();
			if (effect.isFinished())
				iterator.remove();
			else
				effect.tick();
		}
	}

	private void incrementStat(Player from, Player to)
	{
		_statsManager.incrementStat(from, "Global.Valentines2016.GiftsGiven", 1);
		_statsManager.incrementStat(to, "Global.Valentines2016.GiftsReceived", 1);
	}

}
