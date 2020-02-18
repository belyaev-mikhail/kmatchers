package ru.spbstu.matchers

/*
* TODO: generate these guys?
* TODO: unsigned arrays?
* */

fun <T1, T2, T3, T4, T5, T6, Arg> array(
    vararg elements: Unapplier<T1, T2, T3, T4, T5, T6, Arg>,
    size: Unapplier<T1, T2, T3, T4, T5, T6, Int> = any(),
    rest: Unapplier<T1, T2, T3, T4, T5, T6, Sequence<Arg>> = any()
): Unapplier<T1, T2, T3, T4, T5, T6, Array<Arg>> = object : Unapplier<T1, T2, T3, T4, T5, T6, Array<Arg>>() {
    override fun unapply(arg: Array<Arg>, matcher: MatchResultBuilder<T1, T2, T3, T4, T5, T6>): Boolean {
        if (!size.unapply(arg.size, matcher)) return false

        val it = arg.iterator()
        for (e in elements) {
            if (!it.hasNext()) return false
            if (!e.unapply(it.next(), matcher)) return false
        }
        return rest.unapply(it.asSequence(), matcher)
    }
}

fun <Arg> array(): NoResultUnapplier<Array<Arg>> = object : NoResultUnapplier<Array<Arg>>() {
    override fun unapply(arg: Array<Arg>, matcher: NoResultBuilder): Boolean = arg.isEmpty()
}

fun <T1, T2, T3, T4, T5, T6, Arg> array(
    vararg elements: Unapplier<T1, T2, T3, T4, T5, T6, Arg>
): Unapplier<T1, T2, T3, T4, T5, T6, Array<Arg>> = object : Unapplier<T1, T2, T3, T4, T5, T6, Array<Arg>>() {
    override fun unapply(arg: Array<Arg>, matcher: MatchResultBuilder<T1, T2, T3, T4, T5, T6>): Boolean {
        val it = arg.iterator()
        for (e in elements) {
            if (!it.hasNext()) return false
            if (!e.unapply(it.next(), matcher)) return false
        }
        return !it.hasNext()
    }
}

fun <T1, T2, T3, T4, T5, T6> intArray(
    vararg elements: Unapplier<T1, T2, T3, T4, T5, T6, Int>,
    size: Unapplier<T1, T2, T3, T4, T5, T6, Int> = any(),
    rest: Unapplier<T1, T2, T3, T4, T5, T6, Sequence<Int>> = any()
): Unapplier<T1, T2, T3, T4, T5, T6, IntArray> = object : Unapplier<T1, T2, T3, T4, T5, T6, IntArray>() {
    override fun unapply(arg: IntArray, matcher: MatchResultBuilder<T1, T2, T3, T4, T5, T6>): Boolean {
        if (!size.unapply(arg.size, matcher)) return false

        val it = arg.iterator()
        for (e in elements) {
            if (!it.hasNext()) return false
            if (!e.unapply(it.next(), matcher)) return false
        }
        return rest.unapply(it.asSequence(), matcher)
    }
}

fun intArray(): NoResultUnapplier<IntArray> = object : NoResultUnapplier<IntArray>() {
    override fun unapply(arg: IntArray, matcher: NoResultBuilder): Boolean = arg.isEmpty()
}

fun <T1, T2, T3, T4, T5, T6> intArray(
    vararg elements: Unapplier<T1, T2, T3, T4, T5, T6, Int>
): Unapplier<T1, T2, T3, T4, T5, T6, IntArray> = object : Unapplier<T1, T2, T3, T4, T5, T6, IntArray>() {
    override fun unapply(arg: IntArray, matcher: MatchResultBuilder<T1, T2, T3, T4, T5, T6>): Boolean {
        val it = arg.iterator()
        for (e in elements) {
            if (!it.hasNext()) return false
            if (!e.unapply(it.next(), matcher)) return false
        }
        return !it.hasNext()
    }
}

fun <T1, T2, T3, T4, T5, T6> charArray(
    vararg elements: Unapplier<T1, T2, T3, T4, T5, T6, Char>,
    size: Unapplier<T1, T2, T3, T4, T5, T6, Int> = any(),
    rest: Unapplier<T1, T2, T3, T4, T5, T6, Sequence<Char>> = any()
): Unapplier<T1, T2, T3, T4, T5, T6, CharArray> = object : Unapplier<T1, T2, T3, T4, T5, T6, CharArray>() {
    override fun unapply(arg: CharArray, matcher: MatchResultBuilder<T1, T2, T3, T4, T5, T6>): Boolean {
        if (!size.unapply(arg.size, matcher)) return false

        val it = arg.iterator()
        for (e in elements) {
            if (!it.hasNext()) return false
            if (!e.unapply(it.next(), matcher)) return false
        }
        return rest.unapply(it.asSequence(), matcher)
    }
}

fun charArray(): NoResultUnapplier<CharArray> = object : NoResultUnapplier<CharArray>() {
    override fun unapply(arg: CharArray, matcher: NoResultBuilder): Boolean = arg.isEmpty()
}

fun <T1, T2, T3, T4, T5, T6> charArray(
    vararg elements: Unapplier<T1, T2, T3, T4, T5, T6, Char>
): Unapplier<T1, T2, T3, T4, T5, T6, CharArray> = object : Unapplier<T1, T2, T3, T4, T5, T6, CharArray>() {
    override fun unapply(arg: CharArray, matcher: MatchResultBuilder<T1, T2, T3, T4, T5, T6>): Boolean {
        val it = arg.iterator()
        for (e in elements) {
            if (!it.hasNext()) return false
            if (!e.unapply(it.next(), matcher)) return false
        }
        return !it.hasNext()
    }
}

