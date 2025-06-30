package dev.enjarai.trickster.coleus;
//

//import dev.enjarai.trickster.spell.Pattern;
//import dev.enjarai.trickster.spell.trick.Tricks;
//import mod.master_bw3.coleus.HtmlTemplateRegistry;
//import j2html.tags.DomContent;
//import net.minecraft.util.Identifier;
//
//import java.net.URLEncoder;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Path;
//import java.util.Arrays;
//import java.util.Map;
//
//import static j2html.TagCreator.*;

public class TricksterTemplateExpanders {
    //
    //    static DomContent glyphTemplate(Map<String, String> properties, Path pagePath, Path extraResourcesDir) {
    //        Pattern pattern;
    //        if (properties.containsKey("pattern")) {
    //            pattern = Pattern.from(
    //                    Arrays.stream(properties.get("pattern").split(","))
    //                            .map(s -> Byte.valueOf(s, 10)).toList()
    //            );
    //        } else if (properties.containsKey("trick-id")) {
    //            Identifier trickId = Identifier.of(properties.get("trick-id"));
    //            pattern = Tricks.REGISTRY.get(trickId).getPattern();
    //        } else {
    //            return text("");
    //        }
    //
    //        return iframe()
    //                .withSrc("http://localhost:3000/pattern/" + URLEncoder.encode(pattern.toBase64(), StandardCharsets.UTF_8))
    //                .withHeight("200")
    //                .withWidth("200");
    //    }
    //
    //    static void register() {
    //
    //        HtmlTemplateRegistry.register(Identifier.of("trickster", "pattern"), TricksterTemplateExpanders::glyphTemplate);
    //        HtmlTemplateRegistry.register(Identifier.of("trickster", "glyph"), "owo what's this");
    //        HtmlTemplateRegistry.register(Identifier.of("trickster", "spell-preview"), "owo what's this");
    //        HtmlTemplateRegistry.register(Identifier.of("trickster", "spell-preview-unloadable"), "owo what's this");
    //        HtmlTemplateRegistry.register(Identifier.of("trickster", "item-tag"), "owo what's this");
    //        HtmlTemplateRegistry.register(Identifier.of("trickster", "cost-rule"), "owo what's this");
    //    }

}
