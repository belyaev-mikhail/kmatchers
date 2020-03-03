package ru.spbstu.matchers

import org.intellij.lang.annotations.Language
import ru.spbstu.wheels.getEntry

private fun <T1, T2, T3, T4, T5, T6, Arg> unapplyIterator(
    iterator: Iterator<Arg>,
    elements: Array<out Unapplier<T1, T2, T3, T4, T5, T6, Arg>>,
    matcher: MatchResultBuilder<T1, T2, T3, T4, T5, T6>
): Boolean {
    for (e in elements) {
        if (!iterator.hasNext()) return false
        if (!e.unapply(iterator.next(), matcher)) return false
    }
    return true
}

fun <T1, T2, T3, T4, T5, T6, Arg> sequence(
    vararg elements: Unapplier<T1, T2, T3, T4, T5, T6, Arg>,
    rest: Unapplier<T1, T2, T3, T4, T5, T6, Sequence<Arg>>
): Unapplier<T1, T2, T3, T4, T5, T6, Sequence<Arg>> = unapplier { arg, matcher ->
    arg.iterator().let { it ->
        unapplyIterator(it, elements, matcher) && rest.unapply(it.asSequence(), matcher)
    }
}

fun <Arg> sequence(): NoResultUnapplier<Sequence<Arg>> = unapplier { arg, _ -> !arg.iterator().hasNext() }

fun <T1, T2, T3, T4, T5, T6, Arg> sequence(
    vararg elements: Unapplier<T1, T2, T3, T4, T5, T6, Arg>
): Unapplier<T1, T2, T3, T4, T5, T6, Sequence<Arg>> = unapplier { arg, matcher ->
    arg.iterator().let { it ->
        unapplyIterator(it, elements, matcher) && !it.hasNext()
    }
}

fun <T1, T2, T3, T4, T5, T6, Arg> collection(
    vararg elements: Unapplier<T1, T2, T3, T4, T5, T6, Arg>,
    size: Unapplier<T1, T2, T3, T4, T5, T6, Int> = any(),
    rest: Unapplier<T1, T2, T3, T4, T5, T6, Sequence<Arg>> = any()
): Unapplier<T1, T2, T3, T4, T5, T6, Collection<Arg>> = unapplier { arg, matcher ->
    size.unapply(arg.size, matcher) &&
            arg.iterator().let { it ->
                unapplyIterator(it, elements, matcher) && rest.unapply(it.asSequence(), matcher)
            }
}

fun <Arg> collection(): NoResultUnapplier<Collection<Arg>> = unapplier { arg, _ -> arg.isEmpty() }

fun <T1, T2, T3, T4, T5, T6, Arg> collection(
    vararg elements: Unapplier<T1, T2, T3, T4, T5, T6, Arg>
): Unapplier<T1, T2, T3, T4, T5, T6, Collection<Arg>> = unapplier { arg, matcher ->
    arg.size == elements.size && unapplyIterator(arg.iterator(), elements, matcher)
}

fun <T1, T2, T3, T4, T5, T6, A, B> Pair(
    first: Unapplier<T1, T2, T3, T4, T5, T6, A> = any(),
    second: Unapplier<T1, T2, T3, T4, T5, T6, B> = any()
): Unapplier<T1, T2, T3, T4, T5, T6, Pair<A, B>> = unapplier { arg, matchResultBuilder ->
    first.unapply(arg.first, matchResultBuilder) && second.unapply(arg.second, matchResultBuilder)
}

fun <T1, T2, T3, T4, T5, T6, A, B, C> Triple(
    first: Unapplier<T1, T2, T3, T4, T5, T6, A> = any(),
    second: Unapplier<T1, T2, T3, T4, T5, T6, B> = any(),
    third: Unapplier<T1, T2, T3, T4, T5, T6, C> = any()
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
