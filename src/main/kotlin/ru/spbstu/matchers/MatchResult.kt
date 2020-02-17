package ru.spbstu.matchers

import ru.spbstu.wheels.Option
import ru.spbstu.wheels.getOrElse

sealed class MatchResult<out T1, out T2, out T3, out T4, out T5, out T6> {
    abstract operator fun component1(): T1
    abstract operator fun component2(): T2
    abstract operator fun component3(): T3
    abstract operator fun component4(): T4
    abstract operator fun component5(): T5
    abstract operator fun component6(): T6
}

class MatchResultBuilder<out T1, out T2, out T3, out T4, out T5, out T6>(
    var v1: Option<@UnsafeVariance T1> = Option.empty(),
    var v2: Option<@UnsafeVariance T2> = Option.empty(),
    var v3: Option<@UnsafeVariance T3> = Option.empty(),
    var v4: Option<@UnsafeVariance T4> = Option.empty(),
    var v5: Option<@UnsafeVariance T5> = Option.empty(),
    var v6: Option<@UnsafeVariance T6> = Option.empty()
) : MatchResult<T1, T2, T3, T4, T5, T6>() {
    override operator fun component1(): T1 =
        v1.getOrElse { throw IllegalArgumentException("Matching component 1 did not match anything") }

    override operator fun component2(): T2 =
        v2.getOrElse { throw IllegalArgumentException("Matching component 2 did not match anything") }

    override operator fun component3(): T3 =
        v3.getOrElse { throw IllegalArgumentException("Matching component 3 did not match anything") }

    override operator fun component4(): T4 =
        v4.getOrElse { throw IllegalArgumentException("Matching component 4 did not match anything") }

    override operator fun component5(): T5 =
        v5.getOrElse { throw IllegalArgumentException("Matching component 5 did not match anything") }

    override operator fun component6(): T6 =
        v6.getOrElse { throw IllegalArgumentException("Matching component 6 did not match anything") }
}