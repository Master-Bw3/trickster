package dev.enjarai.trickster.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.enjarai.trickster.ModSounds;
import dev.enjarai.trickster.Trickster;
import dev.enjarai.trickster.render.SpellCircleRenderer;
import dev.enjarai.trickster.spell.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.render.*;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static dev.enjarai.trickster.render.SpellCircleRenderer.*;

public class SpellPartWidget extends AbstractParentElement implements Drawable, Selectable {
    public static final Pattern CREATE_SUBCIRCLE_GLYPH = Pattern.of(0, 4, 8, 7);
    public static final Pattern CREATE_GLYPH_CIRCLE_GLYPH = Pattern.of(0, 4, 8, 5);
    public static final Pattern DELETE_CIRCLE_GLYPH = Pattern.of(0, 4, 8);
    public static final Pattern CLEAR_DISABLED_GLYPH = Pattern.of(0, 4, 8, 5, 2, 1, 0, 3, 6, 7, 8);
    public static final Pattern COPY_OFFHAND_LITERAL = Pattern.of(4, 0, 1, 4, 2, 1);
    public static final Pattern COPY_OFFHAND_LITERAL_INNER = Pattern.of(1, 2, 4, 1, 0, 4, 7);
    public static final Pattern COPY_OFFHAND_EXECUTE = Pattern.of(4, 3, 0, 4, 5, 2, 4, 1);

    private SpellPart spellPart;
//    private List<SpellPartWidget> partWidgets;

    public double x;
    public double y;
    public double size;
    private double amountDragged;

    private boolean isMutable = true;

    private Consumer<SpellPart> updateListener;
    private Supplier<SpellPart> otherHandSpellSupplier;
    @Nullable
    private SpellPart toBeReplaced;
    private Runnable initializeReplace;

    private SpellPart drawingPart;
    private List<Byte> drawingPattern;

    private final SpellCircleRenderer renderer;

    public SpellPartWidget(SpellPart spellPart, double x, double y, double size, Consumer<SpellPart> updateListener, Supplier<SpellPart> otherHandSpellSupplier, Runnable initializeReplace) {
        this.spellPart = spellPart;
        this.x = x;
        this.y = y;
        this.size = size;
        this.updateListener = updateListener;
        this.otherHandSpellSupplier = otherHandSpellSupplier;
        this.initializeReplace = initializeReplace;
        this.renderer = new SpellCircleRenderer(() -> this.drawingPart, () -> this.drawingPattern);
    }

    @Override
    public List<? extends Element> children() {
        return List.of();
    }

    public void setSpell(SpellPart spellPart) {
        this.spellPart = spellPart;
//        partWidgets.clear();
//        spellPart.
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (isMutable) {
            this.renderer.setMousePosition(mouseX, mouseY);
        }
        this.renderer.renderPart(
                context.getMatrices(), context.getVertexConsumers(), Optional.of(spellPart),
                (float) x, (float) y, (float) size, 0, delta,
                size -> (float) Math.clamp(1 / (size / context.getScaledWindowHeight() * 2) - 0.2, 0, 1)
        );
    }

    public static boolean isCircleClickable(float size) {
        return size >= 16 && size <= 256;
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {

    }

    @Override
    public SelectionType getType() {
        return SelectionType.NONE;
    }

    public void setMutable(boolean mutable) {
        isMutable = mutable;
        if (!mutable) {
            this.renderer.setMousePosition(Double.MIN_VALUE, Double.MIN_VALUE);
        }
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return true; // TODO make more granular?
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
            return true;
        }

        var intensity = verticalAmount * size / 10;
        size += intensity;
        x += verticalAmount * (x - mouseX) / 10;
        y += verticalAmount * (y - mouseY) / 10;

        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
            return true;
        }

