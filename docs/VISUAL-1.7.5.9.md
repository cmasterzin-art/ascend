# Relatório Visual — ASCEND 1.7.5.9 (*A Reforma de Aetheris* · NORTHBORN 4.0)

> Comparativo real entre a versão `1.7.5.1` ([`docs/screenshots/1.7.5.1/`](screenshots/1.7.5.1/)) e as **30 capturas reais** da versão `1.7.5.9` ([`docs/screenshots/1.7.5.9/`](screenshots/1.7.5.9/)), renderizadas em Jetpack Compose + Robolectric (`NorthbornUiReforgeTest.kt`) e inspecionadas visualmente antes da publicação.

---

## 1. Pranchas Conceituais NORTHBORN 4.0 (Referência fora do APK)

| Prancha Mestre NORTHBORN | Direção Estrutural por Módulo |
|---|---|
| <img src="art/concepts/northborn-master-board-1759.webp" width="380" alt="Prancha Mestre NORTHBORN 4.0"> | <img src="art/concepts/northborn-screen-direction.webp" width="380" alt="Direção de Telas NORTHBORN 4.0"> |

> *Conceito não é captura real.* Os arquivos acima são referências de direção artística fora do APK. O único ativo gerado integrado ao APK é [`northborn_materials_atlas.webp`](../app/src/main/res/drawable-nodpi/northborn_materials_atlas.webp) (`1024×512`, `47,402 B`).

---

## 2. Ruptura Estrutural (`1.7.5.1` × `1.7.5.9`)

| Tela | 1.7.5.1 (Antes) | 1.7.5.9 (Depois · NORTHBORN 4.0) | Melhorias Estruturais Verificadas |
|---|---|---|---|
| **Mesa de Odin** | <img src="screenshots/1.7.5.1/01-mesa-de-odin-refinada.png" width="220" alt="Mesa 1.7.5.1"> | <img src="screenshots/1.7.5.9/06-mesa-de-odin.png" width="220" alt="Mesa 1.7.5.9"> | Centro de comando desenclausurado; Botões 4.0 limpos sem adesivo rasterizado; KPIs minerais de alto contraste; abas limpas |
| **Gantt Interativo** | <img src="screenshots/1.7.5.1/06-gantt-refinado.png" width="220" alt="Gantt 1.7.5.1"> | <img src="screenshots/1.7.5.9/11-gantt.png" width="220" alt="Gantt 1.7.5.9"> | Cabeçalho de abas desenclausurado; trilhos escuros com contraste claro/escuro unificado; divisores geométricos limpos |
| **Quadro Kanban** | <img src="screenshots/1.7.5.1/07-kanban-refinado.png" width="220" alt="Kanban 1.7.5.1"> | <img src="screenshots/1.7.5.9/12-kanban.png" width="220" alt="Kanban 1.7.5.9"> | Eliminado caixa-em-caixa: raias arquitetônicas abertas com filete lateral por estado e cartões operacionais diretos |
| **Forja do Tempo** | <img src="screenshots/1.7.5.1/09-forja-do-tempo-trabalho.png" width="220" alt="Forja 1.7.5.1"> | <img src="screenshots/1.7.5.9/13-forja-do-tempo.png" width="220" alt="Forja 1.7.5.9"> | Cronômetro preservado como peça central tátil; seções de som ambiente e notificações desenclausuradas |
| **Conselho dos Nove** | <img src="screenshots/1.7.5.1/12-conselho-dos-nove-refinado.png" width="220" alt="Conselho 1.7.5.1"> | <img src="screenshots/1.7.5.9/15-conselho-dos-nove.png" width="220" alt="Conselho 1.7.5.9"> | Cabeçalho desenclausurado; variação controlada de acentos entre casas; progresso enxuto sem bordas duplicadas |
| **Tema Claro** | <img src="screenshots/1.7.5.1/13-tema-claro-1751.png" width="220" alt="Claro 1.7.5.1"> | <img src="screenshots/1.7.5.9/25-tema-claro.png" width="220" alt="Claro 1.7.5.9"> | Papel linho documental (`#F2EDE3` / `#FAF6EE`) com tinta ferro-gálica (`#1E2228`) e bronze queimado AAA — não é tema escuro invertido |

---

## 3. Galeria Completa das 30 Capturas Reais (`1.7.5.9`)

| 01 · Jornada | 02 · Navegação Inferior | 03 · Registro de Hábito |
|---|---|---|
| <img src="screenshots/1.7.5.9/01-jornada.png" width="220"> | <img src="screenshots/1.7.5.9/02-navegacao-inferior.png" width="220"> | <img src="screenshots/1.7.5.9/03-registro-habito.png" width="220"> |

| 04 · Lista de Hábitos | 05 · Perfil do Herói | 06 · Mesa de Odin |
|---|---|---|
| <img src="screenshots/1.7.5.9/04-lista-habitos.png" width="220"> | <img src="screenshots/1.7.5.9/05-perfil-heroi.png" width="220"> | <img src="screenshots/1.7.5.9/06-mesa-de-odin.png" width="220"> |

