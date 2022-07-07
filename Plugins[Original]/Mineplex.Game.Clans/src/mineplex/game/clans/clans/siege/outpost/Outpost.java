package mineplex.game.clans.clans.siege.outpost;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import mineplex.core.common.Pair;
import mineplex.core.common.util.C;
import mineplex.core.common.util.ColorFader;
import mineplex.core.common.util.EnclosedObject;
import mineplex.core.common.util.F;
import mineplex.core.common.util.LoopIterator;
import mineplex.core.common.util.RGBData;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilColor;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilTrig;
import mineplex.core.hologram.Hologram;
import mineplex.core.hologram.HologramInteraction;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.ClanInfo;
import mineplex.game.clans.clans.event.ClansWaterPlaceEvent;
import mineplex.game.clans.clans.event.IronDoorOpenEvent;
import mineplex.game.clans.clans.event.PlayerClaimTerritoryEvent;
import mineplex.game.clans.clans.siege.events.SiegeWeaponExplodeEvent;
import mineplex.game.clans.clans.siege.outpost.build.OutpostBlock;
import mineplex.game.clans.clans.siege.repository.tokens.OutpostToken;
import mineplex.game.clans.core.repository.ClanTerritory;
import net.minecraft.server.v1_8_R3.AxisAlignedBB;

public class Outpost implements Listener
{
	protected static final long MAX_LIFETIME = 45 * 60 * 1000; // 30 minutes
	public static final ItemStack OUTPOST_ITEM = new ItemBuilder(Material.BEACON, 1).setRawTitle(C.Reset + C.cBlue + "Outpost").build();
	
	public static final long PREP_TIME = 2 * 60 * 1000;
	
	private static final int MAX_HEALTH = 100;
	
	private OutpostManager _outpostManager;
	
	private final int _uniqueId;
	
	private ClanInfo _ownerClan;
	
	private Location _startCorner;
	private Location _origin;
	private Location _endCorner;
	
	private Location _forceFieldStart;
	private Location _forceFieldEnd;
	
	private Location _core;
	
	private LinkedHashMap<String, OutpostBlock> _blocks;
	private LinkedHashMap<String, OutpostBlock> _buildQueue = new LinkedHashMap<>();
	
	protected OutpostType _type;
	private OutpostState _state;
	
	private Hologram _preHologram;
	private Hologram _preHologram2;
	
	private LoopIterator<Vector> _circleStages;
	private LoopIterator<Vector> _reverseCircleStages;
	
	private ColorFader _fader = new ColorFader(30, UtilColor.hexToRgb(0x00A296), UtilColor.hexToRgb(0x29E6B6));
	
	private long _timeSpawned;
	
	public ClanInfo _againstClan;
	
	private Hologram _lifetimeLeft;
	
	private int _health;
	
	private long _lastDamage;
	
	private long _lastRegen;
	
