package mineplex.minecraft.game.core.condition.conditions;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import mineplex.core.Managers;
import mineplex.core.visibility.VisibilityManager;
import mineplex.minecraft.game.core.condition.Condition;
import mineplex.minecraft.game.core.condition.ConditionManager;

public class Cloak extends Condition
{
	public Cloak(ConditionManager manager, String reason, LivingEntity ent,
			LivingEntity source, ConditionType type, int mult, int ticks,
			boolean add, Material visualType, byte visualData,
			boolean showIndicator) 
	{
		super(manager, reason, ent, source, type, mult, ticks, add, visualType,
				visualData, showIndicator, false);

		_informOn = "You are now invisible.";
		_informOff = "You are no longer invisible.";
	}
	
	@Override
	public boolean needsForceRemove()
	{
		return true;
	}

	@Override
	public void Add() 
	{
		if (!(_ent instanceof Player))
			return;
		
		VisibilityManager vm = Managers.require(VisibilityManager.class);
		
		Bukkit.getOnlinePlayers().forEach(player ->
		{
			vm.hidePlayer(player, (Player)_ent, "Cloaking " + _reason);
		});
			
		for (Entity ent : _ent.getWorld().getEntities())
		{
			if (!(ent instanceof Creature))
				continue;
			
			Creature creature = (Creature)ent;
			
			if (creature.getTarget() != null && !creature.getTarget().equals(_ent))
				continue;
			
			creature.setTarget(null);
		}
	}

	@Override
	public void Remove() 
	{
		super.Remove();
		
		VisibilityManager vm = Managers.require(VisibilityManager.class);
		
		Bukkit.getOnlinePlayers().forEach(player ->
		{
			vm.showPlayer(player, (Player)_ent, "Cloaking " + _reason);
		});
	}
}