package nautilus.game.arcade.game.games.tug;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import com.mineplex.anticheat.checks.move.Glide;
import com.mineplex.anticheat.checks.move.HeadRoll;

import mineplex.core.Managers;
import mineplex.core.antihack.AntiHack;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.events.PlayerKitGiveEvent;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.TeamGame;
import nautilus.game.arcade.game.games.tug.entities.TugChicken;
import nautilus.game.arcade.game.games.tug.entities.TugCow;
import nautilus.game.arcade.game.games.tug.entities.TugEntity;
import nautilus.game.arcade.game.games.tug.entities.TugPig;
import nautilus.game.arcade.game.games.tug.entities.TugSheep;
import nautilus.game.arcade.game.games.tug.kits.KitTugArcher;
import nautilus.game.arcade.game.games.tug.kits.KitTugLeaper;
import nautilus.game.arcade.game.games.tug.kits.KitTugSmasher;
import nautilus.game.arcade.kit.Kit;

public class TugOfWool extends TeamGame
{

	private static final String[] DESCRIPTION =
			{
					"Your animals are " + C.cRed + "Hungry" + C.Reset + ".",
					"Guide them to " + C.cYellow + "Enemy Crops" + C.Reset + ".",
					"Kill animals and players to get " + C.cGold + "Gold" + C.Reset + ".",
					"Eat " + C.cAqua + "All" + C.Reset + " enemy crops to win!"
			};
	private static final int MAX_ANIMALS = 30;
	private static final long INCREASE_TIME = TimeUnit.MINUTES.toMillis(2);
	private static final long GAME_TIMEOUT = TimeUnit.MINUTES.toMillis(8);
	private static final long PURCHASE_COOLDOWN = TimeUnit.SECONDS.toMillis(5);
	private static final int GOLD_SLOT = 8;
	private static final int MAX_GOLD = 50;
	private static final double GOLD_PER_ANIMAL = 0.2, GOLD_PER_PLAYER = 0.3, GOLD_PER_SEC = 0.05, GOLD_PER_CROP_LOST = 3, GOLD_LOST_DEATH = 2;

	private final Set<TugTeam> _teams;
	private final List<TugItem> _items;
	private final Map<Player, Double> _gold;

	private float _speed = 1;
	private long _lastIncrease;

	public TugOfWool(ArcadeManager manager)
	{
		super(manager, GameType.Tug, new Kit[]
				{
						new KitTugArcher(manager),
						new KitTugSmasher(manager),
						new KitTugLeaper(manager)
				}, DESCRIPTION);

		_teams = new HashSet<>();
		_items = new ArrayList<>();
		_gold = new HashMap<>();

		DamageFall = false;
		DeathOut = false;
		DeathSpectateSecs = 10;
		HungerSet = 20;

		AntiHack antiHack = Managers.get(AntiHack.class);
		antiHack.addIgnoredCheck(Glide.class);
		antiHack.addIgnoredCheck(HeadRoll.class);
		registerChatStats(
				Kills,
				Deaths,
				BlankLine,
				DamageDealt,
				DamageTaken
		);
	}

	@Override
	public void ParseData()
	{
		TugTeam red = new TugTeam(GetTeam(ChatColor.RED), setupCrops("RED"));
		TugTeam blue = new TugTeam(GetTeam(ChatColor.AQUA), setupCrops("BLUE"));

		red.setEnemy(blue);
		blue.setEnemy(red);

		_teams.add(red);
		_teams.add(blue);

		_items.add(new TugItem(EntityType.CHICKEN, 3));
		_items.add(new TugItem(EntityType.PIG, 5));
		_items.add(new TugItem(EntityType.COW, 10));
	}

	private List<Location> setupCrops(String key)
	{
		List<Location> crops = WorldData.GetDataLocs(key);

		crops.forEach(location ->
		{
			Block block = location.getBlock();

			block.getRelative(BlockFace.DOWN).setType(Material.SOIL);
			block.setType(Material.CROPS);
			block.setData((byte) 7);
		});

		return crops;
	}

	@EventHandler
	public void live(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Prepare)
		{
			return;
		}

		_lastIncrease = System.currentTimeMillis();
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (!IsLive())
		{
			return;
		}

