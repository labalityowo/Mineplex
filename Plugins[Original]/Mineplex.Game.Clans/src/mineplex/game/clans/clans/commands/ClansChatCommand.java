package mineplex.game.clans.clans.commands;

import org.bukkit.entity.Player;

import mineplex.core.Managers;
import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.punish.Punish;
import mineplex.core.punish.PunishClient;
import mineplex.game.clans.clans.ClanInfo;
import mineplex.game.clans.clans.ClansManager;

public class ClansChatCommand extends CommandBase<ClansManager>
{	
	public ClansChatCommand(ClansManager plugin)
	{
		super(plugin, ClansManager.Perm.CLAN_CHAT_COMMAND, "cc", "fc");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args == null || args.length == 0)
		{
			ClanInfo clan = Plugin.getClanUtility().getClanByPlayer(caller);
			if (clan == null)
			{
				UtilPlayer.message(caller, F.main("Clans", "You are not in a Clan."));
			}
			else
			{
				Plugin.Get(caller).setClanChat(!Plugin.Get(caller).isClanChat());
				UtilPlayer.message(caller, F.main("Clans", "Clan Chat: " + F.oo(Plugin.Get(caller).isClanChat())));
			}
			return;
		}

		//Single Clan
		if (!Plugin.Get(caller).isClanChat())
		{
			ClanInfo clan = Plugin.getClanUtility().getClanByPlayer(caller);
			if (clan == null)
				UtilPlayer.message(caller, F.main("Clans", "You are not in a Clan."));
			else
			{
				PunishClient punishClient = Managers.get(Punish.class).GetClient(caller.getName());
				if (punishClient != null && punishClient.IsMuted())
				{
					UtilPlayer.message(caller, F.main("Clans", "You cannot do this while muted!"));
					return;
				}
				Plugin.chatClan(clan, caller, F.combine(args, 0, null, false));
			}
		}
	}
}