# Continuidade de assinatura

Identificador estável: `br.com.ascend.aetheris`. Versão 1.2.0, versionCode 3.

## APK fornecido nesta entrega

O APK 1.2 reutiliza a chave privada da 1.1, sem regenerá-la. Certificado SHA-256 de ambos:

`e1f003efe40d53754db66325c40177178aaa81d4ad02ab910c85642c681caae4`

Instale a 1.2 por cima do APK 1.1 fornecido no repositório. A identidade de assinatura é a mesma e o banco não mudou de versão. A chave permanece fora do Git público, no arquivo privado preservado `ascend-1.1.keystore`.

A chave original da 1.0 não está disponível; seu certificado é diferente. Para sair da 1.0, exporte um backup e siga [INSTALACAO.md](INSTALACAO.md). Se possui a chave da sua instalação, pode recompilar a atualização com ela.

## Compilações seguintes

Guarde uma cópia privada da chave correspondente ao APK instalado antes de trocar de ambiente. Não publique a chave no GitHub. O arquivo atual usa alias `androiddebugkey` e senhas padrão de debug `android`; é uma chave para instalação pessoal, não uma configuração de distribuição comercial.

Defina `ASCEND_KEYSTORE_PATH` com o caminho absoluto da chave de debug preservada e execute `./gradlew assembleDebug`. O projeto usa essa chave, em vez de criar outra. A configuração atual assume o alias e as senhas padrão acima. Para chaves com outras credenciais, ajuste sua configuração privada de assinatura.

No GitHub Actions, o secret opcional `ASCEND_KEYSTORE_BASE64` deve conter a chave que corresponde à instalação desejada. O workflow a restaura no local padrão do Android. Sem esse secret, o Actions gera outra assinatura e o APK recompilado exige reinstalação. O job `distribuir` apenas confere e entrega o APK já incluído em `downloads/`.

## Dados

Room v2: migration `MIGRATION_1_2` acrescenta somente `rest_days`. As tabelas da 1.0 são preservadas. Os dois esquemas estão em `app/schemas/`.

Backup ASCEND v3: JSON com checksum SHA-256, validação antes da importação e substituição transacional. A leitura aceita versões 1, 2 e 3. Backups v3 devem ser importados na versão 1.2 ou posterior. Preferências de aparência e permissões Android ficam no aparelho. O diário no backup é texto legível; guarde o arquivo privadamente.
