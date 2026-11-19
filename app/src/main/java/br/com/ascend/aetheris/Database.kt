package br.com.ascend.aetheris

import androidx.room.*

@Dao
interface GameDao {
    @Query("SELECT * FROM plan_exceptions ORDER BY date") suspend fun plans():List<PlanException>
    @Query("SELECT * FROM focus_sessions ORDER BY endedAt DESC") suspend fun focusSessions():List<FocusSession>
    @Upsert suspend fun plan(v:PlanException)
    @Upsert suspend fun focusSession(v:FocusSession)
    @Query("DELETE FROM plan_exceptions WHERE id=:id") suspend fun deletePlan(id:String)
    @Query("DELETE FROM plan_exceptions") suspend fun clearPlans()
    @Query("DELETE FROM focus_sessions") suspend fun clearFocusSessions()

    @Query("SELECT * FROM rest_days ORDER BY date") suspend fun restDays():List<RestDay>
    @Upsert suspend fun restDay(v:RestDay)
    @Query("DELETE FROM rest_days WHERE date=:date") suspend fun deleteRestDay(date:String)
    @Query("DELETE FROM rest_days") suspend fun clearRestDays()
    @Query("SELECT * FROM player LIMIT 1") suspend fun player():Player?
    @Query("SELECT * FROM habits ORDER BY position") suspend fun habits():List<Habit>
    @Query("SELECT * FROM records ORDER BY date DESC") suspend fun records():List<HabitRecord>
    @Query("SELECT * FROM ledger") suspend fun ledger():List<Reward>
    @Query("SELECT * FROM quests") suspend fun quests():List<Quest>
    @Query("SELECT * FROM claims") suspend fun claims():List<Claim>
    @Query("SELECT * FROM assets") suspend fun assets():List<Asset>
    @Query("SELECT * FROM achievements") suspend fun achievements():List<Unlocked>
    @Query("SELECT * FROM diary ORDER BY date DESC") suspend fun journal():List<Journal>
    @Query("SELECT * FROM routines") suspend fun routines():List<Routine>
    @Query("SELECT * FROM focus LIMIT 1") suspend fun focus():Focus?
    @Upsert suspend fun player(v:Player)
    @Upsert suspend fun habit(v:Habit)
    @Upsert suspend fun record(v:HabitRecord)
    @Upsert suspend fun reward(v:Reward)
    @Upsert suspend fun quest(v:Quest)
    @Upsert suspend fun claim(v:Claim)
    @Upsert suspend fun asset(v:Asset)
    @Upsert suspend fun unlock(v:Unlocked)
    @Upsert suspend fun journal(v:Journal)
    @Upsert suspend fun routine(v:Routine)
    @Upsert suspend fun focus(v:Focus)
    @Query("DELETE FROM focus") suspend fun clearFocus()
    @Query("SELECT * FROM paths ORDER BY position, name") suspend fun paths():List<PersonalPath>
    @Query("SELECT * FROM path_stages ORDER BY position") suspend fun pathStages():List<PathStage>
    @Query("SELECT * FROM path_tasks ORDER BY position, deadline") suspend fun pathTasks():List<PathTask>
    @Query("SELECT * FROM path_subtasks ORDER BY position") suspend fun pathSubtasks():List<PathSubtask>
    @Query("SELECT * FROM path_habit_links ORDER BY created") suspend fun pathHabitLinks():List<PathHabitLink>
    @Query("SELECT * FROM time_blocks ORDER BY date, startTime") suspend fun timeBlocks():List<TimeBlock>
    @Query("SELECT * FROM agenda_events ORDER BY date, startTime") suspend fun agendaEvents():List<AgendaEvent>
    @Query("SELECT * FROM inbox_items ORDER BY created DESC") suspend fun inboxItems():List<InboxItem>
    @Upsert suspend fun path(v:PersonalPath)
    @Upsert suspend fun pathStage(v:PathStage)
    @Upsert suspend fun pathTask(v:PathTask)
    @Upsert suspend fun pathSubtask(v:PathSubtask)
    @Upsert suspend fun pathHabitLink(v:PathHabitLink)
    @Upsert suspend fun timeBlock(v:TimeBlock)
    @Upsert suspend fun agendaEvent(v:AgendaEvent)
    @Upsert suspend fun inboxItem(v:InboxItem)
    @Query("DELETE FROM path_habit_links WHERE id=:id") suspend fun deletePathHabitLink(id:String)
    @Query("DELETE FROM path_habit_links WHERE pathId=:pathId") suspend fun deletePathHabitLinksByPath(pathId:String)
    @Query("DELETE FROM path_subtasks WHERE id=:id") suspend fun deletePathSubtask(id:String)
    @Query("DELETE FROM path_subtasks WHERE taskId=:taskId") suspend fun deletePathSubtasksByTask(taskId:String)
    @Query("DELETE FROM path_tasks WHERE id=:id") suspend fun deletePathTask(id:String)
    @Query("DELETE FROM path_stages WHERE id=:id") suspend fun deletePathStage(id:String)
    @Query("DELETE FROM path_stages WHERE pathId=:pathId") suspend fun deletePathStagesByPath(pathId:String)
    @Query("DELETE FROM paths WHERE id=:id") suspend fun deletePath(id:String)
    @Query("DELETE FROM time_blocks WHERE id=:id") suspend fun deleteTimeBlock(id:String)
    @Query("DELETE FROM agenda_events WHERE id=:id") suspend fun deleteAgendaEvent(id:String)
    @Query("DELETE FROM inbox_items WHERE id=:id") suspend fun deleteInboxItem(id:String)
    @Query("DELETE FROM path_habit_links") suspend fun clearPathHabitLinks()
    @Query("DELETE FROM path_subtasks") suspend fun clearPathSubtasks()
    @Query("DELETE FROM path_tasks") suspend fun clearPathTasks()
    @Query("DELETE FROM path_stages") suspend fun clearPathStages()
    @Query("DELETE FROM paths") suspend fun clearPaths()
    @Query("DELETE FROM time_blocks") suspend fun clearTimeBlocks()
    @Query("DELETE FROM agenda_events") suspend fun clearAgendaEvents()
    @Query("DELETE FROM inbox_items") suspend fun clearInboxItems()

