package nautilus.game.arcade.game.modules.gamesummary;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.achievement.Achievement;
import mineplex.core.achievement.AchievementData;
import mineplex.core.achievement.AchievementLog;
import mineplex.core.achievement.AchievementManager;
import mineplex.core.common.Pair;
import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.jsonchat.HoverEvent;
import mineplex.core.common.jsonchat.JsonMessage;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilServer;

import nautilus.game.arcade.ArcadeFormat;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.modules.Module;
import nautilus.game.arcade.game.modules.gamesummary.components.AchievementSummaryComponent;
import nautilus.game.arcade.game.modules.gamesummary.components.ExperienceSummaryComponent;
import nautilus.game.arcade.game.modules.gamesummary.components.GemSummaryComponent;
import nautilus.game.arcade.game.modules.gamesummary.components.KitSummaryComponent;
import nautilus.game.arcade.game.modules.gamesummary.components.LevelUpSummaryComponent;
import nautilus.game.arcade.game.modules.gamesummary.components.ShardSummaryComponent;

public class GameSummaryModule extends Module
{

	private final List<GameSummaryComponent<?>> _components;

	public GameSummaryModule()
	{
		_components = new ArrayList<>(8);
	}

	@Override
	protected void setup()
	{
		setupDefaultComponents();
	}

	@Override
	public void cleanup()
	{
		_components.clear();
	}

	public GameSummaryModule addComponent(GameSummaryComponent<?> component)
	{
		_components.add(component);
		return this;
	}

	public GameSummaryModule replaceComponent(GameSummaryComponentType type, GameSummaryComponent<?> newComponent)
	{
		for (int i = 0; i < _components.size(); i++)
		{
			if (_components.get(i).getType().equals(type))
			{
				_components.set(i, newComponent);
				break;
			}
		}

		return this;
	}

	private void setupDefaultComponents()
	{
		Function<Player, Pair<AchievementLog, AchievementData>> experienceFunction = player ->
		{
			AchievementManager manager = getGame().getArcadeManager().GetAchievement();
			AchievementLog log = manager.getLog(player).get(Achievement.GLOBAL_MINEPLEX_LEVEL);
			AchievementData data = manager.get(player, Achievement.GLOBAL_MINEPLEX_LEVEL);

			return Pair.create(log, data);
		};

		addComponent(new GemSummaryComponent(player -> getGame().GetGems(player), GlobalCurrency.GEM.getColor(), GlobalCurrency.GEM.getPrefix()));
		addComponent(new ShardSummaryComponent(getGame().getArcadeManager(), player -> Pair.create(player, getGame().getArcadeManager().getGameRewardManager().getBaseShardsEarned(player))));
		addComponent(new ExperienceSummaryComponent(experienceFunction));
		addComponent(new LevelUpSummaryComponent(experienceFunction));
		addComponent(new AchievementSummaryComponent(getGame().getArcadeManager()));
		addComponent(new KitSummaryComponent(getGame().getArcadeManager()));
	}

	@EventHandler
	public void gameDisable(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Dead)
		{
			return;
		}

		ArcadeManager manager = getGame().getArcadeManager();

		if (!manager.IsRewardGems() || !manager.IsRewardStats() || !manager.IsRewardAchievements() || manager.GetGame().getGameLiveTime() == 0)
		{
			return;
		}

		UtilServer.getPlayersCollection().forEach(this::informRewards);
	}

	private void informRewards(Player player)
	{
		if (!getGame().getArcadeManager().hasBeenPlaying(player))
		{
			return;
		}

		player.sendMessage(ArcadeFormat.Line);

		player.sendMessage(C.Bold + "Game Rewards");
		player.sendMessage("");

		_components.forEach(component ->
		{
			try
			{
				component.sendMessage(player);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		});

		new JsonMessage(C.cGray + "Hover over for details.")
				.hover(HoverEvent.SHOW_TEXT, C.cGray + "Don't hover over me, silly!")
				.sendToPlayer(player);
		player.sendMessage(ArcadeFormat.Line);

		player.playSound(player.getLocation(), Sound.LEVEL_UP, 2, 1);

		getGame().getArcadeManager().GetAchievement().clearLog(player);
	}
}
