package io.colyseus.async

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.newSingleThreadContext

public object ColyseusAsync : CoroutineScope {
    override val coroutineContext = newSingleThreadContext("ColyseusClient-Thread")
}