package nautilus.game.arcade.game.games.moba.kit.ivy;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.game.games.moba.Moba;
import nautilus.game.arcade.game.games.moba.buff.BuffManager;
import nautilus.game.arcade.game.games.moba.buff.buffs.BuffCrippled;
import nautilus.game.arcade.game.games.moba.kit.HeroSkill;

public class SkillBoxingRing extends HeroSkill
{

	private static final String[] DESCRIPTION = {
			"Creates a wall of thorns that damages",
			"and roots any players that are contained",
			"inside."
	};
	private static final ItemStack SKILL_ITEM = new ItemStack(Material.NETHER_STAR);
	private static final long DURATION = TimeUnit.SECONDS.toMillis(5);
	private static final int BLOCK_RADIUS = 3;
	private static final PotionEffect BUFF = new PotionEffect(PotionEffectType.REGENERATION, 60, 1, false, false);
	private static final long DEBUFF_DURATION = TimeUnit.SECONDS.toMillis(2);
	private static final int DAMAGE = 2;

	private final Set<BoxingRingData> _data = new HashSet<>();

	public SkillBoxingRing(int slot)
	{
		super("Thorn Wall", DESCRIPTION, SKILL_ITEM, slot, ActionType.ANY);

		setCooldown(45000);
		setDropItemActivate(true);
	}

	@EventHandler
	public void interact(PlayerInteractEvent event)
	{
		if (!isSkillItem(event))
		{
			return;
		}

		Player player = event.getPlayer();
		Location location = player.getLocation().subtract(0, 1, 0);
		byte colour = (byte) (Manager.GetGame().GetTeam(player).GetColor() == ChatColor.RED ? 14 : 11);

		location.getWorld().playEffect(location, Effect.STEP_SOUND, Material.STAINED_CLAY, colour);

		for (Block block : getSquareBlocks(location, BLOCK_RADIUS, false))
		{
			Manager.GetBlockRestore().add(block, Material.STAINED_CLAY.getId(), colour, DURATION + UtilMath.rRange(-500, 500));
		}


		location.add(0, 1, 0);

		for (Block block : getSquareBlocks(location, BLOCK_RADIUS, true))
		{
			Manager.GetBlockRestore().add(block, Material.RED_ROSE.getId(), (byte) 0, DURATION + UtilMath.r(500));
			block.getWorld().playEffect(block.getLocation().add(0.5, 0.5, 0.5), Effect.STEP_SOUND, Material.RED_ROSE);
		}

		_data.add(new BoxingRingData(player));
		useSkill(player);
	}

	private List<Block> getSquareBlocks(Location center, int radius, boolean border)
	{
		return UtilBlock.getInBoundingBox(center.clone().subtract(radius, 0, radius), center.clone().add(radius, 0, radius), false, false, border, false);
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
		{
			return;
		}

		_data.removeIf(data ->
		{
			Player player = data.Owner;
			Location location = data.Center;

			if (!player.isOnline() || UtilTime.elapsed(data.Start, DURATION))
			{
				return true;
			}

			Moba host = (Moba) Manager.GetGame();
			BuffManager buffManager = host.getBuffManager();

			for (Player nearby : UtilPlayer.getNearby(location, BLOCK_RADIUS))
			{
				if (isTeamDamage(player, nearby))
				{
					nearby.addPotionEffect(BUFF);
				}
				else if (Recharge.Instance.use(nearby, GetName() + " Rooting", 2000, false, false))
				{
					buffManager.apply(new BuffCrippled(host, nearby, DEBUFF_DURATION));
				}
				else if (Recharge.Instance.use(nearby, GetName() + " Damage", 900, false, false))
				{
					Manager.GetDamage().NewDamageEvent(nearby, player, null, DamageCause.CUSTOM, DAMAGE, false, true, false, player.getName(), GetName());
				}
			}

			return false;
		});
	}

	private class BoxingRingData
	{

		Player Owner;
		Location Center;
		long Start;

		BoxingRingData(Player owner)
		{
			Owner = owner;
			Center = owner.getLocation();
			Start = System.currentTimeMillis();
		}
	}
}

