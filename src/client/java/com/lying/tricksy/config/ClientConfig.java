package com.lying.tricksy.config;

import java.io.FileWriter;
import java.util.Properties;

public class ClientConfig extends Config
{
	private static final Properties DEFAULT_SETTINGS = new Properties();
	
	/** Renders behaviour trees in a more fanciful fashion, but with much higher CPU costs */
	private boolean fancyTrees = true;
	
	public ClientConfig(String fileIn)
	{
		super(fileIn);
	}
	
	protected Properties getDefaults() { return DEFAULT_SETTINGS; }
	
	protected void readValues(Properties valuesIn)
	{
		fancyTrees = parseBoolOr(valuesIn.getProperty("FancyTrees"), true);
	}
	
	protected void writeValues(FileWriter writer)
	{
		writeBool(writer, "FancyTrees", fancyTrees);
	}
	
	public boolean fancyTrees() { return this.fancyTrees; }
	
	static
	{
		DEFAULT_SETTINGS.setProperty("FancyTrees", "1");
	}
}
