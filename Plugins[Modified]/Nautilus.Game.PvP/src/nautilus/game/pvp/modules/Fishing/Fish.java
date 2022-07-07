package nautilus.game.pvp.modules.Fishing;

public enum Fish
{
	Bass(260, 1, Rarity.Common),
	Flounder(200, 2, Rarity.Common),
	Barramundi(140, 3, Rarity.Common),
	Snapper(80, 4, Rarity.Moderate),
	Tuna(60, 6, Rarity.Moderate),
	Mackarel(40, 8, Rarity.Moderate),
	Trout(20, 12, Rarity.Rare),
	Salmon(10, 16, Rarity.Rare),
	Shark(1, 24, Rarity.Legendary);

	private int scale;
	private double mult;
	private Rarity rarity;

	private Fish(int scale, double mult, Rarity rarity) 
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
