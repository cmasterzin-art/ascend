# Validação — ASCEND 1.1.0

Data: 16/11/2026. Base: `989eb19458c02773caca5db885807367ab21f286` (versão 1.0), com as alterações da versão 1.1 nesta branch.

## Resultado

- Tarefas Gradle **testDebugUnitTest** e **assembleDebug**: **BUILD SUCCESSFUL**.
- **36 testes aprovados, zero falhas e zero erros**.
- Ambiente: JDK Temurin 17 completo, SDK Android 35 e Gradle 8.9, mesma versão declarada no wrapper.
- APK gerado: `app/build/outputs/apk/debug/app-debug.apk`.
- Cópia para distribuição: `downloads/ASCEND-1.1.0.apk`, 18.460.249 bytes (aproximadamente 17,60 MiB).
- SHA-256: `528dd5c2669c1ac4fb030be9519a787985c70d36f9135e1c0125d040eff4486b`.
- Assinatura conferida com `apksigner verify --verbose --print-certs`: v2, um assinante RSA de 2048 bits.
- Alinhamento conferido com `zipalign -c 4`.
- Manifesto inspecionado: `br.com.ascend.aetheris`, versionCode 2, versionName 1.1.0, minSdk 26, targetSdk 35; **sem permissão INTERNET**.
- `git diff --check`: sem problemas de whitespace.

## Cobertura

| Suíte | Testes | Verificação |
|---|---:|---|
| ProgressionTest | 6 | Níveis, múltiplos avanços, proporcionalidade, calendários, missões e moedas |
| RepositoryTest | 12 | Snapshots, reversão, compras concorrentes, foco, missões, backup e banco em disco |
| InsightsTest | 5 | Descansos nas sequências, recorde histórico, taxa de cumprimento, arquivo e fuso na virada do dia |
| Version11Test | 5 | Persistência de descanso, backup v2, leitura de v1, migração 1 → 2 e programação após arquivar |
| RemindersTest | 4 | Descanso, retomada após semanas, horário de verão e lembretes desativados |
| OnboardingTest | 4 | Criação de personagem, hábitos/XP/edição/desfazer, tema claro e descanso pelo calendário |

O fluxo de interface cria Kael/Mago, cria Estudar com 50 XP e Ler com 30 XP, conclui ambos e confere 80 XP; muda a recompensa futura de Ler para 40 XP, confere que o registro antigo continua com 30 XP e desfaz a conclusão, voltando a 50 XP.

O teste original de personagem registra a meta de 60 minutos, confere 60 XP e os pontos de atributo, e visita Mundo, Vila, Missões, Personagem, Conquistas e Histórico. O teste de tema seleciona Claro pelas configurações e confere a preferência local. O teste de calendário ativa e remove descanso sem gerar XP.

Resultados JUnit completos estão em [docs/validation/](docs/validation/). Capturas da renderização nativa dos testes, inspecionadas visualmente:

- [Jornada escura](docs/screenshots/jornada-escuro.png)
- [Jornada clara](docs/screenshots/jornada-claro.png)

## Compatibilidade e assinatura

A migração é testada com um banco em disco marcado como v1, com a identidade Room original, personagem e recompensa gravados. A abertura na v2 preserva nome, histórico e XP e permite criar descansos. As tabelas v1 exportadas são idênticas às correspondentes v2; a única adição é `rest_days`.

O teste de backup v1 remove o campo novo, recalcula o checksum no envelope original e restaura os dados. Backups v2 incluem descansos. Importações inválidas são rejeitadas sem alterar o estado atual.

O certificado da versão 1.0 é diferente do APK 1.1 fornecido. A chave original não está neste ambiente, e o acesso a secrets do GitHub retornou HTTP 403. Nenhum secret foi modificado. A nova chave permanece fora do Git público. O procedimento de exportar/reinstalar/importar está em [INSTALACAO.md](INSTALACAO.md).

## Alcance

As verificações de interface usam Robolectric Android API 28, Compose UI Test e renderização nativa em viewport de 411 × 891 dp. Não houve teste em aparelho físico ou emulador com sistema operacional completo. Permissões de notificação e o seletor de documentos do fabricante precisam de verificação no telefone. Não foi executado Android Lint nesta sessão.

A compilação baixou ferramentas e dependências de desenvolvimento gratuitas. TLS permaneceu habilitado, usando os certificados do ambiente. O aplicativo instalado usa somente recursos locais e não depende desse ambiente de desenvolvimento.