| 07 · Propostas de Odin | 08 · Agenda Diária | 09 · Agenda Mensal |
|---|---|---|
| <img src="screenshots/1.7.5.9/07-propostas-odin.png" width="220"> | <img src="screenshots/1.7.5.9/08-agenda-diaria.png" width="220"> | <img src="screenshots/1.7.5.9/09-agenda-mensal.png" width="220"> |

| 10 · Caminhos | 11 · Gantt | 12 · Kanban |
|---|---|---|
| <img src="screenshots/1.7.5.9/10-caminhos.png" width="220"> | <img src="screenshots/1.7.5.9/11-gantt.png" width="220"> | <img src="screenshots/1.7.5.9/12-kanban.png" width="220"> |

| 13 · Forja do Tempo | 14 · Modo Imersivo | 15 · Conselho dos Nove |
|---|---|---|
| <img src="screenshots/1.7.5.9/13-forja-do-tempo.png" width="220"> | <img src="screenshots/1.7.5.9/14-modo-imersivo.png" width="220"> | <img src="screenshots/1.7.5.9/15-conselho-dos-nove.png" width="220"> |

| 16 · Biblioteca de Mímir | 17 · Flashcards SM-2 | 18 · Arsenal |
|---|---|---|
| <img src="screenshots/1.7.5.9/16-biblioteca-mimir.png" width="220"> | <img src="screenshots/1.7.5.9/17-flashcards.png" width="220"> | <img src="screenshots/1.7.5.9/18-arsenal.png" width="220"> |

| 19 · Forja de Darian | 20 · Atlas de Aetheris | 21 · Exploração |
|---|---|---|
| <img src="screenshots/1.7.5.9/19-forja-darian.png" width="220"> | <img src="screenshots/1.7.5.9/20-atlas.png" width="220"> | <img src="screenshots/1.7.5.9/21-exploracao.png" width="220"> |

| 22 · Codex Rúnico | 23 · Configurações | 24 · Onboarding |
|---|---|---|
| <img src="screenshots/1.7.5.9/22-codex-runico.png" width="220"> | <img src="screenshots/1.7.5.9/23-configuracoes.png" width="220"> | <img src="screenshots/1.7.5.9/24-onboarding.png" width="220"> |

| 25 · Tema Claro | 26 · Tema Escuro | 27 · Modo Minimalista |
|---|---|---|
| <img src="screenshots/1.7.5.9/25-tema-claro.png" width="220"> | <img src="screenshots/1.7.5.9/26-tema-escuro.png" width="220"> | <img src="screenshots/1.7.5.9/27-modo-minimalista.png" width="220"> |

| 28 · Tela Estreita 320dp | 29 · Fonte 130% | 30 · Diálogos e Formulários |
|---|---|---|
| <img src="screenshots/1.7.5.9/28-tela-320dp.png" width="220"> | <img src="screenshots/1.7.5.9/29-fonte-130.png" width="220"> | <img src="screenshots/1.7.5.9/30-dialogos-formularios.png" width="220"> |

---

## 4. Autoauditoria Visual (10 Critérios) e Ajustes Iterativos Aplicados

Após a primeira geração das 30 capturas, a inspeção real encontrou e corrigiu:
1. **Margem direita da `NorthbornTopBar` (`30-dialogos-formularios.png`):** o botão `Salvar` encostava na borda direita (`margin=1px`). Ajustado `padding(start = 8.dp, end = 16.dp, top = 6.dp, bottom = 6.dp)` em `NorthbornDesignSystem.kt`.
2. **Restrição real de largura `320dp` (`28-tela-320dp.png`):** na primeira captura o `Surface` raiz expandia para `411dp`; envolvido em `Box(Modifier.fillMaxSize())` no teste para validar composição real em `320dp`.
3. **Captura representativa de Exploração (`21-exploracao.png`):** trocada de região `0` (Vila) para região `1` (*Floresta da Persistência*) para exibir arte regional, marcos e expedições veladas.

---

## 5. Limites Declarados com Transparência

1. **Cota de geração pictórica (`generate_image`):** após gerar as 3 peças originais NORTHBORN (`northborn-master-board-1759.webp`, `northborn-screen-direction.webp`, `northborn_materials_atlas.webp`), o serviço `gemini-3.1-flash-image` atingiu exaustão temporária de cota (`retry after 69h15m`). Nenhum vetor Compose foi declarado como imagem gerada; toda iconografia vetorial (`NorthbornIcon`, `NorthbornDivider`) e os ativos pictóricos anteriores preservados estão catalogados separadamente em [`docs/art/README.md`](art/README.md).
2. **Renderização em Robolectric vs. dispositivo físico:** as 30 capturas foram renderizadas em **Robolectric (Native Graphics, `w411dp-h891dp-mdpi`, API 28)** via `View.draw(Canvas)`. Em telas sem `Scaffold` com rolagem horizontal ativa (`07`, `08`, `09`, `10`, `11`, `16`, `22`), o software canvas do Robolectric reproduz o recorte conhecido em `x > 380 px` já documentado na `1.7.5.1`; em `MainActivity.kt` todas as telas executam dentro de `Scaffold` sobre GPU Android real. A validação física em aparelho Android real permanece pendente.
