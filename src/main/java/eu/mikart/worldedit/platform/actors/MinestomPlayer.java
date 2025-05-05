
package eu.mikart.worldedit.platform.actors;

import com.sk89q.util.StringUtil;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.extension.platform.AbstractPlayerActor;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.internal.cui.CUIEvent;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.session.SessionKey;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.formatting.WorldEditText;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.item.ItemType;
import eu.mikart.worldedit.MinestomWorldEdit;
import eu.mikart.worldedit.platform.MinestomPlatform;
import eu.mikart.worldedit.platform.adapters.MinestomAdapter;
import eu.mikart.worldedit.platform.misc.SessionKeyImpl;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;

public final class MinestomPlayer extends AbstractPlayerActor {
    private final MinestomPlatform platform;
    private final Player player;

    public MinestomPlayer(@NotNull MinestomPlatform platform, @NotNull Player player) {
        this.platform = platform;
        this.player = player;
    }

    @NotNull
    public UUID getUniqueId() {
        return this.player.getUuid();
    }

    @NotNull
    public String[] getGroups() {
        return new String[0];
    }

    public boolean hasPermission(@NotNull String permission) {
        return MinestomWorldEdit.getInstance().getPermissionHandler().test(player, permission);
    }

    @Override
    public boolean trySetPosition(Vector3 pos) {
        Pos oldPos = player.getPosition();
        player.teleport(new Pos(pos.x(), pos.y(), pos.z(), oldPos.yaw(), oldPos.pitch()));
        return true;
    }

    @Override
    public boolean trySetPosition(Vector3 pos, float pitch, float yaw) {
        player.teleport(new Pos(pos.x(), pos.y(), pos.z(), yaw, pitch));
        return true;
    }

    @Override
    public void dispatchCUIEvent(CUIEvent event) {
        String[] params = event.getParameters();
        String send = event.getTypeId();
        if (params.length > 0) {
            send = send + "|" + StringUtil.joinString(params, "|");
        }
        player.sendPluginMessage("worldedit:cui", send.getBytes(StandardCharsets.UTF_8));
    }

    @NotNull
    public SessionKey getSessionKey() {
        return new SessionKeyImpl(this.player.getUuid(), this.player.getUsername());
    }

    @NotNull
    public String getName() {
        return this.player.getUsername();
    }

    public void printRaw(@NotNull String msg) {
        this.sendColorized(msg, TextColor.YELLOW);
    }

    public void printDebug(@NotNull String msg) {
        this.sendColorized(msg, TextColor.YELLOW);
    }

    public void print(@NotNull String msg) {
        this.sendColorized(msg, TextColor.WHITE);
    }

    public void print(@NotNull Component component) {
        final Component newComponent = WorldEditText.format(component, this.getLocale());
        this.player.sendMessage(MinestomAdapter.INSTANCE.asComponent(newComponent));
    }

    public void printError(@NotNull String msg) {
        this.sendColorized(msg, TextColor.RED);
    }

    @NotNull
    public Locale getLocale() {
        return Locale.getDefault();
    }

    @Nullable
    public Object getFacet(@Nullable Class cls) {
        return null;
    }

    @NotNull
    public Location getLocation() {
        return MinestomAdapter.INSTANCE.asLocation(this.getWorld(), this.player.getPosition());
    }

    public boolean setLocation(@NotNull Location location) {
        this.player.teleport(MinestomAdapter.INSTANCE.toPosition(location));
        return true;
    }

    @Nullable
    public BaseEntity getState() {
        throw new RuntimeException("An operation is not implemented: " + "Not yet implemented");
    }

    @NotNull
    public World getWorld() {
        return MinestomAdapter.INSTANCE.asWorld(this.player.getInstance());
    }

    @NotNull
    public BaseItemStack getItemInHand(@Nullable HandSide handSide) {
        try {
            return MinestomAdapter.INSTANCE.asBaseItemStack(player.getItemInHand(handSide == HandSide.MAIN_HAND ? PlayerHand.MAIN : PlayerHand.OFF));
        } catch(IOException e) {
            return new BaseItemStack(ItemType.REGISTRY.get("minecraft:air"));
        }
    }

    public void giveItem(@NotNull BaseItemStack itemStack) {
        this.player.getInventory().addItemStack(MinestomAdapter.INSTANCE.toItemStack(itemStack));
    }

    @Nullable
    public BlockBag getInventoryBlockBag() {
        return null;
    }

    private void sendColorized(String msg, TextColor formatting) {
        for(String line : msg.split("\n")) {
            print(TextComponent.of(line, formatting));
        }
    }
}
