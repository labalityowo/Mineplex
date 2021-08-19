package mineplex.minecraft.game.classcombat.shop.salespackage;

import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.shop.item.SalesPackageBase;
import mineplex.minecraft.game.classcombat.Skill.ISkill;
import org.bukkit.Material;

public class SkillSalesPackage extends SalesPackageBase
{
	public SkillSalesPackage(ISkill skill)
	{
		super("Champions " + skill.GetName(), Material.BOOK, (byte)0, skill.GetDesc(0), skill.GetGemCost());
		Free = skill.IsFree();
		KnownPackage = false;
		CurrencyCostMap.put(GlobalCurrency.GEM, skill.GetGemCost());
	}
}
