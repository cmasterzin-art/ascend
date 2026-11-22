package br.com.ascend.aetheris

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 1.7.3.1 — O Legado das Runas:
 * UI composables for Heráldica Rúnica, Estandartes, Codex Rúnico (24 Elder Futhark),
 * Selos de Aprimoramento da Forja e Coleções Lendárias.
 */

// ============================================================================
// 1. BRASÃO & ESTANDARTE HERÁLDICO REUTILIZÁVEIS
// ============================================================================

@Composable
fun HeraldicCrest(
    profile: HeraldryProfile,
    size: Dp = 56.dp,
    modifier: Modifier = Modifier,
    showInscription: Boolean = false
) {
    val minimal = LocalAppearance.current.minimal
    val symbol = remember(profile.symbolId) { heraldrySymbolById(profile.symbolId) }
    val shape = remember(profile.shapeId) { heraldryShapeById(profile.shapeId) }
    val metal = remember(profile.metalId) { heraldryMetalById(profile.metalId) }
    val secondary = remember(profile.secondaryColorId) { heraldryColorById(profile.secondaryColorId) }
    val ornament = remember(profile.ornamentId) { heraldryOrnamentById(profile.ornamentId) }
    val runes = remember(profile.inscriptionRunes) { parseHeraldryInscription(profile.inscriptionRunes) }

    val desc = "Brasão heráldico: ${symbol.name} em ${shape.name}, ${metal.name}, cor ${secondary.name}" +
        if (runes.isNotEmpty()) ", inscrição ${runes.joinToString(" ") { it.runeName }}" else ""

    Column(
        modifier = modifier.semantics { contentDescription = desc },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            Modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            if (minimal || size < 24.dp) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(secondary.color.copy(alpha = 0.85f))
                        .border(1.5.dp, metal.rimColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val rune = runes.firstOrNull() ?: HistoricalFutharkRune.Othala
                    FutharkRuneGlyph(rune, metal.tint, size = size * 0.54f)
                }
            } else {
                // Secondary field glow behind shield base
                Canvas(Modifier.fillMaxSize(0.76f)) {
                    drawCircle(
                        Brush.radialGradient(
                            listOf(secondary.color.copy(alpha = 0.72f), secondary.color.copy(alpha = 0.12f), Color.Transparent)
                        )
                    )
                }
                // Layer 1: Blank Viking shield / plaque / medallion base
                AtlasCell(
                    resource = R.drawable.viking_heraldic_bases_atlas,
                    index = shape.atlasCell,
                    columns = 4,
                    rows = 2,
                    modifier = Modifier.fillMaxSize(),
                    filter = ColorFilter.tint(metal.tint, BlendMode.Modulate)
                )
                // Inner heraldic field wash
                Canvas(Modifier.fillMaxSize(0.62f)) {
                    drawCircle(
                        Brush.radialGradient(
                            listOf(secondary.color.copy(alpha = 0.55f), Color.Transparent)
                        )
                    )
                }
                // Layer 2: Central Viking heraldic symbol
                AtlasCell(
                    resource = R.drawable.ancestral_heraldry_atlas,
                    index = symbol.atlasCell,
                    columns = 4,
                    rows = 2,
                    modifier = Modifier.fillMaxSize(0.56f)
                )
            }
        }
        if (ornament.atlasCell != null && !minimal && size >= 56.dp) {
            AtlasCell(
                resource = R.drawable.forge_consacration_atlas,
                index = ornament.atlasCell,
                columns = 4,
                rows = 2,
                modifier = Modifier.width(size * 0.92f).height(size * 0.24f)
            )
        }
        if (showInscription && runes.isNotEmpty()) {
            Box(
                Modifier
                    .clip(CutCornerShape(4.dp))
                    .background(Color(0xFF14181F))
                    .border(0.8.dp, metal.rimColor.copy(alpha = 0.7f), CutCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    runes.forEach { r ->
                        FutharkRuneGlyph(r, metal.tint, size = 14.dp)
                    }
                }
            }
        }
    }
}

