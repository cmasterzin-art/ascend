package br.com.ascend.aetheris

import android.appwidget.*
import android.content.*
import android.view.*
import android.widget.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[28],application=AscendApp::class,qualifiers="w411dp-h891dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class WidgetTest{
 private val app get()=RuntimeEnvironment.getApplication() as AscendApp
 private val repo get()=app.repository
 @Before fun setup()=runBlocking{repo.refresh();repo.create(Player(name="Eira"),emptyList())}
 @After fun close(){app.database.close()}
 private fun render(expanded:Boolean)=HabitWidget.views(app,repo.state.value!!,expanded).apply(app,FrameLayout(app))
 private fun shot(view:View,name:String,width:Int,height:Int){view.measure(View.MeasureSpec.makeMeasureSpec(width,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(height,View.MeasureSpec.EXACTLY));view.layout(0,0,width,height);val bitmap=android.graphics.Bitmap.createBitmap(width,height,android.graphics.Bitmap.Config.ARGB_8888);view.draw(android.graphics.Canvas(bitmap));val file=java.io.File("build/reports/screenshots/$name.png");file.parentFile!!.mkdirs();file.outputStream().use{bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it)};bitmap.recycle()}
 @Test fun remoteViewsHaveCompactExpandedAndUnconfiguredStates()=runBlocking{
 val simple=Habit(name="Ler uma página",xp=30);val timed=Habit(name="Estudar Direito",type=2,target=60);repo.habit(simple);repo.habit(timed)
 val compact=render(false);assertEquals(View.GONE,compact.findViewById<View>(R.id.widget_habit1).visibility);assertTrue(compact.findViewById<TextView>(R.id.widget_summary).text.contains("2 pendentes"));shot(compact,"widget-compacto-150",300,140)
 val expanded=render(true);assertEquals(View.VISIBLE,expanded.findViewById<View>(R.id.widget_habit2).visibility);assertTrue(expanded.findViewById<TextView>(R.id.widget_habit1).text.startsWith("Concluir"));assertTrue(expanded.findViewById<TextView>(R.id.widget_habit2).text.startsWith("Registrar"));shot(expanded,"widget-expandido-150",340,320)
 val unconfigured=HabitWidget.views(app,GameState(),true).apply(app,FrameLayout(app));assertTrue(unconfigured.findViewById<TextView>(R.id.widget_summary).text.contains("iniciar"))
 expanded.findViewById<View>(R.id.widget_habit2).performClick();val intent=Shadows.shadowOf(app).nextStartedActivity;assertEquals(timed.id,intent.getStringExtra("habit"));assertTrue(repo.state.value!!.records.isEmpty())
 }
 @Test fun actualPendingIntentCompletesSimpleHabitAndViewsUpdate()=runBlocking{
 val h=Habit(name="Arrumar a cama",xp=31);repo.habit(h);val view=render(true);view.findViewById<View>(R.id.widget_habit1).performClick();Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
 withTimeout(10000){repo.state.first{it?.record(h.id)?.complete==true}};assertEquals(31L,repo.state.value!!.xp)
 view.findViewById<View>(R.id.widget_habit1).performClick();Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();repo.refresh();assertEquals(31L,repo.state.value!!.xp);assertEquals(1,repo.state.value!!.records.size)
 val updated=render(true);assertTrue(updated.findViewById<TextView>(R.id.widget_summary).text.contains("1/1"));assertEquals(View.GONE,updated.findViewById<View>(R.id.widget_habit1).visibility)
 }
 @Test fun installedWidgetReceivesRepositoryChangesAndSchedulesDayRefresh()=runBlocking{
 val manager=AppWidgetManager.getInstance(app);val shadow=Shadows.shadowOf(manager);val info=AppWidgetProviderInfo().apply{provider=ComponentName(app,HabitWidget::class.java);initialLayout=R.layout.habit_widget}
 shadow.addBoundWidget(81,info);HabitWidget.refresh(app,repo.state.value!!)
 val h=Habit(name="Novo ritual",xp=10);repo.habit(h)
 val updated=shadow.getViewFor(81);assertTrue(updated.findViewById<TextView>(R.id.widget_summary).text.contains("1 pendentes"))
 repo.widgetComplete(h.id,today());assertTrue(shadow.getViewFor(81).findViewById<TextView>(R.id.widget_summary).text.contains("1/1"))
 repo.progress(h,today(),0);assertTrue(shadow.getViewFor(81).findViewById<TextView>(R.id.widget_summary).text.contains("0/1"))
 val alarms=Shadows.shadowOf(app.getSystemService(android.app.AlarmManager::class.java)).scheduledAlarms;assertTrue(alarms.isNotEmpty())
 }
 @Test @Config(qualifiers="w411dp-h891dp-night-mdpi") fun darkWidgetAndResizingKeepActionsWithinSpace()=runBlocking{
 (1..3).forEach{repo.habit(Habit(name="Ritual $it"))};val full=render(true);shot(full,"widget-escuro-150",340,340)
 val resized=HabitWidget.views(app,repo.state.value!!,true,1).apply(app,FrameLayout(app));assertEquals(View.VISIBLE,resized.findViewById<View>(R.id.widget_habit1).visibility);assertEquals(View.GONE,resized.findViewById<View>(R.id.widget_habit2).visibility)
 resized.findViewById<View>(R.id.widget_header).performClick();assertTrue(Shadows.shadowOf(app).nextStartedActivity.getBooleanExtra("widget_journey",false))
 }

}
