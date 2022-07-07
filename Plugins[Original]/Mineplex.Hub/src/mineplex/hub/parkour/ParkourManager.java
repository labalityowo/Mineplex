package mineplex.hub.parkour;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.event.GadgetBlockEvent;
import mineplex.core.gadget.event.GadgetCollideEntityEvent;
import mineplex.core.gadget.event.GadgetEnableEvent;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.mission.MissionTrackerType;
import mineplex.core.newnpc.NPC;
import mineplex.core.newnpc.NewNPCManager;
import mineplex.core.newnpc.event.NPCInteractEvent;
import mineplex.core.recharge.Recharge;
import mineplex.core.teleport.event.MineplexTeleportEvent;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.world.MineplexWorld;
import mineplex.hub.HubManager;
import mineplex.hub.doublejump.DoubleJumpPrepareEvent;
import mineplex.hub.parkour.data.Snake;
import mineplex.hub.parkour.data.SnakeParkourData;
import mineplex.hub.parkour.data.SprintingParkourData;
import mineplex.hub.player.HubPlayerManager;

@ReflectivelyCreateMiniPlugin
public class ParkourManager extends MiniPlugin
{

	public static final int DIFFICULTY_EASY = 0;
	public static final int DIFFICULTY_MEDIUM = 1;
	public static final int DIFFICULTY_HARD = 2;
	private static final ItemStack RESTART_ITEM = new ItemBuilder(Material.FEATHER)
			.setTitle(C.cGreenB + "Reset your time")
			.addLore("Click me to restart your parkour", "time!")
			.build();

	private final HubManager _hubManager;
	private final HubPlayerManager _hubPlayerManager;
	private final GadgetManager _gadgetManager;
	private final MineplexWorld _worldData;

	private final Map<Player, ParkourAttempt> _attempts;
	private final List<ParkourData> _parkours;

	private ParkourManager()
	{
		super("Parkour");

		_hubManager = require(HubManager.class);
		_hubPlayerManager = require(HubPlayerManager.class);
		_gadgetManager = require(GadgetManager.class);
		_worldData = _hubManager.getWorldData();

		_attempts = new HashMap<>();
		_parkours = new ArrayList<>();

		_parkours.add(new ParkourData(this, "HayYou", new String[]
				{
						"Jump fast and jump quick, just don't",
						"stick around long enough to wave to",
						"everyone or you'll have a bad time!"
				}, DIFFICULTY_EASY));

		List<Snake> snakes = new ArrayList<>();
		List<Location> path = _worldData.getSpongeLocations(String.valueOf(Material.QUARTZ_ORE.getId()));

		for (Location head : _worldData.getIronLocations("LIGHT_BLUE"))
		{
			snakes.add(new Snake(head, path));
		}

		_parkours.add(new SnakeParkourData(this, snakes));


		_parkours.add(new SprintingParkourData(this, "Splinter", new String[]
				{
						"Don't poke around to much or",
						"you might hurt yourself. You have",
						"to go fast to beat this one!"
				}, DIFFICULTY_MEDIUM));
		_parkours.add(new ParkourData(this, "Ruins", new String[]
				{
						"Something was here at some point,",
						"we aren't sure what, but you are free to",
						"jump around it a whole bunch."
				}, DIFFICULTY_MEDIUM));
		_parkours.add(new ParkourData(this, "Mystery Pillars", "HOUSE", new String[]
				{
						"This house came from the sky, only to leave",
						"these weird pillars all over the place. See",
						"if you can reach the top and collect it's prize!"
				}, DIFFICULTY_HARD));

		NewNPCManager npcManager = require(NewNPCManager.class);

		for (ParkourData data : _parkours)
		{
			npcManager.spawnNPCs(data.getKey() + " START", null);
			npcManager.spawnNPCs(data.getKey() + " END", null);
		}
	}

	public boolean isParkourMode(Entity player)
	{
		return player instanceof Player && _attempts.containsKey(player);
	}

	@EventHandler
	public void checkpointUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
		{
			return;
		}

