# Continuidade de assinatura

Identificador estável: `br.com.ascend.aetheris`. Versão 1.6.9, `versionCode 16`.

## Estado do APK 1.6.9 e preservação do certificado

O APK `1.6.8` publicado em `downloads/ASCEND-1.6.8.apk` reutiliza a chave privada das versões `1.1` a `1.6.1` (`ascend-1.1.keystore`), com certificado SHA-256:

`e1f003efe40d53754db66325c40177178aaa81d4ad02ab910c85642c681caae4`

Na compilação da versão `1.6.9` no GitHub Actions (execução `44190969728`, `22847850` bytes), a verificação local de `ascend-1.1.keystore` confirmou a chave oficial preservada fora do Git, porém o secret opcional `ASCEND_KEYSTORE_BASE64` ainda não estava configurado no repositório GitHub. Por isso, o APK gerado no artifact `ASCEND-Compilacao` utilizou o certificado temporário de depuração do runner:

`2af2b8789e804a3897403cd2de1029cfefa83c3c7858704549ffa31a8f0bc4f4`

Para não quebrar a compatibilidade de atualização sobre as versões `1.1` a `1.6.8`, **`downloads/ASCEND-1.6.9.apk` não foi publicado com essa chave temporária**. A chave privada `ascend-1.1.keystore` permanece fora do repositório público.

Para publicar o APK `1.6.9` oficialmente assinado sem exigir desinstalação:

1. Codifique o arquivo privado `ascend-1.1.keystore` em Base64 e cadastre no secret `ASCEND_KEYSTORE_BASE64` em **Settings → Secrets and variables → Actions** de `cmasterzin-art/ascend` (ou re-assine localmente o APK do artifact com `apksigner` e `ascend-1.1.keystore`).
2. Execute novamente o workflow **Compilar ASCEND** (`.github/workflows/android.yml`) na branch `feat/ascend-1.6.9` para produzir o APK assinado com `e1f003ef...`.
3. Publique o APK resultante em `downloads/ASCEND-1.6.9.apk`.

## APK 1.6.8 publicado e migração de dados

Instale a 1.6.8 (e futuramente a 1.6.9 após assinatura oficial) por cima dos APKs 1.1, 1.2, 1.3, 1.3.1, 1.4, 1.4.5, 1.5.0, 1.5.1, 1.5.2, 1.6.0 ou 1.6.1 fornecidos no repositório. A identidade de assinatura é a mesma; as versões 1.6.x mantêm Room v6 / backup v8 da 1.6.0 e a migração aditiva 5 → 6 sobre a 1.5.2. As migrações anteriores continuam testadas. A atualização em telefone físico não foi executada. A chave permanece fora do Git público, no arquivo privado preservado `ascend-1.1.keystore`.

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
