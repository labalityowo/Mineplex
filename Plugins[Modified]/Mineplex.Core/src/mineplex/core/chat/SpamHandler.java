package mineplex.core.chat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import mineplex.core.Managers;
import mineplex.core.common.util.UtilServer;
import mineplex.core.packethandler.IPacketHandler;
import mineplex.core.packethandler.PacketHandler;
import mineplex.core.packethandler.PacketInfo;
import mineplex.core.slack.SlackAPI;
import mineplex.core.slack.SlackMessage;
import mineplex.core.slack.SlackTeam;
import net.minecraft.server.v1_8_R3.PacketPlayInChat;

public class SpamHandler implements IPacketHandler
{
	private static final int DETECTION_THRESHOLD = 30;
	
	private final Cache<String, Integer> _recentChat = CacheBuilder.newBuilder().expireAfterWrite(500, TimeUnit.MILLISECONDS).build();
	
	public SpamHandler()
	{
		Managers.require(PacketHandler.class).addPacketHandler(this, PacketPlayInChat.class);
	}

	@Override
	public void handle(PacketInfo packetInfo)
	{
		if (packetInfo.getPacket() instanceof PacketPlayInChat)
		{
			PacketPlayInChat chat = (PacketPlayInChat) packetInfo.getPacket();
			String message = chat.a();
			if (isFlagged(message))
			{
				packetInfo.setCancelled(true);
			}
		}
	}
	
	private boolean isFlagged(String message)
	{
		Optional<String> similar = chooseMostSimilar(message, _recentChat.asMap().keySet(), true);
		
		String mapKey = message;
		int newValue = 1;
		
		if (similar.isPresent())
		{
			mapKey = similar.get();
			try
			{
				newValue = _recentChat.get(similar.get(), () -> 0) + 1;
			}
			catch (ExecutionException e)
			{
				e.printStackTrace();
			}
		}
		
		_recentChat.put(mapKey, newValue);
		
		if (newValue >= DETECTION_THRESHOLD)
		{
			SlackAPI.getInstance().sendMessage(SlackTeam.DEVELOPER, "#spam-alert", new SlackMessage("Spambot Alert System", "heavy_exclamation_mark", "A spambot threat was detected on " + UtilServer.getRegion().name() + " " + UtilServer.getServerName() + ": MSG=\"" + message + "\" & OCCURRENCES=" + newValue), true);
			return true;
		}
		
		return false;
	}
	
	/**
	 * Compares two chars to see if they are the same.
	 *
	 * @param a The first char.
	 * @param b The second char.
	 * @param ignoreCase Whether to ignore case when making the comparison.
	 *
	 * @return <code>0</code> if the chars are the same. <code>1</code> if the chars are different.
	 */
	private int caseCheck(char a, char b, boolean ignoreCase)
	{
		if ((ignoreCase && Character.toUpperCase(a) == Character.toUpperCase(b)) || (!ignoreCase && a == b))
		{
			return 0;
		}

		return 1;
	}
	
	/**
	 * Determines how similar two provided strings are, via the Levenshtein distance.
	 * A return value of <code>0</code> indicates the two strings are identical.
	 *
	 * @param s The first string.
	 * @param t The second string.
	 * @param ignoreCase Whether to ignore case when comparing the strings.
	 *
	 * @return The value of similarity.
	 */
	private int checkSimilarity(String s, String t, boolean ignoreCase)
	{
		if ((ignoreCase && s.equalsIgnoreCase(t)) || (!ignoreCase && s.equals(t)))
		{
			return 0;
		}

		int n = s.length();
		int m = t.length();

		if (m == 0)
		{
			return n;
		}
		else if (n == 0)
		{
			return m;
		}

		int[][] matrix = new int[m + 1][n + 1];

		matrix[0][0] = 0;

		for (int i = 1; i <= m; i++)
		{
			matrix[i][0] = i;
		}

		for (int i = 1; i <= n; i++)
		{
			matrix[0][i] = i;
		}

		for (int nn = 1; nn <= n; nn++)
		{
			for (int mm = 1; mm <= m; mm++)
			{
				char i = s.charAt(nn - 1);
				char j = t.charAt(mm - 1);

				int a = matrix[mm - 1][nn] + 1;
				int b = matrix[mm][nn - 1] + 1;
				int c = matrix[mm - 1][nn - 1] + caseCheck(i, j, ignoreCase);

				matrix[mm][nn] = Math.min(a, Math.min(b, c));
			}
		}

		return matrix[m][n];
	}
	
