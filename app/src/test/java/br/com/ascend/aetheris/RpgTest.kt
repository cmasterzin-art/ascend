package br.com.ascend.aetheris

import androidx.room.Room
import kotlinx.coroutines.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[28],application=AscendApp::class)
class RpgTest {
    private lateinit var db:GameDatabase
    private lateinit var repo:GameRepository
    @Before fun setup()=runBlocking{db=Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(),GameDatabase::class.java).build();repo=GameRepository(db);repo.refresh();repo.create(Player(name="Kael"),emptyList())}
    @After fun close(){db.close()}
    private suspend fun earn(){val h=Habit(name="Estudar",xp=2500,coins=300);repo.habit(h);repo.progress(h,today(),1)}
    @Test fun skillCostsOnePointAndGrantsPersistentEquippableCosmetic()=runBlocking{
        earn();val before=skillPoints(repo.state.value!!);repo.unlockSkill("ember")
        assertEquals(before-1,skillPoints(repo.state.value!!))
        val item=repo.state.value!!.assets.first{it.id=="ember-amulet"};repo.equip(item)
        assertTrue(runCatching{repo.unlockSkill("ember")}.isFailure)
        val fresh=GameRepository(db);fresh.refresh();assertTrue(fresh.state.value!!.assets.first{it.id==item.id}.equipped)
        val backup=Backup.decode(Backup.encode(fresh.state.value!!));repo.importState(backup);assertTrue(repo.state.value!!.assets.any{it.id=="skill:ember"})
    }
    @Test fun lockedSkillAndDirectPurchaseCannotBypassProgression()=runBlocking{
        assertTrue(runCatching{repo.unlockSkill("ember")}.isFailure)
        earn();assertTrue(runCatching{repo.unlockSkill("will")}.isFailure)
        assertTrue(runCatching{repo.buy(shopItems.first{it.id=="will-helm"})}.isFailure)
        val item=shopItems.first{it.id=="silver-blade"};assertTrue(runCatching{repo.buy(item.copy(cost=0))}.isFailure)
    }
    @Test fun oneItemPerSlotAndUndoCannotFarmSkillPoints()=runBlocking{
        earn();repo.unlockSkill("ember");repo.buy(shopItems.first{it.id=="aether-amulet"})
        repo.equip(repo.state.value!!.assets.first{it.id=="ember-amulet"});repo.equip(repo.state.value!!.assets.first{it.id=="aether-amulet"})
        assertEquals(1,repo.state.value!!.assets.count{it.kind=="accessory"&&it.equipped})
        val h=repo.state.value!!.habits.single();repo.progress(h,today(),0)
        assertEquals(0,skillPoints(repo.state.value!!));assertTrue(repo.state.value!!.assets.any{it.id=="skill:ember"})
        repo.progress(h,today(),1);assertTrue(runCatching{repo.unlockSkill("ember")}.isFailure)
    }
    @Test fun restoredVillageChangesDialogueAndSurvivesReload()=runBlocking{
        earn();val before=villagerDialogue(0,0);repo.restore(0);repo.restore(0)
        val fresh=GameRepository(db);fresh.refresh();val stage=fresh.state.value!!.assets.first{it.id=="building:0"}.stage
        assertEquals(2,stage);assertNotEquals(before,villagerDialogue(0,stage));assertTrue(chapterUnlocked(2,fresh.state.value!!))
    }
    @Test fun versionTwoBackupPreservesProgressAndOldEquipment()=runBlocking{
        earn();repo.rest(today(),true);repo.buy(shopItems.first{it.id=="cloak"})
        val old=repo.state.value!!;val gson=com.google.gson.Gson()
        val envelope=gson.fromJson(Backup.encode(old),BackupEnvelope::class.java)
        repo.importState(Backup.decode(gson.toJson(envelope.copy(version=2))))
        assertEquals(old,repo.state.value)
        val cloak=repo.state.value!!.assets.first{it.id=="cloak"}
        repo.equip(cloak.copy(kind="weapon"))
        assertEquals("outfit",repo.state.value!!.assets.first{it.id=="cloak"}.kind)
        repo.unlockSkill("ember");assertEquals(old.xp,repo.state.value!!.xp)
    }
}
