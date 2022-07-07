package nautilus.game.pvp.modules.Fishing;

public enum Age
{
	Young(50, 1, Rarity.Common),
	Mature(100, 2, Rarity.Common),
	Old(50, 4, Rarity.Moderate),
	Ancient(10, 8, Rarity.Rare),
	Mythical(1, 16, Rarity.Legendary);

	private int scale;
	private double mult;
	private Rarity rarity;

	private Age(int scale, double mult, Rarity rarity)
	{
		this.scale = scale;
		this.mult = mult;
		this.rarity = rarity;
	}

	public int GetScale()
	{
		return scale;
	}

	public double GetMult()
	{
		return mult;
	}

	public Rarity GetRarity()
	{
		return rarity;
	}
}
