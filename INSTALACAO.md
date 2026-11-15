# Instalar ASCEND no Android

Requisito: Android 8.0 (API 26) ou superior. O uso do aplicativo não requer internet nem conta.

1. Baixe `ASCEND-1.0.0.apk` no celular.
2. Abra o arquivo em Downloads/Arquivos. O Android pode pedir para permitir **Instalar apps desconhecidos** para o navegador ou gerenciador de arquivos usado.
3. Permita a instalação dessa origem e toque em **Instalar**.
4. Abra ASCEND, escolha seu nome, classe, retrato e hábitos iniciais.
5. Na Jornada, use `+` para registrar um hábito ou o botão de reprodução para iniciar foco em um hábito cronometrado.

O APK é uma compilação pessoal de depuração assinada. Não foi publicado na Play Store. O Android pode exibir um aviso ao instalar APKs externos.

Para atualizar, instale uma versão com o mesmo applicationId e a mesma chave sobre a versão atual. Não desinstale o aplicativo para atualizar: a desinstalação apaga o banco local. Exporte um backup em **Herói → Configurações** antes de trocar de aparelho ou de versão.

## Alternativa GitHub Actions

O projeto inclui `.github/workflows/android.yml`. Em um repositório do GitHub, abra **Actions → Compilar ASCEND → Run workflow**. Após o job verde, baixe o artefato **ASCEND-Android**, extraia o ZIP e abra o APK no celular. Não requer Android Studio no seu computador.

O workflow está preparado no código; isso não significa que tenha sido enviado ou executado em um repositório remoto. A execução depende de um repositório disponível. Use apenas a franquia gratuita aplicável à sua conta; nenhum serviço pago foi configurado.

Consulte `ASSINATURA.md` para manter a mesma assinatura em novas compilações e `PROGRESSO.md` para recursos e limites reais da versão.
