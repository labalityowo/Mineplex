package mineplex.core.account.redis;

import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.serverdata.commands.CommandCallback;

public class ClearGroupsHandler implements CommandCallback<ClearGroups>
{
	private final CoreClientManager _clientManager;
	
	public ClearGroupsHandler(CoreClientManager clientManager)
	{
		_clientManager = clientManager;
	}
	
	@Override
	public void run(ClearGroups command)
	{
		_clientManager.runSync(() ->
		{
			Optional<? extends Player> opt = Bukkit.getOnlinePlayers().stream().filter(player -> _clientManager.getAccountId(player) == command.getAccountId()).findAny();
			
			if (opt.isPresent())
			{
				PermissionGroup group = PermissionGroup.PLAYER;
				_clientManager.Get(opt.get()).setPrimaryGroup(group);
				_clientManager.Get(opt.get()).getAdditionalGroups().clear();
				UtilPlayer.message(opt.get(), F.main(_clientManager.getName(), "Your ranks have been cleared!"));
			}
		});
	}
}