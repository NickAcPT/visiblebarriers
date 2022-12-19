package xyz.amymialee.visiblebarriers;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.command.argument.BlockStateArgumentType;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.amymialee.visiblebarriers.util.ArrayPairList;

@Environment(EnvType.CLIENT)
public class VisibleBarriers implements ClientModInitializer {
    public final static String MOD_ID = "visiblebarriers";
    public final static Logger logger = LoggerFactory.getLogger(MOD_ID);
    private static final KeyBinding keyBindingVisibility = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.visiblebarriers.bind",
            GLFW.GLFW_KEY_B,
            "category.visiblebarriers"
    ));
    private static final KeyBinding keyBindingFullBright = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.visiblebarriers.fullbright",
            InputUtil.UNKNOWN_KEY.getCode(),
            "category.visiblebarriers"
    ));
    private static final KeyBinding keyBindingBarriers = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.visiblebarriers.barriers",
            InputUtil.UNKNOWN_KEY.getCode(),
            "category.visiblebarriers"
    ));
    private static final KeyBinding keyBindingLights = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.visiblebarriers.lights",
            InputUtil.UNKNOWN_KEY.getCode(),
            "category.visiblebarriers"
    ));
    private static final KeyBinding keyBindingStructureVoids = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.visiblebarriers.structure_voids",
            InputUtil.UNKNOWN_KEY.getCode(),
            "category.visiblebarriers"
    ));
    private static final KeyBinding keyBindingHighlights = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.visiblebarriers.highlights",
            InputUtil.UNKNOWN_KEY.getCode(),
            "category.visiblebarriers"
    ));
    public static final ArrayPairList<KeyBinding, Boolean> spareKeyBindings = new ArrayPairList<>();
    public static VBConfig config = new VBConfig();
    protected static boolean visible = false;
    protected static boolean visibleBarriers = false;
    protected static boolean visibleLights = false;
    protected static boolean visibleStructureVoids = false;
    protected static boolean visibleHighlights = false;

    @Override
    public void onInitializeClient() {
        for (int i = 0; i < config.getCustomKeyCount(); i++) {
            final int index = i;
            KeyBinding keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                    "key.visiblebarriers.spare",
                    InputUtil.UNKNOWN_KEY.getCode(),
                    "category.visiblebarriers"
            ));
            spareKeyBindings.put(keyBinding, false);
            ClientTickEvents.END_CLIENT_TICK.register(client -> {
                if (keyBinding.wasPressed()) {
                    spareKeyBindings.put(keyBinding, !spareKeyBindings.getOrDefault(keyBinding, true));
                    this.reloadWorldRenderer();
                    ClientPlayerEntity player = client.player;
                    if (player != null) {
                        player.sendMessage(Text.translatable("command.visiblebarriers.customkey", index + 1, spareKeyBindings.get(keyBinding) ?
                                Text.translatable("command.visiblebarriers.customkey.on") : Text.translatable("command.visiblebarriers.customkey.off")), true);
                    }
                }
            });
        }
        FabricItemGroupBuilder.create(id("visible_barriers"))
                .icon(() -> new ItemStack(Items.REPEATING_COMMAND_BLOCK))
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
                    stacks.add(new ItemStack(Items.FIREWORK_STAR));
                    for (int i = 1; i <= 3; i++) {
                        ItemStack firework1 = new ItemStack(Items.FIREWORK_ROCKET);
                        firework1.getOrCreateSubNbt("Fireworks").putByte("Flight", (byte)i);
                        stacks.add(firework1);
                    }
                }).build();
        BlockRenderLayerMap.INSTANCE.putBlock(Blocks.BARRIER, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(Blocks.STRUCTURE_VOID, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(Blocks.LIGHT, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(Blocks.AIR, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(Blocks.CAVE_AIR, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(Blocks.VOID_AIR, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(Blocks.MOVING_PISTON, RenderLayer.getTranslucent());
        ClientCommandRegistrationCallback.EVENT.register((commandDispatcher, commandRegistryAccess) -> commandDispatcher.register(
                ClientCommandManager.literal("visiblebarriers")
                        .then(ClientCommandManager.literal("reload").executes(context -> {
                            config.loadConfig();
                            config.saveConfig();
                            this.reloadWorldRenderer();
                            context.getSource().sendFeedback(Text.translatable("command.visiblebarriers.reload").formatted(Formatting.GRAY));
                            return 1;
                        }))

                        .then(ClientCommandManager.literal("visible").executes(context -> {
                            visible = !visible;
                            this.reloadWorldRenderer();
                            if (visible) {
                                context.getSource().sendFeedback(Text.translatable("command.visiblebarriers.visible.true").formatted(Formatting.GRAY));
                            } else {
                                context.getSource().sendFeedback(Text.translatable("command.visiblebarriers.visible.false").formatted(Formatting.GRAY));
                            }
                            return 1;
                        }).then(ClientCommandManager.argument("visible", BoolArgumentType.bool()).executes(context -> {
                            boolean shouldBeVisible = BoolArgumentType.getBool(context, "visible");
                            visible = shouldBeVisible;
                            this.reloadWorldRenderer();
                            if (shouldBeVisible) {
                                context.getSource().sendFeedback(Text.translatable("command.visiblebarriers.visible.true").formatted(Formatting.GRAY));
                            } else {
                                context.getSource().sendFeedback(Text.translatable("command.visiblebarriers.visible.false").formatted(Formatting.GRAY));
                            }
                            return 1;
                        })))

                        .then(ClientCommandManager.literal("visibleair").executes(context -> {
                            boolean shouldBeVisible = !config.isAirVisible();
                            config.setVisibleAir(shouldBeVisible);
                            this.reloadWorldRenderer();
                            if (shouldBeVisible) {
                                context.getSource().sendFeedback(Text.translatable("command.visiblebarriers.visibleair.true").formatted(Formatting.GRAY));
                            } else {
                                context.getSource().sendFeedback(Text.translatable("command.visiblebarriers.visibleair.false").formatted(Formatting.GRAY));
                            }
                            return 1;
                        }).then(ClientCommandManager.argument("visible", BoolArgumentType.bool()).executes(context -> {
                            boolean shouldBeVisible = BoolArgumentType.getBool(context, "visible");
                            config.setVisibleAir(shouldBeVisible);
                            this.reloadWorldRenderer();
                            if (shouldBeVisible) {
                                context.getSource().sendFeedback(Text.translatable("command.visiblebarriers.visibleair.true").formatted(Formatting.GRAY));
                            } else {
                                context.getSource().sendFeedback(Text.translatable("command.visiblebarriers.visibleair.false").formatted(Formatting.GRAY));
                            }
                            return 1;
                        })))

                        .then(ClientCommandManager.literal("hiddenparticles").executes(context -> {
                            boolean shouldBeHidden = !config.shouldHideParticles();
                            config.setHideParticles(shouldBeHidden);
                            if (shouldBeHidden) {
                                context.getSource().sendFeedback(Text.translatable("command.visiblebarriers.hiddenparticles.true").formatted(Formatting.GRAY));
                            } else {
                                context.getSource().sendFeedback(Text.translatable("command.visiblebarriers.hiddenparticles.false").formatted(Formatting.GRAY));
                            }
                            return 1;
                        }).then(ClientCommandManager.argument("visible", BoolArgumentType.bool()).executes(context -> {
                            boolean shouldBeVisible = BoolArgumentType.getBool(context, "visible");
                            config.setHideParticles(shouldBeVisible);
                            if (shouldBeVisible) {
                                context.getSource().sendFeedback(Text.translatable("command.visiblebarriers.hiddenparticles.true").formatted(Formatting.GRAY));
                            } else {
                                context.getSource().sendFeedback(Text.translatable("command.visiblebarriers.hiddenparticles.false").formatted(Formatting.GRAY));
                            }
                            return 1;
                        })))

                        .then(ClientCommandManager.literal("fullbright").executes(context -> {
                            boolean shouldBeEnabled = !config.isFullBright();
                            config.setFullBright(shouldBeEnabled);
                            if (shouldBeEnabled) {
                                context.getSource().sendFeedback(Text.translatable("command.visiblebarriers.fullbright.true").formatted(Formatting.GRAY));
                            } else {
                                context.getSource().sendFeedback(Text.translatable("command.visiblebarriers.fullbright.false").formatted(Formatting.GRAY));
                            }
                            return 1;
                        }).then(ClientCommandManager.argument("enabled", BoolArgumentType.bool()).executes(context -> {
                            boolean shouldBeEnabled = BoolArgumentType.getBool(context, "enabled");
                            config.setFullBright(shouldBeEnabled);
                            if (shouldBeEnabled) {
                                context.getSource().sendFeedback(Text.translatable("command.visiblebarriers.fullbright.true").formatted(Formatting.GRAY));
                            } else {
                                context.getSource().sendFeedback(Text.translatable("command.visiblebarriers.fullbright.false").formatted(Formatting.GRAY));
                            }
                            return 1;
                        })))

                        .then(ClientCommandManager.literal("addblock").then(ClientCommandManager.argument("block", BlockStateArgumentType.blockState(commandRegistryAccess)).then(ClientCommandManager.argument("color", IntegerArgumentType.integer()).executes(context -> {
                            Block block = getBlockState(context, "block").getBlockState().getBlock();
                            boolean replacement = config.hasBlock(block);
                            config.addBlock(block, IntegerArgumentType.getInteger(context, "color"));
                            this.reloadWorldRenderer();
                            if (!replacement) {
                                context.getSource().sendFeedback(Text.translatable("command.visiblebarriers.addblock", block.getName()).formatted(Formatting.GRAY));
                            } else {
                                context.getSource().sendFeedback(Text.translatable("command.visiblebarriers.addblock.replaced", block.getName()).formatted(Formatting.GRAY));
                            }
                            return 1;
                        }))))
                        .then(ClientCommandManager.literal("removeblock").then(ClientCommandManager.argument("block", BlockStateArgumentType.blockState(commandRegistryAccess)).executes(context -> {
                            Block block = getBlockState(context, "block").getBlockState().getBlock();
                            boolean removed = config.hasBlock(block);
                            config.removeBlock(block);
                            this.reloadWorldRenderer();
                            if (removed) {
                                context.getSource().sendFeedback(Text.translatable("command.visiblebarriers.removeblock.success", block.getName()).formatted(Formatting.GRAY));
                            } else {
                                context.getSource().sendFeedback(Text.translatable("command.visiblebarriers.removeblock.failure", block.getName()).formatted(Formatting.RED));
                            }
                            return 1;
                        })))

                        .then(ClientCommandManager.literal("additem").then(ClientCommandManager.argument("item", ItemStackArgumentType.itemStack(commandRegistryAccess)).then(ClientCommandManager.argument("color", IntegerArgumentType.integer()).executes(context -> {
                            Item item = ItemStackArgumentType.getItemStackArgument(context, "item").getItem();
                            boolean replacement = config.hasItem(item);
                            config.addItem(item, IntegerArgumentType.getInteger(context, "color"));
                            if (!replacement) {
                                context.getSource().sendFeedback(Text.translatable("command.visiblebarriers.additem", item.getName()).formatted(Formatting.GRAY));
                            } else {
                                context.getSource().sendFeedback(Text.translatable("command.visiblebarriers.additem.replaced", item.getName()).formatted(Formatting.GRAY));
                            }
                            return 1;
                        }))))
                        .then(ClientCommandManager.literal("removeitem").then(ClientCommandManager.argument("item", ItemStackArgumentType.itemStack(commandRegistryAccess)).executes(context -> {
                            Item item = ItemStackArgumentType.getItemStackArgument(context, "item").getItem();
                            boolean removed = config.hasItem(item);
                            config.removeItem(item);
                            if (removed) {
                                context.getSource().sendFeedback(Text.translatable("command.visiblebarriers.removeitem.success", item.getName()).formatted(Formatting.GRAY));
                            } else {
                                context.getSource().sendFeedback(Text.translatable("command.visiblebarriers.removeitem.failure", item.getName()).formatted(Formatting.RED));
                            }
                            return 1;
                        })))
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (keyBindingVisibility.wasPressed()) {
                visible = !visible;
                this.reloadWorldRenderer();
                if (client.player != null) {
                    if (visible) {
                        client.player.sendMessage(Text.translatable("command.visiblebarriers.visible.true").formatted(Formatting.GRAY), true);
                    } else {
                        client.player.sendMessage(Text.translatable("command.visiblebarriers.visible.false").formatted(Formatting.GRAY), true);
                    }
                }
            }
            if (keyBindingFullBright.wasPressed()) {
                boolean shouldBeEnabled = !config.isFullBright();
                config.setFullBright(shouldBeEnabled);
                if (client.player != null) {
                    if (shouldBeEnabled) {
                        client.player.sendMessage(Text.translatable("command.visiblebarriers.fullbright.true").formatted(Formatting.GRAY), true);
                    } else {
                        client.player.sendMessage(Text.translatable("command.visiblebarriers.fullbright.false").formatted(Formatting.GRAY), true);
                    }
                }
            }
            if (keyBindingBarriers.wasPressed()) {
                visibleBarriers = !visibleBarriers;
                this.reloadWorldRenderer();
                if (client.player != null) {
                    if (visibleBarriers) {
                        client.player.sendMessage(Text.translatable("command.visiblebarriers.barriers.true").formatted(Formatting.GRAY), true);
                    } else {
                        client.player.sendMessage(Text.translatable("command.visiblebarriers.barriers.false").formatted(Formatting.GRAY), true);
                    }
                }
            }
            if (keyBindingLights.wasPressed()) {
                visibleLights = !visibleLights;
                this.reloadWorldRenderer();
                if (client.player != null) {
                    if (visibleLights) {
                        client.player.sendMessage(Text.translatable("command.visiblebarriers.lights.true").formatted(Formatting.GRAY), true);
                    } else {
                        client.player.sendMessage(Text.translatable("command.visiblebarriers.lights.false").formatted(Formatting.GRAY), true);
                    }
                }
            }
            if (keyBindingStructureVoids.wasPressed()) {
                visibleStructureVoids = !visibleStructureVoids;
                this.reloadWorldRenderer();
                if (client.player != null) {
                    if (visibleStructureVoids) {
                        client.player.sendMessage(Text.translatable("command.visiblebarriers.structurevoids.true").formatted(Formatting.GRAY), true);
                    } else {
                        client.player.sendMessage(Text.translatable("command.visiblebarriers.structurevoids.false").formatted(Formatting.GRAY), true);
                    }
                }
            }
            if (keyBindingHighlights.wasPressed()) {
                visibleHighlights = !visibleHighlights;
                this.reloadWorldRenderer();
                if (client.player != null) {
                    if (visibleHighlights) {
                        client.player.sendMessage(Text.translatable("command.visiblebarriers.highlights.true").formatted(Formatting.GRAY), true);
                    } else {
                        client.player.sendMessage(Text.translatable("command.visiblebarriers.highlights.false").formatted(Formatting.GRAY), true);
                    }
                }
            }
        });
    }

    private void reloadWorldRenderer() {
        MinecraftClient.getInstance().worldRenderer.reload();
    }

    public static BlockStateArgument getBlockState(CommandContext<FabricClientCommandSource> context, String name) {
        return context.getArgument(name, BlockStateArgument.class);
    }

    public static boolean isVisible() {
        return visible;
    }

    public static boolean areBarriersVisible() {
        return visibleBarriers || isVisible();
    }

    public static boolean areLightsVisible() {
        return visibleLights || isVisible();
    }

    public static boolean areStructureVoidsVisible() {
        return visibleStructureVoids || isVisible();
    }

    public static boolean areHighlightsEnabled() {
        return visibleHighlights;
    }

    public static boolean areEntitiesVisible() {
        return isVisible();
    }

    public static Identifier id(String... path) {
        return new Identifier(MOD_ID, String.join(".", path));
    }
}