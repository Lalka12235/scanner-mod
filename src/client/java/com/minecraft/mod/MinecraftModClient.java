package com.minecraft.mod;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MinecraftModClient implements ClientModInitializer{
    private KeyMapping sendToChatKey;
    private KeyMapping.Category CATEGORY;

    public int posX;
    public int posY;
    public int posZ;

    public int posXSearch = 10;
    public int posYSearch = 10;
    public int posZSearch = 10;


    public void registerKeyMappings(){
        CATEGORY = KeyMapping.Category.register(
		MinecraftMod.id("custom_category")
        );

        sendToChatKey = KeyMappingHelper.registerKeyMapping(
	    new KeyMapping(
			"key.example-mod.send_to_chat",
			InputConstants.Type.KEYSYM,
			InputConstants.KEY_L,
			this.CATEGORY
	));
    }

    public LevelChunk getChunk(int chunkX,int chunkZ,Minecraft client){
        return client.level.getChunk(chunkX,chunkZ);
    }

    public void setPos(Minecraft client){
        posX = client.player.getBlockX();
        posY = client.player.getBlockY();
        posZ = client.player.getBlockZ();
        return;
    }

    public void registerKeyBindings(){
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
	    while (this.sendToChatKey.consumeClick()) {
		    if (client.player == null) return;
            setPos(client);
            int chunkX = Math.floorDiv(posX, 16);
            int chunkZ = Math.floorDiv(posZ, 16);


            LevelChunk chunk = getChunk(chunkX,chunkZ,client);
            BlockPos blockPos = client.player.getOnPos();
            Map<Block, List<BlockPos>> blockAndCountable = Scanner(chunk,blockPos);

            // out
            printResults(blockAndCountable,client);

        }
});
    }

    public Map<Block, List<BlockPos>> Scanner (LevelChunk chunk, BlockPos startPos) {
        Map<Block, List<BlockPos>> blockAndCountable = new HashMap<>();

        for (int i = -2; i <= posXSearch; i++) { // x
            int blockPosX = startPos.getX() + i;
            for (int j = -2; j <= posYSearch; j++) { // y
                int blockPosY = startPos.getY() - j;
                for (int k = -2; k <= posZSearch; k++) {// z
                    int blockPosZ = startPos.getZ() + k;
                    BlockPos blockPos1 = new BlockPos(blockPosX, blockPosY, blockPosZ);
                    BlockState blockInfoFromChunk = chunk.getBlockState(blockPos1);
                    Block blockFromChunk = blockInfoFromChunk.getBlock();
                    if (
                            blockFromChunk == Blocks.IRON_ORE ||
                            blockFromChunk == Blocks.DEEPSLATE_IRON_ORE ||
                            blockFromChunk == Blocks.GOLD_ORE ||
                            blockFromChunk == Blocks.DEEPSLATE_GOLD_ORE ||
                            blockFromChunk == Blocks.DIAMOND_ORE ||
                            blockFromChunk == Blocks.DEEPSLATE_DIAMOND_ORE ||
                                    blockFromChunk == Blocks.COAL_ORE
                    ) {
                        blockAndCountable
                                .computeIfAbsent(blockFromChunk, key -> new ArrayList<>())
                                .add(blockPos1);
                    }

                }
            }
        }
        return blockAndCountable;
    }

    public void printResults(Map<Block, List<BlockPos>> results, Minecraft client){
        if (results.isEmpty()) {
            client.player.sendSystemMessage(
                    Component.literal("No ores found!")
            );
            return;
        }
        for (Map.Entry<Block,List<BlockPos>> entry: results.entrySet()) {
            Block block = entry.getKey();
            List<BlockPos> positions = entry.getValue();

            client.player.sendSystemMessage(
                    Component.literal(
                            block.getName().getString() + ": " + positions.size()
                    )
            );
            for (BlockPos pos : positions) {
                String coordinates = String.format(
                        "  X: %d Y: %d Z: %d",
                        pos.getX(), pos.getY(), pos.getZ()
                );
                client.player.sendSystemMessage(
                        Component.literal(coordinates)
                );
            }
        }
    }

    @Override
    public void onInitializeClient() {
        registerKeyMappings();
        registerKeyBindings();
    }

}
