package ru.spbstu.matchers.wheels

import ru.spbstu.matchers.*
import ru.spbstu.wheels.*

/**
 * Since we are already dependent on wheels, why not support them?
 * */

fun <T1, T2, T3, T4, T5, T6, Arg> optionJust(
    value: Unapplier<T1, T2, T3, T4, T5, T6, Arg>
): Unapplier<T1, T2, T3, T4, T5, T6, Option<Arg>> = value.comapNotEmpty { it }

fun optionEmpty(): NoResultUnapplier<Option<Any?>> = unapplier { arg, _ -> arg.isEmpty() }

fun <T1, T2, T3, T4, T5, T6, Arg> tryJust(
    value: Unapplier<T1, T2, T3, T4, T5, T6, Arg>
): Unapplier<T1, T2, T3, T4, T5, T6, Try<Arg>> = value.comapNotEmpty {
    when {
        it.isNotException() -> Option.just(it.getOrThrow())
        else -> Option.empty()
    }
}

fun <T1, T2, T3, T4, T5, T6> tryException(
    exception: Unapplier<T1, T2, T3, T4, T5, T6, Exception>
): Unapplier<T1, T2, T3, T4, T5, T6, Try<Any?>> = exception.comapNotEmpty {
    Option.ofNullable(it.getExceptionOrNull()).map { it!! }
}

fun <T1, T2, T3, T4, T5, T6, Arg> MutableRef(
    value: Unapplier<T1, T2, T3, T4, T5, T6, Arg>
): Unapplier<T1, T2, T3, T4, T5, T6, MutableRef<Arg>> = value.comap { it.value }
