package com.lying.tricksy.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import com.lying.tricksy.TricksyFoxes;

public abstract class Config
{
	private String fileName;
	
	protected Config(String fileIn)
	{
		this.fileName = fileIn;
	}
	
	protected abstract Properties getDefaults();
	
	protected void readValues(Properties valuesIn) { }
	
	public void read()
	{
		Properties values = new Properties(getDefaults());
		
		try
		{
			FileReader reader = new FileReader(fileName);
			values.load(reader);
			reader.close();
			
		}
		catch(FileNotFoundException e)
		{
			TricksyFoxes.LOGGER.error("Generated new config file "+fileName);
			save();
			return;
		}
		catch(IOException e)
		{
			TricksyFoxes.LOGGER.error("Couldn't read config file "+fileName);
			e.printStackTrace();
		}
		
		readValues(values);
	}
	
	protected void writeValues(FileWriter writer) { }
	
	public void save()
	{
		try
		{
			File file = new File(fileName);
			boolean fileExists = file.exists();
			File directory = file.getParentFile();
			if(!directory.exists())
				directory.mkdirs();
			
			FileWriter writer = new FileWriter(file);
			writeValues(writer);
			writer.close();
			if(!fileExists)
				TricksyFoxes.LOGGER.info("Generated new config file "+fileName);
		}
		catch(IOException e)
		{
			TricksyFoxes.LOGGER.error("Couldn't read config file "+fileName);
			e.printStackTrace();
		}
	}
	
	protected static void writeBool(FileWriter writer, String name, boolean value)
	{
		try
		{
			writer.write(name+"="+(value ? "1" : "0")+'\n');
		}
		catch(Exception e) { }
	}
	
	protected static boolean parseBoolOr(String name, boolean val)
	{
		try
		{
			return Integer.parseInt(name) > 0;
		}
		catch(NumberFormatException e) { return val; }
	}
	
	protected static void writeInt(FileWriter writer, String name, int value)
	{
		try
		{
			writer.write(name+"="+Integer.valueOf(value)+'\n');
		}
		catch(Exception e) { }
	}
	
	protected static int parseIntOr(String name, int val)
	{
		try
		{
			return Integer.parseInt(name);
		}
		catch(NumberFormatException e) { return val; }
	}
	
	protected static void writeString(FileWriter writer, String name, String value)
	{
		try
		{
			writer.write(name+"="+value);
		}
		catch(Exception e) { }
	}
	
	protected static String parseStringOr(String name, String val)
	{
		try
		{
			return name;
		}
		catch(Exception e) { return val; }
	}
}
