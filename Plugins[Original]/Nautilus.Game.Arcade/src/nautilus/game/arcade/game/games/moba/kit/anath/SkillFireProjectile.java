package nautilus.game.arcade.game.games.moba.kit.anath;

import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.recharge.Recharge;
import nautilus.game.arcade.game.games.moba.Moba;
import nautilus.game.arcade.game.games.moba.kit.HeroSkill;
import nautilus.game.arcade.game.games.moba.util.MobaConstants;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class SkillFireProjectile extends HeroSkill
{

	private static final String[] DESCRIPTION = {
			"Fires an Ember at high speed in front of you.",
			"Any enemies it collides with take damage and are set on fire."
	};
	private static final int DAMAGE = 5;
	private static final ItemStack SKILL_ITEM = new ItemStack(Material.BLAZE_ROD);

	public SkillFireProjectile(int slot)
	{
		super("Flame Wand", DESCRIPTION, SKILL_ITEM, slot, ActionType.ANY);
	}

	@EventHandler
	public void interact(PlayerInteractEvent event)
	{
		if (!isSkillItem(event))
		{
			return;
		}

		Player player = event.getPlayer();

		if (!Recharge.Instance.use(player, GetName(), 500, false, true) || !_kit.useAmmo(player, 1))
		{
			return;
		}

		Vector direction = player.getLocation().getDirection().multiply(1.25);
		Item item = player.getWorld().dropItem(player.getEyeLocation().add(direction), _kit.getAmmo());
		item.setVelocity(direction);

		Manager.GetFire().Add(item, player, 3, 0, 1, DAMAGE, MobaConstants.BASIC_ATTACK, false);
		((Moba) Manager.GetGame()).getTowerManager().addProjectile(player, item, DAMAGE);
	}
}
