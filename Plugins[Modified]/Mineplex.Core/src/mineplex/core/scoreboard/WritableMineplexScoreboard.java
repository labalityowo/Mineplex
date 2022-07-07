package mineplex.core.scoreboard;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <pre>
 * The WritableMineplexScoreboard is a version of the MineplexScoreboard which does not require you to
 * register your own lines, but instead allows you to write to it as if it were a canvas.
 *
 * As soon as the class is created, it is ready to be written to
 *
 * The general process of using this class is something like the following:
 *
 * * Call {@link #write} up to 15 times for each line in the scoreboard
 * * When ready, call {@link #draw} to draw the lines onto the scoreboard
 * * Upon drawing, the buffer of lines will be cleared. This allows you to use {@link #write} again, starting from scratch
 *
 * This class is optimized to minimize updates when possible, so it is alright to redraw the same line
 *   over and over; they will not be sent to the player
 * </pre>
 */
public class WritableMineplexScoreboard extends MineplexScoreboard
{
	// The constant list of 15 lines which will be used
	private final List<WritableScoreboardLine> _lines;

	// The list of lines currently buffered for drawing
	protected final List<String> _bufferedLines = new ArrayList<>(15);

	// The list of lines which were drawn in the last draw
	private final List<String> _drawnLines = new ArrayList<>(15);

	/**
	 * Creates a new WritableMineplexScoreboard with no owner
	 */
	public WritableMineplexScoreboard()
	{
		this(null);
	}

	/**
	 * Creates a new WritableMineplexScoreboard with the specified owner
	 *
	 * @param owner The owner of this scoreboard
	 */
	public WritableMineplexScoreboard(Player owner)
	{
		super(owner);

		List<WritableScoreboardLine> lines = new ArrayList<>();
		for (int i = 0; i < 15; i++)
		{
			lines.add(new WritableScoreboardLine());
		}
		_lines = Collections.unmodifiableList(lines);
	}

	/**
	 * Appends the given line to the buffer. This will not update the scoreboard
	 *
	 * @param line The line to buffer
	 */
	public void write(String line)
	{
		_bufferedLines.add(line);
	}

	/**
	 * Appends a blank line to the buffer
	 */
	public void writeNewLine()
	{
		write("");
	}

	/**
	 * Draws the currently buffered lines to the actual scoreboard, then flushes the buffers to allow rewriting
	 */
	public void draw()
	{
		if (_bufferedLines.size() > 15)
		{
			throw new IllegalStateException("Too many lines! (" + _bufferedLines.size() + " > 15)");
		}

		if (_bufferedLines.size() > _drawnLines.size())
		{
			for (int i = _drawnLines.size(); i < _bufferedLines.size(); i++)
			{
				super.register(_lines.get(i));
			}
		}
		else if (_bufferedLines.size() < _drawnLines.size())
		{
			for (int i = _bufferedLines.size(); i < _drawnLines.size(); i++)
			{
				super.unregister(_lines.get(i));
			}
		}

		recalculate();

		for (int i = 0; i < _bufferedLines.size(); i++)
		{
			get(_lines.get(i)).write(_bufferedLines.get(i));
		}

		this._drawnLines.clear();
		this._drawnLines.addAll(this._bufferedLines);
		this._bufferedLines.clear();
	}

	/**
	 * Resets the currently buffered lines
	 */
	public void reset()
	{
		this._bufferedLines.clear();
	}

	@Override
	public MineplexScoreboard register(ScoreboardLine line)
	{
		throw new IllegalArgumentException("You cannot register lines with a WritableMineplexScoreboard!");
	}

	@Override
	public MineplexScoreboard unregister(ScoreboardLine line)
	{
		throw new IllegalArgumentException("You cannot unregister lines with a WritableMineplexScoreboard!");
	}

	/**
	 * Simply an implementation of ScoreboardLine. More may be added later for metadata purposes
	 */
	private class WritableScoreboardLine implements ScoreboardLine
	{

	}
}
