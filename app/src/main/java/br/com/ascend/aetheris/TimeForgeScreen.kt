package br.com.ascend.aetheris

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay

/**
 * ASCEND 1.7.5 — A Forja do Tempo (Foco 3.0)
 * 5 modos (Foco livre, Pomodoro, Concentração profunda, Contagem regressiva, Ciclos personalizados),
 * separação rigorosa entre foco real e pausas (pausas nunca somam minutos de foco),
 * vínculo direto com blocos da Agenda, modo Imersivo Nórdico opcional e 3 sons ambientes offline.
 */

@Composable
fun TimeForgeScreen(
    s: GameState,
    vm: GameViewModel,
    initialImmersive: Boolean = false,
    initialBlockId: String = "",
    onOpenHistory: () -> Unit = {}
) {
    val context = LocalContext.current
    val immersivePref by vm.immersiveFocus.collectAsStateWithLifecycle()
    val ambientSceneId by vm.ambientSceneId.collectAsStateWithLifecycle()
    val ambientVolume by vm.ambientVolume.collectAsStateWithLifecycle()
    val notifyFocusEnd by vm.notifyFocusEnd.collectAsStateWithLifecycle()
    val notifyBreakEnd by vm.notifyBreakEnd.collectAsStateWithLifecycle()
    val notifyNextBlock by vm.notifyNextBlock.collectAsStateWithLifecycle()
    val notifyUpcomingEvent by vm.notifyUpcomingEvent.collectAsStateWithLifecycle()
    val reducedMotion = LocalAppearance.current.reducedMotion

    var immersiveMode by rememberSaveable(initialImmersive) { mutableStateOf(initialImmersive || immersivePref) }
    var selectedModeId by rememberSaveable { mutableStateOf("pomodoro") }
    val modeSpec = remember(selectedModeId) { focusModeSpec(selectedModeId) }

    var workMinText by rememberSaveable(selectedModeId) { mutableStateOf(modeSpec.defaultWorkMinutes.toString()) }
    var shortBreakText by rememberSaveable(selectedModeId) { mutableStateOf(modeSpec.defaultShortBreak.toString()) }
    var longBreakText by rememberSaveable(selectedModeId) { mutableStateOf(modeSpec.defaultLongBreak.toString()) }
    var cyclesText by rememberSaveable(selectedModeId) { mutableStateOf(modeSpec.defaultCycles.toString()) }
    var autoAdvance by rememberSaveable { mutableStateOf(false) }
    var selectedBlockId by rememberSaveable(initialBlockId) { mutableStateOf(initialBlockId) }
    var customLabel by rememberSaveable { mutableStateOf("") }

    val focus = s.focus
    var tick by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(focus?.startedAt, focus?.phase, focus?.currentCycle, focus?.autoAdvance) {
        if (focus != null && focus.startedAt > 0L) {
            val mono0 = runCatching { android.os.SystemClock.elapsedRealtime() }.getOrDefault(0L)
            if (focus.autoAdvance && focus.isPhaseCompleted(System.currentTimeMillis(), mono0)) {
                val wasBreak = focus.isBreakPhase
                if (vm.repo.reconcileFocusTimer()) {
                    if (wasBreak && notifyBreakEnd) {
                        Reminders.notifyTimeForge(context, 1752, "Pausa concluída", "Hora de retornar à Forja do Tempo.")
                    } else if (!wasBreak && notifyFocusEnd) {
                        Reminders.notifyTimeForge(context, 1751, "Ciclo de foco concluído", "Seu tempo efetivo foi registrado.")
                    }
                    return@LaunchedEffect
                }
            }
        }
        while (focus != null && focus.startedAt > 0L) {
            delay(1000L)
            val now = System.currentTimeMillis()
            tick = now
            val mono = runCatching { android.os.SystemClock.elapsedRealtime() }.getOrDefault(0L)
            if (focus.autoAdvance && focus.isPhaseCompleted(now, mono)) {
                val wasBreak = focus.isBreakPhase
                if (vm.repo.reconcileFocusTimer(now, mono)) {
                    if (wasBreak && notifyBreakEnd) {
                        Reminders.notifyTimeForge(context, 1752, "Pausa concluída", "Hora de retornar à Forja do Tempo.")
                    } else if (!wasBreak && notifyFocusEnd) {
                        Reminders.notifyTimeForge(context, 1751, "Ciclo de foco concluído", "Seu tempo efetivo foi registrado.")
                    }
                    break
                }
            }
        }
    }

    // Controla motor de som ambiente procedural offline respeitando desligado/volume
    DisposableEffect(ambientSceneId, ambientVolume) {
        AmbientAudioEngine.setVolume(ambientVolume)
        if (ambientSceneId != "off") AmbientAudioEngine.start(ambientSceneId, ambientVolume) else AmbientAudioEngine.stop()
        onDispose { AmbientAudioEngine.stop() }
    }

    val activeScene = remember(ambientSceneId) { ambientScenes.find { it.id == ambientSceneId } ?: ambientScenes.first() }
    val todayBlocks = remember(s.timeBlocks) { s.timeBlocks.filter { it.date == today() }.sortedBy { it.startTime } }

    if (immersiveMode) {
        ImmersiveNordicFocusView(
            focus = focus,
            tick = tick,
            scene = activeScene,
            ambientVolume = ambientVolume,
            reducedMotion = reducedMotion,
            onExitImmersive = { immersiveMode = false; vm.setting("immersive_focus", false) },
            onSelectScene = { vm.setAmbientScene(it) },
            onVolume = { vm.setAmbientVolume(it) },
            onPauseResume = { vm.run { vm.repo.pauseFocus() } },
            onAdvancePhase = {
                vm.run {
                    val wasBreak = focus?.isBreakPhase == true
                    vm.repo.advanceFocusPhase()
                    if (wasBreak && notifyBreakEnd) {
                        Reminders.notifyTimeForge(context, 1752, "Pausa concluída", "Hora de retornar à Forja do Tempo.")
                    } else if (!wasBreak && notifyFocusEnd) {
                        Reminders.notifyTimeForge(context, 1751, "Ciclo de foco concluído", "Seu tempo efetivo foi registrado.")
                    }
                }
            },
            onEnd = { register -> vm.run { vm.repo.endFocus(register) } }
        )
        return
    }

    val minimal = LocalAppearance.current.minimal

    LazyColumn(
        Modifier.fillMaxSize().testTag("time_forge_list"),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Title("Forja do Tempo", "Cinco modos de foco, Pomodoro real sem contar pausas, blocos vinculados e modo imersivo.")
        }

        // Painel ativo de cronômetro (instrumentos pictóricos DS 3.2.1: trabalho bronze vs descanso prata vs pausa)
        item {
            val isPaused = focus != null && focus.startedAt == 0L
            val isBreak = focus?.isBreakPhase == true
            val panelMaterial = when {
                focus == null -> CouncilMaterial.OxidizedSilver
                isBreak -> CouncilMaterial.OxidizedSilver
                isPaused -> CouncilMaterial.DarkIron
                else -> CouncilMaterial.HammeredBronze
            }
            val panelAccent = when {
                isBreak -> Blue
                isPaused -> ColdIron
                else -> Gold
            }
            CouncilMaterialSurface(panelMaterial, accent = panelAccent) {
                if (focus != null) {
                    val mono = runCatching { android.os.SystemClock.elapsedRealtime() }.getOrDefault(0L)
                    val secs = focus.seconds(tick, mono)
                    val totalSecs = focus.activePhaseMinutes() * 60L
                    val remaining = (totalSecs - secs).coerceAtLeast(0L)
                    val phaseLabel = when {
                        isPaused && isBreak -> "PAUSA RETIDA · CRONÔMETRO PAUSADO"
                        isPaused -> "FOCO RETIDO · CRONÔMETRO PAUSADO"
                        focus.phase == "short_break" -> "PAUSA CURTA · NÃO CONTA COMO FOCO"
                        focus.phase == "long_break" -> "PAUSA LONGA · NÃO CONTA COMO FOCO"
                        else -> "${focusModeSpec(focus.mode).name.uppercase()} · CICLO ${focus.currentCycle}/${focus.totalCycles}"
                    }
                    val phaseRune = when {
                        isPaused -> HistoricalFutharkRune.Isa
                        isBreak -> HistoricalFutharkRune.Wunjo
                        else -> HistoricalFutharkRune.Kenaz
                    }
                    NordicSectionHeader(
                        eyebrow = phaseLabel,
                        subtitle = if (isBreak) "Regeneração consciente separada do tempo efetivo de foco." else "Tempo efetivo protegido por relógio monotônico.",
                        rune = phaseRune,
                        accent = panelAccent
                    )
                    Box(Modifier.fillMaxWidth().padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                        if (!minimal) {
                            TimeForgeRefinedPiece(
                                col = if (isBreak) 1 else 0,
                                row = 0,
                                modifier = Modifier.size(184.dp),
                                alpha = if (isPaused) 0.65f else 1f
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                "%02d:%02d".format(remaining / 60, remaining % 60),
                                style = MaterialTheme.typography.headlineLarge,
                                color = if (isBreak) Blue else if (isPaused) Muted else Gold,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                when {
                                    isPaused -> "Pausado"
                                    isBreak -> "Descanso"
                                    else -> "Ciclo ${focus.currentCycle}/${focus.totalCycles}"
                                },
                                color = if (isBreak) Blue else Muted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (isPaused) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!minimal) {
                                TimeForgeRefinedPiece(col = 0, row = 1, modifier = Modifier.size(22.dp))
                                Spacer(Modifier.width(6.dp))
                            }
                            StatusSeal("Cronômetro pausado · toque em Retomar", ColdIron)
                        }
                    } else if (isBreak) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            StatusSeal("Pausa regenerativa · 0 min somados ao foco", Blue)
                        }
                    }

                    // Medalhões de progresso dos ciclos
                    if (focusModeSpec(focus.mode).usesBreaks && focus.totalCycles > 1) {
                        val shownCycles = focus.totalCycles.coerceAtMost(8)
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (c in 1..shownCycles) {
                                val doneCycle = c < focus.currentCycle
                                val currentCycle = c == focus.currentCycle
                                Box(
                                    Modifier.padding(horizontal = 3.dp).size(if (currentCycle) 28.dp else 22.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!minimal) {
                                        TimeForgeRefinedPiece(
                                            col = if (doneCycle) 1 else 2,
                                            row = if (doneCycle) 1 else 0,
                                            modifier = Modifier.matchParentSize(),
                                            alpha = when {
                                                doneCycle -> 1f
                                                currentCycle -> 0.95f
                                                else -> 0.38f
                                            }
                                        )
                                    } else {
                                        Text(
                                            if (doneCycle) "◆" else if (currentCycle) "◈" else "◇",
                                            color = if (doneCycle) Green else if (currentCycle) panelAccent else Muted,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Text(
                        focus.targetLabel.ifBlank { "Sessão ativa" },
                        color = Ink,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    if (focus.hasClockAnomaly(tick, mono)) {
                        Text("Ajuste de relógio do sistema detectado; contagem protegida por relógio monotônico.", color = Gold, fontSize = 11.sp)
                    }
                    NordicStrategyDivider(
                        style = if (isBreak) StrategyFrameAsset.BorreDivider else StrategyFrameAsset.UrnesDivider,
                        rune = phaseRune,
                        accent = panelAccent
                    )
                    BoxWithConstraints(Modifier.fillMaxWidth()) {
                        val narrow = maxWidth < 310.dp
                        if (narrow) {
                            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                SagaButton(
                                    if (focus.startedAt > 0L) "Pausar" else "Retomar",
                                    { vm.run { vm.repo.pauseFocus() } },
                                    Modifier.fillMaxWidth(),
                                    style = if (focus.startedAt > 0L) VikingButtonStyle.BronzeForjado else VikingButtonStyle.Confirmacao
                                )
                                if (focusModeSpec(focus.mode).usesBreaks) {
                                    SagaButton(
                                        if (focus.isBreakPhase) "Próximo foco" else "Ir p/ pausa",
                                        { vm.run { vm.repo.advanceFocusPhase() } },
                                        Modifier.fillMaxWidth(),
                                        style = VikingButtonStyle.FerroRunico
                                    )
                                }
                                SagaButton(
                                    if (focus.isBreakPhase) "Encerrar pausa" else "Registrar foco",
                                    { vm.run { vm.repo.endFocus(register = true) } },
                                    Modifier.fillMaxWidth(),
                                    style = if (focus.isBreakPhase) VikingButtonStyle.CompactoRunico else VikingButtonStyle.CerimonialViking
                                )
                                SagaButton(
                                    "Modo Imersivo",
                                    { immersiveMode = true; vm.setting("immersive_focus", true) },
                                    Modifier.fillMaxWidth(),
                                    style = VikingButtonStyle.CompactoRunico
                                )
                            }
                        } else {
                            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    SagaButton(
                                        if (focus.startedAt > 0L) "Pausar" else "Retomar",
                                        { vm.run { vm.repo.pauseFocus() } },
                                        Modifier.weight(1f),
                                        style = if (focus.startedAt > 0L) VikingButtonStyle.BronzeForjado else VikingButtonStyle.Confirmacao
                                    )
                                    if (focusModeSpec(focus.mode).usesBreaks) {
                                        SagaButton(
                                            if (focus.isBreakPhase) "Próximo foco" else "Ir p/ pausa",
                                            { vm.run { vm.repo.advanceFocusPhase() } },
                                            Modifier.weight(1f),
                                            style = VikingButtonStyle.FerroRunico
                                        )
                                    }
                                }
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    SagaButton(
                                        if (focus.isBreakPhase) "Encerrar pausa" else "Registrar foco",
                                        { vm.run { vm.repo.endFocus(register = true) } },
                                        Modifier.weight(1f),
                                        style = if (focus.isBreakPhase) VikingButtonStyle.CompactoRunico else VikingButtonStyle.Confirmacao
                                    )
                                    SagaButton(
                                        "Modo Imersivo",
                                        { immersiveMode = true; vm.setting("immersive_focus", true) },
                                        Modifier.weight(1f),
                                        style = VikingButtonStyle.CompactoRunico
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (!minimal) {
                            TimeForgeRefinedPiece(col = 2, row = 1, modifier = Modifier.size(50.dp))
                        } else {
                            FutharkRuneGlyph(HistoricalFutharkRune.Dagaz, Gold, size = 24.dp)
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Eyebrow("Iniciar nova sessão", Gold)
                            Text("Escolha o ritmo da Forja do Tempo e vincule a um bloco de hoje.", color = Muted, fontSize = 12.sp)
                        }
                    }
                    ChipRow(
                        timeForgeModes.map { it.name },
                        timeForgeModes.indexOfFirst { it.id == selectedModeId }.coerceAtLeast(0),
                        { selectedModeId = timeForgeModes[it].id }
                    )
                    Text(modeSpec.subtitle, color = Muted, fontSize = 12.sp)

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(Modifier.weight(1f)) { FormField(workMinText, { workMinText = it }, "Foco (min)", true) }
                        if (modeSpec.usesBreaks) {
                            Box(Modifier.weight(1f)) { FormField(shortBreakText, { shortBreakText = it }, "Pausa curta", true) }
                            Box(Modifier.weight(1f)) { FormField(longBreakText, { longBreakText = it }, "Pausa longa", true) }
                        }
                    }
                    if (modeSpec.usesBreaks) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.weight(1f)) { FormField(cyclesText, { cyclesText = it }, "Ciclos", true) }
                            Column(Modifier.weight(2f)) {
                                Toggle("Avançar fases automaticamente", autoAdvance, { autoAdvance = it })
                            }
                        }
                    }
                    FormField(customLabel, { customLabel = it }, "Rótulo opcional da sessão")

                    if (todayBlocks.isNotEmpty()) {
                        Text("Vincular diretamente a um bloco de hoje (sem dupla contagem):", color = Muted, fontSize = 12.sp)
                        val blockLabels = listOf("Sem bloco") + todayBlocks.map { "${it.startTime} ${it.title.take(18)}" }
                        val blockIds = listOf("") + todayBlocks.map { it.id }
                        ChipRow(blockLabels, blockIds.indexOf(selectedBlockId).coerceAtLeast(0), { selectedBlockId = blockIds[it] })
                    }

                    BoxWithConstraints(Modifier.fillMaxWidth()) {
                        val startAction = {
                            vm.run {
                                vm.repo.startTimeForgeFocus(
                                    mode = selectedModeId,
                                    workMinutes = (workMinText.toIntOrNull() ?: modeSpec.defaultWorkMinutes).coerceIn(1, 1440),
                                    shortBreakMinutes = if (modeSpec.usesBreaks) (shortBreakText.toIntOrNull() ?: modeSpec.defaultShortBreak).coerceIn(0, 120) else 0,
                                    longBreakMinutes = if (modeSpec.usesBreaks) (longBreakText.toIntOrNull() ?: modeSpec.defaultLongBreak).coerceIn(0, 180) else 0,
                                    totalCycles = if (modeSpec.usesBreaks) (cyclesText.toIntOrNull() ?: modeSpec.defaultCycles).coerceIn(1, 12) else 1,
                                    autoAdvance = autoAdvance,
                                    blockId = selectedBlockId,
                                    targetLabel = customLabel.trim()
                                )
                            }
                        }
                        if (maxWidth < 310.dp) {
                            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                SagaButton("Iniciar ${modeSpec.name}", startAction, Modifier.fillMaxWidth(), style = VikingButtonStyle.CerimonialViking)
                                SagaButton("Modo Imersivo", { immersiveMode = true; vm.setting("immersive_focus", true) }, Modifier.fillMaxWidth(), style = VikingButtonStyle.FerroRunico)
                            }
                        } else {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                SagaButton("Iniciar ${modeSpec.name}", startAction, Modifier.weight(1f), style = VikingButtonStyle.CerimonialViking)
                                SagaButton("Modo Imersivo", { immersiveMode = true; vm.setting("immersive_focus", true) }, Modifier.weight(1f), style = VikingButtonStyle.FerroRunico)
                            }
                        }
                    }
                }
            }
        }

        // 3 Sons Ambientes Offline Procedurais
        item {
            CouncilMaterialSurface(CouncilMaterial.NordicWood) {
                NordicSectionHeader(
                    eyebrow = "Ambiente sonoro offline (síntese local)",
                    subtitle = "Sem arquivos externos ou streaming. Desligue ou ajuste o volume livremente.",
                    rune = HistoricalFutharkRune.Laguz,
                    accent = Gold
                )
                ChipRow(
                    ambientScenes.map { it.name },
                    ambientScenes.indexOfFirst { it.id == ambientSceneId }.coerceAtLeast(0),
                    { vm.setAmbientScene(ambientScenes[it].id) }
                )
                Text("${activeScene.subtitle} · Volume ${(ambientVolume * 100).toInt()}%", color = Muted, fontSize = 12.sp)
                Slider(
                    value = ambientVolume,
                    onValueChange = { vm.setAmbientVolume(it) },
                    enabled = ambientSceneId != "off",
                    colors = SliderDefaults.colors(thumbColor = Gold, activeTrackColor = Gold, inactiveTrackColor = ColdIron.copy(alpha = 0.35f))
                )
            }
        }

        // Notificações locais opcionais
        item {
            CouncilMaterialSurface(CouncilMaterial.DarkIron) {
                NordicSectionHeader(
                    eyebrow = "Notificações locais da Forja e Agenda",
                    rune = HistoricalFutharkRune.Ansuz,
                    accent = Blue
                )
                Toggle("Avisar ao concluir bloco de foco", notifyFocusEnd, { vm.setting("notify_focus_end", it) })
                Toggle("Avisar ao concluir pausa", notifyBreakEnd, { vm.setting("notify_break_end", it) })
                Toggle("Lembrar próximo bloco planejado", notifyNextBlock, { vm.setting("notify_next_block", it) })
                Toggle("Lembrar compromisso próximo", notifyUpcomingEvent, { vm.setting("notify_upcoming_event", it) })
                SagaButton(
                    "Ver histórico completo de foco",
                    onOpenHistory,
                    Modifier.fillMaxWidth(),
                    style = VikingButtonStyle.FerroRunico
                )
            }
        }
    }
}

