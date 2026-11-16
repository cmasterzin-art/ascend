package br.com.ascend.aetheris

import java.text.Normalizer
import java.util.Locale

data class HabitTemplate(val id:String,val name:String,val narrativeName:String,val description:String,val category:String,val attribute:Int,val icon:Int,val color:Int,val type:Int,val target:Int,val unit:String,val days:String,val xp:Int,val coins:Int,val points:Int,val difficulty:Int,val tags:List<String>,val estimatedMinutes:Int,val trackStreak:Boolean=true){
    fun instantiate(position:Int=0)=Habit(name=name,narrativeName=narrativeName,templateId=id,description=description,category=category,attribute=attribute,icon=icon,color=color,type=type,target=target,unit=unit,days=days,xp=xp,coins=coins,points=points,difficulty=difficulty,position=position,trackStreak=trackStreak)
}
private fun template(id:String,name:String,narrative:String,description:String,category:String,attribute:Int,target:Int,unit:String,xp:Int,minutes:Int,days:String,difficulty:Int)=HabitTemplate(id,name,narrative,description,category,attribute,attribute,attribute,if(unit=="minutos")2 else if(target==1)0 else 1,target,unit,days,xp,5,5,difficulty,listOf(category,attributes[attribute],unit,when(attribute){0->"conhecimento estudo aprendizado";1->"movimento treino físico";2->"organização responsabilidade";3->"descanso bem-estar autocuidado";else->"criação arte projetos"}),minutes,attribute!=3)
val habitTemplates=listOf(
    template("study_law","Estudar Direito","Os Tomos da Justiça","Escolha um tema jurídico e reserve um período para compreendê-lo.","Intelecto",0,60,"minutos",60,60,"1,2,3,4,5,6,7",2),
    template("study_subject","Estudar uma disciplina","A Mesa do Sábio","Avance em uma disciplina no seu próprio ritmo.","Intelecto",0,45,"minutos",45,45,"1,2,3,4,5,6,7",2),
    template("read_book","Ler um livro","A Sabedoria dos Antigos","Leia algumas páginas e registre o que despertou sua curiosidade.","Intelecto",0,20,"páginas",30,30,"1,2,3,4,5,6,7",1),
    template("review_notes","Revisar anotações","Os Fragmentos do Saber","Retorne às suas notas e destaque dúvidas importantes.","Intelecto",0,30,"minutos",30,30,"1,2,3,4,5,6,7",1),
    template("solve_questions","Resolver questões","O Desafio dos Tomos","Pratique questões e observe como chegou às respostas.","Intelecto",0,20,"questões",40,40,"1,2,3,4,5,6,7",1),
    template("learn_language","Aprender um idioma","As Línguas dos Reinos","Explore uma língua por leitura, escuta ou conversação.","Intelecto",0,30,"minutos",40,30,"1,2,3,4,5,6,7",1),
    template("vocabulary","Praticar vocabulário","As Palavras do Vento","Revise palavras em contextos que façam sentido para você.","Intelecto",0,15,"minutos",20,15,"1,2,3,4,5,6,7",0),
    template("watch_class","Assistir a uma aula","A Voz dos Mestres","Acompanhe uma aula e guarde suas principais perguntas.","Intelecto",0,60,"minutos",40,60,"1,2,3,4,5,6,7",2),
    template("summary","Produzir resumo","O Pergaminho Essencial","Sintetize um assunto com suas próprias palavras.","Intelecto",0,1,"resumo",45,30,"1,2,3,4,5,6,7",1),
    template("programming","Estudar programação","As Runas da Lógica","Estude um conceito de código e experimente na prática.","Intelecto",0,60,"minutos",60,60,"1,2,3,4,5,6,7",2),
    template("new_concept","Aprender um conceito novo","A Centelha do Saber","Investigue uma ideia nova e explique-a com um exemplo.","Intelecto",0,1,"conceito",25,15,"1,2,3,4,5,6,7",0),
    template("educational_audio","Ouvir conteúdo educativo","Ecos de Conhecimento","Ouça um conteúdo educativo escolhido por você.","Intelecto",0,30,"minutos",20,30,"1,2,3,4,5,6,7",1),
    template("book_notes","Escrever um fichamento","O Registro dos Tomos","Organize referências, ideias e observações de uma leitura.","Intelecto",0,1,"fichamento",50,45,"1,2,3,4,5,6,7",2),
    template("presentation","Preparar uma apresentação","A Palavra na Assembleia","Organize o que deseja comunicar e ensaie com calma.","Intelecto",0,45,"minutos",45,45,"1,2,3,4,5,6,7",2),
    template("essay","Praticar redação","A Pena do Aprendiz","Escreva um texto e revise um aspecto por vez.","Intelecto",0,30,"minutos",35,30,"1,2,3,4,5,6,7",1),
    template("walk","Caminhar","A Trilha do Peregrino","Escolha um percurso e uma duração adequados ao seu momento.","Vigor",1,30,"minutos",35,30,"1,3,5",1),
    template("run","Correr","O Passo do Vento","Pratique corrida em ritmo e condições adequados a você.","Vigor",1,20,"minutos",50,20,"1,3,5",1),
    template("strength","Musculação","A Forja do Guerreiro","Registre seu treino de força adaptando carga e duração.","Vigor",1,45,"minutos",60,45,"1,3,5",2),
    template("stretch","Alongamento","Os Laços do Corpo","Faça movimentos confortáveis dentro dos seus limites.","Vigor",1,15,"minutos",20,15,"1,3,5",0),
    template("yoga","Praticar yoga","O Equilíbrio da Árvore","Pratique uma sequência de yoga compatível com sua experiência.","Vigor",1,30,"minutos",35,30,"1,3,5",1),
    template("cycle","Pedalar","As Rodas do Horizonte","Pedale em um percurso e intensidade que funcionem para você.","Vigor",1,30,"minutos",40,30,"1,3,5",1),
    template("swim","Nadar","O Chamado do Fiorde","Reserve tempo para nadar em local e condições seguros.","Vigor",1,30,"minutos",45,30,"1,3,5",1),
    template("sport","Praticar esporte","Os Jogos da Vila","Pratique um esporte de que gosta, respeitando seu ritmo.","Vigor",1,60,"minutos",50,60,"1,3,5",2),
    template("mobility","Fazer mobilidade","As Articulações da Jornada","Explore movimentos de mobilidade sem forçar desconforto.","Vigor",1,15,"minutos",20,15,"1,3,5",0),
    template("dance","Dançar","O Ritmo da Fogueira","Movimente-se com uma música que tenha significado para você.","Vigor",1,30,"minutos",30,30,"1,3,5",1),
    template("gentle_walk","Fazer caminhada leve","O Caminho Sereno","Faça uma caminhada leve conforme sua disposição.","Vigor",1,15,"minutos",15,15,"1,3,5",0),
    template("martial_arts","Praticar artes marciais","A Disciplina da Lâmina","Registre uma prática orientada e adequada ao seu nível.","Vigor",1,45,"minutos",55,45,"1,3,5",2),
    template("endurance","Treinar resistência","A Constância da Montanha","Realize seu treino de resistência com duração adaptável.","Vigor",1,30,"minutos",45,30,"1,3,5",1),
    template("home_exercise","Realizar exercícios em casa","O Treinamento do Guerreiro","Escolha movimentos e intensidade adequados às suas condições.","Vigor",1,20,"minutos",30,20,"1,3,5",1),
    template("active_break","Fazer pausa ativa","O Corpo Desperto","Interrompa um período parado com movimento confortável.","Vigor",1,5,"minutos",10,5,"1,3,5",0),
    template("make_bed","Arrumar a cama","A Primeira Ordem","Prepare seu espaço de descanso com um pequeno gesto.","Disciplina",2,1,"vez",10,5,"1,2,3,4,5,6,7",0),
    template("organize_room","Organizar o quarto","A Ordem do Refúgio","Escolha uma parte do quarto para organizar.","Disciplina",2,1,"vez",20,15,"1,2,3,4,5,6,7",0),
    template("clear_desk","Limpar a mesa","A Mesa da Jornada","Deixe sua mesa pronta para a próxima atividade.","Disciplina",2,1,"vez",10,5,"1,2,3,4,5,6,7",0),
    template("plan_day","Planejar o dia","O Juramento da Aurora","Escolha prioridades realistas, incluindo espaço para pausas.","Disciplina",2,1,"planejamento",20,10,"1,2,3,4,5,6,7",0),
    template("review_tasks","Revisar tarefas","A Lista do Cronista","Revise compromissos e ajuste o que deixou de fazer sentido.","Disciplina",2,1,"revisão",15,10,"1,2,3,4,5,6,7",0),
    template("organize_files","Organizar arquivos","O Arquivo da Ordem","Organize um grupo de arquivos físicos ou digitais.","Disciplina",2,20,"minutos",25,20,"1,2,3,4,5,6,7",1),
    template("prepare_study","Preparar o material de estudo","A Mesa dos Tomos","Separe apenas o material necessário para sua sessão.","Disciplina",2,1,"vez",10,5,"1,2,3,4,5,6,7",0),
    template("dishes","Lavar a louça","O Cuidado do Lar","Cuide da louça em uma quantidade compatível com seu tempo.","Disciplina",2,1,"vez",15,15,"1,2,3,4,5,6,7",0),
    template("clean_house","Limpar a casa","As Pedras do Refúgio","Escolha uma área da casa para cuidar.","Disciplina",2,30,"minutos",35,30,"1,2,3,4,5,6,7",1),
    template("organize_clothes","Organizar roupas","Os Tecidos da Ordem","Organize, dobre ou separe as roupas que precisar.","Disciplina",2,20,"minutos",20,20,"1,2,3,4,5,6,7",1),
    template("review_budget","Revisar o orçamento","A Balança dos Fragmentos","Observe entradas e gastos sem transformar números em julgamento.","Disciplina",2,1,"revisão",30,20,"7",1),
    template("meal_prep","Preparar refeições","A Mesa Compartilhada","Prepare uma refeição ou organize ingredientes disponíveis.","Disciplina",2,1,"preparação",25,30,"1,2,3,4,5,6,7",1),
    template("organize_documents","Organizar documentos","Os Selos da Vila","Reúna documentos e organize uma forma de encontrá-los.","Disciplina",2,30,"minutos",30,30,"1,2,3,4,5,6,7",1),
    template("resolve_task","Resolver uma pendência","O Nó Desfeito","Escolha uma pendência possível de resolver hoje.","Disciplina",2,1,"pendência",25,20,"1,2,3,4,5,6,7",1),
    template("plan_week","Planejar a semana","Os Sete Caminhos","Organize a semana considerando compromissos e descanso.","Disciplina",2,1,"planejamento",40,30,"7",1),
    template("meditate","Meditar","O Silêncio das Runas","Reserve um momento de atenção, sem cobrar um resultado.","Equilíbrio",3,10,"minutos",20,10,"1,2,3,4,5,6,7",0),
    template("breathe","Fazer exercício de respiração","O Sopro do Fiorde","Pratique uma respiração confortável, sem forçar retenções.","Equilíbrio",3,5,"minutos",10,5,"1,2,3,4,5,6,7",0),
    template("leisure_read","Ler por lazer","As Histórias da Fogueira","Leia algo pelo prazer de acompanhar uma história.","Equilíbrio",3,20,"minutos",20,20,"1,2,3,4,5,6,7",1),
    template("rest_break","Fazer pausa de descanso","O Abrigo Entre Passos","Interrompa atividades e permita-se uma pausa.","Equilíbrio",3,15,"minutos",15,15,"1,2,3,4,5,6,7",0),
    template("sleep_time","Manter horário de dormir","O Pacto com a Noite","Escolha um horário possível para sua rotina, sem perfeccionismo.","Equilíbrio",3,1,"noite",25,5,"1,2,3,4,5,6,7",0),
    template("mood","Registrar o humor","As Marés Interiores","Anote como está se sentindo, sem precisar explicar tudo.","Equilíbrio",3,1,"registro",10,5,"1,2,3,4,5,6,7",0),
    template("journal","Escrever no diário","As Crônicas da Jornada","Guarde algumas palavras sobre seu dia.","Equilíbrio",3,1,"entrada",20,10,"1,2,3,4,5,6,7",0),
    template("outdoors","Passar tempo ao ar livre","Sob o Céu de Aetheris","Encontre um momento ao ar livre conforme suas possibilidades.","Equilíbrio",3,20,"minutos",25,20,"1,2,3,4,5,6,7",1),
    template("gratitude","Praticar gratidão","As Pequenas Brasas","Registre algo que aprecia, se isso fizer sentido hoje.","Equilíbrio",3,1,"registro",10,5,"1,2,3,4,5,6,7",0),
    template("relax","Fazer uma atividade relaxante","O Refúgio da Névoa","Escolha uma atividade que ajude você a desacelerar.","Equilíbrio",3,30,"minutos",25,30,"1,2,3,4,5,6,7",1),
    template("hobby","Dedicar tempo a um hobby","O Prazer da Jornada","Dedique tempo a algo de que gosta, sem exigir produtividade.","Equilíbrio",3,30,"minutos",25,30,"1,2,3,4,5,6,7",1),
    template("loved_one","Conversar com alguém querido","Os Laços da Vila","Entre em contato com alguém com quem deseja conversar.","Equilíbrio",3,1,"interação",20,15,"1,2,3,4,5,6,7",0),
    template("digital_break","Reduzir distrações digitais","Além dos Espelhos","Reserve um intervalo com menos interrupções digitais.","Equilíbrio",3,30,"minutos",20,30,"1,2,3,4,5,6,7",1),
    template("sleep_routine","Preparar a rotina de sono","O Limiar da Noite","Prepare um ambiente e pequenos gestos que favoreçam seu descanso.","Equilíbrio",3,1,"vez",15,10,"1,2,3,4,5,6,7",0),
    template("free_time","Reservar tempo livre","O Horizonte Aberto","Deixe um espaço livre, sem a obrigação de preenchê-lo.","Equilíbrio",3,30,"minutos",20,30,"1,2,3,4,5,6,7",1),
    template("write","Escrever","A Pena do Artífice","Escreva um trecho de qualquer gênero, ajustando a quantidade.","Criatividade",4,500,"palavras",40,30,"1,3,5",1),
    template("draw","Desenhar","Os Traços do Mundo","Explore formas e linhas sem exigir uma obra pronta.","Criatividade",4,30,"minutos",35,30,"1,3,5",1),
    template("paint","Pintar","As Cores da Aurora","Experimente cores e materiais em uma sessão de pintura.","Criatividade",4,30,"minutos",35,30,"1,3,5",1),
    template("music","Praticar instrumento musical","O Canto dos Skalds","Pratique um instrumento em um trecho ou exercício escolhido.","Criatividade",4,30,"minutos",40,30,"1,3,5",1),
    template("personal_project","Trabalhar em projeto pessoal","A Forja do Artífice","Avance uma parte possível de um projeto seu.","Criatividade",4,60,"minutos",60,60,"1,3,5",2),
    template("photography","Fazer fotografia","A Luz Guardada","Observe o ambiente e experimente registrar imagens.","Criatividade",4,20,"minutos",25,20,"1,3,5",1),
    template("video_editing","Aprender edição de vídeo","As Memórias em Movimento","Explore uma técnica de edição em um pequeno trecho.","Criatividade",4,30,"minutos",35,30,"1,3,5",1),
    template("design","Praticar design","Os Sinais do Artífice","Pratique composição, tipografia ou outra habilidade visual.","Criatividade",4,30,"minutos",35,30,"1,3,5",1),
    template("app_dev","Desenvolver um aplicativo","A Arquitetura das Runas","Implemente ou refine uma parte de seu aplicativo.","Criatividade",4,60,"minutos",60,60,"1,3,5",2),
    template("story","Escrever uma história","A Saga por Escrever","Crie uma cena, personagem ou passagem de sua história.","Criatividade",4,30,"minutos",40,30,"1,3,5",1),
    template("content","Criar conteúdo","A Voz do Artífice","Produza um conteúdo no formato que preferir.","Criatividade",4,1,"produção",35,30,"1,3,5",1),
    template("crafts","Fazer artesanato","As Mãos da Madeira","Trabalhe em uma peça manual respeitando seu ritmo.","Criatividade",4,30,"minutos",30,30,"1,3,5",1),
    template("model_3d","Aprender modelagem 3D","As Formas do Amanhã","Experimente uma técnica de modelagem em três dimensões.","Criatividade",4,45,"minutos",45,45,"1,3,5",2),
    template("plan_project","Planejar um projeto","O Desenho da Forja","Defina uma próxima etapa concreta do seu projeto.","Criatividade",4,30,"minutos",30,30,"1,3,5",1),
    template("art_technique","Experimentar uma técnica artística","A Centelha Criativa","Explore um material ou técnica sem obrigação de dominar.","Criatividade",4,30,"minutos",35,30,"1,3,5",1),
    template("resume","Atualizar currículo","O Registro dos Ofícios","Atualize uma parte do currículo com sua experiência real.","Desenvolvimento profissional",2,1,"atualização",25,20,"1,2,3,4,5,6,7",1),
    template("exam_study","Estudar para concurso","Os Portões do Ofício","Estude um tema de seu planejamento para concurso.","Desenvolvimento profissional",2,45,"minutos",45,45,"1,2,3,4,5,6,7",2),
    template("interview","Preparar entrevista","A Palavra Preparada","Organize exemplos e perguntas para uma entrevista.","Desenvolvimento profissional",2,30,"minutos",30,30,"1,2,3,4,5,6,7",1),
    template("application","Enviar candidatura","A Carta ao Horizonte","Envie uma candidatura a uma oportunidade escolhida por você.","Desenvolvimento profissional",2,1,"candidatura",20,15,"1,2,3,4,5,6,7",0),
    template("portfolio","Desenvolver portfólio","As Obras do Caminho","Selecione ou apresente um trabalho de seu portfólio.","Desenvolvimento profissional",2,45,"minutos",45,45,"1,2,3,4,5,6,7",2),
    template("course","Participar de curso","A Oficina dos Mestres","Acompanhe uma etapa de um curso que tenha escolhido.","Desenvolvimento profissional",2,45,"minutos",40,45,"1,2,3,4,5,6,7",2),
    template("professional_skill","Aprimorar habilidade profissional","O Ofício em Prática","Pratique uma habilidade útil para seu trabalho.","Desenvolvimento profissional",2,30,"minutos",35,30,"1,2,3,4,5,6,7",1),
    template("expenses","Registrar despesas","O Livro dos Fragmentos","Registre despesas para compreender seus próprios números.","Finanças pessoais",2,1,"registro",15,5,"1,2,3,4,5,6,7",0),
    template("finance_budget","Revisar orçamento pessoal","O Cofre da Jornada","Confira seu orçamento e ajuste o que for necessário.","Finanças pessoais",2,1,"revisão",25,20,"7",1),
    template("purchases","Planejar compras","A Escolha do Mercador","Organize compras conforme suas prioridades e possibilidades.","Finanças pessoais",2,1,"planejamento",15,10,"1,2,3,4,5,6,7",0),
    template("receipts","Organizar comprovantes","Os Registros do Cofre","Separe comprovantes importantes para encontrá-los depois.","Finanças pessoais",2,1,"organização",15,10,"1,2,3,4,5,6,7",0),
    template("finance_study","Estudar educação financeira","O Saber do Mercador","Estude um conceito financeiro em uma fonte de sua escolha.","Finanças pessoais",2,20,"minutos",25,20,"1,2,3,4,5,6,7",1),
    template("saving_goals","Revisar objetivos de economia","Os Planos do Cofre","Ajuste objetivos às suas condições atuais, sem metas impostas.","Finanças pessoais",2,1,"revisão",20,15,"7",0),
    template("family","Conversar com familiares","As Raízes da Jornada","Converse com uma pessoa da família com quem deseja estar.","Relações pessoais",3,1,"conversa",20,15,"1,2,3,4,5,6,7",0),
    template("friends","Encontrar amigos","O Encontro na Fogueira","Reserve um encontro possível e agradável com amigos.","Relações pessoais",3,1,"encontro",25,30,"1,2,3,4,5,6,7",1),
    template("together","Realizar atividade conjunta","A Trilha Compartilhada","Escolha uma atividade para realizar em companhia.","Relações pessoais",3,1,"atividade",25,30,"1,2,3,4,5,6,7",1),
    template("thank_someone","Demonstrar gratidão","A Palavra de Apreço","Expresse apreço a alguém, do modo que preferir.","Relações pessoais",3,1,"gesto",10,5,"1,2,3,4,5,6,7",0),
    template("quality_time","Dedicar tempo de qualidade a alguém","A Presença Inteira","Ofereça um momento de atenção a alguém querido.","Relações pessoais",3,20,"minutos",20,20,"1,2,3,4,5,6,7",1),
    template("water","Beber água","A Fonte do Caminho","Registre uma pausa para beber água. Ajuste a meta às suas necessidades.","Autocuidado cotidiano",3,1,"copo",10,5,"1,2,3,4,5,6,7",0),
    template("hygiene","Realizar higiene pessoal","O Cuidado Consigo","Reserve tempo para os cuidados cotidianos que fazem sentido para você.","Autocuidado cotidiano",3,1,"vez",10,10,"1,2,3,4,5,6,7",0),
    template("prepare_meal","Preparar uma refeição","O Alimento do Refúgio","Prepare uma refeição conforme os recursos disponíveis.","Autocuidado cotidiano",3,1,"refeição",20,30,"1,2,3,4,5,6,7",1),
    template("rest_space","Organizar ambiente de descanso","O Abrigo da Noite","Faça um pequeno ajuste para tornar seu espaço mais confortável.","Autocuidado cotidiano",3,1,"vez",15,10,"1,2,3,4,5,6,7",0),
    template("meal_break","Fazer pausa para alimentação","A Mesa do Viajante","Reserve um intervalo para se alimentar sem pressa, quando possível.","Autocuidado cotidiano",3,15,"minutos",15,15,"1,2,3,4,5,6,7",0),
    template("plant","Cuidar de uma planta","As Raízes do Refúgio","Observe sua planta e cuide dela conforme sua necessidade.","Autocuidado cotidiano",3,1,"cuidado",10,5,"1,2,3,4,5,6,7",0),
    template("plan_tomorrow","Planejar o dia seguinte","A Promessa do Amanhã","Escolha poucas prioridades para amanhã.","Disciplina",2,1,"planejamento",20,10,"1,2,3,4,5,6,7",0),
    template("ideas","Registrar ideias","As Sementes da Forja","Guarde ideias sem precisar executá-las agora.","Criatividade",4,1,"registro",15,10,"1,3,5",0),
    template("review_project","Revisar o progresso do projeto","As Marcas da Forja","Observe o que avançou e escolha um próximo passo.","Criatividade",4,1,"revisão",20,15,"1,3,5",0)
)
val catalogCategories=habitTemplates.map{it.category}.distinct()
val difficultyNames=listOf("Leve","Moderada","Desafiadora")
val habitTypeNames=listOf("Simples","Quantidade","Tempo","Repetições")
fun searchKey(text:String)=Normalizer.normalize(text,Normalizer.Form.NFD).replace(Regex("\\p{M}+"),"").lowercase(Locale.ROOT).trim()
data class CatalogFilter(val query:String="",val category:String?=null,val difficulty:Int?=null,val type:Int?=null,val attribute:Int?=null,val frequency:Int=0,val duration:Int=0,val order:Int=0):java.io.Serializable
fun frequencyLabel(days:String)=when(days){"1,2,3,4,5,6,7"->"Todos os dias";"1,2,3,4,5"->"Dias úteis";else->"${days.split(',').size} dia(s) por semana"}
fun filterTemplates(filter:CatalogFilter):List<HabitTemplate>{
    val terms=searchKey(filter.query).split(' ').filter{it.isNotBlank()}
    val found=habitTemplates.filter{t->
        val hay=searchKey(listOf(t.name,t.narrativeName,t.description,t.category,t.tags.joinToString(" ")).joinToString(" "))
        terms.all{it in hay}&&(filter.category==null||t.category==filter.category)&&(filter.difficulty==null||t.difficulty==filter.difficulty)&&(filter.type==null||t.type==filter.type)&&(filter.attribute==null||t.attribute==filter.attribute)&&
        when(filter.frequency){1->t.days.split(',').size==7;2->t.days.split(',').size in 2..6;3->t.days.split(',').size==1;else->true}&&
        when(filter.duration){1->t.estimatedMinutes<=15;2->t.estimatedMinutes in 16..30;3->t.estimatedMinutes>30;else->true}
    }
    return when(filter.order){1->found.sortedWith(compareBy({searchKey(it.category)},{searchKey(it.name)}));2->found.sortedWith(compareBy({it.estimatedMinutes},{searchKey(it.name)}));3->found.sortedWith(compareBy({it.difficulty},{searchKey(it.name)}));else->found.sortedBy{searchKey(it.name)}}
}
fun matchingHabit(habits:List<Habit>,candidate:Habit)=habits.firstOrNull{!it.deleted&&(candidate.templateId.isNotBlank()&&it.templateId==candidate.templateId||searchKey(it.name)==searchKey(candidate.name))}
fun recommendations(interests:Set<String>,dismissed:Set<String>,habits:List<Habit>)=habitTemplates.filter{it.category in interests && it.id !in dismissed && matchingHabit(habits,it.instantiate())==null}.take(4)
data class RoutineChoice(val templateId:String,val target:Int?=null,val xp:Int?=null){fun instantiate(position:Int=0):Habit{val t=habitTemplates.first{it.id==templateId};return t.instantiate(position).copy(target=target?:t.target,xp=xp?:t.xp)}}
data class RoutineTemplate(val id:String,val name:String,val description:String,val choices:List<RoutineChoice>,val time:String="08:00")
private fun choices(vararg ids:String)=ids.map{RoutineChoice(it)}
val routineTemplates=listOf(
    RoutineTemplate("aurora","Ritual da Aurora","Um começo possível, com organização e uma pausa de leitura.",choices("make_bed","plan_day","stretch")+RoutineChoice("leisure_read",15)),
    RoutineTemplate("knowledge","Ritual do Conhecimento","Estudo, revisão e prática, sem recompensas extras pelo conjunto.",listOf(RoutineChoice("study_subject",60,60))+choices("review_notes","solve_questions","summary")),
    RoutineTemplate("discipline","Ritual da Disciplina","Cuide do ambiente e escolha o próximo passo.",choices("organize_room","review_tasks","resolve_task","plan_tomorrow")),
    RoutineTemplate("warrior","Ritual do Guerreiro","Adapte esforço e recuperação às suas condições.",choices("walk","home_exercise","mobility","rest_break")),
    RoutineTemplate("rest","Ritual do Descanso","Um convite ao descanso. Selecione apenas o que acolhe seu dia.",choices("journal","rest_space","relax","sleep_routine"),"20:00"),
    RoutineTemplate("artisan","Ritual do Artífice","Um espaço para criar, experimentar e reconhecer avanços.",choices("personal_project","draw","ideas","review_project"))
)
