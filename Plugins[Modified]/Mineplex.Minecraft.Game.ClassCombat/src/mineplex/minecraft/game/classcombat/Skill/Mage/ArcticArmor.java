package mineplex.minecraft.game.classcombat.Skill.Mage;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerDropItemEvent;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilGear;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.classcombat.Class.IPvpClass.ClassType;
import mineplex.minecraft.game.classcombat.Skill.Skill;
import mineplex.minecraft.game.classcombat.Skill.SkillFactory;
import mineplex.minecraft.game.classcombat.Skill.event.SkillTriggerEvent;

public class ArcticArmor extends Skill
{

	private static final double DELTA_THETA = Math.PI / 10;
	private static final double FENCE_FROM_CENTER_DIST = 0.95;

	private final Set<Player> _active = new HashSet<>();

	public ArcticArmor(SkillFactory skills, String name, ClassType classType, SkillType skillType, int cost, int levels)
	{
		super(skills, name, classType, skillType, cost, levels);

		SetDesc(new String[]
				{
						"Drop Axe/Sword to Toggle.",
						"",
						"Create a freezing area around you",
						"in a #3#1 Block radius. Allies inside",
						"this area receive Protection 2.",
						"",
						"You receive Protection 2."
				});
	}

	@Override
	public String GetEnergyString()
	{
		return "Energy: #11#-1 per Second";
	}

	@EventHandler
	public void Toggle(PlayerDropItemEvent event)
	{
		Player player = event.getPlayer();

		if (getLevel(player) == 0)
			return;

		if (!UtilGear.isWeapon(event.getItemDrop().getItemStack()))
			return;

		event.setCancelled(true);

		//Check Allowed
		SkillTriggerEvent trigger = new SkillTriggerEvent(player, GetName(), GetClassType());
		UtilServer.getServer().getPluginManager().callEvent(trigger);
		if (trigger.IsCancelled())
			return;

		if (_active.contains(player))
		{
			Remove(player);
		}
		else
		{
			if (!Factory.Energy().Use(player, "Enable " + GetName(), 10, true, true))
				return;

			Add(player);
		}
	}

	public void Add(Player player)
	{
		_active.add(player);
		UtilPlayer.message(player, F.main(GetClassType().name(), F.skill(GetName()) + ": " + F.oo("Enabled", true)));
	}

	public void Remove(Player player)
	{
		_active.remove(player);
		UtilPlayer.message(player, F.main(GetClassType().name(), F.skill(GetName()) + ": " + F.oo("Disabled", false)));
		Factory.Condition().EndCondition(player, null, GetName());
	}

	@EventHandler
	public void Audio(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		for (Player cur : _active)
			cur.getWorld().playSound(cur.getLocation(), Sound.AMBIENCE_RAIN, 0.3f, 0f);
	}

	@EventHandler
	public void SnowAura(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Player cur : GetUsers())
		{
			if (!_active.contains(cur))
				continue;

			//Level
			int level = getLevel(cur);
			if (level == 0)
			{
				Remove(cur);
				continue;
			}

			//Check Allowed
			SkillTriggerEvent trigger = new SkillTriggerEvent(cur, GetName(), GetClassType());
			UtilServer.getServer().getPluginManager().callEvent(trigger);
			if (trigger.IsCancelled())
			{
				Remove(cur);
				continue;
			}

			//Energy
			if (!Factory.Energy().Use(cur, GetName(), 0.55 - (level * 0.05), true, true))
			{
				Remove(cur);
				continue;
			}

			//Blocks
			Location location = cur.getLocation();
			double duration = 2000;
			int radius = 3 + level;

			UtilBlock.getInRadius(location, radius).forEach((block, scale) ->
			{
				Block up = block.getRelative(BlockFace.UP);

				if (block.getType().toString().contains("BANNER") || up.isLiquid() || block.getLocation().getY() > cur.getLocation().getY())
				{
					return;
				}

				//Freeze
				Material type = block.getType();

				if (UtilBlock.water(type) || type == Material.ICE)
				{
					for (Player player : block.getWorld().getPlayers())
					{
						if (UtilPlayer.isSpectator(player))
						{
							continue;
						}

						Location playerLocation = player.getLocation();
						Block playerBlock = playerLocation.getBlock();
						Location moveTo = null;

						// Force players up if they try and get themselves stuck
						if (playerBlock.equals(block))
						{
							moveTo = playerLocation.add(0, 1, 0);
						}
						else
						{
							Location blockLocation = block.getLocation().add(0.5, 0, 0.5);

							for (BlockFace face : UtilBlock.horizontals)
							{
								Block nextBlock = block.getRelative(face);

								if (UtilBlock.isFence(nextBlock) && playerBlock.equals(nextBlock) && Math.abs(playerLocation.getX() - blockLocation.getX()) < FENCE_FROM_CENTER_DIST && Math.abs(playerLocation.getZ() - blockLocation.getZ()) < FENCE_FROM_CENTER_DIST)
								{
									moveTo = block.getLocation().add(0.5, 1, 0.5);

									moveTo.setYaw(playerLocation.getYaw());
									moveTo.setPitch(playerLocation.getPitch());
									break;
								}
							}
						}

						// Only move the player if they are being moved into air
						if (moveTo != null && moveTo.getBlock().getType() == Material.AIR)
						{
							player.teleport(moveTo);
						}
					}

					Factory.BlockRestore().add(block, 79, (byte) 0, (long) (duration * (1 + scale)));
				}
			});

			location.add(0, 0.1, 0);

			for (double t = 0; t < 2 * Math.PI; t += DELTA_THETA)
			{
				double x = radius * Math.cos(t), z = radius * Math.sin(t);

				location.add(x, 0, z);

				UtilParticle.PlayParticleToAll(ParticleType.SNOW_SHOVEL, location, 0.5F, 0.5F, 0.5F, 0, 1, ViewDist.LONG);

				location.subtract(x, 0, z);
			}
		}
	}

	@EventHandler
	public void ProtectionAura(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}

		for (Player cur : _active)
		{
			//Protection
			for (Player other : UtilPlayer.getNearby(cur.getLocation(), 3 + getLevel(cur)))
			{
				if (!Factory.Relation().canHurt(cur, other) || other.equals(cur))
				{
					Factory.Condition().Factory().Protection(GetName(), other, cur, 1.9, 1, false, true, true);
				}
			}
		}
	}

	@EventHandler
	public void Damage(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		for (Player cur : UtilServer.getPlayers())
		{
			if (UtilPlayer.isSpectator(cur))
				continue;

			if (cur.getEyeLocation().getBlock().getType() == Material.ICE)
			{
				if (!Factory.BlockRestore().contains(cur.getEyeLocation().getBlock()))
					continue;

				Player damager = null;
				double closest = 0;

				for (Player player : _active)
				{
					if (player.equals(cur))
						continue;

					double dist = UtilMath.offsetSquared(player, cur);

					if (dist < 25 && (damager == null || dist < closest))
					{
						damager = player;
						closest = dist;
					}
				}

				//Damage Event
				Factory.Damage().NewDamageEvent(cur, damager, null,
						DamageCause.CUSTOM, 2, false, true, false,
						damager == null ? null : damager.getName(), "Arctic Ice");
			}
		}
	}

	@Override
	public void Reset(Player player)
	{
		_active.remove(player);
	}
}
