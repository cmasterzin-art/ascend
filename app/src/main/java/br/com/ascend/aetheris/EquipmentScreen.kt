package br.com.ascend.aetheris

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*

/**
 * Rarity visual specification across all 5 tiers using distinct pictorial crests, Elder Futhark runes,
 * Nordic material accents, and border treatments (never relying on color alone).
 */
data class RarityVisualSpec(
    val level: Int,
    val label: String,
    val materialLabel: String,
    val rune: HistoricalFutharkRune,
    val crestAtlasCell: Int, // viking_arsenal_ui_atlas cells 4..7
    val borderWidth: Dp
)

fun rarityVisualSpec(tier: Int): RarityVisualSpec = when (tier.coerceIn(0, 4)) {
    0 -> RarityVisualSpec(0, "Comum", "Ferro Simples", HistoricalFutharkRune.Fehu, 4, 1.dp)
    1 -> RarityVisualSpec(1, "Incomum", "Ferro Temperado", HistoricalFutharkRune.Raido, 4, 1.1.dp)
    2 -> RarityVisualSpec(2, "Raro", "Bronze Viking", HistoricalFutharkRune.Ansuz, 5, 1.3.dp)
    3 -> RarityVisualSpec(3, "Épico", "Prata Rúnica", HistoricalFutharkRune.Kenaz, 6, 1.5.dp)
    else -> RarityVisualSpec(4, "Lendário", "Ouro dos Reinos", HistoricalFutharkRune.Dagaz, 7, 1.8.dp)
}

enum class ArsenalSort(val label: String) {
    NAME("Nome"),
    RARITY("Raridade"),
    ACQUISITION("Aquisição"),
    COST("Custo"),
    STAGE("Estágio")
}

data class ArsenalFilterState(
    val query: String = "",
    val category: Int = 0, // 0=Todos, 1=Cabeça, 2=Trajes, 3=Acessórios, 4=Armas, 5=Aliados, 6=Fundos, 7=Molduras
    val rarityTier: Int = -1, // -1=Todas, 0..4
    val collectionId: String = "", // ""=Todas
    val origin: String = "", // ""=Todas, "forge", "expedition", "skill", "shop"
    val ownership: Int = 0, // 0=Padrão, 1=Equipados, 2=Na coleção, 3=Fabricáveis
    val upgradeStage: Int = 0, // 0=Todos, 1=Forjado, 2=Gravado, 3=Consagrado
    val sort: ArsenalSort = ArsenalSort.NAME
) {
    val hasActiveFilters: Boolean
        get() = query.isNotBlank() || category != 0 || rarityTier >= 0 ||
            collectionId.isNotBlank() || origin.isNotBlank() ||
            ownership != 0 || upgradeStage != 0
}

fun itemOriginCode(item: ShopItem): String = when {
    item.id in forgeItemIds -> "forge"
    item.id in expeditionRelicIds -> "expedition"
    skillDefs.any { it.item == item.id } -> "skill"
    else -> "shop"
}

fun itemOriginLabel(item: ShopItem): String = when (itemOriginCode(item)) {
    "forge" -> "Forja de Darian"
    "expedition" -> "Expedição"
    "skill" -> "Habilidade"
    else -> "Mercador"
}

fun filterAndSortArsenalItems(s: GameState, store: Boolean, f: ArsenalFilterState): List<ShopItem> {
    val ownedById = s.assets.associateBy { it.id }
    val q = f.query.trim().lowercase()

    val filtered = shopItems.filter { item ->
        val owned = ownedById[item.id]
        val originCode = itemOriginCode(item)
        val col = collectionForItem(item.id)

        val visibleInContext = if (store) {
            originCode == "shop"
        } else {
            when (f.ownership) {
                1 -> owned?.equipped == true
                2 -> owned != null && !owned.equipped
                3 -> item.id in forgeItemIds && owned == null
                else -> owned != null || f.collectionId.isNotBlank()
            }
        }
        if (!visibleInContext) return@filter false
        if (!matchesArsenalFilter(item, f.category)) return@filter false
        if (f.rarityTier >= 0 && rarity(item) != f.rarityTier) return@filter false
        if (f.collectionId.isNotBlank() && col?.id != f.collectionId) return@filter false
        if (f.origin.isNotBlank() && originCode != f.origin) return@filter false
        if (f.upgradeStage > 0) {
            if (item.id !in forgeItemIds || owned == null || owned.stage != f.upgradeStage) return@filter false
        }
        if (q.isNotBlank()) {
            val hay = listOfNotNull(
                item.name,
                item.description,
                itemOriginLabel(item),
                col?.name
            ).joinToString(" ").lowercase()
            if (!hay.contains(q)) return@filter false
        }
        true
    }

    return when (f.sort) {
        ArsenalSort.NAME -> filtered.sortedBy { it.name.lowercase() }
        ArsenalSort.RARITY -> filtered.sortedWith(compareByDescending<ShopItem> { rarity(it) }.thenBy { it.name.lowercase() })
        ArsenalSort.ACQUISITION -> filtered.sortedWith(compareByDescending<ShopItem> { ownedById[it.id]?.date ?: "" }.thenBy { it.name.lowercase() })
        ArsenalSort.COST -> filtered.sortedWith(compareByDescending<ShopItem> { forgeRecipeById(it.id)?.cost ?: it.cost.toLong() }.thenBy { it.name.lowercase() })
        ArsenalSort.STAGE -> filtered.sortedWith(compareByDescending<ShopItem> { ownedById[it.id]?.stage ?: 0 }.thenByDescending { rarity(it) }.thenBy { it.name.lowercase() })
    }
}

internal fun encodeStudioDraft(selection: Map<String, String?>): String =
    equipmentSlots.joinToString(";") { (kind, _) -> "$kind=${selection[kind].orEmpty()}" }

internal fun decodeStudioDraft(raw: String): Map<String, String?> {
    val parsed = raw.split(';')
        .mapNotNull { entry ->
            val idx = entry.indexOf('=')
            if (idx <= 0) null else entry.substring(0, idx) to entry.substring(idx + 1).ifBlank { null }
        }.toMap()
    return equipmentSlots.associate { (kind, _) -> kind to parsed[kind] }
}

