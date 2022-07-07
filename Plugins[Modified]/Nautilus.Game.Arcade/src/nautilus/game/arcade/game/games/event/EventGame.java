package nautilus.game.arcade.game.games.event;

import mineplex.core.Managers;
import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.*;
import mineplex.core.disguise.disguises.*;
import mineplex.core.gadget.event.GadgetEnableEvent;
import mineplex.core.gadget.types.Gadget;
import mineplex.core.gadget.types.GadgetType;
import mineplex.core.recharge.Recharge;
import mineplex.core.shop.item.SalesPackageBase;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.visibility.VisibilityManager;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.games.event.kits.KitPlayer;
import nautilus.game.arcade.game.games.event.staffoscars.StaffOscarsModule;
import nautilus.game.arcade.game.modules.compass.CompassModule;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.managers.GameHostManager;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class EventGame extends Game
{
	private GameHostManager _mps;  

	private String[] _sideText = new String[] {"","","","","","","","","","","","","","",""};

	private boolean _doubleJump = false;
	private boolean _gadgetsEnabled = true;
	private NautHashMap<String, Integer> _forcefield = new NautHashMap<String, Integer>();

	private ItemStack[] _kitItems = new ItemStack[6];

	private boolean _allowAllGadgets = false;
	private HashSet<SalesPackageBase> _gadgetWhitelist = new HashSet<SalesPackageBase>();
	
	private HashMap<Sign, Long> _functionSigns;
	private ArrayList<Sign> _powerdedSigns;
	
	private HashMap<Integer, EventArea> _customAreas;

	public EventGame(ArcadeManager manager) 
	{
		super(manager, GameType.Event,

				new Kit[]
						{
				new KitPlayer(manager),
						},

						new String[]
								{
				""
								});

		this.JoinInProgress = true;

		this.DamageTeamSelf = true;
		this.DamagePvP = false;
		this.DamageEvP = false;
		this.DamagePvE = false;

		this.DeathMessages = false;
		this.DeathOut = false;

		this.CanAddStats = false;
		this.CanGiveLoot = false;

		this.GadgetsDisabled = false;

		this.TeleportsDisqualify = false;

		this.PrepareFreeze = false;

		this.BlockPlaceCreative = true;
		this.BlockBreakCreative = true;

		this.InventoryClick = true;
		this.InventoryOpenBlock = true;
		this.InventoryOpenChest = true;
		

		//Dont timeout
		this.GameTimeout = -1;

		_mps = manager.GetGameHostManager();
		_functionSigns = new HashMap<>();
		_powerdedSigns = new ArrayList<>();

		this.CreatureAllow = true;
		
		_customAreas = new HashMap<>();

		new CompassModule()
				.setGiveCompass(true)
				.setGiveCompassToSpecs(true)
				.setGiveCompassToAlive(false)
				.register(this);
	}

	@Override
	public void ParseData()
	{
		if (WorldData.MapName.equals("Staff Oscars"))
		{
			new StaffOscarsModule().register(this);
		}
	}

	@EventHandler
	public void registerSigns(GameStateChangeEvent event)
	{
		if(event.GetState() != GameState.Live)
			return;
		
		for(Location loc : WorldData.GetDataLocs("RED"))
		{
			for(int i = -5; i < 5; i++)
			{
				Location temp = loc.clone().add(0, i, 0);
				if(temp.getBlock().getType() == Material.SIGN_POST || temp.getBlock().getType() == Material.WALL_SIGN)
				{
					if(!_functionSigns.containsKey((Sign) temp.getBlock().getState()))
					{
						_functionSigns.put((Sign) temp.getBlock().getState(), System.currentTimeMillis());
					}
				}
			}
		}
	}
	
	@EventHandler
	public void registerAreas(GameStateChangeEvent event)
	{
		if(event.GetState() != GameState.Live)
			return;
		
		for(String name : WorldData.GetAllCustomLocs().keySet())
		{
			try 
			{
				EventArea area = new EventArea(Integer.parseInt(name.split(" ")[0]));
				Location tempA = WorldData.GetAllCustomLocs().get(name).get(0);
				Location tempB = WorldData.GetAllCustomLocs().get(name).get(1);
				area.CornerA = new Location(tempA.getWorld(), Math.min(tempA.getX(), tempB.getX()), Math.min(tempA.getY(), tempB.getY()), Math.min(tempA.getZ(), tempB.getZ()));
				area.CornerB = new Location(tempA.getWorld(), Math.max(tempA.getX(), tempB.getX()), Math.max(tempA.getY(), tempB.getY()), Math.max(tempA.getZ(), tempB.getZ()));
				area.DamageAll = name.contains("ALL");
				area.DamagePvP = name.contains("PVP");
				area.DamagePvE = name.contains("PVE");
				area.DamageEvP = name.contains("EVP");
				area.Usable = true;
				_customAreas.put(Integer.parseInt(name.split(" ")[0]), area);
			}
			catch (Exception e)
			{
				System.out.println("Error while parsing area locs");
			}
		}
	}
	
	@EventHandler
	public void signPlace(SignChangeEvent event)
	{
		if(!IsLive())
			return;
		
		if(!Manager.GetGameHostManager().isAdmin(event.getPlayer(), true))
			return;
		
		if(event.getLine(0).startsWith("[") && event.getLine(0).endsWith("]"))
			_functionSigns.put((Sign) event.getBlock().getState(), System.currentTimeMillis());
	}

	//Before GamePlayerManager puts onto Spec!
	@EventHandler(priority = EventPriority.LOW)
	public void specToTeam(PlayerJoinEvent event)
	{
		if (InProgress())
			joinTeam(event.getPlayer());
	}

	public void joinTeam(Player player)
	{
		//Set Team
		SetPlayerTeam(player, GetTeamList().get(0), true);

		//Kit
		SetKit(player, GetKits()[0], true);
		GetKits()[0].ApplyKit(player);

		//Refresh
		VisibilityManager vm = Managers.require(VisibilityManager.class);
		Bukkit.getOnlinePlayers().forEach(pl -> vm.refreshVisibility(pl, player));

		//Spawn
		GetTeamList().get(0).SpawnTeleport(player);

		//GameMode
		player.setGameMode(GameMode.SURVIVAL);
	}

	@EventHandler
	public void doubleJumpTrigger(PlayerToggleFlightEvent event)
	{
		if (!_doubleJump)
			return;

		Player player = event.getPlayer();

		if (player.getGameMode() == GameMode.CREATIVE)
			return;

		//Chicken Cancel
		DisguiseBase disguise = Manager.GetDisguise().getDisguise(player);
		if (disguise != null && 
				((disguise instanceof DisguiseChicken && !((DisguiseChicken)disguise).isBaby()) || disguise instanceof DisguiseBat || disguise instanceof DisguiseEnderman || disguise instanceof DisguiseWither))
			return;

		event.setCancelled(true);
		player.setFlying(false);

		//Disable Flight
		player.setAllowFlight(false);

		//Velocity
		UtilAction.velocity(player, 1.4, 0.2, 1, true);

		//Sound
		player.playEffect(player.getLocation(), Effect.BLAZE_SHOOT, 0);
	}

	@EventHandler
	public void doubleJumpRefresh(UpdateEvent event)
	{
		if (!_doubleJump)
			return;

		if (event.getType() != UpdateType.TICK)
			return;

		for (Player player : UtilServer.getPlayers())
		{
			if (player.getGameMode() == GameMode.CREATIVE)
				continue;

			if (UtilEnt.isGrounded(player) || UtilBlock.solid(player.getLocation().getBlock().getRelative(BlockFace.DOWN)))
			{
				player.setAllowFlight(true);
				player.setFlying(false);
			}
		}
	}

	@EventHandler
	public void gadgetActivate(GadgetEnableEvent event)
	{
		if (!_gadgetsEnabled)
			event.setCancelled(true);
	}

	@EventHandler
	public void forcefieldUpdate(UpdateEvent event)
	{
		if (!InProgress())
			return;

		if (event.getType() != UpdateType.FASTER)
			return;

		for (Player player : UtilServer.getPlayers())
		{
			if (_forcefield.containsKey(player.getName()))
			{
				for (Player other : UtilServer.getPlayers())
				{
					if (player.equals(other))
						continue;

					if (_mps.isAdmin(other, false))
						continue;

					int range = _forcefield.get(player.getName());

					if (UtilMath.offset(other, player) > range)
						continue;

					if (Recharge.Instance.use(other, "Forcefield Bump", 500, false, false))
					{
						Entity bottom = other;
						while (bottom.getVehicle() != null)
							bottom = bottom.getVehicle();

						UtilAction.velocity(bottom, UtilAlg.getTrajectory2d(player, bottom), 1.6, true, 0.8, 0, 10, true);
						other.getWorld().playSound(other.getLocation(), Sound.CHICKEN_EGG_POP, 2f, 0.5f);
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void explosionBlocks(EntityExplodeEvent event)
	{
		event.blockList().clear();	
	}

	@Override
	public void EndCheck()
	{

	}

	@Override
	@EventHandler
	public void ScoreboardUpdate(UpdateEvent event)
	{
		if (!InProgress())
			return;

		if (event.getType() != UpdateType.FAST)
			return;

		if (GetTeamList().isEmpty())
			return;

		Scoreboard.reset();

		for (int i=_sideText.length-1 ; i>=0 ; i--)
		{
			Scoreboard.write(_sideText[i]);
		}

		Scoreboard.draw();
	}

	//This re-enables cosmetic hotbar, because MPS disables it - temp fix until i find out whats disabling it repeatedly
	@EventHandler(priority = EventPriority.MONITOR)
	public void fixHotbarItemTemp(UpdateEvent event)
	{	
		Manager.GetServerConfig().HotbarInventory = true;
	}

	public void giveItems(Player player)
	{
		UtilInv.Clear(player);

		for (int i=0 ; i<_kitItems.length ; i++)
		{
			if (_kitItems[i] == null)
				continue;

			player.getInventory().addItem(_kitItems[i].clone());
		}

		UtilInv.Update(player);
	}

	@EventHandler
	public void creatureNaturalRemove(CreatureSpawnEvent event)
	{
		if (event.getSpawnReason() != SpawnReason.CUSTOM)
			event.setCancelled(true);
	}

	@EventHandler
	public void gadgetDisable(GadgetEnableEvent event)
	{
		if (_allowAllGadgets)
			return;

		if (!_gadgetWhitelist.contains(event.getGadget()))
		{
			event.setCancelled(true);
			event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.NOTE_BASS, 0.5f, 0.5f);
			//event.getPlayer().closeInventory();
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void entityDeathEvent(EntityDeathEvent event)
	{
		event.getDrops().clear();
	}
	
	public boolean isAllowGadget()
	{
		return _allowAllGadgets;
	}
	
	public void setAllowGadget(boolean var)
	{
		_allowAllGadgets = var;
	}
	
	public HashSet<SalesPackageBase> getGadgetWhitelist()
	{
		return _gadgetWhitelist;
	}
	
	public boolean isDoubleJump()
	{
		return _doubleJump;
	}
	
	public void setDoubleJump(boolean var)
	{
		_doubleJump = var;
	}
	
	public NautHashMap<String, Integer> getForcefieldList() 
	{
		return _forcefield;
	}
	
	public ItemStack[] getKitItems()
	{
		return _kitItems;
	}
	
	public void setKitItems(ItemStack[] kit)
	{
		_kitItems = kit;
	}
	
	public String[] getSideText()
	{
		return _sideText;
	}

	@Override
	public List<Player> getWinners()
	{
		if (GetState().ordinal() >= GameState.End.ordinal())
		{
			List<Player> places = GetTeamList().get(0).GetPlacements(true);

			if (places.isEmpty() || !places.get(0).isOnline())
				return Arrays.asList();
			else
				return Arrays.asList(places.get(0));
		}
		else
			return null;
	}

	@Override
	public List<Player> getLosers()
	{
		List<Player> winners = getWinners();

		if (winners == null)
			return null;

		List<Player> losers = GetTeamList().get(0).GetPlayers(false);

		losers.removeAll(winners);

		return losers;
	}
	
	/*@EventHandler
	public void signCheck(BlockRedstoneEvent event)
	{	
		if(event.getBlock().getType() != Material.SIGN_POST && event.getBlock().getType() != Material.WALL_SIGN)
			return;
		
		useSign(((Sign) event.getBlock().getState()).getLines());
	}*/
	
	@EventHandler
	public void signClock(UpdateEvent event)
	{
		if(event.getType() != UpdateType.TICK)
			return;
		
		for(Sign sign : _functionSigns.keySet())
		{
			
			Sign cooldown = null;
			
			for(BlockFace face : BlockFace.values())
			{
				if(face == BlockFace.UP)
					continue;
				
				if(sign.getBlock().getRelative(BlockFace.DOWN).getRelative(face).getType() == Material.WALL_SIGN)
				{	
					cooldown = (Sign) sign.getBlock().getRelative(BlockFace.DOWN).getRelative(face).getState();
					break;
				}
			}
			
			if(cooldown == null)
			{
				if(!sign.getBlock().isBlockPowered())
				{
					_powerdedSigns.remove(sign);
					continue;
				}
					
				if(_powerdedSigns.contains(sign))
					continue;
				
				useSign(sign, ((Sign) sign.getBlock().getState()).getLines());
				
				if(sign.getBlock().isBlockPowered())
				{
					if(!_powerdedSigns.contains(sign))
					{
						_powerdedSigns.add(sign);
					}
				}
				continue;
			}
				
			if(!sign.getBlock().isBlockPowered() && !UtilTime.elapsed(_functionSigns.get(sign), Long.parseLong(cooldown.getLine(0)) * 1000))
				continue;
			
			if(!sign.getBlock().isBlockPowered())
			{
				_powerdedSigns.remove(sign);
			}
			
			if(_powerdedSigns.contains(sign))
				continue;
			
			_functionSigns.put(sign, System.currentTimeMillis());
			useSign(sign, ((Sign) sign.getBlock().getState()).getLines());
			
			if(sign.getBlock().isBlockPowered())
			{
				if(!_powerdedSigns.contains(sign))
				{
					_powerdedSigns.add(sign);
				}
			}
		}
	}
	
	public void useSign(final Sign sign, String[] args)
	{
		String command = args[0];
		String playerName = args[1];
		
		HashMap<Integer, ArrayList<Player>> varMap = new HashMap<>();
		
		int i = 0;
		for(String varArgs : args)
		{
			ArrayList<Player> players = new ArrayList<>();
			if(Bukkit.getPlayer(varArgs) != null)
				players.add(Bukkit.getPlayer(varArgs));
			
			if(varArgs.contentEquals("@p"))
			{
				for(Player player : GetPlayers(true))
				{
					boolean found;
					found = true;
					for(Player otherPlayer : GetPlayers(true))
					{
						if(player == otherPlayer)
							continue;
						
						if(UtilMath.offset(sign.getLocation(), player.getLocation()) >= UtilMath.offset(sign.getLocation(), otherPlayer.getLocation()))
						{
							found = false;
							break;
						}
					}
					if(found)
					{
						players.add(player);
						break;
					}
				}
			}
			if(playerName.contains("@a"))
			{
				int radius = 0;
				if(playerName.contains("r="))
				{
					try
					{
						radius = Integer.parseInt(playerName.split("=")[1]);
					}
					catch (Exception e) {}
				}
				for(Player player : GetPlayers(true))
				{
					if(UtilMath.offset(sign.getLocation(), player.getLocation()) <= radius || radius <= 0)
						players.add(player);
				}
			}
			
			varMap.put(i, players);
			i++;
		}
		
		if(command.contentEquals("[BC]"))
		{
			String message = args[1];
			for(int e = 2; e <= 3; e++)
				message += " " + args[e];
			
			String colored = ChatColor.translateAlternateColorCodes('&', message);
			this.Announce(F.main("Event", colored), true);
		}
		
		if(command.contentEquals("[TELEPORT]"))
		{
			for(Player player : varMap.get(1))
			{
				if(Bukkit.getPlayer(args[2]) != null)
				{
					player.teleport(Bukkit.getPlayer(args[2]));
				}
				else
				{
					String[] coords = args[2].split(" ");
					int x = Integer.parseInt(coords[0]);
					int y = Integer.parseInt(coords[1]);
					int z = Integer.parseInt(coords[2]);
					player.teleport(new Location(sign.getWorld(), x, y, z));
				}
			}
		}
		
		if(command.contentEquals("[MESSAGE]"))
		{
			String message = args[2];
			message += " " + args[3];
			
			String colored = ChatColor.translateAlternateColorCodes('&', message);
			
			for(Player player : varMap.get(1))
			{
				UtilPlayer.message(player, F.main("Event", colored));
			}
		}
		
		if(command.contentEquals("[SETBLOCK]"))
		{
			Material mat = Material.getMaterial(Integer.parseInt(args[1]));
			String[] coords = args[2].split(" ");
			int x = Integer.parseInt(coords[0]);
			int y = Integer.parseInt(coords[1]);
			int z = Integer.parseInt(coords[2]);
			sign.getWorld().getBlockAt(new Location(sign.getWorld(), x, y, z)).setType(mat);
		}
		
		if(command.contentEquals("[TESTFOR]"))
		{
			boolean redstone = false;
			Material mat = Material.getMaterial(Integer.parseInt(args[1]));
			if(args[2].contains("r="))
			{
				int radius = 0;
				try 
				{
					radius = Integer.parseInt(args[2].split("=")[1]);
					if(radius >= 30)
						radius = 30;
					
					for(int x = -radius + sign.getLocation().getBlockX(); x < radius + sign.getLocation().getBlockX(); x++)
					{
						for(int y = -radius + sign.getLocation().getBlockY(); y < radius + sign.getLocation().getBlockY(); y++)
						{
							for(int z = -radius + sign.getLocation().getBlockZ(); z < radius + sign.getLocation().getBlockZ(); z++)
							{
								if(sign.getWorld().getBlockAt(new Location(sign.getWorld(), x, y, z)).getType() == mat)
								{
									redstone = true;
								}
							}
						}
					}
				}
				catch (Exception e) {}
			}
			else
			{
				String[] coords = args[2].split(" ");
				int x = Integer.parseInt(coords[0]);
				int y = Integer.parseInt(coords[1]);
				int z = Integer.parseInt(coords[2]);
				if(sign.getWorld().getBlockAt(new Location(sign.getWorld(), x, y, z)).getType() == mat)
				{
					redstone = true;
				}
			}
			if(redstone)
			{
				for(final BlockFace face : BlockFace.values())
				{	
					if(face != BlockFace.UP
							&& face != BlockFace.NORTH_WEST
							&& face != BlockFace.NORTH_EAST
							&& face != BlockFace.SOUTH_EAST
							&& face != BlockFace.SOUTH_WEST)
					{
						continue;
					}
					
					if(sign.getBlock().getRelative(face).getType() != Material.AIR)
						continue;
						
					if(sign.getBlock().getRelative(face).isBlockPowered())
						continue;
					
					sign.getBlock().getRelative(face).setType(Material.REDSTONE_BLOCK);
					
					Manager.runSyncLater(new Runnable()
					{

						@Override
						public void run()
						{
							sign.getBlock().getRelative(face).setType(Material.AIR);
						}
						
					}, 3L);
				}
			}
		}
		
		if(varMap.get(1).isEmpty())
			return;
		
		String[] vars = new String[args.length - 1];
		vars[0] = args[0];
		vars[1] = args[2];
		vars[2] = args[3];
		
		String tempArgs = "";
		for(String str : vars)
		{
			for(String string : str.split(" "))
			{
				tempArgs += string + " ";
			}
		}
		String[] commandArgs = tempArgs.split(" "); 
		
		if(command.contentEquals("[MOB]"))
		{
			for(Player player : varMap.get(1))
				Manager.GetEventModule().commandMob(player, commandArgs);
		}
		if(command.contentEquals("[SCORE]"))
		{
			for(Player player : varMap.get(1))
				Manager.GetEventModule().commandScoreboard(player, vars);
		}
		if(command.contentEquals("[GIVE]"))
		{
			try
			{
				for(Player player : varMap.get(1))
				{
					player.getInventory().addItem(new ItemStack(Material.getMaterial(Integer.parseInt(args[2])), Integer.parseInt(args[3])));
				}
				
			} catch (Exception e)
			{
				
			}
		}
		if(command.contentEquals("[DOUBLEJUMP]"))
		{
			for(Player player : varMap.get(1))
				Manager.GetEventModule().commandDoubleJump(player, vars);
		}
		if(command.contentEquals("[EFFECT]"))
		{
			//Manager.GetEventModule().commandEffect(Manager.GetGameHostManager().getHost(), commandArgs, varMap.get(1));
			for(Player player : varMap.get(1))
			{
				player.addPotionEffect(new PotionEffect(PotionEffectType.getByName(args[2]), Integer.parseInt(args[3].split(" ")[0]), Integer.parseInt(args[3].split(" ")[1])));
			}
			
		}
		if(command.contentEquals("[KIT]"))
		{
			for(Player player : varMap.get(1))
				Manager.GetEventModule().commandKit(player, commandArgs);
		}
	}
	
	@EventHandler
	public void signBreak(BlockBreakEvent event)
	{
		if(event.getBlock().getType() == Material.SIGN_POST || event.getBlock().getType() == Material.WALL_SIGN || event.getBlock().getType() == Material.SIGN)
		{
			Iterator<Sign> signIter = _functionSigns.keySet().iterator();
			while(signIter.hasNext())
			{
				Sign sign = signIter.next();
				if(sign.getLocation().getBlockX() == event.getBlock().getLocation().getBlockX()
						&& sign.getLocation().getBlockY() == event.getBlock().getLocation().getBlockY()
						&& sign.getLocation().getBlockZ() == event.getBlock().getLocation().getBlockZ())
				{
					signIter.remove();
				}
			}
		}
	}
	
	public boolean isInArea(EventArea area, Entity entity)
	{
		if(entity.getLocation().getX() < area.CornerA.getX() || entity.getLocation().getX() > area.CornerB.getX())
			return false;
	
		if(entity.getLocation().getY() < area.CornerA.getY() || entity.getLocation().getY() > area.CornerB.getY())
			return false;
	
		if(entity.getLocation().getZ() < area.CornerA.getZ() || entity.getLocation().getZ() > area.CornerB.getZ())
			return false;
		
		return true;
	}
	
	@EventHandler
	public void activateGadget(GadgetEnableEvent event)
	{
		for(EventArea area : _customAreas.values())
		{
			if(area.CornerA == null || area.CornerB == null)
				continue;
			
			if(area.Usable == false)
				continue;
			
			if(!isInArea(area, event.getPlayer()))
				continue;
			
			if(!area.GadgetsEnabled.contains(event.getGadget()))
			{
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void areaPotionEffect(UpdateEvent event)
	{
		if(event.getType() != UpdateType.SEC)
			return;
		
		for(EventArea area : _customAreas.values())
		{
			for(Player player : GetPlayers(true))
			{
				if(area.CornerA == null || area.CornerB == null)
					continue;
			
				if(area.Usable == false)
					continue;
			
				if(!isInArea(area, player))
					continue;
				
				for(PotionEffectType type : area.PotionEffects.keySet())
				{
					player.addPotionEffect(new PotionEffect(type, 60, area.PotionEffects.get(type)), true);
				}
			}
		}
	}
	
	@EventHandler(ignoreCancelled=true)
	public void areaDamage(CustomDamageEvent event)
	{
		for(EventArea area : _customAreas.values())
		{
			if(area.CornerA == null || area.CornerB == null)
				continue;
			
			if(area.Usable == false)
				continue;
			
			if(Manager.GetEventModule().getDamagePlayers().contains(event.GetDamagerPlayer(true)))
			{
				if(!Manager.GetGame().DamagePvP)
				{
					Manager.GetGame().Damage = true;
					Manager.GetGame().DamagePvP = true;
					Bukkit.getPluginManager().callEvent(event);
					Manager.GetGame().DamagePvP = false;
					Manager.GetGame().Damage = false;
				}	
				return;
			}
			
			if(!isInArea(area, event.GetDamageeEntity()))
				continue;
			
			if(!area.DamageAll)
			{
				event.SetCancelled("Event Area");
				continue;
			}
			if(event.GetCause() == DamageCause.ENTITY_ATTACK && event.GetDamagerPlayer(true) == null)
			{
				if(!area.DamageEvP)
				{
					event.SetCancelled("Event Area");
					continue;
				}
			}
			if(!(event.GetDamageeEntity() instanceof Player))
			{
				if(!area.DamagePvE)
				{
					event.SetCancelled("Event Area");
					continue;
				}
			}
			if((event.GetDamageeEntity() instanceof Player) && (event.GetDamagerEntity(true) instanceof Player))
			{
				if(!area.DamagePvP)
				{
					event.SetCancelled("Event Area");
					continue;
				}
			}
			if(!Manager.GetGame().DamagePvP)
			{
				Manager.GetGame().Damage = true;
				Manager.GetGame().DamagePvP = true;
				Bukkit.getPluginManager().callEvent(event);
				Manager.GetGame().DamagePvP = false;
				Manager.GetGame().Damage = false;
			}
		}
	}
	
	public void listAreaSettings(Player player)
	{
		for(EventArea area : _customAreas.values())
		{
			UtilPlayer.message(player, F.main("Event", "============================"));
			UtilPlayer.message(player, F.main("Event", "Settings for area " + area.ID));
			UtilPlayer.message(player, F.oo("Damage All", area.DamageAll) + ", "
			+ F.oo("Damage PvP", area.DamagePvP) + ", "
			+ F.oo("Damage PvE", area.DamagePvE)
			+ ", " + F.oo("Damage EvP", area.DamageEvP));
			
			UtilPlayer.message(player, F.main("Event", "Potion Effects for area " + area.ID));
			for(PotionEffectType type : area.PotionEffects.keySet())
				UtilPlayer.message(player, F.oo(type.getName(), true) + " level: " + area.PotionEffects.get(type));
			
			UtilPlayer.message(player, F.main("Event", "Gadgets for area " + area.ID));
			for(SalesPackageBase gadget : area.GadgetsEnabled)
				UtilPlayer.message(player, F.oo(gadget.getName(), true));
		}
	}
	
	public void editArea(Player player, String[] args)
	{

		if(args[1].equalsIgnoreCase("Info"))
		{
			listAreaSettings(player);
			return;
		}
		try 
		{
			if(_customAreas.containsKey(Integer.parseInt(args[1])))
			{
				if(args[2].equalsIgnoreCase("Del"))
				{
					_customAreas.remove(Integer.parseInt(args[1]));
					UtilPlayer.message(player, F.main("Event", "Region deleted"));
				}
				if(args[2].equalsIgnoreCase("ALL"))
				{
					_customAreas.get(Integer.parseInt(args[1])).DamageAll = !_customAreas.get(Integer.parseInt(args[1])).DamageAll;
					UtilPlayer.message(player, F.main("Event", "Damage all for Region " + args[1] + ": " + F.tf(_customAreas.get(Integer.parseInt(args[1])).DamageAll)));
				}
				if(args[2].equalsIgnoreCase("PVP"))
				{
					_customAreas.get(Integer.parseInt(args[1])).DamagePvP = !_customAreas.get(Integer.parseInt(args[1])).DamagePvP;
					UtilPlayer.message(player, F.main("Event", "Damage PvP for Region " + args[1] + ": " + F.tf(_customAreas.get(Integer.parseInt(args[1])).DamagePvP)));
				}
				if(args[2].equalsIgnoreCase("PVE"))
				{
					_customAreas.get(Integer.parseInt(args[1])).DamagePvE = !_customAreas.get(Integer.parseInt(args[1])).DamagePvE;
					UtilPlayer.message(player, F.main("Event", "Damage PvE for Region " + args[1] + ": " + F.tf(_customAreas.get(Integer.parseInt(args[1])).DamagePvE)));
				}
				if(args[2].equalsIgnoreCase("EVP"))
				{
					_customAreas.get(Integer.parseInt(args[1])).DamageEvP = !_customAreas.get(Integer.parseInt(args[1])).DamageEvP;
					UtilPlayer.message(player, F.main("Event", "Damage EvP for Region " + args[1] + ": " + F.tf(_customAreas.get(Integer.parseInt(args[1])).DamageEvP)));
				}
				if(args[2].equalsIgnoreCase("Effect"))
				{
					PotionEffectType type = PotionEffectType.getByName(args[3]);
					if(_customAreas.get(Integer.parseInt(args[1])).PotionEffects.containsKey(type))
					{
						_customAreas.get(Integer.parseInt(args[1])).PotionEffects.remove(type);
						UtilPlayer.message(player, F.main("Event", "Removed potion effect from area"));
						return;
					}	
					if (type == null)
					{
						UtilPlayer.message(player, F.main("Effect", "Invalid Effect Type: " + args[2]));
						UtilPlayer.message(player, F.value("Valid Types", "http://minecraft.gamepedia.com/Status_effect"));
						return;
					}	
					int strenght = 0;
					try
					{
						strenght = Integer.parseInt(args[4]);
					} 
					catch (Exception e) {}
					_customAreas.get(Integer.parseInt(args[1])).PotionEffects.put(type, strenght);
					UtilPlayer.message(player, F.main("Event", "Potion Effect added for Region " + args[1]));
				}
				if(args[2].equalsIgnoreCase("Gadget"))
				{
					//Gadgets
					for (GadgetType type : GadgetType.values())
					{
						for (Gadget gadget : Manager.getCosmeticManager().getGadgetManager().getGadgets(type))
						{
							if (gadget.getName().replaceAll(" ", "").equalsIgnoreCase(args[3]))
							{
								if (_customAreas.get(Integer.parseInt(args[1])).GadgetsEnabled.remove(gadget))
								{
									Manager.GetGame().Announce(F.main("Inventory", F.value(gadget.getName() + " Gadget for area " + args[1], F.ed(false))));
								}
								else
								{
									Manager.GetGame().Announce(F.main("Inventory", F.value(gadget.getName() + " Gadget for area " + args[1], F.ed(true))));
									_customAreas.get(Integer.parseInt(args[1])).GadgetsEnabled.add(gadget);
								}

								return;
							}
						}
					}
				}
			}
			else
			{
				UtilPlayer.message(player, F.main("Event", "No Area Found"));
			}
			if(args[2].equalsIgnoreCase("Add"))
			{
				if(!_customAreas.containsKey(Integer.parseInt(args[1])))
					_customAreas.put(Integer.parseInt(args[1]), new EventArea(Integer.parseInt(args[1])));
				
				EventArea area = _customAreas.get(Integer.parseInt(args[1]));
					
				if(args[3].equalsIgnoreCase("A"))
				{
					area.CornerA = player.getLocation();
					UtilPlayer.message(player, F.main("Event", "Corner A set!"));
				}
				if(args[3].equalsIgnoreCase("B"))
				{
					area.CornerB = player.getLocation();
					UtilPlayer.message(player, F.main("Event", "Corner B set!"));
				}
				if(area.CornerA != null && area.CornerB != null)
				{
					Location tempA = area.CornerA.clone();
					Location tempB = area.CornerB.clone();
					area.CornerA = new Location(tempA.getWorld(), Math.min(tempA.getX(), tempB.getX()), Math.min(tempA.getY(), tempB.getY()), Math.min(tempA.getZ(), tempB.getZ()));
					area.CornerB = new Location(tempA.getWorld(), Math.max(tempA.getX(), tempB.getX()), Math.max(tempA.getY(), tempB.getY()), Math.max(tempA.getZ(), tempB.getZ()));
					area.Usable = true;
					UtilPlayer.message(player, F.main("Event", "Region is Usable"));
				}
			}
		}
		catch (Exception e)
		{
			UtilPlayer.message(player, F.main("Event", "Error while executing command"));
		}
	}
	
	@EventHandler
	public void preventChestDrop(PlayerDropItemEvent event)
	{
		if(event.getItemDrop() == null)
			return;
		
		if(event.getItemDrop().getItemStack().getType() != Material.CHEST)
			return;
		
		if(!event.getItemDrop().getItemStack().hasItemMeta())
			return;
	
		event.setCancelled(true);
	}
	
	@EventHandler
	public void gemSign(final PlayerInteractEvent event)
	{
		if(event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		
		if(event.getClickedBlock().getType() != Material.SIGN && event.getClickedBlock().getType() != Material.WALL_SIGN && event.getClickedBlock().getType() != Material.SIGN_POST)
			return;
		
		Sign sign = (Sign) event.getClickedBlock().getState();
		if(!sign.getLine(0).contentEquals("(GEM)"))
			return;
		
		final Material mat = Material.getMaterial(Integer.parseInt(sign.getLine(1)));
		Integer price = Integer.parseInt(sign.getLine(2));
		if(price > 500)
			price = 500;
		
		if(price <= 0)
		{
			UtilPlayer.message(event.getPlayer(), F.main("Event", "You got an item for free."));	
			event.getPlayer().getInventory().addItem(new ItemStack(mat));
			return;
		}
		
		if(Manager.GetDonation().Get(event.getPlayer()).getBalance(GlobalCurrency.GEM) < price)
		{
			UtilPlayer.message(event.getPlayer(), F.main("Event", "You dont have enough Gems."));
			return;
		}
		
		final int gems = price;
		Manager.GetDonation().rewardCurrency(GlobalCurrency.GEM, event.getPlayer(), "Gem Sign", -price, completed ->
		{
			if (completed)
			{
				UtilPlayer.message(event.getPlayer(), F.main("Event", "You bought an item for " + gems + " Gems."));
				event.getPlayer().getInventory().addItem(new ItemStack(mat));
			}
		});
	}
	
	public class EventArea
	{
		
		public int ID;
		
		public Location CornerA;
		public Location CornerB;
		
		public boolean DamageAll;
		public boolean DamagePvP;
		public boolean DamagePvE;
		public boolean DamageEvP;
		
		public boolean Usable;
		
		public HashMap<PotionEffectType, Integer> PotionEffects;
		public HashSet<SalesPackageBase> GadgetsEnabled;
		
		public EventArea(Integer id)
		{
			ID = id;
			Usable = false;
			PotionEffects = new HashMap<>();
			GadgetsEnabled  = new HashSet<SalesPackageBase>();
		}
		
	}
	
}
