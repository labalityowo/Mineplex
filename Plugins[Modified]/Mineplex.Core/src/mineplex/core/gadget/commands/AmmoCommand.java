package mineplex.core.gadget.commands;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.Gadget;
import mineplex.core.gadget.types.GadgetType;

public class AmmoCommand extends CommandBase<GadgetManager>
{
    private GadgetManager _plugin;

    public AmmoCommand(GadgetManager plugin)
    {
        super(plugin, GadgetManager.Perm.AMMO_COMMAND, "ammo");
        
        _plugin = plugin;
    }

    @Override
    public void Execute(Player caller, String[] args)
    {
        if (args.length == 2)
        {
            String gadgetName = args[0];
            String amount = args[1];
            Gadget gadget = getGadgetByName(gadgetName);
            if (gadget == null)
            {
                UtilPlayer.message(caller, F.main("Ammo", "Could not find gadget " + F.skill(gadgetName)));
            }
            else
            {
                addAmmo(caller, caller, gadget, amount);
            }
        }
        else if (args.length == 3)
        {
            String gadgetName = args[0];
            String amount = args[1];
            String targetName = args[2];
            Player target = UtilPlayer.searchExact(targetName);

            Gadget gadget = getGadgetByName(gadgetName);
            if (gadget == null)
            {
                UtilPlayer.message(caller, F.main("Ammo", "Could not find gadget " + F.skill(gadgetName)));
            }
            else
            {
                if (target == null)
                {
                    _plugin.getClientManager().loadClientByName(targetName, loadedProfile ->
                    {
                        if (loadedProfile != null)
                        {
                            addAmmo(caller, loadedProfile.GetPlayer(), gadget, amount);
                        }
                        else
                        {
                            UtilPlayer.message(caller, F.main("Ammo", "Could not find player " + F.name(targetName)));
                        }
                    });
                }
                else
                {
                    addAmmo(caller, target, gadget, amount);
                }
            }
        }
        else
        {
            UtilPlayer.message(caller, F.main("Ammo", "Usage: /ammo <gadget> <ammo> [player]"));
        }
    }

    private void addAmmo(Player caller, Player target, Gadget gadget, String amount)
    {
        try
        {
            int ammo = Integer.parseInt(amount);
            _plugin.getInventoryManager().addItemToInventory(target, gadget.getName(), ammo);
            UtilPlayer.message(caller, F.main("Ammo", "You gave " + F.elem(ammo + " Ammo") + " for the gadget " + F.skill(gadget.getName()) + " to " + F.name(target.getName()) + "."));
            if (target != null)
            {
                UtilPlayer.message(target, F.main("Ammo", F.name(caller.getName()) + " gave you " + F.elem(ammo + " Ammo") + " for the gadget " + F.skill(gadget.getName()) + "."));
            }
        }
        catch (Exception e)
        {
            UtilPlayer.message(caller, F.main("Ammo", "Invalid Ammo Amount"));
        }
    }

    private Gadget getGadgetByName(String gadgetName)
    {
        for (Gadget gadget : _plugin.getGadgets(GadgetType.ITEM))
        {
            if (gadget.getName().equalsIgnoreCase(gadgetName.replaceAll("_", " ")))
            {
                return gadget;
            }
        }
        return null;
    }
}