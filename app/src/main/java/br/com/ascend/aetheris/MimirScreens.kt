package br.com.ascend.aetheris

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*

@Composable
fun ArchetypeSealArt(index: Int, modifier: Modifier = Modifier.size(48.dp), dimmed: Boolean = false) {
    val art = ImageBitmap.imageResource(R.drawable.archetype_seals_atlas)
    val safeIndex = index.coerceIn(0, 9)
    Canvas(modifier.alpha(if (dimmed) 0.42f else 1f)) {
        val cols = 5
        val rows = 2
        val cellW = art.width / cols
        val cellH = art.height / rows
        val col = safeIndex % cols
        val row = safeIndex / cols
        drawImage(
            image = art,
            srcOffset = IntOffset(col * cellW, row * cellH),
            srcSize = IntSize(cellW, cellH),
            dstSize = IntSize(size.width.toInt().coerceAtLeast(1), size.height.toInt().coerceAtLeast(1)),
            filterQuality = FilterQuality.Medium
        )
    }
}

@Composable
fun MimirCodexArt(cover: Int, modifier: Modifier = Modifier.size(60.dp), accent: Color = Gold) {
    val shape = CutCornerShape(4.dp)
    Box(
        modifier
            .clip(shape)
            .background(Color(0xFF121417))
            .border(1.dp, accent.copy(alpha = 0.48f), shape)
    ) {
        ArtTile(R.drawable.mimir_codex_atlas, cover.coerceIn(0, 5), 2, Modifier.matchParentSize())
    }
}

@Composable
private fun MimirLibraryBanner(
    notebooksCount: Int,
    pagesCount: Int,
    dueCount: Int,
    onStartReview: () -> Unit
) {
    val minimal = LocalAppearance.current.minimal
    val shape = CutCornerShape(topStart = 8.dp, topEnd = 2.dp, bottomEnd = 8.dp, bottomStart = 2.dp)
    if (minimal) {
        AetherSurface(SurfaceKind.Heroic) {
            Eyebrow("A Biblioteca de Mímir", Gold)
            Text("Conhecimento e Memória", style = MaterialTheme.typography.headlineSmall)
            Text(
                "$notebooksCount ${if (notebooksCount == 1) "caderno" else "cadernos"} · $pagesCount ${if (pagesCount == 1) "página" else "páginas"} · $dueCount ${if (dueCount == 1) "revisão pendente" else "revisões pendentes"}",
                color = Muted,
                fontSize = 12.sp
            )
            if (dueCount > 0) {
                SagaButton("Revisar Runas ($dueCount)", onStartReview, Modifier.fillMaxWidth())
            }
        }
        return
    }
    Box(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 138.dp)
            .clip(shape)
            .border(1.dp, OldBronze.copy(alpha = 0.52f), shape)
    ) {
        Image(
            painterResource(R.drawable.mimir_library),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center
        )
        Box(
            Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color(0xCC0D1013),
                        0.45f to Color(0x99101419),
                        1f to Color(0xEE0C0F12)
                    )
                )
        )
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Eyebrow("A Biblioteca de Mímir", Gold)
            Text(
                "Acervo Pessoal de Sabedoria",
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFFECE6D9)
            )
            Text(
                "$notebooksCount ${if (notebooksCount == 1) "caderno" else "cadernos"} · $pagesCount ${if (pagesCount == 1) "página" else "páginas"} · $dueCount ${if (dueCount == 1) "revisão pendente" else "revisões pendentes"}",
                color = Color(0xFFC5BEB0),
                fontSize = 12.sp
            )
            if (dueCount > 0) {
                Spacer(Modifier.height(2.dp))
                SagaButton("Iniciar Runas da Memória ($dueCount)", onStartReview, Modifier.fillMaxWidth())
            }
        }
    }
}

private fun parseInlineMarkdown(line: String, codeBg: Color, linkColor: Color): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        val n = line.length
        while (i < n) {
            if (line.startsWith("**", i)) {
                val end = line.indexOf("**", i + 2)
                if (end > i + 2) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(line.substring(i + 2, end))
                    }
                    i = end + 2
                    continue
                }
            }
            if (line[i] == '`') {
                val end = line.indexOf('`', i + 1)
                if (end > i + 1) {
                    withStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = codeBg)) {
                        append(" ${line.substring(i + 1, end)} ")
                    }
                    i = end + 1
                    continue
                }
            }
            if (line[i] == '*' && (i + 1 < n && line[i + 1] != ' ')) {
                val end = line.indexOf('*', i + 1)
                if (end > i + 1) {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(line.substring(i + 1, end))
                    }
                    i = end + 1
                    continue
                }
            }
            if (line[i] == '[') {
                val closeLabel = line.indexOf(']', i + 1)
                if (closeLabel > i + 1 && closeLabel + 1 < n && line[closeLabel + 1] == '(') {
                    val closeUrl = line.indexOf(')', closeLabel + 2)
                    if (closeUrl > closeLabel + 2) {
                        val label = line.substring(i + 1, closeLabel)
                        val url = line.substring(closeLabel + 2, closeUrl)
                        withStyle(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)) {
                            append(label)
                        }
                        if (url.isNotBlank() && url != label) {
                            withStyle(SpanStyle(color = linkColor.copy(alpha = 0.72f), fontSize = 11.sp)) {
                                append(" ($url)")
                            }
                        }
                        i = closeUrl + 1
                        continue
                    }
                }
            }
            append(line[i])
            i++
        }
    }
}

