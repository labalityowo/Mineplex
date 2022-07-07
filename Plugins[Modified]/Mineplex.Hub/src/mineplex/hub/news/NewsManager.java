package mineplex.hub.news;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.entity.Entity;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextTop;
import mineplex.core.common.util.UtilTime;
import mineplex.core.disguise.disguises.DisguiseBase;
import mineplex.core.disguise.disguises.DisguiseWither;
import mineplex.core.gadget.gadgets.morph.MorphWither;
import mineplex.core.gadget.gadgets.mount.types.MountDragon;
import mineplex.core.gadget.types.Gadget;
import mineplex.core.gadget.types.GadgetType;
import mineplex.core.updater.UpdateType;
import mineplex.core.utils.UtilScheduler;
import mineplex.hub.HubManager;
import mineplex.hub.news.command.NewsCommand;
import mineplex.hub.news.redis.NewsUpdateCommand;
import mineplex.serverdata.commands.ServerCommandManager;

@ReflectivelyCreateMiniPlugin
public class NewsManager extends MiniPlugin
{

	public enum Perm implements Permission
	{

		NEWS_COMMAND,
		NEWS_ADD_COMMAND,
		NEWS_DELETE_COMMAND,
		NEWS_LIST_COMMAND,
		NEWS_REFRESH_COMMAND
	}

	private static final long DISPLAY_NEWS_TIME = TimeUnit.SECONDS.toMillis(5);

	private final HubManager _manager;
	private final NewsRepository _repository;

	private List<NewsElement> _elements;
	private long _lastDisplay;
	private int _newsIndex;

	private boolean _enabled = true;

	private NewsManager()
	{
		super("News");

		_manager = require(HubManager.class);

		_repository = new NewsRepository();
		_elements = new ArrayList<>();

		require(SalesBoardManager.class);

		generatePermissions();

		fetchNews();
		UtilScheduler.runEvery(UpdateType.FASTEST, this::displayNews);

		ServerCommandManager.getInstance().registerCommandType(NewsUpdateCommand.class, command -> _elements = command.getElements());
	}

	private void generatePermissions()
	{
		PermissionGroup.ADMIN.setPermission(Perm.NEWS_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.NEWS_ADD_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.NEWS_DELETE_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.NEWS_LIST_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.NEWS_REFRESH_COMMAND, true, true);
	}

	@Override
	public void addCommands()
	{
		addCommand(new NewsCommand(this));
	}

	public void setEnabled(boolean enabled)
	{
		_enabled = enabled;
	}

	public boolean isEnabled()
	{
		return _enabled;
	}

	private void displayNews()
	{
		if (!_enabled || _elements.isEmpty())
		{
			return;
		}

		if (UtilTime.elapsed(_lastDisplay, DISPLAY_NEWS_TIME))
		{
			if (++_newsIndex == _elements.size())
			{
				_newsIndex = 0;
			}

			_lastDisplay = System.currentTimeMillis();
		}

		String text = _elements.get(_newsIndex).getValue();
		double healthPercent = _newsIndex / (double) (_elements.size() - 1);

		UtilTextTop.displayProgress(text, healthPercent, UtilServer.getPlayers());

		// Fix Pet names
		for (Entity pet : _manager.getPetManager().getPets())
		{
			DisguiseBase disguise = _manager.GetDisguise().getActiveDisguise(pet);

			if (disguise instanceof DisguiseWither)
			{
				((DisguiseWither) disguise).setName(text);
				disguise.resendMetadata();
			}
		}

		// Fix Dragon Mount names
		for (Gadget mount : _manager.GetGadget().getGadgets(GadgetType.MOUNT))
		{
			if (mount instanceof MountDragon)
			{
				((MountDragon) mount).SetName(text);
			}
		}

		// Fix Wither Morph names
		for (Gadget gadget : _manager.GetGadget().getGadgets(GadgetType.MORPH))
		{
			if (gadget instanceof MorphWither)
			{
				((MorphWither) gadget).setWitherData(text, healthPercent);
			}
		}
	}

	private void fetchNews()
	{
		runAsync(() -> _repository.fetchNews(elements -> _elements = elements));
	}

	public void refreshNews()
	{
		runAsync(() -> new NewsUpdateCommand(_elements).publish());
	}

	public void addNews(String value)
	{
		runAsync(() -> _repository.insertNews(element -> _elements.add(0, element), value));
	}

	public NewsElement deleteNews(int id)
	{
		for (NewsElement element : _elements)
		{
			if (element.getId() == id)
			{
				runAsync(() -> _repository.deleteNews(element));
				_elements.remove(element);
				return element;
			}
		}

		return null;
	}

	public List<NewsElement> getElements()
	{
		return _elements;
	}
}
