package ru.spbstu.matchers

import ru.spbstu.wheels.Option

abstract class Unapplier<out T1, out T2, out T3, out T4, out T5, out T6, in Arg> {
    abstract fun unapply(
        arg: Arg, matcher: MatchResultBuilder<
                @UnsafeVariance T1,
                @UnsafeVariance T2,
                @UnsafeVariance T3,
                @UnsafeVariance T4,
                @UnsafeVariance T5,
                @UnsafeVariance T6>
    ): Boolean
}

internal inline fun <T1, T2, T3, T4, T5, T6, Arg, Res> Unapplier<T1, T2, T3, T4, T5, T6, Arg>.comap(crossinline body: (Res) -> Arg): Unapplier<T1, T2, T3, T4, T5, T6, Res> =
    unapplier { arg, matchResultBuilder ->
        this@comap.unapply(body(arg), matchResultBuilder)
    }

internal inline fun <T1, T2, T3, T4, T5, T6, Arg> Unapplier<T1, T2, T3, T4, T5, T6, Arg>.filter(crossinline body: (Arg) -> Boolean): Unapplier<T1, T2, T3, T4, T5, T6, Arg> =
    unapplier { arg, matchResultBuilder ->
        if (body(arg)) unapply(arg, matchResultBuilder)
        else false
    }

internal fun  <T1, T2, T3, T4, T5, T6, Arg> combine(vararg elements: Unapplier<T1, T2, T3, T4, T5, T6, Arg>): Unapplier<T1, T2, T3, T4, T5, T6, Arg> =
    unapplier { arg, matchResultBuilder -> elements.all { it.unapply(arg, matchResultBuilder) } }

// same as comap(Option<Arg>::get).filter(Option<Arg>::isNotEmpty).comap(body), but inlined
internal inline fun <T1, T2, T3, T4, T5, T6, Arg, Res> Unapplier<T1, T2, T3, T4, T5, T6, Arg>.comapNotEmpty(crossinline body: (Res) -> Option<Arg>): Unapplier<T1, T2, T3, T4, T5, T6, Res> =
    unapplier { arg, matchResultBuilder ->
        val opt = body(arg)
        if (opt.isEmpty()) return@unapplier false
        unapply(opt.get(), matchResultBuilder)
    }

typealias NoResultBuilder = MatchResultBuilder<Nothing, Nothing, Nothing, Nothing, Nothing, Nothing>
typealias NoResultUnapplier<Arg> = Unapplier<Nothing, Nothing, Nothing, Nothing, Nothing, Nothing, Arg>

class Ignore<T> : NoResultUnapplier<T>() {
    override fun unapply(arg: T, matcher: NoResultBuilder): Boolean = true
}

fun <T> any(): NoResultUnapplier<T> = Ignore()

class Arg1<T> : Unapplier<T, Nothing, Nothing, Nothing, Nothing, Nothing, T>() {
    override fun unapply(arg: T, matcher: MatchResultBuilder<T, Nothing, Nothing, Nothing, Nothing, Nothing>): Boolean {
        matcher.v1 = Option.just(arg)
        return true
    }
}

fun <T> _1(): Unapplier<T, Nothing, Nothing, Nothing, Nothing, Nothing, T> = Arg1()

class Arg2<T> : Unapplier<Nothing, T, Nothing, Nothing, Nothing, Nothing, T>() {
    override fun unapply(arg: T, matcher: MatchResultBuilder<Nothing, T, Nothing, Nothing, Nothing, Nothing>): Boolean {
        matcher.v2 = Option.just(arg)
        return true
    }
}

fun <T> _2(): Unapplier<Nothing, T, Nothing, Nothing, Nothing, Nothing, T> = Arg2()

class Arg3<T> : Unapplier<Nothing, Nothing, T, Nothing, Nothing, Nothing, T>() {
    override fun unapply(arg: T, matcher: MatchResultBuilder<Nothing, Nothing, T, Nothing, Nothing, Nothing>): Boolean {
        matcher.v3 = Option.just(arg)
        return true
    }
}

fun <T> _3(): Unapplier<Nothing, Nothing, T, Nothing, Nothing, Nothing, T> = Arg3()

class Arg4<T> : Unapplier<Nothing, Nothing, Nothing, T, Nothing, Nothing, T>() {
    override fun unapply(arg: T, matcher: MatchResultBuilder<Nothing, Nothing, Nothing, T, Nothing, Nothing>): Boolean {
        matcher.v4 = Option.just(arg)
        return true
    }
}

fun <T> _4(): Unapplier<Nothing, Nothing, Nothing, T, Nothing, Nothing, T> = Arg4()

class Arg5<T> : Unapplier<Nothing, Nothing, Nothing, Nothing, T, Nothing, T>() {
    override fun unapply(arg: T, matcher: MatchResultBuilder<Nothing, Nothing, Nothing, Nothing, T, Nothing>): Boolean {
        matcher.v5 = Option.just(arg)
        return true
    }
}

