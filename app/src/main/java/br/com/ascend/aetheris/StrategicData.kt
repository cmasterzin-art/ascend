package br.com.ascend.aetheris

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.max

/**
 * ASCEND 1.7.5 — A Mesa de Odin
 * Entidades Room v11, catálogos estratégicos (6 estratégias de Odin, 9 áreas do Conselho dos Nove,
 * 6 modelos de projeto reutilizáveis) e auxiliares puros de validação.
 */

@Entity(
    tableName = "availability_windows",
    indices = [Index("dayOfWeek")]
)
data class AvailabilityWindow(
    @PrimaryKey val id: String = uid(),
    val dayOfWeek: Int = 1, // 1 = Segunda .. 7 = Domingo (ISO-8601)
    val startTime: String = "08:00",
    val endTime: String = "12:00",
    val label: String = "Manhã",
    val preferredCategory: String = "",
    val enabled: Boolean = true
)

@Entity(
    tableName = "availability_exceptions",
    indices = [Index("date")]
)
data class AvailabilityException(
    @PrimaryKey val id: String = uid(),
    val date: String = today(),
    val blocked: Boolean = true, // true = dia indisponível/feriado/descanso; false = janela especial
    val startTime: String = "",
    val endTime: String = "",
    val note: String = ""
)

@Entity(tableName = "planning_config")
data class PlanningConfig(
    @PrimaryKey val id: Int = 1,
    val strategyId: String = "strategist",
    val dailyLimitMinutes: Int = 360,
    val bufferMinutes: Int = 10,
    val minBlockMinutes: Int = 25,
    val maxBlockMinutes: Int = 90,
    val groupSimilar: Boolean = true,
    val reduceContextSwitch: Boolean = true,
    val useHistorySoftPreference: Boolean = true,
    val preferredStudyPeriod: String = "morning", // morning, afternoon, evening, any
    val updated: String = today()
)

@Entity(
    tableName = "task_dependencies",
    indices = [Index("taskId"), Index("dependsOnTaskId")]
)
data class TaskDependency(
    @PrimaryKey val id: String = uid(),
    val taskId: String,
    val dependsOnTaskId: String,
    val overridden: Boolean = false,
    val overrideReason: String = "",
    val overrideDate: String = "",
    val created: String = today()
)

@Entity(
    tableName = "path_milestones",
    indices = [Index("pathId"), Index("targetDate")]
)
data class PathMilestone(
    @PrimaryKey val id: String = uid(),
    val pathId: String,
    val title: String,
    val targetDate: String = today(),
    val completed: Boolean = false,
    val completedDate: String = "",
    val notes: String = "",
    val position: Int = 0
)

@Entity(tableName = "life_areas")
data class LifeArea(
    @PrimaryKey val id: String = uid(),
    val name: String,
    val description: String = "",
    val sealIndex: Int = 0, // 0..8 -> nine_council_seals_atlas.webp (3x3)
    val color: Int = 0,
    val weeklyTargetMinutes: Int = 120,
    val archived: Boolean = false,
    val position: Int = 0
)

@Entity(
    tableName = "strategic_goals",
    indices = [Index("areaId"), Index("horizon")]
)
data class StrategicGoal(
    @PrimaryKey val id: String = uid(),
    val title: String,
    val description: String = "",
    val areaId: String = "",
    val horizon: String = "quarterly", // quarterly | annual
    val periodLabel: String = "",
    val deadline: String = "",
    val linkedPathIds: String = "",
    val linkedHabitIds: String = "",
    val targetValue: Int = 100,
    val archived: Boolean = false,
    val created: String = today()
)

@Entity(
    tableName = "applied_plans",
    indices = [Index("appliedAt")]
)
data class AppliedPlanRecord(
    @PrimaryKey val id: String = uid(),
    val appliedAt: Long = System.currentTimeMillis(),
    val dateFrom: String = today(),
    val dateTo: String = today(),
    val strategyId: String = "strategist",
    val blockIds: String = "",
    val taskSnapshotsJson: String = "",
    val undone: Boolean = false,
    val summary: String = ""
)

