package mineplex.core.reward;

import mineplex.core.treasure.reward.RewardRarity;

public enum RewardType
{
	//% Chances			Mythic		Legend		Rare		Uncommon    Common
	GAME_LOOT(			 0.000001,	0.00001,	0.0001,		3,           30),

	OLD_CHEST(			 20,		0.06,		0.8,		16,          40),
	ANCIENT_CHEST(		 0,			2,			8,			72,          0),
	MYTHICAL_CHEST(		 0.001,		4,			18,			72,          0),
	WINTER_CHEST(		 0,			5,			18,			32,          40),
	ILLUMINATED_CHEST(	 0,			2,			16,			72,          0),
	FREEDOM_CHEST(       0,         5,          18,         0,           0),
	HAUNTED_CHEST(       0,         5,          18,         0,           0),
	OMEGA_CHEST(         0,         32,         16,         2,           0),
	TRICK_OR_TREAT_CHEST(0,         2,          16,         2,           0),
	THANKFUL_CHEST(      0.1,       2,          16,         0,           0),
	GINGERBREAD_CHEST(   0,         2,          16,         0,           0),
	MINESTRIKE_CHEST(    0,         2,          16,         0,           0),
	LOVE_CHEST(          0,         6,          18,         0,           0),
	VALENTINES_GIFT(	 0,			7,			20,			20,          0),
	ST_PATRICKS(         0,         6,          18,         0,           0),
	SPRING(              0,         6,          18,         0,           0),
	MOBA(                0,         5,          20,         30,          0),

	SPINNER_FILLER(		 0.1,		1,			5,			20,          28),
	SPINNER_REAL(		 0.000005,	0.15,		1.2,		6,           18);

	private final Rarity[] _rarities;

	RewardType(double mythical, double legend, double rare, double uncommon, double common)
	{
		_rarities = new Rarity[]
				{
						new Rarity(RewardRarity.MYTHICAL, mythical),
						new Rarity(RewardRarity.LEGENDARY, legend),
						new Rarity(RewardRarity.RARE, rare),
						new Rarity(RewardRarity.UNCOMMON, uncommon),
						new Rarity(RewardRarity.COMMON, common)
				};
	}

	public RewardRarity generateRarity()
	{
		double totalRoll = 0;

		for (Rarity rarity : _rarities)
		{
			totalRoll += rarity.Chance;
		}

		double roll = Math.random() * totalRoll;

		for (Rarity rarity : _rarities)
		{
			if ((roll -= rarity.Chance) < 0)
			{
				return rarity.RewardRarity;
			}
		}

		return null;
	}

	private class Rarity
	{

		RewardRarity RewardRarity;
		double Chance;

		Rarity(RewardRarity rewardRarity, double chance)
		{
			RewardRarity = rewardRarity;
			Chance = chance;
		}
	}
}
