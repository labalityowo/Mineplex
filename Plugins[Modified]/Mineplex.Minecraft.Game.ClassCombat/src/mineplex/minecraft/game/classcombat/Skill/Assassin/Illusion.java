package mineplex.minecraft.game.classcombat.Skill.Assassin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import com.mojang.authlib.GameProfile;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.disguise.disguises.DisguisePlayer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.utils.UtilGameProfile;
import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.classcombat.Skill.SkillActive;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;
import mineplex.minecraft.game.classcombat.event.ClassCombatCreatureAllowSpawnEvent;
import mineplex.minecraft.game.core.condition.Condition.ConditionType;

public class Illusion extends SkillActive
{
	private HashMap<Player, Skeleton> _active = new HashMap<Player, Skeleton>();

	public Illusion(SkillFactory skills, String name, ClassType classType, SkillType skillType, 
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
				"Hold Block to go invisible and create an",
				"illusion of yourself that runs towards",
				"your target location.",
				"",
				"Invisibility ends if you release Block",
				"or your Illusion is killed.",
				"",
				"Illusion lasts up to #2#1 seconds.",
				"",
				"Gives Slow 2 for up to 4 seconds",
				"to nearby players upon ending."
				});
		
		setAchievementSkill(true);
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
		//Spawn
		ClassCombatCreatureAllowSpawnEvent enableEvent = new ClassCombatCreatureAllowSpawnEvent(player.getWorld().getName(), true);
		UtilServer.getServer().getPluginManager().callEvent(enableEvent);
		
		Skeleton skel = player.getWorld().spawn(player.getLocation(), Skeleton.class);
		skel.teleport(player.getLocation());
		UtilEnt.vegetate(skel);
		UtilEnt.silence(skel, true);
		
		skel.setMaxHealth(14);
		skel.setHealth(14);
		
		ClassCombatCreatureAllowSpawnEvent disableEvent = new ClassCombatCreatureAllowSpawnEvent(player.getWorld().getName(), false);
		UtilServer.getServer().getPluginManager().callEvent(disableEvent);
		
		skel.getEquipment().setHelmet(player.getInventory().getHelmet());
		skel.getEquipment().setChestplate(player.getInventory().getChestplate());
		skel.getEquipment().setLeggings(player.getInventory().getLeggings());
		skel.getEquipment().setBoots(player.getInventory().getBoots());
		skel.getEquipment().setItemInHand(Math.random() > 0.5 ? player.getItemInHand() : new ItemStack(Material.IRON_AXE));

		//Get in range
		ArrayList<UUID> inRange = new ArrayList<>();
		for (Player other : UtilServer.getPlayers())
			if (UtilMath.offset2d(skel, other) < 70)
				inRange.add(other.getUniqueId());
				
		//Disguise
		GameProfile profile = UtilGameProfile.getGameProfile(player);

		try
		{
			UtilGameProfile.changeId(profile, UUID.randomUUID());
			DisguisePlayer disguise = new DisguisePlayer(skel, profile);
			Factory.Disguise().disguise(disguise, attempted -> inRange.contains(attempted.getUniqueId()));
		}
		catch (ReflectiveOperationException ex)
		{
			ex.printStackTrace();
		}

		//Invis
		Factory.Condition().Factory().untrueCloak(GetName(), player, player, 2 + 1*level, false);
		
		_active.put(player, skel);

		//Inform
		UtilPlayer.message(player, F.main(GetClassType().name(), "You used " + F.skill(GetName()) + "."));
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Player cur : GetUsers())
		{
			if (!_active.containsKey(cur))
				continue;

			Skeleton skel = _active.get(cur);
			
			if (Factory.Condition().GetActiveCondition(cur, ConditionType.UNTRUE_CLOAK) == null ||
				!UtilPlayer.isBlocking(cur) || 
				//!Factory.Energy().Use(cur, getName(), 0.625 - (getLevel(cur) * 0.025), true, true) ||
				skel == null ||
				!skel.isValid())
			{
				end(cur);
				continue;
			}
			else
			{
				if (UtilEnt.isGrounded(skel) &&
					(!UtilBlock.airFoliage(skel.getLocation().add(skel.getLocation().getDirection()).getBlock()) ||
					!UtilBlock.airFoliage(skel.getLocation().add(skel.getLocation().getDirection().multiply(2)).getBlock())))
				{
					UtilAction.velocity(skel, 0.6, 0.4, 1, false);
				}
				
				UtilEnt.CreatureMoveFast(skel, cur.getTargetBlock((HashSet<Byte>)null, 30).getLocation().add(0, 2, 0), 1.8f);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void illusionDeath(EntityDeathEvent event)
	{
		if (_active.containsValue(event.getEntity()))
		{
			event.getDrops().clear();
			event.getEntity().remove();
		}
	}
	
	private void end(Player player)
	{
		Factory.Condition().EndCondition(player, null, GetName());
		
		Skeleton skel = _active.remove(player);
		if (skel == null)
			return;
					
		//Level
		int level = getLevel(player);
		
		//Blind
		HashMap<LivingEntity, Double> targets = UtilEnt.getInRadius(skel.getLocation(), 6d + 0.5 * level);
		for (LivingEntity cur : targets.keySet())
		{
			if (cur.equals(player))
				continue;
			
			if (cur instanceof Player && !Factory.Relation().canHurt(player, (Player)cur))
			{
				continue;
			}

			//Condition
			Factory.Condition().Factory().Slow(GetName(), cur, player, 4 * targets.get(cur), 1, false, false, false, false);
		}
		
		//Effect
		UtilParticle.PlayParticle(ParticleType.LARGE_SMOKE, skel.getLocation().add(0, 1, 0), 0.3f, 0.3f, 0.3f, 0.06f, 30,
				ViewDist.LONGER, UtilServer.getPlayers());
		
		for (int i=0 ; i<2 ; i++)
			skel.getWorld().playSound(skel.getLocation(), Sound.FIZZ, 2f, 0.4f);
		
		skel.getEquipment().clear();
		skel.remove();
	}

	@Override
	public void Reset(Player player) 
	{
		end(player);
	}
}