    @Query("SELECT * FROM knowledge_notebooks ORDER BY updated DESC, name") suspend fun notebooks():List<KnowledgeNotebook>
    @Query("SELECT * FROM knowledge_pages ORDER BY updated DESC, title") suspend fun knowledgePages():List<KnowledgePage>
    @Query("SELECT * FROM knowledge_page_links ORDER BY created") suspend fun knowledgeLinks():List<KnowledgePageLink>
    @Query("SELECT * FROM knowledge_references ORDER BY created DESC") suspend fun knowledgeReferences():List<KnowledgeReference>
    @Query("SELECT * FROM flashcards ORDER BY nextReviewDate, created") suspend fun flashcards():List<Flashcard>
    @Query("SELECT * FROM flashcard_reviews ORDER BY reviewedAt DESC") suspend fun flashcardReviews():List<FlashcardReview>
    @Upsert suspend fun notebook(v:KnowledgeNotebook)
    @Upsert suspend fun knowledgePage(v:KnowledgePage)
    @Upsert suspend fun knowledgeLink(v:KnowledgePageLink)
    @Upsert suspend fun knowledgeReference(v:KnowledgeReference)
    @Upsert suspend fun flashcard(v:Flashcard)
    @Upsert suspend fun flashcardReview(v:FlashcardReview)
    @Query("DELETE FROM knowledge_notebooks WHERE id=:id") suspend fun deleteNotebook(id:String)
    @Query("DELETE FROM knowledge_pages WHERE id=:id") suspend fun deleteKnowledgePage(id:String)
    @Query("DELETE FROM knowledge_page_links WHERE id=:id") suspend fun deleteKnowledgeLink(id:String)
    @Query("DELETE FROM knowledge_page_links WHERE sourcePageId=:pageId OR targetPageId=:pageId") suspend fun deleteKnowledgeLinksForPage(pageId:String)
    @Query("DELETE FROM knowledge_references WHERE id=:id") suspend fun deleteKnowledgeReference(id:String)
    @Query("DELETE FROM knowledge_references WHERE pageId=:pageId") suspend fun deleteKnowledgeReferencesForPage(pageId:String)
    @Query("DELETE FROM flashcards WHERE id=:id") suspend fun deleteFlashcard(id:String)
    @Query("DELETE FROM flashcard_reviews WHERE flashcardId=:flashcardId") suspend fun deleteFlashcardReviewsForCard(flashcardId:String)
    @Query("DELETE FROM flashcard_reviews") suspend fun clearFlashcardReviews()
    @Query("DELETE FROM flashcards") suspend fun clearFlashcards()
    @Query("DELETE FROM knowledge_references") suspend fun clearKnowledgeReferences()
    @Query("DELETE FROM knowledge_page_links") suspend fun clearKnowledgeLinks()
    @Query("DELETE FROM knowledge_pages") suspend fun clearKnowledgePages()
    @Query("DELETE FROM knowledge_notebooks") suspend fun clearNotebooks()

