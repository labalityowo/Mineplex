package mineplex.minecraft.game.classcombat.Skill.Mage;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;

import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.core.common.util.F;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.updater.UpdateType;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilPlayer;
import mineplex.minecraft.game.classcombat.Skill.SkillActive;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;

public class Fissure extends SkillActive 
{

	private final Set<FissureData> _active = new HashSet<>();
	
	public Fissure(SkillFactory skills, String name, ClassType classType, SkillType skillType, 
			int cost, int levels, 
			int energy, int energyMod, 
			long recharge, long rechargeMod, boolean rechargeInform, 
			Material[] itemArray, 
			Action[] actionArray) 
	{
		super(skills, name, classType, skillType,
				cost, levels,
				energy, energyMod, 
				recharge, rechargeMod, rechargeInform, 
				itemArray,
				actionArray);

		SetDesc(new String[] 
				{
				"Fissures the earth infront of you,",
				"creating an impassable wall.",
				"",
				"Players struck by the initial slam",
				"receive Slow 2 for #2#0.5 seconds",
				"",
				"Players struck by the fissure",
				"receive #2#0.4 damage plus an ",
				"additional #0.6#0.2 damage for",
				"every block fissure has travelled."
				});
	}

	@Override
	public boolean CustomCheck(Player player, int level) 
	{
		if (isInWater(player))
		{
			UtilPlayer.message(player, F.main("Skill", "You cannot use " + F.skill(GetName()) + " in water."));
			return false;
		}
		
		if (!UtilEnt.isGrounded(player))
		{
			UtilPlayer.message(player, F.main("Skill", "You cannot use " + F.skill(GetName()) + " while airborne."));
			return false;
		}

		return true;
	}

	@Override
	public void Skill(Player player, int level) 
	{
		FissureData data = new FissureData(this, player, level, player.getLocation().getDirection(), player.getLocation().add(0, -0.4, 0));
		_active.add(data);
		
		//Inform
		UtilPlayer.message(player, F.main(GetClassType().name(), "You used " + F.skill(GetName(level)) + "."));
	}
	
	@EventHandler
	public void Update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		_active.removeIf(data ->
		{
			if (data.Update())
			{
				data.Clear();
				return true;
			}

			return false;
		});
	}

	@Override
	public void Reset(Player player) 
	{

	}
}
