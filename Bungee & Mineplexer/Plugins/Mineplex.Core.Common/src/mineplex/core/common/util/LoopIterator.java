package mineplex.core.common.util;

import java.util.List;

public class LoopIterator<T>
{
	private List<T> _list;
	private int _pointer;
	
	public LoopIterator(List<T> list)
	{
		_list = list;
	}
	
	public T next()
	{
		if (_list.isEmpty())
		{
			return null;
		}
		
		if (++_pointer == _list.size())
		{
			_pointer = 0;
		}
		
		return _list.get(_pointer);
	}
	
	public T peekNext()
	{
		if (_list.isEmpty())
		{
			return null;
		}
		
		int pointer = _pointer;
		
		if (++pointer == _list.size())
		{
			pointer = 0;
		}
		
		return _list.get(pointer);
	}
	
	public T peekPrev()
	{
		if (_list.isEmpty())
		{
			return null;
		}
		
		int pointer = _pointer;
		
		if (--pointer < 0)
		{
			pointer = _list.size() - 1;
		}
		
		return _list.get(pointer);
	}
	
	public T prev()
	{
		if (_list.isEmpty())
		{
			return null;
		}
		
		if (--_pointer < 0)
		{
			_pointer = _list.size() - 1;
		}
		
		return _list.get(_pointer);
	}

	public T current()
	{
		return _list.get(_pointer);
	}
}
