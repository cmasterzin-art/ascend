# Instalar ASCEND 1.6.2 no Android

Android 8.0 (API 26) ou superior. O uso não exige internet, conta ou pagamento.

## Atualizar os APKs 1.1, 1.2, 1.3, 1.3.1, 1.4, 1.4.5, 1.5.0, 1.5.1, 1.5.2, 1.6.0 ou 1.6.1 fornecidos neste repositório

1. Em Herói → Configurações, exporte um backup de segurança.
2. [Baixe ASCEND-1.6.2.apk](downloads/ASCEND-1.6.2.apk?raw=true) e abra o arquivo.
3. Confirme a atualização **sem desinstalar**. O pacote e o certificado são os mesmos; o versionCode passou para 13.
4. Abra o app e confira seu personagem. A 1.6.2 mantém Room v6 e backup v8 da 1.6.0. Sobre a 1.5.2, a migração Room v5 → v6 continua aditiva, incluindo planejamento e histórico de foco. Não recria as tabelas antigas. As migrações anteriores são mantidas.

Se o Android indicar conflito de assinatura, sua instalação foi compilada com outra chave. Não desinstale antes de exportar os dados; consulte ASSINATURA.md.

## Se já usa a 1.0

**Exporte seus dados antes de desinstalar.** Este APK tem uma assinatura diferente da primeira entrega, porque a chave original não está disponível nesta sessão.

1. Na **versão 1.0 ainda instalada**, abra **Herói → Configurações → Exportar backup**.
2. Salve o JSON em Downloads ou outra pasta fora do aplicativo. Confirme que o arquivo foi criado e guarde uma cópia.
3. Desinstale a 1.0. Essa ação apaga o banco do aplicativo; o backup salvo no passo anterior será usado na restauração.
4. [Baixe ASCEND-1.6.2.apk](downloads/ASCEND-1.6.2.apk?raw=true), abra pelo gerenciador de arquivos e instale.
5. Na tela de boas-vindas, toque em **Já tenho uma jornada · Restaurar backup**, escolha o JSON e confirme. A 1.6.2 aceita backups v1/v2/v3/v4/v5/v6/v7/v8. Se já criou outro personagem, use **Herói → Configurações → Importar backup**; os dados atuais serão substituídos pelos do arquivo.
6. Confira nome, hábitos e XP antes de apagar qualquer cópia de segurança.

Se você tem a chave original, pode compilar a 1.6.2 com ela e instalar diretamente por cima da 1.0, sem desinstalar. A migração do banco preserva os dados. Veja `ASSINATURA.md`.

## Primeira instalação

1. Baixe o APK e abra-o em Downloads/Arquivos.
2. Se solicitado pelo Android, permita **Instalar apps desconhecidos** para o gerenciador de arquivos ou navegador usado.
3. Instale e abra ASCEND; escolha personagem, classe e hábitos iniciais.
4. Na Jornada, use **+** para criar hábitos e registrar progresso. Hábitos simples são concluídos com um toque.

O APK é uma compilação pessoal de depuração assinada, distribuída fora da Play Store. O checksum está em `downloads/ASCEND-1.6.2.apk.sha256`.

## Novidades da 1.6

- **Jornada ou Hábitos → Planejador:** selecione um dia, ajuste atividades pontuais ou descanso. Exceções só podem ser editadas de hoje em diante e não concedem XP.
- **Jornada ou Hábitos → Análises:** períodos, gráficos, comparação e indicadores. Nos detalhes do hábito, use **Análises deste hábito**.
- **Hábito cronometrado → Foco:** configure o intervalo, pause/retome pela Jornada e registre ou descarte. **Jornada ou Hábitos → Foco** abre o histórico detalhado.
- Sessões iniciadas antes da 1.6 não recebem detalhes inventados. Seus registros antigos de minutos continuam nas análises.
- [Comparações visuais](docs/VISUAL-1.6.md) · [Regras e dados](docs/PRODUTIVIDADE-1.6.md).

## Correções da 1.5.2

- Quantidades inválidas não apagam o progresso; o formulário mostra como corrigir.
- Novos registros usam os dados atuais do hábito e preservam os snapshots históricos.
- Foco revalida o hábito e considera descansos ao registrar.
- Backups têm validação temporal adicional, sem alterar o formato.
- Widgets explicam ações antigas, revalidam o registro complexo e escondem barras sem atividade.
- Rascunhos de hábitos/registros são preservados no estado salvo da Activity; saída com mudanças pede confirmação.
- [Auditoria e checklist de testes em aparelho](docs/ESTABILIDADE-1.5.2.md). A instalação física de atualização ainda precisa ser conferida no seu aparelho.

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
