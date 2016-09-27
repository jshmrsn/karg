package com.jshmrsn.karg

// This file exists to help write the README. Not a real main function.

class ExampleArguments(raw: RawArguments) : Arguments(raw,
     name = "example",
     description = "An example of Karg's usage.") {

   // Parameter/field values can be non-null and val (not var) since they are initialized at construction
   val textToPrint = parameter(
        name = "text-to-print",
        aliasNames = listOf("text"),
        shortNames = listOf('t'),
        description = "Print this text."
   )

   // Argument values are forced to be nullable when the argument is optional (and no default is provided)
   val textToPrintAfter = optionalParameter(
        name = "text-to-print-after",
        description = "If provided, print this text after the primary text."
   )

   val shouldShout = optionalFlag(
        name = "shout",
        description = "Print in all uppercase.",
        default = false
   )

   // Positional arguments must be defined after all other arguments (but can be provided in any order on the command line)
   val moreTextToPrint = positionalArguments(name = "more-text")
}

fun main(args: Array<String>) {
   val exampleArguments = parseArguments(args, ::ExampleArguments)

   // textToPrint is not optional, so null check is not needed
   var output = exampleArguments.textToPrint

   exampleArguments.moreTextToPrint.forEach { moreTextToPrintEntry ->
      output += " " + moreTextToPrintEntry
   }

   // textToPrintAfter is optional, so compiler forces a null check
   if (exampleArguments.textToPrintAfter != null) {
      output += "\n" + exampleArguments.textToPrintAfter
   }

   // shouldShout is optional, but has a default value, so null check is not needed
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

      main(arrayOf("--text", "Hello", "--shout", "more", "text"))
   }
}
