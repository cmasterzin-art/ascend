# Continuidade de assinatura

Identificador estável: `br.com.ascend.aetheris`. Versão 1.3.1, versionCode 5.

## APK fornecido nesta entrega

O APK 1.3.1 reutiliza a chave privada das versões 1.1/1.2/1.3, sem regenerá-la. Certificado SHA-256 dessas versões:

`e1f003efe40d53754db66325c40177178aaa81d4ad02ab910c85642c681caae4`

Instale a 1.3.1 por cima dos APKs 1.1, 1.2 ou 1.3 fornecidos no repositório. A identidade de assinatura é a mesma; a migração aditiva do banco foi testada. A chave permanece fora do Git público, no arquivo privado preservado `ascend-1.1.keystore`.

A chave original da 1.0 não está disponível; seu certificado é diferente. Para sair da 1.0, exporte um backup e siga [INSTALACAO.md](INSTALACAO.md). Se possui a chave da sua instalação, pode recompilar a atualização com ela.

## Compilações seguintes

Guarde uma cópia privada da chave correspondente ao APK instalado antes de trocar de ambiente. Não publique a chave no GitHub. O arquivo atual usa alias `androiddebugkey` e senhas padrão de debug `android`; é uma chave para instalação pessoal, não uma configuração de distribuição comercial.

Defina `ASCEND_KEYSTORE_PATH` com o caminho absoluto da chave de debug preservada e execute `./gradlew assembleDebug`. O projeto usa essa chave, em vez de criar outra. A configuração atual assume o alias e as senhas padrão acima. Para chaves com outras credenciais, ajuste sua configuração privada de assinatura.

No GitHub Actions, o secret opcional `ASCEND_KEYSTORE_BASE64` deve conter a chave que corresponde à instalação desejada. O workflow a restaura no local padrão do Android. Sem esse secret, o Actions gera outra assinatura e o APK recompilado exige reinstalação. O job `distribuir` apenas confere e entrega o APK já incluído em `downloads/`.

## Dados

Room v3: `MIGRATION_1_2` acrescenta `rest_days`; `MIGRATION_2_3` acrescenta `templateId`, `narrativeName` e `trackStreak` a `habits`, com valores padrão. Os três esquemas estão em `app/schemas/`. Não há fallback destrutivo.

Backup ASCEND v4: JSON com checksum SHA-256, validação antes da importação e substituição transacional. A leitura aceita versões 1, 2, 3 e 4. Backups v4 devem ser importados na versão 1.3.1 ou posterior. Preferências de aparência, interesses, sugestões dispensadas e permissões Android ficam no aparelho. O diário no backup é texto legível; guarde o arquivo privadamente.
