package mineplex.core.leaderboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.hologram.Hologram.HologramTarget;
import mineplex.core.recharge.Recharge;

public class DynamicLeaderboard extends LeaderboardDisplay implements PlayerActionHook
{

	private final List<StaticLeaderboard> _leaderboards;
	private final Map<Player, Integer> _viewingTime;

	public DynamicLeaderboard(LeaderboardManager manager, List<StaticLeaderboard> leaderboards)
	{
		super(manager);

		_leaderboards = leaderboards;
		_viewingTime = new HashMap<>();

		_leaderboards.forEach(leaderboard ->
		{
			leaderboard.getHologram().setHologramTarget(HologramTarget.WHITELIST);
			leaderboard.getHologram().setInteraction((player, clickType) -> rotateTime(player));
		});
	}

	private void rotateTime(Player player)
	{
		if (_leaderboards.size() == 1 || !Recharge.Instance.use(player, "Leaderboard Interact", 500, false, false))
		{
			return;
		}

		int newIndex = _viewingTime.get(player) + 1;

		if (newIndex == _leaderboards.size())
		{
			newIndex = 0;
		}

		setViewingTime(player, newIndex, true);
	}

	@Override
	public void onPlayerJoin(Player player)
	{
		setViewingTime(player, 0);
	}

	@Override
	public void onPlayerQuit(Player player)
	{
		Integer index = _viewingTime.remove(player);

		if (index != null)
		{
			_leaderboards.get(index).getHologram().removePlayer(player);
		}
	}

	public void setViewingTime(Player player)
	{
		setViewingTime(player, 0);
	}

	public void setViewingTime(Player player, int newIndex)
	{
		setViewingTime(player, newIndex, false);
	}

	public void setViewingTime(Player player, int newIndex, boolean inform)
	{
		Integer oldIndex = _viewingTime.get(player);

		if (oldIndex != null)
		{
			_leaderboards.get(oldIndex).getHologram().removePlayer(player);
		}

		if (newIndex == -1)
		{
			return;
		}

		StaticLeaderboard newLeaderboard = _leaderboards.get(newIndex);

		if (inform)
		{
			player.playSound(player.getLocation(), Sound.WOOD_CLICK, 1, 1);
			player.sendMessage(F.main(_manager.getName(), "You are now viewing " + F.name(newLeaderboard.getName()) + "."));
		}

		newLeaderboard.getHologram().addPlayer(player);
		_viewingTime.put(player, newIndex);
	}

	@Override
	public void register()
	{
	}

	@Override
	public void unregister()
	{
		_leaderboards.forEach(LeaderboardDisplay::unregister);
		_viewingTime.clear();
	}

	@Override
	public void update()
	{
		_leaderboards.forEach(leaderboard ->
		{
			leaderboard.update();

			if (_leaderboards.size() > 1)
			{
				String[] text = leaderboard.getHologram().getText();
				String[] newText = new String[text.length + 3];

				System.arraycopy(text, 0, newText, 0, text.length);
				newText[text.length] = C.blankLine;
				newText[text.length + 1] = C.cYellowB + "Click to view the";
				newText[text.length + 2] = C.cYellowB + "next category";

				leaderboard.getHologram().setText(newText);
			}
		});
	}

	@Override
	public List<Leaderboard> getDisplayedLeaderboards()
	{
		List<Leaderboard> leaderboards = new ArrayList<>();

		_leaderboards.forEach(display -> leaderboards.addAll(display.getDisplayedLeaderboards()));

		return leaderboards;
	}

	public List<StaticLeaderboard> getLeaderboards()
	{
		return _leaderboards;
	}
}