	public Outpost(OutpostManager outpostManager, OutpostToken token)
	{
		_outpostManager = outpostManager;
		
		_health = MAX_HEALTH;
		
		_uniqueId = token.UniqueId;
		
		_ownerClan = token.OwnerClan;
		
		_startCorner = token.Origin.clone().subtract(token.Type._size, 1.1, token.Type._size);
		_endCorner = token.Origin.clone().add(token.Type._size + .9, token.Type._ySize - 1, token.Type._size + .9);
		
		_forceFieldStart = _startCorner.clone().subtract(4, 0, 4);
		_forceFieldEnd = _endCorner.clone().add(4.5, 0, 4.5);
		
		_origin = token.Origin.clone();
		
		_type = token.Type;
		
		_timeSpawned = token.TimeSpawned;
		
		_core = _type.getCoreLocation(_origin);
		
		_preHologram = new Hologram(outpostManager.getClansManager().getHologramManager(), _origin.clone().add(0.5, 2.3, 0.5), F.elem(_ownerClan.getName()) + C.cWhite + "'s Outpost block (Right-Click to activate)");
		_preHologram2 = new Hologram(outpostManager.getClansManager().getHologramManager(), _origin.clone().add(0.5, 3, 0.5), "Despawning: " + UtilText.getProgress(null, 0, null, true));
		
		_lifetimeLeft = new Hologram(outpostManager.getClansManager().getHologramManager(), _origin.clone().add(0.5, 1.5, 0.5), "Despawning in " + F.time(UtilTime.MakeStr(MAX_LIFETIME)));
		_lifetimeLeft.setInteraction(new HologramInteraction()
		{
			public void onClick(Player player, ClickType clickType)
			{
				if (_ownerClan.isMember(player) || _state != OutpostState.LIVE)
					return;
				
				if (clickType == ClickType.LEFT || clickType == ClickType.SHIFT_LEFT)
				{
					if (_outpostManager.getClansManager().hasTimer(player))
					{
						UtilPlayer.message(player, F.main("Clans", "You cannot destroy an Outpost whilst protected from PvP. Run " + F.elem("/pvp") + " to enable PvP!"));
						return;
					}
					if (!UtilTime.elapsed(_lastDamage, 5000))
					{
						return;
					}
					if (_health <= 2)
					{
						UtilPlayer.message(player, F.main("Clans", "You have destroyed " + F.elem(_ownerClan.getName()) + "'s Outpost!"));

						_core.getBlock().setType(Material.AIR);

						_ownerClan.inform("Your Outpost has been destroyed!", null);
						UtilTextMiddle.display("Siege", "Your Outpost has been destroyed", 20, 100, 20, _ownerClan.getOnlinePlayersArray());

						if (getState() == OutpostState.AWAITING)
							cleanup();
						else
							kill();
						
						return;
					}
					
					_lastDamage = System.currentTimeMillis();
					_health -= 2;
				}
			}
	
		});
		
		
		if (token.OutpostState == OutpostState.AWAITING)
		{
			_preHologram.start();
			_preHologram2.start();
		}
		
		_blocks = _type.createBuildQueue(_origin, _ownerClan.Clans, _ownerClan);
		
		_state = token.OutpostState;
		
		_circleStages = new LoopIterator<Vector>(UtilTrig.GetCirclePoints(new Vector(0., 0., 0.), 40, .6d));
		
		List<Vector> reverse = UtilTrig.GetCirclePoints(new Vector(0., 0., 0.), 40, .6d);
		Collections.reverse(reverse);
		_reverseCircleStages = new LoopIterator<Vector>(reverse);
		
	}
	
