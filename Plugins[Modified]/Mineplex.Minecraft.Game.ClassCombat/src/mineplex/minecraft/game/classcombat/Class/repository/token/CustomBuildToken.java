package mineplex.minecraft.game.classcombat.Class.repository.token;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;

import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.classcombat.Skill.ISkill;
import mineplex.minecraft.game.classcombat.item.Item;

public class CustomBuildToken
{
	public static final int MAX_SKILL_TOKENS = 12;
	public static final int MAX_ITEM_TOKENS = 12;
	
	public int CustomBuildId;

	public String PlayerName;
	public String Name;
	public boolean Active;

	public Integer CustomBuildNumber = 0;

	public String PvpClass = "";

	public String SwordSkill = "";
	public Integer SwordSkillLevel = 0;

	public String AxeSkill = "";
	public Integer AxeSkillLevel = 0;

	public String BowSkill = "";
	public Integer BowSkillLevel = 0;

	public String ClassPassiveASkill = "";
	public Integer ClassPassiveASkillLevel = 0;

	public String ClassPassiveBSkill = "";
	public Integer ClassPassiveBSkillLevel = 0;

	public String GlobalPassiveSkill = "";
	public Integer GlobalPassiveSkillLevel = 0;

	public final List<SlotToken> Slots = new ArrayList<>(9);

	public int SkillTokens = MAX_SKILL_TOKENS;
	public int ItemTokens = 1;
	
	public CustomBuildToken() { }
	
	public CustomBuildToken(ClassType gameClassType)
	{
		PvpClass = gameClassType.name();
		
		for (int i = 0; i < 9; i++)
		{
			Slots.add(new SlotToken());
		}
		
		Slots.set(0, new SlotToken("Standard Sword", Material.IRON_SWORD, 1));
		Slots.set(1, new SlotToken("Standard Axe", Material.IRON_AXE, 1));
		
		for (int i = 2; i < 9; i++)
		{
			Slots.set(i, new SlotToken("Mushroom Soup", Material.MUSHROOM_SOUP, 1));
		}
		
		if (gameClassType == ClassType.Assassin || gameClassType == ClassType.Ranger)
		{
			Slots.set(2, new SlotToken("Standard Bow", Material.BOW, 1));
			Slots.set(3, new SlotToken(gameClassType.name() + " Arrows", Material.ARROW, gameClassType == ClassType.Assassin ? 12 : 24));
			ItemTokens = 1;
		}
		else
		{
			if (gameClassType != ClassType.Mage)
			{
				Slots.set(7, new SlotToken("Water Bottle", Material.POTION, 1));
			}
			else
			{
				Slots.set(7, new SlotToken("Web", Material.WEB, 3));
			}
			
			Slots.set(8, new SlotToken());
			ItemTokens = 0;
		}
	}
	
	public void printInfo()
	{
		System.out.println("CustomBuildId : " + CustomBuildId);
		System.out.println("PlayerName : " + PlayerName);
		System.out.println("Name : " + Name);
		System.out.println("Active : " + Active);

		System.out.println("CustomBuildNumber : " + CustomBuildNumber);

		System.out.println("PvpClass : " + PvpClass);

		System.out.println("SwordSkill : " + SwordSkill);
		System.out.println("SwordLevel : " + SwordSkillLevel);

		System.out.println("AxeSkill : " + AxeSkill);
		System.out.println("AxeLevel : " + AxeSkillLevel);

		System.out.println("BowSkill : " + BowSkill);
		System.out.println("BowLevel : " + BowSkillLevel);

		System.out.println("ClassPassiveASkill : " + ClassPassiveASkill);
		System.out.println("ClassPassiveALevel : " + ClassPassiveASkillLevel);

		System.out.println("ClassPassiveBSkill : " + ClassPassiveBSkill);
		System.out.println("ClassPassiveBLevel : " + ClassPassiveBSkillLevel);

		System.out.println("GlobalPassiveSkill : " + GlobalPassiveSkill);
		System.out.println("GlobalPassiveLevel : " + GlobalPassiveSkillLevel);

		for (SlotToken token : Slots)
		{
			token.printInfo();
		}
	}

	public void setSkill(ISkill skill, int level)
	{
		switch (skill.GetSkillType())
		{
			case Axe:
				AxeSkill = skill.GetName();
				AxeSkillLevel = level;
				break;
			case Bow:
				BowSkill = skill.GetName();
				BowSkillLevel = level;
				break;
			case Class:
				break;
			case GlobalPassive:
				GlobalPassiveSkill = skill.GetName();
				GlobalPassiveSkillLevel = level;
				break;
			case PassiveA:
				ClassPassiveASkill = skill.GetName();
				ClassPassiveASkillLevel = level;
				break;
			case PassiveB:
				ClassPassiveBSkill = skill.GetName();
				ClassPassiveBSkillLevel = level;
				break;
			case Sword:
				SwordSkill = skill.GetName();
				SwordSkillLevel = level;
				break;
			default:
				break;
		}
		
		SkillTokens -= skill.GetTokenCost() * level;
	}
	
