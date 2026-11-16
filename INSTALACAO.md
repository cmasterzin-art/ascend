# Instalar ASCEND 1.1 no Android

Android 8.0 (API 26) ou superior. O uso não exige internet, conta ou pagamento.

## Se já usa a 1.0

**Exporte seus dados antes de desinstalar.** Este APK tem uma assinatura diferente da primeira entrega, porque a chave original não está disponível nesta sessão.

1. Na **versão 1.0 ainda instalada**, abra **Herói → Configurações → Exportar backup**.
2. Salve o JSON em Downloads ou outra pasta fora do aplicativo. Confirme que o arquivo foi criado e guarde uma cópia.
3. Desinstale a 1.0. Essa ação apaga o banco do aplicativo; o backup salvo no passo anterior será usado na restauração.
4. [Baixe ASCEND-1.1.0.apk](downloads/ASCEND-1.1.0.apk?raw=true), abra pelo gerenciador de arquivos e instale.
5. Na tela de boas-vindas, toque em **Já tenho uma jornada · Restaurar backup**, escolha o JSON e confirme. A 1.1 aceita backups v1. Se já criou outro personagem, use **Herói → Configurações → Importar backup**; os dados atuais serão substituídos pelos do arquivo.
6. Confira nome, hábitos e XP antes de apagar qualquer cópia de segurança.

Se você tem a chave original, pode compilar a 1.1 com ela e instalar diretamente por cima da 1.0, sem desinstalar. A migração do banco preserva os dados. Veja `ASSINATURA.md`.

## Primeira instalação

1. Baixe o APK e abra-o em Downloads/Arquivos.
2. Se solicitado pelo Android, permita **Instalar apps desconhecidos** para o gerenciador de arquivos ou navegador usado.
3. Instale e abra ASCEND; escolha personagem, classe e hábitos iniciais.
4. Na Jornada, use **+** para criar hábitos e registrar progresso. Hábitos simples são concluídos com um toque.

O APK é uma compilação pessoal de depuração assinada, distribuída fora da Play Store. O checksum está em `downloads/ASCEND-1.1.0.apk.sha256`.

## Novidades que você pode experimentar

- **Herói → Configurações:** tema claro/escuro/sistema, reduzir animações e modo minimalista.
- **Hábitos:** busca, filtros e toque no nome para abrir detalhes e histórico.
- **Hábitos → Calendário:** escolha uma data e ative ou remova o descanso planejado.
- **Herói → Estatísticas:** períodos, curva de XP e comparação com o período anterior.
- **Mundo → Crônicas:** nove capítulos desbloqueados pelo progresso real.

## Compilar no computador

Abra o projeto no Android Studio, use JDK 17 ou 21 e SDK Android 35. Execute `./gradlew testDebugUnitTest assembleDebug`. O APK sai em `app/build/outputs/apk/debug/app-debug.apk`.

O workflow **Compilar ASCEND** também oferece o arquivo de `downloads/` no artefato **ASCEND-Android**. O artefato **ASCEND-Compilacao** é uma recompilação; sua assinatura depende do secret privado descrito em `ASSINATURA.md`. Nenhum serviço pago foi configurado.
