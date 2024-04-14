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
import circt.stage.ChiselStage

import hwdbg.configs._
import hwdbg.interpreter._

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
    val rdWrAddr = Output(UInt(bramAddrWidth.W)) // read/write address
    val rdData = Input(UInt(bramDataWidth.W)) // read data
    val wrEna = Output(Bool()) // enable writing
    val wrData = Output(UInt(bramDataWidth.W)) // write data

  })

  //
  // Instantiate the remote debugger packet interpreter module
  //
  val (
    rdWrAddr,
    interpretationDone,
    foundValidPacket,
    requestedActionOfThePacket
  ) =
    DebuggerPacketInterpreter(
      debug,
      bramAddrWidth,
      bramDataWidth
    )(
      io.en,
      io.plInSignal,
      io.rdData
    )

  //
  // Configure the output signals
  //
  for (i <- 0 until numberOfOutputPins) {
    io.outputPin(i) := requestedActionOfThePacket(i)
  }

  io.rdWrAddr := rdWrAddr
  io.wrEna := false.B
  io.wrData := 0.U
  io.psOutInterrupt := false.B // For now, just assert false

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
      plInSignal: Bool,
      rdData: UInt
  ): (Vec[UInt], Bool, UInt, Bool, UInt) = {

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
    val rdWrAddr = Wire(UInt(bramAddrWidth.W))
    val wrEna = Wire(Bool())
    val wrData = Wire(UInt(bramDataWidth.W))

    //
    // Configure the input signals
    //
    debuggerMainModule.io.en := en
    debuggerMainModule.io.inputPin := inputPin
    debuggerMainModule.io.plInSignal := plInSignal
    debuggerMainModule.io.rdData := rdData

    //
    // Configure the output signals
    //
    outputPin := debuggerMainModule.io.outputPin
    psOutInterrupt := debuggerMainModule.io.psOutInterrupt
    rdWrAddr := debuggerMainModule.io.rdWrAddr
    wrEna := debuggerMainModule.io.wrEna
    wrData := debuggerMainModule.io.wrData

    //
    // Return the output result
    //
    (outputPin, psOutInterrupt, rdWrAddr, wrEna, wrData)
  }
}
