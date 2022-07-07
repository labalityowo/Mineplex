package nautilus.game.arcade.game.games.minecraftleague.variation;

import java.lang.reflect.InvocationTargetException;

import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.games.minecraftleague.DataLoc;
import nautilus.game.arcade.game.games.minecraftleague.MinecraftLeague;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class VariationManager implements Listener
{
	private MinecraftLeague _host;
	
	private GameVariation _variation;
	//private VariationType _selected;
	
	public VariationManager(MinecraftLeague host)
	{
		_host = host;
		Bukkit.getPluginManager().registerEvents(this, _host.getArcadeManager().getPlugin());
	}
	
	public GameVariation getSelected()
	{
		return _variation;
	}
	
	public void selectVariation()
	{
		VariationType type = null;
		if (_host.getMapVariantIDS().size() != 1)
			type = VariationType.STANDARD;
		else
		{
			type = VariationType.getFromID(_host.getMapVariantIDS().get(0).replace(DataLoc.VARIANT_BASE.getKey(), ""));
		}
		
		try
		{
			_variation = type.getVariation().getConstructor(MinecraftLeague.class).newInstance(_host);
			//_selected = type;
		}
		catch (NoSuchMethodException ex)
		{
			System.err.println("Is the constructor for " + type.toString() + " using only one argument?");
			ex.printStackTrace();
			return;
		}
		catch (InvocationTargetException ex)
		{
			ex.getCause().printStackTrace();
			return;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return;
		}
		
		//Bukkit.broadcastMessage(type.getDisplayMessage());
		Bukkit.getPluginManager().registerEvents(_variation, _host.getArcadeManager().getPlugin());
		_variation.ParseData();
	}
	
	public void deregister()
	{
		if (_variation != null)
			_variation.deregister();
		
		HandlerList.unregisterAll(this);
	}
	
	@EventHandler
	public void handleDeregister(GameStateChangeEvent event)
	{
		if (event.GetGame() != _host)
			return;
		
		if (event.GetState() != GameState.Dead)
			return;
		
		deregister();
	}
	
	@EventHandler
	public void onInform(PlayerJoinEvent event)
	{
		if (_variation != null)
		{
			//event.getPlayer().sendMessage(_selected.getDisplayMessage());
		}
	}
}