@Composable
private fun ImmersiveNordicFocusView(
    focus: Focus?,
    tick: Long,
    scene: AmbientSceneSpec,
    ambientVolume: Float,
    reducedMotion: Boolean,
    onExitImmersive: () -> Unit,
    onSelectScene: (String) -> Unit,
    onVolume: (Float) -> Unit,
    onPauseResume: () -> Unit,
    onAdvancePhase: () -> Unit,
    onEnd: (Boolean) -> Unit
) {
    val minimal = LocalAppearance.current.minimal
    val mono = runCatching { android.os.SystemClock.elapsedRealtime() }.getOrDefault(0L)
    val secs = focus?.seconds(tick, mono) ?: 0L
    val totalSecs = (focus?.activePhaseMinutes() ?: 25) * 60L
    val remaining = (totalSecs - secs).coerceAtLeast(0L)
    val isBreak = focus?.isBreakPhase == true
    val isPaused = focus != null && focus.startedAt == 0L
    val phaseAccent = if (isBreak) Blue else if (isPaused) ColdIron else Gold
    val phaseRune = when {
        isPaused -> HistoricalFutharkRune.Isa
        isBreak -> HistoricalFutharkRune.Wunjo
        else -> HistoricalFutharkRune.Kenaz
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0D12))
            .testTag("immersive_focus_view")
    ) {
        if (!minimal) {
            ImmersiveFocusScene(
                col = scene.atlasCol,
                row = scene.atlasRow,
                modifier = Modifier.matchParentSize(),
                alpha = if (reducedMotion) 0.34f else 0.48f
            )
            Box(
                Modifier.matchParentSize().background(
                    Brush.verticalGradient(listOf(Color(0xAA090C11), Color(0xCC090C11), Color(0xF0090C11)))
                )
            )
        }

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FutharkRuneGlyph(phaseRune, phaseAccent, size = 15.dp)
                    Text(
                        "FORJA DO TEMPO · IMERSIVO",
                        color = phaseAccent,
                        fontSize = 11.sp,
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
                NordicCompactAction(
                    text = "Sair",
                    onClick = onExitImmersive,
                    accent = Color(0xFFE7C582),
                    rune = HistoricalFutharkRune.Othala
                )
            }

            Spacer(Modifier.height(4.dp))

            Box(Modifier.size(216.dp), contentAlignment = Alignment.Center) {
                if (!minimal) {
                    TimeForgeRefinedPiece(
                        col = if (isBreak) 1 else 0,
                        row = 0,
                        modifier = Modifier.matchParentSize(),
                        alpha = if (isPaused) 0.62f else 1f
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        if (focus == null) "25:00" else "%02d:%02d".format(remaining / 60, remaining % 60),
                        style = MaterialTheme.typography.displaySmall,
                        color = if (isBreak) Blue else if (isPaused) Color(0xFFA8B3C2) else Color(0xFFF3ECE1),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        when {
                            focus == null -> "Pronto para iniciar"
                            isPaused -> "Cronômetro pausado"
                            isBreak -> "Pausa regenerativa"
                            else -> "Ciclo ${focus.currentCycle}/${focus.totalCycles}"
                        },
                        color = if (isBreak) Blue else Color(0xFFD2C7B4),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (focus != null && focusModeSpec(focus.mode).usesBreaks && focus.totalCycles > 1) {
                val shownCycles = focus.totalCycles.coerceAtMost(8)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    for (c in 1..shownCycles) {
                        val doneCycle = c < focus.currentCycle
                        val currentCycle = c == focus.currentCycle
                        if (!minimal) {
                            TimeForgeRefinedPiece(
                                col = if (doneCycle) 1 else 2,
                                row = if (doneCycle) 1 else 0,
                                modifier = Modifier.size(if (currentCycle) 26.dp else 20.dp),
                                alpha = if (doneCycle || currentCycle) 1f else 0.4f
                            )
                        } else {
                            Text(if (doneCycle) "◆" else if (currentCycle) "◈" else "◇", color = phaseAccent, fontSize = 13.sp)
                        }
                    }
                }
            }

            Text(
                focus?.targetLabel?.ifBlank { "Concentração profunda" } ?: "Nenhuma sessão ativa no momento",
                color = Color(0xFFF3ECE1),
                style = MaterialTheme.typography.titleMedium
            )

            NordicStrategyDivider(
                style = if (isBreak) StrategyFrameAsset.BorreDivider else StrategyFrameAsset.UrnesDivider,
                rune = phaseRune,
                accent = phaseAccent
            )

            if (focus != null) {
                BoxWithConstraints(Modifier.fillMaxWidth()) {
                    if (maxWidth < 310.dp) {
                        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            SagaButton(
                                if (focus.startedAt > 0L) "Pausar" else "Retomar",
                                onPauseResume,
                                Modifier.fillMaxWidth(),
                                style = if (focus.startedAt > 0L) VikingButtonStyle.BronzeForjado else VikingButtonStyle.Confirmacao
                            )
                            if (focusModeSpec(focus.mode).usesBreaks) {
                                SagaButton(
                                    if (isBreak) "Voltar ao foco" else "Pausa",
                                    onAdvancePhase,
                                    Modifier.fillMaxWidth(),
                                    style = VikingButtonStyle.FerroRunico
                                )
                            }
                            SagaButton(
                                if (isBreak) "Encerrar pausa" else "Concluir e registrar",
                                { onEnd(true) },
                                Modifier.fillMaxWidth(),
                                style = if (isBreak) VikingButtonStyle.CompactoRunico else VikingButtonStyle.CerimonialViking
                            )
                        }
                    } else {
                        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                SagaButton(
                                    if (focus.startedAt > 0L) "Pausar" else "Retomar",
                                    onPauseResume,
                                    Modifier.weight(1f),
                                    style = if (focus.startedAt > 0L) VikingButtonStyle.BronzeForjado else VikingButtonStyle.Confirmacao
                                )
                                if (focusModeSpec(focus.mode).usesBreaks) {
                                    SagaButton(
                                        if (isBreak) "Voltar ao foco" else "Pausa",
                                        onAdvancePhase,
                                        Modifier.weight(1f),
                                        style = VikingButtonStyle.FerroRunico
                                    )
                                }
                            }
                            SagaButton(
                                if (isBreak) "Encerrar pausa" else "Concluir e registrar",
                                { onEnd(true) },
                                Modifier.fillMaxWidth(),
                                style = if (isBreak) VikingButtonStyle.CompactoRunico else VikingButtonStyle.CerimonialViking
                            )
                        }
                    }
                }
            }

            CouncilMaterialSurface(CouncilMaterial.DarkIron, accent = phaseAccent) {
                NordicSectionHeader(
                    eyebrow = "Atmosfera sonora · ${scene.name}",
                    rune = HistoricalFutharkRune.Laguz,
                    accent = phaseAccent
                )
                ChipRow(
                    ambientScenes.map { it.name },
                    ambientScenes.indexOfFirst { it.id == scene.id }.coerceAtLeast(0),
                    { onSelectScene(ambientScenes[it].id) }
                )
                Slider(
                    value = ambientVolume,
                    onValueChange = onVolume,
                    enabled = scene.id != "off",
                    colors = SliderDefaults.colors(thumbColor = Gold, activeTrackColor = Gold)
                )
            }
        }
    }
}
