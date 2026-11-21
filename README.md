# ASCEND — Crônicas da Ascensão · 1.7.5.1

[**Baixar APK Android 1.7.5.1**](downloads/ASCEND-1.7.5.1.apk?raw=true) · [APK 1.7.5](downloads/ASCEND-1.7.5.apk?raw=true) · [APK 1.7.3.1](downloads/ASCEND-1.7.3.1.apk?raw=true) · [APK 1.7.3](downloads/ASCEND-1.7.3.apk?raw=true) · [APK 1.7.2](downloads/ASCEND-1.7.2.apk?raw=true) · [APK 1.7.1](downloads/ASCEND-1.7.1.apk?raw=true) · [APK 1.7.0.1](downloads/ASCEND-1.7.0.1.apk?raw=true) · [APK 1.6.9.19](downloads/ASCEND-1.6.9.19.apk?raw=true)

Sistema pessoal offline de planejamento estratégico, projetos avançados, agenda adaptativa, foco, base de conhecimento e RPG nórdico para Android 8.0 ou superior. **Tudo funciona localmente**, sem conta, internet, IA externa, anúncios, assinatura ou serviços pagos. O GitHub distribui o código e o instalador; o aplicativo não tem permissão INTERNET.

A versão **1.7.5.1 — O Conselho Restaurado** (*Technical Stabilization & Nordic Viking Visual Refinement*) consolida a estabilidade técnica e refina a estética nórdica sobre a base `feat/ascend-1.7.5`, preservando integralmente **Room v11**, **Backup v13**, todos os módulos anteriores e a economia do RPG:
- **Etapa I — Estabilização Técnica (`AUDITORIA-1.7.5.1.md`, `ESTABILIDADE-1.7.5.1.md`):** reconciliação automática de ciclos vencidos em segundo plano na Forja do Tempo (`reconcileFocusTimer` protegido por `Mutex`), projeção real de recorrências (`@rec:YYYY-MM-DD`) em Agenda, Simulador de Capacidade, Mesa de Odin e Replanejamento, validação centralizada de horários (`validateScheduledSlot`, sem fallback silencioso às `09:00`), gating estrito de dependências no agendamento completo e parcial (`TaskScheduleState`), desfazer condicional preservando edições manuais de `PathTask` e `PlanningClock` injetável filtrando horas passadas do dia atual.
- **Etapa II — Design System 3.2.1 & Refinamento Nórdico (`NordicCouncilDesign.kt`, `NorthComponents.kt`):** 6 famílias metálicas de botões (`restored_council_buttons_atlas.webp`) com hierarquia clara e contraste AA/AAA, 8 marcadores dedicados de planejamento (`nordic_planning_marks_atlas.webp`), 6 medalhões refinados das estratégias de Odin (`odin_council_seals_refined_atlas.webp`), instrumentos dedicados de trabalho/descanso/ciclo/pausa na Forja do Tempo (`time_forge_refinement_atlas.webp`), cantoneiras e divisores Urnes/Borre/Velino (`nordic_strategy_frames_atlas.webp`) e runas históricas contextuais do *Elder Futhark* (`365,6 KB / 0,36 MB` adicionados ao APK).
- **Mesa de Odin, Caminhos 3.0, Agenda 3.0, Forja do Tempo & Conselho dos Nove:** centro de comando com CTA cerimonial em largura total, Gantt sem truncamento nas barras, Kanban em 4 colunas distintas com ações táteis, distinção visual de ocorrências recorrentes projetadas (`@rec:`), distinção bronze/prata/pausa na Forja do Tempo e selos ampliados no Conselho dos Nove.

A `1.7.5.1` tem **227 testes aprovados** (`218` preservados da `1.7.5` + `8` testes de estabilização em `Stability1751AuditTest` + `1` suíte de 14 capturas comparativas em `VisualRefinement1751UiTest`), **14 capturas reais comparativas inspecionadas** e Android Lint com **0 erros**. **Room v11** e **backup v13** permanecem inalterados e retrocompatíveis com `v1`–`v13`.

**Atualização dos APKs 1.1 a 1.7.5:** a `1.7.5.1` (`versionCode 28`) mantém `applicationId` (`br.com.ascend.aetheris`), certificado oficial SHA-256 (`e1f003ef...`) e Room v11; exporte um backup de segurança e instale por cima, **sem desinstalar**. A antiga 1.0 possui assinatura diferente; consulte [INSTALACAO.md](INSTALACAO.md).

