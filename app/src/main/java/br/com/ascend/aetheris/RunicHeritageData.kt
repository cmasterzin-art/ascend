package br.com.ascend.aetheris

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * 1.7.3.1 — O Legado das Runas:
 * - Heráldica Rúnica de Aetheris (8 símbolos, 4 formatos, 4 metais, cores, inscrições, ornamentos, 3 estandartes)
 * - Codex Rúnico completo (24 runas históricas do Elder Futhark com contextualização rigorosa)
 * - 3 Coleções Lendárias de Equipamentos (12 peças da Forja, distintas das 3 configurações salvas)
 * - 3 Estágios de Aprimoramento Cosmético (Forjado, Gravado, Consagrado)
 */

// ============================================================================
// 1. HERÁLDICA RÚNICA DE AETHERIS
// ============================================================================

@Entity(tableName = "heraldry_profile")
data class HeraldryProfile(
    @PrimaryKey val id: Int = 1,
    val symbolId: String = "raven",
    val shapeId: String = "round_shield",
    val metalId: String = "bronze",
    val secondaryColorId: String = "fjord_blue",
    val inscriptionRunes: String = "",
    val ornamentId: String = "none",
    val bannerId: String = "wool",
    val showOnPortrait: Boolean = true,
    val updated: String = today()
)

data class HeraldrySymbolDef(
    val id: String,
    val name: String,
    val atlasCell: Int,
    val lore: String,
    val unlockLabel: String,
    val defaultUnlocked: Boolean = false
)

val heraldrySymbols: List<HeraldrySymbolDef> = listOf(
    HeraldrySymbolDef("raven", "Corvo do Norte", 0, "Símbolo de observação atenta e memória que atravessa a névoa dos fiordes.", "Disponível desde o início", true),
    HeraldrySymbolDef("wolf", "Lobo da Vigília", 1, "Entalhe escandinavo de lealdade, passo firme e vigília constante no inverno.", "Nível 3+ ou 1 expedição concluída"),
    HeraldrySymbolDef("oak", "Carvalho Ancestral", 2, "Raízes entrelaçadas em estilo Borre que celebram crescimento silencioso e duradouro.", "Disponível desde o início", true),
    HeraldrySymbolDef("axe", "Machado do Juramento", 3, "Lâmina gravada que representa trabalho manual, corte limpo e palavra cumprida.", "A Forja etapa 1+ ou 1 peça forjada"),
    HeraldrySymbolDef("shield", "Escudo da Persistência", 4, "Umbo de ferro e borda rúnica para quem sustenta a própria rotina sem recuar.", "Disponível desde o início", true),
    HeraldrySymbolDef("tome", "Tomo de Mímir", 5, "Volume encadernado em bronze para os guardiões de cadernos, notas e memória.", "Conquista 'Primeiro Tomo' ou 1 caderno em Mímir"),
    HeraldrySymbolDef("mountain", "Montanha Boreal", 6, "Cumes talhados sob o frio do Norte, marco de disciplina silenciosa.", "Disponível desde o início", true),
    HeraldrySymbolDef("yggdrasil", "Árvore dos Nove Reinos", 7, "Entrelaçamento Urnes que reúne raiz, tronco e horizonte em um só emblema.", "Nível 6+, 1 construção restaurada ou 1 coleção completa")
)

data class HeraldryShapeDef(val id: String, val name: String, val atlasCell: Int, val description: String)

val heraldryShapes: List<HeraldryShapeDef> = listOf(
    HeraldryShapeDef("round_shield", "Escudo Circular", 0, "Escudo viking de madeira escura com aro de ferro rebitado."),
    HeraldryShapeDef("oval_shield", "Escudo Oval", 1, "Escudo alongado nórdico com borda martelada."),
    HeraldryShapeDef("runic_plaque", "Placa Rúnica", 2, "Ardósia vulcânica chanfrada com entrelace escandinavo."),
    HeraldryShapeDef("ceremonial_medallion", "Medalhão Cerimonial", 3, "Moldura octogonal de metal batido para selos de honra.")
)

data class HeraldryMetalDef(
    val id: String,
    val name: String,
    val tint: Color,
    val rimColor: Color,
    val unlockLabel: String,
    val defaultUnlocked: Boolean = false
)

val heraldryMetals: List<HeraldryMetalDef> = listOf(
    HeraldryMetalDef("iron", "Ferro Enegrecido", Color(0xFF7D8794), Color(0xFF5A6573), "Disponível desde o início", true),
    HeraldryMetalDef("bronze", "Bronze Envelhecido", Color(0xFFC29B63), Color(0xFF9F7A46), "Disponível desde o início", true),
    HeraldryMetalDef("silver", "Prata Oxidada", Color(0xFFC5D0DE), Color(0xFF93A5BC), "Nível 4+ ou 1 expedição concluída"),
    HeraldryMetalDef("gold", "Ouro Ancestral", Color(0xFFE0BC72), Color(0xFFB88E3E), "Nível 8+, 1 peça Consagrada ou 1 coleção completa")
)

