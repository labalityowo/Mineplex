package nautilus.game.arcade.game.games.lobbers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.explosion.ExplosionEvent;
import mineplex.core.projectile.IThrown;
import mineplex.core.projectile.ProjectileUser;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.combat.CombatComponent;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.TeamGame;
import nautilus.game.arcade.game.games.lobbers.events.TNTPreExplodeEvent;
import nautilus.game.arcade.game.games.lobbers.events.TNTThrowEvent;
import nautilus.game.arcade.game.games.lobbers.kits.KitArmorer;
import nautilus.game.arcade.game.games.lobbers.kits.KitJumper;
import nautilus.game.arcade.game.games.lobbers.kits.KitPitcher;
import nautilus.game.arcade.game.games.lobbers.kits.KitWaller;
import nautilus.game.arcade.game.games.lobbers.trackers.Tracker6Kill;
import nautilus.game.arcade.game.games.lobbers.trackers.TrackerBlastProof;
import nautilus.game.arcade.game.games.lobbers.trackers.TrackerDirectHit;
import nautilus.game.arcade.game.games.lobbers.trackers.TrackerNoDamage;
import nautilus.game.arcade.game.games.lobbers.trackers.TrackerTNTThrown;
import nautilus.game.arcade.game.modules.TeamArmorModule;
import nautilus.game.arcade.game.modules.compass.CompassModule;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.managers.chat.ChatStatData;

public class BombLobbers extends TeamGame implements IThrown
{

	private static final String[] DESCRIPTION =
			{
					"Fight against your enemies using",
					"the power of " + C.cRed + "Explosives!",
					C.cGreen + "Left-Click" + C.Reset + " TNT to throw at your enemy.",
					C.cYellow + "Last Team" + C.Reset + " alive wins!"
			};

	private final Map<GameTeam, Location> _averageSpawns = new HashMap<>();
	private final Map<TNTPrimed, BombToken> _tnt = new HashMap<>();
	private final Map<Player, Double> _kills = new HashMap<>();

	@SuppressWarnings("unchecked")
	public BombLobbers(ArcadeManager manager)
	{
		super(manager, GameType.Lobbers, new Kit[]
				{
						new KitJumper(manager),
						new KitArmorer(manager),
						new KitPitcher(manager),
						new KitWaller(manager)
				}, DESCRIPTION);

		DamageFall = true;
		DamageEvP = true;

		WorldWaterDamage = 5;

		PrepareFreeze = false;

		InventoryOpenChest = false;
		InventoryOpenBlock = false;

		ItemDrop = false;

		BlockPlace = false;

		Manager.GetExplosion().SetLiquidDamage(false);

		HungerSet = 20;

		WorldTimeSet = 6000;

		registerStatTrackers(
				new Tracker6Kill(this),
				new TrackerBlastProof(this),
				new TrackerNoDamage(this),
				new TrackerTNTThrown(this),
				new TrackerDirectHit(this)
		);

		registerChatStats(
				Kills,
				Assists,
				DamageTaken,
				BlankLine,
				new ChatStatData("Thrown", "Bombs Lobbed", true)
		);

		new CompassModule()
				.setGiveCompass(true)
				.setGiveCompassToSpecs(true)
				.setGiveCompassToAlive(false)
				.register(this);

		new TeamArmorModule()
				.giveTeamArmor()
				.giveHotbarItem()
				.register(this);
	}

	@EventHandler
	public void setTime(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Prepare)
		{
			return;
		}