	public void removeSkill(ISkill skill)
	{
		int level = 0;
		
		switch (skill.GetSkillType())
		{
			case Axe:
				AxeSkill = "";
				level = AxeSkillLevel;
				AxeSkillLevel = 0;
				break;
			case Bow:
				BowSkill = "";
				level = BowSkillLevel;
				BowSkillLevel = 0;
				break;
			case Class:
				break;
			case GlobalPassive:
				GlobalPassiveSkill = "";
				level = GlobalPassiveSkillLevel;
				GlobalPassiveSkillLevel = 0;
				break;
			case PassiveA:
				ClassPassiveASkill = "";
				level = ClassPassiveASkillLevel;
				ClassPassiveASkillLevel = 0;
				break;
			case PassiveB:
				ClassPassiveBSkill = "";
				level = ClassPassiveBSkillLevel;
				ClassPassiveBSkillLevel = 0;
				break;
			case Sword:
				SwordSkill = "";
				level = SwordSkillLevel;
				SwordSkillLevel = 0;
				break;
			default:
				break;
		}
		
		SkillTokens += skill.GetTokenCost() * level;
	}

	public boolean hasSkill(ISkill skill)
	{
		return SwordSkill.equalsIgnoreCase(skill.GetName()) 
				|| AxeSkill.equalsIgnoreCase(skill.GetName()) 
				|| BowSkill.equalsIgnoreCase(skill.GetName())
				|| ClassPassiveASkill.equalsIgnoreCase(skill.GetName())
				|| ClassPassiveBSkill.equalsIgnoreCase(skill.GetName())
				|| GlobalPassiveSkill.equalsIgnoreCase(skill.GetName());
	}

	public int getLevel(ISkill skill)
	{
		switch (skill.GetSkillType())
		{
			case Axe:
				return AxeSkillLevel;
			case Bow:
				return BowSkillLevel;
			case GlobalPassive:
				return GlobalPassiveSkillLevel;
			case PassiveA:
				return ClassPassiveASkillLevel;
			case PassiveB:
				return ClassPassiveBSkillLevel;
			case Sword:
				return SwordSkillLevel;
			default:
				return 0;
		}
	}

	public boolean hasItem(Material material, String name)
	{
		for (SlotToken token : Slots)
		{
			// Stupid json crap giving me null values.
			if (token == null)
				continue;
			
			if (token.Material == null)
				continue;
			
			if (token.Name == null)
				continue;
			
			if (token.Material.equalsIgnoreCase(material.name()) && token.Name.equalsIgnoreCase(name))
				return true;
		}
		
		return false;
	}
	
	public boolean hasItemType(Material material)
	{
		for (SlotToken token : Slots)
		{
			// Stupid json crap giving me null values.
			if (token == null)
				continue;
			
			if (token.Material == null)
				continue;
			
			if (token.Material.equalsIgnoreCase(material.name()))
				return true;
		}
		
		return false;
	}
	
	public boolean hasItemWithNameLike(String name)
	{
		for (SlotToken token : Slots)
		{
			// Stupid json crap giving me null values.
			if (token == null)
				continue;
			
			token.printInfo();
			
			if (token.Name == null)
				continue;
			
			if (token.Name.contains(name))
			{ 
				return true;
			}
		}
		
		return false;
	}
	
	public int getLastItemIndexWithNameLike(String name)
	{
		for (int i = Slots.size() - 1; i >= 0; i--)
		{
			SlotToken token = Slots.get(i);
			
			// Stupid json crap giving me null values.
			if (token == null)
				continue;
			
			if (token.Name == null)
				continue;
			
			if (token.Name.contains(name))
				return Slots.indexOf(token);
		}
		
		return -1;
	}
	
	public int getItemIndexWithNameLike(String name)
	{
		for (SlotToken token : Slots)
		{
			// Stupid json crap giving me null values.
			if (token == null)
				continue;
			
			if (token.Name == null)
				continue;
			
			if (token.Name.contains(name))
				return Slots.indexOf(token);
		}
		
		return -1;
	}

	public void addItem(Item item, int index)
	{		
		SlotToken token = Slots.get(index);

		token.Material = item.GetType().name();
		token.Amount = item.GetAmount();
		token.Name = item.GetName();

		ItemTokens -= item.getTokenCost();
	}
	
	public void removeItem(Item item, int index)
	{
		SlotToken token = Slots.get(index);

		token.Material = null;
		token.Amount = 0;
		token.Name = null;
		
		ItemTokens += item.getTokenCost();
	}
}
