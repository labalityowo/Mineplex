package mineplex.core.treasure.types;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.bukkit.inventory.ItemStack;

import com.google.common.collect.ImmutableMap;

import mineplex.core.Managers;
import mineplex.core.common.util.C;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.balloons.BalloonType;
import mineplex.core.gadget.gadgets.flag.FlagType;
import mineplex.core.gadget.gadgets.hat.HatType;
import mineplex.core.gadget.gadgets.kitselector.SingleParticleKitSelector;
import mineplex.core.gadget.gadgets.weaponname.WeaponNameType;
import mineplex.core.gadget.types.Gadget;
import mineplex.core.gadget.types.GadgetType;
import mineplex.core.gadget.types.ItemGadget;
import mineplex.core.pet.PetType;
import mineplex.core.reward.Reward;
import mineplex.core.reward.RewardType;
import mineplex.core.reward.rewards.ChestReward;
import mineplex.core.reward.rewards.GadgetReward;
import mineplex.core.reward.rewards.InventoryReward;
import mineplex.core.reward.rewards.PetReward;
import mineplex.core.reward.rewards.RankReward;
import mineplex.core.reward.rewards.TitleReward;
import mineplex.core.titles.tracks.TrackManager;
import mineplex.core.treasure.TreasureLocation;
import mineplex.core.treasure.animation.TreasureAnimation;
import mineplex.core.treasure.reward.RewardRarity;
import mineplex.core.treasure.reward.TreasureRewardManager;

public class Treasure
{

	private static final String PURCHASABLE_FROM_STORE = "Purchase at mineplex.com/shop";
	private static final Map<RewardRarity, Integer> SHARD_WORTH = ImmutableMap.<RewardRarity, Integer>builder()
			.put(RewardRarity.UNCOMMON, 100)
			.put(RewardRarity.RARE, 500)
			.put(RewardRarity.LEGENDARY, 5000)
			.build();

	private static final TreasureRewardManager TREASURE_REWARD_MANAGER = Managers.require(TreasureRewardManager.class);

	public static TreasureRewardManager getRewardManager()
	{
		return TREASURE_REWARD_MANAGER;
	}

	protected static final GadgetManager GADGET_MANAGER = Managers.require(GadgetManager.class);
	private static final TrackManager TRACK_MANAGER = Managers.require(TrackManager.class);

	private final TreasureType _treasureType;
	private final List<String> _purchaseMethods;

	private RewardType _rewardType;
	private Function<TreasureLocation, TreasureAnimation> _animationFunction;

	private boolean _duplicates;
	private int _rewardsPerChest;
	private boolean _purchasable;
	private int _purchasePrice;
	private boolean _enabledDefault;
	private boolean _featured;

	Treasure(TreasureType treasureType)
	{
		_treasureType = treasureType;
		_purchaseMethods = new ArrayList<>(2);

		addCommon(RewardRarity.COMMON);
		addUncommon(RewardRarity.UNCOMMON);
		addRare(RewardRarity.RARE);
		addLegendary(RewardRarity.LEGENDARY);
		addMythical(RewardRarity.MYTHICAL);
	}

	protected void addCommon(RewardRarity rarity)
	{
	}

	protected void addUncommon(RewardRarity rarity)
	{
	}

	protected void addRare(RewardRarity rarity)
	{
	}

	protected void addLegendary(RewardRarity rarity)
	{
	}

	protected void addMythical(RewardRarity rarity)
	{
	}

	public TreasureType getTreasureType()
	{
		return _treasureType;
	}

	public RewardType getRewardType()
	{
		return _rewardType;
	}

	public void allowDuplicates()
	{
		_duplicates = true;
	}

	public boolean isDuplicates()
	{
		return _duplicates;
	}

	public void setRewardsPerChest(int rewardsPerChest)
	{
		_rewardsPerChest = rewardsPerChest;
	}

	public int getRewardsPerChest()
	{
		return _rewardsPerChest;
	}

	protected void purchasableFromStore()
	{
		addPurchaseMethod(PURCHASABLE_FROM_STORE);
	}

	protected void addPurchaseMethod(String method)
	{
		_purchaseMethods.add(C.cBlue + method);
	}

	protected void setRewards(RewardType rewardType)
	{
		_rewardType = rewardType;
	}

	protected void setAnimation(Function<TreasureLocation, TreasureAnimation> animations)
	{
		_animationFunction = animations;
	}

	public TreasureAnimation getAnimations(TreasureLocation location)
	{
		return _animationFunction.apply(location);
	}

	public boolean hasAnimations()
	{
		return _animationFunction != null;
	}

	public boolean isPurchasable()
	{
		return _enabledDefault && _purchasable;
	}

	public int getPurchasePrice()
	{
		return _purchasePrice;
	}

	protected void setPurchasable(int purchasePrice)
	{
		_purchasable = true;
		_purchasePrice = purchasePrice;

		addPurchaseMethod("Craftable for " + C.cAqua + _purchasePrice + " Treasure Shards");
	}

	public List<String> getPurchaseMethods()
	{
		return _purchaseMethods;
	}

	protected void enabledByDefault()
	{
		_enabledDefault = true;
	}

	protected void setFeaturedChest()
	{
		_featured = true;
	}

	public boolean isFeaturedChest()
	{
		return _featured;
	}

