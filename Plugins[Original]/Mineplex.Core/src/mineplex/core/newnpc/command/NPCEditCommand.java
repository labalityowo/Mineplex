package mineplex.core.newnpc.command;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.skin.SkinData;
import mineplex.core.common.util.F;
import mineplex.core.newnpc.NewNPCManager;
import mineplex.core.newnpc.NewNPCManager.Perm;
import mineplex.core.newnpc.PlayerNPC;
import mineplex.core.newnpc.StoredNPC;
import mineplex.core.recharge.Recharge;
import mineplex.core.utils.UtilGameProfile;

public class NPCEditCommand extends CommandBase<NewNPCManager>
{

	public static final String RECHARGE_KEY = "Edit NPC";

	NPCEditCommand(NewNPCManager plugin)
	{
		super(plugin, Perm.EDIT_NPC_COMMAND, "edit");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args.length > 1)
		{
			String type = args[1];
			int id;

			try
			{
				id = Integer.parseInt(args[0]);
			}
			catch (NumberFormatException ex)
			{
				sendInvalidNPC(caller);
				return;
			}

			StoredNPC npc = Plugin.getNPC(id);

			if (npc == null)
			{
				sendInvalidNPC(caller);
				return;
			}

			switch (type)
			{
				case "info":
					npc.setInfo(Arrays.asList
							(
									Arrays.stream(args)
											.skip(2)
											.collect(Collectors.joining(" "))
											.split(StoredNPC.LINE_DELIMITER)
							));
					break;
				case "spawn":
					npc.setSpawn(caller.getLocation());
					break;
				case "equipment":
					npc.setEquipment(caller.getItemInHand(), caller.getInventory().getArmorContents());
					break;
				case "skin":
					if (npc instanceof PlayerNPC)
					{
						((PlayerNPC) npc).setSkinData(SkinData.constructFromGameProfile(UtilGameProfile.getGameProfile(caller), true, true));
					}
					else
					{
						sendInvalidNPC(caller);
					}
					break;
				case "update":
					Plugin.updateNPCProperties(npc);
					caller.sendMessage(F.main(Plugin.getName(), "Updated the properties of " + F.name(npc.getEntity().getName()) + "."));
					break;
			}
			return;
		}

		caller.sendMessage(F.main(Plugin.getName(), "Punch the NPC you wish to edit. You will have " + F.time("30 seconds") + " to do so."));
		Recharge.Instance.useForce(caller, RECHARGE_KEY, TimeUnit.SECONDS.toMillis(30));
	}

	private void sendInvalidNPC(Player caller)
	{
		caller.sendMessage(F.main(Plugin.getName(), "That is not a valid npc."));
	}
}
