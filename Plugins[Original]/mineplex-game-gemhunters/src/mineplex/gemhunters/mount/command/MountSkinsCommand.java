package mineplex.gemhunters.mount.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.gadget.gadgets.gamemodifiers.GameCosmeticCategory;
import mineplex.core.game.GameDisplay;
import mineplex.gemhunters.mount.MountModule;
import mineplex.gemhunters.mount.MountModule.Perm;

public class MountSkinsCommand extends CommandBase<MountModule>
{

	public MountSkinsCommand(MountModule plugin)
	{
		super(plugin, Perm.MOUNT_SKINS_COMMAND, "mount", "mounts", "skins", "mountskins");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		GameCosmeticCategory category = Plugin.getGadgetManager().getGameCosmeticManager().getCategoryFrom(GameDisplay.GemHunters, "Mount Skins");
		Plugin.getCosmeticManager().getShop().openPageForPlayer(caller, category.getGadgetPage(Plugin.getCosmeticManager(), caller));
	}
}
