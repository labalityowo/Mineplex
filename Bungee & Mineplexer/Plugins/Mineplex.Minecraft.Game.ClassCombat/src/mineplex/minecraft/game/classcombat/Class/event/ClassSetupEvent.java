package mineplex.minecraft.game.classcombat.Class.event;

import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.classcombat.Class.repository.token.CustomBuildToken;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ClassSetupEvent extends Event 
{
	public enum SetupType
	{
		OpenMain,
		
		ApplyDefaultBuilt,
		
		OpenCustomBuilds,
		
		ApplyCustomBuild,
		SaveEditCustomBuild,
		EditCustomBuild,
		DeleteCustomBuild,
		
		Close
	}
	
    private static final HandlerList handlers = new HandlerList();
    
    private Player _player;
    private SetupType _actionType;
    private ClassType _classType;
    private int _position = 0; //0 = Null, 1-5 = Slot
    private CustomBuildToken _classBuild;
    
    private boolean _cancelled = false;

    public ClassSetupEvent(Player player, SetupType type, ClassType classType, int position, CustomBuildToken build) 
    {
    	_player = player;
    	
    	_actionType = type;
    	_classType = classType;
    	
    	_position = position;
    	
    	_classBuild = build;
    }
  
    public HandlerList getHandlers() 
    {
        return handlers;
    }
 
    public static HandlerList getHandlerList() 
    {
        return handlers;
    }

	public SetupType GetType() 
	{
		return _actionType;
	}

	public int GetPosition() 
	{
		return _position;
	}
	
	public Player GetPlayer()
	{
		return _player;
	}

	public ClassType GetClassType() 
	{
		return _classType;
	}
	
	public CustomBuildToken GetBuild()
	{
		return _classBuild;
	}
	
	public void SetCancelled(boolean cancel)
	{
		_cancelled = cancel;
	}
	
	public boolean IsCancelled()
	{
		return _cancelled;
	}
}