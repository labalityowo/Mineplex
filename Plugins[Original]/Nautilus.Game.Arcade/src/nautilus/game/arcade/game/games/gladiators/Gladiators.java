package nautilus.game.arcade.game.games.gladiators;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import net.minecraft.server.v1_8_R3.PacketPlayOutAnimation;

import com.mojang.authlib.GameProfile;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.ProfileLoader;
import mineplex.core.common.util.UUIDFetcher;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilShapes;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.disguise.disguises.DisguisePlayer;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;
import nautilus.game.arcade.ArcadeFormat;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.events.PlayerPrepareTeleportEvent;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.SoloGame;
import nautilus.game.arcade.game.games.gladiators.events.PlayerChangeArenaEvent;
import nautilus.game.arcade.game.games.gladiators.events.RoundStartEvent;
import nautilus.game.arcade.game.games.gladiators.hotbar.HotbarEditor;
import nautilus.game.arcade.game.games.gladiators.hotbar.HotbarLayout;
import nautilus.game.arcade.game.games.gladiators.kits.KitGladiator;
import nautilus.game.arcade.game.games.gladiators.trackers.BrawlerTracker;
import nautilus.game.arcade.game.games.gladiators.trackers.FlawlessTracker;
import nautilus.game.arcade.game.games.gladiators.trackers.PrecisionTracker;
import nautilus.game.arcade.game.games.gladiators.trackers.SwiftKillTracker;
import nautilus.game.arcade.game.games.gladiators.trackers.UntouchableTracker;
import nautilus.game.arcade.game.games.gladiators.tutorial.TutorialGladiators;
import nautilus.game.arcade.game.modules.compass.CompassModule;
import nautilus.game.arcade.gametutorial.events.GameTutorialStartEvent;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.managers.chat.ChatStatData;

/**
 * Created by William (WilliamTiger).
 * 07/12/15
 */
public class Gladiators extends SoloGame
{

	private ArrayList<Arena> _allArenas;
	private ArrayList<Arena> _gameArenaSet;

	private HashMap<Player, Arena> _playerArenas;

	private RoundState _roundState;

	private ArenaType _furthestOutCurrent;

	private HotbarEditor _hotbarEditor;

	public Gladiators(ArcadeManager manager)
	{
		this(manager, new Kit[]
			{
				new KitGladiator(manager)
			},
			GameType.Gladiators);

		this.PlayerGameMode = GameMode.ADVENTURE;

		registerStatTrackers(
				new BrawlerTracker(this),
				new UntouchableTracker(this),
				new FlawlessTracker(this),
				new PrecisionTracker(this),
				new SwiftKillTracker(this)
		);

		registerChatStats(
				Kills,
				Assists,
				BlankLine,
				new ChatStatData("Untouchable", "Untouchable", true),
				BlankLine,
				DamageDealt,
				DamageTaken
		);
	}
	
	public Gladiators(ArcadeManager manager, Kit[] kits, GameType type)
	{
		super(manager, type, kits,
				new String[]
						{
								"This is a 1v1 tournament!",
								"Kill and then run to the next arena!",
								"There is only one victor!"
						});

		Damage = true;
		DamageFall = false;
		DamagePvP = true;
		DamageSelf = true;
		DamageTeamSelf = true;
		HungerSet = 20;
		EnableTutorials = false;

		BlockBreakAllow.add(Material.SUGAR_CANE_BLOCK.getId());
		BlockBreakAllow.add(Material.DEAD_BUSH.getId());
		BlockBreakAllow.add(Material.LONG_GRASS.getId());

		_playerArenas = new HashMap<>();

		_roundState = RoundState.WAITING;

		_hotbarEditor = new HotbarEditor();
		_hotbarEditor.register(this);

		new CompassModule()
				.setGiveCompass(true)
				.setGiveCompassToSpecs(true)
				.setGiveCompassToAlive(false)
				.register(this);

		StrictAntiHack = true;
	}

