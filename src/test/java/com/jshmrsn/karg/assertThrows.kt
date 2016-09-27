package com.jshmrsn.karg

internal inline fun <reified T : Throwable> assertThrows(logic: () -> Unit) {
   var didThrow = false

   try {
      logic()
   } catch(thrown: Throwable) {
      if (thrown is T) {
         didThrow = true
      } else {
         throw thrown
      }
   }

   if (!didThrow) {
      throw Exception("Did not throw")
   }
}