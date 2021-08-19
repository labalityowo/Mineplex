package nautilus.game.pvp.modules.clans;

import nautilus.game.pvp.modules.clans.ClansClan.Role;
import nautilus.game.pvp.modules.clans.ClansUtility.ClanRelation;

import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import me.chiss.Core.Combat.Event.CombatDeathEvent;
import mineplex.minecraft.game.core.classcombat.SkillTriggerEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import mineplex.core.blockrestore.BlockRestoreData;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilTime.TimeUnit;
import mineplex.minecraft.core.condition.Condition.ConditionType;
import mineplex.core.itemstack.ItemStackFactory;

public class ClansGame 
{
	private Clans Clans;
	public ClansGame(Clans clans)
	{
		Clans = clans;
	}

	//Player Chunk Display
	public void UpdateDisplay()
	{
		for (Player cur : UtilServer.getPlayers())
			Clans.CDisplay().Update(cur);
	}

	public void UpdateSafe()
	{
		for (Player cur : UtilServer.getPlayers())
			if (Clans.CUtil().isSafe(cur.getLocation()))
			{
				ClansClan clan = Clans.CUtil().getClanByPlayer(cur);
				if (clan != null)
					if (!clan.GetEnemyEvent().isEmpty())
					{
						UtilPlayer.message(cur, F.main("Safe Zone", "You are not safe during invasions."));
						return;
					}


				long lastDamager = Clans.Clients().Get(cur).Player().GetLastDamager();

				if (!UtilTime.elapsed(lastDamager, 15000))
				{
					UtilPlayer.message(cur, F.main("Safe Zone", "You are not safe for " + 
							F.time(UtilTime.convertString(15000 - (System.currentTimeMillis() - lastDamager), 1, TimeUnit.FIT))));

					Clans.Condition().Factory().Custom("Unsafe", cur, cur, ConditionType.CUSTOM, 1, 0, false, Material.FIRE, (byte)0, true);
				}
			}
	}

	public void SafeSkill(SkillTriggerEvent event)
	{
		if (!Clans.CUtil().isSafe(event.GetPlayer()))
			return;

		UtilPlayer.message(event.GetPlayer(), F.main("Safe Zone", "You cannot use " + F.skill(event.GetSkillName() + " in " + F.elem("Safe Zone") + ".")));

		event.SetCancelled(true);
	}

	public void BlockBurn(BlockBurnEvent event)
	{
		if (Clans.CUtil().isBorderlands(event.getBlock().getLocation()))
			event.setCancelled(true);
	}

	public void BlockSpread(BlockIgniteEvent event)
	{
		if (event.getCause() == IgniteCause.SPREAD)
			if (Clans.CUtil().isBorderlands(event.getBlock().getLocation()))
				event.setCancelled(true);
	}

	//Block Place (After Interact)
	public void BlockPlace(BlockPlaceEvent event)
	{
		if (event.getBlock().getType() != Material.LADDER)
			return;
		
		if (Clans.CUtil().getAccess(event.getPlayer(), event.getBlock().getLocation()) == ClanRelation.SELF)
			return;
		
		final Block block = event.getBlock();
		
		UtilServer.getServer().getScheduler().scheduleSyncDelayedTask(Clans.Plugin(), new Runnable()
		{
			public void run()
			{
				Clans.BlockRestore().Add(block, 65, block.getData(), 30000);
				
				BlockRestoreData data = Clans.BlockRestore().GetData(block);
				if (data != null)
				{
					data.setFromId(0);
					data.setFromData((byte)0);
				}
			}
		}, 0);
		
	}

