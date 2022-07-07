package mineplex.game.clans.items.attributes;

import java.util.HashSet;
import java.util.Set;

public class AttributeContainer 
{

	private ItemAttribute _superPrefix;
	public ItemAttribute getSuperPrefix() { return _superPrefix; }
	public void setSuperPrefix(ItemAttribute attribute) { _superPrefix = attribute; }
	 
	private ItemAttribute _prefix;
	public ItemAttribute getPrefix() { return _prefix; }
	public void setPrefix(ItemAttribute attribute) { _prefix = attribute; }
	
	private ItemAttribute _suffix;
	public ItemAttribute getSuffix() { return _suffix; }
	public void setSuffix(ItemAttribute attribute) { _suffix = attribute; }
	
	public AttributeContainer(ItemAttribute superPrefix, ItemAttribute prefix, ItemAttribute suffix)
	{
		_superPrefix = superPrefix;
		_prefix = prefix;
		_suffix = suffix;
	}
	
	public AttributeContainer()
	{
		this(null, null, null);
	}
	
	public Set<ItemAttribute> getAttributes()
	{
		Set<ItemAttribute> attributes = new HashSet<ItemAttribute>();
		
		if (_superPrefix != null) attributes.add(_superPrefix);
		if (_prefix != null) attributes.add(_prefix);
		if (_suffix != null) attributes.add(_suffix);
		
		return attributes;
	}
	
	public Set<AttributeType> getRemainingTypes()
	{
		Set<AttributeType> remainingTypes = new HashSet<AttributeType>();
		
		if (_superPrefix == null) remainingTypes.add(AttributeType.SUPER_PREFIX);
		if (_prefix == null) remainingTypes.add(AttributeType.PREFIX);
		if (_suffix == null) remainingTypes.add(AttributeType.SUFFIX);
		
		return remainingTypes;
	}
	
	public String formatItemName(String displayName)
	{
		String itemName = displayName;
		
		if (_prefix != null)
		{
			itemName = _prefix.getDisplayName() + " " + itemName;
		} 
		
		if (_superPrefix != null)
		{
			itemName = _superPrefix.getDisplayName() + " " + itemName;
		}
		
		if (_suffix != null)
		{
			itemName += " of " + _suffix.getDisplayName();
		}
		
		return itemName;
	}
	
	public void addAttribute(ItemAttribute attribute)
	{
		switch(attribute.getType())
		{
		case SUPER_PREFIX:
			setSuperPrefix(attribute);
			break;
		case PREFIX:
			setPrefix(attribute);
			break;
		case SUFFIX:
			setSuffix(attribute);
			break;
		}
	}
}
