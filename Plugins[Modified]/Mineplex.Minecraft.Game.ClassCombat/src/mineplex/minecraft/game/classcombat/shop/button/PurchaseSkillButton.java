package mineplex.minecraft.game.classcombat.shop.button;

import mineplex.core.shop.item.IButton;
import mineplex.minecraft.game.classcombat.Skill.ISkill;
import mineplex.minecraft.game.classcombat.shop.page.SkillPage;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

public class PurchaseSkillButton implements IButton
{
	private SkillPage _page;
	private ISkill _skill;
	
	public PurchaseSkillButton(SkillPage page, ISkill skill)
	{
		_page = page;
		_skill = skill;
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		_page.PurchaseSkill(player, _skill);
	}
}