- [Instalação e atualização](INSTALACAO.md)
- [Funcionalidades e limites reais](PROGRESSO.md)
- [Assinatura e preservação de atualizações](ASSINATURA.md)
- [Validação](VALIDACAO.md)
- [Auditoria 1.7.5.1](docs/AUDITORIA-1.7.5.1.md) · [Estabilidade 1.7.5.1](docs/ESTABILIDADE-1.7.5.1.md) · [Design System 3.2.1](docs/DESIGN-SYSTEM-3.2.1.md) · [Visual 1.7.5.1](docs/VISUAL-1.7.5.1.md) · [Diário 1.7.5.1](docs/DESENVOLVIMENTO-1.7.5.1.md)
- [Mesa de Odin](docs/MESA-DE-ODIN.md) · [Caminhos 3.0](docs/CAMINHOS-3.0.md) · [Agenda 3.0](docs/AGENDA-3.0.md) · [Forja do Tempo](docs/FORJA-DO-TEMPO.md) · [Conselho dos Nove](docs/CONSELHO-DOS-NOVE.md) · [Design System 3.2](docs/DESIGN-SYSTEM-3.2.md) · [Visual 1.7.5](docs/VISUAL-1.7.5.md)
- [Heráldica Rúnica](docs/HERALDICA-RUNICA.md) · [Codex Rúnico](docs/CODEX-RUNICO.md) · [Coleções Lendárias](docs/COLECOES-LENDARIAS.md) · [Aprimoramentos da Forja](docs/APRIMORAMENTOS-DA-FORJA.md) · [Visual 1.7.3.1](docs/VISUAL-1.7.3.1.md)
- [Retrato Vivo 1.7.3](docs/RETRATO-VIVO-1.7.3.md) · [Forja de Darian 1.7.3](docs/FORJA-DARIAN-1.7.3.md) · [Arsenal 1.7.3](docs/ARSENAL-1.7.3.md) · [Visual 1.7.3](docs/VISUAL-1.7.3.md)
- [Exploração 1.7.2](docs/EXPLORACAO-1.7.2.md) · [Atlas 2.0](docs/ATLAS-2.0.md) · [Design System 3.0](docs/DESIGN-SYSTEM-3.0.md) · [Biblioteca de Mímir 1.7.1](docs/BIBLIOTECA-MIMIR-1.7.1.md)

## Visual

| Mesa de Odin (DS 3.2.1) | Caminhos 3.0 · Gantt Refinado | Forja do Tempo (Trabalho) |
|---|---|---|
| <img src="docs/screenshots/1.7.5.1/01-mesa-de-odin-refinada.png" width="240" alt="Mesa de Odin 1.7.5.1"> | <img src="docs/screenshots/1.7.5.1/06-gantt-refinado.png" width="240" alt="Gantt refinado 1.7.5.1"> | <img src="docs/screenshots/1.7.5.1/09-forja-do-tempo-trabalho.png" width="240" alt="Forja do Tempo 1.7.5.1"> |

**[Relatório visual e 14 capturas reais 1.7.5.1](docs/VISUAL-1.7.5.1.md)** · [Visual 1.7.5](docs/VISUAL-1.7.5.md) · [Visual 1.7.3.1](docs/VISUAL-1.7.3.1.md) · [Visual 1.7.3](docs/VISUAL-1.7.3.md) · [Visual 1.7.2](docs/VISUAL-1.7.2.md) · [Visual 1.7.1](docs/VISUAL-1.7.1.md) · [Visual 1.7.0](docs/VISUAL-1.7.0.md)

Capturas dos testes de interface com personagens de teste. Nenhuma conclusão, expedição, fabricação, heráldica, plano ou objetivo fictício é incluído no aplicativo.

## Desenvolvimento

Kotlin, Jetpack Compose, Material 3, Room, DataStore, MVVM, Navigation Compose, Coroutines e StateFlow. JDK 17 ou 21, SDK Android 35, Gradle Wrapper 8.9. Configure o SDK por `ANDROID_HOME` ou `local.properties`.

```sh
./gradlew testDebugUnitTest assembleDebug lintDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`. Preparar as dependências de desenvolvimento exige internet; usar o APK não exige conexão. Não é necessário pagar por nenhuma ferramenta.

Para usar a chave de depuração preservada, configure `ASCEND_KEYSTORE_PATH` com o caminho do arquivo antes da compilação. Sem essa variável, o Android Gradle Plugin usa a chave de depuração padrão do ambiente. Consulte `ASSINATURA.md` antes de gerar atualizações.

O fluxo central continua: registro por hábito/dia → recompensa com chave determinística → ledger transacional → nível/atributos/moedas derivados. Recompensas são snapshots; editar um hábito não reescreve XP histórico. Estratégias da Mesa de Odin, replanejamentos, Caminhos 3.0, Agenda 3.0, pausas da Forja do Tempo, Conselho dos Nove e Objetivos Pessoais não inventam XP ou moedas nem aplicam penalidades ao RPG.

Banco **Room v11**, com migrações explícitas `1 → 2 → 3 → 4 → 5 → 6 → 7 → 8 → 9 → 10 → 11`; **backups v13** leem também arquivos `v1`–`v12`. A migração `10 → 11` é puramente aditiva, acrescentando colunas com `DEFAULT` e 9 novas tabelas estratégicas sem reconstruir ou alterar dados históricos. Nenhum fallback destrutivo é usado. Preferências de aparência, favoritos do Codex, som ambiente e notificações ficam no aparelho via DataStore.