	@EventHandler
	public void tutorialStart(GameTutorialStartEvent e)
	{
		Location white = WorldData.GetDataLocs("WHITE").get(0);
		Location orange = UtilAlg.findClosest(white, WorldData.GetDataLocs("ORANGE"));
		Location pink = UtilAlg.findClosest(orange, WorldData.GetDataLocs("PINK"));
		ArrayList<Location> spawns = new ArrayList<>(WorldData.GetDataLocs("BROWN"));
		Location spawn1 = spawns.get(0);
		spawns.remove(spawn1);
		Location spawn2 = spawns.get(0);

		e.getTutorial().getPhase(1).setLocation(white);
		e.getTutorial().getPhase(1).setTarget(orange);

		CreatureAllowOverride = true;
		Zombie zombie1 = (Zombie) WorldData.World.spawnEntity(spawn1, EntityType.ZOMBIE);
		Zombie zombie2 = (Zombie) WorldData.World.spawnEntity(spawn2, EntityType.ZOMBIE);
		CreatureAllowOverride = false;
		for (Zombie zombie : Arrays.asList(zombie1, zombie2))
		{
			GameProfile tiger = new ProfileLoader(UUIDFetcher.getUUIDOf("WilliamTiger").toString(), "WilliamTiger").loadProfile();
			GameProfile random = stealGameProfile();
			DisguisePlayer player = new DisguisePlayer(zombie, (zombie.equals(zombie1) ? tiger : random));
			Manager.GetDisguise().disguise(player);

			UtilEnt.vegetate(zombie);
			zombie.getEquipment().setHelmet(ArenaType.ORANGE.getLoadout().getHelmet());
			zombie.getEquipment().setChestplate(ArenaType.ORANGE.getLoadout().getChestplate());
			zombie.getEquipment().setLeggings(ArenaType.ORANGE.getLoadout().getLeggings());
			zombie.getEquipment().setBoots(ArenaType.ORANGE.getLoadout().getBoots());
			zombie.getEquipment().setItemInHand(ArenaType.ORANGE.getLoadout().getSword());

			UtilEnt.CreatureMoveFast(zombie, orange, 1);
		}

		((TutorialGladiators)e.getTutorial()).setOrange(orange);
		((TutorialGladiators)e.getTutorial()).setPink(pink);
		((TutorialGladiators)e.getTutorial()).setZombie1(zombie1);
		((TutorialGladiators)e.getTutorial()).setZombie2(zombie2);
	}

	private GameProfile stealGameProfile()
	{
		Player random = UtilServer.getPlayers()[0];
		GameProfile gp = new GameProfile(UUID.randomUUID(), random.getName());
		gp.getProperties().putAll(((CraftPlayer)random).getHandle().getProfile().getProperties());
		return gp;
	}

	@EventHandler
	public void tutorialUpdate(UpdateEvent e)
	{
		if (e.getType() != UpdateType.FASTEST)
			return;

		if (!IsLive())
			return;

		if (GetState() != GameState.Prepare)
			return;

		if (GetTeamList().get(0).getTutorial() == null)
			return;

		TutorialGladiators tutorial = (TutorialGladiators) GetTeamList().get(0).getTutorial();

		if (tutorial == null)
		{
			System.out.println("tutorial object null");

			if (GetTeamList().get(0).getTutorial() == null)
				System.out.println("tutorial is null");

			if (!(GetTeamList().get(0).getTutorial() instanceof TutorialGladiators))
				System.out.println("its not a gladiators one");

			return;
		}

		if (tutorial.hasEnded())
			return;

		UtilEnt.CreatureMoveFast(tutorial.getZombie1(), tutorial.getOrange(), 1);
		UtilEnt.CreatureMoveFast(tutorial.getZombie2(), tutorial.getOrange(), 1);

		if (tutorial.getRunning() >= 2000 && !tutorial.isHasHit1())
		{
			tutorial.setHasHit1(true);

			// Zombie hit one

			PacketPlayOutAnimation packet = new PacketPlayOutAnimation();
			packet.a = Manager.GetDisguise().getDisguise(tutorial.getZombie1()).getEntityId();
			packet.b = 0;

			for (Player p : UtilServer.getPlayers())
			{
				UtilPlayer.sendPacket(p, packet); // Attack effect
			}

			tutorial.getZombie2().damage(1); // Hurt effect

			return;
		}

		if (tutorial.getRunning() >= 4000 && !tutorial.isHasHit2())
		{
			tutorial.setHasHit2(true);

			// Zombie hit two

			PacketPlayOutAnimation packet = new PacketPlayOutAnimation();
			packet.a = Manager.GetDisguise().getDisguise(tutorial.getZombie1()).getEntityId();
			packet.b = 0;

			for (Player p : UtilServer.getPlayers())
			{
				UtilPlayer.sendPacket(p, packet); // Attack effect
			}

			tutorial.getZombie2().damage(1); // Hurt effect

			return;
		}

		if (tutorial.getRunning() >= 5000 && !tutorial.getZombie2().isDead())
		{
			// Zombie remove time

			tutorial.getZombie2().damage(1);
			UtilFirework.playFirework(tutorial.getZombie2().getLocation(), FireworkEffect.Type.BALL, Color.ORANGE, false, false);
			tutorial.getZombie2().remove();

			Arena gateArena = getArenaByMid(tutorial.getOrange());
			for (Location loc : gateArena.getDoorBlocks())
				loc.getBlock().setType(Material.AIR); // Manual door open.
		}

		if (tutorial.getRunning() > 5000)
		{
			// Particles

			if (tutorial.getZombie1() == null || tutorial.getPink() == null)
				return;

			UtilEnt.CreatureMoveFast(tutorial.getZombie1(), tutorial.getPink(), 1);

			for (Location loc : UtilShapes.getLinesDistancedPoints(tutorial.getZombie1().getLocation(), tutorial.getPink(), 0.2))
			{
				UtilParticle.PlayParticle(UtilParticle.ParticleType.HAPPY_VILLAGER, loc, 0.03f, 0.03f, 0.03f, 0, 3,
						UtilParticle.ViewDist.LONG, UtilServer.getPlayers());
			}
		}
	}

