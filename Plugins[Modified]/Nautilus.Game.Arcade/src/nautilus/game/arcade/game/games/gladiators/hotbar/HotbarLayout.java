package nautilus.game.arcade.game.games.gladiators.hotbar;

/**
 * Created by William (WilliamTiger).
 * 18/12/15
 */
public class HotbarLayout
{
	private int sword;
	private int rod;
	private int bow;
	private int arrows;

	public HotbarLayout(int sword, int rod, int bow, int arrows)
	{
		this.sword = sword;
		this.rod = rod;
		this.bow = bow;
		this.arrows = arrows;
	}

	public int getSword()
	{
		return sword;
	}

	public int getRod()
	{
		return rod;
	}

	public int getBow()
	{
		return bow;
	}

	public int getArrows()
	{
		return arrows;
	}

	public void setSword(int sword)
	{
		this.sword = sword;
	}

	public void setRod(int rod)
	{
		this.rod = rod;
	}

	public void setBow(int bow)
	{
		this.bow = bow;
	}

	public void setArrows(int arrows)
	{
		this.arrows = arrows;
	}

	public int toDataSaveNumber()
	{
		String str = "";
		str += (getSword() + 1);
		str += (getRod() + 1);
		str += (getBow() + 1);
		str += (getArrows() + 1);

		System.out.println("sword = " + sword);
		System.out.println("rod = " + rod);
		System.out.println("bow = " + bow);
		System.out.println("arrows = " + arrows);
		System.out.println("string = " + str);
		System.out.println("integer = " + Integer.parseInt(str));


		return Integer.parseInt(str);
	}

	public int getEmpty()
	{
		for (int i = 0; i < 9; i++)
		{
			if (i == sword || i == rod || i == bow || i == arrows)
				continue;

			return i;
		}

		return 0;
	}
}
