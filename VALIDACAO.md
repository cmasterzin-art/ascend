# Validação — ASCEND 1.4.5

16/11/2026. Base 1.4: `2e043984e1b2553dbf5b5d8345e85aa5c7820109`.

- `testDebugUnitTest assembleDebug`: **BUILD SUCCESSFUL**, **78 testes aprovados**, zero falhas, erros ou ignorados.
- APK: `downloads/ASCEND-1.4.5.apk`, 22,568,922 bytes.
- SHA-256: `f1a86f6d14aabe7250641f46aa4e9d1663c5078023eb2a7a70b25e7390b545bf`.
- Assinatura v2 válida; certificado preservado desde a 1.1: `e1f003efe40d53754db66325c40177178aaa81d4ad02ab910c85642c681caae4`.
- `zipalign -c 4` e `git diff --check`: aprovados.
- Manifesto: `br.com.ascend.aetheris`, versionName 1.4.5, versionCode 7, minSdk 26/targetSdk 35. **Sem INTERNET ou VIBRATE**.
- JDK Temurin 17, SDK Android 35, Gradle 8.9. APK pessoal de depuração assinado; chave privada fora do Git.

## Cobertura

Os 67 testes da 1.4 continuam passando. Nos testes de interface, foi removida a tentativa de rolar os menus fixos do Herói: as opções agora ficam inteiramente visíveis. Não foram removidas verificações funcionais. Builders de bancos antigos atravessam também a migração 4 → 5.

| Suíte nova | Testes | Verificação |
|---|---:|---|
| VoicesTest | 7 | Semana civil, percentual e correções; dias de descanso e avanço de saga por período limitado à meta (sem avanço fictício após a conclusão); abandono congelado sem recompensa, arquivo e reinício com nova identidade/linha de base; resgate único após arquivo; conversas persistidas como texto; memórias sem datas inventadas; migração real v4; backup v5 sem novos campos; política sonora e preferências locais |
| VoicesUiTest | 4 | Cinco áreas e retorno Android; menus de Missões inteiros; Crônica e Salão; retrato ampliado e retorno ao Inventário; calendário Android real; abandono/arquivo/reinício; 320 dp/fonte 130%/tema claro/redução de movimento; Crônica com feitos e histórico de conversas |

A regressão de 67 testes cobre Grimório/rotinas, XP/snapshots, correções, missões, Sagas, inventário, árvore, reconstrução, estatísticas, descanso, lembretes, migrações 1/2/3 e backups antigos. A suíte completa exercita 411 × 891 dp e 320 × 740 dp, texto 130%, temas claro/escuro, modo minimalista e redução de movimento. Capturas são da renderização nativa Robolectric API 28, com fixtures de teste que não são incluídas no aplicativo.

## Dados e regras

Room v5: migração explícita 4 → 5 adiciona `sagaState`, `sagaEnd`, `sagaStopped` em `quests` e `memorySpeaker`, `memoryText` em `assets`, com defaults. Mantém 1 → 2 → 3 → 4; sem fallback destrutivo. Backup v6 lê v1–v6 e preenche campos ausentes. Estados antigos de saga começam ativos; resgates antigos continuam reconhecidos no histórico.

A Crônica Semanal é derivada, sem cópias persistidas dos totais. O Salão consulta histórico, ledger, resgates, conquistas e assets. Nenhuma das duas telas concede XP. Sagas abandonadas guardam uma fotografia do progresso; nova tentativa possui UUID e baseline próprios. Recompensas continuam usando o ledger existente e resgate único, sem alterações na economia.

## Evidências e limites

[Comparação visual e capturas](docs/VISUAL-1.4.5.md) · [JUnit e certificado](docs/validation/) · [Regras das Sagas](SAGAS.md) · [Áudio original e limites](docs/art/SOUNDS.md).

Não houve instalação ou audição em telefone físico, emulador completo, Android Lint ou benchmark físico. A lógica de áudio e preferências foi testada; volume percebido, DND, restrições de fabricante, permissões Android 13+ e insets físicos de gestos/três botões ainda dependem de verificação no aparelho. O código usa sonificação, modo normal, volume de mídia e navigationBarsPadding.

As artes, textos, modelos e sons são locais. Sem conta, backend, serviço pago, música contínua ou vibração. Preferências de som/aparência ficam no aparelho e não acompanham o backup de progresso. Conversas anteriores sem texto só podem ser reconstruídas quando a condição histórica ainda está disponível; datas desconhecidas não são fabricadas.

O package/certificado iguais e versionCode maior permitem atualizar os APKs 1.1–1.4 fornecidos sem desinstalar; atualização sobre uma instalação física não foi executada. A 1.0 usa outra assinatura. Exporte um backup antes de atualizar, conforme [INSTALACAO.md](INSTALACAO.md).
