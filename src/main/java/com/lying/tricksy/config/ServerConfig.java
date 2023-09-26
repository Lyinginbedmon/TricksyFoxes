package com.lying.tricksy.config;

import java.io.FileWriter;
import java.util.Properties;

public class ServerConfig extends Config
{
	private static final Properties DEFAULT_SETTINGS = new Properties();
	
	private int nodeCap = 12;
	
	public ServerConfig(String fileIn)
	{
		super(fileIn);
	}
	
	protected Properties getDefaults() { return DEFAULT_SETTINGS; }
	
	protected void readValues(Properties valuesIn)
	{
		nodeCap = parseIntOr(valuesIn.getProperty("TreeNodeCap"), 12);
	}
	
	protected void writeValues(FileWriter writer)
	{
		writeInt(writer, "TreeNodeCap", nodeCap);
	}
	
	static
	{
		DEFAULT_SETTINGS.setProperty("TreeNodeCap", "12");
	}
}
