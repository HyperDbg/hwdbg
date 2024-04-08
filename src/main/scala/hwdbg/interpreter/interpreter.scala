/** @file
  *   interpreter.scala
  * @author
  *   Sina Karvandi (sina@hyperdbg.org)
  * @brief
  *   Remote debugger packet interpreter module
  * @details
  * @version 0.1
  * @date
  *   2024-04-08
  *
  * @copyright
  *   This project is released under the GNU Public License v3.
  */
package hwdbg.interpreter

import chisel3._
import circt.stage.ChiselStage

import hwdbg.configs._
import hwdbg.types._

class DebuggerPacketInterpreter(
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
    // Note: Only PL input signal is received here,
    // a separate module will control the PS signal
    //
    val plInSignal = Input(Bool()) // PS to PL signal

    //
    // BRAM (Block RAM) ports
    //
    val rdWrAddr = Output(UInt(bramAddrWidth.W)) // read/write address
    val rdData = Input(UInt(bramDataWidth.W)) // read data
    val wrEna = Output(Bool()) // enable writing
    val wrData = Output(UInt(bramDataWidth.W)) // write data

    //
    // Interpretation signals
    //
    val interpretationDone = Output(Bool()) // interpretation done or not?
    val foundValidPacket = Output(Bool()) // packet was valid or not
    val requestedActionOfThePacket = Output(UInt(32.W)) // the requested action

  })

  /*
  val receivedPacketBuffer = Wire(new DebuggerRemotePacket())
  receivedPacketBuffer.key := io.key
  receivedPacketBuffer.value := io.value
  io.struct_key := ms.key
  io.struct_value := ms.value
   */

  // ------------------------------------------------------------------------------------------------------------

  //
  // Used for testing verilog generation, should be removed
  //
  for (i <- 0 until numberOfOutputPins) {
    io.outputPin(i) := 0.U
  }

  io.rdWrAddr := 0.U
  io.wrEna := false.B
  io.wrData := 0.U
  io.interpretationDone := false.B
  io.foundValidPacket := false.B
  io.requestedActionOfThePacket := 0.U

  // ------------------------------------------------------------------------------------------------------------

}

object DebuggerPacketInterpreter {

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
  ): (Vec[UInt], UInt, Bool, UInt, Bool, Bool, UInt) = {

    val debuggerPacketInterpreter = Module(
      new DebuggerPacketInterpreter(
        debug,
        numberOfInputPins,
        numberOfOutputPins,
        bramAddrWidth,
        bramDataWidth
      )
    )

    val outputPin = Wire(Vec(numberOfOutputPins, UInt((1.W))))
    val rdWrAddr = Wire(UInt(bramAddrWidth.W))
    val wrEna = Wire(Bool())
    val wrData = Wire(UInt(bramDataWidth.W))
    val interpretationDone = Wire(Bool())
    val foundValidPacket = Wire(Bool())
    val requestedActionOfThePacket = Wire(UInt(32.W))

    //
    // Configure the input signals
    //
    debuggerPacketInterpreter.io.en := en
    debuggerPacketInterpreter.io.inputPin := inputPin
    debuggerPacketInterpreter.io.plInSignal := plInSignal
    debuggerPacketInterpreter.io.rdData := rdData

    //
    // Configure the output signals
    //
    outputPin := debuggerPacketInterpreter.io.outputPin
    rdWrAddr := debuggerPacketInterpreter.io.rdWrAddr
    wrEna := debuggerPacketInterpreter.io.wrEna
    wrData := debuggerPacketInterpreter.io.wrData

    //
    // Configure the output signals related to interpreted packets
    //
    interpretationDone := debuggerPacketInterpreter.io.interpretationDone
    foundValidPacket := debuggerPacketInterpreter.io.foundValidPacket
    requestedActionOfThePacket := debuggerPacketInterpreter.io.requestedActionOfThePacket

    //
    // Return the output result
    //
    (
      outputPin,
      rdWrAddr,
      wrEna,
      wrData,
      interpretationDone,
      foundValidPacket,
      requestedActionOfThePacket
    )
  }
}
