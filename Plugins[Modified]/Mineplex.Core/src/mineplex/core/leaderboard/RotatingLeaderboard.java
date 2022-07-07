package mineplex.core.leaderboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import mineplex.core.common.Pair;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.hologram.Hologram;
import mineplex.core.recharge.Recharge;

public class RotatingLeaderboard extends LeaderboardDisplay implements PlayerActionHook
{

	private final Map<Player, Integer> _viewingGroup;
	private final List<Pair<String, List<DynamicLeaderboard>>> _leaderboards;
	private final Hologram _rotator;

	public RotatingLeaderboard(LeaderboardManager manager, Location rotatorLocation)
	{
		super(manager);

		_viewingGroup = new HashMap<>();
		_leaderboards = new ArrayList<>(3);
		_rotator = new Hologram(manager.getHologramManager(), rotatorLocation, C.cGreenB + "Click to view the", C.cGreenB + "next mode!")
				.setInteraction((player, clickType) -> rotateGroup(player));
	}

	public RotatingLeaderboard addMode(String modeName, List<DynamicLeaderboard> leaderboards)
	{
		_leaderboards.add(Pair.create(modeName, leaderboards));
		return this;
	}

	public void rotateGroup(Player player)
	{
		if (!Recharge.Instance.use(player, "Leaderboard Interact", 500, false, false))
		{
			return;
		}

		int newIndex = _viewingGroup.get(player) + 1;

		if (newIndex == _leaderboards.size())
		{
			newIndex = 0;
		}

		setViewingGroup(player, newIndex);
	}

	@Override
	public void onPlayerJoin(Player player)
	{
		_viewingGroup.put(player, 0);
		_leaderboards.get(0).getRight().forEach(leaderboard -> leaderboard.onPlayerJoin(player));
	}

	@Override
	public void onPlayerQuit(Player player)
	{
		_viewingGroup.remove(player);
		_leaderboards.forEach(pair -> pair.getRight().forEach(leaderboard -> leaderboard.onPlayerQuit(player)));
	}

	private void setViewingGroup(Player player, int newIndex)
	{
		Integer oldIndex = _viewingGroup.put(player, newIndex);
		Pair<String, List<DynamicLeaderboard>> newPair = _leaderboards.get(newIndex);
		List<DynamicLeaderboard> newLeaderboards = newPair.getRight();

		if (oldIndex != null)
		{
			_leaderboards.get(oldIndex).getRight().forEach(leaderboard -> leaderboard.setViewingTime(player, -1));
			player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
			player.sendMessage(F.main(_manager.getName(), "You are now viewing " + F.name(newPair.getLeft()) + "."));
		}

		newLeaderboards.forEach(leaderboard -> leaderboard.setViewingTime(player));
	}

	@Override
	public void register()
	{
		_rotator.start();
	}

	@Override
	public void unregister()
	{
		_rotator.stop();
		_leaderboards.forEach(pair -> pair.getRight().forEach(LeaderboardDisplay::unregister));
		_leaderboards.clear();
		_viewingGroup.clear();
	}

	@Override
	public void update()
	{
		_leaderboards.forEach(pair -> pair.getRight().forEach(leaderboard ->
		{
			leaderboard.update();
			leaderboard.getLeaderboards().forEach(staticLeaderboard ->
			{
				String[] newText = staticLeaderboard.getHologram().getText();
				newText[0] += " " + pair.getLeft();
				staticLeaderboard.getHologram().setText(newText);
			});
		}));
	}

	@Override
	public List<Leaderboard> getDisplayedLeaderboards()
	{
		List<Leaderboard> leaderboards = new ArrayList<>();

		_leaderboards.forEach(pair -> pair.getRight().forEach(leaderboard -> leaderboards.addAll(leaderboard.getDisplayedLeaderboards())));

		return leaderboards;
	}
}
