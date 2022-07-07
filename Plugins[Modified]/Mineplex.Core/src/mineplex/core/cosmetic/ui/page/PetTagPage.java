package mineplex.core.cosmetic.ui.page;

import net.minecraft.server.v1_8_R3.ItemStack;
import net.minecraft.server.v1_8_R3.Items;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import mineplex.cache.player.PlayerCache;
import mineplex.core.account.CoreClientManager;
import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.cosmetic.CosmeticManager;
import mineplex.core.cosmetic.ui.CosmeticShop;
import mineplex.core.cosmetic.ui.button.CloseButton;
import mineplex.core.cosmetic.ui.button.SelectTagButton;
import mineplex.core.donation.DonationManager;
import mineplex.core.pet.PetExtra;
import mineplex.core.pet.PetType;
import mineplex.core.pet.repository.token.PetChangeToken;
import mineplex.core.pet.repository.token.PetToken;
import mineplex.core.shop.confirmation.ConfirmationPage;
import mineplex.core.shop.item.SalesPackageBase;
import mineplex.core.shop.item.SalesPackageProcessor;
import mineplex.core.shop.page.ShopPageBase;

public class PetTagPage extends ShopPageBase<CosmeticManager, CosmeticShop>
{
	private String _tagName = "Pet Tag";
	private PetType _petType;
	private boolean _petPurchase;
	
    public PetTagPage(CosmeticManager plugin, CosmeticShop shop, CoreClientManager clientManager, DonationManager donationManager, String name, Player player, PetType petType, boolean petPurchase)
    {
        super(plugin, shop, clientManager, donationManager, name, player, 3);
        
        _petType = petType;
        _petPurchase = petPurchase;
        
        buildPage();
        
        getPlayer().setLevel(5);
    }

	@Override
	protected void buildPage()
	{
		inventory.setItem(0, new ItemStack(Items.NAME_TAG));
		
		getButtonMap().put(0, new CloseButton());
		getButtonMap().put(1, new CloseButton());
		getButtonMap().put(2, new SelectTagButton(this));
	}
	
	@Override
	public void playerClosed()
	{
		super.playerClosed();
		
		getPlayer().setLevel(0);
	}

	public void SelectTag()
	{
		_tagName = ChatColor.stripColor(_tagName);
		_tagName = _tagName.replaceAll("[^A-Za-z0-9]", "");
		System.out.println("Pet name: " + _tagName + ".");
		if (_tagName.length() == 0)
		{
			UtilPlayer.message(getPlayer(), F.main(getPlugin().getName(), ChatColor.RED + "Supplied pet name contains invalid characters."));
			playDenySound(getPlayer());
			
			getShop().openPageForPlayer(getPlayer(), new PetPage(getPlugin(), getShop(), getClientManager(), getDonationManager(), "Pets", getPlayer()));
			return;
		}
		
		if (_tagName.length() > 16)
		{
			UtilPlayer.message(getPlayer(), F.main(getPlugin().getName(), ChatColor.RED + "Pet name cannot be longer than 16 characters."));
			playDenySound(getPlayer());
			
			getShop().openPageForPlayer(getPlayer(), new PetPage(getPlugin(), getShop(), getClientManager(), getDonationManager(), "Pets", getPlayer()));
			return;
		}

		if (_tagName.toLowerCase().contains("ultra"))
		{
			UtilPlayer.message(getPlayer(), F.main(getPlugin().getName(), ChatColor.RED + _tagName + " is a restricted name."));
			playDenySound(getPlayer());

			getShop().openPageForPlayer(getPlayer(), new PetPage(getPlugin(), getShop(), getClientManager(), getDonationManager(), "Pets", getPlayer()));
			return;
		}

		final SalesPackageBase salesPackage;
		if (_petPurchase)
		{
			salesPackage = _petType.toSalesPackage(_tagName);

		} else
		{
			salesPackage = PetExtra.NAME_TAG.toSalesPackage("Rename " + _petType.getName() + " to " + _tagName);
		}

		getShop().openPageForPlayer(getPlayer(), new ConfirmationPage<>(_player, this, new SalesPackageProcessor(_player, GlobalCurrency.TREASURE_SHARD, salesPackage, _donationManager, () ->
		{
			PetChangeToken token = new PetChangeToken();

			if (getClientManager().Get(getPlayer()) != null)
				token.AccountId = getClientManager().Get(getPlayer()).getAccountId();
			else
				token.AccountId = PlayerCache.getInstance().getAccountId(getPlayer().getUniqueId());

			token.Name = getPlayer().getName();
			token.PetType = _petType.toString();
			token.PetName = _tagName;

			PetToken petToken = new PetToken();
			petToken.PetType = token.PetType;

			if (_petPurchase)
			{
				getPlugin().getPetManager().getRepository().AddPet(token);
				getPlugin().getPetManager().addPetOwnerToQueue(getPlayer().getName(), _petType);
			}
			else
			{
				getPlugin().getPetManager().getRepository().UpdatePet(token);
				getPlugin().getPetManager().addRenamePetToQueue(getPlayer().getName(), token.PetName);
			}

			getPlugin().getPetManager().Get(getPlayer()).getPets().put(_petType, token.PetName);

			getShop().openPageForPlayer(getPlayer(), new Menu(getPlugin(), getShop(), getClientManager(), getDonationManager(), getPlayer()));
		}), salesPackage.buildIcon()));
	}

	public void SetTagName(String tagName)
	{
		_tagName = tagName;
	}
}
