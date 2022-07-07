package nautilus.game.minekart.gp.command;

import org.bukkit.entity.Player;

import nautilus.game.minekart.gp.GPManager;
import nautilus.game.minekart.item.KartItemType;
import nautilus.game.minekart.kart.Kart;
import mineplex.core.command.CommandBase;
import mineplex.core.common.Rank;
import mineplex.core.common.util.UtilEnum;

public class ItemCommand extends CommandBase<GPManager>
{
	public ItemCommand(GPManager plugin)
	{
		super(plugin, Rank.MODERATOR, "item");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args.length < 1)
			return;

		Kart kart = Plugin.KartManager.GetKart(caller);
		
		KartItemType kartItem = UtilEnum.fromString(KartItemType.class, args[0]);
		
		if (kart != null)
			kart.SetItemStored(kartItem);
	}
}
