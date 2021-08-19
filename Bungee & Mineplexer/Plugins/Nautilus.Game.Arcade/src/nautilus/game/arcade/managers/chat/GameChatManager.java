package nautilus.game.arcade.managers.chat;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.chat.ChatChannel;
import mineplex.core.chat.event.FormatPlayerChatEvent;
import mineplex.core.chat.format.LevelFormatComponent;
import mineplex.core.chat.format.RankFormatComponent;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilServer;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.managers.GameHostManager;

public class GameChatManager implements Listener
{
	public enum Perm implements Permission
	{
		TEAM_SPY,
		SPEC_ALWAYS_SPEAK,
		SPEC_ALWAYS_HEAR,
	}

	private final ArcadeManager _manager;

	private final LinkedList<ChatStatData> _chatStats;

	public boolean TeamSpy = true;

	public GameChatManager(ArcadeManager manager)
	{
		_manager = manager;
		UtilServer.RegisterEvents(this);

		_chatStats = new LinkedList<>();
		manager.GetChat().setFormatComponents(
				new LevelFormatComponent(_manager.GetAchievement()),
				new RankFormatComponent(_manager.GetClients()),
				player ->
				{
					TextComponent component = new TextComponent(player.getName());
					component.setColor(_manager.GetColor(player).asBungee());
					return component;
				}
		);

		generatePermissions();
	}

	private void generatePermissions()
	{
		PermissionGroup.MOD.setPermission(Perm.TEAM_SPY, true, true);
		PermissionGroup.TRAINEE.setPermission(Perm.SPEC_ALWAYS_SPEAK, true, true);
		PermissionGroup.MOD.setPermission(Perm.SPEC_ALWAYS_HEAR, true, true);
	}

	@EventHandler(ignoreCancelled = true)
	public void playerChat(FormatPlayerChatEvent event)
	{
		ChatChannel chatChannel = event.getChatChannel();

		if (!chatChannel.isModerated())
		{
			return;
		}

		Game game = _manager.GetGame();

		if (game == null)
		{
			return;
		}

		event.getFormatComponents().clear();

		Player sender = event.getPlayer();
		GameTeam team = game.GetTeam(sender);

		if (team != null && !team.IsAlive(sender))
		{
			event.getFormatComponents().add(player ->
			{
				TextComponent component = new TextComponent("Dead");
				component.setColor(net.md_5.bungee.api.ChatColor.GRAY);
				return component;
			});
		}

		event.getFormatComponents().add(new LevelFormatComponent(_manager.GetAchievement()));

		GameHostManager hostManager = _manager.GetGameHostManager();
		String mpsName = null;

		if (hostManager.isHost(sender))
		{
			if (hostManager.isEventServer())
			{
				mpsName = "Event Host";
			}
			else if (hostManager.isCommunityServer())
			{
				mpsName = C.cDGreenB + "MCS Host " + C.Reset;
			}
			else
			{
				mpsName = C.cDGreenB + "MPS Host " + C.Reset;
			}
		}
		else if (hostManager.isAdmin(sender, false))
		{
			if (hostManager.isEventServer())
			{
				mpsName = C.cDGreenB + "Event Co-Host " + C.Reset;
			}
			else if (hostManager.isCommunityServer())
			{
				mpsName = C.cDGreenB + "MCS Co-Host " + C.Reset;
			}
			else
			{
				mpsName = C.cDGreenB + "MPS Co-Host " + C.Reset;
			}
		}

		if (mpsName == null)
		{
			event.getFormatComponents().add(new RankFormatComponent(_manager.GetClients()));
		}
		else
		{
			String finalRank = mpsName;

			event.getFormatComponents().add(player ->
			{
				TextComponent component = new TextComponent(finalRank);

				component.setColor(net.md_5.bungee.api.ChatColor.DARK_GREEN);
				component.setBold(true);

				return component;
			});
		}

		if (chatChannel == ChatChannel.TEAM && team != null && team.IsAlive(sender))
		{
			event.getRecipients().removeIf(receiver ->
			{
				if (TeamSpy && _manager.GetClients().Get(receiver).hasPermission(Perm.TEAM_SPY))
				{
					return false;
				}

				GameTeam receiverTeam = game.GetTeam(receiver);
				return receiverTeam == null || !receiverTeam.equals(team);
			});

			event.getFormatComponents().add(0, player ->
			{
				TextComponent component = new TextComponent("TEAM");
				component.setBold(true);
				return component;
			});
		}
		else
		{
			if (!game.ShowEveryoneSpecChat)
			{
				event.getRecipients().removeIf(receiver ->
				{
					if (_manager.IsAlive(sender) || _manager.GetClients().Get(sender).hasPermission(Perm.SPEC_ALWAYS_SPEAK))
					{
						return false;
					}

					return _manager.IsAlive(receiver) && !_manager.GetClients().Get(receiver).hasPermission(Perm.SPEC_ALWAYS_HEAR);
				});
			}
		}

		event.getFormatComponents().add(player ->
		{
			TextComponent component = new TextComponent(player.getName());

			component.setColor(_manager.GetColor(player).asBungee());
			component.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new ComponentBuilder(String.join("\n", buildHoverText(player)))
					.create()));

			return component;
		});
	}

	public void setGameChatStats(ChatStatData... stats)
	{
		_chatStats.clear();
		Collections.addAll(_chatStats, stats);
	}

	private List<String> buildHoverText(Player player)
	{
		Game game = _manager.GetGame();
		List<String> lines = new ArrayList<>();

		String rank = _manager.GetClients().Get(player).getPrimaryGroup().getDisplay(true, true, true, false);

		lines.add(rank + (rank.isEmpty() ? "" : " ") + _manager.GetColor(player) + player.getName() + "'s Stats");
		lines.add("");

		if (game == null || !game.GetStats().containsKey(player))
		{
			lines.add("No in-game stats available.");
		}
		else
		{
			Map<String, Integer> stats = game.GetStats().get(player);
			String gameName = game.GetName();

			_chatStats.forEach(statData ->
			{
				String display = (statData.getDisplay() == null ? statData.getStat() : statData.getDisplay());

				if (!statData.isValue())
				{
					lines.add(statData.getDisplay());
					return;
				}

				if (statData.getStat().equalsIgnoreCase("kit"))
				{
					lines.add(buildLine(display, game.GetKit(player).GetName()));
					return;
				}

				if (statData.getStat().equalsIgnoreCase("kdratio"))
				{
					int kills = stats.getOrDefault(gameName + ".Kills", 0);
					int deaths = stats.getOrDefault(gameName + ".Deaths", 0);

					lines.add(buildLine(display, String.valueOf(getRatio(kills, deaths, "##.##"))));
					return;
				}

				lines.add(buildLine(display, (stats.getOrDefault(gameName + "." + statData.getStat(), 0).toString())));
			});
		}

		return lines;
	}

	private String buildLine(String first, String second)
	{
		return C.cWhite + first + C.cGray + ": " + second;
	}

	private double getRatio(int var1, int var2, String format)
	{
		double ratio;

		if (var1 <= 0) ratio = 0d;
		else if (var2 <= 1) ratio = (double) var1;
		else ratio = ((double) var1 / var2);

		return Double.parseDouble(new DecimalFormat(format).format(ratio));
	}
}