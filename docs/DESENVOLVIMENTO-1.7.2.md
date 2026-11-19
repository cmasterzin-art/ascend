# ASCEND 1.7.2 — ALÉM DAS FRONTEIRAS

**Subtítulo técnico:** World Expansion & Nordic Viking Renaissance Update  
**Branch de origem:** `feat/ascend-1.7.1` (`88565d6`)  
**Branch de desenvolvimento:** `feat/ascend-1.7.2`  
**Alvo de versão:** `versionName = "1.7.2"`, `versionCode = 23`  
**Baseline preservada:** 167 testes (0 falhas, 0 erros, 0 ignorados), Room v8, Backup v10, offline-only (`br.com.ascend.aetheris`).

---

## Plano de Execução por Etapas (Checkpoints Recuperáveis)

### Etapa 0 — Correções Prioritárias da 1.7.1 (Estabilidade & Integridade)
- [ ] **6.1 Integridade de cadernos, páginas e flashcards:** corrigir `movePage()`, `deleteNotebook()` e `deletePage()` para evitar flashcards órfãos apontando para páginas de outro caderno excluído, preservando histórico e validação de backup (`validateState`).
- [ ] **6.2 Arquivamento consistente na Biblioteca de Mímir:** cadernos arquivados não vazam em recentes, buscas, favoritos, tags ou revisões ativas; visualização explícita de arquivados preservada sem perda de dados.
- [ ] **6.3 Proteção de rascunhos em formulários/editores:** preservar texto não salvo em recriação de Activity sem exceder limites do `Bundle` (cache em `GameViewModel` + `rememberSaveable` para campos curtos); fechar diálogos somente após persistência bem-sucedida.
- [ ] **6.4 Revisões de flashcards:** auditar agendamento/persistência e permitir reexibição opcional na mesma sessão para cartões avaliados como `Errei` sem duplicar `FlashcardReview` diário nem recompensas.
- [ ] Testes automatizados cobrindo todos os cenários da Etapa 0 + commit checkpoint.

### Etapa 1 — Expansão da Biblioteca da Forja (Renascimento Rúnico — até 10 gerações `generate_image`)
- [ ] Prancha conceitual `docs/art/concepts/nordic-viking-ui-172.webp` (fora do APK) gerada e inspecionada antes do DS 3.0.
- [ ] 9 atlases pictóricos de produção em `app/src/main/res/drawable-nodpi/` (<= 3 MB totais adicionados ao APK):
  1. `realm_library_atlas.webp` (3×3)
  2. `realm_mountains_atlas.webp` (3×3)
  3. `realm_relics_atlas.webp` (2×2)
  4. `realm_ornaments_atlas.webp` (3×2)
  5. `viking_button_atlas.webp` (6 células retangulares)
  6. `aetheris_runes_atlas.webp` (6×4 = 24 selos)
  7. `viking_functional_icons_atlas.webp` (4×4 = 16 símbolos)
  8. `viking_knotwork_atlas.webp` (8 células de entrelaçamento/molduras/divisores)
  9. `viking_controls_atlas.webp` (8 células de controles nórdicos)
- [ ] Inspeção visual individual de cada geração, recorte/transparência limpa, otimização WebP e registro de orçamento/dimensões + commit checkpoint.

### Etapa 2 — Design System 3.0 (Nordic Viking Interface)
- [ ] `drawAtlasCellNineSlice` (extração explícita de célula antes de nine-slice; nunca nine-slice no atlas inteiro).
- [ ] 6 famílias de botões em `SagaButton` / `Action` (Ferro Rúnico, Bronze Forjado, Cerimonial Viking, Confirmação, Destrutivo, Compacto Rúnico) com 6 estados interativos, feedback sutil respeitando `reducedMotion` e toque >= 48 dp.
- [ ] Sistema Rúnico de Aetheris (runas vetoriais históricas verificadas vs. selos fictícios de Aetheris claramente documentados), divisores nórdicos, cantoneiras metálicas, barras de progresso entalhadas, ícones funcionais com fallback `CarvedGlyph` em escala reduzida.
- [ ] Refinamento nórdico-viking transversal (Jornada, Herói, Mundo, Hábitos/Agenda, Mímir, Foco preservando altura compacta, Crônicas/Missões e barra inferior de 5 itens com amuleto rúnico) + commit checkpoint.

