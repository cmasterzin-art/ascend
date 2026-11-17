package br.com.ascend.aetheris

import android.view.View
import android.widget.FrameLayout
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
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
 }
}
