package mineplex.core.reward.rewards;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.cache.player.PlayerCache;
import mineplex.core.Managers;
import mineplex.core.pet.PetManager;
import mineplex.core.pet.PetType;
import mineplex.core.pet.repository.token.PetChangeToken;
import mineplex.core.pet.repository.token.PetToken;
import mineplex.core.reward.Reward;
import mineplex.core.reward.RewardData;
import mineplex.core.treasure.reward.RewardRarity;

public class PetReward extends Reward
{

	private static final PetManager PET_MANAGER = Managers.require(PetManager.class);

	private String _packageName;
	private ItemStack _itemStack;
	private PetType _petType;

	public PetReward(String packageName, PetType petType, RewardRarity rarity, int shardValue)
	{
		super(rarity, shardValue);

		_packageName = packageName;
		_itemStack = new ItemStack(Material.MONSTER_EGG, 1, petType.getEntityType().getTypeId());
		_petType = petType;
	}

	@Override
	protected RewardData giveRewardCustom(Player player)
	{
		PetChangeToken token = new PetChangeToken();

		if (INVENTORY_MANAGER.getClientManager().Get(player) != null)
		{
			token.AccountId = INVENTORY_MANAGER.getClientManager().Get(player).getAccountId();
		}
		else
		{
			token.AccountId = PlayerCache.getInstance().getAccountId(player.getUniqueId());
		}

		token.Name = player.getName();
		token.PetType = _petType.toString();
		token.PetName = _packageName;

		PetToken petToken = new PetToken();
		petToken.PetType = token.PetType;

		PET_MANAGER.getRepository().AddPet(token);
		PET_MANAGER.Get(player).getPets().put(_petType, token.PetName);

		INVENTORY_MANAGER.addItemToInventory(player, _petType.toString(), 1);

		return getFakeRewardData(player);
	}

	@Override
	public RewardData getFakeRewardData(Player player)
	{
		return new RewardData(getRarity().getDarkColor() + "Pet", getRarity().getColor() + _petType.getName(), _itemStack, getRarity());
	}

	@Override
	public boolean canGiveReward(Player player)
	{
		if (DONATION_MANAGER.Get(player) == null)
		{
			System.out.println("Could not give reward " + _packageName + " to Offline Player: " + player.getName());
			return false;
		}

		return !PET_MANAGER.Get(player).getPets().containsKey(_petType);
	}
}
