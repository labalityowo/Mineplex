package nautilus.game.arcade.game.games.moba.kit.rowena;

import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.particles.effects.LineParticle;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.game.games.moba.Moba;
import nautilus.game.arcade.game.games.moba.kit.HeroSkill;
import nautilus.game.arcade.game.games.moba.structure.tower.Tower;
import nautilus.game.arcade.game.games.moba.structure.tower.TowerManager;
import nautilus.game.arcade.game.games.moba.util.MobaUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class SkillLightArrows extends HeroSkill
{

	private static final String[] DESCRIPTION = {
			"Your next 5 arrows become light infused.",
			"They pass through blocks and deal high damage."
	};
	private static final ItemStack SKILL_ITEM = new ItemStack(Material.GOLD_NUGGET);

	private final Map<Player, Integer> _playerArrows = new HashMap<>();
	private final Map<Player, Set<LineParticle>> _arrows = new HashMap<>();

	public SkillLightArrows(int slot)
	{
		super("Light Arrows", DESCRIPTION, SKILL_ITEM, slot, ActionType.ANY);

		setCooldown(12000);
	}

	@EventHandler
	public void interact(PlayerInteractEvent event)
	{
		if (!isSkillItem(event))
		{
			return;
		}

		Player player = event.getPlayer();

		_playerArrows.put(player, 5);
		UtilInv.addDullEnchantment(player.getItemInHand());
		player.getItemInHand().setAmount(5);
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

		event.getProjectile().remove();

		LineParticle lineParticle = new LineParticle(player.getEyeLocation(), player.getLocation().getDirection(), 0.4, 40, ParticleType.FIREWORKS_SPARK, UtilServer.getPlayers());
		lineParticle.setIgnoreAllBlocks(true);

		_arrows.putIfAbsent(player, new HashSet<>());
		_arrows.get(player).add(lineParticle);
	}

	@EventHandler
	public void updateArrows(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		TowerManager towerManager = ((Moba) Manager.GetGame()).getTowerManager();

		for (Entry<Player, Set<LineParticle>> entry : _arrows.entrySet())
		{
			Player player = entry.getKey();
			Iterator<LineParticle> iterator = entry.getValue().iterator();
			double damage = MobaUtil.scaleDamageWithBow(player.getInventory().getItem(0), 0) - 1;

			lineParticleLoop : while (iterator.hasNext())
			{
				LineParticle lineParticle = iterator.next();

				for (int i = 0; i < 6; i++)
				{
					if (!lineParticle.update())
					{
						Tower hitTower = towerManager.damageTowerAt(lineParticle.getLastLocation(), player, damage);

						if (hitTower != null && UtilMath.offsetSquared(hitTower.getCrystal(), player) > Tower.TARGET_RANGE_SQUARED)
						{
							continue;
						}

						for (LivingEntity entity : UtilEnt.getInRadius(lineParticle.getLastLocation(), 1.5).keySet())
						{
							if (!isTeamDamage(entity, player) && Recharge.Instance.use(player, GetName() + entity.getUniqueId(), 500, false, false))
							{
								player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 1, 0.8F);
								Manager.GetDamage().NewDamageEvent(entity, player, null, DamageCause.CUSTOM, damage, true, true, false, player.getName(), GetName());
							}
						}
					}
					else
					{
						iterator.remove();
						continue lineParticleLoop;
					}
				}
			}
		}
	}

	@EventHandler
	public void playerDeath(PlayerDeathEvent event)
	{
		_playerArrows.remove(event.getEntity());
		_arrows.remove(event.getEntity());
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		_playerArrows.remove(event.getPlayer());
		_arrows.remove(event.getPlayer());
	}
}

