package mineplex.core.itemstack;

import java.util.ArrayList;

import org.bukkit.inventory.ItemStack;

public class ItemLayout
{

    private int _invSize = 0;
    private ArrayList<Integer> _size = new ArrayList<Integer>();

    public ItemLayout(String... strings)
    {
        _invSize = strings.length * 9;
        for (int row = 0; row < strings.length; row++)
        {
            String string = strings[row];
            if (string.length() != 9)
                throw new IllegalArgumentException("String '" + string + "' does not a length of 9 but instead has a length of "
                        + string.length());
            char[] cArray = string.toCharArray();
            for (int slot = 0; slot < 9; slot++)
            {
                char letter = cArray[slot];
                if ('x' == Character.toLowerCase(letter))
                {
                    continue;
                }
                else if ('o' == Character.toLowerCase(letter))
                {
                    _size.add((row * 9) + slot);
                }
                else
                    throw new IllegalArgumentException("Unrecognised character " + letter);
            }
        }
    }

    public ArrayList<Integer> getItemSlots()
    {
        return _size;
    }

    public ItemStack[] generate(ArrayList<ItemStack> items)
    {
        return generate(items.toArray(new ItemStack[0]));
    }

    public ItemStack[] generate(ItemStack... items)
    {
        return generate(true, items);
    }

    public ItemStack[] generate(boolean doRepeats, ItemStack... items)
    {
        ItemStack[] itemArray = new ItemStack[_invSize];

        if (items.length == 0)
            return itemArray;

        int i = 0;
        for (int slot : _size)
        {
            if (i < items.length)
            {
                if (doRepeats)
                    i = 0;
                else
                    break;
            }

            itemArray[slot] = items[i];

        }
        return itemArray;
    }

}
