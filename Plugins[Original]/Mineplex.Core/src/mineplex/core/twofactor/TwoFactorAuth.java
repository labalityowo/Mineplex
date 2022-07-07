package mineplex.core.twofactor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;

import mineplex.core.Managers;
import mineplex.core.MiniClientPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.command.CommandBase;
import mineplex.core.common.util.BukkitFuture;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayerBase;
import mineplex.core.common.util.UtilServer;
import mineplex.core.recharge.Recharge;
import mineplex.serverdata.commands.TwoFactorResetCommand;
import mineplex.serverdata.database.DBPool;

@ReflectivelyCreateMiniPlugin
public class TwoFactorAuth extends MiniClientPlugin<TwoFactorData>
{
	public enum Perm implements Permission
	{
		USE_2FA,
		RESET_2FA,
	}

	private final Map<UUID, String> setupData = new HashMap<>();
	private final Set<UUID> authenticating = new HashSet<>();

	private static final GoogleAuthenticator authenticator = new GoogleAuthenticator(
			new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder().setWindowSize(5).build()
	);
	private final CoreClientManager _clientManager = Managers.require(CoreClientManager.class);
	private final TwoFactorRepository _repository = new TwoFactorRepository(DBPool.getAccount());

	public TwoFactorAuth()
	{
		super("Two-factor Authentication");
		_clientManager.addStoredProcedureLoginProcessor(
				_repository.buildSecretKeyLoginProcessor((uuid, secretKey) -> Get(uuid).setSecretKey(secretKey))
		);
		_clientManager.addStoredProcedureLoginProcessor(
				_repository.buildLastIpLoginProcessor((uuid, ip) -> Get(uuid).setLastLoginIp(ip))
		);
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{

		PermissionGroup.BUILDER.setPermission(Perm.USE_2FA, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.RESET_2FA, true, true);
	}

	private void sendTokenRequest(Player player)
	{
		player.sendMessage(F.main("2FA", "Please enter your two-factor auth code"));
	}

	@Override
	public void addCommands()
	{
		addCommand(new CommandBase<TwoFactorAuth>(this, Perm.USE_2FA, "2fa", "tfa")
		{
			@Override
			public void Execute(Player caller, String[] args)
			{
				if (args.length < 1 || !args[0].toLowerCase().equals("reset"))
				{
					if (_clientManager.Get(caller).hasPermission(Perm.RESET_2FA))
					{
						caller.sendMessage(F.main("2FA", "Usage: /2fa reset [player]"));
					} else
					{
						caller.sendMessage(F.main("2FA", "Usage: /2fa reset"));
					}
					return;
				}

				if (args.length == 1) // Resetting their own 2FA
				{
					caller.sendMessage(F.main("2FA", "Resetting 2FA.."));
					runAsync(() ->
					{
						new TwoFactorResetCommand(caller.getName(), caller.getUniqueId().toString(), caller.getName(), caller.getUniqueId().toString()).publish();
					});
					_repository.deletePlayerData(_clientManager.getAccountId(caller)).whenComplete(BukkitFuture.<Void>complete((__, err) ->
					{
						if (err != null)
						{
							caller.sendMessage(F.main("2FA", "Something went wrong. Have you already reset 2FA?"));
							err.printStackTrace();
						}
						else
						{
							caller.sendMessage(F.main("2FA", "Successfully reset."));
							setup2FA(caller);
						}
					}));
					return;
				}

				if (!_clientManager.Get(caller).hasPermission(Perm.RESET_2FA))
				{
					UtilPlayerBase.message(caller, C.mHead + "Permissions> " + C.mBody + "You do not have permission to do that.");
					return;
				}

				_clientManager.getOrLoadClient(args[1], client ->
				{
					if (client == null)
					{
						caller.sendMessage(F.main("2FA", "Couldn't find player with the name \"" + args[1] + "\""));
						return;
					}

					caller.sendMessage(F.main("2FA", "Resetting 2FA for \"" + client.getName() + "\""));
					runAsync(() ->
					{
						new TwoFactorResetCommand(caller.getName(), caller.getUniqueId().toString(), client.getName(), client.getUniqueId() == null ? "null" : client.getUniqueId().toString()).publish();
					});
					_repository.deletePlayerData(client.getAccountId()).whenComplete(BukkitFuture.<Void>complete((__, err) ->
					{
						if (err != null)
						{
							caller.sendMessage(F.main("2FA", "Something went wrong. Maybe they've already reset 2FA?"));
						}
						else
						{
							caller.sendMessage(F.main("2FA", "Successfully reset."));
							if (client.GetPlayer() != null)
							{
								setup2FA(client.GetPlayer());
							}
						}
					}));
				});
			}
		});
	}

	public boolean isAuthenticating(Player player)
	{
		return authenticating.contains(player.getUniqueId()) || setupData.containsKey(player.getUniqueId());
	}

	private void setup2FA(Player player)
	{
		String secret = authenticator.createCredentials().getKey();

		MapView view = Bukkit.createMap(player.getWorld());
		for (MapRenderer renderer : view.getRenderers())
		{
			view.removeRenderer(renderer);
		}
		view.addRenderer(new TwoFactorMapRenderer(player, UtilServer.isTestServer() ? "Mineplex%20Test" : "Mineplex", secret));

		ItemStack stack = new ItemStack(Material.MAP);
		stack.setDurability(view.getId());

		// Find first free hotbar slot
		int slot = 0;
		for (int i = 0; i < 9; i++)
		{
			if (player.getInventory().getItem(i) == null)
			{
				slot = i;
				break;
			}
		}

		player.getInventory().setHeldItemSlot(slot);
		player.getInventory().setItemInHand(stack);

		setupData.put(player.getUniqueId(), secret);
		player.sendMessage(F.main("2FA", "Setting up two-factor authentication."));
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();

		TwoFactorData data = Get(player);

		if (data.getLastLoginIp().isPresent() && player.getAddress().getAddress().toString().substring(1).equals(data.getLastLoginIp().get()))
		{
			player.sendMessage(F.main("2FA", "Authenticated"));
			return;
		}

		if (data.getSecretKey().isPresent())
		{
			// Hooray 2FA
			sendTokenRequest(player);
			authenticating.add(player.getUniqueId());
		}
		else
		{
			// 2FA not set up yet.
			if (_clientManager.Get(player).hasPermission(Perm.USE_2FA))
			{
				runSync(() -> setup2FA(event.getPlayer()));
			}
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();
		if (setupData.remove(player.getUniqueId()) != null)
		{
			player.setItemInHand(null);
		}
		authenticating.remove(player.getUniqueId());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onChat(AsyncPlayerChatEvent event)
	{
		Player player = event.getPlayer();

		String secret = null; // Check setup data first

		if (setupData.containsKey(player.getUniqueId()))
		{
			secret = setupData.get(player.getUniqueId());
		}
		else if (authenticating.contains(player.getUniqueId()))
		{
			secret = Get(player).getSecretKey().get();
		}

		if (secret == null)
		{
			return;
		}

		// Hooray 2FA - let's see if their message matches their auth code

		event.setCancelled(true);

		int code;
		try
		{
			code = Integer.parseInt(event.getMessage().replaceAll(" ", ""));
		}
		catch (NumberFormatException e)
		{
			player.sendMessage(F.main("2FA", "Invalid authentication code (not a number)."));
			return;
		}

		if (!authenticator.authorize(secret, code))
		{
			player.sendMessage(F.main("2FA", "Invalid authentication code."));
			return;
		}

		// Success!

		player.sendMessage(F.main("2FA", "Authorized for 24 hours."));

		if (setupData.containsKey(player.getUniqueId()))
		{
			// Remove setup map + save secret
			player.setItemInHand(null);
			Get(player).setSecretKey(secret);

			player.sendMessage(F.main("2FA", "Saving secret.."));
			_repository.saveSecret(player, secret).whenComplete(BukkitFuture.<Void>complete((v, throwable) ->
			{
				if (!player.isOnline())
				{
					return;
				}

				if (throwable != null)
				{
					Get(player).setSecretKey(null);
					player.sendMessage(F.main("2FA", "Something went wrong. Please try again in a moment."));
				}
				else
				{
					player.sendMessage(F.main("2FA", "Secret key saved."));
				}
			}));
		}

		_repository.saveLogin(player, player.getAddress().getAddress().toString().substring(1));

		setupData.remove(player.getUniqueId());
		authenticating.remove(player.getUniqueId());
	}

	/**
	 *
	 * @param event - The event being called
	 * @param player - The player in question for this event
	 * @return Whether the player is currently authenticating and action had to be taken
	 */
	private <T extends Cancellable> boolean handleCancelAuth(T event, Player player)
	{
		if (isAuthenticating(player))
		{
			sendTokenRequestIfReady(player);
			event.setCancelled(true);
			return true;
		}

		return false;
	}

	private <T extends PlayerEvent & Cancellable> boolean handleCancelAuth(T event)
	{
		return handleCancelAuth(event, event.getPlayer());
	}

	private <T extends InventoryEvent & Cancellable> boolean handleCancelAuth(T event)
	{
		return handleCancelAuth(event, (Player)event.getView().getPlayer());
	}

	private boolean canSendTokenRequest(Player player)
	{
		return Recharge.Instance.use(player, "two-factor message cooldown", 3000L, false, false);
	}

	private void sendTokenRequestIfReady(Player player)
	{
		if (canSendTokenRequest(player))
		{
			sendTokenRequest(player);
		}
	}

	// Cancel relevant events

	@EventHandler(ignoreCancelled = true)
	public void onChangeHeldItem(PlayerItemHeldEvent event)
	{
		Player player = event.getPlayer();
		if (setupData.containsKey(player.getUniqueId()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onClick(InventoryClickEvent event)
	{
		handleCancelAuth(event);
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onDrag(InventoryDragEvent event)
	{
		handleCancelAuth(event);
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onDrop(PlayerDropItemEvent event)
	{
		handleCancelAuth(event);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onInteract(PlayerInteractEvent event)
	{
		handleCancelAuth(event);
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onInteractWithEntity(PlayerInteractEntityEvent event)
	{
		handleCancelAuth(event);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onCommand(PlayerCommandPreprocessEvent event)
	{
		if (handleCancelAuth(event))
		{
			event.setMessage("/");
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onMove(PlayerMoveEvent event)
	{
		Player player = event.getPlayer();
		if (isAuthenticating(player))
		{
			sendTokenRequestIfReady(player);
			event.getTo().setX(event.getFrom().getX());
			event.getTo().setZ(event.getFrom().getZ());
		}
	}

	@Override
	protected TwoFactorData addPlayer(UUID uuid)
	{
		return new TwoFactorData();
	}
}