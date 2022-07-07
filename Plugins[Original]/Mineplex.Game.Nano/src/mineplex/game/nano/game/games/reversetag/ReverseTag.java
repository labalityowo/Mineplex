package mineplex.game.nano.game.games.reversetag;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.Color;
import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.nano.NanoManager;
import mineplex.game.nano.game.GameType;
import mineplex.game.nano.game.ScoredSoloGame;
import mineplex.game.nano.game.event.GameStateChangeEvent;
import mineplex.game.nano.game.event.PlayerStateChangeEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class ReverseTag extends ScoredSoloGame
{

	private static final double EMERALD_FACTOR = 0.5;

	private final List<Player> _holders;

	public ReverseTag(NanoManager manager)
	{
		super(manager, GameType.REVERSE_TAG, new String[]
				{
						"Try to keep hold of the " + C.cGreen + "Emeralds" + C.Reset + "!",
						C.cYellow + "Punch players" + C.Reset + " to take their " + C.cGreen + "Emeralds" + C.Reset + "!",
						"Every " + C.cYellow + "Second" + C.Reset + " with " + C.cGreen + "Emeralds" + C.Reset + " you earn points.",
						C.cYellow + "Most points" + C.Reset + " wins!"
				});

		_holders = new ArrayList<>();

		_damageComponent
				.setPvp(false)
				.setFall(false);

		_prepareComponent.setPrepareFreeze(false);

		_playerComponent.setHideParticles(true);

		_endComponent.setTimeout(TimeUnit.MINUTES.toMillis(1));
	}

	@Override
	protected void parseData()
	{

	}

	@Override
	public void disable()
	{
		_holders.clear();
	}

	@EventHandler
	public void live(GameStateChangeEvent event)
	{
		if (event.getState() != GameState.Live)
		{
			return;
		}

		List<Player> alive = getAlivePlayers();
		int players = (int) Math.ceil(alive.size() * EMERALD_FACTOR);

		if (players == 0)
		{
			setState(GameState.End);
			return;
		}

		while (alive.size() > players)
		{
			alive.remove(UtilMath.r(alive.size()));
		}

		_holders.addAll(alive);
		_holders.forEach(player -> setTagged(player, true, true));
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC || !isLive())
		{
			return;
		}

		_holders.forEach(player -> incrementScore(player, 1));
	}

	@EventHandler
	public void damage(CustomDamageEvent event)
	{
		if (!isLive())
		{
			return;
		}

		Player damagee = event.GetDamageePlayer(), damager = event.GetDamagerPlayer(false);

		if (damagee == null || damager == null || UtilPlayer.isSpectator(damagee) || UtilPlayer.isSpectator(damager) || !_holders.contains(damagee) || _holders.contains(damager))
		{
			return;
		}

		String name = "Tag Player";

		if (!Recharge.Instance.usable(damagee, name) || !Recharge.Instance.use(damager, name, 500, false, false))
		{
			return;
		}

		damagee.playEffect(EntityEffect.HURT);
		setTagged(damagee, false, false);
		setTagged(damager, true, false);
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
			ItemStack inHand = new ItemBuilder(Material.EMERALD)
					.setTitle(C.cGreenB + "Keep These!")
					.build();

			for (int i = 0; i < 9; i++)
			{
				inventory.setItem(i, inHand);
			}

			inventory.setHelmet(new ItemStack(Material.EMERALD_BLOCK));
			inventory.setChestplate(new ItemBuilder(Material.LEATHER_CHESTPLATE)
					.setColor(Color.LIME)
					.build());
			inventory.setLeggings(new ItemBuilder(Material.LEATHER_LEGGINGS)
					.setColor(Color.LIME)
					.build());
			inventory.setBoots(new ItemBuilder(Material.LEATHER_BOOTS)
					.setColor(Color.LIME)
					.build());

			player.removePotionEffect(PotionEffectType.SPEED);
			UtilTextMiddle.display(null, C.cGreen + "You have the Emeralds! Run!", 0, initial ? 50 : 20, 10, player);
		}
		else
		{
			_holders.remove(player);
			UtilPlayer.clearInventory(player);
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false));
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

	@EventHandler
	public void playerQut(PlayerStateChangeEvent event)
	{
		if (!event.isAlive())
		{
			_holders.remove(event.getPlayer());
		}
	}
}