@Entity(
    tableName = "recurrence_rules",
    indices = [Index("templateId")]
)
data class RecurrenceRule(
    @PrimaryKey val id: String = uid(),
    val entityType: String = "block", // block | event
    val templateId: String = "",
    val pattern: String = "weekly", // daily | weekly | weekdays | monthly
    val daysOfWeek: String = "", // "1,3,5"
    val startDate: String = today(),
    val endDate: String = "",
    val excludedDates: String = "",
    val active: Boolean = true
)

// =====================================================================
// ESTRATÉGIAS DE ODIN (apenas pesos de ordenação e alocação — zero efeito no RPG)
// =====================================================================

data class OdinStrategyDef(
    val id: String,
    val name: String,
    val title: String,
    val summary: String,
    val atlasCol: Int,
    val atlasRow: Int,
    val deadlineWeight: Double,
    val priorityWeight: Double,
    val studyFocusWeight: Double,
    val criticalPathWeight: Double,
    val habitStabilityWeight: Double,
    val creativeBlockWeight: Double,
    val preferredMaxBlockMinutes: Int,
    val preferredBufferMinutes: Int,
    val targetDailyLoadRatio: Double // 0.65..1.00 da disponibilidade configurada
)

val odinStrategies: List<OdinStrategyDef> = listOf(
    OdinStrategyDef(
        id = "guardian",
        name = "Guardião",
        title = "Sustentabilidade e Margem",
        summary = "Prioriza hábitos essenciais, margens generosas entre blocos e carga sustentável sem sobrecarga.",
        atlasCol = 0, atlasRow = 0,
        deadlineWeight = 1.15,
        priorityWeight = 1.00,
        studyFocusWeight = 0.90,
        criticalPathWeight = 1.10,
        habitStabilityWeight = 1.65,
        creativeBlockWeight = 0.95,
        preferredMaxBlockMinutes = 60,
        preferredBufferMinutes = 15,
        targetDailyLoadRatio = 0.72
    ),
    OdinStrategyDef(
        id = "scholar",
        name = "Erudito",
        title = "Estudo Profundo e Memória",
        summary = "Agrupa estudos, revisões de Mímir e leitura profunda nos horários de maior clareza mental.",
        atlasCol = 1, atlasRow = 0,
        deadlineWeight = 1.10,
        priorityWeight = 1.05,
        studyFocusWeight = 1.80,
        criticalPathWeight = 1.15,
        habitStabilityWeight = 1.05,
        creativeBlockWeight = 1.00,
        preferredMaxBlockMinutes = 90,
        preferredBufferMinutes = 10,
        targetDailyLoadRatio = 0.85
    ),
    OdinStrategyDef(
        id = "berserker",
        name = "Berserker",
        title = "Ataque Direto a Prazos",
        summary = "Concentra energia máxima nas tarefas urgentes, atrasadas e com prazo mais curto.",
        atlasCol = 2, atlasRow = 0,
        deadlineWeight = 2.00,
        priorityWeight = 1.55,
        studyFocusWeight = 0.90,
        criticalPathWeight = 1.45,
        habitStabilityWeight = 0.80,
        creativeBlockWeight = 0.75,
        preferredMaxBlockMinutes = 90,
        preferredBufferMinutes = 5,
        targetDailyLoadRatio = 0.98
    ),
    OdinStrategyDef(
        id = "strategist",
        name = "Estrategista",
        title = "Caminho Crítico e Equilíbrio",
        summary = "Combina caminho crítico de projetos, prazos próximos e equilíbrio saudável entre áreas.",
        atlasCol = 0, atlasRow = 1,
        deadlineWeight = 1.45,
        priorityWeight = 1.35,
        studyFocusWeight = 1.15,
        criticalPathWeight = 1.85,
        habitStabilityWeight = 1.15,
        creativeBlockWeight = 1.05,
        preferredMaxBlockMinutes = 75,
        preferredBufferMinutes = 10,
        targetDailyLoadRatio = 0.88
    ),
    OdinStrategyDef(
        id = "explorer",
        name = "Explorador",
        title = "Variedade e Desbloqueio",
        summary = "Alterna categorias e frentes de projeto, favorecendo tarefas curtas que destravam novas frentes.",
        atlasCol = 1, atlasRow = 1,
        deadlineWeight = 1.15,
        priorityWeight = 1.10,
        studyFocusWeight = 1.05,
        criticalPathWeight = 1.25,
        habitStabilityWeight = 1.00,
        creativeBlockWeight = 1.35,
        preferredMaxBlockMinutes = 50,
        preferredBufferMinutes = 10,
        targetDailyLoadRatio = 0.82
    ),
    OdinStrategyDef(
        id = "artificer",
        name = "Artífice",
        title = "Blocos Longos de Construção",
        summary = "Protege janelas contínuas para produção profunda, projetos autorais e entregas criativas.",
        atlasCol = 2, atlasRow = 1,
        deadlineWeight = 1.20,
        priorityWeight = 1.25,
        studyFocusWeight = 1.10,
        criticalPathWeight = 1.40,
        habitStabilityWeight = 0.90,
        creativeBlockWeight = 1.85,
        preferredMaxBlockMinutes = 120,
        preferredBufferMinutes = 15,
        targetDailyLoadRatio = 0.90
    )
)

