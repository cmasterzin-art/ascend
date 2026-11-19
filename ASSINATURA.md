# Continuidade de assinatura

Identificador estável: `br.com.ascend.aetheris`. Versão `1.7.3.1`, `versionCode 25`. O certificado do APK oficial é preservado via `ASCEND_KEYSTORE_BASE64` e coincide com as versões `1.1` a `1.7.3`. [Relatório de assinatura](docs/validation/APK-1.7.3.1.txt).

## Estado dos APKs 1.6.9 a 1.7.3.1 e preservação do certificado

Os APKs publicados em `downloads/ASCEND-1.7.3.1.apk`, `downloads/ASCEND-1.7.3.apk`, `downloads/ASCEND-1.7.2.apk`, `downloads/ASCEND-1.7.1.apk`, `downloads/ASCEND-1.7.0.1.apk`, `downloads/ASCEND-1.7.0.apk`, `downloads/ASCEND-1.6.9.19.apk`, `downloads/ASCEND-1.6.9.15.apk`, `downloads/ASCEND-1.6.9.10.apk`, `downloads/ASCEND-1.6.9.apk` e `downloads/ASCEND-1.6.8.apk` reutilizam a chave privada oficial das versões `1.1` a `1.7.3` (`ascend-1.1.keystore`), com certificado SHA-256 verificado por `apksigner verify --verbose --print-certs`:

`e1f003efe40d53754db66325c40177178aaa81d4ad02ab910c85642c681caae4`

O secret `ASCEND_KEYSTORE_BASE64` está configurado no repositório GitHub (`cmasterzin-art/ascend`) e o workflow `.github/workflows/android.yml` exporta `ASCEND_KEYSTORE_PATH` durante a compilação, garantindo que o APK gerado e publicado em `downloads/` preserve a mesma identidade criptográfica sem expor a chave privada no repositório público.

## APKs publicados e migração de dados

Instale a `1.7.3.1` diretamente por cima dos APKs `1.1` a `1.7.3` fornecidos no repositório. A identidade de assinatura é a mesma; a `1.7.3.1` evolui o esquema aditivamente para **Room v10 (`MIGRATION_9_10`)** e **backup v12** sem migração destrutiva, mantendo todas as migrações anteriores (`1 → 2 → 3 → 4 → 5 → 6 → 7 → 8 → 9 → 10`) e leitura dos backups **v1 a v12**. A atualização em telefone físico não foi executada. A chave permanece fora do Git público, no arquivo privado preservado `ascend-1.1.keystore`.

A chave original da 1.0 não está disponível; seu certificado é diferente. Para sair da 1.0, exporte um backup e siga [INSTALACAO.md](INSTALACAO.md). Se possui a chave da sua instalação, pode recompilar a atualização com ela.

## Compilações seguintes

Guarde uma cópia privada da chave correspondente ao APK instalado antes de trocar de ambiente. Não publique a chave no GitHub. O arquivo atual usa alias `androiddebugkey` e senhas padrão de debug `android`; é uma chave para instalação pessoal, não uma configuração de distribuição comercial.

Defina `ASCEND_KEYSTORE_PATH` com o caminho absoluto da chave de debug preservada e execute `./gradlew assembleDebug`. O projeto usa essa chave, em vez de criar outra. A configuração atual assume o alias e as senhas padrão acima. Para chaves com outras credenciais, ajuste sua configuração privada de assinatura.

No GitHub Actions, o secret opcional `ASCEND_KEYSTORE_BASE64` deve conter a chave que corresponde à instalação desejada. O workflow a restaura no local padrão do Android. Sem esse secret, o Actions gera outra assinatura e o APK recompilado exige reinstalação. O job `distribuir` apenas confere e entrega o APK já incluído em `downloads/`.

## Dados

