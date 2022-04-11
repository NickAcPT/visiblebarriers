package amymialee.visiblebarriers;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class VisibleBarriers implements ClientModInitializer {
    public final static String MODID = "visiblebarriers";
    public static VisibleBarriersConfig config = null;
    private static KeyBinding keyBinding;

    public static boolean visible = false;
    public static boolean visible_air = false;

    public static final ItemGroup EXTRA_ITEMS = FabricItemGroupBuilder.create(new Identifier(MODID, "extra_items")).icon(() -> new ItemStack(Items.COMMAND_BLOCK))
            .appendItems(stacks -> {
                stacks.add(new ItemStack(Items.COMMAND_BLOCK));
                stacks.add(new ItemStack(Items.CHAIN_COMMAND_BLOCK));
                stacks.add(new ItemStack(Items.REPEATING_COMMAND_BLOCK));
                stacks.add(new ItemStack(Items.STRUCTURE_BLOCK));
                stacks.add(new ItemStack(Items.JIGSAW));
                stacks.add(new ItemStack(Items.SPAWNER));
                stacks.add(new ItemStack(Items.BARRIER));
                stacks.add(new ItemStack(Items.STRUCTURE_VOID));
                stacks.add(new ItemStack(Items.LIGHT));

                stacks.add(new ItemStack(Items.DEBUG_STICK));
                stacks.add(new ItemStack(Items.KNOWLEDGE_BOOK));
                stacks.add(new ItemStack(Items.DRAGON_EGG));
                stacks.add(new ItemStack(Items.COMMAND_BLOCK_MINECART));
                stacks.add(new ItemStack(Items.FIREWORK_ROCKET));
                stacks.add(new ItemStack(Items.FIREWORK_STAR));
                stacks.add(new ItemStack(Items.SUSPICIOUS_STEW));
            }).build();

    @Override
    public void onInitializeClient() {
        AutoConfig.register(VisibleBarriersConfig.class, Toml4jConfigSerializer::new);
        config = AutoConfig.getConfigHolder(VisibleBarriersConfig.class).getConfig();

        BlockRenderLayerMap.INSTANCE.putBlock(Blocks.BARRIER, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(Blocks.STRUCTURE_VOID, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(Blocks.LIGHT, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(Blocks.AIR, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(Blocks.CAVE_AIR, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(Blocks.VOID_AIR, RenderLayer.getTranslucent());

        keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.visiblebarriers.bind",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                "category.visiblebarriers.bind"
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (keyBinding.wasPressed()) {
                visible = !visible;
                if (Screen.hasShiftDown()) {
                    if (visible) {
                        visible_air = true;
                    }
                }
                if (!visible) {
                    visible_air = false;
                }
                client.worldRenderer.reload();
            }
        });
    }
}