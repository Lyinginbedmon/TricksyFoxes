package com.lying.tricksy.utility.fakeplayer;

import java.util.Set;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.AdvancementTabC2SPacket;
import net.minecraft.network.packet.c2s.play.BoatPaddleStateC2SPacket;
import net.minecraft.network.packet.c2s.play.BookUpdateC2SPacket;
import net.minecraft.network.packet.c2s.play.ButtonClickC2SPacket;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientSettingsC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.CraftRequestC2SPacket;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.JigsawGeneratingC2SPacket;
import net.minecraft.network.packet.c2s.play.KeepAliveC2SPacket;
import net.minecraft.network.packet.c2s.play.PickFromInventoryC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayPongC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.QueryBlockNbtC2SPacket;
import net.minecraft.network.packet.c2s.play.QueryEntityNbtC2SPacket;
import net.minecraft.network.packet.c2s.play.RecipeBookDataC2SPacket;
import net.minecraft.network.packet.c2s.play.RecipeCategoryOptionsC2SPacket;
import net.minecraft.network.packet.c2s.play.RenameItemC2SPacket;
import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket;
import net.minecraft.network.packet.c2s.play.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.c2s.play.SelectMerchantTradeC2SPacket;
import net.minecraft.network.packet.c2s.play.SpectatorTeleportC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateBeaconC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateCommandBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateCommandBlockMinecartC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateDifficultyC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateDifficultyLockC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateJigsawC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdatePlayerAbilitiesC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateStructureBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class ServerFakePlayNetworkHandler extends ServerPlayNetworkHandler
{
	public ServerFakePlayNetworkHandler(MinecraftServer server, ClientConnection connection, ServerPlayerEntity player)
	{
		super(server, connection, player);
	}
	
    @Override
    public void onPlayerInput(PlayerInputC2SPacket packet) { }
    
    @Override
    public void onVehicleMove(VehicleMoveC2SPacket packet) { }
    
    @Override
    public void onTeleportConfirm(TeleportConfirmC2SPacket packet) { }
    
    @Override
    public void onRecipeBookData(RecipeBookDataC2SPacket packet) { }
    
    @Override
    public void onRecipeCategoryOptions(RecipeCategoryOptionsC2SPacket packet) { }
    
    @Override
    public void onAdvancementTab(AdvancementTabC2SPacket packet) { }
    
    @Override
    public void onRequestCommandCompletions(RequestCommandCompletionsC2SPacket packet) { }
    
    @Override
    public void onUpdateCommandBlock(UpdateCommandBlockC2SPacket packet) { }
    
    @Override
    public void onUpdateCommandBlockMinecart(UpdateCommandBlockMinecartC2SPacket packet) { }
    
    @Override
    public void onPickFromInventory(PickFromInventoryC2SPacket packet) { }
    
    @Override
    public void onRenameItem(RenameItemC2SPacket packet) { }
    
    @Override
    public void onUpdateBeacon(UpdateBeaconC2SPacket packet) { }
    
    @Override
    public void onUpdateStructureBlock(UpdateStructureBlockC2SPacket packet) { }
    
    @Override
    public void onUpdateJigsaw(UpdateJigsawC2SPacket packet) { }
    
    @Override
    public void onJigsawGenerating(JigsawGeneratingC2SPacket packet) { }
    
    @Override
    public void onSelectMerchantTrade(SelectMerchantTradeC2SPacket packet) { }
    
    @Override
    public void onBookUpdate(BookUpdateC2SPacket packet) { }
    
    @Override
    public void onQueryEntityNbt(QueryEntityNbtC2SPacket packet) { }
    
    @Override
    public void onQueryBlockNbt(QueryBlockNbtC2SPacket packet) { }
    
    @Override
    public void onPlayerMove(PlayerMoveC2SPacket packet) { }
    
    @Override
    public void requestTeleport(double x, double y, double z, float yaw, float pitch) { }
    
    @Override
    public void requestTeleport(double x, double y, double z, float yaw, float pitch, Set<PositionFlag> flags) { }
    
    @Override
    public void onPlayerAction(PlayerActionC2SPacket packet) { }
    
    @Override
    public void onPlayerInteractBlock(PlayerInteractBlockC2SPacket packet) { }
    
    @Override
    public void onPlayerInteractItem(PlayerInteractItemC2SPacket packet) { }
    
    @Override
    public void onSpectatorTeleport(SpectatorTeleportC2SPacket packet) { }
    
    @Override
    public void onResourcePackStatus(ResourcePackStatusC2SPacket packet) { }
    
    @Override
    public void onBoatPaddleState(BoatPaddleStateC2SPacket packet) { }
    
    @Override
    public void onPong(PlayPongC2SPacket packet) { }
    
    @Override
    public void onDisconnected(Text reason) { }
    
    @Override
    public void sendPacket(Packet<?> packet) { }
    
    @Override
    public void sendPacket(Packet<?> packet, @Nullable PacketCallbacks callbacks) { }
    
    @Override
    public void onUpdateSelectedSlot(UpdateSelectedSlotC2SPacket packet) { }
    
    @Override
    public void onChatMessage(ChatMessageC2SPacket packet) { }
    
    @Override
    public void onHandSwing(HandSwingC2SPacket packet) { }
    
    @Override
    public void onClientCommand(ClientCommandC2SPacket packet) { }
    
    @Override
    public void onPlayerInteractEntity(PlayerInteractEntityC2SPacket packet) { }
    
    @Override
    public void onClientStatus(ClientStatusC2SPacket packet) { }
    
    @Override
    public void onCloseHandledScreen(CloseHandledScreenC2SPacket packet) { }
    
    @Override
    public void onClickSlot(ClickSlotC2SPacket packet) { }
    
    @Override
    public void onCraftRequest(CraftRequestC2SPacket packet) { }
    
    @Override
    public void onButtonClick(ButtonClickC2SPacket packet) { }
    
    @Override
    public void onCreativeInventoryAction(CreativeInventoryActionC2SPacket packet) { }
    
    @Override
    public void onUpdateSign(UpdateSignC2SPacket packet) { }
    
    @Override
    public void onKeepAlive(KeepAliveC2SPacket packet) { }
    
    @Override
    public void onUpdatePlayerAbilities(UpdatePlayerAbilitiesC2SPacket packet) { }
    
    @Override
    public void onClientSettings(ClientSettingsC2SPacket packet) { }
    
    @Override
    public void onCustomPayload(CustomPayloadC2SPacket packet) { }
    
    @Override
    public void onUpdateDifficulty(UpdateDifficultyC2SPacket packet) { }
    
    @Override
    public void onUpdateDifficultyLock(UpdateDifficultyLockC2SPacket packet) { }
    
    @Override
    public void tick() { }
    
    @Override
    public void disconnect(Text reason) { }
}
