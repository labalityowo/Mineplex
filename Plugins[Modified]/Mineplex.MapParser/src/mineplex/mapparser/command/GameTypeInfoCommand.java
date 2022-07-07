package mineplex.mapparser.command;

import com.google.common.collect.Lists;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.mapparser.GameType;
import mineplex.mapparser.GameTypeInfo;
import mineplex.mapparser.MapParser;
import org.bukkit.entity.Player;

/**
 *
 */
public class GameTypeInfoCommand extends BaseCommand
{

	public GameTypeInfoCommand(MapParser plugin)
	{
		super(plugin, "gameinfo", "gametypeinfo");
		setUsage("/info <gametype> & /info addInfo <gameType> <info>");
	}

	@Override
	public boolean execute(Player player, String alias, String[] args)
	{
		if (args.length == 1)
		{
			String gameRaw = args[0];
			GameType gameType;
			try
			{
				gameType = GameType.match(gameRaw);
			} catch (Exception e)
			{
				player.sendMessage(C.cRed + "Invalid Game Type: " + gameRaw);
				return true;
			}
			GameTypeInfo info = getPlugin().getInfo(gameType);
			if (info == null)
			{
				player.sendMessage(C.cRed + "No info found for " + gameType.GetName());
				return true;
			}
			info.sendInfo(player);
			return true;
		}
		if (args.length >= 3 && args[0].equalsIgnoreCase("addInfo"))
		{
			String gameRaw = args[1];
			GameType gameType;
			try
			{
				gameType = GameType.match(gameRaw);
			} catch (Exception e)
			{
				player.sendMessage(C.cRed + "Invalid Game Type: " + gameRaw);
				return true;
			}
			GameTypeInfo info = getPlugin().getInfo(gameType);
			StringBuilder builder = new StringBuilder();
			for (int i = 2; i < args.length; i++)
			{
				builder.append(args[i]);
				if ((i + 1) != args.length)
				{
					builder.append(" ");
				}
			}
			if (info == null)
			{
				info = new GameTypeInfo(gameType, Lists.newArrayList());
				getPlugin().setInfo(gameType, info);
			}

			info.addInfo(builder.toString());
			player.sendMessage(C.cGray + "Added new info to " + F.elem(gameRaw));
			return true;
		}
		return false;
	}
}
