package com.lying.tricksy.utility.fakeplayer;

import net.minecraft.network.listener.PacketListener;
import net.minecraft.text.Text;

public class FakePacketListener implements PacketListener
{
	public static final FakePacketListener INSTANCE = new FakePacketListener();
	
	@Override
	public void onDisconnected(Text var1) { }

	@Override
	public boolean isConnectionOpen() { return false; }

}
