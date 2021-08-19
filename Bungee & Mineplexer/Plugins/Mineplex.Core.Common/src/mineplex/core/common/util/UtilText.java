package mineplex.core.common.util;

import javax.imageio.ImageIO;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;

public class UtilText
{
	private static Map<String, String> _leetReplace = new HashMap<>();
	private static Map<Character, Integer> _characters = new HashMap<>();
	private static Map<Character, BufferedImage> _characterImages = new HashMap<>();

	private final static char[] VOWELS = new char[]{'a', 'e', 'i', 'o', 'u'};

	static
	{
		_leetReplace.put("4", "a");
		_leetReplace.put("3", "e");
		_leetReplace.put("6", "d");
		_leetReplace.put("1", "i");
		_leetReplace.put("0", "o");
		_leetReplace.put("5", "s");
		_leetReplace.put("7", "t");
		_leetReplace.put("_", "");

		try
		{
			InputStream inputStream = UtilText.class.getResourceAsStream("/ascii.png");
			BufferedImage image = ImageIO.read(inputStream);

			char[] text = new char[]
				{
						' ',
						'!',
						'"',
						'#',
						'$',
						'%',
						'&',
						'\'',
						'(',
						')',
						'*',
						'+',
						',',
						'-',
						'.',
						'/',
						'0',
						'1',
						'2',
						'3',
						'4',
						'5',
						'6',
						'7',
						'8',
						'9',
						':',
						';',
						'<',
						'=',
						'>',
						'?',
						'@',
						'A',
						'B',
						'C',
						'D',
						'E',
						'F',
						'G',
						'H',
						'I',
						'J',
						'K',
						'L',
						'M',
						'N',
						'O',
						'P',
						'Q',
						'R',
						'S',
						'T',
						'U',
						'V',
						'W',
						'X',
						'Y',
						'Z',
						'[',
						'\\',
						']',
						'^',
						'_',
						'`',
						'a',
						'b',
						'c',
						'd',
						'e',
						'f',
						'g',
						'h',
						'i',
						'j',
						'k',
						'l',
						'm',
						'n',
						'o',
						'p',
						'q',
						'r',
						's',
						't',
						'u',
						'v',
						'w',
						'x',
						'y',
						'z',
						'{',
						'|',
						'}',
						'~'
				};

			int x = 0;
			int y = 16;

			for (char c : text)
			{
				grab(c, image, x, y);

				if (x < 15 * 8)
				{
					x += 8;
				}
				else
				{
					x = 0;
					y += 8;
				}
			}

			inputStream.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static String center(String string, LineFormat lineFormat)
	{
		int length = getLength(string);

		if (length > lineFormat.getLength())
		{
			return string;
		}

		// Get the number of empty pixels on both sides of the string
		int div = (int) Math.floor((lineFormat.getLength() - length) / 2D);

		div -= 2; // For the gap between the strings

		return fillLine(" ", div) + string + fillLine(" ", div);
	}
	
	public static String alignRight(String string, LineFormat lineFormat)
	{
		int length = getLength(string);

		if (length > lineFormat.getLength())
		{
			return string;
		}

		// Get the number of empty pixels on both sides of the string
		int div = lineFormat.getLength() - length;

		div -= 1; // For the gap between the strings

		return fillLine(" ", div) + string;
	}

	public static String centerChat(String string, LineFormat lineFormat)
	{
		int length = getLength(string);

		if (length > lineFormat.getLength())
		{
			return string;
		}

		// Get the number of empty pixels on both sides of the string
		int div = (int) Math.floor(((lineFormat.getLength() + 10) - length) / 2D);

		div -= 2; // For the gap between the strings

		return fillLine(" ", div) + string;
	}

	public static String substringPixels(String string, int cutoff)
	{
		int len = 0;

		char[] array = string.toCharArray();
		boolean bold = false;

		for (int i = 0; i < array.length; i++)
		{
			char c = array[i];

			if (c == '�')
			{
				if (++i < array.length)
				{
					ChatColor color = ChatColor.getByChar(array[i]);

					if (color != null)
					{
						if (color.equals(ChatColor.BOLD))
						{
							bold = true;
						}
						else if (color.equals(ChatColor.RESET) || color.isColor())
						{
							bold = false;
						}
					}
				}

				continue;
			}

			if (!_characters.containsKey(c))
			{
				continue;
			}

			int toAdd = _characters.get(c);

			if (bold)
			{
				toAdd++;
			}

			if (len + toAdd > cutoff)
			{
				return string.substring(0, Math.max(0, i - 1));
			}

			if (i + 1 < array.length)
			{
				len++;
			}
		}

		return string;
	}

	public static String[] splitLinesToArray(String[] strings, LineFormat lineFormat)
	{
		ArrayList<String> lineList = splitLines(strings, lineFormat);

		String[] lineArray = new String[lineList.size()];
		lineArray = lineList.toArray(lineArray);

		return lineArray;
	}

	public static ArrayList<String> splitLines(String[] strings, LineFormat lineFormat)
	{
		ArrayList<String> lines = new ArrayList<String>();

		for (String s : strings)
		{
			lines.addAll(splitLine(s, lineFormat));
		}

		return lines;
	}

	public static String[] splitLineToArray(String string, LineFormat lineFormat)
	{
		return splitLinesToArray(string.split("\n"), lineFormat);
	}

	public static ArrayList<String> splitLine(String string, int lineLength)
	{
		ArrayList<String> strings = new ArrayList<String>();

		// Ignore lines with #
		if (string.startsWith("#"))
		{
			strings.add(string.substring(1, string.length()));
			return strings;
		}

		// Empty
		if (string.equals("") || string.equals(" "))
		{
			strings.add(" ");
			return strings;
		}

		String current = "";
		int currentLength = 0;
		String[] split = string.split(" ");
		String colors = "";

		for (int i = 0; i < split.length; i++)
		{
			String word = split[i];
			int wordLength = getLength(colors + word);

			if (currentLength + wordLength + 4 > lineLength && !current.isEmpty())
			{
				strings.add(current);
				current = colors + word;
				currentLength = wordLength + 1;
				continue;
			}

			if (i != 0)
			{
				current += " ";
				currentLength += 4;
			}

			current += word;
			currentLength += wordLength;
			colors = ChatColor.getLastColors(current);
		}

		if (!current.isEmpty())
		{
			strings.add(current);
		}

		return strings;
	}

	public static ArrayList<String> splitLine(String string, LineFormat lineFormat)
	{
		return splitLine(string, lineFormat.getLength());
	}

	public static String fillLine(String filler, int maxPixels)
	{
		int pixels = getLength(filler);

		if (pixels <= 0)
		{
			return "";
		}

		String toReturn = "";
		int currentLen = 0;

		int offset = maxPixels % 4;
		boolean isOffset = false;

		if (offset > 0)
		{
			toReturn += C.Bold;
		}

		while (currentLen + pixels <= maxPixels)
		{
			currentLen += pixels + 1;
			toReturn += filler;

			if (offset-- > 0)
			{
				currentLen++;

				if (offset == 0)
				{
					isOffset = false;
					toReturn += ChatColor.RESET;
				}
			}
		}

		if (isOffset)
		{
			toReturn += ChatColor.RESET;
		}

		return toReturn;
	}

	public static boolean fitsOneLine(String string, LineFormat lineFormat)
	{
		return getLength(string) <= lineFormat.getLength();
	}

	public static int getLength(String string)
	{
		int len = 0;

		char[] array = string.toCharArray();
		boolean bold = false;

		for (int i = 0; i < array.length; i++)
		{
			char c = array[i];

			if (c == '�')
			{
				if (++i < array.length)
				{
					ChatColor color = ChatColor.getByChar(array[i]);

					if (color != null)
					{
						if (color.equals(ChatColor.BOLD))
						{
							bold = true;
						}
						else if (color.equals(ChatColor.RESET) || color.isColor())
						{
							bold = false;
						}
					}
				}

				continue;
			}

			if (!_characters.containsKey(c))
			{
				continue;
			}

			len += _characters.get(c);

			if (bold)
			{
				len++;
			}

			if (i + 1 < array.length)
			{
				len++;
			}
		}

		return len;
	}

	private static void grab(Character character, BufferedImage image, int imageX, int imageY)
	{
		BufferedImage newImage = image.getSubimage(imageX, imageY, 8, 8);

		int width = 8;

		if (character == ' ')
		{
			width = 3;
		}
		else
		{
			for (int x = 0; x < 8; x++)
			{
				boolean isTransparentLine = true;

				for (int y = 0; y < 8; y++)
				{
					int pixel = image.getRGB(imageX + x, imageY + y);

					if ((pixel >> 24) != 0x00)
					{
						isTransparentLine = false;
						break;
					}
				}

				if (isTransparentLine)
				{
					width = x + 1;
					break;
				}
			}
		}

		newImage = newImage.getSubimage(0, 0, width, 8);

		_characterImages.put(character, newImage);
		_characters.put(character, width);
	}

	public static int getLength(char character)
	{
		if (!_characters.containsKey(character))
		{
			return 16;
		}

		return _characters.get(character);
	}

	public static BufferedImage getImage(char character)
	{
		if (!_characterImages.containsKey(character))
		{
			character = '?';
		}

		return _characterImages.get(character);
	}

	public static boolean isStringSimilar(String newString, String oldString, float matchRequirement)
	{
		if (newString.length() <= 3)
		{
			return newString.toLowerCase().equals(oldString.toLowerCase());
		}

		for (int i = 0; i < newString.length() * matchRequirement; i++)
		{
			int matchFromIndex = 0;

			// Look for substrings starting at i
			for (int j = 0; j < oldString.length(); j++)
			{
				// End of newString
				if (i + j >= newString.length())
				{
					break;
				}

				// Matched
				if (newString.charAt(i + j) == oldString.charAt(j))
				{
					matchFromIndex++;

					if (matchFromIndex >= newString.length() * matchRequirement)
						return true;
				}
				// No Match > Reset
				else
				{
					break;
				}
			}
		}

		return false;
	}

	public static <T> String listToString(Collection<T> inputList, boolean comma)
	{
		String out = "";

		for (T cur : inputList)
		{
			out += cur.toString() + (comma ? ", " : " ");
		}

		if (out.length() > 0)
		{
			out = out.substring(0, out.length() - (comma ? 2 : 1));
		}

		return out;
	}

	public static int lowerCaseCount(String input)
	{
		int count = 0;

		for (int k = 0; k < input.length(); k++)
		{

			char ch = input.charAt(k);
			if (Character.isLowerCase(ch))
				count++;

		}

		return count;
	}

	public static int upperCaseCount(String input)
	{
		int count = 0;

		for (int k = 0; k < input.length(); k++)
		{

			char ch = input.charAt(k);
			if (Character.isUpperCase(ch))
				count++;

		}

		return count;
	}

	public static String[] wrap(String text, int lineLength)
	{
		return wrap(text, lineLength, true);
	}

	public static String[] wrap(String text, int lineLength, boolean wrapLongerWords)
	{
		return WordUtils.wrap(text, lineLength, "\00D0", wrapLongerWords).split("\00D0");
	}

	public static String repeat(String txt, int times)
	{
		if (times <= 0)
		{
			return new String();
		}
		
		return new String(new byte[times]).replace("\0", txt);
	}

	public static boolean plural(int x)
	{
		return x != 1;
	}
	
	public static String plural(String word, int amount)
	{
		if(!plural(amount)) return word;
		String sufix = "s";
		if(word.endsWith("s") || word.endsWith("x") || word.endsWith("z") || word.endsWith("ch")) sufix = "es";
		else if(word.endsWith("y"))
		{
			word.substring(0, word.length()-2);
			sufix = "ies";
		}
		return word + sufix;
	}

	public static String trim(int maxLength, String s)
	{
		return s.length() <= maxLength ? s : s.substring(0, maxLength);
	}

	public static <X> String arrayToString(X[] array, String delimiter)
	{
		StringBuilder string = new StringBuilder();

		int index = 0;
		for (X x : array)
		{
			string.append(x.toString());

			if (index != array.length - 1)
			{
				string.append(delimiter);
			}

			index++;
		}

		return string.toString();
	}

	public static <X> String arrayToString(X[] array)
	{
		return arrayToString(array, null);
	}
	
	public static String getProgress(String prefix, double amount, String suffix, boolean progressDirectionSwap)
	{
		return getProgress(prefix, amount, suffix, progressDirectionSwap, 24); 
	}

	public static String getProgress(String prefix, double amount, String suffix, boolean progressDirectionSwap, int bars)
	{
		return getProgress(prefix, amount, suffix, progressDirectionSwap, bars, C.cRed, C.cGreen);
	}
	
	public static String getProgress(String prefix, double amount, String suffix, boolean progressDirectionSwap, int bars, String emptyColor, String fillColor)
	{
		if (progressDirectionSwap)
			amount = 1 - amount;
		
		//Generate Bar
		String progressBar = fillColor + "";
		boolean colorChange = false;
		for (int i=0 ; i<bars ; i++)
		{
			if (!colorChange && (float)i/(float)bars >= amount)
			{
				progressBar += emptyColor;
				colorChange = true;
			}
			
			progressBar += "▌";
		}

		return(prefix == null ? "" : prefix + ChatColor.RESET + " ") + progressBar + (suffix == null ? "" : ChatColor.RESET + " " + suffix);
	}

	public static String possesive(String possesiveNoun, String noun)
	{
		if (possesiveNoun == null || noun == null)
		{
			return "???";
		}
		
		if (possesiveNoun.isEmpty() || noun.isEmpty())
		{
			return "???";
		}
		
		return possesiveNoun.endsWith("s") ? possesiveNoun + "' " + noun : possesiveNoun + "'s " + noun;
	}
	
	public static boolean startsWithVowel(String word)
	{
		if(word == null || word.isEmpty()) return false;
		
		char v = word.toLowerCase().charAt(0);
		
		for(char c : VOWELS)
		{
			if(c == v) return true;
		}
		return false;
	}
	
	public static String getPronoun(String word)
	{
		return startsWithVowel(word) ? "an" : "a";
	}
	
	public static String prefixPronoun(String word)
	{
		return getPronoun(word) + " " + word;
	}
	
	/**
	 * Do a replaceAll on all strings in the array. It will replace the strings inside
	 * the given array. The returned array is the same instance as the one provided.
	 */
	public static String[] replaceAll(String[] array, String regex, String replacement)
	{
		if(array == null) return null;
		
		for(int i = 0; i < array.length; i++)
		{
			if(array[i] == null) continue;
			array[i] = array[i].replaceAll(regex, replacement);
		}
		return array;
	}

	public static String colorWords(String str, ChatColor... colors)
	{
		int c = 0, maxC = colors.length - 1;
		StringBuilder stringBuilder = new StringBuilder();
		for (String word : str.split(" "))
		{
			stringBuilder.append(colors[c]);
			stringBuilder.append(word + " ");
			if (c < maxC)
				c++;
			else
				c = 0;
		}
		return stringBuilder.toString();
	}

	/**
	 * Creates an image based on a string
	 * Removed from EffectLib
	 * @param font The font that is used
	 * @param s The string
	 * @return A buffered image containing the text
	 */
	public static BufferedImage stringToBufferedImage(Font font, String s)
	{
		BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics g = img.getGraphics();
		g.setFont(font);

		FontRenderContext frc = g.getFontMetrics().getFontRenderContext();
		Rectangle2D rect = font.getStringBounds(s, frc);
		g.dispose();

		img = new BufferedImage((int) Math.ceil(rect.getWidth()), (int) Math.ceil(rect.getHeight()), BufferedImage.TYPE_4BYTE_ABGR);
		g = img.getGraphics();
		g.setColor(Color.black);
		g.setFont(font);

		FontMetrics fm = g.getFontMetrics();
		int x = 0;
		int y = fm.getAscent();

		g.drawString(s, x, y);
		g.dispose();

		return img;
	}

	static final int MIN_VALUE = 1;
	static final int MAX_VALUE = 3999;
	static final String[] RN_M = {"", "M", "MM", "MMM"};
	static final String[] RN_C = {"", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM"};
	static final String[] RN_X = {"", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC"};
	static final String[] RN_I = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX"};

	public static String toRomanNumeral(int number)
	{
		if (number < MIN_VALUE || number > MAX_VALUE)
		{
			throw new IllegalArgumentException(
					String.format(
							"The number must be in the range [%d, %d]",
							MIN_VALUE,
							MAX_VALUE
					)
			);
		}

		return RN_M[number / 1000] +
				RN_C[number % 1000 / 100] +
				RN_X[number % 100 / 10] +
				RN_I[number % 10];
	}

	public static String replaceLeet(String in)
	{
		if (in.trim().isEmpty())
		{
			return in;
		}

		for (Map.Entry<String, String> entry : _leetReplace.entrySet())
		{
			in = in.replaceAll(entry.getKey(), entry.getValue());
		}

		return in;
	}

	public static String capitalise(String input)
	{
		return Character.toUpperCase(input.charAt(0)) + input.toLowerCase().substring(1);
	}
}