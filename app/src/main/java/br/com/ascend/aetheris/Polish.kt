package br.com.ascend.aetheris

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Presentation only: database/backup dates remain ISO. */
fun displayDate(iso:String):String=runCatching{LocalDate.parse(iso).format(DateTimeFormatter.ofPattern("dd/MM/yyyy",Locale.forLanguageTag("pt-BR")))}.getOrDefault(iso)
fun widgetDate(iso:String):String=runCatching{LocalDate.parse(iso).format(DateTimeFormatter.ofPattern("EEE · d MMM",Locale.forLanguageTag("pt-BR")))}.getOrDefault(iso)
fun displayPeriod(start:String,end:String)=if(start==end)displayDate(start) else "${displayDate(start)} a ${displayDate(end)}"
@Composable fun ReadingBlock(text:String,label:String?=null,color:Color=Ink){
 Column(Modifier.fillMaxWidth().padding(vertical=4.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
  label?.let{Eyebrow(it,Muted)}
  text.split("\n\n").filter{it.isNotBlank()}.forEach{paragraph->Text(paragraph,color=color,fontSize=15.sp,lineHeight=25.sp)}
 }
}
@Composable fun StatusSeal(text:String,color:Color=Blue){
 Text(text,Modifier.background(color.copy(alpha=.10f),CutCornerShape(4.dp)).border(1.dp,color.copy(alpha=.25f),CutCornerShape(4.dp)).padding(horizontal=10.dp,vertical=7.dp),color=color,fontSize=12.sp,lineHeight=18.sp,fontWeight=FontWeight.Medium)
}
@Composable fun Spoils(xp:Int,coins:Int,item:String=""){
 Column(Modifier.fillMaxWidth().background(Gold.copy(alpha=.07f),CutCornerShape(5.dp)).padding(14.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){
  Eyebrow("Recompensa final",Gold)
  Row(horizontalArrangement=Arrangement.spacedBy(24.dp)){Column{Text("$xp",fontFamily=SagaFont,fontSize=23.sp,color=Ink);Text("XP",color=Muted,fontSize=11.sp)};Column{Text("$coins",fontFamily=SagaFont,fontSize=23.sp,color=Ink);Text("Fragmentos",color=Muted,fontSize=11.sp)}}
  if(item.isNotBlank())Text(item,color=Gold,fontSize=13.sp)
 }
}
@Composable fun ChronicleNumber(value:String,label:String,color:Color=Sepia){
 Column(Modifier.padding(vertical=4.dp),verticalArrangement=Arrangement.spacedBy(4.dp)){Text(value,color=color,fontFamily=SagaFont,fontSize=28.sp,lineHeight=34.sp);Text(label,color=color,fontSize=12.sp,lineHeight=18.sp)}
}
