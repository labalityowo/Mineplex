package nautilus.game.arcade.game.games.zombiesurvival;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.minecraft.server.v1_8_R3.EntityCreature;
import net.minecraft.server.v1_8_R3.NavigationAbstract;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftCreature;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import mineplex.core.Managers;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.visibility.VisibilityManager;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.GameTeam.PlayerState;
import nautilus.game.arcade.game.SoloGame;
import nautilus.game.arcade.game.games.zombiesurvival.kits.KitSurvivorArcher;
import nautilus.game.arcade.game.games.zombiesurvival.kits.KitSurvivorKnight;
import nautilus.game.arcade.game.games.zombiesurvival.kits.KitSurvivorRogue;
import nautilus.game.arcade.game.games.zombiesurvival.kits.KitUndeadAlpha;
import nautilus.game.arcade.game.games.zombiesurvival.kits.KitUndeadZombie;
import nautilus.game.arcade.game.modules.compass.CompassModule;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.NullKit;

public class ZombieSurvival extends SoloGame
{
	private GameTeam _survivors;
	private GameTeam _undead;

	private HashMap<Creature, ZombieData> _mobs = new HashMap<Creature, ZombieData>();
	
	public ZombieSurvival(ArcadeManager manager) 
	{
		super(manager, GameType.ZombieSurvival,

				new Kit[]
						{
				new KitSurvivorKnight(manager),
				new KitSurvivorRogue(manager),
				new KitSurvivorArcher(manager),
				new NullKit(manager),
				new KitUndeadAlpha(manager),
				new KitUndeadZombie(manager),
						},

						new String[]
								{
				"The Undead are attacking!",
				"Run, fight or hide to survive!",
				"When you die, you become Undead",
				"The last Survivor alive wins!"
								});
		
		this.DeathOut = false;
		this.HungerSet = 20;

		new CompassModule()
				.setGiveCompassToAlive(true)
				.register(this);

		registerChatStats(
				Kills,
				Assists
		);
	}
	
	@Override
	public void RestrictKits()
	{
		for (Kit kit : GetKits())
		{
			for (GameTeam team : GetTeamList())
			{
				if (team.GetColor() == ChatColor.RED)
				{
					if (kit.GetName().contains("Survivor"))
						team.GetRestrictedKits().add(kit);
				}
				else
				{
					if (kit.GetName().contains("Undead"))
						team.GetRestrictedKits().add(kit);
				}
			}
		}
	}

	@Override
	@EventHandler
	public void CustomTeamGeneration(GameStateChangeEvent event) 
	{
		if (event.GetState() != GameState.Recruit)
			return;

		_survivors = this.GetTeamList().get(0);
		_survivors.SetName("Survivors");

		//Undead Team
		_undead = new GameTeam(this, "Undead", ChatColor.RED, WorldData.GetDataLocs("RED"));
		_undead.SetVisible(false);
		AddTeam(_undead);
		
		RestrictKits();
	}

	@EventHandler
	public void UpdateChasers(UpdateEvent event)
	{
		if (!IsLive())
			return;

		if (event.getType() != UpdateType.FAST)
			return;

		int req = 1 + _survivors.GetPlayers(true).size()/20;
			
		while (_undead.GetPlayers(true).size() < req && _survivors.GetPlayers(true).size() > 0)
		{
			Player player = _survivors.GetPlayers(true).get(UtilMath.r(_survivors.GetPlayers(true).size()));
			SetChaser(player, true);
		}
	}

	@EventHandler
	public void PlayerDeath(PlayerDeathEvent event) 
	{
		if (_survivors.HasPlayer(event.getEntity()))
			SetChaser(event.getEntity(), false);
	}

	public void SetChaser(Player player, boolean forced)
	{
		//Set them as OUT!
		if (GetTeam(player) != null)
			GetTeam(player).SetPlacement(player, PlayerState.OUT);

		//Change to Undead
		SetPlayerTeam(player, _undead, true);

		//Kit
		Kit newKit = this.GetKits()[4];
		if (forced)
			newKit = this.GetKits()[5];
		SetKit(player, newKit, true);
		newKit.ApplyKit(player);

		//Refresh
		VisibilityManager vm = Managers.require(VisibilityManager.class);
		Bukkit.getOnlinePlayers().forEach(pl -> vm.refreshVisibility(pl, player));

		if (forced)
		{
			player.eject();
			player.teleport(_undead.GetSpawn());

			AddGems(player, 10, "Forced Undead", false, false);

			Announce(F.main("Game", F.elem(_survivors.GetColor() + player.getName()) + " has become an " + 
					F.elem(_undead.GetColor() + "Alpha Zombie") + "."));

			player.getWorld().strikeLightningEffect(player.getLocation());
		}
		
		UtilPlayer.message(player, C.cRed + C.Bold + "You have been Zombified! Braaaaaiiiinnnssss!");
	}

