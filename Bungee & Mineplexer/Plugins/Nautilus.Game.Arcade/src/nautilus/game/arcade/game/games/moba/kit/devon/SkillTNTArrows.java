package nautilus.game.arcade.game.games.moba.kit.devon;

import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;
import nautilus.game.arcade.game.games.moba.Moba;
import nautilus.game.arcade.game.games.moba.kit.HeroSkill;
import nautilus.game.arcade.game.games.moba.util.MobaUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class SkillTNTArrows extends HeroSkill
{

	private static final String[] DESCRIPTION = {
			"Your next 3 arrows are infused with TNT.",
			"They explode on contact dealing damage and knockback."
	};
	private static final ItemStack SKILL_ITEM = new ItemStack(Material.TNT);

	private final Map<Player, Integer> _playerArrows = new HashMap<>();
	private final Set<Arrow> _arrows = new HashSet<>();

	public SkillTNTArrows(int slot)
	{
		super("TNT Infusion", DESCRIPTION, SKILL_ITEM, slot, ActionType.ANY);

		setCooldown(17000);
	}

	@EventHandler
	public void interact(PlayerInteractEvent event)
	{
		if (!isSkillItem(event))
		{
			return;
		}

		Player player = event.getPlayer();

		_playerArrows.put(player, 3);
		UtilInv.addDullEnchantment(player.getItemInHand());
		player.getItemInHand().setAmount(3);
	}

	@EventHandler
	public void shootArrow(EntityShootBowEvent event)
	{
		if (!(event.getEntity() instanceof Player))
		{
			return;
		}

		Player player = (Player) event.getEntity();

		if (!hasPerk(player) || !_playerArrows.containsKey(player))
		{
			return;
		}

		ItemStack itemStack = player.getInventory().getItem(getSlot());
		int arrows = _playerArrows.get(player);

		if (arrows == 1)
		{
			_playerArrows.remove(player);
			useSkill(player);
		}
		else
		{
			arrows--;
			_playerArrows.put(player, arrows);
			itemStack.setAmount(arrows);
		}

		Moba moba = (Moba) Manager.GetGame();

		moba.getArrowKbManager().allowKnockback(event.getProjectile());
		_arrows.add((Arrow) event.getProjectile());
	}

	@EventHandler
	public void projectileHit(ProjectileHitEvent event)
	{
		ProjectileSource source = event.getEntity().getShooter();

		if (!(source instanceof Player))
		{
			return;
		}

		Player player = (Player) source;
		Projectile projectile = event.getEntity();

		if (!_arrows.contains(projectile))
		{
			return;
		}

		_arrows.remove(projectile);

		Location location = projectile.getLocation();

		location.getWorld().playSound(location, Sound.EXPLODE, 1, 0.9F);
		UtilParticle.PlayParticle(ParticleType.HUGE_EXPLOSION, location, 0, 0, 0, 0.1F, 1, ViewDist.LONG);
		double damage = MobaUtil.scaleDamageWithBow(player.getInventory().getItem(0),5);

		for (Entry<LivingEntity, Double> entry : UtilEnt.getInRadius(location, 5).entrySet())
		{
			if (entry.getKey().equals(player))
			{
				continue;
			}

			Manager.GetDamage().NewDamageEvent(entry.getKey(), player, null, DamageCause.CUSTOM, entry.getValue() * damage, true, true, false, UtilEnt.getName(player), GetName());
		}
	}

	@EventHandler
	public void playerDeath(CombatDeathEvent event)
	{
		_playerArrows.remove(event.GetEvent().getEntity());
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		_playerArrows.remove(event.getPlayer());
	}
}

