package nautilus.game.arcade.game.modules.gamesummary.components;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.bukkit.entity.Player;

import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.boosters.Booster;
import mineplex.core.common.Pair;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.modules.gamesummary.GameSummaryComponent;
import nautilus.game.arcade.game.modules.gamesummary.GameSummaryComponentType;
import nautilus.game.arcade.managers.GameRewardManager.Perm;

public class ShardSummaryComponent extends GameSummaryComponent<Pair<Player, Integer>>
{

	private final ArcadeManager _manager;

	public ShardSummaryComponent(ArcadeManager manager, Function<Player, Pair<Player, Integer>> getFunction)
	{
		super(GameSummaryComponentType.SHARDS, getFunction);

		_manager = manager;
	}

	@Override
	public String getMainText(Pair<Player, Integer> data)
	{
		Game game = _manager.GetGame();
		Player player = data.getLeft();
		final int baseShards = data.getRight();
		int totalShards = baseShards;
		double extraMult = 0;

		for (Perm shardMultPerm : Perm.values())
		{
			if (_manager.GetClients().Get(player).hasPermission(shardMultPerm))
			{
				extraMult += 0.5;
			}
		}

		if (extraMult > 0)
		{
			totalShards += ((int) (extraMult * baseShards));
		}

		Booster booster = _manager.getBoosterManager().getActiveBooster();

		if (game.GemBoosterEnabled && booster != null)
		{
			double multiplier = booster.getMultiplier() - 1;
			totalShards += (int) (multiplier * baseShards);
		}

		return C.cGray + "+" + C.cAqua + totalShards + C.cGray + " Shards";
	}

	@Override
	public List<String> getHoverText(Pair<Player, Integer> data)
	{
		List<String> text = new ArrayList<>();
		Game game = _manager.GetGame();
		Player player = data.getLeft();
		final int baseShards = data.getRight();
		double extraMult = 0;

		PermissionGroup group = _manager.GetClients().Get(player).getPrimaryGroup();

		for (Perm shardMultPerm : Perm.values())
		{
			if (_manager.GetClients().Get(player).hasPermission(shardMultPerm))
			{
				extraMult += 0.5;
			}
		}

		text.add(get(baseShards, "Earning " + baseShards + " Game Gems"));

		if (extraMult > 0)
		{
			int extraShards = ((int) (extraMult * baseShards));

			text.add(get(extraShards, group.getDisplay(true, true, true, true) + C.cYellow + " Rank " + C.cAqua + "+" + Math.round((extraMult * 100)) + "%"));
		}

		Booster booster = _manager.getBoosterManager().getActiveBooster();
		if (game.GemBoosterEnabled && booster != null)
		{
			double multiplier = booster.getMultiplier() - 1;
			int extraShards = ((int) (multiplier * baseShards));

			text.add(get(extraShards, booster.getPlayerName() + "'s" + F.elem(" Game Amplifier" + C.cAqua + " +" + Math.round((multiplier * 100)) + "%")));
		}

		return text;
	}

	private String get(int shards, String reason)
	{
		return C.cGray + "+" + C.cAqua + shards + C.cGray + " for " + C.cYellow + reason;
	}
}