		switch (event.getType())
		{
			case TICK:
				updateTargets();
				break;
			case SEC:
				updateSpawns();
				break;
		}
	}

	private void updateSpawns()
	{
		CreatureAllowOverride = true;

		_teams.forEach(team ->
		{
			if (team.getEntities().size() < MAX_ANIMALS)
			{
				team.getEntities().add(spawnTugEntity(EntityType.SHEEP, team));
			}
		});

		CreatureAllowOverride = false;

		if (UtilTime.elapsed(_lastIncrease, INCREASE_TIME))
		{
			Announce(F.main(getArcadeManager().getName(), "Your animals grow hungrier. Their " + F.elem("Speed") + " have increased!"));
			_speed *= 1.1;
			DeathSpectateSecs++;
			_lastIncrease = System.currentTimeMillis();
		}
	}

	private void spawnFor(EntityType entityType, Player player)
	{
		TugTeam team = getTugTeam(player);

		if (team == null)
		{
			return;
		}

		CreatureAllowOverride = true;
		TugEntity entity = spawnTugEntity(entityType, team);
		CreatureAllowOverride = false;

		if (entity != null)
		{
			player.playSound(player.getLocation(), Sound.NOTE_PLING, 1, 1);
			player.sendMessage(F.main(Manager.getName(), "You spawned a " + F.name(UtilEnt.getName(entityType)) + "."));
			team.getEntities().add(entity);
		}
	}

	private TugEntity spawnTugEntity(EntityType entityType, TugTeam team)
	{
		switch (entityType)
		{
			case SHEEP:
				return new TugSheep(this, team, getSpawn(team));
			case COW:
				return new TugCow(this, team, getSpawn(team));
			case PIG:
				return new TugPig(this, team, getSpawn(team));
			case CHICKEN:
				return new TugChicken(this, team, getSpawn(team));
			default:
				return null;
		}
	}

	private Location getSpawn(TugTeam team)
	{
		Location location = UtilAlg.Random(team.getSpawns());

		if (location == null)
		{
			location = GetSpectatorLocation();
		}

		location.setYaw(UtilAlg.GetYaw(UtilAlg.getTrajectory(location, GetSpectatorLocation())));

		return location;
	}

	private void updateTargets()
	{
		_teams.forEach(team ->
		{
			List<Location> targetCrops = team.getEnemy().getCrops();
			List<TugEntity> targetEntities = team.getEnemy().getEntities();

			team.getEntities().forEach(tugEntity ->
			{
				tugEntity.updateName();

				LivingEntity targetEntity = tugEntity.getTargetEntity();

				// Has a target entity
				if (targetEntity != null)
				{
					if (!targetEntity.isValid())
					{
						tugEntity.setTargetEntity(null);
					}

					// Attempt to attack it, if unsuccessful move closer
					if (!tugEntity.attemptAttack(targetEntity))
					{
						tugEntity.move();
					}

					return;
				}

				Location targetLocation = tugEntity.getTargetLocation();

				for (TugEntity otherTugEntity : targetEntities)
				{
					// Attempt to target entity. If successful no need to handle anything else
					if (tugEntity.attemptTarget(otherTugEntity.getEntity()))
					{
						return;
					}
				}

				// No target crop or crop no longer exists
				if (targetLocation == null || !targetCrops.contains(targetLocation))
				{
					Location closest = UtilAlg.findClosest(tugEntity.getEntity().getLocation(), targetCrops);

					if (closest == null)
					{
						return;
					}

					tugEntity.setTargetLocation(closest);
				}
				else if (tugEntity.attemptEat())
				{
					for (Player player : team.getEnemy().getGameTeam().GetPlayers(true))
					{
						incrementGold(player, GOLD_PER_CROP_LOST);
					}

					targetCrops.remove(targetLocation);
					return;
				}

				// Finally move the entity closer to it's target
				tugEntity.move();
			});
		});

		_teams.forEach(team -> team.getEntities().removeIf(tugEntity -> !tugEntity.getEntity().isValid()));
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void damage(CustomDamageEvent event)
	{
		if (!IsLive())
		{
			return;
		}

		event.SetDamageToLevel(false);

		if (event.IsCancelled())
		{
			return;
		}

		LivingEntity damagee = event.GetDamageeEntity();
		Player damager = event.GetDamagerPlayer(true);

		if (damager == null)
		{
			return;
		}

		TugTeam team = getTugTeam(damager);

		if (team == null)
		{
			return;
		}

		if (team.isEntity(damagee))
		{
			event.SetCancelled("Own Tug Entity");
		}
		else
		{
			if (damagee instanceof Player)
			{
				incrementGold(damager, GOLD_PER_PLAYER);
			}
			else
			{
				event.SetKnockback(false);
				incrementGold(damager, GOLD_PER_ANIMAL);
			}
		}
	}

	@EventHandler
	public void entityDeath(EntityDeathEvent event)
	{
		if (!IsLive())
		{
			return;
		}

		event.getDrops().clear();
		_teams.forEach(team -> team.getEntities().removeIf(other -> other.getEntity().equals(event.getEntity())));
	}

	@EventHandler(ignoreCancelled = true)
	public void entityTarget(EntityTargetEvent event)
	{
		if (!IsLive())
		{
			return;
		}

		event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void cropTrample(EntityChangeBlockEvent event)
	{
		if (!IsLive())
		{
			return;
		}

		event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void cropTrample(EntityInteractEvent event)
	{
		if (!IsLive() || event.getBlock() == null || event.getBlock().getType() != Material.SOIL)
		{
			return;
		}

		event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void cropTrample(PlayerInteractEvent event)
	{
		if (!IsLive() || event.getClickedBlock() == null || event.getClickedBlock().getRelative(BlockFace.DOWN).getType() == Material.SOIL)
		{
			return;
		}

		event.setCancelled(true);
	}

	@EventHandler
	public void playerKitGive(PlayerKitGiveEvent event)
	{
		Player player = event.getPlayer();

		int slot = 3;

		for (TugItem item : _items)
		{
			player.getInventory().setItem(slot++, item.getItemStack());
		}

		setGold(player, _gold.getOrDefault(player, 0D) / GOLD_LOST_DEATH);
	}

	@EventHandler
	public void itemInteract(PlayerInteractEvent event)
	{
		if (!IsLive() || event.getAction() == Action.PHYSICAL)
		{
			return;
		}

		Player player = event.getPlayer();
		ItemStack itemStack = player.getItemInHand();

		if (itemStack == null)
		{
			return;
		}

		for (TugItem item : _items)
		{
			if (item.getItemStack().equals(itemStack))
			{
				event.setCancelled(true);

				int gold = getGold(player);

				if (gold < item.getCost())
				{
					player.sendMessage(F.main(Manager.getName(), "You don't have enough " + C.cGold + "Gold" + C.mBody + " to buy a " + F.name(UtilEnt.getName(item.getEntityType())) + ". You need " + F.count(item.getCost() - gold) + " more."));
					return;
				}
				else if (!Recharge.Instance.use(player, "Buy Animal", PURCHASE_COOLDOWN, true, true))
				{
					return;
				}

				incrementGold(player, -item.getCost());
				spawnFor(item.getEntityType(), player);
				return;
			}
		}
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		_gold.remove(event.getPlayer());
	}

	@EventHandler
	public void updatePassiveGold(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC || !IsLive())
		{
			return;
		}

		for (Player player : GetPlayers(true))
		{
			if (UtilPlayer.isSpectator(player))
			{
				continue;
			}

			incrementGold(player, GOLD_PER_SEC);
		}
	}

	@Override
	@EventHandler
	public void ScoreboardUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST || !InProgress())
		{
			return;
		}

		Scoreboard.writeNewLine();

		_teams.forEach(team ->
		{
			Scoreboard.write(team.getGameTeam().GetFormattedName());
			Scoreboard.write(team.getCrops().size() + " Wheat Left");
			Scoreboard.writeNewLine();
		});

		if (IsLive())
		{
			Scoreboard.write(C.cYellowB + "Time Left");
			Scoreboard.write(UtilTime.MakeStr(Math.max(0, GetStateTime() + GAME_TIMEOUT - System.currentTimeMillis())));
			Scoreboard.writeNewLine();
		}

		Scoreboard.draw();
	}

	@Override
	public void EndCheck()
	{
		if (!IsLive())
		{
			return;
		}

		GameTeam winner = null;

		if (UtilTime.elapsed(GetStateTime(), GAME_TIMEOUT))
		{
			int mostCrops = 0;

			for (TugTeam team : _teams)
			{
				int crops = team.getCrops().size();

				if (crops > mostCrops)
				{
					winner = team.getGameTeam();
					mostCrops = crops;
				}
			}
		}
		else
		{
			for (TugTeam team : _teams)
			{
				if (team.getCrops().isEmpty())
				{
					winner = team.getEnemy().getGameTeam();
					break;
				}
			}
		}

		if (winner != null)
		{
			for (Player player : GetPlayers(false))
			{
				if (!player.isOnline())
				{
					continue;
				}

				AddGems(player, 10, "Participation", false, false);

				if (winner.HasPlayer(player))
				{
					AddGems(player, 20, "Winning Team", false, false);
				}
			}

			AnnounceEnd(winner);
			SetState(GameState.End);
		}
	}

	public TugTeam getTugTeam(Player player)
	{
		for (TugTeam other : _teams)
		{
			if (other.getGameTeam().HasPlayer(player))
			{
				return other;
			}
		}

		return null;
	}

	public float getSpeed()
	{
		return _speed;
	}

	public void incrementGold(Player player, double amount)
	{
		setGold(player, _gold.getOrDefault(player, 0D) + amount);

		if (amount > 0)
		{
			AddGems(player, amount / 10D, "Gold Collected", true, true);
		}
	}

	public void setGold(Player player, double amount)
	{
		if (amount >= MAX_GOLD)
		{
			amount = MAX_GOLD;
		}

		int amountFloor = (int) Math.floor(amount);

		player.setLevel(amountFloor);
		player.setExp((float) (amount % 1));

		if (amountFloor == 0)
		{
			player.getInventory().setItem(GOLD_SLOT, null);
		}
		else
		{
			player.getInventory().setItem(GOLD_SLOT, new ItemBuilder(Material.GOLD_NUGGET, Math.max(1, amountFloor))
					.setTitle(C.cGold + amountFloor + " Gold")
					.addLore("Collect gold by killing animals", "and players! You can then spend", "it on more animals!")
					.build());
		}

		_gold.put(player, amount);
	}

	public int getGold(Player player)
	{
		return (int) Math.floor(_gold.getOrDefault(player, 0D));
	}
}
