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
  *   The configuration of ports and pins
  */
object DebuggerPorts {

  //
  // The following constant shows the key value object of the mappings
  // of pins to ports (used for inputs)
  // For example,
  //                port 0 (in) -> contains 12 pins
  //                port 1 (in) -> contains 9 pins
  //                port 2 (in) -> contains 11 pins
  //
  val PORT_PINS_MAP_INPUT: Map[Int, Int] = Map(0 -> 12, 1 -> 9, 2 -> 11)

  //
  // The following constant shows the key value object of the mappings
  // of pins to ports (used for outputs)
  // For example,
  //                port 0 (out) -> contains 12 pins
  //                port 1 (out) -> contains 9 pins
  //                port 2 (out) -> contains 11 pins
  //
  val PORT_PINS_MAP_OUTPUT: Map[Int, Int] =
    Map(0 -> 4, 1 -> 7, 2 -> 11, 3 -> 10)

}

/** @brief
  *   Design constants
  */
object DebuggerConfigurations {

  //
  // whether to enable debug or not
  //
  val ENABLE_DEBUG: Boolean = false

  //
  // Number of input pins
  //
  val NUMBER_OF_INPUT_PINS: Int = 32

  //
  // Number of output pins
  //
  val NUMBER_OF_OUTPUT_PINS: Int = 32

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
  *   The constants for configuration
  */
object GeneralConfigurations {

  //
  // Default number of bytes used in initialized SRAM memory
  //
  val DEFAULT_CONFIGURATION_INITIALIZED_MEMORY_SIZE: Int = 8192 // 8 KB
}