fun odinStrategyById(id: String): OdinStrategyDef =
    odinStrategies.find { it.id == id } ?: odinStrategies[3] // Estrategista padrão

fun resolvedPlanningConfig(s: GameState): PlanningConfig = s.planningConfig ?: PlanningConfig()

// =====================================================================
// CONSELHO DOS NOVE — 9 ÁREAS OPCIONAIS DA VIDA
// =====================================================================

data class DefaultLifeAreaSpec(
    val id: String,
    val name: String,
    val description: String,
    val sealIndex: Int,
    val color: Int,
    val weeklyTargetMinutes: Int
)

val defaultLifeAreaSpecs: List<DefaultLifeAreaSpec> = listOf(
    DefaultLifeAreaSpec("area-estudos", "Estudos", "Leitura, pesquisa, cursos e revisões em Mímir.", 0, 1, 240),
    DefaultLifeAreaSpec("area-saude", "Saúde", "Sono, movimento, alimentação e vitalidade física.", 1, 3, 180),
    DefaultLifeAreaSpec("area-disciplina", "Disciplina", "Rotinas estruturantes, clareza e constância diária.", 2, 0, 150),
    DefaultLifeAreaSpec("area-carreira", "Carreira", "Projetos profissionais, entregas e evolução técnica.", 3, 0, 300),
    DefaultLifeAreaSpec("area-criatividade", "Criatividade", "Escrita, arte, música, design e construção autoral.", 4, 2, 120),
    DefaultLifeAreaSpec("area-financas", "Finanças", "Organização financeira, revisão mensal e reservas.", 5, 0, 60),
    DefaultLifeAreaSpec("area-social", "Social", "Família, amizades, comunidade e vínculos reais.", 6, 4, 120),
    DefaultLifeAreaSpec("area-equilibrio", "Equilíbrio", "Descanso intencional, reflexão e silêncio.", 7, 3, 120),
    DefaultLifeAreaSpec("area-proposito", "Propósito", "Projetos de longo prazo, valores e direção de vida.", 8, 2, 120)
)

fun defaultLifeAreas(): List<LifeArea> = defaultLifeAreaSpecs.mapIndexed { idx, spec ->
    LifeArea(
        id = spec.id,
        name = spec.name,
        description = spec.description,
        sealIndex = spec.sealIndex,
        color = spec.color,
        weeklyTargetMinutes = spec.weeklyTargetMinutes,
        archived = false,
        position = idx
    )
}

