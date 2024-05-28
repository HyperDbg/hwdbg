/**
 * @file
 *   global_stages.scala
 * @author
 *   Sina Karvandi (sina@hyperdbg.org)
 * @brief
 *   Data types related to stage registers
 * @details
 * @version 0.1
 * @date
 *   2024-05-17
 *
 * @copyright
 *   This project is released under the GNU Public License v3.
 */
package hwdbg.stage

import chisel3._

import hwdbg.configs._

object GlobalStages {

  //
  // Stage registers
  //
  val stageRegs = Reg(
    Vec(
      ScriptEngineConfigurations.MAXIMUM_NUMBER_OF_STAGES,
      new StageRegisters(
        DebuggerConfigurations.ENABLE_DEBUG,
        DebuggerConfigurations.NUMBER_OF_PINS,
        ScriptEngineConfigurations.MAXIMUM_NUMBER_OF_STAGES
      )
    )
  )

}
