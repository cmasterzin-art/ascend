# Validação — ASCEND 1.3.1

Data: 16/11/2026. Base: `6e6aa3cb6192773c6ae8b2d69fc459070aeed978` (1.3).

## Resultado

- `testDebugUnitTest assembleDebug`: **BUILD SUCCESSFUL**.
- **59 testes aprovados**, zero falhas, erros ou testes ignorados.
- JDK Temurin 17, SDK Android 35, Gradle 8.9.
- APK: `downloads/ASCEND-1.3.1.apk`, 23,034,449 bytes.
- SHA-256: `1644a02e5fde657c044a0ffabbe34beb2a8d3a1afcde37c75732f33a6fde7bf8`.
- Assinatura v2 válida, RSA 2048 bits, mesmo certificado dos APKs 1.1/1.2/1.3 fornecidos. Chave preservada, fora do Git.
- `zipalign -c 4`: aprovado. `git diff --check`: aprovado.
- Manifesto: `br.com.ascend.aetheris`, versionCode **5**, versionName **1.3.1**, minSdk 26, targetSdk 35. **Sem permissão INTERNET**.

## Cobertura

| Suíte | Testes | Verificação |
|---|---:|---|
| CatalogTest | 5 | 102 modelos íntegros e IDs únicos, instanciação independente, busca normalizada, seis filtros/quatro ordenações, sugestões/dispensa e seis rotinas |
| CatalogRepositoryTest | 6 | Personalização e edição com snapshot, reload/backup, oito tentativas concorrentes de duplicação, reutilização de hábitos manuais, rotina atômica/idempotente, migração real de v2 e sequência opcional |
| GrimoireUiTest | 4 | Busca narrativa → editar XP/meta/frequência → salvar → concluir → editar preservando XP; interesses/dispensa; rotina parcial com personalização/lembrete em 320 dp; três cenas de biblioteca ligadas ao estado salvo |
| ProgressionTest | 6 | Níveis, proporcionalidade, calendários, missões e moedas |
| RepositoryTest | 12 | Snapshots, reversão, compras concorrentes, foco, missões, backup e persistência |
| InsightsTest | 5 | Descansos, sequências, taxa, arquivo e virada de dia |
| Version11Test | 5 | Descansos, backups antigos, migração real 1 → 2 → 3 e programação histórica |
| RemindersTest | 4 | Descanso, retomada, horário de verão e lembretes desativados |
| OnboardingTest | 7 | Criação, edição, temas, telas RPG, equipamentos, vila/chefe/crônicas e tela estreita |
| RpgTest | 5 | Habilidades, exclusividade de equipamento, correção de XP, reconstrução e backup v2 |

O fluxo principal cria um hábito com XP **73** e meta **15 minutos**, conclui e depois altera XP para **95**. O registro mantém **73**, e a configuração editada persiste. O teste de rotina desmarca dois dos quatro hábitos, personaliza XP para **44**, ativa um lembrete e confere dois hábitos salvos, uma rotina e **zero XP por adição**. A revisão inclui cartões completos em 320 dp com fonte a 130%.

## Compatibilidade e dados

A Room v3 acrescenta somente `templateId`, `narrativeName` e `trackStreak` por `ALTER TABLE`, com defaults explícitos. Não recria tabelas nem usa fallback destrutivo. Os testes constroem os esquemas antigos exportados, inserem dados e executam as migrações reais. O teste v2 compara todo o GameState antes/depois: personagem, hábitos, registros, XP, moedas, inventário, descanso, diário, rotina, missões e reconstrução. O teste v1 atravessa ambas as migrações.

Backup v4 aceita v1–v4. Um backup v3 sem os novos campos restaura nome funcional, origem vazia e sequências ativadas. Catálogo e rotinas copiam valores; alterações no catálogo não reescrevem os hábitos criados. Não houve alteração da fórmula de XP ou de recompensas da economia.

Certificado SHA-256: `e1f003efe40d53754db66325c40177178aaa81d4ad02ab910c85642c681caae4`. Package e certificado iguais com versionCode maior permitem atualizar os APKs 1.1–1.3 fornecidos sem desinstalar. A antiga 1.0 usa outro certificado; veja [instalação](INSTALACAO.md).

## Evidências e limites

[JUnit e verificação do APK](docs/validation/) · [Capturas e revisão visual](docs/VISUAL-1.3.1.md) · [Catálogo e rotinas](docs/GRIMORIO.md).

Robolectric API 28 com renderização nativa cobre 411 × 891 dp e 320 × 740 dp, fonte a 130%, redução de movimento e tema claro. Não houve instalação em aparelho físico ou emulador completo, nem teste de atualização sobre um telefone real, Android Lint ou benchmark físico. Permissão de notificação Android 13+, entrega de alarmes pelo fabricante e seletor de arquivos ainda dependem de verificação em aparelho. Lembretes são locais e aproximados.

Trata-se de APK pessoal de depuração assinado. Dez pinturas WebP (seis anteriores + quatro atlas novos), fonte e catálogo estão incorporados; não há backend, download durante o uso ou serviço pago.
