package nautilus.game.arcade.game.games.draw;

import java.util.ArrayList;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;

import nautilus.game.arcade.kit.Kit;

public class DrawRound 
{
	public Draw Host;

	public Player Drawer;
	public String Word;
	public int Revealed = 0;
	public boolean[] WordReveal;
	public long Time;
	public double TimeMax;
	public double TimeChooseMax = 10;
	public HashSet<String> Guessed = new HashSet<String>(); 
	public ArrayList<String> WordChoices;

	public DrawRound(Draw draw, Player player)
	{
		Host = draw;

		Drawer = player;
	
		Time = System.currentTimeMillis();
		
		//Kit Modification
		Kit kit = draw.GetKit(player);
		
		//Time
		TimeMax = 60;
		
		//Word
		SetWord(draw.GetWord());
	}

	public void SetWord(String word)
	{
		Word = word;
		
		WordReveal = new boolean[word.length()];
		for (int i=0 ; i< WordReveal.length ; i++)
			WordReveal[i] = false;
		
		Time = System.currentTimeMillis();
		
		UtilPlayer.message(Drawer, C.cWhite + C.Bold + "You must draw: " + C.cGreen + C.Bold + Word);
		
		Host.Manager.GetChat().setChatSilence(0, false);
	}
	
	public boolean Guessed(Player player)
	{
		if (Guessed.add(player.getName()))
		{
			TimeMax -= 5;

			Bukkit.getPluginManager().callEvent(new DrawGuessCorrectlyEvent(player, this, Guessed.size()));

			return true;
		}

		return false;
	}

	public void UpdateReveal()
	{
		int required = (int) (((System.currentTimeMillis()-Time)/1000d) / (TimeMax*1.75) * (double)Word.length());

		if (Revealed < required)
		{
			for (int i=0 ; i<50 ; i++)
			{
				int j = UtilMath.r(WordReveal.length);

				if (WordReveal[j] == false && Word.charAt(j) != ' ')
				{
					WordReveal[j] = true;
					Revealed++;

					for (Player player : UtilServer.getPlayers())
					{
						if (Host.GetTeam(ChatColor.RED).HasPlayer(player))
							continue;
						
						player.playSound(player.getLocation(), Sound.CHICKEN_EGG_POP, 1.5f, 2f);
					}

					break;
				}
			}
		}
	}

	public String GetRevealedWord()
	{
		String out = "";

		for (int i=0 ; i<Word.length() ; i++)
		{
			if (Word.charAt(i) == ' ')
			{
				out += "  ";
			}
			else if (WordReveal[i])
			{
				out += Word.charAt(i) + " ";
			}
			else
			{
				out += "_ ";
			}
		}

		if (out.length() > 0)
			out = out.substring(0, out.length()-1);

		return out;
	}

	public boolean IsDone()
	{
		UpdateReveal();

		return UtilTime.elapsed(Time, (long)(TimeMax * 1000));
	}

	public double GetTimePercent() 
	{
		if (!IsReady())
			return (TimeChooseMax - (double)(System.currentTimeMillis()-Time)/1000d)/TimeChooseMax;
		
		return (TimeMax - (double)(System.currentTimeMillis()-Time)/1000d)/TimeMax;
	}
	
	public String GetTimeString() 
	{
		if (!IsReady())
			return UtilTime.MakeStr((long)(TimeChooseMax*1000 - (double)(System.currentTimeMillis()-Time)));
		
		return UtilTime.MakeStr((long)(TimeMax*1000 - (double)(System.currentTimeMillis()-Time)));
	}

	public boolean AllGuessed(ArrayList<Player> players) 
	{
		for (Player player : players)
			if (!Guessed.contains(player.getName()))
				return false;

		return true;
	}

	public Player getDrawer()
	{
		return Drawer;
	}

	public boolean ChooseWordUpdate()
	{
		if (Word == null && UtilTime.elapsed(Time, (long)(TimeChooseMax*1000)))
		{
			if (WordChoices.isEmpty())
			{
				SetWord(Host.GetWord());
			}
			else
			{
				SetWord(UtilAlg.Random(WordChoices));
			}
		}

		return IsReady();
	}

	public boolean IsReady()
	{
		return Word != null;
	}

	public void WordClicked(String string)
	{
		if (IsReady())
			return;
		
		if (WordChoices.contains(string))
		{
			SetWord(string);
		}
	}
}
