package nautilus.game.arcade.game.games.common.ctf_data;

import java.util.ArrayList;
import java.util.List;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTime;
import mineplex.core.hologram.Hologram;
import mineplex.core.mission.MissionTrackerType;
import mineplex.core.recharge.Recharge;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.common.CaptureTheFlag;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Flag
{
	private CaptureTheFlag _host;
	
	private Location _spawn;
	private Location _particleLoc;
	private Location _beacon;
	
	private Block _flagBlock;
	
	private Material _priorBlock;
	private Byte _priorData;
	
	private GameTeam _team;
	private ChatColor _enemy;
	
	private ItemStack _representation;
	private DyeColor _dyeData;
	private List<Pattern> _patterns;
	
	private Player _holding = null;
	
	private boolean _moved = false;
	private boolean _dropped = false;
	private long _dropTime = 0;
	private int _respawnTimer;
	
	private Hologram _name, _time;
	
	private String _displayName;
	
	public Flag(CaptureTheFlag host, Location spawn, GameTeam team)
	{
		_host = host;
		_spawn = spawn;
		_beacon = _spawn.clone().add(0, -2, 0);
		_team = team;
		_displayName = team.GetColor() + team.GetName() + "'s Flag".replace("s's", "s'");
		_patterns = new ArrayList<Pattern>();
		
		ItemStack i;
		if (team.GetColor() == ChatColor.RED)
		{
			i = new ItemStack(Material.BANNER);
			_dyeData = DyeColor.RED;
			_enemy = ChatColor.BLUE;
			_patterns.add(new Pattern(DyeColor.WHITE, PatternType.CROSS));
		}
		else
		{
			i = new ItemStack(Material.BANNER);
			_dyeData = DyeColor.BLUE;
			_enemy = ChatColor.RED;
			_patterns.add(new Pattern(DyeColor.WHITE, PatternType.CIRCLE_MIDDLE));
		}
		
		BannerMeta im = (BannerMeta) i.getItemMeta();
		im.setBaseColor(_dyeData);
		im.setPatterns(_patterns);
		im.setDisplayName(_displayName);
		i.setItemMeta(im);
		
		_representation = i;
		
		_name = new Hologram(_host.getArcadeManager().getHologramManager(), spawn, team.GetColor() + team.GetName() + "'s Flag".replace("s's", "s'"));
		_time = new Hologram(_host.getArcadeManager().getHologramManager(), spawn, "");
		
		spawnFlag(spawn);
	}
	
	public GameTeam getTeam()
	{
		return _team;
	}
	
	public boolean isAtHome()
	{
		return !_moved;
	}
	
	public Player getCarrier()
	{
		return _holding;
	}
	
	public Location getPlacedLocation()
	{
		return _flagBlock.getLocation().clone().add(0.5, 0, 0.5);
	}

	public ItemStack getRepresentation()
	{
		return _representation;
	}
	
	public String getDisplayName()
	{
		return _displayName;
	}
	
	private void announceSteal(Player cap, boolean steal)
	{
		if (steal)
		{
			UtilTextMiddle.display("", _host.GetTeam(cap).GetColor() + cap.getName() + C.cWhite + " stole " + _team.GetColor() + _team.GetName() + " Flag!", 0, 60, 10);
			_host.getArcadeManager().getMissionsManager().incrementProgress(cap, 1, MissionTrackerType.CTF_TAKE, _host.GetType().getDisplay(), null);
		}
		else
		{
			UtilTextMiddle.display("", _host.GetTeam(cap).GetColor() + cap.getName() + C.cWhite + " picked up " + _team.GetColor() + _team.GetName() + " Flag!", 0, 60, 10);
		}
	}
	
	private void announceDrop(Player player)
	{
		UtilTextMiddle.display("", _enemy + player.getName() + C.cWhite + " dropped " + _team.GetColor() + _team.GetName() + " Flag!", 0, 60, 10);
	}
	
	private void announceCapture(Player player)
	{
		UtilTextMiddle.display("", _host.GetTeam(player).GetColor() + player.getName() + C.cWhite + " scored for " + _host.GetTeam(player).GetColor() + _host.GetTeam(player).GetName(), 0, 60, 10);
		_host.getArcadeManager().getMissionsManager().incrementProgress(player, 1, MissionTrackerType.CTF_CAPTURE, _host.GetType().getDisplay(), null);
	}
	
	private void announceReturn()
	{
		UtilTextMiddle.display("", _team.GetColor() + _team.GetName() + " Flag " + C.cWhite + " has returned to base", 0, 60, 10);
	}
	
	public void update()
	{
		if (!_moved || _dropped)
		{
			_flagBlock.setType(Material.STANDING_BANNER);
			Banner banner = (Banner) _flagBlock.getState();
			banner.setBaseColor(_dyeData);
			banner.setPatterns(_patterns);
			banner.update();
		}
		
		if (_moved)
		{
			if (_dropped)
			{
				Integer r = Math.max(0, _respawnTimer);
				_time.setText(remainColor(r) + r.toString());
				
				if (_respawnTimer > -1)
					return;
				
				deleteFlag();
				
				_moved = false;
				_dropped = false;
				spawnFlag(_spawn);
				
				announceReturn();
				
				UtilFirework.playFirework(_spawn.clone().add(0, 1, 0), FireworkEffect.builder().flicker(false).withColor(_team.GetColorBase()).with(Type.BURST).trail(true).build());
				return;
			}
			
			if (_holding == null)
				return;
			
			while (UtilMath.offset(_particleLoc, _holding.getEyeLocation().clone().add(0.0, 0.5, 0.0)) > .2)
			{
				if (_team.GetColor() == ChatColor.RED)
				{
					for (int i = 0 ; i < 2 ; i++)
						UtilParticle.PlayParticle(ParticleType.RED_DUST, _particleLoc, 0, 0, 0, 0, 1,
								ViewDist.MAX, UtilServer.getPlayers());
				}
				else
				{
					for (int i = 0 ; i < 2 ; i++)
						UtilParticle.PlayParticle(ParticleType.RED_DUST, _particleLoc, -1, 1, 1, 1, 0,
								ViewDist.MAX, UtilServer.getPlayers());
				}
				
				_particleLoc.add(UtilAlg.getTrajectory(_particleLoc, _holding.getEyeLocation().clone().add(0.0, 0.5, 0.0)).multiply(.2));
			}
			
			if (_host.GetTeam(_holding).GetColor() == ChatColor.RED)
				_holding.setCompassTarget(_host.getRedFlag());
			else
				_holding.setCompassTarget(_host.getBlueFlag());
			
			if (!_holding.hasPotionEffect(PotionEffectType.SLOW))
				_holding.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 1000000, 1));
				
			//_holding.getWorld().playSound(_holding.getLocation(), Sound.NOTE_STICKS, 2f, 1f);
			score(_holding);
		}
	}
	
	public void handleRespawn()
	{
		if (_moved)
		{
			if (_dropped)
			{
				_respawnTimer = _respawnTimer - 1;
			}
			else
			{
				if (_respawnTimer < 15)
					_respawnTimer = _respawnTimer + 1;
			}
		}
	}
	
	public void handleBottomInfo(boolean flicker, ChatColor color)
	{
		if (_holding != null)
		{
			if (flicker)
				UtilTextBottom.display(color + "Return the Flag to your Capture Zone!", _holding);
			else
				UtilTextBottom.display("Return the Flag to your Capture Zone!", _holding);
		}
	}
	
	private ChatColor remainColor(int remain)
	{
		if (remain >= 9) return ChatColor.GREEN;
		if ((remain < 9) && (remain >= 6)) return ChatColor.YELLOW;
		return ChatColor.RED;
	}

	@SuppressWarnings("deprecation")
	public void spawnFlag(Location loc)
	{
		while (!UtilBlock.airFoliage(loc.getBlock().getRelative(BlockFace.DOWN)))
			loc.add(0, -1, 0);
		
		while (!UtilBlock.airFoliage(loc.getBlock()))
			loc.add(0, 1, 0);
		
		_flagBlock = loc.getBlock();
		_host.getArcadeManager().getClassManager().GetSkillFactory().BlockRestore().restore(_flagBlock);
		
		_priorBlock = loc.getBlock().getType();
		
		_priorData = loc.getBlock().getData();
		
		_flagBlock.setType(Material.STANDING_BANNER);
		Banner banner = (Banner) _flagBlock.getState();
		banner.setBaseColor(_dyeData);
		banner.setPatterns(_patterns);
		banner.update();
		
		if (!_moved)
		{
			_name.setLocation(loc.getBlock().getLocation().clone().add(0.5, 3, 0.5));
			_name.start();
			_beacon.getBlock().setType(Material.BEACON);
			_respawnTimer = 15;
		}
		else
		{			
			_name.setLocation(loc.getBlock().getLocation().clone().add(0.5, 3, 0.5));
			_name.start();
			Integer re = Math.max(0, _respawnTimer);
			_time.setText(remainColor(re) + re.toString());
			_time.setLocation(loc.getBlock().getLocation().clone().add(0.5, 2.5, 0.5));
			_time.start();
		}
	}
	
	@SuppressWarnings("deprecation")
	private void deleteFlag()
	{
		_flagBlock.setType(_priorBlock);
		_flagBlock.setData(_priorData);
		_beacon.getBlock().setType(Material.WOOL);
		_beacon.getBlock().setData(_dyeData.getWoolData());
		
		_name.stop();
		_time.stop();
	}
	
	public boolean pickup(Player player)
	{
		if (_holding == null)
		{
			if (!UtilPlayer.isSpectator(player))
			{				
				if (_host.IsAlive(player))
				{
					if (_host.GetTeam(player) != _team)
					{
						if (_dropped)
						{
							if (!UtilTime.elapsed(_dropTime, 3000))
								return false;
						}
						
						deleteFlag();
						
						_host.saveInventory(player);
						
						for (int i = 0; i < 9; i++)
							player.getInventory().setItem(i, _representation);
						
						ItemStack compass = new ItemStack(Material.COMPASS);
						ItemMeta im = compass.getItemMeta();
						im.setDisplayName(C.cDAqua + "Your Base");
						compass.setItemMeta(im);
						player.getInventory().setItem(4, compass);
						player.getInventory().setHelmet(_representation);
						player.updateInventory();
						player.getInventory().setHeldItemSlot(4);
						
						for (PotionEffect effect : player.getActivePotionEffects())
							player.removePotionEffect(effect.getType());
						
						player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 1000000, 1));
						
						player.setPassenger(null);
						
						if (_moved)
							announceSteal(player, false);
						else
							announceSteal(player, true);
						
						_particleLoc = player.getEyeLocation().clone().add(0.0, 0.5, 0.0);
						_holding = player;
						_dropped = false;
						_dropTime = 0;
						_moved = true;
						
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	public void drop(Player player)
	{
		if (_moved)
		{
			if (_holding != null)
			{
				if (_holding.getUniqueId() == player.getUniqueId())
				{
					spawnFlag(player.getLocation());
					announceDrop(player);
					_holding = null;
					_dropped = true;
					_dropTime = System.currentTimeMillis();
				}
			}
		}
	}
	
	public void score(Player player)
	{
		if (_moved)
		{
			if (_holding != null)
			{
				if (_holding.getUniqueId() == player.getUniqueId())
				{
					if (_team.GetColor() == ChatColor.RED)
						if (!_host.isInZone(player.getLocation(), false)) return;
					
					if (_team.GetColor() == ChatColor.BLUE)
						if (!_host.isInZone(player.getLocation(), true)) return;
					
					if (!_host.isAtHome(_team.GetColor() == ChatColor.RED ? ChatColor.BLUE : ChatColor.RED))
					{
						if (_host.getArcadeManager().IsTournamentServer())
						{
							if (Recharge.Instance.use(player, "No Cap Message", 1000, false, false))
								UtilTextMiddle.display("", C.cRed + "Your flag must be at home to capture!", player);
							
							return;
						}
					}
					
					_host.resetInventory(player);
					announceCapture(player);
					_host.AddGems(player, 10, "Enemy Flag Captured", true, true);
					PlayerCaptureFlagEvent event = new PlayerCaptureFlagEvent(player);
					Bukkit.getPluginManager().callEvent(event);
					_host.addCapture(_host.GetTeam(player));
					
					_moved = false;
					_dropped = false;
					_dropTime = 0;
					spawnFlag(_spawn);
					_holding = null;
				}
			}
		}
	}
	
}
