package com.jshmrsn.karg.internal

import com.jshmrsn.karg.InvalidArgumentsException
import com.jshmrsn.karg.RawArguments


internal interface ArgumentToken

internal class ValueArgumentToken(val value: String) : ArgumentToken {
   var isClaimed = false
      private set

   fun claim(): String {
      this.isClaimed = true
      return value
   }
}

internal class ShortNameArgumentToken(val shortNames: List<Char>) : ArgumentToken {
   private val unclaimedShortNames = shortNames.toMutableList()
   val immutableUnclaimedShortNames: List<Char> get() = this.unclaimedShortNames.toList()

   fun claim(shortNamesToCheck: List<Char>): Char? {
      shortNamesToCheck.forEach { shortNameToCheck ->
         if (this.unclaimedShortNames.remove(shortNameToCheck)) {
            return shortNameToCheck
         }
      }

      return null
   }
}

internal class NameArgumentToken(val name: String) : ArgumentToken {
   var isClaimed = false
      private set

   fun claim(namesToCheck: List<String>): Boolean {
      namesToCheck.forEach { nameToCheck ->
         if (this.name == nameToCheck) {
            this.isClaimed = true
            return true
         }
      }

      return false
   }
}

internal class RawArgumentsImplementation(val rawArguments: List<String>) : RawArguments
internal class RawArgumentsForInspectionImplementation() : RawArguments

internal class ArgumentsParser(rawArguments: RawArguments,
                               positionalUntilSeparator: Boolean = false) {
   val isForHelp: Boolean
   val isForInspection: Boolean

   private val argumentTokens: List<ArgumentToken>
   private val postSeparatorStrings: List<String>

   init {
      if (rawArguments is RawArgumentsImplementation) {
         val argumentTokens = mutableListOf<ArgumentToken>()
         val postSeparatorStrings = mutableListOf<String>()
         var hasReachedSeparator = false
         var defaultPositional = positionalUntilSeparator

         rawArguments.rawArguments.forEach { rawArgument ->
            if (rawArgument == "---") {
               defaultPositional = !defaultPositional
            } else if (hasReachedSeparator || defaultPositional) {
               postSeparatorStrings.add(rawArgument)
            } else if (rawArgument == "--") {
               hasReachedSeparator = true
            } else if (rawArgument.startsWith("--")) {
               val name = rawArgument.substring(2)
               argumentTokens.add(NameArgumentToken(name))
            } else if (rawArgument.startsWith("-")) {
               val shortNames = rawArgument.substring(1).toList()
               argumentTokens.add(ShortNameArgumentToken(shortNames))
            } else {
               argumentTokens.add(ValueArgumentToken(rawArgument))
            }
         }

         this.argumentTokens = argumentTokens
         this.postSeparatorStrings = postSeparatorStrings

         this.isForHelp = parseFlag("help", namesToCheck = listOf("help"), shortNamesToCheck = listOf('h')) == true
         this.isForInspection = this.isForHelp
      } else if (rawArguments is RawArgumentsForInspectionImplementation) {
         this.isForHelp = false
         this.isForInspection = true
         this.argumentTokens = listOf()
         this.postSeparatorStrings = listOf()
      } else {
         throw InvalidArgumentsException("Provided raw arguments should always be obtained through a call to parseArguments or inspectArguments")
      }
   }

   fun parseFlag(name: String, namesToCheck: List<String>, shortNamesToCheck: List<Char>): Boolean? {
      var result: Boolean? = null

      val inverseNamesToCheck = namesToCheck.map { "no-" + it }

      this.argumentTokens.forEach { token ->
         if (token is NameArgumentToken) {
            if (token.claim(inverseNamesToCheck)) {
               if (result != null)
                  throw InvalidArgumentsException("Flag provided multiple times: $name")

               result = false
            } else if (token.claim(namesToCheck)) {
               if (result != null)
                  throw InvalidArgumentsException("Flag provided multiple times: $name")

               result = true
            }
         } else if (token is ShortNameArgumentToken) {
            val claimedShortName = token.claim(shortNamesToCheck)

            if (claimedShortName != null) {
               if (result != null)
                  throw InvalidArgumentsException("Flag provided multiple times: $name")

               result = true
            }
         }
      }

      return result
   }

   fun parseMultiParameter(name: String, namesToCheck: List<String>, shortNamesToCheck: List<Char>): List<String> {
      var tokenIndex = 0

      val result = mutableListOf<String>()

      while (tokenIndex < this.argumentTokens.size) {
         val token = this.argumentTokens[tokenIndex]
         val nextToken = this.argumentTokens.getOrNull(tokenIndex + 1)

         if (token is NameArgumentToken) {
            if (token.claim(namesToCheck)) {
               if (nextToken is ValueArgumentToken) {
                  result.add(nextToken.claim())
               } else {
                  throw InvalidArgumentsException("Expected value for parameter $name (${token.name})")
               }
            }
         } else if (token is ShortNameArgumentToken) {
            val claimedShortName = token.claim(shortNamesToCheck)

            if (claimedShortName != null) {
               if (nextToken is ValueArgumentToken) {
                  result.add(nextToken.claim())
               } else {
                  throw InvalidArgumentsException("Expected value for parameter $name ($claimedShortName)")
               }
            }
         }

         tokenIndex++
      }

      return result
   }

   fun parseParameter(name: String, namesToCheck: List<String>, shortNamesToCheck: List<Char>): String? {
      val results = this.parseMultiParameter(name, namesToCheck, shortNamesToCheck)

      if (results.size > 1)
         throw InvalidArgumentsException("Multiple values provided for parameter: " + name)

      return results.firstOrNull()
   }

   fun parsePositionalArguments(): List<String> {
      val positionalArguments = arrayListOf<String>()

      this.argumentTokens.forEach { token ->
         if (token is ValueArgumentToken) {
            if (!token.isClaimed) {
               positionalArguments.add(token.claim())
            }
         }
      }

      return positionalArguments + this.postSeparatorStrings
   }

   fun finalize() {
      if (!this.isForInspection) {
         this.argumentTokens.forEach { token ->
            if (token is NameArgumentToken) {
               if (!token.isClaimed) {
                  throw InvalidArgumentsException("Unclaimed name argument: " + token.name)
               }
            } else if (token is ShortNameArgumentToken) {
               val unclaimedShortNames = token.immutableUnclaimedShortNames

               if (!unclaimedShortNames.isEmpty()) {
                  throw InvalidArgumentsException("Unclaimed short-named arguments: " + unclaimedShortNames.joinToString(", "))
               }
            } else if (token is ValueArgumentToken) {
               if (!token.isClaimed) {
                  throw InvalidArgumentsException("Unclaimed argument value: " + token.value)
               }
            }
         }
      }
   }
}
