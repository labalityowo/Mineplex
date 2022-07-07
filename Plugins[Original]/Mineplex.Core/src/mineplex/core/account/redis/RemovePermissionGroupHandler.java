package mineplex.core.account.redis;

import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.serverdata.commands.CommandCallback;

public class RemovePermissionGroupHandler implements CommandCallback<RemovePermissionGroup>
{
	private final CoreClientManager _clientManager;
	
	public RemovePermissionGroupHandler(CoreClientManager clientManager)
	{
		_clientManager = clientManager;
	}
	
	@Override
	public void run(RemovePermissionGroup command)
	{
		_clientManager.runSync(() ->
		{
			Optional<Player> opt = Bukkit.getOnlinePlayers().stream().map(Player.class::cast).filter(player -> _clientManager.getAccountId(player) == command.getAccountId()).findAny();
			
			if (opt.isPresent())
			{
				PermissionGroup group = PermissionGroup.valueOf(command.getGroupIdentifier());
				_clientManager.Get(opt.get()).removeAdditionalGroup(group);
				UtilPlayer.message(opt.get(), F.main(_clientManager.getName(), "You have lost sub-rank " + F.elem(group.name().toLowerCase()) + "!"));
			}
		});
	}
}