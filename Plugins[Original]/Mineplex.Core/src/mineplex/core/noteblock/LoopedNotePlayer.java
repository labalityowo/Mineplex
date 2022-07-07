package mineplex.core.noteblock;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ThreadFactory;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import mineplex.core.common.util.UtilServer;
import mineplex.core.lifetimes.Component;
import mineplex.core.lifetimes.Lifetime;
import mineplex.core.lifetimes.Lifetimed;

public class LoopedNotePlayer implements Runnable, Component, Lifetimed
{

	private static final ThreadFactory THREAD_FACTORY = new ThreadFactoryBuilder()
			.setNameFormat("NoteBlockPlayer %1$d")
			.build();

	private final Lifetime _lifetime;

	protected final NoteSong _song;
	protected final long _sleepMillis;

	private final Collection<Player> _listeners;
	private final Predicate<Player> _shouldPlay;

	private volatile boolean _playing;
	protected volatile int _tick;

	public LoopedNotePlayer(Lifetime lifetime, NoteSong song)
	{
		this(lifetime, song, (Predicate<Player>) null);
	}

	public LoopedNotePlayer(Lifetime lifetime, NoteSong song, Predicate<Player> shouldPlay)
	{
		this(lifetime, song, null, shouldPlay);
	}

	public LoopedNotePlayer(Lifetime lifetime, NoteSong song, Collection<Player> listeners)
	{
		this(lifetime, song, listeners, null);
	}

	private LoopedNotePlayer(Lifetime lifetime, NoteSong song, Player listener, int sleepMillis)
	{
		this(lifetime, song, sleepMillis, Collections.singleton(listener), null);

		activate();
	}

	public LoopedNotePlayer(Lifetime lifetime, NoteSong song, Collection<Player> listeners, Predicate<Player> shouldPlay)
	{
		this(lifetime, song, (long) (1000 / (song.getTempo() / 100D)), listeners, shouldPlay);
	}

	public LoopedNotePlayer(Lifetime lifetime, NoteSong song, long sleepMillis, Collection<Player> listeners, Predicate<Player> shouldPlay)
	{
		_lifetime = lifetime;

		_song = song;
		_sleepMillis = sleepMillis;

		_listeners = listeners;
		_shouldPlay = shouldPlay;

		setPlaying(true);
	}

	@Override
	public void activate()
	{
		THREAD_FACTORY.newThread(this).start();
	}

	@Override
	public void deactivate()
	{
	}

	@Override
	public void run()
	{
		while (getLifetime().isActive())
		{
			if (!_playing)
			{
				sleep();
				continue;
			}

			setTick(_tick + 1);

			if (_tick > _song.getLength())
			{
				onSongEnd();
			}

			playTick(_tick);
			sleep();
		}
	}

	protected void onSongEnd()
	{
		_tick = 1;
	}

	private void playTick(int tick)
	{
		Collection<Player> listeners = _listeners == null ? (Collection<Player>) UtilServer.getPlayersCollection() : _listeners;

		if (_shouldPlay != null)
		{
			listeners = listeners.stream()
					.filter(_shouldPlay)
					.collect(Collectors.toSet());
		}

		for (NoteLayer layer : _song.getLayerMap().values())
		{
			Note note = layer.getNote(tick);

			if (note != null)
			{
				Sound sound = UtilNote.getInstrumentSound(note.getInstrument());
				float volume = layer.getVolume() / 100F;
				float pitch = (float) UtilNote.getPitch(note.getNote() - 33);

				for (Player player : listeners)
				{
					player.playSound(player.getEyeLocation(), sound, volume, pitch);
				}
			}
		}
	}

	private void sleep()
	{
		try
		{
			Thread.sleep(_sleepMillis);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	protected void setTick(int tick)
	{
		_tick = tick;
	}

	public void setPlaying(boolean playing)
	{
		_playing = playing;
	}

	public LoopedNotePlayer cloneForPlayer(Player player, float tempoFactor)
	{
		return new LoopedNotePlayer(getLifetime(), _song, player, (int) (_sleepMillis * tempoFactor));
	}

	@Override
	public Lifetime getLifetime()
	{
		return _lifetime;
	}
}
