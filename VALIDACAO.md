# ASCEND 1.6.8 — validação

17/11/2026. Base 1.6.5 (`cdc6ea9`). `testDebugUnitTest assembleDebug lintDebug`: BUILD SUCCESSFUL. **135 testes aprovados**, zero falhas/erros/ignorados. Uma captura do cronômetro foi acrescentada ao fluxo existente, sem remover asserções.

APK 23684205 bytes, versionName1.6.8 / code15. SHA-256 `7632ecacd5b8dbb65b8ae7c7875c700913c13a63b8c7d7742a2b8fd480156143`. Assinatura v2 válida com certificado preservado `e1f003efe40d53754db66325c40177178aaa81d4ad02ab910c85642c681caae4`; zipalign aprovado; sem INTERNET. Room v6, backup v8 e repositórios preservados.

Renderizações reais de 11 interfaces e cenários estreitos em docs/screenshots/v1.6.8. Conceito da 1.6.5 preservado em docs/art/concepts. Sem teste físico de instalação, launcher ou TalkBack. Alcance artístico parcial e limitações em docs/VISUAL-1.6.8.md. Logs em docs/validation.

# ASCEND 1.6.5 — validação

17/11/2026. Base 1.6.2 (`8e56c8c`). `testDebugUnitTest assembleDebug lintDebug`: BUILD SUCCESSFUL. **135 testes aprovados**, zero falhas/erros/ignorados. Uma captura do cronômetro foi acrescentada ao fluxo existente, sem remover asserções.

APK 22748016 bytes, versionName1.6.5 / code14. SHA-256 `5a2b724dbb06d91aea28afdb47343fd7bd948b88e15505d6bcd0858378977ab0`. Assinatura v2 válida com certificado preservado `e1f003efe40d53754db66325c40177178aaa81d4ad02ab910c85642c681caae4`; zipalign aprovado; sem INTERNET. Room v6, backup v8 e repositórios preservados.

Renderizações reais de 11 interfaces e cenários estreitos em docs/screenshots/v1.6.5. Conceito separado em docs/art/concepts. Sem teste físico de instalação, launcher ou TalkBack. Alcance artístico parcial e limitações em docs/VISUAL-1.6.5.md. Logs em docs/validation.

# Validação — ASCEND 1.6.2

16/11/2026. Base `feat/ascend-1.6.1`, commit `4add529`. Branch `feat/ascend-1.6.2`.

- `testDebugUnitTest assembleDebug lintDebug`: **BUILD SUCCESSFUL**, JDK17 / SDK35 / Gradle8.9.
- **135 testes aprovados**, zero falhas, erros ou ignorados. Nenhum teste existente foi modificado ou removido.
- Lint: **zero erros / 20 avisos** preexistentes, sem novas supressões.
- APK: [ASCEND-1.6.2.apk](downloads/ASCEND-1.6.2.apk?raw=true), **22654144 bytes**.
- SHA-256: `5f54280fbf93c4f7187b80253e38857d4b1165661df98cd64d04ab6c0c3f0130`.
- Assinatura v2 válida, certificado idêntico ao APK 1.6.1: `e1f003efe40d53754db66325c40177178aaa81d4ad02ab910c85642c681caae4`. `zipalign -c 4` aprovado.
- Manifesto: `br.com.ascend.aetheris`, versionName **1.6.2**, versionCode **13**, minSdk26 / targetSdk35. Permissões idênticas à 1.6.1, sem INTERNET.
- Escopo: somente apresentação/layout e versionamento. Room v6, backup v8, repositórios, entidades, regras, economia, recompensas, programação e dados preservados.
- Quatro assets WebP originais somam **135838 bytes**, distribuídos localmente no APK; sem serviço ou download em execução.
- Nove capturas atuais de UI real em renderização nativa Robolectric, incluindo telas prioritárias, navegação em contexto e cenários de 320 dp / fonte130%. Comparação com a 1.6.1 preservada em [VISUAL-1.6.2.md](docs/VISUAL-1.6.2.md).
- A primeira execução revelou seletores ligados ao texto do cabeçalho; o texto original foi mantido e a suíte completa voltou a passar sem adaptação dos testes. Inspeção visual também corrigiu contraste no velino e compactou o resumo diário.
- Testes físicos de instalação sobre a 1.6.1, launcher e TalkBack **não executados**. Pacote, certificado, permissões e incremento de versão foram conferidos; isso não substitui teste físico.

[Build](docs/validation/BUILD-1.6.2.txt) · [APK/assinatura](docs/validation/APK-1.6.2.txt) · [Lint](docs/validation/LINT-1.6.2.txt) · [XMLs executados](docs/validation/).

---

## Registro anterior da 1.6.1

# Validação — ASCEND 1.6.1

16/11/2026. Base `feat/ascend-1.6`, commit `861acb5`. Branch de entrega `feat/ascend-1.6.1`.

