package br.com.ascend.aetheris

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[28],application=AscendApp::class,qualifiers="w411dp-h891dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class OnboardingTest {
    @get:Rule val compose=createAndroidComposeRule<MainActivity>()
    @Test fun createMageAndRecordStudyThroughInterface(){
        val repo=(RuntimeEnvironment.getApplication() as AscendApp).repository
        compose.waitForIdle()
        waitForState{repo.state.value!=null}
        compose.onNodeWithText("Acender a primeira chama").performScrollTo().performClick()
        compose.onNodeWithText("Nome do aventureiro").performTextInput("Aria")
        compose.onNodeWithText("Continuar").performScrollTo().performClick()
        compose.onNodeWithText("Entrar em Aetheris").performScrollTo().performClick()
        waitForState{repo.state.value?.player!=null}
        compose.onNodeWithText("Aria").assertExists()
        compose.onNodeWithContentDescription("Registrar progresso").performScrollTo().performClick()
        compose.onNodeWithText("Atingir meta").performScrollTo().performClick()
        compose.onNodeWithText("Salvar").performClick()
        waitForState{repo.state.value?.xp==60L}
        assertEquals(1,repo.state.value!!.player!!.heroClass)
        assertEquals(60,repo.state.value!!.records.single().progress)
        assertEquals(5L,repo.state.value!!.points(0))
        compose.runOnIdle {
            val view=compose.activity.window.decorView
            val image=android.graphics.Bitmap.createBitmap(view.width,view.height,android.graphics.Bitmap.Config.ARGB_8888)
            view.draw(android.graphics.Canvas(image))
            val path=java.io.File("build/reports/screenshots/jornada.png");path.parentFile!!.mkdirs()
            path.outputStream().use{image.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it)}
        }
        compose.onNodeWithText("Mundo").performClick()
        compose.onNodeWithText("Atlas de Aetheris").assertExists()
        compose.onAllNodesWithText("Vila").onFirst().performClick()
        compose.onNodeWithText("A Biblioteca").assertExists()
        compose.onNodeWithText("Missões").performClick()
        compose.onNodeWithText("Contratos da jornada").assertExists()
        compose.onNodeWithText("Herói").performClick()
        compose.onNodeWithText("Seu aventureiro").assertExists()
        compose.onNodeWithText("Conquistas").performScrollTo().performClick()
        compose.onNodeWithText("✦ O Primeiro Passo").assertExists()
        compose.onNodeWithText("Hábitos").performClick()
        compose.onNodeWithText("Histórico").performScrollTo().performClick()
        compose.onNodeWithText("Estudar Direito").assertExists()
    }
    private fun waitForState(condition:()->Boolean){
        compose.waitUntil(15000){Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();condition()}
        compose.waitForIdle()
    }

}