		if (WorldData.MapName.equalsIgnoreCase("Intergalactic"))
		{
			WorldTimeSet = 18000;
		}
	}

	public void addKill(Player player)
	{
		_kills.put(player, _kills.getOrDefault(player, 0D) + 1);
	}

	public double getKills(Player player)
	{
		return _kills.getOrDefault(player, 0D);
	}

	@EventHandler
	public void onKill(CombatDeathEvent event)
	{
		if (!IsLive())
			return;

		Player dead = UtilPlayer.searchExact(event.GetEvent().getEntity().getName());

		if (!IsAlive(dead))
			return;

		for (CombatComponent damager : event.GetLog().GetAttackers())
		{
			Player killer = UtilPlayer.searchExact(damager.GetName());

			if (killer == null || !killer.isOnline())
				continue;

			if (IsAlive(killer))
			{
				if (event.GetLog().GetKiller() == damager)
				{
					addKill(killer);
				}
			}
		}
	}

	@EventHandler
	public void loadTeamLocations(GameStateChangeEvent event)
	{
		if (event.GetState() == GameState.Live)
		{
			for (GameTeam team : _teamList)
			{
				_averageSpawns.put(team, UtilAlg.getAverageLocation(team.GetSpawns()));
			}
		}
	}

	@EventHandler
	public void disableFlying(GameStateChangeEvent event)
	{
		for (Player player : GetPlayers(true))
		{
			player.setAllowFlight(false);
		}
	}

	@EventHandler
	public void throwTNT(PlayerInteractEvent event)
	{
		if (event.getAction() == Action.PHYSICAL || !IsLive())
		{
			return;
		}


		Player player = event.getPlayer();
		ItemStack itemStack = player.getItemInHand();

		if (!IsAlive(player) || itemStack == null || itemStack.getType() != Material.TNT)
		{
			return;
		}

		event.setCancelled(true);

		UtilInv.remove(player, Material.TNT, (byte) 0, 1);
		player.updateInventory();

		TNTPrimed tnt = player.getWorld().spawn(player.getEyeLocation().add(player.getLocation().getDirection()), TNTPrimed.class);
		tnt.setMetadata("owner", new FixedMetadataValue(Manager.getPlugin(), player.getUniqueId()));
		tnt.setFuseTicks(60);

		UtilAction.velocity(tnt, player.getLocation().getDirection(), 2.0D, false, 0.0D, 0.1D, 10.0D, false);

		List<Player> canHit = new ArrayList<>();
		for (Player pos : GetPlayers(true))
		{
			if (GetTeam(player).HasPlayer(pos))
			{
				continue;
			}

			canHit.add(pos);
		}

		Manager.GetProjectile().AddThrow(tnt, player, this, -1L, true, true, false, true, .2F, canHit);

		Manager.getPlugin().getServer().getPluginManager().callEvent(new TNTThrowEvent(player, tnt));

		_tnt.put(tnt, new BombToken(player));

		AddGems(player, 0.5, "TNT Thrown", true, true);
	}

	public Player getThrower(TNTPrimed tnt)
	{
		BombToken token = _tnt.get(tnt);
		return token == null ? null : UtilPlayer.searchExact(_tnt.get(tnt).Thrower);
	}

	@EventHandler
	public void onTNTExplode(ExplosionPrimeEvent event)
	{
		if (!IsLive() || !(event.getEntity() instanceof TNTPrimed))
		{
			return;
		}

		TNTPrimed tnt = (TNTPrimed) event.getEntity();

		if (!_tnt.containsKey(tnt))
		{
			return;
		}

		Player thrower = UtilPlayer.searchExact(_tnt.get(tnt).Thrower);

		if (thrower == null || GetTeam(thrower).equals(getSide(tnt.getLocation())))
		{
			event.setCancelled(true);
			tnt.remove();
			_tnt.remove(tnt);
			return;
		}

		TNTPreExplodeEvent preExplode = new TNTPreExplodeEvent(thrower, tnt);
		UtilServer.CallEvent(preExplode);

		if (preExplode.isCancelled())
		{
			event.setCancelled(true);
			tnt.remove();
		}
		else
		{
			for (Player other : UtilPlayer.getNearby(event.getEntity().getLocation(), 14))
			{
				Manager.GetCondition().Factory().Explosion("Throwing TNT", other, thrower, 50, 0.1, false, false);
			}
		}

		_tnt.remove(tnt);
	}

	@EventHandler
	public void updateTNT(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK || !IsLive())
		{
			return;
		}

		Map<TNTPrimed, BombToken> toAdd = new HashMap<>();

		Iterator<Entry<TNTPrimed, BombToken>> iterator = _tnt.entrySet().iterator();
		while (iterator.hasNext())
		{
			Entry<TNTPrimed, BombToken> tnt = iterator.next();

			if (tnt.getKey() == null || !tnt.getKey().isValid())
			{
				continue;
			}

			BombToken token = tnt.getValue();
			Player thrower = UtilPlayer.searchExact(token.Thrower);

			if (thrower == null || !IsPlaying(thrower))
			{
				continue;
			}

			if (!token.Primed)
			{
				if (tnt.getKey().getFuseTicks() <= 20)
				{
					//Respawn
					TNTPrimed newTNT = tnt.getKey().getWorld().spawn(tnt.getKey().getLocation(), TNTPrimed.class);
					newTNT.setMetadata("owner", new FixedMetadataValue(Manager.getPlugin(), thrower.getUniqueId()));
					newTNT.setVelocity(tnt.getKey().getVelocity());
					newTNT.setFuseTicks(60);

					Manager.GetProjectile().AddThrow(newTNT, thrower, this, -1L, true, true, false, true, .2F);

					tnt.getKey().remove();

					iterator.remove();
					toAdd.put(newTNT, token);
				}
			}
		}

		//Prevent concurrent modification thigns
		for (Entry<TNTPrimed, BombToken> entry : toAdd.entrySet())
		{
			_tnt.put(entry.getKey(), entry.getValue());
		}
	}

	@EventHandler
	public void blockCollision(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK || !IsLive())
		{
			return;
		}

		for (Entry<TNTPrimed, BombToken> tnt : _tnt.entrySet())
		{
			if (tnt.getKey() == null || !tnt.getKey().isValid())
			{
				continue;
			}

			BombToken token = tnt.getValue();
			Player thrower = UtilPlayer.searchExact(token.Thrower);

			if (thrower == null || !IsPlaying(thrower))
			{
				continue;
			}

			if (!token.Primed)
			{
				//8+ insta explode
				if (UtilTime.elapsed(token.Created, 8000))
				{
					token.Primed = true;
					tnt.getKey().setFuseTicks(0);
				}
				else if (UtilTime.elapsed(token.Created, 3000))
				{
					for (Block block : UtilBlock.getSurrounding(tnt.getKey().getLocation().getBlock(), true))
					{
						if (block.getType() != Material.AIR)
						{
							token.Primed = true;
							tnt.getKey().setFuseTicks(0);
							break;
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void updateParticles(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK || !IsLive())
		{
			return;
		}

		for (Entry<TNTPrimed, BombToken> tnt : _tnt.entrySet())
		{
			if (tnt.getKey() == null || !tnt.getKey().isValid()  || UtilEnt.isGrounded(tnt.getKey()) || tnt.getKey().isOnGround())
			{
				continue;
			}

			BombToken token = tnt.getValue();
			Player thrower = UtilPlayer.searchExact(token.Thrower);

			if (thrower == null)
			{
				continue;
			}

			GameTeam team = GetTeam(thrower);

			if (team == null)
			{
				continue;
			}

			//A is current
			//B is previous
			token.B = token.A;
			token.A = tnt.getKey().getLocation();

			if (token.A == null || token.B == null)
				continue;

			//Adapted from static lazer code
			double curRange = 0;
			double distance = Math.abs(token.A.distance(token.B));

			while (curRange <= distance)
			{
				Location newTarget = token.B.add(UtilAlg.getTrajectory(token.B, token.A).multiply(curRange));

				//Progress Forwards
				curRange += 0.2;

				if (team.GetColor() == ChatColor.AQUA)
				{
					for (int i = 0; i < 2; i++)
						UtilParticle.PlayParticle(ParticleType.RED_DUST, newTarget.clone().add(0.0, 0.5, 0.0), -1, 1, 1, 1, 0,
								ViewDist.NORMAL, UtilServer.getPlayers());
				}
				else
				{
					for (int i = 0; i < 2; i++)
						UtilParticle.PlayParticle(ParticleType.RED_DUST, newTarget.clone().add(0.0, 0.5, 0.0), 0, 0, 0, 0, 1,
								ViewDist.NORMAL, UtilServer.getPlayers());
				}
			}
		}
	}

	@EventHandler
	public void preventCheating(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC || !IsLive())
		{
			return;
		}

		for (Player player : GetPlayers(true))
		{
			if (!GetTeam(player).equals(getSide(player.getLocation())))
			{
				Manager.GetDamage().NewDamageEvent(player, null, null, DamageCause.CUSTOM, 500, false, true, true, GetName(), "Cheating");
			}
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void damageBlocks(ExplosionEvent event)
	{
		if (!IsLive())
		{
			return;
		}

		Iterator<Block> iterator = event.GetBlocks().iterator();

		while (iterator.hasNext())
		{
			Block block = iterator.next();

			//Stone
			if (block.getType() == Material.STONE)
			{
				block.setType(Material.COBBLESTONE);
				iterator.remove();
				continue;
			}

			//Stone Brick
			if (block.getType() == Material.SMOOTH_BRICK && block.getData() != 2)
			{
				block.setData((byte) 2);
				iterator.remove();
			}
		}
	}

	private GameTeam getSide(Location entityLoc)
	{
		Location nearest = UtilAlg.findClosest(entityLoc, _averageSpawns.values());

		for (Entry<GameTeam, Location> entry : _averageSpawns.entrySet())
		{
			if (entry.getValue().equals(nearest))
			{
				return entry.getKey();
			}
		}
		return null;
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data)
	{
		if (!(data.getThrown() instanceof TNTPrimed))
			return;

		if (!(data.getThrower() instanceof Player))
			return;

		if (!(target instanceof Player))
			return;

		if (GetTeam((Player) target) == GetTeam((Player) data.getThrower()))
			return;

		UtilAction.velocity(target, UtilAlg.getTrajectory2d(data.getThrown().getLocation(), target.getLocation()), .2, false, 0, 0.2, .4, true);
		Manager.GetDamage().NewDamageEvent(target, data.getThrower(), null, DamageCause.PROJECTILE, 10.0, false, false, false, "Throwing TNT", "Throwing TNT Direct Hit");

		AddGems((Player) data.getThrower(), 4.0, "Direct Hit", true, true);
	}

	@Override
	public void Idle(ProjectileUser data)
	{

	}

	@Override
	public void Expire(ProjectileUser data)
	{

	}

	@Override
	public void ChunkUnload(ProjectileUser data)
	{
		data.getThrown().remove();
	}
}