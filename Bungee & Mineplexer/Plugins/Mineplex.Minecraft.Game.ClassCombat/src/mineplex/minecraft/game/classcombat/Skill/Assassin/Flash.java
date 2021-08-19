package mineplex.minecraft.game.classcombat.Skill.Assassin;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.server.v1_8_R3.AxisAlignedBB;
import net.minecraft.server.v1_8_R3.Entity;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.classcombat.Skill.SkillActive;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;
import mineplex.minecraft.game.classcombat.Skill.event.SkillTeleportEvent;

public class Flash extends SkillActive
{

	private final Map<Player, Integer> _flash = new HashMap<>();
	private boolean _allowTrapping = false, _startWithCharges = true;

	public Flash(SkillFactory skills, String name, ClassType classType, SkillType skillType,
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
						"Teleport forwards 6 Blocks.",
						"Store up to #1#1 Flash Charges.",
						"Cannot be used while Slowed."
				});
	}

	@Override
	public String GetRechargeString()
	{
		return "Recharge: 4 Seconds per Charge";
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

		if (isInWater(player))
		{
			UtilPlayer.message(player, F.main("Skill", "You cannot use " + F.skill(GetName()) + " in water."));
			return false;
		}

		// Check to see if teleporting event is valid
		Location destination = getDestination(player, getMaxRange(player));
		SkillTeleportEvent teleportEvent = new SkillTeleportEvent(player, destination);
		Bukkit.getPluginManager().callEvent(teleportEvent);

		if (teleportEvent.isCancelled()) return false;    // Teleport cancelled

		//No Flash
		_flash.putIfAbsent(player, _startWithCharges ? 1 + level : 1);

		if (_flash.get(player) == 0)
		{
			UtilPlayer.message(player, F.main("Skill", "You have no " + F.skill(GetName() + " Charges") + "."));
			return false;
		}

		return true;
	}

	@EventHandler
	public void recharge(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		_flash.entrySet().forEach(entry ->
		{
			Player cur = entry.getKey();
			int charges = entry.getValue();
			int level = getLevel(cur);

			if (charges >= 1 + level || !Recharge.Instance.use(cur, "Flash Recharge", 4000, false, false))
			{
				return;
			}

			entry.setValue(charges + 1);

			//Inform
			UtilPlayer.message(cur, F.main(GetClassType().name(), "Flash Charges: " + F.elem((charges + 1) + "")));
		});
	}

	@SuppressWarnings("deprecation")
	@Override
	public void Skill(Player player, int level)
	{
		//Use Recharge
		Recharge.Instance.use(player, "Flash Recharge", 8000, false, false);

		_flash.put(player, _flash.get(player) - 1);

		//Inform
		UtilPlayer.message(player, F.main(GetClassType().name(), "Flash Charges: " + F.elem(_flash.get(player) + "")));

		Location start = player.getLocation().add(new Vector(0, 0.2, 0));
		Location test;
		Location end = start.clone();
		Vector dir = player.getLocation().getDirection();
		AxisAlignedBB box;
		Entity ent = ((CraftEntity) player).getHandle();
		double maxRange = getMaxRange(player);
		double range = 0;

		rangeLoop:
		while (range <= maxRange)
		{
			test = start.clone().add(dir.clone().multiply(range));
			float halfWidth = ent.width / 2;
			float length = ent.length;
			box = new AxisAlignedBB(test.getX() - halfWidth, test.getY(), test.getZ() - halfWidth, test.getX() + halfWidth, test.getY() + length, test.getZ() + halfWidth);
			for (Block b : UtilBlock.getInBoundingBox(player.getWorld(), box))
			{
				if (b.getType() == Material.STEP || b.getType() == Material.WOOD_STEP || b.getType() == Material.STONE_SLAB2)
				{
					boolean bottom = false;
					int data = b.getData();
					if (data <= 7)
					{
						bottom = true;
					}

					boolean locIsBottom = (Math.max(test.getY(), b.getY()) - Math.min(test.getY(), b.getY())) < 0.5;

					if (bottom == locIsBottom)
					{
						break rangeLoop;
					}
				}
				else if (UtilItem.isBoundless(b.getType()))
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

			range += 0.1;
			end = test.clone();
			//Smoke Trail
			UtilParticle.PlayParticleToAll(ParticleType.FIREWORKS_SPARK, end.clone().add(0, 0.5, 0), 0, 0, 0, 0, 1, ViewDist.LONG);
		}

		if (range > 0)
		{
			player.teleport(end);
		}

		player.setFallDistance(0);

		//Effect
		player.getWorld().playSound(player.getLocation(), Sound.WITHER_SHOOT, 0.4f, 1.2f);
		player.getWorld().playSound(player.getLocation(), Sound.SILVERFISH_KILL, 1f, 1.6f);
	}

	@Override
	public void Reset(Player player)
	{
		_flash.remove(player);
	}

	private double getMaxRange(Player player)
	{
		return 6;
	}

	private Location getDestination(Player player, double range)
	{
		return player.getLocation().add(player.getLocation().getDirection().multiply(range).add(new Vector(0, 0.4, 0)));
	}

	public void setAllowTrapping(boolean allowTrapping)
	{
		_allowTrapping = allowTrapping;
	}

	public void setStartWithCharges(boolean startWithCharges)
	{
		_startWithCharges = startWithCharges;
	}
}