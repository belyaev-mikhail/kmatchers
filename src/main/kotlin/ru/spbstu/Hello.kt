@file:Suppress(Warnings.NON_APPLICABLE_CALL_FOR_BUILDER_INFERENCE)
package ru.spbstu

import kotlinx.warnings.Warnings
import ru.spbstu.matchers.*

data class Meee(val a: Int, val moo: Meee?)

fun <T1, T2, T3, T4, T5, T6> Meee(a: Unapplier<T1, T2, T3, T4, T5, T6, Int> = any(),
                                  moo: Unapplier<T1, T2, T3, T4, T5, T6, Meee?> = any()
): Unapplier<T1, T2, T3, T4, T5, T6, Meee> =
    unapplier { arg, matcher -> a.unapply(arg.a, matcher) && moo.unapply(arg.moo, matcher) }

sealed class Expr
data class Var(val name: String): Expr()
data class Plus(val lhv: Expr, val rhv: Expr): Expr()
data class Times(val lhv: Expr, val rhv: Expr): Expr()
data class Const(val value: Long): Expr()

operator fun Expr.plus(that: Expr): Expr = Plus(this, that)
operator fun Expr.times(that: Expr): Expr = Times(this, that)

fun <T1, T2, T3, T4, T5, T6> Var(
    name: Unapplier<T1, T2, T3, T4, T5, T6, String> = any()
): Unapplier<T1, T2, T3, T4, T5, T6, Expr> = unapplier { arg, matcher ->
        arg is Var && name.unapply(arg.name, matcher)
}

fun <T1, T2, T3, T4, T5, T6> Plus(
    lhv: Unapplier<T1, T2, T3, T4, T5, T6, Expr> = any(),
    rhv: Unapplier<T1, T2, T3, T4, T5, T6, Expr> = any()
): Unapplier<T1, T2, T3, T4, T5, T6, Expr> = unapplier { arg, matcher ->
        arg is Plus && lhv.unapply(arg.lhv, matcher) && rhv.unapply(arg.rhv, matcher)
}

fun <T1, T2, T3, T4, T5, T6> Times(
    lhv: Unapplier<T1, T2, T3, T4, T5, T6, Expr> = any(),
    rhv: Unapplier<T1, T2, T3, T4, T5, T6, Expr> = any()
): Unapplier<T1, T2, T3, T4, T5, T6, Expr> = unapplier { arg, matcher ->
    arg is Times && lhv.unapply(arg.lhv, matcher) && rhv.unapply(arg.rhv, matcher)
}

operator fun <T1, T2, T3, T4, T5, T6> Unapplier<T1, T2, T3, T4, T5, T6, Expr>.plus(that: Unapplier<T1, T2, T3, T4, T5, T6, Expr>) =
    Plus(this, that)

operator fun <T1, T2, T3, T4, T5, T6> Unapplier<T1, T2, T3, T4, T5, T6, Expr>.times(that: Unapplier<T1, T2, T3, T4, T5, T6, Expr>) =
    Times(this, that)

fun <T1, T2, T3, T4, T5, T6> Const(
    value: Unapplier<T1, T2, T3, T4, T5, T6, Long> = any()
): Unapplier<T1, T2, T3, T4, T5, T6, Expr> = unapplier { arg, matcher ->
    arg is Const && value.unapply(arg.value, matcher)
}

fun simplifyStep(e: Expr): Expr = match(e) {
    case(Const(_1()) + Const(_2())) of { (l, r) ->
        Const(l + r)
    }
    case(_1<Expr>() + Const(const { 0L })) or
            case(Const(const { 0L }) + _1<Expr>()) of { (v) ->
        v
    }
    case(any<Expr>() * Const(const { 0L })) or
            case(Const(const { 0L }) * any<Expr>()) of { _ ->
        Const(0)
    }
    case(_1<Expr>() * Const(const { 1L })) or
            case(Const(const { 1L }) * _1<Expr>()) of { (v) ->
        v
    }
    case(_1<Expr>() + _2<Expr>()) of { (l, r) ->
        Plus(simplify(l), simplify(r))
    }

    otherwise { e }
}

fun simplify(e: Expr): Expr {
    var expr = e
    var simplified = simplifyStep(expr)
    while(expr != simplified) {
        expr = simplified
        simplified = simplifyStep(expr)
    }
    return expr
}

fun evalStep(e: Expr): Long = match(e) {
    case(ofType<Const>() with _1()) of { (v) -> v.value }
    case(_1<Expr>() + _2<Expr>()) of { (l, r) ->
        evalStep(l) + evalStep(r)
    }

    otherwise { throw IllegalArgumentException("Cannot eval $e") }
}

fun main() {
    println(simplify(Var("x") + Const(0) + Var("y")))
    println(simplify(Const(2) + Const(4) + Const(6)))
    println(simplify(Const(2) + Var("y")))

    match((0..10).asSequence()) {
        case(sequence(_1<Int>(), rest = sequence(_2<Int>()))) of { (a, b) -> a / b }

        otherwise { 2 }
    }

    val cc = collection(const { 1 }, const { 2 }, rest = _1())

    match(listOf<Int>(1,2,3,4)) {
        case(cc) of { (it) ->
            println(it.toList())
        }

        case(collection(const { 1 }, const { 2 }, rest = _1())) of { (it) ->
            println(it.toList())
        }

        otherwise { }
    }

    val failing = match(2) {
        otherwise { "" }
    }

    val xx = match(mapOf(3 to "World", 2 to "Hello")) {
        case(mapContaining(2 to _1<String>(), 3 to _2<String>())) of { (v2, v3) ->
            "$v2 $v3"
        }

        otherwise { "" }
    }
    println(xx)

    match(2 to listOf("Hello")) {
        case(Pair(first = const{ 3 } with _1(), second = any<List<String>>())) of {
            println("first")
        }

        case(Pair(
            _1<Int>(),
            collection(_2<String>() with re("H(.*)lo", _3()))
        )) guardedBy { (n, _, _) -> n > 0 } of { (n, s, r) ->
            println("s = $s; n = $n; r = $r")
        }

        otherwise { println("last") }
    }

    match(Any()) {
        case(ofType<List<Int>>() with _1()) of {

        }

        case(ofType<Int>() with _1()) or
                case(ofType<List<Int>>() with collection(_1())) of { (it) ->
        }

    }

}
