package dev.enjarai.trickster.spell;

import dev.enjarai.trickster.spell.blunder.OverweightFragmentBlunder;
import dev.enjarai.trickster.spell.execution.executor.DefaultSpellExecutor;
import dev.enjarai.trickster.spell.execution.executor.SpellExecutor;
import dev.enjarai.trickster.spell.fragment.FragmentType;
import dev.enjarai.trickster.spell.fragment.VoidFragment;
import dev.enjarai.trickster.spell.trick.Tricks;
import dev.enjarai.trickster.spell.blunder.BlunderException;
import dev.enjarai.trickster.spell.blunder.UnknownTrickBlunder;
import dev.enjarai.trickster.spell.trick.func.ForkingTrick;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import net.minecraft.text.Text;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;
import java.util.stream.Stream;

public record PatternGlyph(Pattern pattern) implements Fragment {
    public static final StructEndec<PatternGlyph> ENDEC = StructEndecBuilder.of(
            Pattern.ENDEC.fieldOf("pattern", PatternGlyph::pattern),
            PatternGlyph::new
    );

    public PatternGlyph() {
        this(Pattern.EMPTY);
    }

    public PatternGlyph(int... pattern) {
        this(Stream.of(ArrayUtils.toObject(pattern)).map(Integer::byteValue).toList());
    }

    public PatternGlyph(List<Byte> pattern) {
        this(Pattern.from(pattern));
    }

    @Override
    public Fragment activateAsGlyph(SpellContext ctx, List<Fragment> fragments) throws BlunderException {
        if (pattern.equals(Pattern.EMPTY)) {
            return VoidFragment.INSTANCE;
        }


        //handle custom tricks
        SpellPart customTrick = ctx.state().getImportedTricks().get(pattern);
        if (customTrick != null) {
            DefaultSpellExecutor executor = new DefaultSpellExecutor(customTrick, ctx.state().recurseOrThrow(fragments));
            return executor.singleTickRun(ctx);
        }

        var trick = Tricks.lookup(pattern);

        if (trick == null)
            throw new UnknownTrickBlunder();

        var result = trick.activate(ctx, fragments);

        if (result.getWeight() > Fragment.MAX_WEIGHT) {
            throw new OverweightFragmentBlunder(trick, result);
        }

        return result;
    }

    @Override
    public boolean forks(SpellContext ctx, List<Fragment> args) {
        return Tricks.lookup(pattern) instanceof ForkingTrick;
    }

    @Override
    public SpellExecutor makeFork(SpellContext ctx, List<Fragment> fragments) throws BlunderException {
        var trick = Tricks.lookup(pattern);

        if (trick instanceof ForkingTrick forkingTrick)
            return forkingTrick.makeFork(ctx, fragments);

        throw new UnknownTrickBlunder();
    }

    @Override
    public FragmentType<?> type() {
        return FragmentType.PATTERN;
    }

    @Override
    public Text asText() {
        var trick = Tricks.lookup(pattern);
        if (trick != null) {
            return trick.getName();
        }
        return Text.of("Unknown");
    }

    @Override
    public boolean asBoolean() {
        return pattern.asBoolean();
    }

    @Override
    public int getWeight() {
        return pattern.getWeight();
    }
}
