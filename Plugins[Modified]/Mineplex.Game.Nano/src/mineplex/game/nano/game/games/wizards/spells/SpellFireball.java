package mineplex.game.nano.game.games.wizards.spells;

import java.util.concurrent.TimeUnit;

import org.bukkit.Material;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import mineplex.game.nano.game.games.wizards.Spell;
import mineplex.game.nano.game.games.wizards.Wizards;

public class SpellFireball extends Spell
{

	public SpellFireball(Wizards game)
	{
		super(game, "Fireball", SpellType.Attack, new ItemStack(Material.BLAZE_ROD), TimeUnit.SECONDS.toMillis(1));
	}

	@Override
	protected void onSpellUse(Player player)
	{
		Vector velocity = player.getLocation().getDirection();

		Fireball fireball = player.launchProjectile(Fireball.class);
		fireball.setVelocity(velocity.clone().multiply(2));
	}

	@EventHandler
	public void entityExplode(EntityExplodeEvent event)
	{
		if (!(event.getEntity() instanceof Fireball))
		{
			return;
		}

		event.setCancelled(true);

		Player damager = (Player) ((Fireball) event.getEntity()).getShooter();

		_game.createExplosion(damager, getName(), event.getEntity().getLocation(), 2, 14);
	}
}