data class HeraldrySecondaryColorDef(val id: String, val name: String, val color: Color)

val heraldrySecondaryColors: List<HeraldrySecondaryColorDef> = listOf(
    HeraldrySecondaryColorDef("fjord_blue", "Azul Fiorde", Color(0xFF3E5A78)),
    HeraldrySecondaryColorDef("crimson_ember", "Brasa Antiga", Color(0xFF7C3B34)),
    HeraldrySecondaryColorDef("forest_moss", "Musgo do Norte", Color(0xFF3C5D49)),
    HeraldrySecondaryColorDef("obsidian", "Carvão Vulcânico", Color(0xFF232830)),
    HeraldrySecondaryColorDef("ivory_ash", "Cinza de Velino", Color(0xFF7A7366))
)

data class HeraldryOrnamentDef(
    val id: String,
    val name: String,
    val atlasCell: Int?, // cells 4..7 of forge_consacration_atlas
    val unlockLabel: String,
    val defaultUnlocked: Boolean = false
)

val heraldryOrnaments: List<HeraldryOrnamentDef> = listOf(
    HeraldryOrnamentDef("none", "Sem ornamento", null, "Disponível desde o início", true),
    HeraldryOrnamentDef("iron_filigree", "Entalhe de Ferro", 4, "Disponível desde o início", true),
    HeraldryOrnamentDef("bronze_borre", "Entrelaçado Borre", 5, "A Forja etapa 1+"),
    HeraldryOrnamentDef("silver_ringerike", "Filigrana Ringerike", 6, "1 peça Gravada (estágio II) na Forja"),
    HeraldryOrnamentDef("ceremonial_gold", "Coroa Cerimonial", 7, "1 peça Consagrada (estágio III) ou 1 coleção completa")
)

data class HeraldryBannerDef(
    val id: String,
    val name: String,
    val atlasCell: Int, // cells 4..6 of viking_heraldic_bases_atlas
    val description: String,
    val unlockLabel: String,
    val defaultUnlocked: Boolean = false
)

val heraldryBanners: List<HeraldryBannerDef> = listOf(
    HeraldryBannerDef("wool", "Estandarte de Tecido", 4, "Lã nórdica tecida em trave de ferro forjado.", "Disponível desde o início", true),
    HeraldryBannerDef("leather", "Estandarte de Couro", 5, "Couro curtido com costura aparente e anéis de bronze.", "Disponível desde o início", true),
    HeraldryBannerDef("ceremonial", "Estandarte Cerimonial", 6, "Tecido escuro emoldurado por nós Borre de honra.", "Nível 5+ ou 1 conquista Épica/Lendária")
)

fun heraldrySymbolById(id: String): HeraldrySymbolDef = heraldrySymbols.find { it.id == id } ?: heraldrySymbols.first()
fun heraldryShapeById(id: String): HeraldryShapeDef = heraldryShapes.find { it.id == id } ?: heraldryShapes.first()
fun heraldryMetalById(id: String): HeraldryMetalDef = heraldryMetals.find { it.id == id } ?: heraldryMetals[1]
fun heraldryColorById(id: String): HeraldrySecondaryColorDef = heraldrySecondaryColors.find { it.id == id } ?: heraldrySecondaryColors.first()
fun heraldryOrnamentById(id: String): HeraldryOrnamentDef = heraldryOrnaments.find { it.id == id } ?: heraldryOrnaments.first()
fun heraldryBannerById(id: String): HeraldryBannerDef = heraldryBanners.find { it.id == id } ?: heraldryBanners.first()

fun resolvedHeraldry(s: GameState): HeraldryProfile =
    s.heraldry ?: HeraldryProfile(updated = s.player?.created ?: today())

fun parseHeraldryInscription(raw: String): List<HistoricalFutharkRune> =
    raw.split(',').map { it.trim() }.filter { it.isNotBlank() }.mapNotNull { HistoricalFutharkRune.byStableId(it) }.take(6)

fun heraldrySymbolUnlocked(s: GameState, symbolId: String): Boolean = when (symbolId) {
    "raven", "oak", "shield", "mountain" -> true
    "wolf" -> s.level >= 3 || allExpeditions.any { expeditionComplete(s, it) }
    "axe" -> forgeStage(s) >= 1 || s.assets.any { it.id in forgeItemIds }
    "tome" -> s.achievements.any { it.id == "mimir_first_tome" } || s.notebooks.isNotEmpty()
    "yggdrasil" -> s.level >= 6 || s.assets.any { it.kind == "building" && it.stage >= 2 } || legendaryCollections.any { isCollectionComplete(s, it) }
    else -> false
}

