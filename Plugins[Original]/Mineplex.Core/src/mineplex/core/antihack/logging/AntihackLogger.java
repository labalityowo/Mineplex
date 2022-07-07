package mineplex.core.antihack.logging;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZ;
import org.tukaani.xz.XZOutputStream;

import com.google.gson.Gson;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.antihack.AntiHack;
import mineplex.core.antihack.logging.builtin.PartyInfoMetadata;
import mineplex.core.antihack.logging.builtin.PlayerInfoMetadata;
import mineplex.core.antihack.logging.builtin.ServerInfoMetadata;
import mineplex.core.antihack.logging.builtin.ViolationInfoMetadata;
import mineplex.core.command.CommandBase;
import mineplex.core.common.jsonchat.ClickEvent;
import mineplex.core.common.jsonchat.JsonMessage;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilColor;
import mineplex.core.common.util.UtilPlayer;

@ReflectivelyCreateMiniPlugin
public class AntihackLogger extends MiniPlugin
{
	public static final Gson GSON = new Gson();
	private final static String READABLE_NAME = "GWEN";

	public enum Perm implements Permission
	{
		SAVE_METADATA_COMMAND,
	}

	private final CoreClientManager _clientManager = require(CoreClientManager.class);

	private final Map<String, AnticheatMetadata> _metadata = new HashMap<>();

	private final AnticheatDatabase _db;

	private AntihackLogger()
	{
		super("AnticheatPlugin");

		_db = new AnticheatDatabase();

		registerMetadata(new ServerInfoMetadata());
		registerMetadata(new ViolationInfoMetadata());
		registerMetadata(new PartyInfoMetadata());
		registerMetadata(new PlayerInfoMetadata());
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{
		PermissionGroup.ADMIN.setPermission(Perm.SAVE_METADATA_COMMAND, true, true);
		PermissionGroup.QA.setPermission(Perm.SAVE_METADATA_COMMAND, true, true);
	}

	public void addCommands()
	{
		addCommand(new CommandBase<AntihackLogger>(this, Perm.SAVE_METADATA_COMMAND, "savemetadata", "smeta")
		{
			@Override
			public void Execute(Player caller, String[] args)
			{
				if (args.length != 1)
				{
					UtilPlayer.message(caller, F.main(READABLE_NAME, "Usage: " + F.elem("/smeta <player>")));
					return;
				}

				Player player = Bukkit.getPlayer(args[0]);
				if (player != null)
				{
					JsonObject custom = new JsonObject();
					custom.addProperty("is-test-metadata", true);
					String id = AntiHack.generateId();
					saveMetadata(player, id, () ->
					{
						new JsonMessage(READABLE_NAME + "> ")
								.color(UtilColor.chatColorToJsonColor(ChatColor.BLUE))
								.extra("Saved metadata for ")
								.color(UtilColor.chatColorToJsonColor(ChatColor.GRAY))
								.extra(player.getName())
								.color(UtilColor.chatColorToJsonColor(ChatColor.YELLOW))
								.extra(" with id ")
								.color(UtilColor.chatColorToJsonColor(ChatColor.GRAY))
								.extra(id)
								.color(UtilColor.chatColorToJsonColor(ChatColor.GREEN))
								.click(ClickEvent.OPEN_URL, String.format("https://frozor.io/gwen/meta/%s", id))
								.sendToPlayer(caller);
					}, custom);
				}
				else
				{
					UtilPlayer.message(caller, F.main(READABLE_NAME, "That player doesn't exist!"));
				}
			}
		});
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event)
	{
		_metadata.values().forEach(metadata -> metadata.remove(event.getPlayer().getUniqueId()));
	}

	public void saveMetadata(Player player, String id, Runnable after, JsonObject custom)
	{
		runAsync(() ->
		{
			JsonObject info = new JsonObject();

			for (AnticheatMetadata anticheatMetadata : _metadata.values())
			{
				try
				{
					info.add(anticheatMetadata.getId(), anticheatMetadata.build(player.getUniqueId()));
				}
				catch (Throwable t)
				{
					t.printStackTrace();
				}
			}

			info.add("custom", custom == null ? JsonNull.INSTANCE: custom);

			String str = GSON.toJson(info);
			byte[] b = str.getBytes(StandardCharsets.UTF_8);

			ByteArrayOutputStream bout = new ByteArrayOutputStream();

			try
			{
				XZOutputStream o2 = new XZOutputStream(bout, new LZMA2Options(LZMA2Options.PRESET_MIN), XZ.CHECK_NONE);
				o2.write(b);
				o2.close();
			}
			catch (IOException ex)
			{
				// Should never happen
				ex.printStackTrace();
			}

			String base64 = Base64.getEncoder().encodeToString(bout.toByteArray());

			_db.saveMetadata(_clientManager.getAccountId(player), id, base64, after);
		});
	}

	public void registerMetadata(AnticheatMetadata metadata)
	{
		if (!_metadata.containsKey(metadata.getId()))
		{
			_metadata.put(metadata.getId(), metadata);
		}
		else
		{
			throw new IllegalArgumentException("Attempting to register: " + metadata.getId());
		}
	}
}