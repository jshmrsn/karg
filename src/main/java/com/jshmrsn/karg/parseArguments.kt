package com.jshmrsn.karg

import com.jshmrsn.karg.RawArguments
import com.jshmrsn.karg.internal.RawArgumentsImplementation
import kotlin.system.exitProcess

val defaultPrintHelpCallback = { generatedHelpText: String ->
    println(generatedHelpText)
    exitProcess(0)
}

fun <T: Arguments> parseArguments(args: List<String>,
                                  builder: (rawArguments: RawArguments) -> T,
                                  printHelpCallback: (String) -> Unit = defaultPrintHelpCallback): T {
    val parsedArguments = RawArgumentsImplementation(args)
    val result = builder(parsedArguments)
    result.finalize(printHelpCallback)
    return result
}


fun <T: Arguments> parseArguments(args: Array<String>, builder: (rawArguments: RawArguments) -> T,
                                  printHelpCallback: (String) -> Unit = defaultPrintHelpCallback): T {
    return parseArguments(args.toList(), builder, printHelpCallback)
}
