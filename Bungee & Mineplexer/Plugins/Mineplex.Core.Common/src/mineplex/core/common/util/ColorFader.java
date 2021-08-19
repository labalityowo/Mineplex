package mineplex.core.common.util;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ColorFader {

	private final int _loopsBetween;
	
	private final List<RGBData> _colors;
	private final LoopIterator<RGBData> _iterator;
	private int _loopsSinceLast;
	
	public ColorFader(int loopsBetweenColors, RGBData... colors)
	{
		this(loopsBetweenColors, Arrays.asList(colors));
	}
	
	public ColorFader(int loopsBetweenColors, List<RGBData> colors)
	{
		_loopsBetween = loopsBetweenColors;
		
		_colors = new LinkedList<>(colors);
		_iterator = new LoopIterator<>(_colors);
	}
	
	public RGBData next()
	{
		RGBData rgb;
		
		if (_loopsSinceLast >= _loopsBetween)
		{
			rgb = _iterator.next();
			_loopsSinceLast = 0;
		}
		else
		{
			int redStep = (_iterator.peekNext().getFullRed() - _iterator.current().getFullRed()) / _loopsBetween;
			int greenStep = (_iterator.peekNext().getFullGreen() - _iterator.current().getFullGreen()) / _loopsBetween;
			int blueStep = (_iterator.peekNext().getFullBlue() - _iterator.current().getFullBlue()) / _loopsBetween;
			
			int red = _iterator.current().getFullRed();
			int green = _iterator.current().getFullGreen();
			int blue = _iterator.current().getFullBlue();
			
			for (int i = 0; i < _loopsSinceLast; i++)
			{
				red += redStep;
				green += greenStep;
				blue += blueStep;
			}
			
			rgb = new RGBData(red, green, blue);
		}
		
		_loopsSinceLast++;
		
		return rgb;
	}
	
}
