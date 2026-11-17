# Validação — ASCEND 1.5.2

16/11/2026. Base: `feat/ascend-1.5.1`, commit `2ab6c47a0abafb26d6094d80f14f5c2651ee8dbe`. Publicação em `feat/ascend-1.5.2`.

- `testDebugUnitTest assembleDebug lintDebug`: **BUILD SUCCESSFUL**.
- **116 testes aprovados**: 97 anteriores + 19 novos, zero falhas, erros ou ignorados. Nenhum teste anterior foi removido.
- Android Lint: **zero erros, 21 avisos**, revisados e documentados. Relatórios sem supressão em [docs/validation/](docs/validation/).
- APK: [downloads/ASCEND-1.5.2.apk](downloads/ASCEND-1.5.2.apk?raw=true), **23220921 bytes**.
- SHA-256: `b1bcbb6b2f004e90a8e471e38f7c9ea66f8419350af366aa5bbd165d4b1df739`.
- `zipalign -c 4`: aprovado; assinatura v2 válida.
- Certificado comparado diretamente com o APK 1.5.1: `e1f003efe40d53754db66325c40177178aaa81d4ad02ab910c85642c681caae4`.
- Manifesto: `br.com.ascend.aetheris`, versionName **1.5.2**, versionCode **10**, minSdk **26**, targetSdk **35**. Sem INTERNET, VIBRATE, serviço contínuo ou alarme exato. Permissões anteriores preservadas.
- APK pessoal de depuração; chave privada preservada fora do Git. JDK Temurin 17, SDK 35, Gradle 8.9.

## Defeitos e evidência de regressão

[Relatório da auditoria e gravidades](docs/ESTABILIDADE-1.5.2.md).

Foram reproduzidos: progresso inválido sendo convertido em zero, snapshot novo com hábito desatualizado, início de foco com referência incompatível/excluída, descanso omitido do snapshot de foco, aceitação de datas inválidas em campos de backup, perda de rascunho ao restaurar estado, recusa silenciosa de ação antiga do widget e barra sem atividades. O manipulador de atalhos complexos também foi auditado e passou a revalidar agenda/tipo/estado/data.

| Suíte nova | Quantidade | Evidência |
|---|---:|---|
| StabilityTest | 11 | Snapshot atual e histórico preservado, rejeição de quantidade negativa, foco com referência antiga, descanso, tempo/reload/encerramento repetido, datas inválidas em backup, rollback com falha de inserção injetada, resolução de atalhos e período de um dia |
| WidgetStabilityTest | 5 | Widget mínimo em 180 dp/130%, ação real do dia anterior com mensagem, agenda e estados alterados com concorrência, host após correção/backup e fallback sem botões antigos diante de falha de leitura injetada |
| StabilityUiTest | 3 | Entrada inválida recuperável sem apagar registro; rascunho de hábito via estado salvo com descarte confirmado; Activity recriada duas vezes após registro de widget, preservando quantidade e sem reabrir atalho consumido |

Na reprodução inicial, sete testes do núcleo falharam, assim como os cenários de widget e formulário relacionados. Após a correção, a suíte completa final passou. Ajustes nos testes anteriores limitam-se ao período de data única da Crônica; verificações econômicas não foram reduzidas.

A regressão inclui catálogo/rotinas, criação/edição, pausa/arquivo/programação, histórico/correção, XP proporcional/personalizado, compensação, missões, Sagas, resgates concorrentes, três Expedições/sete marcos/três fases do Guardião, transformações, Memórias, inventário, reconstrução, estatísticas, preferências sonoras, migrações e backups antigos.

## Dados, compatibilidade e offline

Room **v5** e backup **v7** foram mantidos, sem alteração de tabelas/migração nova ou fallback destrutivo. Migrações 1 → 2 → 3 → 4 → 5 e formatos de backup v1–v7 continuam cobertos pelos testes anteriores. Novas validações são anteriores à substituição transacional. A injeção de falha durante a importação comprovou rollback e preservação do estado no banco recarregado; depois, o backup válido foi restaurado.

Datas brasileiras são apenas apresentação; datas civis persistidas permanecem ISO sem conversão de fuso. A fórmula de XP, recompensas, snapshots históricos e economia não foram alterados. Pinturas/fontes/sons locais foram preservados e comparados nos APKs. Nenhum recurso depende de conta, servidor ou serviço pago.

Pacote, certificado, minSdk compatível e versionCode maior atendem aos requisitos de atualização sobre a 1.5.1 fornecida. **Instalação/atualização física não foi executada.** A primeira 1.0 usa outro certificado. Exporte um backup e siga [INSTALACAO.md](INSTALACAO.md), sem desinstalar a 1.5.1.

## Lint e manutenção

Os 21 avisos incluem atualização de dependências, atributos de widget para API 31 com fallback de dimensões mínimas, textos XML fixos, texto de 10 sp, offset por lambda e simetria RTL. Não foram ocultados; não justificaram atualização ampla de dependências ou redesenho nesta versão. [Relatório](docs/validation/LINT-1.5.2.txt).

## Ambiente e limites reais

SDK, build-tools, platform-tools e ADB foram verificados. `adb devices -l`: nenhum dispositivo. Emulador, system-images e AVD não disponíveis; não foi montada infraestrutura adicional.

Robolectric API 28: UI nativa em 411/320 dp, claro/escuro, 130%, modo minimalista e redução de movimento nas suítes de regressão. As duas recriações de Activity foram executadas nesse ambiente; restauração de estado Compose também foi exercitada. RemoteViews/PendingIntents/receivers são reais; o host do launcher é simulado. Falhas de escrita/leitura foram injetadas em bancos de teste, sem alegar defeito físico real de armazenamento.

Não foram executados instalação em telefone, launcher físico, bloqueio/encerramento físico do processo, bateria/reboot, meia-noite/fuso reais, audição, benchmark de FPS/RAM ou insets/teclado/leitor de tela físicos. `adjustResize` e `imePadding` foram preservados; o teste de digitação não substitui teclado Android completo. Atualizações do widget e lembretes continuam aproximadas e sujeitas ao fabricante.

O [checklist físico](docs/ESTABILIDADE-1.5.2.md#checklist-físico-pendente--nada-abaixo-foi-marcado-como-aprovado) está integralmente pendente. Limites anteriores, como ausência de edição/abandono de Expedições após início e leitura de backup até 20 MB, permanecem documentados. Não houve expansão de conteúdo.