@Composable
fun RarityCrestBadge(tier: Int, color: Color, modifier: Modifier = Modifier) {
    val spec = rarityVisualSpec(tier)
    val minimal = LocalAppearance.current.minimal
    Row(
        modifier = modifier
            .clip(CutCornerShape(4.dp))
            .background(color.copy(alpha = 0.12f))
            .border(0.8.dp, color.copy(alpha = 0.55f), CutCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (!minimal) {
            AtlasCell(R.drawable.viking_arsenal_ui_atlas, spec.crestAtlasCell, 4, 2, Modifier.size(16.dp))
        }
        FutharkRuneGlyph(spec.rune, color, size = 11.dp)
        Text(
            text = spec.label.uppercase(),
            color = color,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.7.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Equipment(
    s: GameState,
    vm: GameViewModel,
    store: Boolean = false,
    onOpenForge: (() -> Unit)? = null,
    onOpenHeraldry: (() -> Unit)? = null
) {
    var arsenalTab by rememberSaveable { mutableIntStateOf(0) } // 0=Equipamentos, 1=Coleções, 2=Configurações, 3=Estúdio
    var selected by remember { mutableStateOf<String?>(null) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableIntStateOf(0) }
    var rarityFilter by rememberSaveable { mutableIntStateOf(-1) }
    var collectionFilter by rememberSaveable { mutableStateOf("") }
    var originFilter by rememberSaveable { mutableStateOf("") }
    var ownershipFilter by rememberSaveable { mutableIntStateOf(0) }
    var stageFilter by rememberSaveable { mutableIntStateOf(0) }
    var sortIndex by rememberSaveable { mutableIntStateOf(0) }
    var filtersExpanded by rememberSaveable { mutableStateOf(false) }

    var studioOpen by rememberSaveable { mutableStateOf(false) }
    var renamingLoadout by remember { mutableStateOf<EquipmentLoadout?>(null) }
    var previousAppearance by remember { mutableStateOf<Map<String, String?>?>(null) }
    val vmError by vm.error.collectAsState()

    val filterLabels = listOf("Todos", "Cabeça", "Trajes", "Acessórios", "Armas", "Aliados", "Fundos", "Molduras")
    val filterState = ArsenalFilterState(
        query = searchQuery,
        category = filter,
        rarityTier = rarityFilter,
        collectionId = collectionFilter,
        origin = originFilter,
        ownership = ownershipFilter,
        upgradeStage = stageFilter,
        sort = ArsenalSort.entries[sortIndex.coerceIn(ArsenalSort.entries.indices)]
    )

    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (!store) {
            AetherisTabs(
                values = listOf("Equipamentos", "Coleções", "Configurações", "Estúdio"),
                selected = arsenalTab,
                onSelect = { arsenalTab = it }
            )
        }

        if (!store && arsenalTab == 1) {
            LegendaryCollectionsSection(
                s = s,
                vm = vm,
                onOpenForgeRecipe = onOpenForge?.let { open -> { _ -> open() } }
            )
        } else if (!store && arsenalTab == 2) {
            LoadoutDeckSection(
                s = s,
                vm = vm,
                onRename = { renamingLoadout = it }
            )
        } else if (!store && arsenalTab == 3 && s.player != null) {
            HeroCustomizationStudio(
                s = s,
                previousAppearance = previousAppearance,
                onDismiss = { arsenalTab = 0 },
                onSave = { confirmedSelection ->
                    val currentBeforeSave = equipmentSlots.associate { (kind, _) ->
                        kind to s.assets.find { it.kind == kind && it.equipped }?.id
                    }
                    vm.run {
                        vm.repo.applyStudioSelection(confirmedSelection)
                        previousAppearance = currentBeforeSave
                    }
                },
                inline = true,
                scrollableInline = false,
                saveErrorMessage = vmError
            )
        } else {
            if (!store) {
                val minimal = LocalAppearance.current.minimal
                val spec = activeSpecialization(s)
                val dark = LocalAppearance.current.dark
                val heraldry = remember(s.heraldry, s.player?.created) { resolvedHeraldry(s) }
                // Arsenal dos Nove Reinos — câmara central com Retrato Vivo, brasão e 7 nichos nórdicos
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(CutCornerShape(8.dp))
                        .border(1.dp, OldBronze.copy(alpha = 0.52f), CutCornerShape(8.dp))
                        .background(Surface)
                ) {
                    if (!minimal) {
                        AtlasCellCropped(
                            resId = R.drawable.hero_background_atlas,
                            index = 0,
                            columns = 2,
                            rows = 2,
                            modifier = Modifier.matchParentSize(),
                            alpha = if (dark) 0.24f else 0.14f
                        )
                    }
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    if (dark) listOf(
                                        Color(0xFF0E1217).copy(alpha = 0.72f),
                                        Color(0xFF0E1217).copy(alpha = 0.88f)
                                    ) else listOf(
                                        Surface.copy(alpha = 0.86f),
                                        Surface.copy(alpha = 0.94f)
                                    )
                                )
                            )
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    FutharkRuneGlyph(HistoricalFutharkRune.Isa, Gold, size = 13.dp)
                                    Eyebrow("Arsenal dos Nove Reinos", Gold)
                                }
                                Text(
                                    "Câmara dos equipamentos",
                                    color = if (dark) WornIvory else Ink,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    spec?.let { "${classes[s.player!!.heroClass]} · ${it.name}" }
                                        ?: "${classes[s.player!!.heroClass]} · Retrato Vivo",
                                    color = if (dark) WornIvory.copy(alpha = 0.82f) else Muted,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            HeraldicCrest(
                                profile = heraldry,
                                size = 38.dp,
                                modifier = if (onOpenHeraldry != null) Modifier.clickable(onClick = onOpenHeraldry) else Modifier
                            )
                        }

                        BoxWithConstraints(Modifier.fillMaxWidth()) {
                            val narrow = maxWidth < 330.dp
                            val nicheW = if (narrow) 60.dp else 68.dp
                            val portraitSize = (maxWidth - nicheW * 2 - 20.dp).coerceIn(112.dp, 182.dp)
                            val cold = Color(0xFFB8C7D9)
                            Canvas(Modifier.matchParentSize()) {
                                drawRect(
                                    Brush.radialGradient(
                                        listOf(cold.copy(alpha = 0.14f), Color.Transparent),
                                        center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height * 0.34f),
                                        radius = size.width * 0.52f
                                    )
                                )
                            }
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                    equipmentSlots.take(2).forEach { (kind, label) ->
                                        EquipmentSlot(kind, label, s, nicheW) { itemId ->
                                            if (itemId != null) selected = itemId
                                            else filter = slotFilterIndex(kind)
                                        }
                                    }
                                }
                                ForgePortrait(s, portraitSize)
                                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                    equipmentSlots.drop(2).take(2).forEach { (kind, label) ->
                                        EquipmentSlot(kind, label, s, nicheW) { itemId ->
                                            if (itemId != null) selected = itemId
                                            else filter = slotFilterIndex(kind)
                                        }
                                    }
                                }
                            }
                        }

                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            equipmentSlots.drop(4).forEach { (kind, label) ->
                                EquipmentSlot(kind, label, s, 68.dp) { itemId ->
                                    if (itemId != null) selected = itemId
                                    else filter = slotFilterIndex(kind)
                                }
                            }
                        }

                        VikingKnotDivider(variant = 7, alpha = 0.65f, height = 14.dp)

                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SagaButton(
                                text = "Estúdio",
                                onClick = { studioOpen = true },
                                modifier = Modifier.weight(1f).testTag("open_studio_button"),
                                style = VikingButtonStyle.BronzeForjado,
                                leadingRune = HistoricalFutharkRune.Mannaz
                            )
                            if (onOpenForge != null) {
                                SagaButton(
                                    text = "Forja de Darian",
                                    onClick = onOpenForge,
                                    modifier = Modifier.weight(1f).testTag("open_forge_from_arsenal"),
                                    style = VikingButtonStyle.FerroRunico,
                                    leadingRune = HistoricalFutharkRune.Fehu
                                )
                            }
                        }
                        if (onOpenHeraldry != null) {
                            SagaButton(
                                text = "Heráldica Rúnica",
                                onClick = onOpenHeraldry,
                                modifier = Modifier.fillMaxWidth().testTag("open_heraldry_from_arsenal"),
                                style = VikingButtonStyle.CompactoRunico,
                                leadingRune = HistoricalFutharkRune.Othala
                            )
                        }

                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${s.assets.count { it.equipped }} equipados · ${s.assets.count { a -> shopItems.any { it.id == a.id } }} na coleção",
                                color = Muted,
                                fontSize = 11.sp
                            )
                            Text(
                                "◈ ${s.coins}",
                                color = Gold,
                                fontFamily = SagaFont,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            } else {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .materialSurface(accent = OldBronze, leather = true)
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FutharkRuneGlyph(HistoricalFutharkRune.Fehu, Gold, size = 13.dp)
                        Eyebrow("MERCADOR DE RELÍQUIAS · AETHERIS")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        RelicArt("weapon", Gold, Modifier.size(72.dp), "silver-blade")
                        Column(Modifier.weight(1f)) {
                            Text("Entre couro e ferro", style = MaterialTheme.typography.titleLarge)
                            Text(
                                "Peças do mercador. Obras da Forja de Darian e relíquias de expedição nunca entram à venda.",
                                color = Muted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Eyebrow(if (store) "Espólios do mercador" else "Coleção do Arsenal")
                Text("◈ ${s.coins}", color = Gold, fontFamily = SagaFont, fontSize = 19.sp)
            }

            // Arsenal 2.1: Busca + filtros combináveis + ordenação
            FormField(
                value = searchQuery,
                onChange = { searchQuery = it },
                label = "Buscar por nome, origem ou coleção",
                modifier = Modifier.testTag("arsenal_search")
            )

            ChipRow(filterLabels, filter, { filter = it })

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { filtersExpanded = !filtersExpanded },
                    modifier = Modifier.testTag("arsenal_filters_toggle")
                ) {
                    Text(
                        if (filtersExpanded) "Ocultar filtros avançados"
                        else if (filterState.hasActiveFilters) "Filtros avançados (ativos)"
                        else "Filtros avançados e ordenação",
                        fontSize = 12.sp
                    )
                }
                if (filterState.hasActiveFilters) {
                    TextButton(
                        onClick = {
                            searchQuery = ""
                            filter = 0
                            rarityFilter = -1
                            collectionFilter = ""
                            originFilter = ""
                            ownershipFilter = 0
                            stageFilter = 0
                        },
                        modifier = Modifier.testTag("arsenal_clear_filters")
                    ) {
                        Text("Limpar filtros", fontSize = 12.sp, color = Gold)
                    }
                }
            }

            if (filtersExpanded) {
                Panel(accent = Blue) {
                    Eyebrow("Ordenar por", Blue)
                    ChipRow(ArsenalSort.entries.map { it.label }, sortIndex, { sortIndex = it })

                    Eyebrow("Raridade", Gold)
                    ChipRow(
                        listOf("Todas", "Comum", "Incomum", "Raro", "Épico", "Lendário"),
                        rarityFilter + 1,
                        { rarityFilter = it - 1 }
                    )

                    if (!store) {
                        Eyebrow("Estado e Posse", Blue)
                        ChipRow(
                            listOf("Possuídos", "Equipados", "Não equipados", "Fabricáveis"),
                            ownershipFilter,
                            { ownershipFilter = it }
                        )

                        Eyebrow("Origem", Muted)
                        val originOpts = listOf("" to "Todas", "forge" to "Forja", "expedition" to "Expedição", "skill" to "Habilidade", "shop" to "Mercador")
                        ChipRow(
                            originOpts.map { it.second },
                            originOpts.indexOfFirst { it.first == originFilter }.coerceAtLeast(0),
                            { originFilter = originOpts[it].first }
                        )

                        Eyebrow("Coleção Lendária", Gold)
                        val colOpts = listOf("" to "Todas") + legendaryCollections.map { it.id to it.name }
                        ChipRow(
                            colOpts.map { it.second },
                            colOpts.indexOfFirst { it.first == collectionFilter }.coerceAtLeast(0),
                            { collectionFilter = colOpts[it].first }
                        )

                        Eyebrow("Estágio da Forja", OldBronze)
                        ChipRow(
                            listOf("Todos", "Forjado", "Gravado", "Consagrado"),
                            stageFilter,
                            { stageFilter = it }
                        )
                    }
                }
            }

            val items = remember(s.assets, store, filterState) {
                filterAndSortArsenalItems(s, store, filterState)
            }

            if (items.isEmpty()) {
                Empty(
                    if (filterState.hasActiveFilters) "Nenhuma peça corresponde aos filtros atuais. Limpe ou ajuste os filtros."
                    else if (store) "Nenhum item do mercador nesta categoria."
                    else "Nenhuma peça possuída nesta categoria. Explore expedições, desperte habilidades ou visite a Forja de Darian."
                )
            }

            items.chunked(2).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { item ->
                        val tier = rarity(item)
                        val spec = rarityVisualSpec(tier)
                        val color = rarityColor(tier)
                        val owned = s.assets.find { it.id == item.id }
                        val originTag = itemOriginLabel(item)
                        val isForge = item.id in forgeItemIds
                        val upgradeStage = if (isForge && owned != null) owned.stage else 0
                        val stageBorderColor = when (upgradeStage) {
                            FORGE_STAGE_CONSECRATED -> Gold
                            FORGE_STAGE_ENGRAVED -> Color(0xFFB5C4D8)
                            else -> color
                        }
                        AetherSurface(
                            SurfaceKind.Artifact,
                            Modifier
                                .weight(1f)
                                .border(
                                    width = if (owned?.equipped == true || upgradeStage >= FORGE_STAGE_CONSECRATED) spec.borderWidth + 0.5.dp else spec.borderWidth,
                                    color = stageBorderColor.copy(alpha = if (owned?.equipped == true || upgradeStage >= FORGE_STAGE_ENGRAVED) 0.88f else 0.36f),
                                    shape = CutCornerShape(6.dp)
                                )
                                .clickable { selected = item.id }
                        ) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                RarityCrestBadge(tier, color)
                                if (upgradeStage > 0) {
                                    UpgradeStageBadge(upgradeStage)
                                }
                            }
                            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                RelicArt(item.kind, color, Modifier.size(92.dp), item.id, dim = owned == null && !store)
                            }
                            SagaDivider(color = stageBorderColor)
                            Text(
                                item.name,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "$originTag · ${spec.materialLabel}",
                                color = Muted,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                when {
                                    owned?.equipped == true -> "◆ Equipado"
                                    owned != null -> "◆ Na coleção"
                                    isForge -> "⚒ Forja · ◈ ${forgeRecipeById(item.id)?.cost ?: item.cost}"
                                    else -> "◈ ${item.cost} fragmentos"
                                },
                                color = if (owned != null) stageBorderColor else color,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }

            if (!store) {
                s.assets.filter { it.kind == "trophy" }.forEach { a ->
                    val colTrophy = if (a.id.startsWith("collection:")) {
                        legendaryCollectionById(a.id.removePrefix("collection:"))
                    } else null
                    Panel {
                        RuneSeal(4, Gold, Modifier.size(36.dp))
                        Eyebrow(if (colTrophy != null) "Troféu de Coleção Lendária" else "Troféu de missão")
                        Text(
                            colTrophy?.let { "${it.name} · Título: ${it.rewardTitle}" }
                                ?: s.quests.find { q -> s.claims.any { it.questId == q.id && "quest:${it.id}" == a.id } }?.special
                                ?: "Memória da jornada"
                        )
                    }
                }
            }
        }
    }

    // Dialog de inspeção detalhada do item (6.1 comandos idempotentes equipItem / unequipItem)
    selected?.let { id ->
        val item = shopItems.first { it.id == id }
        val owned = s.assets.find { it.id == id }
        val tier = rarity(item)
        val spec = rarityVisualSpec(tier)
        val color = rarityColor(tier)
        val isForge = item.id in forgeItemIds
        val col = collectionForItem(item.id)
        val canBuyInShop = store && owned == null && item.id !in forgeItemIds && item.id !in expeditionRelicIds && skillDefs.none { it.item == item.id }
        AetherDialog(
            onDismissRequest = { selected = null },
            title = item.name,
            kind = SurfaceKind.Artifact,
            confirmButton = {
                if (owned != null) {
                    Action(
                        if (owned.equipped) "Desequipar" else "Equipar",
                        {
                            vm.run {
                                if (owned.equipped) vm.repo.unequipItem(owned.id)
                                else vm.repo.equipItem(owned.id)
                                selected = null
                            }
                        },
                        ceremonial = !owned.equipped
                    )
                } else if (canBuyInShop) {
                    Action(
                        "Adquirir · ${item.cost}",
                        {
                            vm.run { vm.repo.buy(item); selected = null }
                        },
                        enabled = s.coins >= item.cost,
                        ceremonial = true
                    )
                } else if (isForge && onOpenForge != null) {
                    Action("Abrir Forja de Darian", { selected = null; onOpenForge() }, ceremonial = true)
                }
            },
            dismissButton = { TextButton({ selected = null }) { Text("Fechar") } }
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RelicArt(item.kind, color, Modifier.size(104.dp), item.id)
                if (item.kind != "decoration" && s.player != null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        LivingHeroPortrait(
                            p = s.player,
                            s = s,
                            size = 86.dp,
                            previewOverrides = mapOf(item.kind to item.id),
                            showBadges = true
                        )
                        Text("Prévia no herói", color = Muted, fontSize = 10.sp)
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                RarityCrestBadge(tier, color)
                if (isForge && owned != null) UpgradeStageBadge(owned.stage)
            }
            Text(
                "${spec.materialLabel} · ${portraitRepresentationFor(item).label}",
                color = color,
                fontSize = 12.sp
            )
            Text(item.description, fontSize = 13.sp)
            if (isForge && owned != null) {
                Text(upgradeVisualScopeFor(item.id), color = Gold, fontSize = 11.sp)
            }
            if (col != null) {
                Text("Coleção Lendária: ${col.name} (${collectionOwnedItems(s, col).size}/4 peças)", color = Blue, fontSize = 12.sp)
            }
            SagaDivider(color = color)
            Eyebrow("Procedência", color)
            Text(itemOrigin(item), color = Muted, fontSize = 12.sp)
            if (owned != null) {
                Text("Guardado na coleção em ${displayDate(owned.date)}", color = Muted, fontSize = 11.sp)
            }
        }
    }

    // Estúdio de Personalização em tela cheia (6.4: erro de salvamento mantém Estúdio aberto)
    if (studioOpen && s.player != null) {
        HeroCustomizationStudio(
            s = s,
            previousAppearance = previousAppearance,
            onDismiss = { studioOpen = false },
            onSave = { confirmedSelection ->
                val currentBeforeSave = equipmentSlots.associate { (kind, _) ->
                    kind to s.assets.find { it.kind == kind && it.equipped }?.id
                }
                vm.run {
                    vm.repo.applyStudioSelection(confirmedSelection)
                    previousAppearance = currentBeforeSave
                    studioOpen = false
                }
            },
            saveErrorMessage = vmError
        )
    }

    // Dialog para renomear configuração de equipamento
    renamingLoadout?.let { loadout ->
        var text by remember(loadout.id) { mutableStateOf(loadout.name) }
        AetherDialog(
            onDismissRequest = { renamingLoadout = null },
            title = "Renomear configuração",
            confirmButton = {
                SagaButton(
                    text = "Salvar nome",
                    onClick = {
                        vm.run {
                            vm.repo.renameLoadout(loadout.id, text)
                            renamingLoadout = null
                        }
                    },
                    enabled = text.trim().isNotEmpty(),
                    style = VikingButtonStyle.BronzeForjado
                )
            },
            dismissButton = { TextButton({ renamingLoadout = null }) { Text("Cancelar") } }
        ) {
            FormField(text, { text = it.take(40) }, "Nome da configuração (até 40 caracteres)")
        }
    }
}

