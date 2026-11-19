# ASCEND — Crônicas da Ascensão · 1.7.3

[**Baixar APK Android 1.7.3**](downloads/ASCEND-1.7.3.apk?raw=true) · [APK 1.7.2](downloads/ASCEND-1.7.2.apk?raw=true) · [APK 1.7.1](downloads/ASCEND-1.7.1.apk?raw=true) · [APK 1.7.0.1](downloads/ASCEND-1.7.0.1.apk?raw=true) · [APK 1.7.0](downloads/ASCEND-1.7.0.apk?raw=true) · [APK 1.6.9.19](downloads/ASCEND-1.6.9.19.apk?raw=true)

RPG de hábitos, projetos pessoais, agenda avançada, base de conhecimento, exploração de mundo e fabricação cosmética offline para Android 8.0 ou superior. **Tudo funciona localmente**, sem conta, internet, anúncios, assinatura ou serviços pagos. O GitHub distribui o código e o instalador; o aplicativo não tem permissão INTERNET.

A versão **1.7.3 — A Forja das Lendas** (*Living Hero, Runic Crafting & Nordic Viking Arsenal Update*) faz o personagem refletir materialmente sua jornada sobre a base consolidada da `1.7.2`, preservando a Biblioteca de Mímir, o Atlas 2.0, as regiões exploráveis, os Arquétipos, os Caminhos, a Agenda 2.0, o Foco e toda a integridade econômica:
- **Retrato Vivo (`LivingPortrait.kt`):** composição 2D em camadas calibradas sobre os três retratos originais (`wanderer_light/medium/dark.webp`, preservados integralmente sem ocultar rosto ou olhos), refletindo trajes/armaduras, elmos/coroas abertos, fíbulas no manto, fundos nórdicos, molduras pictóricas e aura/runa *Elder Futhark* própria para todas as **10 especializações**. [Retrato Vivo 1.7.3](docs/RETRATO-VIVO-1.7.3.md).
- **A Forja de Darian (`ForgeCrafting.kt`, `ForgeScreen.kt`):** integra a oficina funcional de fabricação cosmética à construção **A Forja** (`building:1`, mantendo as 2 etapas de restauração), com **8 receitas exclusivas** em Fragmentos de Aether (`craft:<recipeId>`, `0 XP`, fora da Loja, idempotência concorrente), prévia ao vivo na bigorna e diálogo/selo exclusivos para `mestre_da_forja` sem vantagem econômica. [Forja de Darian 1.7.3](docs/FORJA-DARIAN-1.7.3.md).
- **Arsenal dos Nove Reinos, Estúdio & 3 Conjuntos (`EquipmentScreen.kt`):** 7 nichos forjados com 5 níveis de raridade diferenciados por brasão e runa, **Estúdio de Personalização** com prévia segura de itens bloqueados (sem conceder posse), **3 conjuntos configuráveis** (`Jornada`, `Conselho`, `Expedição`) persistidos em **Room v9 (`MIGRATION_8_9`)** e **Backup v11** (`v1`–`v11`), além de **8 novos recursos pictóricos originais** (`1` prancha conceitual fora do APK + `7` atlas WebP somando `606,5 KB / 0,59 MB`). [Arsenal 1.7.3](docs/ARSENAL-1.7.3.md) · [Relatório Visual 1.7.3](docs/VISUAL-1.7.3.md).
- **Estabilidade da 1.7.2:** expedições de Foco exigem mínimo efetivo de 5 minutos (`>= 300s`) preservando claims legados válidos; arquivar administrativamente uma tarefa concluída preserva o crédito na expedição (reabrir ou excluir remove); documentação de botões e estágios regionais alinhada ao código real.

A `1.7.3` tem **191 testes aprovados** (`181` preservados + `10` novos) e Android Lint com **0 erros**. **Room v9** (`MIGRATION_8_9`) e **backup v11** são aditivos, sem alteração destrutiva e com retrocompatibilidade `v1`–`v11`.

**Atualização dos APKs 1.1 a 1.7.2:** a `1.7.3` (`versionCode 24`) mantém `applicationId` (`br.com.ascend.aetheris`), certificado oficial SHA-256 (`e1f003ef...`) e migração explícita `8 → 9`; exporte um backup de segurança e instale por cima, **sem desinstalar**. A antiga 1.0 possui assinatura diferente; consulte [INSTALACAO.md](INSTALACAO.md).

