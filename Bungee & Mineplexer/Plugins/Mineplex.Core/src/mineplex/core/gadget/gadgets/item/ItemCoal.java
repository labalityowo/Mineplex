package mineplex.core.gadget.gadgets.item;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.Callback;
import mineplex.core.common.util.F;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilGear;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.event.ItemGadgetOutOfAmmoEvent;
import mineplex.core.gadget.event.PlayerUseCoalEvent;
import mineplex.core.gadget.gadgets.Ammo;
import mineplex.core.gadget.gadgets.hat.HatType;
import mineplex.core.gadget.gadgets.particle.ParticleCoalFumes;
import mineplex.core.gadget.types.ItemGadget;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.pet.PetType;
import mineplex.core.recharge.Recharge;
import mineplex.core.reward.RewardData;
import mineplex.core.reward.rewards.PetReward;
import mineplex.core.treasure.reward.RewardRarity;

public class ItemCoal extends ItemGadget
{
	private int _hat = 250;
	private int _pet = 500;
	private int _particle = 1000;

	public String[] Messages = new String[]
			{
			"Maybe you could.... eat it?",
			"Yep, you're holding some coal...",
			"This sure is a nice piece of coal!",
			"Na na na na, Na na na na, " + C.cDGray + "BLACK COAL" + C.cGray + "!",
			"Did you know that coal can be used for " + C.Scramble + "nothing" + C.mBody + "!",
			"Now... if only you had a furnace.",
			"I hope you didn’t miss any diamonds.",
			"With 9 of these you could make a block!",
			"Were you really that naughty this year?",
			"With a few more of these you could make a snowman face!",
			"Lava is hotter but Coal is quicker.",
			"What do you even need Coal for on a Minigame server?",
			"Maybe if I got more I could start a really big fire.",
			"Maybe you can give this to your siblings next Christmas.",
			"Did you know a diamond is formed from coal?",
			"Coal is a word that sounds weird if you say it too much.",
			"Who do you think mined the first block of coal?",
			"Maybe if you get enough, something cool will happen! Or perhaps not...",
			};

	public ItemCoal(GadgetManager manager)
	{
		super(manager, "Coal",
				UtilText.splitLineToArray(C.cGray + "Just a large chunk of coal. Maybe you were naughty or something?", LineFormat.LORE)
				, -1, Material.COAL, (byte) 0, 1000, new Ammo("Coal", "1 Piece of Coal", Material.COAL, (byte) 0, new String[]
						{
					C.cDGray + "Exclusive Coal!",
					C.cDGray + "Earned by being naughty"
						}
				, -1, 1));
	}

