package mineplex.minecraft.game.classcombat.Skill.Ranger;

import java.util.HashSet;
import java.util.Iterator;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilServer;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.updater.UpdateType;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.minecraft.game.classcombat.Skill.SkillActive;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class NapalmShot extends SkillActive
{
	private HashSet<Entity> _arrows = new HashSet<Entity>();
	private HashSet<Player> _napalm = new HashSet<Player>();

	public NapalmShot(SkillFactory skills, String name, ClassType classType, SkillType skillType,
            int cost, int levels,
            int energy, int energyMod, 
            long recharge, long rechargeMod, boolean rechargeInform, 
            Material[] itemArray, 
            Action[] actionArray) 
    {
        super(skills, name, classType, skillType, 
                cost, levels,
                energy, energyMod, 
                recharge, rechargeMod, rechargeInform, 
                itemArray,
                actionArray);

		SetDesc(new String[] 
				{
				"Prepare a Napalm Shot;",
				"",
				"Your next arrow will burst into",
				"#8#8 flames on impact.",
				"",
				"If your arrow hit an enemy, it",
				"will ignite them for #2#1 seconds."
				});
	}

	@Override
	public boolean CustomCheck(Player player, int level) 
	{
		if (isInWater(player))
		{
			UtilPlayer.message(player, F.main("Skill", "You cannot use " + F.skill(GetName()) + " in water."));
			return false;
		}

		return true;
	}

	@Override
	public void Skill(Player player, int level) 
	{
		//Action
		_napalm.add(player);

		//Inform
		UtilPlayer.message(player, F.main(GetClassType().name(), "You prepared " + F.skill(GetName(level)) + "."));

		//Effect
		player.getWorld().playSound(player.getLocation(), Sound.BLAZE_BREATH, 2.5f, 2.0f);
	}

	@EventHandler
	public void bowShoot(EntityShootBowEvent event)
	{
		if (!(event.getEntity() instanceof Player))
			return;

		if (!(event.getProjectile() instanceof Arrow))
			return;

		Player player = (Player)event.getEntity();

		if (!_napalm.remove(player))
			return;

		//Inform
		UtilPlayer.message(player, F.main(GetClassType().name(), "You fired " + F.skill(GetName(getLevel(player))) + "."));

		_arrows.add(event.getProjectile());
		event.getProjectile().setFireTicks(120);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void arrowDamage(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;

		if (event.GetCause() != DamageCause.PROJECTILE)
			return;

		Projectile projectile = event.GetProjectile();
		if (projectile == null)	return;

		if (!_arrows.contains(projectile))
			return;

		LivingEntity damagee = event.GetDamageeEntity();
		if (damagee == null)	return;

		Player damager = event.GetDamagerPlayer(true);
		if (damager == null)	return;

		//Level
		int level = getLevel(damager);
		if (level == 0)			return;

		//Ignite
		Factory.Condition().Factory().Ignite(GetName(), damagee, damager, 2 + level, true, true);

		//Damage
		event.AddMod(damager.getName(), GetName(), 0, true);

		//Effect 
		damagee.getWorld().playSound(damagee.getLocation(), Sound.FIZZ, 2f, 1.5f);
		
		//Remove
		projectile.remove();
	}
	
	@EventHandler(priority=EventPriority.MONITOR)	//make it happen after the damage event ^
	public void projectileHit(ProjectileHitEvent event)
	{
		Projectile proj = event.getEntity();

		if (!_arrows.contains(proj))
			return;

		if (proj.getShooter() == null)
			return;

		if (!(proj.getShooter() instanceof Player))
			return;

		Player damager = (Player)proj.getShooter();
		int level = getLevel(damager);
		if (level == 0)		return;

		proj.getWorld().playSound(proj.getLocation(), Sound.EXPLODE, 0.4f, 2f);

		for (int i = 0 ; i < 8 + (8 * level) ; i++)
		{
			ItemStack itemStack = ItemStackFactory.Instance.CreateStack(Material.BLAZE_POWDER, 1);
			ItemMeta meta = itemStack.getItemMeta();
			meta.setDisplayName(ChatColor.RESET + "" + UtilEnt.getNewEntityId(false));
			itemStack.setItemMeta(meta);
			
			Item fire = proj.getWorld().dropItemNaturally(proj.getLocation(), itemStack);
			Factory.Fire().Add(fire, damager, 16, 0.25, 2, 0.25, GetName(), true);
			fire.setVelocity(fire.getVelocity().multiply(1 + (0.15 * level)));
		}
		
		//Remove
		proj.remove();
	}
	
	@EventHandler
	public void particle(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		for (Entity ent : _arrows)
		{
			UtilParticle.PlayParticle(ParticleType.FLAME, ent.getLocation(), 0, 0, 0, 0, 1,
					ViewDist.MAX, UtilServer.getPlayers());
		}
	}
	
	@EventHandler
	public void clean(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		_arrows.removeIf(arrow -> !arrow.isValid() || arrow.isOnGround());
	}

	@Override
	public void Reset(Player player) 
	{
		_napalm.remove(player);
	}
}
