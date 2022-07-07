package mineplex.core.bonuses.gui;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import mineplex.core.bonuses.BonusManager;
import mineplex.core.bonuses.gui.buttons.RewardButton;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.gui.DisplayItem;
import mineplex.core.gui.SimpleGui;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.reward.Reward;
import mineplex.core.reward.RewardData;
import mineplex.core.shop.item.ShopItem;
import mineplex.core.treasure.reward.RewardRarity;
import mineplex.core.treasure.reward.TreasureRewardManager;
import mineplex.core.treasure.types.CarlTreasure;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class SpinGui extends SimpleGui
{

	private static final int HOPPER_SLOT = 4;
	private static final int CARL_SLOT = 22;
	private static final int[] LINE_NUMS = { /*-27, -18,*/ -9, 9/*, 18*/};
	private static final CarlTreasure TREASURE = new CarlTreasure();

	private RewardData _rewardData;
	private Reward _reward;
	private BonusManager _manager;
	private int _currentRewardIndex;
	private int _ticksThisSwap;
	private int _ticksPerSwap;
	private Reward[] _rewards;
	private boolean _stopped;
	private boolean _rewarded;
	private ArrayList<Integer> _ticks;
	private int _frame;
	private float _pitch;
	private int _stopSpinnerAt;

	public SpinGui(Plugin plugin, Player player, TreasureRewardManager rewardManager, BonusManager manager)
	{
		super(plugin, player, "Carl's Spinner", 27);

		_manager = manager;

		ShopItem carlItem = new ShopItem(Material.SKULL_ITEM, (byte) 4, "Carl's Spinner", new String[]{ChatColor.RESET + "Good Luck!"}, 1, false, false);

		setItem(HOPPER_SLOT, new DisplayItem(new ItemStack(Material.HOPPER)));
		//setItem(CARL_SLOT, new DisplayItem(carlItem));


		_ticks = new ArrayList<>();
		_frame = 0;
		_pitch = 1;


		_ticksPerSwap = 1;

		for (int i = 0; i < 40; i++)
			_ticks.add(1);

		for (int i = 0; i < 20; i++)
			_ticks.add(2);

		for (int i = 0; i < 10; i++)
			_ticks.add(4);

		for (int i = 0; i < 4; i++)
			_ticks.add(6);

		for (int i = 0; i < 3; i++)
			_ticks.add(8);

		if (Math.random() > 0.5)
		{
			_ticks.add(12);
		}

		_stopSpinnerAt = _ticks.size();

		//Create Rewards
		_rewards = new Reward[_stopSpinnerAt + 10];        //Adding 10, so theres items to the right still.
		for (int i = 0; i < _stopSpinnerAt + 10; i++)
		{
			if (i != _stopSpinnerAt + 4)
			{
				_rewards[i] = TREASURE.nextReward(player, true);
			}
			else
			{
				_rewards[i] = TREASURE.nextReward(player, false);
				_reward = _rewards[i];
			}
		}

		_reward.giveReward(player, data -> _rewardData = data);
	}

	private void tick()
	{

		if (_stopped)
			return;

		_ticksThisSwap++;

		// Swap
		if (_ticksThisSwap >= _ticksPerSwap)
		{
			_ticksThisSwap = 0;

			if (_pitch == 1)
				_pitch = (float) 1.5;
			else if (_pitch == 1.5)
				_pitch = 2;
			else if (_pitch == 2)
				_pitch = 1;

			getPlayer().playSound(getPlayer().getEyeLocation(), Sound.NOTE_PLING, 1, _pitch);

			_currentRewardIndex++;

			updateGui();

			// Slow
			_ticksPerSwap = _ticks.get(_currentRewardIndex - 1);

			if (_currentRewardIndex == _stopSpinnerAt)
				_stopped = true;
		}
	}

	public void updateGui()
	{
		for (int i = 0; i < 9; i++)
		{
			int index = _currentRewardIndex + i;

			int slot = 9 + i;
			RewardData data = _rewards[index].getFakeRewardData(getPlayer());
			setItem(slot, new RewardButton(data));

			// Glass Panes
			for (int j = 0; j < LINE_NUMS.length; j++)
			{
				int paneSlot = slot + LINE_NUMS[j];
				if (paneSlot == HOPPER_SLOT)
					continue;

				setItem(paneSlot, new DisplayItem(data.getRarity().getItemStack()));
			}
		}
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		tick();
		checkIfDone();
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void close(InventoryCloseEvent event)
	{
		if (_rewarded)
			return;

		if (event.getPlayer() != getPlayer())
			return;

		_manager.addPendingExplosion(getPlayer(), _reward);

		if (_reward.getRarity() == RewardRarity.RARE)
		{
			Bukkit.broadcastMessage(F.main("Treasure", F.name(event.getPlayer().getName()) + " won " + C.cPurple + "Rare " + _rewardData.getFriendlyName() + C.cGray + " from Carl's Spinner."));
		}
		else if (_reward.getRarity() == RewardRarity.LEGENDARY)
		{
			Bukkit.broadcastMessage(F.main("Treasure", F.name(event.getPlayer().getName()) + " won " + C.cGreen + "Legendary " + _rewardData.getFriendlyName() + C.cGray + " from Carl's Spinner."));
		}
		else if (_reward.getRarity() == RewardRarity.MYTHICAL)
		{
			Bukkit.broadcastMessage(F.main("Treasure", F.name(event.getPlayer().getName()) + " won " + C.cRed + "Mythical " + _rewardData.getFriendlyName() + C.cGray + " from Carl's Spinner."));
		}
		else
		{
			UtilPlayer.message(getPlayer(), F.main("Carl's Spinner", "You won " + _rewardData.getRarity().getColor() + _rewardData.getFriendlyName() + C.cGray + " from Carl's Spinner."));
		}
	}

	@EventHandler
	public void Glass(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		if (!_stopped)
			return;

		if (!_rewarded)
			return;

		if (_frame == 0)
		{
			setItem(CARL_SLOT, new DisplayItem(_rewardData.getRarity().getItemStack()));
			setItem(HOPPER_SLOT, new DisplayItem(_rewardData.getRarity().getItemStack()));
			_frame++;
		}
		else if (_frame < 5)
		{
			setItem(HOPPER_SLOT + _frame, new DisplayItem(_rewardData.getRarity().getItemStack()));
			setItem(HOPPER_SLOT - _frame, new DisplayItem(_rewardData.getRarity().getItemStack()));

			setItem(CARL_SLOT + _frame, new DisplayItem(_rewardData.getRarity().getItemStack()));
			setItem(CARL_SLOT - _frame, new DisplayItem(_rewardData.getRarity().getItemStack()));

			setItem(13 + _frame, new DisplayItem(_rewardData.getRarity().getItemStack()));
			setItem(13 - _frame, new DisplayItem(_rewardData.getRarity().getItemStack()));
			_frame++;
		}
		if (_frame == 6)
		{

		}
	}

	public void checkIfDone()
	{
		if (!_stopped)
			return;

		if (_rewarded)
			return;

		_manager.addPendingExplosion(getPlayer(), _reward);
		if (_reward.getRarity() == RewardRarity.RARE)
		{
			Bukkit.broadcastMessage(F.main("Treasure", F.name(getPlayer().getName()) + " won " + C.cPurple + "Rare " + _rewardData.getFriendlyName() + C.cGray + " from Carl's Spinner."));
		}
		else if (_reward.getRarity() == RewardRarity.LEGENDARY)
		{
			Bukkit.broadcastMessage(F.main("Treasure", F.name(getPlayer().getName()) + " won " + C.cGreen + "Legendary " + _rewardData.getFriendlyName() + C.cGray + " from Carl's Spinner."));
		}
		else if (_reward.getRarity() == RewardRarity.MYTHICAL)
		{
			Bukkit.broadcastMessage(F.main("Treasure", F.name(getPlayer().getName()) + " won " + C.cRed + "Mythical " + _rewardData.getFriendlyName() + C.cGray + " from Carl's Spinner."));
		}
		else
		{
			UtilPlayer.message(getPlayer(), F.main("Carl's Spinner", "You won " + _rewardData.getRarity().getColor() + _rewardData.getFriendlyName() + C.cGray + " from Carl's Spinner."));
		}
		ItemStack item = getInventory().getItem(13);
		getInventory().setItem(13, ItemStackFactory.Instance.CreateStack(item.getType(), (byte) 0, 1, _rewardData.getFriendlyName()));
		_rewarded = true;

	}

}
