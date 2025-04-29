package com.lying.tricksy.config;

import java.io.FileWriter;
import java.util.Properties;

public class ServerConfig extends Config
{
	private static final Properties DEFAULT_SETTINGS = new Properties();
	
	private boolean verboseLogs = false;
	private boolean candleEnabled = true;
	private int nodeCap = 25;
	
	public ServerConfig(String fileIn)
	{
		super(fileIn);
	}
	
	protected Properties getDefaults() { return DEFAULT_SETTINGS; }
	
	public int treeSizeCap() { return this.nodeCap; }
	
	public boolean verboseLogging() { return this.verboseLogs; }
	
	public boolean prescientCandlesEnabled() { return this.candleEnabled; }
	
	protected void readValues(Properties valuesIn)
	{
		verboseLogs = parseBoolOr(valuesIn.getProperty("VerboseLogs"), false);
		candleEnabled = parseBoolOr(valuesIn.getProperty("EnablePrescientCandles"), true);
		nodeCap = parseIntOr(valuesIn.getProperty("TreeNodeCap"), 25);
	}
	
	protected void writeValues(FileWriter writer)
	{
		writeBool(writer, "VerboseLogs", false);
		writeInt(writer, "TreeNodeCap", nodeCap);
	}
	
	static
	{
		DEFAULT_SETTINGS.setProperty("VerboseLogs", "0");
		DEFAULT_SETTINGS.setProperty("EnablePrescientCandles", "1");
		DEFAULT_SETTINGS.setProperty("TreeNodeCap", "25");
	}
}
