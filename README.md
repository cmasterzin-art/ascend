# ASCEND — Crônicas da Ascensão · 1.7.1

[**Baixar APK Android 1.7.1**](downloads/ASCEND-1.7.1.apk?raw=true) · [APK 1.7.0.1](downloads/ASCEND-1.7.0.1.apk?raw=true) · [APK 1.7.0](downloads/ASCEND-1.7.0.apk?raw=true) · [APK 1.6.9.19](downloads/ASCEND-1.6.9.19.apk?raw=true) · [APK 1.6.9.15](downloads/ASCEND-1.6.9.15.apk?raw=true)

RPG de hábitos, projetos pessoais, agenda avançada e base de conhecimento offline para Android 8.0 ou superior. **Tudo funciona localmente**, sem conta, internet, anúncios, assinatura ou serviços pagos. O GitHub distribui o código e o instalador; o aplicativo não tem permissão INTERNET.

A versão **1.7.1 — A Sabedoria dos Nove Reinos** (*Knowledge & Character Ascension Update*) combina duas grandes expansões sobre a base consolidada da `1.7.0.1`, preservando a Biblioteca da Forja (`1.6.9.19`), o foco, os Caminhos, a Agenda 2.0 e a integridade econômica:
- **A Biblioteca de Mímir:** cadernos temáticos com capas pictóricas, páginas em Markdown (editar e visualizar), tags normalizadas, favoritos, busca, ordenação, duplicação, movimentação, ligações explícitas entre páginas + backlinks, referências bibliográficas e integração opcional com `Caminhos` e `Tarefas`. [Documentação da Biblioteca de Mímir](docs/BIBLIOTECA-MIMIR-1.7.1.md).
- **Runas da Memória (Flashcards & SM-2):** revisão espaçada determinística (`Errei`, `Difícil`, `Bom`, `Fácil`), sessão interrompível com persistência por cartão, proteção contra revisão repetida no mesmo dia, `0 XP` / `0 moedas` extras e atalho discreto em `Rumos do Dia` quando há revisões pendentes.
- **Ascensão dos Arquétipos:** **10 especializações** (2 por classe, a partir do nível 3, trocáveis sem apagar habilidades), **10 novas habilidades de classe** usando o sistema existente de pontos de habilidade (preservando integralmente as 6 habilidades universais), **5 Marcos de Sabedoria** (`0` moedas) e **3 novos recursos pictóricos originais** (`O Acervo de Mímir`, `351,1 KB`). [Documentação de Arquétipos](docs/ARQUETIPOS-1.7.1.md) · [Relatório visual 1.7.1](docs/VISUAL-1.7.1.md).

A `1.7.1` tem **167 testes aprovados** (`155` preservados + `12` novos) e Android Lint com **0 erros**. **Room v8** (`MIGRATION_7_8`) e **backup v10** mantêm migração explícita e compatibilidade com todos os formatos anteriores (`v1`–`v9`).

**Atualização dos APKs 1.1 a 1.7.0.1:** a `1.7.1` (`versionCode 22`) mantém `applicationId` (`br.com.ascend.aetheris`), certificado oficial SHA-256 (`e1f003ef...`) e migração aditiva Room `v7 → v8` / backup `v10`; exporte um backup de segurança e instale por cima, **sem desinstalar**. A antiga 1.0 possui assinatura diferente; consulte [INSTALACAO.md](INSTALACAO.md).

