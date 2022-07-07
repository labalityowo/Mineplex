package mineplex.core.titles.tracks.custom;

import net.md_5.bungee.api.ChatColor;

import mineplex.core.common.util.C;

public class ScrollAnimation
{

	private final String _input;
	private String _primary;
	private String _secondary;
	private String _tertiary;
	private boolean _bold;

	public ScrollAnimation(String input)
	{
		_input = input;
		_primary = ChatColor.WHITE.toString();
		_secondary = _primary;
		_tertiary = _primary;
	}

	public ScrollAnimation withPrimaryColour(ChatColor colour)
	{
		_primary = colour.toString();
		return this;
	}

	public ScrollAnimation withSecondaryColour(ChatColor colour)
	{
		_secondary = colour.toString();
		return this;
	}

	public ScrollAnimation withTertiaryColour(ChatColor colour)
	{
		_tertiary = colour.toString();
		return this;
	}

	public ScrollAnimation bold()
	{
		_bold = true;
		return this;
	}

	public String[] build()
	{
		String[] output = new String[_input.length() * 2];
		String[] primaryRun = getFrames(_primary, _secondary);
		String[] secondaryRun = getFrames(_secondary, _primary);

		System.arraycopy(primaryRun, 0, output, 0, _input.length());
		System.arraycopy(secondaryRun, 0, output, _input.length(), _input.length());

		return output;
	}

	private String[] getFrames(String primary, String secondary)
	{
		String[] output = new String[_input.length()];

		for (int i = 0; i < _input.length(); i++)
		{
			StringBuilder builder = new StringBuilder(_input.length() * 3)
					.append(primary)
					.append(_bold ? C.Bold : "");

			for (int j = 0; j < _input.length(); j++)
			{
				char c = _input.charAt(j);

				if (j == i)
				{
					builder.append(_tertiary).append(_bold ? C.Bold : "");
				}
				else if (j == i + 1)
				{
					builder.append(secondary).append(_bold ? C.Bold : "");
				}

				builder.append(c);
			}

			output[i] = builder.toString();
		}

		return output;
	}

}