- [Instalação e atualização](INSTALACAO.md)
- [Funcionalidades e limites reais](PROGRESSO.md)
- [Assinatura e preservação de atualizações](ASSINATURA.md)
- [Validação](VALIDACAO.md)
- [Retrato Vivo 1.7.3](docs/RETRATO-VIVO-1.7.3.md) · [Forja de Darian 1.7.3](docs/FORJA-DARIAN-1.7.3.md) · [Arsenal 1.7.3](docs/ARSENAL-1.7.3.md) · [Visual 1.7.3](docs/VISUAL-1.7.3.md) · [Diário 1.7.3](docs/DESENVOLVIMENTO-1.7.3.md)
- [Exploração 1.7.2](docs/EXPLORACAO-1.7.2.md) · [Atlas 2.0](docs/ATLAS-2.0.md) · [Design System 3.0](docs/DESIGN-SYSTEM-3.0.md) · [Renascimento Rúnico](docs/RENASCIMENTO-RUNICO.md) · [Visual 1.7.2](docs/VISUAL-1.7.2.md)
- [Biblioteca de Mímir 1.7.1](docs/BIBLIOTECA-MIMIR-1.7.1.md) · [Arquétipos 1.7.1](docs/ARQUETIPOS-1.7.1.md) · [Visual 1.7.1](docs/VISUAL-1.7.1.md)
- [Auditoria 1.7.0.1](docs/AUDITORIA-1.7.0.1.md) · [Caminhos 1.7.0](docs/CAMINHOS-1.7.0.md) · [Agenda 2.0](docs/AGENDA-1.7.0.md) · [Visual 1.7.0](docs/VISUAL-1.7.0.md)
- [Biblioteca da Forja 1.6.9.19](docs/VISUAL-1.6.9.19.md) · [Legado da Forja 1.6.9.15](docs/VISUAL-1.6.9.15.md) · [Refinamento 1.6.9.10](docs/VISUAL-1.6.9.10.md) · [Consolidação 1.6.9](docs/VISUAL-1.6.9.md)
- [Auditoria, correções e checklist físico da 1.5.2](docs/ESTABILIDADE-1.5.2.md)
- [Exploração, fases, recompensas e widgets](docs/EXPLORACAO-1.5.md)

## Visual

| Retrato Vivo · Forja | Arsenal dos Nove Reinos | A Forja de Darian |
|---|---|---|
| <img src="docs/screenshots/1.7.3/02-retrato-equipado-forja.png" width="240" alt="Retrato Vivo Forja 1.7.3"> | <img src="docs/screenshots/1.7.3/06-arsenal-nove-reinos.png" width="240" alt="Arsenal dos Nove Reinos 1.7.3"> | <img src="docs/screenshots/1.7.3/11-forja-darian-oficina.png" width="240" alt="A Forja de Darian 1.7.3"> |

**[Relatório visual e 14 capturas reais 1.7.3](docs/VISUAL-1.7.3.md)** · [Visual 1.7.2](docs/VISUAL-1.7.2.md) · [Visual 1.7.1](docs/VISUAL-1.7.1.md) · [Visual 1.7.0](docs/VISUAL-1.7.0.md) · [Guia do Grimório e rotinas](docs/GRIMORIO.md)

Capturas dos testes de interface com personagens de teste. Nenhuma conclusão, expedição, fabricação, caderno ou flashcard fictício é incluído no aplicativo.

## Desenvolvimento

Kotlin, Jetpack Compose, Material 3, Room, DataStore, MVVM, Navigation Compose, Coroutines e StateFlow. JDK 17 ou 21, SDK Android 35, Gradle Wrapper 8.9. Configure o SDK por `ANDROID_HOME` ou `local.properties`.

```sh
./gradlew testDebugUnitTest assembleDebug lintDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`. Preparar as dependências de desenvolvimento exige internet; usar o APK não exige conexão. Não é necessário pagar por nenhuma ferramenta.

Para usar a chave de depuração preservada, configure `ASCEND_KEYSTORE_PATH` com o caminho do arquivo antes da compilação. Sem essa variável, o Android Gradle Plugin usa a chave de depuração padrão do ambiente. Consulte `ASSINATURA.md` antes de gerar atualizações.

O fluxo central continua: registro por hábito/dia → recompensa com chave determinística → ledger transacional → nível/atributos/moedas derivados. Recompensas são snapshots; editar um hábito não reescreve XP histórico. Reverter moedas já gastas gera compensação futura, com saldo disponível limitado a zero. Projetos (`Caminhos`), tarefas, blocos de tempo, compromissos, foco em projeto, cadernos, páginas, revisões SM-2, especializações e fabricação na Forja não inventam XP ou moedas.

Banco **Room v9**, com migrações explícitas `1 → 2 → 3 → 4 → 5 → 6 → 7 → 8 → 9`; **backups v11** leem também arquivos `v1`–`v10`. A migração `8 → 9` acrescenta apenas a tabela `equipment_loadouts` sem reconstruir ou alterar dados históricos. Nenhum fallback destrutivo é usado. Preferências de aparência, interesses e sugestões dispensadas ficam no aparelho e não são transferidas pelo backup.
