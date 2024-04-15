/** @file
  *   configs.scala
  * @author
  *   Sina Karvandi (sina@hyperdbg.org)
  * @brief
  *   Configuration files for testing hwdbg
  * @details
  * @version 0.1
  * @date
  *   2024-04-15
  *
  * @copyright
  *   This project is released under the GNU Public License v3.
  */
package hwdbg.configs

import chisel3._
import chisel3.util._

/** @brief
  *   The configuration constants for testing
  */
object TestingConfigurations {

  val BRAM_INITIALIZATION_FILE_PATH: String =
    "./src/resources/8kb_BRAM.hex.txt"
}
