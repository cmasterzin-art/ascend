# Validação — ASCEND 1.2.0

Data: 16/11/2026. Base: `0d30503180f43500112ffac9ffc91617c6998b0c` (versão 1.1).

## Resultado

- `testDebugUnitTest assembleDebug`: **BUILD SUCCESSFUL**.
- **42 testes aprovados, zero falhas, erros ou testes ignorados**.
- JDK Temurin 17, SDK Android 35 e Gradle 8.9.
- APK distribuído: `downloads/ASCEND-1.2.0.apk`, 18,556,554 bytes.
- SHA-256: `37278902d029c9ed87b5e37238df746aeaef39432addf5fe8b2f1c593b027243`.
- `apksigner verify --verbose --print-certs`: assinatura v2 válida, um assinante RSA de 2048 bits. Certificado idêntico ao APK 1.1 fornecido.
- `zipalign -c 4`: aprovado.
- Manifesto: `br.com.ascend.aetheris`, versionCode 3, versionName 1.2.0, minSdk 26, targetSdk 35; **sem permissão INTERNET**.
- `git diff --check`: aprovado.

## Cobertura

| Suíte | Testes | Verificação |
|---|---:|---|
| ProgressionTest | 6 | Níveis, proporcionalidade, calendários, missões e moedas |
| RepositoryTest | 12 | Snapshots, reversão, compras concorrentes, foco, missões, backup e banco em disco |
| InsightsTest | 5 | Descansos, sequências, taxa, arquivo e virada de dia |
| Version11Test | 5 | Descansos, backup, leitura de v1, migração 1 → 2 e programação histórica |
| RemindersTest | 4 | Descanso, retomada, horário de verão e lembretes desativados |
| OnboardingTest | 5 | Personagem, hábitos/XP, edição/desfazer, tema, calendário e novas telas RPG |
| RpgTest | 5 | Custo/pré-requisito de habilidades, exclusividade de equipamentos, correção de XP, reconstrução e backup v2 |

O novo teste de interface cria personagem e hábito, registra progresso, compra um elmo, visita livro/mapa/inventário/habilidades, equipa o elmo pela interface e verifica a persistência. As capturas não incluem dados reais do usuário.

Os testes de habilidades verificam concessão e persistência da relíquia, rejeição de desbloqueio duplicado, rejeição de compra direta de relíquias exclusivas, rejeição de preço adulterado e compensação de pontos após desfazer XP. O teste de backup v2 preserva hábitos, recompensa, descanso e traje antigo, depois libera uma habilidade. A restauração da biblioteca é reaberta por outro repositório e muda o diálogo e o capítulo disponível.

JUnit: [docs/validation/](docs/validation/). Capturas inspecionadas:

- [Livro de missões](docs/screenshots/livro-missoes.png)
- [Mapa](docs/screenshots/mapa-aetheris.png)
- [Inventário](docs/screenshots/inventario.png)
- [Habilidades](docs/screenshots/habilidades.png)
- [Jornada escura](docs/screenshots/jornada-escuro.png) e [clara](docs/screenshots/jornada-claro.png)

## Compatibilidade e alcance

Room permanece em v2. Não houve alteração de esquema nesta versão; a migração 1 → 2 continua testada. Backups v1, v2 e v3 são aceitos, com validação antes da substituição transacional.

O certificado SHA-256 conferido nos APKs 1.1 e 1.2 é `e1f003efe40d53754db66325c40177178aaa81d4ad02ab910c85642c681caae4`. A chave privada não está incluída no Git ou nos artefatos. A assinatura da antiga 1.0 é diferente.

Interface executada em Robolectric API 28 com renderização nativa, em 411 × 891 dp. Não houve teste em aparelho físico nem emulador completo; instalação por cima da 1.1 não foi executada em um telefone. A compatibilidade foi verificada pelo pacote, versionCode, certificado e testes de dados. O seletor de arquivos e permissões do fabricante precisam ser conferidos no aparelho. Android Lint não foi executado.

O APK é uma compilação pessoal de depuração assinada. O uso é offline; baixar e preparar as ferramentas de desenvolvimento exige conexão. Consulte [PROGRESSO.md](PROGRESSO.md) para recursos ainda não implementados do documento de expansão.