	/**
	 * Attempts to find the string that is most similar to that provided out of a list. <br>
	 *
	 * This method uses a multitude of methods to attempt to find the most similar string.
	 * <ul>
	 *     <li>1) Ignore any empty or null strings.</li>
	 *     <li>2) Check if there are any exact matches.</li>
	 *     <li>3) Check based on number of matching words.</li>
	 *     <li>4) Check based on Levenshtein distance.</li>
	 * </ul>
	 *
	 * @param needle The provided string to check for.
	 * @param hay A list of strings to check against.
	 * @param ignoreCase Whether to ignore case when comparing strings and characters.
	 *
	 * @return The most likely candidate for a match.
	 */
	private Optional<String> chooseMostSimilar(String needle, Collection<String> hay, boolean ignoreCase)
	{
		Collection<String> haystack = new ArrayList<>(hay);

		haystack.removeAll(Arrays.asList("",null));

		if (haystack.size() == 0 || needle.length() == 0)
		{
			return Optional.empty();
		}

		for (String direct : haystack)
		{
			if ((ignoreCase && needle.equalsIgnoreCase(direct)) || (!ignoreCase && needle.equals(direct)))
			{
				return Optional.of(direct);
			}
		}

		String match = null;
		int matchCount = 0;
		boolean levenshteinFlag = true;

		String[] splitNeedle = needle.split(" ");

		for (String item : haystack)
		{
			String[] splitItem = item.split(" ");

			int words = 0;

			for (String n : splitNeedle)
			{
				for (String i : splitItem)
				{
					if ((ignoreCase && n.equalsIgnoreCase(i)) || (!ignoreCase && n.equals(i)))
					{
						words++;
					}
				}
			}

			if (words > 0)
			{
				if (match == null)
				{
					match = item;
					matchCount = words;
					levenshteinFlag = false;
				}
				else
				{
					if (matchCount == words)
					{
						levenshteinFlag = true;
					}
					else if (matchCount < words)
					{
						match = item;
						matchCount = words;
						levenshteinFlag = false;
					}
				}
			}
		}

		if (!levenshteinFlag)
		{
			return Optional.of(match);
		}
		else
		{
			List<String> similar = new ArrayList<>();
			int similarity = Integer.MAX_VALUE;

			for (String item : haystack)
			{
				if (needle.length() > item.length() * 2 || needle.length() < item.length() / 2)
				{
					continue;
				}

				int levenshteinValue = checkSimilarity(item, needle, ignoreCase);

				if (levenshteinValue > (item.length() / 2) + 1)
				{
					continue;
				}

				if (levenshteinValue < similarity)
				{
					similar.clear();
					similar.add(item);
					similarity = levenshteinValue;
				}
				else if (levenshteinValue == similarity)
				{
					similar.add(item);
				}
			}

			if (similar.size() == 0)
			{
				return Optional.empty();
			}
			else if (similar.size() == 1)
			{
				return Optional.of(similar.get(0));
			}
			else
			{
				String finalSim = null;

				for (String sim : similar)
				{
					if (caseCheck(sim.charAt(0), needle.charAt(0), ignoreCase) == 0)
					{
						if (finalSim == null)
						{
							finalSim = sim;
						}
						else
						{
							finalSim = null;
							break;
						}
					}
				}

				if (finalSim == null)
				{
					return Optional.empty();
				}
				else
				{
					return Optional.of(finalSim);
				}
			}
		}
	}
}