        if (!isDrawing()) {
            x += deltaX;
            y += deltaY;

            amountDragged += Math.abs(deltaX) + Math.abs(deltaY);

            return true;
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMutable || isDrawing()) {
            // We need to return true on the mouse down event to make sure the screen knows if we're on a clickable node
            if (propagateMouseEvent(spellPart, (float) x, (float) y, (float) size, 0, mouseX, mouseY,
                    (part, x, y, size) -> true)) {
                return true;
            }

            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (isMutable || isDrawing()) {
            var dragged = amountDragged;
            amountDragged = 0;
            if (dragged > 5) {
                return false;
            }

            if (button == 0 && !isDrawing()) {
                if (propagateMouseEvent(spellPart, (float) x, (float) y, (float) size, 0, mouseX, mouseY,
                        (part, x, y, size) -> selectPattern(part, x, y, size, mouseX, mouseY))) {
                    return true;
                }
            }

            if (drawingPart != null) {
                stopDrawing();
                return true;
            }
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (isDrawing()) {
            propagateMouseEvent(spellPart, (float) x, (float) y, (float) size, 0, mouseX, mouseY,
                    (part, x, y, size) -> selectPattern(part, x, y, size, mouseX, mouseY));
        }

        super.mouseMoved(mouseX, mouseY);
    }

    protected boolean selectPattern(SpellPart part, float x, float y, float size, double mouseX, double mouseY) {
        if (drawingPart != null && drawingPart != part) {
            // Cancel early if we're already drawing in another part
            return false;
        }

        var patternSize = size / PATTERN_TO_PART_RATIO;
        var pixelSize = patternSize / PART_PIXEL_RADIUS;

        for (int i = 0; i < 9; i++) {
            var pos = getPatternDotPosition(x, y, i, patternSize);

            if (isInsideHitbox(pos, pixelSize, mouseX, mouseY)) {
                if (drawingPart == null) {
                    drawingPart = part;
                    part.glyph = new PatternGlyph(List.of());
                    drawingPattern = new ArrayList<>();
                }

                if (drawingPattern.size() >= 2 && drawingPattern.get(drawingPattern.size() - 2) == (byte) i) {
                    drawingPattern.removeLast();
                    MinecraftClient.getInstance().player.playSoundToPlayer(
                            ModSounds.DRAW, SoundCategory.MASTER,
                            1f, ModSounds.randomPitch(0.6f, 0.2f)
                    );
                } else if (drawingPattern.isEmpty() ||
                        (drawingPattern.getLast() != (byte) i && !hasOverlappingLines(drawingPattern, drawingPattern.getLast(), (byte) i))) {
                    drawingPattern.add((byte) i);
                    // TODO click sound?
                    MinecraftClient.getInstance().player.playSoundToPlayer(
                            ModSounds.DRAW, SoundCategory.MASTER,
                            1f, ModSounds.randomPitch(1f, 0.2f)
                    );
                }

                return true;
            }
        }

        return false;
    }

    protected void stopDrawing() {
        var compiled = Pattern.from(drawingPattern);
        var patternSize = drawingPattern.size();

        if (compiled.equals(CREATE_SUBCIRCLE_GLYPH)) {
            drawingPart.subParts.add(Optional.of(new SpellPart()));
        } else if (compiled.equals(CREATE_GLYPH_CIRCLE_GLYPH)) {
            drawingPart.glyph = new SpellPart();
        } else if (compiled.equals(DELETE_CIRCLE_GLYPH)) {
            setSubPartInTree(drawingPart, Optional.empty(), spellPart);
        } else if (compiled.equals(CLEAR_DISABLED_GLYPH)) {
            drawingPart.subParts.removeIf(Optional::isEmpty);
        } else if (compiled.equals(COPY_OFFHAND_LITERAL)) {
            if (drawingPart == spellPart) {
                spellPart = otherHandSpellSupplier.get().deepClone();
            } else {
                setSubPartInTree(drawingPart, Optional.of(otherHandSpellSupplier.get().deepClone()), spellPart);
            }
        } else if (compiled.equals(COPY_OFFHAND_LITERAL_INNER)) {
            drawingPart.glyph = otherHandSpellSupplier.get().deepClone();
        } else if (compiled.equals(COPY_OFFHAND_EXECUTE)) {
            toBeReplaced = drawingPart;
            initializeReplace.run();
        } else if (patternSize > 1) {
            drawingPart.glyph = new PatternGlyph(compiled, drawingPattern);
        }

        drawingPart = null;
        drawingPattern = null;

        updateListener.accept(spellPart);

        MinecraftClient.getInstance().player.playSoundToPlayer(
                ModSounds.COMPLETE, SoundCategory.MASTER,
                1f, patternSize > 1 ? 1f : 0.6f
        );
    }

    public void replaceCallback(Fragment fragment) {
        if (toBeReplaced != null) {
            toBeReplaced.glyph = fragment;
            toBeReplaced = null;
            updateListener.accept(spellPart);
        }
    }

    public boolean isDrawing() {
        return drawingPart != null;
    }

    protected boolean setSubPartInTree(SpellPart target, Optional<SpellPart> replacement, SpellPart current) {
        if (current.glyph instanceof SpellPart part) {
            if (part == target) {
                current.glyph = new PatternGlyph();
                return true;
            }

            if (setSubPartInTree(target, replacement, part)) {
                return true;
            }
        }

        int i = 0;
        for (var part : current.subParts) {
            if (part.isPresent()) {
                if (part.get() == target) {
                    current.subParts.set(i, replacement);
                    return true;
                }

                if (setSubPartInTree(target, replacement, part.get())) {
                    return true;
                }
            }
            i++;
        }

        return false;
    }

    protected static boolean hasOverlappingLines(List<Byte> pattern, byte p1, byte p2) {
        Byte first = null;

        for (Byte second : pattern) {
            if (first != null && (first == p1 && second == p2 || first == p2 && second == p1)) {
                return true;
            }

            first = second;
        }

        return false;
    }

    protected static boolean propagateMouseEvent(SpellPart part, float x, float y, float size, float startingAngle, double mouseX, double mouseY, MouseEventHandler callback) {
        var closest = part;
        var closestAngle = startingAngle;
        var closestX = x;
        var closestY = y;
        var closestSize = size;

        // These two dont need to be updated for the actual closest
        var initialDiffX = x - mouseX;
        var initialDiffY = y - mouseY;

        var centerAvailable = isCircleClickable(size) || part.glyph instanceof SpellPart;
        var closestDistanceSquared = centerAvailable ? initialDiffX * initialDiffX + initialDiffY * initialDiffY : Double.MAX_VALUE;

        int partCount = part.getSubParts().size();
        // We dont change this, its the same for all subcircles
        var nextSize = Math.min(size / 2, size / (float) (partCount / 2));
        int i = 0;
        for (var child : part.getSubParts()) {
            if (child.isPresent()) {
                var childPart = child.get();

                var angle = startingAngle + (2 * Math.PI) / partCount * i - (Math.PI / 2);

                var nextX = x + (size * Math.cos(angle));
                var nextY = y + (size * Math.sin(angle));
                var diffX = nextX - mouseX;
                var diffY = nextY - mouseY;
                var distanceSquared = diffX * diffX + diffY * diffY;

                if (distanceSquared < closestDistanceSquared) {
                    closest = childPart;
                    closestAngle = (float) angle;
                    closestX = (float) nextX;
                    closestY = (float) nextY;
                    closestSize = nextSize;
                    closestDistanceSquared = distanceSquared;
                }
            }

            i++;
        }

        if (Math.sqrt(closestDistanceSquared) <= size && size >= 16) {
            if (closest == part) {
                // Special handling for part glyphs, because of course
                // This makes it impossible to interact with direct parents of part glyphs, but thats not an issue
                if (closest.glyph instanceof SpellPart centerPart) {
                    closest = centerPart;
                    closestSize /= 3;
                } else {
                    return callback.handle(closest, closestX, closestY, closestSize);
                }
            }

            return propagateMouseEvent(closest, closestX, closestY, closestSize, closestAngle, mouseX, mouseY, callback);
        }

        return false;
    }

    interface MouseEventHandler {
        boolean handle(SpellPart part, float x, float y, float size);
    }

//    @Override
//    public void setX(int x) {
//        this.x = x + size;
//    }
//
//    @Override
//    public void setY(int y) {
//        this.y = y + size;
//    }
//
//    @Override
//    public int getX() {
//        return (int) (x - size);
//    }
//
//    @Override
//    public int getY() {
//        return (int) (y - size);
//    }
//
//    @Override
//    public int getWidth() {
//        return (int) size * 2;
//    }
//
//    @Override
//    public int getHeight() {
//        return (int) size * 2;
//    }
//
//    @Override
//    public void forEachChild(Consumer<ClickableWidget> consumer) {
//
//    }
}
