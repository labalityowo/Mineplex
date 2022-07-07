package mineplex.minecraft.game.core;

import org.bukkit.entity.Player;

public interface IRelation 
{
	public boolean canHurt(Player a, Player b);
	public boolean canHurt(String a, String b);
	public boolean isSafe(Player a);
}
