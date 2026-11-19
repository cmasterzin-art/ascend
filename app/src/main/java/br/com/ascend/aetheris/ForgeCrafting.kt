package br.com.ascend.aetheris

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * 1.7.3 — Persistência de Conjuntos de Equipamento (3 espaços configuráveis)
 * e Catálogo da Forja de Darian (8 receitas cosméticas originais).
 *
 * Regras invioláveis:
 * - Zero nova moeda (usa exclusivamente Fragmentos do ledger existente).
 * - Zero XP ou bônus de combate/economia ao forjar ou equipar conjuntos.
 * - Especialização `mestre_da_forja` concede apenas diálogo exclusivo e ornamento estético.
 */

@Entity(tableName = "equipment_loadouts")
data class EquipmentLoadout(
    @PrimaryKey val id: String,
    val name: String,
    val headId: String = "",
    val outfitId: String = "",
    val accessoryId: String = "",
    val weaponId: String = "",
    val companionId: String = "",
    val frameId: String = "",
    val backgroundId: String = "",
    val updated: String = today()
)

val defaultLoadoutDefinitions: List<Pair<String, String>> = listOf(
    "loadout:1" to "Jornada",
    "loadout:2" to "Conselho",
    "loadout:3" to "Expedição"
)

val validLoadoutIds: Set<String> = defaultLoadoutDefinitions.map { it.first }.toSet()

fun resolvedLoadouts(s: GameState): List<EquipmentLoadout> {
    val byId = s.loadouts.associateBy { it.id }
    return defaultLoadoutDefinitions.map { (id, defaultName) ->
        byId[id] ?: EquipmentLoadout(id = id, name = defaultName, updated = s.player?.created ?: today())
    }
}

fun EquipmentLoadout.slotMap(): Map<String, String> = linkedMapOf(
    "head" to headId,
    "outfit" to outfitId,
    "accessory" to accessoryId,
    "weapon" to weaponId,
    "companion" to companionId,
    "frame" to frameId,
    "background" to backgroundId
).filterValues { it.isNotBlank() }

fun EquipmentLoadout.slotFor(kind: String): String = when (kind) {
    "head" -> headId
    "outfit" -> outfitId
    "accessory" -> accessoryId
    "weapon" -> weaponId
    "companion" -> companionId
    "frame" -> frameId
    "background" -> backgroundId
    else -> ""
}

fun loadoutMissingItems(loadout: EquipmentLoadout, s: GameState): List<String> {
    val owned = s.assets.map { it.id }.toSet()
    return loadout.slotMap().values.filter { it !in owned }
}

fun loadoutIsEmpty(loadout: EquipmentLoadout): Boolean = loadout.slotMap().isEmpty()

fun validateLoadoutInput(loadout: EquipmentLoadout) {
    require(loadout.id in validLoadoutIds) { "Conjunto de equipamento inválido." }
    val trimmed = loadout.name.trim()
    require(trimmed.isNotEmpty() && trimmed.length <= 40) { "Informe um nome de 1 a 40 caracteres para o conjunto." }
    LocalDate.parse(loadout.updated)
    val checks = listOf(
        "head" to loadout.headId,
        "outfit" to loadout.outfitId,
        "accessory" to loadout.accessoryId,
        "weapon" to loadout.weaponId,
        "companion" to loadout.companionId,
        "frame" to loadout.frameId,
        "background" to loadout.backgroundId
    )
    checks.forEach { (kind, itemId) ->
        if (itemId.isNotBlank()) {
            val item = shopItems.find { it.id == itemId }
            require(item != null && item.kind == kind) {
                "Item '$itemId' não é compatível com o espaço '$kind'."
            }
        }
    }
}

data class ForgeRecipe(
    val id: String,
    val name: String,
    val kind: String,
    val rarity: Int,
    val cost: Int,
    val minForgeStage: Int,
    val minLevel: Int = 1,
    val requiredAchievementId: String? = null,
    val narrativeMaterials: String,
    val lore: String,
    val requirementLabel: String,
    val rune: HistoricalFutharkRune
)

