package nautilus.game.arcade.game.games.wizards.spells;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilMath;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.projectile.IThrown;
import mineplex.core.projectile.ProjectileUser;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.game.games.wizards.Spell;
import nautilus.game.arcade.game.games.wizards.spellinterfaces.SpellClick;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class SpellIcePrison extends Spell implements SpellClick, IThrown
{

	private HashMap<Block, Long> _prisonExpires = new HashMap<Block, Long>();

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event)
	{
		Block block = event.getBlock();

		if (_prisonExpires.containsKey(block))
		{
			event.setCancelled(true);
			block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getTypeId());
			block.setType(Material.AIR);
		}
	}

	@EventHandler
	public void onBlockMelt(BlockFadeEvent event)
	{
		Block block = event.getBlock();

		if (_prisonExpires.containsKey(block))
		{
			event.setCancelled(true);
			block.setType(Material.AIR);
		}
	}

	@Override
	public void castSpell(final Player player)
	{
		shoot(player, getSpellLevel(player));

		charge(player);
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data)
	{
		if (target != data.getThrower())
		{
			IcePrison(data);
		}
	}

	@Override
	public void Expire(ProjectileUser data)
	{
		IcePrison(data);
	}

	public void IcePrison(ProjectileUser data)
	{
		Location loc = data.getThrown().getLocation();
		data.getThrown().remove();

		HashMap<Block, Double> blocks = UtilBlock.getInRadius(loc.getBlock(),
				data.getThrown().getMetadata("PrisonStrength").get(0).asDouble(), true);

		for (Block block : blocks.keySet())
		{
			if (_prisonExpires.containsKey(block) || UtilBlock.airFoliage(block))
			{
				block.setType(Material.ICE);

				_prisonExpires.put(block, System.currentTimeMillis() + ((20 + UtilMath.r(10)) * 1000L));
			}
		}

		// Effect
		loc.getWorld().playSound(loc, Sound.SILVERFISH_HIT, 2f, 1f);
	}

	@Override
	public void Idle(ProjectileUser data)
	{
		IcePrison(data);
	}
	
	@Override
	public void ChunkUnload(ProjectileUser data)
	{
		data.getThrown().remove();
	}

	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		Iterator<Entry<Block, Long>> itel = _prisonExpires.entrySet().iterator();

		while (itel.hasNext())
		{
			Entry<Block, Long> entry = itel.next();

			if (entry.getValue() < System.currentTimeMillis())
			{
				itel.remove();

				if (entry.getKey().getType() == Material.ICE)
				{
					entry.getKey().setType(Material.AIR);
				}
			}
		}
	}

	private void shoot(Player player, int spellLevel)
	{

		if (Wizards.IsAlive(player))
		{
			org.bukkit.entity.Item ent = player.getWorld().dropItem(
					player.getEyeLocation(),
					ItemStackFactory.Instance.CreateStack(Material.PACKED_ICE, (byte) 0, 1, "Ice Prison" + player.getName() + " "
							+ System.currentTimeMillis()));

			ent.setMetadata("PrisonStrength", new FixedMetadataValue(Wizards.getArcadeManager().getPlugin(), 3 + spellLevel));

			UtilAction.velocity(ent, player.getLocation().getDirection(), 1.7, false, 0, 0.2, 10, false);
			Wizards.getArcadeManager().GetProjectile().AddThrow(ent, player, this, -1, true, true, true, false, 2f);

			player.getWorld().playSound(player.getLocation(), Sound.CREEPER_HISS, 1.2F, 0.8F);
		}
	}
}