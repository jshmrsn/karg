package com.jshmrsn.karg

import com.jshmrsn.karg.internal.RawArguments

// This file exists to help write the README. Not a real main function.

class ExampleArguments(raw: RawArguments) : Arguments(raw,
                name = "example",
                description = "An example of Karg's usage.") {
    val textToPrint = parameter(
            name = "text-to-print",
            aliasNames = listOf("text"),
            shortNames= listOf('t'),
            description = "Print this text."
    )

    val textToPrintAfter = optionalParameter(
            name = "text-to-print-after",
            description = "If provided, print this text after the primary text."
    )

    val shouldShout = optionalFlag(
            name = "shout",
            description = "Print in all uppercase.",
            default = false
    )
}

fun main(args: Array<String>) {
    val exampleArguments = parseArguments(args, { ExampleArguments(it) })

    // textToPrint is not optional, so null check is not needed
    var output = exampleArguments.textToPrint

    // textToPrintAfter is optional, so compiler forces a null check
    if (exampleArguments.textToPrintAfter != null) {
        output += "\n" + exampleArguments.textToPrintAfter
    }

    // shouldShould is optional, but has a default value, so null check is not needed
    if (exampleArguments.shouldShout) {
        output = output.toUpperCase()
    }

    println(output)
}

class Example {
    @org.junit.Test
    fun example() {
        val helpText = inspectArguments { ExampleArguments(it) }.generateHelpText()
        println(helpText)

        main(arrayOf("--text", "Hello", "--shout"))
    }
}
