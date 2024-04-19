/**
 * @file
 *   interpreter.scala
 * @author
 *   Sina Karvandi (sina@hyperdbg.org)
 * @brief
 *   Remote debugger packet interpreter module
 * @details
 * @version 0.1
 * @date
 *   2024-04-19
 *
 * @copyright
 *   This project is released under the GNU Public License v3.
 */
package hwdbg.communication.interpreter

import chisel3._
import chisel3.util.{switch, is}
import circt.stage.ChiselStage

import hwdbg.configs._
import hwdbg.types._
import hwdbg.utils._
import hwdbg.constants._

object DebuggerPacketInterpreterEnums {
  object State extends ChiselEnum {
    val sIdle, sDone = Value
  }
}

class DebuggerPacketInterpreter(
    debug: Boolean = DebuggerConfigurations.ENABLE_DEBUG,
    bramAddrWidth: Int = DebuggerConfigurations.BLOCK_RAM_ADDR_WIDTH,
    bramDataWidth: Int = DebuggerConfigurations.BLOCK_RAM_DATA_WIDTH
) extends Module {

  //
  // Import state enum
  //
  import DebuggerPacketInterpreterEnums.State
  import DebuggerPacketInterpreterEnums.State._

  val io = IO(new Bundle {

    //
    // Chip signals
    //
    val en = Input(Bool()) // chip enable signal

    //
    // Receiving signals
    //
    val requestedActionOfThePacketInput = Input(UInt(new DebuggerRemotePacket().RequestedActionOfThePacket.getWidth.W)) // the requested action
    val requestedActionOfThePacketInputValid = Input(Bool()) // whether data on the requested action is valid or not

    val noNewDataReceiver = Output(Bool()) // are interpreter expects more data?
    val readNextData = Output(Bool()) // whether the next data should be read or not?

    val dataValidInput = Input(Bool()) // whether data on the receiving data line is valid or not?
    val receivingData = Input(UInt(bramDataWidth.W)) // data to be received in interpreter

    //
    // Sending singals
    //
    val beginSendingBuffer = Output(Bool()) // should sender start sending buffers or not?
    val noNewDataSender = Output(Bool()) // should sender finish sending buffers or not?
    val dataValidOutput = Output(Bool()) // should sender send next buffer or not?

    val sendWaitForBuffer = Input(Bool()) // should the interpreter send next buffer or not?

    val requestedActionOfThePacketOutput = Output(UInt(new DebuggerRemotePacket().RequestedActionOfThePacket.getWidth.W)) // the requested action
    val sendingData = Output(UInt(bramDataWidth.W)) // data to be sent to the debugger

  })

  //
  // State registers
  //
  val state = RegInit(sIdle)

  //
  // Output pins
  //
  val noNewDataReceiver = WireInit(false.B)
  val readNextData = WireInit(false.B)

  val beginSendingBuffer = WireInit(false.B)
  val noNewDataSender = WireInit(false.B)
  val dataValidOutput = WireInit(false.B)
  val requestedActionOfThePacketOutput = WireInit(0.U(new DebuggerRemotePacket().RequestedActionOfThePacket.getWidth.W))
  val sendingData = WireInit(0.U(bramDataWidth.W))

  //
  // Apply the chip enable signal
  //
  when(io.en === true.B) {

    switch(state) {

      is(sIdle) {

        //
        // Go to the idle state
        //
        state := sDone
      }
      is(sDone) {

        //
        // Go to the idle state
        //
        state := sIdle
      }
    }
  }

  // ---------------------------------------------------------------------

  //
  // Connect output pins
  //
  io.noNewDataReceiver := noNewDataReceiver
  io.readNextData := readNextData

  io.beginSendingBuffer := beginSendingBuffer
  io.noNewDataSender := noNewDataSender
  io.dataValidOutput := dataValidOutput
  io.requestedActionOfThePacketOutput := requestedActionOfThePacketOutput
  io.sendingData := sendingData

}

object DebuggerPacketInterpreter {

  def apply(
      debug: Boolean = DebuggerConfigurations.ENABLE_DEBUG,
      bramAddrWidth: Int = DebuggerConfigurations.BLOCK_RAM_ADDR_WIDTH,
      bramDataWidth: Int = DebuggerConfigurations.BLOCK_RAM_DATA_WIDTH
  )(
      en: Bool,
      requestedActionOfThePacketInput: UInt,
      requestedActionOfThePacketInputValid: Bool,
      dataValidInput: Bool,
      receivingData: UInt,
      sendWaitForBuffer: Bool
  ): (Bool, Bool, Bool, Bool, Bool, UInt, UInt) = {

    val debuggerPacketInterpreter = Module(
      new DebuggerPacketInterpreter(
        debug,
        bramAddrWidth,
        bramDataWidth
      )
    )

    val noNewDataReceiver = Wire(Bool())
    val readNextData = Wire(Bool())

    val beginSendingBuffer = Wire(Bool())
    val noNewDataSender = Wire(Bool())
    val dataValidOutput = Wire(Bool())

    val requestedActionOfThePacketOutput = Wire(UInt(new DebuggerRemotePacket().RequestedActionOfThePacket.getWidth.W))
    val sendingData = Wire(UInt(bramDataWidth.W))

    //
    // Configure the input signals
    //
    debuggerPacketInterpreter.io.en := en

    //
    // Configure the input signals related to the receiving signals
    //
    debuggerPacketInterpreter.io.requestedActionOfThePacketInput := requestedActionOfThePacketInput
    debuggerPacketInterpreter.io.requestedActionOfThePacketInputValid := requestedActionOfThePacketInputValid
    debuggerPacketInterpreter.io.dataValidInput := dataValidInput
    debuggerPacketInterpreter.io.receivingData := receivingData

    //
    // Configure the input signals related to the sending signals
    //
    debuggerPacketInterpreter.io.sendWaitForBuffer := sendWaitForBuffer

    //
    // Configure the output signals
    //
    noNewDataReceiver := debuggerPacketInterpreter.io.noNewDataReceiver
    readNextData := debuggerPacketInterpreter.io.readNextData

    //
    // Configure the output signals related to sending packets
    //
    beginSendingBuffer := debuggerPacketInterpreter.io.beginSendingBuffer
    noNewDataSender := debuggerPacketInterpreter.io.noNewDataSender
    dataValidOutput := debuggerPacketInterpreter.io.dataValidOutput

    //
    // Configure the output signals related to received packets
    //
    requestedActionOfThePacketOutput := debuggerPacketInterpreter.io.requestedActionOfThePacketOutput
    sendingData := debuggerPacketInterpreter.io.sendingData

    //
    // Return the output result
    //
    (noNewDataReceiver, readNextData, beginSendingBuffer, noNewDataSender, dataValidOutput, requestedActionOfThePacketOutput, sendingData)
  }
}
