package br.com.ascend.aetheris

import androidx.compose.foundation.*
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
            includeMimirReviews = includeMimir
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
            includeMimirReviews = includeMimir
        )
    }

    val weekCapacity = remember(s, todayDate) {
        OdinPlanningEngine.simulateCapacity(s, CapacityPeriodKind.NEXT_7_DAYS, todayDate)
    }
    val replanItems = remember(s, todayDate) {
        OdinPlanningEngine.generateReplanProposal(s, todayDate)
    }

    val tabs = listOf("Salão", "Proposta", "Capacidade", "Replanejar", "Eisenhower", "Horários")

    LazyColumn(
        Modifier.fillMaxSize().testTag("odin_council_list"),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            OdinHallHeroBanner(
                title = "Mesa de Odin",
                subtitle = "Planejamento estratégico determinístico, 100% offline e orientado à sua disponibilidade real.",
                activeStrategyName = "${activeStrategy.name} · ${activeStrategy.title}"
            )
        }
        item {
            AetherisTabs(tabs, tab, { tab = it })
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
    val todayBlocks = remember(s.timeBlocks, todayDate) { s.timeBlocks.filter { it.date == todayDate }.sortedBy { it.startTime } }
    val todayEvents = remember(s.agendaEvents, todayDate) { s.agendaEvents.filter { !it.archived && it.date == todayDate }.sortedBy { it.startTime } }
    val dueMimir = remember(s, todayDate) { dueFlashcards(s, todayDate) }
    val tasksById = remember(s.pathTasks) { s.pathTasks.associateBy { it.id } }
    val blockedTasks = remember(s.pathTasks, s.taskDependencies) {
        s.pathTasks.filter { !it.completed && !it.archived && OdinPlanningEngine.blockingPrerequisites(it.id, tasksById, s.taskDependencies).isNotEmpty() }
    }
    val nextDeadlines = remember(s.pathTasks, todayDate) {
        s.pathTasks.filter { !it.completed && !it.archived && it.deadline.isNotBlank() }
            .sortedBy { it.deadline }.take(4)
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // KPIs do Conselho
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CouncilKpiTile(
                label = "Capacidade 7d",
                value = if (weekCapacity.status == CapacityStatus.UNCONFIGURED) "—" else "${weekCapacity.utilizationPercent}%",
                detail = weekCapacity.status.label,
                accent = when (weekCapacity.status) {
                    CapacityStatus.OVERLOADED -> MaterialTheme.colorScheme.error
                    CapacityStatus.TIGHT -> Gold
                    CapacityStatus.UNCONFIGURED -> Muted
                    else -> Green
                },
                modifier = Modifier.weight(1f)
            )
            CouncilKpiTile(
                label = "Hoje",
                value = "${todayBlocks.size + todayEvents.size}",
                detail = "${todayBlocks.size} blocos · ${todayEvents.size} eventos",
                accent = Blue,
                modifier = Modifier.weight(1f)
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CouncilKpiTile(
                label = "Replanejar",
                value = "${replanItems.size}",
                detail = if (replanItems.isEmpty()) "Agenda em dia" else "itens aguardando decisão",
                accent = if (replanItems.isEmpty()) Green else Gold,
                modifier = Modifier.weight(1f)
            )
            CouncilKpiTile(
                label = "Mímir",
                value = "${dueMimir.size}",
                detail = if (dueMimir.isEmpty()) "Sem revisões hoje" else "runas para revisar",
                accent = Gold,
                modifier = Modifier.weight(1f)
            )
        }

        // Atalhos rápidos estratégicos
        CouncilMaterialSurface(CouncilMaterial.NordicWood) {
            Eyebrow("Centro de comando estratégico")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SagaButton("Gerar plano", { onGoTab(1) }, secondary = false)
                SagaButton("Disponibilidade", { onGoTab(5) }, secondary = true)
                SagaButton("Forja do Tempo", onOpenTimeForge, secondary = true)
                SagaButton("Conselho dos 9", onOpenCouncilNine, secondary = true)
                SagaButton("Caminhos 3.0", onOpenPaths, secondary = true)
                SagaButton("Agenda 3.0", onOpenAgenda, secondary = true)
            }
            if (!OdinPlanningEngine.hasConfiguredAvailability(s)) {
                Text(
                    "Nenhuma janela semanal configurada ainda. O motor nunca presume 24h livres — defina seus horários reais para destravar propostas e capacidade.",
                    color = Gold,
                    fontSize = 12.sp
                )
                SagaButton("Aplicar horários base (Seg–Sáb)", { vm.run { vm.repo.applyDefaultAvailability() } }, Modifier.fillMaxWidth(), secondary = true)
            }
        }

        // Estratégia ativa
        CouncilMaterialSurface(CouncilMaterial.HammeredBronze) {
            Eyebrow("Estratégias de Odin (apenas pesos de planejamento)")
            Text("Alteram prioridades, margem entre blocos e carga alvo — nunca alteram XP, moedas ou classes.", color = Muted, fontSize = 12.sp)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                odinStrategies.chunked(2).forEach { pair ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        pair.forEach { st ->
                            val selected = st.id == activeStrategy.id
                            val shape = CutCornerShape(6.dp)
                            Row(
                                Modifier
                                    .weight(1f)
                                    .clip(shape)
                                    .background(if (selected) Gold.copy(alpha = 0.16f) else Surface.copy(alpha = 0.55f))
                                    .border(1.dp, if (selected) Gold else ColdIron.copy(alpha = 0.4f), shape)
                                    .clickable { vm.run { vm.repo.setOdinStrategy(st.id) } }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OdinStrategyMedal(st, Modifier.size(40.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(st.name, color = if (selected) Gold else Ink, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 1)
                                    Text(st.title, color = Muted, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Plano de hoje
        CouncilMaterialSurface(CouncilMaterial.DarkIron) {
            Eyebrow("Plano de hoje · ${displayDate(todayDate)}")
            if (todayBlocks.isEmpty() && todayEvents.isEmpty()) {
                Text("Nenhum bloco ou compromisso agendado para hoje.", color = Muted, fontSize = 13.sp)
            } else {
                todayEvents.forEach { ev ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        RunicCalendarGlyph(col = 0, row = 1, modifier = Modifier.size(20.dp))
                        Column(Modifier.weight(1f)) {
                            Text(ev.title, color = Ink, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            Text("${ev.kind} · ${ev.startTime.ifBlank { "Dia todo" }}${if (ev.endTime.isNotBlank()) "–${ev.endTime}" else ""}", color = Muted, fontSize = 12.sp)
                        }
                    }
                }
                todayBlocks.forEach { blk ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        RunicCalendarGlyph(col = 2, row = 0, modifier = Modifier.size(20.dp))
                        Column(Modifier.weight(1f)) {
                            Text(blk.title, color = Ink, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            Text("${blk.startTime}–${blk.endTime} · ${blk.category}${if (blk.pinned) " · Fixado" else ""}${if (blk.sourcePlanId.isNotBlank()) " · Odin" else ""}", color = Muted, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Prazos próximos, bloqueios e áreas prioritárias
        CouncilMaterialSurface(CouncilMaterial.AgedVellum) {
            Eyebrow("Prazos, dependências e revisões")
            if (nextDeadlines.isEmpty()) {
                Text("Nenhuma tarefa com prazo definido no momento.", color = Muted, fontSize = 13.sp)
            } else {
                nextDeadlines.forEach { t ->
                    val overdue = t.deadline < todayDate
                    Text(
                        "• ${t.title} — ${displayDate(t.deadline)}${if (overdue) " (atrasada)" else ""}",
                        color = if (overdue) MaterialTheme.colorScheme.error else Ink,
                        fontSize = 13.sp
                    )
                }
            }
            if (blockedTasks.isNotEmpty()) {
                Text("${blockedTasks.size} tarefa(s) bloqueada(s) por dependências em Caminhos 3.0.", color = Gold, fontSize = 12.sp)
            }
            if (dueMimir.isNotEmpty()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("${dueMimir.size} flashcard(s) aguardando revisão em Mímir.", color = Blue, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    TextButton(onOpenMimir) { Text("Revisar", color = Gold) }
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
        CouncilMaterialSurface(CouncilMaterial.NordicWood) {
            Eyebrow("Simulação de plano · ${displayPeriod(proposal.dateFrom, proposal.dateTo)}")
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
                CouncilKpiTile("Disponível", "${proposal.totalAvailableMinutes} min", modifier = Modifier.weight(1f), accent = Blue)
                CouncilKpiTile("Proposto", "${proposal.totalSuggestedMinutes} min", "${proposal.suggestions.size} blocos", modifier = Modifier.weight(1f), accent = Gold)
            }
            if (proposal.usedHistoryPreference && proposal.preferredHistoryBucket != null) {
                val label = when (proposal.preferredHistoryBucket) {
                    "morning" -> "manhã"
                    "afternoon" -> "tarde"
                    else -> "noite"
                }
                Text("Preferência suave aplicada a partir do histórico real de foco: período da $label.", color = Green, fontSize = 12.sp)
            }

            // Comparação opcional de duas estratégias
            Text("Comparar com outra estratégia", color = Muted, fontSize = 12.sp)
            val compOptions = listOf("Nenhuma") + odinStrategies.filter { it.id != previewStrategyId }.map { it.name }
            val compIds = listOf("") + odinStrategies.filter { it.id != previewStrategyId }.map { it.id }
            ChipRow(compOptions, compIds.indexOf(compareStrategyId).coerceAtLeast(0), { onCompareStrategy(compIds[it]) })

            compareProposal?.let { cp ->
                CouncilMaterialSurface(CouncilMaterial.DarkIron, accent = Blue) {
                    Eyebrow("Comparativo: ${proposal.strategy.name} × ${cp.strategy.name}", Blue)
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
                Eyebrow("Conflitos e restrições detectadas", MaterialTheme.colorScheme.error)
                proposal.conflicts.forEach { c ->
                    Text("• $c", color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }
                if (!OdinPlanningEngine.hasConfiguredAvailability(s)) {
                    SagaButton("Configurar disponibilidade", onGoAvailability, Modifier.fillMaxWidth(), secondary = true)
                }
            }
        }

        // Lista de sugestões com justificativa explicável, seleção parcial e fixação
        CouncilMaterialSurface(CouncilMaterial.ProjectLeather) {
            Eyebrow("Blocos propostos ($selectedCount/${proposal.suggestions.size} selecionados)")
            if (proposal.suggestions.isEmpty()) {
                Text("Nenhuma sugestão pôde ser encaixada neste período.", color = Muted, fontSize = 13.sp)
            } else {
                proposal.suggestions.forEach { sug ->
                    val checked = sug.id !in deselectedIds
                    val isPinned = sug.id in pinnedIds
                    val shape = CutCornerShape(6.dp)
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clip(shape)
                            .background(if (checked) Surface.copy(alpha = 0.72f) else Surface.copy(alpha = 0.3f))
                            .border(1.dp, if (isPinned) Gold else ColdIron.copy(alpha = 0.35f), shape)
                            .padding(10.dp),
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
                                Text(sug.title, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("${displayDate(sug.date)} · ${sug.startTime}–${sug.endTime} (${sug.minutes} min) · ${sug.category}", color = Blue, fontSize = 12.sp)
                            }
                            TextButton({
                                pinnedIds = if (isPinned) pinnedIds - sug.id else pinnedIds + sug.id
                            }) {
                                Text(if (isPinned) "Fixado" else "Fixar", color = if (isPinned) Gold else Muted, fontSize = 12.sp)
                            }
                        }
                        Text("Motivo: ${sug.reason}", color = Muted, fontSize = 11.sp)
                    }
                }
                SagaButton(
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
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !applying && selectedCount > 0
                )
            }
        }

        // Itens não encaixados — transparência obrigatória!
        if (proposal.unscheduled.isNotEmpty()) {
            CouncilMaterialSurface(CouncilMaterial.AgedVellum, accent = Gold) {
                Eyebrow("Itens não alocados (${proposal.unscheduled.size})", Gold)
                Text("A Mesa de Odin nunca esconde itens que excedem sua disponibilidade real.", color = Muted, fontSize = 12.sp)
                proposal.unscheduled.take(10).forEach { u ->
                    Text("• ${u.title} (${u.remainingMinutes} min) — ${u.reason}", color = Ink, fontSize = 12.sp)
                }
            }
        }

        // Histórico de planos aplicados com desfazer inteligente
        if (activeAppliedPlans.isNotEmpty() || undoReport != null) {
            CouncilMaterialSurface(CouncilMaterial.DarkIron) {
                Eyebrow("Planos aplicados recentemente")
                undoReport?.let { Text(it, color = Green, fontSize = 12.sp) }
                activeAppliedPlans.forEach { rec ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text(rec.summary, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text("${displayPeriod(rec.dateFrom, rec.dateTo)}", color = Muted, fontSize = 11.sp)
                        }
                        TextButton({
                            vm.run {
                                val (removed, preserved) = vm.repo.undoAppliedPlan(rec.id)
                                onUndoReport("Desfeito: $removed bloco(s) removido(s); $preserved bloco(s) editados/fixados preservado(s).")
                            }
                        }) {
                            Text("Desfazer", color = Gold)
                        }
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

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        CouncilMaterialSurface(CouncilMaterial.NordicWood) {
            Eyebrow("Simulador de capacidade")
            ChipRow(CapacityPeriodKind.entries.map { it.label }, periodIdx, { periodIdx = it })
            if (kind == CapacityPeriodKind.CUSTOM) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.weight(1f)) { FormField(customFrom, { customFrom = it }, "Início (AAAA-MM-DD)") }
                    Box(Modifier.weight(1f)) { FormField(customTo, { customTo = it }, "Fim (AAAA-MM-DD)") }
                }
            }
            Text("Período analisado: ${displayPeriod(sim.dateFrom, sim.dateTo)}", color = Muted, fontSize = 12.sp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CouncilKpiTile("Disponível", "${sim.configuredAvailableMinutes} min", "Livre: ${sim.freeCapacityMinutes} min", Blue, Modifier.weight(1f))
                CouncilKpiTile(
                    "Saldo",
                    "${if (sim.balanceMinutes >= 0) "+" else ""}${sim.balanceMinutes} min",
                    sim.status.label,
                    if (sim.balanceMinutes < 0) MaterialTheme.colorScheme.error else Green,
                    Modifier.weight(1f)
                )
            }
            Meter((sim.utilizationPercent.coerceAtMost(100) / 100f), if (sim.balanceMinutes < 0) MaterialTheme.colorScheme.error else Gold)
            Text(
                "Compromissos fixos: ${sim.fixedEventsMinutes} min · Blocos já agendados: ${sim.existingBlocksMinutes} min\n" +
                    "Demanda pendente não alocada: ${sim.totalUnallocatedDemandMinutes} min " +
                    "(tarefas: ${sim.pendingTasksDemandMinutes} · hábitos: ${sim.habitsDemandMinutes} · Mímir: ${sim.mimirReviewDemandMinutes})",
                color = Muted,
                fontSize = 12.sp
            )
            if (sim.status == CapacityStatus.UNCONFIGURED) {
                SagaButton("Configurar horários disponíveis", onConfigureAvailability, Modifier.fillMaxWidth(), secondary = true)
            }
        }

        if (sim.warnings.isNotEmpty() || sim.unestimatedTasks.isNotEmpty() || sim.incompatibleDeadlineTasks.isNotEmpty()) {
            CouncilMaterialSurface(CouncilMaterial.AgedVellum, accent = Gold) {
                Eyebrow("Alertas e premissas explícitas", Gold)
                sim.warnings.forEach { w -> Text("• $w", color = Ink, fontSize = 13.sp) }
                if (sim.unestimatedTasks.isNotEmpty()) {
                    Text("Tarefas sem duração estimada:", color = Gold, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    sim.unestimatedTasks.take(6).forEach { t -> Text("  – ${t.title}", color = Muted, fontSize = 12.sp) }
                }
                if (sim.incompatibleDeadlineTasks.isNotEmpty()) {
                    Text("Prazos incompatíveis com a capacidade:", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    sim.incompatibleDeadlineTasks.take(6).forEach { t -> Text("  – ${t.title} (${displayDate(t.deadline)})", color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
                }
            }
        }
    }
}

@Composable
private fun AdaptiveReplanSection(s: GameState, vm: GameViewModel, items: List<ReplanItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CouncilMaterialSurface(CouncilMaterial.NordicWood) {
            Eyebrow("Replanejamento adaptativo")
            Text(
                "Itens passados ou não concluídos nunca são alterados automaticamente e nunca geram penalidades no RPG. Escolha a ação desejada para cada item.",
                color = Muted,
                fontSize = 13.sp
            )
        }
        if (items.isEmpty()) {
            CouncilMaterialSurface(CouncilMaterial.DarkIron, accent = Green) {
                Text("Nenhum item atrasado ou bloco pendente para replanejar.", color = Green, fontWeight = FontWeight.Medium)
            }
        } else {
            items.forEach { item ->
                CouncilMaterialSurface(CouncilMaterial.ProjectLeather) {
                    Text(item.title, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Text("${item.reason} · ${item.minutes} min", color = Muted, fontSize = 12.sp)
                    item.options.forEach { opt ->
                        SagaButton(
                            text = "${opt.action.label}: ${opt.summary}",
                            onClick = { vm.run { vm.repo.applyReplanChoice(item, opt) } },
                            modifier = Modifier.fillMaxWidth(),
                            secondary = opt.action != ReplanActionKind.TRANSFER
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EisenhowerMatrixSection(s: GameState, vm: GameViewModel) {
    val grouped = remember(s.pathTasks) { OdinPlanningEngine.groupByEisenhower(s.pathTasks) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CouncilMaterialSurface(CouncilMaterial.AgedVellum) {
            Eyebrow("Matriz de Eisenhower")
            Text(
                "Classificação baseada exclusivamente nos critérios explícitos de Importância e Urgência definidos por você em cada tarefa.",
                color = Muted,
                fontSize = 12.sp
            )
        }
        EisenhowerQuadrant.entries.forEach { q ->
            val tasks = grouped[q].orEmpty()
            val accent = when (q) {
                EisenhowerQuadrant.Q1_DO -> MaterialTheme.colorScheme.error
                EisenhowerQuadrant.Q2_PLAN -> Gold
                EisenhowerQuadrant.Q3_DELEGATE -> Blue
                EisenhowerQuadrant.Q4_ELIMINATE -> Muted
            }
            CouncilMaterialSurface(CouncilMaterial.DarkIron, accent = accent) {
                Eyebrow("${q.title} · ${q.subtitle} (${tasks.size})", accent)
                if (tasks.isEmpty()) {
                    Text("Nenhuma tarefa classificada neste quadrante.", color = Muted, fontSize = 12.sp)
                } else {
                    tasks.take(8).forEach { t ->
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Column(Modifier.weight(1f)) {
                                Text(t.title, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                if (t.deadline.isNotBlank()) Text("Prazo: ${displayDate(t.deadline)}", color = Muted, fontSize = 11.sp)
                            }
                            TextButton({ vm.run { vm.repo.setTaskEisenhower(t.id, !t.important, t.urgent) } }) {
                                Text(if (t.important) "Imp ★" else "Imp ☆", color = if (t.important) Gold else Muted, fontSize = 11.sp)
                            }
                            TextButton({ vm.run { vm.repo.setTaskEisenhower(t.id, t.important, !t.urgent) } }) {
                                Text(if (t.urgent) "Urg !" else "Urg ·", color = if (t.urgent) MaterialTheme.colorScheme.error else Muted, fontSize = 11.sp)
                            }
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
            Eyebrow("Janelas semanais autorizadas")
            Text("A Mesa de Odin só aloca atividades dentro das janelas que você autorizar abaixo.", color = Muted, fontSize = 12.sp)
            if (s.availabilityWindows.isEmpty()) {
                SagaButton("Preencher semana padrão (Seg–Sáb)", { vm.run { vm.repo.applyDefaultAvailability() } }, Modifier.fillMaxWidth())
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
                secondary = true
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
            Eyebrow("Exceções por data (feriados, folgas ou janelas extras)")
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
            }, Modifier.fillMaxWidth(), secondary = true)
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
            Eyebrow("Limites e preferências flexíveis")
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
            }, Modifier.fillMaxWidth())
        }
    }
}
