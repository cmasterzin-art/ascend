# ASCEND 1.7.2 — Validação

19/11/2026. Base `feat/ascend-1.7.1` (`88565d6`). Branch `feat/ascend-1.7.2`. GitHub Actions execução `44468019721` (commits `f3e369b` / `e0f12f8`): `testDebugUnitTest assembleDebug lintDebug` concluído com **BUILD SUCCESSFUL**.

- **Testes:** **181 testes aprovados** (`167` preservados da `1.7.1` + `4` regressões em `MimirAndArchetypesTest` + `10` novos em `WorldExpansionTest` e `WorldExpansionUiTest`), zero falhas, erros ou ignorados.
- **Android Lint:** **0 erros e 29 avisos** no CI (`28` localmente), sem novas supressões.
- **APK oficial assinado:** [downloads/ASCEND-1.7.2.apk](downloads/ASCEND-1.7.2.apk?raw=true) com **25.734.582 bytes**, `versionName='1.7.2'`, `versionCode='23'`, `package: name='br.com.ascend.aetheris'`, `minSdkVersion='26'`, `targetSdkVersion='35'`, sem permissão `INTERNET`.
- **Checksum SHA-256 do APK:** `69cf6a92ffa2166d5a4cec9a48a9d79dcc0a45fbac2c7829d730d5f31c9a897b` (`sha256sum -c downloads/ASCEND-1.7.2.apk.sha256`: **OK**).
- **Assinatura e alinhamento:** `Zipalign OK`; APK Signature Scheme v2 válida com o certificado oficial preservado `e1f003efe40d53754db66325c40177178aaa81d4ad02ab910c85642c681caae4`.
- **Persistência e retrocompatibilidade:** Room v8 e backup v10 preservados integralmente sem migração destrutiva (compatibilidade `v1`–`v10` validada em teste), Floresta da Persistência (`roots`, `sanctuary`, `vigil`) e economia de XP/moedas preservadas.
- **Acervo pictórico e capturas:** 10 novos recursos WebP originais (`1` prancha conceitual em `docs/art/concepts/nordic-viking-ui-172.webp` fora do APK + `9` atlas de produção somando `845.042 bytes` / `0,81 MB` dentro do APK, abaixo do limite de `3 MB`). 14 capturas reais de produção em `docs/screenshots/1.7.2/`, incluindo prancha do Design System 3.0, Atlas 2.0, Biblioteca das Eras e Montanhas da Disciplina (antes e depois), Relíquia equipada, Tema Claro e `320 dp` / fonte `130%`.
- **Limites:** validação executada em Robolectric e GitHub Actions; instalação física por cima da `1.7.1` em aparelho Android, gestos reais de pinça em tela sensível ao toque, teclado IME físico e TalkBack não foram executados.

[APK](downloads/ASCEND-1.7.2.apk?raw=true) · [Build](docs/validation/BUILD-1.7.2.txt) · [Lint](docs/validation/LINT-1.7.2.txt) · [Certificado e manifesto](docs/validation/APK-1.7.2.txt) · [Exploração 1.7.2](docs/EXPLORACAO-1.7.2.md) · [Atlas 2.0](docs/ATLAS-2.0.md) · [Design System 3.0](docs/DESIGN-SYSTEM-3.0.md) · [Renascimento Rúnico](docs/RENASCIMENTO-RUNICO.md) · [Visual 1.7.2](docs/VISUAL-1.7.2.md) · [Diário 1.7.2](docs/DESENVOLVIMENTO-1.7.2.md)

---

# ASCEND 1.7.1 — Validação

19/11/2026. Base `feat/ascend-1.7.0.1` (`230cc15`). Branch `feat/ascend-1.7.1`. GitHub Actions execução `44441287022` (commits `6148640` / `2a44389`): `testDebugUnitTest assembleDebug lintDebug` concluído com **BUILD SUCCESSFUL**.

