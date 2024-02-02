// Package changing strangeness needed to access tbrp.fileNamePattern correctly
@file:Suppress("PackageDirectoryMismatch")
package ch.qos.logback.core.rolling

import ch.qos.logback.core.joran.spi.NoAutoStart
import ch.qos.logback.core.rolling.helper.CompressionMode
import ch.qos.logback.core.rolling.helper.FileFilterUtil
import java.io.File

@NoAutoStart
class SessionBasedFNATP<E> : TimeBasedFileNamingAndTriggeringPolicyBase<E>() {
    private var currentPeriodsCounter = 0

    override fun start() {
        super.start()

        setDateInCurrentPeriod(System.currentTimeMillis())

        computePeriodCounter()

        started = true
    }

    private fun computePeriodCounter() {
        val regex = tbrp.fileNamePattern.toRegexForFixedDate(dateInCurrentPeriod)
        val stemRegex = FileFilterUtil.afterLastSlash(regex)

        val file = File(currentPeriodsFileNameWithoutCompressionSuffix)
        val parentDir = file.parentFile

        val matchingFileArray = FileFilterUtil.filesInFolderMatchingStemRegex(parentDir, stemRegex)

        if (matchingFileArray == null || matchingFileArray.isEmpty()) {
            currentPeriodsCounter = 0
            return
        }
        currentPeriodsCounter = FileFilterUtil.findHighestCounter(matchingFileArray, stemRegex)

        if (tbrp.parentsRawFileProperty != null || (tbrp.compressionMode != CompressionMode.NONE)) {
            currentPeriodsCounter++
        }
    }

    override fun isTriggeringEvent(activeFile: File?, event: E): Boolean {
        if(ready) {
            ready = false
            elapsedPeriodsFileName = tbrp.fileNamePatternWithoutCompSuffix.convertMultipleArguments(
                dateInCurrentPeriod,
                currentPeriodsCounter
            )
            currentPeriodsCounter++
            return true
        }
        return false
    }

    override fun getCurrentPeriodsFileNameWithoutCompressionSuffix(): String {
        return tbrp.fileNamePatternWithoutCompSuffix.convertMultipleArguments(
            dateInCurrentPeriod,
            currentPeriodsCounter
        )
    }


    companion object {
        var ready = true
    }
}