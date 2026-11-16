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
import androidx.compose.ui.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.*
import androidx.lifecycle.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import kotlinx.coroutines.*

class MainActivity:ComponentActivity(){override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);enableEdgeToEdge();setContent{Ascend()}}}
@Composable fun Ascend(){
    val app=LocalContext.current.applicationContext as android.app.Application
    val vm:GameViewModel=viewModel(factory=remember(app){ViewModelProvider.AndroidViewModelFactory(app)})
    val theme by vm.appearance.collectAsStateWithLifecycle()
    val reduced by vm.reducedMotion.collectAsStateWithLifecycle()
    val minimal by vm.minimal.collectAsStateWithLifecycle()
    AscendTheme(theme,reduced,minimal){AscendContent(vm)}
}
@Composable fun AscendContent(vm:GameViewModel){
    val appearance=LocalAppearance.current
    val view=androidx.compose.ui.platform.LocalView.current
    val activity=LocalContext.current as? android.app.Activity
    SideEffect{activity?.window?.let{window->androidx.core.view.WindowCompat.getInsetsController(window,view).apply{isAppearanceLightStatusBars=!appearance.dark;isAppearanceLightNavigationBars=!appearance.dark}}}
    val s by vm.state.collectAsStateWithLifecycle();val error by vm.error.collectAsStateWithLifecycle();val relaxed by vm.relaxed.collectAsStateWithLifecycle();val bosses by vm.bosses.collectAsStateWithLifecycle()
    val nav=rememberNavController();val entry by nav.currentBackStackEntryAsState();val route=entry?.destination?.route?:"journey";val context=LocalContext.current
    var habitEditor by remember{mutableStateOf<Habit?>(null)};var progress by remember{mutableStateOf<Pair<Habit,HabitRecord?>?>(null)};var focus by remember{mutableStateOf<Habit?>(null)};var levelup by remember{mutableStateOf<GameEvent?>(null)}
    var welcomeBackup by remember{mutableStateOf<GameState?>(null)}
    var readingBackup by remember{mutableStateOf(false)}
    val welcomeRestore=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri->if(uri!=null)vm.run{readingBackup=true;try{welcomeBackup=readLocalBackup(context,uri)}finally{readingBackup=false}}}
    val snack=remember{SnackbarHostState()}
    val permission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){granted->if(!granted)vm.error.value="As notificações estão desativadas pelo Android. Você pode ativá-las nas configurações do aparelho."}
    LaunchedEffect(Unit){vm.repo.events.collect{event->if(event.level>0)levelup=event else {snack.currentSnackbarData?.dismiss();launch{snack.showSnackbar(if(event.xp>0)"+${event.xp} XP · ${event.message}" else event.message,withDismissAction=true,duration=SnackbarDuration.Short)}}}}
    LaunchedEffect(s?.habits,s?.restDays){s?.let{Reminders.sync(context,it.habits,it.restDates)}}
    val lifecycle=androidx.lifecycle.compose.LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle){val observer=LifecycleEventObserver{_,event->if(event==Lifecycle.Event.ON_RESUME)vm.run{vm.repo.refresh()}};lifecycle.addObserver(observer);onDispose{lifecycle.removeObserver(observer)}}
    val addHabit={habitEditor=Habit(position=s?.habits?.size?:0)}
    Surface(Modifier.fillMaxSize(),color=Night){
        when{
            s==null->Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){Column(horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(16.dp)){Eyebrow("ASCEND");CircularProgressIndicator(color=Gold);Text("Abrindo as crônicas…",color=Muted)}}
            s!!.player==null->Box(Modifier.safeDrawingPadding()){Onboarding(onRestore={if(!readingBackup)welcomeRestore.launch(arrayOf("application/json","text/plain","application/octet-stream"))}){p,hs->vm.run{vm.repo.create(p,hs)}}}
            else->{val state=s!!;Scaffold(snackbarHost={SnackbarHost(snack){data->Snackbar(data,modifier=Modifier.padding(12.dp).border(1.dp,OldBronze,androidx.compose.foundation.shape.CutCornerShape(6.dp)),shape=androidx.compose.foundation.shape.CutCornerShape(6.dp),containerColor=Surface,contentColor=Ink,dismissActionContentColor=Gold)}},bottomBar={NavigationBar(modifier=Modifier.border(1.dp,OldBronze.copy(alpha=.24f)),containerColor=Night,tonalElevation=0.dp){val routes=listOf("journey","habits","missions","world","character");val names=listOf("Jornada","Hábitos","Missões","Mundo","Herói");val icons=listOf(Icons.Default.AutoAwesome,Icons.Default.Checklist,Icons.Default.OutlinedFlag,Icons.Default.Public,Icons.Default.PersonOutline);routes.forEachIndexed{i,r->NavigationBarItem(selected=route==r,onClick={nav.navigate(r){popUpTo("journey"){saveState=true};launchSingleTop=true;restoreState=true}},icon={Column(horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(4.dp)){Icon(icons[i],names[i],Modifier.size(22.dp));Box(Modifier.width(22.dp).height(1.dp).background(if(route==r)Gold else androidx.compose.ui.graphics.Color.Transparent))}},label={Text(names[i],modifier=Modifier.requiredWidth(56.dp),fontSize=10.sp,maxLines=1,textAlign=androidx.compose.ui.text.style.TextAlign.Center,overflow=androidx.compose.ui.text.style.TextOverflow.Ellipsis)},colors=NavigationBarItemDefaults.colors(selectedIconColor=Gold,selectedTextColor=Gold,indicatorColor=androidx.compose.ui.graphics.Color.Transparent,unselectedIconColor=Muted,unselectedTextColor=Muted))}}}){padding->
                Column(Modifier.padding(padding)){
                    if(route=="settings"||route=="journal")Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){IconButton({nav.popBackStack()}){Icon(Icons.AutoMirrored.Filled.ArrowBack,"Voltar")};Text(if(route=="settings")"Configurações" else "Diário",color=Muted,fontSize=14.sp)}
                    NavHost(nav,startDestination="journey",enterTransition={fadeIn(tween(if(appearance.reducedMotion)0 else 200))},exitTransition={fadeOut(tween(if(appearance.reducedMotion)0 else 150))}){
                        composable("journey"){Journey(state,{h,r->progress=h to r},{habitEditor=it},{focus=it},addHabit,{nav.navigate("journal")},vm,relaxed)}
                        composable("habits"){HabitsScreen(state,vm,addHabit,{habitEditor=it},{h,r->progress=h to r},{focus=it})}
                        composable("missions"){MissionsScreen(state,vm,bosses)}
                        composable("world"){WorldScreen(state,vm)}
                        composable("character"){CharacterScreen(state,vm,{nav.navigate("settings")},{nav.navigate("journal")})}
                        composable("journal"){JournalScreen(state,vm)}
                        composable("settings"){SettingsScreen(state,vm,relaxed)}
                    }
                }
            }}
        }
    }
    habitEditor?.let{h->HabitEditor(h,{habitEditor=null},{updated->vm.run{vm.repo.habit(updated);if(updated.reminder&&Build.VERSION.SDK_INT>=33)permission.launch(Manifest.permission.POST_NOTIFICATIONS);habitEditor=null}})}
    progress?.let{(h,r)->ProgressEditor(h,r,{progress=null},{date,amount,correction->vm.run{vm.repo.progress(h,date,amount,correction);progress=null}})}
    focus?.let{h->var minutes by remember(h.id){mutableStateOf(h.target.coerceAtMost(1440).toString())};AlertDialog(onDismissRequest={focus=null},title={Text("Foco · ${h.name}")},text={Column(verticalArrangement=Arrangement.spacedBy(12.dp)){Text("Reserve um intervalo. O tempo será registrado no mesmo histórico do hábito.",color=Muted);FormField(minutes,{minutes=it},"Duração em minutos",true)}},confirmButton={TextButton({vm.run{vm.repo.startFocus(h,minutes.toIntOrNull()?:0);focus=null;nav.navigate("journey"){launchSingleTop=true}}}){Text("Iniciar")}},dismissButton={TextButton({focus=null}){Text("Cancelar")}})}
    welcomeBackup?.let{backup->AlertDialog(onDismissRequest={welcomeBackup=null},title={Text("Retomar sua jornada?")},text={Text("${backup.player?.name} · nível ${backup.level}\n${backup.habits.size} hábitos · ${backup.xp} XP\n\nSeus dados serão restaurados neste aparelho.")},confirmButton={TextButton({vm.run{vm.repo.importState(backup);welcomeBackup=null}}){Text("Restaurar")}},dismissButton={TextButton({welcomeBackup=null}){Text("Cancelar")}})}
    if(readingBackup)AlertDialog(onDismissRequest={},title={Text("Lendo seu backup")},text={CircularProgressIndicator()},confirmButton={})
    error?.let{AlertDialog(onDismissRequest={vm.error.value=null},title={Text("Não foi possível concluir")},text={Text(it)},confirmButton={TextButton({vm.error.value=null}){Text("Entendi")}})}
    levelup?.let{event->LevelUp(event,{levelup=null})}
}
@Composable fun LevelUp(event:GameEvent,onClose:()->Unit){
    val reduced=LocalAppearance.current.reducedMotion
    val scale=if(reduced)1f else {val transition=rememberInfiniteTransition(label="chama");val animated by transition.animateFloat(.95f,1.05f,infiniteRepeatable(tween(1400),RepeatMode.Reverse),label="brilho");animated}
    AlertDialog(onDismissRequest=onClose,containerColor=Surface,title={Column(horizontalAlignment=Alignment.CenterHorizontally,modifier=Modifier.fillMaxWidth()){Eyebrow("A chama se fortalece");RuneSeal(4,Gold,Modifier.size(78.dp).padding(8.dp).graphicsLayer{scaleX=if(reduced)1f else scale;scaleY=if(reduced)1f else scale});Text("Nível ${event.level}",style=MaterialTheme.typography.headlineLarge,color=Gold)}},text={Column(verticalArrangement=Arrangement.spacedBy(16.dp)){Text("+${event.xp} XP · ◈ 10 por nível alcançado",color=Blue);Text("“O sino ecoou um pouco mais longe. Agora sabemos que há caminhos além da névoa.”",fontFamily=androidx.compose.ui.text.font.FontFamily.Serif,fontSize=18.sp);Text("Consulte os novos títulos e regiões disponíveis no perfil e no mapa.",color=Muted,fontSize=12.sp)}},confirmButton={Action("Continuar a jornada",onClose)})
}
@Composable fun SettingsScreen(s:GameState,vm:GameViewModel,relaxed:Boolean){
    val context=LocalContext.current;var pending by remember{mutableStateOf<GameState?>(null)};var busy by remember{mutableStateOf(false)}
    val export=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")){uri->if(uri!=null)vm.run{busy=true;try{withContext(Dispatchers.IO){context.contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use{it.write(Backup.encode(s))}?:error("Não foi possível abrir o arquivo.")};vm.repo.events.emit(GameEvent("Backup exportado"))}finally{busy=false}}}
    val importBackup=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri->if(uri!=null)vm.run{busy=true;try{pending=readLocalBackup(context,uri)}finally{busy=false}}}
    val appearance=LocalAppearance.current
    Page{Title("Configurações","Seu progresso fica com você.");
        Panel{Eyebrow("Aparência e acessibilidade");Text("Tema visual",color=Muted);val theme by vm.appearance.collectAsStateWithLifecycle();ChipRow(listOf("Escuro","Claro","Sistema"),listOf("dark","light","system").indexOf(theme).coerceAtLeast(0),{vm.theme(listOf("dark","light","system")[it])});Toggle("Reduzir animações",appearance.reducedMotion,{vm.setting("reducedMotion",it)});Toggle("Modo minimalista",appearance.minimal,{vm.setting("minimal",it)});Text("O modo minimalista simplifica a Jornada e mantém seus hábitos e recompensas.",color=Muted,fontSize=12.sp)};
        Panel{Eyebrow("Ritmo de jornada");Toggle("Modo livre de cobrança de frequência",relaxed,{vm.setting("relaxed",it)});Text(if(relaxed)"A jornada oferece hábitos ativos todos os dias, sem cobrança por atraso. A programação original é preservada no calendário." else "Dias não programados são descanso. Nenhuma ausência remove XP, níveis ou conquistas.",color=Muted,fontSize=13.sp);Text("Para descanso temporário, pause o hábito no Livro de hábitos. Edite seus dias para reorganizar a semana.",color=Muted,fontSize=12.sp)};Panel(accent=Blue){Eyebrow("Backup local versionado",Blue);Text("Exporte todas as crônicas, hábitos, recompensas, compras e páginas do diário. A importação valida o arquivo antes de substituir os dados.",color=Muted,fontSize=13.sp);Action("Exportar backup",{export.launch("ASCEND-backup-${today()}.json")},enabled=!busy);OutlinedButton({importBackup.launch(arrayOf("application/json","text/plain","application/octet-stream"))},enabled=!busy){Text("Importar backup")};if(busy)CircularProgressIndicator();Text("Guarde seu backup em um local privado. O arquivo inclui o diário e não é criptografado. As preferências de interface e permissões do Android são configuradas neste aparelho.",color=Muted,fontSize=12.sp)}
        if(s.debt>0)Panel{Text("Compensação pendente: ${s.debt} fragmentos",color=Gold);Text("Registros corrigidos tinham recompensas já gastas. Novos ganhos compensarão essa diferença automaticamente.",color=Muted,fontSize=13.sp)}
        Panel{Eyebrow("ASCEND · versão 1.3.0");Text("Crônicas da Ascensão\nAetheris existe nos seus próximos passos.",fontFamily=androidx.compose.ui.text.font.FontFamily.Serif,fontSize=18.sp);Text("Aplicativo offline. Sem conta, anúncios, compras reais ou coleta remota de dados.",color=Muted,fontSize=12.sp)}
    }
    pending?.let{backup->AlertDialog(onDismissRequest={pending=null},title={Text("Restaurar as crônicas?")},text={Text("Personagem: ${backup.player?.name}\n${backup.habits.size} hábitos · ${backup.records.size} registros\n\nOs dados atuais deste aparelho serão substituídos. Exporte um backup antes se desejar preservá-los.")},confirmButton={TextButton({vm.run{vm.repo.importState(backup);pending=null}}){Text("Restaurar")}},dismissButton={TextButton({pending=null}){Text("Cancelar")}})}
}