- **Testes:** **167 testes aprovados** (`155` preservados da `1.7.0.1` + `12` novos em `MimirAndArchetypesTest` e `MimirAndArchetypesUiTest`), zero falhas, erros ou ignorados.
- **Android Lint:** **0 erros e 24 avisos** no CI (`23` localmente), sem novas supressões.
- **APK oficial assinado:** [downloads/ASCEND-1.7.1.apk](downloads/ASCEND-1.7.1.apk?raw=true) com **24.755.664 bytes**, `versionName='1.7.1'`, `versionCode='22'`, `package: name='br.com.ascend.aetheris'`, `minSdkVersion='26'`, `targetSdkVersion='35'`, sem permissão `INTERNET`.
- **Checksum SHA-256 do APK:** `dd1cf12d99822388b10fecbcce55b5e52af8c17a67d10b44a9d2b2438537c39f` (`sha256sum -c downloads/ASCEND-1.7.1.apk.sha256`: **OK**).
- **Assinatura e alinhamento:** `Zipalign OK`; APK Signature Scheme v2 válida com o certificado oficial preservado `e1f003efe40d53754db66325c40177178aaa81d4ad02ab910c85642c681caae4`.
- **Persistência e retrocompatibilidade:** Room v8 (`MIGRATION_7_8` validada sobre banco SQLite real v7), backup v10 (compatibilidade `v9 → v10`, round-trip completo e rollback atômico em falha validados em teste) e Biblioteca da Forja (`1.6.9.19`) integralmente preservados.
- **Acervo pictórico e capturas:** 3 novos WebPs (`mimir_library.webp`, `archetype_seals_atlas.webp`, `mimir_codex_atlas.webp`) somando `359.502` bytes (`351,1 KB`). Renderizações reais em Robolectric em `docs/screenshots/v1.7.1/` (7 novas telas/variantes da 1.7.1 + 6 telas preservadas da 1.7.0), incluindo `320 dp` / fonte `130%` / tema claro / minimalista.
- **Limites:** validação em Robolectric e CI; instalação física por cima da `1.7.0.1` em aparelho Android, IME físico em texto longo Markdown e TalkBack não foram executados.

[APK](downloads/ASCEND-1.7.1.apk?raw=true) · [Build](docs/validation/BUILD-1.7.1.txt) · [Lint](docs/validation/LINT-1.7.1.txt) · [Certificado e manifesto](docs/validation/APK-1.7.1.txt) · [Biblioteca de Mímir](docs/BIBLIOTECA-MIMIR-1.7.1.md) · [Arquétipos](docs/ARQUETIPOS-1.7.1.md) · [Visual 1.7.1](docs/VISUAL-1.7.1.md) · [Diário 1.7.1](docs/DESENVOLVIMENTO-1.7.1.md)

---

# ASCEND 1.7.0.1 — Validação

18/11/2026. Base `9365b39` (1.7.0) na branch `feat/ascend-1.7.0.1`.

- `./gradlew testDebugUnitTest lintDebug assembleDebug --offline` concluiu com sucesso. Foram aprovados **155 testes**, incluindo os **143 anteriores e 12 novos**, com zero falhas, erros ou ignorados. Android Lint registrou **0 erros e 22 avisos**.
- Os três primeiros testes de regressão falharam na base e passaram após a correção. Os testes de UI renderizaram foco de hábito, projeto e tarefa, incluindo pausa, encerramento, retorno ao app e recriação da Activity.
- O APK tem **25529982 bytes**, versão 1.7.0.1, código 21, pacote `br.com.ascend.aetheris` e minSdk 26 (Android 8.0). O alinhamento e a assinatura passaram. O certificado é idêntico ao da 1.7.0 e as permissões são as mesmas, sem INTERNET.
- SHA-256 do APK: `ddbbc50a3d3cb27a37ff0d6cb1df11f798111274be5da2eae7cdac98cad83548`.
- Room v7 e backup v9 preservados. A suíte inclui migrações, leitura de backups antigos e rollback de importação e triagem.
- Capturas em Robolectric. A instalação sobre a 1.7.0 foi conferida por pacote, certificado e incremento do código. O cenário de atualização em aparelho físico segue sem verificação.

