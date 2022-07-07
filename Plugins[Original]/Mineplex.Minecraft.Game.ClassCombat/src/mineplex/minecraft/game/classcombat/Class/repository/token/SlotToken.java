package mineplex.minecraft.game.classcombat.Class.repository.token;

import org.bukkit.Material;

public class SlotToken 
{
	public String Name = "";
	public String Material = "";
	public int Amount = 0;
	
	public SlotToken() { }
	
	public SlotToken(String name, Material material, int amount)
	{
		Name = name;
		Material = material.name();
		Amount = amount;
	}

	public void printInfo()
	{
		System.out.println("Name : " + Name);
		System.out.println("Material : " + Material);
		System.out.println("Amount : " + Amount);
	}
}
