package br.com.ascend.aetheris

import androidx.room.*
import java.time.LocalDate
import kotlin.math.max
import kotlin.math.round

val notebookCategories = listOf("Estudo", "Leitura", "Projeto", "Filosofia", "Ciência", "Idiomas", "Pessoal")
val mimirCoverNames = listOf(
    "Tomo de Couro",
    "Códice de Pergaminho",
    "Manuscrito Rúnico",
    "Runa de Ardósia",
    "Pergaminho Selado",
    "Placa de Bronze"
)
val flashcardRatingLabels = listOf("Errei", "Difícil", "Bom", "Fácil")

@Entity(
    tableName = "knowledge_notebooks",
    indices = [Index("pathId")]
)
data class KnowledgeNotebook(
    @PrimaryKey val id: String = uid(),
    val name: String = "",
    val description: String = "",
    val category: String = "Estudo",
    val icon: Int = 0,
    val cover: Int = 0,
    val pathId: String = "",
    val archived: Boolean = false,
    val created: String = today(),
    val updated: String = today(),
    @ColumnInfo(defaultValue = "''") val areaId: String = ""
)

@Entity(
    tableName = "knowledge_pages",
    foreignKeys = [
        ForeignKey(
            entity = KnowledgeNotebook::class,
            parentColumns = ["id"],
            childColumns = ["notebookId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("notebookId"), Index("pathId"), Index("taskId")]
)
data class KnowledgePage(
    @PrimaryKey val id: String = uid(),
    val notebookId: String,
    val title: String = "",
    val content: String = "",
    val tags: String = "",
    val favorite: Boolean = false,
    val archived: Boolean = false,
    val pathId: String = "",
    val taskId: String = "",
    val created: String = today(),
    val updated: String = today()
)

@Entity(
    tableName = "knowledge_page_links",
    foreignKeys = [
        ForeignKey(
            entity = KnowledgePage::class,
            parentColumns = ["id"],
            childColumns = ["sourcePageId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = KnowledgePage::class,
            parentColumns = ["id"],
            childColumns = ["targetPageId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("sourcePageId"), Index("targetPageId")]
)
data class KnowledgePageLink(
    @PrimaryKey val id: String,
    val sourcePageId: String,
    val targetPageId: String,
    val created: String = today()
)

@Entity(
    tableName = "knowledge_references",
    foreignKeys = [
        ForeignKey(
            entity = KnowledgePage::class,
            parentColumns = ["id"],
            childColumns = ["pageId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("pageId")]
)
data class KnowledgeReference(
    @PrimaryKey val id: String = uid(),
    val pageId: String,
    val author: String = "",
    val title: String = "",
    val year: String = "",
    val source: String = "",
    val url: String = "",
    val location: String = "",
    val notes: String = "",
    val created: String = today()
)

@Entity(
    tableName = "flashcards",
    foreignKeys = [
        ForeignKey(
            entity = KnowledgeNotebook::class,
            parentColumns = ["id"],
            childColumns = ["notebookId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("notebookId"), Index("pageId"), Index("nextReviewDate")]
)
data class Flashcard(
    @PrimaryKey val id: String = uid(),
    val notebookId: String,
    val pageId: String = "",
    val question: String = "",
    val answer: String = "",
    val tags: String = "",
    val repetitions: Int = 0,
    val intervalDays: Int = 0,
    val easeFactor: Double = 2.5,
    val nextReviewDate: String = today(),
    val lastReviewDate: String = "",
    val archived: Boolean = false,
    val created: String = today(),
    val updated: String = today()
)

@Entity(
    tableName = "flashcard_reviews",
    foreignKeys = [
        ForeignKey(
            entity = Flashcard::class,
            parentColumns = ["id"],
            childColumns = ["flashcardId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("flashcardId"), Index("reviewDate")]
)
data class FlashcardReview(
    @PrimaryKey val id: String = uid(),
    val flashcardId: String,
    val reviewDate: String = today(),
    val rating: Int = 2,
    val intervalBefore: Int = 0,
    val intervalAfter: Int = 1,
    val easeAfter: Double = 2.5,
    val reviewedAt: Long = System.currentTimeMillis()
)

enum class PageSort { UPDATED, CREATED, NAME }

fun parseTags(raw: String): List<String> {
    if (raw.isBlank()) return emptyList()
    val seen = LinkedHashSet<String>()
    val result = ArrayList<String>()
    raw.split(',', ';', '\n').forEach { token ->
        val clean = token.trim().removePrefix("#").trim().take(32)
        if (clean.isNotEmpty() && seen.add(clean.lowercase())) {
            result.add(clean)
        }
    }
    return result.take(16)
}

fun normalizeTags(raw: String): String = parseTags(raw).joinToString(", ")

fun archivedNotebookIds(s: GameState): Set<String> =
    s.notebooks.filter { it.archived }.mapTo(HashSet()) { it.id }

fun activeKnowledgePages(s: GameState): List<KnowledgePage> {
    val archivedNbs = archivedNotebookIds(s)
    return s.knowledgePages.filter { !it.archived && it.notebookId !in archivedNbs }
}

fun allKnowledgeTags(
    pages: List<KnowledgePage>,
    archivedNotebookIds: Set<String> = emptySet()
): List<Pair<String, Int>> {
    val counts = LinkedHashMap<String, Pair<String, Int>>()
    pages.filter { !it.archived && it.notebookId !in archivedNotebookIds }.forEach { page ->
        parseTags(page.tags).forEach { tag ->
            val key = tag.lowercase()
            val cur = counts[key]
            counts[key] = (cur?.first ?: tag) to ((cur?.second ?: 0) + 1)
        }
    }
    return counts.values.sortedWith(compareByDescending<Pair<String, Int>> { it.second }.thenBy { it.first.lowercase() })
}

fun activeKnowledgeTags(s: GameState): List<Pair<String, Int>> =
    allKnowledgeTags(s.knowledgePages, archivedNotebookIds(s))

fun searchKnowledgePages(
    pages: List<KnowledgePage>,
    query: String = "",
    notebookId: String = "",
    tag: String = "",
    favoritesOnly: Boolean = false,
    includeArchived: Boolean = false,
    sort: PageSort = PageSort.UPDATED,
    archivedNotebookIds: Set<String> = emptySet()
): List<KnowledgePage> {
    val q = query.trim().lowercase()
    val t = tag.trim().lowercase()
    val filtered = pages.filter { p ->
        val visibleByArchive = includeArchived ||
            (!p.archived && (notebookId.isNotBlank() || p.notebookId !in archivedNotebookIds))
        visibleByArchive &&
            (notebookId.isBlank() || p.notebookId == notebookId) &&
            (!favoritesOnly || p.favorite) &&
            (t.isBlank() || parseTags(p.tags).any { it.lowercase() == t }) &&
            (q.isBlank() || p.title.lowercase().contains(q) || p.content.lowercase().contains(q) || parseTags(p.tags).any { it.lowercase().contains(q) })
    }
    return when (sort) {
        PageSort.UPDATED -> filtered.sortedWith(compareByDescending<KnowledgePage> { it.updated }.thenByDescending { it.created }.thenBy { it.title.lowercase() })
        PageSort.CREATED -> filtered.sortedWith(compareByDescending<KnowledgePage> { it.created }.thenBy { it.title.lowercase() })
        PageSort.NAME -> filtered.sortedBy { it.title.lowercase() }
    }
}

fun outgoingLinkedPages(s: GameState, pageId: String): List<KnowledgePage> {
    val ids = s.knowledgeLinks.filter { it.sourcePageId == pageId }.map { it.targetPageId }.toSet()
    return s.knowledgePages.filter { it.id in ids }.sortedBy { it.title.lowercase() }
}

fun backlinkPages(s: GameState, pageId: String): List<KnowledgePage> {
    val ids = s.knowledgeLinks.filter { it.targetPageId == pageId }.map { it.sourcePageId }.toSet()
    return s.knowledgePages.filter { it.id in ids }.sortedBy { it.title.lowercase() }
}

fun dueFlashcards(s: GameState, date: String = today(), notebookId: String = ""): List<Flashcard> {
    val activeNotebooks = s.notebooks.filter { !it.archived }.mapTo(HashSet()) { it.id }
    return s.flashcards.filter { card ->
        !card.archived &&
            card.notebookId in activeNotebooks &&
            (notebookId.isBlank() || card.notebookId == notebookId) &&
            card.nextReviewDate <= date
    }.sortedWith(compareBy<Flashcard> { it.nextReviewDate }.thenBy { it.repetitions }.thenBy { it.created }.thenBy { it.id })
}

fun nextFlashcardReviewDate(s: GameState, notebookId: String = ""): String? {
    val activeNotebooks = s.notebooks.filter { !it.archived }.mapTo(HashSet()) { it.id }
    return s.flashcards
        .filter { !it.archived && it.notebookId in activeNotebooks && (notebookId.isBlank() || it.notebookId == notebookId) }
        .minOfOrNull { it.nextReviewDate }
}

data class Sm2Outcome(
    val repetitions: Int,
    val intervalDays: Int,
    val easeFactor: Double,
    val nextReviewDate: String
)

fun sanitizeEaseFactor(ease: Double): Double {
    val safe = if (ease.isNaN() || ease.isInfinite()) 2.5 else ease
    return round(safe.coerceIn(1.3, 3.5) * 100.0) / 100.0
}

fun computeNextReview(card: Flashcard, rating: Int, reviewDate: String = today()): Sm2Outcome {
    require(rating in 0..3) { "Avaliação de flashcard inválida." }
    val baseDate = LocalDate.parse(reviewDate)
    val prevReps = card.repetitions.coerceAtLeast(0)
    val prevInterval = card.intervalDays.coerceAtLeast(0)
    val prevEase = sanitizeEaseFactor(card.easeFactor)

    val quality = when (rating) {
        0 -> 1
        1 -> 3
        2 -> 4
        else -> 5
    }
    val rawEase = if (rating == 0) {
        prevEase - 0.20
    } else {
        prevEase + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02))
    }
    val nextEase = sanitizeEaseFactor(rawEase)

    val (nextReps, rawInterval) = when (rating) {
        0 -> 0 to 1
        1 -> when (prevReps) {
            0 -> 1 to 1
            1 -> 2 to 3
            else -> (prevReps + 1) to max(prevInterval + 1, round(prevInterval * 1.2).toInt())
        }
        2 -> when (prevReps) {
            0 -> 1 to 1
            1 -> 2 to 6
            else -> (prevReps + 1) to max(prevInterval + 1, round(prevInterval * nextEase).toInt())
        }
        else -> when (prevReps) {
            0 -> 1 to 4
            1 -> 2 to 10
            else -> (prevReps + 1) to max(prevInterval + 2, round(prevInterval * nextEase * 1.3).toInt())
        }
    }
    val nextInterval = rawInterval.coerceIn(1, 3650)
    val nextDate = baseDate.plusDays(nextInterval.toLong()).toString()
    return Sm2Outcome(nextReps, nextInterval, nextEase, nextDate)
}

data class SpecializationDef(
    val id: String,
    val name: String,
    val heroClass: Int,
    val atlasIndex: Int,
    val unlockLevel: Int,
    val titleGrant: String,
    val motto: String,
    val description: String,
    val effectSummary: String
)

val specializationDefs = listOf(
    SpecializationDef(
        id = "berserker",
        name = "Berserker",
        heroClass = 0,
        atlasIndex = 0,
        unlockLevel = 3,
        titleGrant = "Fúria do Norte",
        motto = "O impacto que abre passagem na tempestade.",
        description = "Canaliza disciplina feroz em sessões intensas e enfrentamento direto da estagnação.",
        effectSummary = "Emblema do Machado Rúnico e título 'Fúria do Norte' no perfil do herói."
    ),
    SpecializationDef(
        id = "sentinela",
        name = "Sentinela",
        heroClass = 0,
        atlasIndex = 1,
        unlockLevel = 3,
        titleGrant = "Guarda da Muralha",
        motto = "Nenhuma noite apaga quem sustenta a vigília.",
        description = "Honra a constância silenciosa, mantendo a linha mesmo nos dias mais frios.",
        effectSummary = "Emblema do Escudo de Ferro e título 'Guarda da Muralha' no perfil do herói."
    ),
    SpecializationDef(
        id = "erudito",
        name = "Erudito",
        heroClass = 1,
        atlasIndex = 2,
        unlockLevel = 3,
        titleGrant = "Leitor de Mímir",
        motto = "Cada página anotada é uma lâmpada contra o esquecimento.",
        description = "Transforma leituras, anotações e referências em sabedoria estruturada na Biblioteca de Mímir.",
        effectSummary = "Emblema do Tomo Aberto, título 'Leitor de Mímir' e atalho à Biblioteca no perfil."
    ),
    SpecializationDef(
        id = "arquimago_runico",
        name = "Arquimago Rúnico",
        heroClass = 1,
        atlasIndex = 3,
        unlockLevel = 3,
        titleGrant = "Tecelão do Véu",
        motto = "Conecta ideias distantes como constelações gravadas em pedra.",
        description = "Domina vínculos entre notas e revisão espaçada para consolidar memória duradoura.",
        effectSummary = "Emblema do Círculo Rúnico, título 'Tecelão do Véu' e acesso rápido às Runas da Memória."
    ),
    SpecializationDef(
        id = "protetor",
        name = "Protetor",
        heroClass = 2,
        atlasIndex = 4,
        unlockLevel = 3,
        titleGrant = "Escudo da Aurora",
        motto = "Proteger o essencial é a primeira forma de coragem.",
        description = "Zela pelos compromissos fundamentais e pela integridade do ritmo diário.",
        effectSummary = "Emblema do Escudo Ornado e título 'Escudo da Aurora' no perfil do herói."
    ),
    SpecializationDef(
        id = "druida",
        name = "Druida",
        heroClass = 2,
        atlasIndex = 5,
        unlockLevel = 3,
        titleGrant = "Voz das Raízes",
        motto = "O que cresce em silêncio atravessa invernos inteiros.",
        description = "Cultiva equilíbrio entre esforço, pausas planejadas e renovação contínua.",
        effectSummary = "Emblema da Árvore Ancestral e título 'Voz das Raízes' no perfil do herói."
    ),
    SpecializationDef(
        id = "cartografo",
        name = "Cartógrafo",
        heroClass = 3,
        atlasIndex = 6,
        unlockLevel = 3,
        titleGrant = "Mestre dos Mapas",
        motto = "Quem traça o caminho enxerga além da névoa.",
        description = "Mapeia grandes projetos, etapas e cadernos temáticos com clareza estratégica.",
        effectSummary = "Emblema da Bússola Nórdica, título 'Mestre dos Mapas' e atalho aos Caminhos."
    ),
    SpecializationDef(
        id = "patrulheiro",
        name = "Patrulheiro",
        heroClass = 3,
        atlasIndex = 7,
        unlockLevel = 3,
        titleGrant = "Olhos da Névoa",
        motto = "Cada trilha percorrida revela um novo horizonte.",
        description = "Mantém atenção ágil em campo, capturando ideias e avançando passo a passo.",
        effectSummary = "Emblema do Arco de Vigília e título 'Olhos da Névoa' no perfil do herói."
    ),
    SpecializationDef(
        id = "mestre_da_forja",
        name = "Mestre da Forja",
        heroClass = 4,
        atlasIndex = 8,
        unlockLevel = 3,
        titleGrant = "Martelo Antigo",
        motto = "A obra duradoura nasce da paciência sobre a bigorna.",
        description = "Dá forma concreta a planos, protótipos e tarefas complexas com precisão artesanal.",
        effectSummary = "Emblema da Bigorna e Martelo e título 'Martelo Antigo' no perfil do herói."
    ),
    SpecializationDef(
        id = "tecelao_de_runas",
        name = "Tecelão de Runas",
        heroClass = 4,
        atlasIndex = 9,
        unlockLevel = 3,
        titleGrant = "Entalhador de Éter",
        motto = "Entrelaça técnica, beleza e memória em cada detalhe.",
        description = "Refina sistemas pessoais de estudo e criação com estética e organização rigorosa.",
        effectSummary = "Emblema do Nó de Urnes e título 'Entalhador de Éter' no perfil do herói."
    )
)

fun specializationById(id: String): SpecializationDef? = specializationDefs.find { it.id == id }

fun specializationsForClass(heroClass: Int): List<SpecializationDef> =
    specializationDefs.filter { it.heroClass == heroClass }

fun activeSpecialization(s: GameState): SpecializationDef? {
    val p = s.player ?: return null
    return specializationById(p.specialization)?.takeIf { it.heroClass == p.heroClass }
}

fun canSelectSpecialization(s: GameState, spec: SpecializationDef): Boolean {
    val p = s.player ?: return false
    return p.heroClass == spec.heroClass && s.level >= spec.unlockLevel
}

fun canUnlockSkill(s: GameState, skill: SkillDef): Boolean {
    val p = s.player ?: return false
    if (s.assets.any { it.id == "skill:${skill.id}" }) return false
    if (skill.heroClass >= 0 && p.heroClass != skill.heroClass) return false
    if (s.level < skill.level || skillPoints(s) <= 0) return false
    if (skill.parent != null && s.assets.none { it.id == "skill:${skill.parent}" }) return false
    return true
}

fun skillRequirementText(s: GameState, skill: SkillDef): String {
    val parts = ArrayList<String>()
    if (skill.heroClass >= 0 && s.player?.heroClass != skill.heroClass) {
        parts.add("Classe ${classes.getOrElse(skill.heroClass) { "" }}")
    }
    if (s.level < skill.level) parts.add("Nível ${skill.level}")
    if (skill.parent != null && s.assets.none { it.id == "skill:${skill.parent}" }) {
        val parentName = skillDefs.find { it.id == skill.parent }?.name ?: skill.parent
        parts.add("Requer $parentName")
    }
    if (skillPoints(s) <= 0) parts.add("1 ponto de habilidade")
    return if (parts.isEmpty()) "Disponível · 1 ponto" else parts.joinToString(" · ")
}

fun validateNotebookInput(n: KnowledgeNotebook, paths: List<PersonalPath>) {
    require(n.id.isNotBlank() && n.name.isNotBlank() && n.name.length <= 120) {
        "Informe um nome para o caderno (até 120 caracteres)."
    }
    require(n.description.length <= 2000 && n.category.length <= 60) {
        "Descrição ou categoria do caderno muito longa."
    }
    require(n.icon in glyphs.indices && n.cover in mimirCoverNames.indices) {
        "Ícone ou capa de caderno inválidos."
    }
    LocalDate.parse(n.created)
    LocalDate.parse(n.updated)
    if (n.pathId.isNotBlank()) {
        require(paths.any { it.id == n.pathId }) { "Caminho vinculado ao caderno não encontrado." }
    }
}

fun validatePageInput(
    p: KnowledgePage,
    notebooks: List<KnowledgeNotebook>,
    paths: List<PersonalPath>,
    tasks: List<PathTask>
) {
    require(p.id.isNotBlank() && p.title.isNotBlank() && p.title.length <= 200) {
        "Informe um título para a página (até 200 caracteres)."
    }
    require(p.content.length <= 500_000 && p.tags.length <= 500) {
        "Conteúdo ou tags excedem o limite permitido."
    }
    require(notebooks.any { it.id == p.notebookId }) { "Caderno da página não encontrado." }
    LocalDate.parse(p.created)
    LocalDate.parse(p.updated)
    if (p.pathId.isNotBlank()) {
        require(paths.any { it.id == p.pathId }) { "Caminho vinculado à página não encontrado." }
    }
    if (p.taskId.isNotBlank()) {
        require(tasks.any { it.id == p.taskId }) { "Tarefa vinculada à página não encontrada." }
    }
}

fun validateReferenceInput(r: KnowledgeReference, pages: List<KnowledgePage>) {
    require(r.id.isNotBlank() && r.title.isNotBlank() && r.title.length <= 300) {
        "Informe o título da referência bibliográfica."
    }
    require(r.author.length <= 200 && r.year.length <= 20 && r.source.length <= 300 && r.url.length <= 1000 && r.location.length <= 120 && r.notes.length <= 4000) {
        "Um dos campos da referência excede o tamanho máximo."
    }
    require(pages.any { it.id == r.pageId }) { "Página associada à referência não encontrada." }
    LocalDate.parse(r.created)
}

fun validateFlashcardInput(
    c: Flashcard,
    notebooks: List<KnowledgeNotebook>,
    pages: List<KnowledgePage>
) {
    require(c.id.isNotBlank() && c.question.isNotBlank() && c.answer.isNotBlank()) {
        "Preencha pergunta e resposta do flashcard."
    }
    require(c.question.length <= 4000 && c.answer.length <= 10000 && c.tags.length <= 500) {
        "Texto do flashcard muito longo."
    }
    require(notebooks.any { it.id == c.notebookId }) { "Caderno do flashcard não encontrado." }
    if (c.pageId.isNotBlank()) {
        require(pages.any { it.id == c.pageId }) { "Página vinculada ao flashcard não encontrada." }
    }
    require(c.repetitions >= 0 && c.intervalDays in 0..3650 && !c.easeFactor.isNaN() && !c.easeFactor.isInfinite() && c.easeFactor in 1.3..3.5) {
        "Estado de revisão espaçada inválido."
    }
    LocalDate.parse(c.nextReviewDate)
    if (c.lastReviewDate.isNotBlank()) LocalDate.parse(c.lastReviewDate)
    LocalDate.parse(c.created)
    LocalDate.parse(c.updated)
}
