# ASCEND — Crônicas da Ascensão

RPG Android de hábitos, inteiramente local. O aplicativo funciona sem internet; o GitHub serve apenas para distribuir o código e o instalador. Kotlin, Jetpack Compose e Material 3; Room com transações e relacionamentos; DataStore para preferências; MVVM, Navigation Compose, Coroutines e StateFlow.

- [Instalação e alternativa GitHub Actions](INSTALACAO.md)
- [Funcionalidades e limites](PROGRESSO.md)
- [Preservação de assinatura e atualizações](ASSINATURA.md)
- [Validação](VALIDACAO.md)

## Desenvolvimento na nuvem

JDK completo 17, SDK Android 35, Gradle Wrapper 8.9. O SDK pode ser configurado por `ANDROID_HOME` ou por `local.properties` (não versionado). Dependências de compilação são gratuitas e exigem acesso à internet somente durante a preparação do ambiente.

```sh
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`.

O fluxo central é: registro por hábito/dia → recompensa com chave determinística → ledger transacional → nível/atributos/moedas derivados. Recompensas de registros são snapshots, de modo que alterações de hábitos não reescrevem o passado. Reverter uma recompensa já gasta gera compensação futura, com saldo disponível limitado a zero.

Arquivos principais: `Domain.kt` (modelos/regras), `Database.kt` (Room), `Repository.kt` (transações e ViewModel), `Design.kt` (tema/arte vetorial), arquivos de telas e `Reminders.kt` (lembretes locais).
