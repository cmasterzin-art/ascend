package br.com.ascend.aetheris

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*

/**
 * 1.7.3 — Módulo II: A Forja de Darian.
 *
 * Estende a construção existente `A Forja` (`building:1`) em uma oficina funcional de fabricação
 * cosmética com 8 receitas originais, usando exclusivamente Fragmentos do ledger existente
 * (zero nova moeda, zero XP, zero atributos de combate).
 */
@Composable
fun ForgeScreen(
    s: GameState,
    vm: GameViewModel,
    onBackToVillage: () -> Unit = {}
) {
    val hero = requireNotNull(s.player)
    val stage = forgeStage(s)
    val isMaster = isForgeMasterSpecialization(s)
    val dialogue = remember(s.assets, hero.specialization, hero.heroClass) { darianWorkshopDialogue(s) }
    val minimal = LocalAppearance.current.minimal

    var forgeSection by remember { mutableIntStateOf(0) } // 0=Fabricar, 1=Aprimorar, 2=Coleções
    var previewRecipeId by remember { mutableStateOf(forgeRecipes.first().id) }
    var filterTab by remember { mutableIntStateOf(0) } // 0=Todas, 1=Prontas, 2=Forjadas, 3=Bloqueadas
    var confirmCraft by remember { mutableStateOf<ForgeRecipe?>(null) }
    var confirmUpgrade by remember { mutableStateOf<Pair<ForgeRecipe, Int>?>(null) }
    var confirmRestore by remember { mutableStateOf(false) }

    val previewRecipe = forgeRecipeById(previewRecipeId) ?: forgeRecipes.first()
    val craftedCount = s.assets.count { it.id in forgeItemIds }

    val dark = LocalAppearance.current.dark
    Page {
        // Cabeçalho pictórico da Oficina de Darian
        Column(
            Modifier
                .fillMaxWidth()
                .clip(CutCornerShape(8.dp))
                .border(1.dp, if (isMaster) Gold.copy(alpha = 0.78f) else OldBronze.copy(alpha = 0.55f), CutCornerShape(8.dp))
                .background(Surface)
        ) {
            if (!minimal) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 148.dp)
                ) {
                    AtlasCellCropped(
                        resId = R.drawable.darian_forge_atlas,
                        index = stage.coerceIn(0, 2), // 0=ruínas, 1=parcial, 2=restaurada
                        columns = 2,
                        rows = 2,
                        modifier = Modifier.matchParentSize()
                    )
                    Box(
                        Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    0f to Color(0xFF0A0D12).copy(alpha = 0.28f),
                                    0.45f to Color(0xFF0A0D12).copy(alpha = 0.60f),
                                    1f to Color(0xFF0A0D12).copy(alpha = 0.92f)
                                )
                            )
                    )
                    Row(
                        Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 46.dp, bottom = 12.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FutharkRuneGlyph(HistoricalFutharkRune.Fehu, Gold, size = 14.dp)
                                Eyebrow("A Forja de Darian · Etapa $stage/2", Gold)
                            }
                            Text(
                                "Oficina de Fabricação Cosmética",
                                style = MaterialTheme.typography.headlineSmall,
                                color = WornIvory
                            )
                        }
                        Text("◈ ${s.coins}", color = Gold, fontFamily = SagaFont, fontSize = 20.sp, softWrap = false)
                    }
                }
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (minimal) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FutharkRuneGlyph(HistoricalFutharkRune.Fehu, Gold, size = 14.dp)
                                Eyebrow("A Forja de Darian · Etapa $stage/2", Gold)
                            }
                            Text(
                                "Oficina de Fabricação Cosmética",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Ink
                            )
                        }
                        Text("◈ ${s.coins}", color = Gold, fontFamily = SagaFont, fontSize = 20.sp, softWrap = false)
                    }
                }

                // Retrato e fala de Darian (com selo e diálogo exclusivos para Mestre da Forja, sem desconto econômico)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(CutCornerShape(6.dp))
                        .background(if (dark) Night.copy(alpha = 0.48f) else Night.copy(alpha = 0.06f))
                        .border(1.dp, if (isMaster) Gold.copy(alpha = 0.68f) else ColdIron.copy(alpha = 0.4f), CutCornerShape(6.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!minimal) {
                        AtlasCellCropped(
                            resId = R.drawable.darian_forge_atlas,
                            index = 3, // Retrato de Darian na oficina
                            columns = 2,
                            rows = 2,
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CutCornerShape(5.dp))
                                .border(1.dp, OldBronze.copy(alpha = 0.7f), CutCornerShape(5.dp))
                        )
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                dialogue.first,
                                color = Gold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (isMaster) {
                                ArchetypeSealArt(8, Modifier.size(20.dp))
                            }
                        }
                        Text(
                            "“${dialogue.second}”",
                            fontFamily = FontFamily.Serif,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = Ink
                        )
                        if (isMaster) {
                            Text(
                                "Selo Mestre da Forja · Reconhecimento narrativo e estético (mesmo custo em Fragmentos).",
                                color = Blue,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Estado de restauração (preserva estritamente as 2 etapas existentes de building:1)
                if (stage < 2) {
                    val nextCost = (stage + 1) * 30
                    Panel(accent = if (stage == 0) Gold else Blue) {
                        Eyebrow(
                            if (stage == 0) "Forja ainda em ruínas · Etapa 0/2" else "Forja parcialmente restaurada · Etapa 1/2",
                            if (stage == 0) Gold else Blue
                        )
                        Text(
                            if (stage == 0) "Restaure a Etapa 1 de A Forja na Vila do Despertar (◈ 30) para despertar o fole e fabricar as peças iniciais."
                            else "Receitas iniciais já podem ser forjadas e gravadas. Conclua a Etapa 2 (◈ 60) para obras épicas, lendárias e consagrações.",
                            color = Muted,
                            fontSize = 12.sp
                        )
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SagaButton(
                                text = "Restaurar Etapa ${stage + 1} · ◈ $nextCost",
                                onClick = { confirmRestore = true },
                                modifier = Modifier.weight(1f),
                                enabled = s.coins >= nextCost,
                                style = VikingButtonStyle.BronzeForjado
                            )
                            SagaButton(
                                text = "Vila",
                                onClick = onBackToVillage,
                                style = VikingButtonStyle.FerroRunico
                            )
                        }
                    }
                } else {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Bigorna e brasas plenamente restauradas",
                            color = Green,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "$craftedCount / ${forgeRecipes.size} forjadas",
                            color = Gold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            softWrap = false
                        )
                    }
                }
            }
        }

        // Navegação compacta da Forja: Fabricar (12) · Aprimorar · Coleções (3)
        AetherisTabs(
            values = listOf("Fabricar (${forgeRecipes.size})", "Aprimorar", "Coleções (${legendaryCollections.size})"),
            selected = forgeSection,
            onSelect = { forgeSection = it }
        )

        if (forgeSection == 2) {
            LegendaryCollectionsSection(
                s = s,
                vm = vm,
                onOpenForgeRecipe = { itemId ->
                    previewRecipeId = itemId
                    forgeSection = 0
                }
            )
        } else if (forgeSection == 1) {
            // Aprimoramento cosmético em 3 estágios: I Forjado -> II Gravado -> III Consagrado
            Panel(accent = Gold) {
                Eyebrow("Aprimoramentos da Bigorna (3 estágios cosméticos)", Gold)
                Text(
                    "Forjado → Gravado → Consagrado. Sem aleatoriedade, sem pular estágios e sem bônus de XP ou combate.",
                    color = Muted,
                    fontSize = 12.sp
                )
            }
            val ownedForgeRecipes = forgeRecipes.filter { r -> s.assets.any { it.id == r.id } }
            if (ownedForgeRecipes.isEmpty()) {
                Empty("Forje ao menos uma peça na aba Fabricar para gravar e consagrar seu acabamento.")
            } else {
                ownedForgeRecipes.forEach { recipe ->
                    val asset = s.assets.first { it.id == recipe.id }
                    val currentStage = asset.stage.coerceIn(FORGE_STAGE_FORGED, FORGE_STAGE_CONSECRATED)
                    val nextStage = (currentStage + 1).takeIf { it <= FORGE_STAGE_CONSECRATED }
                    val nextCost = nextStage?.let { forgeUpgradeCost(recipe, it) } ?: 0
                    val missingReasons = nextStage?.let { forgeUpgradeMissingReasons(s, recipe, it) } ?: emptyList()
                    val canUpgrade = nextStage != null && canUpgradeForgeItem(s, recipe, nextStage)
                    val color = rarityColor(recipe.rarity)

                    AetherSurface(
                        SurfaceKind.Artifact,
                        Modifier
                            .testTag("forge_upgrade_${recipe.id}")
                            .border(
                                width = if (currentStage == FORGE_STAGE_CONSECRATED) 1.5.dp else 1.dp,
                                color = if (currentStage == FORGE_STAGE_CONSECRATED) Gold else color.copy(alpha = 0.5f),
                                shape = CutCornerShape(6.dp)
                            )
                    ) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(Modifier.size(72.dp), contentAlignment = Alignment.Center) {
                                RelicArt(recipe.kind, color, Modifier.matchParentSize(), itemId = recipe.id)
                                if (currentStage >= FORGE_STAGE_ENGRAVED && !minimal) {
                                    AtlasCell(R.drawable.forge_consacration_atlas, 3, 4, 2, Modifier.matchParentSize())
                                }
                            }
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    UpgradeStageBadge(currentStage)
                                    RarityCrestBadge(recipe.rarity, color)
                                }
                                Text(recipe.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(forgeUpgradeStageDef(currentStage).description, color = Muted, fontSize = 11.sp)
                            }
                        }
                        Text(upgradeVisualScopeFor(recipe.id), color = Blue, fontSize = 11.sp, lineHeight = 16.sp)
                        if (nextStage == null) {
                            Text("◆ Obra plenamente Consagrada — estágio máximo alcançado.", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        } else {
                            val nextDef = forgeUpgradeStageDef(nextStage)
                            Text("Próximo estágio: ${nextDef.shortLabel} · ◈ $nextCost Fragmentos · 0 XP", color = Gold, fontSize = 12.sp)
                            if (missingReasons.isNotEmpty()) {
                                missingReasons.forEach { r -> Text("• $r", color = Muted, fontSize = 11.sp) }
                            } else if (s.coins < nextCost) {
                                Text("Faltam ${nextCost - s.coins} Fragmentos de Aether.", color = Blue, fontSize = 11.sp)
                            }
                            SagaButton(
                                text = "Aprimorar para ${nextDef.name} · ◈ $nextCost",
                                onClick = { confirmUpgrade = recipe to nextStage },
                                enabled = canUpgrade,
                                modifier = Modifier.fillMaxWidth().testTag("upgrade_btn_${recipe.id}_$nextStage"),
                                style = if (nextStage == FORGE_STAGE_CONSECRATED) VikingButtonStyle.CerimonialViking else VikingButtonStyle.BronzeForjado,
                                leadingRune = recipe.rune
                            )
                        }
                    }
                }
            }
        } else {
            // Bancada de prévia ao vivo no Retrato Vivo (usando Oficina dos Artífices como fundo)
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(CutCornerShape(8.dp))
                    .border(1.dp, OldBronze.copy(alpha = 0.45f), CutCornerShape(8.dp))
                    .background(Surface)
            ) {
                if (!minimal) {
                    AtlasCellCropped(
                        resId = R.drawable.hero_background_atlas,
                        index = 3, // Oficina dos Artífices
                        columns = 2,
                        rows = 2,
                        modifier = Modifier.matchParentSize(),
                        alpha = if (dark) 0.25f else 0.14f
                    )
                }
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(if (dark) Color(0xFF0D1116).copy(alpha = 0.78f) else Surface.copy(alpha = 0.88f))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FutharkRuneGlyph(previewRecipe.rune, Gold, size = 13.dp)
                            Eyebrow("Prévia na Bigorna · ${previewRecipe.name}", Gold)
                        }
                        RarityCrestBadge(previewRecipe.rarity, rarityColor(previewRecipe.rarity))
                    }
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LivingHeroPortrait(
                            p = hero,
                            s = s,
                            size = 112.dp,
                            previewOverrides = mapOf(previewRecipe.kind to previewRecipe.id)
                        )
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(previewRecipe.lore, color = if (dark) WornIvory.copy(alpha = 0.92f) else Ink, fontSize = 12.sp, lineHeight = 18.sp)
                            Text("Materiais: ${previewRecipe.narrativeMaterials}", color = Muted, fontSize = 11.sp, lineHeight = 16.sp)
                            collectionForItem(previewRecipe.id)?.let { col ->
                                Text("Coleção: ${col.name}", color = col.accent, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                            Text("Requisitos: ${previewRecipe.requirementLabel}", color = Blue, fontSize = 11.sp)
                            Text("Custo: ◈ ${previewRecipe.cost} Fragmentos · 0 XP (cosmético)", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            VikingKnotDivider(variant = 7, alpha = 0.72f, height = 14.dp)

            // Catálogo das 12 receitas
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Eyebrow("Moldes da Forja (${forgeRecipes.size})", Gold)
                Text("Sem nova moeda · Sem venda na Loja", color = Muted, fontSize = 11.sp)
            }

            ChipRow(
                values = listOf("Todas (${forgeRecipes.size})", "Prontas", "Forjadas", "Bloqueadas"),
                selected = filterTab,
                onSelect = { filterTab = it }
            )

            val visibleRecipes = forgeRecipes.filter { r ->
                val st = forgeRecipeState(s, r)
                when (filterTab) {
                    1 -> st == ForgeRecipeState.Available
                    2 -> st == ForgeRecipeState.Crafted
                    3 -> st == ForgeRecipeState.Locked || st == ForgeRecipeState.InsufficientFragments
                    else -> true
                }
            }

            if (visibleRecipes.isEmpty()) {
                Empty("Nenhuma receita neste filtro. Confira os requisitos de restauração da Forja, nível ou conquistas.")
            }

            visibleRecipes.forEach { recipe ->
                val state = forgeRecipeState(s, recipe)
                val missing = forgeRecipeMissingReasons(s, recipe)
                val color = rarityColor(recipe.rarity)
                val isPreviewed = previewRecipeId == recipe.id
                val ownedAsset = s.assets.find { it.id == recipe.id }
                val col = collectionForItem(recipe.id)

                AetherSurface(
                    SurfaceKind.Artifact,
                    Modifier
                        .testTag("forge_recipe_${recipe.id}")
                        .border(
                            width = if (isPreviewed) 1.6.dp else 1.dp,
                            color = when (state) {
                                ForgeRecipeState.Crafted -> Green.copy(alpha = 0.7f)
                                ForgeRecipeState.Available -> Gold.copy(alpha = 0.78f)
                                else -> color.copy(alpha = 0.35f)
                            },
                            shape = CutCornerShape(6.dp)
                        )
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(72.dp), contentAlignment = Alignment.Center) {
                            RelicArt(
                                kind = recipe.kind,
                                color = color,
                                modifier = Modifier.matchParentSize(),
                                itemId = recipe.id,
                                dim = state == ForgeRecipeState.Locked
                            )
                            if (ownedAsset != null && ownedAsset.stage >= FORGE_STAGE_ENGRAVED && !minimal) {
                                AtlasCell(R.drawable.forge_consacration_atlas, 3, 4, 2, Modifier.matchParentSize())
                            }
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RarityCrestBadge(recipe.rarity, color)
                                if (ownedAsset != null) {
                                    UpgradeStageBadge(ownedAsset.stage)
                                } else {
                                    Text(
                                        text = state.label,
                                        color = when (state) {
                                            ForgeRecipeState.Crafted -> Green
                                            ForgeRecipeState.Available -> Gold
                                            ForgeRecipeState.InsufficientFragments -> Blue
                                            ForgeRecipeState.Locked -> Muted
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                FutharkRuneGlyph(recipe.rune, color, size = 12.dp)
                                Text(
                                    recipe.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                "${equipmentSlots.find { it.first == recipe.kind }?.second ?: recipe.kind} · ◈ ${recipe.cost} Fragmentos${if (col != null) " · ${col.name}" else ""}",
                                color = Gold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Text(recipe.lore, color = Muted, fontSize = 12.sp, lineHeight = 18.sp)
                    Text("Materiais: ${recipe.narrativeMaterials}", color = Muted.copy(alpha = 0.85f), fontSize = 11.sp)

                    if (missing.isNotEmpty()) {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .clip(CutCornerShape(4.dp))
                                .background(Color.Black.copy(alpha = 0.22f))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            missing.forEach { reason ->
                                Text("• $reason", color = Muted, fontSize = 11.sp)
                            }
                        }
                    } else if (state == ForgeRecipeState.InsufficientFragments) {
                        Text(
                            "Faltam ${recipe.cost - s.coins} Fragmentos de Aether para forjar esta peça.",
                            color = Blue,
                            fontSize = 11.sp
                        )
                    }

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SagaButton(
                            text = if (isPreviewed) "Na prévia" else "Provar",
                            onClick = { previewRecipeId = recipe.id },
                            modifier = Modifier.weight(1f),
                            style = VikingButtonStyle.FerroRunico
                        )
                        when {
                            ownedAsset != null -> {
                                SagaButton(
                                    text = if (ownedAsset.equipped) "Equipado" else "Equipar",
                                    onClick = { vm.run { vm.repo.equipItem(ownedAsset.id) } },
                                    modifier = Modifier.weight(1f),
                                    enabled = !ownedAsset.equipped,
                                    style = VikingButtonStyle.FerroRunico
                                )
                            }
                            else -> {
                                SagaButton(
                                    text = "Forjar · ◈ ${recipe.cost}",
                                    onClick = { confirmCraft = recipe },
                                    modifier = Modifier.weight(1f).testTag("craft_btn_${recipe.id}"),
                                    enabled = state == ForgeRecipeState.Available,
                                    style = VikingButtonStyle.BronzeForjado,
                                    leadingRune = recipe.rune
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirmação explícita de fabricação cosmética
    confirmCraft?.let { recipe ->
        AetherDialog(
            onDismissRequest = { confirmCraft = null },
            title = "Forjar ${recipe.name}?",
            kind = SurfaceKind.Ceremonial,
            seal = SealKind.Reward,
            confirmButton = {
                SagaButton(
                    text = "Confirmar Forja",
                    onClick = {
                        vm.run {
                            vm.repo.craftForgeItem(recipe.id)
                            confirmCraft = null
                        }
                    },
                    style = VikingButtonStyle.BronzeForjado
                )
            },
            dismissButton = {
                TextButton({ confirmCraft = null }) { Text("Cancelar") }
            }
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                LivingHeroPortrait(
                    p = hero,
                    s = s,
                    size = 112.dp,
                    previewOverrides = mapOf(recipe.kind to recipe.id)
                )
            }
            Text(
                "Custo único: ◈ ${recipe.cost} Fragmentos de Aether (saldo atual: ◈ ${s.coins}).",
                color = Gold,
                fontSize = 13.sp
            )
            Text(
                "A peça entra na sua coleção no estágio I · Forjado. Fabricação puramente cosmética (0 XP, sem atributos de combate) e irreversível.",
                color = Muted,
                fontSize = 12.sp
            )
        }
    }

    // Confirmação explícita de aprimoramento cosmético (Estágio II Gravado / III Consagrado)
    confirmUpgrade?.let { (recipe, targetStage) ->
        val nextDef = forgeUpgradeStageDef(targetStage)
        val cost = forgeUpgradeCost(recipe, targetStage)
        AetherDialog(
            onDismissRequest = { confirmUpgrade = null },
            title = "${nextDef.name}: ${recipe.name}?",
            kind = SurfaceKind.Ceremonial,
            seal = SealKind.Reward,
            confirmButton = {
                SagaButton(
                    text = "Confirmar · ◈ $cost",
                    onClick = {
                        vm.run {
                            vm.repo.upgradeForgeItem(recipe.id, targetStage)
                            confirmUpgrade = null
                        }
                    },
                    style = VikingButtonStyle.CerimonialViking
                )
            },
            dismissButton = {
                TextButton({ confirmUpgrade = null }) { Text("Cancelar") }
            }
        ) {
            Text(nextDef.description, color = Ink, fontSize = 13.sp)
            Text(upgradeVisualScopeFor(recipe.id), color = Blue, fontSize = 12.sp)
            Text(
                "Custo determinístico: ◈ $cost Fragmentos de Aether · 0 XP · sem aleatoriedade.",
                color = Gold,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }

    if (confirmRestore && stage < 2) {
        val nextCost = (stage + 1) * 30
        AetherDialog(
            onDismissRequest = { confirmRestore = false },
            title = "Restaurar A Forja (Etapa ${stage + 1})?",
            kind = SurfaceKind.Ceremonial,
            seal = SealKind.Reward,
            confirmButton = {
                SagaButton(
                    text = "Restaurar · ◈ $nextCost",
                    onClick = {
                        vm.run {
                            vm.repo.restore(1)
                            confirmRestore = false
                        }
                    },
                    style = VikingButtonStyle.BronzeForjado
                )
            },
            dismissButton = { TextButton({ confirmRestore = false }) { Text("Cancelar") } }
        ) {
            Text("Esta etapa custa $nextCost Fragmentos de Aether e atualiza A Forja na Vila do Despertar.", color = Muted)
        }
    }
}
