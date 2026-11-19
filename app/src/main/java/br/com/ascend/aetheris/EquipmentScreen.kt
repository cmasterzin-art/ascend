package br.com.ascend.aetheris

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

@Composable
fun Equipment(
    s: GameState,
    vm: GameViewModel,
    store: Boolean = false,
    onOpenForge: (() -> Unit)? = null
) {
    var selected by remember { mutableStateOf<String?>(null) }
    var filter by remember { mutableIntStateOf(0) }
    var studioOpen by remember { mutableStateOf(false) }
    var renamingLoadout by remember { mutableStateOf<EquipmentLoadout?>(null) }
    var previousAppearance by remember { mutableStateOf<Map<String, String?>?>(null) }

    val filterLabels = listOf("Todos", "Cabeça", "Trajes", "Acessórios", "Armas", "Aliados", "Molduras")

    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
    if (!store) {
        val minimal = LocalAppearance.current.minimal
        val spec = activeSpecialization(s)
        // Arsenal dos Nove Reinos — câmara central com Retrato Vivo e 7 nichos nórdicos
        val dark = LocalAppearance.current.dark
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
                    index = 0, // Salão Viking como ambiência da câmara do Arsenal
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
                    RuneSeal(3, Gold, Modifier.size(28.dp))
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

        // 3 Conjuntos de Equipamento salvos e alternáveis
        LoadoutDeckSection(
            s = s,
            vm = vm,
            onRename = { renamingLoadout = it }
        )
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

    ChipRow(filterLabels, filter, { filter = it })

    val items = shopItems.filter { item ->
        val visibleInTab = if (store) {
            skillDefs.none { it.item == item.id } &&
                item.id !in expeditionRelicIds &&
                item.id !in forgeItemIds
        } else {
            s.assets.any { it.id == item.id }
        }
        visibleInTab && matchesArsenalFilter(item, filter)
    }

    if (items.isEmpty()) {
        Empty(
            if (store) "Nenhum item do mercador nesta categoria."
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
                val originTag = when {
                    item.id in forgeItemIds -> "Forja de Darian"
                    item.id in expeditionRelicIds -> "Expedição"
                    skillDefs.any { it.item == item.id } -> "Habilidade"
                    else -> "Mercador"
                }
                AetherSurface(
                    SurfaceKind.Artifact,
                    Modifier
                        .weight(1f)
                        .border(
                            width = if (owned?.equipped == true) spec.borderWidth + 0.4.dp else spec.borderWidth,
                            color = color.copy(alpha = if (owned?.equipped == true) 0.85f else 0.36f),
                            shape = CutCornerShape(6.dp)
                        )
                        .clickable { selected = item.id }
                ) {
                    RarityCrestBadge(tier, color)
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        RelicArt(item.kind, color, Modifier.size(92.dp), item.id)
                    }
                    SagaDivider(color = color)
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
                            else -> "◈ ${item.cost} fragmentos"
                        },
                        color = color,
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
            Panel {
                RuneSeal(4, Gold, Modifier.size(36.dp))
                Eyebrow("Troféu de missão")
                Text(s.quests.find { q -> s.claims.any { it.questId == q.id && "quest:${it.id}" == a.id } }?.special ?: "Memória da jornada")
            }
        }
    }
    }

    // Dialog de inspeção detalhada do item com prévia instantânea no Retrato Vivo
    selected?.let { id ->
        val item = shopItems.first { it.id == id }
        val owned = s.assets.find { it.id == id }
        val tier = rarity(item)
        val spec = rarityVisualSpec(tier)
        val color = rarityColor(tier)
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
                            vm.run { vm.repo.equip(owned); selected = null }
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
            RarityCrestBadge(tier, color)
            Text(
                "${spec.materialLabel} · ${portraitRepresentationFor(item).label}",
                color = color,
                fontSize = 12.sp
            )
            Text(item.description, fontSize = 13.sp)
            SagaDivider(color = color)
            Eyebrow("Procedência", color)
            Text(itemOrigin(item), color = Muted, fontSize = 12.sp)
            if (owned != null) {
                Text("Guardado na coleção em ${displayDate(owned.date)}", color = Muted, fontSize = 11.sp)
            }
        }
    }

    // Estúdio de Personalização em tela cheia
    if (studioOpen && s.player != null) {
        HeroCustomizationStudio(
            s = s,
            previousAppearance = previousAppearance,
            onDismiss = { studioOpen = false },
            onSave = { confirmedSelection ->
                val currentBeforeSave = equipmentSlots.associate { (kind, _) ->
                    kind to s.assets.find { it.kind == kind && it.equipped }?.id
                }
                previousAppearance = currentBeforeSave
                vm.run {
                    vm.repo.applyStudioSelection(confirmedSelection)
                    studioOpen = false
                }
            }
        )
    }

    // Dialog para renomear conjunto
    renamingLoadout?.let { loadout ->
        var text by remember(loadout.id) { mutableStateOf(loadout.name) }
        AetherDialog(
            onDismissRequest = { renamingLoadout = null },
            title = "Renomear conjunto",
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
            FormField(text, { text = it.take(40) }, "Nome do conjunto (até 40 caracteres)")
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
    var selectedLoadoutId by remember { mutableStateOf(loadouts.first().id) }
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
                Eyebrow("Conjuntos de Equipamento (3)", Gold)
            }
            Text("${slots.size}/7 peças", color = Muted, fontSize = 11.sp)
        }

        ChipRow(
            values = loadouts.map { it.name },
            selected = loadouts.indexOfFirst { it.id == activeLoadout.id }.coerceAtLeast(0),
            onSelect = { idx -> selectedLoadoutId = loadouts[idx].id }
        )

        if (empty) {
            Text(
                "Espaço vazio. Equipe seu visual no Arsenal ou Estúdio e toque em \"Salvar atual\" para guardar neste conjunto.",
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
                    "Aviso: ${missing.size} ${if (missing.size == 1) "item não está" else "itens não estão"} na coleção atual e ${if (missing.size == 1) "será ignorado" else "serão ignorados"} ao aplicar, sem substituir peças.",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 11.sp
                )
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SagaButton(
                text = "Aplicar",
                onClick = { vm.run { vm.repo.applyLoadout(activeLoadout.id) } },
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
}

/**
 * Estúdio de Personalização — prévia ao vivo em tamanho destacado no Retrato Vivo,
 * experimentação de itens possuídos e bloqueados (claramente marcados sem conceder posse),
 * desfazer alterações não confirmadas, salvar e restaurar aparência anterior.
 */
@Composable
fun HeroCustomizationStudio(
    s: GameState,
    previousAppearance: Map<String, String?>?,
    onDismiss: () -> Unit,
    onSave: (Map<String, String?>) -> Unit,
    inline: Boolean = false
) {
    val hero = requireNotNull(s.player)
    val spec = activeSpecialization(s)
    val dark = LocalAppearance.current.dark
    val initialEquipped = remember(s.assets) {
        equipmentSlots.associate { (kind, _) ->
            kind to s.assets.find { it.kind == kind && it.equipped }?.id
        }
    }
    var draft by remember(initialEquipped) { mutableStateOf(initialEquipped) }
    var activeSlotIndex by remember { mutableIntStateOf(0) }
    val ownedIds = remember(s.assets) { s.assets.map { it.id }.toSet() }

    val lockedPreviewItems = remember(draft, ownedIds) {
        draft.values.filterNotNull().filter { it.isNotBlank() && it !in ownedIds }
    }
    val hasChanges = draft != initialEquipped
    val canSave = lockedPreviewItems.isEmpty()

    val slotKinds = equipmentSlots
    val currentSlot = slotKinds[activeSlotIndex]
    val candidates = remember(currentSlot.first) {
        shopItems.filter { it.kind == currentSlot.first }
    }

    val saveLabel = if (canSave) "Salvar" else "Bloqueado"
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
                            draft = draft.mapValues { (k, v) -> if (v != null && v !in ownedIds) initialEquipped[k] else v }
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
                        onClick = { draft = initialEquipped },
                        modifier = Modifier.weight(1f).testTag("studio_undo_btn"),
                        enabled = hasChanges,
                        style = VikingButtonStyle.FerroRunico
                    )
                    if (previousAppearance != null) {
                        SagaButton(
                            text = "Restaurar anterior",
                            onClick = { draft = previousAppearance },
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

        // Option to clear the active slot
        val currentChosen = draft[currentSlot.first]
        Row(
            Modifier
                .fillMaxWidth()
                .clip(CutCornerShape(5.dp))
                .background(if (currentChosen == null) Gold.copy(alpha = 0.14f) else Surface)
                .border(1.dp, if (currentChosen == null) Gold.copy(alpha = 0.72f) else ColdIron.copy(alpha = 0.35f), CutCornerShape(5.dp))
                .clickable { draft = draft + (currentSlot.first to null) }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Nenhum (${currentSlot.second})", color = Ink, fontSize = 13.sp)
            Text(if (currentChosen == null) "Selecionado" else "Remover", color = if (currentChosen == null) Gold else Muted, fontSize = 11.sp)
        }

        candidates.forEach { item ->
            val owned = item.id in ownedIds
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
                    .clickable { draft = draft + (currentSlot.first to item.id) }
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RelicArt(item.kind, color, Modifier.size(54.dp), item.id, dim = !owned)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(item.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
        Column(Modifier.fillMaxSize().background(Night)) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(Surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
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
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = body
            )
        }
    } else {
        FullForm(
            title = "Estúdio de Personalização",
            onDismiss = onDismiss,
            onSave = { if (canSave) onSave(draft) },
            saveLabel = saveLabel,
            content = body
        )
    }
}

private fun slotFilterIndex(kind: String): Int = when (kind) {
    "head" -> 1
    "outfit" -> 2
    "accessory" -> 3
    "weapon" -> 4
    "companion" -> 5
    "frame", "background" -> 6
    else -> 0
}

private fun matchesArsenalFilter(item: ShopItem, filter: Int): Boolean = when (filter) {
    1 -> item.kind == "head"
    2 -> item.kind == "outfit"
    3 -> item.kind == "accessory"
    4 -> item.kind == "weapon"
    5 -> item.kind == "companion"
    6 -> item.kind in listOf("frame", "background", "decoration")
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
            if (a == null) "Vazio" else "Ativo",
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