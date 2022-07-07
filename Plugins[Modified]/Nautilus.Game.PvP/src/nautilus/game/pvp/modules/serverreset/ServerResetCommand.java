package nautilus.game.pvp.modules.serverreset;

import java.io.File;
import java.io.FilenameFilter;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import mineplex.core.common.util.F;
import mineplex.core.Rank;
import me.chiss.Core.Commands.CommandBase;
import mineplex.minecraft.account.GetClientEvent;

public class ServerResetCommand extends CommandBase<ServerReset>
{
	public ServerResetCommand(ServerReset plugin)
	{
		super(plugin, Rank.OWNER, "serverreset");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		ClearEconomy();
		System.out.println(F.main(Plugin.getName(), caller.getName() + " cleared Economy."));
		caller.sendMessage(F.main(Plugin.getName(), "Cleared Economy."));
		
		ClearNpcs();
	}
	
	private void ClearNpcs()
	{
		new File("npcs.dat").delete();
	}
	
	private void ClearEconomy()
	{
		for (Player player : Bukkit.getOnlinePlayers())
		{
			GetClientEvent clientEvent = new GetClientEvent(player);
			
			Plugin.GetPluginManager().callEvent(clientEvent);
			
			clientEvent.GetClient().Game().SetEconomyBalance(0);
		}
		
		File economyDir = new File("shop/");
		
	    FilenameFilter statsFilter = new FilenameFilter() 
	    {
            public boolean accept(File paramFile, String paramString) 
            {
                if (paramString.endsWith("dat"))
                {
                    return true;
                }
                
                return false;
            }
	    };
	    
	    for (File f : economyDir.listFiles(statsFilter))
	    {
	    	f.delete();
	    }
	}
}
