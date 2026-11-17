package br.com.ascend.aetheris

import android.app.*
import android.appwidget.*
import android.content.*
import android.content.res.Configuration
import android.os.Looper
import android.view.View
import android.widget.*
import kotlinx.coroutines.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[28],application=AscendApp::class,qualifiers="w320dp-h740dp-mdpi")
class WidgetStabilityTest{
 private val app get()=RuntimeEnvironment.getApplication() as AscendApp
 private val repo get()=app.repository
 @Before fun setup()=runBlocking{repo.refresh();repo.create(Player(name="Freya"),emptyList());ShadowToast.reset()}
 @After fun close(){app.database.close()}
 @Test fun emptyCompactHasNoMeaninglessProgressAndFitsItsMinimumWidth(){
  val ctx=app.createConfigurationContext(Configuration(app.resources.configuration).apply{fontScale=1.3f})
  val view=HabitWidget.views(ctx,repo.state.value!!,false,height=110,width=180).apply(ctx,FrameLayout(ctx))
  view.measure(View.MeasureSpec.makeMeasureSpec(180,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(110,View.MeasureSpec.EXACTLY));view.layout(0,0,180,110)
  assertEquals(View.GONE,view.findViewById<View>(R.id.widget_progress).visibility)
  val name=view.findViewById<TextView>(R.id.widget_header);assertTrue(name.paint.measureText(name.text.toString())<=name.width)
 }
 @Test fun oldPendingIntentRefusesPaymentAndExplainsTheNewDay()=runBlocking{
  val h=Habit(name="Pequena chama",xp=31);repo.habit(h)
  val action=PendingIntent.getBroadcast(app,999,Intent(app,HabitWidget::class.java).setAction("br.com.ascend.WIDGET_COMPLETE").putExtra("habit",h.id).putExtra("date",LocalDate.now().minusDays(1).toString()),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
  action.send();withTimeout(5000){while(ShadowToast.getTextOfLatestToast()==null){Shadows.shadowOf(Looper.getMainLooper()).idle();delay(10)}}
  assertEquals("O dia mudou. Atualize o widget.",ShadowToast.getTextOfLatestToast().toString());assertTrue(repo.state.value!!.records.isEmpty());assertEquals(0L,repo.state.value!!.xp)
 }
 @Test fun changedSchedulingAndInactiveHabitsCannotBeCompletedByAnOldAction()=runBlocking{
  val h=Habit(name="Minha agenda");repo.habit(h)
  for(updated in listOf(h.copy(paused=true),h.copy(archived=true),h.copy(deleted=true),h.copy(type=1,target=20),h.copy(type=2,target=30),h.copy(days=LocalDate.now().plusDays(1).dayOfWeek.value.toString()))){
   repo.habit(updated);assertTrue(widgetDay(repo.state.value!!).pending.none{it.id==h.id}||updated.type!=0);assertTrue(runCatching{repo.widgetComplete(h.id,today())}.isFailure);assertTrue(repo.state.value!!.records.isEmpty())
  }
  repo.habit(h);repo.rest(today(),true);assertTrue(widgetDay(repo.state.value!!).pending.isEmpty());assertTrue(runCatching{repo.widgetComplete(h.id,today())}.isFailure)
  repo.rest(today(),false);coroutineScope{(1..8).map{async{repo.widgetComplete(h.id,today())}}.awaitAll()};assertEquals(1,repo.state.value!!.records.size);assertEquals(h.xp.toLong(),repo.state.value!!.xp)
 }
 @Test fun hostRefreshesAfterScheduleChangesCorrectionAndBackupRestore()=runBlocking{
  val manager=AppWidgetManager.getInstance(app);val host=Shadows.shadowOf(manager)
  host.addBoundWidget(152,AppWidgetProviderInfo().apply{provider=ComponentName(app,HabitWidget::class.java);initialLayout=R.layout.habit_widget})
  val h=Habit(name="Ler",xp=12);repo.habit(h);val backup=Backup.encode(repo.state.value!!)
  fun summary()=host.getViewFor(152).findViewById<TextView>(R.id.widget_summary).text.toString()
  assertTrue(summary().contains("0/1"));repo.widgetComplete(h.id,today());assertTrue(summary().contains("1/1"));repo.progress(h,today(),0);assertTrue(summary().contains("0/1"))
  repo.habit(h.copy(paused=true));assertTrue(summary().contains("Nenhum ritual"));repo.importState(Backup.decode(backup));assertTrue(summary().contains("0/1"));assertEquals(0L,repo.state.value!!.xp)
 }
 @Test fun failedWidgetLoadKeepsARecoverableJourneyActionWithoutStaleButtons()=runBlocking{
  val manager=AppWidgetManager.getInstance(app);val host=Shadows.shadowOf(manager)
  host.addBoundWidget(153,AppWidgetProviderInfo().apply{provider=ComponentName(app,HabitWidget::class.java);initialLayout=R.layout.habit_widget})
  repo.habit(Habit(name="Ritual"));app.database.openHelper.writableDatabase.execSQL("ALTER TABLE player RENAME TO unavailable_player")
  HabitWidget().onUpdate(app,manager,intArrayOf(153))
  withTimeout(5000){while(host.getViewFor(153).findViewById<TextView>(R.id.widget_summary).text.toString()!="Abra a Jornada para atualizar os rituais."){Shadows.shadowOf(Looper.getMainLooper()).idle();delay(10)}}
  val view=host.getViewFor(153);assertEquals(View.GONE,view.findViewById<View>(R.id.widget_habit1).visibility);assertEquals(View.GONE,view.findViewById<View>(R.id.widget_progress).visibility)
  view.findViewById<View>(R.id.widget_open).performClick();val intent=Shadows.shadowOf(app).nextStartedActivity;assertTrue(intent.getBooleanExtra("widget_journey",false));assertTrue(intent.flags and Intent.FLAG_ACTIVITY_SINGLE_TOP!=0);assertTrue(intent.flags and Intent.FLAG_ACTIVITY_CLEAR_TOP!=0)
  app.database.openHelper.writableDatabase.execSQL("ALTER TABLE unavailable_player RENAME TO player")
 }

}
