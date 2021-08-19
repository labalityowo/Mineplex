package mineplex.minecraft.game.classcombat.shop.button;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import mineplex.core.shop.item.IButton;
import mineplex.minecraft.game.classcombat.Skill.ISkill;
import mineplex.minecraft.game.classcombat.shop.page.SkillPage;

public class SelectSkillButton implements IButton
{
	private SkillPage _page;
	private ISkill _skill;
	private int _level;
	private boolean _canAfford;
	
	public SelectSkillButton(SkillPage page, ISkill skill, int level, boolean canAfford)
	{
		_page = page;
		_skill = skill;
		_level = level;
		_canAfford = canAfford;
	}

	@Override
	public void onClick(Player player, ClickType clickType)
	{
		if (clickType.isLeftClick())
		{
			if (!_canAfford)
			{
				player.playSound(player.getLocation(), Sound.NOTE_BASS_GUITAR, 1f, 0.5f);
				return;
			}

			_page.SelectSkill(player, _skill, _level);
		}
		else if (clickType.isRightClick())
		{
			_page.DeselectSkill(player, _skill);
		}
	}

}