fun heraldryMetalUnlocked(s: GameState, metalId: String): Boolean = when (metalId) {
    "iron", "bronze" -> true
    "silver" -> s.level >= 4 || allExpeditions.any { expeditionComplete(s, it) }
    "gold" -> s.level >= 8 || s.assets.any { it.id in forgeItemIds && it.stage >= FORGE_STAGE_CONSECRATED } || legendaryCollections.any { isCollectionComplete(s, it) }
    else -> false
}

fun heraldryOrnamentUnlocked(s: GameState, ornamentId: String): Boolean = when (ornamentId) {
    "none", "iron_filigree" -> true
    "bronze_borre" -> forgeStage(s) >= 1
    "silver_ringerike" -> s.assets.any { it.id in forgeItemIds && it.stage >= FORGE_STAGE_ENGRAVED }
    "ceremonial_gold" -> s.assets.any { it.id in forgeItemIds && it.stage >= FORGE_STAGE_CONSECRATED } || legendaryCollections.any { isCollectionComplete(s, it) }
    else -> false
}

fun heraldryBannerUnlocked(s: GameState, bannerId: String): Boolean = when (bannerId) {
    "wool", "leather" -> true
    "ceremonial" -> s.level >= 5 || s.achievements.any { a -> achievementDefs.any { it.id == a.id && it.rarity in listOf("Épica", "Lendária") } }
    else -> false
}

fun validateHeraldryProfile(h: HeraldryProfile) {
    require(h.id == 1) { "Identificador de brasão inválido." }
    require(heraldrySymbols.any { it.id == h.symbolId }) { "Símbolo heráldico inválido." }
    require(heraldryShapes.any { it.id == h.shapeId }) { "Formato de brasão inválido." }
    require(heraldryMetals.any { it.id == h.metalId }) { "Metal heráldico inválido." }
    require(heraldrySecondaryColors.any { it.id == h.secondaryColorId }) { "Cor secundária heráldica inválida." }
    require(heraldryOrnaments.any { it.id == h.ornamentId }) { "Ornamento heráldico inválido." }
    require(heraldryBanners.any { it.id == h.bannerId }) { "Estandarte inválido." }
    LocalDate.parse(h.updated)
    if (h.inscriptionRunes.isNotBlank()) {
        val parts = h.inscriptionRunes.split(',').map { it.trim() }.filter { it.isNotBlank() }
        require(parts.size <= 6) { "A inscrição heráldica aceita até 6 runas." }
        parts.forEach { token ->
            require(HistoricalFutharkRune.byStableId(token) != null) { "Runa inválida na inscrição: $token." }
        }
    }
}

/**
 * Approximate phonetic transliteration from Latin text to Elder Futhark characters (up to 6 runes).
 * Explicitly presented to users as an approximate transliteration, never as Old Norse translation.
 */
fun approximateRuneTransliteration(input: String): List<HistoricalFutharkRune> {
    val clean = input.trim().lowercase()
        .replace("á", "a").replace("à", "a").replace("â", "a").replace("ã", "a")
        .replace("é", "e").replace("ê", "e")
        .replace("í", "i")
        .replace("ó", "o").replace("ô", "o").replace("õ", "o")
        .replace("ú", "u")
        .replace("ç", "s")
    val out = mutableListOf<HistoricalFutharkRune>()
    var i = 0
    while (i < clean.length && out.size < 6) {
        val next2 = if (i + 1 < clean.length) clean.substring(i, i + 2) else ""
        val rune = when {
            next2 == "th" -> { i += 2; HistoricalFutharkRune.Thurisaz }
            next2 == "ng" -> { i += 2; HistoricalFutharkRune.Ingwaz }
            else -> {
                val ch = clean[i++]
                when (ch) {
                    'f', 'v' -> HistoricalFutharkRune.Fehu
                    'u' -> HistoricalFutharkRune.Uruz
                    'a' -> HistoricalFutharkRune.Ansuz
                    'r' -> HistoricalFutharkRune.Raido
                    'k', 'c', 'q' -> HistoricalFutharkRune.Kenaz
                    'g' -> HistoricalFutharkRune.Gebo
                    'w' -> HistoricalFutharkRune.Wunjo
                    'h' -> HistoricalFutharkRune.Hagalaz
                    'n' -> HistoricalFutharkRune.Nauthiz
                    'i' -> HistoricalFutharkRune.Isa
                    'j', 'y' -> HistoricalFutharkRune.Jera
                    'p' -> HistoricalFutharkRune.Perthro
                    'z' -> HistoricalFutharkRune.Algiz
                    's' -> HistoricalFutharkRune.Sowilo
                    't' -> HistoricalFutharkRune.Tiwaz
                    'b' -> HistoricalFutharkRune.Berkano
                    'e' -> HistoricalFutharkRune.Ehwaz
                    'm' -> HistoricalFutharkRune.Mannaz
                    'l' -> HistoricalFutharkRune.Laguz
                    'd' -> HistoricalFutharkRune.Dagaz
                    'o' -> HistoricalFutharkRune.Othala
                    else -> null
                }
            }
        }
        if (rune != null) out += rune
    }
    return out
}

