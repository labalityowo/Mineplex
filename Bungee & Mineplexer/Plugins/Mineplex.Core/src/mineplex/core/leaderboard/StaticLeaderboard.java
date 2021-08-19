package mineplex.core.leaderboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Location;

import mineplex.core.common.util.C;
import mineplex.core.hologram.Hologram;

public class StaticLeaderboard extends LeaderboardDisplay
{

	private final String _name;
	private final Leaderboard _leaderboard;
	private final Hologram _hologram;

	public StaticLeaderboard(LeaderboardManager manager, String name, Leaderboard leaderboard, Location location)
	{
		super(manager);

		_name = name;
		_leaderboard = leaderboard;
		_hologram = new Hologram(manager.getHologramManager(), location);
	}

	@Override
	public void register()
	{
	}

	@Override
	public void unregister()
	{
		_hologram.stop();
	}

	@Override
	public void update()
	{
		List<String> text = new ArrayList<>();

		text.add(C.cAquaB + _name);
		text.add(C.blankLine);
		text.addAll(_leaderboard.getFormattedEntries());

		_hologram.setText(text.toArray(new String[0]));
		_hologram.start();
	}

	@Override
	public List<Leaderboard> getDisplayedLeaderboards()
	{
		return Collections.singletonList(_leaderboard);
	}

	public String getName()
	{
		return _name;
	}

	public Hologram getHologram()
	{
		return _hologram;
	}
}
