package com.jshmrsn.karg

import com.jshmrsn.karg.RawArguments
import com.jshmrsn.karg.internal.RawArgumentsForInspectionImplementation

fun <T: Arguments> inspectArguments(builder: (rawArguments: RawArguments) -> T): InspectionArguments {
    val parsedArguments = RawArgumentsForInspectionImplementation()
    val result = builder(parsedArguments)
    return result.generateInspection()
}