Histórico até Room v5: `MIGRATION_1_2` acrescenta `rest_days`; `MIGRATION_2_3` acrescenta `templateId`, `narrativeName` e `trackStreak` a `habits`, com valores padrão. `MIGRATION_3_4` acrescenta `sagaStart` e `sagaBaseline` às tabelas `quests` e `claims`. `MIGRATION_4_5` acrescenta estado, data e progresso do abandono em `quests` e texto/interlocutor de conversas em `assets`. Os esquemas anteriores estão em `app/schemas/`. Não há fallback destrutivo.

Backup ASCEND v10: JSON com checksum SHA-256, validação estrita antes da importação e substituição transacional com rollback atômico. A leitura aceita versões 1, 2, 3, 4, 5, 6, 7, 8, 9 e 10. Backups v10 devem ser importados na versão 1.7.1 ou posterior. Preferências de aparência, interesses, sugestões dispensadas, som/volume e permissões Android ficam no aparelho. O diário e a Biblioteca de Mímir no backup são texto legível; guarde o arquivo privadamente.

A versão 1.5 não altera o esquema Room v5. Expedições usam quests/claims e assets existentes, com identificadores estáveis. O backup v7 identifica os novos conteúdos, itens e conquista.

A 1.5.1 preserva integralmente Room v5 e backup v7. Os formatos brasileiros de data são apenas de apresentação; o armazenamento permanece ISO.

A 1.5.2 foi comparada diretamente com o certificado do APK 1.5.1 e mantém Room v5 / backup v7. Datas inválidas de personagem/foco são rejeitadas antes da importação. Uma restauração com falha de gravação injetada comprovou o rollback; isso não substitui teste físico de atualização.

## Dados da 1.6, 1.7.0 e 1.7.1

Room v6: `MIGRATION_5_6` cria `plan_exceptions`, `focus_sessions` e acrescenta `sessionId`/`beganAt` ao foco ativo. Backup v8 acrescenta esses dados.

Room v7 (`1.7.0` / `1.7.0.1`): `MIGRATION_6_7` cria `paths`, `path_stages`, `path_tasks`, `path_subtasks`, `path_habit_links`, `time_blocks`, `agenda_events` e `inbox_items`, além de acrescentar colunas opcionais de vínculo a projeto/tarefa em `focus` e `focus_sessions` (`pathId`, `taskId`, `targetLabel`). Backup v9 inclui todos esses dados.

Room v8 (`1.7.1`): `MIGRATION_7_8` acrescenta a coluna `specialization` (`DEFAULT ''`) em `player` e cria `knowledge_notebooks`, `knowledge_pages`, `knowledge_page_links`, `knowledge_references`, `flashcards` e `flashcard_reviews` com chaves estrangeiras `RESTRICT` e índices explícitos. Mantém integralmente os esquemas `1.json` a `7.json`, não usa fallback destrutivo e preserva todos os dados históricos sem inventar cadernos ou revisões. Backup v10 inclui os novos dados, mantém compatibilidade com `v1`–`v9`, checksum SHA-256 e rollback transacional validado em teste automatizado.

Room v9 (`1.7.3`): `MIGRATION_8_9` cria a tabela aditiva `equipment_loadouts` (`9.json`). Backup v11 inclui os conjuntos de equipamento e mantém compatibilidade `v1`–`v11`.

Room v10 (`1.7.3.1`): `MIGRATION_9_10` acrescenta `focusRuleVersion INTEGER NOT NULL DEFAULT 0` em `quests` e `claims` (preservando fases e claims legados da 1.7.2) e cria a tabela aditiva `heraldry_profile` (`10.json`). Aprimoramentos da Forja reutilizam `assets.stage` (`1..3` para `forgeItemIds`, iniciando em `1` para itens já forjados na 1.7.3). Backup v12 inclui `heraldry` e `focusRuleVersion`, mantendo compatibilidade `v1`–`v12`, checksum SHA-256 e rollback atômico validado em teste. A assinatura continua idêntica (`e1f003efe40d53754db66325c40177178aaa81d4ad02ab910c85642c681caae4`). Atualização em aparelho físico não foi executada.
