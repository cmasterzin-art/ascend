package br.com.ascend.aetheris

import android.view.View
import android.widget.FrameLayout
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.*
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.*

/** Also run unchanged against 1.5.2 in an isolated worktree for real comparisons. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[28],application=AscendApp::class,qualifiers="w411dp-h891dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ProductVisualTourTest{
 @get:Rule val compose=createAndroidComposeRule<MainActivity>()
 private val app get()=RuntimeEnvironment.getApplication() as AscendApp
 private val vm get()=ViewModelProvider(compose.activity)[GameViewModel::class.java]
 private fun wait(condition:()->Boolean){compose.waitUntil(15000){Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();condition()};compose.waitForIdle();val reward=compose.onAllNodesWithText("Continuar a jornada");if(reward.fetchSemanticsNodes().isNotEmpty())reward.onFirst().performClick();val dismiss=compose.onAllNodesWithContentDescription("Dismiss");if(dismiss.fetchSemanticsNodes().isNotEmpty())dismiss.onFirst().performClick()}
 private fun save(v:View,name:String){val b=android.graphics.Bitmap.createBitmap(v.width,v.height,android.graphics.Bitmap.Config.ARGB_8888);v.draw(android.graphics.Canvas(b));val f=java.io.File("build/reports/screenshots/tour-$name.png");f.parentFile!!.mkdirs();f.outputStream().use{b.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it)};b.recycle()}
 private fun shot(name:String){compose.waitForIdle();compose.runOnIdle{save(compose.activity.window.decorView,name)}}
 private fun back(){compose.runOnIdle{compose.activity.onBackPressedDispatcher.onBackPressed()};compose.waitForIdle()}
 @After fun close(){compose.activityRule.scenario.close();app.database.close()}
 @Test fun captureActualMainScreensAndWidgetWithTheSameFixture(){
  runBlocking{app.preferences.edit{it.clear()}};wait{app.repository.state.value!=null}
  vm.run{app.repository.create(Player(name="Sigrid"),emptyList());vm.setting("reducedMotion",true);val h=Habit(id="ritual",name="Caminhar ao amanhecer",attribute=1,xp=40,coins=100);val t=Habit(id="study",name="Ler as crônicas",type=2,target=60,xp=60,proportional=true);app.repository.habit(h);app.repository.habit(t);app.repository.progress(h,today(),1);app.repository.progress(t,today(),30);app.repository.buy(shopItems.first{it.id=="traveler-helm"});app.repository.saga(Quest(name="O juramento da semana",habitIds=h.id,target=3,xp=50));app.repository.rememberNarrative(nextNarrative(app.repository.state.value!!)!!.id)}
  wait{app.repository.state.value!!.assets.any{it.kind=="memory"}&&vm.reducedMotion.value};shot("jornada")
  compose.onNodeWithTag("nav:habits").performClick();shot("habitos")
  compose.onNodeWithTag("nav:missions").performClick();shot("missoes");compose.onNodeWithText("Sagas").performScrollTo().performClick();compose.onNodeWithText("Sagas Pessoais").performScrollTo();shot("sagas")
  compose.onNodeWithTag("nav:character").performClick();shot("heroi");compose.onNodeWithText("Inventário").performClick();shot("inventario")
  compose.onNodeWithTag("nav:world").performClick();shot("atlas");compose.onNodeWithText("Crônicas").performClick();compose.onNodeWithText("Crônica da Semana").performScrollTo().performClick();shot("cronica");back();compose.onNodeWithText("Salão das Memórias").performScrollTo().performClick();shot("memorias")
  compose.runOnIdle{val v=HabitWidget.views(app,app.repository.state.value!!,true).apply(app,FrameLayout(app));v.measure(View.MeasureSpec.makeMeasureSpec(340,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(340,View.MeasureSpec.EXACTLY));v.layout(0,0,340,340);save(v,"widget")}
  compose.runOnIdle{
   compose.activity.setContent{AscendTheme("dark",reducedMotion=true,minimal=false){DesignSystemBoard16915()}}
   val v=compose.activity.window.decorView
   v.measure(View.MeasureSpec.makeMeasureSpec(860,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(1020,View.MeasureSpec.EXACTLY))
   v.layout(0,0,860,1020)
  }
  compose.waitForIdle()
  compose.runOnIdle{
   val v=compose.activity.window.decorView
   v.measure(View.MeasureSpec.makeMeasureSpec(860,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(1020,View.MeasureSpec.EXACTLY))
   v.layout(0,0,860,1020)
   val b=android.graphics.Bitmap.createBitmap(860,1020,android.graphics.Bitmap.Config.ARGB_8888)
   v.draw(android.graphics.Canvas(b))
   val f=java.io.File("build/reports/screenshots/prancha-componentes-16915.png")
   f.parentFile!!.mkdirs()
   f.outputStream().use{b.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it)}
   b.recycle()
  }
  compose.runOnIdle{
   compose.activity.setContent{AscendTheme("dark",reducedMotion=true,minimal=false){PictorialLibraryBoard16919()}}
   val v=compose.activity.window.decorView
   v.measure(View.MeasureSpec.makeMeasureSpec(920,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(1120,View.MeasureSpec.EXACTLY))
   v.layout(0,0,920,1120)
  }
  compose.waitForIdle()
  compose.runOnIdle{
   val v=compose.activity.window.decorView
   v.measure(View.MeasureSpec.makeMeasureSpec(920,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(1120,View.MeasureSpec.EXACTLY))
   v.layout(0,0,920,1120)
   val b=android.graphics.Bitmap.createBitmap(920,1120,android.graphics.Bitmap.Config.ARGB_8888)
   v.draw(android.graphics.Canvas(b))
   val f=java.io.File("build/reports/screenshots/prancha-biblioteca-16919.png")
   f.parentFile!!.mkdirs()
   f.outputStream().use{b.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it)}
   b.recycle()
  }
 }
}

@Composable private fun PictorialLibraryBoard16919(){
 Column(Modifier.fillMaxSize().background(Night).padding(22.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){
   Column(verticalArrangement=Arrangement.spacedBy(3.dp)){
    Eyebrow("ASCEND 1.6.9.19 · A BIBLIOTECA DA FORJA",Gold)
    Text("Nordic Pictorial Library — 19 Recursos Originais",fontFamily=SagaFont,fontSize=26.sp,color=Ink)
    Text("Família I (6 materiais) · Família II (7 placas e controles UI) · Família III (4 atlas ornamentais) · Família IV (2 placas de widget).",color=Muted,fontSize=12.sp)
   }
   RuneSeal(4,Gold,Modifier.size(44.dp))
  }
  NorthDivider()
  Eyebrow("FAMÍLIA I — MATERIAIS ORIGINAIS (512×512 WEBP)",Gold)
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){
   listOf(
    ForgeMaterial.IronNordic to "Ferro Nórdico",
    ForgeMaterial.BronzeAncient to "Bronze Antigo",
    ForgeMaterial.SilverOxidized to "Prata Oxidada",
    ForgeMaterial.SlateVolcanic to "Ardósia Vulcânica",
    ForgeMaterial.LeatherCarved to "Couro Entalhado",
    ForgeMaterial.VellumAged to "Velino Antigo"
   ).forEach{(mat,label)->
    Column(Modifier.weight(1f),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(4.dp)){
     Image(androidx.compose.ui.res.painterResource(mat.resId),null,Modifier.fillMaxWidth().height(74.dp).border(1.dp,ColdIron.copy(alpha=.45f)),contentScale=androidx.compose.ui.layout.ContentScale.Crop)
     Text(label,color=Ink,fontSize=10.sp)
    }
   }
  }
  NorthDivider(silver=true)
  Row(Modifier.fillMaxWidth().weight(1f),horizontalArrangement=Arrangement.spacedBy(18.dp)){
   Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(11.dp)){
    Eyebrow("FAMÍLIA II — COMPONENTES DE INTERFACE (NOVE FATIAS)",Gold)
    SagaButton("Placa Primária · Bronze Forjado (ui_plate_primary)",{},Modifier.fillMaxWidth())
    SagaButton("Placa Secundária · Ferro Enegrecido",{},Modifier.fillMaxWidth(),secondary=true)
    SagaButton("Placa Cerimonial · Juramento",{},Modifier.fillMaxWidth(),ceremonial=true)
    FormField("Inscrição em Basalto (ui_field_inscription)",{},"Campo de Inscrição")
    ChipRow(listOf("Tabuleta Ativa","Ardósia","Bronze"),0,{})
    Toggle("Superfície de Controle (ui_control_surface)",true,{})
    Box(Modifier.fillMaxWidth().height(118.dp).pictorialFrame(R.drawable.ui_frame_dialog,capXRatio=.18f,capYRatio=.18f,alpha=.95f).padding(16.dp)){
     Column(verticalArrangement=Arrangement.spacedBy(4.dp)){
      Eyebrow("MOLDURA DE DIÁLOGO (UI_FRAME_DIALOG)",Gold)
      Text("Placa de basalto vulcânico com cantoneiras em L de bronze Urnes e rebites forjados.",color=Ink,fontSize=12.sp)
     }
    }
   }
   Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(11.dp)){
    Eyebrow("FAMÍLIA III — ORNAMENTAÇÃO (DIVISORES, MOLDURAS, SELOS, TRILHOS)",Gold)
    AetherSurface(SurfaceKind.Operational){
     Text("Molduras de Artefatos por Raridade (orn_frames_rarity · 4 graus)",color=Muted,fontSize=11.sp)
     Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){
      listOf("Comum" to Muted,"Raro" to Blue,"Épico" to Violet,"Lendário" to Gold).forEachIndexed{i,(lbl,col)->
       Column(horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(4.dp)){
        Box(Modifier.size(64.dp)){
         ArtTile(R.drawable.relic_atlas,i,3,Modifier.matchParentSize().padding(6.dp))
         ArtifactFrame(Modifier.matchParentSize(),rarity=i)
        }
        Text(lbl,color=col,fontSize=10.sp)
       }
      }
     }
    }
    AetherSurface(SurfaceKind.Heroic){
     Text("Medalhões Urnes e Selos Rúnicos (orn_medallions_seals · 6 células)",color=Muted,fontSize=11.sp)
     Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){
      (0..5).forEach{i->AtlasCell(R.drawable.orn_medallions_seals,i,3,2,Modifier.size(46.dp))}
     }
     Text("Trilho Esculpido em Pedra e Metal (orn_track_progress)",color=Muted,fontSize=11.sp)
     Meter(0.72f,Gold)
    }
    Eyebrow("FAMÍLIA IV — PLACAS PICTÓRICAS DE WIDGETS",Gold)
    Box(Modifier.fillMaxWidth().height(64.dp).background(CardColor).pictorialFrame(R.drawable.wid_plate_compact,capXRatio=.16f,capYRatio=.28f,alpha=1f).padding(horizontal=16.dp,vertical=10.dp),contentAlignment=Alignment.CenterStart){
     Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){
      Text("wid_plate_compact · Placa Compacta",color=Ink,fontFamily=SagaFont,fontSize=13.sp)
      Text("628×140 WebP",color=Gold,fontSize=11.sp)
     }
    }
    Box(Modifier.fillMaxWidth().height(110.dp).background(CardColor).pictorialFrame(R.drawable.wid_plate_expanded,capXRatio=.16f,capYRatio=.22f,alpha=1f).padding(16.dp)){
     Column(verticalArrangement=Arrangement.spacedBy(4.dp)){
      Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){
       Text("wid_plate_expanded · Superfície Expandida",color=Ink,fontFamily=SagaFont,fontSize=13.sp)
       Text("628×320 WebP",color=Gold,fontSize=11.sp)
      }
      Text("Ardósia vulcânica chanfrada com grampos diagonais de bronze nos quatro cantos (9-slice sem distorção).",color=Muted,fontSize=11.sp)
     }
    }
   }
  }
 }
}

@Composable private fun DesignSystemBoard16915(){
 Column(Modifier.fillMaxSize().background(Night).padding(24.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){
   Column(verticalArrangement=Arrangement.spacedBy(4.dp)){
    Eyebrow("ASCEND 1.6.9.15 · O LEGADO DA FORJA",Gold)
    Text("Prancha do Design System Nórdico",fontFamily=SagaFont,fontSize=28.sp,color=Ink)
    Text("Metais forjados, ardósia vulcânica, filigrana cerimonial, pergaminho escandinavo e entalhes rúnicos.",color=Muted,fontSize=13.sp)
   }
   CeremonialSeal(SealKind.Saga,true,Modifier.size(44.dp))
  }
  NorthDivider()
  Row(Modifier.fillMaxWidth().weight(1f),horizontalArrangement=Arrangement.spacedBy(20.dp)){
   Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(14.dp)){
    Eyebrow("1 · Botões Forjados e Estados (SagaButton)",Gold)
    SagaButton("Iniciar ritual · Primário Forjado",{},Modifier.fillMaxWidth())
    SagaButton("Resgatar recompensa · Cerimonial",{},Modifier.fillMaxWidth(),ceremonial=true)
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){
     SagaButton("Secundário · Ferro",{},Modifier.weight(1f),secondary=true)
     SagaButton("Concluído",{},Modifier.weight(1f),completed=true)
    }
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){
     SagaButton("Destrutivo · Brasa",{},Modifier.weight(1f),destructive=true)
     SagaButton("Sob a névoa",{},Modifier.weight(1f),enabled=false)
    }
    NorthDivider()
    Eyebrow("2 · Inscrições, Seletores e Controles",Gold)
    FormField("Caminhar ao amanhecer",{},"Nome do ritual")
    ChipRow(listOf("Diário","Quantidade","Tempo"),2,{})
    Toggle("Modo minimalista · entalhes discretos",true,{})
    NorthDivider()
    Eyebrow("3 · Abas Editoriais e Trilhos de Progresso",Gold)
    AetherisTabs(listOf("Resumo","Atributos","Inventário","Loja"),0,{})
    Column(verticalArrangement=Arrangement.spacedBy(6.dp)){
     Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("Trilho entalhado (25% / 50% / 75%)",color=Muted,fontSize=12.sp);Text("68%",color=Gold,fontSize=12.sp)}
     Meter(0.68f,Gold)
     Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("Trilho compacto · Gelo e Prata",color=Muted,fontSize=12.sp);Text("45%",color=Blue,fontSize=12.sp)}
     Meter(0.45f,Blue,slim=true)
    }
   }
   Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(12.dp)){
    Eyebrow("4 · Emblemas de Classe, Atributos e Selos",Gold)
    AetherSurface(SurfaceKind.Operational){
     Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){
      (0..2).forEach{ClassEmblem(it,Modifier.size(34.dp))}
      (0..4).forEach{AttributeEmblem(it,Modifier.size(28.dp))}
      SealKind.entries.forEach{CeremonialSeal(it,true,Modifier.size(30.dp))}
     }
    }
    Eyebrow("5 · Cinco Famílias de Superfície (AetherSurface)",Gold)
    AetherSurface(SurfaceKind.Operational){
     Eyebrow("Operacional · Ardósia e Ferro Frio",Blue)
     Text("Hábitos, Planejador, Central de Análises e Configurações.",color=Ink,fontSize=13.sp)
    }
    AetherSurface(SurfaceKind.Heroic){
     Eyebrow("Heroica · Basalto e Bronze Envelhecido",Gold)
     Text("Herói, Retrato, Atributos e Progressão de Nível.",color=Ink,fontSize=13.sp)
    }
    AetherSurface(SurfaceKind.Ceremonial){
     Eyebrow("Cerimonial · Ouro Oxidado e Filigrana Dupla",Gold)
     Text("Sagas Pessoais, Marcos de Expedição e Recompensas.",color=Ink,fontSize=13.sp)
    }
    AetherSurface(SurfaceKind.Parchment){
     Eyebrow("Pergaminho · Manuscrito Escandinavo e Sépia",Sepia)
     Text("Crônica da Semana, Capítulos do Mundo e Diário.",color=Sepia,fontSize=13.sp)
     Meter(0.75f,Sepia,slim=true)
    }
    AetherSurface(SurfaceKind.Artifact){
     Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(12.dp),verticalAlignment=Alignment.CenterVertically){
      RelicArt("head",Gold,Modifier.size(44.dp),itemId="traveler-helm")
      Column(Modifier.weight(1f)){
       Eyebrow("Artefato · Nicho de Pedra e Engaste",Gold)
       Text("Inventário, Relíquias Equipadas e Mercador.",color=Ink,fontSize=13.sp)
      }
     }
    }
   }
  }
 }
}
