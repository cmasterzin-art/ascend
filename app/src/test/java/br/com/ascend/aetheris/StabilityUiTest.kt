package br.com.ascend.aetheris

import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
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
class StabilityUiTest{
    @get:Rule val compose=createAndroidComposeRule<MainActivity>()
    private val app get()=RuntimeEnvironment.getApplication() as AscendApp
    private val vm get()=ViewModelProvider(compose.activity)[GameViewModel::class.java]
    @Before fun setup(){runBlocking{app.preferences.edit{it.clear()}};wait{app.repository.state.value!=null};vm.run{app.repository.create(Player(name="Sigrid"),emptyList());vm.setting("reducedMotion",true)};wait{app.repository.state.value!!.player!=null&&vm.reducedMotion.value}}
    @After fun close(){compose.activityRule.scenario.close();app.database.close()}
    private fun wait(test:()->Boolean){compose.waitUntil(15000){Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();test()};compose.waitForIdle();val dismiss=compose.onAllNodesWithContentDescription("Dismiss");if(dismiss.fetchSemanticsNodes().isNotEmpty())dismiss.onFirst().performClick()}
    private fun shot(name:String){compose.waitForIdle();compose.runOnIdle{val dialog=org.robolectric.shadows.ShadowDialog.getLatestDialog();val view=if(dialog?.isShowing==true)dialog.window!!.decorView else compose.activity.window.decorView;val bitmap=android.graphics.Bitmap.createBitmap(view.width,view.height,android.graphics.Bitmap.Config.ARGB_8888);view.draw(android.graphics.Canvas(bitmap));val file=java.io.File("build/reports/screenshots/$name.png");file.parentFile!!.mkdirs();file.outputStream().use{bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it)};bitmap.recycle()}}

    @Test fun invalidProgressStaysEditableWithoutErasingTheRecord(){
        vm.run{val h=Habit(id="measure",name="Leitura diária",type=1,target=20,proportional=true,xp=20);app.repository.habit(h);app.repository.progress(h,today(),8)};wait{app.repository.state.value!!.records.isNotEmpty()}
        compose.onNodeWithContentDescription("Registrar progresso").performScrollTo().performClick()
        compose.onNodeWithText("Progresso total nesta data").performTextReplacement("abc");compose.onNodeWithText("Salvar").performClick();compose.waitUntil(5000){app.repository.state.value!!.records.single().progress!=8||compose.onAllNodesWithText("Informe um número inteiro igual ou maior que zero.").fetchSemanticsNodes().isNotEmpty()}
        assertEquals(8,app.repository.state.value!!.records.single().progress)
        compose.onNodeWithText("Registrar · Leitura diária").assertExists();compose.onNodeWithText("Informe um número inteiro igual ou maior que zero.").assertExists();assertEquals(8,app.repository.state.value!!.records.single().progress)
        shot("registro-validacao-152")
        compose.onNodeWithText("Progresso total nesta data").performTextReplacement("10");compose.onNodeWithText("Salvar").performClick();wait{app.repository.state.value!!.records.single().progress==10};assertEquals(10L,app.repository.state.value!!.xp)
    }
    @Test fun habitDraftSurvivesSavedStateAndCanStillBeCancelled(){
        compose.runOnIdle{compose.activity.findViewById<android.view.ViewGroup>(android.R.id.content).removeAllViews()}
        val restoration=StateRestorationTester(compose);restoration.setContent{Ascend()};compose.waitForIdle()
        compose.onNodeWithTag("nav:habits").performClick();compose.onNodeWithText("+ Criar hábito").performScrollTo().performClick()
        compose.onNodeWithText("Nome do hábito").performTextReplacement("Ritual preservado");compose.onNodeWithText("XP ao atingir a meta").performTextReplacement("73")
        restoration.emulateSavedInstanceStateRestore();compose.waitForIdle()
        compose.onNodeWithText("Novo hábito").assertExists();compose.onNodeWithText("Nome do hábito").assertTextContains("Ritual preservado");compose.onNodeWithText("XP ao atingir a meta").assertTextContains("73")
        compose.onNodeWithContentDescription("Voltar").performClick();compose.onNodeWithText("Descartar alterações?").assertExists();compose.onNodeWithText("Continuar editando").performClick()
        compose.onNodeWithText("Salvar").performClick();wait{app.repository.state.value!!.habits.isNotEmpty()};assertEquals(73,app.repository.state.value!!.habits.single().xp)
        compose.onNodeWithText("Ritual preservado").performScrollTo().performClick();compose.onNodeWithText("Editar",useUnmergedTree=true).performClick()
        compose.onNodeWithText("XP ao atingir a meta").performTextReplacement("900");compose.onNodeWithContentDescription("Voltar").performClick();compose.onNodeWithText("Descartar").performClick()
        assertEquals(73,app.repository.state.value!!.habits.single().xp)
    }
    @Test fun widgetRecordDraftAndConsumedIntentSurviveActivityRecreation(){
        vm.run{app.repository.habit(Habit(id="read",name="Leitura guardada",type=1,target=20))};wait{app.repository.state.value!!.habits.isNotEmpty()}
        compose.runOnIdle{val a=compose.activity;val intent=android.content.Intent(a.intent).putExtra("habit","read").putExtra("widget_journey",true).putExtra("date",today());a.javaClass.getDeclaredMethod("onNewIntent",android.content.Intent::class.java).apply{isAccessible=true}.invoke(a,intent)}
        compose.onNodeWithText("Registrar · Leitura guardada").assertExists();compose.onNodeWithText("Progresso total nesta data").performTextReplacement("7")
        assertFalse(compose.activity.intent.hasExtra("habit"));compose.activityRule.scenario.recreate();compose.waitForIdle()
        compose.onNodeWithText("Registrar · Leitura guardada").assertExists();compose.onNodeWithText("Progresso total nesta data").assertTextContains("7");assertTrue(app.repository.state.value!!.records.isEmpty())
        compose.onNodeWithContentDescription("Voltar").performClick();compose.onNodeWithText("Descartar").performClick();compose.onNodeWithTag("nav:character").performClick()
        compose.activityRule.scenario.recreate();compose.waitForIdle();compose.onNodeWithText("Seu aventureiro").assertExists();compose.onNodeWithText("Registrar · Leitura guardada").assertDoesNotExist()
    }

}