	@Override
	public void addTutorials()
	{
		GetTeamList().get(0).setTutorial(new TutorialGladiators(Manager));
	}

	@Override
	public void ParseData()
	{
		parseArenas();
		parseDoors();
	}

	private void parseDoors()
	{
		for (Location loc : WorldData.GetCustomLocs("129"))
		{
			ArrayList<Location> mids = new ArrayList<>(getAllArenaMids());
			Arena arena1 = getArenaByMid(UtilAlg.findClosest(loc, mids));
			mids.remove(UtilAlg.findClosest(loc, mids));
			Arena arena2 = getArenaByMid(UtilAlg.findClosest(loc, mids));

			if (arena1.getColour().furtherOut(arena2.getColour()))
				arena1.getDoorBlocks().add(loc);
			else
				arena2.getDoorBlocks().add(loc);

			loc.getBlock().setType(Material.FENCE);
		}
	}

	private void parseArenas()
	{
		_allArenas = new ArrayList<>();

		for (Location mid : WorldData.GetDataLocs("RED"))
			_allArenas.add(new Arena(this, mid, ArenaType.RED));

		for (Location mid : WorldData.GetDataLocs("ORANGE"))
			_allArenas.add(new Arena(this, mid, ArenaType.ORANGE));

		for (Location mid : WorldData.GetDataLocs("YELLOW"))
			_allArenas.add(new Arena(this, mid, ArenaType.YELLOW));

		for (Location mid : WorldData.GetDataLocs("GREEN"))
			_allArenas.add(new Arena(this, mid, ArenaType.GREEN));

		for (Arena a : _allArenas)
		{
			if (a.getColour().equals(ArenaType.GREEN))
				continue;

			// Set the children of that arena.
			ArrayList<Location> possible = (ArrayList<Location>) getAllArenaMidsOfType(getPreviousColour(a.getColour())).clone();
			a.setChild(0, getArenaByMid(UtilAlg.findClosest(a.getMid(), possible)));
			possible.remove(a.getChildAt(0).getMid());
			a.setChild(1, getArenaByMid(UtilAlg.findClosest(a.getMid(), possible)));
		}
	}

