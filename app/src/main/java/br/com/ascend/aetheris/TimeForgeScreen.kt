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
    LaunchedEffect(focus?.startedAt, focus?.phase) {
        while (focus != null && focus.startedAt > 0L) {
            delay(1000L)
            tick = System.currentTimeMillis()
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

    LazyColumn(
        Modifier.fillMaxSize().testTag("time_forge_list"),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Title("Forja do Tempo", "Cinco modos de foco, Pomodoro real sem contar pausas, blocos vinculados e modo imersivo.")
        }

        // Painel ativo de cronômetro (com instrumento pictórico)
        item {
            CouncilMaterialSurface(CouncilMaterial.OxidizedSilver, accent = if (focus?.isBreakPhase == true) Blue else Gold) {
                if (focus != null) {
                    val mono = runCatching { android.os.SystemClock.elapsedRealtime() }.getOrDefault(0L)
                    val secs = focus.seconds(tick, mono)
                    val totalSecs = focus.activePhaseMinutes() * 60L
                    val remaining = (totalSecs - secs).coerceAtLeast(0L)
                    val phaseLabel = when (focus.phase) {
                        "short_break" -> "PAUSA CURTA · NÃO CONTA COMO FOCO"
                        "long_break" -> "PAUSA LONGA · NÃO CONTA COMO FOCO"
                        else -> "${focusModeSpec(focus.mode).name.uppercase()} · CICLO ${focus.currentCycle}/${focus.totalCycles}"
                    }
                    Eyebrow(phaseLabel, if (focus.isBreakPhase) Blue else Gold)
                    Box(Modifier.fillMaxWidth().padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                        TimeForgeInstrument(
                            col = 0,
                            row = if (focus.isBreakPhase) 1 else 0,
                            modifier = Modifier.size(168.dp)
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "%02d:%02d".format(remaining / 60, remaining % 60),
                                style = MaterialTheme.typography.headlineLarge,
                                color = if (focus.isBreakPhase) Blue else Gold,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                if (focus.isBreakPhase) "Pausa" else "Ciclo ${focus.currentCycle}/${focus.totalCycles}",
                                color = Muted,
                                fontSize = 11.sp
                            )
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
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SagaButton(if (focus.startedAt > 0L) "Pausar" else "Retomar", { vm.run { vm.repo.pauseFocus() } }, Modifier.weight(1f))
                        if (focusModeSpec(focus.mode).usesBreaks) {
                            SagaButton(
                                if (focus.isBreakPhase) "Próximo foco" else "Ir p/ pausa",
                                { vm.run { vm.repo.advanceFocusPhase() } },
                                Modifier.weight(1f),
                                secondary = true
                            )
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SagaButton(
                            if (focus.isBreakPhase) "Encerrar pausa" else "Registrar foco",
                            { vm.run { vm.repo.endFocus(register = true) } },
                            Modifier.weight(1f),
                            secondary = true
                        )
                        SagaButton("Modo Imersivo", { immersiveMode = true; vm.setting("immersive_focus", true) }, Modifier.weight(1f), secondary = true)
                    }
                } else {
                    Eyebrow("Iniciar nova sessão")
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

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SagaButton(
                            "Iniciar ${modeSpec.name}",
                            {
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
                            },
                            Modifier.weight(1f)
                        )
                        SagaButton("Modo Imersivo", { immersiveMode = true; vm.setting("immersive_focus", true) }, Modifier.weight(1f), secondary = true)
                    }
                }
            }
        }

        // 3 Sons Ambientes Offline Procedurais
        item {
            CouncilMaterialSurface(CouncilMaterial.NordicWood) {
                Eyebrow("Ambiente sonoro offline (síntese local)")
                Text("Sem arquivos externos ou streaming. Desligue ou ajuste o volume livremente.", color = Muted, fontSize = 12.sp)
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
                Eyebrow("Notificações locais da Forja e Agenda")
                Toggle("Avisar ao concluir bloco de foco", notifyFocusEnd, { vm.setting("notify_focus_end", it) })
                Toggle("Avisar ao concluir pausa", notifyBreakEnd, { vm.setting("notify_break_end", it) })
                Toggle("Lembrar próximo bloco planejado", notifyNextBlock, { vm.setting("notify_next_block", it) })
                Toggle("Lembrar compromisso próximo", notifyUpcomingEvent, { vm.setting("notify_upcoming_event", it) })
                SagaButton("Ver histórico completo de foco", onOpenHistory, Modifier.fillMaxWidth(), secondary = true)
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
    val mono = runCatching { android.os.SystemClock.elapsedRealtime() }.getOrDefault(0L)
    val secs = focus?.seconds(tick, mono) ?: 0L
    val totalSecs = (focus?.activePhaseMinutes() ?: 25) * 60L
    val remaining = (totalSecs - secs).coerceAtLeast(0L)
    val isBreak = focus?.isBreakPhase == true

    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0D12))
            .testTag("immersive_focus_view")
    ) {
        if (!LocalAppearance.current.minimal) {
            ImmersiveFocusScene(
                col = scene.atlasCol,
                row = scene.atlasRow,
                modifier = Modifier.matchParentSize(),
                alpha = if (reducedMotion) 0.38f else 0.52f
            )
            Box(
                Modifier.matchParentSize().background(
                    Brush.verticalGradient(listOf(Color(0xAA090C11), Color(0xCC090C11), Color(0xEE090C11)))
                )
            )
        }

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("FORJA DO TEMPO · IMERSIVO", color = Gold, fontSize = 11.sp, letterSpacing = 1.4.sp, fontWeight = FontWeight.SemiBold)
                TextButton(onExitImmersive) { Text("Sair do imersivo", color = Color(0xFFE7C582)) }
            }

            Spacer(Modifier.height(8.dp))

            Box(Modifier.size(220.dp), contentAlignment = Alignment.Center) {
                TimeForgeInstrument(col = 0, row = if (isBreak) 1 else 0, modifier = Modifier.matchParentSize())
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        if (focus == null) "25:00" else "%02d:%02d".format(remaining / 60, remaining % 60),
                        style = MaterialTheme.typography.displaySmall,
                        color = if (isBreak) Blue else Color(0xFFF3ECE1),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        when {
                            focus == null -> "Pronto para iniciar"
                            isBreak -> "Pausa regenerativa"
                            else -> "Ciclo ${focus.currentCycle}/${focus.totalCycles}"
                        },
                        color = Color(0xFFD2C7B4),
                        fontSize = 12.sp
                    )
                }
            }

            Text(
                focus?.targetLabel?.ifBlank { "Concentração profunda" } ?: "Nenhuma sessão ativa no momento",
                color = Color(0xFFF3ECE1),
                style = MaterialTheme.typography.titleMedium
            )

            if (focus != null) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SagaButton(if (focus.startedAt > 0L) "Pausar" else "Retomar", onPauseResume, Modifier.weight(1f))
                    if (focusModeSpec(focus.mode).usesBreaks) {
                        SagaButton(if (isBreak) "Voltar ao foco" else "Pausa", onAdvancePhase, Modifier.weight(1f), secondary = true)
                    }
                }
                SagaButton(if (isBreak) "Encerrar pausa" else "Concluir e registrar", { onEnd(true) }, Modifier.fillMaxWidth(), secondary = true)
            }

            CouncilMaterialSurface(CouncilMaterial.DarkIron, accent = Gold) {
                Eyebrow("Atmosfera sonora · ${scene.name}")
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
