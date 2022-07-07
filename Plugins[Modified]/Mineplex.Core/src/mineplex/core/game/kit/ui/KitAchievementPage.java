package mineplex.core.game.kit.ui;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.achievement.Achievement;
import mineplex.core.achievement.AchievementData;
import mineplex.core.common.util.C;
import mineplex.core.game.MineplexGameManager;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemBuilder;

public class KitAchievementPage extends KitPage
{

	KitAchievementPage(MineplexGameManager plugin, Player player, GameKit kit)
	{
		super(plugin, player, kit, kit.getDisplayName() + " Achievements", 45);

		buildPage();
	}

	@Override
	protected void buildPage()
	{
		addBackButton();

		Achievement[] achievements = _kit.getAchievements();
		int currentIndex = Math.max(22 - (achievements.length / 2), 18);

		for (Achievement achievement : achievements)
		{
			AchievementData data = _plugin.getAchievementManager().get(_player, achievement);
			boolean hasUnlocked = data.getLevel() >= achievement.getMaxLevel();

			ItemBuilder builder = new ItemBuilder(hasUnlocked ? Material.EXP_BOTTLE : Material.GLASS_BOTTLE);
			builder.setTitle((hasUnlocked ? C.cGreen : C.cRed) + achievement.getName());

			List<String> lore = new ArrayList<>();

			lore.add(C.blankLine);

			for (String line : achievement.getDesc())
			{
				lore.add(C.cWhite + line);
			}

			if (hasUnlocked)
			{
				lore.add(C.blankLine);
				lore.add(C.cAqua + "Complete!");
			}

			builder.addLores(lore);

			addButtonNoAction(currentIndex++, builder.build());
		}
	}
}
