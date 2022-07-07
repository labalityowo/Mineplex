package mineplex.core.pet;

import java.util.HashMap;
import java.util.Map;

import mineplex.core.pet.repository.token.ClientPetToken;
import mineplex.core.pet.repository.token.PetToken;

public class PetClient
{
    private Map<PetType, String> _pets = new HashMap<>();
    private int _petNameTagCount;
    
    public void load(ClientPetToken token)
    {
	    for (PetToken petToken : token.Pets)
	    {
			PetType type = PetType.valueOf(petToken.PetType);
			_pets.put(type, petToken.PetName != null ? petToken.PetName : type.getName());
	    }
	    
	    _petNameTagCount = Math.max(0, token.PetNameTagCount);
    }
    
	public Map<PetType, String> getPets()
	{
		return _pets;
	}

	public Integer GetPetNameTagCount()
	{
		return _petNameTagCount;
	}

	public void SetPetNameTagCount(int count)
	{
		_petNameTagCount = count;
	}
}
