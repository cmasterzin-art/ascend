# Continuidade de assinatura

Identificador estável: `br.com.ascend.aetheris`. Versão 1.4.5, versionCode 7.

## APK fornecido nesta entrega

O APK 1.4.5 reutiliza a chave privada das versões 1.1/1.2/1.3/1.3.1/1.4, sem regenerá-la. Certificado SHA-256 dessas versões:

`e1f003efe40d53754db66325c40177178aaa81d4ad02ab910c85642c681caae4`

Instale a 1.4.5 por cima dos APKs 1.1, 1.2, 1.3, 1.3.1 ou 1.4 fornecidos no repositório. A identidade de assinatura é a mesma; a migração aditiva do banco foi testada. A chave permanece fora do Git público, no arquivo privado preservado `ascend-1.1.keystore`.

A chave original da 1.0 não está disponível; seu certificado é diferente. Para sair da 1.0, exporte um backup e siga [INSTALACAO.md](INSTALACAO.md). Se possui a chave da sua instalação, pode recompilar a atualização com ela.

## Compilações seguintes

Guarde uma cópia privada da chave correspondente ao APK instalado antes de trocar de ambiente. Não publique a chave no GitHub. O arquivo atual usa alias `androiddebugkey` e senhas padrão de debug `android`; é uma chave para instalação pessoal, não uma configuração de distribuição comercial.

Defina `ASCEND_KEYSTORE_PATH` com o caminho absoluto da chave de debug preservada e execute `./gradlew assembleDebug`. O projeto usa essa chave, em vez de criar outra. A configuração atual assume o alias e as senhas padrão acima. Para chaves com outras credenciais, ajuste sua configuração privada de assinatura.

No GitHub Actions, o secret opcional `ASCEND_KEYSTORE_BASE64` deve conter a chave que corresponde à instalação desejada. O workflow a restaura no local padrão do Android. Sem esse secret, o Actions gera outra assinatura e o APK recompilado exige reinstalação. O job `distribuir` apenas confere e entrega o APK já incluído em `downloads/`.

## Dados

Room v5: `MIGRATION_1_2` acrescenta `rest_days`; `MIGRATION_2_3` acrescenta `templateId`, `narrativeName` e `trackStreak` a `habits`, com valores padrão. `MIGRATION_3_4` acrescenta `sagaStart` e `sagaBaseline` às tabelas `quests` e `claims`. `MIGRATION_4_5` acrescenta estado, data e progresso do abandono em `quests` e texto/interlocutor de conversas em `assets`. Os cinco esquemas estão em `app/schemas/`. Não há fallback destrutivo.

Backup ASCEND v6: JSON com checksum SHA-256, validação antes da importação e substituição transacional. A leitura aceita versões 1, 2, 3, 4, 5 e 6. Backups v6 devem ser importados na versão 1.4.5 ou posterior. Preferências de aparência, interesses, sugestões dispensadas, som/volume e permissões Android ficam no aparelho. O diário no backup é texto legível; guarde o arquivo privadamente.
