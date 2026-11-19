# ASCEND 1.7.1 — A Biblioteca de Mímir & Runas da Memória

## 1. Visão geral

A **Biblioteca de Mímir** integra o gerenciamento pessoal de conhecimento (PKM) e a revisão espaçada (**Runas da Memória**) diretamente ao ASCEND, de forma **100% local e offline**, sem criar uma sexta aba na barra inferior e sem alterar a economia de XP ou moedas.

- **Entrada principal:** `Mundo → Crônicas → Abrir Biblioteca de Mímir` (com rotas `mimir`, `mimir_notebook/{id}`, `mimir_page/{id}`, `mimir_review` e `mimir_review/{notebookId}` ancoradas à aba `Mundo`).
- **Acesso discreto na Jornada:** quando há flashcards vencidos no dia atual (`dueFlashcards(s, today()).isNotEmpty()`), `Rumos do Dia` exibe o atalho `◈ Runas da Memória · N revisões pendentes`.
- **Integração opcional com Caminhos e Tarefas:** cadernos podem ser associados a um `PersonalPath`, e páginas a um `PersonalPath` e/ou `PathTask`. Em `PathDetailScreen`, os cadernos e páginas vinculados aparecem com navegação direta. Excluir um Caminho ou Tarefa limpa apenas as referências `pathId`/`taskId`, sem apagar cadernos nem páginas.

## 2. Persistência (Room v8 e Backup v10)

A migração aditiva `MIGRATION_7_8` (`Database.kt`, esquema `app/schemas/br.com.ascend.aetheris.GameDatabase/8.json`) cria 6 novas tabelas com chaves estrangeiras `RESTRICT` e índices explícitos, sem fallback destrutivo e sem alterar dados históricos:

| Tabela | Entidade | Papel |
|---|---|---|
| `knowledge_notebooks` | `KnowledgeNotebook` | Cadernos temáticos com categoria, capa pictórica (`0..5`), vínculo opcional a `pathId` e arquivamento. |
| `knowledge_pages` | `KnowledgePage` | Páginas em Markdown com tags normalizadas, favorito, arquivamento e vínculo opcional a `pathId`/`taskId`. |
| `knowledge_page_links` | `KnowledgePageLink` | Ligações direcionadas explícitas entre páginas (`sourcePageId -> targetPageId`), habilitando links de saída e backlinks. |
| `knowledge_references` | `KnowledgeReference` | Referências bibliográficas estruturadas (`author`, `title`, `year`, `source`, `url`, `location`, `notes`) associadas a uma página. |
| `flashcards` | `Flashcard` | Cartões de revisão espaçada vinculados a um caderno (e opcionalmente a uma página) com estado SM-2 (`repetitions`, `intervalDays`, `easeFactor`, `nextReviewDate`, `lastReviewDate`). |
| `flashcard_reviews` | `FlashcardReview` | Histórico imutável de revisões realizadas (`reviewDate`, `rating`, `intervalBefore`, `intervalAfter`, `easeAfter`, `reviewedAt`). |

O **Backup v10** (`BackupEnvelope(version = 10)`) inclui todas as 6 tabelas com integridade SHA-256, validação referencial completa (`validateState`) e rollback atômico em `importState`. Backups antigos (`v1` a `v9`) continuam sendo importados sem criar cadernos, páginas ou revisões fictícias.

## 3. Funcionalidades implementadas (`MimirAndArchetypes.kt`, `MimirScreens.kt`, `Repository.kt`)

- **Cadernos:** criar, editar, arquivar/reativar e excluir com confirmação (exclusão em cascata transacional remove `flashcard_reviews → flashcards → knowledge_references → knowledge_links → knowledge_pages → notebook`, sem registros órfãos).
- **Páginas em Markdown:** alternância `Visualizar` / `Editar`, renderizador local (`MarkdownPreview`) para títulos (`#`, `##`, `###`), **negrito**, *itálico*, `código inline`, blocos cercados `` ``` ``, listas (`-`, `*`, numeradas), citações (`>`) e links `[rótulo](url)`.
- **Organização:** normalização de tags (`parseTags` / `normalizeTags`), favoritos, busca global e por caderno (título, conteúdo e tags), ordenação (`Atualização`, `Criação`, `Nome`), duplicação (copiando referências bibliográficas), movimentação entre cadernos, arquivamento e exclusão segura (limpa `pageId` nos flashcards sem apagá-los).
- **Ligações e Backlinks:** seleção explícita de páginas relacionadas, rejeição de auto-referência (`sourcePageId != targetPageId`), preservação automática ao renomear títulos e seção `Referenciada por` (`backlinkPages`).
- **Referências bibliográficas:** cadastro, edição e remoção por página, removidas automaticamente quando a página é excluída.

## 4. Runas da Memória — Algoritmo SM-2 determinístico

`computeNextReview(card, rating, reviewDate)` implementa SM-2 determinístico com 4 avaliações (`0 = Errei`, `1 = Difícil`, `2 = Bom`, `3 = Fácil`), sanitização de `easeFactor` em `[1.3, 3.5]` (protegendo contra `NaN`/infinito/extremos) e teto de intervalo em `[1, 3650]` dias:

- **Errei (`0`):** reinicia `repetitions = 0`, `intervalDays = 1`, reduz `easeFactor` em `0.20` (mínimo `1.3`).
- **Difícil (`1`):** `1d → 3d → max(prev + 1, round(prev * 1.2))` com ajuste moderado de facilidade.
- **Bom (`2`):** `1d → 6d → max(prev + 1, round(prev * easeFactor))`.
- **Fácil (`3`):** `4d → 10d → max(prev + 2, round(prev * easeFactor * 1.3))`.
- **Regras de integridade:** revisão repetida no mesmo dia é bloqueada (`card.lastReviewDate != reviewDate && card.nextReviewDate <= reviewDate`); flashcards ou cadernos arquivados são excluídos de `dueFlashcards`; cada card avaliado persiste imediatamente (sessão interrompível sem perda); **nenhuma revisão concede XP ou moedas extras**.

## 5. Limites explícitos da 1.7.1

Não fazem parte desta entrega: visualização em grafo, importação/sincronização Obsidian ou Anki, OCR/leitura de PDF, formatação ABNT completa, geração por IA ou sincronização em nuvem.
