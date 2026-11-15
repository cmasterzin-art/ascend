# Continuidade de assinatura

Identificador estável: `br.com.ascend.aetheris`. Versão inicial: versionCode 1, versionName 1.0.0.

Esta entrega usa a chave pessoal de depuração criada na compilação. A chave original permanece preservada no ambiente Cloud em `/workspace/work/signing/debug.keystore`; ela não está publicada no repositório ou entre os downloads. Guarde uma cópia privada ao transferir o ambiente de compilação. Nunca publique a chave em um repositório público. A chave usa o formato padrão de debug do Android (alias `androiddebugkey`, senhas `android`); ela serve para continuidade desta instalação pessoal, não para distribuição comercial.

Para uma nova compilação compatível, restaure esse mesmo arquivo em `~/.android/debug.keystore` no ambiente de compilação, aumente `versionCode` no projeto e mantenha o applicationId. Não gere outra chave. Instale o APK sobre o anterior e preserve o banco.

## GitHub Actions

Crie o secret de repositório `ASCEND_KEYSTORE_BASE64` com o conteúdo base64 do arquivo original. O workflow o restaura antes da compilação. O segredo não deve aparecer em logs ou arquivos versionados. A integração atual não tem permissão para configurar secrets do Actions (HTTP 403); por isso ele ainda precisa ser configurado por um administrador no GitHub antes de gerar atualizações com a mesma assinatura.

Se o secret não estiver configurado, o workflow ainda gera um APK, mas ele usará uma chave nova e não poderá atualizar esta instalação. Esse comportamento é informado no log. Para atualizar sem a chave, será necessário exportar um backup, reinstalar e importar; prefira preservar a chave.

## Banco e migrações

Room exporta o esquema em `app/schemas/`. A versão inicial do banco é 1. Não há fallback destrutivo. Futuras mudanças de esquema devem incrementar a versão de Room, fornecer `Migration` ou `AutoMigration` adequada e testar com um banco de versão anterior. Alterar apenas código ou aparência sem mudar o esquema não exige migração.

O backup local usa formato ASCEND v1, checksum SHA-256 e validação antes da substituição transacional. Para novos formatos de backup, preserve a leitura da versão anterior ou implemente conversão explícita.