	//Block Break
	public void BlockBreak(BlockBreakEvent event)
	{
		if (event.getPlayer().getWorld().getEnvironment() != Environment.NORMAL)
			return;
		
		//Borderlands
		if (Clans.CUtil().isBorderlands(event.getBlock().getLocation()) && event.getPlayer().getGameMode() != GameMode.CREATIVE)
		{
			//Disallow
			event.setCancelled(true);

			//Inform
			UtilPlayer.message(event.getPlayer(), F.main("Clans", "You can not break " +
					F.elem(ItemStackFactory.Instance.GetName(event.getBlock(), true)) +
					" in " + 
					F.elem("Borderlands") +
					"."));
			return;
		}

		if (Clans.CBlocks().canBreak(event.getBlock().getTypeId()))
			return;

		if (Clans.CUtil().getAccess(event.getPlayer(), event.getBlock().getLocation()) == ClanRelation.SELF)
		{
			//Disallow Shops
			if (event.getBlock().getType() == Material.ENDER_CHEST || event.getBlock().getType() == Material.ENCHANTMENT_TABLE)
				if (Clans.CUtil().isSafe(event.getBlock().getLocation()))
				{
					//Disallow
					event.setCancelled(true);

					//Inform
					UtilPlayer.message(event.getPlayer(), F.main("Clans", "You can not break " +
							F.elem(ItemStackFactory.Instance.GetName(event.getBlock(), true)) +
							" in " + 
							Clans.CUtil().getOwnerStringRel(event.getBlock().getLocation(), event.getPlayer().getName()) +
							"."));
				}

			//Disallow Recruit Chest
			if (Clans.CUtil().isClaimed(event.getBlock().getLocation()))
				if (event.getBlock().getTypeId() == 54)
				{
					if (Clans.CUtil().getRole(event.getPlayer()) == Role.RECRUIT)
					{
						//Disallow
						event.setCancelled(true);

						//Inform
						UtilPlayer.message(event.getPlayer(), F.main("Clans", "Clan Recruits cannot break " +
								F.elem(ItemStackFactory.Instance.GetName(event.getBlock(), true)) + "."));
					}
				}

			//Allow
			return;
		}

		//Disallow
		event.setCancelled(true);

		//Inform
		UtilPlayer.message(event.getPlayer(), F.main("Clans", "You can not break " +
				F.elem(ItemStackFactory.Instance.GetName(event.getBlock(), true)) +
				" in " + 
				Clans.CUtil().getOwnerStringRel(event.getBlock().getLocation(), event.getPlayer().getName()) +
				"."));
	}

	public void Damage(CustomDamageEvent event)
	{
		Player damagee = event.GetDamageePlayer();	
		if (damagee == null)	return;

		if (Clans.CUtil().isSafe(damagee))
			event.SetCancelled("Safe Zone");	

		Player damager = event.GetDamagerPlayer(true);
		if (damager == null)	return;

		if (!Clans.CUtil().canHurt(damagee, damager))
		{
			//Cancel
			event.SetCancelled("Clans Ally");

			//Inform
			if (damager != null)
			{
				ClanRelation rel = Clans.GetRelation(damagee.getName(), damager.getName());
				UtilPlayer.message(damager, F.main("Clans", 
						"You cannot harm " + Clans.CUtil().mRel(rel, damagee.getName(), false) + "."));
			}		
		}
	}

	public void DeathDominance(CombatDeathEvent event)
	{
		if (!(event.GetEvent().getEntity() instanceof Player))
			return;

		Player player = (Player)event.GetEvent().getEntity();

		ClansClan clan = Clans.CUtil().getClanByPlayer(player);
		if (clan == null)
			return;

		//Lose Power
		if (Clans.IsPowerEnabled())
			clan.modifyPower(-1);

		//Dominance
		if (clan.GetDateCreated() < 86400000)
			return;

		if (event.GetLog().GetKiller() == null || !event.GetLog().GetKiller().IsPlayer())	
		{
			ClansClan territoryClan = Clans.CUtil().getOwner(player.getLocation());

			if (territoryClan == null)
				return;

			if (territoryClan.GetDateCreated() < 86400000)
				return;

			if (!territoryClan.isOnlineNow())
				return;

			//Add Dominance
			territoryClan.gainDominance(clan);
		}
		else
		{
			ClansClan killerClan = Clans.CUtil().getClanByPlayer(event.GetLog().GetKiller().getName());

			if (killerClan == null)
				return;

			if (killerClan.IsAdmin())
				return;

			if (killerClan.GetDateCreated() < 86400000)
				return;

			if (!killerClan.isOnlineNow())
				return;

			//Add Dominance
			killerClan.gainDominance(clan);
		}
	}

