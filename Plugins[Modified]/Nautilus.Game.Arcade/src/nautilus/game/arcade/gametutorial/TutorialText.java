package nautilus.game.arcade.gametutorial;

import org.bukkit.Sound;


public class TutorialText
{
 
	private String _text;
	private String _title;
	private int _stayTime;
	private int _id;
	private Sound _sound;
	
	public TutorialText(String title, String text, int stayTime, int id, Sound sound)
	{
		_text = text;
		_title = title;
		_id = id;
		_stayTime = stayTime;
		_sound = sound;
	}
	
	public TutorialText(String title, String text, int id, Sound sound)
	{
		this(title, text, (int) (Math.round(1.2 * text.length()) + 25), id, sound);
	}
	
	public TutorialText(String text, int id)
	{
		this(null, text, (int) (Math.round(1.2 * text.length()) + 25), id, Sound.NOTE_PLING);
	}
	
	public TutorialText(String text, int id, Sound sound)
	{
		this(null, text, (int) (Math.round(1.2 * text.length()) + 25), id, sound);
	}
	
	public TutorialText(String text, int stayTime, int id)
	{
		this(null, text, stayTime, id, Sound.NOTE_PLING);
	}
	
	public String getTitle()
	{
		return _title;
	}
	
	public String getText()
	{
		return _text;
	}
	
	public int ID()
	{
		return _id;
	}
	
	public Sound getSound()
	{
		return _sound;
	}
	
	public int getStayTime()
	{
		return _stayTime;
	}
	
	public void setText(String text)
	{
		_text = text;
	}
	
	public void setID(int id)
	{
		_id = id;
	}
	
}
