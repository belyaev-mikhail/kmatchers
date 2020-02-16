package ru.spbstu

import ru.spbstu.matchers.*

data class Meee(val a: Int, val moo: Meee?)

fun <T1, T2, T3, T4, T5, T6> Meee(a: Unapplier<T1, T2, T3, T4, T5, T6, Int> = ignore(),
                                  moo: Unapplier<T1, T2, T3, T4, T5, T6, Meee?> = ignore()) =
    object : Unapplier<T1, T2, T3, T4, T5, T6, Meee>() {
        override fun unapply(arg: Meee, matcher: MatchResultBuilder<T1, T2, T3, T4, T5, T6>): Boolean =
            a.unapply(arg.a, matcher) && moo.unapply(arg.moo, matcher)
    }

sealed class Expr
data class Var(val name: String): Expr()
data class Plus(val lhv: Expr, val rhv: Expr): Expr()
data class Const(val value: Long): Expr()

operator fun Expr.plus(that: Expr): Expr = Plus(this, that)

fun <T1, T2, T3, T4, T5, T6> Var(
    name: Unapplier<T1, T2, T3, T4, T5, T6, String> = ignore()
) = object : Unapplier<T1, T2, T3, T4, T5, T6, Expr>() {
    override fun unapply(arg: Expr, matcher: MatchResultBuilder<T1, T2, T3, T4, T5, T6>): Boolean =
        arg is Var && name.unapply(arg.name, matcher)
}

fun <T1, T2, T3, T4, T5, T6> Plus(
    lhv: Unapplier<T1, T2, T3, T4, T5, T6, Expr> = ignore(),
    rhv: Unapplier<T1, T2, T3, T4, T5, T6, Expr> = ignore()
) = object : Unapplier<T1, T2, T3, T4, T5, T6, Expr>() {
    override fun unapply(arg: Expr, matcher: MatchResultBuilder<T1, T2, T3, T4, T5, T6>): Boolean =
        arg is Plus && lhv.unapply(arg.lhv, matcher) && rhv.unapply(arg.rhv, matcher)
}

operator fun <T1, T2, T3, T4, T5, T6> Unapplier<T1, T2, T3, T4, T5, T6, Expr>.plus(that: Unapplier<T1, T2, T3, T4, T5, T6, Expr>) =
    Plus(this, that)

fun <T1, T2, T3, T4, T5, T6> Const(
    value: Unapplier<T1, T2, T3, T4, T5, T6, Long> = ignore()
) = object : Unapplier<T1, T2, T3, T4, T5, T6, Expr>() {
    override fun unapply(arg: Expr, matcher: MatchResultBuilder<T1, T2, T3, T4, T5, T6>): Boolean =
        arg is Const && value.unapply(arg.value, matcher)
}

fun simplifyStep(e: Expr): Expr = match(e) {
    Plus(Const(_1()), Const(_2())) of { (l, r) ->
        Const(l + r)
    }
    Plus(_1(), Const(const { 0L })) of { (it) ->
        it
    }
    Plus(Const(const { 0L }), _1()) of { (it) ->
        it
    }
    Plus(_1(), _2()) of { (l, r) ->
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

fun main() {
    println(simplify(Var("x") + Const(0) + Var("y")))
    println(simplify(Const(2) + Const(4) + Const(6)))
    println(simplify(Const(2) + Var("y")))

    match((0..10).asSequence()) {
        sequence(_1<Int>(), rest = sequence(_2<Int>())) of { (a, b) -> a / b }

        otherwise { 2 }
    }

    val cc = collection(const { 1 }, const { 2 }, rest = _1())

    match(listOf<Int>(1,2,3,4)) {
        cc of { (it) ->
            println(it.toList())
        }

        collection(const { 1 }, const { 2 }, rest = _1()) of { (it) ->
            println(it.toList())
        }

        otherwise { }
    }

}