	@Override
	public void ActivateCustom(final Player player)
	{
		int goal = -1;
		
		//Coal Hat
		if (!Manager.getHatGadget(HatType.COAL).ownsGadget(player))
		{
			goal = _hat;
			
			if(Manager.getInventoryManager().Get(player).getItemCount(getName()) >= _hat)
			{
				Recharge.Instance.recharge(player, getName());
				Recharge.Instance.use(player, getName(), 30000, true, true);
				
				Manager.getInventoryManager().addItemToInventory(new Callback<Boolean>()
						{
					@Override
					public void run(Boolean data)
					{
						if(data)
						{
							Bukkit.broadcastMessage(F.main("Treasure", C.cGreen + player.getName() + C.cGray + " crafted " + C.cGreen + "Legendary Coal Hat"));
							UtilParticle.PlayParticleToAll(ParticleType.LARGE_SMOKE, player.getLocation().add(0, 0.5, 0), 0.1f, 0.5f, 0.1f, 0, 100, ViewDist.LONG);
							UtilParticle.PlayParticleToAll(ParticleType.LARGE_SMOKE, player.getLocation(), 0.5f, 0.0f, 0.5f, 0, 100, ViewDist.LONG);
							player.getWorld().playSound(player.getLocation(), Sound.WITHER_DEATH, 0.8f, 0);

							Manager.getInventoryManager().addItemToInventory(player, getName(), -_hat);
							player.getInventory().setItem(Manager.getActiveItemSlot(), ItemStackFactory.Instance.CreateStack(getDisplayMaterial(), getDisplayData(), 1, F.item(Manager.getInventoryManager().Get(player).getItemCount(getName()) + " " + getName())));
							
							Manager.getDonationManager().Get(player).addOwnedUnknownSalesPackage("Lump of Coal Hat");

							UtilServer.CallEvent(new PlayerUseCoalEvent(player, PlayerUseCoalEvent.CoalReward.HAT, _particle));
						} 
						else 
						{
							player.sendMessage("Something went wrong...");
						}
					}
						}, player, "Lump of Coal Hat", 1);
				return;
			}
		}

		//Coal Apparition
		if (!Manager.getPetManager().Get(player).getPets().containsKey(PetType.PIG_ZOMBIE))
		{
			goal = _pet;
			
			if(Manager.getInventoryManager().Get(player).getItemCount(getName()) >= _pet)
			{
				Recharge.Instance.recharge(player, getName());
				Recharge.Instance.use(player, getName(), 30000, true, true);
				
				PetReward reward = new PetReward(
						"Coal Apparition", PetType.PIG_ZOMBIE, RewardRarity.LEGENDARY, 0);

				if (reward.canGiveReward(player))
					reward.giveReward(player, new Callback<RewardData>()
							{
						@Override
						public void run(RewardData data)
						{
							Bukkit.broadcastMessage(F.main("Treasure", C.cGreen + player.getName() + C.cGray + " crafted " + C.cGreen + "Legendary Coal Apparition Pet"));
							UtilParticle.PlayParticleToAll(ParticleType.LARGE_SMOKE, player.getLocation().add(0, 0.5, 0), 0.15f, 0.5f, 0.15f, 0, 250, ViewDist.LONG);
							UtilParticle.PlayParticleToAll(ParticleType.LARGE_SMOKE, player.getLocation(), 1f, 0.0f, 1f, 0, 250, ViewDist.LONG);
							player.getWorld().playSound(player.getLocation(), Sound.WITHER_DEATH, 0.8f, 0);

							Manager.getInventoryManager().addItemToInventory(player, getName(), -_pet);
							player.getInventory().setItem(Manager.getActiveItemSlot(), ItemStackFactory.Instance.CreateStack(getDisplayMaterial(), getDisplayData(), 1, F.item(Manager.getInventoryManager().Get(player).getItemCount(getName()) + " " + getName())));
							
							Manager.getDonationManager().Get(player).addOwnedUnknownSalesPackage("Coal Apparition");

							UtilServer.CallEvent(new PlayerUseCoalEvent(player, PlayerUseCoalEvent.CoalReward.PET, _particle));
						}
							});

				return;
			}
		}

		//Coal Particle
		if (!Manager.getGadget(ParticleCoalFumes.class).ownsGadget(player))
		{
			goal = _particle;
			
			if(Manager.getInventoryManager().Get(player).getItemCount(getName()) >= _particle)
			{
				Recharge.Instance.recharge(player, getName());
				Recharge.Instance.use(player, getName(), 30000, true, true);
				
				Manager.getInventoryManager().addItemToInventory(new Callback<Boolean>()
						{
					@Override
					public void run(Boolean data)
					{
						if(data)
						{
							Bukkit.broadcastMessage(F.main("Treasure", C.cRed + player.getName() + C.cGray + " crafted " + C.cRed + "Mythical Coal Fumes Particles"));
							UtilParticle.PlayParticleToAll(ParticleType.LARGE_SMOKE, player.getLocation().add(0, 0.5, 0), 0.2f, 0.5f, 0.2f, 0, 500, ViewDist.LONG);
							UtilParticle.PlayParticleToAll(ParticleType.LARGE_SMOKE, player.getLocation(), 1.5f, 0.0f, 1.5f, 0, 500, ViewDist.LONG);
							player.getWorld().playSound(player.getLocation(), Sound.WITHER_DEATH, 0.8f, 0);

							Manager.getInventoryManager().addItemToInventory(player, getName(), -_particle);
							player.getInventory().setItem(Manager.getActiveItemSlot(), ItemStackFactory.Instance.CreateStack(getDisplayMaterial(), getDisplayData(), 1, F.item(Manager.getInventoryManager().Get(player).getItemCount(getName()) + " " + getName())));
							
							Manager.getDonationManager().Get(player).addOwnedUnknownSalesPackage("Coal Fumes");

							UtilServer.CallEvent(new PlayerUseCoalEvent(player, PlayerUseCoalEvent.CoalReward.PARTICLE, _particle));
						} 
						else 
						{
							player.sendMessage("Something went wrong...");
						}
					}
						}, player, "Coal Fumes", 1);
				return;
			}
		}

		goal -= Manager.getInventoryManager().Get(player).getItemCount(getName());
		
		if (goal > 0 && Math.random() > 0.95)	
		{
			player.sendMessage(F.main("Coal", "Only " + goal + " to go..."));
			
			player.playSound(player.getLocation(), Sound.IRONGOLEM_DEATH, 0.2f, 0.5f);
		}
		else
		{

			int i = UtilMath.r(Messages.length);
			String msg = Messages[i];
			player.sendMessage(F.main("Coal", msg));
			
			player.playSound(player.getLocation(), Sound.GHAST_FIREBALL, 0.2f, 1.5f*(i/(float)Messages.length));
		}
	}

	@EventHandler @Override
	public void Activate(PlayerInteractEvent event)
	{
		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		if (UtilBlock.usable(event.getClickedBlock()))
			return;

		if (!UtilGear.isMat(event.getPlayer().getItemInHand(), this.getDisplayMaterial()))
			return;

		Player player = event.getPlayer();

		if (!isActive(player))
			return;

		event.setCancelled(true); 

		//Stock
		if (Manager.getInventoryManager().Get(player).getItemCount(getName()) <= 0)
		{

			UtilPlayer.message(player, F.main("Gadget", "You do not have any " + getName() + " left."));

			ItemGadgetOutOfAmmoEvent ammoEvent = new ItemGadgetOutOfAmmoEvent(event.getPlayer(), this);
			Bukkit.getServer().getPluginManager().callEvent(ammoEvent);

			return;
		}

		//Recharge
		if (!Recharge.Instance.use(player, getName(), getName(), _recharge, _recharge > 1000, true, false, true, "Cosmetics"))
		{
			UtilInv.Update(player);
			return;	
		}

		player.getInventory().setItem(Manager.getActiveItemSlot(), ItemStackFactory.Instance.CreateStack(getDisplayMaterial(), getDisplayData(), 1, F.item(Manager.getInventoryManager().Get(player).getItemCount(getName()) + " " + getName())));

		ActivateCustom(event.getPlayer());
	}

}
