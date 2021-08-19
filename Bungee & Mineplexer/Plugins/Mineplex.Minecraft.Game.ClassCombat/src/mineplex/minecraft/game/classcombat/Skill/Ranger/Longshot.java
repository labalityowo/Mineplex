package mineplex.minecraft.game.classcombat.Skill.Ranger;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.metadata.FixedMetadataValue;

import mineplex.core.common.util.UtilMath;
import mineplex.core.recharge.Recharge;
import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.classcombat.Skill.Skill;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class Longshot extends Skill
{

    public Longshot(SkillFactory skills, String name, ClassType classType, SkillType skillType, int cost, int levels)
    {
        super(skills, name, classType, skillType, cost, levels);

        SetDesc(new String[]
            {
            		"Active by default, hold sneak to not use it.",
					"",
                    "Arrows fire 20% faster and",
					"deal an additional 1 damage",
                    "for every #4.5#-0.5 Blocks they travelled.",
                    "", 
                    "Maximum of 12 additional damage."
            });
    }

	@Override
	public String GetRechargeString()
	{
		return "Recharge: 7#-1 Seconds.";
	}

	@EventHandler
    public void ShootBow(EntityShootBowEvent event)
    {
        int level = getLevel(event.getEntity());

        if (level == 0)
		{
			return;
		}

		Player player = (Player) event.getEntity();
		Entity projectile = event.getProjectile();

		if (player.isSneaking() || !Recharge.Instance.use(player, GetName(), 7000 - (1000 * level), true, false))
		{
			return;
		}

		player.getWorld().playSound(player.getLocation(), Sound.FIZZ, 1, 1);
		projectile.setVelocity(projectile.getVelocity().multiply(1.2));
		projectile.setMetadata("ShotFrom", new FixedMetadataValue(Factory.getPlugin(), projectile.getLocation()));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void Damage(CustomDamageEvent event)
    {
        if (event.IsCancelled())
		{
			return;
		}

        Projectile projectile = event.GetProjectile();

        if (projectile == null || !projectile.hasMetadata("ShotFrom"))
		{
			return;
		}

        Player damager = event.GetDamagerPlayer(true);

        if (damager == null)
		{
			return;
		}

        int level = getLevel(damager);

        Location loc = (Location) projectile.getMetadata("ShotFrom").get(0).value();
        double length = UtilMath.offset(loc, projectile.getLocation());

        // Damage
        double damage = length / (4.5 - 0.5 * level);
        damage = Math.max(Math.min(damage, 12), 0);

        event.AddMod(damager.getName(), GetName(), damage, damage > 0);
    }

    @Override
    public void Reset(Player player)
    {

    }
}
