package nautilus.game.arcade.game.games.evolution.mobs;


public class KitSkeleton 
//extends EvoKit
{
	/**
	 * @author Mysticate
	 */
	
//	public KitSkeleton(ArcadeManager manager)
//	{
//		super(manager, "Skeletal Archer",
//				new String[] 
//						{
//				F.elem("Charge Bow") + " to use " + F.elem("Barrage")
//						}, 
//						new String[] 
//								{
//				F.elem("Charge Bow") + C.cWhite + " - " + F.elem("Barrage")
//								},
//						new Perk[] 
//								{
//				new PerkConstructor("Fletcher", 3, 2, Material.ARROW, "Fletched Arrow", false),
//				new PerkBarrage(5, 250, true, false)
//								}, EntityType.SKELETON);
//	}
//
//	@Override
//	public void GiveItems(Player player) 
//	{
//		player.getInventory().setHelmet(new ItemBuilder(Material.CHAINMAIL_HELMET).build());
//		player.getInventory().setChestplate(new ItemBuilder(Material.CHAINMAIL_CHESTPLATE).build());
//		player.getInventory().setLeggings(new ItemBuilder(Material.CHAINMAIL_LEGGINGS).build());
//		player.getInventory().setBoots(new ItemBuilder(Material.IRON_BOOTS).build());
//				
//		player.getInventory().addSkillItem(ItemStackFactory.Instance.CreateStack(Material.BOW));
//		
//		player.getWorld().playSound(player.getLocation(), Sound.SKELETON_IDLE, 4f, 1f);
//		
//		//Disguise
//		DisguiseSkeleton disguise = new DisguiseSkeleton(player);
//		disguise.setName(Manager.GetGame().GetTeam(player).GetColor() + player.getName());
//		disguise.setCustomNameVisible(true);
//		
//		Manager.GetDisguise().undisguise(player);
//		Manager.GetDisguise().disguise(disguise);
//		
//		player.getInventory().setItem(8, new ItemBuilder(Material.COMPASS).setTitle(F.item("Tracking Compass")).build());
//	}
//	
//	@EventHandler
//	public void onBowShoot(final EntityShootBowEvent event)
//	{
//		if (!(event.getEntity() instanceof Player))
//			return;
//
//		if (!Manager.GetGame().IsLive())
//			return;
//		
//		final Player player = (Player) event.getEntity();
//		
//		if (!HasKit(player))
//			return;
//		
//		EvolutionAbilityUseEvent useEvent = new EvolutionAbilityUseEvent(player);
//		Bukkit.getServer().getPluginManager().callEvent(useEvent);
//		
//		if (useEvent.isCancelled())
//		{
//			event.setCancelled(true);
//			
//			Manager.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(Manager.getPlugin(), new Runnable()
//			{
//				@Override
//				public void run()
//				{
//					UtilInv.Update(player);
//				}
//			}, 10);
//			return;
//		}
//	}
}
