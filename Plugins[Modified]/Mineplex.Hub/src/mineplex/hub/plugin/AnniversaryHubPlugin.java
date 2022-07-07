package mineplex.hub.plugin;

public class AnniversaryHubPlugin extends HubPlugin
{

	public AnniversaryHubPlugin()
	{
		super("Anniversary");
	}

	@Override
	protected void setupWorld()
	{
		_manager.GetSpawn().getWorld().setTime(18000);
	}
}
