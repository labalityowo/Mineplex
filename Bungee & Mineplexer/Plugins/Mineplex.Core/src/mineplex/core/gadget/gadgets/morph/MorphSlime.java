package mineplex.core.gadget.gadgets.morph;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;

import mineplex.core.achievement.AchievementManager;
import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilText;
import mineplex.core.disguise.disguises.DisguiseSlime;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.morph.managers.UtilMorph;
import mineplex.core.gadget.types.MorphGadget;
import mineplex.core.recharge.Recharge;

public class MorphSlime extends MorphGadget 
{	
	private AchievementManager _achievementManager;
	
	public MorphSlime(GadgetManager manager, AchievementManager achievements)
	{
		super(manager, "Big Larry Morph", UtilText.splitLinesToArray(new String[] 
				{
				C.cGray + "Have you ever looked at Big Larry and thought, \'I really want to be that guy\'!? Well, today is your lucky day!",
				C.blankLine,
				"#" + C.cWhite + "Left Click to use Bounce",
				C.blankLine,
				"#" + C.cWhite + "+1 Size per 10 Mineplex Levels",
				}, LineFormat.LORE),
				80000,
				Material.SLIME_BALL, (byte)0);
		
		_achievementManager = achievements;
	}

	@Override
	public void enableCustom(final Player player, boolean message)
	{
		this.applyArmor(player, message);

		DisguiseSlime disguise = new DisguiseSlime(player);
		
		int size = 1 + _achievementManager.getMineplexLevelNumber(player) / 8; 
		
		if (size < 1)
			size = 1;
		
		if (size > 12)
			size = 12;
		
		disguise.SetSize(size);

		UtilMorph.disguise(player, disguise, Manager);
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		this.removeArmor(player);
		UtilMorph.undisguise(player, Manager.getDisguiseManager());
	}

	@EventHandler
	public void skill(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();

		if (!isActive(player))
			return;

		if (!UtilEvent.isAction(event, ActionType.L))
			return;
		
		if (!UtilEnt.isGrounded(player))
			return;
		
		if (!Recharge.Instance.use(player, getName(), 1000, false, false, "Cosmetics"))
			return;
		
		player.getWorld().playSound(player.getLocation(), Sound.SLIME_ATTACK, 1f, 1f);
		
		//Size
		int size = 1 + _achievementManager.getMineplexLevelNumber(player) / 8; 
		
		if (size < 1)
			size = 1;
		
		if (size > 12)
			size = 12;
		
		//Vel
		UtilAction.velocity(player, 1 + (size * 0.2), 0, 10, true);
	}
}