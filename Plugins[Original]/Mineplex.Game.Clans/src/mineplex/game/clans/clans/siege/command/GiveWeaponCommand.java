package mineplex.game.clans.clans.siege.command;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilPlayer;
import mineplex.game.clans.clans.siege.SiegeManager;
import mineplex.game.clans.clans.siege.weapon.Cannon;

public class GiveWeaponCommand extends CommandBase<SiegeManager>
{
	public GiveWeaponCommand(SiegeManager plugin)
	{
		super(plugin, SiegeManager.Perm.GIVE_CANNON_COMMAND, "giveweapon", "siegeweapon", "givecannon");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		UtilInv.insert(caller, Cannon.CANNON_ITEM.clone());
		UtilPlayer.message(caller, F.main(Plugin.getName(), "You have been given a cannon!"));
	}
}