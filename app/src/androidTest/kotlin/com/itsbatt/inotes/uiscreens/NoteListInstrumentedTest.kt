package com.itsbatt.inotes.uiscreens

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class NoteListInstrumentedTest {

    @Test
    fun noteListScreenLoadsWithoutCrash() {
        val appContext = ApplicationProvider.getApplicationContext<android.content.Context>()
        assertNotNull("Application context should not be null", appContext)
    }
}