// ============================================================================
// 2. CODEX RÚNICO — 24 RUNAS HISTÓRICAS DO ELDER FUTHARK
// ============================================================================

data class CodexSurfaceDef(val id: String, val name: String, val atlasCell: Int, val inkColor: Color)

val codexSurfaces: List<CodexSurfaceDef> = listOf(
    CodexSurfaceDef("dark_stone", "Pedra Escura", 0, Color(0xFFE7DFD0)),
    CodexSurfaceDef("light_stone", "Pedra Clara", 1, Color(0xFF1F2329)),
    CodexSurfaceDef("iron", "Ferro Entalhado", 2, Color(0xFFDCE4EE)),
    CodexSurfaceDef("vellum", "Velino Antigo", 3, Color(0xFF282018)),
    CodexSurfaceDef("wood", "Madeira Escandinava", 4, Color(0xFFEBD9BA)),
    CodexSurfaceDef("bronze", "Bronze Cerimonial", 5, Color(0xFF1E1710))
)

const val CODEX_HISTORICAL_DISCLAIMER =
    "Nota histórica: O Elder Futhark (24 runas) foi o alfabeto rúnico mais antigo dos povos germânicos (aprox. séc. II–VIII d.C.). " +
        "Durante a Era Viking (aprox. séc. VIII–XI d.C.), o sistema predominante na Escandinávia evoluiu para o Younger Futhark, reduzido a 16 runas. " +
        "Neste Codex, dados linguísticos reconstruídos são separados explicitamente das associações ficcionais do universo de Aetheris."

data class RuneCodexEntry(
    val rune: HistoricalFutharkRune,
    val aett: String,
    val reconstructedName: String,
    val documentedMeaning: String,
    val historicalContext: String,
    val aetherisAssociation: String
)

