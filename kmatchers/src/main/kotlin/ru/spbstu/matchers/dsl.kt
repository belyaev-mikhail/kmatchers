package ru.spbstu.matchers

import ru.spbstu.wheels.Option
import ru.spbstu.wheels.getOrElse
import kotlin.experimental.ExperimentalTypeInference

@DslMarker
annotation class KMatchersDSL

inline fun <T1, T2, T3, T4, T5, T6, Arg, R>
        Unapplier<T1, T2, T3, T4, T5, T6, Arg>.match(
    arg: Arg,
    body: (MatchResult<T1, T2, T3, T4, T5, T6>) -> R
): Option<R> = run {
    val builder = MatchResultBuilder<T1, T2, T3, T4, T5, T6>()
    if (unapply(arg, builder)) Option.just(body(builder)) else Option.empty()
}

inline fun <T1, T2, T3, T4, T5, T6, Arg, R>
        match(
    arg: Arg,
    pattern: Unapplier<T1, T2, T3, T4, T5, T6, Arg>,
    body: (MatchResult<T1, T2, T3, T4, T5, T6>) -> R
): R? = run {
    val builder = MatchResultBuilder<T1, T2, T3, T4, T5, T6>()
    if (pattern.unapply(arg, builder)) body(builder) else null
}

@KMatchersDSL
class MatchScope<T, R>(val value: T, @PublishedApi internal val dryRun: Boolean = false) {
    @KMatchersDSL
    object FakeReceiver

    @PublishedApi
    internal var result: Option<R> = Option.empty()
    @PublishedApi
    internal var applicable: Boolean = false

    inline infix fun <T1, T2, T3, T4, T5, T6> Case<T1, T2, T3, T4, T5, T6, T>.of(body: FakeReceiver.(MatchResult<T1, T2, T3, T4, T5, T6>) -> R) {
        if (result.isEmpty() || !applicable) {
            when(val matchingResult = unapply(value)) {
                null -> {}
                else -> {
                    applicable = true
                    if(!dryRun) result = Option.just(FakeReceiver.body(matchingResult))
                }
            }
        }
    }

    inline fun otherwise(body: FakeReceiver.() -> R) {
        if (result.isEmpty() || !applicable) {
            applicable = true
            if(!dryRun) result = Option.just(FakeReceiver.body())
        }
    }
}

@UseExperimental(ExperimentalTypeInference::class)
inline fun <T, R> match(value: T, @BuilderInference body: MatchScope<T, R>.() -> Unit): R = run {
    val scope = MatchScope<T, R>(value)
    scope.body()
    scope.result.getOrElse { throw IllegalStateException("Matching failed") }
}

class PartialFunction<in T, out R> (val body: MatchScope<@UnsafeVariance T, @UnsafeVariance R>.() -> Unit): (T) -> R {
    override fun invoke(arg: T): R = match(arg, body)

    fun isApplicableTo(arg: T): Boolean {
        val scope = MatchScope<T, R>(arg, dryRun = true)
        scope.body()
        return scope.applicable
    }
}
