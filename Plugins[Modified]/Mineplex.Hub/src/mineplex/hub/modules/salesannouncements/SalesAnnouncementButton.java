package mineplex.hub.modules.salesannouncements;

import org.bukkit.event.inventory.ClickType;

public class SalesAnnouncementButton extends SalesAnnouncementGUIButton
{
	private SalesAnnouncementData _data;
	private SalesAnnouncementPage _page;
	
	public SalesAnnouncementButton(SalesAnnouncementData data, SalesAnnouncementPage page)
	{
		super(data.getButtonForm());
		_data = data;
		_page = page;
	}
	
	public int getId()
	{
		return _data.getId();
	}

	@Override
	public void update()
	{
		Button = _data.getButtonForm();
	}

	@Override
	public void handleClick(ClickType type)
	{
		if (type == ClickType.RIGHT)
		{
			_page.deleteAnnouncement(_data);
		}
		else
		{
			_page.toggleAnnouncement(_data);
		}
	}
}