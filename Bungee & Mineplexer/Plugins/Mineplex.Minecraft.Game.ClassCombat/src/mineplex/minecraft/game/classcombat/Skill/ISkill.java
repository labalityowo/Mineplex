package mineplex.minecraft.game.classcombat.Skill;

import java.util.Set;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;

public interface ISkill
{
	public enum SkillType
	{ 
		Axe,
		Bow,
		Sword,
		PassiveA,
		PassiveB,
		GlobalPassive,
		Class,
	}
	
    String GetName();
    int getLevel(Entity ent);
    ClassType GetClassType();
    SkillType GetSkillType();
    int GetGemCost();
    int GetTokenCost();
    boolean IsFree();
    void setFree(boolean free);
    String[] GetDesc(int level);
    void Reset(Player player);
    
    Set<Player> GetUsers();
    void AddUser(Player player, int level);
    void RemoveUser(Player player);
    
	Integer GetSalesPackageId();
	int getMaxLevel();
	
	boolean isAchievementSkill();

	void setLocationFilter(LocationFilter locationFilter);
	LocationFilter getLocationFilter();
}
