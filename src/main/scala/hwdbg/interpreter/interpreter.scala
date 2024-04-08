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
import chisel3.util.{switch, is}
import circt.stage.ChiselStage

import hwdbg.configs._
import hwdbg.types._

object DebuggerPacketInterpreterEnums {
  object State extends ChiselEnum {
    val sIdle, sInit = Value
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

    //
    // Interpretation signals
    //
    val interpretationDone = Output(Bool()) // interpretation done or not?
    val foundValidPacket = Output(Bool()) // packet was valid or not
    val requestedActionOfThePacket = Output(UInt(32.W)) // the requested action

  })

  //
  // State registers
  //
  val state = RegInit(sIdle)

  //
  // Output pins (registers)
  //
  val regRdWrAddr = RegInit(0.U(bramAddrWidth.W))
  val regInterpretationDone = RegInit(false.B)
  val regFoundValidPacket = RegInit(false.B)
  val regRequestedActionOfThePacket = RegInit(0.U(32.W))

  // -------------------------------

  /*
  val receivedPacketBuffer = Wire(new DebuggerRemotePacket())
  receivedPacketBuffer.key := io.key
  receivedPacketBuffer.value := io.value
  io.struct_key := ms.key
  io.struct_value := ms.value
   */

  switch(state) {

    is(sIdle) {

      //
      // Check whether the interrupt from the PS is received or not
      //
      when(io.en === true.B && io.plInSignal === true.B) {
        state := sInit
      }

      //
      // Configure the registers in case of sIdle
      //
      regRdWrAddr := 0.U
      regInterpretationDone := false.B
      regFoundValidPacket := false.B
      regRequestedActionOfThePacket := 0.U

    }
    is(sInit) {

      //
      // Test
      //
    }
  }

  // ---------------------------------------------------------------------

  //
  // Connect output pins to internal registers
  //
  io.rdWrAddr := regRdWrAddr
  io.interpretationDone := regInterpretationDone
  io.foundValidPacket := regFoundValidPacket
  io.requestedActionOfThePacket := regRequestedActionOfThePacket

}

object DebuggerPacketInterpreter {

  def apply(
      debug: Boolean = DebuggerConfigurations.ENABLE_DEBUG,
      bramAddrWidth: Int = DebuggerConfigurations.BLOCK_RAM_ADDR_WIDTH,
      bramDataWidth: Int = DebuggerConfigurations.BLOCK_RAM_DATA_WIDTH
  )(
      en: Bool,
      plInSignal: Bool,
      rdData: UInt
  ): (UInt, Bool, Bool, UInt) = {

    val debuggerPacketInterpreter = Module(
      new DebuggerPacketInterpreter(
        debug,
        bramAddrWidth,
        bramDataWidth
      )
    )

    val rdWrAddr = Wire(UInt(bramAddrWidth.W))
    val interpretationDone = Wire(Bool())
    val foundValidPacket = Wire(Bool())
    val requestedActionOfThePacket = Wire(UInt(32.W))

    //
    // Configure the input signals
    //
    debuggerPacketInterpreter.io.en := en
    debuggerPacketInterpreter.io.plInSignal := plInSignal
    debuggerPacketInterpreter.io.rdData := rdData

    //
    // Configure the output signals
    //
    rdWrAddr := debuggerPacketInterpreter.io.rdWrAddr

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
      rdWrAddr,
      interpretationDone,
      foundValidPacket,
      requestedActionOfThePacket
    )
  }
}
