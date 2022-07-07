package mineplex.gemhunters.quest;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import mineplex.core.Managers;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilUI;
import mineplex.core.menu.Button;
import mineplex.core.menu.Menu;
import mineplex.gemhunters.economy.EconomyModule;

public class QuestUI extends Menu<QuestModule>
{

	private final EconomyModule _economy;
	
	public QuestUI(QuestModule plugin)
	{
		super("Quest Master", plugin);
		
		_economy = Managers.require(EconomyModule.class);
	}

	@Override
	protected Button[] setUp(Player player)
	{
		Button[] buttons = new Button[21];
		QuestPlayerData playerData = getPlugin().Get(player);

		int i = 0;
		int[] slots = UtilUI.getIndicesFor(playerData.getPossibleQuests().size(), 1);
		for (Integer id : playerData.getPossibleQuests())
		{
			Quest quest = getPlugin().getFromId(id);
			ItemStack itemStack = getPlugin().getItemStack(quest, player, true, UtilInv.hasSpace(player, 1), _economy.Get(player) >= quest.getStartCost());

			buttons[slots[i++]] = new QuestSelectButton(getPlugin(), itemStack, quest);
		}

		return buttons;
	}

	public class QuestSelectButton extends Button<QuestModule>
	{

		private final Quest _quest;

		public QuestSelectButton(QuestModule plugin, ItemStack itemStack, Quest quest)
		{
			super(itemStack, plugin);

			_quest = quest;
		}

		@Override
		public void onClick(Player player, ClickType clickType)
		{
			getPlugin().startQuest(_quest, player, true);
			resetAndUpdate();
		}

	}

}