fun <T1, T2, T3, T4, T5, T6> shortArray(
    vararg elements: Unapplier<T1, T2, T3, T4, T5, T6, Short>,
    size: Unapplier<T1, T2, T3, T4, T5, T6, Int> = any(),
    rest: Unapplier<T1, T2, T3, T4, T5, T6, Sequence<Short>> = any()
): Unapplier<T1, T2, T3, T4, T5, T6, ShortArray> = object : Unapplier<T1, T2, T3, T4, T5, T6, ShortArray>() {
    override fun unapply(arg: ShortArray, matcher: MatchResultBuilder<T1, T2, T3, T4, T5, T6>): Boolean {
        if (!size.unapply(arg.size, matcher)) return false

        val it = arg.iterator()
        for (e in elements) {
            if (!it.hasNext()) return false
            if (!e.unapply(it.next(), matcher)) return false
        }
        return rest.unapply(it.asSequence(), matcher)
    }
}

fun shortArray(): NoResultUnapplier<ShortArray> = object : NoResultUnapplier<ShortArray>() {
    override fun unapply(arg: ShortArray, matcher: NoResultBuilder): Boolean = arg.isEmpty()
}

fun <T1, T2, T3, T4, T5, T6> shortArray(
    vararg elements: Unapplier<T1, T2, T3, T4, T5, T6, Short>
): Unapplier<T1, T2, T3, T4, T5, T6, ShortArray> = object : Unapplier<T1, T2, T3, T4, T5, T6, ShortArray>() {
    override fun unapply(arg: ShortArray, matcher: MatchResultBuilder<T1, T2, T3, T4, T5, T6>): Boolean {
        val it = arg.iterator()
        for (e in elements) {
            if (!it.hasNext()) return false
            if (!e.unapply(it.next(), matcher)) return false
        }
        return !it.hasNext()
    }
}

fun <T1, T2, T3, T4, T5, T6> byteArray(
    vararg elements: Unapplier<T1, T2, T3, T4, T5, T6, Byte>,
    size: Unapplier<T1, T2, T3, T4, T5, T6, Int> = any(),
    rest: Unapplier<T1, T2, T3, T4, T5, T6, Sequence<Byte>> = any()
): Unapplier<T1, T2, T3, T4, T5, T6, ByteArray> = object : Unapplier<T1, T2, T3, T4, T5, T6, ByteArray>() {
    override fun unapply(arg: ByteArray, matcher: MatchResultBuilder<T1, T2, T3, T4, T5, T6>): Boolean {
        if (!size.unapply(arg.size, matcher)) return false

        val it = arg.iterator()
        for (e in elements) {
            if (!it.hasNext()) return false
            if (!e.unapply(it.next(), matcher)) return false
        }
        return rest.unapply(it.asSequence(), matcher)
    }
}

fun byteArray(): NoResultUnapplier<ByteArray> = object : NoResultUnapplier<ByteArray>() {
    override fun unapply(arg: ByteArray, matcher: NoResultBuilder): Boolean = arg.isEmpty()
}

fun <T1, T2, T3, T4, T5, T6> byteArray(
    vararg elements: Unapplier<T1, T2, T3, T4, T5, T6, Byte>
): Unapplier<T1, T2, T3, T4, T5, T6, ByteArray> = object : Unapplier<T1, T2, T3, T4, T5, T6, ByteArray>() {
    override fun unapply(arg: ByteArray, matcher: MatchResultBuilder<T1, T2, T3, T4, T5, T6>): Boolean {
        val it = arg.iterator()
        for (e in elements) {
            if (!it.hasNext()) return false
            if (!e.unapply(it.next(), matcher)) return false
        }
        return !it.hasNext()
    }
}

fun <T1, T2, T3, T4, T5, T6> longArray(
    vararg elements: Unapplier<T1, T2, T3, T4, T5, T6, Long>,
    size: Unapplier<T1, T2, T3, T4, T5, T6, Int> = any(),
    rest: Unapplier<T1, T2, T3, T4, T5, T6, Sequence<Long>> = any()
): Unapplier<T1, T2, T3, T4, T5, T6, LongArray> = object : Unapplier<T1, T2, T3, T4, T5, T6, LongArray>() {
    override fun unapply(arg: LongArray, matcher: MatchResultBuilder<T1, T2, T3, T4, T5, T6>): Boolean {
        if (!size.unapply(arg.size, matcher)) return false

        val it = arg.iterator()
        for (e in elements) {
            if (!it.hasNext()) return false
            if (!e.unapply(it.next(), matcher)) return false
        }
        return rest.unapply(it.asSequence(), matcher)
    }
}

fun longArray(): NoResultUnapplier<LongArray> = object : NoResultUnapplier<LongArray>() {
    override fun unapply(arg: LongArray, matcher: NoResultBuilder): Boolean = arg.isEmpty()
}

fun <T1, T2, T3, T4, T5, T6> longArray(
    vararg elements: Unapplier<T1, T2, T3, T4, T5, T6, Long>
): Unapplier<T1, T2, T3, T4, T5, T6, LongArray> = object : Unapplier<T1, T2, T3, T4, T5, T6, LongArray>() {
    override fun unapply(arg: LongArray, matcher: MatchResultBuilder<T1, T2, T3, T4, T5, T6>): Boolean {
        val it = arg.iterator()
        for (e in elements) {
            if (!it.hasNext()) return false
            if (!e.unapply(it.next(), matcher)) return false
        }
        return !it.hasNext()
    }
}
