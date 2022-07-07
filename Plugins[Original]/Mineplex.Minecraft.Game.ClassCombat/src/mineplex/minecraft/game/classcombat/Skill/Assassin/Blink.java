package mineplex.minecraft.game.classcombat.Skill.Assassin;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilWorld;
import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.classcombat.Skill.SkillActive;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;
import mineplex.minecraft.game.classcombat.Skill.event.SkillTeleportEvent;
import net.minecraft.server.v1_8_R3.AxisAlignedBB;
import net.minecraft.server.v1_8_R3.Entity;

public class Blink extends SkillActive
{
	private Map<Player, Location> _loc = new HashMap<>();
	private Map<Player, Long> _blinkTime = new HashMap<>();
	private boolean _allowTrapping = false;
	
	public Blink(SkillFactory skills, String name, ClassType classType, SkillType skillType, 
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
				"Instantly teleport forwards #9#3 Blocks.",
				"Cannot be used while Slowed.",
				"",
				"Using again within #6#-1 seconds De-Blinks,",
				"returning you to your original location.",
				"Cannot be used while Slowed."
				});
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean CustomCheck(Player player, int level) 
	{
		if (player.hasPotionEffect(PotionEffectType.SLOW))
		{
			UtilPlayer.message(player, F.main(GetClassType().name(), "You cannot use " + F.skill(GetName()) + " while Slowed."));
			return false;
		}
		else if (isInWater(player))
		{
			UtilPlayer.message(player, F.main("Skill", "You cannot use " + F.skill(GetName()) + " in water."));
			return false;
		}
		
		// Check to see if teleporting event is valid
		Location destination = getDestination(player, getMaxRange(player));
		SkillTeleportEvent teleportEvent = new SkillTeleportEvent(player, destination);
		Bukkit.getPluginManager().callEvent(teleportEvent);
		
		if (teleportEvent.isCancelled()) return false;	// Teleport cancelled

		//Deblink
		if (_loc.containsKey(player) && _blinkTime.containsKey(player))
		{
			long time = _blinkTime.get(player);

			if (!UtilTime.elapsed(time, 6000 - (1000 * level)))
			{
				//Require 500ms after blink to deblink
				if (UtilTime.elapsed(time, 500))
					Deblink(player, level);
				
				return false;
			}
		}

		return true;
	}
	
	public void blinky(Player player)
	{
		Location start = player.getLocation().add(new Vector(0, 0.2, 0));
		Location test = null;
		Location end = start.clone();
		Vector dir = player.getLocation().getDirection();
		AxisAlignedBB box = null;
		Entity ent = ((CraftEntity)player).getHandle();
		double maxRange = getMaxRange(player);
		double range = 0;
		
		rangeLoop: while (range <= maxRange)
		{
			test = start.clone().add(dir.clone().multiply(range));
			float halfWidth = ent.width / 2;
			float length = ent.length;
			box = new AxisAlignedBB(test.getX() - halfWidth, test.getY(), test.getZ() - halfWidth, test.getX() + halfWidth, test.getY() + length, test.getZ() + halfWidth);
			
			//Lock Players
			playerLoop: for (Player cur : player.getWorld().getPlayers())
			{
				if (cur.equals(player) || cur.getGameMode() == GameMode.SPECTATOR || UtilPlayer.isSpectator(cur))
				{
					continue playerLoop;
				}

				if (UtilMath.offset(test, cur.getLocation()) > 1)
				{
					continue playerLoop;
				}

				//Action
				Location target = cur.getLocation().add(player.getLocation().subtract(cur.getLocation()).toVector().normalize());
				player.teleport(UtilWorld.locMerge(player.getLocation(), target));

				//Inform
				UtilPlayer.message(player, F.main(GetClassType().name(), "You used " + F.skill(GetName()) + "."));

				//Effect
				player.getWorld().playEffect(player.getLocation(), Effect.BLAZE_SHOOT, 0);
				return;
			}
			
			for (Block b : UtilBlock.getInBoundingBox(player.getWorld(), box))
			{
				if (UtilBlock.airFoliage(b))
				{
					if (b.getType() == Material.STRING && _allowTrapping)
					{
						break rangeLoop;
					}
				}
				else
				{
					break rangeLoop;
				}
			}
			
			range += 0.2;
			end = test.clone();
			//Smoke Trail
			UtilParticle.PlayParticle(ParticleType.LARGE_SMOKE, end.clone().add(0, 0.5, 0), 0, 0, 0, 0, 1,
					ViewDist.LONGER, UtilServer.getPlayers());
		}
		
		_loc.put(player, player.getLocation());
		
		if (range > 0)
		{
			player.teleport(end);
		}
		player.setFallDistance(0);
	}

	@Override
	public void Skill(Player player, int level) 
	{
		Location start = player.getLocation().add(new Vector(0, 0.2, 0));
		Location test = null;
		Location end = start.clone();
		Vector dir = player.getLocation().getDirection();
		AxisAlignedBB box = null;
		Entity ent = ((CraftEntity)player).getHandle();
		double maxRange = getMaxRange(player);
		double range = 0;
		
		rangeLoop: while (range <= maxRange)
		{
			test = start.clone().add(dir.clone().multiply(range));
			float halfWidth = ent.width / 2;
			float length = ent.length;
			box = new AxisAlignedBB(test.getX() - halfWidth, test.getY(), test.getZ() - halfWidth, test.getX() + halfWidth, test.getY() + length, test.getZ() + halfWidth);
			
			//Lock Players
			playerLoop: for (Player cur : player.getWorld().getPlayers())
			{
				if (cur.equals(player) || cur.getGameMode() == GameMode.SPECTATOR || UtilPlayer.isSpectator(cur))
				{
					continue playerLoop;
				}

				if (UtilMath.offset(test, cur.getLocation()) > 1)
				{
					continue playerLoop;
				}

				//Action
				Location target = cur.getLocation().add(player.getLocation().subtract(cur.getLocation()).toVector().normalize());
				player.teleport(UtilWorld.locMerge(player.getLocation(), target));

				//Inform
				UtilPlayer.message(player, F.main(GetClassType().name(), "You used " + F.skill(GetName()) + "."));

				//Effect
				player.getWorld().playEffect(player.getLocation(), Effect.BLAZE_SHOOT, 0);
				return;
			}
			
			for (Block b : UtilBlock.getInBoundingBox(player.getWorld(), box))
			{
				if (UtilBlock.airFoliage(b))
				{
					if (b.getType() == Material.TRIPWIRE && _allowTrapping)
					{
						break rangeLoop;
					}
				}
				else
				{
					break rangeLoop;
				}
			}
			
			range += 0.2;
			end = test.clone();
			//Smoke Trail
			UtilParticle.PlayParticle(ParticleType.LARGE_SMOKE, end.clone().add(0, 0.5, 0), 0, 0, 0, 0, 1,
					ViewDist.LONGER, UtilServer.getPlayers());
		}
		
		_loc.put(player, player.getLocation());
		
		if (range > 0)
		{
			player.teleport(end);
		}
		
		player.setFallDistance(0);

		//Inform
		UtilPlayer.message(player, F.main(GetClassType().name(), "You used " + F.skill(GetName()) + "."));

		//Effect
		player.getWorld().playEffect(player.getLocation(), Effect.BLAZE_SHOOT, 0);

		//Record
		_blinkTime.put(player, System.currentTimeMillis());
	}

	public void Deblink(Player player, int level)
	{
		Location target = _loc.remove(player);
		if (!player.getWorld().equals(target.getWorld()))
		{
			UtilPlayer.message(player, F.main(GetClassType().name(), "You cannot use " + F.skill("De-Blink") + " between worlds!"));
			return;
		}
		UtilPlayer.message(player, F.main(GetClassType().name(), "You used " + F.skill("De-Blink") + "."));

		//Smoke Trail
		Block lastSmoke = player.getLocation().getBlock();

		double curRange = 0;

		boolean done = false;
		while (!done)
		{
			Vector vec = UtilAlg.getTrajectory(player.getLocation(), 
					new Location(player.getWorld(), target.getX(), target.getY(), target.getZ()));

			Location newTarget = player.getLocation().add(vec.multiply(curRange));

			//Progress Forwards
			curRange += 0.2;

			//Smoke Trail
			UtilParticle.PlayParticle(ParticleType.LARGE_SMOKE, newTarget.clone().add(0, 0.5, 0), 0, 0, 0, 0, 1,
					ViewDist.LONGER, UtilServer.getPlayers());
				
			lastSmoke = newTarget.getBlock();

			if (UtilMath.offset(newTarget, target) < 0.4)
						done = true;
					
					if (curRange > 24)
				done = true;

			if (curRange > 24)
				done = true;
		}

		target.setYaw(player.getLocation().getYaw());
		target.setPitch(player.getLocation().getPitch());
		
		player.teleport(target);
		
		player.setFallDistance(0);

		//Effect
		player.getWorld().playEffect(player.getLocation(), Effect.BLAZE_SHOOT, 0);
	}

	@Override
	public void Reset(Player player) 
	{
		_loc.remove(player);
		_blinkTime.remove(player);
	}
	
	private double getMaxRange(Player player)
	{
		return 9 + (getLevel(player) * 3);
	}
	
	private Location getDestination(Player player, double range)
	{
		return player.getLocation().add(player.getLocation().getDirection().multiply(range).add(new Vector(0, 0.4, 0)));
	}
	
	public boolean isAllowTrapping()
	{
		return _allowTrapping;
	}
	
	public void setAllowTrapping(boolean allowTrapping)
	{
		_allowTrapping = allowTrapping;
	}
}