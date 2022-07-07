package mineplex.game.clans.clans.worldevent.kinghill;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mineplex.core.common.util.*;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import mineplex.core.thereallyoldscoreboardapiweshouldremove.ScoreboardManager;
import mineplex.game.clans.clans.ClanInfo;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.worldevent.WorldEventManager;
import mineplex.game.clans.economy.GoldManager;
import mineplex.minecraft.game.core.boss.EventMap;
import mineplex.minecraft.game.core.boss.EventState;
import mineplex.minecraft.game.core.boss.WorldEvent;

public class KingHill extends WorldEvent
{
	private static List<HillData> LOADED_HILLS = new ArrayList<HillData>();
	
	private static int GOLD_PER_TICK = 4;
	
	private ClanInfo _clanOnHill;
	
	static
	{
		// TODO load hills from schematic folder with extra hill data from a
		// config file?
		try
		{
			LOADED_HILLS.add(new HillData("ClansKOTH.schematic", 28, 28, 28, 5, 5, 5));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
	}
	
	private ClansManager _clansManager;
	private Map<ClanInfo, CaptureData> _scoreMap;
	private HillData _hill;
	private long _nextLootDrop;
	private int _lootDropCount;
	
	private long _lastGoldDrop;
	private long _lastOnHillMessage;
	private long _goldDrops;
	
	public KingHill(WorldEventManager eventManager, Location centerLocation, SkillFactory skillFactory)
	{
		super(eventManager.getClans().getDisguiseManager(), eventManager.getClans().getProjectile(), eventManager.getDamage(), eventManager.getBlockRestore(), eventManager.getClans().getCondition(), "King of the Hill", centerLocation);
		_clansManager = eventManager.getClans();
		_scoreMap = new HashMap<ClanInfo, CaptureData>();
		_hill = LOADED_HILLS.get(0);
		_nextLootDrop = System.currentTimeMillis() + getRandomRange(300000, 600000);
		
	}
	
	@Override
	protected void customStart()
	{
		setMap(new EventMap(_hill.getSchematic(), getCenterLocation()), new Runnable()
		{
			@Override
			public void run()
			{
				setState(EventState.LIVE);
			}
		});
	}
	
	@Override
	protected void customCancel()
	{
		System.out.println("Custom Cancel - King Hill");
	}
	
	@Override
	protected void customTick()
	{
		tickHill();
		
		if (getState() == EventState.PREPARE)
		{
			System.out.println("Constructed " + getName() + " at " + UtilWorld.locToStrClean(getCenterLocation()) + ".");
			announceStart();
			setState(EventState.LIVE);
		}
		
		if (System.currentTimeMillis() > _nextLootDrop)
		{
			// Drop Loot!
			_lootDropCount++;
			_nextLootDrop = System.currentTimeMillis() + getRandomRange(300000, 600000);
			
			if (_lootDropCount >= 4) setState(EventState.COMPLETE);
		}
	}
	
	private void tickHill()
	{
		int clanCount = 0;
		ClanInfo lastClan = null;
		
		for (Player player : UtilServer.getPlayers())
		{
			if (_hill.isOnHill(player.getLocation(), getCenterLocation()))
			{
				ClanInfo playerClan = _clansManager.getClan(player);
				if (playerClan != null && !playerClan.equals(lastClan))
				{
					clanCount++;
					lastClan = playerClan;
				}
				
				updateLastActive();
			}
		}
		
		if (clanCount == 1 && lastClan != null)
		{
			_clanOnHill = lastClan;
			
			CaptureData capData = _scoreMap.get(lastClan);
			if (capData == null)
			{
				capData = new CaptureData();
				_scoreMap.put(lastClan, capData);
			}
			capData.TicksOnHill++;
			
			if (System.currentTimeMillis() - _lastGoldDrop > 20000)
			{
				_goldDrops = 1;
				_lastGoldDrop = System.currentTimeMillis();
			}
			
			if (_goldDrops > 0 && _goldDrops < 200)
			{
				GoldManager.getInstance().dropGold(getCenterLocation().clone().add(0, 13, 0), GOLD_PER_TICK);
				_goldDrops++;
			}
			else
			{
				_goldDrops = 0;
			}
			
			if (System.currentTimeMillis() - _lastOnHillMessage > 60000)
			{
				Bukkit.broadcastMessage(F.main("Hill", F.elem(lastClan.getName()) + " own the hill (" + F.time(UtilTime.MakeStr(capData.TicksOnHill * 50)) + ")"));
				_lastOnHillMessage = System.currentTimeMillis();
			}
		}
		else
		{
			_clanOnHill = null;
		}
	}
	
	@Override
	public List<String> getLines(ScoreboardManager manager, Player player, List<String> out)
	{
		List<String> list = new ArrayList<String>(1);
		
		if (_clanOnHill != null && _scoreMap.containsKey(_clanOnHill))
		{
			list.add("  " + _clanOnHill.getName() + " are on the Hill");
		}
		
		return list;
	}
	
	private static class CaptureData
	{
		public int TicksOnHill;
	}

	@Override
	public void announceStart()
	{
		for(Player player : UtilServer.getPlayers()) {
			if(_clansManager.getTutorial().inTutorial(player)) continue;

			UtilTextMiddle.display(C.cGreen + getName(), UtilWorld.locToStrClean(getCenterLocation()), 10, 100, 40, player);
			player.sendMessage(F.main("Event", F.elem(getName()) + " has started at coordinates " + F.elem(UtilWorld.locToStrClean(getCenterLocation()))));
		}
	}
}
