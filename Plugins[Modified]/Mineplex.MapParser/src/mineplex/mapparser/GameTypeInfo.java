package mineplex.mapparser;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

/**
 *
 */
public class GameTypeInfo
{

	private final String LINE = C.cAqua + C.Bold + C.Strike + "========================================";

	private GameType _gameType;
	private List<String> _info;

	public GameTypeInfo(GameType gameType, List<String> info)
	{
		_gameType = gameType;
		_info = info;
	}

	public void addInfo(String info)
	{
		_info.add(info);
	}

	public void remove(int index)
	{
		_info.remove(index);
	}

	public List<String> getInfo()
	{
		return _info;
	}

	public void sendInfo(Player player)
	{
		player.sendMessage(LINE);
		player.sendMessage(" ");
		player.sendMessage(F.elem(_gameType.GetName()));
		player.sendMessage(" ");
		for(String s : _info)
		{
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', s));
		}
		player.sendMessage(" ");
		player.sendMessage(LINE);
	}

	public GameType getGameType()
	{
		return _gameType;
	}
}