	public void DeathColor(CombatDeathEvent event) 
	{
		event.GetLog().SetClans(true);
	}

	//Block Interact and Placement
	public void Interact(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();

		if (player.getWorld().getEnvironment() != Environment.NORMAL)
			return;

		if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK)
			return;

		//Block Interaction
		Location loc = event.getClickedBlock().getRelative(event.getBlockFace()).getLocation();
		if (UtilBlock.usable(event.getClickedBlock()))		loc = event.getClickedBlock().getLocation();
		
		//Borderlands
		if (	player.getGameMode() != GameMode.CREATIVE &&
				player.getItemInHand() != null && 
				Clans.CBlocks().denyUsePlace(player.getItemInHand().getTypeId()) &&
				Clans.CUtil().isBorderlands(event.getClickedBlock().getLocation()))
		{
			//Disallow
			event.setCancelled(true);

			//Inform
			UtilPlayer.message(player, F.main("Clans", "You cannot use/place " +
					F.elem(ItemStackFactory.Instance.GetName(event.getClickedBlock(), true)) +
					" in " + 
					F.elem("Borderlands") +
					"."));

			return;
		}

		ClanRelation access = Clans.CUtil().getAccess(player, loc);
		
		//Hoe Return
		if (access != ClanRelation.SELF && !UtilBlock.usable(event.getClickedBlock()))
		{
			if (Clans.Util().Gear().isHoe(player.getItemInHand()))
			{
				event.setCancelled(true);
				return;
			}
		}

		//Full Access
		if (access == ClanRelation.SELF)
		{
			//Recruits cannot open Chests
			if (event.getClickedBlock().getTypeId() == 54)
			{
				if (Clans.CUtil().getRole(player) == Role.RECRUIT)
				{
					//Disallow
					event.setCancelled(true);

					//Inform
					UtilPlayer.message(player, F.main("Clans", "Clan Recruits cannot access " +
							F.elem(ItemStackFactory.Instance.GetName(event.getClickedBlock(), true)) +
							"."));
				}
			}

			//Wilderness Adjacent
			if (	event.getAction() == Action.RIGHT_CLICK_BLOCK && 
					!UtilBlock.usable(event.getClickedBlock()) &&
					player.getItemInHand() != null && 
					Clans.CBlocks().denyUsePlace(player.getItemInHand().getTypeId()) &&
					!Clans.CUtil().isClaimed(loc))
			{

				String enemy = null;
				boolean self = false;

				for (int x=-1 ; x<=1 ; x++)
					for (int z=-1 ; z<=1 ; z++)
					{
						if (self)
							continue;

						if (x != 0 && z != 0 || x == 0 && z == 0)
							continue;

						Location sideLoc = new Location(loc.getWorld(), loc.getX()+x, loc.getY(), loc.getZ()+z);

						if (Clans.CUtil().isSelf(player.getName(), sideLoc))
							self = true;

						if (enemy != null)
							continue;

						if (Clans.CUtil().getAccess(player, sideLoc) != ClanRelation.SELF)
							enemy = Clans.CUtil().getOwnerStringRel(
									new Location(loc.getWorld(), loc.getX()+x, loc.getY(), loc.getZ()+z), 
									player.getName());
					}

				if (enemy != null && !self)
				{
					//Disallow
					event.setCancelled(true);

					//Inform
					UtilPlayer.message(player, F.main("Clans", "You cannot use/place " +
							F.elem(ItemStackFactory.Instance.GetName(player.getItemInHand(), true)) +
							" next to " + 
							enemy +
							"."));

					return;
				}
			}

			return;
		}


