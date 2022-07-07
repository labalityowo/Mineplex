package mineplex.core.progression.math;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Maps;

import net.md_5.bungee.api.ChatColor;

/**
 * This class handles all the math and static fields needed for Kit Progressions
 * You can find some util methods in here as well that relate to numbers
 */
public class Calculations
{

	private static final int[] LEVELS = new int[100];
	private static final int[] UPGRADE_LEVELS = {5, 10, 30, 75, 100};
	private static final Map<Integer, Integer> GEMS_FOR_UPGRADE = Maps.newHashMap();
	private static final Map<Integer, Integer> GEMS_FOR_XPLESS_UPGRADE = new HashMap<>();

	static
	{
		generateDefaultExperienceLevels();
		for (int level : UPGRADE_LEVELS)
		{
			GEMS_FOR_UPGRADE.put(level, level * 1000);
		}
		GEMS_FOR_XPLESS_UPGRADE.put(1, 1000);
		GEMS_FOR_XPLESS_UPGRADE.put(2, 5000);
		GEMS_FOR_XPLESS_UPGRADE.put(3, 15000);
		GEMS_FOR_XPLESS_UPGRADE.put(4, 50000);
		GEMS_FOR_XPLESS_UPGRADE.put(5, 100000);
	}

	/**
	 * Generate the default XP values for leveling up.
	 */
	private static void generateDefaultExperienceLevels()
	{
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

	/**
	 * Determines if the players current level is a level at which an upgrade is unlocked
	 *
	 * @param currentLevel The players level
	 * @return Whether or not the players level is within the {@code GEMS_FOR_UPGRADE} map
	 */
	public static boolean isUpgradeLevelEligible(int currentLevel)
	{
		return GEMS_FOR_UPGRADE.containsKey(currentLevel);
	}
	
	public static boolean isUpgradeLevelEligibleXpLess(int gems)
	{
		boolean afford = false;
		
		for (int cost : GEMS_FOR_XPLESS_UPGRADE.values())
		{
			if (cost <= gems)
			{
				afford = true;
			}
		}
		return afford;
	}

	/**
	 * Determines if the players current level, and his gems, are what he needs to level up this upgrade
	 *
	 * @param currentLevel The players level
	 * @param gems         The players current gems
	 * @return Whether or not the player can upgrade
	 */
	public static boolean canUpgrade(int currentLevel, int gems)
	{
		return GEMS_FOR_UPGRADE.containsKey(currentLevel) && GEMS_FOR_UPGRADE.get(currentLevel) <= gems;
	}
	
	public static boolean canUpgradeXpLess(int upgradeLevel, int gems)
	{
		return GEMS_FOR_XPLESS_UPGRADE.containsKey(upgradeLevel) && GEMS_FOR_XPLESS_UPGRADE.get(upgradeLevel) <= gems;
	}

	/**
	 * Get the XP required for the next level
	 *
	 * @param currLevel The players current level
	 * @return The XP for the next level
	 */
	public static int getXpForNextLevel(int currLevel)
	{
		if (currLevel >= 100)
		{
			return LEVELS[LEVELS.length - 1];
		}

		//lets get this calculation right....
		return LEVELS[currLevel - 1];
	}

	/**
	 * Get the difference between the players current level, and the next level he needs for upgrades
	 *
	 * @param currentLevel The players level
	 * @return The integer difference
	 */
	public static int getUpgradeLevelDifference(int currentLevel)
	{
		for (int i = 0; i < UPGRADE_LEVELS.length; i++) {
			if (UPGRADE_LEVELS[i] > currentLevel) {
				return UPGRADE_LEVELS[i] - currentLevel;
			}
		}
		return 0;
	}

	/**
	 * Utility method for percentage based chat colors
	 *
	 * @param i The initial number
	 * @param n The total number
	 * @return The chat color corresponding to the percentage
	 */
	public static ChatColor getColor(int i, int n)
	{
		ChatColor color = ChatColor.GREEN;

		float perc = (i * 100.0f) / n;

		if (perc <= 25)
		{
			return ChatColor.GRAY;
		}
		if (perc <= 50)
		{
			return ChatColor.YELLOW;
		}
		if (perc <= 75)
		{
			return ChatColor.GOLD;
		}

		return color;
	}

	/**
	 * Get the next integer upgrade level based of the players current level
	 *
	 * @param currentLevel The players current level
	 * @return The next upgrade level
	 */
	public static int getNextUpgradeLevel(int currentLevel)
	{
		for (int i = 0; i < UPGRADE_LEVELS.length; i++) {
			if (UPGRADE_LEVELS[i] <= currentLevel) {
				return UPGRADE_LEVELS[i];
			}
		}
		return 5;
	}
	
	public static int getNextUpgradeLevelXpLess(int currentLevel)
	{
		int current = 5;
		for (int level : GEMS_FOR_XPLESS_UPGRADE.keySet())
		{
			if (level < current && level > currentLevel)
			{
				current = level;
			}
		}
		
		return current;
	}

	/**
	 * Get the next level the player needs to reach to unlock an upgrade
	 *
	 * @param currentLevel The players current level
	 * @return The next level the player needs to reach to unlock an upgrade
	 */
	public static int getNextUpgradeLevelPlayer(int currentLevel)
	{
		for (int i = 0; i < UPGRADE_LEVELS.length; i++) {
			if (UPGRADE_LEVELS[i] >= currentLevel) {
				return UPGRADE_LEVELS[i];
			}
		}
		return 100;
	}

	public static int getLevelRequiredFor(int upgradeLevel)
	{
		switch (upgradeLevel)
		{
			case 1:
				return 5;
			case 2:
				return 10;
			case 3:
				return 30;
			case 4:
				return 75;
			case 5:
				return 100;
		}

		return -1;
	}
	
	/**
	 * Get the number of gems required to purchase an upgrade from the upgrade level
	 * 
	 * @param upgradeLevel The upgrade level (1-5)
	 * @return The integer value of the number of gems required to purchase an upgrade
	 */
	public static int getGemsCost(int upgradeLevel)
	{
		return GEMS_FOR_UPGRADE.get(getLevelRequiredFor(upgradeLevel));
	}
	
	public static int getGemsCostXpLess(int upgradeLevel)
	{
		return GEMS_FOR_XPLESS_UPGRADE.get(upgradeLevel);
	}
}