val forgeRecipes: List<ForgeRecipe> = listOf(
    ForgeRecipe(
        id = "oath-helm",
        name = "Elmo do Juramento",
        kind = "head",
        rarity = 2,
        cost = 45,
        minForgeStage = 1,
        minLevel = 3,
        narrativeMaterials = "Ferro nórdico martelado, faixa de bronze envelhecido e couro curtido.",
        lore = "Forjado sobre a bigorna de Darian com visor aberto para quem sustenta a própria palavra.",
        requirementLabel = "A Forja (etapa 1+) · Nível 3+",
        rune = HistoricalFutharkRune.Isa
    ),
    ForgeRecipe(
        id = "north-mail",
        name = "Cota do Norte",
        kind = "outfit",
        rarity = 2,
        cost = 55,
        minForgeStage = 1,
        minLevel = 4,
        narrativeMaterials = "Anéis de ferro escuro, lã pesada de cinza vulcânica e ombreiras rebitadas.",
        lore = "Malha resistente às rajadas dos fiordes, tecida para longas travessias sem alarde.",
        requirementLabel = "A Forja (etapa 1+) · Nível 4+",
        rune = HistoricalFutharkRune.Berkano
    ),
    ForgeRecipe(
        id = "scribe-robes",
        name = "Vestes do Escriba",
        kind = "outfit",
        rarity = 2,
        cost = 60,
        minForgeStage = 1,
        requiredAchievementId = "mimir_first_tome",
        narrativeMaterials = "Linho espesso tingido em ardósia, fios de bronze antigo e gola dobrada.",
        lore = "Costurada para noites silenciosas entre pergaminhos, cadernos e memórias de Mímir.",
        requirementLabel = "A Forja (etapa 1+) · Conquista 'Primeiro Tomo'",
        rune = HistoricalFutharkRune.Ansuz
    ),
    ForgeRecipe(
        id = "yggdrasil-brooch",
        name = "Fíbula de Yggdrasil",
        kind = "accessory",
        rarity = 3,
        cost = 75,
        minForgeStage = 1,
        requiredAchievementId = "forest",
        narrativeMaterials = "Bronze antigo fundido em molde de pedra e pino de ferro batido.",
        lore = "Prende o manto com o entrelaçamento das raízes antigas, honrando quem atravessou a névoa.",
        requirementLabel = "A Forja (etapa 1+) · Expedição 'A Vigília da Floresta'",
        rune = HistoricalFutharkRune.Gebo
    ),
    ForgeRecipe(
        id = "mimir-runic-crown",
        name = "Coroa Rúnica de Mímir",
        kind = "head",
        rarity = 3,
        cost = 85,
        minForgeStage = 2,
        requiredAchievementId = "library_portico",
        narrativeMaterials = "Bronze escurecido, prata oxidada, entalhes rúnicos e pedra-da-lua pálida.",
        lore = "Diadema austero que repousa na fronte de quem recolhe saber sem pressa.",
        requirementLabel = "A Forja restaurada (etapa 2) · Conquista 'Abertura das Eras'",
        rune = HistoricalFutharkRune.Kenaz
    ),
    ForgeRecipe(
        id = "rune-mantle",
        name = "Manto das Runas",
        kind = "outfit",
        rarity = 3,
        cost = 90,
        minForgeStage = 2,
        minLevel = 6,
        narrativeMaterials = "Lã de obsidiana, alças de couro entalhado e borda de bronze geométrico.",
        lore = "Cada dobra leva o peso do silêncio e a memória dos compromissos cumpridos.",
        requirementLabel = "A Forja restaurada (etapa 2) · Nível 6+",
        rune = HistoricalFutharkRune.Raido
    ),
    ForgeRecipe(
        id = "vigil-armor",
        name = "Armadura da Vigília",
        kind = "outfit",
        rarity = 3,
        cost = 110,
        minForgeStage = 2,
        requiredAchievementId = "mountains_trail",
        narrativeMaterials = "Lamelas de ferro negro, rebites de bronze oxidado e forro de couro fervido.",
        lore = "Peitoral lamelar forjado para suportar o frio dos cumes com disciplina constante.",
        requirementLabel = "A Forja restaurada (etapa 2) · Conquista 'Passo de Ferro'",
        rune = HistoricalFutharkRune.Ehwaz
    ),
    ForgeRecipe(
        id = "nine-realms-frame",
        name = "Moldura dos Nove Reinos",
        kind = "frame",
        rarity = 4,
        cost = 140,
        minForgeStage = 2,
        minLevel = 10,
        narrativeMaterials = "Ferro forjado, bronze dourado antigo, incrustação de prata e selo de Yggdrasil.",
        lore = "A obra-prima de Darian: enquadra o retrato do herói sob o brasão da árvore dos reinos.",
        requirementLabel = "A Forja restaurada (etapa 2) · Nível 10+",
        rune = HistoricalFutharkRune.Dagaz
    )
)