fun defaultAvailabilityWindows(): List<AvailabilityWindow> = (1..5).flatMap { day ->
    listOf(
        AvailabilityWindow(id = "avail-$day-m", dayOfWeek = day, startTime = "09:00", endTime = "12:00", label = "Manhã", enabled = true),
        AvailabilityWindow(id = "avail-$day-t", dayOfWeek = day, startTime = "14:00", endTime = "17:00", label = "Tarde", enabled = true)
    )
} + listOf(
    AvailabilityWindow(id = "avail-6-m", dayOfWeek = 6, startTime = "09:30", endTime = "12:00", label = "Sábado · Manhã", enabled = true)
)

// =====================================================================
// CAMINHOS 3.0 — 6 MODELOS REUTILIZÁVEIS DE PROJETO
// =====================================================================

data class ProjectTemplateStageSpec(val name: String, val note: String)
data class ProjectTemplateTaskSpec(
    val stageIndex: Int,
    val title: String,
    val estimateMinutes: Int,
    val priority: Int,
    val dependsOnIndices: List<Int> = emptyList(),
    val offsetDays: Int = 0
)
data class ProjectTemplateMilestoneSpec(val title: String, val offsetDays: Int)
data class ProjectTemplateDef(
    val id: String,
    val name: String,
    val category: String,
    val summary: String,
    val durationDays: Int,
    val attribute: Int,
    val stages: List<ProjectTemplateStageSpec>,
    val tasks: List<ProjectTemplateTaskSpec>,
    val milestones: List<ProjectTemplateMilestoneSpec>
)

