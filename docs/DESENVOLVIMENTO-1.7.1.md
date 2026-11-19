# ASCEND 1.7.1 — A Sabedoria dos Nove Reinos (Diário de Desenvolvimento)

- **Branch base:** `feat/ascend-1.7.0.1` (`230cc15`)
- **Branch de trabalho:** `feat/ascend-1.7.1`
- **Versão entregue:** `versionName = "1.7.1"`, `versionCode = 22`, `applicationId = "br.com.ascend.aetheris"`
- **Direção:** Knowledge & Character Ascension Update — offline, Kotlin/Compose/Room/DataStore, preservando integralmente os 155 testes da `1.7.0.1`, a Biblioteca da Forja (`1.6.9.19`), as correções de foco e a integridade econômica.

---

## Checkpoints concluídos

- [x] **Auditoria inicial direcionada e criação da branch**
  - Criada branch `feat/ascend-1.7.1` a partir de `origin/feat/ascend-1.7.0.1` (`230cc15`), sem alterar `feat/ascend-1.7.0.1` nem `main`.
- [x] **Acervo de Mímir (Geração pictórica — orçamento estrito de 3 imagens via `generate_image`)**
  - `mimir_library.webp` (`1280 × 704`, `116,684` bytes), `archetype_seals_atlas.webp` (`1280 × 512` RGBA `5 × 2`, `198,934` bytes), `mimir_codex_atlas.webp` (`960 × 640` RGB `3 × 2`, `43,884` bytes) — total `351,1 KB`.
- [x] **Etapa 1 — Base, Room v8 (`MIGRATION_7_8`), Backup v10 e Biblioteca de Mímir**
  - Entidades `KnowledgeNotebook`, `KnowledgePage`, `KnowledgePageLink`, `KnowledgeReference`, `Flashcard`, `FlashcardReview`, esquema `8.json`, leitura compatível de backups `v1`–`v10`, cadernos, páginas em Markdown (editar/visualizar), tags, favoritos, busca, ordenação, duplicar, mover, ligações explícitas, backlinks, referências bibliográficas e vínculo opcional com Caminhos/Tarefas.
- [x] **Etapa 2 — Runas da Memória (Flashcards & SM-2)**
  - Algoritmo determinístico `computeNextReview` (`Errei`/`Difícil`/`Bom`/`Fácil`), sanitização de `easeFactor`, sessão interrompível com persistência imediata por cartão, bloqueio de revisão repetida no mesmo dia, `0 XP` / `0 moedas` extras e atalho discreto em `Rumos do Dia`.
- [x] **Etapa 3 — Ascensão dos Arquétipos**
  - 10 especializações (`Player.specialization`, 2 por classe, nível 3), troca/remoção segura preservando habilidades, 10 novas habilidades de ascensão integradas a `skillPoints(s)` + preservação das 6 universais originais e 5 Marcos de Sabedoria idempotentes (`coins = 0`).
- [x] **Etapa 4 — Integração visual do Acervo de Mímir, inspeção real e refinamento**
  - 7 capturas reais em Robolectric inspecionadas (`docs/screenshots/v1.7.1/`), incluindo `320 dp` / fonte `130%` / tema claro / minimalista. Refinamento aplicado após inspeção visual (troca de `SurfaceKind.Artifact` por `SurfaceKind.Operational` em cadernos, flashcards e painel de arquétipos).
- [x] **Etapa 5 — Testes, Lint, Compilação e Documentação**
  - `testDebugUnitTest`: **167 testes aprovados** (`155` preservados + `12` novos em `MimirAndArchetypesTest` e `MimirAndArchetypesUiTest`), 0 falhas/erros/ignorados.
  - `lintDebug`: **0 erros** (`23` avisos preexistentes).
  - `assembleDebug`: `versionName = "1.7.1"`, `versionCode = 22`, `br.com.ascend.aetheris`, sem permissão `INTERNET`.
  - Documentação publicada em `docs/BIBLIOTECA-MIMIR-1.7.1.md`, `docs/ARQUETIPOS-1.7.1.md`, `docs/VISUAL-1.7.1.md`, `docs/art/README.md`, `README.md`, `CHANGELOG.md`, `PROGRESSO.md`, `VALIDACAO.md`, `INSTALACAO.md` e `ASSINATURA.md`.
