package mineplex.staffServer.ui.pet;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import mineplex.core.account.CoreClient;
import mineplex.core.common.util.C;
import mineplex.core.pet.PetType;
import mineplex.core.pet.repository.token.PetChangeToken;
import mineplex.core.shop.item.ShopItem;
import mineplex.core.shop.page.MultiPageManager;
import mineplex.staffServer.customerSupport.CustomerSupport;
import mineplex.staffServer.ui.SupportPage;
import mineplex.staffServer.ui.SupportShop;

public class SupportPetPage extends SupportPage
{
	private MultiPageManager<PetType> _multiPageManager;

	public SupportPetPage(CustomerSupport plugin, SupportShop shop, Player player, CoreClient target, SupportPage previousPage)
	{
		super(plugin, shop, player, target, previousPage, "Pets");

		_multiPageManager = new MultiPageManager<>(this, this::getPetTypeList, this::addPet);

		buildPage();
	}

	private ItemStack getPetIcon(PetType petType, boolean owned)
	{
		ItemStack item = petType.getDisplayItem().clone();

		ItemMeta meta = item.getItemMeta();

		if (owned)
		{
			meta.setDisplayName(C.cRedB + petType.getName());
			meta.setLore(Arrays.asList(
					C.cYellow + _target.getName() + C.mBody + " already",
					C.mBody + "owns a " + C.cYellow + petType.getName() + " Pet" + C.mBody + "!"
			));
		}
		else
		{
			meta.setDisplayName(C.cGreenB + petType.getName());
			meta.setLore(Arrays.asList(
					C.mBody + "Click to give this",
					C.mBody + "player a " + C.cYellow + petType.getName() + " Pet"
			));
		}

		item.setItemMeta(meta);

		return new ShopItem(item, false, true);
	}

	private Map<PetType, String> getPets()
	{
		return getShop().getPetClients().get(_target.getAccountId()).getPets();
	}

	private void addPet(PetType petType, int slot)
	{
		// Owns the pet
		if (getPets().containsKey(petType))
		{
			addItem(slot, getPetIcon(petType, true));
			addGlow(slot);
		}
		else
		{
			addButton(slot, getPetIcon(petType, false), (p, c) ->
			{
				PetChangeToken token = new PetChangeToken();
				token.AccountId = _target.getAccountId();
				token.Name = _target.getName();
				token.PetType = petType.toString();
				token.PetName = petType.getName();

				getPlugin().getPetRepository().AddPet(token);

				getPlugin().getInventoryManager().addItemToInventoryForOffline((success) ->
				{
					if (success)
					{

						playSuccess();
						message("Successfully gave " + C.cYellow + petType.getName() + " Pet" + C.mBody + " to " + C.cYellow + _target.getName());

						getPets().put(petType, petType.getName());
						refresh();
					}
					else
					{
						playFail();
						message("Unable to give " + C.cYellow + petType.getName() + " Pet" + C.mBody + " to " + C.cYellow + _target.getName() + C.mBody + ", please try again later.");
					}
				}, _target.getAccountId(), petType.toString(), 1);
			});
		}
	}

	private LinkedList<PetType> getPetTypeList()
	{
		return Arrays.stream(PetType.values()).collect(Collectors.toCollection(LinkedList::new));
	}

	@Override
	protected void buildPage()
	{
		super.buildPage();

		_multiPageManager.buildPage();
	}
}
