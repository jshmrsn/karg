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

internal class ArgumentsParser(rawArguments: RawArguments) {
    val isForHelp: Boolean
    val isForInspection: Boolean

    private val argumentTokens: List<ArgumentToken>

    init {
        if (rawArguments is RawArgumentsImplementation) {
            this.argumentTokens = rawArguments.rawArguments.map { rawArgument ->
                if (rawArgument.startsWith("--")) {
                    val name = rawArgument.substring(2)
                    NameArgumentToken(name)
                } else if (rawArgument.startsWith("-")) {
                    val shortNames = rawArgument.substring(1).toList()
                    ShortNameArgumentToken(shortNames)
                } else {
                    ValueArgumentToken(rawArgument)
                }
            }

            this.isForHelp = parseFlag("help", namesToCheck = listOf("help"), shortNamesToCheck = listOf('h')) == true
            this.isForInspection = this.isForHelp
        } else if (rawArguments is RawArgumentsForInspectionImplementation) {
            this.isForHelp = false
            this.isForInspection = true
            this.argumentTokens = listOf()
        } else {
            throw InvalidArgumentsException("Provided raw arguments should always be obtained through a call to parseArguments or inspectArguments")
        }
    }

    lateinit var positionalArguments: List<String>

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

        var result = mutableListOf<String>()

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
        var results = this.parseMultiParameter(name, namesToCheck, shortNamesToCheck)

        if (results.size > 1)
            throw InvalidArgumentsException("Multiple values provided for parameter: " + name)

        return results.firstOrNull()
    }

    fun finalize() {
        if (this.isForInspection) {
            this.positionalArguments = listOf()
        } else {
            val positionalArguments = arrayListOf<String>()

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
                        positionalArguments.add(token.value)
                    }
                }
            }

            this.positionalArguments = positionalArguments
        }
    }
}
