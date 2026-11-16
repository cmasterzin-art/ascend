package br.com.ascend.aetheris

import android.content.Context
import android.media.*

fun effectsAllowed(enabled:Boolean,ringer:Int,streamVolume:Int)=enabled&&ringer==AudioManager.RINGER_MODE_NORMAL&&streamVolume>0
fun effectFor(event:GameEvent):String?=when{
 event.level>0->"bell"
 event.highlights.any{it.startsWith("Expedição resgatada")||it.startsWith("Saga resgatada")||it.startsWith("Épica:")||it.startsWith("Rara:")||it.startsWith("Lendária:")}->"bell"
 event.message.startsWith("Entre as ruínas")->"wood"
 event.message=="Item equipado"->"iron"
 event.message=="A chama ganhou força"->"mark"
 else->null
}
class AetherisAudio(context:Context){
 private val manager=context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
 private val pool=SoundPool.Builder().setMaxStreams(1).setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()).build()
 private val loaded=mutableSetOf<Int>();private val samples=mapOf("mark" to R.raw.aether_mark,"bell" to R.raw.aether_bell,"wood" to R.raw.aether_wood,"iron" to R.raw.aether_iron,"page" to R.raw.aether_page).mapValues{pool.load(context,it.value,1)}
 var enabled=false;var volume=.35f
 init{pool.setOnLoadCompleteListener{_,sample,status->if(status==0)loaded.add(sample)}}
 fun play(name:String){if(!effectsAllowed(enabled,manager.ringerMode,manager.getStreamVolume(AudioManager.STREAM_MUSIC)))return;val sample=samples[name]?:return;if(sample in loaded)pool.play(sample,volume,volume,1,0,1f)}
 fun close(){pool.release()}
}
