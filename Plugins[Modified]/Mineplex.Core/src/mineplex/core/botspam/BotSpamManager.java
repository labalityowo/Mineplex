package mineplex.core.botspam;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.botspam.command.BotSpamCommand;
import mineplex.core.botspam.repository.BotSpamRepository;
import mineplex.core.message.PrivateMessageEvent;
import mineplex.core.punish.Category;
import mineplex.core.punish.Punish;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.serverdata.commands.ServerCommandManager;

public class BotSpamManager extends MiniPlugin
{
	public enum Perm implements Permission
	{
		BYPASS_BOTSPAM,
		BOTSPAM_COMMAND,
		ADD_BOTSPAM_COMMAND,
		TOGGLE_BOTSPAM_COMMAND,
		UPDATE_BOTSPAM_COMMAND,
		LIST_BOTSPAM_COMMAND,
	}

	private Punish _punish;
	private CoreClientManager _clientManager;
	private BotSpamRepository _repository;

	private volatile List<SpamText> _spam = new ArrayList<>();

	public BotSpamManager(JavaPlugin plugin, CoreClientManager clientManager, Punish punish)
	{
		super("SpamBot Manager", plugin);

		_punish = punish;
		_clientManager = clientManager;
		_repository = new BotSpamRepository(plugin);
		_spam = _repository.getSpamText();

		ServerCommandManager.getInstance().registerCommandType(ForceUpdateCommand.class, command -> runAsync(() -> _spam = _repository.getSpamText()));

		generatePermissions();
	}

	private void generatePermissions()
	{

		PermissionGroup.TRAINEE.setPermission(Perm.BYPASS_BOTSPAM, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.BOTSPAM_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.ADD_BOTSPAM_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.TOGGLE_BOTSPAM_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.UPDATE_BOTSPAM_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.LIST_BOTSPAM_COMMAND, true, true);
	}

	@EventHandler
	public void onPrivateMessage(PrivateMessageEvent event)
	{
		Player recipient = event.getRecipient();
		// Ignore messages sent to staff members
		if (_clientManager.Get(recipient).hasPermission(Perm.BYPASS_BOTSPAM))
			return;
		
		// Ignore messages sent by staff members
		if (_clientManager.Get(event.getSender()).hasPermission(Perm.BYPASS_BOTSPAM))
			return;

		for (SpamText spamText : _spam)
		{
			if (spamText.isEnabled() && spamText.isSpam(event.getMessage()))
			{
				punishBot(event.getSender(), spamText);
				event.setCancelled(true);
				return;
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onChat(final AsyncPlayerChatEvent event)
	{
		// Ignore messages sent by staff members
		if (_clientManager.Get(event.getPlayer()).hasPermission(Perm.BYPASS_BOTSPAM))
			return;
		
		for (final SpamText spamText : _spam)
		{
			if (spamText.isEnabled() && spamText.isSpam(event.getMessage()))
			{
				runSync(() -> punishBot(event.getPlayer(), spamText));
				event.setCancelled(true);
				return;
			}
		}
	}

	public void punishBot(Player player, final SpamText botText)
	{
		_punish.AddPunishment(player.getName(), Category.Other, "Bot Spam #" + botText.getId(), "Chiss", 1, true, -1, true);

		// Update bot text count
		runAsync(() -> _repository.addPunishment(botText));
	}

	public void addSpamText(final String caller, final String text, final Runnable callback)
	{
		runAsync(() ->
		{
			_repository.addSpamText(caller, text);
			_spam = _repository.getSpamText();

			if (callback != null)
			{
				runSync(callback);
			}
		});
	}

	public void enableSpamText(final String caller, final SpamText spamText, final Runnable callback)
	{
		runAsync(() ->
		{
			_repository.enableSpamText(caller, spamText);

			runSync(() ->
			{
				spamText.setEnabled(true);
				spamText.setEnabledBy(caller);

				if (callback != null)
				{
					callback.run();
				}
			});
		});
	}

	public void disableSpamText(final String caller, final SpamText spamText, final Runnable callback)
	{
		runAsync(() ->
		{
			_repository.disableSpamText(caller, spamText);

			runSync(() ->
			{
				spamText.setEnabled(false);
				spamText.setDisabledBy(caller);

				if (callback != null)
				{
					callback.run();
				}
			});
		});
	}

	public List<SpamText> getSpamTexts()
	{
		return _spam;
	}

	@EventHandler
	public void updateText(UpdateEvent event)
	{
		if (event.getType() == UpdateType.MIN_01)
		{
			runAsync(() -> _spam = _repository.getSpamText());
		}
	}

	@Override
	public void addCommands()
	{
		addCommand(new BotSpamCommand(this));
	}
}