	protected void findGameArenaSet()
	{
		_gameArenaSet = new ArrayList<>();

		GetTeamList().get(0).GetSpawns().clear(); // Clear the original game spawns.

		int neededSpawns = Math.min(GetPlayers(true).size(), 16); // Quick fix
		Arena masterNode = getArenasOfType(ArenaType.RED).get(0);

		HashMap<Arena, Integer> spawnsPerRoom = new HashMap<>();

		Queue<Arena> queue = new LinkedList<>();
		Queue<Arena> nextQueue = new LinkedList<>();
		queue.add(masterNode);

		int sum;
		boolean solved = false;

		while (!queue.isEmpty() && !solved)
		{
			sum = 0;
			ArrayList<Arena> currentNodes = new ArrayList<>();
			while (!queue.isEmpty())
			{
				currentNodes.add(queue.poll());
			}

			for (Arena node : currentNodes)
			{
				sum += node.getCapacity();
				node.setIsUsed(true);
			}

			if (sum >= neededSpawns)
			{
				solved = true;
			}
			else
			{
				for (Arena node : currentNodes)
				{
					for(int i = 0; i < node.getChilds().length; i++)
					{
						//System.out.println("Adding child of node: " + node.getColour() + "number of childs: " + node.getChilds().length);
						nextQueue.add(node.getChildAt(i));
						queue.add(node.getChildAt(i));
					}
				}

				while (!nextQueue.isEmpty())
				{
					Arena node = nextQueue.poll();
					node.setIsUsed(true);

					//System.out.println("Node: " + node.getColour());
					sum = sum + node.getCapacity() - 1;

					/*
					if (node.getParent().areChildrenUsed())
					{
						node.getParent().setIsUsed(false);
					}
					*/

					if (sum >= neededSpawns)
					{
						solved = true;
						break;
					}
				}
			}

			if (solved)
			{
				masterNode.getUsageMap(spawnsPerRoom);
				//System.out.println("Solution: ");

				for (Map.Entry<Arena, Integer> entry : spawnsPerRoom.entrySet())
				{
					//System.out.println("Color: " + entry.getKey().getColour() + ", Spawns: " + entry.getValue());
					_gameArenaSet.add(entry.getKey());
				}
			}
		}

		for (Arena a : _gameArenaSet)
		{
			if (a.getCapacity() <= 0)
				continue;

			for (Location l : a.capacitySpawns())
				GetTeamList().get(0).GetSpawns().add(l);
		}
	}

	public ArrayList<Arena> getGameArenaSet()
	{
		return _gameArenaSet;
	}

	public ArrayList<Arena> getArenasOfType(ArenaType type)
	{
		ArrayList<Arena> arenas = new ArrayList<>();

		for (Arena a : _allArenas)
			if (a.getColour().equals(type))
				arenas.add(a);

		return arenas;
	}

	public Arena getArenaByMid(Location mid)
	{
		for (Arena a : _allArenas)
			if (a.getMid().equals(mid))
				return a;

		return null;
	}

	public ArrayList<Location> getAllArenaMidsOfType(ArenaType type)
	{
		ArrayList<Location> mids = new ArrayList<>();

		for (Arena a : _allArenas)
			if (a.getColour().equals(type))
				mids.add(a.getMid());

		return mids;
	}

	public HashMap<Player, Arena> getPlayerArenas()
	{
		return _playerArenas;
	}

	public ArrayList<Location> getAllArenaMids()
	{
		ArrayList<Location> mids = new ArrayList<>();

		for (Arena a : _allArenas)
			mids.add(a.getMid());

		return mids;
	}

	public ArenaType getNextColour(ArenaType old)
	{
		switch (old)
		{
			case GREEN: return ArenaType.YELLOW;
			case YELLOW: return ArenaType.ORANGE;
			case ORANGE: return ArenaType.RED;
		}

		return null;
	}

	public ArenaType getPreviousColour(ArenaType old)
	{
		switch (old)
		{
			case RED: return ArenaType.ORANGE;
			case ORANGE: return ArenaType.YELLOW;
			case YELLOW: return ArenaType.GREEN;
		}

		return null;
	}

	@EventHandler
	public void setups(GameStateChangeEvent e)
	{
		if (e.GetState().equals(GameState.Live))
		{
			for (Player p : GetPlayers(true))
			{
				Location closest = UtilAlg.findClosest(p.getLocation(), getAllArenaMids());
				Arena arena = getArenaByMid(closest);
				arena.getPastPlayers().add(p);
				_playerArenas.put(p, arena);

				giveLoadout(p, _playerArenas.get(p).getColour());
			}

			for (Arena a : _gameArenaSet)
			{
				if (a.getPastPlayers().size() != 0)
					a.setState(ArenaState.WAITING);
			}

			return;
		}

		if (e.GetState() != GameState.Prepare)
			return;

		findGameArenaSet();
		//closeUnusedArenas();
	}

