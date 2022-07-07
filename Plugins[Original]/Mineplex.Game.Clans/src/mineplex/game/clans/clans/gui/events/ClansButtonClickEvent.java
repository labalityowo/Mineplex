package mineplex.game.clans.clans.gui.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ClansButtonClickEvent  extends Event
{
    private static final HandlerList handlers = new HandlerList();

    private Player _player;
    private ButtonType _type;

    private boolean _cancelled;

    public ClansButtonClickEvent(Player player, ButtonType type)
    {
        _player = player;
        _type = type;
    }

    public Player getPlayer()
    {
        return _player;
    }

    public void setCancelled(boolean cancelled)
    {
        _cancelled = cancelled;
    }

    public boolean isCancelled()
    {
        return _cancelled;
    }

    public ButtonType getButtonType() {
        return _type;
    }

    public HandlerList getHandlers()
    {
        return handlers;
    }

    public static HandlerList getHandlerList()
    {
        return handlers;
    }


    public enum ButtonType
    {
        AddAlly,
        AddTrusted,
        AddWar,
        Create,
        Disband,
        Energy,
        Invite,
        Join,
        Leave,
        Member,
        Territory,
        Who;
    }
}
