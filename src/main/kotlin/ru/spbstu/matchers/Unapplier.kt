package ru.spbstu.matchers

import ru.spbstu.wheels.Option

abstract class Unapplier<out T1, out T2, out T3, out T4, out T5, out T6, in Arg> {
    abstract fun unapply(arg: Arg, matcher: MatchResultBuilder<
            @UnsafeVariance T1,
            @UnsafeVariance T2,
            @UnsafeVariance T3,
            @UnsafeVariance T4,
            @UnsafeVariance T5,
            @UnsafeVariance T6>
    ): Boolean
}

typealias NoResultBuilder = MatchResultBuilder<Nothing, Nothing, Nothing, Nothing, Nothing, Nothing>
typealias NoResultUnapplier<Arg> = Unapplier<Nothing, Nothing, Nothing, Nothing, Nothing, Nothing, Arg>

class Ignore<T> : NoResultUnapplier<T>() {
    override fun unapply(arg: T, matcher: NoResultBuilder): Boolean = true
}

fun <T> ignore(): NoResultUnapplier<T> = Ignore()

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

inline fun <Arg> guard(crossinline body: (Arg) -> Boolean): NoResultUnapplier<Arg> =
    object : NoResultUnapplier<Arg>() {
        override fun unapply(arg: Arg, matcher: NoResultBuilder): Boolean = body(arg)
    }

inline fun <Arg> const(crossinline body: () -> Arg): NoResultUnapplier<Arg> =
    object : NoResultUnapplier<Arg>() {
        override fun unapply(arg: Arg, matcher: NoResultBuilder): Boolean = arg == body()
    }

fun <T1, T2, T3, T4, T5, T6, Arg> notNull(
    inner: Unapplier<T1, T2, T3, T4, T5, T6, Arg>
): Unapplier<T1, T2, T3, T4, T5, T6, Arg?> = object : Unapplier<T1, T2, T3, T4, T5, T6, Arg?>() {
    override fun unapply(arg: Arg?, matcher: MatchResultBuilder<T1, T2, T3, T4, T5, T6>): Boolean {
        arg ?: return false
        return inner.unapply(arg, matcher)
    }
}

fun <T1, T2, T3, T4, T5, T6, Arg> sequence(
    vararg elements: Unapplier<T1, T2, T3, T4, T5, T6, Arg>,
    rest: Unapplier<T1, T2, T3, T4, T5, T6, Sequence<Arg>>
): Unapplier<T1, T2, T3, T4, T5, T6, Sequence<Arg>> = object : Unapplier<T1, T2, T3, T4, T5, T6, Sequence<Arg>>() {
    override fun unapply(arg: Sequence<Arg>, matcher: MatchResultBuilder<T1, T2, T3, T4, T5, T6>): Boolean {
        val it = arg.iterator()
        for(e in elements) {
            if(!it.hasNext()) return false
            if(!e.unapply(it.next(), matcher)) return false
        }
        return rest.unapply(it.asSequence(), matcher)
    }
}

fun <Arg> sequence(): NoResultUnapplier<Sequence<Arg>> = object : NoResultUnapplier<Sequence<Arg>>() {
    override fun unapply(arg: Sequence<Arg>, matcher: NoResultBuilder): Boolean =
        !arg.iterator().hasNext()
}

fun <T1, T2, T3, T4, T5, T6, Arg> sequence(
    vararg elements: Unapplier<T1, T2, T3, T4, T5, T6, Arg>
): Unapplier<T1, T2, T3, T4, T5, T6, Sequence<Arg>> = object : Unapplier<T1, T2, T3, T4, T5, T6, Sequence<Arg>>() {
    override fun unapply(arg: Sequence<Arg>, matcher: MatchResultBuilder<T1, T2, T3, T4, T5, T6>): Boolean {
        val it = arg.iterator()
        for(e in elements) {
            if(!it.hasNext()) return false
            if(!e.unapply(it.next(), matcher)) return false
        }
        return !it.hasNext()
    }
}

fun <T1, T2, T3, T4, T5, T6, Arg> collection(
    vararg elements: Unapplier<T1, T2, T3, T4, T5, T6, Arg>,
    rest: Unapplier<T1, T2, T3, T4, T5, T6, Sequence<Arg>>
): Unapplier<T1, T2, T3, T4, T5, T6, Collection<Arg>> = object : Unapplier<T1, T2, T3, T4, T5, T6, Collection<Arg>>() {
    override fun unapply(arg: Collection<Arg>, matcher: MatchResultBuilder<T1, T2, T3, T4, T5, T6>): Boolean {
        val it = arg.iterator()
        for(e in elements) {
            if(!it.hasNext()) return false
            if(!e.unapply(it.next(), matcher)) return false
        }
        return rest.unapply(it.asSequence(), matcher)
    }
}

fun <T1, T2, T3, T4, T5, T6, Arg> collection(
    vararg elements: Unapplier<T1, T2, T3, T4, T5, T6, Arg>
): Unapplier<T1, T2, T3, T4, T5, T6, Collection<Arg>> = object : Unapplier<T1, T2, T3, T4, T5, T6, Collection<Arg>>() {
    override fun unapply(arg: Collection<Arg>, matcher: MatchResultBuilder<T1, T2, T3, T4, T5, T6>): Boolean {
        val it = arg.iterator()
        for(e in elements) {
            if(!it.hasNext()) return false
            if(!e.unapply(it.next(), matcher)) return false
        }
        return !it.hasNext()
    }
}
