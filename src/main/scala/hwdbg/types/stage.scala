/**
 * @file
 *   stage.scala
 * @author
 *   Sina Karvandi (sina@hyperdbg.org)
 * @brief
 *   Data types related to stage registers
 * @details
 * @version 0.1
 * @date
 *   2024-05-07
 *
 * @copyright
 *   This project is released under the GNU Public License v3.
 */
package hwdbg.stage

import chisel3._

import hwdbg.configs._

class StageRegisters(
    debug: Boolean = DebuggerConfigurations.ENABLE_DEBUG,
    numberOfPins: Int = DebuggerConfigurations.NUMBER_OF_PINS,
    maximumNumberOfStages: Int = DebuggerConfigurations.MAXIMUM_NUMBER_OF_STAGES
) extends Bundle {
  val pinValues = Vec(maximumNumberOfStages, UInt(numberOfPins.W))
}