    @Query("SELECT * FROM equipment_loadouts ORDER BY id") suspend fun loadouts():List<EquipmentLoadout>
    @Upsert suspend fun loadout(v:EquipmentLoadout)
    @Query("DELETE FROM equipment_loadouts WHERE id=:id") suspend fun deleteLoadout(id:String)
    @Query("DELETE FROM equipment_loadouts") suspend fun clearLoadouts()

    @Query("DELETE FROM diary WHERE id=:id") suspend fun deleteJournal(id:String)
    @Query("DELETE FROM routines WHERE id=:id") suspend fun deleteRoutine(id:String)
    @Query("DELETE FROM player") suspend fun clearPlayer()
    @Query("DELETE FROM records") suspend fun clearRecords()
    @Query("DELETE FROM habits") suspend fun clearHabits()
    @Query("DELETE FROM ledger") suspend fun clearLedger()
    @Query("DELETE FROM quests") suspend fun clearQuests()
    @Query("DELETE FROM claims") suspend fun clearClaims()
    @Query("DELETE FROM assets") suspend fun clearAssets()
    @Query("DELETE FROM achievements") suspend fun clearAchievements()
    @Query("DELETE FROM diary") suspend fun clearJournal()
    @Query("DELETE FROM routines") suspend fun clearRoutines()
}
val MIGRATION_1_2 = object : androidx.room.migration.Migration(1,2) {
    override fun migrate(db:androidx.sqlite.db.SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `rest_days` (`date` TEXT NOT NULL, PRIMARY KEY(`date`))")
    }
}
val MIGRATION_2_3 = object : androidx.room.migration.Migration(2,3) {
    override fun migrate(db:androidx.sqlite.db.SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE habits ADD COLUMN templateId TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE habits ADD COLUMN narrativeName TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE habits ADD COLUMN trackStreak INTEGER NOT NULL DEFAULT 1")
    }
}
@Database(entities=[Player::class,Habit::class,HabitRecord::class,Reward::class,Quest::class,Claim::class,Asset::class,Unlocked::class,Journal::class,Routine::class,Focus::class,RestDay::class,PlanException::class,FocusSession::class,PersonalPath::class,PathStage::class,PathTask::class,PathSubtask::class,PathHabitLink::class,TimeBlock::class,AgendaEvent::class,InboxItem::class,KnowledgeNotebook::class,KnowledgePage::class,KnowledgePageLink::class,KnowledgeReference::class,Flashcard::class,FlashcardReview::class,EquipmentLoadout::class],version=9,exportSchema=true)
abstract class GameDatabase:RoomDatabase(){abstract fun dao():GameDao}

val MIGRATION_3_4 = object : androidx.room.migration.Migration(3,4) {
    override fun migrate(db:androidx.sqlite.db.SupportSQLiteDatabase) {
        listOf("quests","claims").forEach { table ->
            db.execSQL("ALTER TABLE $table ADD COLUMN sagaStart TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE $table ADD COLUMN sagaBaseline TEXT NOT NULL DEFAULT ''")
        }
    }
}

val MIGRATION_4_5 = object : androidx.room.migration.Migration(4,5) {
 override fun migrate(db:androidx.sqlite.db.SupportSQLiteDatabase){
  db.execSQL("ALTER TABLE quests ADD COLUMN sagaState INTEGER NOT NULL DEFAULT 0")
  db.execSQL("ALTER TABLE quests ADD COLUMN sagaEnd TEXT NOT NULL DEFAULT ''")
  db.execSQL("ALTER TABLE quests ADD COLUMN sagaStopped INTEGER NOT NULL DEFAULT 0")
  db.execSQL("ALTER TABLE assets ADD COLUMN memorySpeaker TEXT NOT NULL DEFAULT ''")
  db.execSQL("ALTER TABLE assets ADD COLUMN memoryText TEXT NOT NULL DEFAULT ''")
 }
}

