package br.com.ascend.aetheris

import androidx.compose.runtime.saveable.Saver
import com.google.gson.Gson

// Small editor requests in Android saved state, separate from Room and backup formats.
private val draftJson=Gson()
val HabitEditorState=Saver<Habit?,String>(save={it?.let(draftJson::toJson)?:""},restore={value->if(value.isBlank())null else runCatching{draftJson.fromJson(value,Habit::class.java)}.getOrNull()})
private data class RecordEditorRequest(val habit:Habit,val record:HabitRecord?)
val RecordEditorState=Saver<Pair<Habit,HabitRecord?>?,String>(save={it?.let{draftJson.toJson(RecordEditorRequest(it.first,it.second))}?:""},restore={value->if(value.isBlank())null else runCatching{draftJson.fromJson(value,RecordEditorRequest::class.java).let{it.habit to it.record}}.getOrNull()})
