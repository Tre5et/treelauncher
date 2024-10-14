package dev.treset.treelauncher.logging

import ch.qos.logback.core.rolling.SessionBasedFNATP
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy

class SessionBasedRollingPolicy<E> : TimeBasedRollingPolicy<E>() {

    override fun start() {
        val sessionBasedFNATP = SessionBasedFNATP<E>()

        timeBasedFileNamingAndTriggeringPolicy = sessionBasedFNATP

        super.start()
    }

    override fun toString(): String {
        return "c.q.l.core.rolling.SessionBasedRollingPolicy@" + this.hashCode()
    }
}