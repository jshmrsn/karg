package com.jshmrsn.karg

import com.jshmrsn.karg.internal.ArgumentsParser

interface RawArguments

abstract class Arguments(rawArguments: RawArguments,
                         val name: String = "",
                         val description: String = "",
                         positionalUntilSeparator: Boolean = false) {
   private val parser = ArgumentsParser(rawArguments, positionalUntilSeparator)

   private var inspectionPositionalArguments: InspectionPositionalArguments? = null

   private fun validatePositionalArgumentsNotParsed() {
      if (this.inspectionPositionalArguments != null) {
         throw InvalidArgumentDefinitionException("Additional arguments cannot be defined after positional arguments are defined. Please place your positional arguments property initialization below all other argument definitions.")
      }
   }

   protected fun positionalArguments(name: String = "", description: String = "", minCount: Int? = null, maxCount: Int? = null): List<String> {
      if (this.inspectionPositionalArguments != null)
         throw InvalidArgumentDefinitionException("Positional arguments must not be configured more than once")

      this.inspectionPositionalArguments = InspectionPositionalArguments(name, description, minCount, maxCount)

      if (this.parser.isForInspection) {
         return listOf()
      }

      val positionalArguments = this.parser.parsePositionalArguments()

      val positionalArgumentsCount = positionalArguments.size

      if (maxCount != null) {
         if (positionalArgumentsCount > maxCount)
            throw Exception("Too many positional arguments provided. Provided $positionalArgumentsCount, expected no more than ${maxCount}")
      }

      if (minCount != null) {
         if (positionalArgumentsCount < minCount)
            throw Exception("Too few positional arguments provided. Provided $positionalArgumentsCount, expected no fewer than ${maxCount}")
      }

      return positionalArguments
   }

   private val inspectionArguments = mutableListOf<InspectionArgument>()


   protected fun multiParameter(name: String, aliasNames: List<String> = listOf(), shortNames: List<Char> = listOf(), description: String = ""): List<String> {
      validatePositionalArgumentsNotParsed()

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
      validatePositionalArgumentsNotParsed()

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
      validatePositionalArgumentsNotParsed()

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
      validatePositionalArgumentsNotParsed()

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
      validatePositionalArgumentsNotParsed()

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
      validatePositionalArgumentsNotParsed()

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
      validatePositionalArgumentsNotParsed()

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
           arguments = this.inspectionArguments.toList(),
           positionalArguments = this.inspectionPositionalArguments
      )
   }

   open fun validate() {
   }
}