val MIGRATION_5_6=object:androidx.room.migration.Migration(5,6){
 override fun migrate(db:androidx.sqlite.db.SupportSQLiteDatabase){
  db.execSQL("ALTER TABLE focus ADD COLUMN sessionId TEXT NOT NULL DEFAULT ''")
  db.execSQL("ALTER TABLE focus ADD COLUMN beganAt INTEGER NOT NULL DEFAULT 0")
  db.execSQL("CREATE TABLE IF NOT EXISTS plan_exceptions (id TEXT NOT NULL, habitId TEXT NOT NULL, date TEXT NOT NULL, include INTEGER NOT NULL, PRIMARY KEY(id), FOREIGN KEY(habitId) REFERENCES habits(id) ON UPDATE NO ACTION ON DELETE RESTRICT)")
  db.execSQL("CREATE INDEX IF NOT EXISTS index_plan_exceptions_habitId ON plan_exceptions(habitId)")
  db.execSQL("CREATE TABLE IF NOT EXISTS focus_sessions (id TEXT NOT NULL, habitId TEXT NOT NULL, habitName TEXT NOT NULL, date TEXT NOT NULL, startedAt INTEGER NOT NULL, endedAt INTEGER NOT NULL, elapsedSeconds INTEGER NOT NULL, plannedMinutes INTEGER NOT NULL, status TEXT NOT NULL, registered INTEGER NOT NULL, PRIMARY KEY(id), FOREIGN KEY(habitId) REFERENCES habits(id) ON UPDATE NO ACTION ON DELETE RESTRICT)")
  db.execSQL("CREATE INDEX IF NOT EXISTS index_focus_sessions_habitId ON focus_sessions(habitId)")
 }
}