[APK](downloads/ASCEND-1.7.0.1.apk?raw=true) · [Build](docs/validation/BUILD-1.7.0.1.txt) · [Lint](docs/validation/LINT-1.7.0.1.txt) · [Certificado e manifesto](docs/validation/APK-1.7.0.1.txt) · [Resultados JUnit](docs/validation/v1.7.0.1/) · [Auditoria](docs/AUDITORIA-1.7.0.1.md) · [Capturas](docs/VISUAL-1.7.0.1.md)

---

# ASCEND 1.7.0 — validação

18/11/2026. Base `feat/ascend-1.6.9.19` (`448e7cd`). Branch `feat/ascend-1.7.0`. GitHub Actions execução `44320889380` (commits `475da08` / `2d3ac9d`): `testDebugUnitTest assembleDebug lintDebug`: BUILD SUCCESSFUL (`56` tarefas executadas). **143 testes aprovados** (`135` preservados + `8` novos em `PathsAndAgendaTest` e `PathsAndAgendaUiTest`), zero falhas, erros ou ignorados; `lintDebug` com **0 erros e 23 avisos** (`22` localmente).

APK publicado em [downloads/ASCEND-1.7.0.apk](downloads/ASCEND-1.7.0.apk?raw=true) com `24100362` bytes, `versionName 1.7.0` / `versionCode 20`, SHA-256 `0b8956ecc729590d1b4be79144dc3c4d1db5ab290ae64effc8e5c6226d7efe20`; `zipalign -c 4` aprovado; assinatura v2 válida com certificado oficial preservado `e1f003efe40d53754db66325c40177178aaa81d4ad02ab910c85642c681caae4`; sem `INTERNET`. Room v7 (`MIGRATION_6_7` validada sobre esquema real `v6` em SQLite), backup v9 (compatibilidade v8 → v9, round-trip e rollback atômico validados em teste) e Biblioteca da Forja (`1.6.9.19`) integralmente preservados.

Renderizações reais Robolectric de 36 telas/variantes em `docs/screenshots/v1.7.0`, incluindo as 11 capturas novas (`tour-jornada-rumos-170.png`, `tour-caminhos-170.png`, `tour-caminho-detalhe-170.png`, `tour-agenda-dia-170.png`, `tour-agenda-semana-170.png`, `tour-agenda-mes-170.png`, `tour-entrada-170.png`, `tour-pendencias-170.png`, `caminhos-320-170.png`, `agenda-320-170.png`, `pendencias-320-170.png` em `320 dp` / fonte `130%` / tema claro / minimalista). Sem teste físico de instalação em aparelho, launcher físico ou TalkBack. Documentação em [docs/CAMINHOS-1.7.0.md](docs/CAMINHOS-1.7.0.md), [docs/AGENDA-1.7.0.md](docs/AGENDA-1.7.0.md) e [docs/VISUAL-1.7.0.md](docs/VISUAL-1.7.0.md). Logs em [docs/validation/BUILD-1.7.0.txt](docs/validation/BUILD-1.7.0.txt), [docs/validation/LINT-1.7.0.txt](docs/validation/LINT-1.7.0.txt) e [docs/validation/APK-1.7.0.txt](docs/validation/APK-1.7.0.txt).

# ASCEND 1.6.9.19 — validação

18/11/2026. Base `feat/ascend-1.6.9.15` (`e85e7bc`). Branch `feat/ascend-1.6.9.19`. GitHub Actions execução `44256122496` (commit `5e92407` / `69de462`): `testDebugUnitTest assembleDebug lintDebug`: BUILD SUCCESSFUL (`56` tarefas executadas). **135 testes aprovados**, zero falhas, erros ou ignorados; `lintDebug` com **0 erros e 23 avisos**.

