package com.pm.foodscanner.util

import java.time.ZoneId
import java.util.TimeZone

object AppTimeZone {
    const val ZONE_ID: String = "Asia/Almaty"
    val timeZone: TimeZone get() = TimeZone.getTimeZone(ZONE_ID)
    val zoneId: ZoneId get() = ZoneId.of(ZONE_ID)
}
