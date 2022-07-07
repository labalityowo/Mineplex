package mineplex.core.common.util;

public class UtilUI
{
	public static int[] getIndicesFor(int items, int startingLine, int newLinePadding)
	{
		return getIndicesFor(items, startingLine, 5, newLinePadding);
	}
	
	public static int[] getIndicesFor(int items, int startingLine)
	{
		return getIndicesFor(items, startingLine, 5, 0);
	}
	
	public static int[] getIndicesFor(int items, int startingLine, int itemsPerLine, int newLinePadding)
	{
		itemsPerLine = UtilMath.clamp(itemsPerLine, 1, 5);
		
		int[] indices = new int[items];
		
		int lines = (int) Math.ceil(items / ((double) itemsPerLine));
		for (int line = 0; line < lines; line++)
		{
			int itemsInCurLine = line == lines - 1 ? items - (line * itemsPerLine) : itemsPerLine;
			int startIndex = (startingLine * 9) + ((newLinePadding * 9) * line) + 9 * line - itemsInCurLine + 5;
			
			for (int item = 0; item < itemsInCurLine; item++)
			{
				indices[(line * itemsPerLine) + item] = startIndex + (item * 2);
			}
		}
		
		return indices;
	}
	
}
