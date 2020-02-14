package ru.spbstu.matchers

import ru.spbstu.Meee
import ru.spbstu.wheels.Option
import ru.spbstu.wheels.getOrElse
import kotlin.experimental.ExperimentalTypeInference

inline fun <T1, T2, T3, T4, T5, T6, Arg, R>
        Unapplier<T1, T2, T3, T4, T5, T6, Arg>.match(arg: Arg,
                                                     body: (MatchResult<T1, T2, T3, T4, T5, T6>) -> R): Option<R> = run {
    val builder = MatchResultBuilder<T1, T2, T3, T4, T5, T6>()
    if(unapply(arg, builder)) Option.just(body(builder)) else Option.empty()
}

inline fun <T1, T2, T3, T4, T5, T6, Arg, R>
        match(arg: Arg,
              pattern: Unapplier<T1, T2, T3, T4, T5, T6, Arg>,
              body: (MatchResult<T1, T2, T3, T4, T5, T6>) -> R): R? = run {
    val builder = MatchResultBuilder<T1, T2, T3, T4, T5, T6>()
    if(pattern.unapply(arg, builder)) body(builder) else null
}

class MatchScope<T, R>(val value: T, var result: Option<R>) {
    inline infix fun <T1, T2, T3, T4, T5, T6> Unapplier<T1, T2, T3, T4, T5, T6, T>
            .of(body: (MatchResult<T1, T2, T3, T4, T5, T6>) -> R) {
        if(result.isEmpty()) result = match(value, body)
    }

    inline fun otherwise(body: () -> R) {
        if(result.isEmpty()) result = Option.just(body())
    }
}

@UseExperimental(ExperimentalTypeInference::class)
fun <T, R> match(value: T, @BuilderInference body: MatchScope<T, R>.() -> Unit): R = run {
    val scope = MatchScope(value, Option.empty<R>())
    scope.body()
    scope.result.getOrElse { throw IllegalStateException("Matching failed") }
}