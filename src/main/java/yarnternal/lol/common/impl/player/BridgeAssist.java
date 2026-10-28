package yarnternal.lol.common.impl.player;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FallingBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import yarnternal.lol.api.utility.player.MoveUtil;
import yarnternal.lol.common.api.Module;
import yarnternal.lol.common.api.ModuleAnnotation;
import yarnternal.lol.common.api.ModuleList;
import yarnternal.lol.common.settings.BooleanSetting;
import yarnternal.lol.common.settings.ModeSetting;
import yarnternal.lol.common.settings.NumberSetting;
import yarnternal.lol.core.Yarnternal;

/**
 * BridgeAssist — легитный помощник при постройке мостов.
 * <ul>
 *   <li><b>NinjaBridge</b> — FastBridge: нативная эмуляция микро-приседа (Sneak) ровно на краю блока.
 *       100% непакетный обход для Vulcan, Grim и BedWars серверов.</li>
 *   <li><b>GodBridge</b> — скоростная постройка назад без приседаний с фиксацией угла наклона прицела (81.5°).</li>
 *   <li><b>Vanilla</b> — установка блоков без ограничений.</li>
 * </ul>
 */
@ModuleAnnotation(name = "BridgeAssist", desc = "Помощник при постройке мостов (NinjaBridge, GodBridge)", type = ModuleList.Player)
public class BridgeAssist extends Module {

    private final ModeSetting mode = addSetting(
            new ModeSetting("Mode", "NinjaBridge", "NinjaBridge", "GodBridge", "Vanilla")
    );

    private final BooleanSetting pitchAssist = addSetting(
            new BooleanSetting("Pitch Assist", true).visible(() -> !mode.is("Vanilla"))
    );

    private final BooleanSetting tower = addSetting(
            new BooleanSetting("Tower", true)
    );

    private final BooleanSetting autoSelect = addSetting(
            new BooleanSetting("Auto Select", true)
    );

    private final NumberSetting delay = addSetting(
            new NumberSetting("Delay", 0.0, 0.0, 4.0, 1.0, " ticks").visible(() -> mode.is("Vanilla"))
    );

    private boolean wasSneaking = false;
    private boolean wasUsing = false;
    private int vanillaTimer = 0;

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        if (!isToggled()) return;


        // Авто-выбор блока на хотбаре
        if (autoSelect.getValue()) {
            int slot = findBlockSlot();
            if (slot != -1 && mc.player.getInventory().getSelectedSlot() != slot) {
                mc.player.getInventory().setSelectedSlot(slot);
            }
        }

        // Башня вверх (Tower)
        if (tower.getValue() && mc.options.jumpKey.isPressed() && !MoveUtil.isMoving()) {
            handleTower();
            return;
        }

        if (mode.is("NinjaBridge")) {
            handleNinjaBridge();
        } else if (mode.is("GodBridge")) {
            handleGodBridge();
        } else if (mode.is("Vanilla")) {
            handleVanilla();
        }
    }

    /**
     * NinjaBridge (FastBridge):
     * Присед зажимается только на краю блока и отжимается сразу после установки.
     */
    private void handleNinjaBridge() {
        if (mc.player == null || mc.world == null) return;

        BlockPos under = BlockPos.ofFloored(mc.player.getX(), mc.player.getY() - 0.5, mc.player.getZ());
        boolean onEdge = mc.world.getBlockState(under).isAir() || mc.world.getBlockState(under).isReplaceable();

        if (pitchAssist.getValue() && MoveUtil.isMoving()) {
            mc.player.setPitch(81.5f);
        }

        if (onEdge) {
            mc.options.sneakKey.setPressed(true);
            wasSneaking = true;

            if (mc.crosshairTarget instanceof BlockHitResult hitResult && hitResult.getType() == HitResult.Type.BLOCK) {
                mc.options.useKey.setPressed(true);
                wasUsing = true;
            }
        } else {
            if (wasSneaking) {
                mc.options.sneakKey.setPressed(false);
                wasSneaking = false;
            }
            if (wasUsing) {
                mc.options.useKey.setPressed(false);
                wasUsing = false;
            }
        }
    }

    /**
     * GodBridge:
     * Быстрый бридж назад без приседаний.
     */
    private void handleGodBridge() {
        if (mc.player == null || mc.world == null) return;

        if (pitchAssist.getValue()) {
            mc.player.setPitch(81.5f);
        }

        BlockPos under = BlockPos.ofFloored(mc.player.getX(), mc.player.getY() - 0.5, mc.player.getZ());
        boolean onEdge = mc.world.getBlockState(under).isAir() || mc.world.getBlockState(under).isReplaceable();

        if (onEdge) {
            if (mc.crosshairTarget instanceof BlockHitResult hitResult && hitResult.getType() == HitResult.Type.BLOCK) {
                mc.options.useKey.setPressed(true);
                wasUsing = true;
            }
        } else if (wasUsing) {
            mc.options.useKey.setPressed(false);
            wasUsing = false;
        }
    }

    /**
     * Непакетная башня вверх: смотрит под ноги и строит столб вверх.
     */
    private void handleTower() {
        if (mc.player == null || mc.world == null) return;

        mc.player.setPitch(90.0f);

        if (mc.crosshairTarget instanceof BlockHitResult hitResult && hitResult.getType() == HitResult.Type.BLOCK) {
            mc.options.useKey.setPressed(true);
            wasUsing = true;
        }
    }

    /**
     * Ванильный режим (пакетный).
     */
    private void handleVanilla() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        if (vanillaTimer < delay.getValue()) {
            vanillaTimer++;
            return;
        }

        BlockPos under = BlockPos.ofFloored(mc.player.getX(), mc.player.getY() - 0.8, mc.player.getZ());
        BlockState state = mc.world.getBlockState(under);

        if (!state.isAir() && !state.isReplaceable()) return;

        if (mc.crosshairTarget instanceof BlockHitResult hitResult && hitResult.getType() == HitResult.Type.BLOCK) {
            if (mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult).isAccepted()) {
                mc.player.swingHand(Hand.MAIN_HAND);
                vanillaTimer = 0;
            }
        }
    }

    /**
     * Поиск блока на хотбаре (0-8).
     */
    private int findBlockSlot() {
        if (mc.player == null) return -1;

        int selected = mc.player.getInventory().getSelectedSlot();
        if (isValidBlock(mc.player.getInventory().getStack(selected))) {
            return selected;
        }

        for (int i = 0; i < 9; i++) {
            if (isValidBlock(mc.player.getInventory().getStack(i))) {
                return i;
            }
        }

        return -1;
    }

    private boolean isValidBlock(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem blockItem)) {
            return false;
        }
        Block block = blockItem.getBlock();
        if (block instanceof FallingBlock) {
            return false;
        }
        BlockState state = block.getDefaultState();
        return state.isOpaqueFullCube() && !state.isAir();
    }

    /**
     * Количество блоков на хотбаре.
     */
    public int getBlockCount() {
        if (mc.player == null) return 0;
        int count = 0;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (isValidBlock(stack)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    @Override
    public void onDisable() {
        releaseBridgeInputs();
        vanillaTimer = 0;
    }

    /** Отпускаем только те клавиши, которые нажал сам BridgeAssist. */
    private void releaseBridgeInputs() {
        if (mc.options == null) {
            return;
        }
        if (wasSneaking) {
            mc.options.sneakKey.setPressed(false);
            wasSneaking = false;
        }
        if (wasUsing) {
            mc.options.useKey.setPressed(false);
            wasUsing = false;
        }
    }

    public String getMode() {
        return mode.get();
    }
}
