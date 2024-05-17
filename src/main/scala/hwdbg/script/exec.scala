/**
 * @file
 *   exec.scala
 * @author
 *   Sina Karvandi (sina@hyperdbg.org)
 * @brief
 *   Script execution engine
 * @details
 * @version 0.1
 * @date
 *   2024-05-07
 *
 * @copyright
 *   This project is released under the GNU Public License v3.
 */
package hwdbg.script

import chisel3._
import chisel3.util._

import hwdbg.configs._
import hwdbg.stage._

class ScriptExecutionEngine(
    debug: Boolean = DebuggerConfigurations.ENABLE_DEBUG,
    numberOfPins: Int = DebuggerConfigurations.NUMBER_OF_PINS,
    maximumNumberOfStages: Int = DebuggerConfigurations.MAXIMUM_NUMBER_OF_STAGES,
    bramAddrWidth: Int = DebuggerConfigurations.BLOCK_RAM_ADDR_WIDTH,
    bramDataWidth: Int = DebuggerConfigurations.BLOCK_RAM_DATA_WIDTH,
    portsConfiguration: Map[Int, Int] = DebuggerPorts.PORT_PINS_MAP
) extends Module {

  val io = IO(new Bundle {

    //
    // Chip signals
    //
    val en = Input(Bool()) // chip enable signal

    //
    // Input/Output signals
    //
    val inputPin = Input(Vec(numberOfPins, UInt((1.W)))) // input pins
    val outputPin = Output(Vec(numberOfPins, UInt((1.W)))) // output pins
  })

  //
  // Output pins
  //
  val outputPin = Wire(Vec(numberOfPins, UInt((1.W))))

  //
  // Stage registers
  //
  val stageRegs = Reg(new StageRegisters(debug, numberOfPins, maximumNumberOfStages))

  // -----------------------------------------------------------------------
  //
  // *** Move each register (input vector) to the next stage at each clock ***
  //
  for (i <- 0 until maximumNumberOfStages) {

    if (i == 0) {

      //
      // At the first stage, the input registers should be passed to the
      // first registers set of the stage registers
      //
      stageRegs.pinValues(i) := io.inputPin.asUInt

      //
      // Each pin start initially start from 0th target stage
      //
      // stageRegs.targetStage(i) := 0.U

    } else if (i == (maximumNumberOfStages - 1)) {

      //
      // At the last stage, the state registers should be passed to the output
      // Note: At this stage script symbol is useless
      //
      for (j <- 0 until numberOfPins) {
        outputPin(j) := stageRegs.pinValues(i)(j)
      }

    } else {

      //
      // At the normal (middle) stage, the state registers should be passed to
      // the next level of stage registers
      //
      stageRegs.pinValues(i + 1) := stageRegs.pinValues(i)

      //
      // Pass the target stage symbol number to the next stage
      //
      // stageRegs.targetStage(i + 1) := stageRegs.targetStage(i) // Uncomment

    }
  }

  // -----------------------------------------------------------------------

  //
  // Connect the output signals
  //
  io.outputPin := outputPin

}

object ScriptExecutionEngine {

  def apply(
      debug: Boolean = DebuggerConfigurations.ENABLE_DEBUG,
      numberOfPins: Int = DebuggerConfigurations.NUMBER_OF_PINS,
      maximumNumberOfStages: Int = DebuggerConfigurations.MAXIMUM_NUMBER_OF_STAGES,
      bramAddrWidth: Int = DebuggerConfigurations.BLOCK_RAM_ADDR_WIDTH,
      bramDataWidth: Int = DebuggerConfigurations.BLOCK_RAM_DATA_WIDTH,
      portsConfiguration: Map[Int, Int] = DebuggerPorts.PORT_PINS_MAP
  )(
      en: Bool,
      inputPin: Vec[UInt]
  ): (Vec[UInt]) = {

    val scriptExecutionEngineModule = Module(
      new ScriptExecutionEngine(
        debug,
        numberOfPins,
        maximumNumberOfStages,
        bramAddrWidth,
        bramDataWidth,
        portsConfiguration
      )
    )

    val outputPin = Wire(Vec(numberOfPins, UInt((1.W))))

    //
    // Configure the input signals
    //
    scriptExecutionEngineModule.io.en := en
    scriptExecutionEngineModule.io.inputPin := inputPin

    //
    // Configure the output signals
    //
    outputPin := scriptExecutionEngineModule.io.outputPin

    //
    // Return the output result
    //
    (outputPin)
  }
}