val MIGRATION_6_7=object:androidx.room.migration.Migration(6,7){
 override fun migrate(db:androidx.sqlite.db.SupportSQLiteDatabase){
  db.execSQL("CREATE TABLE IF NOT EXISTS `focus_v7` (`id` INTEGER NOT NULL, `habitId` TEXT NOT NULL, `date` TEXT NOT NULL, `duration` INTEGER NOT NULL, `elapsed` INTEGER NOT NULL, `startedAt` INTEGER NOT NULL, `baseline` INTEGER NOT NULL, `sessionId` TEXT NOT NULL DEFAULT '', `beganAt` INTEGER NOT NULL DEFAULT 0, `pathId` TEXT NOT NULL DEFAULT '', `taskId` TEXT NOT NULL DEFAULT '', `targetLabel` TEXT NOT NULL DEFAULT '', PRIMARY KEY(`id`))")
  db.execSQL("INSERT INTO `focus_v7` (`id`, `habitId`, `date`, `duration`, `elapsed`, `startedAt`, `baseline`, `sessionId`, `beganAt`, `pathId`, `taskId`, `targetLabel`) SELECT `id`, `habitId`, `date`, `duration`, `elapsed`, `startedAt`, `baseline`, `sessionId`, `beganAt`, '', '', '' FROM `focus`")
  db.execSQL("DROP TABLE `focus`")
  db.execSQL("ALTER TABLE `focus_v7` RENAME TO `focus`")
  db.execSQL("CREATE INDEX IF NOT EXISTS `index_focus_habitId` ON `focus` (`habitId`)")
  db.execSQL("CREATE INDEX IF NOT EXISTS `index_focus_pathId` ON `focus` (`pathId`)")
  db.execSQL("CREATE INDEX IF NOT EXISTS `index_focus_taskId` ON `focus` (`taskId`)")

  db.execSQL("CREATE TABLE IF NOT EXISTS `focus_sessions_v7` (`id` TEXT NOT NULL, `habitId` TEXT NOT NULL, `habitName` TEXT NOT NULL, `date` TEXT NOT NULL, `startedAt` INTEGER NOT NULL, `endedAt` INTEGER NOT NULL, `elapsedSeconds` INTEGER NOT NULL, `plannedMinutes` INTEGER NOT NULL, `status` TEXT NOT NULL, `registered` INTEGER NOT NULL, `pathId` TEXT NOT NULL DEFAULT '', `taskId` TEXT NOT NULL DEFAULT '', `targetLabel` TEXT NOT NULL DEFAULT '', PRIMARY KEY(`id`))")
  db.execSQL("INSERT INTO `focus_sessions_v7` (`id`, `habitId`, `habitName`, `date`, `startedAt`, `endedAt`, `elapsedSeconds`, `plannedMinutes`, `status`, `registered`, `pathId`, `taskId`, `targetLabel`) SELECT `id`, `habitId`, `habitName`, `date`, `startedAt`, `endedAt`, `elapsedSeconds`, `plannedMinutes`, `status`, `registered`, '', '', '' FROM `focus_sessions`")
  db.execSQL("DROP TABLE `focus_sessions`")
  db.execSQL("ALTER TABLE `focus_sessions_v7` RENAME TO `focus_sessions`")
  db.execSQL("CREATE INDEX IF NOT EXISTS `index_focus_sessions_habitId` ON `focus_sessions` (`habitId`)")
  db.execSQL("CREATE INDEX IF NOT EXISTS `index_focus_sessions_pathId` ON `focus_sessions` (`pathId`)")
  db.execSQL("CREATE INDEX IF NOT EXISTS `index_focus_sessions_taskId` ON `focus_sessions` (`taskId`)")

  db.execSQL("CREATE TABLE IF NOT EXISTS `paths` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `description` TEXT NOT NULL, `category` TEXT NOT NULL, `icon` INTEGER NOT NULL, `color` INTEGER NOT NULL, `startDate` TEXT NOT NULL, `deadline` TEXT NOT NULL, `priority` INTEGER NOT NULL, `state` INTEGER NOT NULL, `attribute` INTEGER NOT NULL, `criteria` TEXT NOT NULL, `notes` TEXT NOT NULL, `completedDate` TEXT NOT NULL, `position` INTEGER NOT NULL, PRIMARY KEY(`id`))")
  db.execSQL("CREATE TABLE IF NOT EXISTS `path_stages` (`id` TEXT NOT NULL, `pathId` TEXT NOT NULL, `name` TEXT NOT NULL, `targetNote` TEXT NOT NULL, `position` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`pathId`) REFERENCES `paths`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT )")
  db.execSQL("CREATE INDEX IF NOT EXISTS `index_path_stages_pathId` ON `path_stages` (`pathId`)")
  db.execSQL("CREATE TABLE IF NOT EXISTS `path_tasks` (`id` TEXT NOT NULL, `pathId` TEXT NOT NULL, `stageId` TEXT NOT NULL, `title` TEXT NOT NULL, `description` TEXT NOT NULL, `priority` INTEGER NOT NULL, `deadline` TEXT NOT NULL, `scheduledDate` TEXT NOT NULL, `scheduledTime` TEXT NOT NULL, `estimateMinutes` INTEGER NOT NULL, `completed` INTEGER NOT NULL, `completedDate` TEXT NOT NULL, `archived` INTEGER NOT NULL, `position` INTEGER NOT NULL, `notes` TEXT NOT NULL, `created` TEXT NOT NULL, PRIMARY KEY(`id`))")
  db.execSQL("CREATE INDEX IF NOT EXISTS `index_path_tasks_pathId` ON `path_tasks` (`pathId`)")
  db.execSQL("CREATE INDEX IF NOT EXISTS `index_path_tasks_stageId` ON `path_tasks` (`stageId`)")
  db.execSQL("CREATE INDEX IF NOT EXISTS `index_path_tasks_scheduledDate` ON `path_tasks` (`scheduledDate`)")
  db.execSQL("CREATE INDEX IF NOT EXISTS `index_path_tasks_deadline` ON `path_tasks` (`deadline`)")
  db.execSQL("CREATE TABLE IF NOT EXISTS `path_subtasks` (`id` TEXT NOT NULL, `taskId` TEXT NOT NULL, `title` TEXT NOT NULL, `done` INTEGER NOT NULL, `position` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`taskId`) REFERENCES `path_tasks`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT )")
  db.execSQL("CREATE INDEX IF NOT EXISTS `index_path_subtasks_taskId` ON `path_subtasks` (`taskId`)")
  db.execSQL("CREATE TABLE IF NOT EXISTS `path_habit_links` (`id` TEXT NOT NULL, `pathId` TEXT NOT NULL, `habitId` TEXT NOT NULL, `created` TEXT NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`pathId`) REFERENCES `paths`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT , FOREIGN KEY(`habitId`) REFERENCES `habits`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT )")
  db.execSQL("CREATE INDEX IF NOT EXISTS `index_path_habit_links_pathId` ON `path_habit_links` (`pathId`)")
  db.execSQL("CREATE INDEX IF NOT EXISTS `index_path_habit_links_habitId` ON `path_habit_links` (`habitId`)")
  db.execSQL("CREATE TABLE IF NOT EXISTS `time_blocks` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `date` TEXT NOT NULL, `startTime` TEXT NOT NULL, `endTime` TEXT NOT NULL, `category` TEXT NOT NULL, `color` INTEGER NOT NULL, `description` TEXT NOT NULL, `pathId` TEXT NOT NULL, `taskId` TEXT NOT NULL, `habitId` TEXT NOT NULL, PRIMARY KEY(`id`))")
  db.execSQL("CREATE INDEX IF NOT EXISTS `index_time_blocks_date` ON `time_blocks` (`date`)")
  db.execSQL("CREATE INDEX IF NOT EXISTS `index_time_blocks_pathId` ON `time_blocks` (`pathId`)")
  db.execSQL("CREATE INDEX IF NOT EXISTS `index_time_blocks_taskId` ON `time_blocks` (`taskId`)")
  db.execSQL("CREATE INDEX IF NOT EXISTS `index_time_blocks_habitId` ON `time_blocks` (`habitId`)")
  db.execSQL("CREATE TABLE IF NOT EXISTS `agenda_events` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `date` TEXT NOT NULL, `startTime` TEXT NOT NULL, `endTime` TEXT NOT NULL, `kind` TEXT NOT NULL, `description` TEXT NOT NULL, `completed` INTEGER NOT NULL, `archived` INTEGER NOT NULL, PRIMARY KEY(`id`))")
  db.execSQL("CREATE INDEX IF NOT EXISTS `index_agenda_events_date` ON `agenda_events` (`date`)")
  db.execSQL("CREATE TABLE IF NOT EXISTS `inbox_items` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `notes` TEXT NOT NULL, `created` TEXT NOT NULL, `archived` INTEGER NOT NULL, PRIMARY KEY(`id`))")
 }
}

