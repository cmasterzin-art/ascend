package br.com.ascend.aetheris

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import kotlinx.coroutines.*
import java.time.*

/** Resolve in the device zone, including DST gaps, and skip all planned rest dates. */
fun nextReminderAt(h:Habit,restDates:Set<String>,now:ZonedDateTime=ZonedDateTime.now(),plans:List<PlanException> = emptyList()):ZonedDateTime? {
    if(!h.reminder||h.paused||h.archived||h.deleted)return null
    val time=runCatching{LocalTime.parse(h.time)}.getOrNull()?:return null
    val start=maxOf(now.toLocalDate(),LocalDate.parse(h.created))
    for(offset in 0..(restDates.size+1)*7){
        val date=start.plusDays(offset.toLong())
        if(scheduled(h,date.toString(),restDates,plans)){
            val candidate=date.atTime(time).atZone(now.zone)
            if(candidate.isAfter(now))return candidate
        }
    }
    return null
}
object Reminders {
    fun sync(context:Context,habits:List<Habit>,restDates:Set<String> = emptySet(),plans:List<PlanException> = emptyList()){habits.forEach{schedule(context,it,restDates,plans)}}
    fun schedule(context:Context,h:Habit,restDates:Set<String> = emptySet(),plans:List<PlanException> = emptyList()){
        val alarms=context.getSystemService(AlarmManager::class.java)
        val intent=Intent(context,ReminderReceiver::class.java).setData(android.net.Uri.parse("ascend://habit/${h.id}")).putExtra("habit",h.id)
        val pending=PendingIntent.getBroadcast(context,0,intent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarms.cancel(pending)
        val next=nextReminderAt(h,restDates,plans=plans)?:return
        alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,next.toInstant().toEpochMilli(),pending)
    }
    fun show(context:Context,h:Habit){
        if(Build.VERSION.SDK_INT>=33 && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)return
        val nm=context.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel("habits","Lembretes de hábitos",NotificationManager.IMPORTANCE_DEFAULT))
        val open=PendingIntent.getActivity(context,0,Intent(context,MainActivity::class.java),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        nm.notify(h.id.hashCode(),Notification.Builder(context,"habits").setSmallIcon(br.com.ascend.aetheris.R.drawable.ic_ascend).setContentTitle(h.name).setContentText("Seu próximo passo em Aetheris está aqui. No seu ritmo.").setContentIntent(open).setAutoCancel(true).build())
    }
    fun notifyTimeForge(context:Context,id:Int,title:String,body:String){
        if(Build.VERSION.SDK_INT>=33 && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)return
        val nm=context.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel("time_forge","Forja do Tempo e Agenda",NotificationManager.IMPORTANCE_DEFAULT))
        val open=PendingIntent.getActivity(context,id,Intent(context,MainActivity::class.java),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        nm.notify(id,Notification.Builder(context,"time_forge").setSmallIcon(br.com.ascend.aetheris.R.drawable.ic_ascend).setContentTitle(title).setContentText(body).setContentIntent(open).setAutoCancel(true).build())
    }
    fun scheduleAgendaAlert(context:Context,key:String,triggerAtMillis:Long,title:String,body:String){
        val alarms=context.getSystemService(AlarmManager::class.java)
        val intent=Intent(context,ReminderReceiver::class.java).setData(android.net.Uri.parse("ascend://agenda/$key")).putExtra("agenda_title",title).putExtra("agenda_body",body).putExtra("agenda_id",key.hashCode())
        val pending=PendingIntent.getBroadcast(context,0,intent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarms.cancel(pending)
        if(triggerAtMillis>System.currentTimeMillis()){
            alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,triggerAtMillis,pending)
        }
    }
}
class ReminderReceiver:BroadcastReceiver(){override fun onReceive(context:Context,intent:Intent){
    val agendaTitle=intent.getStringExtra("agenda_title")
    if(!agendaTitle.isNullOrBlank()){
        Reminders.notifyTimeForge(context,intent.getIntExtra("agenda_id",1750),agendaTitle,intent.getStringExtra("agenda_body").orEmpty())
        return
    }
    val pending=goAsync();CoroutineScope(Dispatchers.IO).launch{try{val d=(context.applicationContext as AscendApp).database.dao();val h=d.habits().find{it.id==intent.getStringExtra("habit")}?:return@launch;val rests=d.restDays().map{it.date}.toSet();val plans=d.plans();if(h.reminder&&!h.paused&&!h.archived&&!h.deleted&&scheduled(h,today(),rests,plans)&&d.records().none{it.habitId==h.id&&it.date==today()&&it.complete})Reminders.show(context,h);Reminders.schedule(context,h,rests,plans)}finally{pending.finish()}}
}}
class BootReceiver:BroadcastReceiver(){override fun onReceive(context:Context,intent:Intent){if(intent.action!=Intent.ACTION_BOOT_COMPLETED)return;val pending=goAsync();CoroutineScope(Dispatchers.IO).launch{try{val d=(context.applicationContext as AscendApp).database.dao();Reminders.sync(context,d.habits(),d.restDays().map{it.date}.toSet(),d.plans())}finally{pending.finish()}}}}
