package br.com.ascend.aetheris

import androidx.compose.ui.test.*
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[28],application=AscendApp::class,qualifiers="w411dp-h891dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class OnboardingTest {
    @get:Rule val compose=createAndroidComposeRule<MainActivity>()
    @Before fun resetAppearance(){runBlocking{(RuntimeEnvironment.getApplication() as AscendApp).preferences.edit{it.clear()}}}
    @After fun closeUiDatabase(){
        compose.activityRule.scenario.close()
        (RuntimeEnvironment.getApplication() as AscendApp).database.close()
    }
    @Test fun createMageAndRecordStudyThroughInterface(){
        val repo=(RuntimeEnvironment.getApplication() as AscendApp).repository
        compose.waitForIdle()
        waitForState{repo.state.value!=null}
        compose.onNodeWithText("Acender a primeira chama").performScrollTo().performClick()
        compose.onNodeWithText("Nome do aventureiro").performTextInput("Aria")
        compose.onNodeWithText("Continuar").performScrollTo().performClick()
        compose.onNodeWithText("Entrar em Aetheris").performScrollTo().performClick()
        waitForState{repo.state.value?.player!=null}
        compose.onNodeWithText("Aria").assertExists()
        compose.onNodeWithContentDescription("Registrar progresso").performScrollTo().performClick()
        compose.onNodeWithText("Atingir meta").performScrollTo().performClick()
        compose.onNodeWithText("Salvar").performClick()
        waitForState{repo.state.value?.xp==60L}
        assertEquals(1,repo.state.value!!.player!!.heroClass)
        assertEquals(60,repo.state.value!!.records.single().progress)
        assertEquals(5L,repo.state.value!!.points(0))
        capture("jornada-escuro")
        compose.onNodeWithText("Mundo").performClick()
        compose.onNodeWithText("Atlas de Aetheris").assertExists()
        compose.onAllNodesWithText("Vila").onFirst().performClick()
        compose.onNodeWithText("A Biblioteca").assertExists()
        compose.onNodeWithText("Missões").performClick()
        compose.onNodeWithText("Contratos da jornada").assertExists()
        compose.onNodeWithText("Herói").performClick()
        compose.onNodeWithText("Seu aventureiro").assertExists()
        compose.onNodeWithText("Conquistas").performClick()
        compose.onNodeWithText("✦ O Primeiro Passo").assertExists()
        compose.onNodeWithText("Hábitos").performClick()
        compose.onNodeWithText("Histórico").performScrollTo().performClick()
        compose.onNodeWithText("Estudar Direito").assertExists()
    }
    @Test fun customHabitsPreserveHistoricalXpAndUndoThroughInterface(){
        val repo=(RuntimeEnvironment.getApplication() as AscendApp).repository
        waitForState{repo.state.value!=null}
        compose.onNodeWithText("Acender a primeira chama").performScrollTo().performClick()
        compose.onNodeWithText("Nome do aventureiro").performTextInput("Kael")
        compose.onNodeWithText("Continuar").performScrollTo().performClick()
        compose.onNodeWithText("Entrar em Aetheris").performScrollTo().performClick()
        waitForState{repo.state.value?.player!=null}
        createHabit("Estudar","50")
        compose.onAllNodesWithContentDescription("Registrar progresso").onLast().performScrollTo().performClick()
        waitForState{repo.state.value!!.xp==50L}
        createHabit("Ler","30")
        compose.onAllNodesWithContentDescription("Registrar progresso").onLast().performScrollTo().performClick()
        waitForState{repo.state.value!!.xp==80L}
        compose.onNodeWithText("Ler").performScrollTo().performClick()
        compose.onNodeWithText("Editar",useUnmergedTree=true).performClick()
        compose.onNodeWithText("XP ao atingir a meta").performScrollTo().performTextReplacement("40")
        compose.onNodeWithText("Salvar").performClick()
        waitForState{repo.state.value!!.habits.first{it.name=="Ler"}.xp==40}
        assertEquals(30,repo.state.value!!.records.first{it.name=="Ler"}.xp)
        assertEquals(80L,repo.state.value!!.xp)
        compose.onAllNodesWithContentDescription("Editar conclusão").onLast().performScrollTo().performClick()
        compose.onNodeWithText("Desfazer").performScrollTo().performClick()
        compose.onNodeWithText("Salvar").performClick()
        waitForState{repo.state.value!!.xp==50L}
        assertEquals(1,repo.state.value!!.level)
    }
    @Test fun lightThemeCanBeSelectedAndPersistsLocally(){
        val app=RuntimeEnvironment.getApplication() as AscendApp
        waitForState{app.repository.state.value!=null}
        val vm=androidx.lifecycle.ViewModelProvider(compose.activity)[GameViewModel::class.java]
        vm.run{app.repository.create(Player(name="Kael"),listOf(Habit(name="Ler")))}
        waitForState{app.repository.state.value?.player!=null}
        compose.onNodeWithText("Herói").performClick()
        compose.onNodeWithContentDescription("Configurações").performClick()
        compose.onNodeWithText("Claro").performScrollTo().performClick()
        waitForState{vm.appearance.value=="light"}
        compose.onNodeWithText("Jornada").performClick()
        capture("jornada-claro")
        compose.onNodeWithText("Kael").assertExists()
    }
    @Test fun planAndRemoveRestThroughCalendar(){
        val repo=(RuntimeEnvironment.getApplication() as AscendApp).repository
        waitForState{repo.state.value!=null}
        val vm=androidx.lifecycle.ViewModelProvider(compose.activity)[GameViewModel::class.java]
        vm.run{repo.create(Player(name="Kael"),listOf(Habit(name="Ler")))}
        waitForState{repo.state.value?.player!=null}
        compose.onNodeWithText("Hábitos").performClick()
        compose.onNodeWithText("Calendário").performScrollTo().performClick()
        val restToggle=isToggleable() and hasAnySibling(hasText("Descanso planejado nesta data"))
        compose.onNode(restToggle).performScrollTo().performClick()
        waitForState{repo.state.value!!.restDays.size==1}
        assertTrue(repo.state.value!!.due().isEmpty())
        assertEquals(0L,repo.state.value!!.xp)
        compose.onNode(restToggle).performClick()
        waitForState{repo.state.value!!.restDays.isEmpty()}
        assertEquals(1,repo.state.value!!.due().size)
    }
    @Test fun inspectRpgScreensAndEquipPurchasedRelic(){
        val app=RuntimeEnvironment.getApplication() as AscendApp
        waitForState{app.repository.state.value!=null}
        val vm=androidx.lifecycle.ViewModelProvider(compose.activity)[GameViewModel::class.java]
        vm.run{val h=Habit(name="Estudar",coins=100);app.repository.create(Player(name="Kael"),listOf(h));app.repository.progress(h,today(),1);app.repository.buy(shopItems.first{it.id=="traveler-helm"})}
        waitForState{app.repository.state.value!!.assets.any{it.id=="traveler-helm"}}
        compose.onNodeWithText("Missões").performClick()
        compose.onNodeWithText("Livro de missões").assertExists();shot("livro-missoes")
        compose.onNodeWithText("Mundo").performClick();shot("mapa-aetheris")
        compose.onNodeWithText("Herói").performClick()
        compose.onNodeWithText("Inventário").performClick()
        shot("inventario")
        compose.onNodeWithText("Elmo do Viajante").performScrollTo().performClick()
        compose.onNodeWithText("Equipar").performClick()
        waitForState{app.repository.state.value!!.assets.first{it.id=="traveler-helm"}.equipped}
        compose.onNodeWithText("Habilidades").performClick()
        shot("habilidades");compose.onNodeWithText("Constelações da alma").assertExists()
    }
    @Test fun inspectNordicEquipmentVillageBossAndChronicle(){
        val app=RuntimeEnvironment.getApplication() as AscendApp
        waitForState{app.repository.state.value!=null}
        val vm=androidx.lifecycle.ViewModelProvider(compose.activity)[GameViewModel::class.java]
        vm.run{
            val h=Habit(name="Caminhar entre os pinheiros",xp=2000,coins=1000)
            app.repository.create(Player(name="Eira",face=1,heroClass=0),listOf(h))
            app.repository.progress(h,today(),1)
            listOf("traveler-helm","armor","aether-amulet","silver-blade","frame","night","corvus","lantern").forEach{id->
                app.repository.buy(shopItems.first{it.id==id});app.repository.equip(app.repository.state.value!!.assets.first{it.id==id})
            }
            app.repository.restore(0);app.repository.restore(0)
            vm.setting("bosses",true);vm.setting("reducedMotion",true)
        }
        waitForState{app.repository.state.value!!.assets.any{it.id=="building:0"&&it.stage==2}}
        waitForState{vm.reducedMotion.value}
        capture("jornada-equipada")
        compose.onNodeWithText("Herói").performClick();shot("perfil")
        compose.onNodeWithText("Inventário").performClick();shot("inventario-equipado")
        compose.onNodeWithContentDescription("Cabeça: Elmo do Viajante").performScrollTo().performClick()
        compose.onNodeWithText("Desequipar").performClick()
        waitForState{app.repository.state.value!!.assets.none{it.kind=="head"&&it.equipped}}
        assertEquals(7,app.repository.state.value!!.assets.count{it.equipped})
        compose.onNodeWithText("Loja").performClick();shot("loja")
        compose.onNodeWithText("Mundo").performClick();shot("mapa-revelado")
        compose.onNodeWithText("Vila").performClick();shot("vila")
        compose.onNodeWithText("Crônicas").performScrollTo().performClick()
        compose.onAllNodesWithText("Abrir capítulo").onFirst().performScrollTo().performClick();compose.onNodeWithText(chapterTexts[0].split("\n\n")[0]).assertExists();shot("cronicas")
        compose.onNodeWithText("Fechar").performClick()
        compose.onNodeWithText("Missões").performClick()
        compose.onNodeWithText("Chefes").performScrollTo().performClick()
        compose.onAllNodesWithText("Vida:",substring=true).onFirst().performScrollTo();shot("chefe")
    }
    @Test @Config(qualifiers="w320dp-h740dp-mdpi")
    fun narrowScreenWithLargeTextKeepsNavigationAndEquipmentUsable(){
        val app=RuntimeEnvironment.getApplication() as AscendApp
        waitForState{app.repository.state.value!=null}
        val vm=androidx.lifecycle.ViewModelProvider(compose.activity)[GameViewModel::class.java]
        vm.run{app.repository.create(Player(name="Astrid",face=2),listOf(Habit(name="Ler uma saga")));vm.setting("reducedMotion",true)}
        waitForState{app.repository.state.value!!.player!=null}
        compose.runOnIdle{compose.activity.setContent{
            val density=LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density,1.3f)){Ascend()}
        }}
        compose.waitForIdle();capture("jornada-320-texto130")
        compose.onNodeWithText("Herói").performClick()
        compose.onNodeWithText("Inventário").performClick();shot("inventario-320-texto130")
        compose.onNodeWithText("Habilidades").performClick()
        compose.onNodeWithText("Constelações da alma").assertExists()
        compose.onNodeWithContentDescription("Configurações").performClick()
        compose.onNodeWithText("Modo minimalista").performScrollTo()
        compose.onNodeWithContentDescription("Modo minimalista").performClick()
        waitForState{vm.minimal.value}
        compose.onNodeWithText("Jornada").performClick();capture("jornada-minimalista")
        compose.onNodeWithContentDescription("Registrar progresso").performScrollTo().performClick()
        waitForState{app.repository.state.value!!.records.any{it.complete}}
    }
    private fun shot(name:String){
        compose.waitForIdle();compose.runOnIdle{
            val dialog=org.robolectric.shadows.ShadowDialog.getLatestDialog()
            val view=if(dialog?.isShowing==true)dialog.window!!.decorView else compose.activity.window.decorView
            val bitmap=android.graphics.Bitmap.createBitmap(view.width,view.height,android.graphics.Bitmap.Config.ARGB_8888)
            view.draw(android.graphics.Canvas(bitmap))
            val file=java.io.File("build/reports/screenshots/$name.png");file.parentFile!!.mkdirs()
            file.outputStream().use{bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it)}
            bitmap.recycle()
        }
    }
    private fun capture(name:String){
        compose.onNodeWithText("ASCEND  /  MINHA JORNADA").performScrollTo()
        compose.waitForIdle()
        compose.runOnIdle {
            val dialog=org.robolectric.shadows.ShadowDialog.getLatestDialog()
            val view=if(dialog?.isShowing==true)dialog.window!!.decorView else compose.activity.window.decorView
            val image=android.graphics.Bitmap.createBitmap(view.width,view.height,android.graphics.Bitmap.Config.ARGB_8888)
            view.draw(android.graphics.Canvas(image))
            val path=java.io.File("build/reports/screenshots/$name.png");path.parentFile!!.mkdirs()
            path.outputStream().use{image.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it)}
            image.recycle()
        }
    }
    private fun createHabit(name:String,xp:String){
        compose.onNodeWithContentDescription("Criar hábito").performScrollTo().performClick()
        compose.onNodeWithText("Nome do hábito").performScrollTo().performTextInput(name)
        compose.onNodeWithText("XP ao atingir a meta").performScrollTo().performTextReplacement(xp)
        compose.onNodeWithText("Salvar").performClick()
        val repo=(RuntimeEnvironment.getApplication() as AscendApp).repository
        waitForState{repo.state.value!!.habits.any{it.name==name}}
    }
    private fun waitForState(condition:()->Boolean){
        compose.waitUntil(15000){Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();condition()}
        compose.waitForIdle()
        val dismiss=compose.onAllNodesWithContentDescription("Dismiss")
        if(dismiss.fetchSemanticsNodes().isNotEmpty())dismiss.onFirst().performClick();val reward=compose.onAllNodesWithText("Continuar a jornada");if(reward.fetchSemanticsNodes().isNotEmpty())reward.onFirst().performClick()
    }

}
