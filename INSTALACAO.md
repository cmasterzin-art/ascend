# Instalar ASCEND 1.5.1 no Android

Android 8.0 (API 26) ou superior. O uso não exige internet, conta ou pagamento.

## Atualizar os APKs 1.1, 1.2, 1.3, 1.3.1, 1.4, 1.4.5 ou 1.5.0 fornecidos neste repositório

1. Em Herói → Configurações, exporte um backup de segurança.
2. [Baixe ASCEND-1.5.1.apk](downloads/ASCEND-1.5.1.apk?raw=true) e abra o arquivo.
3. Confirme a atualização **sem desinstalar**. O pacote e o certificado são os mesmos; o versionCode passou para 9.
4. Abra o app e confira seu personagem. A 1.5.1 mantém Room v5 e backup v7 da 1.5.0, sem recriar o banco ou alterar as tabelas. As migrações anteriores são mantidas.

Se o Android indicar conflito de assinatura, sua instalação foi compilada com outra chave. Não desinstale antes de exportar os dados; consulte ASSINATURA.md.

## Se já usa a 1.0

**Exporte seus dados antes de desinstalar.** Este APK tem uma assinatura diferente da primeira entrega, porque a chave original não está disponível nesta sessão.

1. Na **versão 1.0 ainda instalada**, abra **Herói → Configurações → Exportar backup**.
2. Salve o JSON em Downloads ou outra pasta fora do aplicativo. Confirme que o arquivo foi criado e guarde uma cópia.
3. Desinstale a 1.0. Essa ação apaga o banco do aplicativo; o backup salvo no passo anterior será usado na restauração.
4. [Baixe ASCEND-1.5.1.apk](downloads/ASCEND-1.5.1.apk?raw=true), abra pelo gerenciador de arquivos e instale.
5. Na tela de boas-vindas, toque em **Já tenho uma jornada · Restaurar backup**, escolha o JSON e confirme. A 1.5.1 aceita backups v1/v2/v3/v4/v5/v6/v7. Se já criou outro personagem, use **Herói → Configurações → Importar backup**; os dados atuais serão substituídos pelos do arquivo.
6. Confira nome, hábitos e XP antes de apagar qualquer cópia de segurança.

Se você tem a chave original, pode compilar a 1.5.1 com ela e instalar diretamente por cima da 1.0, sem desinstalar. A migração do banco preserva os dados. Veja `ASSINATURA.md`.

## Primeira instalação

1. Baixe o APK e abra-o em Downloads/Arquivos.
2. Se solicitado pelo Android, permita **Instalar apps desconhecidos** para o gerenciador de arquivos ou navegador usado.
3. Instale e abra ASCEND; escolha personagem, classe e hábitos iniciais.
4. Na Jornada, use **+** para criar hábitos e registrar progresso. Hábitos simples são concluídos com um toque.

O APK é uma compilação pessoal de depuração assinada, distribuída fora da Play Store. O checksum está em `downloads/ASCEND-1.5.1.apk.sha256`.

## Refinamentos da 1.5.1

- Widgets com hierarquia de progresso, ações distintas e apresentação ajustada à altura, largura e fonte.
- Expedições e Guardião com fase/objetivo em destaque; Memórias com linha do tempo contínua e datas brasileiras.
- Crônica Semanal em seções, perfil mais compacto e dias da semana com alvos de toque maiores.
- [Capturas e limites reais](docs/VISUAL-1.5.1.md).

## Recursos preservados

- **Mundo → Mapa → Floresta da Persistência → Explorar região:** disponível no nível 3.
- **Tela inicial do Android → Widgets → ASCEND:** Jornada compacta ou Próximos passos. Redimensione para mostrar mais ações.

- **Mundo → Crônicas:** Crônica da Semana e Salão das Memórias. O Salão também abre pelo Perfil.
- **Herói → Configurações:** efeitos sonoros opcionais e volume. Começam desligados.
- **Missões → Sagas:** histórico, abandono, arquivo e nova tentativa.

- **Missões → Sagas:** escolha um dos seis modelos ou crie seu juramento.
- **Recompensas:** painel consolidado com valores reais e diálogos contextuais guardáveis.

- **Hábitos → Explorar hábitos:** Grimório com 102 sugestões e seis rotinas. Personalize antes de salvar.
- **Jornada:** painel heroico, retrato ilustrado, fiorde e progressos entalhados.
- **Missões:** códice de couro, páginas costuradas, selos e chefe ilustrado.
- **Mundo:** atlas geográfico, vila com três pinturas por construção e crônicas em pergaminho gasto.
- **Herói → Inventário / Loja:** arsenal, equipamentos pintados em nichos ao redor do retrato.
- **Herói → Configurações:** temas, reduzir animações e modo minimalista continuam disponíveis.

Não é necessário baixar fontes, imagens ou pacotes adicionais após a instalação. Todas as artes e funcionalidades são locais.

## Compilar no computador

Abra o projeto no Android Studio, use JDK 17 ou 21 e SDK Android 35. Execute `./gradlew testDebugUnitTest assembleDebug`. O APK sai em `app/build/outputs/apk/debug/app-debug.apk`.

O workflow **Compilar ASCEND** também oferece o arquivo de `downloads/` no artefato **ASCEND-Android**. O artefato **ASCEND-Compilacao** é uma recompilação; sua assinatura depende do secret privado descrito em `ASSINATURA.md`. Nenhum serviço pago foi configurado.
