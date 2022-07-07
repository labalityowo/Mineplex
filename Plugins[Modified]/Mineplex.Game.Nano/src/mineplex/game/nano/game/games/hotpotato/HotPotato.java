package mineplex.game.nano.game.games.hotpotato;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.Color;
import org.bukkit.EntityEffect;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
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
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.nano.NanoManager;
import mineplex.game.nano.game.GameType;
import mineplex.game.nano.game.SoloGame;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class HotPotato extends SoloGame
{

	private static final long ROUND_END_TIME = TimeUnit.SECONDS.toMillis(4);
	private static final int ROUND_MIN_TIME = 12;
	private static final int ROUND_MAX_TIME = 18;
	private static final double POTATO_FACTOR = 0.5;
	private static final int ROUND_TELEPORT_AMOUNT = 4;

	private final List<Player> _holders;

	private List<Location> _teleportTo;
	private long _lastRoundStart, _lastRoundEnd, _roundTime;

	public HotPotato(NanoManager manager)
	{
		super(manager, GameType.HOT_POTATO, new String[]
				{
						"Run away from players with " + C.cRed + "TNT" + C.Reset + "!",
						C.cYellow + "Punch players" + C.Reset + " to give them the " + C.cYellow + "Potato" + C.Reset + "!",
						"Watch out! Potatoes are " + C.cRed + "Explosive" + C.Reset + ".",
						C.cYellow + "Last player" + C.Reset + " standing wins!"
				});

		_holders = new ArrayList<>();

		_damageComponent
				.setPvp(false)
				.setFall(false);

		_prepareComponent.setPrepareFreeze(false);

		_playerComponent.setHideParticles(true);

		_scoreboardComponent.setSidebar((viewer, scoreboard) ->
		{
			scoreboard.writeNewLine();

			scoreboard.write(C.cYellowB + "Players");
			scoreboard.write(getAlivePlayers().size() + " Alive");

			if (isLive())
			{
				scoreboard.writeNewLine();

				scoreboard.write(C.cRedB + "Potatoes");

				if (_holders.isEmpty())
				{
					scoreboard.write("Next Round in " + UtilTime.MakeStr(_lastRoundEnd + ROUND_END_TIME - System.currentTimeMillis()));
				}
				else
				{
					for (Player player : _holders.subList(0, Math.min(9, _holders.size())))
					{
						scoreboard.write(player.getName());
					}
				}
			}

			scoreboard.writeNewLine();

			scoreboard.draw();
		});
	}

	@Override
	protected void parseData()
	{
		_teleportTo = _mineplexWorld.getIronLocations("GREEN");
		_teleportTo.forEach(location -> location.setYaw(UtilAlg.GetYaw(UtilAlg.getTrajectory(location, getSpectatorLocation()))));
	}

	@Override
	public void disable()
	{
		_holders.clear();
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK || !isLive())
		{
			return;
		}

		// Round over
		if (_holders.isEmpty())
		{
			if (!UtilTime.elapsed(_lastRoundEnd, ROUND_END_TIME))
			{
				return;
			}

			List<Player> alive = getAlivePlayers();
			_holders.addAll(alive);
			int players = (int) Math.floor(_holders.size() * POTATO_FACTOR);

			if (players == 0)
			{
				setState(GameState.End);
				return;
			}

			while (_holders.size() > players)
			{
				_holders.remove(UtilMath.r(_holders.size()));
			}

			announce(F.main(getManager().getName(), "A round has started with " + F.count(players) + " potato" + (players == 1 ? "" : "es") + "!"));
			_lastRoundStart = System.currentTimeMillis();
			_roundTime = TimeUnit.SECONDS.toMillis(UtilMath.rRange(ROUND_MIN_TIME, ROUND_MAX_TIME));

			for (Player player : _holders)
			{
				setTagged(player, true, true);
			}

			if (alive.size() <= ROUND_TELEPORT_AMOUNT)
			{
				for (int i = 0; i < alive.size(); i++)
				{
					alive.get(i).teleport(_teleportTo.get(i));
				}
			}
		}
		// Round in progress
		else
		{
			long diff = System.currentTimeMillis() - _lastRoundStart;

			if (diff > _roundTime)
			{
				_lastRoundEnd = System.currentTimeMillis();

				int exploded = 0;
				FireworkEffect effect = FireworkEffect.builder()
						.with(UtilMath.randomElement(Type.values()))
						.withColor(Color.RED)
						.build();

				for (Player player : getAlivePlayers())
				{
					if (!_holders.contains(player))
					{
						continue;
					}

					Location location = player.getLocation().add(0, 1, 0);

					UtilParticle.PlayParticleToAll(ParticleType.HUGE_EXPLOSION, location, null, 0, 1, ViewDist.LONG);
					location.getWorld().playSound(location, Sound.EXPLODE, 1, 0.5F);

					if (exploded++ < 5)
					{
						for (int i = 0; i < 3; i++)
						{
							UtilFirework.launchFirework(location, effect, null, i);
						}
					}

					getManager().getDamageManager().NewDamageEvent(player, null, null, DamageCause.ENTITY_EXPLOSION, 500, false, true, true, getGameType().getName(), "The Hot Potato");
					getManager().runSyncLater(() -> UtilAction.velocity(player, new Vector(0, 2, 0)), 1);
				}

				UtilTextBottom.display(C.cRedB + "BOOM!", UtilServer.getPlayers());
				_holders.clear();
			}
			else
			{
				diff = _roundTime - diff;

				UtilTextBottom.displayProgress("Detonation", (double) diff / _roundTime, (diff < 3000 ? C.cRed : "") + UtilTime.MakeStr(Math.max(0, diff)), UtilServer.getPlayers());

				_holders.forEach(player ->
				{
					List<Player> players = player.getWorld().getPlayers();
					players.remove(player);

					UtilParticle.PlayParticle(ParticleType.FLAME, player.getLocation().add(0, 2, 0), null, 0.01F, 1, ViewDist.LONG, players.toArray(new Player[0]));
				});
			}
		}
	}

	@EventHandler
	public void damage(CustomDamageEvent event)
	{
		if (!isLive())
		{
			return;
		}

		Player damagee = event.GetDamageePlayer(), damager = event.GetDamagerPlayer(false);

		if (damagee == null || damager == null || UtilPlayer.isSpectator(damagee) || UtilPlayer.isSpectator(damager) || _holders.contains(damagee) || !_holders.contains(damager))
		{
			return;
		}

		String name = "Tag Player";

		if (!Recharge.Instance.usable(damagee, name) || !Recharge.Instance.use(damager, name, 500, false, false))
		{
			return;
		}

		damagee.playEffect(EntityEffect.HURT);
		setTagged(damagee, true, false);
		setTagged(damager, false, false);
	}

	private void setTagged(Player player, boolean tagged, boolean initial)
	{
		if (tagged)
		{
			if (!initial)
			{
				_holders.add(player);
			}

			PlayerInventory inventory = player.getInventory();
			ItemStack inHand = new ItemBuilder(Material.POTATO_ITEM)
					.setTitle(C.cRedB + "Punch Someone!")
					.build();

			for (int i = 0; i < 9; i++)
			{
				inventory.setItem(i, inHand);
			}

			inventory.setHelmet(new ItemStack(Material.TNT));
			inventory.setChestplate(new ItemBuilder(Material.LEATHER_CHESTPLATE)
					.setColor(Color.RED)
					.build());
			inventory.setLeggings(new ItemBuilder(Material.LEATHER_LEGGINGS)
					.setColor(Color.RED)
					.build());
			inventory.setBoots(new ItemBuilder(Material.LEATHER_BOOTS)
					.setColor(Color.RED)
					.build());

			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false));

			UtilTextMiddle.display(null, C.cRed + "You have the Potato! Hit someone to get rid of it!", 0, initial ? 50 : 20, 10, player);
		}
		else
		{
			_holders.remove(player);
			UtilPlayer.clearInventory(player);
			player.removePotionEffect(PotionEffectType.SPEED);
		}
	}

	@EventHandler
	public void sneak(PlayerToggleSneakEvent event)
	{
		if (event.isSneaking() && isAlive(event.getPlayer()))
		{
			event.setCancelled(true);
		}
	}
}
