package eu.mikart.worldedit;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.event.platform.PlatformReadyEvent;
import com.sk89q.worldedit.event.platform.PlatformsRegisteredEvent;
import com.sk89q.worldedit.internal.block.BlockStateIdAccess;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.item.ItemType;
import eu.mikart.worldedit.platform.MinestomPlatform;
import eu.mikart.worldedit.platform.adapters.MinestomAdapter;
import eu.mikart.worldedit.platform.config.WorldEditConfiguration;
import lombok.Getter;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;

import java.nio.file.Path;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.BiPredicate;

@Getter
public class MinestomWorldEdit {

    private static MinestomWorldEdit INSTANCE;
    private WorldEditConfiguration config;
    public EventNode<Event> eventNode = EventNode.all("worldedit-node");
    private BiPredicate<CommandSender, String> permissionHandler;
    private Path dataDirectory;

    public static MinestomWorldEdit getInstance() {
        return INSTANCE;
    }

	public MinestomWorldEdit(BiPredicate<CommandSender, String> permissionHandler, Path dataDirectory) {
        INSTANCE = this;
        this.permissionHandler = permissionHandler;
        this.dataDirectory = dataDirectory;

        MinestomAdapter.platform = new MinestomPlatform(this);
        loadConfig();

        WorldEdit.getInstance().getPlatformManager().register(MinestomAdapter.platform);
        WorldEdit.getInstance().getEventBus().post(new PlatformReadyEvent(MinestomAdapter.platform));
        WorldEdit.getInstance().getEventBus().post(new PlatformsRegisteredEvent());

        registerBlocks();
        registerItems();
        MinecraftServer.getGlobalEventHandler().addChild(eventNode);
    }

    private void loadConfig() {
        config = new WorldEditConfiguration();
        config.load();
        LocalSession.MAX_HISTORY_SIZE = 50; // Increase max history to 50
    }

    private void registerItems() {
        for (Material itemType : Material.values()) {
            String id = itemType.name();
            if (!ItemType.REGISTRY.keySet().contains(id)) {
                ItemType.REGISTRY.register(id, new ItemType(id));
            }
        }
    }

    private void registerBlocks() {
        for (Block minestomBlock : Block.values()) {
            String id = minestomBlock.name();
            if (!BlockType.REGISTRY.keySet().contains(id)) {
                BlockType block = new BlockType(id, null);

                for (BlockState state : block.getAllStates()) {
                    SortedMap<String, String> stateMap = new TreeMap<>();
                    for (Map.Entry<Property<?>, Object> entry : state.getStates().entrySet()) {
                        stateMap.put(entry.getKey().getName(), entry.getValue().toString());
                    }

                    int stateId = minestomBlock.withProperties(stateMap).stateId();
                    BlockStateIdAccess.register(state, stateId);
                }
                BlockType.REGISTRY.register(id, block);
            }
        }
    }

    public void terminate() {
        WorldEdit.getInstance().getSessionManager().unload();
        WorldEdit.getInstance().getPlatformManager().unregister(MinestomAdapter.platform);
    }
}
