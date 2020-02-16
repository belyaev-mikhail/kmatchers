package ru.spbstu.matchers

import ru.spbstu.wheels.getOption

fun <T1, T2, T3, T4, T5, T6, Arg> sequence(
    vararg elements: Unapplier<T1, T2, T3, T4, T5, T6, Arg>,
    rest: Unapplier<T1, T2, T3, T4, T5, T6, Sequence<Arg>>
): Unapplier<T1, T2, T3, T4, T5, T6, Sequence<Arg>> = object : Unapplier<T1, T2, T3, T4, T5, T6, Sequence<Arg>>() {
    override fun unapply(arg: Sequence<Arg>, matcher: MatchResultBuilder<T1, T2, T3, T4, T5, T6>): Boolean {
        val it = arg.iterator()
        for (e in elements) {
            if (!it.hasNext()) return false
            if (!e.unapply(it.next(), matcher)) return false
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
        for (e in elements) {
            if (!it.hasNext()) return false
            if (!e.unapply(it.next(), matcher)) return false
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
        for (e in elements) {
            if (!it.hasNext()) return false
            if (!e.unapply(it.next(), matcher)) return false
        }
        return rest.unapply(it.asSequence(), matcher)
    }
}

fun <Arg> collection(): NoResultUnapplier<Collection<Arg>> = object : NoResultUnapplier<Collection<Arg>>() {
    override fun unapply(arg: Collection<Arg>, matcher: NoResultBuilder): Boolean = arg.isEmpty()
}

fun <T1, T2, T3, T4, T5, T6, Arg> collection(
    vararg elements: Unapplier<T1, T2, T3, T4, T5, T6, Arg>
): Unapplier<T1, T2, T3, T4, T5, T6, Collection<Arg>> = object : Unapplier<T1, T2, T3, T4, T5, T6, Collection<Arg>>() {
    override fun unapply(arg: Collection<Arg>, matcher: MatchResultBuilder<T1, T2, T3, T4, T5, T6>): Boolean {
        val it = arg.iterator()
        for (e in elements) {
            if (!it.hasNext()) return false
            if (!e.unapply(it.next(), matcher)) return false
        }
        return !it.hasNext()
    }
}

fun <T1, T2, T3, T4, T5, T6, A, B> Pair(
    first: Unapplier<T1, T2, T3, T4, T5, T6, A> = ignore(),
    second: Unapplier<T1, T2, T3, T4, T5, T6, B> = ignore()
): Unapplier<T1, T2, T3, T4, T5, T6, Pair<A, B>> = unapplier { arg, matchResultBuilder ->
    first.unapply(arg.first, matchResultBuilder) && second.unapply(arg.second, matchResultBuilder)
}

fun <T1, T2, T3, T4, T5, T6, A, B, C> Triple(
    first: Unapplier<T1, T2, T3, T4, T5, T6, A> = ignore(),
    second: Unapplier<T1, T2, T3, T4, T5, T6, B> = ignore(),
    third: Unapplier<T1, T2, T3, T4, T5, T6, C> = ignore()
): Unapplier<T1, T2, T3, T4, T5, T6, Triple<A, B, C>> = unapplier { arg, matchResultBuilder ->
    first.unapply(arg.first, matchResultBuilder)
            && second.unapply(arg.second, matchResultBuilder)
            && third.unapply(arg.third, matchResultBuilder)
}

fun <T1, T2, T3, T4, T5, T6, K, V> mapContaining(
    vararg entries: Pair<K, Unapplier<T1, T2, T3, T4, T5, T6, V>>
): Unapplier<T1, T2, T3, T4, T5, T6, Map<K, V>> = unapplier { arg, matchResultBuilder ->
    entries.all { (k, m) ->
        val value = arg.getOption(k)
        value.isNotEmpty() && m.unapply(value.get(), matchResultBuilder)
    }
}
