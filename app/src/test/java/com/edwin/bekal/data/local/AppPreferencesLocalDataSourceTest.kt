package com.edwin.bekal.data.local

import android.content.Context
import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AppPreferencesLocalDataSourceTest {

    private val context = mockk<Context>()
    private val sharedPreferences = mockk<SharedPreferences>()
    private val editor = mockk<SharedPreferences.Editor>()

    private lateinit var dataSource: AppPreferencesLocalDataSource

    @Before
    fun setUp() {
        every { context.getSharedPreferences("bekal_app_preferences", Context.MODE_PRIVATE) } returns sharedPreferences
        every { sharedPreferences.edit() } returns editor
        every { editor.putBoolean(any(), any()) } returns editor
        every { editor.apply() } returns Unit

        dataSource = AppPreferencesLocalDataSource(context)
    }

    @Test
    fun `isFirstLaunch returns true by default`() {
        every { sharedPreferences.getBoolean("is_first_launch", true) } returns true

        val result = dataSource.isFirstLaunch()

        assertTrue(result)
        verify { sharedPreferences.getBoolean("is_first_launch", true) }
    }

    @Test
    fun `isFirstLaunch returns false when already completed`() {
        every { sharedPreferences.getBoolean("is_first_launch", true) } returns false

        val result = dataSource.isFirstLaunch()

        assertFalse(result)
        verify { sharedPreferences.getBoolean("is_first_launch", true) }
    }

    @Test
    fun `setFirstLaunchCompleted writes false to preferences`() {
        dataSource.setFirstLaunchCompleted()

        verify {
            editor.putBoolean("is_first_launch", false)
            editor.apply()
        }
    }
}
