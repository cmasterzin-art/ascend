# ASCEND — Crônicas da Ascensão · NORTHBORN 4.2

O **ASCEND 1.7.5.9.3 · Refinamento Nórdico** eleva o acabamento estético da base `1.7.5.9.2` com placas metálicas acetinadas em supersampling 3× (sem colisão de texto nem bordas pretas pesadas no tema claro), fechos dedicados para tema escuro e tema claro na navegação inferior, abas editoriais unificadas com filete geométrico em bronze, divisores limpos sem halo de recorte, câmara da Mesa de Odin em ferro vulcânico limpo com grade equilibrada, leitura de Mímir sem empilhamento de lombadas, Missões sem banner duplicado e capa atmosférica no Conselho dos Nove. [Relatório visual e comparativos `1.7.5.9.2 × 1.7.5.9.3`](docs/VISUAL-1.7.5.9.3.md)

[**Baixar ASCEND 1.7.5.9.3 · código 37**](downloads/ASCEND-1.7.5.9.3.apk?raw=true) · [SHA-256](downloads/ASCEND-1.7.5.9.3.apk.sha256) · [Validação](VALIDACAO.md)

Base anterior preservada: [ASCEND 1.7.5.9.2 · código 36](downloads/ASCEND-1.7.5.9.2-forja.apk?raw=true) ([design dos controles](docs/design/NORTHBORN-FORJA.md)) · [APK 1.7.5.9.2 código 35](downloads/ASCEND-1.7.5.9.2.apk?raw=true) ([galeria 4.1](docs/design/NORTHBORN-FINALIZACAO.md)) · [Direção artística](docs/design/NORTHBORN-ART-DIRECTION.md)

[APK do piloto rústico](downloads/ASCEND-1.7.5.9.1-dev-rustico.apk?raw=true) · [APK de fundos e gravuras](downloads/ASCEND-1.7.5.9.1-dev-gravuras.apk?raw=true) · [APK de escudo, madeira e velino](downloads/ASCEND-1.7.5.9.1-dev-entalhe.apk?raw=true) · [APK da revisão nórdica](downloads/ASCEND-1.7.5.9.1-dev-nordico.apk?raw=true) · [APK do primeiro protótipo](downloads/ASCEND-1.7.5.9.1-dev.apk?raw=true)

[**Baixar APK Android 1.7.5.9**](downloads/ASCEND-1.7.5.9.apk?raw=true) · [APK 1.7.5.1](downloads/ASCEND-1.7.5.1.apk?raw=true) · [APK 1.7.5](downloads/ASCEND-1.7.5.apk?raw=true) · [APK 1.7.3.1](downloads/ASCEND-1.7.3.1.apk?raw=true) · [APK 1.7.3](downloads/ASCEND-1.7.3.apk?raw=true) · [APK 1.7.2](downloads/ASCEND-1.7.2.apk?raw=true) · [APK 1.7.1](downloads/ASCEND-1.7.1.apk?raw=true) · [APK 1.7.0.1](downloads/ASCEND-1.7.0.1.apk?raw=true) · [APK 1.6.9.19](downloads/ASCEND-1.6.9.19.apk?raw=true)

Sistema pessoal offline de planejamento estratégico, projetos avançados, agenda adaptativa, foco, base de conhecimento e RPG nórdico para Android 8.0 ou superior. **Tudo funciona localmente**, sem conta, internet, IA externa, anúncios, assinatura ou serviços pagos. O GitHub distribui o código e o instalador; o aplicativo não tem permissão INTERNET.

A versão **1.7.5.9.3 — Refinamento Nórdico (NORTHBORN 4.2)** preserva integralmente **100% das regras de negócio**, **Room v11**, **Backup v13**, todos os **229 testes aprovados** e a economia do RPG:
- **Metalurgia Acetinada e Tema Claro Documental (`docs/art/tools/refine_northborn_craft.py`, `ForgedControls.kt`, `NorthbornDesignSystem.kt`):** placas `forge_plate_*.webp` e 9-patches de widgets redesenhados em supersampling 3× (grão escovado fino, bisel boreal luminoso, micro-losangos nas extremidades com margem segura `20–24 dp`, placa `pewter` em alabastro quente sem contorno preto no tema claro).
- **Navegação, Abas e Coerência Entre Módulos (`AetherisNavigation.kt`, `NorthbornRebirth.kt`, `OdinCouncilScreens.kt`, `MimirScreens.kt`, `RpgDesign.kt`, `CouncilOfNineScreen.kt`, `JourneyHome.kt`):** topo da barra inferior em filete de bronze com micro-losango, fechos distintos escuro/claro (`engraved_clasp.webp` e `engraved_clasp_light.webp`), abas editoriais chanfradas unificadas, ferro vulcânico limpo e grade `2 + 3` na Mesa de Odin, leitura de Mímir com lombada costurada exclusiva nos fólios, Missões sem banner duplo e atalhos da Jornada nivelados em `320 dp / 130%`.

