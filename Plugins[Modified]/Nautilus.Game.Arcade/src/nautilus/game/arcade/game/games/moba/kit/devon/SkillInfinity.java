package nautilus.game.arcade.game.games.moba.kit.devon;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.moba.Moba;
import nautilus.game.arcade.game.games.moba.kit.HeroSkill;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class SkillInfinity extends HeroSkill
{

	private static final String[] DESCRIPTION = {
			"For 7 seconds, your arrow shots don't consume arrows.",
			"They also become heat-seeking and inflict wither onto players."
	};
	private static final ItemStack SKILL_ITEM = new ItemStack(Material.NETHER_STAR);

	private final Map<Entity, Player> _arrows = new HashMap<>();
	private final Set<Player> _active = new HashSet<>();

	public SkillInfinity(int slot)
	{
		super("Infinity", DESCRIPTION, SKILL_ITEM, slot, ActionType.ANY);

		setCooldown(60000);
		setDropItemActivate(true);
	}

	@Override
	@EventHandler
	public void interact(PlayerInteractEvent event)
	{
		if (!isSkillItem(event))
		{
			return;
		}

		Player player = event.getPlayer();
		ItemStack bow = player.getInventory().getItem(0);

		if (bow == null || bow.getType() != Material.BOW)
		{
			return;
		}

		// Give 1 arrow just incase the player didn't have one
		_kit.giveAmmo(player, 1);
		bow.addEnchantment(Enchantment.ARROW_INFINITE, 1);

		broadcast(player);
		useActiveSkill(() ->
		{
			bow.removeEnchantment(Enchantment.ARROW_INFINITE);
		}, player, 7000);
	}

	@EventHandler
	public void shootArrow(EntityShootBowEvent event)
	{
		if (!(event.getEntity() instanceof Player))
		{
			return;
		}

		Player player = (Player) event.getEntity();

		if (!hasPerk(player) || !_active.contains(player))
		{
			return;
		}

		Moba moba = (Moba) Manager.GetGame();

		moba.getArrowKbManager().allowKnockback(event.getProjectile());
		_arrows.put(event.getProjectile(), player);
	}

	@EventHandler
	public void updateArrowTarget(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTEST)
		{
			return;
		}

		for (Entry<Entity, Player> entry : _arrows.entrySet())
		{
			Entity entity = entry.getKey();
			Player player = entry.getValue();
			GameTeam team = Manager.GetGame().GetTeam(player);

			for (Player nearby : UtilPlayer.getInRadius(entity.getLocation(), 6).keySet())
			{
				// If the target is on the same team
				if (UtilPlayer.isSpectator(player) || team.equals(Manager.GetGame().GetTeam(nearby)))
				{
					continue;
				}

				UtilAction.velocity(entity, UtilAlg.getTrajectory(entity.getLocation(), nearby.getLocation().add(0, 1.5, 0)));
			}
		}
	}

	@EventHandler
	public void arrowDamage(CustomDamageEvent event)
	{
		if (_arrows.containsKey(event.GetProjectile()))
		{
			Manager.GetCondition().Factory().Wither(GetName(), event.GetDamageeEntity(), event.GetDamagerEntity(true), 3, 0, false, true, false);
		}
	}

	@EventHandler
	public void projectileHit(ProjectileHitEvent event)
	{
		// Delay this as the when the CustomDamageEvent is run. The arrow is already removed from the map.
		Manager.runSyncLater(() -> _arrows.remove(event.getEntity()), 1);
	}
}

