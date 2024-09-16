package dev.enjarai.trickster.spell.trick.particle;

import dev.enjarai.trickster.cca.ModEntityCumponents;
import dev.enjarai.trickster.fleck.LineFleck;
import dev.enjarai.trickster.spell.Fragment;
import dev.enjarai.trickster.spell.Pattern;
import dev.enjarai.trickster.spell.SpellContext;
import dev.enjarai.trickster.spell.fragment.FragmentType;
import dev.enjarai.trickster.spell.trick.Trick;
import dev.enjarai.trickster.spell.trick.blunder.BlunderException;
import org.joml.Vector3f;

import java.util.List;

public class LineFleckTrick extends Trick {
    public LineFleckTrick() {
        super(Pattern.of(
                2, 5, 7, 4, 3, 1, 2
        ));
    }

    @Override
    public Fragment activate(SpellContext ctx, List<Fragment> fragments) throws BlunderException {
        var id = expectInput(fragments, FragmentType.NUMBER, 0).asInt();
        var pos1 = expectInput(fragments, FragmentType.VECTOR, 1).vector();
        var pos2 = expectInput(fragments, FragmentType.VECTOR, 2).vector();
        var entities = supposeInput(fragments, FragmentType.LIST, 3); // list of entities / players to render to

        ctx.source().getWorld().getPlayers().stream().filter(n -> true).forEach( //todo only show this to relevant players
                player -> player.getComponent(ModEntityCumponents.FLECKS).addFleck(id, new LineFleck(
                        pos1.get(new Vector3f()),
                        pos2.get(new Vector3f())
                ))
        );

        return fragments.getFirst(); //id for passthrough, like bars
    }
}
