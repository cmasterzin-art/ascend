# Relatório Visual — ASCEND 1.7.5 (A Mesa de Odin)

Todas as **17 capturas reais** da versão `1.7.5` foram renderizadas via Robolectric + Compose (`OdinCouncilUiTest`) em `docs/screenshots/1.7.5/` e inspecionadas visualmente antes do encerramento da versão.

## Prancha Conceitual (Fora do APK)

- `docs/art/concepts/nordic-council-175.webp` (`1376 × 768`, referência de direção artística; não substitui UI funcional).

## Catálogo das 17 Capturas Reais Inspecionadas

| # | Arquivo | Tela / Cenário Validado | Observação da Inspeção |
|---:|---|---|---|
| 01 | `docs/screenshots/1.7.5/01-mesa-de-odin.png` | Salão do Conselho (Mesa de Odin) | Banner panorâmico, KPIs, atalhos estratégicos e abas nítidos |
| 02 | `docs/screenshots/1.7.5/02-configuracao-disponibilidade.png` | Configuração de disponibilidade | Dias da semana, horários de início/fim e lista de janelas |
| 03 | `docs/screenshots/1.7.5/03-proposta-plano-semanal.png` | Proposta de plano semanal | Horizonte, estratégia da prévia, totais disponível/proposto e comparador |
| 04 | `docs/screenshots/1.7.5/04-estrategias-odin.png` | 6 Estratégias de Odin + Plano de Hoje | 6 medalhões forjados em grade 2×3, destaque dourado no Estrategista |
| 05 | `docs/screenshots/1.7.5/05-simulador-capacidade.png` | Simulador de capacidade | 5 períodos, saldo, barra metálica e alertas explícitos de prazo |
| 06 | `docs/screenshots/1.7.5/06-replanejamento-adaptativo.png` | Replanejamento adaptativo | 5 alternativas reais sobre horários livres, sem punição RPG |
| 07 | `docs/screenshots/1.7.5/07-matriz-eisenhower.png` | Matriz de Eisenhower | 4 quadrantes explícitos com alternadores `Imp ★` e `Urg !` |
| 08 | `docs/screenshots/1.7.5/08-caminhos-dependencias-critico.png` | Dependências e Caminho Crítico (CPM) | Esforço crítico, data real pela disponibilidade, sequência e editor |
| 09 | `docs/screenshots/1.7.5/09-gantt-interativo.png` | Gráfico de Gantt interativo | Ajustado após inspeção: chips de escala empilhados acima do trilho; linha de hoje e marcos nítidos |
| 10 | `docs/screenshots/1.7.5/10-quadro-kanban.png` | Quadro Kanban (4 colunas) | A fazer, Em andamento, Bloqueadas com botões acessíveis de transição |
| 11 | `docs/screenshots/1.7.5/11-agenda-30-recorrencia-fixacao.png` | Agenda 3.0 (Fixação e Recorrência) | Selo de recorrência, marca `Fixado · Odin` e ação `Forja do Tempo` |
| 12 | `docs/screenshots/1.7.5/12-forja-do-tempo-pomodoro.png` | Forja do Tempo (Pomodoro) | Ajustado após inspeção: rótulo da sessão abaixo do anel forjado de bronze |
| 13 | `docs/screenshots/1.7.5/13-modo-imersivo-foco.png` | Modo Imersivo de Foco | Aro cronométrico sobre atmosfera nórdica escura + controle de som ambiente |
| 14 | `docs/screenshots/1.7.5/14-conselho-dos-nove.png` | Conselho dos Nove (Áreas da Vida) | Selos metálicos nítidos, progresso semanal e reordenação |
| 15 | `docs/screenshots/1.7.5/15-objetivos-pessoais.png` | Objetivos Pessoais | Metas trimestrais e anuais com progresso verificável |
| 16 | `docs/screenshots/1.7.5/16-tema-claro-175.png` | Tema claro (Mesa de Odin) | Alto contraste sobre velino claro sem elementos estourados |
| 17 | `docs/screenshots/1.7.5/17-estreito-320dp-fonte-130-175.png` | Tela estreita `320dp` + fonte `130%` | Quebra de linha limpa nas abas, KPIs e botões sem sobreposição |

## Limitações Declaradas

- Capturas renderizadas via **Robolectric Native Graphics + Compose** (`sdk = 28`, `w411dp-h891dp` e `320dp` a `130%`), não em dispositivo físico Android real.
- Reprodução sonora do `AudioTrack` (`AmbientAudioEngine`) foi validada via síntese determinística de buffers PCM em JVM; percepção acústica final depende de teste em aparelho físico.
