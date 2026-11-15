package br.com.ascend.aetheris

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import kotlinx.coroutines.*
import java.time.*

object Reminders {
    fun sync(context:Context,habits:List<Habit>){habits.forEach{schedule(context,it)}}
    fun schedule(context:Context,h:Habit){
        val alarms=context.getSystemService(AlarmManager::class.java)
        val intent=Intent(context,ReminderReceiver::class.java).setData(android.net.Uri.parse("ascend://habit/${h.id}")).putExtra("habit",h.id)
        val pending=PendingIntent.getBroadcast(context,0,intent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarms.cancel(pending)
        if(!h.reminder||h.paused||h.archived||h.deleted)return
        val time=runCatching{LocalTime.parse(h.time)}.getOrNull()?:return
        var next=LocalDate.now().atTime(time)
        repeat(9){if(next.isAfter(LocalDateTime.now())&&scheduled(h,next.toLocalDate().toString())){alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),pending);return};next=next.plusDays(1)}
    }
    fun show(context:Context,h:Habit){
        if(Build.VERSION.SDK_INT>=33 && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)return
        val nm=context.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel("habits","Lembretes de hábitos",NotificationManager.IMPORTANCE_DEFAULT))
        val open=PendingIntent.getActivity(context,0,Intent(context,MainActivity::class.java),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        nm.notify(h.id.hashCode(),Notification.Builder(context,"habits").setSmallIcon(br.com.ascend.aetheris.R.drawable.ic_ascend).setContentTitle(h.name).setContentText("Seu próximo passo em Aetheris está aqui. No seu ritmo.").setContentIntent(open).setAutoCancel(true).build())
    }
}
class ReminderReceiver:BroadcastReceiver(){override fun onReceive(context:Context,intent:Intent){val pending=goAsync();CoroutineScope(Dispatchers.IO).launch{try{val d=(context.applicationContext as AscendApp).database.dao();val h=d.habits().find{it.id==intent.getStringExtra("habit")}?:return@launch;if(h.reminder&&!h.paused&&!h.archived&&!h.deleted&&scheduled(h,today())&&d.records().none{it.habitId==h.id&&it.date==today()&&it.complete})Reminders.show(context,h);Reminders.schedule(context,h)}finally{pending.finish()}}}}
class BootReceiver:BroadcastReceiver(){override fun onReceive(context:Context,intent:Intent){if(intent.action!=Intent.ACTION_BOOT_COMPLETED)return;val pending=goAsync();CoroutineScope(Dispatchers.IO).launch{try{Reminders.sync(context,(context.applicationContext as AscendApp).database.dao().habits())}finally{pending.finish()}}}}
