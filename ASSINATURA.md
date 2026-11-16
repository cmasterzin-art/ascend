# Continuidade de assinatura

Identificador estável: `br.com.ascend.aetheris`. Versão 1.1.0, versionCode 2.

## APK fornecido nesta entrega

A chave original da 1.0 não existe no ambiente desta sessão. Não é possível recuperá-la a partir de um APK assinado. Por isso, o APK 1.1 fornecido usa uma **nova chave**, preservada privadamente em `/workspace/work/signing/ascend-1.1.keystore`, fora do repositório e dos downloads.

Este APK não instala por cima do APK 1.0 de assinatura diferente. Primeiro exporte um backup dentro da 1.0 e guarde-o fora da pasta do aplicativo. Só então desinstale, instale a 1.1 e importe o backup. Desinstalar sem backup apaga os dados locais.

Se você possui a chave original da 1.0, compile o código 1.1 com essa chave para uma atualização direta que preserve os dados. O banco possui migração explícita 1 → 2.

## Compilações seguintes

Guarde uma cópia privada da chave correspondente ao APK instalado antes de trocar de ambiente. Não publique a chave no GitHub. O arquivo atual usa alias `androiddebugkey` e senhas padrão de debug `android`; é uma chave para instalação pessoal, não uma configuração de distribuição comercial.

Defina `ASCEND_KEYSTORE_PATH` com o caminho absoluto da chave de debug preservada e execute `./gradlew assembleDebug`. O projeto usa essa chave, em vez de criar outra. A configuração atual assume o alias e as senhas padrão acima. Para chaves com outras credenciais, ajuste sua configuração privada de assinatura.

No GitHub Actions, o secret opcional `ASCEND_KEYSTORE_BASE64` deve conter a chave que corresponde à instalação desejada. O workflow a restaura no local padrão do Android. Sem esse secret, o Actions gera outra assinatura e o APK recompilado exige reinstalação. O job `distribuir` apenas confere e entrega o APK já incluído em `downloads/`.

## Dados

Room v2: migration `MIGRATION_1_2` acrescenta somente `rest_days`. As tabelas da 1.0 são preservadas. Os dois esquemas estão em `app/schemas/`.

Backup ASCEND v2: JSON com checksum SHA-256, validação antes da importação e substituição transacional. A leitura aceita versões 1 e 2. Backups v2 não são compatíveis com o leitor antigo da versão 1.0. Preferências de aparência e permissões Android ficam no aparelho. O diário no backup é texto legível; guarde o arquivo privadamente.
