/** @file
  *   main.scala
  * @author
  *   Sina Karvandi (sina@hyperdbg.org)
  * @brief
  *   hwdbg's main debugger module
  * @details
  * @version 0.1
  * @date
  *   2024-04-04
  *
  * @copyright
  *   This project is released under the GNU Public License v3.
  */
package hwdbg

import chisel3._
import chisel3.util.Counter
import circt.stage.ChiselStage

import hwdbg.configs._

class DebuggerMain(
    debug: Boolean = DebuggerConfigurations.ENABLE_DEBUG,
    numberOfInputPins: Int = DebuggerConfigurations.NUMBER_OF_INPUT_PINS,
    numberOfOutputPins: Int = DebuggerConfigurations.NUMBER_OF_OUTPUT_PINS,
    bramAddrWidth: Int = DebuggerConfigurations.BLOCK_RAM_ADDR_WIDTH,
    bramDataWidth: Int = DebuggerConfigurations.BLOCK_RAM_DATA_WIDTH
) extends Module {
  val io = IO(new Bundle {

    //
    // Chip signals
    //
    val en = Input(Bool()) // chip enable signal

    //
    // Input/Output signals
    //
    val inputPin = Input(Vec(numberOfInputPins, UInt((1.W)))) // input pins
    val outputPin = Output(Vec(numberOfOutputPins, UInt((1.W)))) // output pins

    //
    // Interrupt signals (lines)
    //
    val plInSignal = Input(Bool()) // PS to PL signal
    val psOutInterrupt = Output(Bool()) // PL to PS interrupt

    //
    // BRAM (Block RAM) ports
    //
    val rdAddr = Input(UInt(bramAddrWidth.W)) // read address
    val rdData = Output(UInt(bramDataWidth.W)) // read data
    val wrAddr = Input(UInt(bramAddrWidth.W)) // write address
    val wrEna = Input(Bool()) // enable writing
    val wrData = Input(UInt(bramDataWidth.W)) // write data

  })

  io.outputPin := io.inputPin

}

object DebuggerMain {

  def apply(
      debug: Boolean = DebuggerConfigurations.ENABLE_DEBUG,
      numberOfInputPins: Int = DebuggerConfigurations.NUMBER_OF_INPUT_PINS,
      numberOfOutputPins: Int = DebuggerConfigurations.NUMBER_OF_OUTPUT_PINS,
      bramAddrWidth: Int = DebuggerConfigurations.BLOCK_RAM_ADDR_WIDTH,
      bramDataWidth: Int = DebuggerConfigurations.BLOCK_RAM_DATA_WIDTH
  )(
      en: Bool,
      inputPin: Vec[UInt],
      psOutInterrupt: Bool,
      rdAddr: UInt,
      wrAddr: UInt,
      wrEna: Bool,
      wrData: UInt
  ): (Vec[UInt], Bool, UInt) = {

    val debuggerMainModule = Module(
      new DebuggerMain(
        debug,
        numberOfInputPins,
        numberOfOutputPins,
        bramAddrWidth,
        bramDataWidth
      )
    )

    val outputPin = Wire(Vec(numberOfOutputPins, UInt((1.W))))
    val psOutInterrupt = Wire(Bool())
    val rdData = Wire(UInt(bramDataWidth.W))

    //
    // Configure the input signals
    //
    debuggerMainModule.io.en := en
    debuggerMainModule.io.inputPin := inputPin
    debuggerMainModule.io.rdAddr := rdAddr
    debuggerMainModule.io.wrAddr := wrAddr
    debuggerMainModule.io.wrEna := wrEna
    debuggerMainModule.io.wrData := wrData

    //
    // Configure the input signals
    //
    outputPin := debuggerMainModule.io.outputPin
    psOutInterrupt := debuggerMainModule.io.psOutInterrupt
    rdData := debuggerMainModule.io.rdData

    //
    // Return the output result
    //
    (outputPin, psOutInterrupt, rdData)
  }
}
