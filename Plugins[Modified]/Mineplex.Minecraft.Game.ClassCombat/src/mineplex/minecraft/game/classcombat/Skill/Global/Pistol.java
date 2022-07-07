package mineplex.minecraft.game.classcombat.Skill.Global;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

import mineplex.core.common.util.UtilEnt;
import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilMath;
import mineplex.minecraft.game.classcombat.Skill.SkillActive;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;

public class Pistol extends SkillActive
{
	public Pistol(SkillFactory skills, String name, ClassType classType, SkillType skillType, 
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
	"Pew Pew"
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

		//Use Gunpowder
		if (!UtilInv.remove(player, Material.MELON_SEEDS, (byte)0, 1))
		{
			UtilPlayer.message(player, F.main("Skill", "You need " + F.item("Pistol Ammo") + " to use " + F.skill(GetName()) + "."));
			return false;
		}

		return true;
	}

	@Override
	public void Skill(Player player, int level) 
	{
		//Action
		double sharpness = 0.1;

		double travel = 0;
		double maxTravel = 100;

		double hitBox = 0.5;

		//Effect
		player.getWorld().playEffect(player.getEyeLocation().add(player.getLocation().getDirection()), Effect.SMOKE, 4);
		player.getWorld().playSound(player.getEyeLocation(), Sound.EXPLODE, 0.6f, 2f);

		while (travel < maxTravel)
		{
			Location loc = player.getEyeLocation().add(player.getLocation().getDirection().multiply(travel));
			for (Entity ent : player.getWorld().getEntities())
			{
				if (!(ent instanceof LivingEntity))
					continue;

				LivingEntity cur = (LivingEntity)ent;

				if (cur.equals(player))
					continue;

				if (cur instanceof Player)
				{
					if (UtilMath.offset(loc, ((Player)cur).getEyeLocation()) < 0.3)
					{
						rifleHit(cur, player, true);
						player.getWorld().playSound(loc, Sound.BLAZE_HIT, 0.4f, 2f);
						return;
					}
					else if (UtilMath.offset2d(loc, cur.getLocation()) < hitBox)
					{
						if (loc.getY() > cur.getLocation().getY() && loc.getY() < cur.getEyeLocation().getY())
						{
							rifleHit(cur, player, false);
							player.getWorld().playSound(loc, Sound.BLAZE_HIT, 0.4f, 2f);
							return;
						}		
					}
				}
				else 
				{
					if (UtilMath.offset(loc, cur.getEyeLocation()) < 0.3)
					{
						rifleHit(cur, player, true);
						player.getWorld().playSound(loc, Sound.BLAZE_HIT, 0.4f, 2f);
						return;
					}
					else if (UtilMath.offset2d(loc, cur.getLocation()) < hitBox)
					{
						if (loc.getY() > cur.getLocation().getY() && loc.getY() < cur.getLocation().getY() + 1)
						{
							rifleHit(cur, player, false);
							player.getWorld().playSound(loc, Sound.BLAZE_HIT, 0.4f, 2f);
							return;
						}		
					}
				}

				if (UtilMath.offset2d(loc, cur.getLocation()) < hitBox)
				{
					if (loc.getY() > cur.getLocation().getY() && loc.getY() < cur.getEyeLocation().getY())
					{
						rifleHit(cur, player, false);
						player.getWorld().playSound(loc, Sound.BLAZE_HIT, 0.4f, 2f);
						return;
					}		
				}
			}	

			if (UtilBlock.solid(loc.getBlock()))
			{
				loc.getBlock().getWorld().playEffect(loc, Effect.STEP_SOUND, loc.getBlock().getTypeId());
				player.getWorld().playSound(player.getLocation(), Sound.BLAZE_HIT, 0.4f, 2f);
				return;
			}

			travel += sharpness;
		}
	}

	public void rifleHit(LivingEntity hit, Player attacker, boolean headshot) 
	{
		/*
		int damage = 12;
		if (headshot)
		{
			if (hit instanceof Player)
			{
				Player damagee = (Player)hit;
				if (Factory.Clans().CUtil().canHurt(damagee, attacker))
				{
					if (damagee.getInventory().getHelmet() == null)
					{
						//Damage
						damage = 500;
						damagee.getWorld().playEffect(damagee.getEyeLocation(), Effect.STEP_SOUND, 55);

						//Inform
						UtilServer.broadcast(F.main("Item", F.name(((Player)hit).getName()) + " was headshotted by " + F.name(attacker.getName()) + " with " + F.item("Rifle") + "."));
					}
					else
					{
						int id = 55;
						if (damagee.getInventory().getHelmet().getType() == Material.IRON_HELMET)	id = 42;
						if (damagee.getInventory().getHelmet().getType() == Material.GOLD_HELMET)	id = 41;
						if (damagee.getInventory().getHelmet().getType() == Material.DIAMOND_HELMET)	id = 57;
						if (damagee.getInventory().getHelmet().getType() == Material.CHAINMAIL_HELMET)	id = 4;
						if (damagee.getInventory().getHelmet().getType() == Material.LEATHER_HELMET)	id = 17;
						damagee.getWorld().playEffect(damagee.getEyeLocation(), Effect.STEP_SOUND, id);	
							
						Item item = damagee.getWorld().dropItemNaturally(damagee.getEyeLocation(), damagee.getInventory().getHelmet());
						item.setPickupDelay(60);
						damagee.getInventory().setHelmet(null);
					}
				}
			}
			else
			{
				hit.getWorld().playEffect(hit.getEyeLocation(), Effect.STEP_SOUND, 55);
				damage = 500;
			}
		}

		if (damage > 0)
			return;
		//UtilPlayer.Damage(hit, attacker, damage, getName(), true, true, false);
		*/
	}

	@Override
	public void Reset(Player player)
	{
	
	}
}
