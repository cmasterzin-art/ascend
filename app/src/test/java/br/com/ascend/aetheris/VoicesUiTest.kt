package br.com.ascend.aetheris

import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[28],application=AscendApp::class,qualifiers="w411dp-h891dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class VoicesUiTest{
    @get:Rule val compose=createAndroidComposeRule<MainActivity>()
    private val app get()=RuntimeEnvironment.getApplication() as AscendApp
    private val vm get()=ViewModelProvider(compose.activity)[GameViewModel::class.java]
    @Before fun setup(){runBlocking{app.preferences.edit{it.clear()}};wait{app.repository.state.value!=null};vm.run{app.repository.create(Player(name="Sigrid"),emptyList());vm.setting("reducedMotion",true)};wait{app.repository.state.value!!.player!=null&&vm.reducedMotion.value}}
    @After fun close(){compose.activityRule.scenario.close();app.database.close()}
    private fun wait(test:()->Boolean){compose.waitUntil(15000){Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();test()};compose.waitForIdle();val dismiss=compose.onAllNodesWithContentDescription("Dismiss");if(dismiss.fetchSemanticsNodes().isNotEmpty())dismiss.onFirst().performClick()}
    private fun open(){compose.onNodeWithText("Hábitos").performClick();compose.onNodeWithText("Explorar hábitos").performScrollTo().performClick();compose.onNodeWithText("Grimório dos Hábitos").assertExists()}
    private fun shot(name:String){compose.waitForIdle();compose.runOnIdle{val dialog=org.robolectric.shadows.ShadowDialog.getLatestDialog();val view=if(dialog?.isShowing==true)dialog.window!!.decorView else compose.activity.window.decorView;val bitmap=android.graphics.Bitmap.createBitmap(view.width,view.height,android.graphics.Bitmap.Config.ARGB_8888);view.draw(android.graphics.Canvas(bitmap));val file=java.io.File("build/reports/screenshots/$name.png");file.parentFile!!.mkdirs();file.outputStream().use{bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it)};bitmap.recycle()}}
    @Test fun mainRoutesWeeklyMemoriesAndBackPreserveSections(){
        compose.onNodeWithTag("nav:habits").performClick();compose.onNodeWithText("Explorar hábitos").performScrollTo().performClick()
        compose.onNodeWithText("Buscar nome ou palavra-chave").performScrollTo().performTextInput("direito")
        compose.runOnIdle{compose.activity.onBackPressedDispatcher.onBackPressed()};compose.waitForIdle();compose.onNodeWithText("Livro de hábitos").assertExists()
        compose.onNodeWithTag("nav:missions").performClick();listOf("Diárias","Semanais","Épicas","Chefes","Resgates","Sagas").forEach{compose.onNodeWithText(it).assertIsDisplayed()};shot("menus-missoes-145")
        compose.onNodeWithTag("nav:world").performClick();compose.onNodeWithText("Crônicas").performClick();compose.onNodeWithText("Crônica da Semana").performScrollTo().performClick();compose.onNodeWithText("Crônica da Semana").assertExists();shot("cronica-semanal-145")
        compose.runOnIdle{compose.activity.onBackPressedDispatcher.onBackPressed()};compose.waitForIdle();compose.onNodeWithText("Salão das Memórias").performScrollTo().performClick();shot("salao-memorias-145")
        compose.runOnIdle{compose.activity.onBackPressedDispatcher.onBackPressed()};compose.waitForIdle()
        compose.onNodeWithTag("nav:character").performClick();compose.onNodeWithText("Ampliar retrato").performScrollTo().performClick();shot("retrato-ampliado-145")
        compose.runOnIdle{compose.activity.onBackPressedDispatcher.onBackPressed()};compose.waitForIdle();compose.onNodeWithText("Inventário").performClick();shot("inventario-145")
        compose.onNodeWithTag("nav:journey").performClick();shot("jornada-145")
        compose.onNodeWithTag("nav:character").performClick();compose.onNodeWithText("Câmara dos equipamentos",ignoreCase=true).assertExists()
    }
    @Test fun datePickerAndSagaAbandonmentHaveRealControls(){
        vm.run{app.repository.habit(Habit(name="Meu ritual",xp=0))};wait{app.repository.state.value!!.habits.isNotEmpty()}
        compose.onNodeWithTag("nav:missions").performClick();compose.onNodeWithText("Sagas").performClick();compose.onNodeWithText("Iniciar saga").performScrollTo().performClick();compose.onNodeWithText("Criar saga personalizada").performScrollTo().performClick()
        compose.onNodeWithText("Nome da saga").performTextReplacement("Uma nova voz")
        compose.onNodeWithText("Escolher data").performScrollTo().performClick()
        compose.runOnIdle{val dialog=org.robolectric.shadows.ShadowDialog.getLatestDialog() as android.app.DatePickerDialog;val date=java.time.LocalDate.now().plusDays(5);dialog.datePicker.updateDate(date.year,date.monthValue-1,date.dayOfMonth);dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE).performClick()}
        compose.onNodeWithText("Meu ritual").performScrollTo();compose.onAllNodes(isToggleable()).onFirst().performClick();compose.onNodeWithText("Iniciar jornada").performClick();wait{app.repository.state.value!!.quests.any{it.sagaStart.isNotBlank()}}
        assertEquals(java.time.LocalDate.now().plusDays(5).toString(),app.repository.state.value!!.quests.first{it.sagaStart.isNotBlank()}.deadline)
        compose.onNodeWithText("Abandonar saga").performScrollTo().performClick();compose.onNodeWithText("Abandonar").performClick();wait{app.repository.state.value!!.quests.any{it.sagaState==1}};shot("saga-historico-145")
        compose.onNodeWithText("Arquivar saga").performScrollTo().performClick();wait{app.repository.state.value!!.quests.any{it.sagaState==3}}
        compose.onNodeWithText("Reiniciar desafio").performScrollTo().performClick();wait{app.repository.state.value!!.quests.count{it.sagaStart.isNotBlank()}==2};assertEquals(0L,app.repository.state.value!!.xp)
    }
    @Test @Config(qualifiers="w320dp-h740dp-mdpi") fun menusRemainUsableWithLargeTextAndLightTheme(){
        vm.setting("reducedMotion",true);vm.theme("light");wait{vm.appearance.value=="light"}
        compose.runOnIdle{compose.activity.setContent{val density=LocalDensity.current;CompositionLocalProvider(LocalDensity provides Density(density.density,1.3f)){Ascend()}}};compose.waitForIdle()
        mainRoutes.forEach{r->compose.onNodeWithTag("nav:$r").assertIsDisplayed().performClick()}
        compose.onNodeWithText("Inventário").performClick();shot("menus-320-claro-145")
        compose.onNodeWithText("Habilidades").performClick();compose.onNodeWithText("Constelações da alma").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("nav:missions").performClick();compose.onNodeWithText("Sagas").performScrollTo().performClick();shot("sagas-320-claro-145")
        compose.onNodeWithTag("nav:world").performClick();compose.onNodeWithText("Crônicas").performClick();compose.onNodeWithText("Crônica da Semana").performScrollTo().performClick();shot("cronica-320-claro-145")
    }
    @Test fun recognizedConversationsAndRichWeeklyPageCanBeRevisited(){
        vm.run{
            val h=Habit(name="Cuidar da vila",xp=0,coins=200);app.repository.habit(h)
            val q=Quest(name="O Primeiro Abrigo",habitIds=h.id,target=1,xp=0,coins=0);app.repository.saga(q);app.repository.progress(h,today(),1)
            app.repository.claim(app.repository.state.value!!.quests.first{it.id==q.id});app.repository.restore(0);app.repository.restore(0)
            app.repository.rememberNarrative(nextNarrative(app.repository.state.value!!)!!.id)
        };wait{app.repository.state.value!!.assets.any{it.kind=="memory"}}
        val reward=compose.onAllNodesWithText("Continuar a jornada");if(reward.fetchSemanticsNodes().isNotEmpty())reward.onFirst().performClick()
        compose.onNodeWithTag("nav:world").performClick();compose.onNodeWithText("Crônicas").performClick();compose.onNodeWithText("Crônica da Semana").performScrollTo().performClick();shot("cronica-ativa-145")
        compose.runOnIdle{compose.activity.onBackPressedDispatcher.onBackPressed()};compose.waitForIdle();compose.onNodeWithText("Salão das Memórias").performScrollTo().performClick();shot("salao-feitos-145")
        compose.onNodeWithText("História").performScrollTo().performClick();compose.onNodeWithText("O Cronista").performScrollTo().assertExists();shot("conversas-145")
    }

}
