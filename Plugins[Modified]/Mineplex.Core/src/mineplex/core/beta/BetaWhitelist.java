package mineplex.core.beta;

import java.util.Set;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import com.google.common.collect.ImmutableSet;

import mineplex.core.MiniPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.powerplayclub.PowerPlayClubRepository;

public class BetaWhitelist extends MiniPlugin
{
	private static final Set<UUID> EXTRA_PLAYERS = ImmutableSet.<UUID>builder()
			// GI Members
			.add(UUID.fromString("8506533f-1da7-4d5c-a835-a483b5a18b54")) // Awquard
			.add(UUID.fromString("a8526c97-95be-4cb7-ae58-7df5d3b108a6")) // ASlime
			.add(UUID.fromString("ae6d71b7-3d49-429f-b31f-5cf5af136540")) // Cabob
			.add(UUID.fromString("ea1f709c-031f-4028-8f7d-2073c5a37d1a")) // CharlieHacks
			.add(UUID.fromString("d3c1457a-1084-43e1-846c-addc47393b90")) // Chocobutter
			.add(UUID.fromString("6b60782e-f95b-4449-a39e-0ad7fa5fdab0")) // CosmoLink
			.add(UUID.fromString("18697323-50d3-47ea-a5c2-e7ac1a0d9fa0")) // Danah
			.add(UUID.fromString("1cc18d8d-ab28-4354-8cce-f93fb06423bf")) // Fetch
			.add(UUID.fromString("c56e5b96-8dc3-46ca-b682-24cf8467e3a1")) // KingOfWizards
			.add(UUID.fromString("ea30fe99-2044-438f-bfd8-97bcc639239e")) // Mauo
			.add(UUID.fromString("933b2f93-806a-4f39-88a2-935442418ae5")) // Tier4Global
			.add(UUID.fromString("ac239b94-3079-4a8a-a52f-7b81c8a87b4d")) // Paddi
			.add(UUID.fromString("3ced328d-f079-45e4-ad71-8c721c4a699b")) // Smaland47
			.add(UUID.fromString("d51fc65b-fce9-4464-9391-b259525dc6ca")) // SnitSays
			.add(UUID.fromString("12bbeda2-567a-400a-9d66-f76fab832de0")) // StoneColdKiller
			.add(UUID.fromString("2e0c1d88-7f44-44f5-85b4-9ad0b2cfddce")) // Tours
			.add(UUID.fromString("32aff2d0-f68c-4eb9-b5d4-139fc48b7ca6")) // Trimzon
			.add(UUID.fromString("3dcfe366-fcaa-48f7-abcc-b73fb62616e1")) // gamefish32
			.add(UUID.fromString("6795643a-2b61-41bf-9429-c7549fd128a8")) // umGim
			.add(UUID.fromString("47ba454a-4999-42f4-a269-2f4114ceb3c7")) // falconviii
			.build();

	public enum Perm implements Permission
	{
		BYPASS_WHITELIST,
	}

	private final CoreClientManager _clientManager;
	private final PowerPlayClubRepository _powerPlayClubRepository;

	public BetaWhitelist(CoreClientManager clientManager, PowerPlayClubRepository powerPlayRepository)
	{
		super("Beta Whitelist");
		_clientManager = clientManager;
		_powerPlayClubRepository = powerPlayRepository;
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{

		PermissionGroup.ULTRA.setPermission(Perm.BYPASS_WHITELIST, true, true);
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();
		if (_clientManager.Get(player).hasPermission(Perm.BYPASS_WHITELIST) // If this player is Ultra+
				|| _powerPlayClubRepository.getCachedData(player).isSubscribed() // a PPC subscriber,
				|| EXTRA_PLAYERS.contains(player.getUniqueId())) // or explicitly whitelisted,
		{
			return; // allow them in
		}

		// Otherwise, kick them out
		event.getPlayer().kickPlayer("Sorry, you aren't whitelisted on this beta server.\n\nSubscribe to " + ChatColor.GOLD + "Power Play Club " + ChatColor.WHITE + "at " + ChatColor.GREEN + "mineplex.com/shop" + ChatColor.WHITE + "!");
	}
}