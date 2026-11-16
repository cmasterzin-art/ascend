package br.com.ascend.aetheris

import android.content.res.Configuration
import android.view.*
import android.widget.*
import kotlinx.coroutines.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[28],application=AscendApp::class,qualifiers="w320dp-h740dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class WidgetPolishTest{
 private val app get()=RuntimeEnvironment.getApplication() as AscendApp
 @Before fun setup()=runBlocking{app.repository.refresh();app.repository.create(Player(name="Freya"),emptyList())}
 @After fun close(){app.database.close()}
 private fun render(s:GameState,expanded:Boolean,height:Int,scale:Float,name:String):View{
 val ctx=app.createConfigurationContext(Configuration(app.resources.configuration).apply{fontScale=scale})
 val view=HabitWidget.views(ctx,s,expanded,height=height,width=220).apply(ctx,FrameLayout(ctx));view.measure(View.MeasureSpec.makeMeasureSpec(220,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(height,View.MeasureSpec.EXACTLY));view.layout(0,0,220,height)
 val bitmap=android.graphics.Bitmap.createBitmap(220,height,android.graphics.Bitmap.Config.ARGB_8888);view.draw(android.graphics.Canvas(bitmap));val file=java.io.File("build/reports/screenshots/$name.png");file.parentFile!!.mkdirs();file.outputStream().use{bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it)};bitmap.recycle();return view
 }
 @Test fun compactEmptyAndUnconfiguredStatesFitSmallHeight(){
 val noPlayer=render(GameState(),false,110,1.3f,"widget-sem-personagem-151");assertEquals(View.GONE,noPlayer.findViewById<View>(R.id.widget_progress).visibility);assertTrue(noPlayer.findViewById<TextView>(R.id.widget_summary).text.contains("iniciar"));assertTrue(noPlayer.findViewById<View>(R.id.widget_summary).bottom<=110)
 val empty=render(app.repository.state.value!!,false,110,1.3f,"widget-sem-rituais-151");assertTrue(empty.findViewById<TextView>(R.id.widget_summary).text.contains("sem rituais"));assertEquals(View.GONE,empty.findViewById<View>(R.id.widget_numbers).visibility);assertTrue(empty.findViewById<View>(R.id.widget_progress).bottom<=110)
 }
 @Test @Config(qualifiers="w320dp-h740dp-night-mdpi") fun longNamesKeepOneAccessibleActionAndCompletionFeedback()=runBlocking{
 val h=Habit(name="Preparar o material do meu próximo estudo com tranquilidade",xp=17);app.repository.habit(h);app.repository.habit(Habit(name="Praticar leitura",type=1,target=20))
 val before=render(app.repository.state.value!!,true,320,1.3f,"widget-estreito-130-151");val button=before.findViewById<Button>(R.id.widget_habit1);assertTrue(button.contentDescription.contains(h.name));assertEquals(View.VISIBLE,button.visibility);assertTrue(button.bottom<=320)
 app.repository.widgetComplete(h.id,today());val after=render(app.repository.state.value!!,true,320,1.3f,"widget-apos-concluir-151");assertEquals("50%",after.findViewById<TextView>(R.id.widget_percent).text.toString());assertTrue(after.findViewById<Button>(R.id.widget_habit1).text.startsWith("Registrar"));assertEquals(17L,app.repository.state.value!!.xp)
 }
}
