package br.com.ascend.aetheris

import android.app.*
import android.appwidget.*
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import android.widget.Toast
import kotlinx.coroutines.*
import java.time.*

data class WidgetDay(val due:Int,val complete:Int,val pending:List<Habit>)
fun widgetDay(s:GameState,date:String=today()):WidgetDay{val due=s.due(date).filter{!it.paused&&!it.archived&&!it.deleted};return WidgetDay(due.size,due.count{s.record(it.id,date)?.complete==true},due.filter{s.record(it.id,date)?.complete!=true}.take(3))}
fun widgetRegistrationHabit(s:GameState,id:String,date:String?):Habit?=if(date!=null&&date!=today())null else s.habits.find{it.id==id&&it.type!=0&&!it.paused&&!it.archived&&!it.deleted&&scheduled(it,today(),s.restDates,s.plans)}
data class WidgetSpace(val numbers:Boolean,val slots:Int)
fun widgetSpace(height:Int,width:Int,fontScale:Float,expanded:Boolean):WidgetSpace{
 val scale=fontScale.coerceAtLeast(1f);val numbers=height>=if(expanded)(148*scale).toInt() else (164*scale).toInt()
 val base=if(numbers)(158*scale).toInt() else (104*scale).toInt()
 val row=(if(width<260)64 else 52)*scale
 return WidgetSpace(numbers,if(expanded)((height-base)/row).toInt().coerceIn(0,3) else 0)
}
open class HabitWidget:AppWidgetProvider(){
 override fun onUpdate(context:Context,manager:AppWidgetManager,ids:IntArray){load(context)}
 override fun onAppWidgetOptionsChanged(context:Context,manager:AppWidgetManager,id:Int,options:Bundle){load(context)}
 override fun onEnabled(context:Context){load(context)}
 override fun onDisabled(context:Context){if(allIds(context).isEmpty())context.getSystemService(AlarmManager::class.java).cancel(dayIntent(context))}
 override fun onReceive(context:Context,intent:Intent){
  if(intent.action in listOf("br.com.ascend.WIDGET_DAY",Intent.ACTION_BOOT_COMPLETED,Intent.ACTION_DATE_CHANGED,Intent.ACTION_TIMEZONE_CHANGED,Intent.ACTION_TIME_CHANGED)){load(context);return}
  if(intent.action=="br.com.ascend.WIDGET_COMPLETE"){
   val result=goAsync();CoroutineScope(Dispatchers.IO).launch{
    try{
     val repo=(context.applicationContext as AscendApp).repository
     try{repo.widgetComplete(intent.getStringExtra("habit")?:"",intent.getStringExtra("date")?:"")}
     catch(e:CancellationException){throw e}
     catch(e:Exception){withContext(Dispatchers.Main){Toast.makeText(context,if(e is IllegalArgumentException)e.message?:"Abra a Jornada para registrar este hábito." else "Não foi possível registrar. Abra a Jornada e tente novamente.",Toast.LENGTH_LONG).show()}}
     repo.refresh();repo.state.value?.let{refresh(context,it)}
    }catch(e:CancellationException){throw e}catch(_:Exception){showUnavailable(context)}finally{result?.finish()}
   };return
  };super.onReceive(context,intent)
 }
 private fun load(context:Context){val result=goAsync();CoroutineScope(Dispatchers.IO).launch{try{val repo=(context.applicationContext as AscendApp).repository;repo.refresh();repo.state.value?.let{refresh(context,it)}}catch(e:CancellationException){throw e}catch(_:Exception){showUnavailable(context)}finally{result?.finish()}}}
 private fun showUnavailable(context:Context){runCatching{val manager=AppWidgetManager.getInstance(context);allIds(context).forEach{id->val views=RemoteViews(context.packageName,R.layout.habit_widget);views.setTextViewText(R.id.widget_summary,"Abra a Jornada para atualizar os rituais.");listOf(R.id.widget_progress,R.id.widget_numbers,R.id.widget_stats,R.id.widget_habit1,R.id.widget_habit2,R.id.widget_habit3).forEach{views.setViewVisibility(it,View.GONE)};val open=PendingIntent.getActivity(context,500,Intent(context,MainActivity::class.java).putExtra("widget_journey",true).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE);views.setOnClickPendingIntent(R.id.widget_header,open);views.setOnClickPendingIntent(R.id.widget_open,open);manager.updateAppWidget(id,views)}}}
 companion object{
  fun allIds(c:Context)=listOf(HabitWidget::class.java,ExpandedHabitWidget::class.java).flatMap{AppWidgetManager.getInstance(c).getAppWidgetIds(ComponentName(c,it)).toList()}
  fun dayIntent(c:Context)=PendingIntent.getBroadcast(c,501,Intent(c,HabitWidget::class.java).setAction("br.com.ascend.WIDGET_DAY"),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
  fun refresh(c:Context,s:GameState){
   val manager=AppWidgetManager.getInstance(c);val ids=allIds(c);if(ids.isEmpty())return
   ids.forEach{id->val options=manager.getAppWidgetOptions(id);val height=options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT).takeIf{it>0}?:if(manager.getAppWidgetInfo(id)?.provider?.className?.endsWith("ExpandedHabitWidget")==true)340 else 140;val width=options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH).takeIf{it>0}?:300;val expanded=height>=150;val space=widgetSpace(height,width,c.resources.configuration.fontScale,expanded);manager.updateAppWidget(id,views(c,s,expanded,space.slots,height,width))}
   val next=LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
   c.getSystemService(AlarmManager::class.java).setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,next,dayIntent(c))
  }
  fun views(c:Context,s:GameState,expanded:Boolean,maxHabits:Int=3,height:Int=if(expanded)340 else 140,width:Int=300):RemoteViews{
   val v=RemoteViews(c.packageName,R.layout.habit_widget);val date=today();val day=widgetDay(s,date);val space=widgetSpace(height,width,c.resources.configuration.fontScale,expanded);val remaining=day.due-day.complete;val pendingLabel=if(remaining==1)"1 pendente" else "$remaining pendentes"
   v.setInt(R.id.widget_root,"setBackgroundResource",if(expanded)R.drawable.widget_surface_expanded else R.drawable.widget_surface)
   val open=PendingIntent.getActivity(c,500,Intent(c,MainActivity::class.java).putExtra("widget_journey",true).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
   v.setViewVisibility(R.id.widget_sigil,if(width<210)View.GONE else View.VISIBLE)
   v.setOnClickPendingIntent(R.id.widget_header,open);v.setOnClickPendingIntent(R.id.widget_open,open)
   v.setInt(R.id.widget_summary,"setMaxLines",if(height<150)1 else 2)
   v.setTextViewText(R.id.widget_percent,if(day.due==0)"—" else "${day.complete*100/day.due}%")
   v.setTextViewText(R.id.widget_period,"Agenda de hoje\n${widgetDate(date)}")
   v.setViewVisibility(R.id.widget_numbers,if(space.numbers&&s.player!=null)View.VISIBLE else View.GONE)

   v.setTextViewText(R.id.widget_summary,if(s.player==null)"Abra o ASCEND para iniciar sua jornada." else if(day.due==0)"Nenhum ritual programado hoje." else "${day.complete}/${day.due} ${if(day.due==1)"concluído" else "concluídos"} · $pendingLabel")
   if(height<(135*c.resources.configuration.fontScale).toInt())v.setTextViewText(R.id.widget_summary,if(s.player==null)"Abra o ASCEND para iniciar." else if(day.due==0)"Hoje, sem rituais." else "${day.complete}/${day.due} · $pendingLabel")
   v.setViewVisibility(R.id.widget_progress,if(s.player==null||day.due==0)View.GONE else View.VISIBLE)
   v.setProgressBar(R.id.widget_progress,day.due.coerceAtLeast(1),day.complete,false)
   v.setTextViewText(R.id.widget_stats,if(s.player==null)"Tudo fica neste aparelho." else "${s.player.name} · Nível ${s.level} · ${s.xp} XP")
   v.setViewVisibility(R.id.widget_stats,if(expanded&&height>=160)View.VISIBLE else View.GONE)
   listOf(R.id.widget_habit1,R.id.widget_habit2,R.id.widget_habit3).forEachIndexed{i,viewId->val h=day.pending.getOrNull(i);v.setViewVisibility(viewId,if(expanded&&h!=null&&i<minOf(maxHabits,space.slots))View.VISIBLE else View.GONE)
    if(h!=null){v.setInt(viewId,"setBackgroundResource",if(h.type==0)R.drawable.widget_complete else R.drawable.widget_secondary);v.setTextColor(viewId,c.getColor(if(h.type==0)R.color.widget_action_text else R.color.widget_text));v.setContentDescription(viewId,"${if(h.type==0)"Concluir hábito simples" else "Abrir registro de progresso"}: ${h.name}");v.setTextViewText(viewId,"${if(h.type==0)"Concluir" else "Registrar"} · ${h.name}");val intent=if(h.type==0)Intent(c,HabitWidget::class.java).setAction("br.com.ascend.WIDGET_COMPLETE").putExtra("date",date) else Intent(c,MainActivity::class.java).putExtra("widget_journey",true).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
     intent.putExtra("date",date)
     intent.setData(Uri.parse("ascend://widget/${h.id}/$date"));intent.putExtra("habit",h.id)
     val flags=PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
     val action=if(h.type==0)PendingIntent.getBroadcast(c,0,intent,flags) else PendingIntent.getActivity(c,0,intent,flags)
     v.setOnClickPendingIntent(viewId,action)
    }
   };return v
  }
 }
}
class ExpandedHabitWidget:HabitWidget()