	@EventHandler
	public void helpMessage(PlayerPrepareTeleportEvent e)
	{
		UtilTextMiddle.display(C.cGreen + "Gladiators!", C.cGreen + "Defeat your opponent to advance", 20, 20 * 7, 20);
	}

	/**
	 *-------------------
	 * MAIN UPDATE METHOD
	 *-------------------
	 */
	@EventHandler
	public void roundUpdateCheck(UpdateEvent e)
	{
		if (!IsLive())
			return;

		if (e.getType() != UpdateType.TICK)
			return;

		if (_roundState == RoundState.WAITING)
		{
			_roundState = RoundState.STARTING_5;

			UtilTextMiddle.display("", C.cGreen + C.Bold + getRoundNotation(), 0, 80 , 0);

			_furthestOutCurrent = getFurthestOut(); // Find furthest out for fight.

			for (Arena a : _gameArenaSet)
			{
				if (a.getState() == ArenaState.WAITING && a.getColour() == _furthestOutCurrent)
				{
					// This arena is going to fight.
					a.setState(ArenaState.FIGHTING);
					a.setStateTime(System.currentTimeMillis());
				}

				if (a.getState() == ArenaState.WAITING)
				{
					// No fight for them, they have a bye.
					for (Player p : a.getPastPlayers())
					{
						p.sendMessage(ArcadeFormat.Line);
						p.sendMessage("   " + C.cWhite + C.Bold + "You have a bye!");
						p.sendMessage("   " + C.cGreen + "You automatically go through this round.");
						p.sendMessage(ArcadeFormat.Line);
					}
				}
			}

			Manager.getPluginManager().callEvent(new RoundStartEvent());
		}
		else if (_roundState.equals(RoundState.FIGHTING))
		{
			for (Arena a : _gameArenaSet)
				if (!(a.getState() == ArenaState.WAITING ||
						a.getState() == ArenaState.ENDED ||
						a.getState() == ArenaState.EMPTY))
					return;

			//All of the arenas are waiting for the next fight, so let's make them wait a bit.
			_roundState = RoundState.WAITING;
			return;
		}
	}

	private ArenaType getFurthestOut()
	{
		ArenaType best = null;
		for (Arena a : _gameArenaSet)
		{
			if (a.getState() != ArenaState.WAITING)
				continue;

			if (best == null || a.getColour().furtherOut(best))
				best = a.getColour();
		}

		return best;
	}

	private void closeUnusedArenas()
	{
		for (Arena a : _allArenas)
			if (!_gameArenaSet.contains(a))
				a.closeDoor();
	}

	@EventHandler
	public void arenaCheckPlayer(UpdateEvent e)
	{
		if (!IsLive())
			return;

		if (e.getType() != UpdateType.TICK)
			return;

		for (Arena a : _gameArenaSet)
		{
			Iterator<Player> pls = a.getPastPlayers().iterator();
			while (pls.hasNext())
			{
				Player p = pls.next();

				if (!GetPlayers(true).contains(p))
					pls.remove();
			}
		}
	}

	@EventHandler
	public void arenaMoveCheck(PlayerMoveEvent e)
	{
		if (!IsLive())
			return;

		if (!GetPlayers(true).contains(e.getPlayer()))
			return;

		if (!_playerArenas.containsKey(e.getPlayer()))
			return;

		Player p = e.getPlayer();

		//if (!_playerArenas.get(p).isOpenDoor())
			//return; // No need to check since no door to go through.

		Arena closest = getArenaByMid(UtilAlg.findClosest(p.getLocation(), getAllArenaMids()));

		if (closest != _playerArenas.get(p))
		{
			if (closest.getColour().furtherOut(_playerArenas.get(p).getColour()))
			{
				// No going backwards. Bounce baby bounce.
				if (Recharge.Instance.use(p, "Arena Bounce", 500, false, false))
				{
					//p.sendMessage("bounce. closest = " + closest.toString() + " player arena = " + _playerArenas.get(p).toString());
					UtilAction.velocity(p, UtilAlg.getTrajectory2d(e.getTo(), p.getLocation()), 1.7, true, 0.2, 0, 3, true);
					sendPlayerArenaBounceCheck(p);
				}

				return;
			}

			if (!_playerArenas.get(p).isOpenDoor())
				return;

			//p.sendMessage("ARENA MOVE CHECK METHOD!");
			Manager.getPluginManager().callEvent(new PlayerChangeArenaEvent(e.getPlayer(), closest, _playerArenas.get(p)));
			_playerArenas.put(p, closest);

			// Push player a little
			UtilAction.velocity(p, UtilAlg.getTrajectory2d(p.getLocation(), closest.getMid()), 1.7, false, 0.2, 0, 3, false);
		}
	}

