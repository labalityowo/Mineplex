package nautilus.game.arcade.game.games.typewars.kits;

import mineplex.core.common.util.C;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemStackFactory;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.typewars.MinionSize;
import nautilus.game.arcade.game.games.typewars.Spell;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public abstract class KitTypeWarsBase extends Kit
{

	private Spell[] _spells;
	protected ArcadeManager _manager;

	KitTypeWarsBase(ArcadeManager manager, GameKit gameKit, Perk[] perks, Spell[] spells)
	{
		super(manager, gameKit, perks);

		_spells = spells;
		_manager = manager;
	}
	
	@Override
	public void GiveItems(Player player)
	{
		int e = 0;
		for(Spell spell : getSpells())
		{
			player.getInventory().setItem(e, ItemStackFactory.Instance.CreateStack(spell.getMaterial(), (byte) 0, 1, C.cYellow + "Activate " + spell.getName() + " Cost: " + spell.getCost()));
			e++;
		}
		
		int i = 4;
		for(MinionSize type : MinionSize.values())
		{
			if(type != MinionSize.BOSS && type != MinionSize.FREAK)
			{
				player.getInventory().setItem(i, ItemStackFactory.Instance.CreateStack(type.getDisplayItem().getType(), (byte) 0, 1, (short) type.getDisplayItem().getDurability(), C.cYellow + "Spawn " + type.getDisplayName() + " Minion Cost: " + type.getCost(), new String[]{}));
				i++;
			}
		}
	}
	
	public Spell[] getSpells()
	{
		return _spells;
	}
	
	@EventHandler
	public void activateSpell(PlayerInteractEvent event)
	{
		if(!Manager.GetGame().IsLive())
			return;
		
		if(!Manager.GetGame().IsPlaying(event.getPlayer()))
			return;
			
		if(event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)
			return;
		
		if(event.getPlayer().getItemInHand() == null)
			return;
		
		if(!Manager.GetGame().GetKit(event.getPlayer()).GetName().equalsIgnoreCase(GetName()))
			return;
		
		executeSpell(event.getPlayer(), event.getPlayer().getItemInHand().getType());
	}
	
	public boolean executeSpell(Player player, Material mat)
	{
		for(Spell spell : getSpells())
		{
			if(spell.getMaterial() == mat)
			{
				spell.prepareExecution(player);
				return true;
			}
		}
		return false;
	}

}
