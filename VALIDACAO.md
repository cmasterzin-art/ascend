# Validação — ASCEND 1.4.0

16/11/2026. Base 1.3.1: `5dc9d7083bfbf111bff86b9612abdc605d04c05d`.

- `testDebugUnitTest assembleDebug`: **BUILD SUCCESSFUL**, **67 testes aprovados**, zero falhas, erros ou ignorados.
- APK: `downloads/ASCEND-1.4.0.apk`, 22,362,121 bytes.
- SHA-256: `26c187f35f5e69b68ff0c8e12f67107dc11807648f39f8df91856ab70b8114ad`.
- Assinatura v2 válida, mesmo certificado desde a 1.1: `e1f003efe40d53754db66325c40177178aaa81d4ad02ab910c85642c681caae4`.
- `zipalign -c 4`: aprovado. Manifesto: `br.com.ascend.aetheris`, versionName 1.4.0, versionCode 6, minSdk 26/targetSdk 35, **sem INTERNET**.
- JDK Temurin 17, SDK Android 35, Gradle 8.9; compilação pessoal de depuração assinada. Chave privada preservada, fora do Git.

## Cobertura nova e regressão

As 59 verificações anteriores foram preservadas. Adaptações nos testes de interface confirmam o novo painel de recompensa, e a leitura agora verifica o primeiro parágrafo em vez de um único bloco de capítulo. As migrações antigas atravessam também 3 → 4.

| Suíte | Testes | Escopo |
|---|---:|---|
| SagaTest | 6 | Baseline do dia atual, marcos 25/50/75/100, seis resgates concorrentes com um sucesso, correções/zero/restauração do progresso, minutos/páginas/conclusões, edição e termos fixos, reload, backup v5, backup v4 sem novos campos, migração real v3 e diálogos/recompensas consolidadas |
| NorthUiTest | 2 | Criação pelo formulário, seleção real de hábito, resgate com nível no mesmo painel, diálogo contextual e saga em 320 dp/fonte 130%/redução de movimento |
| CatalogTest / CatalogRepositoryTest / GrimoireUiTest | 15 | 102 modelos, seis rotinas, filtros, busca, duplicatas concorrentes, personalização e snapshots, persistência e criação/edição pela interface |
| ProgressionTest / RepositoryTest / InsightsTest | 23 | XP, moedas, atributos, missões, snapshot, reversões, foco, estatísticas e descanso |
| Version11Test / RemindersTest / RpgTest / OnboardingTest | 21 | Migrações 1 → 4 e 2 → 4, backups antigos, notificações, inventário, árvore, reconstrução, temas, navegação e telas RPG |

Recompensa consolidada validada com deltas reais: 150 XP, nível 2 e atributos; ação repetida sem mudança não gera novos destaques. No fluxo de saga da interface, um único resgate concede 150 XP e 20 Fragmentos (10 configurados + 10 do nível), exibidos juntos. Corrigir um registro reduz a recompensa da saga no mesmo ledger; recuperar o progresso revalida o resgate existente, sem novo Claim.

Room v4: somente quatro colunas novas no total (`sagaStart` e `sagaBaseline` em `quests` e `claims`), com defaults vazios. Migrações explícitas 1 → 2 → 3 → 4 e esquemas exportados, sem fallback destrutivo. Backup v5 aceita v1–v5; preserva sagas, snapshots e memórias narrativas. Nenhuma fórmula histórica de XP foi alterada.

## Evidências e limites

[Relatórios JUnit e verificação de assinatura](docs/validation/) · [Comparação e capturas reais](docs/VISUAL-1.4.md) · [Regras das sagas](SAGAS.md).

Robolectric API 28 com renderização nativa: 411 × 891 dp, 320 × 740 dp, fonte a 130%, tema claro, modo minimalista e redução de movimento. Não houve instalação em aparelho físico ou emulador completo, teste de atualização sobre telefone real, Android Lint ou benchmark físico. Permissão de notificações Android 13+, seletor de arquivos e entrega de alarmes pelo fabricante permanecem sujeitos à verificação em aparelho.

O package e certificado iguais, com versionCode maior, permitem atualizar os APKs 1.1–1.3.1 fornecidos sem desinstalar. A antiga 1.0 tem outra assinatura. Confira [INSTALACAO.md](INSTALACAO.md) e exporte um backup antes de atualizar.

Sem sons/vibração nesta entrega. As imagens, fonte, modelos e textos são locais; o APK não exige conta, backend ou serviço pago.
