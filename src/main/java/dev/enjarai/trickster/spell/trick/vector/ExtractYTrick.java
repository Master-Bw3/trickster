package dev.enjarai.trickster.spell.trick.vector;

import dev.enjarai.trickster.spell.Pattern;
import dev.enjarai.trickster.spell.SpellContext;
import dev.enjarai.trickster.spell.blunder.BlunderException;
import dev.enjarai.trickster.spell.fragment.FragmentType;
import dev.enjarai.trickster.spell.fragment.NumberFragment;
import dev.enjarai.trickster.spell.fragment.VectorFragment;
import dev.enjarai.trickster.spell.trick.DistortionTrick;
import dev.enjarai.trickster.spell.type.Signature;

public class ExtractYTrick extends DistortionTrick<ExtractYTrick> {
    public ExtractYTrick() {
        super(Pattern.of(0, 4, 7), Signature.of(FragmentType.VECTOR, ExtractYTrick::extract, FragmentType.NUMBER));
    }

    public NumberFragment extract(SpellContext ctx, VectorFragment vec) throws BlunderException {
        return new NumberFragment(vec.vector().y());
    }
}
