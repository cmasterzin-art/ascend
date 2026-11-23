package br.com.ascend.aetheris

import androidx.compose.foundation.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import java.time.LocalDate

/**
 * ASCEND 1.7.5 — A Mesa de Odin
 * Telas do Salão do Conselho:
 * 1. OdinCouncilScreen (tabs: Salão, Proposta, Capacidade, Replanejar, Eisenhower, Disponibilidade)
 * Com visualização completa, edição/fixação antes de aplicar, comparação de estratégias,
 * aplicação transacional e reversão consciente de edições posteriores.
 */

private val weekdayFullNames = listOf("Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado", "Domingo")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OdinCouncilScreen(
    s: GameState,
    vm: GameViewModel,
    initialTab: Int = 0,
    onOpenAgenda: () -> Unit = {},
    onOpenPaths: () -> Unit = {},
    onOpenCouncilNine: () -> Unit = {},
    onOpenTimeForge: () -> Unit = {},
    onOpenMimir: () -> Unit = {}
) {
    var tab by rememberSaveable(initialTab) { mutableIntStateOf(initialTab.coerceIn(0, 5)) }
    val todayDate = currentDay()
    val config = remember(s.planningConfig) { resolvedPlanningConfig(s) }
    val activeStrategy = remember(config.strategyId) { odinStrategyById(config.strategyId) }

    // Estado de proposta independente para visualização e comparação
    var proposalDays by rememberSaveable { mutableIntStateOf(7) }
    var previewStrategyId by rememberSaveable(config.strategyId) { mutableStateOf(config.strategyId) }
    var compareStrategyId by rememberSaveable { mutableStateOf("") }
    var includeHabits by rememberSaveable { mutableStateOf(true) }
    var includeMimir by rememberSaveable { mutableStateOf(true) }
    var undoReport by remember { mutableStateOf<String?>(null) }

    val toDate = remember(todayDate, proposalDays) {
        LocalDate.parse(todayDate).plusDays((proposalDays - 1).coerceAtLeast(0).toLong()).toString()
    }

    val proposal = remember(s, todayDate, toDate, previewStrategyId, config, includeHabits, includeMimir) {
        OdinPlanningEngine.generateOdinProposal(
            s = s,
            fromDate = todayDate,
            toDate = toDate,
            strategyId = previewStrategyId,
            config = config,
            includeHabits = includeHabits,
            includeMimirReviews = includeMimir,
            clock = PlanningClock.fromClock()
        )
    }

    val compareProposal = remember(s, todayDate, toDate, compareStrategyId, config, includeHabits, includeMimir) {
        if (compareStrategyId.isBlank()) null
        else OdinPlanningEngine.generateOdinProposal(
            s = s,
            fromDate = todayDate,
            toDate = toDate,
            strategyId = compareStrategyId,
            config = config,
            includeHabits = includeHabits,
            includeMimirReviews = includeMimir,
            clock = PlanningClock.fromClock()
        )
    }

    val weekCapacity = remember(s, todayDate) {
        OdinPlanningEngine.simulateCapacity(s, CapacityPeriodKind.NEXT_7_DAYS, todayDate)
    }
    val replanItems = remember(s, todayDate) {
        OdinPlanningEngine.generateReplanProposal(s, todayDate, PlanningClock.fromClock())
    }

    val tabs = listOf("Salão", "Proposta", "Capacidade", "Replanejar", "Eisenhower", "Horários")

    val nb = LocalNorthborn.current
    LazyColumn(
        Modifier.fillMaxSize().background(nb.canvas).testTag("odin_council_list"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            if (tab == 0) RebirthMasthead("Mesa de Odin", "Estratégia ativa · ${activeStrategy.name}", R.drawable.rebirth_chart)
            RebirthIndex(tabs, tab) { tab = it }
        }

        when (tab) {
            0 -> item {
                OdinHallOverviewSection(
                    s = s,
                    vm = vm,
                    todayDate = todayDate,
                    config = config,
                    activeStrategy = activeStrategy,
                    weekCapacity = weekCapacity,
                    replanItems = replanItems,
                    onGoTab = { tab = it },
                    onOpenAgenda = onOpenAgenda,
                    onOpenPaths = onOpenPaths,
                    onOpenCouncilNine = onOpenCouncilNine,
                    onOpenTimeForge = onOpenTimeForge,
                    onOpenMimir = onOpenMimir
                )
            }
            1 -> item {
                OdinProposalSection(
                    s = s,
                    vm = vm,
                    proposal = proposal,
                    compareProposal = compareProposal,
                    proposalDays = proposalDays,
                    onProposalDays = { proposalDays = it },
                    previewStrategyId = previewStrategyId,
                    onPreviewStrategy = { previewStrategyId = it },
                    compareStrategyId = compareStrategyId,
                    onCompareStrategy = { compareStrategyId = it },
                    includeHabits = includeHabits,
                    onIncludeHabits = { includeHabits = it },
                    includeMimir = includeMimir,
                    onIncludeMimir = { includeMimir = it },
                    undoReport = undoReport,
                    onUndoReport = { undoReport = it },
                    onGoAvailability = { tab = 5 }
                )
            }
            2 -> item {
                CapacitySimulatorSection(s = s, todayDate = todayDate, onConfigureAvailability = { tab = 5 })
            }
            3 -> item {
                AdaptiveReplanSection(s = s, vm = vm, items = replanItems)
            }
            4 -> item {
                EisenhowerMatrixSection(s = s, vm = vm)
            }
            5 -> item {
                AvailabilityAndConfigSection(s = s, vm = vm, config = config)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OdinHallOverviewSection(
    s: GameState,
    vm: GameViewModel,
    todayDate: String,
    config: PlanningConfig,
    activeStrategy: OdinStrategyDef,
    weekCapacity: CapacitySimulation,
    replanItems: List<ReplanItem>,
    onGoTab: (Int) -> Unit,
    onOpenAgenda: () -> Unit,
    onOpenPaths: () -> Unit,
    onOpenCouncilNine: () -> Unit,
    onOpenTimeForge: () -> Unit,
    onOpenMimir: () -> Unit
) {
    val nb = LocalNorthborn.current
    // Preserva projeções virtuais recorrentes da Etapa I no resumo diário da Mesa de Odin
    val todayBlocks = remember(s, todayDate) { OdinPlanningEngine.effectiveBlocksForDate(s, todayDate).sortedBy { it.startTime } }
    val todayEvents = remember(s, todayDate) { OdinPlanningEngine.effectiveEventsForDate(s, todayDate).sortedBy { it.startTime } }
    val dueMimir = remember(s, todayDate) { dueFlashcards(s, todayDate) }
    val tasksById = remember(s.pathTasks) { s.pathTasks.associateBy { it.id } }
    val blockedTasks = remember(s.pathTasks, s.taskDependencies) {
        s.pathTasks.filter { !it.completed && !it.archived && OdinPlanningEngine.blockingPrerequisites(it.id, tasksById, s.taskDependencies).isNotEmpty() }
    }
    val nextDeadlines = remember(s.pathTasks, todayDate) {
        s.pathTasks.filter { !it.completed && !it.archived && it.deadline.isNotBlank() }
            .sortedBy { it.deadline }.take(4)
    }

    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Capacidade 7d", color = nb.inkSecondary, style = MaterialTheme.typography.labelLarge)
                    Text(
                        if (weekCapacity.status == CapacityStatus.UNCONFIGURED) "—" else "${weekCapacity.utilizationPercent}%",
                        color = nb.inkPrimary, style = MaterialTheme.typography.displaySmall
                    )
                    Text(weekCapacity.status.label, color = nb.inkSecondary, fontSize = 13.sp)
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Hoje", style = MaterialTheme.typography.titleMedium, color = nb.inkPrimary)
                    Text("${todayBlocks.size} ${if (todayBlocks.size == 1) "bloco" else "blocos"} · " +
                        "${todayEvents.size} ${if (todayEvents.size == 1) "evento" else "eventos"}", color = nb.inkSecondary, fontSize = 13.sp)
                    Text("${replanItems.size} para replanejar · ${dueMimir.size} ${if (dueMimir.size == 1) "revisão" else "revisões"}", color = nb.inkSecondary, fontSize = 13.sp)
                }
            }
            Meter(weekCapacity.utilizationPercent.toFloat() / 100f, nb.accentPatina, slim = true)
            RebirthAction("Gerar plano", { onGoTab(1) }, Modifier.fillMaxWidth(), glyph = NorthbornGlyph.Odin)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                RebirthAction("Forja do Tempo", onOpenTimeForge, primary = false)
                RebirthAction("Disponibilidade", { onGoTab(5) }, primary = false)
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                NordicCompactAction("Conselho dos 9", onOpenCouncilNine, accent = nb.accentBronze)
                NordicCompactAction("Caminhos 3.0", onOpenPaths, accent = nb.inkSecondary)
                NordicCompactAction("Agenda 3.0", onOpenAgenda, accent = nb.inkSecondary)
            }
            if (!OdinPlanningEngine.hasConfiguredAvailability(s)) {
                NorthbornSurface(role = NorthbornSurfaceRole.Informative, accent = nb.accentBronze) {
                    Text(
                        "Nenhuma janela semanal configurada ainda. O motor nunca presume 24h livres — defina seus horários reais para destravar propostas e capacidade.",
                        color = nb.accentBronze,
                        fontSize = 12.5.sp
                    )
                    NorthbornButton(
                        "Aplicar horários base (Seg–Sáb)",
                        { vm.run { vm.repo.applyDefaultAvailability() } },
                        role = NorthbornButtonRole.Confirm,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Column {
            RebirthRule()
            NordicSectionHeader(
                eyebrow = "Estratégias de Odin (apenas pesos de planejamento)",
                subtitle = "Alteram prioridades, margem entre blocos e carga alvo."
            )
            odinStrategies.forEach { strategy ->
                val selected = strategy.id == activeStrategy.id
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 56.dp)
                        .selectable(selected, role = Role.RadioButton) { vm.run { vm.repo.setOdinStrategy(strategy.id) } }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OdinStrategyMedal(strategy, Modifier.size(34.dp))
                    Column(Modifier.weight(1f)) {
                        Text(strategy.name, style = MaterialTheme.typography.titleMedium, color = nb.inkPrimary)
                        Text(strategy.title, color = nb.inkSecondary, fontSize = 12.sp)
                    }
                    if (selected) Text("Ativa", color = nb.accentPatina, fontSize = 12.sp)
                }
                RebirthRule()
            }
        }

        // Plano de hoje em formato editorial aberto (sem empilhar outro cartão pesado)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            NorthbornDivider(NorthbornDividerStyle.DiamondKnot)
            NorthbornEditorialHeader("Plano de hoje · ${displayDate(todayDate)}", kicker = "Leitura do dia")
            if (todayBlocks.isEmpty() && todayEvents.isEmpty()) {
                Text("Nenhum bloco ou compromisso agendado para hoje.", color = nb.inkMuted, fontSize = 13.sp)
            } else {
                todayEvents.forEach { ev ->
                    val isRec = ev.recurrenceId.isNotBlank() || "@rec:" in ev.id
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(NorthbornShape.sm)
                            .background(nb.surfaceBase.copy(alpha = 0.72f))
                            .border(1.dp, nb.hairline, NorthbornShape.sm)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        NordicPlanningMark(
                            mark = when {
                                ev.completed -> PlanningMark.PlanCompleted
                                isRec -> PlanningMark.Recurring
                                else -> PlanningMark.Deadline
                            },
                            modifier = Modifier.size(20.dp),
                            tintFallback = nb.accentFjord
                        )
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(ev.title, color = nb.inkPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            Text(
                                "${ev.kind} · ${ev.startTime.ifBlank { "Dia todo" }}${if (ev.endTime.isNotBlank()) "–${ev.endTime}" else ""}${if (isRec) " · Recorrente" else ""}",
                                color = nb.inkSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                todayBlocks.forEach { blk ->
                    val isRec = blk.recurrenceId.isNotBlank() || "@rec:" in blk.id
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(NorthbornShape.sm)
                            .background(nb.surfaceBase.copy(alpha = 0.72f))
                            .border(1.dp, if (blk.pinned) nb.accentBronze.copy(alpha = 0.5f) else nb.hairline, NorthbornShape.sm)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        NordicPlanningMark(
                            mark = when {
                                blk.pinned -> PlanningMark.Pinned
                                isRec -> PlanningMark.Recurring
                                blk.sourcePlanId.isNotBlank() -> PlanningMark.CriticalTask
                                else -> PlanningMark.Milestone
                            },
                            modifier = Modifier.size(20.dp),
                            tintFallback = nb.accentBronze
                        )
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(blk.title, color = nb.inkPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            Text(
                                "${blk.startTime}–${blk.endTime} · ${blk.category}${if (blk.pinned) " · Fixado" else ""}${if (blk.sourcePlanId.isNotBlank()) " · Odin" else ""}${if (isRec) " · Recorrente" else ""}",
                                color = nb.inkSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Prazos próximos, bloqueios e revisões em seção documental limpa
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            NorthbornDivider(NorthbornDividerStyle.Hairline)
            NorthbornEditorialHeader("Prazos, dependências e revisões", kicker = "Vigilância")
            if (nextDeadlines.isEmpty()) {
                Text("Nenhuma tarefa com prazo definido no momento.", color = nb.inkMuted, fontSize = 13.sp)
            } else {
                nextDeadlines.forEach { t ->
                    val overdue = t.deadline < todayDate
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NordicPlanningMark(
                            mark = if (overdue) PlanningMark.Conflict else PlanningMark.Deadline,
                            modifier = Modifier.size(16.dp),
                            tintFallback = if (overdue) nb.accentEmber else nb.accentBronze
                        )
                        Text(
                            "${t.title} — ${displayDate(t.deadline)}${if (overdue) " (atrasada)" else ""}",
                            color = if (overdue) nb.accentEmber else nb.inkPrimary,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            if (blockedTasks.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NordicPlanningMark(PlanningMark.Dependency, Modifier.size(16.dp))
                    Text("${blockedTasks.size} tarefa(s) bloqueada(s) por dependências em Caminhos 3.0.", color = nb.accentBronze, fontSize = 12.sp)
                }
            }
            if (dueMimir.isNotEmpty()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("${dueMimir.size} flashcard(s) aguardando revisão em Mímir.", color = nb.accentFjord, fontSize = 12.5.sp, modifier = Modifier.weight(1f))
                    NordicCompactAction("Revisar", onOpenMimir, accent = nb.accentBronze, rune = HistoricalFutharkRune.Ansuz)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OdinProposalSection(
    s: GameState,
    vm: GameViewModel,
    proposal: OdinProposal,
    compareProposal: OdinProposal?,
    proposalDays: Int,
    onProposalDays: (Int) -> Unit,
    previewStrategyId: String,
    onPreviewStrategy: (String) -> Unit,
    compareStrategyId: String,
    onCompareStrategy: (String) -> Unit,
    includeHabits: Boolean,
    onIncludeHabits: (Boolean) -> Unit,
    includeMimir: Boolean,
    onIncludeMimir: (Boolean) -> Unit,
    undoReport: String?,
    onUndoReport: (String?) -> Unit,
    onGoAvailability: () -> Unit
) {
    // Controle de seleção parcial e fixação individual antes de aplicar
    var deselectedIds by remember(proposal) { mutableStateOf(emptySet<String>()) }
    var pinnedIds by remember(proposal) { mutableStateOf(emptySet<String>()) }
    var applying by remember { mutableStateOf(false) }

    val selectedCount = proposal.suggestions.count { it.id !in deselectedIds }
    val activeAppliedPlans = remember(s.appliedPlans) { s.appliedPlans.filter { !it.undone }.take(3) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            NordicSectionHeader("Simulação de plano · ${displayPeriod(proposal.dateFrom, proposal.dateTo)}", rune = HistoricalFutharkRune.Ansuz)
            ChipRow(
                listOf("Hoje", "3 dias", "7 dias", "14 dias"),
                listOf(1, 3, 7, 14).indexOf(proposalDays).coerceAtLeast(2),
                { onProposalDays(listOf(1, 3, 7, 14)[it]) }
            )
            Text("Estratégia da prévia", color = Muted, fontSize = 12.sp)
            ChipRow(
                odinStrategies.map { it.name },
                odinStrategies.indexOfFirst { it.id == previewStrategyId }.coerceAtLeast(0),
                { onPreviewStrategy(odinStrategies[it].id) }
            )
            Toggle("Incluir hábitos programados", includeHabits, onIncludeHabits)
            Toggle("Incluir revisões pendentes de Mímir", includeMimir, onIncludeMimir)

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CouncilKpiTile("Disponível", "${proposal.totalAvailableMinutes} min", modifier = Modifier.weight(1f), accent = Blue, rune = HistoricalFutharkRune.Jera)
                CouncilKpiTile("Proposto", "${proposal.totalSuggestedMinutes} min", "${proposal.suggestions.size} blocos", modifier = Modifier.weight(1f), accent = Gold, rune = HistoricalFutharkRune.Tiwaz)
            }
            if (proposal.usedHistoryPreference && proposal.preferredHistoryBucket != null) {
                val label = when (proposal.preferredHistoryBucket) {
                    "morning" -> "manhã"
                    "afternoon" -> "tarde"
                    else -> "noite"
                }
                Text("Preferência suave aplicada a partir do histórico real de foco: período da $label.", color = Green, fontSize = 12.sp)
            }

            NordicStrategyDivider(StrategyFrameAsset.UrnesDivider, HistoricalFutharkRune.Sowilo)

            // Comparação opcional de duas estratégias
            Text("Comparar com outra estratégia", color = Muted, fontSize = 12.sp)
            val compOptions = listOf("Nenhuma") + odinStrategies.filter { it.id != previewStrategyId }.map { it.name }
            val compIds = listOf("") + odinStrategies.filter { it.id != previewStrategyId }.map { it.id }
            ChipRow(compOptions, compIds.indexOf(compareStrategyId).coerceAtLeast(0), { onCompareStrategy(compIds[it]) })

            compareProposal?.let { cp ->
                CouncilMaterialSurface(CouncilMaterial.DarkIron, accent = Blue) {
                    NordicSectionHeader("Comparativo: ${proposal.strategy.name} × ${cp.strategy.name}", rune = HistoricalFutharkRune.Gebo, accent = Blue)
                    Text(
                        "${proposal.strategy.name}: ${proposal.suggestions.size} blocos (${proposal.totalSuggestedMinutes} min) · ${proposal.unscheduled.size} não alocados\n" +
                            "${cp.strategy.name}: ${cp.suggestions.size} blocos (${cp.totalSuggestedMinutes} min) · ${cp.unscheduled.size} não alocados",
                        color = Ink,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Conflitos explícitos (nunca ocultos)
        if (proposal.conflicts.isNotEmpty()) {
            CouncilMaterialSurface(CouncilMaterial.DarkIron, accent = MaterialTheme.colorScheme.error) {
                NordicSectionHeader("Conflitos e restrições detectadas", mark = PlanningMark.Conflict, accent = MaterialTheme.colorScheme.error)
                proposal.conflicts.forEach { c ->
                    Text("• $c", color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }
                if (!OdinPlanningEngine.hasConfiguredAvailability(s)) {
                    SagaButton("Configurar disponibilidade", onGoAvailability, Modifier.fillMaxWidth(), style = VikingButtonStyle.FerroRunico)
                }
            }
        }

        // Lista de sugestões aberta (sem caixa-em-caixa) com justificativa explicável, seleção parcial e fixação
        val nbProp = LocalNorthborn.current
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            NorthbornDivider(NorthbornDividerStyle.DiamondKnot)
            NorthbornEditorialHeader(
                title = "Blocos propostos ($selectedCount/${proposal.suggestions.size} selecionados)",
                kicker = "Proposta"
            )
            if (proposal.suggestions.isEmpty()) {
                Text("Nenhuma sugestão pôde ser encaixada neste período.", color = nbProp.inkMuted, fontSize = 13.sp)
            } else {
                proposal.suggestions.forEach { sug ->
                    val checked = sug.id !in deselectedIds
                    val isPinned = sug.id in pinnedIds
                    val shape = NorthbornShape.md
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clip(shape)
                            .background(if (checked) nbProp.surfaceBase else nbProp.surfaceBase.copy(alpha = 0.45f))
                            .border(if (isPinned) 1.4.dp else 1.dp, if (isPinned) nbProp.accentBronze else nbProp.hairline, shape)
                            .padding(11.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = { on ->
                                    deselectedIds = if (on) deselectedIds - sug.id else deselectedIds + sug.id
                                }
                            )
                            Column(Modifier.weight(1f)) {
                                Text(sug.title, color = nbProp.inkPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("${displayDate(sug.date)} · ${sug.startTime}–${sug.endTime} (${sug.minutes} min) · ${sug.category}", color = nbProp.accentFjord, fontSize = 12.sp)
                            }
                            NordicCompactAction(
                                text = if (isPinned) "Fixado" else "Fixar",
                                onClick = { pinnedIds = if (isPinned) pinnedIds - sug.id else pinnedIds + sug.id },
                                accent = if (isPinned) nbProp.accentBronze else nbProp.inkSecondary,
                                rune = if (isPinned) HistoricalFutharkRune.Isa else null
                            )
                        }
                        Text("Motivo: ${sug.reason}", color = nbProp.inkSecondary, fontSize = 11.5.sp)
                    }
                }
                NorthbornButton(
                    text = if (selectedCount == proposal.suggestions.size) "Aplicar plano completo ($selectedCount)" else "Aplicar seleção parcial ($selectedCount)",
                    onClick = {
                        val chosen = proposal.suggestions.filter { it.id !in deselectedIds }.map { it.id }.toSet()
                        applying = true
                        vm.run {
                            try {
                                vm.repo.applyOdinProposal(proposal, chosen, pinnedIds)
                            } finally {
                                applying = false
                            }
                        }
                    },
                    role = NorthbornButtonRole.Ceremonial,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !applying && selectedCount > 0,
                    leadingIcon = NorthbornGlyph.Check
                )
            }
        }

        // Itens não encaixados — transparência obrigatória!
        if (proposal.unscheduled.isNotEmpty()) {
            CouncilMaterialSurface(CouncilMaterial.AgedVellum, accent = nbProp.accentBronze) {
                NordicSectionHeader(
                    eyebrow = "Itens não alocados (${proposal.unscheduled.size})",
                    subtitle = "A Mesa de Odin nunca esconde itens que excedem sua disponibilidade real.",
                    mark = PlanningMark.Conflict,
                    accent = nbProp.accentBronze
                )
                proposal.unscheduled.take(10).forEach { u ->
                    Text("• ${u.title} (${u.remainingMinutes} min) — ${u.reason}", color = nbProp.inkPrimary, fontSize = 12.sp)
                }
            }
        }

        // Histórico de planos aplicados com desfazer inteligente
        if (activeAppliedPlans.isNotEmpty() || undoReport != null) {
            CouncilMaterialSurface(CouncilMaterial.DarkIron) {
                NordicSectionHeader("Planos aplicados recentemente", mark = PlanningMark.PlanCompleted, accent = nbProp.accentPatina)
                undoReport?.let { Text(it, color = nbProp.accentPatina, fontSize = 12.sp) }
                activeAppliedPlans.forEach { rec ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text(rec.summary, color = nbProp.inkPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text("${displayPeriod(rec.dateFrom, rec.dateTo)}", color = nbProp.inkMuted, fontSize = 11.sp)
                        }
                        NordicCompactAction(
                            text = "Desfazer",
                            onClick = {
                                vm.run {
                                    val (removed, preserved) = vm.repo.undoAppliedPlan(rec.id)
                                    onUndoReport("Desfeito: $removed bloco(s) removido(s); $preserved bloco(s) editados/fixados preservado(s).")
                                }
                            },
                            accent = nbProp.accentBronze
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CapacitySimulatorSection(
    s: GameState,
    todayDate: String,
    onConfigureAvailability: () -> Unit
) {
    var periodIdx by rememberSaveable { mutableIntStateOf(1) }
    var customFrom by rememberSaveable { mutableStateOf(todayDate) }
    var customTo by rememberSaveable { mutableStateOf(LocalDate.parse(todayDate).plusDays(13).toString()) }
    val kind = CapacityPeriodKind.entries[periodIdx.coerceIn(CapacityPeriodKind.entries.indices)]
    val sim = remember(s, kind, todayDate, customFrom, customTo) {
        OdinPlanningEngine.simulateCapacity(s, kind, todayDate, customFrom, customTo)
    }
    val nb = LocalNorthborn.current

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        CouncilMaterialSurface(CouncilMaterial.NordicWood) {
            NordicSectionHeader("Simulador de capacidade", rune = HistoricalFutharkRune.Jera)
            ChipRow(CapacityPeriodKind.entries.map { it.label }, periodIdx, { periodIdx = it })
            if (kind == CapacityPeriodKind.CUSTOM) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.weight(1f)) { FormField(customFrom, { customFrom = it }, "Início (AAAA-MM-DD)") }
                    Box(Modifier.weight(1f)) { FormField(customTo, { customTo = it }, "Fim (AAAA-MM-DD)") }
                }
            }
            Text("Período analisado: ${displayPeriod(sim.dateFrom, sim.dateTo)}", color = nb.inkSecondary, fontSize = 12.sp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CouncilKpiTile("Disponível", "${sim.configuredAvailableMinutes} min", "Livre: ${sim.freeCapacityMinutes} min", nb.accentFjord, Modifier.weight(1f), rune = HistoricalFutharkRune.Dagaz)
                CouncilKpiTile(
                    "Saldo",
                    "${if (sim.balanceMinutes >= 0) "+" else ""}${sim.balanceMinutes} min",
                    sim.status.label,
                    if (sim.balanceMinutes < 0) nb.accentEmber else nb.accentPatina,
                    Modifier.weight(1f),
                    rune = HistoricalFutharkRune.Sowilo
                )
            }
            Meter((sim.utilizationPercent.coerceAtMost(100) / 100f), if (sim.balanceMinutes < 0) nb.accentEmber else nb.accentBronze)
            Text(
                "Compromissos fixos: ${sim.fixedEventsMinutes} min · Blocos já agendados: ${sim.existingBlocksMinutes} min\n" +
                    "Demanda pendente não alocada: ${sim.totalUnallocatedDemandMinutes} min " +
                    "(tarefas: ${sim.pendingTasksDemandMinutes} · hábitos: ${sim.habitsDemandMinutes} · Mímir: ${sim.mimirReviewDemandMinutes})",
                color = nb.inkSecondary,
                fontSize = 12.sp
            )
            if (sim.status == CapacityStatus.UNCONFIGURED) {
                SagaButton("Configurar horários disponíveis", onConfigureAvailability, Modifier.fillMaxWidth(), style = VikingButtonStyle.BronzeForjado)
            }
        }

        if (sim.warnings.isNotEmpty() || sim.unestimatedTasks.isNotEmpty() || sim.incompatibleDeadlineTasks.isNotEmpty()) {
            CouncilMaterialSurface(CouncilMaterial.AgedVellum, accent = nb.accentBronze) {
                NordicSectionHeader("Alertas e premissas explícitas", mark = PlanningMark.Conflict, accent = nb.accentBronze)
                sim.warnings.forEach { w -> Text("• $w", color = nb.inkPrimary, fontSize = 13.sp) }
                if (sim.unestimatedTasks.isNotEmpty()) {
                    NordicStrategyDivider(StrategyFrameAsset.VellumDivider)
                    Text("Tarefas sem duração estimada:", color = nb.accentBronze, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    sim.unestimatedTasks.take(6).forEach { t -> Text("  – ${t.title}", color = nb.inkSecondary, fontSize = 12.sp) }
                }
                if (sim.incompatibleDeadlineTasks.isNotEmpty()) {
                    NordicStrategyDivider(StrategyFrameAsset.VellumDivider)
                    Text("Prazos incompatíveis com a capacidade:", color = nb.accentEmber, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    sim.incompatibleDeadlineTasks.take(6).forEach { t -> Text("  – ${t.title} (${displayDate(t.deadline)})", color = nb.accentEmber, fontSize = 12.sp) }
                }
            }
        }
    }
}

@Composable
private fun AdaptiveReplanSection(s: GameState, vm: GameViewModel, items: List<ReplanItem>) {
    val nb = LocalNorthborn.current
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        NorthbornEditorialHeader(
            title = "Replanejamento adaptativo",
            kicker = "Autonomia",
            subtitle = "Itens passados ou não concluídos nunca são alterados automaticamente e nunca geram penalidades no RPG. Escolha a ação desejada para cada item."
        )
        if (items.isEmpty()) {
            NorthbornSurface(role = NorthbornSurfaceRole.Informative, accent = nb.accentPatina) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    NordicPlanningMark(PlanningMark.PlanCompleted, Modifier.size(22.dp), tintFallback = nb.accentPatina)
                    Text("Nenhum item atrasado ou bloco pendente para replanejar.", color = nb.accentPatina, fontWeight = FontWeight.Medium)
                }
            }
        } else {
            items.forEach { item ->
                NorthbornSurface(role = NorthbornSurfaceRole.Operational) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NordicPlanningMark(PlanningMark.Deadline, Modifier.size(20.dp))
                        Column(Modifier.weight(1f)) {
                            Text(item.title, color = nb.inkPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Text("${item.reason} · ${item.minutes} min", color = nb.inkSecondary, fontSize = 12.sp)
                        }
                    }
                    item.options.forEach { opt ->
                        val btnStyle = when (opt.action) {
                            ReplanActionKind.TRANSFER -> VikingButtonStyle.BronzeForjado
                            ReplanActionKind.SPLIT, ReplanActionKind.POSTPONE -> VikingButtonStyle.FerroRunico
                            ReplanActionKind.SEND_TO_PENDING -> VikingButtonStyle.CompactoRunico
                            ReplanActionKind.REMOVE_SCHEDULE -> VikingButtonStyle.Destrutivo
                        }
                        SagaButton(
                            text = "${opt.action.label}: ${opt.summary}",
                            onClick = { vm.run { vm.repo.applyReplanChoice(item, opt) } },
                            modifier = Modifier.fillMaxWidth(),
                            style = btnStyle
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EisenhowerMatrixSection(s: GameState, vm: GameViewModel) {
    val nb = LocalNorthborn.current
    val grouped = remember(s.pathTasks) { OdinPlanningEngine.groupByEisenhower(s.pathTasks) }
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        NorthbornEditorialHeader(
            title = "Matriz de Eisenhower",
            kicker = "Priorização",
            subtitle = "Classificação baseada exclusivamente nos critérios explícitos de Importância e Urgência definidos por você em cada tarefa."
        )
        EisenhowerQuadrant.entries.forEach { q ->
            val tasks = grouped[q].orEmpty()
            val accent = when (q) {
                EisenhowerQuadrant.Q1_DO -> nb.accentEmber
                EisenhowerQuadrant.Q2_PLAN -> nb.accentBronze
                EisenhowerQuadrant.Q3_DELEGATE -> nb.accentFjord
                EisenhowerQuadrant.Q4_ELIMINATE -> nb.inkMuted
            }
            val mark = when (q) {
                EisenhowerQuadrant.Q1_DO -> PlanningMark.CriticalTask
                EisenhowerQuadrant.Q2_PLAN -> PlanningMark.Milestone
                EisenhowerQuadrant.Q3_DELEGATE -> PlanningMark.Deadline
                EisenhowerQuadrant.Q4_ELIMINATE -> PlanningMark.Dependency
            }
            NorthbornSurface(role = NorthbornSurfaceRole.Informative, accent = accent) {
                NordicSectionHeader("${q.title} · ${q.subtitle} (${tasks.size})", mark = mark, accent = accent)
                if (tasks.isEmpty()) {
                    Text("Nenhuma tarefa classificada neste quadrante.", color = nb.inkMuted, fontSize = 12.sp)
                } else {
                    tasks.take(8).forEachIndexed { idx, t ->
                        if (idx > 0) NorthbornDivider(NorthbornDividerStyle.Hairline)
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Column(Modifier.weight(1f)) {
                                Text(t.title, color = nb.inkPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.Medium)
                                if (t.deadline.isNotBlank()) Text("Prazo: ${displayDate(t.deadline)}", color = nb.inkSecondary, fontSize = 11.sp)
                            }
                            NordicCompactAction(
                                text = if (t.important) "Imp ★" else "Imp ☆",
                                onClick = { vm.run { vm.repo.setTaskEisenhower(t.id, !t.important, t.urgent) } },
                                accent = if (t.important) nb.accentBronze else nb.inkMuted
                            )
                            NordicCompactAction(
                                text = if (t.urgent) "Urg !" else "Urg ·",
                                onClick = { vm.run { vm.repo.setTaskEisenhower(t.id, t.important, !t.urgent) } },
                                accent = if (t.urgent) nb.accentEmber else nb.inkMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AvailabilityAndConfigSection(s: GameState, vm: GameViewModel, config: PlanningConfig) {
    var selectedDay by rememberSaveable { mutableIntStateOf(1) }
    var startTime by rememberSaveable { mutableStateOf("09:00") }
    var endTime by rememberSaveable { mutableStateOf("12:00") }
    var label by rememberSaveable { mutableStateOf("Manhã") }

    var exDate by rememberSaveable { mutableStateOf(today()) }
    var exBlocked by rememberSaveable { mutableStateOf(true) }
    var exStart by rememberSaveable { mutableStateOf("10:00") }
    var exEnd by rememberSaveable { mutableStateOf("12:00") }
    var exNote by rememberSaveable { mutableStateOf("") }

    var dailyLimit by remember(config) { mutableStateOf(config.dailyLimitMinutes.toString()) }
    var bufferMin by remember(config) { mutableStateOf(config.bufferMinutes.toString()) }
    var maxBlock by remember(config) { mutableStateOf(config.maxBlockMinutes.toString()) }
    var useHist by remember(config) { mutableStateOf(config.useHistorySoftPreference) }
    var groupSim by remember(config) { mutableStateOf(config.groupSimilar) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        CouncilMaterialSurface(CouncilMaterial.NordicWood) {
            NordicSectionHeader(
                eyebrow = "Janelas semanais autorizadas",
                subtitle = "A Mesa de Odin só aloca atividades dentro das janelas que você autorizar abaixo.",
                rune = HistoricalFutharkRune.Dagaz
            )
            if (s.availabilityWindows.isEmpty()) {
                SagaButton("Preencher semana padrão (Seg–Sáb)", { vm.run { vm.repo.applyDefaultAvailability() } }, Modifier.fillMaxWidth(), style = VikingButtonStyle.Confirmacao)
            }
            ChipRow(listOf("Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom"), selectedDay - 1, { selectedDay = it + 1 })
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.weight(1f)) { FormField(startTime, { startTime = it }, "Início (HH:mm)") }
                Box(Modifier.weight(1f)) { FormField(endTime, { endTime = it }, "Fim (HH:mm)") }
            }
            FormField(label, { label = it }, "Rótulo opcional (ex.: Manhã, Estudo)")
            SagaButton(
                "+ Adicionar janela em ${weekdayFullNames[selectedDay - 1]}",
                {
                    vm.run {
                        vm.repo.saveAvailabilityWindow(
                            AvailabilityWindow(
                                dayOfWeek = selectedDay,
                                startTime = startTime.trim(),
                                endTime = endTime.trim(),
                                label = label.trim().ifBlank { weekdayFullNames[selectedDay - 1] }
                            )
                        )
                    }
                },
                Modifier.fillMaxWidth(),
                style = VikingButtonStyle.FerroRunico
            )
            s.availabilityWindows.sortedWith(compareBy<AvailabilityWindow> { it.dayOfWeek }.thenBy { it.startTime }).forEach { w ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${weekdayFullNames[(w.dayOfWeek - 1).coerceIn(0, 6)]} · ${w.startTime}–${w.endTime} (${w.label})",
                        color = Ink,
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton({ vm.run { vm.repo.deleteAvailabilityWindow(w.id) } }) {
                        Text("Remover", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            }
        }

        CouncilMaterialSurface(CouncilMaterial.DarkIron) {
            NordicSectionHeader("Exceções por data (feriados, folgas ou janelas extras)", rune = HistoricalFutharkRune.Wunjo)
            FormField(exDate, { exDate = it }, "Data (AAAA-MM-DD)")
            Toggle("Dia indisponível (folga/feriado)", exBlocked, { exBlocked = it })
            if (!exBlocked) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.weight(1f)) { FormField(exStart, { exStart = it }, "Início") }
                    Box(Modifier.weight(1f)) { FormField(exEnd, { exEnd = it }, "Fim") }
                }
            }
            FormField(exNote, { exNote = it }, "Observação")
            SagaButton("+ Salvar exceção", {
                vm.run {
                    vm.repo.saveAvailabilityException(
                        AvailabilityException(
                            date = exDate.trim(),
                            blocked = exBlocked,
                            startTime = if (exBlocked) "" else exStart.trim(),
                            endTime = if (exBlocked) "" else exEnd.trim(),
                            note = exNote.trim()
                        )
                    )
                    exNote = ""
                }
            }, Modifier.fillMaxWidth(), style = VikingButtonStyle.FerroRunico)
            s.availabilityExceptions.forEach { ex ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${displayDate(ex.date)} · ${if (ex.blocked) "Indisponível" else "${ex.startTime}–${ex.endTime}"}${if (ex.note.isNotBlank()) " · ${ex.note}" else ""}",
                        color = Ink,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton({ vm.run { vm.repo.deleteAvailabilityException(ex.id) } }) {
                        Text("Remover", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            }
        }

        CouncilMaterialSurface(CouncilMaterial.AgedVellum) {
            NordicSectionHeader("Limites e preferências flexíveis", rune = HistoricalFutharkRune.Isa)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.weight(1f)) { FormField(dailyLimit, { dailyLimit = it }, "Limite diário", true) }
                Box(Modifier.weight(1f)) { FormField(bufferMin, { bufferMin = it }, "Margem (min)", true) }
                Box(Modifier.weight(1f)) { FormField(maxBlock, { maxBlock = it }, "Bloco máx", true) }
            }
            Toggle("Agrupar atividades semelhantes", groupSim, { groupSim = it })
            Toggle("Usar histórico real de foco como preferência suave (mín. 5 sessões)", useHist, { useHist = it })
            SagaButton("Salvar limites da Mesa de Odin", {
                vm.run {
                    vm.repo.savePlanningConfig(
                        config.copy(
                            dailyLimitMinutes = dailyLimit.toIntOrNull() ?: config.dailyLimitMinutes,
                            bufferMinutes = bufferMin.toIntOrNull() ?: config.bufferMinutes,
                            maxBlockMinutes = maxBlock.toIntOrNull() ?: config.maxBlockMinutes,
                            groupSimilar = groupSim,
                            useHistorySoftPreference = useHist
                        )
                    )
                }
            }, Modifier.fillMaxWidth(), style = VikingButtonStyle.Confirmacao)
        }
    }
}
