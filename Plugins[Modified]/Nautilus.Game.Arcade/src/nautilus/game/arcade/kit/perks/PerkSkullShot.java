package nautilus.game.arcade.kit.perks;

import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.kit.Perk;

public class PerkSkullShot extends Perk
{
	private HashMap<Player, Long> _shootTime = new HashMap<Player, Long>();
	private HashMap<WitherSkull, Vector> _skullDir = new HashMap<WitherSkull, Vector>();

	public PerkSkullShot() 
	{
		super("Skull Shot", new String[] 
				{ 
				C.cYellow + "Shoot Bow" + C.cGray + " to use " + C.cGreen + "Skull Shot"
				});
	}

	@EventHandler
	public void Fire(EntityShootBowEvent event)
	{
		if (!(event.getEntity() instanceof Player))
			return;

		Player player = (Player)event.getEntity();

		if (!Kit.HasKit(player))
			return;

		Vector vel = event.getProjectile().getVelocity();
		event.getProjectile().remove();

		WitherSkull skull = player.launchProjectile(WitherSkull.class);
		skull.setDirection(vel);
		skull.setVelocity(vel);

		_skullDir.put(skull, vel.multiply(0.5));

		_shootTime.put(player, System.currentTimeMillis());

		//Helmet
		player.getInventory().setHelmet(null);
		player.getInventory().remove(Material.ARROW);

		//Sound
		player.getWorld().playSound(player.getLocation(), Sound.WITHER_SHOOT, 1f, 1f);

		//Inform
		UtilPlayer.message(player, F.main("Skill", "You used " + F.skill(GetName()) + "."));
	}

	@EventHandler
	public void SkullDir(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
			return;

		Iterator<WitherSkull> skullIterator = _skullDir.keySet().iterator();

		while (skullIterator.hasNext())
		{
			WitherSkull skull = skullIterator.next();

			if (!skull.isValid())
			{
				skullIterator.remove();
				continue;
			}

			skull.setVelocity(_skullDir.get(skull));
			skull.setDirection(_skullDir.get(skull));
		}
	}

	@EventHandler
	public void ArrowRespawn(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
			return;

		Iterator<Player> playerIterator = _shootTime.keySet().iterator();

		while (playerIterator.hasNext())
		{
			Player player = playerIterator.next();

			if (!UtilTime.elapsed(_shootTime.get(player), 2000))
				continue;

			playerIterator.remove();

			//Helmet
			ItemStack head = ItemStackFactory.Instance.CreateStack(Material.SKULL_ITEM, (byte)1, 1);
			player.getInventory().setHelmet(head);
			player.getInventory().addItem(new ItemStack(Material.ARROW));
		}
	}

	@EventHandler
	public void Death(PlayerDeathEvent event)
	{
		_shootTime.remove(event.getEntity());
	}
}
