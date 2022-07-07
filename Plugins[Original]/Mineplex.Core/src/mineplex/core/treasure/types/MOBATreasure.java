package mineplex.core.treasure.types;

import mineplex.core.gadget.gadgets.gamemodifiers.moba.shopmorph.ShopMorphType;
import mineplex.core.gadget.gadgets.gamemodifiers.moba.skins.HeroSkinGadget;
import mineplex.core.gadget.gadgets.morph.moba.MorphAnath;
import mineplex.core.gadget.gadgets.morph.moba.MorphBardolf;
import mineplex.core.gadget.gadgets.morph.moba.MorphBiff;
import mineplex.core.gadget.gadgets.morph.moba.MorphDana;
import mineplex.core.gadget.gadgets.morph.moba.MorphDevon;
import mineplex.core.gadget.gadgets.morph.moba.MorphHattori;
import mineplex.core.gadget.gadgets.morph.moba.MorphIvy;
import mineplex.core.gadget.gadgets.morph.moba.MorphLarissa;
import mineplex.core.gadget.gadgets.morph.moba.MorphRowena;
import mineplex.core.reward.RewardType;
import mineplex.core.reward.rewards.GadgetReward;
import mineplex.core.treasure.animation.animations.MOBAChestAnimation;
import mineplex.core.treasure.animation.animations.TrickOrTreatChestAnimation;
import mineplex.core.treasure.reward.RewardRarity;

public class MOBATreasure extends Treasure
{

	public MOBATreasure()
	{
		super(TreasureType.MOBA);

		setAnimation(treasureLocation -> new MOBAChestAnimation(this, treasureLocation));
		setRewards(RewardType.MOBA);
		setRewardsPerChest(4);
		setPurchasable(20000);
		purchasableFromStore();
		enabledByDefault();
		allowDuplicates();
	}

	@Override
	protected void addUncommon(RewardRarity rarity)
	{
		for (ShopMorphType shopMorphType : ShopMorphType.values())
		{
			if (shopMorphType.getRarity() == rarity)
			{
				addMOBAShopSkin(shopMorphType, rarity, 1);
			}
		}

		addAllMOBASkins(rarity);
	}

	@Override
	protected void addRare(RewardRarity rarity)
	{
		for (ShopMorphType shopMorphType : ShopMorphType.values())
		{
			if (shopMorphType.getRarity() == rarity)
			{
				addMOBAShopSkin(shopMorphType, rarity, 1);
			}
		}

		addAllMOBASkins(rarity);
	}

	@Override
	protected void addLegendary(RewardRarity rarity)
	{
		addGadgetReward(getGadget(MorphHattori.class), rarity, 1);
		addGadgetReward(getGadget(MorphDevon.class), rarity, 1);
		addGadgetReward(getGadget(MorphAnath.class), rarity, 1);
		addGadgetReward(getGadget(MorphDana.class), rarity, 1);
		addGadgetReward(getGadget(MorphBardolf.class), rarity, 1);
		addGadgetReward(getGadget(MorphRowena.class), rarity, 1);
		addGadgetReward(getGadget(MorphLarissa.class), rarity, 1);
		addGadgetReward(getGadget(MorphBiff.class), rarity, 1);
		addGadgetReward(getGadget(MorphIvy.class), rarity, 1);

		addAllMOBASkins(rarity);
	}

	@Override
	protected void addMythical(RewardRarity rarity)
	{
	}

	private void addAllMOBASkins(RewardRarity rarity)
	{
		HeroSkinGadget.getSkins().forEach((name, dataList) ->
		{
			if (name.equals("Bardolf-Werewolf"))
			{
				return;
			}

			dataList.forEach(data ->
			{
				if (data.getRarity() != rarity)
				{
					return;
				}

				addMOBASkin(data.getGadget(), rarity, 1);
			});
		});
	}

	public GadgetReward addMOBASkin(HeroSkinGadget gadget, RewardRarity rarity, int weight)
	{
		return addMOBASkin(gadget, rarity, weight, getShards(rarity));
	}

	public GadgetReward addMOBASkin(HeroSkinGadget gadget, RewardRarity rarity, int weight, int shards)
	{
		return addGadgetReward(gadget, rarity, weight, shards);
	}

	public GadgetReward addMOBAShopSkin(ShopMorphType morphType, RewardRarity rarity, int weight)
	{
		return addMOBAShopSkin(morphType, rarity, weight, getShards(rarity));
	}

	public GadgetReward addMOBAShopSkin(ShopMorphType morphType, RewardRarity rarity, int weight, int shards)
	{
		return addGadgetReward(GADGET_MANAGER.getGameCosmeticManager().getGadgetFrom(morphType.getName()), rarity, weight, shards);
	}
}
