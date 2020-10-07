package io.colyseus.annotations

import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class SchemaField(val type: String, val ref: KClass<out Any> = Any::class)