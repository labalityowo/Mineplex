package mineplex.core.noteblock;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;

import org.bukkit.entity.Player;

import mineplex.core.lifetimes.SimpleLifetime;

public class SingleRunNotePlayer extends LoopedNotePlayer
{

	private final SimpleLifetime _lifetime;

	public SingleRunNotePlayer(NoteSong song, Player player)
	{
		this(song, Collections.singleton(player));
	}

	public SingleRunNotePlayer(NoteSong song, Predicate<Player> shouldPlay)
	{
		this(new SimpleLifetime(), song, null, shouldPlay);
	}

	public SingleRunNotePlayer(NoteSong song, Collection<Player> listeners)
	{
		this(new SimpleLifetime(), song, listeners, null);
	}

	public SingleRunNotePlayer(NoteSong song, Collection<Player> listeners, Predicate<Player> shouldPlay)
	{
		this(new SimpleLifetime(), song, listeners, shouldPlay);
	}

	private SingleRunNotePlayer(SimpleLifetime lifetime, NoteSong song, Collection<Player> listeners, Predicate<Player> shouldPlay)
	{
		super(lifetime, song, listeners, shouldPlay);

		_lifetime = lifetime;
		lifetime.register(this);
	}

	public SingleRunNotePlayer start()
	{
		if (!_lifetime.isActive())
		{
			_lifetime.start();
		}

		return this;
	}

	public void end()
	{
		if (_lifetime.isActive())
		{
			_lifetime.end();
		}
	}

	@Override
	protected void onSongEnd()
	{
		super.onSongEnd();

		_lifetime.end();
	}

	@Override
	public SimpleLifetime getLifetime()
	{
		return _lifetime;
	}
}
