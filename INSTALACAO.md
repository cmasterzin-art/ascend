# Instalar ASCEND 1.7.3.1 no Android

Android 8.0 (API 26) ou superior. O uso não exige internet, conta ou pagamento.

## Atualizar os APKs 1.1 a 1.7.3 fornecidos neste repositório

1. Em Herói → Configurações, exporte um backup de segurança.
2. [Baixe ASCEND-1.7.3.1.apk](downloads/ASCEND-1.7.3.1.apk?raw=true) e abra o arquivo.
3. Confirme a atualização **sem desinstalar**. O pacote (`br.com.ascend.aetheris`) e o certificado oficial (`e1f003ef...`) são os mesmos; o `versionCode` passou para `25`.
4. Abra o app e confira seu personagem. A `1.7.3.1` migra aditivamente para **Room v10 (`MIGRATION_9_10`)** e **backup v12** (compatível com `v1`–`v12`), mantendo todos os dados anteriores (hábitos, foco, Caminhos, Agenda 2.0, Biblioteca de Mímir, especializações, exploração regional, Forja de Darian, conjuntos de equipamento, inventário e memórias).

Se o Android indicar conflito de assinatura, sua instalação foi compilada com outra chave. Não desinstale antes de exportar os dados; consulte `ASSINATURA.md`.

## Se já usa a 1.0

**Exporte seus dados antes de desinstalar.** Este APK tem uma assinatura diferente da primeira entrega, porque a chave original não está disponível nesta sessão.

1. Na **versão 1.0 ainda instalada**, abra **Herói → Configurações → Exportar backup**.
2. Salve o JSON em Downloads ou outra pasta fora do aplicativo. Confirme que o arquivo foi criado e guarde uma cópia.
3. Desinstale a 1.0. Essa ação apaga o banco do aplicativo; o backup salvo no passo anterior será usado na restauração.
4. [Baixe ASCEND-1.7.3.1.apk](downloads/ASCEND-1.7.3.1.apk?raw=true), abra pelo gerenciador de arquivos e instale.
5. Na tela de boas-vindas, toque em **Já tenho uma jornada · Restaurar backup**, escolha o JSON e confirme. A `1.7.3.1` aceita backups `v1` a `v12`. Se já criou outro personagem, use **Herói → Configurações → Importar backup**; os dados atuais serão substituídos pelos do arquivo.
6. Confira nome, hábitos e XP antes de apagar qualquer cópia de segurança.

Se você tem a chave original, pode compilar a `1.7.3.1` com ela e instalar diretamente por cima da 1.0, sem desinstalar. A migração do banco preserva os dados. Veja `ASSINATURA.md`.

## Primeira instalação

1. Baixe o APK e abra-o em Downloads/Arquivos.
2. Se solicitado pelo Android, permita **Instalar apps desconhecidos** para o gerenciador de arquivos ou navegador usado.
3. Instale e abra ASCEND; escolha personagem, classe e hábitos iniciais.
4. Na Jornada, use **+** para criar hábitos e registrar progresso, personalize seu **Retrato Vivo 1.1** e sua **Heráldica Rúnica** no **Arsenal 2.1**, consulte o **Codex Rúnico (24 runas do Elder Futhark)** pela **Biblioteca de Mímir** ou **Crônicas**, restaure **A Forja** na Vila para abrir **A Forja de Darian** (12 receitas, 3 estágios de aprimoramento e 3 Coleções Lendárias) e explore o **Atlas de Aetheris 2.0**.

O APK é uma compilação pessoal de depuração assinada com a chave preservada, distribuída fora da Play Store. O checksum está em `downloads/ASCEND-1.7.3.1.apk.sha256`.

## Novidades da 1.7.3.1, 1.7.3, 1.7.2, 1.7.1, 1.7.0 e 1.6

