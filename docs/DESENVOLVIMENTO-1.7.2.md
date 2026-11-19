# ASCEND 1.7.2 — Registro de Desenvolvimento

- **Branch base:** `feat/ascend-1.7.1` (preservada intacta).
- **Branch de entrega:** `feat/ascend-1.7.2`.
- **Versão:** `versionName = "1.7.2"`, `versionCode = 23`, `applicationId = "br.com.ascend.aetheris"`, offline (zero permissão `INTERNET`), Room v8 e backup v10 preservados sem migração destrutiva.

## Etapas Incrementais Executadas

1. **Etapa 0 — Estabilização prioritária da 1.7.1 (`f463689`):** reforço de preservação de rascunhos/retry na Biblioteca de Mímir e verificação de regressão (`MimirAndArchetypesTest` 13/13).
2. **Etapa 1 — Expansão da Biblioteca da Forja: Renascimento Rúnico (`1b5fdb9`):** geração de 10 recursos pictóricos originais (`1` prancha conceitual fora do APK + `9` atlas WebP de produção somando `845.042 bytes` / `0,81 MB`, dentro do orçamento `<= 3 MB`).
3. **Etapa 2 — Design System 3.0 (`693a378`):** criação de `VikingDesignSystem.kt` com `drawAtlasCellNineSlice` (9-slice por célula), 6 famílias de botões viking, separação estrita entre 12 runas históricas Elder Futhark vetoriais e 12 selos fictícios de Aetheris, 16 ícones funcionais, divisores Urnes/Borre, amuleto de navegação e controles forjados.
4. **Etapas 3 a 5 — Motor Regional, Atlas 2.0, Novas Regiões e Relíquias (`ccdcdce`):** generalização do motor em `Exploration.kt`, Atlas de Aetheris 2.0 (pinça/pan com limites, controles acessíveis, lista de regiões e 6 estados explícitos), Biblioteca das Eras (nível 5, Eydis, 3 expedições/8 fases), Montanhas da Disciplina (nível 8, Haldor, 3 expedições/8 fases), 4 relíquias exclusivas da exploração, 4 conquistas regionais, 2 títulos e novas métricas (`4 = Foco`, `5 = Dias de revisão em Mímir`, `6 = Tarefas de Caminhos`) com fallback explícito para hábitos.
5. **Etapa 6 — Testes, Inspeção Visual Real, Refinamento e Entrega:** criação de `WorldExpansionTest` (4 testes de domínio/repositório/retrocompatibilidade) e `WorldExpansionUiTest` (6 testes Robolectric Compose gerando 14 capturas reais), auditoria visual de todas as 14 capturas e aplicação de 5 correções cirúrgicas antes da publicação:
   - Recorte quadrado centralizado em `VikingCheckSeal` (eliminando achatamento horizontal da célula `2 × 4`).
   - Scrim escuro e texto em marfim no `ChipRow` selecionado em modo escuro para contraste claro sobre a filigrana.
   - Remoção de emenda vertical sutil no `Meter` não-slim.
   - Migração dos controles de zoom do Atlas 2.0 para `FlowRow` impedindo quebra de `"Zoom 100%"` em `320 dp` com fonte `130%`.
   - Limpeza de capturas (`Surface(Night, Ink)` na prancha de componentes, descarte de snackbar transitório em `shot()`, espera de seed completo em Mímir e `firmament-compass` equipado na captura de relíquias).
