package nautilus.game.arcade.game.games.christmas;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftHorse;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.C;
import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.packethandler.IPacketHandler;
import mineplex.core.packethandler.PacketInfo;
import mineplex.core.recharge.Recharge;
import mineplex.core.titles.tracks.standard.HolidayCheerTrack;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GamePrepareCountdownCommence;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.christmas.kits.KitPlayer;
import nautilus.game.arcade.game.games.christmas.parts.Part;
import nautilus.game.arcade.game.games.christmas.parts.Part1;
import nautilus.game.arcade.game.games.christmas.parts.Part2;
import nautilus.game.arcade.game.games.christmas.parts.Part3;
import nautilus.game.arcade.game.games.christmas.parts.Part4;
import nautilus.game.arcade.game.games.christmas.parts.Part5;
import nautilus.game.arcade.game.modules.compass.CompassModule;
import nautilus.game.arcade.kit.Kit;
import net.minecraft.server.v1_8_R3.PacketPlayOutAttachEntity;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedSoundEffect;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;

public class Christmas extends ChristmasCommon
{
	private GameTeam _badGuys;
	
	private Sleigh _sleigh;
	
	private Location _sleighSpawn;
	
	private ArrayList<Part> _parts = new ArrayList<Part>();
	private Part _part;
	
	private ArrayList<Location> _barrier = new ArrayList<Location>();
	
	private long _gameTime = 1200000;
	
	private long _santaSayTime = 0;
	
