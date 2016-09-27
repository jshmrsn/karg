package com.jshmrsn.karg

import org.junit.Assert.*

class Tests {
   @org.junit.Test
   fun parameter() {
      class TestArguments(rawArguments: RawArguments) : Arguments(rawArguments) {
         val parameter = parameter("param")
      }

      run {
         val argumentsArray = arrayOf("--param", "value")
         val arguments = parseArguments(argumentsArray, ::TestArguments)
         assertEquals("value", arguments.parameter)
      }

      assertThrows<InvalidArgumentsException> {
         val argumentsArray = arrayOf("--param")
         parseArguments(argumentsArray, ::TestArguments)
      }

      assertThrows<InvalidArgumentsException> {
         val argumentsArray = arrayOf<String>()
         parseArguments(argumentsArray, ::TestArguments)
      }

      assertThrows<InvalidArgumentsException> {
         val argumentsArray = arrayOf("--param", "value", "--param", "value again")
         parseArguments(argumentsArray, ::TestArguments)
      }
   }

   @org.junit.Test
   fun parameterWithAlias() {
      class TestArguments(rawArguments: RawArguments) : Arguments(rawArguments) {
         val parameter = parameter("param", aliasNames = listOf("alias", "other"))
      }

      run {
         val argumentsArray = arrayOf("--param", "value")
         val arguments = parseArguments(argumentsArray, ::TestArguments)
         assertEquals("value", arguments.parameter)
      }

      assertThrows<InvalidArgumentsException> {
         val argumentsArray = arrayOf("--param")
         parseArguments(argumentsArray, ::TestArguments)
      }

      assertThrows<InvalidArgumentsException> {
         val argumentsArray = arrayOf("--alias", "value", "--other", "value again")
         parseArguments(argumentsArray, ::TestArguments)
      }

      run {
         val argumentsArray = arrayOf("--alias", "alias value")
         val arguments = parseArguments(argumentsArray, ::TestArguments)
         assertEquals("alias value", arguments.parameter)
      }

      assertThrows<InvalidArgumentsException> {
         val argumentsArray = arrayOf("--alias")
         parseArguments(argumentsArray, ::TestArguments)
      }

      run {
         val argumentsArray = arrayOf("--other", "other alias value")
         val arguments = parseArguments(argumentsArray, ::TestArguments)
         assertEquals("other alias value", arguments.parameter)
      }

      assertThrows<InvalidArgumentsException> {
         val argumentsArray = arrayOf<String>()
         parseArguments(argumentsArray, ::TestArguments)
      }
   }

   @org.junit.Test
   fun flag() {
      class TestArguments(rawArguments: RawArguments) : Arguments(rawArguments) {
         val flag = flag("flag", aliasNames = listOf("alias"), shortNames = listOf('f'))
      }

      run {
         val argumentsArray = arrayOf("--flag")
         val arguments = parseArguments(argumentsArray, ::TestArguments)
         assertEquals(true, arguments.flag)
      }

      run {
         val argumentsArray = arrayOf("--no-flag")
         val arguments = parseArguments(argumentsArray, ::TestArguments)
         assertEquals(false, arguments.flag)
      }

      assertThrows<InvalidArgumentsException> {
         val argumentsArray = arrayOf<String>()
         parseArguments(argumentsArray, ::TestArguments)
      }

      assertThrows<InvalidArgumentsException> {
         val argumentsArray = arrayOf("--no-flag", "--no-flag")
         parseArguments(argumentsArray, ::TestArguments)
      }
   }

   @org.junit.Test
   fun optionalFlag() {
      class TestArguments(rawArguments: RawArguments) : Arguments(rawArguments) {
         val flag = optionalFlag("flag", aliasNames = listOf("alias"), shortNames = listOf('f'))
      }

      run {
         val argumentsArray = arrayOf("--flag")
         val arguments = parseArguments(argumentsArray, ::TestArguments)
         assertEquals(true, arguments.flag)
      }

      run {
         val argumentsArray = arrayOf("--no-flag")
         val arguments = parseArguments(argumentsArray, ::TestArguments)
         assertEquals(false, arguments.flag)
      }

      run {
         val argumentsArray = arrayOf<String>()
         val arguments = parseArguments(argumentsArray, ::TestArguments)
         assertEquals(null, arguments.flag)
      }

      assertThrows<InvalidArgumentsException> {
         val argumentsArray = arrayOf("--no-flag", "--no-flag")
         parseArguments(argumentsArray, ::TestArguments)
      }
   }
}
