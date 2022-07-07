package nautilus.game.arcade.game.games.typewars;

import java.util.ArrayList;

import mineplex.core.common.util.UtilMath;
import mineplex.core.itemstack.ItemStackFactory;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum MinionSize
{
	EASY("Easy", 2, ItemStackFactory.Instance.CreateStack(Material.MONSTER_EGG, (byte) 0, 1, (short) 55, "", new String[]{}), 1, 0),
	MEDIUM("Medium", 4, ItemStackFactory.Instance.CreateStack(Material.MONSTER_EGG, (byte) 0, 1, (short) 61, "", new String[]{}), 1, 1),
	HARD("Hard", 6, ItemStackFactory.Instance.CreateStack(Material.MONSTER_EGG, (byte) 0, 1, (short) 52, "", new String[]{}), 1, 2),
	FREAK("Freak", 10000, new ItemStack(Material.MONSTER_EGG), 1, 999999),
	BOSS("Boss", 10000, new ItemStack(Material.MONSTER_EGG), 7, 999999999);
	
	private int _cost;
	private ItemStack _displayItem;
	private int _lives;
	private int _gemReward;
	
	private String _displayName;
	
	private MinionSize(String name, int cost, ItemStack displayItem, int lives, int gemReward)
	{
		_displayName = name;
		_cost = cost;
		_displayItem = displayItem;
		_lives = lives;
		_gemReward = gemReward;
	}
	
	public int getCost()
	{
		return _cost;
	}
	
	public ItemStack getDisplayItem()
	{
		return _displayItem;
	}
	
	public String getDisplayName()
	{
		return _displayName;
	}
	
	public int getGemReward()
	{
		return _gemReward;
	}
	
	public MinionType getRandomType()
	{
		ArrayList<MinionType> minionList = new ArrayList<>();
		for(MinionType type : MinionType.values())
		{
			if(type.getSize() == this)
			{
				minionList.add(type);
			}
		}
		return minionList.get(UtilMath.r(minionList.size()));
	}

	public int getLives()
	{
		return _lives;
	}
	
}