@Composable
fun MarkdownPreview(text: String, modifier: Modifier = Modifier) {
    val dark = LocalAppearance.current.dark
    val codeBg = if (dark) Color(0xFF1F252C) else Color(0xFFE4DDD0)
    val quoteBar = Gold.copy(alpha = 0.75f)
    val inkColor = Ink
    val mutedColor = Muted
    val linkColor = Blue
    if (text.isBlank()) {
        Text("Página em branco. Alterne para Editar para escrever suas notas em Markdown.", color = mutedColor, fontSize = 13.sp)
        return
    }
    val lines = remember(text) { text.replace("\r\n", "\n").split('\n') }
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        var idx = 0
        while (idx < lines.size) {
            val raw = lines[idx]
            val trimmed = raw.trim()
            if (trimmed.startsWith("```")) {
                val codeLines = ArrayList<String>()
                idx++
                while (idx < lines.size && !lines[idx].trim().startsWith("```")) {
                    codeLines.add(lines[idx])
                    idx++
                }
                if (idx < lines.size) idx++
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(codeBg)
                        .border(1.dp, ColdIron.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        codeLines.joinToString("\n"),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = inkColor
                    )
                }
                continue
            }
            if (trimmed.isEmpty()) {
                Spacer(Modifier.height(2.dp))
                idx++
                continue
            }
            when {
                trimmed.startsWith("### ") -> Text(
                    parseInlineMarkdown(trimmed.removePrefix("### "), codeBg, linkColor),
                    style = MaterialTheme.typography.titleMedium,
                    color = Blue,
                    fontWeight = FontWeight.SemiBold
                )
                trimmed.startsWith("## ") -> Text(
                    parseInlineMarkdown(trimmed.removePrefix("## "), codeBg, linkColor),
                    style = MaterialTheme.typography.titleLarge,
                    color = inkColor
                )
                trimmed.startsWith("# ") -> Text(
                    parseInlineMarkdown(trimmed.removePrefix("# "), codeBg, linkColor),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Gold
                )
                trimmed.startsWith("> ") -> Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                ) {
                    Box(Modifier.width(3.dp).heightIn(min = 20.dp).background(quoteBar))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        parseInlineMarkdown(trimmed.removePrefix("> "), codeBg, linkColor),
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        color = mutedColor,
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )
                }
                trimmed.startsWith("- ") || trimmed.startsWith("* ") -> Row(
                    Modifier.fillMaxWidth().padding(start = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("◆", color = Gold, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
                    Text(
                        parseInlineMarkdown(trimmed.drop(2), codeBg, linkColor),
                        color = inkColor,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
                Regex("^\\d+\\.\\s+.*").matches(trimmed) -> {
                    val dot = trimmed.indexOf('.')
                    val num = trimmed.substring(0, dot + 1)
                    val rest = trimmed.substring(dot + 1).trim()
                    Row(
                        Modifier.fillMaxWidth().padding(start = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(num, color = Gold, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            parseInlineMarkdown(rest, codeBg, linkColor),
                            color = inkColor,
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                else -> Text(
                    parseInlineMarkdown(raw, codeBg, linkColor),
                    color = inkColor,
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )
            }
            idx++
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MimirLibraryScreen(
    s: GameState,
    vm: GameViewModel,
    onOpenNotebook: (String) -> Unit,
    onOpenPage: (String) -> Unit,
    onStartReview: (String) -> Unit
) {
    val day = currentDay()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var search by rememberSaveable { mutableStateOf("") }
    var sortIndex by rememberSaveable { mutableIntStateOf(0) }
    var selectedTag by rememberSaveable { mutableStateOf("") }
    var showArchived by rememberSaveable { mutableStateOf(false) }
    var notebookEditorId by rememberSaveable { mutableStateOf<String?>(null) }
    var deleteNotebookCandidate by remember { mutableStateOf<KnowledgeNotebook?>(null) }
    var quickCardOpen by rememberSaveable { mutableStateOf(false) }

    val archivedNbIds = remember(s.notebooks) { archivedNotebookIds(s) }
    val activeNotebooks = remember(s.notebooks) { s.notebooks.filter { !it.archived } }
    val archivedNotebooks = remember(s.notebooks) { s.notebooks.filter { it.archived } }
    val activePages = remember(s.notebooks, s.knowledgePages) { activeKnowledgePages(s) }
    val hasAnyArchivedPages = remember(s.notebooks, s.knowledgePages) {
        s.knowledgePages.any { it.archived || it.notebookId in archivedNbIds }
    }
    val dueCards = remember(s.notebooks, s.flashcards, day) { dueFlashcards(s, day) }
    val sort = PageSort.entries[sortIndex.coerceIn(PageSort.entries.indices)]
    val allTags = remember(s.notebooks, s.knowledgePages) { activeKnowledgeTags(s) }

    Page {
        MimirLibraryBanner(
            notebooksCount = activeNotebooks.size,
            pagesCount = activePages.size,
            dueCount = dueCards.size,
            onStartReview = { onStartReview("") }
        )
        FormField(search, { search = it }, "Buscar páginas por título, texto ou tag", modifier = Modifier.testTag("mimir_search"))
        AetherisTabs(listOf("Cadernos", "Recentes", "Favoritos", "Tags", "Revisões"), tab, { tab = it })

        if (search.isNotBlank() && tab == 0) {
            val matches = remember(s.knowledgePages, archivedNbIds, search, sort, showArchived) {
                searchKnowledgePages(
                    s.knowledgePages,
                    query = search,
                    includeArchived = showArchived,
                    sort = sort,
                    archivedNotebookIds = archivedNbIds
                )
            }
            Eyebrow("Resultados da busca (${matches.size})", Blue)
            if (matches.isEmpty()) {
                Empty("Nenhuma página encontrada para \"$search\".")
            } else {
                matches.forEach { pg ->
                    val nbName = s.notebooks.find { it.id == pg.notebookId }?.name ?: "Caderno"
                    KnowledgePageRow(pg, nbName, { onOpenPage(pg.id) }, { vm.run { vm.repo.togglePageFavorite(pg.id) } })
                }
            }
            Hairline()
        }

        when (tab) {
            0 -> {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Action("+ Novo caderno", { notebookEditorId = "__new__" }, Modifier.weight(1f))
                    if (dueCards.isNotEmpty()) {
                        SagaButton("Revisar (${dueCards.size})", { onStartReview("") }, Modifier.weight(1f), secondary = true)
                    }
                }
                val visible = if (showArchived) s.notebooks else activeNotebooks
                if (visible.isEmpty()) {
                    Empty("Sua Biblioteca de Mímir ainda está vazia. Crie um caderno para organizar estudos, leituras e anotações pessoais.")
                } else {
                    visible.forEach { nb ->
                        val nbPages = s.knowledgePages.count { it.notebookId == nb.id && !it.archived }
                        val nbCards = s.flashcards.count { it.notebookId == nb.id && !it.archived }
                        val nbDue = dueCards.count { it.notebookId == nb.id }
                        val linkedPath = s.paths.find { it.id == nb.pathId }
                        AetherSurface(SurfaceKind.Operational, Modifier.testTag("notebook:${nb.id}")) {
                            Row(
                                Modifier.fillMaxWidth().clickable { onOpenNotebook(nb.id) },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                MimirCodexArt(nb.cover, Modifier.size(62.dp), accent = if (nb.archived) Muted else Gold)
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Eyebrow(
                                        "${nb.category}${if (nb.archived) " · Arquivado" else ""}",
                                        if (nb.archived) Muted else Gold
                                    )
                                    Text(nb.name, style = MaterialTheme.typography.titleLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    if (nb.description.isNotBlank()) {
                                        Text(nb.description, color = Muted, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    }
                                    Text(
                                        "$nbPages ${if (nbPages == 1) "página" else "páginas"} · $nbCards ${if (nbCards == 1) "flashcard" else "flashcards"}${if (nbDue > 0) " · $nbDue pendentes" else ""}",
                                        color = if (nbDue > 0) Blue else Muted,
                                        fontSize = 11.sp
                                    )
                                    linkedPath?.let { lp ->
                                        Text("Caminho: ${lp.name}", color = Violet, fontSize = 11.sp)
                                    }
                                }
                            }
                            FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                TextButton({ onOpenNotebook(nb.id) }) { Text("Abrir", fontSize = 12.sp) }
                                TextButton({ notebookEditorId = nb.id }) { Text("Editar", fontSize = 12.sp) }
                                TextButton({ vm.run { vm.repo.archiveNotebook(nb.id, !nb.archived) } }) {
                                    Text(if (nb.archived) "Reativar" else "Arquivar", fontSize = 12.sp)
                                }
                                TextButton({ deleteNotebookCandidate = nb }) { Text("Excluir", color = Muted, fontSize = 12.sp) }
                            }
                        }
                    }
                }
                if (archivedNotebooks.isNotEmpty()) {
                    TextButton({ showArchived = !showArchived }) {
                        Text(if (showArchived) "Ocultar cadernos arquivados" else "Mostrar arquivados (${archivedNotebooks.size})")
                    }
                }
            }
            1 -> {
                ChipRow(listOf("Atualização", "Criação", "Nome"), sortIndex, { sortIndex = it })
                val list = remember(s.knowledgePages, archivedNbIds, search, sort, showArchived) {
                    searchKnowledgePages(
                        s.knowledgePages,
                        query = search,
                        includeArchived = showArchived,
                        sort = sort,
                        archivedNotebookIds = archivedNbIds
                    )
                }
                if (list.isEmpty()) {
                    Empty("Nenhuma página registrada ainda. Abra um caderno para criar sua primeira nota.")
                } else {
                    list.forEach { pg ->
                        val nb = s.notebooks.find { it.id == pg.notebookId }
                        val nbName = (nb?.name ?: "Caderno") + if (nb?.archived == true) " (arquivado)" else ""
                        KnowledgePageRow(pg, nbName, { onOpenPage(pg.id) }, { vm.run { vm.repo.togglePageFavorite(pg.id) } })
                    }
                }
                if (hasAnyArchivedPages) {
                    TextButton({ showArchived = !showArchived }) {
                        Text(if (showArchived) "Ocultar itens arquivados" else "Incluir itens arquivados")
                    }
                }
            }
            2 -> {
                val favs = remember(s.knowledgePages, archivedNbIds, search, sort, showArchived) {
                    searchKnowledgePages(
                        s.knowledgePages,
                        query = search,
                        favoritesOnly = true,
                        includeArchived = showArchived,
                        sort = sort,
                        archivedNotebookIds = archivedNbIds
                    )
                }
                if (favs.isEmpty()) {
                    Empty("Nenhuma página favoritada. Marque páginas essenciais com ★ para acesso rápido.")
                } else {
                    favs.forEach { pg ->
                        val nb = s.notebooks.find { it.id == pg.notebookId }
                        val nbName = (nb?.name ?: "Caderno") + if (nb?.archived == true) " (arquivado)" else ""
                        KnowledgePageRow(pg, nbName, { onOpenPage(pg.id) }, { vm.run { vm.repo.togglePageFavorite(pg.id) } })
                    }
                }
                if (hasAnyArchivedPages) {
                    TextButton({ showArchived = !showArchived }) {
                        Text(if (showArchived) "Ocultar itens arquivados" else "Incluir itens arquivados")
                    }
                }
            }
            3 -> {
                if (allTags.isEmpty()) {
                    Empty("Nenhuma tag criada ainda. Adicione tags separadas por vírgula ao editar uma página.")
                } else {
                    Eyebrow("Tags da Biblioteca (${allTags.size})", Gold)
                    FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        allTags.forEach { (tag, count) ->
                            val selected = selectedTag.equals(tag, ignoreCase = true)
                            FilterChip(
                                selected = selected,
                                onClick = { selectedTag = if (selected) "" else tag },
                                label = { Text("#$tag ($count)", fontSize = 12.sp) }
                            )
                        }
                    }
                    val tagged = remember(s.knowledgePages, archivedNbIds, selectedTag, search, sort) {
                        if (selectedTag.isBlank()) emptyList()
                        else searchKnowledgePages(
                            s.knowledgePages,
                            query = search,
                            tag = selectedTag,
                            sort = sort,
                            archivedNotebookIds = archivedNbIds
                        )
                    }
                    if (selectedTag.isNotBlank()) {
                        Eyebrow("Páginas com #$selectedTag (${tagged.size})", Blue)
                        tagged.forEach { pg ->
                            val nbName = s.notebooks.find { it.id == pg.notebookId }?.name ?: "Caderno"
                            KnowledgePageRow(pg, nbName, { onOpenPage(pg.id) }, { vm.run { vm.repo.togglePageFavorite(pg.id) } })
                        }
                    }
                }
            }
            4 -> {
                AetherSurface(SurfaceKind.Heroic) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MimirCodexArt(3, Modifier.size(54.dp), accent = Blue)
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Eyebrow("Runas da Memória", Blue)
                            Text(
                                if (dueCards.isEmpty()) "Sem revisões pendentes hoje" else "${dueCards.size} ${if (dueCards.size == 1) "runa aguardando revisão" else "runas aguardando revisão"}",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                "Repetição espaçada determinística (SM-2) sem XP ou moedas artificiais.",
                                color = Muted,
                                fontSize = 12.sp
                            )
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Action(
                            if (dueCards.isEmpty()) "Abrir painel de revisão" else "Iniciar revisão (${dueCards.size})",
                            { onStartReview("") },
                            Modifier.weight(1f),
                            enabled = dueCards.isNotEmpty()
                        )
                        if (activeNotebooks.isNotEmpty()) {
                            SagaButton(
                                "+ Flashcard",
                                { quickCardOpen = true },
                                Modifier.weight(1f),
                                secondary = true
                            )
                        }
                    }
                }
                if (activeNotebooks.isEmpty()) {
                    Empty("Crie um caderno primeiro para adicionar flashcards de revisão espaçada.")
                } else {
                    activeNotebooks.forEach { nb ->
                        val nbCards = s.flashcards.filter { it.notebookId == nb.id && !it.archived }
                        val nbDue = dueCards.count { it.notebookId == nb.id }
                        if (nbCards.isNotEmpty()) {
                            Panel(accent = if (nbDue > 0) Blue else Muted) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(nb.name, style = MaterialTheme.typography.titleMedium)
                                        Text("${nbCards.size} flashcards · $nbDue pendentes hoje", color = if (nbDue > 0) Blue else Muted, fontSize = 12.sp)
                                    }
                                    if (nbDue > 0) {
                                        TextButton({ onStartReview(nb.id) }) { Text("Revisar") }
                                    } else {
                                        TextButton({ onOpenNotebook(nb.id) }) { Text("Abrir") }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    notebookEditorId?.let { id ->
        val editing = if (id == "__new__") KnowledgeNotebook() else s.notebooks.find { it.id == id }
        if (editing == null) {
            notebookEditorId = null
        } else {
            val draftPrefix = "nb_editor:$id:"
            NotebookEditorDialog(
                initial = editing,
                vm = vm,
                draftPrefix = draftPrefix,
                paths = s.paths.filter { it.state in 0..2 },
                onDismiss = {
                    vm.clearDraftsWithPrefix(draftPrefix)
                    notebookEditorId = null
                },
                onSave = { updated ->
                    vm.run {
                        vm.repo.saveNotebook(updated)
                        vm.clearDraftsWithPrefix(draftPrefix)
                        notebookEditorId = null
                    }
                }
            )
        }
    }

    deleteNotebookCandidate?.let { nb ->
        AetherDialog(
            onDismissRequest = { deleteNotebookCandidate = null },
            title = "Excluir caderno?",
            confirmButton = {
                SagaButton("Excluir", { vm.run { vm.repo.deleteNotebook(nb.id); deleteNotebookCandidate = null } }, destructive = true)
            },
            dismissButton = { TextButton({ deleteNotebookCandidate = null }) { Text("Cancelar") } }
        ) {
            Text("O caderno \"${nb.name}\", todas as suas páginas, referências, ligações e flashcards associados serão removidos permanentemente.")
        }
    }

    if (quickCardOpen && activeNotebooks.isNotEmpty()) {
        val draftPrefix = "fc_quick:"
        FlashcardEditorDialog(
            initial = Flashcard(notebookId = activeNotebooks.first().id),
            vm = vm,
            draftPrefix = draftPrefix,
            notebooks = activeNotebooks,
            pages = activePages,
            onDismiss = {
                vm.clearDraftsWithPrefix(draftPrefix)
                quickCardOpen = false
            },
            onSave = { updated ->
                vm.run {
                    vm.repo.saveFlashcard(updated)
                    vm.clearDraftsWithPrefix(draftPrefix)
                    quickCardOpen = false
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun KnowledgePageRow(
    page: KnowledgePage,
    notebookName: String,
    onOpen: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val tags = remember(page.tags) { parseTags(page.tags) }
    AetherSurface(SurfaceKind.Operational, Modifier.testTag("page:${page.id}")) {
        Row(
            Modifier.fillMaxWidth().clickable(onClick = onOpen),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    "$notebookName · ${displayDate(page.updated)}${if (page.archived) " · Arquivada" else ""}",
                    color = Muted,
                    fontSize = 11.sp
                )
                Text(page.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (page.content.isNotBlank()) {
                    Text(
                        page.content.replace("\n", " ").trim(),
                        color = Muted,
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (tags.isNotEmpty()) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        tags.take(4).forEach { tag ->
                            Text("#$tag", color = Blue, fontSize = 11.sp)
                        }
                    }
                }
            }
            TextButton(onToggleFavorite, Modifier.semantics { contentDescription = if (page.favorite) "Remover favorito" else "Favoritar página" }) {
                Text(if (page.favorite) "★" else "☆", color = if (page.favorite) Gold else Muted, fontSize = 18.sp)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NotebookDetailScreen(
    notebookId: String,
    s: GameState,
    vm: GameViewModel,
    onBack: () -> Unit,
    onOpenPage: (String) -> Unit,
    onStartReview: (String) -> Unit
) {
    val nb = s.notebooks.find { it.id == notebookId }
    if (nb == null) {
        Page {
            Empty("Caderno não encontrado ou removido.")
            SagaButton("Voltar", onBack)
        }
        return
    }
    val day = currentDay()
    var subTab by rememberSaveable(notebookId) { mutableIntStateOf(0) }
    var search by rememberSaveable(notebookId) { mutableStateOf("") }
    var sortIndex by rememberSaveable(notebookId) { mutableIntStateOf(0) }
    var showArchived by rememberSaveable(notebookId) { mutableStateOf(false) }
    var editNotebook by rememberSaveable(notebookId) { mutableStateOf(false) }
    var newPageDialog by rememberSaveable(notebookId) { mutableStateOf(false) }
    var movePageCandidate by remember { mutableStateOf<KnowledgePage?>(null) }
    var deletePageCandidate by remember { mutableStateOf<KnowledgePage?>(null) }
    var cardEditorId by rememberSaveable(notebookId) { mutableStateOf<String?>(null) }
    var deleteCardCandidate by remember { mutableStateOf<Flashcard?>(null) }

    val sort = PageSort.entries[sortIndex.coerceIn(PageSort.entries.indices)]
    val pages = remember(s.knowledgePages, notebookId, search, showArchived, sort) {
        searchKnowledgePages(s.knowledgePages, query = search, notebookId = notebookId, includeArchived = showArchived, sort = sort)
    }
    val nbCards = remember(s.flashcards, notebookId, showArchived) {
        s.flashcards.filter { it.notebookId == notebookId && (showArchived || !it.archived) }
    }
    val nbDue = remember(s.notebooks, s.flashcards, notebookId, day) { dueFlashcards(s, day, notebookId) }
    val linkedPath = s.paths.find { it.id == nb.pathId }

    Page {
        AetherSurface(SurfaceKind.Operational) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MimirCodexArt(nb.cover, Modifier.size(68.dp), accent = if (nb.archived) Muted else Gold)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Eyebrow("${nb.category}${if (nb.archived) " · Arquivado" else ""}", Gold)
                    Text(nb.name, style = MaterialTheme.typography.headlineSmall)
                    if (nb.description.isNotBlank()) Text(nb.description, color = Muted, fontSize = 13.sp)
                    linkedPath?.let { Text("Vinculado ao caminho: ${it.name}", color = Violet, fontSize = 11.sp) }
                }
            }
            FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton({ editNotebook = true }) { Text("Editar caderno") }
                TextButton({ vm.run { vm.repo.archiveNotebook(nb.id, !nb.archived) } }) {
                    Text(if (nb.archived) "Reativar" else "Arquivar")
                }
                if (nbDue.isNotEmpty() && !nb.archived) {
                    TextButton({ onStartReview(nb.id) }) { Text("Revisar (${nbDue.size})", color = Blue) }
                }
            }
        }

        AetherisTabs(listOf("Páginas (${pages.size})", "Flashcards (${nbCards.size})"), subTab, { subTab = it })

        when (subTab) {
            0 -> {
                Action("+ Nova página", { newPageDialog = true }, Modifier.fillMaxWidth())
                FormField(search, { search = it }, "Buscar neste caderno")
                ChipRow(listOf("Atualização", "Criação", "Nome"), sortIndex, { sortIndex = it })
                if (pages.isEmpty()) {
                    Empty("Nenhuma página neste caderno. Crie uma página para registrar estudos, resumos ou ideias.")
                } else {
                    pages.forEach { pg ->
                        AetherSurface(SurfaceKind.Operational) {
                            Row(Modifier.fillMaxWidth().clickable { onOpenPage(pg.id) }, verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(pg.title, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        "Atualizada em ${displayDate(pg.updated)}${if (pg.tags.isNotBlank()) " · ${pg.tags}" else ""}",
                                        color = Muted,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                TextButton({ vm.run { vm.repo.togglePageFavorite(pg.id) } }) {
                                    Text(if (pg.favorite) "★" else "☆", color = if (pg.favorite) Gold else Muted, fontSize = 18.sp)
                                }
                            }
                            FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                TextButton({ onOpenPage(pg.id) }) { Text("Abrir", fontSize = 12.sp) }
                                TextButton({ vm.run { vm.repo.duplicatePage(pg.id) } }) { Text("Duplicar", fontSize = 12.sp) }
                                if (s.notebooks.size > 1) {
                                    TextButton({ movePageCandidate = pg }) { Text("Mover", fontSize = 12.sp) }
                                }
                                TextButton({ vm.run { vm.repo.archivePage(pg.id, !pg.archived) } }) {
                                    Text(if (pg.archived) "Reativar" else "Arquivar", fontSize = 12.sp)
                                }
                                TextButton({ deletePageCandidate = pg }) { Text("Excluir", color = Muted, fontSize = 12.sp) }
                            }
                        }
                    }
                }
            }
            1 -> {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Action("+ Novo flashcard", { cardEditorId = "__new__" }, Modifier.weight(1f))
                    if (nbDue.isNotEmpty() && !nb.archived) {
                        SagaButton("Revisar (${nbDue.size})", { onStartReview(nb.id) }, Modifier.weight(1f), secondary = true)
                    }
                }
                if (nbCards.isEmpty()) {
                    Empty("Nenhum flashcard neste caderno. Crie perguntas e respostas para treinar sua memória com repetição espaçada.")
                } else {
                    nbCards.forEach { card ->
                        val isDue = !card.archived && card.nextReviewDate <= day
                        AetherSurface(if (isDue) SurfaceKind.Heroic else SurfaceKind.Operational) {
                            Eyebrow(
                                if (card.archived) "Arquivado"
                                else if (isDue) "Pendente hoje"
                                else "Próxima revisão: ${displayDate(card.nextReviewDate)}",
                                if (isDue) Blue else Muted
                            )
                            Text(card.question, style = MaterialTheme.typography.titleMedium)
                            Text(card.answer, color = Muted, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text(
                                "Repetições: ${card.repetitions} · Intervalo: ${card.intervalDays}d · Facilidade: ${"%.2f".format(card.easeFactor)}",
                                color = Muted,
                                fontSize = 11.sp
                            )
                            FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                TextButton({ cardEditorId = card.id }) { Text("Editar", fontSize = 12.sp) }
                                TextButton({ vm.run { vm.repo.archiveFlashcard(card.id, !card.archived) } }) {
                                    Text(if (card.archived) "Reativar" else "Arquivar", fontSize = 12.sp)
                                }
                                TextButton({ deleteCardCandidate = card }) { Text("Excluir", color = Muted, fontSize = 12.sp) }
                            }
                        }
                    }
                }
            }
        }
        TextButton({ showArchived = !showArchived }) {
            Text(if (showArchived) "Ocultar itens arquivados" else "Mostrar itens arquivados")
        }
    }

    if (editNotebook) {
        val draftPrefix = "nb_detail_edit:${nb.id}:"
        NotebookEditorDialog(
            initial = nb,
            vm = vm,
            draftPrefix = draftPrefix,
            paths = s.paths.filter { it.state in 0..2 },
            onDismiss = {
                vm.clearDraftsWithPrefix(draftPrefix)
                editNotebook = false
            },
            onSave = { updated ->
                vm.run {
                    vm.repo.saveNotebook(updated)
                    vm.clearDraftsWithPrefix(draftPrefix)
                    editNotebook = false
                }
            }
        )
    }

    if (newPageDialog) {
        val draftPrefix = "new_page:${nb.id}:"
        var title by remember(draftPrefix) { mutableStateOf(vm.getDraft("${draftPrefix}title", "")) }
        var tags by remember(draftPrefix) { mutableStateOf(vm.getDraft("${draftPrefix}tags", "")) }
        var content by remember(draftPrefix) { mutableStateOf(vm.getDraft("${draftPrefix}content", "")) }
        var discardConfirm by remember { mutableStateOf(false) }
        val dirty = title.isNotBlank() || tags.isNotBlank() || content.isNotBlank()
        FullForm(
            "Nova página",
            onDismiss = {
                if (dirty) discardConfirm = true
                else {
                    vm.clearDraftsWithPrefix(draftPrefix)
                    newPageDialog = false
                }
            },
            onSave = {
                val created = KnowledgePage(notebookId = nb.id, title = title, content = content, tags = tags, pathId = nb.pathId)
                vm.run {
                    vm.repo.savePage(created)
                    vm.clearDraftsWithPrefix(draftPrefix)
                    newPageDialog = false
                    onOpenPage(created.id)
                }
            }
        ) {
            FormField(title, { title = it.take(200); vm.setDraft("${draftPrefix}title", title) }, "Título da página")
            FormField(tags, { tags = it; vm.setDraft("${draftPrefix}tags", tags) }, "Tags (separadas por vírgula)")
            FormField(content, { content = it; vm.setDraft("${draftPrefix}content", content) }, "Conteúdo em Markdown (# título, **negrito**, - lista, > citação)", multi = true)
        }
        if (discardConfirm) {
            DiscardEdits(
                onDiscard = {
                    vm.clearDraftsWithPrefix(draftPrefix)
                    discardConfirm = false
                    newPageDialog = false
                },
                onKeep = { discardConfirm = false }
            )
        }
    }

    movePageCandidate?.let { pg ->
        val others = s.notebooks.filter { it.id != pg.notebookId }
        AetherDialog(
            onDismissRequest = { movePageCandidate = null },
            title = "Mover página",
            confirmButton = { TextButton({ movePageCandidate = null }) { Text("Fechar") } }
        ) {
            Text("Escolha o caderno de destino para \"${pg.title}\":", color = Muted, fontSize = 13.sp)
            others.forEach { dest ->
                TextButton({ vm.run { vm.repo.movePage(pg.id, dest.id); movePageCandidate = null } }, Modifier.fillMaxWidth()) {
                    Text(dest.name)
                }
            }
        }
    }

    deletePageCandidate?.let { pg ->
        AetherDialog(
            onDismissRequest = { deletePageCandidate = null },
            title = "Excluir página?",
            confirmButton = {
                SagaButton("Excluir", { vm.run { vm.repo.deletePage(pg.id); deletePageCandidate = null } }, destructive = true)
            },
            dismissButton = { TextButton({ deletePageCandidate = null }) { Text("Cancelar") } }
        ) {
            Text("A página \"${pg.title}\", suas ligações e referências bibliográficas serão removidas. Flashcards vinculados permanecem no caderno.")
        }
    }

    cardEditorId?.let { id ->
        val editing = if (id == "__new__") Flashcard(notebookId = nb.id) else s.flashcards.find { it.id == id }
        if (editing == null) {
            cardEditorId = null
        } else {
            val draftPrefix = "fc_nb:${nb.id}:$id:"
            FlashcardEditorDialog(
                initial = editing,
                vm = vm,
                draftPrefix = draftPrefix,
                notebooks = s.notebooks,
                pages = s.knowledgePages.filter { it.notebookId == editing.notebookId && !it.archived },
                onDismiss = {
                    vm.clearDraftsWithPrefix(draftPrefix)
                    cardEditorId = null
                },
                onSave = { updated ->
                    vm.run {
                        vm.repo.saveFlashcard(updated)
                        vm.clearDraftsWithPrefix(draftPrefix)
                        cardEditorId = null
                    }
                }
            )
        }
    }

    deleteCardCandidate?.let { card ->
        AetherDialog(
            onDismissRequest = { deleteCardCandidate = null },
            title = "Excluir flashcard?",
            confirmButton = {
                SagaButton("Excluir", { vm.run { vm.repo.deleteFlashcard(card.id); deleteCardCandidate = null } }, destructive = true)
            },
            dismissButton = { TextButton({ deleteCardCandidate = null }) { Text("Cancelar") } }
        ) {
            Text("O flashcard e seu histórico de revisões serão excluídos.")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PageDetailScreen(
    pageId: String,
    s: GameState,
    vm: GameViewModel,
    onBack: () -> Unit,
    onOpenPage: (String) -> Unit
) {
    val page = s.knowledgePages.find { it.id == pageId }
    if (page == null) {
        Page {
            Empty("Página não encontrada ou removida.")
            SagaButton("Voltar", onBack)
        }
        return
    }
    val notebook = s.notebooks.find { it.id == page.notebookId }
    val draftPrefix = "page_edit:$pageId:"
    var modeTab by rememberSaveable(pageId) { mutableIntStateOf(0) } // 0=Visualizar, 1=Editar
    var title by remember(pageId, page.updated) { mutableStateOf(vm.getDraft("${draftPrefix}title", page.title)) }
    var content by remember(pageId, page.updated) { mutableStateOf(vm.getDraft("${draftPrefix}content", page.content)) }
    var tags by remember(pageId, page.updated) { mutableStateOf(vm.getDraft("${draftPrefix}tags", page.tags)) }
    var linkedPathId by remember(pageId, page.updated) { mutableStateOf(vm.getDraft("${draftPrefix}path", page.pathId)) }
    var linkedTaskId by remember(pageId, page.updated) { mutableStateOf(vm.getDraft("${draftPrefix}task", page.taskId)) }
    var confirmDiscardEdit by remember { mutableStateOf(false) }

    val hasUnsavedChanges = title != page.title || content != page.content || tags != page.tags ||
        linkedPathId != page.pathId || linkedTaskId != page.taskId

    var linkPickerOpen by rememberSaveable(pageId) { mutableStateOf(false) }
    var refEditorId by rememberSaveable(pageId) { mutableStateOf<String?>(null) }
    var flashcardCreator by rememberSaveable(pageId) { mutableStateOf(false) }
    var moveDialog by remember { mutableStateOf(false) }
    var deleteDialog by remember { mutableStateOf(false) }

    val archivedNbIds = remember(s.notebooks) { archivedNotebookIds(s) }
    val outgoing = remember(s.knowledgeLinks, s.knowledgePages, pageId) { outgoingLinkedPages(s, pageId) }
    val backlinks = remember(s.knowledgeLinks, s.knowledgePages, pageId) { backlinkPages(s, pageId) }
    val refs = remember(s.knowledgeReferences, pageId) { s.knowledgeReferences.filter { it.pageId == pageId } }
    val pageCards = remember(s.flashcards, pageId) { s.flashcards.filter { it.pageId == pageId && !it.archived } }
    val activePaths = remember(s.paths) { s.paths.filter { it.state in 0..2 } }
    val activeTasks = remember(s.pathTasks, linkedPathId) {
        s.pathTasks.filter { !it.archived && (linkedPathId.isBlank() || it.pathId == linkedPathId) }.take(15)
    }

    Page {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Eyebrow(notebook?.name ?: "Biblioteca de Mímir", Gold)
                Text(page.title, style = MaterialTheme.typography.headlineSmall)
                Text("Criada ${displayDate(page.created)} · Atualizada ${displayDate(page.updated)}", color = Muted, fontSize = 11.sp)
            }
            TextButton({ vm.run { vm.repo.togglePageFavorite(page.id) } }) {
                Text(if (page.favorite) "★" else "☆", color = if (page.favorite) Gold else Muted, fontSize = 22.sp)
            }
        }

        AetherisTabs(
            listOf("Visualizar", if (hasUnsavedChanges) "Editar *" else "Editar"),
            modeTab,
            { nextTab ->
                if (modeTab == 1 && nextTab == 0 && hasUnsavedChanges) {
                    confirmDiscardEdit = true
                } else {
                    modeTab = nextTab
                }
            }
        )

        if (modeTab == 0) {
            val tagList = remember(page.tags) { parseTags(page.tags) }
            if (tagList.isNotEmpty() || page.pathId.isNotBlank() || page.taskId.isNotBlank()) {
                FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    tagList.forEach { t -> Text("#$t", color = Blue, fontSize = 12.sp) }
                    s.paths.find { it.id == page.pathId }?.let { lp ->
                        Text("· Caminho: ${lp.name}", color = Violet, fontSize = 12.sp)
                    }
                    s.pathTasks.find { it.id == page.taskId }?.let { lt ->
                        Text("· Tarefa: ${lt.title}", color = Gold, fontSize = 12.sp)
                    }
                }
            }
            AetherSurface(SurfaceKind.Operational) {
                MarkdownPreview(page.content)
            }
            FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                SagaButton("Criar flashcard desta página", { flashcardCreator = true }, secondary = true)
                TextButton({ vm.run { vm.repo.duplicatePage(page.id) } }) { Text("Duplicar") }
                if (s.notebooks.size > 1) {
                    TextButton({ moveDialog = true }) { Text("Mover") }
                }
                TextButton({ vm.run { vm.repo.archivePage(page.id, !page.archived) } }) {
                    Text(if (page.archived) "Reativar" else "Arquivar")
                }
                TextButton({ deleteDialog = true }) { Text("Excluir", color = Muted) }
            }
        } else {
            AetherSurface(SurfaceKind.Operational) {
                if (hasUnsavedChanges) {
                    Eyebrow("Rascunho não salvo · protegido em memória", Gold)
                }
                FormField(title, { title = it.take(200); vm.setDraft("${draftPrefix}title", title) }, "Título")
                FormField(tags, { tags = it; vm.setDraft("${draftPrefix}tags", tags) }, "Tags (ex.: estoicismo, história, resumo)")
                Text("Markdown: # título · **negrito** · *itálico* · - lista · > citação · `código` · [link](url)", color = Muted, fontSize = 11.sp)
                FormField(content, { content = it; vm.setDraft("${draftPrefix}content", content) }, "Conteúdo da página", multi = true)
                if (activePaths.isNotEmpty()) {
                    Eyebrow("Vincular a um Caminho (opcional)", Violet)
                    val pathLabels = listOf("Nenhum") + activePaths.map { it.name }
                    val selectedPathIdx = if (linkedPathId.isBlank()) 0 else (activePaths.indexOfFirst { it.id == linkedPathId } + 1).coerceAtLeast(0)
                    ChipRow(pathLabels, selectedPathIdx, { idx ->
                        linkedPathId = if (idx == 0) "" else activePaths[idx - 1].id
                        vm.setDraft("${draftPrefix}path", linkedPathId)
                        if (linkedTaskId.isNotBlank() && s.pathTasks.none { it.id == linkedTaskId && (linkedPathId.isBlank() || it.pathId == linkedPathId) }) {
                            linkedTaskId = ""
                            vm.setDraft("${draftPrefix}task", "")
                        }
                    })
                }
                if (activeTasks.isNotEmpty()) {
                    Eyebrow("Vincular a uma Tarefa (opcional)", Blue)
                    val taskLabels = listOf("Nenhuma") + activeTasks.map { it.title.take(24) }
                    val selectedTaskIdx = if (linkedTaskId.isBlank()) 0 else (activeTasks.indexOfFirst { it.id == linkedTaskId } + 1).coerceAtLeast(0)
                    ChipRow(taskLabels, selectedTaskIdx, { idx ->
                        linkedTaskId = if (idx == 0) "" else activeTasks[idx - 1].id
                        vm.setDraft("${draftPrefix}task", linkedTaskId)
                    })
                }
                Action(
                    "Salvar página",
                    {
                        vm.run {
                            vm.repo.savePage(
                                page.copy(
                                    title = title,
                                    content = content,
                                    tags = tags,
                                    pathId = linkedPathId,
                                    taskId = linkedTaskId
                                )
                            )
                            vm.clearDraftsWithPrefix(draftPrefix)
                            modeTab = 0
                        }
                    },
                    Modifier.fillMaxWidth()
                )
            }
        }

        // Links & Backlinks
        AetherSurface(SurfaceKind.Operational) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Eyebrow("Páginas relacionadas (${outgoing.size})", Blue)
                TextButton({ linkPickerOpen = true }) { Text("+ Ligar página", fontSize = 12.sp) }
            }
            if (outgoing.isEmpty()) {
                Text("Nenhuma ligação explícita ainda. Conecte ideias entre páginas da Biblioteca.", color = Muted, fontSize = 12.sp)
            } else {
                outgoing.forEach { target ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "→ ${target.title}",
                            color = Blue,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f).clickable { onOpenPage(target.id) }.padding(vertical = 4.dp)
                        )
                        TextButton({ vm.run { vm.repo.unlinkPages(page.id, target.id) } }) {
                            Text("Remover", color = Muted, fontSize = 11.sp)
                        }
                    }
                }
            }
            if (backlinks.isNotEmpty()) {
                Hairline()
                Eyebrow("Referenciada por (${backlinks.size})", Gold)
                backlinks.forEach { src ->
                    Text(
                        "← ${src.title}",
                        color = Gold,
                        fontSize = 13.sp,
                        modifier = Modifier.fillMaxWidth().clickable { onOpenPage(src.id) }.padding(vertical = 4.dp)
                    )
                }
            }
        }

        // Bibliographic References
        AetherSurface(SurfaceKind.Operational) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Eyebrow("Referências bibliográficas (${refs.size})", Gold)
                TextButton({ refEditorId = "__new__" }) { Text("+ Referência", fontSize = 12.sp) }
            }
            if (refs.isEmpty()) {
                Text("Adicione livros, artigos, fontes ou capítulos consultados nesta página.", color = Muted, fontSize = 12.sp)
            } else {
                refs.forEach { ref ->
                    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(ref.title, style = MaterialTheme.typography.titleSmall, color = Ink)
                        val meta = listOf(ref.author, ref.year, ref.source, ref.location).filter { it.isNotBlank() }.joinToString(" · ")
                        if (meta.isNotBlank()) Text(meta, color = Muted, fontSize = 12.sp)
                        if (ref.url.isNotBlank()) Text(ref.url, color = Blue, fontSize = 11.sp)
                        if (ref.notes.isNotBlank()) Text(ref.notes, color = Muted, fontSize = 12.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton({ refEditorId = ref.id }) { Text("Editar", fontSize = 11.sp) }
                            TextButton({ vm.run { vm.repo.deleteReference(ref.id) } }) { Text("Remover", color = Muted, fontSize = 11.sp) }
                        }
                    }
                    Hairline()
                }
            }
        }

        // Linked flashcards
        if (pageCards.isNotEmpty()) {
            AetherSurface(SurfaceKind.Operational) {
                Eyebrow("Flashcards desta página (${pageCards.size})", Blue)
                pageCards.forEach { c ->
                    Text("◈ ${c.question}", color = Ink, fontSize = 13.sp)
                    Text("Próxima revisão: ${displayDate(c.nextReviewDate)}", color = Muted, fontSize = 11.sp)
                }
            }
        }
    }

    if (confirmDiscardEdit) {
        DiscardEdits(
            onDiscard = {
                vm.clearDraftsWithPrefix(draftPrefix)
                title = page.title
                content = page.content
                tags = page.tags
                linkedPathId = page.pathId
                linkedTaskId = page.taskId
                confirmDiscardEdit = false
                modeTab = 0
            },
            onKeep = { confirmDiscardEdit = false }
        )
    }

    if (linkPickerOpen) {
        val candidates = s.knowledgePages.filter {
            it.id != page.id && !it.archived && it.notebookId !in archivedNbIds && outgoing.none { o -> o.id == it.id }
        }
        AetherDialog(
            onDismissRequest = { linkPickerOpen = false },
            title = "Ligar a outra página",
            confirmButton = { TextButton({ linkPickerOpen = false }) { Text("Fechar") } }
        ) {
            if (candidates.isEmpty()) {
                Text("Não há outras páginas disponíveis para ligar.", color = Muted)
            } else {
                candidates.forEach { cand ->
                    val nbName = s.notebooks.find { it.id == cand.notebookId }?.name ?: ""
                    TextButton({ vm.run { vm.repo.linkPages(page.id, cand.id); linkPickerOpen = false } }, Modifier.fillMaxWidth()) {
                        Text("${cand.title}${if (nbName.isNotBlank()) " · $nbName" else ""}")
                    }
                }
            }
        }
    }

    refEditorId?.let { id ->
        val editing = if (id == "__new__") KnowledgeReference(pageId = page.id) else s.knowledgeReferences.find { it.id == id }
        if (editing == null) {
            refEditorId = null
        } else {
            val refDraftPrefix = "ref_edit:${page.id}:$id:"
            ReferenceEditorDialog(
                initial = editing,
                vm = vm,
                draftPrefix = refDraftPrefix,
                onDismiss = {
                    vm.clearDraftsWithPrefix(refDraftPrefix)
                    refEditorId = null
                },
                onSave = { updated ->
                    vm.run {
                        vm.repo.saveReference(updated)
                        vm.clearDraftsWithPrefix(refDraftPrefix)
                        refEditorId = null
                    }
                }
            )
        }
    }

    if (flashcardCreator) {
        val fcDraftPrefix = "fc_from_page:${page.id}:"
        FlashcardEditorDialog(
            initial = Flashcard(notebookId = page.notebookId, pageId = page.id, tags = page.tags),
            vm = vm,
            draftPrefix = fcDraftPrefix,
            notebooks = s.notebooks,
            pages = s.knowledgePages.filter { it.notebookId == page.notebookId && !it.archived },
            onDismiss = {
                vm.clearDraftsWithPrefix(fcDraftPrefix)
                flashcardCreator = false
            },
            onSave = { updated ->
                vm.run {
                    vm.repo.saveFlashcard(updated)
                    vm.clearDraftsWithPrefix(fcDraftPrefix)
                    flashcardCreator = false
                }
            }
        )
    }

    if (moveDialog) {
        val others = s.notebooks.filter { it.id != page.notebookId }
        AetherDialog(
            onDismissRequest = { moveDialog = false },
            title = "Mover página",
            confirmButton = { TextButton({ moveDialog = false }) { Text("Fechar") } }
        ) {
            others.forEach { dest ->
                TextButton({ vm.run { vm.repo.movePage(page.id, dest.id); moveDialog = false } }, Modifier.fillMaxWidth()) {
                    Text(dest.name)
                }
            }
        }
    }

    if (deleteDialog) {
        AetherDialog(
            onDismissRequest = { deleteDialog = false },
            title = "Excluir página?",
            confirmButton = {
                SagaButton("Excluir", { vm.run { vm.repo.deletePage(page.id); vm.clearDraftsWithPrefix(draftPrefix); deleteDialog = false; onBack() } }, destructive = true)
            },
            dismissButton = { TextButton({ deleteDialog = false }) { Text("Cancelar") } }
        ) {
            Text("A página \"${page.title}\", suas referências e ligações serão removidas permanentemente.")
        }
    }
}

@Composable
fun FlashcardReviewScreen(
    notebookId: String,
    s: GameState,
    vm: GameViewModel,
    onDone: () -> Unit
) {
    val day = currentDay()
    val pending = remember(s.notebooks, s.flashcards, day, notebookId) { dueFlashcards(s, day, notebookId) }
    var sessionTotal by rememberSaveable(notebookId) { mutableIntStateOf(pending.size) }
    var reviewed by rememberSaveable(notebookId) { mutableIntStateOf(0) }
    var againCount by rememberSaveable(notebookId) { mutableIntStateOf(0) }
    var hardCount by rememberSaveable(notebookId) { mutableIntStateOf(0) }
    var goodCount by rememberSaveable(notebookId) { mutableIntStateOf(0) }
    var easyCount by rememberSaveable(notebookId) { mutableIntStateOf(0) }
    var revealed by rememberSaveable(notebookId) { mutableStateOf(false) }
    var repeatMissedInSession by rememberSaveable(notebookId) { mutableStateOf(true) }
    var retryQueueCsv by rememberSaveable(notebookId) { mutableStateOf("") }

    val retryIds = remember(retryQueueCsv) { retryQueueCsv.split(',').filter { it.isNotBlank() } }
    val retryCards = remember(retryIds, s.flashcards, s.notebooks) {
        val activeNbs = s.notebooks.filter { !it.archived }.mapTo(HashSet()) { it.id }
        val byId = s.flashcards.filter { !it.archived && it.notebookId in activeNbs }.associateBy { it.id }
        retryIds.mapNotNull { byId[it] }
    }
    val isRetryPhase = pending.isEmpty() && retryCards.isNotEmpty()
    val currentCard = pending.firstOrNull() ?: retryCards.firstOrNull()
    val nbTitle = if (notebookId.isBlank()) "Todos os cadernos" else s.notebooks.find { it.id == notebookId }?.name ?: "Caderno"

    Page {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MimirCodexArt(3, Modifier.size(48.dp), accent = Blue)
            Column(Modifier.weight(1f)) {
                Eyebrow("Runas da Memória", Blue)
                Text(nbTitle, style = MaterialTheme.typography.titleLarge)
            }
        }

        if (currentCard == null) {
            AetherSurface(SurfaceKind.Ceremonial) {
                if (reviewed > 0) {
                    Eyebrow("Sessão concluída", Green)
                    Text("$reviewed ${if (reviewed == 1) "runa revisada" else "runas revisadas"}", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        "Errei: $againCount · Difícil: $hardCount · Bom: $goodCount · Fácil: $easyCount",
                        color = Muted,
                        fontSize = 13.sp
                    )
                    Text("O agendamento SM-2 foi salvo automaticamente para cada flashcard.", color = Muted, fontSize = 12.sp)
                } else {
                    Eyebrow("Memória em dia", Green)
                    Text("Nenhuma revisão pendente hoje", style = MaterialTheme.typography.titleLarge)
                    val nextDate = nextFlashcardReviewDate(s, notebookId)
                    Text(
                        if (nextDate != null) "Próxima revisão agendada para ${displayDate(nextDate)}."
                        else "Crie flashcards em seus cadernos da Biblioteca de Mímir para começar.",
                        color = Muted,
                        fontSize = 13.sp
                    )
                }
                Action("Voltar à Biblioteca", onDone, Modifier.fillMaxWidth())
            }
            return@Page
        }

        val totalForBar = maxOf(sessionTotal, reviewed + pending.size).coerceAtLeast(1)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    if (isRetryPhase) "Reforço da sessão" else "Progresso da sessão",
                    color = Muted,
                    fontSize = 12.sp
                )
                Text(
                    if (isRetryPhase) "${retryCards.size} para fixar" else "$reviewed de $totalForBar · ${pending.size} restantes",
                    color = if (isRetryPhase) Gold else Blue,
                    fontSize = 12.sp
                )
            }
            Meter(reviewed.toFloat() / totalForBar, if (isRetryPhase) Gold else Blue, slim = true)
        }

        val cardNotebook = s.notebooks.find { it.id == currentCard.notebookId }
        val cardPage = s.knowledgePages.find { it.id == currentCard.pageId }
        AetherSurface(SurfaceKind.Operational, Modifier.testTag("flashcard_stage")) {
            Eyebrow(
                if (isRetryPhase) "Reforço da sessão · não duplica histórico"
                else listOfNotNull(cardNotebook?.name, cardPage?.title).joinToString(" · ").ifBlank { "Runa da Memória" },
                if (isRetryPhase) Blue else Gold
            )
            Text(currentCard.question, style = MaterialTheme.typography.headlineSmall)
            if (currentCard.tags.isNotBlank()) {
                Text(currentCard.tags, color = Muted, fontSize = 11.sp)
            }
            Hairline()
            if (!revealed) {
                Text("Recupere a resposta em memória antes de revelar.", color = Muted, fontSize = 12.sp)
                Action("Revelar resposta", { revealed = true }, Modifier.fillMaxWidth().testTag("reveal_answer"))
            } else {
                Eyebrow("Resposta", Green)
                Text(currentCard.answer, color = Ink, fontSize = 15.sp, lineHeight = 23.sp)
                Hairline()
                if (isRetryPhase) {
                    Text(
                        "Prática imediata sem alterar o agendamento já salvo para amanhã:",
                        color = Muted,
                        fontSize = 12.sp
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SagaButton(
                            "Rever novamente",
                            {
                                val rest = retryIds.drop(1) + currentCard.id
                                retryQueueCsv = rest.joinToString(",")
                                revealed = false
                            },
                            Modifier.weight(1f),
                            secondary = true
                        )
                        Action(
                            "Lembrei agora",
                            {
                                retryQueueCsv = retryIds.drop(1).joinToString(",")
                                revealed = false
                            },
                            Modifier.weight(1f)
                        )
                    }
                    TextButton({ retryQueueCsv = ""; revealed = false }, Modifier.align(Alignment.End)) {
                        Text("Encerrar reforço", color = Muted, fontSize = 12.sp)
                    }
                } else {
                    Text("Avalie sua lembrança:", color = Muted, fontSize = 12.sp)
                    val outcomes = remember(currentCard.id, day) {
                        (0..3).map { r -> computeNextReview(currentCard, r, day) }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        RatingButton("Errei", "+${outcomes[0].intervalDays}d", MaterialTheme.colorScheme.error, Modifier.weight(1f)) {
                            val id = currentCard.id
                            vm.run {
                                vm.repo.reviewFlashcard(id, 0, day)
                                reviewed++; againCount++; revealed = false
                                if (repeatMissedInSession && id !in retryIds) {
                                    retryQueueCsv = (retryIds + id).joinToString(",")
                                }
                            }
                        }
                        RatingButton("Difícil", "+${outcomes[1].intervalDays}d", Gold, Modifier.weight(1f)) {
                            val id = currentCard.id
                            vm.run {
                                vm.repo.reviewFlashcard(id, 1, day)
                                reviewed++; hardCount++; revealed = false
                            }
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        RatingButton("Bom", "+${outcomes[2].intervalDays}d", Blue, Modifier.weight(1f)) {
                            val id = currentCard.id
                            vm.run {
                                vm.repo.reviewFlashcard(id, 2, day)
                                reviewed++; goodCount++; revealed = false
                            }
                        }
                        RatingButton("Fácil", "+${outcomes[3].intervalDays}d", Green, Modifier.weight(1f)) {
                            val id = currentCard.id
                            vm.run {
                                vm.repo.reviewFlashcard(id, 3, day)
                                reviewed++; easyCount++; revealed = false
                            }
                        }
                    }
                }
            }
        }
        if (!isRetryPhase) {
            Toggle(
                "Repetir cartões avaliados como \"Errei\" ao fim da sessão (sem duplicar histórico)",
                repeatMissedInSession,
                { repeatMissedInSession = it }
            )
        }
        Text(
            "Você pode interromper a qualquer momento: cada card avaliado já está salvo.",
            color = Muted,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun RatingButton(
    label: String,
    intervalHint: String,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = CutCornerShape(4.dp)
    Column(
        modifier
            .heightIn(min = 52.dp)
            .clip(shape)
            .background(accent.copy(alpha = 0.14f))
            .border(1.dp, accent.copy(alpha = 0.62f), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(label, color = accent, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Text(intervalHint, color = Muted, fontSize = 11.sp)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NotebookEditorDialog(
    initial: KnowledgeNotebook,
    vm: GameViewModel,
    draftPrefix: String,
    paths: List<PersonalPath>,
    onDismiss: () -> Unit,
    onSave: (KnowledgeNotebook) -> Unit
) {
    var name by remember(draftPrefix) { mutableStateOf(vm.getDraft("${draftPrefix}name", initial.name)) }
    var description by remember(draftPrefix) { mutableStateOf(vm.getDraft("${draftPrefix}desc", initial.description)) }
    var category by remember(draftPrefix) { mutableStateOf(vm.getDraft("${draftPrefix}cat", initial.category.ifBlank { notebookCategories.first() })) }
    var cover by remember(draftPrefix) {
        mutableIntStateOf(vm.getDraft("${draftPrefix}cover", initial.cover.toString()).toIntOrNull()?.coerceIn(mimirCoverNames.indices) ?: initial.cover.coerceIn(mimirCoverNames.indices))
    }
    var pathId by remember(draftPrefix) { mutableStateOf(vm.getDraft("${draftPrefix}path", initial.pathId)) }
    var discardConfirm by remember { mutableStateOf(false) }

    val dirty = name != initial.name || description != initial.description ||
        category != initial.category.ifBlank { notebookCategories.first() } ||
        cover != initial.cover.coerceIn(mimirCoverNames.indices) || pathId != initial.pathId

    FullForm(
        if (initial.name.isBlank()) "Novo caderno" else "Editar caderno",
        onDismiss = { if (dirty) discardConfirm = true else onDismiss() },
        onSave = {
            onSave(initial.copy(name = name, description = description, category = category, cover = cover, pathId = pathId))
        }
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            MimirCodexArt(cover, Modifier.size(96.dp))
        }
        FormField(name, { name = it.take(120); vm.setDraft("${draftPrefix}name", name) }, "Nome do caderno")
        FormField(description, { description = it.take(2000); vm.setDraft("${draftPrefix}desc", description) }, "Descrição (opcional)", multi = true)
        Eyebrow("Categoria", Gold)
        ChipRow(notebookCategories, notebookCategories.indexOf(category).coerceAtLeast(0), { category = notebookCategories[it]; vm.setDraft("${draftPrefix}cat", category) })
        Eyebrow("Capa pictórica", Gold)
        ChipRow(mimirCoverNames, cover, { cover = it; vm.setDraft("${draftPrefix}cover", cover.toString()) })
        if (paths.isNotEmpty()) {
            Eyebrow("Vincular a um Caminho (opcional)", Violet)
            val labels = listOf("Nenhum") + paths.map { it.name }
            val idx = if (pathId.isBlank()) 0 else (paths.indexOfFirst { it.id == pathId } + 1).coerceAtLeast(0)
            ChipRow(labels, idx, { pathId = if (it == 0) "" else paths[it - 1].id; vm.setDraft("${draftPrefix}path", pathId) })
        }
    }
    if (discardConfirm) {
        DiscardEdits(onDiscard = { discardConfirm = false; onDismiss() }, onKeep = { discardConfirm = false })
    }
}

@Composable
private fun ReferenceEditorDialog(
    initial: KnowledgeReference,
    vm: GameViewModel,
    draftPrefix: String,
    onDismiss: () -> Unit,
    onSave: (KnowledgeReference) -> Unit
) {
    var title by remember(draftPrefix) { mutableStateOf(vm.getDraft("${draftPrefix}title", initial.title)) }
    var author by remember(draftPrefix) { mutableStateOf(vm.getDraft("${draftPrefix}author", initial.author)) }
    var year by remember(draftPrefix) { mutableStateOf(vm.getDraft("${draftPrefix}year", initial.year)) }
    var source by remember(draftPrefix) { mutableStateOf(vm.getDraft("${draftPrefix}source", initial.source)) }
    var location by remember(draftPrefix) { mutableStateOf(vm.getDraft("${draftPrefix}location", initial.location)) }
    var url by remember(draftPrefix) { mutableStateOf(vm.getDraft("${draftPrefix}url", initial.url)) }
    var notes by remember(draftPrefix) { mutableStateOf(vm.getDraft("${draftPrefix}notes", initial.notes)) }
    var discardConfirm by remember { mutableStateOf(false) }

    val dirty = title != initial.title || author != initial.author || year != initial.year ||
        source != initial.source || location != initial.location || url != initial.url || notes != initial.notes

    FullForm(
        if (initial.title.isBlank()) "Nova referência" else "Editar referência",
        onDismiss = { if (dirty) discardConfirm = true else onDismiss() },
        onSave = {
            onSave(
                initial.copy(
                    title = title,
                    author = author,
                    year = year,
                    source = source,
                    location = location,
                    url = url,
                    notes = notes
                )
            )
        }
    ) {
        FormField(title, { title = it.take(300); vm.setDraft("${draftPrefix}title", title) }, "Título da obra ou artigo")
        FormField(author, { author = it.take(200); vm.setDraft("${draftPrefix}author", author) }, "Autor(a)")
        FormField(year, { year = it.take(20); vm.setDraft("${draftPrefix}year", year) }, "Ano")
        FormField(source, { source = it.take(300); vm.setDraft("${draftPrefix}source", source) }, "Fonte, livro ou revista")
        FormField(location, { location = it.take(120); vm.setDraft("${draftPrefix}location", location) }, "Página ou capítulo (opcional)")
        FormField(url, { url = it.take(1000); vm.setDraft("${draftPrefix}url", url) }, "URL (opcional)")
        FormField(notes, { notes = it.take(4000); vm.setDraft("${draftPrefix}notes", notes) }, "Observações", multi = true)
    }
    if (discardConfirm) {
        DiscardEdits(onDiscard = { discardConfirm = false; onDismiss() }, onKeep = { discardConfirm = false })
    }
}

@Composable
private fun FlashcardEditorDialog(
    initial: Flashcard,
    vm: GameViewModel,
    draftPrefix: String,
    notebooks: List<KnowledgeNotebook>,
    pages: List<KnowledgePage>,
    onDismiss: () -> Unit,
    onSave: (Flashcard) -> Unit
) {
    val defaultNb = initial.notebookId.ifBlank { notebooks.firstOrNull()?.id.orEmpty() }
    var notebookId by remember(draftPrefix) { mutableStateOf(vm.getDraft("${draftPrefix}nb", defaultNb)) }
    var pageId by remember(draftPrefix) { mutableStateOf(vm.getDraft("${draftPrefix}page", initial.pageId)) }
    var question by remember(draftPrefix) { mutableStateOf(vm.getDraft("${draftPrefix}q", initial.question)) }
    var answer by remember(draftPrefix) { mutableStateOf(vm.getDraft("${draftPrefix}a", initial.answer)) }
    var tags by remember(draftPrefix) { mutableStateOf(vm.getDraft("${draftPrefix}tags", initial.tags)) }
    var discardConfirm by remember { mutableStateOf(false) }

    val matchingPages = remember(pages, notebookId) { pages.filter { it.notebookId == notebookId } }
    val dirty = notebookId != defaultNb || pageId != initial.pageId || question != initial.question ||
        answer != initial.answer || tags != initial.tags

    FullForm(
        if (initial.question.isBlank()) "Novo flashcard" else "Editar flashcard",
        onDismiss = { if (dirty) discardConfirm = true else onDismiss() },
        onSave = {
            onSave(initial.copy(notebookId = notebookId, pageId = pageId, question = question, answer = answer, tags = tags))
        }
    ) {
        if (notebooks.size > 1) {
            Eyebrow("Caderno", Gold)
            ChipRow(
                notebooks.map { it.name },
                notebooks.indexOfFirst { it.id == notebookId }.coerceAtLeast(0),
                { idx ->
                    notebookId = notebooks[idx].id
                    vm.setDraft("${draftPrefix}nb", notebookId)
                    if (pages.none { it.id == pageId && it.notebookId == notebookId }) {
                        pageId = ""
                        vm.setDraft("${draftPrefix}page", "")
                    }
                }
            )
        }
        if (matchingPages.isNotEmpty()) {
            Eyebrow("Página relacionada (opcional)", Blue)
            val labels = listOf("Nenhuma") + matchingPages.take(12).map { it.title.take(24) }
            val idx = if (pageId.isBlank()) 0 else (matchingPages.take(12).indexOfFirst { it.id == pageId } + 1).coerceAtLeast(0)
            ChipRow(labels, idx, { pageId = if (it == 0) "" else matchingPages[it - 1].id; vm.setDraft("${draftPrefix}page", pageId) })
        }
        FormField(question, { question = it; vm.setDraft("${draftPrefix}q", question) }, "Pergunta (frente)", multi = true)
        FormField(answer, { answer = it; vm.setDraft("${draftPrefix}a", answer) }, "Resposta (verso)", multi = true)
        FormField(tags, { tags = it; vm.setDraft("${draftPrefix}tags", tags) }, "Tags (opcional)")
    }
    if (discardConfirm) {
        DiscardEdits(onDiscard = { discardConfirm = false; onDismiss() }, onKeep = { discardConfirm = false })
    }
}