val forgeItemIds: Set<String> = forgeRecipes.map { it.id }.toSet()

fun forgeRecipeById(id: String): ForgeRecipe? = forgeRecipes.find { it.id == id }

fun forgeStage(s: GameState): Int = (s.assets.find { it.id == "building:1" }?.stage ?: 0).coerceIn(0, 2)

enum class ForgeRecipeState(val label: String) {
    Crafted("Forjada"),
    Available("Pronta para forjar"),
    InsufficientFragments("Fragmentos insuficientes"),
    Locked("Bloqueada")
}

fun forgeRecipeMissingReasons(s: GameState, recipe: ForgeRecipe): List<String> = buildList {
    val stage = forgeStage(s)
    if (stage < recipe.minForgeStage) {
        add(if (recipe.minForgeStage == 1) "Restaure A Forja (etapa 1) na Vila do Despertar" else "Conclua a restauração de A Forja (etapa 2)")
    }
    if (s.level < recipe.minLevel) {
        add("Alcance o nível ${recipe.minLevel} (atual: ${s.level})")
    }
    recipe.requiredAchievementId?.let { achId ->
        val hasAch = s.achievements.any { it.id == achId }
        if (!hasAch) {
            val name = achievementDefs.find { it.id == achId }?.name ?: achId
            add("Conquista necessária: $name")
        }
    }
}

fun forgeRecipeState(s: GameState, recipe: ForgeRecipe): ForgeRecipeState {
    if (s.assets.any { it.id == recipe.id }) return ForgeRecipeState.Crafted
    if (forgeRecipeMissingReasons(s, recipe).isNotEmpty()) return ForgeRecipeState.Locked
    if (s.rawCoins < recipe.cost) return ForgeRecipeState.InsufficientFragments
    return ForgeRecipeState.Available
}

fun canCraftForgeRecipe(s: GameState, recipe: ForgeRecipe): Boolean =
    forgeRecipeState(s, recipe) == ForgeRecipeState.Available

fun isForgeMasterSpecialization(s: GameState): Boolean =
    activeSpecialization(s)?.id == "mestre_da_forja"

fun darianWorkshopDialogue(s: GameState): Pair<String, String> {
    val stage = forgeStage(s)
    val craftedCount = s.assets.count { it.id in forgeItemIds }
    val isMaster = isForgeMasterSpecialization(s)
    return when {
        stage == 0 -> "Darian · Ferreiro da Vila" to
            "A bigorna está fria e o fole ainda partido. Restaure a primeira etapa da Forja na Vila do Despertar antes de aquecermos o ferro."
        isMaster && stage == 2 -> "Darian · Entre Mestres da Forja" to
            if (craftedCount == forgeRecipes.size)
                "Você carrega o olhar de quem entende o peso do martelo. Todas as peças da forja já vestem sua jornada — o preço do ferro continua o mesmo para todos, mas o respeito é entre iguais."
            else
                "Reconheço o compasso de um Mestre da Forja. O custo em fragmentos não muda diante do fogo, mas minha bigorna e meus moldes mais antigos estão abertos ao seu ofício."
        isMaster -> "Darian · Entre Mestres da Forja" to
            "Um Mestre da Forja conhece o valor de um fole firme. Com a primeira etapa de pé já podemos bater ferro e bronze; quando terminarmos a oficina, as obras maiores nos aguardam."
        stage == 1 -> "Darian · Ferreiro da Vila" to
            "As brasas despertaram. Já posso forjar peças de viagem e estudo; conclua a restauração completa da Forja na Vila para trabalhar obras épicas e lendárias."
        craftedCount == forgeRecipes.size -> "Darian · Mestre Ferreiro" to
            "Oito obras saíram destas brasas com a sua medida. Nenhuma dá atalho, mas cada uma lembra o caminho que você sustentou."
        else -> "Darian · Mestre Ferreiro" to
            "O fogo está firme e a bigorna assentada. Escolha o molde, confira o ajuste no retrato e só bata o martelo quando a peça fizer sentido na sua saga."
    }
}
