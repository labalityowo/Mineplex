package mineplex.minecraft.game.core.condition.conditions;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import mineplex.minecraft.game.core.condition.Condition;
import mineplex.minecraft.game.core.condition.ConditionManager;

public class Silence extends Condition
{
	public Silence(ConditionManager manager, String reason, LivingEntity ent,
			LivingEntity source, ConditionType type, int mult, int ticks,
			boolean add, Material visualType, byte visualData,
			boolean showIndicator) 
	{
		super(manager, reason, ent, source, type, mult, ticks, add, visualType,
				visualData, showIndicator, false);
	}

	@Override
	public void Add() 
	{
		if (_ent instanceof Player)
			((Player)_ent).playSound(_ent.getLocation(), Sound.BAT_HURT, 0.8f, 0.8f);
	}

	@Override
	public void Remove() 
	{
		
	}
}
