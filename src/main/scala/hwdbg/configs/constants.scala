/** @file
  *   constants.scala
  * @author
  *   Sina Karvandi (sina@hyperdbg.org)
  * @brief
  *   Constant values
  * @details
  * @version 0.1
  * @date
  *   2024-04-16
  *
  * @copyright
  *   This project is released under the GNU Public License v3.
  */
package hwdbg.constants

import chisel3._
import chisel3.util._

/** @brief
  *   Shared value with HyperDbg
  * @warning
  *   used in HyperDbg
  */
object HyperDbgSharedConstants {

  //
  // Constant indicator of a HyperDbg packet
  //
  val INDICATOR_OF_HYPERDBG_PACKET: Long =
    0x4859504552444247L // HYPERDBG = 0x4859504552444247

}