	private void sendPlayerArenaBounceCheck(final Player p)
	{
		Manager.getScheduler().scheduleSyncDelayedTask(Manager.getPlugin(), new Runnable()
		{
			@Override
			public void run()
			{
				if (!GetPlayers(true).contains(p))
					return;

				Arena closest = getArenaByMid(UtilAlg.findClosest(p.getLocation(), getAllArenaMids()));
				if (closest != _playerArenas.get(p))
				{
					p.teleport(UtilAlg.findClosest(p.getLocation(), _playerArenas.get(p).getSpawns()));
				}
			}
		}, 20L);
	}

	@EventHandler
	public void arenaChange(PlayerChangeArenaEvent e)
	{
		Player p = e.getPlayer();
		Arena old = e.getFrom();
		Arena current = e.getTo();

		old.closeDoor();
		old.getPastPlayers().remove(p);
		old.setState(ArenaState.ENDED);
		current.getPastPlayers().add(p);
		current.setState(ArenaState.WAITING);
		current.setDoBye(true);

		//Bukkit.broadcastMessage("§7§lDEBUG: §3" + p.getName() + " left §b" + old.getColour().toString() + " §3and entered §b" + current.getColour().toString() + "§3.");

		giveLoadout(p, current.getColour()); //Kit
		p.setHealth(p.getMaxHealth()); //Heal
	}

	protected void giveLoadout(Player p, ArenaType type)
	{
		if (!GetPlayers(true).contains(p))
			return;

		HotbarLayout layout = _hotbarEditor.getLayout(p);

		p.getInventory().clear();
		p.getInventory().setArmorContents(null);

		p.getInventory().setItem(layout.getSword(), UtilItem.makeUnbreakable(type.getLoadout().getSword()));
		p.getInventory().setItem(layout.getRod(), UtilItem.makeUnbreakable(type.getLoadout().getRod()));
		p.getInventory().setItem(layout.getBow(), type.getLoadout().getBow());
		p.getInventory().setItem(layout.getArrows(), type.getLoadout().getArrows());

		p.getInventory().setHelmet(type.getLoadout().getHelmet());
		p.getInventory().setChestplate(type.getLoadout().getChestplate());
		p.getInventory().setLeggings(type.getLoadout().getLeggings());
		p.getInventory().setBoots(type.getLoadout().getBoots());

		p.playSound(p.getLocation(), Sound.LEVEL_UP, 1f, 1f);
	}

//	@EventHandler
//	public void debug(PlayerCommandPreprocessEvent e){
//		if (!e.getMessage().equalsIgnoreCase("/arenas"))
//			return;
//		e.setCancelled(true);
//		Player p = e.getPlayer();
//
//		p.sendMessage("Round State = " + _roundState.toString());
//		p.sendMessage("Out state = " + _furthestOutCurrent.toString());
//		for (Arena a : _gameArenaSet)
//		{
//			p.sendMessage(a.getColour().toString() + " - " + a.getPastPlayers().size() + " - " + a.getState().toString());
//		}
//	}

	@EventHandler
	public void arenaUpdateTick(UpdateEvent e)
	{
		if (!IsLive())
			return;

		if (e.getType() != UpdateType.TICK)
			return;

		for (Arena a : _gameArenaSet)
			a.updateTick();
	}

	@EventHandler
	public void arenaUpdate(UpdateEvent e)
	{
		if (!IsLive())
			return;

		if (e.getType() != UpdateType.SEC)
			return;

		for (Arena a : _gameArenaSet)
			a.update();
	}

	@EventHandler
	public void damageCancel(EntityDamageEvent e)
	{
		if (!IsLive())
			return;

		if (!(e.getEntity() instanceof Player))
			return;

		Player p = (Player) e.getEntity();

		if (!_playerArenas.containsKey(p))
			return;

		if ((_playerArenas.get(p).getState() != ArenaState.FIGHTING) || (_roundState != RoundState.FIGHTING))
			e.setCancelled(true);
	}

