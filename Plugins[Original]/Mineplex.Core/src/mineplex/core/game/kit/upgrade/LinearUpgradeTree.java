package mineplex.core.game.kit.upgrade;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.entity.Player;

import mineplex.core.game.MineplexGameManager;
import mineplex.core.game.kit.GameKit;

public class LinearUpgradeTree implements UpgradeTree
{

	private static final int[] LEVELS;
	private static final int[] UPGRADE_AT_LEVELS;

	static
	{
		LEVELS = new int[100];
		UPGRADE_AT_LEVELS = new int[] {5, 10, 30, 75, 100};

		int expReq = 0;

		for (int i = 0; i < 10; i++)
		{
			expReq += 50;
			LEVELS[i] = expReq;
		}

		for (int i = 10; i < 20; i++)
		{
			expReq += 100;
			LEVELS[i] = expReq;
		}

		for (int i = 20; i < 40; i++)
		{
			expReq += 200;
			LEVELS[i] = expReq;
		}

		for (int i = 40; i < 60; i++)
		{
			expReq += 300;
			LEVELS[i] = expReq;
		}

		for (int i = 60; i < 80; i++)
		{
			expReq += 400;
			LEVELS[i] = expReq;
		}

		for (int i = 80; i < LEVELS.length; i++)
		{
			expReq += 500;
			LEVELS[i] = expReq;
		}
	}

	public static int getLevel(int xp)
	{
		int level = 0;
		int expReq = 0;

		for (int xpL : LEVELS)
		{
			expReq += xpL;

			if (xp < expReq)
			{
				break;
			}

			level++;
		}

		return level;
	}

	public static int getXpForLevel(int level)
	{
		if (level > LEVELS.length)
		{
			return 0;
		}

		return LEVELS[level - 1];
	}

	public static int getTotalXpForLevel(int level)
	{
		int expReq = 0;

		for (int i = 1; i <= level; i++)
		{
			expReq += getXpForLevel(level);
		}

		return expReq;
	}

	public static int getLevelsUntilNextUpgrade(int currentLevel, int upgradeLevel)
	{
		return UPGRADE_AT_LEVELS[upgradeLevel - 1] - currentLevel;
	}

	public static int getLevel(MineplexGameManager manager, Player player, GameKit kit)
	{
		return getLevel(getXp(manager, player, kit));
	}

	public static int getXp(MineplexGameManager manager, Player player, GameKit kit)
	{
		return manager.getKitStat(player, kit, KitStat.XP);
	}

	public static int getUpgradeLevel(MineplexGameManager manager, Player player, GameKit kit)
	{
		return manager.getKitStat(player, kit, KitStat.UPGRADE_LEVEL);
	}

	public static int getUpgradeAtLevel(int index)
	{
		return UPGRADE_AT_LEVELS[index];
	}

	public static int getUpgradeCost(int index)
	{
		return getUpgradeAtLevel(index) * 1000;
	}

	private final List<List<String>> _upgrades;

	public LinearUpgradeTree(String[]... upgrades)
	{
		List<List<String>> upgradesList = new ArrayList<>(upgrades.length);

		for (String[] upgrade : upgrades)
		{
			upgradesList.add(Arrays.asList(upgrade));
		}

		_upgrades = Collections.unmodifiableList(upgradesList);
	}

	public List<List<String>> getUpgrades()
	{
		return _upgrades;
	}
}
