package mineplex.core.achievement.leveling.rewards;

import java.util.function.Consumer;

import org.bukkit.entity.Player;

public class LevelCallbackReward extends LevelDummyReward
{

	private final Consumer<Player> _callback;

	public LevelCallbackReward(String description, Consumer<Player> callback)
	{
		super(description);

		_callback = callback;
	}

	@Override
	public void claim(Player player)
	{
		_callback.accept(player);
	}
}
