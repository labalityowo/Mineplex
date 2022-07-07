package mineplex.core.texttutorial.tutorial;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.bukkit.entity.Player;

public abstract class Tutorial
{
	private String _name;
	private String _taskId;
	private int _gemReward;

	private ArrayList<Phase> _phases;
	private HashMap<Player, TutorialData> _playerMap;

	public Tutorial(String name, String taskId, int gemReward)
	{
		_name = name;
		_taskId = taskId;
		_gemReward = gemReward;
		_phases = new ArrayList<>();
		_playerMap = new HashMap<>();
	}

	public String getName()
	{
		return _name;
	}

	public String getTaskId()
	{
		return _taskId;
	}

	public void startTutorial(Player player)
	{
		_playerMap.put(player, new TutorialData(player, _phases.get(0)));
	}

	public void stopTutorial(Player player)
	{
		_playerMap.remove(player);
	}

	public boolean isInTutorial(Player player)
	{
		return _playerMap.containsKey(player);
	}

	protected void addPhase(Phase phase)
	{
		_phases.add(phase);
	}

	public Collection<TutorialData> getTutorialDatas()
	{
		return _playerMap.values();
	}

	public Phase getPhase(int phaseIndex)
	{
		return _phases.get(phaseIndex);
	}

	public int getPhaseSize()
	{
		return _phases.size();
	}

	public int getGemReward()
	{
		return _gemReward;
	}
}
