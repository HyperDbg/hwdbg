package hwdbg.configs

import chisel3._
import chisel3.util._


/** @brief
  *   The constants for min-max tree
  */
object DebuggerConfigurations {

  val ENABLE_DEBUG: Boolean = false // whether to enable debug or not

  val NUMBER_OF_INPUT_PINS: Int = 16 // Number of input pins

  val NUMBER_OF_OUTPUT_PINS: Int = 16 // Number of output pins

  val BLOCK_RAM_ADDR_WIDTH: Int = 13 // Address width of the Block RAM (BRAM)

  val BLOCK_RAM_DATA_WIDTH: Int = 32 // Data width of the Block RAM (BRAM)

}