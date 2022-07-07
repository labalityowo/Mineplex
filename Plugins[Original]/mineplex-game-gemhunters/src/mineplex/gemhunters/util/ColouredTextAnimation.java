package mineplex.gemhunters.util;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import mineplex.core.common.util.UtilTextMiddle;

public class ColouredTextAnimation
{

	private final String _text;
	private final String _prefix;
	private final String _suffix;
	private final String[] _colours;

	private final double _colourChange;
	private final double _colourRequirement;

	// Stage 0
	private int _lastIndex;
	private double _colour;
	private int _colourIndex;

	// Stage 1
	private int _iterations;
	
	// Stage 2
	private int _colourStage;
	
	private String _last;
	
	private int _stage;

	public ColouredTextAnimation(String text, String... colours)
	{
		this(text, null, null, colours);
	}

	public ColouredTextAnimation(String text, String prefix, String suffix, String[] colours)
	{
		_text = text;
		_prefix = prefix;
		_suffix = suffix;
		_colours = colours;

		_colourChange = (double) 1 / text.length() * 2;
		_colourRequirement = (double) 1 / colours.length;

		_lastIndex = text.length() / 2;
		_colour = 0;
		_colourIndex = 0;

		_iterations = 0;
		
		_colourStage = 0;
		
		_stage = 0;
	}

	public boolean displayAsTitle(Player... players)
	{
		String text = next();

		UtilTextMiddle.display(text, null, 0, 20, 20, players);

		return _stage == -1;
	}

	private String next()
	{
		String display = "";

		switch (_stage)
		{
		case 0:
			String text = _text.substring(_lastIndex, _text.length() - _lastIndex);
			String colour = _colours[_colourIndex];

			if (_colour >= _colourRequirement * (_colourIndex + 1))
			{
				_colourIndex++;
			}

			_colour += _colourChange;
			_lastIndex--;

			if (_lastIndex == -1)
			{
				_stage++;
			}

			display = colour + text;
			break;
		case 1:
			_iterations++;
			
			if (_iterations > 4)
			{
				_stage++;
			}
			
			display = _last;
			break;
		case 2:
			_colourStage++;
			
			if (_colourStage > 10)
			{
				// Stop the cycle
				_stage = -1;
			}
			
			display = _colours[_colourStage % _colours.length] + ChatColor.stripColor(_last);
			break;
		default:
			break;
		}

		_last = display;
		return _prefix + display + _suffix;
	}

}
