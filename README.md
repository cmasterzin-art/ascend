# ASCEND — Crônicas da Ascensão · 1.4.5

[**Baixar APK Android 1.4.5**](downloads/ASCEND-1.4.5.apk?raw=true)

RPG de hábitos para Android 8.0 ou superior. **Tudo funciona localmente**, sem conta, internet, anúncios, assinatura ou serviços pagos. O GitHub distribui o código e o instalador; o aplicativo não tem permissão INTERNET.

A versão **1.4.5 — As Vozes de Aetheris** traz navegação própria em ferro, cinco ícones originais e menus internos com opções inteiras. As cinco áreas principais permanecem.

**Crônica da Semana** reúne estatísticas e narrativa locais, recalculadas após correções. **Salão das Memórias** reúne feitos e conversas guardadas, sem inventar datas. As Sagas agora podem ser abandonadas, arquivadas e reiniciadas com nova identidade e linha de base. Há calendário visual, retrato ampliado e sons originais opcionais, desligados inicialmente.

**Atualização dos APKs 1.1, 1.2, 1.3, 1.3.1 ou 1.4 fornecidos neste repositório:** exporte um backup de segurança e instale a 1.4.5 por cima, **sem desinstalar**. O certificado é o mesmo. A antiga 1.0 possui assinatura diferente; consulte [INSTALACAO.md](INSTALACAO.md).

- [Instalação e atualização](INSTALACAO.md)
- [Funcionalidades e limites reais](PROGRESSO.md)
- [Assinatura e preservação de atualizações](ASSINATURA.md)
- [Validação](VALIDACAO.md)

## Visual

| Jornada | Atlas | Arsenal |
|---|---|---|
| <img src="docs/screenshots/jornada-escuro.png" width="240" alt="Jornada nórdica com retrato e fiorde"> | <img src="docs/screenshots/mapa-aetheris.png" width="240" alt="Atlas geográfico de Aetheris"> | <img src="docs/screenshots/inventario-equipado.png" width="240" alt="Personagem com equipamentos em nichos"> |

**[Novas capturas e revisão da 1.4.5](docs/VISUAL-1.4.5.md)** · [Guia do Grimório e rotinas](docs/GRIMORIO.md) · [Comparação 1.2 → 1.3](docs/VISUAL-1.3.md) · [Missões](docs/screenshots/livro-missoes.png) · [Chefe](docs/screenshots/chefe.png) · [Crônicas](docs/screenshots/cronicas.png)

Capturas dos testes de interface com personagens de teste. Nenhuma conclusão fictícia é incluída no aplicativo.

## Desenvolvimento

Kotlin, Jetpack Compose, Material 3, Room, DataStore, MVVM, Navigation Compose, Coroutines e StateFlow. JDK 17 ou 21, SDK Android 35, Gradle Wrapper 8.9. Configure o SDK por `ANDROID_HOME` ou `local.properties`.

```sh
./gradlew testDebugUnitTest assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`. Preparar as dependências de desenvolvimento exige internet; usar o APK não exige conexão. Não é necessário pagar por nenhuma ferramenta.

Para usar a chave de depuração preservada, configure `ASCEND_KEYSTORE_PATH` com o caminho do arquivo antes da compilação. Sem essa variável, o Android Gradle Plugin usa a chave de depuração padrão do ambiente. Consulte `ASSINATURA.md` antes de gerar atualizações.

O fluxo central continua: registro por hábito/dia → recompensa com chave determinística → ledger transacional → nível/atributos/moedas derivados. Recompensas são snapshots; editar um hábito não reescreve XP histórico. Reverter moedas já gastas gera compensação futura, com saldo disponível limitado a zero.

Banco Room v5, com migrações explícitas 1 → 2 → 3 → 4 → 5; backups v6 leem também arquivos v1–v5. A migração 4 → 5 acrescenta estado de saga e texto de conversas reconhecidas. Nenhum fallback destrutivo é usado. Preferências de aparência, interesses e sugestões dispensadas ficam no aparelho e não são transferidas pelo backup.
