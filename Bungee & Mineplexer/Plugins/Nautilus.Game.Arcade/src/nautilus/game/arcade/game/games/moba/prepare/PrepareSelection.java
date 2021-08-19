package nautilus.game.arcade.game.games.moba.prepare;

import mineplex.core.common.entity.ClientArmorStand;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.packethandler.IPacketHandler;
import mineplex.core.packethandler.PacketHandler.ListenerPriority;
import mineplex.core.packethandler.PacketInfo;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.moba.Moba;
import nautilus.game.arcade.game.games.moba.MobaPlayer;
import nautilus.game.arcade.game.games.moba.MobaRole;
import nautilus.game.arcade.game.games.moba.kit.HeroKit;
import nautilus.game.arcade.game.games.moba.kit.RoleSelectEvent;
import net.minecraft.server.v1_8_R3.PacketPlayInUseEntity;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

public class PrepareSelection implements Listener, IPacketHandler
{

	public static ItemStack buildColouredStack(Material material, MobaRole role)
	{
		return new ItemBuilder(material).setColor(role.getColor()).build();
	}

	private final Moba _host;
	private final Map<ClientArmorStand, MobaRole> _roleStands = new HashMap<>();
	private final Map<ClientArmorStand, HeroKit> _kitStands = new HashMap<>();
	private final Map<Player, ClientArmorStand> _goBackStands = new HashMap<>();

	public PrepareSelection(Moba host)
	{
		_host = host;

		_host.getArcadeManager().getPacketHandler().addPacketHandler(this, ListenerPriority.NORMAL, true, PacketPlayInUseEntity.class);
	}

	// Setup
	@EventHandler(priority = EventPriority.HIGHEST)
	public void prepare(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Prepare)
		{
			return;
		}

