package nautilus.game.arcade.game.games.christmasnew.section.six;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.utils.UtilVariant;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.game.games.christmasnew.ChristmasNew;
import nautilus.game.arcade.game.games.christmasnew.ChristmasNewAudio;
import nautilus.game.arcade.game.games.christmasnew.section.Section;
import nautilus.game.arcade.game.games.christmasnew.section.SectionChallenge;
import nautilus.game.arcade.game.games.christmasnew.section.six.phase.BossPhase;
import nautilus.game.arcade.game.games.christmasnew.section.six.phase.Phase1;
import nautilus.game.arcade.game.games.christmasnew.section.six.phase.Phase2;

class BossFight extends SectionChallenge
{

	private static final ItemStack HELMET = new ItemStack(Material.PUMPKIN);
	private static final int TIME_OF_DAY = 1200;

	private final Location _bossSpawn;
	private final List<Location> _lightning;
	private final List<Location> _playerSpawns;
	private final List<Location> _outerGate;
	private final List<Location> _innerGate;

	private final List<BossPhase> _phases;
	private BossPhase _currentPhase;

	private long _start;
	private boolean _enabled;

	private Skeleton _boss;

	private boolean _dialogueA;
	private boolean _dialogueB;
	private boolean _dialogueC;

	BossFight(ChristmasNew host, Location present, Section section)
	{
		super(host, present, section);

		_worldData.GetCustomLocs(String.valueOf(Material.IRON_ORE.getId())).forEach(location -> MapUtil.QuickChangeBlockAt(location, Material.BARRIER));
		_bossSpawn = _worldData.GetCustomLocs("BOSS SPAWN").get(0);
		_bossSpawn.setYaw(180);

		_lightning = _worldData.GetCustomLocs("LIGHTNING");
		_playerSpawns = _worldData.GetCustomLocs("PLAYER SPAWN");
		_outerGate = _worldData.GetCustomLocs(String.valueOf(Material.ENDER_STONE.getId()));
		_innerGate = _worldData.GetCustomLocs(String.valueOf(Material.IRON_ORE.getId()));

		_phases = new ArrayList<>(3);
		_phases.add(new Phase1(host, section));
		_phases.add(new Phase2(host, section));
	}

	@Override
	public void onPresentCollect()
	{
		_outerGate.forEach(location -> MapUtil.QuickChangeBlockAt(location, Material.NETHER_FENCE));
	}

	@Override
	public void onRegister()
	{
		_start = System.currentTimeMillis();
	}

	@Override
	public void onUnregister()
	{
		if (_currentPhase != null)
		{
			_currentPhase.onUnregister();
			_currentPhase.deactivate();
		}
	}

	@EventHandler
	public void updatePhase(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST || !_enabled)
		{
			return;
		}

		if (_currentPhase == null || _currentPhase.isComplete())
		{
			if (_currentPhase != null)
			{
				_currentPhase.onUnregister();
				_currentPhase.deactivate();
			}

			if (_phases.isEmpty())
			{
				return;
			}

			_currentPhase = _phases.remove(0);
			_currentPhase.setBoss(_boss);
			_currentPhase.activate();
			_currentPhase.onRegister();
		}
	}

	@EventHandler
	public void updateStart(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST || _enabled)
		{
			return;
		}

		if (UtilTime.elapsed(_start, 58000))
		{
			_host.sendSantaMessage("We’ll see about that. Prepare for battle!", ChristmasNewAudio.SANTA_PREPARE_FOR_BATTLE);
			_host.getSleigh().unloadSleigh();
			_worldData.World.setStorm(false);
			_section.setTimeSet(TIME_OF_DAY);
			_host.WorldTimeSet = TIME_OF_DAY;
			_host.WorldWeatherEnabled = false;
			_host.WorldChunkUnload = true;
			_enabled = true;

			int spawnIndex = 0;

			for (Player player : _host.GetPlayers(true))
			{
				player.leaveVehicle();
				player.teleport(_playerSpawns.get(spawnIndex));
				player.playSound(player.getLocation(), Sound.NOTE_PIANO, 1, 0.3F);

				if (++spawnIndex == _playerSpawns.size())
				{
					spawnIndex = 0;
				}
			}

			_innerGate.forEach(location -> MapUtil.QuickChangeBlockAt(location, Material.NETHER_FENCE));
		}
		else if (!_dialogueC && UtilTime.elapsed(_start, 50000))
		{
			_dialogueC = true;
			_host.sendBossMessage("Yes it was me! There is nothing you and your friends can do now, you are too late!", ChristmasNewAudio.PK_IT_WAS_ME);
		}
		else if (!_dialogueB && UtilTime.elapsed(_start, 43000))
		{
			_dialogueB = true;
			_host.sendSantaMessage("So it was you who stole all of the presents.", ChristmasNewAudio.SANTA_STOLE_PRESENTS);
		}
		else if (!_dialogueA && UtilTime.elapsed(_start, 36000))
		{
			_dialogueA = true;
			spawnBoss();
			_host.sendBossMessage("Hahaha! I’ve been expecting you Santa Claus. You fell right into my trap.", ChristmasNewAudio.PK_LAUGH);
		}

		if (Math.random() < 0.15)
		{
			Location location = UtilAlg.Random(_lightning);
			location.getWorld().strikeLightningEffect(location);
		}
	}

	private void spawnBoss()
	{
		_host.CreatureAllowOverride = true;

		_boss = UtilVariant.spawnWitherSkeleton(_bossSpawn);
		_boss.setCustomName(C.cGoldB + "The Pumpkin King");
		_boss.setCustomNameVisible(true);
		_boss.getEquipment().setHelmet(HELMET);
		_boss.setRemoveWhenFarAway(false);

		UtilEnt.vegetate(_boss);
		UtilEnt.ghost(_boss, true, false);

		_entities.add(_boss);

		_boss.getWorld().strikeLightningEffect(_bossSpawn);

		_host.CreatureAllowOverride = false;
	}

	@EventHandler
	public void bossDamage(CustomDamageEvent event)
	{
		if (_boss != null && event.GetDamageeEntity().equals(_boss) && !_enabled)
		{
			event.SetCancelled("Cut scene");
		}
	}
}