val MIGRATION_7_8=object:androidx.room.migration.Migration(7,8){
 override fun migrate(db:androidx.sqlite.db.SupportSQLiteDatabase){
  db.execSQL("ALTER TABLE `player` ADD COLUMN `specialization` TEXT NOT NULL DEFAULT ''")
  db.execSQL("CREATE TABLE IF NOT EXISTS `knowledge_notebooks` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `description` TEXT NOT NULL, `category` TEXT NOT NULL, `icon` INTEGER NOT NULL, `cover` INTEGER NOT NULL, `pathId` TEXT NOT NULL, `archived` INTEGER NOT NULL, `created` TEXT NOT NULL, `updated` TEXT NOT NULL, PRIMARY KEY(`id`))")
  db.execSQL("CREATE INDEX IF NOT EXISTS `index_knowledge_notebooks_pathId` ON `knowledge_notebooks` (`pathId`)")
  db.execSQL("CREATE TABLE IF NOT EXISTS `knowledge_pages` (`id` TEXT NOT NULL, `notebookId` TEXT NOT NULL, `title` TEXT NOT NULL, `content` TEXT NOT NULL, `tags` TEXT NOT NULL, `favorite` INTEGER NOT NULL, `archived` INTEGER NOT NULL, `pathId` TEXT NOT NULL, `taskId` TEXT NOT NULL, `created` TEXT NOT NULL, `updated` TEXT NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`notebookId`) REFERENCES `knowledge_notebooks`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT )")
  db.execSQL("CREATE INDEX IF NOT EXISTS `index_knowledge_pages_notebookId` ON `knowledge_pages` (`notebookId`)")
  db.execSQL("CREATE INDEX IF NOT EXISTS `index_knowledge_pages_pathId` ON `knowledge_pages` (`pathId`)")
  db.execSQL("CREATE INDEX IF NOT EXISTS `index_knowledge_pages_taskId` ON `knowledge_pages` (`taskId`)")
  db.execSQL("CREATE TABLE IF NOT EXISTS `knowledge_page_links` (`id` TEXT NOT NULL, `sourcePageId` TEXT NOT NULL, `targetPageId` TEXT NOT NULL, `created` TEXT NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`sourcePageId`) REFERENCES `knowledge_pages`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT , FOREIGN KEY(`targetPageId`) REFERENCES `knowledge_pages`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT )")
  db.execSQL("CREATE INDEX IF NOT EXISTS `index_knowledge_page_links_sourcePageId` ON `knowledge_page_links` (`sourcePageId`)")
  db.execSQL("CREATE INDEX IF NOT EXISTS `index_knowledge_page_links_targetPageId` ON `knowledge_page_links` (`targetPageId`)")
  db.execSQL("CREATE TABLE IF NOT EXISTS `knowledge_references` (`id` TEXT NOT NULL, `pageId` TEXT NOT NULL, `author` TEXT NOT NULL, `title` TEXT NOT NULL, `year` TEXT NOT NULL, `source` TEXT NOT NULL, `url` TEXT NOT NULL, `location` TEXT NOT NULL, `notes` TEXT NOT NULL, `created` TEXT NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`pageId`) REFERENCES `knowledge_pages`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT )")
  db.execSQL("CREATE INDEX IF NOT EXISTS `index_knowledge_references_pageId` ON `knowledge_references` (`pageId`)")
  db.execSQL("CREATE TABLE IF NOT EXISTS `flashcards` (`id` TEXT NOT NULL, `notebookId` TEXT NOT NULL, `pageId` TEXT NOT NULL, `question` TEXT NOT NULL, `answer` TEXT NOT NULL, `tags` TEXT NOT NULL, `repetitions` INTEGER NOT NULL, `intervalDays` INTEGER NOT NULL, `easeFactor` REAL NOT NULL, `nextReviewDate` TEXT NOT NULL, `lastReviewDate` TEXT NOT NULL, `archived` INTEGER NOT NULL, `created` TEXT NOT NULL, `updated` TEXT NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`notebookId`) REFERENCES `knowledge_notebooks`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT )")
  db.execSQL("CREATE INDEX IF NOT EXISTS `index_flashcards_notebookId` ON `flashcards` (`notebookId`)")
  db.execSQL("CREATE INDEX IF NOT EXISTS `index_flashcards_pageId` ON `flashcards` (`pageId`)")
  db.execSQL("CREATE INDEX IF NOT EXISTS `index_flashcards_nextReviewDate` ON `flashcards` (`nextReviewDate`)")
  db.execSQL("CREATE TABLE IF NOT EXISTS `flashcard_reviews` (`id` TEXT NOT NULL, `flashcardId` TEXT NOT NULL, `reviewDate` TEXT NOT NULL, `rating` INTEGER NOT NULL, `intervalBefore` INTEGER NOT NULL, `intervalAfter` INTEGER NOT NULL, `easeAfter` REAL NOT NULL, `reviewedAt` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`flashcardId`) REFERENCES `flashcards`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT )")
  db.execSQL("CREATE INDEX IF NOT EXISTS `index_flashcard_reviews_flashcardId` ON `flashcard_reviews` (`flashcardId`)")
  db.execSQL("CREATE INDEX IF NOT EXISTS `index_flashcard_reviews_reviewDate` ON `flashcard_reviews` (`reviewDate`)")
 }
}

val MIGRATION_8_9=object:androidx.room.migration.Migration(8,9){
 override fun migrate(db:androidx.sqlite.db.SupportSQLiteDatabase){
  db.execSQL("CREATE TABLE IF NOT EXISTS `equipment_loadouts` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `headId` TEXT NOT NULL, `outfitId` TEXT NOT NULL, `accessoryId` TEXT NOT NULL, `weaponId` TEXT NOT NULL, `companionId` TEXT NOT NULL, `frameId` TEXT NOT NULL, `backgroundId` TEXT NOT NULL, `updated` TEXT NOT NULL, PRIMARY KEY(`id`))")
 }
}
