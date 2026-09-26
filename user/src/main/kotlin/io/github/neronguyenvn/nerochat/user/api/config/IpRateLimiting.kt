package io.github.neronguyenvn.nerochat.user.api.config

import java.util.concurrent.TimeUnit

annotation class IpRateLimiting(
    val maxRequestsPerIp: Int = 10,
    val resetIn: Long = 1,
    val timeUnit: TimeUnit = TimeUnit.HOURS
)