- `testDebugUnitTest assembleDebug lintDebug`: **BUILD SUCCESSFUL** com JDK17 / SDK35 / Gradle8.9.
- **135 testes aprovados**, zero falhas, erros ou ignorados. Suíte anterior preservada; seletor do Planejador adaptado à descrição acessível da seta, sem remover asserções.
- Lint: **zero erros / 20 avisos** preexistentes; sem novas supressões.
- APK: [ASCEND-1.6.1.apk](downloads/ASCEND-1.6.1.apk?raw=true), **23,439,812 bytes**.
- SHA-256: `e0a932269b6dd6bd588c4a455ad7b2281b64538c2c3071881df70dcb8a067c2d`.
- Assinatura v2 válida, certificado idêntico ao APK 1.6.0: `e1f003efe40d53754db66325c40177178aaa81d4ad02ab910c85642c681caae4`. `zipalign -c 4` aprovado.
- Manifesto: `br.com.ascend.aetheris`, versionName **1.6.1**, versionCode **12**, minSdk26 / targetSdk35. Sem permissão INTERNET.
- Escopo de produção: somente apresentação/layout/versionamento. Nenhuma alteração em repositórios, entidades, esquemas Room v6, backup v8, ledger, economia, recompensas ou permissões.
- Nove capturas nativas atuais: seis telas P0, Análises e cenários estreitos de Jornada e Planejador. Renderização real em Robolectric, **não em aparelho físico**; comparações preservam imagens anteriores da 1.6.
- Jornada e Planejador em 320 dp / texto130%, tema claro/minimalista/redução de movimento e regressões existentes continuam cobertos. Revisão visual das telas em [VISUAL-1.6.1.md](docs/VISUAL-1.6.1.md).
- Instalação física sobre a 1.6.0, launcher e TalkBack **não executados**. A compatibilidade de identidade é conferida por pacote/certificado e incremento de versão; isso não substitui teste físico.

[Log do build](docs/validation/BUILD-1.6.1.txt) · [APK/assinatura](docs/validation/APK-1.6.1.txt) · [Lint](docs/validation/LINT-1.6.1.txt) · [XMLs executados](docs/validation/).

---

## Registro anterior da 1.6.0

# Validação — ASCEND 1.6.0

16/11/2026. Base `feat/ascend-1.5.2`, commit `67c3bd1cc0309ee262538d7343a4f9015e09d4d6`. Desenvolvimento em `feat/ascend-1.6`, com marcos publicados sem modificar a principal.

- `testDebugUnitTest assembleDebug lintDebug`: **BUILD SUCCESSFUL**.
- **135 testes aprovados**, zero falhas, erros ou ignorados: **116 anteriores + 19 novos**.
- Android Lint: **zero erros, 20 avisos** documentados, sem supressão.
- APK: [downloads/ASCEND-1.6.0.apk](downloads/ASCEND-1.6.0.apk?raw=true), **23.345.281 bytes**.
- SHA-256: `27f8047d59ed4e8e71ef5b46da553e419aa9923e5c60c49ec3492c6d6a80eced`.
- `zipalign -c 4`: aprovado. Assinatura v2 válida; certificado igual ao APK 1.5.2: `e1f003efe40d53754db66325c40177178aaa81d4ad02ab910c85642c681caae4`.
- Manifesto: `br.com.ascend.aetheris`, versionName **1.6.0**, versionCode **11**, minSdk **26**, targetSdk **35**. Sem INTERNET, serviço contínuo, VIBRATE ou alarme exato.
- JDK Temurin 17, SDK Android 35, Gradle 8.9. APK pessoal de depuração; chave preservada fora do Git.

[Log, XMLs, Lint e verificação do APK](docs/validation/).

## Cobertura nova

| Suíte | Testes | Resultado executado |
|---|---:|---|
| ProductivityTest | 15 | Incluir/dispensar/remover exceção, data futura, rejeição de passado, descanso/inatividade/histórico, widget e lembrete com a mesma programação, métricas/snapshots sem XP de planejamento, foco concorrente com pagamento único, cancelamento/conclusão, reload/arquivo/exclusão, backup v8/antigo v7, importação inválida preservada, esquema v5 real → v6 com foco legado, períodos/correções/insights e fechamento/reabertura do arquivo com novas tabelas preenchidas |
| ProductivityUiTest | 3 | Jornada → Planejador → dispensa → widget/métricas → Análises → detalhe de hábito; iniciar foco pelo formulário, encerrar, consultar histórico/detalhes e retornar sem duplicação; Activity recriada com sessão pausada preservando ID/tempo; 320 dp/130%/claro/minimalista/redução de movimento, semana futura e estados vazios |
| ProductVisualTourTest | 1 | Jornada, Hábitos, Missões, Sagas, Herói, Inventário, Atlas, Crônica, Memórias e widget com a mesma fixture real; também executado no código 1.5.2 em worktree isolado para capturas anteriores |

A suíte anterior foi preservada. Testes de migração passaram a incluir o trecho 5 → 6 sem remover esquemas, dados ou verificações. Seletores de UI foram adaptados para rolar a LazyColumn até o item desejado; todas as asserções econômicas anteriores permanecem.

