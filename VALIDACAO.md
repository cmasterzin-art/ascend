# Validação — ASCEND 1.5.1

16/11/2026. Base 1.5.0: `b539db00a355273e016c911d8a89b7c1e82dda0d`. Branch `feat/ascend-1.5.1`.

- `testDebugUnitTest assembleDebug`: **BUILD SUCCESSFUL**, **97 testes aprovados**, zero falhas, erros ou ignorados. Log e XMLs em [docs/validation/](docs/validation/).
- APK: [downloads/ASCEND-1.5.1.apk](downloads/ASCEND-1.5.1.apk?raw=true), **23.189.873 bytes** (22,12 MiB).
- SHA-256: `f8f5e6edfbaa89a74dc129f7e1a069bf9185441430a7688ffd65b338cfd564c5`.
- Assinatura v2 válida; certificado comparado diretamente com o APK 1.5.0: `e1f003efe40d53754db66325c40177178aaa81d4ad02ab910c85642c681caae4`.
- `zipalign -c 4`: aprovado. Identificador `br.com.ascend.aetheris`, versionName **1.5.1**, versionCode **9**, minSdk 26/targetSdk 35.
- Manifesto sem INTERNET, VIBRATE, serviço contínuo ou permissão de alarme exato. POST_NOTIFICATIONS e RECEIVE_BOOT_COMPLETED permanecem.
- JDK Temurin 17, SDK Android 35, Gradle 8.9; APK pessoal de depuração assinado com a chave preservada fora do Git. O comando do wrapper foi executado pelo ambiente de build configurado, sem alterar dependências do projeto.

## Regressão e testes adicionais

Os **90 testes da 1.5.0** permanecem e passaram. Nenhum teste foi removido. A única alteração de expectativa anterior acompanha a concordância corrigida no widget: “1 pendentes” → “1 pendente”.

| Suíte adicional | Testes | Resultado verificado |
|---|---:|---|
| PolishTest | 2 | Datas brasileiras, dia bissexto, fallback para texto inválido e independência do locale/fuso do aparelho; capacidade do widget conforme altura, largura, fonte e modo compacto |
| PolishUiTest | 3 | Atalhos de widget entregues à Activity abrem Jornada/registro, retiram extras consumidos e preservam retorno ao Herói sem criar registros; formulário real salva dia desmarcado em 320 dp/130%, com alvo medido de pelo menos 48 × 48 dp; Crônica e Memórias exibem datas formatadas, filtros e estado vazio |
| WidgetPolishTest | 2 | RemoteViews em 220 × 110 dp/fonte 130% sem personagem ou rituais, sem conteúdo excedendo a altura; tema noturno em 220 × 320 dp/130%, nome longo com descrição acessível completa, conclusão de hábito de 17 XP refletida em 50% e próximo botão Registrar |

A regressão conserva os fluxos Atlas → Região → Local → Expedição, seleção de hábitos, início, marcos, resgate, três fases do Guardião, retorno Android, transformações e Memórias. Continua exercitando 320 dp/130%/claro/minimalista/redução de movimento, os widgets anteriores, PendingIntents, host simulado, correções e idempotência.

Também passaram catálogo/rotinas, criação/edição de hábitos, XP personalizado, persistência, missões, Sagas, inventário, conquistas, reconstrução, Crônicas, sons, descanso, lembretes e migrações/backups anteriores. Capturas são produzidas pelos testes reais, sem progresso fictício incluído no app.

## Dados, compatibilidade e funcionamento offline

**Room v5 e backup v7 permanecem sem alteração.** Não foi criado esquema, tabela ou migração nova. `applicationId`, regras de exploração, snapshots, ledger, XP, moedas e recompensas foram preservados. O diff não modifica repositório, modelos persistidos ou arquivos de esquema. Os testes anteriores de migração explícita 1 → 2 → 3 → 4 → 5, abertura do esquema v5, persistência e leitura de backups v1–v7 continuam aprovados. Sem fallback destrutivo.

As datas são transformadas apenas para apresentação; armazenamento, importação/exportação, programação e chaves continuam ISO. Não há conversão de fuso sobre datas civis salvas. Não foi acrescentado serviço remoto, backend ou dependência de conexão; recursos e fontes continuam embutidos no APK.

Pacote e certificado iguais, minSdk compatível e versionCode maior conferem os requisitos de atualização sobre o APK 1.5.0 fornecido. A instalação sobre um telefone físico **não foi executada**. Os APKs 1.1–1.4.5 fornecidos também usam esse certificado; a primeira 1.0 usa outra assinatura. Instale sem desinstalar e exporte um backup conforme [INSTALACAO.md](INSTALACAO.md).

## Revisão visual e limites

[Comparações reais 1.5.0 → 1.5.1](docs/VISUAL-1.5.1.md) · [Exploração preservada](docs/EXPLORACAO-1.5.md) · [Detalhes do APK](docs/validation/APK-1.5.1.txt).

Capturas nativas Robolectric API 28, 411 × 891 e 320 × 740 dp. Os cenários incluem claro/escuro, fonte 130%, redução de movimento, vazio e telas reais. Widgets são RemoteViews reais desenhadas em ambiente de teste; o host é simulado. Capturas anteriores foram congeladas em `docs/screenshots/v1.5/` para comparação.

Não houve instalação em telefone/launcher físico ou emulador completo, benchmark, Android Lint, audição em aparelho, rotação física, nem teste físico de reboot, bateria, meia-noite/fuso e insets de fabricante. O teste novo de intenções verifica consumo e navegação; não é um teste de recriação física da Activity. Atualizações de widget podem atrasar sob restrições do Android; ações continuam revalidando data/programação e rejeitando conclusão direta de hábitos complexos.

Sons, retratos e pinturas foram preservados. Não há novas fases, regiões ou sistemas, nem redesenho integral da grade do inventário. O polimento se concentra em composição, leitura, controles e estados; detalhes remanescentes estão na revisão visual.
