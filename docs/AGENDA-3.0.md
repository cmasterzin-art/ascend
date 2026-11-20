# Agenda 3.0 — Disponibilidade, Fixação e Recorrência (ASCEND 1.7.5)

A **Agenda 3.0** evolui diretamente o `PlannerScreen` existente (`AgendaScreens.kt`), mantendo visão única integrada de Dia, Semana e Mês sem introduzir uma segunda agenda paralela.

## 1. Diferenciação Visual e Origem dos Blocos

Utilizando os símbolos de `runic_calendar_atlas.webp`, cada item exibe claramente sua natureza:

- **Compromissos fixos (`AgendaEvent`):** respeitados como restrições rígidas pela Mesa de Odin.
- **Blocos manuais (`TimeBlock`):** editáveis livremente.
- **Blocos da Mesa de Odin (`sourcePlanId`):** identificados com a marca `Odin`; se editados manualmente pelo usuário, passam a `modifiedManually = true` para proteção na reversão de planos.
- **Blocos fixados (`pinned = true`):** protegidos contra realocação em todas as 6 estratégias de Odin e preservados durante o desfazer inteligente.
- **Blocos recorrentes (`RecurrenceRule`):** sinalizados com selo rúnico próprio e ação de gerenciamento de série.

## 2. Recorrências Sem Mutação Histórica

- Suporte a quatro padrões: **Diária** (`daily`), **Semanal** (`weekly`), **Dias selecionados** (`weekdays`, ex.: `1,3,5`) e **Mensal** (`monthly`), com data final opcional (`endDate`).
- **Exceção de ocorrência única (`excludeRecurrenceDate`):** permite dispensar uma data específica adicionando-a a `excludedDates` sem alterar ocorrências passadas nem o histórico já cumprido.

## 3. Integração Direta com Mesa de Odin e Forja do Tempo

- Atalhos diretos na própria Agenda para **Mesa de Odin** e **Replanejar**.
- Cada bloco oferece a ação **Forja do Tempo**, iniciando sessões de foco vinculadas diretamente ao `blockId` correspondente, sem dupla contagem no histórico.