val codexRuneEntries: List<RuneCodexEntry> = listOf(
    RuneCodexEntry(HistoricalFutharkRune.Fehu, "1º Ætt · Freyr", "*Fehu", "Gado, bens móveis, riqueza", "Primeira runa do Elder Futhark; seu nome proto-germânico reconstruído refere-se a rebanho e sustento material nas sociedades agrárias.", "Em Aetheris (ficção): marca recompensas conquistadas pelo esforço honesto e o ofício da Forja."),
    RuneCodexEntry(HistoricalFutharkRune.Uruz, "1º Ætt · Freyr", "*Ūruz", "Auroque (boi selvagem)", "Associada ao auroque europeu, animal extinto conhecido por seu porte e resistência; preserva o som vocálico /u/.", "Em Aetheris (ficção): representa o atributo Vigor e a resistência física cultivada no cotidiano."),
    RuneCodexEntry(HistoricalFutharkRune.Thurisaz, "1º Ætt · Freyr", "*Þurisaz", "Gigante (þurs) / espinho nos poemas tardios", "Representa a fricativa dental /θ/ (th). Nos poemas rúnicos medievais posteriores, o nome varia entre gigante (nórdico/islandês) e espinho (anglo-saxão).", "Em Aetheris (ficção): ligada a limites firmes e à lâmina do Machado da Vigília."),
    RuneCodexEntry(HistoricalFutharkRune.Ansuz, "1º Ætt · Freyr", "*Ansuz", "Divindade (Áss) / palavra em tradições tardias", "Fonema /a/. Nos testemunhos antigos refere-se ao grupo de divindades germânicas Æsir; no poema anglo-saxão relaciona-se à boca e à fala.", "Em Aetheris (ficção): selo da Biblioteca de Mímir, estudo atento e memória registrada."),
    RuneCodexEntry(HistoricalFutharkRune.Raido, "1º Ætt · Freyr", "*Raidō", "Cavalgada, viagem, percurso", "Fonema /r/. O termo reconstruído indica deslocamento ordenado e travessia por estrada ou carruagem.", "Em Aetheris (ficção): emblema das expedições, trilhas regionais e continuidade da Jornada."),
    RuneCodexEntry(HistoricalFutharkRune.Kenaz, "1º Ætt · Freyr", "*Kaunan (reconstrução debatida)", "Tocha (tradição nórdica) / úlcera (poemas tardios)", "Fonema /k/. A etimologia exata do nome proto-germânico é debatida por filólogos, alternando entre 'tocha' e 'ferida' nos poemas medievais.", "Em Aetheris (ficção): chama de foco concentrado e clareza do Arquimago Rúnico."),
    RuneCodexEntry(HistoricalFutharkRune.Gebo, "1º Ætt · Freyr", "*Gebō", "Dádiva, troca, hospitalidade", "Fonema /g/. Reflete o conceito social de reciprocidade e hospitalidade nas culturas germânicas antigas; desapareceu como letra distinta no Younger Futhark.", "Em Aetheris (ficção): simboliza vínculos da vila, apoio mútuo e a Fíbula de Yggdrasil."),
    RuneCodexEntry(HistoricalFutharkRune.Wunjo, "1º Ætt · Freyr", "*Wunjō", "Alegria, bem-estar", "Fonema /w/. O nome deriva do poema rúnico anglo-saxão (wynn); também não sobreviveu como caractere separado no Younger Futhark.", "Em Aetheris (ficção): lembra que o descanso planejado sustenta a constância sem culpa."),
    RuneCodexEntry(HistoricalFutharkRune.Hagalaz, "2º Ætt · Hagal", "*Haglaz", "Granizo", "Fonema /h/. Runa inicial do segundo Ætt; aparece com uma haste transversal no Elder Futhark escandinavo e duas nas variantes continentais.", "Em Aetheris (ficção): marca provações inesperadas que temperam a disciplina."),
    RuneCodexEntry(HistoricalFutharkRune.Nauthiz, "2º Ætt · Hagal", "*Naudiz", "Necessidade, privação, esforço", "Fonema /n/. Presente tanto no Elder quanto no Younger Futhark, evoca o peso das circunstâncias que exigem sobriedade.", "Em Aetheris (ficção): representa os dias difíceis em que um único passo pequeno preserva o hábito."),
    RuneCodexEntry(HistoricalFutharkRune.Isa, "2º Ætt · Hagal", "*Īsaz", "Gelo", "Fonema /i/. Uma única haste vertical, mantida inalterada do Elder ao Younger Futhark; os poemas rúnicos descrevem o gelo como brilhante e escorregadio.", "Em Aetheris (ficção): símbolo da firmeza do Elmo do Juramento e da serenidade sob pressão."),
    RuneCodexEntry(HistoricalFutharkRune.Jera, "2º Ætt · Hagal", "*Jēran", "Ano, boa estação, colheita", "Fonema /j/. Relacionada ao ciclo anual das estações agrícolas; evoluiu posteriormente para a runa ár no Younger Futhark.", "Em Aetheris (ficção): celebra resultados que amadurecem ao longo de semanas e meses reais."),
    RuneCodexEntry(HistoricalFutharkRune.Eihwaz, "2º Ætt · Hagal", "*Īwaz", "Teixo (árvore)", "Valor fonético /ï/ (vogal fechada debatida). O teixo era madeira valorizada para arcos e objetos duradouros; a runa caiu em desuso no Younger Futhark.", "Em Aetheris (ficção): inspira paciência de longo prazo nos Caminhos pessoais."),
    RuneCodexEntry(HistoricalFutharkRune.Perthro, "2º Ætt · Hagal", "*Perþō (significado incerto)", "Termo incerto (possivelmente jogo ou árvore frutífera)", "Fonema /p/. Seu significado original permanece desconhecido pelos historiadores; é conhecida sobretudo pelo poema rúnico anglo-saxão (peorð).", "Em Aetheris (ficção): ligada ao Cajado Rúnico e à curiosidade diante do ainda não compreendido."),
    RuneCodexEntry(HistoricalFutharkRune.Algiz, "2º Ætt · Hagal", "*Algiz (nome reconstruído debatido)", "Alce / junco (tradições distintas)", "Representava originalmente a consoante final /z/ (depois /R/). O nome reconstruído é incerto; não possui significado esotérico documentado na Antiguidade.", "Em Aetheris (ficção): selo da coleção Vigília Boreal e guarda das fronteiras do Norte."),
    RuneCodexEntry(HistoricalFutharkRune.Sowilo, "2º Ætt · Hagal", "*Sōwilō", "Sol", "Fonema /s/. Usada na escrita epigráfica comum do Elder Futhark na forma de zigue-zague simples de três traços; evoluiu para sól no Younger Futhark.", "Em Aetheris (ficção): orienta o Broche das Constelações e o retorno após pausas longas."),
    RuneCodexEntry(HistoricalFutharkRune.Tiwaz, "3º Ætt · Tyr", "*Tīwaz", "Deus Týr / céu diurno", "Fonema /t/. Primeira runa do terceiro Ætt; o nome vincula-se à divindade germânica da lei e dos juramentos (Týr) e sobreviveu no Younger Futhark.", "Em Aetheris (ficção): marca a Lâmina do Norte e a coleção Juramento de Ferro."),
    RuneCodexEntry(HistoricalFutharkRune.Berkano, "3º Ætt · Tyr", "*Berkanan", "Bétula (árvore)", "Fonema /b/. Referência à bétula, árvore resistente das regiões nórdicas; preservada no Younger Futhark como bjarkan.", "Em Aetheris (ficção): emblema dos hábitos diários que florescem em ciclos contínuos."),
    RuneCodexEntry(HistoricalFutharkRune.Ehwaz, "3º Ætt · Tyr", "*Ehwaz", "Cavalo", "Fonema /e/. Comum em inscrições antigas do Elder Futhark (como bracteatas e pedras); deixou de existir como letra separada no Younger Futhark.", "Em Aetheris (ficção): ligada à Armadura da Vigília e ao ritmo estável das jornadas."),
    RuneCodexEntry(HistoricalFutharkRune.Mannaz, "3º Ætt · Tyr", "*Mannaz", "Ser humano, pessoa", "Fonema /m/. Documentada nos poemas rúnicos como 'homem/humanidade'; assumiu forma distinta (maðr) no Younger Futhark.", "Em Aetheris (ficção): expressa a identidade pessoal do aventureiro e a tecelagem de escolhas."),
    RuneCodexEntry(HistoricalFutharkRune.Laguz, "3º Ætt · Tyr", "*Laguz", "Água, lago, mar", "Fonema /l/. Comum em todo o período rúnico (Elder e Younger Futhark), remetendo à navegação e às águas do Norte.", "Em Aetheris (ficção): simboliza o fluxo das crônicas semanais e do Salão das Memórias."),
    RuneCodexEntry(HistoricalFutharkRune.Ingwaz, "3º Ætt · Tyr", "*Ingwaz", "Figura lendária/divina Ing (Yngvi)", "Representava o som nasal /ŋ/ (ng). Aparece em forma de losango simples em inscrições antigas; desapareceu no Younger Futhark.", "Em Aetheris (ficção): simboliza projetos semeados hoje para gerar fruto futuro."),
    RuneCodexEntry(HistoricalFutharkRune.Dagaz, "3º Ætt · Tyr", "*Dagaz", "Dia, luz do dia", "Fonema /d/. Presente no Elder Futhark e no Futhorc anglo-saxão, mas ausente no Younger Futhark escandinavo.", "Em Aetheris (ficção): encarna cada recomeço diário e a Moldura dos Nove Reinos."),
    RuneCodexEntry(HistoricalFutharkRune.Othala, "3º Ætt · Tyr", "*Ōþalan", "Herança, propriedade ancestral, lar", "Fonema /o/. Forma histórica pura de losango com pernas cruzadas simples (sem apêndices modernos); caiu em desuso no Younger Futhark.", "Em Aetheris (ficção): representa a Vila do Despertar reconstruída e o legado preservado no backup local.")
)

