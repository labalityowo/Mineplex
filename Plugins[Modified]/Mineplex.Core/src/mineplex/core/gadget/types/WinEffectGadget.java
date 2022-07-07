package mineplex.core.gadget.types;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.mojang.authlib.GameProfile;

import mineplex.core.Managers;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.block.schematic.Schematic;
import mineplex.core.common.block.schematic.SchematicData;
import mineplex.core.common.block.schematic.UtilSchematic;
import mineplex.core.common.skin.SkinData;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilServer;
import mineplex.core.disguise.disguises.DisguisePlayer;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.incognito.IncognitoManager;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.visibility.VisibilityManager;

/**
 * A wrapper for different win effects
 */
public abstract class WinEffectGadget extends Gadget
{

	protected Player _player;
	protected long _start, _finish, _length = TimeUnit.SECONDS.toMillis(12);
	protected Location _baseLocation;
	private int _gameTime = 12000;

	/**
	 * The file name of the schematic used for this win room. Schematics can be found in the "schematic" folder
	 * in the server root folder. This name should not contain the file suffix of ".schematic" as this is automatically applied later.
	 */
	protected String _schematicName = "WinRoomPodium";

	/**
	 * All the players on the winners team. Empty if solo game.
	 */
	protected List<Player> _team;
	/**
	 * All the other players on the other teams that didn't win.
	 */
	protected List<Player> _nonTeam;
	/**
	 * All players on the team that didn't win + spectators which were not in the game at all.
	 */
	protected List<Player> _other;
	/**
	 * All the players that were teleported to the winroom
	 */
	protected List<Player> _allPlayers;

	/**
	 * @param manager                     The normal GadgetManager
	 * @param name                        The display name of the WinEffect
	 * @param desc                        The description of the WinEffect
	 * @param cost                        The shard cost of the WinEffect
	 * @param mat                         The display material of the WinEffect
	 * @param data                        The display data of the WinEffect
	 * @param alternativesalepackageNames Alternative packet names used to check if the player owns this WinEffect
	 */
	public WinEffectGadget(GadgetManager manager, String name, String[] desc, int cost, Material mat, byte data,
						   String... alternativesalepackageNames)
	{
		super(manager, GadgetType.WIN_EFFECT, name, desc, cost, mat, data, 1, alternativesalepackageNames);
	}

	/**
	 * @param manager The normal GadgetManager
	 * @param name    The display name of the WinEffect
	 * @param desc    The description of the WinEffect
	 * @param cost    The shard cost of the WinEffect
	 * @param mat     The display material of the WinEffect
	 * @param data    The display data of the WinEffect
	 * @param free    Sets the gadget to be free for all the players by default
	 * @param altName Alternative package names used to check if the player owns this WinEffect
	 */
	public WinEffectGadget(GadgetManager manager, String name, String[] desc, int cost, Material mat, byte data,
						   boolean free, String... altName)
	{
		super(manager, GadgetType.WIN_EFFECT, name, desc, cost, mat, data, 1, free, altName);
	}

	public void runPlay()
	{
		_finish = _start + _length;
		play();
	}

	public void runFinish()
	{
		try
		{
			finish();
		}
		finally
		{
			VisibilityManager vm = Managers.require(VisibilityManager.class);

			Bukkit.getOnlinePlayers().forEach(p ->
			{
				Bukkit.getOnlinePlayers().forEach(pl -> vm.showPlayer(p, pl, "Inside Win Effect"));
			});
			_player = null;
			_baseLocation = null;
			_team = null;
			_nonTeam = null;
			_other = null;
			_allPlayers = null;
			// Loads gadgets back when players are teleported to the arcade hub, after the win effect
			Manager.getUserGadgetPersistence().setEnabled(true);
			for (Player player : UtilServer.getPlayers())
			{
				Manager.getUserGadgetPersistence().load(player);
			}
		}
	}

	/**
	 * This method is called when the win effect should start playing
	 */
	public abstract void play();

	/**
	 * This method is called when this win effect is finished. Do any necessary clean up here. Note that entities do not need to be
	 * cleared.
	 */
	public abstract void finish();

	public boolean isRunning()
	{
		return _player != null && _baseLocation != null && System.currentTimeMillis() < _finish;
	}

	public Player getPlayer()
	{
		return _player;
	}

	public Location getBaseLocation()
	{
		return _baseLocation.clone();
	}

	/**
	 * @return The time stamp of when this effect started
	 */
	public long getStart()
	{
		return _start;
	}

	/**
	 * @return The time stamp of when this effect should end
	 */
	public long getFinish()
	{
		return _finish;
	}

	/**
	 * @return Returns how many milliseconds there is left before this effect should end. It will return negative values if the
	 * effect has finished.
	 */
	public long getTimeLeft()
	{
		return getFinish() - getStart();
	}

	public long getLength()
	{
		return _length;
	}