**Atualização dos APKs 1.1 a 1.7.5.9.2:** a `1.7.5.9.3` (`versionCode 37`) mantém `applicationId` (`br.com.ascend.aetheris`), certificado oficial SHA-256 (`e1f003ef...`) e Room v11; exporte um backup de segurança e instale por cima, **sem desinstalar**. A antiga 1.0 possui assinatura diferente; consulte [INSTALACAO.md](INSTALACAO.md).

- [Instalação e atualização](INSTALACAO.md)
- [Funcionalidades e limites reais](PROGRESSO.md)
- [Assinatura e preservação de atualizações](ASSINATURA.md)
- [Validação](VALIDACAO.md)
- [Visual 1.7.5.9.3 — NORTHBORN 4.2](docs/VISUAL-1.7.5.9.3.md) · [Controles 1.7.5.9.2](docs/design/NORTHBORN-FORJA.md) · [Auditoria Visual 1.7.5.9](docs/AUDITORIA-VISUAL-1.7.5.9.md) · [NORTHBORN DS 4.0](docs/NORTHBORN-DESIGN-SYSTEM-4.0.md)
- [Auditoria 1.7.5.1](docs/AUDITORIA-1.7.5.1.md) · [Estabilidade 1.7.5.1](docs/ESTABILIDADE-1.7.5.1.md) · [Design System 3.2.1](docs/DESIGN-SYSTEM-3.2.1.md) · [Visual 1.7.5.1](docs/VISUAL-1.7.5.1.md)
- [Mesa de Odin](docs/MESA-DE-ODIN.md) · [Caminhos 3.0](docs/CAMINHOS-3.0.md) · [Agenda 3.0](docs/AGENDA-3.0.md) · [Forja do Tempo](docs/FORJA-DO-TEMPO.md) · [Conselho dos Nove](docs/CONSELHO-DOS-NOVE.md) · [Design System 3.2](docs/DESIGN-SYSTEM-3.2.md) · [Visual 1.7.5](docs/VISUAL-1.7.5.md)
- [Heráldica Rúnica](docs/HERALDICA-RUNICA.md) · [Codex Rúnico](docs/CODEX-RUNICO.md) · [Coleções Lendárias](docs/COLECOES-LENDARIAS.md) · [Aprimoramentos da Forja](docs/APRIMORAMENTOS-DA-FORJA.md) · [Visual 1.7.3.1](docs/VISUAL-1.7.3.1.md)
- [Retrato Vivo 1.7.3](docs/RETRATO-VIVO-1.7.3.md) · [Forja de Darian 1.7.3](docs/FORJA-DARIAN-1.7.3.md) · [Arsenal 1.7.3](docs/ARSENAL-1.7.3.md) · [Visual 1.7.3](docs/VISUAL-1.7.3.md)
- [Exploração 1.7.2](docs/EXPLORACAO-1.7.2.md) · [Atlas 2.0](docs/ATLAS-2.0.md) · [Design System 3.0](docs/DESIGN-SYSTEM-3.0.md) · [Biblioteca de Mímir 1.7.1](docs/BIBLIOTECA-MIMIR-1.7.1.md)

## Visual

| Jornada (Escuro Mineral) | Jornada (Tema Claro Documental) | Mesa de Odin (NORTHBORN 4.2) |
|---|---|---|
| <img src="docs/screenshots/1.7.5.9.3/01-jornada.png" width="240" alt="Jornada 1.7.5.9.3 Escuro"> | <img src="docs/screenshots/1.7.5.9.3/25-tema-claro.png" width="240" alt="Jornada 1.7.5.9.3 Claro"> | <img src="docs/screenshots/1.7.5.9.3/06-mesa-de-odin.png" width="240" alt="Mesa de Odin 1.7.5.9.3"> |

**[Relatório visual e comparativos `1.7.5.9.2 × 1.7.5.9.3`](docs/VISUAL-1.7.5.9.3.md)** · [Controles 1.7.5.9.2](docs/design/NORTHBORN-FORJA.md) · [Visual 1.7.5.9](docs/VISUAL-1.7.5.9.md) · [Visual 1.7.5.1](docs/VISUAL-1.7.5.1.md) · [Visual 1.7.5](docs/VISUAL-1.7.5.md) · [Visual 1.7.3.1](docs/VISUAL-1.7.3.1.md) · [Visual 1.7.3](docs/VISUAL-1.7.3.md) · [Visual 1.7.2](docs/VISUAL-1.7.2.md) · [Visual 1.7.1](docs/VISUAL-1.7.1.md) · [Visual 1.7.0](docs/VISUAL-1.7.0.md)

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
