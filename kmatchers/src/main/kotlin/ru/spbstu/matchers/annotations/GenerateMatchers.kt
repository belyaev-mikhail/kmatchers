package ru.spbstu.matchers.annotations

import kotlin.reflect.KClass

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class GenerateMatchers(val packageName: String = "", val baseClass: KClass<*> = Nothing::class)