		//Deny Interaction
		if (Clans.CBlocks().denyInteract(event.getClickedBlock().getTypeId()))
		{
			//Block Action
			if (access == ClanRelation.NEUTRAL)
			{
				//Allow Field Chest
				if (event.getClickedBlock().getTypeId() == 54)
					if (Clans.CUtil().isSpecial(event.getClickedBlock().getLocation(), "Fields"))
						return;

				//Disallow
				event.setCancelled(true);

				//Inform
				UtilPlayer.message(player, F.main("Clans", "You cannot use " +
						F.elem(ItemStackFactory.Instance.GetName(event.getClickedBlock(), true)) +
						" in " + 
						Clans.CUtil().getOwnerStringRel(event.getClickedBlock().getLocation(), 
								player.getName()) +
						"."));

				return;
			}
			//Block is not Trust Allowed
			else if (!Clans.CBlocks().allowInteract(event.getClickedBlock().getTypeId()) || access != ClanRelation.ALLY_TRUST)
			{
				//Disallow
				event.setCancelled(true);

				//Inform
				UtilPlayer.message(player, F.main("Clans", "You cannot use " +
						F.elem(ItemStackFactory.Instance.GetName(event.getClickedBlock(), true)) +
						" in " + 
						Clans.CUtil().getOwnerStringRel(event.getClickedBlock().getLocation(), 
								player.getName()) +
						"."));

				return;
			}	
		}

		//Block Placement
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
			if (player.getItemInHand().getType() != Material.AIR)
				if (Clans.CBlocks().denyUsePlace(player.getItemInHand().getTypeId()))
				{
					//Disallow
					event.setCancelled(true);

					//Inform
					UtilPlayer.message(player, F.main("Clans", "You cannot use/place " +
							F.elem(ItemStackFactory.Instance.GetName(player.getItemInHand(), true)) +
							" in " + 
							Clans.CUtil().getOwnerStringRel(event.getClickedBlock().getRelative(event.getBlockFace()).getLocation(), 
									player.getName()) +
							"."));

					return;
				}
	}



	public void Join(PlayerJoinEvent event)
	{
		Clans.CUtil().updateRelations(event.getPlayer());
	}

	public void Piston(BlockPistonExtendEvent event)
	{
		ClansClan pistonClan = Clans.CUtil().getOwner(event.getBlock().getLocation());

		Block push = event.getBlock();
		for (int i=0 ; i<13 ; i++)
		{
			push = push.getRelative(event.getDirection());

			Block front = push.getRelative(event.getDirection()).getRelative(event.getDirection());

			if (push.getType() == Material.AIR)
				return;

			ClansClan pushClan = Clans.CUtil().getOwner(front.getLocation());

			if (pushClan == null)
				continue;

			if (pushClan.IsAdmin())
				continue;

			if (pistonClan == null)
			{
				push.getWorld().playEffect(push.getLocation(), Effect.STEP_SOUND, push.getTypeId());
				event.setCancelled(true);
				return;
			}

			if (pistonClan.equals(pushClan))
				continue;

			push.getWorld().playEffect(push.getLocation(), Effect.STEP_SOUND, push.getTypeId());
			event.setCancelled(true);
			return;
		}			
	}

	public void Quit(PlayerQuitEvent event)
	{
		ClansClan clan = Clans.CUtil().getClanByPlayer(event.getPlayer());
		if (clan == null)		return;

		clan.SetLastOnline(System.currentTimeMillis());
	}

	public void Explode(EntityExplodeEvent event)
	{
		try
		{
			if (event.getEntityType() != EntityType.PRIMED_TNT && event.getEntityType() != EntityType.MINECART_TNT)
				return;
		}
		catch (Exception e)
		{
			return;
		}

		ClansClan clan = Clans.CUtil().getOwner(event.getEntity().getLocation());
		if (clan == null)		return;

		if (!clan.isOnline())
			event.setCancelled(true);
		else
			clan.inform(C.cRed + "Your Territory is under attack!", null);
	}
}
