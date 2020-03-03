package ru.spbstu.example

import ru.spbstu.matchers.*
import ru.spbstu.matchers.annotations.GenerateMatchers
import ru.spbstu.matchers.annotations.GenerateMultipleMatchers

@GenerateMultipleMatchers(
    GenerateMatchers(),
    GenerateMatchers(functionName = "toto", functionModifiers = ["infix"])
)
data class MyPair<A, B>(val a: A, val b: B)

sealed class Base
@GenerateMatchers(baseClass = Base::class)
data class Value(val value: Double) : Base()

@GenerateMatchers(baseClass = Base::class)
data class Variable(val name: String) : Base()

@GenerateMatchers(packageName = "ru.spbstu.matchers")
interface Sized {
    val size: Int
}

fun main(args: Array<String>) {
    MyPair(2, "h") match {
        case(
            ofType<MyPair<Int, String>>() with MyPair(
                const { 2 },
                _1()
            )
        ) of { (it) -> println(it) }
        otherwise { }
    }

    val cc = (Variable("x") as Base) match {
        case(Variable(name = re("([a-zA-Z]+)", _1()))) of { (name) -> name }
        case(Value(value = _1())) of { (it) -> it }
    }
    cc.let { println(it) }
}