	public void setup(Player player, List<Player> team, List<Player> nonTeam, Location loc)
	{
		_player = player;
		_team = team;
		_nonTeam = nonTeam;

		IncognitoManager incognitoManager = Managers.get(IncognitoManager.class);

		_other = new ArrayList<>();

		if (incognitoManager != null)
		{
			_other.addAll(UtilServer.getPlayersCollection().stream()
					.distinct()
					.filter((p) -> !incognitoManager.Get(p).Status)
					.collect(Collectors.toList()));
		}
		else
		{
			_other.addAll(UtilServer.getPlayersCollection());
		}

		_other.remove(player);
		_other.removeAll(team);

		_allPlayers = new ArrayList<>();
		_allPlayers.addAll(UtilServer.getPlayersCollection());

		this._start = System.currentTimeMillis();
		this._baseLocation = loc.clone();
	}

	/**
	 * Teleport the players to the win room
	 *
	 * @see #teleport(Location)
	 */
	public void teleport()
	{
		Location loc = getBaseLocation().add(getBaseLocation().getDirection().normalize().multiply(10)).add(0, 3, 0);
		loc.setDirection(getBaseLocation().clone().subtract(loc).toVector());

		teleport(loc);
	}

	/**
	 * Teleport the players to the win room
	 *
	 * @param loc The base location to teleport the players too
	 */
	public void teleport(Location loc)
	{
		createBarriers(loc);

		BukkitRunnable bRunnable = new BukkitRunnable()
		{
			@Override
			public void run()
			{
				VisibilityManager vm = Managers.require(VisibilityManager.class);
				for (Player p : UtilServer.getPlayers())
				{
					Bukkit.getOnlinePlayers().forEach(pl -> vm.hidePlayer(pl, p, "Inside Win Effect"));
					p.eject();
					p.teleport(loc);
					p.setGameMode(GameMode.ADVENTURE);
					p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 10 * 20, 1, false, false));
					p.getInventory().clear();
					p.setAllowFlight(false);
					p.setHealth(p.getMaxHealth());
					p.setFoodLevel(20);
				}
			}
		};
		bRunnable.runTaskLater(UtilServer.getPlugin(), 10L);
	}

	/**
	 * Updates players inventory
	 */
	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST || !isRunning())
		{
			return;
		}

		for (Player player : _allPlayers)
		{
			player.getInventory().clear();
		}
	}

	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent event)
	{
		if (isRunning())
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerDamage(EntityDamageEvent event)
	{
		if (isRunning())
		{
			event.setCancelled(true);
		}
	}

	/**
	 * Build the win room, by default this will paste the scheamtic. Do any major setup here. This is called before the players are
	 * teleported.
	 */
	public void buildWinnerRoom()
	{
		pasteSchematic(_schematicName);
	}

	/**
	 * Get a disguised ArmorStand with the skin of the provided player at the provided location. The ArmorStand got no gravity and
	 * 2048 health.
	 *
	 * @param player The player to create the disguise from
	 * @param loc    The location to spawn the ArmorStand at
	 * @return Returns a disguised ArmorStand at the given location
	 */
	public DisguisePlayer getNPC(Player player, Location loc)
	{
		return getNPC(player, loc, false);
	}

	/**
	 * Get a disguised ArmorStand with the skin of the provided skindata at the provided location. The ArmorStand got no gravity and
	 * 2048 health.
	 *
	 * @param player   The player to create the disguise from
	 * @param loc      The location to spawn the ArmorStand at
	 * @param skinData The skin data to disguise the armorstand
	 * @return Returns a disguised ArmorStand at the given location
	 */
	public DisguisePlayer getNPC(Player player, Location loc, SkinData skinData)
	{
		ArmorStand stand = loc.getWorld().spawn(loc, ArmorStand.class);

		stand.setMaxHealth(2048);
		stand.setHealth(2048);
		stand.setGravity(false);

		GameProfile profile = new GameProfile(UUID.randomUUID(), player.getName());
		profile.getProperties().clear();
		profile.getProperties().put("textures", skinData.getProperty());

		DisguisePlayer disguise = new DisguisePlayer(stand, profile);
		Manager.getDisguiseManager().disguise(disguise);
		return disguise;
	}

	/**
	 * Get a disguised ArmorStand with the skin of the provided player at the provided location. The ArmorStand got 2048 health.
	 *
	 * @param player  The player to create the disguise from
	 * @param loc     The location to spawn the ArmorStand at
	 * @param gravity true if the armorstand should have gravity
	 * @return Returns a disguised ArmorStand at the given location
	 */
	public DisguisePlayer getNPC(Player player, Location loc, boolean gravity)
	{
		ArmorStand stand = loc.getWorld().spawn(loc, ArmorStand.class);

		stand.setMaxHealth(2048);
		stand.setHealth(2048);
		stand.setGravity(gravity);

		GameProfile profile = new GameProfile(UUID.randomUUID(), player.getName());
		profile.getProperties().putAll(((CraftPlayer) player).getHandle().getProfile().getProperties());

		DisguisePlayer disguise = new DisguisePlayer(stand, profile);
		Manager.getDisguiseManager().disguise(disguise);
		return disguise;
	}

	/**
	 * Paste a schematic relative to the base location
	 *
	 * @param schematicName Schematic name without the file suffix ".schematic". The file should be located in the "schematic" folder
	 *                      in the server root directory
	 * @return Returns the schematic after pasting it. Will return <code>null</code> if any errors ocured.
	 */
	public SchematicData pasteSchematic(String schematicName)
	{
		try
		{
			Schematic schematic = UtilSchematic.loadSchematic(new File("../../update/schematic/" + schematicName + ".schematic"));

			if (schematic != null)
			{
				return schematic.paste(getBaseLocation(), false, true);
			}

			return null;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public void spawnFirework(int tick)
	{
		Location loc = getBaseLocation();

		for (int i = 0; i < 3; i++)
		{
			double r = 3;
			double rad = (((Math.PI * 2) / 3.0) * i) + ((tick % 240) * Math.PI / 120.0);
			double x = Math.sin(rad) * r;
			double z = Math.cos(rad) * r;

			Location l = loc.clone().add(x, 0, z);
			UtilFirework.launchFirework(l, FireworkEffect.Type.BALL, Color.fromRGB(UtilMath.r(255 * 255 * 255)), false,
					true, new Vector(0, 0.01, 0), 1);
		}
	}

	private void createBarriers(Location baseLocation)
	{
		// FLOOR
		Location floorBase = baseLocation.clone().subtract(0, 1, 0);
		setBarrierBlock(floorBase);
		setBarrierBlock(floorBase.clone().add(1, 0, 0));
		setBarrierBlock(floorBase.clone().add(0, 0, 1));
		setBarrierBlock(floorBase.clone().subtract(1, 0, 0));
		setBarrierBlock(floorBase.clone().subtract(0, 0, 1));
		setBarrierBlock(floorBase.clone().add(1, 0, 1));
		setBarrierBlock(floorBase.clone().add(1, 0, -1));
		setBarrierBlock(floorBase.clone().add(-1, 0, 1));
		setBarrierBlock(floorBase.clone().subtract(1, 0, 1));

		// WALLS
		floorBase.add(0, 2, 0);
		setBarrierBlock(floorBase.clone().add(2, 0, 0));
		setBarrierBlock(floorBase.clone().add(2, 0, 1));
		setBarrierBlock(floorBase.clone().add(2, 0, -1));

		setBarrierBlock(floorBase.clone().subtract(2, 0, 0));
		setBarrierBlock(floorBase.clone().subtract(2, 0, 1));
		setBarrierBlock(floorBase.clone().subtract(2, 0, -1));

		setBarrierBlock(floorBase.clone().add(0, 0, 2));
		setBarrierBlock(floorBase.clone().add(1, 0, 2));
		setBarrierBlock(floorBase.clone().add(-1, 0, 2));

		setBarrierBlock(floorBase.clone().subtract(0, 0, 2));
		setBarrierBlock(floorBase.clone().subtract(1, 0, 2));
		setBarrierBlock(floorBase.clone().subtract(-1, 0, 2));

		// CEILING
		floorBase.add(0, 2, 0);
		setBarrierBlock(floorBase);
		setBarrierBlock(floorBase.clone().add(1, 0, 0));
		setBarrierBlock(floorBase.clone().add(0, 0, 1));
		setBarrierBlock(floorBase.clone().subtract(1, 0, 0));
		setBarrierBlock(floorBase.clone().subtract(0, 0, 1));
		setBarrierBlock(floorBase.clone().add(1, 0, 1));
		setBarrierBlock(floorBase.clone().add(1, 0, -1));
		setBarrierBlock(floorBase.clone().add(-1, 0, 1));
		setBarrierBlock(floorBase.clone().subtract(1, 0, 1));

		// CHANGES MIDDLE TO AIR
		floorBase.subtract(1, 3, 1);
		for (int x = 0; x < 3; x++)
		{
			for (int y = 0; y < 3; y++)
			{
				for (int z = 0; z < 3; z++)
				{
					floorBase.clone().add(x, y, z).getBlock().setType(Material.AIR);
				}
			}
		}

	}

	private void setBarrierBlock(Location blockLocation)
	{
		if (blockLocation.getBlock().getType() == Material.AIR)
			blockLocation.getBlock().setType(Material.BARRIER);
	}

	/**
	 * Gets the formatted rank of the player
	 *
	 * @param player
	 * @return
	 */
	protected String getRank(Player player)
	{
		PermissionGroup group = Manager.getClientManager().Get(player).getRealOrDisguisedPrimaryGroup();
		if (group.getDisplay(false, false, false, false).isEmpty())
		{
			return C.Reset + "";
		}
		return group.getDisplay(true, true, true, false) + " " + C.Reset;
	}

	public int getGameTime()
	{
		return _gameTime;
	}

	protected void setGameTime(int gameTime)
	{
		_gameTime = gameTime;
	}

	protected void setBaseLocation(Location baseLocation)
	{
		_baseLocation = baseLocation;
	}
}