package br.com.ascend.aetheris

import org.junit.Test
import org.junit.Assert.*

class CatalogTest {
    @Test fun catalogHasCompleteStableIndependentModels(){
        assertEquals(102,habitTemplates.size);assertEquals(9,catalogCategories.size)
        assertEquals(habitTemplates.size,habitTemplates.map{it.id}.distinct().size)
        attributes.forEach{a->assertTrue(habitTemplates.count{it.category==a}>=15)}
        habitTemplates.forEach{t->
            assertTrue(t.id.matches(Regex("[a-z][a-z0-9_]+")));assertTrue(t.description.isNotBlank());assertTrue(t.narrativeName.isNotBlank());assertTrue(t.tags.isNotEmpty());assertTrue(t.estimatedMinutes>0)
            val a=t.instantiate();val b=t.instantiate();assertNotEquals(a.id,b.id);assertEquals(t.id,a.templateId);validateHabitInput(a)
            assertEquals(t.xp,a.xp);assertEquals(t.target,a.target)
        }
    }
    @Test fun searchUnderstandsNarrativeNamesAccentsAndTags(){
        assertEquals("study_law",filterTemplates(CatalogFilter(query="tomos justica")).single().id)
        assertTrue(filterTemplates(CatalogFilter(query="PROGRAMAÇÃO")).any{it.id=="programming"})
        assertTrue(filterTemplates(CatalogFilter(query="fisico")).any{it.id=="walk"})
        assertTrue(filterTemplates(CatalogFilter(query="nada_xyz_123")).isEmpty())
    }
    @Test fun everyFilterAndOrderIsApplied(){
        val f=CatalogFilter(category="Vigor",difficulty=0,type=2,attribute=1,frequency=2,duration=1,order=2)
        val found=filterTemplates(f);assertTrue(found.isNotEmpty())
        assertTrue(found.all{it.category=="Vigor"&&it.difficulty==0&&it.type==2&&it.attribute==1&&it.days.split(',').size in 2..6&&it.estimatedMinutes<=15})
        assertEquals(found.map{it.estimatedMinutes}.sorted(),found.map{it.estimatedMinutes})
        assertTrue(filterTemplates(CatalogFilter(frequency=3)).all{it.days.split(',').size==1})
        assertTrue(filterTemplates(CatalogFilter(frequency=1)).all{it.days.split(',').size==7})
        for(order in 0..3){val items=filterTemplates(CatalogFilter(order=order));assertEquals(102,items.size)}
        assertEquals(filterTemplates(CatalogFilter(order=3)).map{it.difficulty}.sorted(),filterTemplates(CatalogFilter(order=3)).map{it.difficulty})
    }
    @Test fun recommendationsAreLocalExplicitAndDismissible(){
        assertTrue(recommendations(emptySet(),emptySet(),emptyList()).isEmpty())
        val initial=recommendations(setOf("Intelecto"),emptySet(),emptyList());assertEquals(4,initial.size)
        val after=recommendations(setOf("Intelecto"),setOf(initial[0].id),listOf(initial[1].instantiate()))
        assertTrue(after.none{it.id in initial.take(2).map{it.id}});assertTrue(after.all{it.category=="Intelecto"})
    }
    @Test fun routineDefinitionsAndRestModelsAreValid(){
        assertEquals(6,routineTemplates.size)
        routineTemplates.forEach{r->assertEquals(4,r.choices.size);assertEquals(4,r.choices.map{it.templateId}.distinct().size);r.choices.forEach{validateHabitInput(it.instantiate())}}
        assertEquals(15,routineTemplates.first().choices.last().instantiate().target)
        assertTrue(habitTemplates.filter{it.attribute==3}.all{!it.trackStreak})
    }
}
