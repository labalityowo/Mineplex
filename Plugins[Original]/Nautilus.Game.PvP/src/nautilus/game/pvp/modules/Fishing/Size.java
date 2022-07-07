package nautilus.game.pvp.modules.Fishing;

public enum Size
{
	Small(100, 1, Rarity.Common),
	Moderate(200, 2, Rarity.Common),
	Large(50, 4, Rarity.Moderate),
	Huge(5, 8, Rarity.Rare),
	Colossal(1, 16, Rarity.Legendary);

	private int scale;
	private double mult;
	private Rarity rarity;

	private Size(int scale, double mult, Rarity rarity)
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