### Etapa 3 — Motor Regional Extensível & Atlas de Aetheris 2.0
- [ ] Generalizar `Exploration.kt` preservando 100% dos IDs históricos (`exp:roots:0`, `exp:sanctuary:0`, `exp:vigil:0`) e requisitos/recompensas da Floresta.
- [ ] Métricas expandidas de expedição: hábitos, sessões de foco registradas, revisões de flashcards em dias distintos e tarefas de Caminhos (com baseline, não-reaproveitamento entre fases consecutivas, anti-duplicação, alternativa por hábitos e regras de reversão seguras).
- [ ] Atlas de Aetheris 2.0 interativo: pinch-to-zoom, pan/drag, controles acessíveis (`+`/`-`/centralizar), limites na resolução da pintura, marcadores rúnicos sincronizados, 6 estados visuais de região, legenda, lista acessível e Diário de Exploração compacto + commit checkpoint.

### Etapa 4 — Região 3: Biblioteca das Eras (Nível 5)
- [ ] 3 estados pictóricos reais (Abandonada, Parcialmente restaurada, Restaurada).
- [ ] NPC **Eydis, Arquivista das Eras** + guardião narrativo **Vigia das Eras** (sem combate) + variações sutis por especialização do herói.
- [ ] 3 localidades e 3 expedições (8 fases): *O Pórtico dos Escribas* (`O Primeiro Manuscrito`, 2 fases), *O Acervo Submerso* (`As Páginas do Silêncio`, 3 fases, Mímir/flashcards + alternativa por hábitos), *A Câmara de Mímir* (`O Juramento da Sabedoria`, 3 fases).
- [ ] Relíquias (*Medalhão da Memória Eterna*, *Anel dos Escribas*), título *Guardião das Eras*, conquistas regionais, memórias e Crônica Semanal + commit checkpoint.

### Etapa 5 — Região 4: Montanhas da Disciplina (Nível 8)
- [ ] 3 estados pictóricos reais (Abandonada, Parcialmente restaurada, Restaurada).
- [ ] NPC **Haldor, Vigia das Montanhas** + presença narrativa **Guardião de Granito** (sem combate) + variações sutis por especialização do herói.
- [ ] 3 localidades e 3 expedições (8 fases): *A Trilha de Ferro* (`O Caminho dos Antigos`, 2 fases), *A Torre da Vigília* (`A Chama que Não Cessa`, 3 fases, Foco + alternativa por hábitos), *O Observatório do Norte* (`O Silêncio dos Cumes`, 3 fases).
- [ ] Relíquias (*Bússola do Firmamento*, *Broche da Vigília Boreal*), título *Testemunha dos Cumes*, conquistas regionais, memórias e Crônica Semanal + commit checkpoint.

### Etapa 6 — Testes, Validação Visual Comparativa, Documentação e Entrega Assinada
- [ ] Testes completos de unidade, Room, backup/restauração, exploração, métricas, Atlas 2.0 e UI (incluindo estados dos botões DS 3.0, 320 dp + fonte 130%).
- [ ] Prancha real renderizada de componentes + capturas reais comparativas 1.7.1 vs. 1.7.2 + inspeção visual completa.
- [ ] `versionCode = 23`, `versionName = "1.7.2"`, workflow GitHub Actions atualizado, `testDebugUnitTest`, `lintDebug`, `assembleDebug`.
- [ ] Documentação completa (`EXPLORACAO-1.7.2.md`, `ATLAS-2.0.md`, `DESIGN-SYSTEM-3.0.md`, `RENASCIMENTO-RUNICO.md`, `VISUAL-1.7.2.md`, atualização dos documentos principais).
- [ ] Push, verificação do CI com assinatura oficial (`e1f003efe40d53754db66325c40177178aaa81d4ad02ab910c85642c681caae4`) e registro final em `VALIDACAO.md`.