- **O Legado das Runas (`1.7.3.1`):** **Heráldica Rúnica** (8 símbolos, 4 formatos, 4 metais, 5 cores, 5 ornamentos, 3 estandartes e inscrição de até 6 runas), **Codex Rúnico completo** com 24 caracteres vetoriais do *Elder Futhark* e separação rigorosa entre história e ficção, **Forja de Darian expandida** para 12 receitas + **3 estágios de aprimoramento** (*Forjado → Gravado → Consagrado*) + **3 Coleções Lendárias** independentes das 3 Configurações de Equipamento, **Arsenal 2.1** (4 abas, busca, filtros combináveis e ordenação), **Retrato Vivo 1.1** e correções estruturais da `1.7.3` (equipamento idempotente, versionamento de foco em expedições, aplicação parcial segura de configurações e preservação de rascunho no Estúdio). [Heráldica](docs/HERALDICA-RUNICA.md) · [Codex Rúnico](docs/CODEX-RUNICO.md) · [Coleções Lendárias](docs/COLECOES-LENDARIAS.md) · [Aprimoramentos da Forja](docs/APRIMORAMENTOS-DA-FORJA.md) · [Visual 1.7.3.1](docs/VISUAL-1.7.3.1.md).
- **Retrato Vivo, A Forja de Darian & Arsenal dos Nove Reinos (`1.7.3`):** retrato em camadas pictóricas 2D sobre os 3 retratos originais refletindo armaduras, elmos abertos, fíbulas, molduras, fundos e todas as 10 especializações; oficina **A Forja de Darian** (8 receitas cosméticas em Fragmentos, `0 XP`, fora da Loja, diálogo exclusivo para *Mestre da Forja*); **Estúdio de Personalização** (com prévia segura de itens bloqueados, desfazer, salvar e restaurar) e **3 Conjuntos de Equipamento** salvos em Room v9 / backup v11; correções da `1.7.2` (mínimo de 5 min em sessões de foco para expedições e preservação de crédito ao arquivar tarefas concluídas). [Retrato Vivo](docs/RETRATO-VIVO-1.7.3.md) · [A Forja de Darian](docs/FORJA-DARIAN-1.7.3.md) · [Arsenal 1.7.3](docs/ARSENAL-1.7.3.md) · [Visual 1.7.3](docs/VISUAL-1.7.3.md).
- **Atlas 2.0, Biblioteca das Eras & Montanhas da Disciplina (`1.7.2`):** zoom e lista acessível no Atlas, 2 novas regiões exploráveis com 3 expedições cada (Eydis e Haldor), integração opcional com Caminhos, Mímir e Foco (com fallback para hábitos), 4 relíquias colecionáveis fora da Loja, 2 títulos e **Design System 3.0** com 6 famílias de botões viking e 10 novos recursos pictóricos (`0,81 MB`). [Exploração 1.7.2](docs/EXPLORACAO-1.7.2.md) · [Atlas 2.0](docs/ATLAS-2.0.md) · [Visual 1.7.2](docs/VISUAL-1.7.2.md).
- **Biblioteca de Mímir & Runas da Memória (`1.7.1`):** cadernos temáticos, páginas em Markdown (editar/visualizar), tags, favoritos, busca, ligações entre páginas + backlinks, referências bibliográficas, vínculo opcional com Caminhos/Tarefas e flashcards com revisão espaçada SM-2 (`0 XP` / `0 moedas` extras).
- **Ascensão dos Arquétipos (`1.7.1`):** 10 especializações (2 por classe a partir do nível 3, trocáveis sem apagar habilidades), 10 novas habilidades de classe + 6 universais preservadas e 5 Marcos de Sabedoria (`0` moedas). [Biblioteca de Mímir](docs/BIBLIOTECA-MIMIR-1.7.1.md) · [Arquétipos](docs/ARQUETIPOS-1.7.1.md) · [Visual 1.7.1](docs/VISUAL-1.7.1.md).
- **Caminhos (Projetos Pessoais):** organize projetos com etapas, tarefas, subtarefas reordenáveis, prazos, vínculo opcional a hábitos e sessões de foco sem alterar a economia de XP/moedas. Caminhos concluídos entram em **Mundo → Salão das Memórias**.
- **Agenda Profissional 2.0 (Planejador):** alternância entre **Dia**, **Semana** e **Mês**, blocos de tempo com detecção de sobreposição e comparação planejado × foco real, além de compromissos sem XP.
- **Caixa de Entrada e Central de Pendências:** capture ideias rapidamente e faça triagem atômica para tarefa ou compromisso; acompanhe tarefas em **Hoje**, **Próximas**, **Atrasadas**, **Sem data**, **Alta prioridade** e **Concluídas**.
- **Jornada — Rumos do Dia:** painel expansível com próximo compromisso, bloco, tarefas prioritárias, caminhos próximos e atalho discreto para **Runas da Memória** quando houver flashcards pendentes.
- **Central de Análises e Histórico de Foco (`1.6`+):** períodos, gráficos, sessões em hábitos e projetos com duração efetiva e zero XP adicional.
- [Caminhos 1.7.0](docs/CAMINHOS-1.7.0.md) · [Agenda 2.0](docs/AGENDA-1.7.0.md) · [Visual 1.7.0](docs/VISUAL-1.7.0.md) · [Produtividade e regras](docs/PRODUTIVIDADE-1.6.md).

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
