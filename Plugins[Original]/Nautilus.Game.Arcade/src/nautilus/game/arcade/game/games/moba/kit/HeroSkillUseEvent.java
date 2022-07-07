package nautilus.game.arcade.game.games.moba.kit;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class HeroSkillUseEvent extends PlayerEvent implements Cancellable
{

	private static final HandlerList _handlers = new HandlerList();

	private HeroSkill _skill;
	private boolean _cancelled;

	public HeroSkillUseEvent(Player who, HeroSkill skill)
	{
		super(who);

		_skill = skill;
	}

	public HeroSkill getSkill()
	{
		return _skill;
	}

	@Override
	public void setCancelled(boolean cancelled)
	{
		_cancelled = cancelled;
	}

	@Override
	public boolean isCancelled()
	{
		return _cancelled;
	}

	public static HandlerList getHandlerList()
	{
		return _handlers;
	}

	@Override
	public HandlerList getHandlers()
	{
		return getHandlerList();
	}

}
