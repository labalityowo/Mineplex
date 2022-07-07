package nautilus.game.arcade.game.games.halloween;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutCustomSoundEffect;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedSoundEffect;

import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import com.mineplex.ProtocolVersion;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilTime.TimeUnit;
import mineplex.core.gadget.gadgets.mount.types.MountPumpkin;
import mineplex.core.recharge.Recharge;
import mineplex.core.reward.Reward;
import mineplex.core.reward.rewards.GadgetReward;
import mineplex.core.titles.tracks.standard.HolidayCheerTrack;
import mineplex.core.treasure.reward.RewardRarity;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.SoloGame;
import nautilus.game.arcade.game.games.halloween.creatures.CreatureBase;
import nautilus.game.arcade.game.games.halloween.creatures.InterfaceMove;
import nautilus.game.arcade.game.games.halloween.kits.KitFinn;
import nautilus.game.arcade.game.games.halloween.kits.KitRobinHood;
import nautilus.game.arcade.game.games.halloween.kits.KitThor;
import nautilus.game.arcade.game.games.halloween.waves.Wave1;
import nautilus.game.arcade.game.games.halloween.waves.Wave2;
import nautilus.game.arcade.game.games.halloween.waves.Wave3;
import nautilus.game.arcade.game.games.halloween.waves.Wave4;
import nautilus.game.arcade.game.games.halloween.waves.Wave5;
import nautilus.game.arcade.game.games.halloween.waves.WaveBase;
import nautilus.game.arcade.game.games.halloween.waves.WaveBoss;
import nautilus.game.arcade.game.games.halloween.waves.WaveVictory;
import nautilus.game.arcade.game.modules.compass.CompassModule;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.managers.chat.ChatStatData;

public class Halloween extends SoloGame
{
	//Wave Data
	protected ArrayList<ArrayList<Location>> _spawns;

	protected ArrayList<WaveBase> _waves;
	protected int _wave = 0;

	protected int _maxMobs = 80;
	protected ArrayList<CreatureBase<?>> _mobs = new ArrayList<>();
	
	protected HashMap<Player, Long> _damageTime = new HashMap<Player, Long>();
	
	protected HashSet<Player> _soundOff = new HashSet<Player>();
	
	protected UpdateType _updateCreatureMoveRate = UpdateType.FASTEST;
	
	protected boolean doVoices = true;
	
	public String Objective = null;
	
	public long total = 0;
	public long move = 0;
	public int moves = 0;
	public long wave = 0;
	public long sound = 0;
	public long update = 0;
	public long damage = 0;
	public long target = 0;
	
	public long updateBossA = 0;
	public long updateBossB = 0;
	public long updateBossC = 0;
	public long updateBossD = 0;
	public long updateBossE = 0;
	public long updateBossF = 0;
	public long updateBossG = 0;
	public long updateBossH = 0;
	public long updateBossI = 0;
	public long updateBossJ = 0;
	public long updateBossK = 0;
	public long updateBossL = 0;
	public long updateBossM = 0;
	public long updateBossN = 0;
	public long updateBossO = 0;
	
	public boolean debug = false;
	public boolean bossDebug = false;
	
	public Halloween(ArcadeManager manager) 
	{
		this(manager, GameType.Halloween,

				new Kit[] 
						{
				new KitFinn(manager),
				new KitRobinHood(manager),
				new KitThor(manager),
						},

						new String[]
								{
				"Do not die.",
				"Work as a team!",
				"Defeat the waves of monsters",
				"Kill the Pumpkin King"
								});
	}
	
	protected Halloween(ArcadeManager manager, GameType gameType, Kit[] kits, String[] gameDesc)
	{
		super(manager, gameType, kits, gameDesc);
		

		this.DamagePvP = false;

		this.WorldTimeSet = 16000;

		this.ItemDrop = false; 
		this.ItemPickup = false;

		this.PrepareFreeze = false; 

		//this.HungerSet = 20;
		
		this.WorldBoundaryKill = false; 

		registerChatStats(
				DamageDealt,
				DamageTaken,
				BlankLine,
				new ChatStatData("kit", "Kit", true)
		);
		
		_help = new String[] 
				{
				C.cGreen + "Giants one hit kill you! Stay away!!!",
				C.cAqua + "Work together with your team mates.",
				C.cGreen + "Each kit gives a buff to nearby allies.",
				C.cAqua + "Kill monsters to keep their numbers down.",
				C.cGreen + "Kill giants quickly.",
				C.cAqua + "Defend your team mates from monsters.",
				C.cGreen + "Zombies, Giants and Spiders get faster over time.",
				C.cAqua + "Stick together to survive.",
				C.cGreen + "The Pumpkin King gets harder over time!",
				};

		new CompassModule()
				.setGiveCompass(true)
				.setGiveCompassToSpecs(true)
				.setGiveCompassToAlive(false)
				.register(this);
	}

