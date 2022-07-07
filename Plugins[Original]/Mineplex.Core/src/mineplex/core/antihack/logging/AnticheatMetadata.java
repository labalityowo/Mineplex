package mineplex.core.antihack.logging;

import java.util.UUID;

import org.bukkit.event.Listener;

import com.google.gson.JsonElement;

import mineplex.core.common.util.UtilServer;

public abstract class AnticheatMetadata implements Listener
{
	public AnticheatMetadata()
	{
		UtilServer.RegisterEvents(this);
	}

	public abstract String getId();

	public abstract JsonElement build(UUID player);

	public abstract void remove(UUID player);
}