@Composable
fun HeraldicBanner(
    profile: HeraldryProfile,
    heroName: String,
    modifier: Modifier = Modifier,
    width: Dp = 128.dp
) {
    val minimal = LocalAppearance.current.minimal
    val banner = remember(profile.bannerId) { heraldryBannerById(profile.bannerId) }
    val metal = remember(profile.metalId) { heraldryMetalById(profile.metalId) }
    val secondary = remember(profile.secondaryColorId) { heraldryColorById(profile.secondaryColorId) }
    val runes = remember(profile.inscriptionRunes) { parseHeraldryInscription(profile.inscriptionRunes) }

    Box(
        modifier = modifier
            .width(width)
            .height(width * 1.28f)
            .semantics { contentDescription = "Estandarte de $heroName (${banner.name})" },
        contentAlignment = Alignment.TopCenter
    ) {
        if (!minimal) {
            AtlasCell(
                resource = R.drawable.viking_heraldic_bases_atlas,
                index = banner.atlasCell,
                columns = 4,
                rows = 2,
                modifier = Modifier.fillMaxSize(),
                filter = ColorFilter.tint(
                    Color(
                        red = (secondary.color.red * 0.45f + 0.55f).coerceIn(0f, 1f),
                        green = (secondary.color.green * 0.45f + 0.55f).coerceIn(0f, 1f),
                        blue = (secondary.color.blue * 0.45f + 0.55f).coerceIn(0f, 1f),
                        alpha = 1f
                    ),
                    BlendMode.Modulate
                )
            )
        } else {
            Box(
                Modifier
                    .fillMaxSize()
                    .clip(CutCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp))
                    .background(secondary.color.copy(alpha = 0.7f))
                    .border(1.dp, metal.rimColor, CutCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp))
            )
        }
        Column(
            Modifier
                .fillMaxSize()
                .padding(top = width * 0.18f, bottom = width * 0.14f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            HeraldicCrest(profile, size = width * 0.54f, showInscription = false)
            if (runes.isNotEmpty()) {
                Row(
                    Modifier
                        .clip(CutCornerShape(3.dp))
                        .background(Color(0xCC0D1016))
                        .border(0.6.dp, metal.rimColor.copy(alpha = 0.65f), CutCornerShape(3.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    runes.forEach { r -> FutharkRuneGlyph(r, metal.tint, size = 11.dp) }
                }
            }
        }
    }
}

// ============================================================================
// 2. EDITOR DE HERÁLDICA RÚNICA
// ============================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HeraldryEditorScreen(
    s: GameState,
    vm: GameViewModel,
    onOpenCodex: () -> Unit,
    onBack: () -> Unit
) {
    val saved = remember(s.heraldry, s.player?.created) { resolvedHeraldry(s) }
    var symbolId by rememberSaveable { mutableStateOf(saved.symbolId) }
    var shapeId by rememberSaveable { mutableStateOf(saved.shapeId) }
    var metalId by rememberSaveable { mutableStateOf(saved.metalId) }
    var colorId by rememberSaveable { mutableStateOf(saved.secondaryColorId) }
    var ornamentId by rememberSaveable { mutableStateOf(saved.ornamentId) }
    var bannerId by rememberSaveable { mutableStateOf(saved.bannerId) }
    var inscription by rememberSaveable { mutableStateOf(saved.inscriptionRunes) }
    var showOnPortrait by rememberSaveable { mutableStateOf(saved.showOnPortrait) }
    var translitInput by rememberSaveable { mutableStateOf("") }
    var confirmDiscard by remember { mutableStateOf(false) }

    val draft = HeraldryProfile(
        id = 1,
        symbolId = symbolId,
        shapeId = shapeId,
        metalId = metalId,
        secondaryColorId = colorId,
        inscriptionRunes = inscription,
        ornamentId = ornamentId,
        bannerId = bannerId,
        showOnPortrait = showOnPortrait,
        updated = today()
    )
    val dirty = symbolId != saved.symbolId || shapeId != saved.shapeId || metalId != saved.metalId ||
        colorId != saved.secondaryColorId || ornamentId != saved.ornamentId || bannerId != saved.bannerId ||
        inscription != saved.inscriptionRunes || showOnPortrait != saved.showOnPortrait

    val lockedDraft = !heraldrySymbolUnlocked(s, symbolId) ||
        !heraldryMetalUnlocked(s, metalId) ||
        !heraldryOrnamentUnlocked(s, ornamentId) ||
        !heraldryBannerUnlocked(s, bannerId)

    val selectedRunes = remember(inscription) { parseHeraldryInscription(inscription) }

    Page {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            TextButton({ if (dirty) confirmDiscard = true else onBack() }) { Text("Voltar") }
            TextButton(onOpenCodex) { Text("Codex Rúnico (24)") }
        }
        Title("Heráldica Rúnica de Aetheris", "Brasão pessoal, estandarte e inscrição rúnica · 100% cosmético.")

        // Live preview card (Brasão + Estandarte side-by-side)
        Panel(accent = Gold) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Eyebrow("Prévia do Brasão e Estandarte", Gold)
                if (lockedDraft) Text("Prévia bloqueada", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    HeraldicCrest(draft, size = 104.dp, showInscription = true)
                    Text(heraldrySymbolById(symbolId).name, color = Ink, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("${heraldryShapeById(shapeId).name} · ${heraldryMetalById(metalId).name}", color = Muted, fontSize = 11.sp)
                }
                HeraldicBanner(draft, s.player?.name ?: "Aventureiro", width = 112.dp)
            }
            Text(heraldrySymbolById(symbolId).lore, color = Muted, fontSize = 12.sp)
            Toggle("Exibir brasão na moldura do Retrato Vivo", showOnPortrait) { showOnPortrait = it }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SagaButton(
                    text = if (lockedDraft) "Opção bloqueada" else "Salvar brasão",
                    onClick = { vm.run { vm.repo.saveHeraldry(draft) } },
                    enabled = !lockedDraft && dirty,
                    modifier = Modifier.weight(1f).testTag("save_heraldry_btn"),
                    style = VikingButtonStyle.CerimonialViking
                )
                SagaButton(
                    text = "Restaurar",
                    onClick = {
                        symbolId = saved.symbolId; shapeId = saved.shapeId; metalId = saved.metalId
                        colorId = saved.secondaryColorId; ornamentId = saved.ornamentId; bannerId = saved.bannerId
                        inscription = saved.inscriptionRunes; showOnPortrait = saved.showOnPortrait
                    },
                    enabled = dirty,
                    modifier = Modifier.weight(1f),
                    style = VikingButtonStyle.FerroRunico
                )
            }
        }

        // 1. Símbolo Central (8 símbolos)
        Panel {
            Eyebrow("Símbolo Central (8 emblemas)", Gold)
            FlowRow(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                heraldrySymbols.forEach { sym ->
                    val unlocked = heraldrySymbolUnlocked(s, sym.id)
                    val active = symbolId == sym.id
                    Column(
                        Modifier
                            .widthIn(min = 132.dp)
                            .clip(CutCornerShape(6.dp))
                            .background(if (active) Gold.copy(alpha = 0.14f) else CardColor)
                            .border(if (active) 1.4.dp else 0.8.dp, if (active) Gold else ColdIron.copy(alpha = 0.45f), CutCornerShape(6.dp))
                            .clickable { symbolId = sym.id }
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        AtlasCell(R.drawable.ancestral_heraldry_atlas, sym.atlasCell, 4, 2, Modifier.size(44.dp))
                        Text(sym.name, color = Ink, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            if (unlocked) "Disponível" else sym.unlockLabel,
                            color = if (unlocked) Green else Gold,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // 2. Formato (4) & Metal (4)
        Panel {
            Eyebrow("Formato do Brasão (4)", Blue)
            ChipRow(heraldryShapes.map { it.name }, heraldryShapes.indexOfFirst { it.id == shapeId }.coerceAtLeast(0), {
                shapeId = heraldryShapes[it].id
            })
            Spacer(Modifier.height(6.dp))
            Eyebrow("Acabamento Metálico (4)", Gold)
            FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                heraldryMetals.forEach { m ->
                    val unlocked = heraldryMetalUnlocked(s, m.id)
                    val active = metalId == m.id
                    Row(
                        Modifier
                            .clip(CutCornerShape(5.dp))
                            .background(if (active) m.tint.copy(alpha = 0.22f) else CardColor)
                            .border(if (active) 1.3.dp else 0.8.dp, if (active) m.tint else ColdIron.copy(alpha = 0.4f), CutCornerShape(5.dp))
                            .clickable { metalId = m.id }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(Modifier.size(12.dp).clip(CircleShape).background(m.tint).border(0.8.dp, m.rimColor, CircleShape))
                        Column {
                            Text(m.name, color = Ink, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            if (!unlocked) Text(m.unlockLabel, color = Gold, fontSize = 10.sp)
                        }
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Eyebrow("Cor Secundária", Muted)
            ChipRow(heraldrySecondaryColors.map { it.name }, heraldrySecondaryColors.indexOfFirst { it.id == colorId }.coerceAtLeast(0), {
                colorId = heraldrySecondaryColors[it].id
            })
        }

        // 3. Estandarte (3) & Ornamento (5)
        Panel {
            Eyebrow("Estandarte Pessoal (3)", Gold)
            FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                heraldryBanners.forEach { b ->
                    val unlocked = heraldryBannerUnlocked(s, b.id)
                    val active = bannerId == b.id
                    Column(
                        Modifier
                            .widthIn(min = 100.dp)
                            .clip(CutCornerShape(5.dp))
                            .background(if (active) Gold.copy(alpha = 0.14f) else CardColor)
                            .border(if (active) 1.3.dp else 0.8.dp, if (active) Gold else ColdIron.copy(alpha = 0.45f), CutCornerShape(5.dp))
                            .clickable { bannerId = b.id }
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        AtlasCell(R.drawable.viking_heraldic_bases_atlas, b.atlasCell, 4, 2, Modifier.size(44.dp))
                        Text(b.name, color = Ink, fontSize = 11.sp, textAlign = TextAlign.Center)
                        if (!unlocked) Text(b.unlockLabel, color = Gold, fontSize = 9.sp, textAlign = TextAlign.Center)
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Eyebrow("Ornamentação Metálica", Blue)
            FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                heraldryOrnaments.forEach { o ->
                    val unlocked = heraldryOrnamentUnlocked(s, o.id)
                    val active = ornamentId == o.id
                    Column(
                        Modifier
                            .clip(CutCornerShape(4.dp))
                            .background(if (active) Gold.copy(alpha = 0.16f) else CardColor)
                            .border(if (active) 1.2.dp else 0.8.dp, if (active) Gold else ColdIron.copy(alpha = 0.4f), CutCornerShape(4.dp))
                            .clickable { ornamentId = o.id }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(o.name, color = Ink, fontSize = 12.sp)
                        if (!unlocked) Text(o.unlockLabel, color = Gold, fontSize = 10.sp)
                    }
                }
            }
        }

        // 4. Inscrição Rúnica (até 6 runas + transliteração aproximada explícita)
        Panel(accent = Blue) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Eyebrow("Inscrição Rúnica (${selectedRunes.size}/6)", Blue)
                if (selectedRunes.isNotEmpty()) {
                    TextButton({ inscription = "" }) { Text("Remover inscrição", fontSize = 12.sp) }
                }
            }
            if (selectedRunes.isNotEmpty()) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(CutCornerShape(4.dp))
                        .background(CardColor)
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    selectedRunes.forEach { r ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            FutharkRuneGlyph(r, Gold, size = 20.dp)
                            Text(r.transliteration, color = Muted, fontSize = 10.sp)
                        }
                    }
                }
                Text(
                    selectedRunes.joinToString(" · ") { "${it.runeName} (${it.transliteration})" },
                    color = Muted,
                    fontSize = 11.sp
                )
            } else {
                Text("Toque nas runas abaixo para compor uma inscrição curta ou use transliteração aproximada.", color = Muted, fontSize = 12.sp)
            }

            FormField(translitInput, { translitInput = it.take(18) }, "Palavra curta (transliteração aproximada)")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Transliteração aproximada — não é tradução para nórdico antigo.", color = Muted, fontSize = 10.sp, modifier = Modifier.weight(1f))
                SagaButton(
                    text = "Aplicar",
                    onClick = {
                        val mapped = approximateRuneTransliteration(translitInput)
                        if (mapped.isNotEmpty()) inscription = mapped.joinToString(",") { it.name }
                    },
                    enabled = translitInput.isNotBlank(),
                    style = VikingButtonStyle.CompactoRunico
                )
            }
            FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                HistoricalFutharkRune.canonicalOrder.forEach { r ->
                    Column(
                        Modifier
                            .widthIn(min = 52.dp)
                            .clip(CutCornerShape(4.dp))
                            .background(CardColor)
                            .border(0.7.dp, ColdIron.copy(alpha = 0.45f), CutCornerShape(4.dp))
                            .clickable(enabled = selectedRunes.size < 6) {
                                val next = (selectedRunes + r).take(6)
                                inscription = next.joinToString(",") { it.name }
                            }
                            .padding(vertical = 6.dp, horizontal = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        FutharkRuneGlyph(r, Gold, size = 18.dp)
                        Text(r.runeName, color = Ink, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(r.transliteration, color = Muted, fontSize = 9.sp)
                    }
                }
            }
        }
    }

    if (confirmDiscard) {
        AetherDialog(
            onDismissRequest = { confirmDiscard = false },
            title = "Descartar alterações do brasão?",
            confirmButton = {
                SagaButton("Descartar", { confirmDiscard = false; onBack() }, style = VikingButtonStyle.Destrutivo)
            },
            dismissButton = {
                TextButton({ confirmDiscard = false }) { Text("Continuar editando") }
            }
        ) {
            Text("Existem escolhas não salvas no seu brasão heráldico.", color = Muted, fontSize = 13.sp)
        }
    }
}

// ============================================================================
// 3. CODEX RÚNICO — 24 CARACTERES HISTÓRICOS DO ELDER FUTHARK
// ============================================================================

@Composable
fun RunicCodexScreen(
    onOpenHeraldry: () -> Unit,
    onBack: () -> Unit,
    initialRuneName: String? = null,
    vm: GameViewModel? = null
) {
    var query by rememberSaveable { mutableStateOf("") }
    var aettFilter by rememberSaveable { mutableIntStateOf(0) } // 0=Todas, 1=Freyr, 2=Hagal, 3=Tyr, 4=Favoritas
    var surfaceIndex by rememberSaveable { mutableIntStateOf(0) } // 0..5 runic_codex_surfaces_atlas
    var selectedRuneName by rememberSaveable { mutableStateOf(initialRuneName ?: "") }
    var localFavoritesRaw by rememberSaveable { mutableStateOf("") }

    val persistedFavorites by (vm?.codexFavorites?.collectAsState() ?: remember { mutableStateOf(emptySet()) })
    val localFavorites = remember(localFavoritesRaw) { localFavoritesRaw.split(',').filter { it.isNotBlank() }.toSet() }
    val favorites = if (vm != null) persistedFavorites else localFavorites
    val surface = codexSurfaces[surfaceIndex.coerceIn(codexSurfaces.indices)]

    val filtered = remember(query, aettFilter, favorites) {
        val q = query.trim().lowercase()
        codexRuneEntries.filter { e ->
            val matchAett = when (aettFilter) {
                1 -> e.aett.startsWith("1º")
                2 -> e.aett.startsWith("2º")
                3 -> e.aett.startsWith("3º")
                4 -> e.rune.name in favorites
                else -> true
            }
            val matchQuery = q.isBlank() ||
                e.rune.runeName.lowercase().contains(q) ||
                e.rune.transliteration.lowercase().contains(q) ||
                e.documentedMeaning.lowercase().contains(q) ||
                e.aetherisAssociation.lowercase().contains(q)
            matchAett && matchQuery
        }
    }

    val selectedEntry = remember(selectedRuneName) {
        HistoricalFutharkRune.byStableId(selectedRuneName)?.let { codexEntryFor(it) }
    }

    val nbC = LocalNorthborn.current
    Page {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            TextButton(onBack) { Text("Voltar") }
            TextButton(onOpenHeraldry) { Text("Heráldica Rúnica") }
        }
        Title("Codex Rúnico · Elder Futhark", "24 runas históricas vetoriais · enciclopédia offline.")

        NorthbornSurface(role = NorthbornSurfaceRole.Informative, accent = nbC.accentFjord) {
            Eyebrow("Contexto Histórico · Elder vs. Younger Futhark", nbC.accentFjord)
            Text(CODEX_HISTORICAL_DISCLAIMER, color = nbC.inkSecondary, fontSize = 12.sp, lineHeight = 18.sp)
        }

        // Material preview selector aberto (sem caixa redundante)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Eyebrow("Superfície de Estudo (${surface.name})", nbC.accentBronze)
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                codexSurfaces.forEachIndexed { idx, sDef ->
                    val active = idx == surfaceIndex
                    Column(
                        Modifier
                            .width(86.dp)
                            .clip(NorthbornShape.sm)
                            .background(nbC.surfaceBase)
                            .border(if (active) 1.5.dp else 1.dp, if (active) nbC.accentBronze else nbC.hairline, NorthbornShape.sm)
                            .clickable { surfaceIndex = idx },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(Modifier.fillMaxWidth().height(42.dp), contentAlignment = Alignment.Center) {
                            AtlasCellCropped(R.drawable.runic_codex_surfaces_atlas, sDef.atlasCell, 3, 2, Modifier.matchParentSize())
                            FutharkRuneGlyph(HistoricalFutharkRune.Ansuz, sDef.inkColor, size = 20.dp)
                        }
                        Text(sDef.name, color = if (active) nbC.accentBronze else nbC.inkPrimary, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(4.dp))
                    }
                }
            }
        }
        NorthbornDivider(NorthbornDividerStyle.Hairline)

        // Detailed view of selected rune (inline manuscript card)
        if (selectedEntry != null) {
            val isFav = selectedEntry.rune.name in favorites
            Panel(accent = Gold) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Eyebrow("${selectedEntry.aett} · #${selectedEntry.rune.futharkOrder}/24", Gold)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextButton({
                            if (vm != null) {
                                vm.toggleCodexFavorite(selectedEntry.rune.name)
                            } else {
                                val next = if (isFav) favorites - selectedEntry.rune.name else favorites + selectedEntry.rune.name
                                localFavoritesRaw = next.joinToString(",")
                            }
                        }) { Text(if (isFav) "★ Favorita" else "☆ Favoritar", fontSize = 12.sp) }
                        TextButton({ selectedRuneName = "" }) { Text("Fechar", fontSize = 12.sp) }
                    }
                }
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(104.dp)
                            .clip(CutCornerShape(8.dp))
                            .border(1.2.dp, Gold.copy(alpha = 0.7f), CutCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        AtlasCellCropped(R.drawable.runic_codex_surfaces_atlas, surface.atlasCell, 3, 2, Modifier.matchParentSize())
                        FutharkRuneGlyph(selectedEntry.rune, surface.inkColor, size = 56.dp)
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "${selectedEntry.rune.runeName}  ${selectedEntry.rune.unicode}",
                            color = Ink,
                            fontFamily = FontFamily.Serif,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text("Nome reconstruído: ${selectedEntry.reconstructedName}", color = Gold, fontSize = 12.sp)
                        Text("Transliteração: ${selectedEntry.rune.transliteration}", color = Ink, fontSize = 12.sp)
                        Text("Significado documentado/reconstruído: ${selectedEntry.documentedMeaning}", color = Muted, fontSize = 12.sp)
                    }
                }
                Text("Contexto histórico", color = Blue, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Text(selectedEntry.historicalContext, color = Ink, fontSize = 13.sp, lineHeight = 19.sp)
                Text("Associação narrativa em Aetheris (ficção)", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Text(selectedEntry.aetherisAssociation, color = Muted, fontSize = 12.sp, lineHeight = 18.sp)
                SagaButton(
                    text = "Usar na Heráldica Rúnica",
                    onClick = onOpenHeraldry,
                    modifier = Modifier.fillMaxWidth(),
                    style = VikingButtonStyle.BronzeForjado
                )
            }
        }

        // Search + Ætt filters
        FormField(query, { query = it }, "Buscar runa por nome, som ou significado")
        ChipRow(
            listOf("Todas (24)", "1º Ætt", "2º Ætt", "3º Ætt", "Favoritas"),
            aettFilter,
            { aettFilter = it }
        )

        if (filtered.isEmpty()) {
            Panel {
                Text("Nenhuma runa corresponde ao filtro atual.", color = Muted, fontSize = 13.sp)
            }
        } else {
            // 3-column responsive grid of all 24 historical runes
            filtered.chunked(3).forEach { rowItems ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowItems.forEach { entry ->
                        val active = selectedRuneName == entry.rune.name
                        val isFav = entry.rune.name in favorites
                        Column(
                            Modifier
                                .weight(1f)
                                .clip(CutCornerShape(6.dp))
                                .background(if (active) Gold.copy(alpha = 0.14f) else Surface)
                                .border(if (active) 1.4.dp else 0.8.dp, if (active) Gold else ColdIron.copy(alpha = 0.45f), CutCornerShape(6.dp))
                                .clickable { selectedRuneName = entry.rune.name }
                                .testTag("codex_rune_${entry.rune.name}")
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                Modifier
                                    .size(54.dp)
                                    .clip(CutCornerShape(5.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                AtlasCellCropped(R.drawable.runic_codex_surfaces_atlas, surface.atlasCell, 3, 2, Modifier.matchParentSize())
                                FutharkRuneGlyph(entry.rune, surface.inkColor, size = 30.dp)
                            }
                            Text(
                                if (isFav) "★ ${entry.rune.runeName}" else entry.rune.runeName,
                                color = Ink,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "${entry.rune.unicode} · ${entry.rune.transliteration}",
                                color = Muted,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    repeat(3 - rowItems.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

// ============================================================================
// 4. SELO DE APRIMORAMENTO DA FORJA & COLEÇÕES LENDÁRIAS
// ============================================================================

@Composable
fun UpgradeStageBadge(
    stage: Int,
    modifier: Modifier = Modifier
) {
    val def = remember(stage) { forgeUpgradeStageDef(stage) }
    val minimal = LocalAppearance.current.minimal
    val color = when (def.stage) {
        FORGE_STAGE_CONSECRATED -> Gold
        FORGE_STAGE_ENGRAVED -> Color(0xFFB5C4D8)
        else -> OldBronze
    }
    Row(
        modifier = modifier
            .clip(CutCornerShape(4.dp))
            .background(color.copy(alpha = 0.14f))
            .border(0.8.dp, color.copy(alpha = 0.6f), CutCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (!minimal) {
            AtlasCell(R.drawable.forge_consacration_atlas, def.atlasCell, 4, 2, Modifier.size(15.dp))
        }
        Text(
            def.shortLabel.uppercase(),
            color = color,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.6.sp,
            maxLines = 1
        )
    }
}

@Composable
fun LegendaryCollectionsSection(
    s: GameState,
    vm: GameViewModel,
    onOpenForgeRecipe: ((String) -> Unit)? = null
) {
    val minimal = LocalAppearance.current.minimal
    val dark = LocalAppearance.current.dark
    val heraldry = remember(s.heraldry, s.player?.created) { resolvedHeraldry(s) }

    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Panel(accent = Gold) {
            Eyebrow("Coleções Lendárias (3 × 4 obras da Forja)", Gold)
            Text(
                "Coleções temáticas são concluídas pela posse legítima das quatro peças da Forja e concedem títulos exclusivos sem multiplicadores de XP ou Fragmentos. Elas são independentes das três Configurações de Equipamento (Jornada, Conselho e Expedição).",
                color = Muted,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }

        legendaryCollections.forEach { col ->
            val owned = collectionOwnedItems(s, col)
            val missing = collectionMissingItems(s, col)
            val complete = isCollectionComplete(s, col)
            val claimed = collectionRewardClaimed(s, col)

            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(CutCornerShape(8.dp))
                    .border(if (complete) 1.4.dp else 0.9.dp, if (complete) Gold else col.accent.copy(alpha = 0.5f), CutCornerShape(8.dp))
                    .background(Surface)
                    .testTag("collection_card_${col.id}")
            ) {
                Column(Modifier.fillMaxWidth()) {
                    // Thematic painting header
                    Box(Modifier.fillMaxWidth().height(112.dp)) {
                        if (!minimal) {
                            AtlasCellCropped(R.drawable.legendary_collections_atlas, col.paintingAtlasCell, 3, 2, Modifier.matchParentSize())
                        }
                        Box(
                            Modifier
                                .matchParentSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0x660B0E14), Color(0xDD0B0E14))
                                    )
                                )
                        )
                        Row(
                            Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (!minimal) {
                                AtlasCell(
                                    R.drawable.legendary_collections_atlas,
                                    col.medallionAtlasCell,
                                    3,
                                    2,
                                    Modifier.size(54.dp).clip(CircleShape).border(1.2.dp, if (complete) Gold else col.accent, CircleShape)
                                )
                            }
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                    FutharkRuneGlyph(col.rune, if (complete) Gold else col.accent, size = 13.dp)
                                    Text(
                                        col.name,
                                        color = WornIvory,
                                        fontFamily = FontFamily.Serif,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    "${owned.size} / ${col.itemIds.size} peças · Título: ${col.rewardTitle}",
                                    color = if (complete) Gold else Color(0xFFD2C7B5),
                                    fontSize = 12.sp
                                )
                            }
                            if (complete) {
                                HeraldicCrest(heraldry, size = 34.dp)
                            }
                        }
                    }

                    // Body: progress bar, description, pieces list, shortcut/claim
                    Column(
                        Modifier.fillMaxWidth().padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Meter(owned.size / col.itemIds.size.toFloat(), if (complete) Gold else col.accent, slim = true)
                        Text(col.description, color = Ink, fontSize = 13.sp, lineHeight = 18.sp)
                        Text(col.loreNote, color = Muted, fontSize = 11.sp, lineHeight = 16.sp)

                        col.itemIds.forEach { itemId ->
                            val item = shopItems.find { it.id == itemId }
                            val recipe = forgeRecipeById(itemId)
                            val asset = s.assets.find { it.id == itemId }
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clip(CutCornerShape(4.dp))
                                    .background(if (dark) Color(0xFF14181F) else CardColor)
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                RelicArt(item?.kind ?: "accessory", if (asset != null) Gold else Muted, Modifier.size(34.dp), itemId, dim = asset == null)
                                Column(Modifier.weight(1f)) {
                                    Text(item?.name ?: itemId, color = Ink, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    Text(
                                        if (asset != null) "Obtida · ${forgeUpgradeStageDef(asset.stage).name}"
                                        else recipe?.requirementLabel ?: "Forja de Darian",
                                        color = if (asset != null) Green else Muted,
                                        fontSize = 10.sp
                                    )
                                }
                                if (asset != null) {
                                    UpgradeStageBadge(asset.stage)
                                } else if (onOpenForgeRecipe != null) {
                                    TextButton({ onOpenForgeRecipe(itemId) }) { Text("Forja", fontSize = 11.sp) }
                                }
                            }
                        }

                        if (complete) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clip(CutCornerShape(5.dp))
                                    .background(Gold.copy(alpha = 0.14f))
                                    .border(0.8.dp, Gold.copy(alpha = 0.6f), CutCornerShape(5.dp))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text("Coleção Concluída · Título '${col.rewardTitle}'", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Text("Recompensa cosmética e narrativa idempotente (0 XP, 0 bônus).", color = Muted, fontSize = 10.sp)
                                }
                                if (!claimed) {
                                    SagaButton(
                                        text = "Reconhecer",
                                        onClick = { vm.run { vm.repo.claimCollectionReward(col.id) } },
                                        style = VikingButtonStyle.CerimonialViking
                                    )
                                } else {
                                    Text("◆ Selado", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        } else if (missing.isNotEmpty() && onOpenForgeRecipe != null) {
                            SagaButton(
                                text = "Ir para próxima receita na Forja",
                                onClick = { onOpenForgeRecipe(missing.first()) },
                                modifier = Modifier.fillMaxWidth(),
                                style = VikingButtonStyle.FerroRunico
                            )
                        }
                    }
                }
            }
        }
    }
}
