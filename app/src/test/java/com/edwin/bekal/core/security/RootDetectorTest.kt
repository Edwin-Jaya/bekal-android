package com.edwin.bekal.core.security

import android.content.Context
import android.content.pm.PackageManager
import com.scottyab.rootbeer.RootBeer
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RootDetectorTest {

    private val context = mockk<Context>(relaxed = true)
    private val packageManager = mockk<PackageManager>(relaxed = true)
    private val rootBeer = mockk<RootBeer>(relaxed = true)
    private val shellExecutor = mockk<ShellCommandExecutor>(relaxed = true)

    private lateinit var rootDetector: RootDetectorImpl

    @Before
    fun setUp() {
        every { context.packageManager } returns packageManager
        // By default, simulate clean package manager throwing NameNotFoundException
        every {
            packageManager.getPackageInfo(any<String>(), any<Int>())
        } throws PackageManager.NameNotFoundException()
        every { shellExecutor.canExecuteSu() } returns false

        rootDetector = RootDetectorImpl(context, rootBeer, shellExecutor)
    }

    @Test
    fun `when rootbeer detects root with busybox, isRooted returns true`() {
        every { rootBeer.isRootedWithBusyBoxCheck } returns true
        every { rootBeer.isRooted } returns true

        val isRooted = rootDetector.isRooted()

        assertTrue(isRooted)
    }

    @Test
    fun `when rootbeer detects standard root, getRootDetectionResult flags root threat`() {
        every { rootBeer.isRooted } returns true
        every { rootBeer.isRootedWithBusyBoxCheck } returns false

        val result = rootDetector.getRootDetectionResult()

        assertTrue(result.isRooted)
        assertTrue(result.detectedThreats.any { it.contains("RootBeer") })
    }

    @Test
    fun `when clean device and rootbeer returns false, isRooted returns false`() {
        every { rootBeer.isRootedWithBusyBoxCheck } returns false
        every { rootBeer.isRooted } returns false

        val isRooted = rootDetector.isRooted()

        assertFalse(isRooted)
    }

    @Test
    fun `when clean device, getRootDetectionResult returns clean result`() {
        every { rootBeer.isRootedWithBusyBoxCheck } returns false
        every { rootBeer.isRooted } returns false

        val result = rootDetector.getRootDetectionResult()

        assertFalse(result.isRooted)
        assertTrue(result.detectedThreats.isEmpty())
    }

    @Test
    fun `when su execution succeeds, isRooted returns true`() {
        every { shellExecutor.canExecuteSu() } returns true

        val isRooted = rootDetector.isRooted()

        assertTrue(isRooted)
    }

    @Test
    fun `when su execution succeeds, getRootDetectionResult flags su threat`() {
        every { shellExecutor.canExecuteSu() } returns true

        val result = rootDetector.getRootDetectionResult()

        assertTrue(result.isRooted)
        assertTrue(result.detectedThreats.any { it.contains("Execution of 'su'") })
    }
}