val projectTemplates: List<ProjectTemplateDef> = listOf(
    ProjectTemplateDef(
        id = "tpl-exam",
        name = "Preparação para prova",
        category = "Estudo",
        summary = "Estrutura completa de levantamento de edital, estudo dirigido, simulados e revisão espaçada.",
        durationDays = 28,
        attribute = 0,
        stages = listOf(
            ProjectTemplateStageSpec("Diagnóstico", "Mapear tópicos e pesos"),
            ProjectTemplateStageSpec("Estudo dirigido", "Cobrir teoria e resumos"),
            ProjectTemplateStageSpec("Revisão e simulados", "Consolidar memória ativa")
        ),
        tasks = listOf(
            ProjectTemplateTaskSpec(0, "Mapear conteúdos e critérios da prova", 45, 3, emptyList(), 2),
            ProjectTemplateTaskSpec(0, "Separar bibliografia e notas em Mímir", 40, 2, listOf(0), 4),
            ProjectTemplateTaskSpec(1, "Estudar bloco principal de teoria", 120, 3, listOf(1), 10),
            ProjectTemplateTaskSpec(1, "Resolver lista comentada de exercícios", 90, 2, listOf(2), 16),
            ProjectTemplateTaskSpec(2, "Aplicar simulado cronometrado", 90, 3, listOf(3), 22),
            ProjectTemplateTaskSpec(2, "Revisar erros críticos e flashcards", 60, 3, listOf(4), 26)
        ),
        milestones = listOf(
            ProjectTemplateMilestoneSpec("Base teórica fechada", 12),
            ProjectTemplateMilestoneSpec("Simulado geral concluído", 23)
        )
    ),
    ProjectTemplateDef(
        id = "tpl-paper",
        name = "Artigo científico",
        category = "Estudo",
        summary = "Da questão de pesquisa e revisão bibliográfica até redação, citações e revisão final.",
        durationDays = 35,
        attribute = 0,
        stages = listOf(
            ProjectTemplateStageSpec("Fundamentação", "Problema e referências"),
            ProjectTemplateStageSpec("Redação", "Estrutura e escrita"),
            ProjectTemplateStageSpec("Revisão", "Normas e fechamento")
        ),
        tasks = listOf(
            ProjectTemplateTaskSpec(0, "Definir questão de pesquisa e escopo", 60, 3, emptyList(), 3),
            ProjectTemplateTaskSpec(0, "Fichar artigos e referências centrais", 150, 3, listOf(0), 10),
            ProjectTemplateTaskSpec(1, "Montar esqueleto de seções e argumentos", 60, 2, listOf(1), 14),
            ProjectTemplateTaskSpec(1, "Redigir desenvolvimento e metodologia", 180, 3, listOf(2), 24),
            ProjectTemplateTaskSpec(2, "Revisar citações, clareza e resumo", 90, 2, listOf(3), 32)
        ),
        milestones = listOf(
            ProjectTemplateMilestoneSpec("Revisão bibliográfica pronta", 11),
            ProjectTemplateMilestoneSpec("Primeiro rascunho completo", 25),
            ProjectTemplateMilestoneSpec("Versão pronta para submissão", 34)
        )
    ),
    ProjectTemplateDef(
        id = "tpl-career",
        name = "Projeto profissional",
        category = "Trabalho",
        summary = "Desdobramento claro de escopo, entregas técnicas, validação e fechamento.",
        durationDays = 21,
        attribute = 2,
        stages = listOf(
            ProjectTemplateStageSpec("Alinhamento", "Escopo e critérios"),
            ProjectTemplateStageSpec("Execução", "Entregas principais"),
            ProjectTemplateStageSpec("Entrega", "Validação e documentação")
        ),
        tasks = listOf(
            ProjectTemplateTaskSpec(0, "Definir objetivos, restrições e entregáveis", 45, 3, emptyList(), 2),
            ProjectTemplateTaskSpec(1, "Construir entrega central (bloco 1)", 120, 3, listOf(0), 8),
            ProjectTemplateTaskSpec(1, "Construir entrega central (bloco 2)", 120, 2, listOf(1), 14),
            ProjectTemplateTaskSpec(2, "Validar qualidade e registrar lições", 60, 2, listOf(2), 19)
        ),
        milestones = listOf(
            ProjectTemplateMilestoneSpec("Escopo aprovado", 3),
            ProjectTemplateMilestoneSpec("Entrega validada", 20)
        )
    ),
    ProjectTemplateDef(
        id = "tpl-book",
        name = "Leitura de livro",
        category = "Leitura",
        summary = "Leitura estruturada por blocos de capítulos com fichamento e síntese permanente.",
        durationDays = 21,
        attribute = 0,
        stages = listOf(
            ProjectTemplateStageSpec("Abertura", "Panorama da obra"),
            ProjectTemplateStageSpec("Leitura ativa", "Capítulos e anotações"),
            ProjectTemplateStageSpec("Síntese", "Notas e cartões")
        ),
        tasks = listOf(
            ProjectTemplateTaskSpec(0, "Examinar sumário e definir ritmo diário", 25, 1, emptyList(), 1),
            ProjectTemplateTaskSpec(1, "Ler e anotar primeira metade da obra", 120, 2, listOf(0), 9),
            ProjectTemplateTaskSpec(1, "Ler e anotar segunda metade da obra", 120, 2, listOf(1), 17),
            ProjectTemplateTaskSpec(2, "Escrever síntese final e extrair ideias-chave", 60, 2, listOf(2), 20)
        ),
        milestones = listOf(
            ProjectTemplateMilestoneSpec("Metade do livro concluída", 10),
            ProjectTemplateMilestoneSpec("Síntese registrada", 21)
        )
    ),
    ProjectTemplateDef(
        id = "tpl-app",
        name = "Desenvolvimento de aplicativo",
        category = "Tecnologia",
        summary = "Arquitetura, implementação por camadas, testes automatizados e publicação controlada.",
        durationDays = 30,
        attribute = 4,
        stages = listOf(
            ProjectTemplateStageSpec("Arquitetura", "Modelo e fluxos"),
            ProjectTemplateStageSpec("Implementação", "Domínio e interface"),
            ProjectTemplateStageSpec("Validação", "Testes e empacotamento")
        ),
        tasks = listOf(
            ProjectTemplateTaskSpec(0, "Especificar requisitos, dados e telas", 90, 3, emptyList(), 4),
            ProjectTemplateTaskSpec(1, "Implementar núcleo de dados e regras", 150, 3, listOf(0), 12),
            ProjectTemplateTaskSpec(1, "Construir interface e acessibilidade", 150, 2, listOf(1), 20),
            ProjectTemplateTaskSpec(2, "Executar testes automatizados e auditoria", 90, 3, listOf(2), 26),
            ProjectTemplateTaskSpec(2, "Empacotar versão e atualizar documentação", 60, 2, listOf(3), 29)
        ),
        milestones = listOf(
            ProjectTemplateMilestoneSpec("Núcleo funcional compilando", 13),
            ProjectTemplateMilestoneSpec("Build validado", 28)
        )
    ),
    ProjectTemplateDef(
        id = "tpl-creative",
        name = "Projeto criativo",
        category = "Criatividade",
        summary = "Exploração de referências, protótipo bruto, refinamento estético e finalização.",
        durationDays = 24,
        attribute = 4,
        stages = listOf(
            ProjectTemplateStageSpec("Conceito", "Referências e direção"),
            ProjectTemplateStageSpec("Produção", "Esboço e criação"),
            ProjectTemplateStageSpec("Acabamento", "Refino e apresentação")
        ),
        tasks = listOf(
            ProjectTemplateTaskSpec(0, "Reunir referências e definir conceito visual", 60, 2, emptyList(), 3),
            ProjectTemplateTaskSpec(1, "Produzir versão inicial completa", 150, 3, listOf(0), 12),
            ProjectTemplateTaskSpec(2, "Refinar detalhes e revisar coerência", 90, 2, listOf(1), 20),
            ProjectTemplateTaskSpec(2, "Preparar entrega final e arquivo-mestre", 45, 2, listOf(2), 23)
        ),
        milestones = listOf(
            ProjectTemplateMilestoneSpec("Protótipo inicial concluído", 13),
            ProjectTemplateMilestoneSpec("Obra finalizada", 24)
        )
    )
)

