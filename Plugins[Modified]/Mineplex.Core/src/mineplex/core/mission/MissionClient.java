package mineplex.core.mission;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class MissionClient
{

	private final Set<PlayerMission> _missions;

	MissionClient()
	{
		_missions = new TreeSet<>((o1, o2) ->
		{
			int primaryComp = o1.getLength().compareTo(o2.getLength());

			if (primaryComp != 0)
			{
				return primaryComp;
			}

			return o1.getName().compareTo(o2.getName());
		});
	}

	void startMission(PlayerMission mission)
	{
		_missions.add(mission);
	}

	public Map<PlayerMission, Integer> saveProgress()
	{
		Map<PlayerMission, Integer> unsaved = new HashMap<>(_missions.size());

		_missions.forEach(mission ->
		{
			if (mission.isDiscarded() || mission.hasRewarded())
			{
				return;
			}

			unsaved.put(mission, mission.saveProgress());
		});

		return unsaved;
	}

	public boolean has(Mission mission)
	{
		return getMissions().stream()
				.anyMatch(other -> other.getId() == mission.getId());
	}

	public int getMissionsOf(MissionLength length)
	{
		return (int) _missions.stream()
				.filter(mission -> mission.getLength() == length)
				.count();
	}

	public Set<PlayerMission> getMissions()
	{
		return _missions;
	}
}
