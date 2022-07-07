package nautilus.game.arcade.game.games.dragons;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.ItemSpawnEvent;

import com.mineplex.anticheat.checks.move.Glide;
import com.mineplex.anticheat.checks.move.HeadRoll;
import com.mineplex.anticheat.checks.move.Speed;

import mineplex.core.Managers;
import mineplex.core.antihack.AntiHack;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilTime;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.PlayerDeathOutEvent;
import nautilus.game.arcade.game.SoloGame;
import nautilus.game.arcade.game.games.dragons.kits.KitCoward;
import nautilus.game.arcade.game.games.dragons.kits.KitMarksman;
import nautilus.game.arcade.game.games.dragons.kits.KitPyrotechnic;
import nautilus.game.arcade.game.modules.compass.CompassModule;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkSparkler;
import nautilus.game.arcade.managers.chat.ChatStatData;
import nautilus.game.arcade.stats.SparklezStatTracker;

public class Dragons extends SoloGame
{

	private static final String[] DESCRIPTION =
			{
					"You have angered the Dragons!",
					"Survive as best you can!!!",
					"Last player alive wins!"
			};
	private static final int MAX_DRAGONS = 5;

	private final Set<MineplexDragon> _dragons;

	private List<Location> _dragonSpawns;
	private PerkSparkler _sparkler;

	public Dragons(ArcadeManager manager)
	{
		this(manager, GameType.Dragons, DESCRIPTION);
	}

	public Dragons(ArcadeManager manager, GameType gameType, String[] description)
	{
		super(manager, gameType, new Kit[]
						{
								new KitCoward(manager),
								new KitMarksman(manager),
								new KitPyrotechnic(manager)
						}, description);

		_dragons = new HashSet<>(MAX_DRAGONS);

		DamagePvP = false;
		HungerSet = 20;
		WorldWaterDamage = 4;
		PlayerGameMode = GameMode.ADVENTURE;

		registerStatTrackers(
				new SparklezStatTracker(this)
		);

		registerChatStats(
				DamageDealt,
				DamageTaken,
				BlankLine,
				new ChatStatData("kit", "Kit", true)
		);

		new CompassModule()
				.register(this);

		// Disable specific GWEN checks for this game
		AntiHack antiHack = Managers.get(AntiHack.class);
		antiHack.addIgnoredCheck(Speed.class);
		antiHack.addIgnoredCheck(Glide.class);
		antiHack.addIgnoredCheck(HeadRoll.class);
	}

	@Override
	public void ParseData()
	{
		_dragonSpawns = WorldData.GetDataLocs("RED");

		if (_sparkler == null)
		{
			for (Kit kit : GetKits())
			{
				for (Perk perk : kit.GetPerks())
				{
					if (perk instanceof PerkSparkler)
					{
						_sparkler = (PerkSparkler) perk;
					}
				}
			}
		}
	}

	@EventHandler
	public void dragonSpawn(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC_05 || !IsLive())
		{
			return;
		}

		_dragons.removeIf(dragon -> !dragon.getEntity().isValid());

		if (_dragons.size() < MAX_DRAGONS)
		{
			CreatureAllowOverride = true;

			Location location = UtilAlg.Random(_dragonSpawns);

			if (location == null)
			{
				return;
			}

			EnderDragon dragon = location.getWorld().spawn(location, EnderDragon.class);
			UtilEnt.vegetate(dragon);
			UtilEnt.ghost(dragon, true, false);
			UtilEnt.setTickWhenFarAway(dragon, true);

			location.getWorld().playSound(location, Sound.ENDERDRAGON_GROWL, 20f, 1f);

			_dragons.add(new MineplexDragon(this, dragon));

			CreatureAllowOverride = false;
		}
	}

	@EventHandler
	public void dragonTarget(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTEST || !IsLive())
		{
			return;
		}

		List<Player> alive = GetPlayers(true);

		_dragons.forEach(dragon ->
		{
			dragon.updateTarget();

			for (Player player : UtilEnt.getPlayersInsideEntity(dragon.getEntity(), alive))
			{
				if (!Recharge.Instance.use(player, "Hit By " + dragon.getEntity().getEntityId(), 2000, false, false))
				{
					continue;
				}

				player.playEffect(EntityEffect.HURT);
				UtilAction.velocity(player, UtilAlg.getTrajectory(dragon.getEntity(), player), 1, false, 0, 0.6, 2, true);
			}
		});

		for (Item item : _sparkler.GetItems())
		{
			Location location = item.getLocation();

			if (location.getY() < 4 || location.getBlock().isLiquid())
			{
				continue;
			}

			_dragons.forEach(dragon -> dragon.targetSparkler(location));
		}
	}

	@EventHandler
	public void dragonTarget(EntityTargetEvent event)
	{
		if (!IsLive())
		{
			return;
		}

		event.setCancelled(true);
	}

	@EventHandler
	public void dragonArrowDamage(CustomDamageEvent event)
	{
		if (event.IsCancelled())
		{
			return;
		}

		for (MineplexDragon dragon : _dragons)
		{
			if (dragon.getEntity().equals(event.GetDamageeEntity()))
			{
				if (event.GetProjectile() == null)
				{
					dragon.targetSky();
				}

				event.SetCancelled("Dragon");
				dragon.getEntity().playEffect(EntityEffect.HURT);
				return;
			}
		}
	}

	@EventHandler
	public void fallDamage(CustomDamageEvent event)
	{
		if (event.IsCancelled() || event.GetCause() != DamageCause.FALL)
		{
			return;
		}

		event.AddMod(GetName(), "Fall Reduction", -1, false);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void itemSpawn(ItemSpawnEvent event)
	{
		if (event.getEntity().getItemStack().getType() == Material.EMERALD)
		{
			return;
		}

		event.setCancelled(true);
	}

	@EventHandler
	public void playerOut(PlayerDeathOutEvent event)
	{
		giveSurvivedGems(event.GetPlayer());
	}

	protected void giveSurvivedGems(Player player)
	{
		long time = (System.currentTimeMillis() - GetStateTime());
		double gems = time / 10000d;
		String reason = "Survived for " + UtilTime.MakeStr(time);

		AddGems(player, gems, reason, false, false);
	}

	@Override
	public void AnnounceEnd(List<Player> places)
	{
		// Give the winner gems for surviving the latest
		giveSurvivedGems(places.get(0));

		super.AnnounceEnd(places);
	}
}