- [Instalação e atualização](INSTALACAO.md)
- [Funcionalidades e limites reais](PROGRESSO.md)
- [Assinatura e preservação de atualizações](ASSINATURA.md)
- [Validação](VALIDACAO.md)
- [Biblioteca de Mímir 1.7.1](docs/BIBLIOTECA-MIMIR-1.7.1.md) · [Arquétipos 1.7.1](docs/ARQUETIPOS-1.7.1.md) · [Visual 1.7.1](docs/VISUAL-1.7.1.md)
- [Auditoria 1.7.0.1](docs/AUDITORIA-1.7.0.1.md) · [Caminhos 1.7.0](docs/CAMINHOS-1.7.0.md) · [Agenda 2.0](docs/AGENDA-1.7.0.md) · [Visual 1.7.0](docs/VISUAL-1.7.0.md)
- [Biblioteca da Forja 1.6.9.19](docs/VISUAL-1.6.9.19.md) · [Legado da Forja 1.6.9.15](docs/VISUAL-1.6.9.15.md) · [Refinamento 1.6.9.10](docs/VISUAL-1.6.9.10.md) · [Consolidação 1.6.9](docs/VISUAL-1.6.9.md)
- [Auditoria, correções e checklist físico da 1.5.2](docs/ESTABILIDADE-1.5.2.md)
- [Exploração, fases, recompensas e widgets](docs/EXPLORACAO-1.5.md)

## Visual

| Biblioteca de Mímir | Runas da Memória (SM-2) | Ascensão dos Arquétipos |
|---|---|---|
| <img src="docs/screenshots/v1.7.1/mimir-biblioteca-171.png" width="240" alt="Biblioteca de Mímir 1.7.1"> | <img src="docs/screenshots/v1.7.1/mimir-revisao-171.png" width="240" alt="Runas da Memória SM-2 1.7.1"> | <img src="docs/screenshots/v1.7.1/heroi-especializacoes-171.png" width="240" alt="Ascensão do Arquétipo no Herói 1.7.1"> |

**[Relatório visual e capturas 1.7.1](docs/VISUAL-1.7.1.md)** · [Visual 1.7.0](docs/VISUAL-1.7.0.md) · [Prancha da biblioteca e comparações 1.6.9.19](docs/VISUAL-1.6.9.19.md) · [Guia do Grimório e rotinas](docs/GRIMORIO.md)

Capturas dos testes de interface com personagens de teste. Nenhuma conclusão, caderno ou flashcard fictício é incluído no aplicativo.

## Desenvolvimento

Kotlin, Jetpack Compose, Material 3, Room, DataStore, MVVM, Navigation Compose, Coroutines e StateFlow. JDK 17 ou 21, SDK Android 35, Gradle Wrapper 8.9. Configure o SDK por `ANDROID_HOME` ou `local.properties`.

```sh
./gradlew testDebugUnitTest assembleDebug lintDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`. Preparar as dependências de desenvolvimento exige internet; usar o APK não exige conexão. Não é necessário pagar por nenhuma ferramenta.

Para usar a chave de depuração preservada, configure `ASCEND_KEYSTORE_PATH` com o caminho do arquivo antes da compilação. Sem essa variável, o Android Gradle Plugin usa a chave de depuração padrão do ambiente. Consulte `ASSINATURA.md` antes de gerar atualizações.

O fluxo central continua: registro por hábito/dia → recompensa com chave determinística → ledger transacional → nível/atributos/moedas derivados. Recompensas são snapshots; editar um hábito não reescreve XP histórico. Reverter moedas já gastas gera compensação futura, com saldo disponível limitado a zero. Projetos (`Caminhos`), tarefas, blocos de tempo, compromissos, foco em projeto, cadernos, páginas, revisões SM-2 e especializações não inventam XP ou moedas.

Banco **Room v8**, com migrações explícitas `1 → 2 → 3 → 4 → 5 → 6 → 7 → 8`; **backups v10** leem também arquivos `v1`–`v9`. A migração `7 → 8` acrescenta a coluna `specialization` em `player` e as tabelas `knowledge_notebooks`, `knowledge_pages`, `knowledge_page_links`, `knowledge_references`, `flashcards` e `flashcard_reviews` sem reconstruir ou alterar dados históricos. Nenhum fallback destrutivo é usado. Preferências de aparência, interesses e sugestões dispensadas ficam no aparelho e não são transferidas pelo backup.
