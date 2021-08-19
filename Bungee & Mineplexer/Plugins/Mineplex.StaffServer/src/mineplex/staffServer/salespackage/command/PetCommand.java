package mineplex.staffServer.salespackage.command;

import java.util.UUID;

import org.bukkit.entity.Player;

import com.google.gson.Gson;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.Callback;
import mineplex.core.common.util.F;
import mineplex.core.pet.PetClient;
import mineplex.core.pet.PetType;
import mineplex.core.pet.repository.token.ClientPetTokenWrapper;
import mineplex.core.pet.repository.token.PetChangeToken;
import mineplex.staffServer.salespackage.SalesPackageManager;

public class PetCommand extends CommandBase<SalesPackageManager>
{
	public PetCommand(SalesPackageManager plugin)
	{
		super(plugin, SalesPackageManager.Perm.SALES_COMMAND, "pet");
	}

	@Override
	public void Execute(final Player caller, String[] args)
	{
		if (args.length < 2)
			return;

		final String playerName = args[0];
		int petId = Integer.parseInt(args[1]);

		if (petId < 0 || petId >= PetType.values().length)
		{
			caller.sendMessage(F.main(Plugin.getName(), "You have entered an invalid Pet Type."));
			return;
		}
		
		final PetType petType = PetType.values()[petId];
		
		Plugin.getClientManager().loadClientByName(playerName, client ->
		{
			final UUID uuid = Plugin.getClientManager().loadUUIDFromDB(playerName);

			if (uuid != null)
			{
				final Callback<PetClient> clientCallback = new Callback<PetClient>()
				{
					public void run(PetClient petClient)
					{
						if (petClient.getPets().containsKey(petType))
						{
							caller.sendMessage(F.main(Plugin.getName(), F.elem(playerName) + " already has that Pet!"));
						}
						else
						{
							PetChangeToken token = new PetChangeToken();
							token.AccountId = client.getAccountId();
							token.Name = playerName;
							token.PetType = petType.toString();
							token.PetName = petType.getName();
							
							Plugin.getPetRepo().AddPet(token);
							Plugin.getInventoryManager().addItemToInventoryForOffline(success ->
							{
								if (success)
								{
									caller.sendMessage(F.main(Plugin.getName(), F.elem(playerName) + " has been given their Pet!"));
								}
								else
								{
									caller.sendMessage(F.main(Plugin.getName(), "Attempt to give Pet has failed!"));
								}
							}, client.getAccountId(), petType.toString(), 1);
						}
					}
				};
				
				Plugin.runAsync(() ->
				{
					Gson gson = new Gson();
					ClientPetTokenWrapper token = null;
					String response = Plugin.getClientManager().getRepository().getClientByUUID(uuid);
					token = gson.fromJson(response, ClientPetTokenWrapper.class);
					
					PetClient petClient = new PetClient();
					petClient.load(token.DonorToken);
					
					clientCallback.run(petClient);
				});
			}
			else
			{
				caller.sendMessage(F.main(Plugin.getName(), "Couldn't find " + playerName + "'s account!"));
			}
		});
	}
}