	@EventHandler
	public void startCountdown(UpdateEvent e)
	{
		if (!IsLive())
			return;

		if (e.getType() != UpdateType.SEC)
			return;

		if (_roundState.equals(RoundState.FIGHTING))
			return;

		if (_roundState.equals(RoundState.STARTING_5))
		{
			_roundState = RoundState.STARTING_4;
			return;
		}
		else if (_roundState.equals(RoundState.STARTING_4))
		{
			_roundState = RoundState.STARTING_3;
			return;
		}
		else if (_roundState.equals(RoundState.STARTING_3))
		{
			_roundState = RoundState.STARTING_2;

			UtilTextMiddle.display(C.cGreen + "3", C.cGreen + C.Bold + getRoundNotation(), 0, 80, 0);

			for (Player p : UtilServer.getPlayers())
				p.playSound(p.getLocation(), Sound.NOTE_STICKS, 1f, 1f);
			return;
		}
		else if (_roundState.equals(RoundState.STARTING_2))
		{
			_roundState = RoundState.STARTING_1;

			UtilTextMiddle.display(C.cYellow + "2", C.cGreen + C.Bold + getRoundNotation(), 0, 80, 0);

			for (Player p : UtilServer.getPlayers())
				p.playSound(p.getLocation(), Sound.NOTE_STICKS, 1f, 1f);
			return;
		}
		else if (_roundState.equals(RoundState.STARTING_1))
		{
			_roundState = RoundState.STARTED;

			UtilTextMiddle.display(C.cGold + "1", C.cGreen + C.Bold + getRoundNotation(), 0, 80, 0);

			for (Player p : UtilServer.getPlayers())
				p.playSound(p.getLocation(), Sound.NOTE_STICKS, 1f, 1f);
			return;
		}
		else if (_roundState.equals(RoundState.STARTED))
		{
			_roundState = RoundState.FIGHTING;

//			if (_firstRound)
//			{
//				_firstRound = false;
//				return;
//			}

			UtilTextMiddle.display(C.cRed + "FIGHT", C.cGreen + C.Bold + getRoundNotation(), 0, 40, 0);

			for (Player p : UtilServer.getPlayers())
				p.playSound(p.getLocation(), Sound.NOTE_PLING, 2f, 2f);
			return;
		}
	}

	private String getRoundNotation()
	{
		int size = GetPlayers(true).size();

		if (size == 2)
			return "FINALS";
		else if (size == 4)
			return "SEMI-FINALS";
		else if (size == 8)
			return "QUARTER-FINALS";
		else return "ROUND OF " + size + " PLAYERS";
	}

	public void setPlayerArena(Player p, Arena a)
	{
		_playerArenas.put(p, a);
	}

	public RoundState getRoundState()
	{
		return _roundState;
	}

	@EventHandler
	public void onShoot(EntityShootBowEvent e)
	{
		if (_roundState == null)
			return;

		if (_roundState.equals(RoundState.FIGHTING))
			return;

		e.setCancelled(true);

		if (e.getEntity() instanceof Player)
		{
//			((Player)e.getEntity()).getInventory().addItem(new ItemStack(Material.ARROW, 1)); // Arrow fix.
//			((Player)e.getEntity()).updateInventory();
		}
	}

	@Override
	@EventHandler
	public void ScoreboardUpdate(UpdateEvent event)
	{
		if (GetState() != GameState.Live)
			return;

		Scoreboard.reset();

		Scoreboard.writeNewLine();
		Scoreboard.write(C.cGreen + C.Bold + "Status");
		if (_roundState == RoundState.FIGHTING)
		{
			double start = -1D;
			for (Arena a : _gameArenaSet)
			{
				if (start != -1D)
					continue;

				if (a.getState() == ArenaState.FIGHTING)
					start = a.getStateTime();
			}
			double number = ((start + 60000) - System.currentTimeMillis());
			int time = (int)(number / 1000.0 + 0.5);
			if (time < 0)
				Scoreboard.write("Poison Active");
			else
				Scoreboard.write("Poison in " + time + "s");
		}
		else
		{
			Scoreboard.write(_roundState.getScoreboardText());
		}

		Scoreboard.writeNewLine();
		Scoreboard.write(C.cAqua + C.Bold + "Matches Left");
		Scoreboard.write(C.cWhite + getMatchesFighting());

		Scoreboard.writeNewLine();
		Scoreboard.write(C.cYellow + C.Bold + "Players");
		if (GetPlayers(true).size() > 7)
		{
			Scoreboard.write(C.cWhite + GetPlayers(true).size() + " Players");
		}
		else
		{
			for (Player p : GetPlayers(true))
			{
				Scoreboard.write(C.cWhite + p.getName());
			}
		}

		Scoreboard.draw();
	}

