package mineplex.core.gadget.gadgets.gamemodifiers.moba.skins;

import mineplex.core.common.skin.SkinData;
import mineplex.core.treasure.reward.RewardRarity;

public class HeroSkinGadgetData
{

	private final String _hero;
	private final String _name;
	private final String _description;
	private final RewardRarity _rarity;
	private final SkinData _skinData;
	private HeroSkinGadget _gadget;

	public HeroSkinGadgetData(String hero, String name, String description, RewardRarity rarity, SkinData skinData)
	{
		_hero = hero;
		_name = name;
		_description = description;
		_rarity = rarity;
		_skinData = skinData;
	}

	public String getHero()
	{
		return _hero;
	}

	public String getName()
	{
		return _name;
	}

	public String getDescription()
	{
		return _description;
	}

	public RewardRarity getRarity()
	{
		return _rarity;
	}

	public SkinData getSkinData()
	{
		return _skinData;
	}

	public void setGadget(HeroSkinGadget gadget)
	{
		_gadget = gadget;
	}

	public HeroSkinGadget getGadget()
	{
		return _gadget;
	}
}
