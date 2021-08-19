package mineplex.serverdata.commands;


public class AnnouncementCommand extends ServerCommand
{
	private boolean _displayTitle;
	private String _rank;
	private String _message;
	
	public boolean getDisplayTitle() { return _displayTitle; }
	public String getRank() { return _rank; }
	public String getMessage() { return _message; }
	
	public AnnouncementCommand(boolean displayTitle, String rank, String message)
	{
		_displayTitle = displayTitle;
		_rank = rank;
		_message = message;
	}
	
	@Override
	public void run() 
	{
		// Utilitizes a callback functionality to seperate dependencies
	}
}