A regressão cobre Grimório/rotinas, registros/correções, XP proporcional e personalizado, ledger, compras, missões, Sagas, Expedições/sete marcos/três fases do Guardião, transformações, reconstrução, inventário, Memórias, sons, widget/receiver/PendingIntent/host, datas, descanso e backups/migrações anteriores.

## Persistência, migração e economia

Room **v6**: `MIGRATION_5_6` cria `plan_exceptions` e `focus_sessions`, acrescentando `sessionId` e `beganAt` ao slot de foco. Não há reconstrução/destruição das tabelas existentes. Os esquemas 1–6 estão exportados; foram exercitados bancos reais produzidos dos esquemas anteriores, incluindo v5 com dados e foco legado.

Backup **v8** mantém checksum e importação transacional, lendo v1–v7 e acrescentando os novos conjuntos. Restaurar v7 inicia planejamento/sessões vazios; não inventa histórico. O teste fecha e reabre um arquivo de banco com planejamento, sessão e registro preenchidos e compara o estado integral. As validações e o rollback de importação anteriores continuam passando.

Planejamento e análises não concedem XP. O histórico de foco é escrito junto do registro/ledger e limpeza do slot ativo; não há recompensa paralela. Sessões concluídas são fatos históricos e não reescrevem snapshots de recompensa. Alterações/correções dos registros continuam atualizando o ledger e os indicadores derivados.

Programação é resolvida centralmente para Jornada, widget, Planejador, estatísticas, sequências e lembretes. Descanso/inatividade prevalecem sobre inclusão pontual; alterações pontuais só são permitidas de hoje em diante. Datas civis permanecem ISO no armazenamento e brasileiras na apresentação. [Regras completas](docs/PRODUTIVIDADE-1.6.md).

## Revisão visual

[Dez comparações antes/depois e novos fluxos](docs/VISUAL-1.6.md) · [Design System 2.0](docs/DESIGN-SYSTEM-2.0.md).

Capturas reais de Compose/Views/RemoteViews no Robolectric API 28. As imagens anteriores do tour foram geradas na base 1.5.2 em worktree isolado, com teste/fixture iguais. Não são mockups. As pinturas/fontes/sons compartilhados foram comparados byte a byte nos APKs e preservados. Dados de teste não entram no APK.

Foram exercitados 411 × 891 e 320 × 740 dp, fonte 130%, claro/escuro, minimalista e redução de movimento. O teste de sessão usa tempo controlado no banco de teste; não é prova de um minuto físico em aparelho. Os indicadores e telas usam o repositório e as tabelas reais.

## Lint

20 avisos: oito de versões de dependências, seis de textos XML fixos, quatro de atributos de widget usados a partir da API 31 com dimensões mínimas de fallback, um de convenção da posição do parâmetro Modifier e um de simetria RTL. Não foram suprimidos nem usados como justificativa para atualizar todas as dependências. O erro encontrado no produtor assíncrono foi resolvido substituindo-o por estado explícito/LaunchedEffect, mantendo cálculo em dispatcher de trabalho.

## Compatibilidade e limites físicos

SDK, build-tools, platform-tools e ADB disponíveis. `adb devices -l` não encontrou dispositivo; emulador/system-images/AVD não estavam disponíveis. Não foi configurada infraestrutura adicional.

Recriação de Activity e reabertura de banco foram executadas no ambiente de testes. Não houve instalação/atualização física, launcher real, morte física de processo, bloqueio de tela, reboot, meia-noite/fuso reais, bateria, teclado/gestos/leitor de tela físicos ou benchmark de FPS/RAM. Sons opcionais preservados; audição física pendente.

Package/certificado iguais e versionCode 11 atendem aos requisitos de atualização sobre a 1.5.2. Isso não substitui instalar em aparelho. A 1.0 usa outro certificado; siga [INSTALACAO.md](INSTALACAO.md). O APK anterior não foi substituído.

### Checklist físico pendente

- [ ] Exportar backup na 1.5.2, instalar 1.6 por cima sem desinstalar e conferir todos os dados/preferências.
- [ ] Planejar inclusão/dispensa/descanso; conferir Jornada, widget, lembretes e métricas no aparelho.
- [ ] Foco com pausas, bloqueio, troca de app, morte de processo, retomada e encerramento único.
- [ ] Widgets compacto/expandido, redimensionamento, fonte ampliada, tema, ação antiga e virada do dia.
- [ ] Teclado, navegação por gestos, leitor de tela e insets do fabricante em 320/411 dp.
- [ ] Sagas/Expedições, correções, transformações, Memórias e inventário após atualizar.
- [ ] Exportar/restaurar backup v8 e importar backup v7 via provedor de arquivos Android.
- [ ] Uso em modo avião, reinício, lembretes e sons sob restrições reais de bateria.

Limites funcionais: sem novas regiões, serviço permanente, alarme de fim de foco ou edição de sessão concluída; sessões anteriores não são fabricadas. Ranking mostra dez maiores taxas; análise individual tem até 30 registros recentes do período. Algumas telas legadas mantêm rolagem em Column. Fonte acima de 130% e medições físicas adicionais não foram validadas nesta entrega.