fun <T> _5(): Unapplier<Nothing, Nothing, Nothing, Nothing, T, Nothing, T> = Arg5()

class Arg6<T> : Unapplier<Nothing, Nothing, Nothing, Nothing, Nothing, T, T>() {
    override fun unapply(arg: T, matcher: MatchResultBuilder<Nothing, Nothing, Nothing, Nothing, Nothing, T>): Boolean {
        matcher.v6 = Option.just(arg)
        return true
    }
}

fun <T> _6(): Unapplier<Nothing, Nothing, Nothing, Nothing, Nothing, T, T> = Arg6()

inline fun <T1, T2, T3, T4, T5, T6, Arg> unapplier(
    crossinline body: (arg: Arg, matchResultBuilder: MatchResultBuilder<T1, T2, T3, T4, T5, T6>) -> Boolean
): Unapplier<T1, T2, T3, T4, T5, T6, Arg> = object : Unapplier<T1, T2, T3, T4, T5, T6, Arg>() {
    override fun unapply(arg: Arg, matcher: MatchResultBuilder<T1, T2, T3, T4, T5, T6>): Boolean = body(arg, matcher)
}

inline fun <Arg> filter(crossinline body: (Arg) -> Boolean): NoResultUnapplier<Arg> =
    unapplier { arg, _ -> body(arg) }

inline fun <Arg> const(crossinline body: () -> Arg): NoResultUnapplier<Arg> =
    unapplier { arg, _ -> arg == body() }

fun <T1, T2, T3, T4, T5, T6, Arg> notNull(
    inner: Unapplier<T1, T2, T3, T4, T5, T6, Arg>
): Unapplier<T1, T2, T3, T4, T5, T6, Arg?> =
    unapplier { arg, matcher ->
        arg ?: return@unapplier false
        inner.unapply(arg, matcher)
    }

infix fun <T1, T2, T3, T4, T5, T6, Arg> Unapplier<T1, T2, T3, T4, T5, T6, Arg>.with(
    that: Unapplier<T1, T2, T3, T4, T5, T6, Arg>
): Unapplier<T1, T2, T3, T4, T5, T6, Arg> =
    unapplier { arg, matcher ->
        this@with.unapply(arg, matcher) && that.unapply(arg, matcher)
    }

class HasType<T>
fun <T> ofType() = HasType<T>()

inline infix fun <T1, T2, T3, T4, T5, T6, reified Arg> HasType<Arg>.with(
    that: Unapplier<T1, T2, T3, T4, T5, T6, Arg>
): Unapplier<T1, T2, T3, T4, T5, T6, Any?> =
    unapplier { arg, matcher ->
        arg is Arg && that.unapply(arg, matcher)
    }

interface Case<T1, T2, T3, T4, T5, T6, in Arg> {
    fun unapply(arg: Arg): MatchResult<T1, T2, T3, T4, T5, T6>?
}

fun <T1, T2, T3, T4, T5, T6, Arg> case(unapplier: Unapplier<T1, T2, T3, T4, T5, T6, Arg>): Case<T1, T2, T3, T4, T5, T6, Arg> =
    object : Case<T1, T2, T3, T4, T5, T6, Arg> {
        override fun unapply(arg: Arg): MatchResult<T1, T2, T3, T4, T5, T6>? {
            val builder = MatchResultBuilder<T1, T2, T3, T4, T5, T6>()
            return when {
                unapplier.unapply(arg, builder) -> builder
                else -> null
            }
        }
    }

infix fun <T1, T2, T3, T4, T5, T6, Arg> Case<T1, T2, T3, T4, T5, T6, Arg>.or(
    that: Case<T1, T2, T3, T4, T5, T6, Arg>
): Case<T1, T2, T3, T4, T5, T6, Arg> =
    object : Case<T1, T2, T3, T4, T5, T6, Arg> {
        override fun unapply(arg: Arg): MatchResult<T1, T2, T3, T4, T5, T6>? =
            this@or.unapply(arg) ?: that.unapply(arg)
    }

inline infix fun <T1, T2, T3, T4, T5, T6, Arg> Case<T1, T2, T3, T4, T5, T6, Arg>.guardedBy(crossinline body: (MatchResult<T1, T2, T3, T4, T5, T6>) -> Boolean): Case<T1, T2, T3, T4, T5, T6, Arg> =
    object : Case<T1, T2, T3, T4, T5, T6, Arg> {
        override fun unapply(arg: Arg): MatchResult<T1, T2, T3, T4, T5, T6>? =
            when(val result = this@guardedBy.unapply(arg)) {
                null -> null
                else -> if(body(result)) result else null
            }
    }
