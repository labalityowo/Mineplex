package nautilus.game.arcade.game.games.wizards.spells;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import com.google.common.base.Optional;
import mineplex.core.common.util.SpigotUtil;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.wizards.Spell;
import nautilus.game.arcade.game.games.wizards.spellinterfaces.SpellClick;
import nautilus.game.arcade.game.games.wizards.spellinterfaces.SpellClickBlock;

import net.minecraft.server.v1_8_R3.EntityWolf;
import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftWolf;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;

public class SpellSummonWolves extends Spell implements SpellClick, SpellClickBlock
{

	private HashMap<Wolf, Long> _summonedWolves = new HashMap<Wolf, Long>();

	@Override
	public void castSpell(Player player, Block block)
	{
		block = block.getRelative(BlockFace.UP);

		if (!UtilBlock.airFoliage(block))
		{
			block = player.getLocation().getBlock();
		}

		Location loc = block.getLocation().add(0.5, 0, 0.5);

		for (int i = 0; i < getSpellLevel(player); i++)
		{
			Wizards.CreatureAllowOverride = true;

			Wolf wolf = (Wolf) player.getWorld().spawnEntity(
					loc.clone().add(new Random().nextFloat() - 0.5F, 0, new Random().nextFloat() - 0.5F), EntityType.WOLF);

			Wizards.CreatureAllowOverride = false;

			wolf.setCollarColor(DyeColor.YELLOW);
			wolf.setTamed(true);
			SpigotUtil.setOldOwner_RemoveMeWhenSpigotFixesThis(wolf, player);
			wolf.setOwner(player);
			wolf.setBreed(false);
			wolf.setCustomName(player.getDisplayName() + "'s Wolf");
			wolf.setRemoveWhenFarAway(false);
			wolf.setMaxHealth(0.5);
			wolf.setHealth(0.5);

			_summonedWolves.put(wolf, System.currentTimeMillis() + (30L * 1000L));
		}

		UtilParticle.PlayParticle(ParticleType.LARGE_EXPLODE, loc, 0.8F, 0, 0.8F, 0, 4,
				ViewDist.LONG, UtilServer.getPlayers());
		player.getWorld().playSound(player.getLocation(), Sound.BAT_TAKEOFF, 1.2F, 1);
		charge(player);
	}

	@EventHandler
	public void onDamage(CustomDamageEvent event)
	{
		if (event.GetDamagerEntity(false) instanceof Wolf)
		{
			Wolf wolf = (Wolf) event.GetDamagerEntity(false);
			event.AddMult("Summoned Wolf", "Summoned Wolf", 0.3, true);

			AnimalTamer tamer = wolf.getOwner();
			if (tamer instanceof Player)
			{
				event.SetDamager((Player) tamer);
				event.setMetadata("customWeapon", "Summon Wolves");
				event.setKnockbackOrigin(wolf.getLocation());
			}
		}
	}

	@EventHandler
	public void onSecond(UpdateEvent event)
	{
		if (event.getType() == UpdateType.SEC)
		{

			Iterator<Wolf> itel = _summonedWolves.keySet().iterator();

			while (itel.hasNext())
			{
				Wolf wolf = itel.next();
				AnimalTamer wolfOwner = wolf.getOwner();

				if (!wolf.isValid() || _summonedWolves.get(wolf) < System.currentTimeMillis() || !(wolfOwner instanceof Player)
						|| !Wizards.IsAlive((Entity) wolfOwner))
				{
					if (wolf.isValid())
					{
						wolf.getWorld().playEffect(wolf.getLocation(), Effect.EXPLOSION_HUGE, 0);
					}

					wolf.remove();
					itel.remove();
				}
				else
				{

					if (wolf.getTarget() == null || !wolf.getTarget().isValid() || !Wizards.IsAlive(wolf.getTarget())
							|| wolf.getTarget().getLocation().distance(wolf.getLocation()) > 16)
					{

						double dist = 0;
						Player target = null;

						for (Player player : Wizards.GetPlayers(true))
						{

							if (!player.equals(wolfOwner))
							{

								double newDist = player.getLocation().distance(wolf.getLocation());

								if (newDist < 16 && (target == null || dist > newDist))
								{
									dist = newDist;
									target = player;
								}
							}
						}

						if (target != null)
						{
							wolf.setTarget(target);
						}
						else
						{
							Location loc = ((Player) wolfOwner).getLocation();

							if (loc.distance(wolf.getLocation()) > 16)
							{
								wolf.teleport(loc);
							}
						}

					}
				}
			}

		}
	}

	@Override
	public void castSpell(Player player)
	{
		castSpell(player, player.getLocation().getBlock().getRelative(BlockFace.DOWN));
	}
}