	protected final <T extends Gadget> T getGadget(Class<T> clazz)
	{
		return GADGET_MANAGER.getGadget(clazz);
	}

	protected final InventoryReward addGadgetReward(ItemGadget gadget, RewardRarity rarity, int weight, int minAmmo, int maxAmmo)
	{
		return addGadgetReward(gadget, rarity, weight, getShards(rarity), minAmmo, maxAmmo);
	}

	protected final InventoryReward addGadgetReward(ItemGadget gadget, RewardRarity rarity, int weight, int shards, int minAmmo, int maxAmmo)
	{
		InventoryReward reward = new InventoryReward(
				gadget.getDisplayName(),
				gadget.getName(),
				"Gadget",
				minAmmo,
				maxAmmo,
				new ItemStack(gadget.getDisplayMaterial(), 1, (short) 0, gadget.getDisplayData()),
				rarity,
				shards
		);
		addReward(reward, weight);
		return reward;
	}

	protected final GadgetReward addGadgetReward(Gadget gadget, RewardRarity rarity, int weight)
	{
		return addGadgetReward(gadget, rarity, weight, getShards(rarity));
	}

	protected final GadgetReward addGadgetReward(Gadget gadget, RewardRarity rarity, int weight, int shards)
	{
		GadgetReward reward = new GadgetReward(gadget, rarity, shards);
		addReward(reward, weight);
		return reward;
	}

	protected PetReward addPetReward(PetType petType, RewardRarity rarity, int weight)
	{
		return addPetReward(petType, rarity, weight, getShards(rarity));
	}

	protected PetReward addPetReward(PetType petType, RewardRarity rarity, int weight, int shards)
	{
		PetReward reward = new PetReward(petType.getName(), petType, rarity, shards);
		addReward(reward, weight);
		return reward;
	}

	protected GadgetReward addMusicReward(String disc, RewardRarity rarity, int weight)
	{
		return addMusicReward(disc, rarity, weight, getShards(rarity));
	}

	protected GadgetReward addMusicReward(String disc, RewardRarity rarity, int weight, int shards)
	{
		for (Gadget gadget : GADGET_MANAGER.getGadgets(GadgetType.MUSIC_DISC))
		{
			if (gadget.getName().equals(disc + " Disc"))
			{
				return addGadgetReward(gadget, rarity, weight, shards);
			}
		}

		return null;
	}

	protected GadgetReward addBalloonReward(BalloonType type, RewardRarity rarity, int weight)
	{
		return addBalloonReward(type, rarity, weight, getShards(rarity));
	}

	protected GadgetReward addBalloonReward(BalloonType type, RewardRarity rarity, int weight, int shards)
	{
		return addGadgetReward(GADGET_MANAGER.getBalloonGadget(type), rarity, weight, shards);
	}

	protected TitleReward addTitleReward(String id, RewardRarity rarity, int weight)
	{
		return addTitleReward(id, rarity, weight, getShards(rarity));
	}

	protected TitleReward addTitleReward(String id, RewardRarity rarity, int weight, int shards)
	{
		TitleReward reward = new TitleReward(TRACK_MANAGER.getTrackById(id), rarity, shards);
		addReward(reward, weight);
		return reward;
	}

	protected RankReward addRankReward(RewardRarity rarity, boolean canPassLegend, int weight)
	{
		RankReward reward = new RankReward(rarity, 0, canPassLegend);
		addReward(reward, weight);
		return reward;
	}

	protected GadgetReward addHatReward(HatType type, RewardRarity rarity, int weight)
	{
		return addHatReward(type, rarity, weight, getShards(rarity));
	}

	protected GadgetReward addHatReward(HatType type, RewardRarity rarity, int weight, int shards)
	{
		return addGadgetReward(GADGET_MANAGER.getHatGadget(type), rarity, weight, shards);
	}

	protected GadgetReward addFlagReward(FlagType type, RewardRarity rarity, int weight)
	{
		return addFlagReward(type, rarity, weight, getShards(rarity));
	}

	protected GadgetReward addFlagReward(FlagType type, RewardRarity rarity, int weight, int shards)
	{
		return addGadgetReward(GADGET_MANAGER.getFlagGadget(type), rarity, weight, shards);
	}

	protected GadgetReward addWeaponNameReward(WeaponNameType type, RewardRarity rarity, int weight)
	{
		return addWeaponNameReward(type, rarity, weight, getShards(rarity));
	}

	protected GadgetReward addWeaponNameReward(WeaponNameType type, RewardRarity rarity, int weight, int shards)
	{
		return addGadgetReward(GADGET_MANAGER.getWeaponNameGadget(type), rarity, weight, shards);
	}

	protected ChestReward addChestReward(TreasureType type, RewardRarity rarity, int min, int max, int weight)
	{
		ChestReward reward = new ChestReward(type, min, max, rarity, 0);
		addReward(reward, weight);
		return reward;
	}

	protected SingleParticleKitSelector getKitSelector(SingleParticleKitSelector.SingleParticleSelectors singleParticleSelectors)
	{
		return GADGET_MANAGER.getSingleParticleKitSelector(singleParticleSelectors);
	}

	protected int getShards(RewardRarity rarity)
	{
		return SHARD_WORTH.getOrDefault(rarity, 0);
	}

	protected void addReward(Reward reward, int weight)
	{
		TREASURE_REWARD_MANAGER.addReward(this, reward, weight);
	}
}
