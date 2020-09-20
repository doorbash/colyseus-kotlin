package io.colyseus.annotations

import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class SchemaField(val v1: String, val v2: KClass<out Any> = Any::class)