	@Override
	public void ParseData() 
	{
		_spawns = new ArrayList<ArrayList<Location>>();
		_spawns.add(WorldData.GetDataLocs("RED"));
		_spawns.add(WorldData.GetDataLocs("YELLOW"));
		_spawns.add(WorldData.GetDataLocs("GREEN"));
		_spawns.add(WorldData.GetDataLocs("BLUE"));

		_waves = new ArrayList<WaveBase>();
		_waves.add(new Wave1(this));
		_waves.add(new Wave2(this));
		_waves.add(new Wave3(this));
		_waves.add(new Wave4(this));
		_waves.add(new Wave5(this));
		_waves.add(new WaveBoss(this));
		_waves.add(new WaveVictory(this, GetSpawnSet(3)));
		
		//Make zombies break doors
		WorldData.World.setDifficulty(Difficulty.HARD); 
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void Clean(GameStateChangeEvent event) 
	{
		if (event.GetState() != GameState.End)
			return;
		
		for (CreatureBase<?> ent : _mobs)
		{
			ent.remove();
		}
		
		_mobs.clear();
		_spawns.clear();
	}
	
	public ArrayList<CreatureBase<?>> getMobs()
	{
		return _mobs;
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void TeamGen(GameStateChangeEvent event) 
	{
		if (event.GetState() != GameState.Live)
			return;

		GetTeamList().add(new GameTeam(this, "Pumpkin King", ChatColor.RED, WorldData.GetDataLocs("RED")));
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void VoiceCommand(GameStateChangeEvent event) 
	{
		if (event.GetState() != GameState.Live)
			return;
		
		if(!doVoices) return;
		
		Announce(C.Bold + "Type " + C.cGreen + C.Bold + "/voice" + C.cWhite + C.Bold + " to disable voice audio.");
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void TimeReport(UpdateEvent event)
	{
		if (!IsLive())
			return;
		
		if (event.getType() != UpdateType.SLOW)
			return;
		
		if (debug)
		{
			System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
			System.out.println("Wave: " + _wave);
			System.out.println("Mobs: " + _mobs.size());
			System.out.println(" ");
			System.out.println("Total Time: " + UtilTime.convertString(total, 4, TimeUnit.MILLISECONDS));
			System.out.println("Move Time: " + UtilTime.convertString(move, 4, TimeUnit.MILLISECONDS));
			System.out.println("Move Count: " + moves);
			System.out.println("Wave Time: " + UtilTime.convertString(wave, 4, TimeUnit.MILLISECONDS));
			System.out.println("Sound Time: " + UtilTime.convertString(sound, 4, TimeUnit.MILLISECONDS));
			System.out.println("Update Time: " + UtilTime.convertString(update, 4, TimeUnit.MILLISECONDS));
			System.out.println("Damage Time: " + UtilTime.convertString(damage, 4, TimeUnit.MILLISECONDS));
			System.out.println("Target Time: " + UtilTime.convertString(target, 4, TimeUnit.MILLISECONDS));
		}
		
		if (bossDebug)
		{
			System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
			System.out.println("Update Boss State: " + UtilTime.convertString(updateBossA, 4, TimeUnit.MILLISECONDS));
			System.out.println("Update Boss Blocks: " + UtilTime.convertString(updateBossB, 4, TimeUnit.MILLISECONDS));
			System.out.println("Update Boss Health: " + UtilTime.convertString(updateBossC, 4, TimeUnit.MILLISECONDS));
			System.out.println("Update Boss Minion Orbit: " + UtilTime.convertString(updateBossD, 4, TimeUnit.MILLISECONDS));
			System.out.println("Update Boss Minion Move: " + UtilTime.convertString(updateBossE, 4, TimeUnit.MILLISECONDS));
			System.out.println("Update Boss Minion Attack: " + UtilTime.convertString(updateBossF, 4, TimeUnit.MILLISECONDS));
			System.out.println("Update Boss Minion Arrow: " + UtilTime.convertString(updateBossG, 4, TimeUnit.MILLISECONDS));
			System.out.println("Update Boss Minion Spawn: " + UtilTime.convertString(updateBossH, 4, TimeUnit.MILLISECONDS));
			System.out.println("Update Boss King Control: " + UtilTime.convertString(updateBossI, 4, TimeUnit.MILLISECONDS));
			System.out.println("Update Boss King Leap: " + UtilTime.convertString(updateBossJ, 4, TimeUnit.MILLISECONDS));
			System.out.println("Update Boss King Bomb: " + UtilTime.convertString(updateBossK, 4, TimeUnit.MILLISECONDS));
			System.out.println("Update Boss King Target: " + UtilTime.convertString(updateBossL, 4, TimeUnit.MILLISECONDS));
			System.out.println("Update Boss King Trail: " + UtilTime.convertString(updateBossM, 4, TimeUnit.MILLISECONDS));
			System.out.println("Update Boss Shield Orbit: " + UtilTime.convertString(updateBossN, 4, TimeUnit.MILLISECONDS));
			System.out.println("Update Boss Shield Spawn: " + UtilTime.convertString(updateBossO, 4, TimeUnit.MILLISECONDS));
		}

		if (debug || bossDebug)
			System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
		
		total = 0;
		move = 0;
		moves = 0;
		wave = 0;
		sound = 0;
		update = 0;
		damage = 0;
		target = 0;
		
		updateBossA = 0;
		updateBossB = 0;
		updateBossC = 0;
		updateBossD = 0;
		updateBossE = 0;
		updateBossF = 0;
		updateBossG = 0;
		updateBossH = 0;
		updateBossI = 0;
		updateBossJ = 0;
		updateBossK = 0;
		updateBossL = 0;
		updateBossM = 0;
		updateBossN = 0;
		updateBossO = 0;
	}
	
	@EventHandler
	public void SoundUpdate(UpdateEvent event)
	{
		long start = System.currentTimeMillis();
		
		if (event.getType() != UpdateType.SLOW)
			return;

		if (!IsLive())
			return;
		
		if(_waves.get(_wave) instanceof WaveVictory ||
				_waves.get(_wave) instanceof nautilus.game.arcade.game.games.halloween2016.wave.WaveVictory)
			return;

		if (Math.random() > 0.2)
			return;

		for (Player player : UtilServer.getPlayers())
			player.playSound(player.getLocation(), Sound.AMBIENCE_CAVE, 3f, 1f);
		
		total += System.currentTimeMillis() - start;
		sound += System.currentTimeMillis() - start;
	}

	@EventHandler
	public void WaveUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		if (!IsLive())
			return;

		if (_wave >= _waves.size())
		{
			EndCheck();
			return;
		}
		
		long start = System.currentTimeMillis();

		if (_waves.get(_wave).Update(_wave+1))
		{
			_wave++;

			EndCheck();
		}
		
		total += System.currentTimeMillis() - start;
		wave += System.currentTimeMillis() - start;
	}

	public ArrayList<Location> GetSpawnSet(int i)
	{
		return _spawns.get(i);
	}

	public Location GetRandomSpawn()
	{
		ArrayList<Location> locSet = GetSpawnSet(UtilMath.r(_spawns.size()));
		return locSet.get(UtilMath.r(locSet.size()));
	}

	public void AddCreature(CreatureBase<?> mob) 
	{
		_mobs.add(0, mob);
	}

	public ArrayList<CreatureBase<?>> GetCreatures()
	{
		return _mobs;
	}

	@EventHandler
	public void CreatureMoveUpdate(UpdateEvent event)
	{	
		if (event.getType() != _updateCreatureMoveRate)
			return;

		if (_mobs.isEmpty())
			return;
		
		long start = System.currentTimeMillis();
		
		CreatureBase<?> base = _mobs.remove(0);

		if (base instanceof InterfaceMove)
		{
			InterfaceMove move = (InterfaceMove)base;

			move.Move();
		}

		_mobs.add(base);
		
		total += System.currentTimeMillis() - start;
		move += System.currentTimeMillis() - start;
	}

	@EventHandler
	public void CreatureUpdate(UpdateEvent event)
	{
		long start = System.currentTimeMillis();
		
		if (!IsLive())
			return;

		//Clean
		Iterator<CreatureBase<?>> mobIterator = _mobs.iterator();
		while (mobIterator.hasNext())
		{	
			CreatureBase<?> base = mobIterator.next();

			if (base.Updater(event))
			{
				onRemove(base);
				base.remove();
				mobIterator.remove();
			}
		}
		
		total += System.currentTimeMillis() - start;
		update += System.currentTimeMillis() - start;
	}
	
	public void onRemove(CreatureBase<?> mob)
	{
	}

	@EventHandler
	public void CreatureDamage(CustomDamageEvent event)
	{
		long start = System.currentTimeMillis();
		
		for (CreatureBase<?> base : _mobs)
			base.Damage(event);
		
		total += System.currentTimeMillis() - start;
		damage += System.currentTimeMillis() - start;
	}

	@EventHandler
	public void CreatureTarget(EntityTargetEvent event)
	{
		long start = System.currentTimeMillis();
		
		for (CreatureBase<?> base : _mobs)
			base.Target(event);
		
		total += System.currentTimeMillis() - start;
		target += System.currentTimeMillis() - start;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void EntityDeath(EntityDeathEvent event)
	{
		event.getDrops().clear();
	}

	@Override
	public void EndCheck()
	{	
		if (!IsLive())
			return;

		if (_wave >= _waves.size())
		{	
			for (Player player : GetPlayers(false))
			{
				Manager.GetGame().AddGems(player, 30, "Killing the Pumpkin King", false, false);
				Manager.GetGame().AddGems(player, 10, "Participation", false, false);
			}
			
			if (Manager.IsRewardItems())
			{
				SetCustomWinLine("You earned the Pumpkin Mount!");
				Reward reward = new GadgetReward(Manager.getCosmeticManager().getGadgetManager().getGadget(MountPumpkin.class), RewardRarity.LEGENDARY, 0);

				for (Player player : GetPlayers(false))
				{
					//Prevent game hopping
					if (!player.isOnline())
						continue;
					
					reward.giveReward(player, data -> {});

					if (IsAlive(player))
					{
						Manager.getTrackManager().getTrack(HolidayCheerTrack.class).wonGame(player);
					}
					Manager.getTrackManager().getTrack(HolidayCheerTrack.class).wonRound(player);
				}
			}
			
			AnnounceEnd(this.GetTeamList().get(0));
			
			SetState(GameState.End); 
		}

		else if (GetPlayers(true).size() == 0)
		{
			playSound(HalloweenAudio.BOSS_WIN);
			
			for (Player player : GetPlayers(false))
			{
				Manager.GetGame().AddGems(player, 10, "Participation", false, false);
				Manager.getTrackManager().getTrack(HolidayCheerTrack.class).wonRound(player);
			}
			
			AnnounceEnd(this.GetTeamList().get(1));

			SetState(GameState.End);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void Explosion(EntityExplodeEvent event)
	{
		if (!IsLive())
			return;
		
		if (event.getEntity() instanceof Fireball)
		{
			event.blockList().clear();

			Collection<Block> blocks = UtilBlock.getInRadius(event.getLocation(), 3.5d).keySet();

			Iterator<Block> blockIterator = blocks.iterator();

			while (blockIterator.hasNext())
			{
				Block block = blockIterator.next();

				if (block.getY() < 4 || block.getY() <= WorldData.MinY)
					blockIterator.remove();
			}

			Manager.GetExplosion().BlockExplosion(blocks, event.getLocation(), false);
		}
	}

	@EventHandler
	public void makeFireballsNotGetRektBcFireballsAreRudeAf(EntityDamageByEntityEvent event)
	{
		if (!IsLive())
			return;
		
		if (!(event.getDamager() instanceof Player))
			return;
		
		Player player = (Player) event.getDamager();
		
		if (!UtilPlayer.isSpectator(player) || IsAlive(player))
			return;
		
		if (!(event.getEntity() instanceof Fireball))
			return;
		
		event.setCancelled(true);
	}
	
	@EventHandler
	public void ItemSpawn(ItemSpawnEvent event)
	{
		Material type = event.getEntity().getItemStack().getType();

		if (type == Material.DIAMOND_AXE || type == Material.BLAZE_POWDER || type == Material.SNOW_BALL || type == Material.getMaterial(175))
			return;

		event.setCancelled(true);
	}

	public int GetMaxMobs()
	{
		return _maxMobs;
	}
	
	@Override
	@EventHandler
	public void ScoreboardUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		//Wipe Last
		Scoreboard.reset();
		
		//Rounds
		Scoreboard.writeNewLine();
		Scoreboard.write(C.cYellow + C.Bold + "Wave");
		Scoreboard.write(Math.min(6, _wave+1) + " of 6");
		
		Scoreboard.writeNewLine();
		Scoreboard.write(C.cYellow + C.Bold + "Monsters");
		Scoreboard.write("" + _mobs.size());
		
		//Drawer
		Scoreboard.writeNewLine();
		Scoreboard.write(C.cYellow + C.Bold + "Players");
		
		if (GetPlayers(true).size() < 5)
		{
			for (Player player : GetPlayers(true))
			{
				Scoreboard.write(C.cWhite + player.getName());
			}
		}
		else
		{
			Scoreboard.write(GetPlayers(true).size() + " Alive");
		}
		
		if(Objective != null)
		{
			Scoreboard.writeNewLine();
			Scoreboard.write(C.cYellow + C.Bold + "Objective");
			Scoreboard.write(Objective);
		}
		
		Scoreboard.draw();
	}
	
	@EventHandler
	public void soundOff(PlayerCommandPreprocessEvent event)
	{
		if(!doVoices) return;
		
		if (event.getMessage().equalsIgnoreCase("/voice"))
		{
			if (_soundOff.remove(event.getPlayer()))
			{
				UtilPlayer.message(event.getPlayer(), C.Bold + "Voice Audio: " + C.cGreen + "Enabled");
			}
			else
			{
				_soundOff.add(event.getPlayer());
				
				UtilPlayer.message(event.getPlayer(), C.Bold + "Voice Audio: " + C.cRed + "Disabled");
			}
			
			event.setCancelled(true);
		}
	}

	// Custom handling to make sure 1.12+ actually hear the sound
	public void playSound(NamedAudio audio)
	{
		if (!doVoices)
		{
			return;
		}

		for (Player player : UtilServer.getPlayers())
		{
			if (!_soundOff.contains(player))
			{
				Packet packet;
				int protocol = ((CraftPlayer) player).getHandle().getProtocol();
				if (protocol >= ProtocolVersion.v1_12)
				{
					packet = new PacketPlayOutCustomSoundEffect(audio.getAudioPath(),
							player.getLocation().getX(), player.getLocation().getY(),
							player.getLocation().getZ(), 20f, 1f);
				} else
				{
					packet = new PacketPlayOutNamedSoundEffect(audio.getAudioPath(),
							player.getLocation().getBlockX(), player.getLocation().getBlockY(),
							player.getLocation().getBlockZ(),
							20f, 1f);
				}

				UtilPlayer.sendPacket(player, packet);
			}
		}
	}
	
	private int hungerTick = 0;

	@EventHandler
	public void Hunger(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;
		
		if (!IsLive())
			return;
		
		if (_mobs.size() < 30)
		{
			for (Player player : GetPlayers(true))
				UtilPlayer.hunger(player, 1);
			return;
		}
		
		int rate = 4;
		if (_mobs.size() > 60)
			rate = 3;
		if (_mobs.size() > 70)
			rate = 2;
		if (_mobs.size() >= 80)
			rate = 1;
		
		hungerTick = (hungerTick + 1)%rate;
		
		for (Player player : GetPlayers(true))
		{
			if (_damageTime.containsKey(player))
			{
				if (!UtilTime.elapsed(_damageTime.get(player), 2000))
					continue;
			}
			
			player.setSaturation(3f); 
			player.setExhaustion(0f);
			
			if (player.getFoodLevel() <= 2)
			{
				if (Recharge.Instance.use(player, "Food Message", 6000, false, false))
					UtilPlayer.message(player, F.main("Game", "Attack monsters to restore hunger!"));
			}
			
			if (hungerTick == 0)
			{
				if (player.getFoodLevel() > 2)
				{
					UtilPlayer.hunger(player, -1);
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void HungerRestore(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;
		
		if (event.GetDamagerPlayer(true) == null)
			return;
		
		if (event.GetDamage() <= 1)
			return;

		Player damager = event.GetDamagerPlayer(true);
		if (damager == null)
			return;

		if (!Recharge.Instance.use(damager, "Hunger Restore", 100, false, false))
			return;
		
		_damageTime.put(damager, System.currentTimeMillis());

		if (event.GetCause() == DamageCause.PROJECTILE)
			UtilPlayer.hunger(damager, 6);
		else
			UtilPlayer.hunger(damager, 4);
	}
}
