package mineplex.core.account.redis;

import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.serverdata.commands.CommandCallback;

public class AddPermissionGroupHandler implements CommandCallback<AddPermissionGroup>
{
	private final CoreClientManager _clientManager;
	
	public AddPermissionGroupHandler(CoreClientManager clientManager)
	{
		_clientManager = clientManager;
	}
	
	@Override
	public void run(AddPermissionGroup command)
	{
		_clientManager.runSync(() ->
		{
			Optional<Player> opt = Bukkit.getOnlinePlayers().stream().map(Player.class::cast).filter(player -> _clientManager.getAccountId(player) == command.getAccountId()).findAny();
			
			if (opt.isPresent())
			{
				PermissionGroup group = PermissionGroup.valueOf(command.getGroupIdentifier());
				_clientManager.Get(opt.get()).addAdditionalGroup(group);
				UtilPlayer.message(opt.get(), F.main(_clientManager.getName(), "You have gained sub-rank " + F.elem(group.name().toLowerCase()) + "!"));
			}
		});
	}
}