package com.jshmrsn.karg

import com.jshmrsn.karg.internal.*

abstract class Arguments(rawArguments: RawArguments,
                         val name: String = "",
                         val description: String = "") {
    private val parser = ArgumentsParser(rawArguments)

    val positionalArguments: List<String> get() = this.parser.positionalArguments

    private val inspectionArguments = mutableListOf<InspectionArgument>()

    fun multiParameter(name: String, aliasNames: List<String> = listOf(), shortNames: List<Char> = listOf(), description: String = ""): List<String> {
        this.inspectionArguments.add(InspectionArgument(
                type = InspectionArgumentType.MultiParameter,
                name = name,
                aliasNames = aliasNames,
                shortNames = shortNames,
                isOptional = false,
                description = description
        ))

        if (this.parser.isForInspection)
            return listOf()

        val names = arrayListOf(name)
        names.addAll(aliasNames)

        return this.parser.parseMultiParameter(name = name, namesToCheck = names, shortNamesToCheck = shortNames)
    }

    protected fun parameter(name: String, aliasNames: List<String> = listOf(), shortNames: List<Char> = listOf(), description: String = ""): String {
        this.inspectionArguments.add(InspectionArgument(
                type = InspectionArgumentType.Parameter,
                name = name,
                aliasNames = aliasNames,
                shortNames = shortNames,
                isOptional = false,
                description = description
        ))

        if (this.parser.isForInspection)
            return ""

        val namesToCheck = arrayListOf(name)
        namesToCheck.addAll(aliasNames)

        val found = this.parser.parseParameter(name = name, namesToCheck = namesToCheck, shortNamesToCheck = shortNames)

        if (found == null)
            throw InvalidArgumentsException("Missing required parameter: " + name)
        else
            return found
    }

    protected fun optionalParameter(name: String, aliasNames: List<String> = listOf(), shortNames: List<Char> = listOf(), description: String = ""): String? {
        this.inspectionArguments.add(InspectionArgument(
                type = InspectionArgumentType.Parameter,
                name = name,
                aliasNames = aliasNames,
                shortNames = shortNames,
                isOptional = true,
                description = description
        ))

        if (this.parser.isForInspection)
            return null

        val namesToCheck = arrayListOf(name)
        namesToCheck.addAll(aliasNames)

        return this.parser.parseParameter(name = name, namesToCheck = namesToCheck, shortNamesToCheck = shortNames)
    }

    protected fun optionalParameter(name: String, aliasNames: List<String> = listOf(), shortNames: List<Char> = listOf(), description: String = "", default: String): String {
        this.inspectionArguments.add(InspectionArgument(
                type = InspectionArgumentType.Parameter,
                name = name,
                aliasNames = aliasNames,
                shortNames = shortNames,
                isOptional = true,
                description = description,
                defaultValue = default
        ))

        if (this.parser.isForInspection)
            return ""

        val namesToCheck = arrayListOf(name)
        namesToCheck.addAll(aliasNames)

        return this.parser.parseParameter(name = name, namesToCheck = namesToCheck, shortNamesToCheck = shortNames) ?: default
    }


    protected fun flag(name: String, aliasNames: List<String> = listOf(), shortNames: List<Char> = listOf(), description: String = ""): Boolean {
        this.inspectionArguments.add(InspectionArgument(
                type = InspectionArgumentType.Flag,
                name = name,
                aliasNames = aliasNames,
                shortNames = shortNames,
                isOptional = false,
                description = description
        ))

        if (this.parser.isForInspection)
            return false

        val namesToCheck = arrayListOf(name)
        namesToCheck.addAll(aliasNames)

        val found = this.parser.parseFlag(name = name, namesToCheck = namesToCheck, shortNamesToCheck = shortNames)

        if (found == null)
            throw InvalidArgumentsException("Missing required flag: " + name)
        else
            return found
    }

    protected fun optionalFlag(name: String, aliasNames: List<String> = listOf(), shortNames: List<Char> = listOf(), description: String = ""): Boolean? {
        this.inspectionArguments.add(InspectionArgument(
                type = InspectionArgumentType.Flag,
                name = name,
                aliasNames = aliasNames,
                shortNames = shortNames,
                isOptional = true,
                description = description
        ))

        if (this.parser.isForInspection)
            return false

        val namesToCheck = arrayListOf(name)
        namesToCheck.addAll(aliasNames)

        return this.parser.parseFlag(name = name, namesToCheck = namesToCheck, shortNamesToCheck = shortNames)
    }

    protected fun optionalFlag(name: String, aliasNames: List<String> = listOf(), shortNames: List<Char> = listOf(), description: String = "", default: Boolean): Boolean {
        this.inspectionArguments.add(InspectionArgument(
                type = InspectionArgumentType.Flag,
                name = name,
                aliasNames = aliasNames,
                shortNames = shortNames,
                isOptional = true,
                description = description,
                defaultValue = default.toString()
        ))

        if (this.parser.isForInspection)
            return false

        val namesToCheck = arrayListOf(name)
        namesToCheck.addAll(aliasNames)

        return this.parser.parseFlag(name = name, namesToCheck = namesToCheck, shortNamesToCheck = shortNames) ?: default
    }

    internal fun finalize(printHelpCallback: (String) -> Unit) {
        this.parser.finalize()

        if (this.parser.isForHelp) {
            var helpString = this.generateInspection().generateHelpText()
            printHelpCallback(helpString)
        } else if (!this.parser.isForInspection) {
            this.validate()
        }
    }

    internal fun generateInspection(): InspectionArguments {
        return InspectionArguments(
                name = this.name,
                description = this.description,
                arguments = this.inspectionArguments.toList()
        )
    }

    open fun validate() {
    }
}