		for (GameTeam team : _host.GetTeamList())
		{
			spawnRoleUI(team);
		}
	}

	private void spawnRoleUI(GameTeam team)
	{
		Map<String, Location> spawns = _host.getLocationStartsWith("KIT " + team.GetName().toUpperCase());
		Location average = UtilAlg.getAverageLocation(team.GetSpawns());
		Player[] players = team.GetPlayers(true).toArray(new Player[0]);

		UtilServer.runSyncLater(() ->
		{
			for (Player player : team.GetPlayers(true))
			{
				displayRoleInformation(player);
			}

			for (Entry<String, Location> entry : spawns.entrySet())
			{
				Location location = entry.getValue();

				location.setYaw(UtilAlg.GetYaw(UtilAlg.getTrajectory(location, average)));

				try
				{
					MobaRole role = MobaRole.valueOf(entry.getKey().split(" ")[2]);
					ClientArmorStand stand = ClientArmorStand.spawn(prepareLocation(location), players);

					stand.setCustomNameVisible(true);
					stand.setCustomName(C.cGreenB + role.getName() + C.cGray + " - " + C.cGreenB + "AVAILABLE");
					stand.setArms(true);
					stand.setHelmet(role.getSkin().getSkull());
					stand.setChestplate(buildColouredStack(Material.LEATHER_CHESTPLATE, role));
					stand.setLeggings(buildColouredStack(Material.LEATHER_LEGGINGS, role));
					stand.setBoots(buildColouredStack(Material.LEATHER_BOOTS, role));

					_roleStands.put(stand, role);
				}
				catch (IllegalArgumentException e)
				{
				}
			}
			// Only spawn the NPCs once all players have been loaded into the world.
		}, _host.GetPlayers(true).size() * _host.TickPerTeleport + 10);
	}

	private void spawnKitUI(Player player)
	{
		AtomicInteger i = new AtomicInteger();
		GameTeam team = _host.GetTeam(player);
		Map<String, Location> spawns = _host.getLocationStartsWith("KIT " + team.GetName().toUpperCase());
		Location goBack = spawns.remove("KIT " + team.GetName().toUpperCase() + " GO_BACK");
		Location average = UtilAlg.getAverageLocation(team.GetSpawns());

		MobaPlayer mobaPlayer = _host.getMobaData(player);
		List<HeroKit> heroKits = _host.getKits(mobaPlayer.getRole());

		UtilServer.runSyncLater(() ->
		{
			ClientArmorStand goBackStand = ClientArmorStand.spawn(goBack.clone().add(0, 1, 0), player);

			goBackStand.setCustomNameVisible(true);
			goBackStand.setCustomName(C.cGreenB + "Go Back");
			goBackStand.setArms(true);
			goBackStand.setHelmet(new ItemStack(Material.SKULL_ITEM));
			goBackStand.setChestplate(new ItemBuilder(Material.LEATHER_CHESTPLATE).setColor(Color.MAROON).build());
			goBackStand.setLeggings(new ItemBuilder(Material.LEATHER_LEGGINGS).setColor(Color.MAROON).build());
			goBackStand.setBoots(new ItemBuilder(Material.LEATHER_BOOTS).setColor(Color.MAROON).build());

			_goBackStands.put(player, goBackStand);

			for (Location location : spawns.values())
			{
				location.setYaw(UtilAlg.GetYaw(UtilAlg.getTrajectory(location, average)));

				HeroKit kit = heroKits.get(i.getAndIncrement());
				ClientArmorStand stand = ClientArmorStand.spawn(location.clone().add(0, 1, 0), player);

				stand.setCustomNameVisible(true);
				stand.setCustomName((kit.ownsKit(player) ? C.cGreenB : C.cRedB) + kit.GetName());
				stand.setArms(true);
				stand.setHelmet(kit.getSkinData().getSkull());
				stand.setChestplate(buildColouredStack(Material.LEATHER_CHESTPLATE, kit.getRole()));
				stand.setLeggings(buildColouredStack(Material.LEATHER_LEGGINGS, kit.getRole()));
				stand.setBoots(buildColouredStack(Material.LEATHER_BOOTS, kit.getRole()));
				player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 0.5F);
				UtilParticle.PlayParticle(ParticleType.CLOUD, location.clone().add(0, 2, 0), 0.5F, 0.5F, 0.5F, 0.01F, 20, ViewDist.LONG, player);

				_kitStands.put(stand, kit);

				if (i.get() == heroKits.size())
				{
					break;
				}
			}
		}, 20);
	}

	private Location prepareLocation(Location location)
	{
		Block block = location.getBlock();

		block.setType(Material.SMOOTH_BRICK);
		block.setData((byte) 3);

		return location.clone().add(0, 1, 0);
	}

	private void removePodiums()
	{
		_host.getLocationStartsWith("KIT").forEach((key, location) -> location.getBlock().setType(Material.AIR));
	}

	// Listen for those packety clicks
	@Override
	public void handle(PacketInfo packetInfo)
	{
		PacketPlayInUseEntity packet = (PacketPlayInUseEntity) packetInfo.getPacket();
		Player player = packetInfo.getPlayer();
		int entityId = packet.a;

		if (UtilPlayer.isSpectator(player))
		{
			return;
		}

		ClientArmorStand goBackStand = _goBackStands.get(player);

		if (goBackStand != null && goBackStand.getEntityId() == entityId)
		{
			packetInfo.setCancelled(true);
			_host.getMobaData(player).setRole(null);
			_goBackStands.remove(player).remove();

			for (ClientArmorStand stand2 : _kitStands.keySet())
			{
				stand2.remove(player);
			}

			for (ClientArmorStand stand2 : _roleStands.keySet())
			{
				stand2.teleport(stand2.getLocation(), player);
			}
			return;
		}

		for (ClientArmorStand stand : _roleStands.keySet())
		{
			if (stand.getEntityId() != entityId)
			{
				continue;
			}

			packetInfo.setCancelled(true);

			MobaRole role = _roleStands.get(stand);
			RoleSelectEvent event = new RoleSelectEvent(player, stand, role);
			UtilServer.CallEvent(event);

			if (event.isCancelled())
			{
				return;
			}

			for (ClientArmorStand stand2 : _roleStands.keySet())
			{
				stand2.teleport(stand2.getLocation().add(0, 100, 0), player);
			}

			GameTeam team = _host.GetTeam(player);

			if (team == null)
			{
				return;
			}

			spawnKitUI(player);
			displayKitInformation(player, role);
		}

		for (ClientArmorStand stand : _kitStands.keySet())
		{
			if (stand.getEntityId() != entityId)
			{
				continue;
			}

			packetInfo.setCancelled(true);

			HeroKit kit = _kitStands.get(stand);

			if (!kit.ownsKit(player))
			{
				player.sendMessage(F.main("Game", "You have not unlocked this kit. Try picking one with a " + C.cGreen + "Green" + C.cGray + " name."));
				player.playSound(player.getLocation(), Sound.NOTE_PLING, 1, 0.2F);
				return;
			}

			if (goBackStand != null)
			{
				_goBackStands.remove(player).remove();
			}

			for (ClientArmorStand stand2 : _kitStands.keySet())
			{
				stand2.remove(player);
			}

			_host.SetKit(player, kit, true);
		}
	}

	private void displayRoleInformation(Player player)
	{
		String base = "Select the role you would like to play!";

		UtilTextMiddle.display(C.cYellowB + "Role", base, 10, 40, 10, player);
		player.sendMessage(F.main("Game", base));
	}

	private void displayKitInformation(Player player, MobaRole role)
	{
		String base = "Select your " + role.getChatColor() + "Hero";

		UtilTextMiddle.display(role.getChatColor() + role.getName(), "Select your " + role.getChatColor() + "Hero", 10, 40, 10, player);
		player.sendMessage(F.main("Game", base + C.mBody + "!"));
	}

	// Unregister
	@EventHandler
	public void live(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Live)
		{
			return;
		}

		for (ClientArmorStand stand : _roleStands.keySet())
		{
			stand.remove();
		}

		for (ClientArmorStand stand : _kitStands.keySet())
		{
			stand.remove();
		}

		for (ClientArmorStand stand : _goBackStands.values())
		{
			stand.remove();
		}

		_roleStands.clear();
		_kitStands.clear();
		_goBackStands.clear();
		removePodiums();
		_host.getArcadeManager().getPacketHandler().removePacketHandler(this);
	}
}