fun codexEntryFor(rune: HistoricalFutharkRune): RuneCodexEntry =
    codexRuneEntries.first { it.rune == rune }

// ============================================================================
// 3. APRIMORAMENTO DE EQUIPAMENTOS DA FORJA (3 ESTÁGIOS COSMÉTICOS)
// ============================================================================

const val FORGE_STAGE_FORGED = 1
const val FORGE_STAGE_ENGRAVED = 2
const val FORGE_STAGE_CONSECRATED = 3

data class ForgeUpgradeStageDef(
    val stage: Int,
    val name: String,
    val shortLabel: String,
    val atlasCell: Int, // cells 0..2 in forge_consacration_atlas.webp
    val description: String
)

val forgeUpgradeStages: List<ForgeUpgradeStageDef> = listOf(
    ForgeUpgradeStageDef(FORGE_STAGE_FORGED, "Forjado", "I · Forjado", 0, "Estado original recém-saído da bigorna de Darian."),
    ForgeUpgradeStageDef(FORGE_STAGE_ENGRAVED, "Gravado", "II · Gravado", 1, "Acabamento metálico polido, entalhes Borre e selo rúnico gravado."),
    ForgeUpgradeStageDef(FORGE_STAGE_CONSECRATED, "Consagrado", "III · Consagrado", 2, "Apresentação cerimonial lendária com selo solar e moldura aprimorada.")
)

