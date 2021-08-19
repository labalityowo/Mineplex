package mineplex.minecraft.game.classcombat.Skill.Global;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.core.common.util.UtilAction;
import mineplex.minecraft.game.classcombat.Skill.Skill;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;

public class Swim extends Skill
{
	public Swim(SkillFactory skills, String name, ClassType classType, SkillType skillType, int cost, int levels) 
	{
		super(skills, name, classType, skillType, cost, levels);

		SetDesc(new String[] 
				{
				"Tap Crouch to Swim forwards."
				});
	}
	
	@Override
	public String GetEnergyString()
	{
		return "Energy: 5";
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void Crouch(PlayerToggleSneakEvent event)
	{
		if (event.isCancelled())
			return;
		
		Player player = event.getPlayer();

		//Water
		if (player.getLocation().getBlock().getTypeId() != 8 && player.getLocation().getBlock().getTypeId() != 9)
			return;

		//Level
		int level = getLevel(player);
		
		if (level == 0)		
			return;

		//Recharge
		if (!mineplex.core.recharge.Recharge.Instance.use(player, GetName(), GetName(level), 800, false, false))
			return;

		if (!Factory.Energy().Use(player, GetName(level), 5, true, false))
			return;

		//Action
		UtilAction.velocity(player, 0.6, 0.2, 0.6, false);

		//Effect
		player.getWorld().playSound(player.getLocation(), Sound.SPLASH, 0.3f, 2f);
	}

	@Override
	public void Reset(Player player) 
	{

	}
}
