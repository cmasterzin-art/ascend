package br.com.ascend.aetheris

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import kotlinx.coroutines.*

class MainActivity:ComponentActivity(){var widgetHabit by mutableStateOf<String?>(null);var widgetJourney by mutableStateOf(false)
 override fun onNewIntent(intent:android.content.Intent){super.onNewIntent(intent);setIntent(intent);widgetHabit=intent.getStringExtra("habit");widgetJourney=intent.getBooleanExtra("widget_journey",false)}
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);widgetHabit=intent.getStringExtra("habit");widgetJourney=intent.getBooleanExtra("widget_journey",false);enableEdgeToEdge();setContent{Ascend()}}}
@Composable fun Ascend(){
    val app=LocalContext.current.applicationContext as android.app.Application
    val vm:GameViewModel=viewModel(factory=remember(app){ViewModelProvider.AndroidViewModelFactory(app)})
    val theme by vm.appearance.collectAsStateWithLifecycle()
    val reduced by vm.reducedMotion.collectAsStateWithLifecycle()
    val minimal by vm.minimal.collectAsStateWithLifecycle()
    val narrative by vm.narrativeNames.collectAsStateWithLifecycle()
    AscendTheme(theme,reduced,minimal){CompositionLocalProvider(LocalNarrativeNames provides narrative){AscendContent(vm)}}
}
@Composable fun AscendContent(vm:GameViewModel){
    val appearance=LocalAppearance.current
    val view=androidx.compose.ui.platform.LocalView.current
    val activity=LocalContext.current as? android.app.Activity
    SideEffect{activity?.window?.let{window->androidx.core.view.WindowCompat.getInsetsController(window,view).apply{isAppearanceLightStatusBars=!appearance.dark;isAppearanceLightNavigationBars=!appearance.dark}}}
    val s by vm.state.collectAsStateWithLifecycle();val error by vm.error.collectAsStateWithLifecycle();val relaxed by vm.relaxed.collectAsStateWithLifecycle();val bosses by vm.bosses.collectAsStateWithLifecycle()
    val nav=rememberNavController();val entry by nav.currentBackStackEntryAsState();val route=entry?.destination?.route?:"journey";val context=LocalContext.current
    var habitEditor by rememberSaveable(stateSaver=HabitEditorState){mutableStateOf<Habit?>(null)};var progress by rememberSaveable(stateSaver=RecordEditorState){mutableStateOf<Pair<Habit,HabitRecord?>?>(null)};var focus by rememberSaveable(stateSaver=HabitEditorState){mutableStateOf<Habit?>(null)};var pathEditor by remember{mutableStateOf<PersonalPath?>(null)};var pathFocus by remember{mutableStateOf<Triple<String,String,String>?>(null)};var levelup by remember{mutableStateOf<GameEvent?>(null)}
    val widgetHabit=(activity as? MainActivity)?.widgetHabit
    val widgetJourney=(activity as? MainActivity)?.widgetJourney?:false
    LaunchedEffect(widgetHabit,widgetJourney,s?.player){
        if(s?.player!=null&&(widgetHabit!=null||widgetJourney)){
            progress=null
            val requestDate=(activity as? MainActivity)?.intent?.getStringExtra("date")
            if(widgetHabit!=null){
                val h=s?.let{widgetRegistrationHabit(it,widgetHabit,requestDate)}
                if(h!=null)progress=h to s?.record(h.id) else vm.error.value=if(requestDate!=null&&requestDate!=today())"O dia mudou. Escolha um ritual na Jornada." else "Este ritual mudou ou não está programado para hoje. Confira sua Jornada."
            }
            nav.navigate("journey"){launchSingleTop=true}
            (activity as? MainActivity)?.let{it.widgetHabit=null;it.widgetJourney=false;listOf("habit","widget_journey","date").forEach(it.intent::removeExtra)}
        }
    }
    var welcomeBackup by remember{mutableStateOf<GameState?>(null)}
    var readingBackup by remember{mutableStateOf(false)}
    val welcomeRestore=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri->if(uri!=null)vm.run{readingBackup=true;try{welcomeBackup=readLocalBackup(context,uri)}finally{readingBackup=false}}}
    val audio=remember{AetherisAudio(context)}
    val soundEnabled by vm.soundEnabled.collectAsStateWithLifecycle();val soundVolume by vm.soundVolume.collectAsStateWithLifecycle()
    SideEffect{audio.enabled=soundEnabled;audio.volume=soundVolume}
    DisposableEffect(audio){onDispose{audio.close()}}
    LaunchedEffect(route){if(route=="weekly")audio.play("page")}
    val snack=remember{SnackbarHostState()}
    val permission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){granted->if(!granted)vm.error.value="As notificações estão desativadas pelo Android. Você pode ativá-las nas configurações do aparelho."}
    LaunchedEffect(Unit){vm.repo.events.collect{event->effectFor(event)?.let{audio.play(it)};if(event.highlights.isNotEmpty())levelup=event else {snack.currentSnackbarData?.dismiss();launch{snack.showSnackbar(rewardSummary(event).ifBlank{event.message},withDismissAction=true,duration=SnackbarDuration.Short)}}}}
    LaunchedEffect(s?.habits,s?.restDays,s?.plans){s?.let{Reminders.sync(context,it.habits,it.restDates,it.plans)}}
    val lifecycle=androidx.lifecycle.compose.LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle){val observer=LifecycleEventObserver{_,event->if(event==Lifecycle.Event.ON_RESUME)vm.run{vm.repo.refresh();vm.repo.reconcileFocusTimer()}};lifecycle.addObserver(observer);onDispose{lifecycle.removeObserver(observer)}}
    val addHabit={habitEditor=Habit(position=s?.habits?.size?:0)}
    Surface(Modifier.fillMaxSize(),color=Night){
        when{
            s==null->Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){Column(horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(16.dp)){Eyebrow("ASCEND");CircularProgressIndicator(color=Gold);Text("Abrindo as crônicas…",color=Muted)}}
            s!!.player==null->Box(Modifier.safeDrawingPadding()){Onboarding(onRestore={if(!readingBackup)welcomeRestore.launch(arrayOf("application/json","text/plain","application/octet-stream"))}){p,hs->vm.run{vm.repo.create(p,hs)}}}
            else->{val state=s!!;val nb=LocalNorthborn.current;Scaffold(containerColor=nb.canvas,snackbarHost={SnackbarHost(snack){data->Snackbar(data,modifier=Modifier.padding(12.dp).border(1.dp,nb.hairlineStrong,NorthbornShape.md),shape=NorthbornShape.md,containerColor=nb.surfaceElevated,contentColor=nb.inkPrimary,dismissActionContentColor=nb.accentBronze)}},bottomBar={AetherisNavigation(if(route=="memories"&&nav.previousBackStackEntry?.destination?.route=="world")"world" else route){r->nav.navigate(r){popUpTo("journey"){saveState=true};launchSingleTop=true;restoreState=true}}}){padding->
                Column(Modifier.padding(padding)){
                    if(route=="settings"||route=="journal"||route=="grimoire"||route=="weekly"||route=="memories"||route=="portrait"||route=="forge"||route=="heraldry"||route=="codex_runico"||route=="planner"||route=="analysis"||route=="focus_history"||route=="paths"||route=="inbox"||route=="pending"||route=="mimir"||route.startsWith("mimir_")||route.startsWith("path/")||route.startsWith("region/")||route.startsWith("location/")||route.startsWith("expedition/")||route.startsWith("odin")||route.startsWith("time_forge")||route.startsWith("council_nine")){
                        val topTitle = if(route=="settings")"Configurações" else if(route.startsWith("odin"))"Mesa de Odin" else if(route.startsWith("time_forge"))"Forja do Tempo" else if(route.startsWith("council_nine"))"Conselho dos Nove" else if(route=="forge")"A Forja de Darian" else if(route=="heraldry")"Heráldica Rúnica" else if(route=="codex_runico")"Codex Rúnico" else if(route=="grimoire"||route in listOf("planner","analysis","focus_history"))"Hábitos" else if(route=="paths"||route in listOf("inbox","pending"))"Jornada" else if(route.startsWith("path/"))"Caminhos" else if(route=="mimir")"Crônicas · Biblioteca de Mímir" else if(route.startsWith("mimir_notebook/"))"Biblioteca de Mímir" else if(route.startsWith("mimir_page/"))"Caderno" else if(route.startsWith("mimir_review"))"Runas da Memória" else if(route=="weekly")"Crônicas" else if(route=="memories"&&nav.previousBackStackEntry?.destination?.route=="world")"Crônicas" else if(route=="memories"||route=="portrait")"Herói" else if(route.startsWith("region/"))"Atlas" else if(route.startsWith("location/"))"Região" else if(route.startsWith("expedition/"))"Local" else "Diário"
                        NorthbornTopBar(title = topTitle, onBack = { nav.popBackStack() })
                    }
                    NavHost(
                        nav, startDestination = "journey",
                        enterTransition = { craftScreenEnter(appearance.reducedMotion) },
                        popEnterTransition = { craftScreenEnter(appearance.reducedMotion, back = true) },
                        exitTransition = { if (appearance.reducedMotion) ExitTransition.None else fadeOut(tween(150)) }
                    ) {
                        composable("journey"){Journey(state,{h,r->progress=h to r},{habitEditor=it},{focus=it},addHabit,{nav.navigate("journal")},vm,relaxed,{nav.navigate("planner")},{nav.navigate("analysis")},{nav.navigate("focus_history")},{nav.navigate("portrait")},{nav.navigate("missions")},{nav.navigate("region/${regionLevels.indexOfLast{it<=state.level}.coerceAtLeast(0)}")},{nav.navigate("paths")},{nav.navigate("inbox")},{nav.navigate("pending")},{nav.navigate("path/$it")},{nav.navigate("mimir_review")},{nav.navigate("odin")},{nav.navigate("time_forge")},{nav.navigate("council_nine")})}
                        composable("odin"){OdinCouncilScreen(state,vm,0,{nav.navigate("planner")},{nav.navigate("paths")},{nav.navigate("council_nine")},{nav.navigate("time_forge")},{nav.navigate("mimir_review")})}
                        composable("odin/{tab}"){back->OdinCouncilScreen(state,vm,back.arguments?.getString("tab")?.toIntOrNull()?:0,{nav.navigate("planner")},{nav.navigate("paths")},{nav.navigate("council_nine")},{nav.navigate("time_forge")},{nav.navigate("mimir_review")})}
                        composable("time_forge"){TimeForgeScreen(state,vm,false,"",{nav.navigate("focus_history")})}
                        composable("time_forge/{blockId}"){back->TimeForgeScreen(state,vm,false,back.arguments?.getString("blockId")?:"",{nav.navigate("focus_history")})}
                        composable("council_nine"){CouncilOfNineScreen(state,vm,0)}
                        composable("council_nine/{tab}"){back->CouncilOfNineScreen(state,vm,back.arguments?.getString("tab")?.toIntOrNull()?:0)}
                        composable("paths"){PathsScreen(state,vm,{nav.navigate("path/$it")},{pathEditor=PersonalPath(position=state.paths.size)},{pathEditor=it})}
                        composable("path/{id}"){back->PathDetailScreen(back.arguments?.getString("id")?:"",state,vm,{nav.popBackStack()},{pathEditor=it},{pid,tid,lbl->pathFocus=Triple(pid,tid,lbl)},{nav.navigate("mimir_notebook/$it")},{nav.navigate("mimir_page/$it")})}
                        composable("inbox"){InboxScreen(state,vm,{nav.navigate("path/$it")})}
                        composable("pending"){PendingCenterScreen(state,vm,{nav.navigate("path/$it")})}
                        composable("habits"){HabitsScreen(state,vm,addHabit,{habitEditor=it},{h,r->progress=h to r},{focus=it},{nav.navigate("grimoire")},{nav.navigate("planner")},{nav.navigate("analysis")},{nav.navigate("focus_history")})}
                        composable("grimoire"){GrimoireScreen(state,vm,{habitEditor=it},{if(Build.VERSION.SDK_INT>=33)permission.launch(Manifest.permission.POST_NOTIFICATIONS)})}
                        composable("missions"){MissionsScreen(state,vm,bosses)}
                        composable("world"){WorldScreen(state,vm,{nav.navigate("weekly")},{nav.navigate("memories")},{nav.navigate("region/$it")},{nav.navigate("mimir")},{nav.navigate("forge")},{nav.navigate("codex_runico")})}
                        composable("forge"){ForgeScreen(state,vm,{nav.popBackStack()})}
                        composable("heraldry"){HeraldryEditorScreen(state,vm,{nav.navigate("codex_runico")},{nav.popBackStack()})}
                        composable("codex_runico"){RunicCodexScreen({nav.navigate("heraldry")},{nav.popBackStack()},vm=vm)}
                        composable("mimir"){MimirLibraryScreen(state,vm,{nav.navigate("mimir_notebook/$it")},{nav.navigate("mimir_page/$it")},{nbId->if(nbId.isBlank())nav.navigate("mimir_review") else nav.navigate("mimir_review/$nbId")},{nav.navigate("codex_runico")})}
                        composable("mimir_notebook/{id}"){back->NotebookDetailScreen(back.arguments?.getString("id")?:"",state,vm,{nav.popBackStack()},{nav.navigate("mimir_page/$it")},{nbId->nav.navigate("mimir_review/$nbId")})}
                        composable("mimir_page/{id}"){back->PageDetailScreen(back.arguments?.getString("id")?:"",state,vm,{nav.popBackStack()},{nav.navigate("mimir_page/$it")})}
                        composable("mimir_review"){FlashcardReviewScreen("",state,vm,{nav.popBackStack()})}
                        composable("mimir_review/{notebookId}"){back->FlashcardReviewScreen(back.arguments?.getString("notebookId")?:"",state,vm,{nav.popBackStack()})}
                        composable("character"){CharacterScreen(state,vm,{nav.navigate("settings")},{nav.navigate("journal")},{nav.navigate("memories")},{nav.navigate("portrait")},{nav.navigate("mimir")},{nav.navigate("mimir_review")},{nav.navigate("paths")},{nav.navigate("forge")},{nav.navigate("heraldry")},{nav.navigate("codex_runico")})}
                        composable("region/{index}"){back->val index=back.arguments?.getString("index")?.toIntOrNull()?.takeIf{it in regions.indices}?:0;RegionScreen(index,state,{nav.navigate("location/$it")})}
                        composable("location/{id}"){back->expeditionById(back.arguments?.getString("id")?:"")?.let{e->LocationScreen(e,state,{nav.navigate("expedition/${e.id}")})}}
                        composable("expedition/{id}"){back->expeditionById(back.arguments?.getString("id")?:"")?.let{e->ExpeditionScreen(e,state,vm)}}
                        composable("planner"){PlannerScreen(state,vm,{h,r->progress=h to r},{nav.navigate("path/$it")},{nav.navigate("inbox")},{nav.navigate("pending")},{pid,tid,lbl->pathFocus=Triple(pid,tid,lbl)},{nav.navigate("odin")},{nav.navigate("odin/3")},{blk->nav.navigate("time_forge/${blk.id}")})}
                        composable("analysis"){AnalysisScreen(state)}
                        composable("focus_history"){FocusHistoryScreen(state)}
                        composable("weekly"){WeeklyScreen(state)}
                        composable("memories"){MemoryHall(state)}
                        composable("portrait"){PortraitScreen(state,{nav.navigate("heraldry")})}
                        composable("journal"){JournalScreen(state,vm)}
                        composable("settings"){SettingsScreen(state,vm,relaxed)}
                    }
                }
            }}
        }
    }
    habitEditor?.let{h->HabitEditor(h,{habitEditor=null},{updated->vm.run{if(updated.templateId.isNotBlank()&&s?.habits?.none{it.id==updated.id}==true)vm.repo.addCatalogHabit(updated) else vm.repo.habit(updated);if(updated.reminder&&Build.VERSION.SDK_INT>=33)permission.launch(Manifest.permission.POST_NOTIFICATIONS);habitEditor=null}})}
    pathEditor?.let{p->PathEditorDialog(p,{pathEditor=null},s?.pathTasks.orEmpty().filter{it.pathId==p.id&&!it.completed&&!it.archived}.map{it.id}.toSet()){updated,confirmed->vm.run{vm.repo.savePath(updated,confirmed);pathEditor=null}}}
    progress?.let{(h,r)->ProgressEditor(h,r,{progress=null},{date,amount,correction->vm.run{vm.repo.progress(h,date,amount,correction);progress=null}})}
    focus?.let{h->
        var minutes by rememberSaveable(h.id){mutableStateOf(h.target.coerceAtMost(1440).toString())}
        FullForm("Foco · ${h.name}",{focus=null},{vm.run{vm.repo.startFocus(h,minutes.toIntOrNull()?:0);focus=null;nav.navigate("journey"){launchSingleTop=true}}},"Iniciar"){
            SectionHeader("Um intervalo para se concentrar","O tempo alimenta o registro único deste hábito; não há XP extra por sessão.")
            FormField(minutes,{minutes=it},"Duração em minutos",true)
            Text("De 1 a 1440 minutos. Pausas não contam como tempo efetivo. Você pode sair da tela e retomar pela Jornada.",color=Muted,fontSize=13.sp,lineHeight=22.sp)
            NorthbornButton("Cancelar",{focus=null},role=NorthbornButtonRole.Tertiary)
        }
    }
    pathFocus?.let{(pid,tid,lbl)->
        var minutes by rememberSaveable(pid,tid){mutableStateOf("25")}
        FullForm("Foco · $lbl",{pathFocus=null},{vm.run{vm.repo.startPathFocus(pid,tid,minutes.toIntOrNull()?:0);pathFocus=null;nav.navigate("journey"){launchSingleTop=true}}},"Iniciar"){
            SectionHeader("Foco em projeto ou tarefa","O tempo é contabilizado no caminho e no histórico de foco, sem conceder XP ou moedas adicionais.")
            FormField(minutes,{minutes=it},"Duração em minutos",true)
            Text("De 1 a 1440 minutos. Pausas não contam como tempo efetivo.",color=Muted,fontSize=13.sp,lineHeight=22.sp)
            NorthbornButton("Cancelar",{pathFocus=null},role=NorthbornButtonRole.Tertiary)
        }
    }
    welcomeBackup?.let{backup->AetherDialog(onDismissRequest={welcomeBackup=null},title="Retomar sua jornada?",confirmButton={SagaButton("Restaurar",{vm.run{vm.repo.importState(backup);welcomeBackup=null}})},dismissButton={TextButton({welcomeBackup=null}){Text("Cancelar")}}){Text("${backup.player?.name} · nível ${backup.level}\n${backup.habits.size} hábitos · ${backup.xp} XP\n\nSeus dados serão restaurados neste aparelho.")}}
    if(readingBackup)AetherDialog(onDismissRequest={},title="Lendo seu backup",confirmButton={}){CircularProgressIndicator(color=Gold)}
    error?.let{AetherDialog(onDismissRequest={vm.error.value=null},title="Não foi possível concluir",confirmButton={TextButton({vm.error.value=null}){Text("Entendi")}}){Text(it)}}
    levelup?.let{event->LevelUp(event,{levelup=null})}
}
@Composable fun LevelUp(event:GameEvent,onClose:()->Unit){
    val nb=LocalNorthborn.current
    AetherDialog(
        onDismissRequest = onClose,
        title = if(event.level>0)"Nível ${event.level}" else "Marcas na saga",
        kind = SurfaceKind.Ceremonial,
        seal = SealKind.Reward,
        confirmButton = { NorthbornButton("Continuar a jornada", onClose, role = NorthbornButtonRole.Ceremonial) }
    ){
        Eyebrow("O juramento foi cumprido", nb.accentBronze)
        Text(event.message, color = nb.inkSecondary)
        event.highlights.forEach { Text("✦ $it", color = nb.inkPrimary) }
        NorthbornDivider(NorthbornDividerStyle.Hairline)
        Text(rewardSummary(event).ifBlank { "Transformação registrada em Aetheris." }, color = nb.accentFjord)
    }
}
@Composable fun SettingsScreen(s:GameState,vm:GameViewModel,relaxed:Boolean){
    val context=LocalContext.current;var pending by remember{mutableStateOf<GameState?>(null)};var busy by remember{mutableStateOf(false)}
    val export=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")){uri->if(uri!=null)vm.run{busy=true;try{withContext(Dispatchers.IO){context.contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use{it.write(Backup.encode(s))}?:error("Não foi possível abrir o arquivo.")};vm.repo.events.emit(GameEvent("Backup exportado"))}finally{busy=false}}}
    val importBackup=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri->if(uri!=null)vm.run{busy=true;try{pending=readLocalBackup(context,uri)}finally{busy=false}}}
    val appearance=LocalAppearance.current
    val nb=LocalNorthborn.current
    Page{
        Title("Configurações","Seu progresso fica com você, localmente.")

        // Seção editorial aberta: Aparência e acessibilidade (sem caixa redundante)
        Column(verticalArrangement=Arrangement.spacedBy(10.dp)){
            NorthbornEditorialHeader("Aparência e acessibilidade", kicker = "Identidade visual")
            Text("Tema visual", color = nb.inkSecondary, fontSize = 13.sp)
            val theme by vm.appearance.collectAsStateWithLifecycle()
            ChipRow(listOf("Escuro","Claro","Sistema"), listOf("dark","light","system").indexOf(theme).coerceAtLeast(0), { vm.theme(listOf("dark","light","system")[it]) })
            Toggle("Reduzir animações", appearance.reducedMotion, { vm.setting("reducedMotion", it) })
            Toggle("Modo minimalista", appearance.minimal, { vm.setting("minimal", it) })
            Toggle("Exibir nomes narrativos", LocalNarrativeNames.current, { vm.setting("narrative_names", it) })
            Text("O modo minimalista simplifica texturas e mantém intactos todos os hábitos, projetos e recompensas.", color = nb.inkSecondary, fontSize = 12.sp, lineHeight = 18.sp)
        }

        NorthbornDivider(NorthbornDividerStyle.Hairline)

        // Seção editorial aberta: Ritmo de jornada
        Column(verticalArrangement=Arrangement.spacedBy(8.dp)){
            NorthbornEditorialHeader("Ritmo de jornada", kicker = "Constância")
            Toggle("Modo livre de cobrança de frequência", relaxed, { vm.setting("relaxed", it) })
            Text(
                if(relaxed)"A jornada oferece hábitos ativos todos os dias, sem cobrança por atraso. A programação original é preservada no calendário."
                else "Dias não programados são descanso. Nenhuma ausência remove XP, níveis ou conquistas.",
                color = nb.inkSecondary, fontSize = 13.sp, lineHeight = 19.sp
            )
            Text("Os widgets seguem os dias programados, mesmo no modo livre. Para descanso temporário, pause o hábito no Livro de hábitos.", color = nb.inkTertiary, fontSize = 12.sp, lineHeight = 18.sp)
        }

        NorthbornDivider(NorthbornDividerStyle.DiamondKnot)

        // Superfície documental destacada para Backup local (única área crítica enquadrada)
        NorthbornSurface(role = NorthbornSurfaceRole.Document, accent = nb.accentFjord){
            NorthbornEditorialHeader("Backup local versionado", kicker = "Preservação · v13", accent = nb.accentFjord)
            Text("Exporte todas as crônicas, hábitos, caminhos, agenda, Biblioteca de Mímir, flashcards, especializações, heráldica rúnica, conjuntos de equipamento, A Forja, Mesa de Odin, Conselho dos Nove, recompensas e páginas do diário. A importação valida o arquivo antes de substituir os dados.", color = nb.inkSecondary, fontSize = 13.sp, lineHeight = 19.sp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)){
                NorthbornButton("Exportar backup", { export.launch("ASCEND-backup-${today()}.json") }, role = NorthbornButtonRole.Primary, modifier = Modifier.weight(1f), enabled = !busy)
                NorthbornButton("Importar backup", { importBackup.launch(arrayOf("application/json","text/plain","application/octet-stream")) }, role = NorthbornButtonRole.Secondary, modifier = Modifier.weight(1f), enabled = !busy)
            }
            if(busy) CircularProgressIndicator(color = nb.accentBronze)
            Text("Guarde seu backup em um local privado. O arquivo inclui o diário e suas anotações, e não é criptografado.", color = nb.inkTertiary, fontSize = 12.sp, lineHeight = 17.sp)
        }

        NorthbornDivider(NorthbornDividerStyle.Hairline)

        // Seção editorial aberta: Vozes e efeitos
        Column(verticalArrangement=Arrangement.spacedBy(8.dp)){
            NorthbornEditorialHeader("Vozes e efeitos", kicker = "Áudio local")
            val effects by vm.soundEnabled.collectAsStateWithLifecycle()
            val volume by vm.soundVolume.collectAsStateWithLifecycle()
            Toggle("Efeitos sonoros", effects, { vm.setting("sound_enabled", it) })
            Text("Volume dos efeitos · ${(volume*100).toInt()}%", color = nb.inkSecondary, fontSize = 13.sp)
            Slider(
                volume,
                { vm.volume(it) },
                enabled = effects,
                colors = SliderDefaults.colors(
                    thumbColor = nb.accentBronze,
                    activeTrackColor = nb.accentBronze,
                    inactiveTrackColor = nb.hairlineStrong
                )
            )
            Text("Sons locais breves, sem música contínua. Respeitam o modo silencioso e o volume de mídia do Android.", color = nb.inkTertiary, fontSize = 12.sp)
        }

        if(s.debt>0){
            NorthbornSurface(role = NorthbornSurfaceRole.Informative, accent = nb.accentBronze){
                Text("Compensação pendente: ${s.debt} fragmentos", color = nb.accentBronze, fontWeight = FontWeight.SemiBold)
                Text("Registros corrigidos tinham recompensas já gastas. Novos ganhos compensarão essa diferença automaticamente.", color = nb.inkSecondary, fontSize = 13.sp)
            }
        }

        NorthbornDivider(NorthbornDividerStyle.DoubleKnot)

        // Colofão editorial da versão
        Column(Modifier.fillMaxWidth().padding(bottom = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)){
            Eyebrow("ASCEND · versão 1.7.5.9.1", nb.accentBronze)
            Text("A Reforma de Aetheris · NORTHBORN 4.0", fontFamily = SagaFont, fontSize = 18.sp, color = nb.inkPrimary)
            Text("Aetheris existe nos seus próximos passos. Aplicativo 100% offline, sem conta, anúncios ou coleta remota de dados.", color = nb.inkSecondary, fontSize = 12.5.sp, lineHeight = 18.sp)
        }
    }
    pending?.let{backup->AetherDialog(onDismissRequest={pending=null},title="Restaurar as crônicas?",confirmButton={SagaButton("Restaurar",{vm.run{vm.repo.importState(backup);pending=null}})},dismissButton={TextButton({pending=null}){Text("Cancelar")}}){Text("Personagem: ${backup.player?.name}\n${backup.habits.size} hábitos · ${backup.paths.size} caminhos · ${backup.notebooks.size} cadernos · ${backup.flashcards.size} flashcards\n\nOs dados atuais deste aparelho serão substituídos. Exporte um backup antes se desejar preservá-los.")}}
}