fun forgeUpgradeStageDef(stage: Int): ForgeUpgradeStageDef =
    forgeUpgradeStages.find { it.stage == stage.coerceIn(FORGE_STAGE_FORGED, FORGE_STAGE_CONSECRATED) }
        ?: forgeUpgradeStages.first()

fun forgeUpgradeCost(recipe: ForgeRecipe, targetStage: Int): Int = when (targetStage) {
    FORGE_STAGE_ENGRAVED -> when {
        recipe.rarity >= 4 -> 55
        recipe.rarity == 3 -> 45
        else -> 35
    }
    FORGE_STAGE_CONSECRATED -> when {
        recipe.rarity >= 4 -> 90
        recipe.rarity == 3 -> 75
        else -> 60
    }
    else -> 0
}

fun forgeUpgradeMinForgeStage(targetStage: Int): Int =
    if (targetStage >= FORGE_STAGE_CONSECRATED) 2 else 1

fun forgeUpgradeMinLevel(targetStage: Int): Int =
    if (targetStage >= FORGE_STAGE_CONSECRATED) 7 else 4

fun forgeUpgradeMissingReasons(s: GameState, recipe: ForgeRecipe, targetStage: Int): List<String> = buildList {
    val asset = s.assets.find { it.id == recipe.id }
    if (asset == null) {
        add("Forje ${recipe.name} antes de aprimorar")
        return@buildList
    }
    if (targetStage !in FORGE_STAGE_ENGRAVED..FORGE_STAGE_CONSECRATED) {
        add("Estágio de aprimoramento inválido")
        return@buildList
    }
    if (asset.stage >= targetStage) {
        add("Esta peça já alcançou o estágio ${forgeUpgradeStageDef(targetStage).name}")
        return@buildList
    }
    if (targetStage != asset.stage + 1) {
        add("Conclua o estágio ${forgeUpgradeStageDef(asset.stage + 1).name} antes de avançar")
    }
    val reqForge = forgeUpgradeMinForgeStage(targetStage)
    if (forgeStage(s) < reqForge) {
        add(if (reqForge == 2) "Conclua a restauração de A Forja (etapa 2)" else "Restaure A Forja (etapa 1)")
    }
    val reqLevel = forgeUpgradeMinLevel(targetStage)
    if (s.level < reqLevel) {
        add("Alcance o nível $reqLevel (atual: ${s.level})")
    }
}

fun canUpgradeForgeItem(s: GameState, recipe: ForgeRecipe, targetStage: Int): Boolean {
    if (forgeUpgradeMissingReasons(s, recipe, targetStage).isNotEmpty()) return false
    return s.rawCoins >= forgeUpgradeCost(recipe, targetStage)
}

/**
 * Honest visual scope statement for upgrade stages (Section 11.3):
 * clearly states when upgrades apply a calibrated overlay on the Living Portrait vs. Arsenal/Forge frame & seal.
 */
fun upgradeVisualScopeFor(itemId: String): String {
    val item = shopItems.find { it.id == itemId } ?: return "Apresentação aprimorada no nicho e selo do Arsenal."
    return when (item.kind) {
        "outfit" -> "Aplica acabamento de gola Borre (Gravado) e aplique cerimonial no manto (Consagrado) sobre o Retrato Vivo, além de selo e moldura aprimorada."
        "frame" -> "Aplica coroa filigranada sobre o topo da moldura do Retrato Vivo e selo cerimonial no Arsenal."
        "head", "accessory" -> "Exibido pelo selo de estágio, moldura aprimorada e brilho discreto do nicho/retrato (sem alterar a pintura facial base)."
        else -> "Exibido pela moldura viking aprimorada, marcador contextual e selo de estágio no Arsenal e na Forja."
    }
}

// ============================================================================
// 4. COLEÇÕES LENDÁRIAS DE EQUIPAMENTOS (3 × 4 PEÇAS = 12 PEÇAS DA FORJA)
// ============================================================================

data class LegendaryCollectionDef(
    val id: String,
    val name: String,
    val rewardTitle: String,
    val paintingAtlasCell: Int,   // cells 0..2 in legendary_collections_atlas.webp
    val medallionAtlasCell: Int,  // cells 3..5 in legendary_collections_atlas.webp
    val rune: HistoricalFutharkRune,
    val itemIds: List<String>,
    val description: String,
    val loreNote: String,
    val accent: Color
)

