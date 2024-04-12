/** @file
  *   utils.scala
  * @author
  *   Sina Karvandi (sina@hyperdbg.org)
  * @brief
  *   Different utilities and functionalities
  * @details
  * @version 0.1
  * @date
  *   2024-04-12
  *
  * @copyright
  *   This project is released under the GNU Public License v3.
  */
package hwdbg.utils

import chisel3._
import chisel3.util._

/** @brief
  *   Create logs and debug messages
  */
object LogInfo {

  def apply(debug: Boolean)(message: String): Unit = {
    println("[*] debug msg: " + message)
  }
}
