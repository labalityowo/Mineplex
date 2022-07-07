package mineplex.core.creature.command;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Guardian;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;

import mineplex.core.command.MultiCommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.creature.Creature;

public class MobCommand extends MultiCommandBase<Creature>
{
	public MobCommand(Creature plugin)
	{
		super(plugin, Creature.Perm.MOB_COMMAND, "mob");
		
		AddCommand(new KillCommand(Plugin));
	}

	@Override
	protected void Help(Player caller, String[] args)
	{
		if (args == null || args.length == 0)
		{
			Map<EntityType, Integer> entMap = new HashMap<>();
	
			int count = 0;
			for (World world : UtilServer.getServer().getWorlds())
			{
				for (Entity ent : world.getEntities())
				{
					if (!entMap.containsKey(ent.getType()))
						entMap.put(ent.getType(), 0);
	
					entMap.put(ent.getType(), 1 + entMap.get(ent.getType()));
					count++;
				}
			}
	
			UtilPlayer.message(caller, F.main(Plugin.getName(), "Listing Entities:"));
			for (EntityType cur : entMap.keySet())
			{
				UtilPlayer.message(caller, F.desc(UtilEnt.getName(cur), entMap.get(cur)+""));
			}
			
			UtilPlayer.message(caller, F.desc("Total", count+""));
		}
		else
		{
			EntityType type = UtilEnt.searchEntity(caller, args[0], true);

			if (type == null)
				return;

			UtilPlayer.message(caller, F.main(Plugin.getName(), "Spawning Creature(s);"));
			
			//Store Args
			Set<String> argSet = new HashSet<>();
			for (int i = 1 ; i < args.length ; i++)
				if (args[i].length() > 0)
					argSet.add(args[i]);
				

			//Search Count
			int count = 1;
			Set<String> argHandle = new HashSet<>();
			for (String arg : argSet)
			{
				try
				{
					int newCount = Integer.parseInt(arg);

					if (newCount <= 0)
						continue;

					//Set Count
					count = newCount;
					UtilPlayer.message(caller, F.desc("Amount", count+""));

					//Flag Arg
					argHandle.add(arg);
					break;
				}
				catch (Exception e)
				{
					//None
				}
			}
			for (String arg : argHandle)
				argSet.remove(arg);
			
			//Spawn
			Set<Entity> entSet = new HashSet<>();
			for (int i = 0 ; i < count ; i++)
			{
				entSet.add(Plugin.SpawnEntity(caller.getTargetBlock((Set<Material>) null, 150).getLocation().add(0.5, 1, 0.5), type));
			}

			//Search Vars
			for (String arg : argSet)
			{
				if (arg.length() == 0)
					continue;

				//Baby
				else if (arg.equalsIgnoreCase("baby") || arg.equalsIgnoreCase("b"))
				{
					for (Entity ent : entSet)
					{
						if (ent instanceof Ageable)
							((Ageable)ent).setBaby();
						else if (ent instanceof Zombie)
							((Zombie)ent).setBaby(true);
					}
					
					UtilPlayer.message(caller, F.desc("Baby", "True"));
					argHandle.add(arg);
				}
				
				//Lock
				else if (arg.equalsIgnoreCase("age") || arg.equalsIgnoreCase("lock"))
				{
					for (Entity ent : entSet)
						if (ent instanceof Ageable)
						{
							((Ageable)ent).setAgeLock(true);
							UtilPlayer.message(caller, F.desc("Age", "False"));
						}					
					
					argHandle.add(arg);
				}
				
				//Angry
				else if (arg.equalsIgnoreCase("angry") || arg.equalsIgnoreCase("a"))
				{
					for (Entity ent : entSet)
						if (ent instanceof Wolf)
							((Wolf)ent).setAngry(true);
					
					for (Entity ent : entSet)
						if (ent instanceof Skeleton)
							((Skeleton)ent).setSkeletonType(SkeletonType.WITHER);
					
					UtilPlayer.message(caller, F.desc("Angry", "True"));
					argHandle.add(arg);
				}

				else if (arg.equalsIgnoreCase("elder"))
				{
					for (Entity ent : entSet)
						if (ent instanceof Guardian)
							((Guardian)ent).setElder(true);

					UtilPlayer.message(caller, F.desc("Elder", "True"));
					argHandle.add(arg);
				}
				
				//Profession
				else if (arg.toLowerCase().charAt(0) == 'p')
				{
					try
					{
						String prof = arg.substring(1, arg.length());

						Profession profession = null;
						for (Profession cur : Profession.values())
							if (cur.name().toLowerCase().contains(prof.toLowerCase()))
								profession = cur;
		
						UtilPlayer.message(caller, F.desc("Profession", profession.name()));
						
						for (Entity ent : entSet)
							if (ent instanceof Villager)
								((Villager)ent).setProfession(profession);			
					}
					catch (Exception e)
					{
						UtilPlayer.message(caller, F.desc("Profession", "Invalid [" + arg + "] on " + type.name()));
					}
					argHandle.add(arg);
				}
				
				//Size
				else if (arg.toLowerCase().charAt(0) == 's')
				{
					try
					{
						String size = arg.substring(1, arg.length());
						
						UtilPlayer.message(caller, F.desc("Size", Integer.parseInt(size)+""));
						
						for (Entity ent : entSet)
							if (ent instanceof Slime)
								((Slime)ent).setSize(Integer.parseInt(size));
					}
					catch (Exception e)
					{
						UtilPlayer.message(caller, F.desc("Size", "Invalid [" + arg + "] on " + type.name()));
					}
					argHandle.add(arg);
				}
				
				else if (arg.toLowerCase().charAt(0) == 'n' && arg.length() > 1)
				{
					try
					{
						String name = "";
						
						for (char c : arg.substring(1, arg.length()).toCharArray())
						{
							if (c != '_')
								name += c;
							else
								name += " ";
						}
					
						for (Entity ent : entSet)
						{
							if (ent instanceof CraftLivingEntity)
							{
								CraftLivingEntity cEnt = (CraftLivingEntity)ent;
								cEnt.setCustomName(name); 
								cEnt.setCustomNameVisible(true);
							}
						}
					}
					catch (Exception e)
					{
						UtilPlayer.message(caller, F.desc("Size", "Invalid [" + arg + "] on " + type.name()));
					}
					argHandle.add(arg);	
				}

				else if (arg.equalsIgnoreCase("wither"))
				{
					for (Entity ent : entSet)
					{
						if (ent instanceof Skeleton)
						{
							((Skeleton) ent).setSkeletonType(SkeletonType.WITHER);
						}
					}

					argHandle.add(arg);
				}
			}
			for (String arg : argHandle)
				argSet.remove(arg);
			
			for (String arg : argSet)
				UtilPlayer.message(caller, F.desc("Unhandled", arg));
			
			//Inform
			UtilPlayer.message(caller, F.main(Plugin.getName(), "Spawned " + count + " " + UtilEnt.getName(type) + "."));
		}
	}
}