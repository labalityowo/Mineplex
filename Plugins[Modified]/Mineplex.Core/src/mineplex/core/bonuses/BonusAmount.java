package mineplex.core.bonuses;

import java.util.List;

import mineplex.core.common.util.C;

public class BonusAmount
{
	private int _gems;
	private int _shards;
	private GoldAmount _gold = new GoldAmount();
	private int _bonusGems;
	private int _bonusShards;
	private GoldAmount _bonusGold = new GoldAmount();
	private int _experience;
	private int _bonusExperience;
	private int _tickets;
	private int _oldChests;
	private int _ancientChests;
	private int _mythicalChests;
	private int _illuminatedChests;
	private int _omegaChests;

	public BonusAmount()
	{

	}

	public int getGems()
	{
		return _gems;
	}

	public void setGems(int gems)
	{
		_gems = gems;
	}

	public int getShards()
	{
		return _shards;
	}

	public void setShards(int shards)
	{
		_shards = shards;
	}

	public GoldAmount getGold()
	{
		return _gold;
	}

	public void setGold(Integer serverId, Integer gold)
	{
		_gold.setGoldFor(serverId, gold);
	}

	public int getBonusGems()
	{
		return _bonusGems;
	}

	public void setBonusGems(int bonusGems)
	{
		_bonusGems = bonusGems;
	}

	public int getBonusShards()
	{
		return _bonusShards;
	}

	public void setBonusShards(int bonusShards)
	{
		_bonusShards = bonusShards;
	}

	public GoldAmount getBonusGold()
	{
		return _bonusGold;
	}

	public void setBonusGold(Integer serverId, Integer bonusGold)
	{
		_bonusGold.setGoldFor(serverId, bonusGold);
	}

	public int getTotalGems()
	{
		return getGems() + getBonusGems();
	}

	public int getTotalShards()
	{
		return getShards() + getBonusShards();
	}

	public int getTotalGold()
	{
		return getGold().getTotalGold() + getBonusGold().getTotalGold();
	}

	public int getExperience()
	{
		return _experience;
	}

	public void setExperience(int experience)
	{
		_experience = experience;
	}

	public int getBonusExperience()
	{
		return _bonusExperience;
	}

	public void setBonusExperience(int bonusExperience)
	{
		_bonusExperience = bonusExperience;
	}

	public int getTotalExperience()
	{
		return getExperience() + getBonusExperience();
	}

	public int getTickets()
	{
		return _tickets;
	}

	public void setTickets(int tickets)
	{
		_tickets = tickets;
	}

	public int getOldChests()
	{
		return _oldChests;
	}

	public void setOldChests(int oldChests)
	{
		_oldChests = oldChests;
	}

	public int getAncientChests()
	{
		return _ancientChests;
	}

	public void setAncientChests(int ancientChests)
	{
		_ancientChests = ancientChests;
	}

	public int getMythicalChests()
	{
		return _mythicalChests;
	}

	public void setMythicalChests(int mythicalChests)
	{
		_mythicalChests = mythicalChests;
	}

	public int getIlluminatedChests()
	{
		return _illuminatedChests;
	}

	public void setIlluminatedChests(int illuminatedChests)
	{
		_illuminatedChests = illuminatedChests;
	}

	public int getOmegaChests()
	{
		return _omegaChests;
	}

	public void setOmegaChests(int omegaChests)
	{
		_omegaChests = omegaChests;
	}

	public boolean isGreaterThanZero()
	{
		return _bonusShards > 0 || _shards > 0 || _bonusGems > 0 || _gems > 0 || _gold.getTotalGold() > 0 || _bonusGold.getTotalGold() > 0 || _oldChests > 0 || _ancientChests > 0 || _mythicalChests > 0;
	}

	public void addLore(List<String> lore)
	{
		lore.add(C.cYellow + "Rewards");
		addLore(lore, getTickets(), 0, "Carl Spin Ticket" + (getTickets() > 1 ? "s" : ""));
		addLore(lore, getShards(), getBonusShards(), "Treasure Shards");
		addLore(lore, getGems(), getBonusGems(), "Gems");
		addLore(lore, getGold().getTotalGold(), getBonusGold().getTotalGold(), "Gold");
		addLore(lore, getExperience(), getBonusExperience(), "Experience");
		addLore(lore, getOldChests(), 0, "Old Chest", "Old Chests");
		addLore(lore, getAncientChests(), 0, "Ancient Chest", "Ancient Chests");
		addLore(lore, getMythicalChests(), 0, "Mythical Chest", "Mythical Chests");
		addLore(lore, getIlluminatedChests(), 0, "Illuminated Chest", "Illuminated Chests");
		addLore(lore, getOmegaChests(), 0, "Omega Chest", "Omega Chests");
	}

	private void addLore(List<String> lore, int amount, int bonus, String suffix)
	{
		if (amount > 0)
		{
			lore.add("  " + C.cWhite + amount + " " + suffix);
		}

//		if (bonus > 0)
//			lore.add(C.cYellow + "Streak Bonus: " + C.cWhite + bonus + " " + suffix);
	}

	private void addLore(List<String> lore, int amount, int bonus, String suffix, String plural)
	{
		if (amount == 1)
		{
			lore.add("  " + C.cWhite + amount + " " + plural);
		}
		else if (amount > 0)
		{
			lore.add("  " + C.cWhite + amount + " " + suffix);
		}
	}
}
