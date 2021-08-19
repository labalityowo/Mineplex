package mineplex.game.nano.game.games.kingslime;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.nano.NanoManager;
import mineplex.game.nano.game.GameType;
import mineplex.game.nano.game.ScoredSoloGame;
import mineplex.game.nano.game.components.player.DoubleJumpComponent;
import mineplex.game.nano.game.event.GameStateChangeEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class KingSlime extends ScoredSoloGame
{

	private static final int MAX_SLIMES = 40;
	private static final int MAX_BALLS = 10;
	private static final String SLIME_NAME = "Big Brian";

	private Slime _king;
	private Location _kingSpawn;

	private final Set<Slime> _tinySlimes;
	private List<Location> _tinySlimeSpawns;

	public KingSlime(NanoManager manager)
	{
		super(manager, GameType.KING_SLIME, new String[]
				{
						C.cGreen + SLIME_NAME + C.Reset + " is hungry!",
						"Kill the " + C.cYellow + "Tiny Slimes" + C.Reset + " to get " + C.cGreen + "Slime Balls" + C.Reset + ".",
						"Give the " + C.cGreen + "Slime Balls" + C.Reset + " to " + C.cGreen + SLIME_NAME + C.Reset + ".",
						"The player with the most " + C.cYellow + "Balls Fed" + C.Reset + " wins."
				});

		_tinySlimes = new HashSet<>();

		_prepareComponent.setPrepareFreeze(false);

		_damageComponent
				.setPvp(false)
				.setFall(false);

		_spectatorComponent.setDeathOut(false);

		_endComponent.setTimeout(TimeUnit.SECONDS.toMillis(80));

		new DoubleJumpComponent(this).setDirectional(true);
	}

	@Override
	protected void parseData()
	{
		_kingSpawn = _mineplexWorld.getIronLocation("LIME");
		_tinySlimeSpawns = _mineplexWorld.getIronLocations("GREEN");
	}

	@Override
	public void disable()
	{
		super.disable();

		_tinySlimes.clear();
		_tinySlimeSpawns.clear();
	}

	@EventHandler
	public void prepare(GameStateChangeEvent event)
	{
		if (event.getState() != GameState.Prepare)
		{
			return;
		}

		_worldComponent.setCreatureAllowOverride(true);

		_king = _kingSpawn.getWorld().spawn(_kingSpawn, Slime.class);

		_king.setSize(2);
		_king.setCustomName(C.cGreenB + SLIME_NAME);
		_king.setCustomNameVisible(true);

		UtilEnt.vegetate(_king, true);
		UtilEnt.ghost(_king, true, false);
		UtilEnt.setFakeHead(_king, true);

		_worldComponent.setCreatureAllowOverride(false);
	}

	@EventHandler
	public void updateSlimes(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST || !isLive() || _tinySlimeSpawns.isEmpty())
		{
			return;
		}

		_worldComponent.setCreatureAllowOverride(true);

		while (_tinySlimes.size() < MAX_SLIMES)
		{
			Location location = UtilAlg.Random(_tinySlimeSpawns);

			if (location == null)
			{
				break;
			}

			Slime slime = location.getWorld().spawn(location, Slime.class);

			if (Math.random() < 0.05)
			{
				slime.setSize(2);
				slime.setCustomName(C.cYellowB + "Power Slime");
				slime.setCustomNameVisible(true);
			}
			else
			{
				slime.setSize(1);
			}

			_tinySlimes.add(slime);
		}

		_worldComponent.setCreatureAllowOverride(false);
	}

	@EventHandler
	public void entityDeath(EntityDeathEvent event)
	{
		Entity entity = event.getEntity();

		if (_tinySlimes.remove(entity))
		{
			Player killer = event.getEntity().getKiller();

			if (killer != null)
			{
				killer.getInventory().addItem(new ItemStack(Material.SLIME_BALL, (int) Math.pow(((Slime) entity).getSize(), 2)));
				event.setDroppedExp(0);
				event.getDrops().clear();
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void kingInteract(CustomDamageEvent event)
	{
		LivingEntity damagee = event.GetDamageeEntity();
		Player damager = event.GetDamagerPlayer(false);

		if (damagee.equals(_king))
		{
			event.SetCancelled("King Damage");
			feed(damager);
		}
		else if (damager != null && damager.getInventory().contains(Material.SLIME_BALL, MAX_BALLS))
		{
			event.SetCancelled("Too Many Balls");
			damager.sendMessage(F.main(getManager().getName(), "You can only hold a maximum of " + F.count(MAX_BALLS) + " balls."));
		}
	}

	@EventHandler
	public void playerInteract(PlayerInteractEntityEvent event)
	{
		if (!event.getRightClicked().equals(_king))
		{
			return;
		}

		feed(event.getPlayer());
	}

	private void feed(Player player)
	{
		if (player == null || !isLive())
		{
			return;
		}

		ItemStack itemStack = player.getItemInHand();

		if (itemStack == null || itemStack.getType() != Material.SLIME_BALL)
		{
			return;
		}

		int amount = itemStack.getAmount();

		incrementScore(player, amount);
		UtilTextBottom.display(C.cYellow + "+" + amount + C.cGreen + " Slime Ball" + (amount == 1 ? "" : "s"), player);

		player.setItemInHand(null);

		int total = getScores().values().stream()
				.mapToInt(value -> value)
				.sum();

		int size = 2 + (total / 40);

		if (_king.getSize() != size)
		{
			_king.setSize(size);
			_king.getWorld().playSound(_king.getLocation(), Sound.SLIME_WALK2, 2, 1);
			UtilEnt.setPosition(_king, _kingSpawn);

			if (Math.random() > 0.5)
			{
				getManager().runSyncTimer(new BukkitRunnable()
				{
					float yaw = 0;

					@Override
					public void run()
					{
						yaw += 5;

						if (yaw > 360)
						{
							cancel();
							yaw = 0;
						}

						_kingSpawn.setYaw(yaw);
						UtilEnt.CreatureLook(_king, yaw);
					}
				}, 0, 1);
			}
			else
			{
				UtilAction.velocity(_king, new Vector(0, 0.5, 0));
			}
		}
		else
		{
			_king.getWorld().playSound(_king.getLocation(), Sound.SLIME_WALK, 2, 1);
		}

		player.getWorld().playEffect(player.getLocation().add(0, 1, 0), Effect.SLIME, 16);
	}
}