	private int getMatchesFighting()
	{
		if (_gameArenaSet == null)
			return 0;

		int count = 0;
		for (Arena a : _gameArenaSet)
		{
			if (a.getState() == ArenaState.FIGHTING)
				count++;
		}

		return count;
	}

	@EventHandler
	public void overfillCheck(PlayerPrepareTeleportEvent e)
	{
		Player p = e.GetPlayer();
		if (GetPlayers(true).size() > 16)
		{
			SetPlayerState(p, GameTeam.PlayerState.OUT);
			Manager.addSpectator(p, true);
			p.sendMessage(F.main("Game", "Too many players are in this server. You are now spectating, sorry."));
		}
	}

	@EventHandler
	public void fireworkDeath(CombatDeathEvent e)
	{
		Location loc = e.GetEvent().getEntity().getLocation();
		Color color = Color.AQUA;
		switch (_furthestOutCurrent)
		{
			case RED:
				color = Color.RED;
				break;
			case ORANGE:
				color = Color.ORANGE;
				break;
			case YELLOW:
				color = Color.YELLOW;
				break;
			case GREEN:
				color = Color.GREEN;
				break;
		}
		UtilFirework.playFirework(loc, FireworkEffect.Type.BALL, color, false, false);
	}

	@EventHandler
	public void quitAlert(PlayerQuitEvent e)
	{
		if (_playerArenas.containsKey(e.getPlayer()))
		{
			if (_playerArenas.get(e.getPlayer()).getState() != ArenaState.FIGHTING)
				return;

			for (Player p : _playerArenas.get(e.getPlayer()).getPastPlayers())
			{
				if (p.equals(e.getPlayer()))
					continue;

				p.sendMessage(ArcadeFormat.Line);
				p.sendMessage("   " + C.cWhite + C.Bold + "Your opponent has QUIT!");
				p.sendMessage("   " + C.cGreen + "You automatically win the fight.");
				p.sendMessage(ArcadeFormat.Line);
			}
		}
	}

	@EventHandler
	public void deathHealth(CombatDeathEvent e)
	{
		if (!(e.GetEvent().getEntity() instanceof Player))
			return;

		if (e.GetLog().GetKilledColor() != null && e.GetLog().GetKiller().IsPlayer())
		{
			Player killer = UtilPlayer.searchExact(e.GetLog().GetKiller().GetName());
			((Player)e.GetEvent().getEntity()).sendMessage(F.main("Game", "Your killer had " + C.cRed + (new DecimalFormat("#.#").format((killer.getHealth() / 2))) + "❤" + C.cGray + " left."));
		}
	}

	@EventHandler
	public void scoreboardEnd(GameStateChangeEvent e)
	{
		if (e.GetState() != GameState.End)
			return;

		Scoreboard.reset();

		Scoreboard.writeNewLine();
		Scoreboard.write(C.cGreen + C.Bold + "Status");
		Scoreboard.write(C.cWhite + "Ended");
		Scoreboard.writeNewLine();
		Scoreboard.write(C.cAqua + C.Bold + "Matches Left");
		Scoreboard.write(C.cWhite + "0");
		Scoreboard.writeNewLine();
		Scoreboard.write(C.cYellow + C.Bold + "Players");
		if (getWinners() != null && !getWinners().isEmpty())
			Scoreboard.write(C.cWhite + getWinners().get(0).getName());

		Scoreboard.draw();
	}
	
	public HashMap<Player, Arena> getArenas()
	{
		return _playerArenas;
	}
	
	public HotbarEditor getHotbarEditor()
	{
		return _hotbarEditor;
	}
	
}