APK publicado em [downloads/ASCEND-1.6.9.19.apk](downloads/ASCEND-1.6.9.19.apk?raw=true) com `23756298` bytes, `versionName 1.6.9.19` / `versionCode 19`, SHA-256 `918a78246d54a906114e07e59c11fdfb5e1dcbcadfb165b9a8a422d8f58dafcc`; `zipalign -c 4` aprovado; assinatura v2 válida com certificado oficial preservado `e1f003efe40d53754db66325c40177178aaa81d4ad02ab910c85642c681caae4`; sem `INTERNET`. Room v6, backup v8, repositórios e todos os recursos gráficos anteriores preservados.

Biblioteca pictórica original com **20 novos recursos WebP** (`1` prancha conceitual em `docs/art/concepts/biblioteca-forja-16919.webp` e `19` recursos de produção em `app/src/main/res/drawable-nodpi/` somando `855.218` bytes) gerados via `image-generator` antes da integração em código. Renderizações reais Robolectric de 25 telas/variantes em `docs/screenshots/v1.6.9.19`, incluindo a prancha da biblioteca (`prancha-biblioteca-16919.png`), widgets claro/escuro/compacto/expandido e cenários `320 dp` / fonte `130%` (`jornada-320-texto130.png`, `inventario-320-texto130.png`, `saga-320-texto130.png`, `planejador-320-160.png`, `widget-estreito-130-151.png`). Sem teste físico de instalação, launcher ou TalkBack. Catálogo técnico, prancha, comparações `1.6.9.15 × 1.6.9.19` e limites em [docs/VISUAL-1.6.9.19.md](docs/VISUAL-1.6.9.19.md) e [docs/art/README.md](docs/art/README.md). Logs em [docs/validation/BUILD-1.6.9.19.txt](docs/validation/BUILD-1.6.9.19.txt), [docs/validation/LINT-1.6.9.19.txt](docs/validation/LINT-1.6.9.19.txt) e [docs/validation/APK-1.6.9.19.txt](docs/validation/APK-1.6.9.19.txt).

# ASCEND 1.6.9.15 — validação

18/11/2026. Base `feat/ascend-1.6.9.10` (`b13904c`). Branch `feat/ascend-1.6.9.15`. GitHub Actions execução `44240234101` (commit `954bbd0` / `e85e7bc`): `testDebugUnitTest assembleDebug lintDebug`: BUILD SUCCESSFUL (`52` tarefas executadas). **135 testes aprovados**, zero falhas, erros ou ignorados; `lintDebug` com **0 erros e 22 avisos**.

APK publicado em [downloads/ASCEND-1.6.9.15.apk](downloads/ASCEND-1.6.9.15.apk?raw=true) com `22914314` bytes, `versionName 1.6.9.15` / `versionCode 18`, SHA-256 `43c70b08a1a5f970b34e25f7bcfef900236997e824526c673cf73ae84d609653`; `zipalign -c 4` aprovado; assinatura v2 válida com certificado oficial preservado `e1f003efe40d53754db66325c40177178aaa81d4ad02ab910c85642c681caae4`; sem `INTERNET`. Room v6, backup v8 e repositórios preservados.

Renderizações reais Robolectric de 24 telas/variantes em `docs/screenshots/v1.6.9.15`, incluindo a prancha de componentes compartilhados (`prancha-componentes-16915.png`), widgets claro/escuro/compacto/expandido e cenários `320 dp` / fonte `130%` (`jornada-320-texto130.png`, `inventario-320-texto130.png`, `saga-320-texto130.png`, `planejador-320-160.png`, `widget-estreito-130-151.png`). Sem teste físico de instalação, launcher ou TalkBack. Prancha, comparações `1.6.9.10 × 1.6.9.15` e limites em [docs/VISUAL-1.6.9.15.md](docs/VISUAL-1.6.9.15.md). Logs em [docs/validation/BUILD-1.6.9.15.txt](docs/validation/BUILD-1.6.9.15.txt), [docs/validation/LINT-1.6.9.15.txt](docs/validation/LINT-1.6.9.15.txt) e [docs/validation/APK-1.6.9.15.txt](docs/validation/APK-1.6.9.15.txt).

