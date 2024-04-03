/** @file
  *   configs.scala
  * @author
  *   Sina Karvandi (sina@hyperdbg.org)
  * @brief
  *   Configuration files
  * @details
  * @version 0.1
  * @date
  *   2024-04-03
  *
  * @copyright
  *   This project is released under the GNU Public License v3.
  */
package hwdbg.configs

import chisel3._
import chisel3.util._

/** @brief
  *   The constants for min-max tree
  */
object DebuggerConfigurations {

  //
  // whether to enable debug or not
  //
  val ENABLE_DEBUG: Boolean = false

  //
  // Number of input pins
  //
  val NUMBER_OF_INPUT_PINS: Int = 16

  //
  // Number of output pins
  //
  val NUMBER_OF_OUTPUT_PINS: Int = 16

  //
  // Address width of the Block RAM (BRAM)
  //
  val BLOCK_RAM_ADDR_WIDTH: Int = 13

  //
  // Data width of the Block RAM (BRAM)
  //
  val BLOCK_RAM_DATA_WIDTH: Int = 32

}

/** @brief
  *   The constants for min-max tree
  */
object GeneralConfigurations {

  //
  // Default number of bytes used in initialized SRAM memory
  //
  val DEFAULT_CONFIGURATION_INITIALIZED_MEMORY_SIZE: Int = 8192 // 8 KB
}
