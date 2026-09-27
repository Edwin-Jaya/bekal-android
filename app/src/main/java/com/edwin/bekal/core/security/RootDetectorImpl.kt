package com.edwin.bekal.core.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.scottyab.rootbeer.RootBeer
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RootDetectorImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val rootBeer: RootBeer
) : RootDetector {

    override fun isRooted(): Boolean {
        // Quick short-circuit check: if RootBeer detects root, return true immediately
        if (rootBeer.isRootedWithBusyBoxCheck) {
            return true
        }

        // Additional heuristic checks
        if (checkTestKeys()) return true
        if (checkSuBinaryPaths()) return true
        if (checkDangerousPackages()) return true
        if (checkSuExecution()) return true
        if (checkRwMounts()) return true
        if (checkDangerousProps()) return true

        return false
    }

    override fun getRootDetectionResult(): RootDetectionResult {
        val threats = mutableListOf<String>()

        // 1. RootBeer Checks (Native + Java)
        try {
            if (rootBeer.isRooted) {
                threats.add("RootBeer detected standard root indicators")
            }
            if (rootBeer.isRootedWithBusyBoxCheck && !threats.contains("RootBeer detected standard root indicators")) {
                threats.add("RootBeer detected BusyBox root indicators")
            }
        } catch (e: Exception) {
            // Ignore native library issues and continue heuristic checks
        }

        // 2. Build Tags (Test Keys / Custom ROM)
        if (checkTestKeys()) {
            threats.add("Custom ROM / test-keys build tag detected (${Build.TAGS})")
        }

        // 3. SU / Root binary paths
        val foundBinaries = findSuBinaries()
        if (foundBinaries.isNotEmpty()) {
            threats.add("Root binary detected in paths: ${foundBinaries.joinToString()}")
        }

        // 4. Dangerous / Root Management Packages
        val foundPackages = findDangerousPackages()
        if (foundPackages.isNotEmpty()) {
            threats.add("Dangerous root management/cloaking package detected: ${foundPackages.joinToString()}")
        }

        // 5. Su execution check
        if (checkSuExecution()) {
            threats.add("Execution of 'su' command succeeded in shell")
        }

        // 6. Read-Write mount on system partitions
        if (checkRwMounts()) {
            threats.add("System partition is mounted as Read-Write (rw)")
        }

        // 7. Dangerous system properties
        if (checkDangerousProps()) {
            threats.add("Dangerous build properties detected (ro.debuggable / ro.secure)")
        }

        return RootDetectionResult(
            isRooted = threats.isNotEmpty(),
            detectedThreats = threats
        )
    }

    private fun checkTestKeys(): Boolean {
        val buildTags = Build.TAGS
        return buildTags != null && buildTags.contains("test-keys")
    }

    private fun checkSuBinaryPaths(): Boolean {
        return findSuBinaries().isNotEmpty()
    }

    private fun findSuBinaries(): List<String> {
        val found = mutableListOf<String>()
        for (path in SU_PATHS) {
            try {
                val file = File(path)
                if (file.exists()) {
                    found.add(path)
                }
            } catch (_: SecurityException) {
                // If access is denied, ignore
            }
        }
        return found
    }

    private fun checkDangerousPackages(): Boolean {
        return findDangerousPackages().isNotEmpty()
    }

    private fun findDangerousPackages(): List<String> {
        val packageManager = context.packageManager
        val found = mutableListOf<String>()

        for (packageName in DANGEROUS_PACKAGES) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
                } else {
                    @Suppress("DEPRECATION")
                    packageManager.getPackageInfo(packageName, 0)
                }
                found.add(packageName)
            } catch (_: PackageManager.NameNotFoundException) {
                // Package not installed
            } catch (_: Exception) {
                // Fallback: check filesystem path directly
                if (File("/data/data/$packageName").exists()) {
                    found.add(packageName)
                }
            }
        }
        return found
    }

    private fun checkSuExecution(): Boolean {
        var process: Process? = null
        return try {
            process = Runtime.getRuntime().exec(arrayOf("which", "su"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val line = reader.readLine()
            line != null && line.isNotEmpty()
        } catch (_: Throwable) {
            false
        } finally {
            process?.destroy()
        }
    }

    private fun checkRwMounts(): Boolean {
        var reader: BufferedReader? = null
        return try {
            val mountsFile = File("/proc/mounts")
            if (!mountsFile.exists() || !mountsFile.canRead()) return false

            reader = BufferedReader(InputStreamReader(mountsFile.inputStream()))
            var line: String?
            var rwFound = false

            while (reader.readLine().also { line = it } != null) {
                val currentLine = line ?: continue
                val parts = currentLine.split("\\s+".toRegex())
                if (parts.size < 4) continue

                val mountPoint = parts[1]
                val mountOptions = parts[3]

                // Check critical system mount points
                if (mountPoint == "/system" || mountPoint == "/system/bin" || mountPoint == "/vendor") {
                    val options = mountOptions.split(",")
                    if (options.contains("rw")) {
                        rwFound = true
                        break
                    }
                }
            }
            rwFound
        } catch (_: Throwable) {
            false
        } finally {
            try {
                reader?.close()
            } catch (_: Throwable) {}
        }
    }

    private fun checkDangerousProps(): Boolean {
        return try {
            val systemPropertiesClass = Class.forName("android.os.SystemProperties")
            val getMethod = systemPropertiesClass.getMethod("get", String::class.java)

            val debuggable = getMethod.invoke(null, "ro.debuggable") as? String
            val secure = getMethod.invoke(null, "ro.secure") as? String

            debuggable == "1" || secure == "0"
        } catch (_: Throwable) {
            false
        }
    }

    companion object {
        val SU_PATHS = listOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su",
            "/su/xbin/su",
            "/system/xbin/daemonsu",
            "/system/etc/init.d/99SuperSUDaemon",
            "/system/bin/.ext/.su",
            "/system/usr/we-need-root/su-backup",
            "/system/xbin/mu",
            "/magisk/.core/bin/su"
        )

        val DANGEROUS_PACKAGES = listOf(
            "com.noshufou.android.su",
            "com.noshufou.android.su.elite",
            "eu.chainfire.supersu",
            "com.koushikdutta.superuser",
            "com.thirdparty.superuser",
            "com.yellowes.su",
            "com.topjohnwu.magisk",
            "com.kingroot.kinguser",
            "com.kingo.root",
            "com.smedialink.oneclickroot",
            "com.zhiqupk.root.global",
            "com.alephzain.framaroot",
            "com.koushikdutta.rommanager",
            "com.dimonvideo.luckypatcher",
            "com.chelpus.lackypatch",
            "com.ramdroid.appquarantine",
            "com.devadvance.rootcloak",
            "com.devadvance.rootcloakplus",
            "de.robv.android.xposed.installer",
            "com.saurik.substrate",
            "com.amphoras.hidemyroot",
            "com.formyhm.hideroot"
        )
    }
}