	@Override
	public void RespawnPlayer(final Player player)
	{
		Manager.Clear(player);

		if (_undead.HasPlayer(player))
		{
			player.eject();
			player.teleport(_undead.GetSpawn());
		}

		//Re-Give Kit
		Manager.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(Manager.getPlugin(), new Runnable()
		{
			public void run()
			{
				GetKit(player).ApplyKit(player);

				//Refresh on Spawn
				VisibilityManager vm = Managers.require(VisibilityManager.class);
				Bukkit.getOnlinePlayers().forEach(pl -> vm.refreshVisibility(pl, player));
			} 
		}, 0);
	}

	@EventHandler
	public void UndeadUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		if (!InProgress())
			return;

		Iterator<Creature> mobIterator = _mobs.keySet().iterator();

		while (mobIterator.hasNext())
		{	
			Creature mob = mobIterator.next();

			if (!mob.isValid())
			{
				mob.remove();
				mobIterator.remove();
			}
		}

		if (_mobs.size() < 50)
		{
			this.CreatureAllowOverride = true;
			Zombie zombie = _undead.GetSpawn().getWorld().spawn(_undead.GetSpawn(), Zombie.class);
			_mobs.put(zombie, new ZombieData(GetTargetLocation()));
			this.CreatureAllowOverride = false;
		}

		mobIterator = _mobs.keySet().iterator();
		while (mobIterator.hasNext())
		{	
			Creature mob = mobIterator.next();
			Manager.GetCondition().Factory().Speed("Zombie Speed", mob, mob, 1.9, 1, false, false, true);

			ZombieData data = _mobs.get(mob);

			//New Target via Distance
			if (UtilMath.offset(mob.getLocation(), data.Target) < 10 || 
				UtilMath.offset2d(mob.getLocation(), data.Target) < 6 ||
				UtilTime.elapsed(data.Time, 30000))
			{
				data.SetTarget(GetTargetLocation());
				continue;
			}

			//Untarget
			if (mob.getTarget() != null)
			{
				if (UtilMath.offset2d(mob, mob.getTarget()) > 10)
				{
					mob.setTarget(null);
				}
				else
				{
					if (mob.getTarget() instanceof Player)
						if (_undead.HasPlayer((Player)mob.getTarget()))
							mob.setTarget(null);
				}
			}
			//Move
			else
			{
				//Move
				EntityCreature ec = ((CraftCreature)mob).getHandle();
				NavigationAbstract nav = ec.getNavigation();
				
				if (UtilMath.offset(mob.getLocation(), data.Target) > 20)
				{
					Location target = mob.getLocation();
					
					target.add(UtilAlg.getTrajectory(mob.getLocation(), data.Target).multiply(20));
					
					nav.a(target.getX(), target.getY(), target.getZ(), 1.2f);
				}
				else
				{
					nav.a(data.Target.getX(), data.Target.getY(), data.Target.getZ(), 1.2f);
				}
				
			}
		}
	}
	
	public Location GetTargetLocation()
	{
		if (_survivors.GetPlayers(true).size() == 0)
		{
			return _survivors.GetSpawn();
		}
		else
		{
			return _survivors.GetPlayers(true).get(UtilMath.r(_survivors.GetPlayers(true).size())).getLocation();
		}
	}

	@EventHandler
	public void UndeadTarget(EntityTargetEvent event)
	{
		if (event.getTarget() instanceof Player)
			if (_undead.HasPlayer((Player)event.getTarget()))
				event.setCancelled(true);
	}

	@EventHandler
	public void UndeadCombust(EntityCombustEvent event)
	{
		event.setCancelled(true);
	}
	
	@Override
	public void EndCheck()
	{
		if (!IsLive())
			return;

		if (_survivors.GetPlayers(true).size() <= 1)
		{
			List<Player> places = _survivors.GetPlacements(true);
			
			if (places.size() >= 1)
				AddGems(places.get(0), 15, "1st Place", false, false);

			if (places.size() >= 2)
				AddGems(places.get(1), 10, "2nd Place", false, false);

			if (places.size() >= 3)
				AddGems(places.get(2), 5, "3rd Place", false, false);


			for (Player player : GetPlayers(false))
				if (player.isOnline())
					AddGems(player, 10, "Participation", false, false);

			AnnounceEnd(places);
			
			SetState(GameState.End);
		}
	}

	@Override
	@EventHandler
	public void ScoreboardUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		if (_survivors == null || _undead == null)
			return;
		
		Scoreboard.reset();
		
		for (GameTeam team : this.GetTeamList())
		{			
			Scoreboard.writeNewLine();
			Scoreboard.write(team.GetPlayers(true).size() + " " + team.GetColor() + team.GetName());
		}

		Scoreboard.draw();
	}
}
