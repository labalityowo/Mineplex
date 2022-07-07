package nautilus.game.arcade.game.games.hideseek.forms;

import nautilus.game.arcade.game.games.hideseek.HideSeek;

import org.bukkit.entity.Player;

public abstract class Form 
{
	public HideSeek Host;
	public Player Player;
	
	public Form(HideSeek host, Player player)
	{
		Host = host;
		Player = player;
	}
	
	public abstract void Apply();
	public abstract void Remove();
}
