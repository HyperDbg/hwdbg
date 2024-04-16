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
 *   2024-04-08
 *
 * @copyright
 *   This project is released under the GNU Public License v3.
 */
package hwdbg.communication

import chisel3._
import chisel3.util.{switch, is}
import circt.stage.ChiselStage

import hwdbg.configs._
import hwdbg.types._
import hwdbg.utils._
import hwdbg.constants._

object DebuggerPacketInterpreterEnums {
  object State extends ChiselEnum {
    val sIdle, sInit, sReadChecksum, sReadIndicator, sReadTypeOfThePacket, sReadRequestedActionOfThePacket, sDone = Value
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
    val requestedActionOfThePacket = Output(UInt(new DebuggerRemotePacket().getWidth.W)) // the requested action

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

  //
  // Rising-edge detector for interpretation signal
  //
  val risingEdgePlInSignal = io.plInSignal & !RegNext(io.plInSignal)

  //
  // Structure (as register) of the received packet buffer
  //
  val regReceivedPacketBuffer = RegInit(0.U.asTypeOf(new DebuggerRemotePacket()))

  //
  // Apply the chip enable signal
  //
  when(io.en === true.B) {

    switch(state) {

      is(sIdle) {

        //
        // Create logs from communication structure offsets
        //
        LogInfo(debug)(f"The offset of Checksum is 0x${regReceivedPacketBuffer.Offset.checksum}%x")
        LogInfo(debug)(f"The offset of Indicator is 0x${regReceivedPacketBuffer.Offset.indicator}%x")
        LogInfo(debug)(f"The offset of TypeOfThePacket is 0x${regReceivedPacketBuffer.Offset.typeOfThePacket}%x")
        LogInfo(debug)(f"The offset of RequestedActionOfThePacket is 0x${regReceivedPacketBuffer.Offset.requestedActionOfThePacket}%x")

        //
        // Check whether the interrupt from the PS is received or not
        //
        when(risingEdgePlInSignal === true.B) {
          state := sInit
        }

        //
        // Configure the registers in case of sIdle
        //
        regRdWrAddr := 0.U
        regInterpretationDone := false.B
        regFoundValidPacket := false.B
        regReceivedPacketBuffer.RequestedActionOfThePacket := 0.U

      }
      is(sInit) {

        //
        // Adjust address to read Checksum from BRAM
        //
        regRdWrAddr := regReceivedPacketBuffer.Offset.checksum.U

        //
        // Goes to the next section
        //
        state := sReadChecksum
      }
      is(sReadChecksum) {

        //
        // Read the Checksum
        //
        regReceivedPacketBuffer.Checksum := io.rdData

        //
        // Adjust address to read Indicator from BRAM
        //
        regRdWrAddr := regReceivedPacketBuffer.Offset.indicator.U

        //
        // Goes to the next section
        //
        state := sReadIndicator
      }
      is(sReadIndicator) {

        //
        // Read the Indicator
        //
        regReceivedPacketBuffer.Indicator := io.rdData

        //
        // Adjust address to read TypeOfThePacket from BRAM
        //
        regRdWrAddr := regReceivedPacketBuffer.Offset.typeOfThePacket.U

        //
        // Goes to the next section
        //
        state := sReadTypeOfThePacket

      }
      is(sReadTypeOfThePacket) {

        //
        // Read the TypeOfThePacket
        //
        regReceivedPacketBuffer.TypeOfThePacket := io.rdData

        //
        // Adjust address to read RequestedActionOfThePacket from BRAM
        //
        regRdWrAddr := regReceivedPacketBuffer.Offset.requestedActionOfThePacket.U

        //
        // Check whether the indicator is valid or not
        //
        when(regReceivedPacketBuffer.Indicator === HyperDbgSharedConstants.INDICATOR_OF_HYPERDBG_PACKET.U) {

          //
          // Indicator of packet is valid
          // (Goes to the next section)
          //
          state := sReadRequestedActionOfThePacket

        }.otherwise {

          //
          // Type of packet is not valid
          // (interpretation was done but not found a valid packet,
          // so, go to the idle state)
          //
          regInterpretationDone := true.B
          state := sIdle
        }
      }
      is(sReadRequestedActionOfThePacket) {

        //
        // Read the RequestedActionOfThePacket
        //
        regReceivedPacketBuffer.RequestedActionOfThePacket := io.rdData

        //
        // Check whether the type of the packet is valid or not
        //
        when(regReceivedPacketBuffer.Indicator === HyperDbgSharedConstants.INDICATOR_OF_HYPERDBG_PACKET.U) {

          //
          // Type of packet is valid
          // All the values are now valid
          // (Goes to the next section)
          //
          state := sDone
        }.otherwise {

          //
          // Type of packet is not valid
          // (interpretation was done but not found a valid packet,
          // so, go to the idle state)
          //
          regInterpretationDone := true.B
          state := sIdle
        }
      }
      is(sDone) {

        //
        // Adjust the output bits
        //
        regInterpretationDone := true.B
        regFoundValidPacket := true.B

        //
        // Go to the idle state
        //
        state := sIdle
      }
    }
  }

  // ---------------------------------------------------------------------

  //
  // Connect output pins to internal registers
  //
  io.rdWrAddr := regRdWrAddr
  io.interpretationDone := regInterpretationDone
  io.foundValidPacket := regFoundValidPacket
  io.requestedActionOfThePacket := regReceivedPacketBuffer.RequestedActionOfThePacket

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
    val requestedActionOfThePacket = Wire(
      UInt(new DebuggerRemotePacket().getWidth.W)
    )

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
    (rdWrAddr, interpretationDone, foundValidPacket, requestedActionOfThePacket)
  }
}