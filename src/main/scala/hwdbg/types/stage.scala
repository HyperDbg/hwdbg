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
import chisel3.util.log2Ceil

import hwdbg.configs._

class StageRegisters(
    debug: Boolean = DebuggerConfigurations.ENABLE_DEBUG,
    numberOfPins: Int = DebuggerConfigurations.NUMBER_OF_PINS,
    maximumNumberOfStages: Int = DebuggerConfigurations.MAXIMUM_NUMBER_OF_STAGES,
    singleStageScriptSymbolSize: Int = DebuggerConfigurations.SINGLE_STAGE_SCRIPT_SYMBOL_SIZE
) extends Bundle {
  val pinValues = Vec(maximumNumberOfStages, UInt(numberOfPins.W)) // The value of each pin in each stage (should be passed to the next stage)
  val scriptSymbol = UInt(singleStageScriptSymbolSize.W) // Interpreted script symbol for the target stage (should NOT be passed to the next stage)
  val targetStage = UInt(
    log2Ceil(maximumNumberOfStages).W
  ) // Target stage that needs to be executed for the current pin values (should be passed to the next stage)
}
