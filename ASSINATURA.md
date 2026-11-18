# Continuidade de assinatura

Identificador estável: `br.com.ascend.aetheris`. Versão `1.6.9.19`, `versionCode 19` (sobre `1.6.9.15`, `versionCode 18`).

## Estado dos APKs 1.6.9, 1.6.9.10, 1.6.9.15 e 1.6.9.19 e preservação do certificado

Os APKs publicados em `downloads/ASCEND-1.6.9.19.apk`, `downloads/ASCEND-1.6.9.15.apk`, `downloads/ASCEND-1.6.9.10.apk`, `downloads/ASCEND-1.6.9.apk` e `downloads/ASCEND-1.6.8.apk` reutilizam a chave privada oficial das versões `1.1` a `1.6.8` (`ascend-1.1.keystore`), com certificado SHA-256 verificado por `apksigner verify --verbose --print-certs`:

`e1f003efe40d53754db66325c40177178aaa81d4ad02ab910c85642c681caae4`

O secret `ASCEND_KEYSTORE_BASE64` está configurado no repositório GitHub (`cmasterzin-art/ascend`) e o workflow `.github/workflows/android.yml` exporta `ASCEND_KEYSTORE_PATH` durante a compilação, garantindo que o APK gerado e publicado em `downloads/` preserve a mesma identidade criptográfica sem expor a chave privada no repositório público.

## APKs publicados e migração de dados

Instale a `1.6.9.19` (ou a `1.6.9.15` / `1.6.9.10` / `1.6.9`) diretamente por cima dos APKs `1.1`, `1.2`, `1.3`, `1.3.1`, `1.4`, `1.4.5`, `1.5.0`, `1.5.1`, `1.5.2`, `1.6.0`, `1.6.1`, `1.6.2`, `1.6.5`, `1.6.8`, `1.6.9`, `1.6.9.10` ou `1.6.9.15` fornecidos no repositório. A identidade de assinatura é a mesma; as versões `1.6.x` mantêm Room v6 / backup v8 da `1.6.0` e a migração aditiva 5 → 6 sobre a `1.5.2`. As migrações anteriores continuam testadas. A atualização em telefone físico não foi executada. A chave permanece fora do Git público, no arquivo privado preservado `ascend-1.1.keystore`.

A chave original da 1.0 não está disponível; seu certificado é diferente. Para sair da 1.0, exporte um backup e siga [INSTALACAO.md](INSTALACAO.md). Se possui a chave da sua instalação, pode recompilar a atualização com ela.

## Compilações seguintes

Guarde uma cópia privada da chave correspondente ao APK instalado antes de trocar de ambiente. Não publique a chave no GitHub. O arquivo atual usa alias `androiddebugkey` e senhas padrão de debug `android`; é uma chave para instalação pessoal, não uma configuração de distribuição comercial.

Defina `ASCEND_KEYSTORE_PATH` com o caminho absoluto da chave de debug preservada e execute `./gradlew assembleDebug`. O projeto usa essa chave, em vez de criar outra. A configuração atual assume o alias e as senhas padrão acima. Para chaves com outras credenciais, ajuste sua configuração privada de assinatura.

No GitHub Actions, o secret opcional `ASCEND_KEYSTORE_BASE64` deve conter a chave que corresponde à instalação desejada. O workflow a restaura no local padrão do Android. Sem esse secret, o Actions gera outra assinatura e o APK recompilado exige reinstalação. O job `distribuir` apenas confere e entrega o APK já incluído em `downloads/`.

## Dados

Histórico até Room v5: `MIGRATION_1_2` acrescenta `rest_days`; `MIGRATION_2_3` acrescenta `templateId`, `narrativeName` e `trackStreak` a `habits`, com valores padrão. `MIGRATION_3_4` acrescenta `sagaStart` e `sagaBaseline` às tabelas `quests` e `claims`. `MIGRATION_4_5` acrescenta estado, data e progresso do abandono em `quests` e texto/interlocutor de conversas em `assets`. Os esquemas anteriores estão em `app/schemas/`. Não há fallback destrutivo.

Backup ASCEND v8: JSON com checksum SHA-256, validação antes da importação e substituição transacional. A leitura aceita versões 1, 2, 3, 4, 5, 6, 7 e 8. Backups v7 devem ser importados na versão 1.5.0 ou posterior. Preferências de aparência, interesses, sugestões dispensadas, som/volume e permissões Android ficam no aparelho. O diário no backup é texto legível; guarde o arquivo privadamente.

A versão 1.5 não altera o esquema Room v5. Expedições usam quests/claims e assets existentes, com identificadores estáveis. O backup v7 identifica os novos conteúdos, itens e conquista.

A 1.5.1 preserva integralmente Room v5 e backup v7. Os formatos brasileiros de data são apenas de apresentação; o armazenamento permanece ISO.

A 1.5.2 foi comparada diretamente com o certificado do APK 1.5.1 e mantém Room v5 / backup v7. Datas inválidas de personagem/foco são rejeitadas antes da importação. Uma restauração com falha de gravação injetada comprovou o rollback; isso não substitui teste físico de atualização.

## Dados da 1.6

Room v6: `MIGRATION_5_6` cria `plan_exceptions`, `focus_sessions` e acrescenta `sessionId`/`beganAt` ao foco ativo. Mantém os esquemas anteriores e não usa fallback destrutivo. Backup v8 acrescenta esses dados, preserva checksum, validação e importação transacional. Backups v8 precisam da versão 1.6 ou posterior; backups antigos continuam legíveis. A assinatura continua a mesma da 1.5.2. Atualização em aparelho físico não foi executada.