// =====================================================================
// MODOS DA FORJA DO TEMPO & SONS AMBIENTES PROCEDURAIS
// =====================================================================

data class FocusModeSpec(
    val id: String,
    val name: String,
    val subtitle: String,
    val defaultWorkMinutes: Int,
    val defaultShortBreak: Int,
    val defaultLongBreak: Int,
    val defaultCycles: Int,
    val usesBreaks: Boolean
)

val timeForgeModes: List<FocusModeSpec> = listOf(
    FocusModeSpec("free", "Foco livre", "Cronômetro contínuo para trabalho sem interrupção.", 25, 0, 0, 1, false),
    FocusModeSpec("pomodoro", "Pomodoro", "Ciclos clássicos 25/5 min com pausa longa a cada 4 ciclos.", 25, 5, 15, 4, true),
    FocusModeSpec("deep", "Concentração profunda", "Bloco estendido de 50 min com pausa regenerativa de 10 min.", 50, 10, 20, 3, true),
    FocusModeSpec("countdown", "Contagem regressiva", "Meta fechada para concluir uma entrega específica.", 45, 0, 0, 1, false),
    FocusModeSpec("custom", "Ciclos personalizados", "Defina trabalho, pausas curta/longa e quantidade de ciclos.", 35, 7, 18, 3, true)
)

fun focusModeSpec(id: String): FocusModeSpec = timeForgeModes.find { it.id == id } ?: timeForgeModes.first()

data class AmbientSceneSpec(
    val id: String,
    val name: String,
    val subtitle: String,
    val atlasCol: Int,
    val atlasRow: Int
)

val ambientScenes: List<AmbientSceneSpec> = listOf(
    AmbientSceneSpec("off", "Silêncio do Norte", "Sem som ambiente; apenas silêncio limpo.", 1, 1),
    AmbientSceneSpec("rain", "Chuva sobre o salão", "Chuva constante filtrada sobre telhado de madeira.", 0, 0),
    AmbientSceneSpec("hearth", "Lareira da forja", "Crepitar suave de brasas aquecidas em pedra.", 1, 0),
    AmbientSceneSpec("wind", "Vento boreal", "Brisa contínua dos fiordes em baixa frequência.", 0, 1)
)

// =====================================================================
// UTILITÁRIOS DE TEMPO E VALIDAÇÃO DE INTERVALOS
// =====================================================================

fun parseTimeToMinutes(value: String): Int? {
    val t = runCatching { LocalTime.parse(value.trim()) }.getOrNull() ?: return null
    return t.hour * 60 + t.minute
}