@Composable
internal fun LoadoutDeckSection(
    s: GameState,
    vm: GameViewModel,
    onRename: (EquipmentLoadout) -> Unit
) {
    val loadouts = remember(s.loadouts, s.player?.created) { resolvedLoadouts(s) }
    var selectedLoadoutId by rememberSaveable { mutableStateOf(loadouts.first().id) }
    var confirmPartial by remember { mutableStateOf<LoadoutApplicationPreview?>(null) }
    val activeLoadout = loadouts.firstOrNull { it.id == selectedLoadoutId } ?: loadouts.first()
    val slots = activeLoadout.slotMap()
    val missing = remember(activeLoadout, s.assets) { loadoutMissingItems(activeLoadout, s) }
    val empty = slots.isEmpty()

    AetherSurface(SurfaceKind.Operational, Modifier.testTag("loadout_deck")) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FutharkRuneGlyph(HistoricalFutharkRune.Dagaz, Gold, size = 13.dp)
                Eyebrow("Configurações de Equipamento (3)", Gold)
            }
            Text("${slots.size}/7 peças", color = Muted, fontSize = 11.sp)
        }
        Text(
            "Três combinações salváveis (Jornada, Conselho e Expedição), independentes das Coleções Lendárias.",
            color = Muted,
            fontSize = 11.sp
        )

        ChipRow(
            values = loadouts.map { it.name },
            selected = loadouts.indexOfFirst { it.id == activeLoadout.id }.coerceAtLeast(0),
            onSelect = { idx -> selectedLoadoutId = loadouts[idx].id }
        )

        if (empty) {
            Text(
                "Espaço vazio. Equipe seu visual no Arsenal ou Estúdio e toque em \"Salvar atual\" para guardar nesta configuração.",
                color = Muted,
                fontSize = 12.sp
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                slots.forEach { (kind, itemId) ->
                    val item = shopItems.find { it.id == itemId }
                    val isMissing = itemId in missing
                    val slotLabel = equipmentSlots.find { it.first == kind }?.second ?: kind
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "$slotLabel · ${item?.name ?: itemId}",
                            color = if (isMissing) MaterialTheme.colorScheme.error else Ink,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (isMissing) {
                            Text("Ausente", color = MaterialTheme.colorScheme.error, fontSize = 10.sp)
                        }
                    }
                }
            }
            if (missing.isNotEmpty()) {
                Text(
                    "Aviso: ${missing.size} ${if (missing.size == 1) "item não está" else "itens não estão"} na coleção atual. Ao aplicar, você verá uma prévia e os demais espaços válidos serão preservados.",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 11.sp
                )
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SagaButton(
                text = "Aplicar",
                onClick = {
                    if (missing.isNotEmpty()) {
                        confirmPartial = previewLoadoutApplication(activeLoadout, s)
                    } else {
                        vm.run { vm.repo.applyLoadout(activeLoadout.id) }
                    }
                },
                modifier = Modifier.weight(1f).testTag("apply_loadout_btn"),
                enabled = !empty,
                style = VikingButtonStyle.BronzeForjado
            )
            SagaButton(
                text = "Salvar atual",
                onClick = { vm.run { vm.repo.saveCurrentAsLoadout(activeLoadout.id) } },
                modifier = Modifier.weight(1f).testTag("save_loadout_btn"),
                style = VikingButtonStyle.FerroRunico
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton({ onRename(activeLoadout) }, Modifier.weight(1f)) {
                Text("Renomear", fontSize = 12.sp)
            }
            TextButton(
                onClick = { vm.run { vm.repo.clearLoadout(activeLoadout.id) } },
                enabled = !empty,
                modifier = Modifier.weight(1f)
            ) {
                Text("Limpar", fontSize = 12.sp, color = Muted)
            }
        }
    }

    // 6.3: Prévia explícita de aplicação parcial antes de confirmar
    confirmPartial?.let { prev ->
        AetherDialog(
            onDismissRequest = { confirmPartial = null },
            title = "Aplicar configuração parcial?",
            kind = SurfaceKind.Operational,
            confirmButton = {
                SagaButton(
                    text = "Aplicar disponíveis",
                    onClick = {
                        vm.run {
                            vm.repo.applyLoadout(prev.loadout.id)
                            confirmPartial = null
                        }
                    },
                    enabled = prev.availableItemIds.isNotEmpty(),
                    style = VikingButtonStyle.BronzeForjado
                )
            },
            dismissButton = { TextButton({ confirmPartial = null }) { Text("Cancelar") } }
        ) {
            Text(
                "A configuração \"${prev.loadout.name}\" possui itens ausentes na coleção. Os espaços já equipados fora dos itens disponíveis serão preservados.",
                color = Muted,
                fontSize = 12.sp
            )
            if (prev.availableItemIds.isNotEmpty()) {
                Eyebrow("Disponíveis (${prev.availableItemIds.size})", Green)
                prev.availableItemIds.forEach { itemId ->
                    val item = shopItems.find { it.id == itemId }
                    val slotLbl = equipmentSlots.find { it.first == item?.kind }?.second ?: ""
                    Text("• $slotLbl: ${item?.name ?: itemId}", color = Ink, fontSize = 12.sp)
                }
            }
            if (prev.missingItemIds.isNotEmpty()) {
                Eyebrow("Ausentes (${prev.missingItemIds.size})", MaterialTheme.colorScheme.error)
                prev.missingItemIds.forEach { itemId ->
                    val item = shopItems.find { it.id == itemId }
                    val slotLbl = equipmentSlots.find { it.first == item?.kind }?.second ?: ""
                    Text("• $slotLbl: ${item?.name ?: itemId} (ignorado)", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
            if (prev.slotChanges.isNotEmpty()) {
                Eyebrow("Espaços alterados (${prev.slotChanges.size})", Blue)
                prev.slotChanges.forEach { change ->
                    val fromName = change.previousItemId?.let { id -> shopItems.find { it.id == id }?.name ?: id } ?: "Vazio"
                    val toName = change.nextItemId?.let { id -> shopItems.find { it.id == id }?.name ?: id } ?: "Vazio"
                    Text("• ${change.slotLabel}: $fromName → $toName", color = Muted, fontSize = 11.sp)
                }
            }
        }
    }
}

/**
 * Estúdio de Personalização — prévia ao vivo em tamanho destacado no Retrato Vivo,
 * rascunho persistido em rememberSaveable (6.4), confirmação ao descartar alterações,
 * preservação do rascunho se o salvamento falhar, e experimentação de itens bloqueados sem conceder posse.
 */
@Composable
fun HeroCustomizationStudio(
    s: GameState,
    previousAppearance: Map<String, String?>?,
    onDismiss: () -> Unit,
    onSave: (Map<String, String?>) -> Unit,
    inline: Boolean = false,
    scrollableInline: Boolean = true,
    saveErrorMessage: String? = null
) {
    val hero = requireNotNull(s.player)
    val spec = activeSpecialization(s)
    val dark = LocalAppearance.current.dark
    val initialEquipped = remember(s.assets) {
        equipmentSlots.associate { (kind, _) ->
            kind to s.assets.find { it.kind == kind && it.equipped }?.id
        }
    }
    val initialEncoded = remember(initialEquipped) { encodeStudioDraft(initialEquipped) }
    var draftEncoded by rememberSaveable { mutableStateOf(initialEncoded) }
    var activeSlotIndex by rememberSaveable { mutableIntStateOf(0) }
    var confirmDiscard by remember { mutableStateOf(false) }

    val draft = remember(draftEncoded) { decodeStudioDraft(draftEncoded) }
    val updateDraft: (Map<String, String?>) -> Unit = { next ->
        draftEncoded = encodeStudioDraft(next)
    }
    val ownedIds = remember(s.assets) { s.assets.map { it.id }.toSet() }

    val lockedPreviewItems = remember(draft, ownedIds) {
        draft.values.filterNotNull().filter { it.isNotBlank() && it !in ownedIds }
    }
    val hasChanges = draft != initialEquipped
    val canSave = lockedPreviewItems.isEmpty()

    val slotKinds = equipmentSlots
    val currentSlot = slotKinds[activeSlotIndex.coerceIn(slotKinds.indices)]
    val candidates = remember(currentSlot.first) {
        shopItems.filter { it.kind == currentSlot.first }
    }

    val saveLabel = if (canSave) "Salvar" else "Bloqueado"
    val requestDismiss: () -> Unit = {
        if (hasChanges) confirmDiscard = true else onDismiss()
    }

    val body: @Composable ColumnScope.() -> Unit = {
        Box(
            Modifier
                .fillMaxWidth()
                .clip(CutCornerShape(8.dp))
                .border(1.dp, OldBronze.copy(alpha = 0.5f), CutCornerShape(8.dp))
                .background(if (dark) Color(0xFF0E1217) else Surface)
                .padding(14.dp)
        ) {
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Eyebrow(
                            if (lockedPreviewItems.isNotEmpty()) "Prévia com itens bloqueados" else "Prévia ao vivo",
                            if (lockedPreviewItems.isNotEmpty()) Gold else Blue
                        )
                        Text(
                            "${hero.name} · ${classes[hero.heroClass]}${if (spec != null) " · ${spec.name}" else ""}",
                            color = if (dark) WornIvory else Ink,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (spec != null) {
                        ArchetypeSealArt(spec.atlasIndex, Modifier.size(32.dp))
                    }
                }

                BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    LivingHeroPortrait(
                        p = hero,
                        s = s,
                        size = (maxWidth * 0.56f).coerceIn(136.dp, 200.dp),
                        previewOverrides = draft
                    )
                }

                if (!saveErrorMessage.isNullOrBlank()) {
                    Text(
                        "Não foi possível salvar: $saveErrorMessage",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.sp
                    )
                }

                if (lockedPreviewItems.isNotEmpty()) {
                    val names = lockedPreviewItems.mapNotNull { id -> shopItems.find { it.id == id }?.name }.joinToString(", ")
                    Text(
                        "Prévia de item bloqueado ($names): visualizar não concede posse. Remova os bloqueados para salvar.",
                        color = Gold,
                        fontSize = 11.sp
                    )
                    SagaButton(
                        text = "Remover itens bloqueados",
                        onClick = {
                            updateDraft(draft.mapValues { (k, v) -> if (v != null && v !in ownedIds) initialEquipped[k] else v })
                        },
                        style = VikingButtonStyle.CompactoRunico
                    )
                }

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SagaButton(
                        text = "Desfazer",
                        onClick = { updateDraft(initialEquipped) },
                        modifier = Modifier.weight(1f).testTag("studio_undo_btn"),
                        enabled = hasChanges,
                        style = VikingButtonStyle.FerroRunico
                    )
                    if (previousAppearance != null) {
                        SagaButton(
                            text = "Restaurar anterior",
                            onClick = { updateDraft(previousAppearance) },
                            modifier = Modifier.weight(1f).testTag("studio_restore_btn"),
                            style = VikingButtonStyle.FerroRunico
                        )
                    }
                }
            }
        }

        ChipRow(
            values = slotKinds.map {
                when (it.first) {
                    "outfit" -> "Traje"
                    "weapon" -> "Arma"
                    "companion" -> "Aliado"
                    else -> it.second
                }
            },
            selected = activeSlotIndex,
            onSelect = { activeSlotIndex = it }
        )

        val currentChosen = draft[currentSlot.first]
        Row(
            Modifier
                .fillMaxWidth()
                .clip(CutCornerShape(5.dp))
                .background(if (currentChosen == null) Gold.copy(alpha = 0.14f) else Surface)
                .border(1.dp, if (currentChosen == null) Gold.copy(alpha = 0.72f) else ColdIron.copy(alpha = 0.35f), CutCornerShape(5.dp))
                .clickable { updateDraft(draft + (currentSlot.first to null)) }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Nenhum (${currentSlot.second})", color = Ink, fontSize = 13.sp)
            Text(if (currentChosen == null) "Selecionado" else "Remover", color = if (currentChosen == null) Gold else Muted, fontSize = 11.sp)
        }

        candidates.forEach { item ->
            val owned = item.id in ownedIds
            val asset = s.assets.find { it.id == item.id }
            val chosen = currentChosen == item.id
            val tier = rarity(item)
            val color = rarityColor(tier)
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(CutCornerShape(6.dp))
                    .background(if (chosen) color.copy(alpha = 0.14f) else Surface)
                    .border(
                        if (chosen) 1.5.dp else 0.9.dp,
                        if (chosen) color.copy(alpha = 0.82f) else ColdIron.copy(alpha = 0.35f),
                        CutCornerShape(6.dp)
                    )
                    .clickable { updateDraft(draft + (currentSlot.first to item.id)) }
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RelicArt(item.kind, color, Modifier.size(54.dp), item.id, dim = !owned)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(item.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (asset != null && item.id in forgeItemIds) UpgradeStageBadge(asset.stage)
                    }
                    Text(
                        if (owned) "${rarityNames[tier]} · Possuído · ${portraitRepresentationFor(item).label}"
                        else "Bloqueado · Prévia · ${itemOrigin(item)}",
                        color = if (owned) color else Gold,
                        fontSize = 11.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    when {
                        chosen && owned -> "◆ Ativo"
                        chosen && !owned -> "Prévia"
                        owned -> "Usar"
                        else -> "Provar"
                    },
                    color = if (chosen) color else Muted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    if (inline) {
        Column(
            (if (scrollableInline) Modifier.fillMaxSize() else Modifier.fillMaxWidth()).background(Night),
            verticalArrangement = Arrangement.spacedBy(if (scrollableInline) 0.dp else 12.dp)
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(CutCornerShape(6.dp))
                    .background(Surface)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Estúdio de Personalização",
                    style = MaterialTheme.typography.titleLarge,
                    color = Ink,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                SagaButton(
                    text = saveLabel,
                    onClick = { if (canSave) onSave(draft) },
                    enabled = canSave,
                    style = if (canSave) VikingButtonStyle.BronzeForjado else VikingButtonStyle.FerroRunico
                )
            }
            Column(
                if (scrollableInline) Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
                else Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = body
            )
        }
    } else {
        FullForm(
            title = "Estúdio de Personalização",
            onDismiss = requestDismiss,
            onSave = { if (canSave) onSave(draft) },
            saveLabel = saveLabel,
            content = body
        )
    }

    if (confirmDiscard) {
        AetherDialog(
            onDismissRequest = { confirmDiscard = false },
            title = "Descartar alterações no Estúdio?",
            confirmButton = {
                SagaButton(
                    text = "Descartar",
                    onClick = {
                        confirmDiscard = false
                        updateDraft(initialEquipped)
                        onDismiss()
                    },
                    style = VikingButtonStyle.Destrutivo
                )
            },
            dismissButton = {
                TextButton({ confirmDiscard = false }) { Text("Continuar editando") }
            }
        ) {
            Text("Existem escolhas não salvas na aparência do herói.", color = Muted, fontSize = 13.sp)
        }
    }
}

private fun slotFilterIndex(kind: String): Int = when (kind) {
    "head" -> 1
    "outfit" -> 2
    "accessory" -> 3
    "weapon" -> 4
    "companion" -> 5
    "background" -> 6
    "frame", "decoration" -> 7
    else -> 0
}

private fun matchesArsenalFilter(item: ShopItem, filter: Int): Boolean = when (filter) {
    1 -> item.kind == "head"
    2 -> item.kind == "outfit"
    3 -> item.kind == "accessory"
    4 -> item.kind == "weapon"
    5 -> item.kind == "companion"
    6 -> item.kind == "background"
    7 -> item.kind in listOf("frame", "decoration")
    else -> true
}

@Composable
private fun EquipmentSlot(
    kind: String,
    label: String,
    s: GameState,
    width: Dp = 68.dp,
    onTap: (String?) -> Unit
) {
    val a = s.assets.find { it.kind == kind && it.equipped }
    val item = shopItems.find { it.id == a?.id }
    val dark = LocalAppearance.current.dark
    val color = if (item != null) rarityColor(rarity(item)) else if (dark) Color(0xFF9EB0C2) else Color(0xFF5D5446)
    val minimal = LocalAppearance.current.minimal
    val slotShape = CutCornerShape(topStart = 6.dp, topEnd = 2.dp, bottomEnd = 6.dp, bottomStart = 2.dp)

    Column(
        Modifier
            .width(width)
            .heightIn(min = 76.dp)
            .clip(slotShape)
            .background(
                Brush.verticalGradient(
                    if (dark) listOf(Color(0xFF23282E), Color(0xFF131618))
                    else listOf(Surface, Color(0xFFE7DEC9))
                )
            )
            .border(
                if (item != null) 1.4.dp else 0.9.dp,
                color.copy(alpha = if (item != null) 0.85f else 0.45f),
                slotShape
            )
            .clickable(onClickLabel = if (item != null) "Detalhes de ${item.name}" else "Filtrar $label") {
                onTap(item?.id)
            }
            .padding(4.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = "$label: ${item?.name ?: "vazio"}"
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
            if (!minimal) {
                AtlasCell(
                    R.drawable.viking_arsenal_ui_atlas,
                    index = if (item != null) 1 else 0,
                    columns = 4,
                    rows = 2,
                    modifier = Modifier.matchParentSize(),
                    alpha = if (item != null) 0.92f else 0.72f
                )
            }
            RelicArt(kind, color, Modifier.size(34.dp), item?.id, dim = item == null)
        }
        Text(
            when (kind) {
                "companion" -> "Aliado"
                "weapon" -> "Arma"
                "outfit" -> "Traje"
                else -> label
            },
            color = if (item != null) color else Ink,
            fontSize = 9.sp,
            lineHeight = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            when {
                a == null -> "Vazio"
                a.id in forgeItemIds && a.stage >= FORGE_STAGE_CONSECRATED -> "Consag."
                a.id in forgeItemIds && a.stage == FORGE_STAGE_ENGRAVED -> "Gravado"
                else -> "Ativo"
            },
            color = if (item != null) color else Muted,
            fontSize = 8.sp,
            maxLines = 1
        )
    }
}

@Composable
fun SkillTree(s: GameState, vm: GameViewModel) {
    val heroClass = s.player?.heroClass ?: 1
    var scopeTab by remember { mutableIntStateOf(0) } // 0=Minha classe + Universais, 1=Todas as classes
    Title("Constelações da alma", "Habilidades universais e ascensão dos arquétipos.")
    Panel {
        Stat("${skillPoints(s)}", "PONTOS DISPONÍVEIS")
        Text("Um ponto a cada três níveis. Cada habilidade custa um ponto, sem nova moeda e sem multiplicadores artificiais de XP ou fragmentos.", color = Muted, fontSize = 13.sp)
        Text("Se o XP for corrigido ou a classe mudar, habilidades já obtidas permanecem.", color = Muted, fontSize = 11.sp)
    }
    ChipRow(listOf("Minha classe (${classes[heroClass]})", "Todas as classes"), scopeTab, { scopeTab = it })

    val classSkills = skillDefs.filter { it.heroClass >= 0 && (scopeTab == 1 || it.heroClass == heroClass) }
    Eyebrow("Ascensão dos Arquétipos · ${if (scopeTab == 0) classes[heroClass] else "5 classes"}", Gold)
    classSkills.groupBy { it.path }.forEach { (path, nodes) ->
        if (scopeTab == 1) Eyebrow(path, Blue)
        nodes.forEachIndexed { i, skill ->
            if (i > 0) SagaDivider(color = Gold)
            val owned = s.assets.any { it.id == "skill:${skill.id}" }
            val ready = canUnlockSkill(s, skill)
            val accent = if (owned) Green else if (ready) Gold else Violet
            Panel(accent = accent) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ArchetypeSealArt(skill.atlasIndex, Modifier.size(56.dp), dimmed = !owned && !ready)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(skill.name, style = MaterialTheme.typography.titleLarge)
                        Text("${skill.path} · Nível ${skill.level} · 1 ponto", color = Muted, fontSize = 12.sp)
                        Text(if (owned) "Despertada na constelação" else if (ready) "Disponível para despertar" else skillRequirementText(s, skill), color = if (owned) Green else if (ready) Gold else Muted, fontSize = 12.sp)
                    }
                }
                Text(skill.description, color = Muted, fontSize = 13.sp)
                if (skill.effectSummary.isNotBlank()) Text(skill.effectSummary, color = accent, fontSize = 12.sp)
                Action(if (owned) "Habilidade despertada" else "Despertar habilidade", { vm.run { vm.repo.unlockSkill(skill.id) } }, enabled = !owned && ready)
            }
        }
    }

    Hairline()
    Eyebrow("Constelações Universais · Relíquias", Blue)
    skillDefs.filter { it.heroClass < 0 }.groupBy { it.path }.forEach { (path, nodes) ->
        Eyebrow(path)
        nodes.forEachIndexed { i, skill ->
            if (i > 0) SagaDivider(color = Blue)
            val owned = s.assets.any { it.id == "skill:${skill.id}" }
            val ready = canUnlockSkill(s, skill)
            Panel(accent = if (owned) Green else Violet) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    RelicArt(shopItems.first { it.id == skill.item }.kind, if (owned) Green else Violet, itemId = skill.item)
                    Column(Modifier.weight(1f)) {
                        Text(skill.name, style = MaterialTheme.typography.titleLarge)
                        Text("Nível ${skill.level} · 1 ponto", color = Muted, fontSize = 12.sp)
                        Text(if (owned) "Inscrita na constelação" else if (ready) "Disponível" else "Bloqueada", color = if (owned) Green else Muted, fontSize = 12.sp)
                    }
                }
                skill.parent?.let { parent -> Text("Requer: ${skillDefs.first { it.id == parent }.name}", color = Muted, fontSize = 12.sp) }
                Text(skill.description, color = Muted)
                Action(if (owned) "Despertada · item no inventário" else "Despertar habilidade", { vm.run { vm.repo.unlockSkill(skill.id) } }, enabled = !owned && ready)
            }
        }
    }
}

@Composable
fun EquippedNiches(s: GameState) {
    val items = s.assets.filter { it.equipped }.mapNotNull { a -> shopItems.find { it.id == a.id } }
    if (items.isNotEmpty()) {
        Eyebrow("Artefatos preparados")
        items.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { item ->
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        RelicArt(item.kind, rarityColor(rarity(item)), Modifier.fillMaxWidth().aspectRatio(1f), item.id)
                        Text(item.name, color = Muted, fontSize = 10.sp, lineHeight = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}