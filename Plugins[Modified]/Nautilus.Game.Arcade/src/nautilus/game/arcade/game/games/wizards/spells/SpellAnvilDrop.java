package nautilus.game.arcade.game.games.wizards.spells;

import java.util.ArrayList;
import java.util.Iterator;

import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.minecraft.game.core.explosion.CustomExplosion;
import nautilus.game.arcade.game.games.wizards.Spell;
import nautilus.game.arcade.game.games.wizards.spellinterfaces.SpellClick;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class SpellAnvilDrop extends Spell implements SpellClick
{
	private ArrayList<FallingBlock> _fallingBlocks = new ArrayList<FallingBlock>();

	@Override
	public void castSpell(Player player)
	{
		ArrayList<Player> players = new ArrayList<Player>();
		players.add(player);
		int radius = 4 + (getSpellLevel(player) * 2);

		for (Entity entity : player.getNearbyEntities(radius, radius * 3, radius))
		{
			if (entity instanceof Player && Wizards.IsAlive(entity))
			{
				players.add((Player) entity);
			}
		}

		ArrayList<FallingBlock> newFallingBlocks = new ArrayList<FallingBlock>();

		for (Player p : players)
		{
			UtilParticle.PlayParticle(ParticleType.LARGE_SMOKE, p.getLocation(), 0, 0, 0, 0, 1,
					ViewDist.LONG, UtilServer.getPlayers());

			Location loc = p.getLocation().clone().add(0, 15 + (getSpellLevel(player) * 3), 0);
			int lowered = 0;

			while (lowered < 5 && loc.getBlock().getType() != Material.AIR)
			{
				lowered++;
				loc = loc.add(0, -1, 0);
			}

			if (loc.getBlock().getType() == Material.AIR)
			{

				FallingBlock anvil = p.getWorld().spawnFallingBlock(loc.getBlock().getLocation().add(0.5, 0.5, 0.5),
						Material.ANVIL, (byte) 0);

				anvil.setMetadata("SpellLevel", new FixedMetadataValue(Wizards.getArcadeManager().getPlugin(),
						getSpellLevel(player)));

				anvil.setMetadata("Wizard", new FixedMetadataValue(Wizards.getArcadeManager().getPlugin(), player));

				anvil.getWorld().playSound(anvil.getLocation(), Sound.ANVIL_USE, 1.9F, 0);

				newFallingBlocks.add(anvil);

			}

		}

		if (!newFallingBlocks.isEmpty())
		{
			_fallingBlocks.addAll(newFallingBlocks);
			charge(player);
		}
	}

	private void handleAnvil(Entity entity)
	{
		_fallingBlocks.remove(entity);

		int spellLevel = entity.getMetadata("SpellLevel").get(0).asInt();

		CustomExplosion explosion = new CustomExplosion(Wizards.getArcadeManager().GetDamage(), Wizards.getArcadeManager()
				.GetExplosion(), entity.getLocation(), 4 + (spellLevel / 2F), "Anvil Drop");

		explosion.setPlayer((Player) entity.getMetadata("Wizard").get(0).value(), true);

		explosion.setFallingBlockExplosion(true);

		explosion.setDropItems(false);

		explosion.setBlockExplosionSize(explosion.getSize() -3);
		explosion.setExplosionDamage(3 + (spellLevel * 2));

		explosion.explode();

		entity.remove();
	}

	@EventHandler
	public void onDrop(ItemSpawnEvent event)
	{
		Iterator<FallingBlock> itel = _fallingBlocks.iterator();
		FallingBlock b = null;

		while (itel.hasNext())
		{
			FallingBlock block = itel.next();

			if (block.isDead())
			{
				b = block;
				break;
			}
		}

		if (b != null)
		{
			event.setCancelled(true);
			handleAnvil(b);
		}
	}

	@EventHandler
	public void onPlace(EntityChangeBlockEvent event)
	{
		if (_fallingBlocks.contains(event.getEntity()))
		{
			handleAnvil(event.getEntity());
			event.setCancelled(true);
		}
	}
}