fun formatMinutesToTime(minutes: Int): String {
    val clamped = minutes.coerceIn(0, 23 * 60 + 59)
    return "%02d:%02d".format(clamped / 60, clamped % 60)
}

fun validTimeInterval(startTime: String, endTime: String): Boolean {
    val s = parseTimeToMinutes(startTime) ?: return false
    val e = parseTimeToMinutes(endTime) ?: return false
    return e > s
}

fun validIsoDate(date: String): Boolean = runCatching { LocalDate.parse(date) }.isSuccess

fun blockDurationMinutes(b: TimeBlock): Int {
    val st = parseTimeToMinutes(b.startTime) ?: return 0
    val en = parseTimeToMinutes(b.endTime) ?: return 0
    return (en - st).coerceAtLeast(0)
}

/**
 * Calcula progresso verificável de um Objetivo Pessoal a partir de Caminhos e Hábitos vinculados.
 * Retorna Pair(percentual 0..100, descrição explicável).
 */
fun strategicGoalProgress(goal: StrategicGoal, s: GameState, todayDate: String = today()): Pair<Int, String> {
    val pathIds = goal.linkedPathIds.split(',').map { it.trim() }.filter { it.isNotBlank() }.toSet()
    val habitIds = goal.linkedHabitIds.split(',').map { it.trim() }.filter { it.isNotBlank() }.toSet()
    val parts = mutableListOf<Double>()
    val details = mutableListOf<String>()

    if (pathIds.isNotEmpty()) {
        val activePaths = s.paths.filter { it.id in pathIds }
        if (activePaths.isNotEmpty()) {
            val avg = activePaths.map { p ->
                val prog = pathProgress(s, p.id)
                when {
                    p.state == 3 -> 1.0
                    prog.total > 0 -> prog.fraction.toDouble()
                    else -> 0.0
                }
            }.average()
            parts += avg
            details += "${activePaths.size} caminho(s): ${(avg * 100).toInt()}%"
        }
    }

    if (habitIds.isNotEmpty()) {
        val activeHabits = s.habits.filter { it.id in habitIds && !it.deleted }
        if (activeHabits.isNotEmpty()) {
            val end = runCatching { LocalDate.parse(todayDate) }.getOrElse { LocalDate.now() }
            val start = end.minusDays(29)
            val habitScores = activeHabits.map { h ->
                var plannedDays = 0
                var doneDays = 0
                var d = start
                while (!d.isAfter(end)) {
                    val ds = d.toString()
                    if (s.planned(h, ds)) {
                        plannedDays++
                        if (s.record(h.id, ds)?.complete == true) doneDays++
                    }
                    d = d.plusDays(1)
                }
                if (plannedDays == 0) 0.0 else doneDays.toDouble() / plannedDays.toDouble()
            }
            val avg = habitScores.average()
            parts += avg
            details += "${activeHabits.size} hábito(s) (30d): ${(avg * 100).toInt()}%"
        }
    }

    if (parts.isEmpty()) return 0 to "Vincule Caminhos ou Hábitos para medir o avanço real."
    val pct = (parts.average() * 100.0).toInt().coerceIn(0, 100)
    return pct to details.joinToString(" · ")
}

fun areaAllocatedMinutes(areaId: String, s: GameState, fromDate: String, toDate: String): Int {
    val pathArea = s.paths.associate { it.id to it.areaId }
    val taskArea = s.pathTasks.associate { it.id to (it.areaId.ifBlank { pathArea[it.pathId].orEmpty() }) }
    val habitArea = s.habits.associate { it.id to it.areaId }
    return s.timeBlocks.filter { it.date in fromDate..toDate }.sumOf { b ->
        val resolved = b.areaId.ifBlank { taskArea[b.taskId].orEmpty() }.ifBlank { pathArea[b.pathId].orEmpty() }.ifBlank { habitArea[b.habitId].orEmpty() }
        if (resolved == areaId) max(0, blockDurationMinutes(b)) else 0
    }
}
