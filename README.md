Karg
==============
- A command line argument parser built specifically for Kotlin.
- Leverages Kotlin's language features to provide strong type and null safety.
- No use of reflection or annotations. No dependencies.

[![Circle CI](https://circleci.com/gh/jshmrsn/karg.svg?style=svg)](https://circleci.com/gh/jshmrsn/karg)

Why?
==============
There's many existing argument parsers built for Java, but none of them can take full advantage of Kotlin's language features. For example, Karg integrates the optionality of command line parameters with Kotlin's compile-time nullability checks.

Early preview status
==============
I recently built Karg for my own use cases because I wasn't satisfied with using argument parsers built for Java. Thus, Karg has only been used in a few places so far, and likely lacks many features important to people other than myself. For example, Karg does not explicitly support sub-commands with separate parameter configurations (as in ```git pull``` vs ```git checkout```). I also haven't setup proper distribution for Maven, Gradle, etc..

I am putting this up on GitHub in its early state to measure if there's interest in using something like Karg over other Java argument parsers. If there is, I would love to get feedback on Karg's APIs, etc.. Pull requests welcomed!

Example usage
==============
For additional usage examples, please [**view the tests**](https://github.com/jshmrsn/karg/tree/master/src/test/java/com/jshmrsn/karg).

```kotlin
class ExampleArguments(raw: RawArguments) : Arguments(raw,
                name = "example",
                description = "An example of Karg's usage.") {

    // Parameter/field values can be non-null and val (not var) since they are initialized at construction
    val textToPrint = parameter(
            name = "text-to-print",
            aliasNames = listOf("text"),
            shortNames= listOf('t'),
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
```

--help

    example
    An example of Karg's usage.

      --text-to-print | --text | -t <value>
        Print this text.
      [--text-to-print-after] <value>
        If provided, print this text after the primary text.
      [--shout]
        Print in all uppercase.

      <more-text>...

--text Hello --shout more text

    HELLO MORE TEXT


Features
==============
- Compile-time type safety
- Arguments values can be non-null and val (not var) since they are initialized at construction
- Argument values are forced to be nullable when the argument is optional (and no default is provided)
- Parameters
- Flags
- Positional parameters
- Help text generation
- Structured runtime inspection

Known Missing features
==============
- Parameter types other than String (currently, you have to convert Strings to numbers yourself)
- Help information and requirements for positional parameters
- Sub-commands (as in `git pull` vs `git checkout`)
- Override of default --help argument
- Custom terminating arguments other than --help (e.g. implement --version and not require non-optional arguments)