	public Outpost(OutpostManager outpostManager, ClanInfo clan, Location location, OutpostType type)
	{
		_outpostManager = outpostManager;
		
		_health = MAX_HEALTH;
		
		_uniqueId = outpostManager.getSiegeManager().randomId();
		
		_ownerClan = clan;
		
		_startCorner = location.clone().subtract(type._size, 1.1, type._size);
		_endCorner = location.clone().add(type._size + .9, type._ySize - 1, type._size + .9);
		
		_forceFieldStart = _startCorner.clone().subtract(4, 0, 4);
		_forceFieldEnd = _endCorner.clone().add(4.5, 0, 4.5);
		
		_origin = location.clone();
		
		_type = type;
		
		_timeSpawned = System.currentTimeMillis();
		
		_core = _type.getCoreLocation(_origin);
		
		_preHologram = new Hologram(_ownerClan.Clans.getHologramManager(), _origin.clone().add(0.5, 2.3, 0.5), F.elem(_ownerClan.getName()) + C.cWhite + "'s Outpost block (Right-Click to activate)");
		_preHologram2 = new Hologram(_ownerClan.Clans.getHologramManager(), _origin.clone().add(0.5, 3, 0.5), "Despawning: " + UtilText.getProgress(null, 0, null, true));
		
		_preHologram.start();
		_preHologram2.start();
		
		_state = OutpostState.AWAITING;
		
		_outpostManager.getRepository().insertOutpost(toToken());
		
		_circleStages = new LoopIterator<Vector>(UtilTrig.GetCirclePoints(new Vector(0., 0., 0.), 40, .6d));
		
		List<Vector> reverse = UtilTrig.GetCirclePoints(new Vector(0., 0., 0.), 40, .6d);
		Collections.reverse(reverse);
		_reverseCircleStages = new LoopIterator<Vector>(reverse);
		
		UtilServer.RegisterEvents(this);
		
		_lifetimeLeft = new Hologram(outpostManager.getClansManager().getHologramManager(), _origin.clone().add(0.5, 1.5, 0.5), "Despawning in " + F.time(UtilTime.MakeStr(MAX_LIFETIME)));
		_lifetimeLeft.setInteraction(new HologramInteraction()
		{
			public void onClick(Player player, ClickType clickType)
			{
				if (_ownerClan.isMember(player) || _state != OutpostState.LIVE)
					return;
				
				if (clickType == ClickType.LEFT || clickType == ClickType.SHIFT_LEFT)
				{
					if (_outpostManager.getClansManager().hasTimer(player))
					{
						UtilPlayer.message(player, F.main("Clans", "You cannot destroy an Outpost whilst protected from PvP. Run " + F.elem("/pvp") + " to enable PvP!"));
						return;
					}
					if (!UtilTime.elapsed(_lastDamage, 5000))
					{
						return;
					}
					if (_health <= 2)
					{
						UtilPlayer.message(player, F.main("Clans", "You have destroyed " + F.elem(_ownerClan.getName()) + "'s Outpost!"));

						_core.getBlock().setType(Material.AIR);

						_ownerClan.inform("Your Outpost has been destroyed!", null);
						UtilTextMiddle.display("Siege", "Your Outpost has been destroyed", 20, 100, 20, _ownerClan.getOnlinePlayersArray());

						if (getState() == OutpostState.AWAITING)
						{
							cleanup();
						}
						else
						{
							kill();
						}
						
						return;
					}
					
					_lastDamage = System.currentTimeMillis();
					_health -= 2;
				}
			}
		});
	}

