package com.jshmrsn.karg

import com.jshmrsn.karg.internal.RawArguments
import org.junit.Assert

private class DemoArguments(rawArguments: RawArguments) :
        Arguments(rawArguments,
                name = "demo",
                description = "A demo of Karg's usage.") {
    val concise = parameter("concise", description = "A simple parameter without any bells or whistles.")

    val parameter: String = parameter(
            name = "param",
            aliasNames = listOf("param-alias"),
            shortNames = listOf('p', 'a'),
            description = "A required parameter. Since this parameter is not optional, the Kotlin property type can be non-nullable. An exception is thrown if this parameter is not provided."
    )

    val optionalParameter: String? = optionalParameter(
            name = "optional-param",
            shortNames = listOf('o'),
            description = "An optional parameter. Since this parameter is optional and no default value is provided, the Kotlin property type must be nullable. If this parameter is not provided, the property value will be null."
    )

    val optionalParameterWithDefault: String = optionalParameter(
            name = "optional-param-with-default",
            shortNames = listOf('d'),
            description = "An optional parameter with a default value. Although this property is optional, it has a default value, so the Kotlin property type can be non-nullable.",
            default = "default value"
    )

    val multiParameter: List<String> = multiParameter(
            name = "multi-parameter",
            shortNames = listOf('m'),
            description = "A parameter which can be provided multiple times, building a list of values.")

    val flag: Boolean = flag(
            name = "flag",
            aliasNames = listOf("flag-alias"),
            shortNames = listOf('f', 'l'),
            description = "A required flag"
    )

    val optionalFlag: Boolean? = optionalFlag(
            name = "optional-flag",
            shortNames = listOf('o'),
            description = "An optional flag."
    )

    val optionalFlagWithDefault: Boolean = optionalFlag(
            name = "optional-param-with-default",
            description = "An optional flag with a default value",
            default = false
    )
}

class Demo {
    @org.junit.Test
    fun demo() {
        run {
            val argumentsArray = arrayOf(
                    "--concise", "value",
                    "first positional value", "second positional value",
                    "--param-alias", "value for param via alias",
                    "--optional-param", "value for optional param",
                    "-d", "value for optional param with default via short name",
                    "--flag",
                    "--no-optional-flag",
                    "--multi-parameter", "a", "-m", "b", "-m", "c",
                    "third positional value"

            )

            val arguments = parseArguments(argumentsArray, { DemoArguments(it) })

            Assert.assertEquals("value", arguments.concise)

            Assert.assertEquals("value for param via alias", arguments.parameter)
            Assert.assertEquals("value for optional param", arguments.optionalParameter)
            Assert.assertEquals("value for optional param with default via short name", arguments.optionalParameterWithDefault)

            Assert.assertEquals(true, arguments.flag)
            Assert.assertEquals(false, arguments.optionalFlag)
            Assert.assertEquals(false, arguments.optionalFlagWithDefault)

            Assert.assertEquals(3, arguments.positionalArguments.size)
            Assert.assertEquals("first positional value", arguments.positionalArguments[0])
            Assert.assertEquals("second positional value", arguments.positionalArguments[1])
            Assert.assertEquals("third positional value", arguments.positionalArguments[2])

            Assert.assertEquals(3, arguments.multiParameter.size)
            Assert.assertEquals("a", arguments.multiParameter[0])
            Assert.assertEquals("b", arguments.multiParameter[1])
            Assert.assertEquals("c", arguments.multiParameter[2])
        }
    }

    @org.junit.Test
    fun demoHelp() {
        run {
            val argumentsArray = arrayOf("--help")

            var didPrintHelp = false

            val expectedHelpText = """demo
A demo of Karg's usage.

  --concise <value>
    A simple parameter without any bells or whistles.
  --param | --param-alias | -p | -a <value>
    A required parameter. Since this parameter is not optional, the Kotlin property type can be non-nullable. An exception is thrown if this parameter is not provided.
  [--optional-param | -o] <value>
    An optional parameter. Since this parameter is optional and no default value is provided, the Kotlin property type must be nullable. If this parameter is not provided, the property value will be null.
  [--optional-param-with-default | -d] <value>
    An optional parameter with a default value. Although this property is optional, it has a default value, so the Kotlin property type can be non-nullable.
  --multi-parameter | -m <value> (repeat parameter for multiple values)
    A parameter which can be provided multiple times, building a list of values.
  --flag | --flag-alias | -f | -l
    A required flag
  [--optional-flag | -o]
    An optional flag.
  [--optional-param-with-default]
    An optional flag with a default value
"""

            parseArguments(argumentsArray, { DemoArguments(it) }, printHelpCallback = { generatedHelpText ->
                // The printHelpCallback argument can be omitted and will default to printing to standard out, and then
                // exiting the process with status 0. For this demo/test, just validate that this callback is called
                // when --help is provided
                didPrintHelp = true

                Assert.assertEquals(generatedHelpText, expectedHelpText)
            })

            Assert.assertTrue(didPrintHelp)
        }
    }

    @org.junit.Test
    fun demoInspection() {
        run {
            // inspectArguments returns an instance of InspectionArguments, which is a structured representation
            // of the argument's command line API. Internally, InspectionArguments is used to generate help text
            val inspectionArguments = inspectArguments { DemoArguments(it) }

            Assert.assertEquals(8, inspectionArguments.arguments.size)

            Assert.assertEquals("demo", inspectionArguments.name)
            Assert.assertEquals("A demo of Karg's usage.", inspectionArguments.description)

            val firstInspectionArgument = inspectionArguments.arguments.first()
            Assert.assertEquals("concise", firstInspectionArgument.name)
            Assert.assertEquals(InspectionArgumentType.Parameter, firstInspectionArgument.type)
            Assert.assertEquals(false, firstInspectionArgument.isOptional)
        }
    }
}
