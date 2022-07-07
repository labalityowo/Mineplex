package mineplex.minecraft.game.classcombat.shop;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.achievement.Achievement;
import mineplex.core.achievement.AchievementManager;
import mineplex.minecraft.game.classcombat.Class.ClassManager;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;
import mineplex.minecraft.game.classcombat.item.ItemFactory;

public class ClassShopManager extends MiniPlugin
{
	public enum Perm implements Permission
	{
		SKILL_UNLOCK,
		SKILL_UNLOCK_LEGACY,
	}

	private ClassManager _classManager;
	private SkillFactory _skillFactory;
	private ItemFactory _itemFactory;
	private AchievementManager _achievementManager;
	private CoreClientManager _clientManager;
	
	public ClassShopManager(JavaPlugin plugin, ClassManager classManager, SkillFactory skillFactory, ItemFactory itemFactory, AchievementManager achievementManager, CoreClientManager clientManager)
	{
		super("Class Shop Manager", plugin);
		
		_classManager = classManager;
		_skillFactory = skillFactory;
		_itemFactory = itemFactory;
		_achievementManager = achievementManager;
		_clientManager = clientManager;
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{
		PermissionGroup.CONTENT.setPermission(Perm.SKILL_UNLOCK, true, true);
		PermissionGroup.TRAINEE.setPermission(Perm.SKILL_UNLOCK, true, true);
		PermissionGroup.ULTRA.setPermission(Perm.SKILL_UNLOCK_LEGACY, true, true);
	}
	
	public ClassManager GetClassManager()
	{
		return _classManager;
	}

	public SkillFactory GetSkillFactory()
	{
		return _skillFactory;
	}

	public ItemFactory GetItemFactory()
	{
		return _itemFactory;
	}
	
	public boolean hasAchievements(Player player)
	{
		if (_clientManager.Get(player).hasPermission(Perm.SKILL_UNLOCK))
			return true;
		
		return _achievementManager.hasCategory(player, new Achievement[] 
		{
			Achievement.CHAMPIONS_ASSASSINATION,
			Achievement.CHAMPIONS_EARTHQUAKE,
			Achievement.CHAMPIONS_MASS_ELECTROCUTION,
			Achievement.CHAMPIONS_THE_LONGEST_SHOT,
			Achievement.CHAMPIONS_WINS,
			Achievement.CHAMPIONS_CAPTURES,
			Achievement.CHAMPIONS_CLUTCH,
			Achievement.CHAMPIONS_SPECIAL_WIN
		});
	}
}