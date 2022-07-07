package mineplex.game.nano.game.games.wizards.spells;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.game.nano.game.games.wizards.Spell;
import mineplex.game.nano.game.games.wizards.Wizards;

public class SpellTNT extends Spell
{

	private final Map<Entity, Player> _owner;

	public SpellTNT(Wizards game)
	{
		super(game, "Throwing TNT", SpellType.Attack, new ItemStack(Material.TNT), TimeUnit.SECONDS.toMillis(5));

		_owner = new HashMap<>();
	}

	@Override
	protected void onSpellUse(Player player)
	{
		Location location = player.getEyeLocation();
		TNTPrimed tnt = location.getWorld().spawn(location, TNTPrimed.class);

		tnt.setVelocity(location.getDirection());
		tnt.setFuseTicks(20);

		_owner.put(tnt, player);
	}

	@EventHandler
	public void entityExplode(EntityExplodeEvent event)
	{
		Player damager = _owner.remove(event.getEntity());

		if (damager == null)
		{
			return;
		}

		event.setCancelled(true);
		_game.createExplosion(damager, getName(), event.getEntity().getLocation(), 4, 25);
	}
}
