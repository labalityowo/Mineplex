package nautilus.game.arcade.game.games.moba.kit.devon;

import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.minecraft.game.core.condition.ConditionFactory;
import nautilus.game.arcade.game.games.moba.kit.HeroSkill;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;

public class SkillBoost extends HeroSkill
{

	private static final String[] DESCRIPTION = {
			"Gain Speed II and Jump Boost I for 3 seconds.",
	};
	private static final ItemStack SKILL_ITEM = new ItemStack(Material.FEATHER);

	public SkillBoost(int slot)
	{
		super("Hunters Boost", DESCRIPTION, SKILL_ITEM, slot, ActionType.ANY);

		setCooldown(10000);
		setSneakActivate(true);
	}

	@EventHandler
	public void interact(PlayerInteractEvent event)
	{
		if (!isSkillItem(event))
		{
			return;
		}

		Player player = event.getPlayer();
		useSkill(player);
	}

	@EventHandler
	public void toggleSneak(PlayerToggleSneakEvent event)
	{
		if (!isSkillSneak(event))
		{
			return;
		}

		Player player = event.getPlayer();
		useSkill(player);
	}

	@Override
	public void useSkill(Player player)
	{
		super.useSkill(player);

		ConditionFactory factory = Manager.GetCondition().Factory();
		int time = 3;

		factory.Speed(GetName(), player, null, time, 1, true, true, false);
		factory.Jump(GetName(), player, null, time, 0, true, true, false);
	}
}

