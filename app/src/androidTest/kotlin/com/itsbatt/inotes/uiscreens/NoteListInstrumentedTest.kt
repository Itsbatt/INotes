package com.itsbatt.inotes.uiscreens

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.itsbatt.inotes.ui.MainActivity
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class NoteListInstrumentedTest {

    @Test
    fun applicationContextLoadsSuccessfully() {
        val appContext = ApplicationProvider.getApplicationContext<android.content.Context>()
        assertNotNull("Application context should not be null", appContext)
    }

    @Test
    fun mainActivityLaunchesWithoutCrash() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        assertNotNull(scenario)
        scenario.close()
    }
}
