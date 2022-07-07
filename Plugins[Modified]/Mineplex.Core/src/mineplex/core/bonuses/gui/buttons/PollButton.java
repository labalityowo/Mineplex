package mineplex.core.bonuses.gui.buttons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import mineplex.core.account.CoreClientManager;
import mineplex.core.bonuses.BonusAmount;
import mineplex.core.bonuses.BonusManager;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilText;
import mineplex.core.gui.GuiInventory;
import mineplex.core.gui.GuiItem;
import mineplex.core.gui.SimpleGui;
import mineplex.core.gui.SimpleGuiItem;
import mineplex.core.gui.botton.BackBotton;
import mineplex.core.gui.pages.TimedMessageWindow;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.poll.Poll;
import mineplex.core.poll.PollManager;
import mineplex.core.recharge.Recharge;

public class PollButton extends SimpleGui implements GuiItem {
	
	protected boolean _create;
	
	private PollManager _pollManager;
	private BonusManager _bonusManager;
	private CoreClientManager _clientManager;
	private GuiInventory _master;

	private HashMap<Integer, GuiItem> hard = new HashMap<Integer, GuiItem>();

	private Poll _poll;

	public PollButton(Plugin plugin, Player player, PollManager pollManager, CoreClientManager clientManager, GuiInventory master, BonusManager bonusManager)
	{
		super(plugin, player, "Poll:", 6 * 9);
		this._create = true;
		this._master = master;
		this._clientManager = clientManager;
		this._pollManager = pollManager;
		hard.put(0, new BackBotton(master));
		_bonusManager = bonusManager;
	}
	
	@Override
	public void setup()
	{
		if (_create)
		{
			_poll = _pollManager.getNextPoll(_pollManager.Get(getPlayer()), _clientManager.Get(getPlayer()).getPrimaryGroup());
			
			if (_poll != null)
			{
			
				setItem(13, getQuestionItem(_poll.getQuestion()));

				int[] slots = even(9, _poll.getAnswers().length);

				for (int i = 0; i < slots.length; i++)
				{
					AnswerItem item = new AnswerItem(_poll, i);
					setItem(9 * 3 + slots[i], item);
				}
			}
		}
	}
	
	@Override
	public ItemStack getObject()
	{
		List<String> lore = new ArrayList<>();
		if (_poll == null)
		{
			lore.add("");
			lore.add(C.cWhite + "You've voted on all of the polls!");
			return ItemStackFactory.Instance.CreateStack(Material.REDSTONE_BLOCK, (byte) 0, 1, ChatColor.RED + C.Bold + "Vote on Poll", lore);
		}
		else
		{
			lore.add("");
			lore.add(C.cWhite + _poll.getQuestion());
			lore.add("");
			int i = 1;
			for(String str : _poll.getAnswers())
			{
				lore.add(C.cAqua + "" + i + ".) " + C.cWhite + str);
				i++;
			}
			lore.add("");
			BonusAmount amount = new BonusAmount();
			amount.setShards(_poll.getCoinReward());
			amount.setGems(_poll.getCoinReward());
			amount.addLore(lore);
//			lore.add(C.cYellow + "Reward:" + C.cWhite + " 500 Gems");
			lore.add("");
			lore.add(C.cGreen + "Click to go to the vote page!");
			return ItemStackFactory.Instance.CreateStack(Material.BOOK_AND_QUILL, (byte) 0, 1, C.cGreen + C.Bold + "Vote on Poll", lore);
		}
	}

	@Override
	public void click(ClickType clickType)
	{
		if (!Recharge.Instance.use(getPlayer(), "Poll Main Button", 1000, false, false))
		{
			return;
		}
		
		if (_poll == null)
		{
			getPlayer().playSound(getPlayer().getLocation(), Sound.ITEM_BREAK, 1, 1.6f);
		}
		else
		{
			getPlayer().playSound(getPlayer().getLocation(), Sound.NOTE_PLING, 1, 1.6f);
			openInventory();
		}
	}

	public GuiItem getQuestionItem(String question)
	{
		List<String> lore = new ArrayList<>();
		lore.add("");
		for (String string : wrap(question))
		{
			lore.add(C.cWhite + string);
		}
		lore.add("");
		int i = 1;
		for (String str : _poll.getAnswers())
		{
			lore.add(C.cAqua + "" + i + ".) " + C.cWhite + str);
			i++;
		}
		lore.add("");
		BonusAmount amount = new BonusAmount();
		amount.setShards(_poll.getCoinReward());
		amount.setGems(_poll.getCoinReward());
		amount.addLore(lore);
		
		return new SimpleGuiItem(ItemStackFactory.Instance.CreateStack(Material.BOOK_AND_QUILL, (byte) 0, 1, ChatColor.GREEN + C.cGreen + C.Bold + "Vote on Poll",
				lore));
	}

	public static String[] wrap(String text)
	{
		return UtilText.wrap(text, 40);
	}

	public static int[] even(int size, int amount)
	{
		int[] list = new int[amount];

		float interval = (size / amount);
		float offset = -(interval / 2);

		for (int i = 1; i <= amount; i++)
		{
			list[i - 1] = (int) (Math.ceil(i * interval) + offset);
		}
		return list;
	}

	public Poll getPoll()
	{
		return _poll;
	}

	public GuiInventory getMaster()
	{
		return _master;
	}

	private class AnswerItem implements GuiItem
	{
		private int _num;

		private AnswerItem(Poll poll, int num)
		{
			_num = num;
		}

		@Override
		public ItemStack getObject()
		{
			List<String> lore = new ArrayList<>();
			lore.add("");
			lore.add(C.cWhite + getPoll().getAnswers()[_num]);
			return ItemStackFactory.Instance.CreateStack(Material.EMERALD, (byte) 0, 1, ChatColor.GREEN + "Option " + (_num + 1), lore);
		}

		@Override
		public void click(ClickType clickType)
		{
			if (!Recharge.Instance.use(getPlayer(), "Poll Answer Button", 1000, false, false))
			{
				return;
			}
			
			_create = true;
			
			_pollManager.answerPoll(getPlayer(), _poll, _num + 1);
		
			getPlayer().playSound(getPlayer().getLocation(), Sound.NOTE_PLING, 1, 1.6f);
		
			new TimedMessageWindow(getPlugin(), getPlayer(), ItemStackFactory.Instance.CreateStack(Material.EMERALD_BLOCK, (byte) 0, 1, ChatColor.GREEN + "Your anwser:", wrap(getPoll().getAnswers()[_num])), ChatColor.GREEN + "Moo", 6 * 9, 50, getMaster()).openInventory();
			_bonusManager.addPendingExplosion(getPlayer(), "POLL");
			getPlayer().closeInventory();
		}

		@Override
		public void setup() {}

		@Override
		public void close() {}

	}

	@Override
	public void close() {}
}