package br.com.ascend.aetheris

import android.app.*
import android.appwidget.*
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import kotlinx.coroutines.*
import java.time.*

data class WidgetDay(val due:Int,val complete:Int,val pending:List<Habit>)
fun widgetDay(s:GameState,date:String=today()):WidgetDay{val due=s.due(date).filter{!it.paused&&!it.archived&&!it.deleted};return WidgetDay(due.size,due.count{s.record(it.id,date)?.complete==true},due.filter{s.record(it.id,date)?.complete!=true}.take(3))}
open class HabitWidget:AppWidgetProvider(){
 override fun onUpdate(context:Context,manager:AppWidgetManager,ids:IntArray){load(context)}
 override fun onAppWidgetOptionsChanged(context:Context,manager:AppWidgetManager,id:Int,options:Bundle){load(context)}
 override fun onEnabled(context:Context){load(context)}
 override fun onDisabled(context:Context){if(allIds(context).isEmpty())context.getSystemService(AlarmManager::class.java).cancel(dayIntent(context))}
 override fun onReceive(context:Context,intent:Intent){
  if(intent.action in listOf("br.com.ascend.WIDGET_DAY",Intent.ACTION_BOOT_COMPLETED,Intent.ACTION_DATE_CHANGED,Intent.ACTION_TIMEZONE_CHANGED,Intent.ACTION_TIME_CHANGED)){load(context);return}
  if(intent.action=="br.com.ascend.WIDGET_COMPLETE"){
   val result=goAsync();CoroutineScope(Dispatchers.IO).launch{try{val repo=(context.applicationContext as AscendApp).repository;repo.refresh();runCatching{repo.widgetComplete(intent.getStringExtra("habit")?:"",intent.getStringExtra("date")?:"")};repo.state.value?.let{refresh(context,it)}}finally{result?.finish()}};return
  };super.onReceive(context,intent)
 }
 private fun load(context:Context){val result=goAsync();CoroutineScope(Dispatchers.IO).launch{try{val repo=(context.applicationContext as AscendApp).repository;repo.refresh();repo.state.value?.let{refresh(context,it)}}finally{result?.finish()}}}
 companion object{
  fun allIds(c:Context)=listOf(HabitWidget::class.java,ExpandedHabitWidget::class.java).flatMap{AppWidgetManager.getInstance(c).getAppWidgetIds(ComponentName(c,it)).toList()}
  fun dayIntent(c:Context)=PendingIntent.getBroadcast(c,501,Intent(c,HabitWidget::class.java).setAction("br.com.ascend.WIDGET_DAY"),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
  fun refresh(c:Context,s:GameState){
   val manager=AppWidgetManager.getInstance(c);val ids=allIds(c);if(ids.isEmpty())return
   ids.forEach{id->val options=manager.getAppWidgetOptions(id);val expanded=if(options.containsKey(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT))options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)>=150 else manager.getAppWidgetInfo(id)?.provider?.className?.endsWith("ExpandedHabitWidget")==true;val slots=if(options.containsKey(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT))((options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)-135)/54).coerceIn(0,3) else 3;manager.updateAppWidget(id,views(c,s,expanded,slots))}
   val next=LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
   c.getSystemService(AlarmManager::class.java).setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,next,dayIntent(c))
  }
  fun views(c:Context,s:GameState,expanded:Boolean,maxHabits:Int=3):RemoteViews{
   val v=RemoteViews(c.packageName,R.layout.habit_widget);val day=widgetDay(s)
   val open=PendingIntent.getActivity(c,500,Intent(c,MainActivity::class.java).putExtra("widget_journey",true).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
   v.setOnClickPendingIntent(R.id.widget_header,open)
   v.setTextViewText(R.id.widget_summary,if(s.player==null)"Abra o ASCEND para iniciar sua jornada." else "${day.complete}/${day.due} concluídos · ${day.due-day.complete} pendentes")
   v.setProgressBar(R.id.widget_progress,day.due.coerceAtLeast(1),day.complete,false)
   v.setTextViewText(R.id.widget_stats,if(s.player==null)"Tudo fica neste aparelho." else "Nível ${s.level} · ${s.xp} XP")
   v.setViewVisibility(R.id.widget_stats,if(expanded)View.VISIBLE else View.GONE)
   listOf(R.id.widget_habit1,R.id.widget_habit2,R.id.widget_habit3).forEachIndexed{i,viewId->val h=day.pending.getOrNull(i);v.setViewVisibility(viewId,if(expanded&&h!=null&&i<maxHabits)View.VISIBLE else View.GONE)
    if(h!=null){v.setTextViewText(viewId,"${if(h.type==0)"Concluir" else "Registrar"} · ${h.name}");val intent=if(h.type==0)Intent(c,HabitWidget::class.java).setAction("br.com.ascend.WIDGET_COMPLETE").putExtra("date",today()) else Intent(c,MainActivity::class.java).putExtra("widget_journey",true).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
     intent.setData(Uri.parse("ascend://widget/${h.id}/${today()}"));intent.putExtra("habit",h.id)
     val flags=PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
     val action=if(h.type==0)PendingIntent.getBroadcast(c,0,intent,flags) else PendingIntent.getActivity(c,0,intent,flags)
     v.setOnClickPendingIntent(viewId,action)
    }
   };return v
  }
 }
}
class ExpandedHabitWidget:HabitWidget()