	private void cleanup()
	{
		_blocks = null;
		
		if (_preHologram != null) _preHologram.stop();
		if (_preHologram2 != null) _preHologram2.stop();
		
		_preHologram = null;
		_preHologram2 = null;
		
		_state = OutpostState.DEAD;
		
		_outpostManager.queueForRemoval(_ownerClan.getName());
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onInteract(PlayerInteractEvent event)
	{
		if (getState() == OutpostState.LIVE)
		{
			if (event.getClickedBlock() == null)
				return;

			if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
				return;

			if (!UtilItem.isDoor(event.getClickedBlock().getType()))
				return;

			if (_ownerClan.isMember(event.getPlayer()))
				return;

			if (UtilAlg.inBoundingBox(event.getClickedBlock().getLocation(), _startCorner.clone().subtract(.5, 0, .5), _endCorner))
			{
				UtilPlayer.message(event.getPlayer(), F.main("Clans", "You cannot open the doors of this Outpost."));
				event.setCancelled(true);
				return;
			}
		}
		
		if (getState() != OutpostState.AWAITING)
			return;
		
		if (!UtilEvent.isAction(event, ActionType.R_BLOCK))
			return;
		
		if (getLifetime() <= 2000)
			return;
		
		if (event.getClickedBlock() != null && _origin.equals(event.getClickedBlock().getLocation()))
		{
			if (event.getClickedBlock().getType().equals(Material.BEACON))
			{
				if (_outpostManager.getClansManager().hasTimer(event.getPlayer()))
				{
					UtilPlayer.message(event.getPlayer(), F.main("Clans", "You cannot activate an Outpost whilst protected from PvP. Run " + F.elem("/pvp") + " to enable PvP!"));
					return;
				}
				
				if (!_ownerClan.equals(_ownerClan.Clans.getClanUtility().getClanByPlayer(event.getPlayer())))
				{
					UtilPlayer.message(event.getPlayer(), F.main("Clans", "This is not yours to activate!"));
					return;
				}
				
				for (Block block : UtilBlock.getBlocksInRadius(event.getClickedBlock().getLocation(), 5))
				{
					if (block.getType() == Material.WATER || block.getType() == Material.STATIONARY_WATER)
					{
						UtilPlayer.message(event.getPlayer(), F.main("Clans", "You cannot activate an Outpost that close to water!"));
						return;
					}
					if (block.getType() == Material.LAVA || block.getType() == Material.STATIONARY_LAVA)
					{
						UtilPlayer.message(event.getPlayer(), F.main("Clans", "You cannot activate an Outpost that close to lava!"));
						return;
					}
				}
				
				_origin.getBlock().setType(Material.AIR);
				beginConstruction();
			}
		}
	}
	
	@EventHandler
	public void onBlockDamage(BlockDamageEvent event)
	{
		if (event.getBlock().getLocation().equals(_core) && getState() == OutpostState.LIVE)
		{
			event.setCancelled(true);
			if (_outpostManager.getClansManager().hasTimer(event.getPlayer()))
			{
				UtilPlayer.message(event.getPlayer(), F.main("Clans", "You cannot destroy an Outpost whilst protected from PvP. Run " + F.elem("/pvp") + " to enable PvP!"));
				return;
			}
			if (!UtilTime.elapsed(_lastDamage, 5000))
			{
				return;
			}
			if (_health <= 2)
			{
				UtilPlayer.message(event.getPlayer(), F.main("Clans", "You have destroyed " + F.elem(_ownerClan.getName()) + "'s Outpost!"));

				_core.getBlock().setType(Material.AIR);

				_ownerClan.inform("Your Outpost has been destroyed!", null);
				UtilTextMiddle.display("Siege", "Your Outpost has been destroyed", 20, 100, 20, _ownerClan.getOnlinePlayersArray());

				if (getState() == OutpostState.AWAITING)
				{
					cleanup();
				}
				else
				{
					kill();
				}
				
				return;
			}
			
			_lastDamage = System.currentTimeMillis();
			_health -= 2;
		}
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event)
	{
		if (getState() == OutpostState.AWAITING && event.getBlock().getLocation().equals(_origin))
		{
			_origin.getBlock().setType(Material.AIR);
			_origin.getWorld().dropItem(_origin, OUTPOST_ITEM);
			_ownerClan.inform("Your Outpost block has been destroyed.", null);
			cleanup();
			event.setCancelled(true);
			return;
		}
		
		if (event.getBlock().getLocation().equals(_core) && getState() == OutpostState.LIVE)
		{
			event.setCancelled(true);
			if (_outpostManager.getClansManager().hasTimer(event.getPlayer()))
			{
				UtilPlayer.message(event.getPlayer(), F.main("Clans", "You cannot destroy an Outpost whilst protected from PvP. Run " + F.elem("/pvp") + " to enable PvP!"));
				return;
			}
			if (!UtilTime.elapsed(_lastDamage, 5000))
			{
				return;
			}
			if (_health <= 2)
			{
				UtilPlayer.message(event.getPlayer(), F.main("Clans", "You have destroyed " + F.elem(_ownerClan.getName()) + "'s Outpost!"));

				_core.getBlock().setType(Material.AIR);

				_ownerClan.inform("Your Outpost has been destroyed!", null);
				UtilTextMiddle.display("Siege", "Your Outpost has been destroyed", 20, 100, 20, _ownerClan.getOnlinePlayersArray());

				if (getState() == OutpostState.AWAITING)
				{
					cleanup();
				}
				else
				{
					kill();
				}
				
				return;
			}
			
			_lastDamage = System.currentTimeMillis();
			_health -= 2;
		}
		
		if (UtilAlg.inBoundingBox(event.getBlock().getLocation(), _startCorner.clone().subtract(.5, 0, .5), _endCorner))
		{
			UtilPlayer.message(event.getPlayer(), F.main("Clans", "You may not break blocks in Outposts."));
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onWaterPlace(ClansWaterPlaceEvent event)
	{
		if (UtilAlg.inBoundingBox(event.getBlock().getLocation(), _startCorner.clone().subtract(.5, 0, .5), _endCorner))
		{
			UtilPlayer.message(event.getPlayer(), F.main("Clans", "You may not place water in Outposts."));
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockPlace(BlockPlaceEvent event)
	{
		if (event.isCancelled())
			return;
		
		if (UtilAlg.inBoundingBox(event.getBlock().getLocation(), _startCorner.clone().subtract(.5, 0, .5), _endCorner))
		{
			UtilPlayer.message(event.getPlayer(), F.main("Clans", "You may not place blocks in Outposts."));
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void claimTerritory(PlayerClaimTerritoryEvent event)
	{
		if (UtilMath.offset2d(event.getClaimedChunk().getBlock(8, 0, 8).getLocation(), _origin) < 32)
		{
			UtilPlayer.message(event.getClaimer(), F.main("Clans", "You may not claim this close to an Outpost."));
			event.setCancelled(true);
		}
	}

	protected void update()
	{
		if (_state == OutpostState.AWAITING)
		{
			if (getLifetime() > 60000)
			{
				_origin.getBlock().setType(Material.AIR);
				_ownerClan.inform("You have lost your Outpost block, as no one activated it fast enough!", null);
				cleanup();
				return;
			}
			
			_preHologram2.setText(UtilText.getProgress(null, UtilMath.clamp(getLifetime(), 0., 60000.) / 60000., null, true));
			
			RGBData color = UtilColor.RgbLightBlue;
			
			for (int x = -_type._size; x <= _type._size; x++)
				for (int z = -_type._size; z <= _type._size; z++)
					if (x == -_type._size || x == _type._size || z == -_type._size || z == _type._size)
						UtilParticle.PlayParticleToAll(ParticleType.RED_DUST, UtilBlock.getHighest(_origin.getWorld(), _origin.clone().add(x + .5, .1, z + .5).getBlockX(), _origin.clone().add(x + .5, .1, z + .5).getBlockZ()).getLocation().add(0.5, 0, 0.5), new Vector(color.getRed(), color.getGreen(), color.getBlue()), 1f, 0, ViewDist.NORMAL);
			
			return;
		}
		
		if (_lifetimeLeft != null)
		{
			_lifetimeLeft.setText("Health: " + _health, "Despawning in " + F.time(UtilTime.MakeStr(MAX_LIFETIME - (System.currentTimeMillis() - _timeSpawned))));
		}
		
		if (_state == OutpostState.CONSTRUCTING)
		{
			if (_buildQueue.isEmpty())
			{
				_state = OutpostState.LIVE;
				
				return;
			}
			else
			{
				Iterator<String> iterator = _buildQueue.keySet().iterator();
				
				if (iterator.hasNext())
					_buildQueue.remove(iterator.next()).set();
			}
			
			// Forcefield
			RGBData color = UtilColor.RgbLightBlue;
			
			for (int x = _forceFieldStart.getBlockX(); x <= _forceFieldEnd.getBlockX(); x++)
				for (int z = _forceFieldStart.getBlockZ(); z <= _forceFieldEnd.getBlockZ(); z++)
					if (x == _forceFieldStart.getBlockX() || x == _forceFieldEnd.getBlockX() || z == _forceFieldStart.getBlockZ() || z == _forceFieldEnd.getBlockZ())
						UtilParticle.PlayParticleToAll(ParticleType.RED_DUST, new Location(_core.getWorld(), x + .5, UtilBlock.getHighest(_core.getWorld(), x, z).getY() + .15, z + .5), new Vector(color.getRed(), color.getGreen(), color.getBlue()), 1f, 0, ViewDist.NORMAL);
		}
		
		RGBData next = _fader.next();
		
		{
			RGBData color = _state == OutpostState.LIVE ? next : UtilColor.RgbRed;
			
			Vector nextCircleStage = _circleStages.next();
			
			double circleX = nextCircleStage.getX();
			double circleZ = nextCircleStage.getZ();
			
			UtilParticle.PlayParticleToAll(ParticleType.MOB_SPELL, _core.clone().add(circleX + .5, 1.1d, circleZ + .5), new Vector(color.getRed(), color.getGreen(), color.getBlue()), 1.f, 0, ViewDist.NORMAL);
		}
		
		{
			RGBData color = _state == OutpostState.LIVE ? next : UtilColor.RgbRed;
			
			Vector nextCircleStage = _reverseCircleStages.next();
			
			double circleX = nextCircleStage.getX();
			double circleZ = nextCircleStage.getZ();
			
			UtilParticle.PlayParticleToAll(ParticleType.MOB_SPELL, _core.clone().add(circleX + .5, 1.1d, circleZ + .5), new Vector(color.getRed(), color.getGreen(), color.getBlue()), 1.f, 0, ViewDist.NORMAL);
		}
		
		if (_state == OutpostState.LIVE)
		{
			if (UtilTime.elapsed(_timeSpawned, MAX_LIFETIME))
			{
				kill();
			}
			if (_health < MAX_HEALTH && UtilTime.elapsed(_lastDamage, 15000) && UtilTime.elapsed(_lastRegen, 1000))
			{
				_lastRegen = System.currentTimeMillis();
				_health++;
			}
		}
	}
	
	@EventHandler
	public void forcefield(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST || getState() != OutpostState.CONSTRUCTING)
			return;
		
		UtilServer.getPlayersCollection().stream()
											.filter(player -> !_ownerClan.isMember(player))
											.filter(player -> UtilAlg.inBoundingBox(player.getLocation(), _forceFieldStart, _forceFieldEnd))
										.forEach(player -> {
											UtilAction.velocity(player, UtilAlg.getTrajectory2d(_core, player.getLocation()), .77, true, 0.8, 0, 1.1, true);
											UtilPlayer.message(player, F.main("Clans", "This Outpost is still under construction!"));
											player.playSound(player.getLocation(), Sound.NOTE_BASS, 1.0f, 1.0f);
										});
	}
	
	public void beginConstruction()
	{
		// Cleanup pre-Outpost stuff
		_preHologram.stop();
		_preHologram2.stop();
		
		_preHologram = null;
		_preHologram = null;
		
		_lifetimeLeft.start();
		
		_state = OutpostState.CONSTRUCTING;
		_blocks = new LinkedHashMap<>(_buildQueue = _type.createBuildQueue(_origin, _ownerClan.Clans, _ownerClan));
		
		_ownerClan.inform("Siege", "Your Outpost is now being constructed.", null);
		
		//Inform nearby Clans
		for (int chunkX = -3; chunkX < 3; chunkX++)
		{
			for (int chunkZ = -3; chunkZ < 3; chunkZ++)
			{
				ClanTerritory territory = _ownerClan.Clans.getClanUtility().getClaim(_origin.getWorld().getChunkAt(_origin.getChunk().getX() + chunkX, _origin.getChunk().getZ() + chunkZ));
				
				if (territory != null && _outpostManager.getClansManager().getBlacklist().allowed(territory.Owner))
				{
					ClanInfo clan = _ownerClan.Clans.getClanUtility().getClanByClanName(territory.Owner);
					
					clan.inform("A siege has begun near your territory!", null);
					UtilTextMiddle.display("Siege", "A Siege has been declared on your Clan!", 20, 100, 20, clan.getOnlinePlayersArray());
				}
			}
		}
	}
	
	public void kill()
	{
		_state = OutpostState.DESTRUCTING;
		
		EnclosedObject<Integer> wait = new EnclosedObject<>(0);
		
		_blocks.values().stream().filter(block -> UtilMath.random.nextBoolean() && UtilMath.random.nextBoolean()).filter(block -> UtilMath.random.nextBoolean()).limit(13).forEach(block -> 
			_outpostManager.runSyncLater(() ->
			{
				UtilParticle.PlayParticleToAll(ParticleType.HUGE_EXPLOSION, block.getLocation(), new Vector(0,0,0), 1f, 1, ViewDist.MAX);
				_origin.getWorld().playSound(block.getLocation(), Sound.EXPLODE, 1.0f, 1.0f);
			}, wait.Set(wait.Get() + 4 + UtilMath.random.nextInt(4)))
		);
		
		_outpostManager.runSyncLater(() ->
		{
			_blocks.values().stream().forEach(OutpostBlock::restore);
		}, wait.Get() + 5L);
		
		_outpostManager.runSyncLater(() ->
		{
			_blocks.values().stream().forEach(block ->
			{
				Material mat = Material.getMaterial(block.getId());
				
				if (UtilItem.isTranslucent(mat) || UtilMath.random.nextBoolean())
				{
					block.restore();
					return;
				}
				
				FallingBlock fall = block.getLocation().getWorld().spawnFallingBlock(block.getLocation(), block.getId(), block.getData());
				fall.setDropItem(false);
				Vector vec = UtilAlg.getTrajectory(fall.getLocation(), getExactMiddle());

				UtilAction.velocity(fall, vec, 1, false, 0, 0.6, 10, false);
				
				fall.setMetadata("ClansOutpost", new FixedMetadataValue(_ownerClan.Clans.getPlugin(), _ownerClan.getName()));
				
				block.restore();
			});
			
			cleanup();
		}, wait.Get() + 6L);
		
		if (_lifetimeLeft != null) _lifetimeLeft.stop();
		
		_lifetimeLeft = null;
		
		_ownerClan.inform("Your Clan's Outpost has been destroyed.", null);
	}
	
	@EventHandler
	public void doorOpen(IronDoorOpenEvent event)
	{
		if (!UtilAlg.inBoundingBox(event.getBlock().getLocation(), _startCorner.clone().subtract(.5, 0, .5), _endCorner))
		{
			return;
		}
		
		if (_ownerClan.isMember(event.getPlayer()))
		{
			return;
		}
		
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onSiegeWeaponExplode(SiegeWeaponExplodeEvent event)
	{
		if (UtilAlg.inBoundingBox(event.getProjectile().getLocation(), _startCorner.clone().subtract(2, 2, 2), _endCorner.clone().add(2, 2, 2)))
		{
			if (getLifetime() < 15 * 60 * 1000)
			{
				event.setCancelled(true);
			}
		}
	}
	
	public ClanInfo getOwner()
	{
		return _ownerClan;
	}
	
	public long getLifetime()
	{
		return System.currentTimeMillis() - _timeSpawned;
	}
	
	public AxisAlignedBB getBounds()
	{
		return UtilAlg.toBoundingBox(_startCorner, _endCorner);
	}
	
	public Pair<Location, Location> getBoundsBlockBreak()
	{
		return Pair.create(_startCorner.clone().subtract(0.5, 0, 0.5), _endCorner.clone());
	}

	public Location getExactMiddle()
	{
		return UtilAlg.getMidpoint(_startCorner, _endCorner);
	}

	public OutpostState getState()
	{
		return _state;
	}
	
	public ClanInfo getAgainst()
	{
		return _againstClan;
	}
	
	public int getUniqueId()
	{
		return _uniqueId;
	}

	public OutpostToken toToken()
	{
		OutpostToken token = new OutpostToken();
		
		token.UniqueId = _uniqueId;
		token.Origin = _origin;
		token.Type = _type;
		token.OwnerClan = _ownerClan;
		token.TimeSpawned = _timeSpawned;
		token.OutpostState = _state;
		
		return token;
	}

	public Location getCoreLocation()
	{
		return _core;
	}
}
