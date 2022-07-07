package mineplex.core.poll;

/**
 * Created by Shaun on 8/16/2014.
 */
public class Poll
{
	private int _id;
	private boolean _enabled;
	private int _coinReward;
	private String _question;
	private String[] _answers;
	private DisplayType _displayType;

	public Poll(int id, boolean enabled, int coinReward, String question, String answerA, String answerB, String answerC, String answerD, DisplayType displayType)
	{
		_id = id;
		_enabled = enabled;
		_coinReward = coinReward;
		_question = question;
		_answers = new String[4];
		_answers[0] = answerA;
		_answers[1] = answerB;
		_answers[2] = answerC;
		_answers[3] = answerD;
		_displayType = displayType;
	}

	public int getId()
	{
		return _id;
	}

	public String getQuestion()
	{
		return _question;
	}

	public String[] getAnswers()
	{
		return _answers;
	}

	public boolean hasAnswer(int number)
	{
		return number > 0 && number <= _answers.length && _answers[number - 1] != null && _answers[number - 1].length() > 0;
	}

	public boolean isEnabled()
	{
		return _enabled;
	}

	public int getCoinReward()
	{
		return _coinReward;
	}

	public DisplayType getDisplayType()
	{
		return _displayType;
	}

	@Override
	public boolean equals(Object object)
	{
		if (object instanceof Poll)
		{
			return ((Poll) object).getId() == getId();
		}
		return false;
	}
	
	@Override
	public int hashCode()
	{
		return Integer.hashCode(getId());
	}
}