val legendaryCollections: List<LegendaryCollectionDef> = listOf(
    LegendaryCollectionDef(
        id = "iron_oath",
        name = "Juramento de Ferro",
        rewardTitle = "Juramento de Ferro",
        paintingAtlasCell = 0,
        medallionAtlasCell = 3,
        rune = HistoricalFutharkRune.Tiwaz,
        itemIds = listOf("oath-helm", "north-mail", "vigil-axe", "yggdrasil-brooch"),
        description = "Ferro escurecido, malha, peles e lâmina entalhada para quem mantém a palavra nos dias frios.",
        loreNote = "Forjada segundo os moldes dos guardiões dos fiordes, a coleção reúne proteção e constância.",
        accent = Color(0xFFB5BAC4)
    ),
    LegendaryCollectionDef(
        id = "mimir_wisdom",
        name = "Sabedoria de Mímir",
        rewardTitle = "Herdeiro de Mímir",
        paintingAtlasCell = 1,
        medallionAtlasCell = 4,
        rune = HistoricalFutharkRune.Ansuz,
        itemIds = listOf("mimir-runic-crown", "scribe-robes", "runic-staff", "rune-mantle"),
        description = "Bronze envelhecido, prata rúnica, manuscritos e vestes dos salões ancestrais de Aetheris.",
        loreNote = "Vestes do Escriba e Manto das Runas ocupam o mesmo espaço de traje; a coleção é concluída pela posse legítima das quatro obras.",
        accent = Color(0xFFC8A66B)
    ),
    LegendaryCollectionDef(
        id = "boreal_vigil",
        name = "Vigília Boreal",
        rewardTitle = "Guardião Boreal",
        paintingAtlasCell = 2,
        medallionAtlasCell = 5,
        rune = HistoricalFutharkRune.Algiz,
        itemIds = listOf("vigil-armor", "northern-blade", "constellation-brooch", "nine-realms-frame"),
        description = "Prata oxidada, ferro temperado e pedras frias sob o horizonte da aurora boreal.",
        loreNote = "Reúne as obras mais exigentes de Darian, celebrando o caminho completo pelas terras do Norte.",
        accent = Color(0xFF8FB4D9)
    )
)

fun legendaryCollectionById(id: String): LegendaryCollectionDef? = legendaryCollections.find { it.id == id }

fun collectionForItem(itemId: String): LegendaryCollectionDef? =
    legendaryCollections.find { itemId in it.itemIds }

fun collectionOwnedItems(s: GameState, col: LegendaryCollectionDef): List<String> {
    val owned = s.assets.map { it.id }.toSet()
    return col.itemIds.filter { it in owned }
}

fun collectionMissingItems(s: GameState, col: LegendaryCollectionDef): List<String> {
    val owned = s.assets.map { it.id }.toSet()
    return col.itemIds.filter { it !in owned }
}

fun isCollectionComplete(s: GameState, col: LegendaryCollectionDef): Boolean =
    collectionOwnedItems(s, col).size == col.itemIds.size

fun collectionRewardClaimed(s: GameState, col: LegendaryCollectionDef): Boolean =
    s.assets.any { it.id == "collection:${col.id}" }

fun completedCollectionTitles(s: GameState): List<String> =
    legendaryCollections.filter { isCollectionComplete(s, it) }.map { it.rewardTitle }

// ============================================================================
// 5. PRÉVIA SEGURA DE APLICAÇÃO DE CONFIGURAÇÕES (LOADOUTS — SEÇÃO 6.3)
// ============================================================================

data class LoadoutSlotChange(
    val kind: String,
    val slotLabel: String,
    val previousItemId: String?,
    val nextItemId: String?
)

data class LoadoutApplicationPreview(
    val loadout: EquipmentLoadout,
    val availableItemIds: List<String>,
    val missingItemIds: List<String>,
    val slotChanges: List<LoadoutSlotChange>,
    val preservesOtherSlots: Boolean
)

fun previewLoadoutApplication(loadout: EquipmentLoadout, s: GameState): LoadoutApplicationPreview {
    val owned = s.assets.map { it.id }.toSet()
    val equippedByKind = s.assets.filter { it.equipped }.associate { it.kind to it.id }
    val slotMap = loadout.slotMap()
    val available = slotMap.values.filter { it in owned }
    val missing = slotMap.values.filter { it !in owned }
    val partial = missing.isNotEmpty()
    val changes = buildList {
        equipmentSlots.forEach { (kind, label) ->
            val current = equippedByKind[kind]
            val target = slotMap[kind]
            if (partial) {
                // Partial application: only updates slots whose target item is owned; preserves all other valid equipped slots.
                if (target != null && target in owned && target != current) {
                    add(LoadoutSlotChange(kind, label, current, target))
                }
            } else {
                val resolvedTarget = target?.takeIf { it in owned }
                if (resolvedTarget != current) {
                    add(LoadoutSlotChange(kind, label, current, resolvedTarget))
                }
            }
        }
    }
    return LoadoutApplicationPreview(loadout, available, missing, changes, partial)
}
