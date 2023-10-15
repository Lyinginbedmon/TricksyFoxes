package com.lying.tricksy.config;

import java.io.FileWriter;
import java.util.Properties;

import com.lying.tricksy.screen.NodeRenderUtils.NodeDisplay;

public class ClientConfig extends Config
{
	private static final Properties DEFAULT_SETTINGS = new Properties();
	
	/** How the tree screen displays variables {NEVER, HOVER, ALWAYS} */
	private NodeDisplay nodeDisplayStyle = NodeDisplay.HOVERED;
	
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
		nodeDisplayStyle = NodeDisplay.fromString(parseStringOr(valuesIn.getProperty("NodeDisplay"), NodeDisplay.HOVERED.asString()));
	}
	
	protected void writeValues(FileWriter writer)
	{
		writeBool(writer, "FancyTrees", fancyTrees);
		writeString(writer, "NodeDisplay", nodeDisplayStyle.asString());
	}
	
	public boolean fancyTrees() { return this.fancyTrees; }
	
	public NodeDisplay nodeDisplayStyle() { return this.nodeDisplayStyle; }
	
	static
	{
		DEFAULT_SETTINGS.setProperty("FancyTrees", "1");
		DEFAULT_SETTINGS.setProperty("NodeDisplay", NodeDisplay.HOVERED.asString());
	}
}
