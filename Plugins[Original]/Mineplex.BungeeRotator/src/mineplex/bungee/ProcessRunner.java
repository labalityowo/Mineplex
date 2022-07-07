package mineplex.bungee;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ProcessRunner extends Thread
{
	private ProcessBuilder _processBuilder;
	private Process _process;
	private GenericRunnable<Boolean> _runnable;
	
	boolean _done = false;
	Boolean _error = false;
	
	ProcessRunner(String[] args)
	{
		super("ProcessRunner " + args);
		_processBuilder = new ProcessBuilder(args);
	}
	
	public void run()
	{
		try
		{
			_process = _processBuilder.start();
			_process.waitFor();
			
            BufferedReader reader=new BufferedReader(new InputStreamReader(_process.getInputStream())); 
            String line = reader.readLine(); 
            
            while(line != null) 
            {
            	if (line.equals("255"))
            		_error = true;
            	
	            line=reader.readLine(); 
            } 
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
		}
		finally
		{
			_done = true;
			
			if (_runnable != null)
				_runnable.run(_error);
		}
	}
	
	public void start(GenericRunnable<Boolean> runnable)
	{
		super.start();
		
		_runnable = runnable;
	}
	
	public int exitValue() throws IllegalStateException
	{
		if (_process != null)  
		{
			return _process.exitValue();
	    }         
	    
		throw new IllegalStateException("Process not started yet");
	}
	
	public boolean isDone()
	{
		return _done;
	}
	
	public void abort()
	{
		if (!isDone())
		{
			_process.destroy();
		}
	}
 }
