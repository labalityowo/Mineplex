package mineplex.core.bonuses.commands;

import org.bukkit.entity.Player;

import mineplex.core.Managers;
import mineplex.core.bonuses.BonusManager;
import mineplex.core.bonuses.gui.BonusGui;
import mineplex.core.command.CommandBase;
import mineplex.core.treasure.reward.TreasureRewardManager;

public class GuiCommand extends CommandBase<BonusManager>
{
	public GuiCommand(BonusManager plugin)
	{
		super(plugin, BonusManager.Perm.GUI_COMMAND, "bonus");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		new BonusGui(Plugin.getPlugin(), caller, Plugin, Managers.require(TreasureRewardManager.class), Plugin.getYoutubeManager(), Plugin.getThankManager()).openInventory();
	}	
}