# ASCEND 1.6.9.10 — validação

18/11/2026. Base `feat/ascend-1.6.9` (`bd58ab3`). Branch `feat/ascend-1.6.9.10`. GitHub Actions execução `44224838259` (commit `36653d0` / `f95dfec`): `testDebugUnitTest assembleDebug lintDebug`: BUILD SUCCESSFUL (`56` tarefas executadas). **135 testes aprovados**, zero falhas, erros ou ignorados; `lintDebug` com **0 erros e 22 avisos**.

APK publicado em [downloads/ASCEND-1.6.9.10.apk](downloads/ASCEND-1.6.9.10.apk?raw=true) com `22897534` bytes, `versionName 1.6.9.10` / `versionCode 17`, SHA-256 `e471e72899d722b32d9d7dc44e4b74707ed785c4ffa2eace4559585e69df0f84`; `zipalign -c 4` aprovado; assinatura v2 válida com certificado oficial preservado `e1f003efe40d53754db66325c40177178aaa81d4ad02ab910c85642c681caae4`; sem `INTERNET`. Room v6, backup v8 e repositórios preservados.

Renderizações reais Robolectric de 21 telas/variantes em `docs/screenshots/v1.6.9.10`, incluindo cenários `320 dp` / fonte `130%` (`jornada-320-texto130.png`, `inventario-320-texto130.png`, `saga-320-texto130.png`, `planejador-320-160.png`, `widget-estreito-130-151.png`). Sem teste físico de instalação, launcher ou TalkBack. Comparações `1.6.9 × 1.6.9.10` e limites em [docs/VISUAL-1.6.9.10.md](docs/VISUAL-1.6.9.10.md). Logs em [docs/validation/BUILD-1.6.9.10.txt](docs/validation/BUILD-1.6.9.10.txt), [docs/validation/LINT-1.6.9.10.txt](docs/validation/LINT-1.6.9.10.txt) e [docs/validation/APK-1.6.9.10.txt](docs/validation/APK-1.6.9.10.txt).

# ASCEND 1.6.9 — validação

17–18/11/2026. Base `feat/ascend-1.6.8` (`b2252cb`). GitHub Actions execução `44214011907` (`feat/ascend-1.6.9`): `testDebugUnitTest assembleDebug lintDebug`: BUILD SUCCESSFUL (`56` tarefas executadas). **135 testes aprovados**, zero falhas, erros ou ignorados; `lintDebug` com **0 erros e 22 avisos**.

APK publicado em [downloads/ASCEND-1.6.9.apk](downloads/ASCEND-1.6.9.apk?raw=true) com `22847850` bytes, `versionName 1.6.9` / `versionCode 16`, SHA-256 `bd627804224b60679da3206f4fa5a40c70cb59b25178962fb986d789f4e2b991`; `zipalign` aprovado; assinatura v2 válida com certificado oficial preservado `e1f003efe40d53754db66325c40177178aaa81d4ad02ab910c85642c681caae4`; sem `INTERNET`. Room v6, backup v8 e repositórios preservados.

Renderizações reais Robolectric de 22 telas/variantes em `docs/screenshots/v1.6.9`, conceito visual `docs/art/concepts/entalhe-169.webp` e 6 folhas/superfícies WebP originais em `docs/art/README.md`. Sem teste físico de instalação, launcher ou TalkBack. Escopo e limites em `docs/VISUAL-1.6.9.md`. Logs em `docs/validation/BUILD-1.6.9.txt`, `docs/validation/LINT-1.6.9.txt` e `docs/validation/APK-1.6.9.txt`.

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
