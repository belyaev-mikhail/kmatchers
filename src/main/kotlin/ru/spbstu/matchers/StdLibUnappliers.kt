package ru.spbstu.matchers

import org.intellij.lang.annotations.Language
import ru.spbstu.wheels.getEntry

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
    size: Unapplier<T1, T2, T3, T4, T5, T6, Int> = ignore(),
    rest: Unapplier<T1, T2, T3, T4, T5, T6, Sequence<Arg>> = ignore()
): Unapplier<T1, T2, T3, T4, T5, T6, Collection<Arg>> = object : Unapplier<T1, T2, T3, T4, T5, T6, Collection<Arg>>() {
    override fun unapply(arg: Collection<Arg>, matcher: MatchResultBuilder<T1, T2, T3, T4, T5, T6>): Boolean {
        if (!size.unapply(arg.size, matcher)) return false

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
        val value = arg.getEntry(k)
        value != null && m.unapply(value.value, matchResultBuilder)
    }
}

fun re(regex: Regex): NoResultUnapplier<CharSequence> = unapplier { arg, _ ->
    regex.matches(arg)
}

fun re(@Language("RegExp") regex: String): NoResultUnapplier<CharSequence> = re(Regex(regex))

fun <T1, T2, T3, T4, T5, T6> re(
    regex: Regex,
    vararg groups: Unapplier<T1, T2, T3, T4, T5, T6, CharSequence>
): Unapplier<T1, T2, T3, T4, T5, T6, CharSequence> = unapplier { arg, matchResultBuilder ->
    val mres = regex.matchEntire(arg) ?: return@unapplier false
    val mgroups = mres.groups
    if (groups.size + 1 != mgroups.size) return@unapplier false

    for (i in groups.indices) {
        val mgroup = mgroups[i + 1] ?: return@unapplier false
        if (!groups[i].unapply(mgroup.value, matchResultBuilder)) return@unapplier false
    }
    true
}

fun <T1, T2, T3, T4, T5, T6> re(
    @Language("RegExp") regex: String,
    vararg groups: Unapplier<T1, T2, T3, T4, T5, T6, CharSequence>
): Unapplier<T1, T2, T3, T4, T5, T6, CharSequence> = re(Regex(regex), *groups)
