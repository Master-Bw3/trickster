package dev.enjarai.trickster.spell.trick.tree;

import dev.enjarai.trickster.spell.Fragment;
import dev.enjarai.trickster.spell.Pattern;
import dev.enjarai.trickster.spell.SpellContext;
import dev.enjarai.trickster.spell.SpellPart;
import dev.enjarai.trickster.spell.fragment.ListFragment;
import dev.enjarai.trickster.spell.blunder.AddressNotInTreeBlunder;
import dev.enjarai.trickster.spell.blunder.BlunderException;

import java.util.List;

public class SetGlyphTrick extends AbstractMetaTrick {
    public SetGlyphTrick() {
        super(Pattern.of(0, 1, 2, 4, 8, 7, 6));
    }

    @Override
    public Fragment distort(SpellContext ctx, List<Fragment> fragments) throws BlunderException {
        var spell = expectInput(fragments, SpellPart.class, 0);
        var addressFragment = expectInput(fragments, ListFragment.class, 1);
        var glyph = expectInput(fragments, Fragment.class, 2);

        var newSpell = spell.deepClone();
        var node = findNode(newSpell, addressFragment)
                .orElseThrow(() -> new AddressNotInTreeBlunder(this, addressFragment.sanitizeAddress(this)));
        node.glyph = glyph;

        return newSpell;
    }
}
