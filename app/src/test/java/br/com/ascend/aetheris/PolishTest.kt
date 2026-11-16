package br.com.ascend.aetheris

import org.junit.Test
import org.junit.Assert.*
import java.util.Locale
import java.util.TimeZone

class PolishTest{
 @Test fun brazilianDatesDoNotShiftWhenLocaleOrZoneChanges(){
  val locale=Locale.getDefault();val zone=TimeZone.getDefault()
  try{Locale.setDefault(Locale.US);TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Honolulu"));assertEquals("16/11/2026",displayDate("2026-11-16"));assertEquals("29/02/2024",displayDate("2024-02-29"));assertEquals("sem data",displayDate("sem data"));assertTrue(widgetDate("2026-11-16").contains("16"))}finally{Locale.setDefault(locale);TimeZone.setDefault(zone)}
 }
 @Test fun widgetCapacityAccountsForSpaceFontAndWidth(){
  assertEquals(3,widgetSpace(340,300,1f,true).slots)
  assertEquals(0,widgetSpace(110,220,1.3f,true).slots);assertFalse(widgetSpace(110,220,1.3f,true).numbers)
  assertTrue(widgetSpace(320,220,1.3f,true).slots<widgetSpace(340,340,1f,true).slots)
  assertEquals(0,widgetSpace(340,340,1f,false).slots)
 }
}
