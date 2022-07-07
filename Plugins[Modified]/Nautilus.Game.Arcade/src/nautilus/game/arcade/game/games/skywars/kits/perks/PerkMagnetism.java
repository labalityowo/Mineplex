package nautilus.game.arcade.game.games.skywars.kits.perks;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.particles.effects.LineParticle;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.games.skywars.TeamSkywars;

public class PerkMagnetism extends SkywarsPerk
{

	private final long _cooldown;
	private final int _range;
	private final double _magnituideFactor;

	public PerkMagnetism(ItemStack itemStack, long cooldown, int range, double magnitudeFactor)
	{
		super("Magnetism", itemStack);

		_cooldown = cooldown;
		_range = range;
		_magnituideFactor = magnitudeFactor;
	}

	@Override
	public void onUseItem(Player player)
	{
		Player target = UtilPlayer.getPlayerInSight(player, _range, true);

		if (target == null)
		{
			return;
		}

		Game game = Manager.GetGame();

		if (game instanceof TeamSkywars)
		{
			if (game.GetTeam(player).equals(game.GetTeam(target)))
			{
				player.sendMessage(F.main("Game", "They are on your team!"));
				return;
			}
		}

		int magnitude = getAmountOfMetalArmor(target);

		if (magnitude == 0)
		{
			player.sendMessage(F.main("Game", "They do not have any metal armor on."));
			return;
		}

		if (!Recharge.Instance.use(player, GetName(), _cooldown, true, true))
		{
			return;
		}

		Manager.GetDamage().NewDamageEvent(target, player, null, DamageCause.CUSTOM, 1, false, true, true, player.getName(), GetName());

		LineParticle lineParticle = new LineParticle(player.getEyeLocation(), player.getLocation().getDirection(), 0.5, UtilMath.offset(player, target), ParticleType.FIREWORKS_SPARK, UtilServer
				.getPlayers());

		while (!lineParticle.update())
		{
		}

		Vector vector = UtilAlg.getTrajectory(target, player).multiply((.5 + magnitude / 4) * _magnituideFactor);

		UtilAction.velocity(target, vector.setY(Math.max(.6, vector.getY())));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		for (Player player : UtilServer.getPlayers())
		{
			if (!hasPerk(player) || UtilPlayer.isSpectator(player))
			{
				continue;
			}

			int magnitude = getAmountOfMetalArmor(player);

			player.setMaxHealth(20 + magnitude);
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event)
	{
		if (!hasPerk(event.getPlayer()))
		{
			return;
		}

		Block block = event.getBlock();

		if (block.getType() == Material.IRON_ORE)
		{
			block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), new ItemStack(Material.IRON_INGOT));
		}
	}

	private int getAmountOfMetalArmor(Player player)
	{
		int magnitude = 0;

		for (ItemStack itemStack : player.getInventory().getArmorContents())
		{
			if (UtilItem.isIronProduct(itemStack) || UtilItem.isGoldProduct(itemStack) || UtilItem.isChainmailProduct(itemStack))
			{
				magnitude++;
			}
		}

		return magnitude;
	}

}
