package nautilus.game.arcade.game.modules.gamesummary.components;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.entity.Player;

import mineplex.core.common.jsonchat.HoverEvent;
import mineplex.core.common.jsonchat.JsonMessage;
import mineplex.core.common.util.C;
import mineplex.core.game.MineplexGameManager;
import mineplex.core.game.kit.GameKit;
import mineplex.core.game.kit.upgrade.KitStat;
import mineplex.core.game.kit.upgrade.KitStatLog;
import mineplex.core.game.kit.upgrade.LinearUpgradeTree;
import mineplex.core.game.kit.upgrade.UpgradeTree;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.modules.gamesummary.GameSummaryComponentType;

public class KitSummaryComponent extends ComplexSummaryComponent
{

	private final ArcadeManager _manager;

	public KitSummaryComponent(ArcadeManager manager)
	{
		super(GameSummaryComponentType.KIT);

		_manager = manager;
	}

	@Override
	public boolean sendMessage(Player player)
	{
		AtomicInteger progressFor = new AtomicInteger();
		StringBuilder hoverTextBuilder = new StringBuilder(100);

		MineplexGameManager manager = _manager.getMineplexGameManager();
		KitStatLog statLog = manager.getKitStatLog().remove(player);

		if (statLog == null)
		{
			return false;
		}

		int xpGained = statLog.getExperienceEarned();
		int kitXP = xpGained / statLog.getKitsUsed().size();

		for (GameKit kit : statLog.getKitsUsed())
		{
			Optional<UpgradeTree> optional = kit.getUpgradeTree();

			if (!optional.isPresent() || !(optional.get() instanceof LinearUpgradeTree))
			{
				continue;
			}

			int currentXp = manager.getKitStat(player, kit, KitStat.XP);
			int level = LinearUpgradeTree.getLevel(currentXp);

			if (level >= 100)
			{
				continue;
			}

			int newExp = currentXp + kitXP;
			int newLevel = LinearUpgradeTree.getLevel(newExp);

			if (newLevel != level)
			{
				newExp = LinearUpgradeTree.getTotalXpForLevel(level);
				newLevel = level + 1;

				new JsonMessage(C.cPurpleB + "Kit Level Up! " + C.cGray + kit.getDisplayName() + " " + C.cWhite + level + " " + C.cGray + DOUBLE_ARROW + " " + C.cWhite + newLevel)
						.hover(HoverEvent.SHOW_TEXT, getHoverText(kit, newLevel, LinearUpgradeTree.getXpForLevel(newLevel)))
						.sendToPlayer(player);

				manager.setKitStat(player, kit, KitStat.XP, newExp);
			}
			else
			{
				hoverTextBuilder.append("\n").append(getHoverText(kit, level, LinearUpgradeTree.getTotalXpForLevel(level) - newExp));

				progressFor.getAndIncrement();

				manager.incrementKitStat(player, kit, KitStat.XP, kitXP);
			}
		}

		if (progressFor.get() == 0)
		{
			return false;
		}

		new JsonMessage(C.cGray + "Progress for " + C.cYellow + progressFor.get() + " Kit" + (progressFor.get() == 1 ? "" : "s"))
				.hover(HoverEvent.SHOW_TEXT, hoverTextBuilder.toString().substring(1))
				.sendToPlayer(player);

		sendBlank(player);
		return true;
	}

	private String getHoverText(GameKit kit, int level, int difference)
	{
		return C.cGray + "Your " + C.cYellow + kit.getDisplayName() + C.cGray + " is level " + C.cGreen + level + "\n" +
				C.cYellow + difference + " EXP " + C.cGray + "until next level";
	}
}
