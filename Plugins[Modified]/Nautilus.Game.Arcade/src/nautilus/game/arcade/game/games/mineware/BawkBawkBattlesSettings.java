package nautilus.game.arcade.game.games.mineware;

/**
 * This class contains a list of fields that are used as settings for the main game class.
 */
public class BawkBawkBattlesSettings
{
	private boolean _crumbling = false;
	private boolean _waiting = true;
	private boolean _messagesSent = false;
	private boolean _sendingMessages = false;
	private boolean _challengeStarted = false;

	public void setCrumbling(boolean flag)
	{
		_crumbling = flag;
	}

	public boolean isCrumbling()
	{
		return _crumbling;
	}

	public void setWaiting(boolean flag)
	{
		_waiting = flag;
	}

	public boolean isWaiting()
	{
		return _waiting;
	}

	public void markMessagesAsSent(boolean flag)
	{
		_messagesSent = flag;
	}

	public boolean areMessagesSent()
	{
		return _messagesSent;
	}

	public void markMessagesAsSending(boolean flag)
	{
		_sendingMessages = flag;
	}

	public boolean areMessagesBeingSent()
	{
		return _sendingMessages;
	}

	public void setChallengeStarted(boolean flag)
	{
		_challengeStarted = flag;
	}

	public boolean isChallengeStarted()
	{
		return _challengeStarted;
	}
}