		_attempts.entrySet().removeIf(entry ->
		{
			Player player = entry.getKey();
			ParkourAttempt attempt = entry.getValue();
			Location playerLocation = player.getLocation();

			for (Location location : attempt.getData().getCheckpoints())
			{
				if (UtilMath.offset2dSquared(playerLocation, location) < 9)
				{
					attempt.getCheckpoints().add(location);
					return false;
				}
			}

			if (!attempt.getData().isInArea(playerLocation))
			{
				attempt.getData().onEnd(player);
				_hubPlayerManager.giveHotbar(player);
				player.sendMessage(F.main(_moduleName, "You left the " + F.name("Parkour") + " area."));
				return true;
			}

			return false;
		});
	}

	@EventHandler
	public void timeUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		_attempts.forEach((player, attempt) -> UtilTextBottom.display(C.cGreenB + "Time: " + C.cPurple + ((System.currentTimeMillis() - attempt.getStart()) / 1000D) + " seconds", player));
	}

	@EventHandler
	public void npcInteract(NPCInteractEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}

		Player player = event.getPlayer();
		NPC npc = event.getNpc();
		String metadata = npc.getMetadata();

		for (ParkourData data : _parkours)
		{
			if (metadata.equals(data.getKey() + " START"))
			{
				startParkour(player, data);
			}
			else if (metadata.equals(data.getKey() + " END"))
			{
				if (!isParkourMode(player))
				{
					player.sendMessage(F.main(_moduleName, "You must be in " + F.name("Parkour mode") + " in order to complete parkour."));
					return;
				}

				ParkourAttempt attempt = _attempts.remove(player);

				if (!data.cheatCheck(attempt))
				{
					player.sendMessage(F.main(_moduleName, "You seem to have cheated. No reward for you."));
					return;
				}

				attempt.setEnd();
				double duration = (attempt.getDuration() / 1000D);
				player.sendMessage(F.main(_moduleName, "You completed " + F.name(data.getName()) + " in " + F.elem(duration) + " seconds."));
				rewardPlayer(player, data);
				_hubPlayerManager.giveHotbar(player);
			}
		}
	}

	public void startParkour(Player player, ParkourData data)
	{
		if (_hubManager.getHubGameManager().inQueue(player))
		{
			player.sendMessage(F.main(_moduleName, "You cannot go into " + F.name("Parkour") + " when queueing for a game."));
			return;
		}

		if (isParkourMode(player))
		{
			player.sendMessage(F.main(_moduleName, "Reset your time for " + F.name(data.getName()) + "."));
		}
		else
		{
			_gadgetManager.disableAll(player);

			PlayerInventory inventory = player.getInventory();

			inventory.clear();
			inventory.setItem(0, RESTART_ITEM);
			inventory.setHeldItemSlot(0);
		}

		player.setAllowFlight(false);
		player.setFallDistance(0);
		player.sendMessage(F.main(_moduleName, "Started " + F.name(data.getName()) + "."));
		_attempts.put(player, new ParkourAttempt(data));
		data.onStart(player);
	}

	private void rewardPlayer(Player player, ParkourData data)
	{
		_hubManager.getMissionManager().incrementProgress(player, 1, MissionTrackerType.LOBBY_PARKOUR, null, data.getKey());
	}

	public List<Entry<Player, ParkourAttempt>> getActivePlayers(ParkourData data)
	{
		return _attempts.entrySet().stream()
				.filter(entry -> entry.getValue().getData().equals(data))
				.collect(Collectors.toList());
	}

	@EventHandler
	public void playerInteract(PlayerInteractEvent event)
	{
		if (!UtilEvent.isAction(event, ActionType.R))
		{
			return;
		}

		Player player = event.getPlayer();
		ItemStack itemStack = player.getItemInHand();
		ParkourAttempt attempt = _attempts.get(player);

		if (itemStack == null || !itemStack.equals(RESTART_ITEM) || attempt == null || !Recharge.Instance.use(player, "Restart Parkour", 1000, false, false))
		{
			return;
		}

		attempt.getData().reset(player);
		startParkour(player, attempt.getData());
	}

	@EventHandler
	public void playerVelocity(PlayerVelocityEvent event)
	{
		if (isParkourMode(event.getPlayer()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void disableGadgets(GadgetEnableEvent event)
	{
		if (isParkourMode(event.getPlayer()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void gadgetBlock(GadgetBlockEvent event)
	{
		for (ParkourData data : _parkours)
		{
			event.getBlocks().removeIf(block -> data.isInArea(block.getLocation()));
		}
	}

	@EventHandler
	public void gadgetCollide(GadgetCollideEntityEvent event)
	{
		if (isParkourMode(event.getEntity()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void doubleJump(DoubleJumpPrepareEvent event)
	{
		if (!isParkourMode(event.getPlayer()))
		{
			return;
		}

		event.setCancelled(true);
	}

	@EventHandler
	public void playerTeleport(MineplexTeleportEvent event)
	{
		Player player = event.getPlayer();

		if (_attempts.remove(player) != null)
		{
			player.sendMessage(F.main(_moduleName, "You can't teleport during parkour."));
			_hubPlayerManager.giveHotbar(player);
		}
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		ParkourAttempt attempt = _attempts.remove(event.getPlayer());

		if (attempt != null)
		{
			attempt.getData().onEnd(event.getPlayer());
		}
	}

	public List<ParkourData> getParkours()
	{
		return _parkours;
	}

	public HubManager getHubManager()
	{
		return _hubManager;
	}
}