	private IPacketHandler _reindeerPackets = new IPacketHandler()
    {

        @Override
        public void handle(PacketInfo packetInfo)
        {
            if (_sleigh == null)
                return;

            if (packetInfo.getPacket() instanceof PacketPlayOutSpawnEntityLiving)
            {
                PacketPlayOutSpawnEntityLiving spawnPacket = (PacketPlayOutSpawnEntityLiving) packetInfo.getPacket();

                for (SleighHorse horse : _sleigh.getHorses())
                {
                    if (horse.horseId == spawnPacket.a)
                    {
                        horse.spawnHorns(packetInfo.getPlayer());
                        break;
                    }
                }
            }
            else if (packetInfo.getPacket() instanceof PacketPlayOutEntityDestroy)
            {
                try
                {
                    PacketPlayOutEntityDestroy destroyPacket = (PacketPlayOutEntityDestroy) packetInfo.getPacket();
                    int[] entityIds = destroyPacket.a;
					int origLength = entityIds.length;
					for (int a = 0; a < entityIds.length; a++)
                    {
                        for (SleighHorse horse : _sleigh.getHorses())
                        {
                            if (horse.horseId == a)
                            {
                                int p = entityIds.length;
                                entityIds = Arrays.copyOf(entityIds, entityIds.length + 3);
                                for (int i = 0; i < 3; i++)
                                {
                                    entityIds[p + i] = horse.hornsAndNose[i];
                                }
                                break;
                            }
                        }
                    }
                    if (entityIds.length != origLength)
                    {
                    	destroyPacket.a = entityIds;
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        }
    };
        public boolean ReachedEnding = false;
	
	public Christmas(ArcadeManager manager)
	{
		super(manager, GameType.Christmas,

				new Kit[] 
						{ 
				new KitPlayer(manager)
						},

						new String[]
								{
						"Follow Santa Claus",
						"Find the 10 Stolen Presents",
						"Defeat the Thief who stole the Presents!"
								});
		
		BlockBreakAllow.add(4);
		HungerSet = 20;
		WorldTimeSet = 2000;
		PrepareFreeze = false;

		registerChatStats(
				DamageDealt,
				DamageTaken
		);

		new CompassModule()
				.setGiveCompass(true)
				.setGiveCompassToSpecs(true)
				.setGiveCompassToAlive(false)
				.register(this);
	}
	
	//parse 129 19 47 48 103 86 137 56 22 45 121 14 15 16 87 88 89 153 173 172 162
	@Override
	public void ParseData()
	{
		//Sleigh Spawn
		_sleighSpawn = WorldData.GetDataLocs("RED").get(0);
 
		//Sleigh Waypoints
		ArrayList<Location> _sleighWaypoints = new ArrayList<Location>();
		while (!WorldData.GetDataLocs("PINK").isEmpty())
		{
			Location bestLoc = null;
			double bestDist = 0;
			
			for (Location loc : WorldData.GetDataLocs("PINK"))
			{
				double dist = UtilMath.offset(loc, _sleighSpawn);
				
				if (bestLoc == null || bestDist > dist)
				{
					bestLoc = loc;
					bestDist = dist;
				}
			}
			
			_sleighWaypoints.add(bestLoc);
			WorldData.GetDataLocs("PINK").remove(bestLoc);
		}
		
		//Presents
		ArrayList<Location> _presents = new ArrayList<Location>();
		while (!WorldData.GetDataLocs("LIME").isEmpty())
		{
			Location bestLoc = null;
			double bestDist = 0;
			
			for (Location loc : WorldData.GetDataLocs("LIME"))
			{
				double dist = UtilMath.offset(loc, _sleighSpawn);
				
				if (bestLoc == null || bestDist > dist)
				{
					bestLoc = loc;
					bestDist = dist;
				}
			}
			
			_presents.add(bestLoc);
			WorldData.GetDataLocs("LIME").remove(bestLoc);
		}
		
		//Barriers
		for (Location loc : WorldData.GetCustomLocs("129"))
		{
			_barrier.add(loc.getBlock().getLocation());
			MapUtil.QuickChangeBlockAt(loc, 166, (byte)0);
		}
		
		//Parts
		_parts.add(new Part1(this, _sleighWaypoints.remove(0), new Location[] {_presents.remove(0), _presents.remove(0)},
				WorldData.GetDataLocs("BLACK"), 
				WorldData.GetDataLocs("ORANGE"), 
				WorldData.GetCustomLocs("19"), 
				WorldData.GetCustomLocs("47")));
		
		_parts.add(new Part2(this, _sleighWaypoints.remove(0), new Location[] {_presents.remove(0), _presents.remove(0)},
				WorldData.GetDataLocs("YELLOW"), 
				WorldData.GetDataLocs("BROWN"), 
				WorldData.GetCustomLocs("48")));

		_parts.add(new Part3(this, _sleighWaypoints.remove(0), new Location[] {_presents.remove(0), _presents.remove(0)},
				WorldData.GetDataLocs("GRAY"), 
				WorldData.GetCustomLocs("103"),
				WorldData.GetCustomLocs("86"), 
				WorldData.GetCustomLocs("137"),
				WorldData.GetDataLocs("WHITE"), 
				WorldData.GetDataLocs("PURPLE")));
		
		_parts.add(new Part4(getArcadeManager().getHologramManager(), this, _sleighWaypoints.remove(0), new Location[] {_presents.remove(0), _presents.remove(0)},
				WorldData.GetCustomLocs("56"), 
				WorldData.GetDataLocs("MAGENTA"), 
				WorldData.GetCustomLocs("22"), 
				WorldData.GetCustomLocs("45"),
				WorldData.GetCustomLocs("121"),
				WorldData.GetCustomLocs("162"),
				_sleighWaypoints.get(0)));
		
		_parts.add(new Part5(this, _sleighWaypoints.remove(0), new Location[] {_presents.remove(0), _presents.remove(0)},
				WorldData.GetCustomLocs("14"), 
				WorldData.GetCustomLocs("15"), 
				WorldData.GetCustomLocs("16"),
				WorldData.GetCustomLocs("87"),
				WorldData.GetCustomLocs("88"), 
				WorldData.GetCustomLocs("89"), 
				WorldData.GetCustomLocs("153"),
				WorldData.GetCustomLocs("173")));
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void TeamGen(GameStateChangeEvent event) 
	{
		if (event.GetState() != GameState.Live)
			return;

		_badGuys = new GameTeam(this, "Christmas Thieves", ChatColor.RED, WorldData.GetDataLocs("RED"));
		AddTeam(_badGuys);
	}
	
	@EventHandler
	public void PartUpdate(UpdateEvent event)
	{
		if (!IsLive())
			return;
		
		if (event.getType() == UpdateType.SEC)
		{
			if ((_part == null || _part.IsDone()) && _parts != null && !_parts.isEmpty())
			{

				if (_part != null)
					HandlerList.unregisterAll(_part);
				
				_part = _parts.remove(0);
				
				_part.Prepare();
				
				//Register Updates
				UtilServer.getServer().getPluginManager().registerEvents(_part, Manager.getPlugin());
				
				GetSleigh().SetTarget(_part.GetSleighWaypoint()); 
			}
		}
	}
	
	public Sleigh GetSleigh()
	{
		if (_sleigh == null)
		{
			_sleigh = new Sleigh();
			_sleigh.setupSleigh(this, _sleighSpawn);
		}
		
		return _sleigh;
	}
	
	@EventHandler
	public void Intro(GamePrepareCountdownCommence event) 
	{
		SantaSay("Thank you for coming! Someone has stolen all of our Christmas presents! I need your help!", ChristmasAudio.INTRO);
	}
	
	@EventHandler
	public void SleighSpawn(GameStateChangeEvent event) 
	{
		if (event.GetState() != GameState.Prepare)
			return;
		
		
		
		final Christmas christmas = this;
		
		UtilServer.getServer().getScheduler().scheduleSyncDelayedTask(Manager.getPlugin(), new Runnable()
		{
			public void run()
			{
						getArcadeManager().getPacketHandler().addPacketHandler(
								_reindeerPackets,
								PacketPlayOutEntityDestroy.class,
								PacketPlayOutSpawnEntityLiving.class);
			    
				GetSleigh();
				
				Location loc = christmas.GetSleigh().GetLocation();
				
				christmas.CreatureAllowOverride = true;
				for (int i=0 ; i<20 ; i++)
				{
					Location elfLoc = UtilBlock.getHighest(loc.getWorld(), (int)(loc.getX() + 20 - Math.random()*40), (int)(loc.getZ() + 20 - Math.random()*40)).getLocation().add(0.5, 0.5, 0.5);
					
					Villager elf = elfLoc.getWorld().spawn(elfLoc, Villager.class);

					elf.setBaby();
					elf.setAgeLock(true);
					
					elf.setCustomName("Elf");
					
				}
				christmas.CreatureAllowOverride = false;
			}
		}, 20);
	}
	
	@EventHandler
	public void SleighUpdate(UpdateEvent event)
	{
	    if (event.getType() != UpdateType.TICK || _sleigh == null)
	        return;
	    
	    for (SleighHorse horse : _sleigh.getHorses())
	    {
	        horse.onTick();
	    }
	    
		if (!IsLive())
			return;
		
		GetSleigh().Update();
	}
	
	@EventHandler
	public void barrierDecay(UpdateEvent event)
	{
		if (!IsLive())
			return;
		
		if (event.getType() != UpdateType.SEC)
			return;
		
		Location breakAt = null;
		
		for (Location loc : _barrier)
		{
			if (UtilMath.offset(GetSleigh().GetLocation(), loc) > 20)
				continue;
			
			breakAt = loc;
		}
		
		if (breakAt != null)
			 BarrierBreak(breakAt);
	}

	private void BarrierBreak(Location loc) 
	{
		_barrier.remove(loc);
		loc.getBlock().setType(Material.AIR);
		
		for (Block block : UtilBlock.getSurrounding(loc.getBlock(), false))
		{
			if (_barrier.remove(block.getLocation()))
			{
				BarrierBreak(block.getLocation());
			}
		}
	}

	public void SantaSay(String string, ChristmasAudio audio) 
	{
		Announce(C.cRed + C.Bold + "Santa: " + ChatColor.RESET + C.cYellow + string, false);
		
		//Audio
		if (audio != null && UtilTime.elapsed(_santaSayTime, 1500))
		{
			_santaSayTime = System.currentTimeMillis();
			
			for (Player player : UtilServer.getPlayers())
			{
				PacketPlayOutNamedSoundEffect packet = new PacketPlayOutNamedSoundEffect(audio.getName(), 
						player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ(), 
						20f, 1F);
				
				UtilPlayer.sendPacket(player, packet);
			}
		}
			
	}
	
	public void BossSay(String name, String string, ChristmasAudio audio) 
	{
		for (Player player : UtilServer.getPlayers())
		{
			UtilPlayer.message(player, C.cDGreen + C.Bold + name + ": " + ChatColor.RESET + C.cGreen + string, false);
			
			if (audio != null)
			{
				PacketPlayOutNamedSoundEffect packet = new PacketPlayOutNamedSoundEffect(audio.getName(), 
						player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ(), 
						20f, 1F);
				
				UtilPlayer.sendPacket(player, packet);
			}
		}
	}
	
	@EventHandler
	public void Combust(EntityCombustEvent event)
	{
		event.setCancelled(true);
	}
	
	@EventHandler
	public void ItemSpawn(ItemSpawnEvent event)
	{
		event.setCancelled(true);
	}
	
	@EventHandler
	public void BlockFade(BlockFadeEvent event)
	{
		event.setCancelled(true);
	}
	
	@Override
	public void EndCheck()
	{
		if (!IsLive())
			return;

		if (_parts.isEmpty() && _part.IsDone())
		{
			SantaSay("Well done! You've saved Christmas!", ChristmasAudio.END_WIN2);
			
			for (final Player player : GetPlayers(false))
			{
				if (Manager.IsRewardItems())
				{
					if (!player.isOnline())
						continue;

					if (IsAlive(player))
					{
						Manager.getTrackManager().getTrack(HolidayCheerTrack.class).wonGame(player);
					}

					Manager.getTrackManager().getTrack(HolidayCheerTrack.class).wonRound(player);

					if (Manager.GetDonation().Get(player).ownsUnknownSalesPackage("Christmas Kings Head"))
					{
						SetCustomWinMessage(player, "You already earned your reward");
					}
					else
					{
						SetCustomWinMessage(player, "You earned " + C.cYellow + "Christmas King Morph");

						Manager.GetDonation().purchaseUnknownSalesPackage(player, "Christmas Kings Head", GlobalCurrency.TREASURE_SHARD, 0, true, null);
					}
				}
				
				Manager.GetGame().AddGems(player, 30, "Slaying the Pumpkin King", false, false);
				Manager.GetGame().AddGems(player, 10, "Participation", false, false);
			}
			
			AnnounceEnd(getPlayersTeam());
			SetState(GameState.End);
		}
		else if (GetPlayers(true).size() == 0)
		{
			for (Player player : GetPlayers(false))
			{
				Manager.getTrackManager().getTrack(HolidayCheerTrack.class).wonRound(player);
				Manager.GetGame().AddGems(player, 10, "Participation", false, false);
			}

			SetCustomWinLine("You all died...");
			AnnounceEnd(_badGuys);
			SetState(GameState.End);
		}
		else if (UtilTime.elapsed(GetStateTime(), _gameTime) && !ReachedEnding)
		{
			for (Player player : GetPlayers(false))
			{
				Manager.getTrackManager().getTrack(HolidayCheerTrack.class).wonRound(player);
				Manager.GetGame().AddGems(player, 10, "Participation", false, false);
			}

			SetCustomWinLine("You did not save Christmas in time.");
			AnnounceEnd(_badGuys);
			SetState(GameState.End);
		}
		
	}
	
	public void End() 
	{
		if (!IsLive())
			return;
		
		for (Player player : GetPlayers(false))
		{
			Manager.GetGame().AddGems(player, 10, "Participation", false, false);
		}
	
		SetCustomWinLine("Santa Claus was killed by the Giant!");
		AnnounceEnd(_badGuys);
		SetState(GameState.End);
	}
	
	@EventHandler
	public void Skip(PlayerCommandPreprocessEvent event)
	{
		if (event.getMessage().equals("/skip"))
			if (event.getPlayer().getName().equals("Chiss"))
			{
				event.setCancelled(true);

				if (_part != null)
					HandlerList.unregisterAll(_part);
				
				if (_parts != null && !_parts.isEmpty())
				{
					_part = _parts.remove(0);
					
					_part.Prepare();
					
					//Register Updates
					UtilServer.getServer().getPluginManager().registerEvents(_part, Manager.getPlugin());
					
					GetSleigh().SetTarget(_part.GetSleighWaypoint()); 
					
					//XXX REMOVE
					for (Player player : UtilServer.getPlayers())
						player.teleport(_part.GetSleighWaypoint().clone().add(0, 0, 10));
				}
			}
		
		if (event.getMessage().equals("/present"))
			if (event.getPlayer().getName().equals("Chiss"))
			{
				event.setCancelled(true);

				GetSleigh().AddPresent(event.getPlayer().getLocation());
			}
				
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void DamageCancel(CustomDamageEvent event)
	{
		if (_sleigh != null)
			GetSleigh().onDamage(event);
		
		if (event.GetCause() == DamageCause.FALL)
			if (event.GetDamageeEntity().getLocation().getY() > 30)
			{
				event.SetCancelled("Fall Cancel");
			}
			else
			{
				event.AddMod("Christmas", "Fall Damage", 20, false);
			}		
	}
	
	
	@Override
	@EventHandler
	public void ScoreboardUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;
		
		if (!IsLive())
		{
			Scoreboard.reset();
			
			String line = "";
			for (int i = 0 ; i < 20 ; i++)
				line += ((i % 2 == 0 ? C.cRed : C.cDGreen) + "█");
			
			Scoreboard.writeNewLine();
			Scoreboard.write(line);
			
			Scoreboard.writeNewLine();
			
			Scoreboard.write(C.cWhiteB + "May your winter");
			Scoreboard.write(C.cWhiteB + "be filled with");
			Scoreboard.write(C.cYellowB + "joy" + C.cWhiteB + " and " + C.cYellowB + "cheer.");
			
			Scoreboard.writeNewLine();
			
			Scoreboard.write(C.cAquaB + "Happy Holidays!");
			
			Scoreboard.writeNewLine();
			
			Scoreboard.write(C.cWhiteB + "- Your friends at");
			Scoreboard.write(C.cGoldB + "MINEPLEX");
			
			Scoreboard.writeNewLine();
			Scoreboard.write(line);
			
			Scoreboard.draw();
			return;
		}

		//Wipe Last
		Scoreboard.reset();
		
		//Rounds
		Scoreboard.writeNewLine();
		Scoreboard.write(C.cGoldB + "Challenge");
		Scoreboard.write(C.cWhite + (5 - _parts.size()) + " of " + 5);
		
		//Presents
		Scoreboard.writeNewLine();
		Scoreboard.write(C.cGreenB + "Presents");
		Scoreboard.write(C.cWhite + GetSleigh().getPresents().size() + " of " + 10);
		
		//Players
		Scoreboard.writeNewLine();
		Scoreboard.write(C.cYellowB + "Players");
		Scoreboard.write(C.cWhite + GetPlayers(true).size());
				
		//Time
		if (!ReachedEnding)
		{
		        Scoreboard.writeNewLine();
		        Scoreboard.write(C.cYellowB + "Time Left");
		        Scoreboard.write(C.cWhite + UtilTime.MakeStr(_gameTime - (System.currentTimeMillis() - GetStateTime())));
		}
		
		Scoreboard.draw();
	}
	
	@Override
	public Location GetSpectatorLocation()
	{
		if (SpectatorSpawn == null)
		{
			SpectatorSpawn = new Location(this.WorldData.World, 0,0,0);
		}

		Vector vec = new Vector(0,0,0);
		double count = 0;

		for (Player player : GetPlayers(true))
		{				
			count++;
			vec.add(player.getLocation().toVector());
		}

		if (count == 0)
			count++;

		vec.multiply(1d/count);

		SpectatorSpawn.setX(vec.getX());
		SpectatorSpawn.setY(vec.getY() + 10);
		SpectatorSpawn.setZ(vec.getZ());

		return SpectatorSpawn;
	}
	
	@EventHandler
	public void ProjectileClean(ProjectileHitEvent event)
	{
		event.getEntity().remove();
	}
	
	@EventHandler
	public void DeregisterListeners(GameStateChangeEvent event) 
	{
		if (event.GetState() != GameState.End && event.GetState() != GameState.Dead)
			return;
		
		this.getArcadeManager().getPacketHandler().removePacketHandler(_reindeerPackets);
		if (_part != null)
			HandlerList.unregisterAll(_part);
	}
	
	@EventHandler
	public void DamageElf(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;

		if (event.GetDamagerPlayer(true) == null)
			return;

		if (event.GetDamageeEntity() instanceof Villager)
		{
			if (!Recharge.Instance.use(event.GetDamagerPlayer(true), "Elf Hit", 2000, false, false))
				return;

			event.AddMod("Elf", "Negate", -event.GetDamageInitial(), false);
			event.AddMod("Elf", "Add", 4, false);
			
			double r = Math.random();
			
			ChristmasAudio 	audio = ChristmasAudio.ELF1;
			if (r > 0.2)	audio = ChristmasAudio.ELF2;
			if (r > 0.4)	audio = ChristmasAudio.ELF3;
			if (r > 0.6)	audio = ChristmasAudio.ELF4;
			if (r > 0.8)	audio = ChristmasAudio.ELF5;
			
			PacketPlayOutNamedSoundEffect packet = new PacketPlayOutNamedSoundEffect(audio.getName(), 
					event.GetDamageeEntity().getLocation().getBlockX(), event.GetDamageeEntity().getLocation().getBlockY(), event.GetDamageeEntity().getLocation().getBlockZ(), 
					10f, 1.2F);
			
			for (Player player : UtilServer.getPlayers())
				UtilPlayer.sendPacket(player, packet);
		}
	}
	
	@EventHandler
	public void updateReigns(UpdateEvent event)
	{
		if (!InProgress())
			return;
		
		if (event.getType() != UpdateType.FASTER)
			return;
		
		if (_sleigh == null)
			return;
		
		if (_sleigh.getSanta() == null)
			return;
		
		for (SleighHorse horse : _sleigh.getHorses())
		{
			if (horse.Ent == null || !horse.Ent.isValid())
				continue;
			
			PacketPlayOutAttachEntity packet = new PacketPlayOutAttachEntity(1, ((CraftHorse) horse.Ent).getHandle(), (((CraftEntity) _sleigh.getSanta()).getHandle()));
			
			for (Player player : UtilServer.getPlayers())
				UtilPlayer.sendPacket(player, packet);
		}
	}
	
	@EventHandler
	public void preventStand(PlayerInteractAtEntityEvent event)
	{
		event.setCancelled(true);
	}
}
