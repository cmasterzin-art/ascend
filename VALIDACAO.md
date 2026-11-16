# Validação — ASCEND 1.3.0

Data: 16/11/2026. Base: `514bbce72352527ed1b13e6756bd1ba3c9058490` (1.2).

## Resultado

- `testDebugUnitTest assembleDebug`: **BUILD SUCCESSFUL**.
- **44 testes aprovados, zero falhas, erros ou testes ignorados**.
- JDK Temurin 17, SDK Android 35 e Gradle 8.9.
- APK: `downloads/ASCEND-1.3.0.apk`, 20,233,530 bytes.
- SHA-256: `0832811d3c04b5773ef85f9e7cea0f32a08143cd179df1fc3b8605bb0074d790`.
- Assinatura v2 válida conferida por `apksigner verify --verbose --print-certs`; um assinante RSA de 2048 bits, mesmo certificado das versões 1.1/1.2 fornecidas.
- `zipalign -c 4`: aprovado.
- Manifesto: `br.com.ascend.aetheris`, versionCode 4, versionName 1.3.0, minSdk 26, targetSdk 35; **sem permissão INTERNET**.
- `git diff --check`: aprovado. Repositório de dados, Domain e esquemas Room não foram alterados.

## Cobertura

| Suíte | Testes | Verificação |
|---|---:|---|
| ProgressionTest | 6 | Níveis, proporcionalidade, calendários, missões e moedas |
| RepositoryTest | 12 | Snapshots, reversão, compras concorrentes, foco, missões, backup e persistência |
| InsightsTest | 5 | Descansos, sequências, taxa, arquivo e virada de dia |
| Version11Test | 5 | Descansos, backups antigos, migração 1 → 2 e programação histórica |
| RemindersTest | 4 | Descanso, retomada, horário de verão e lembretes desativados |
| OnboardingTest | 7 | Fluxos de interface, temas, telas RPG, equipamentos, vila/chefe/crônicas e tela estreita |
| RpgTest | 5 | Habilidades, exclusividade de equipamento, correção de XP, reconstrução e backup v2 |

O novo fluxo visual usa personagem Eira, equipa oito itens, restaura a biblioteca, visita perfil/arsenal/loja/mapa/vila/crônicas/chefes, desativa um equipamento pela interface e confere o estado persistido. O segundo fluxo usa 320 × 740 dp com fonte em 130%, navega pelas abas fixas, ativa modo minimalista e conclui um hábito. Ambos usam redução de movimento.

Os cinco testes anteriores de interface continuam aprovados: criação de personagem, registro de hábitos, edição preservando XP histórico, desfazer, tema claro, descanso e inventário/habilidades.

Relatórios JUnit: [docs/validation/](docs/validation/). [Revisão visual com capturas e comparação 1.2 → 1.3](docs/VISUAL-1.3.md).

## Dados e instalação

Room permanece em v2 e o backup em v3, com leitura de v1/v2/v3. Não foram alteradas tabelas, identificadores de itens, regras de progresso ou comportamento do repositório. A migração 1 → 2 e a importação de backups antigos continuam nos testes.

Certificado SHA-256: `e1f003efe40d53754db66325c40177178aaa81d4ad02ab910c85642c681caae4`. A chave privada foi reutilizada, não regenerada, e continua fora do Git. O package, certificado e versionCode permitem atualizar os APKs 1.1/1.2 fornecidos sem desinstalação. A antiga 1.0 tem certificado diferente.

Não houve instalação em aparelho físico nem emulador completo; a atualização sobre uma instalação real não foi executada. Robolectric API 28 com renderização nativa cobriu 411 × 891 dp e 320 × 740 dp. Não foram executados Android Lint ou benchmark físico. Seletor de arquivos e permissões do fabricante permanecem sujeitos à verificação no telefone.

As seis pinturas WebP e a fonte estão incorporadas ao APK. Nenhum acesso remoto, backend, cobrança ou download de conteúdo é necessário durante o uso. Trata-se de compilação pessoal de depuração assinada.
