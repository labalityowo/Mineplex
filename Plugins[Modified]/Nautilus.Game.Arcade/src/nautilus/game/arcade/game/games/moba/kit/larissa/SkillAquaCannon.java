package nautilus.game.arcade.game.games.moba.kit.larissa;

import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.particles.effects.LineParticle;
import mineplex.core.recharge.Recharge;
import nautilus.game.arcade.game.games.moba.Moba;
import nautilus.game.arcade.game.games.moba.kit.HeroSkill;
import nautilus.game.arcade.game.games.moba.structure.tower.TowerManager;
import nautilus.game.arcade.game.games.moba.util.MobaConstants;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class SkillAquaCannon extends HeroSkill
{

	private static final String[] DESCRIPTION = {
			"Fires a beam of water that deals damage",
			"to the first enemy it comes in contact with."
	};
	private static final int DAMAGE = 3;
	private static final ItemStack SKILL_ITEM = new ItemStack(Material.DIAMOND_HOE);

	public SkillAquaCannon(int slot)
	{
		super("Aqua Cannon", DESCRIPTION, SKILL_ITEM, slot, ActionType.ANY);
	}

	@EventHandler
	public void interact(PlayerInteractEvent event)
	{
		if (!isSkillItem(event))
		{
			return;
		}

		Player player = event.getPlayer();

		if (!Recharge.Instance.use(player, GetName(), 500, false, true) || !_kit.useAmmo(player, 1))
		{
			return;
		}

		TowerManager towerManager = ((Moba) Manager.GetGame()).getTowerManager();
		Vector direction = player.getLocation().getDirection();

		LineParticle lineParticle = new LineParticle(player.getLocation().add(0, 1.7, 0), direction, 0.2, 10, ParticleType.DRIP_WATER, UtilServer.getPlayers());

		while (!lineParticle.update() && towerManager.damageTowerAt(lineParticle.getLastLocation(), player, DAMAGE) == null)
		{
			for (LivingEntity entity : UtilEnt.getInRadius(lineParticle.getLastLocation(), 2).keySet())
			{
				if (isTeamDamage(entity, player))
				{
					continue;
				}

				Manager.GetDamage().NewDamageEvent(entity, player, null, DamageCause.CUSTOM, DAMAGE, false, false, false, player.getName(), MobaConstants.BASIC_ATTACK);
				break;
			}
		}

		player.getWorld().playSound(lineParticle.getLastLocation(), Sound.BLAZE_HIT, 1, 1.4F);
		UtilParticle.PlayParticleToAll(ParticleType.FIREWORKS_SPARK, lineParticle.getLastLocation(), 0, 0, 0, 0.2F, 10, ViewDist.LONG);
	}
}
