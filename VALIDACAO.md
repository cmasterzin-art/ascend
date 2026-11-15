# Validação executada — ASCEND 1.0.0

Data: 15/11/2026. Projeto: `/workspace/work/ascend`.

## Resultado

- `./gradlew testDebugUnitTest assembleDebug lintDebug`: **BUILD SUCCESSFUL**.
- **19 testes aprovados, zero falhas**: 6 de regras, 12 de persistência/transações/backup, 1 de interface Compose.
- Android Lint: **0 erros e 8 avisos**. Os avisos apontam versões mais recentes de bibliotecas; as versões usadas são estáveis e compatíveis.
- APK realmente criado: `app/build/outputs/apk/debug/app-debug.apk`.
- Tamanho: 18,385,502 bytes (17.53 MiB).
- SHA-256: `feb0f146b7f50482262484028b314bb39d80585032523e5753166817d475fb67`.
- Assinatura verificada com `apksigner verify --verbose`: APK Signature Scheme v2, um assinante.
- Alinhamento verificado com `zipalign -c 4`.
- Manifesto inspecionado com `aapt dump badging`: applicationId `br.com.ascend.aetheris`, versão 1.0.0, minSdk 26, targetSdk 35, atividade inicial `MainActivity`.
- ABIs do APK: arm64-v8a, armeabi-v7a, x86, x86_64.
- **Sem permissão INTERNET** no APK.

## O que foi exercitado

- Limiares de níveis e avanço por múltiplos níveis; recompensas proporcionais sem overflow.
- Sequências que ignoram dias não previstos, pausas e alterações de programação.
- Recorrência diária/semanal de missões baseada em registros reais.
- Registros repetidos sem pagamento duplo, conclusão parcial, edição de valores e reversão de XP/pontos/nível.
- Snapshot de recompensas após editar hábitos e snapshots de missões já resgatadas.
- Conclusão manual junto de cronômetro, persistência de foco pausado.
- Compras concorrentes, prevenção de duplicidade e de gastos sem saldo.
- Reconstrução, desbloqueio de capítulo, recarga dos dados e fechamento/reabertura de um banco Room em disco.
- Backup íntegro, corrupção rejeitada, importação atômica e importação inválida sem modificar dados.
- Interface em Android simulado por Robolectric API 28 com renderização nativa: criar Aria/Mago, usar hábito inicial Estudar Direito, registrar meta de 60 minutos, confirmar 60 XP e 5 pontos de Intelecto; visitar Mundo, Vila, Missões, Personagem, Conquistas e Histórico.
- Captura real da renderização do teste em `app/build/reports/screenshots/jornada.png`, inspecionada visualmente.

## Alcance da validação

Os testes usam JUnit, Room/SQLite com Robolectric e Compose UI Test no Cloud. Não foi usado um celular físico nem um emulador Android com sistema operacional completo. Instalação em aparelho real, permissões/notificações do fabricante e seleção de documentos precisam de verificação no telefone do usuário.

Relatórios completos: `app/build/reports/tests/testDebugUnitTest/` e `app/build/reports/lint-results-debug.html`.

## Entrega e ambiente

A compilação ocorreu integralmente no Cloud usando JDK Temurin 17, SDK Android 35 e Gradle Wrapper 8.9. O proxy e certificados do ambiente foram respeitados; TLS não foi desativado.

A pasta de saída especificada pelo chat não pode ser criada pelo usuário do ambiente: `Permission denied`. O APK está preservado no caminho de build acima. A entrega alternativa utiliza o repositório `cmasterzin-art/ascend` no GitHub. A conexão permite ler o repositório; o envio e a publicação precisam ser verificados antes de informar links de download. A configuração automática de secrets do Actions foi recusada pela API (HTTP 403), e nenhuma chave de assinatura foi publicada.
