/**
 * @file
 *   eval.scala
 * @author
 *   Sina Karvandi (sina@hyperdbg.org)
 * @brief
 *   Script execution engine
 * @details
 * @version 0.1
 * @date
 *   2024-05-17
 *
 * @copyright
 *   This project is released under the GNU Public License v3.
 */
package hwdbg.script

import chisel3._
import chisel3.util._

import hwdbg.configs._
import hwdbg.utils._
import hwdbg.stage._

object ScriptEvalFunc {
  object ScriptOperators extends ChiselEnum {
    val sFuncInc, sFuncDec, sFuncDec2, sFuncDec3, sFuncDec4, sFuncDec5, sFuncDec6, sFuncDec7 = Value
  }
}

class ScriptEngineEval(
    debug: Boolean = DebuggerConfigurations.ENABLE_DEBUG,
    numberOfPins: Int = DebuggerConfigurations.NUMBER_OF_PINS,
    maximumNumberOfStages: Int = ScriptEngineConfigurations.MAXIMUM_NUMBER_OF_STAGES,
    portsConfiguration: Map[Int, Int] = DebuggerPorts.PORT_PINS_MAP
) extends Module {

  //
  // Import state enum
  //
  import ScriptEvalFunc.ScriptOperators
  import ScriptEvalFunc.ScriptOperators._

  val io = IO(new Bundle {

    //
    // Chip signals
    //
    val en = Input(Bool()) // chip enable signal

    //
    // Evaluation operator symbol
    //
    val operator = Input(new SYMBOL)

    val currentStage = Input(UInt(log2Ceil(maximumNumberOfStages).W))
    val nextStage = Output(UInt(log2Ceil(maximumNumberOfStages).W))

    //
    // Input/Output signals
    //
    val inputPin = Input(Vec(numberOfPins, UInt(1.W))) // input pins
    val outputPin = Output(Vec(numberOfPins, UInt(1.W))) // output pins
  })

  //
  // Output pins
  //
  // val outputPin = Wire(Vec(numberOfPins, UInt(1.W)))
  val nextStage = WireInit(0.U(log2Ceil(maximumNumberOfStages).W))

  //
  // Assign operator value (split the signal into only usable part)
  //
  LogInfo(debug)("Usable size of Value in the SYMBOL: " + ScriptOperators().getWidth)
  val operatorValue = io.operator.Value(ScriptOperators().getWidth - 1, 0).asTypeOf(ScriptOperators())

  //
  // *** Implementing the evaluation engine ***
  //

  //
  // Apply the chip enable signal
  //
  when(io.en === true.B) {

    switch(operatorValue) {

      is(sFuncInc) {
        nextStage := io.currentStage + 1.U
      }
      is(sFuncDec) {
        nextStage := io.currentStage + 2.U
      }
    }
  }

  // ---------------------------------------------------------------------

  //
  // Increment the stage
  //
  // nextStage := io.currentStage + 1.U

  //
  // Connect the output signals
  //
  io.nextStage := nextStage
  // io.outputPin := outputPin
  io.outputPin := io.inputPin

}

object ScriptEngineEval {

  def apply(
      debug: Boolean = DebuggerConfigurations.ENABLE_DEBUG,
      numberOfPins: Int = DebuggerConfigurations.NUMBER_OF_PINS,
      maximumNumberOfStages: Int = ScriptEngineConfigurations.MAXIMUM_NUMBER_OF_STAGES,
      portsConfiguration: Map[Int, Int] = DebuggerPorts.PORT_PINS_MAP
  )(
      en: Bool,
      operator: SYMBOL,
      currentStage: UInt,
      inputPin: Vec[UInt]
  ): (UInt, Vec[UInt]) = {

    val scriptEngineEvalModule = Module(
      new ScriptEngineEval(
        debug,
        numberOfPins,
        maximumNumberOfStages,
        portsConfiguration
      )
    )

    val outputPin = Wire(Vec(numberOfPins, UInt(1.W)))
    val nextStage = Wire(UInt(log2Ceil(maximumNumberOfStages).W))

    //
    // Configure the input signals
    //
    scriptEngineEvalModule.io.en := en
    scriptEngineEvalModule.io.operator := operator
    scriptEngineEvalModule.io.currentStage := currentStage
    scriptEngineEvalModule.io.inputPin := inputPin

    //
    // Configure the output signals
    //
    nextStage := scriptEngineEvalModule.io.nextStage
    outputPin := scriptEngineEvalModule.io.outputPin

    //
    // Return the output result
    //
    (nextStage, outputPin)
  }
}
