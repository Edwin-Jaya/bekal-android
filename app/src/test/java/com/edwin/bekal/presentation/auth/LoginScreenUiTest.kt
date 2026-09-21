//package com.edwin.bekal.presentation.auth
//
//import androidx.compose.ui.test.ExperimentalTestApi
//import androidx.compose.ui.test.SemanticsMatcher
//import androidx.compose.ui.test.hasAnyAncestor
//import androidx.compose.ui.test.hasContentDescription
//import androidx.compose.ui.test.hasSetTextAction
//import androidx.compose.ui.test.hasTestTag
//import androidx.compose.ui.test.hasText
//import androidx.compose.ui.test.junit4.AndroidComposeTestRule
//import androidx.compose.ui.test.junit4.createAndroidComposeRule
//import androidx.compose.ui.test.onNodeWithText
//import androidx.compose.ui.test.performClick
//import androidx.test.ext.junit.rules.ActivityScenarioRule
//import androidx.test.platform.app.InstrumentationRegistry
//import com.edwin.bekal.MainActivity
//import org.junit.rules.ExternalResource
//import kotlin.getValue
//import com.edwin.bekal.data.local.AuthSessionLocalDataSource
//import kotlinx.coroutines.runBlocking
//import org.junit.Rule
//import org.junit.Test
//
//private const val STEP_DELAY_MILLIS = 2_000L
//private const val NETWORK_TIMEOUT_MILLIS = 15_000L
//private const val UNKNOWN_EMAIL = "unknown.user@masesas.test"
//private const val WRONG_PASSWORD = "wrong-password"
//
//class ClearAuthSessionRule : ExternalResource() {
//
//    private val localDataSource by lazy {
//        AuthSessionLocalDataSource(InstrumentationRegistry.getInstrumentation().targetContext)
//    }
//
//    override fun before() = runBlocking { localDataSource.clear() }
//
//    override fun after() = runBlocking { localDataSource.clear() }
//}
//
//private typealias HomeComposeRule =
//        AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>
//
//private fun pause() = Thread.sleep(STEP_DELAY_MILLIS)
//
//private fun inputOf(tag: String): SemanticsMatcher = hasSetTextAction() and hasAnyAncestor(hasTestTag(tag))
//
//private fun HomeComposeRule.string(resId: Int): String = activity.getString(resId)
//
//private fun HomeComposeRule.tapLoginButton() {
//    onNodeWithText("Masuk").performClick()
//}
//
//@OptIn(ExperimentalTestApi::class)
//private fun HomeComposeRule.awaitText(text: String) {
//    waitUntilAtLeastOneExists(hasText(text), NETWORK_TIMEOUT_MILLIS)
//}
//
//class login{
//    @get:Rule(order = 0)
//    val clearAuthSessionRule = ClearAuthSessionRule()
//
//    @get:Rule(order = 1)
//    val composeRule = createAndroidComposeRule<MainActivity>()
//
//    @OptIn(ExperimentalTestApi::class)
//    @Test
//    fun opensMainScreenWhenLoggingInWithDebugCredentials(){
//        composeRule.awaitText("Login")
//        pause()
//
//        composeRule.tapLoginButton()
//        pause()
//
//        composeRule.waitUntilAtLeastOneExists(
//            hasContentDescription(composeRule.string()),
//            NETWORK_TIMEOUT_MILLIS
//        )
//        pause()
//    }
//}
//
