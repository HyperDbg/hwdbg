/**
 * @file
 *   sender.scala
 * @author
 *   Sina Karvandi (sina@hyperdbg.org)
 * @brief
 *   Remote debugger packet sender module
 * @details
 * @version 0.1
 * @date
 *   2024-04-16
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
import hwdbg.constants._

object DebuggerPacketSenderEnums {
  object State extends ChiselEnum {
    val sIdle, sWriteChecksum, sWriteIndicator, sWriteTypeOfThePacket, sWriteRequestedActionOfThePacket, sWriteSendingDataArray, sDone = Value
  }
}

class DebuggerPacketSender(
    debug: Boolean = DebuggerConfigurations.ENABLE_DEBUG,
    bramAddrWidth: Int = DebuggerConfigurations.BLOCK_RAM_ADDR_WIDTH,
    bramDataWidth: Int = DebuggerConfigurations.BLOCK_RAM_DATA_WIDTH,
    lengthOfDataSendingArray: Int = DebuggerConfigurations.LENGTH_OF_DATA_SENDING_ARRAY
) extends Module {

  //
  // Import state enum
  //
  import DebuggerPacketSenderEnums.State
  import DebuggerPacketSenderEnums.State._

  val io = IO(new Bundle {

    //
    // Chip signals
    //
    val en = Input(Bool()) // chip enable signal

    //
    // Interrupt signals (lines)
    // Note: Only PS output signal is exported here,
    // a separate module will control the PL signal
    //
    val psOutInterrupt = Output(Bool()) // PL to PS interrupt

    //
    // BRAM (Block RAM) ports
    //
    val rdWrAddr = Output(UInt(bramAddrWidth.W)) // read/write address
    val wrEna = Output(Bool()) // enable writing
    val wrData = Output(UInt(bramDataWidth.W)) // write data

    //
    // Sending signals
    //
    val beginSendingBuffer = Input(Bool()) // should sender start sending buffers or not?
    val sendingSignalDone = Output(Bool()) // sending signal done or not?
    val requestedActionOfThePacket = Input(UInt(new DebuggerRemotePacket().RequestedActionOfThePacket.getWidth.W)) // the requested action
    val sendingDataArray = Input(Vec(lengthOfDataSendingArray, UInt((bramDataWidth.W)))) // data to be sent to the debugger

  })

  //
  // State registers
  //
  val state = RegInit(sIdle)

  //
  // Output pins (registers)
  //
  val regPsOutInterrupt = RegInit(false.B)
  val regRdWrAddr = RegInit(0.U(bramAddrWidth.W))
  val regWrEna = RegInit(false.B)
  val regWrData = RegInit(0.U(bramDataWidth.W))
  val regSendingSignalDone = RegInit(false.B)

  //
  // Rising-edge detector for start sending signal
  //
  val risingEdgeBeginSendingBuffer = io.beginSendingBuffer & !RegNext(io.beginSendingBuffer)

  //
  // Structure (as wire) of the received packet buffer
  //
  val sendingPacketBuffer = WireInit(0.U.asTypeOf(new DebuggerRemotePacket()))

  //
  // Apply the chip enable signal
  //
  when(io.en === true.B) {

    switch(state) {

      is(sIdle) {

        //
        // Check whether the interrupt from the PS is received or not
        //
        when(risingEdgeBeginSendingBuffer === true.B) {
          state := sWriteChecksum
        }

        //
        // Configure the registers in case of sIdle
        //
        regPsOutInterrupt := false.B
        regRdWrAddr := 0.U
        regWrEna := false.B
        regWrData := 0.U
        regSendingSignalDone := false.B

      }
      is(sWriteChecksum) {

        //
        // Enable writing to the BRAM
        //
        regWrEna := true.B

        //
        // Adjust address to write Checksum to BRAM (Not Used)
        //
        regRdWrAddr := (MemoryCommunicationConfigurations.BASE_ADDRESS_OF_PL_TO_PS_COMMUNICATION + sendingPacketBuffer.Offset.checksum).U

        //
        // Adjust data to write Checksum
        //
        regWrData := 0.U // Checksum is ignored

        //
        // Goes to the next section
        //
        state := sWriteIndicator
      }
      is(sWriteIndicator) {

        //
        // Adjust address to write Indicator to BRAM
        //
        regRdWrAddr := (MemoryCommunicationConfigurations.BASE_ADDRESS_OF_PL_TO_PS_COMMUNICATION + sendingPacketBuffer.Offset.indicator).U

        //
        // Adjust data to write Indicator
        //
        regWrData := HyperDbgSharedConstants.INDICATOR_OF_HYPERDBG_PACKET.U

        //
        // Goes to the next section
        //
        state := sWriteTypeOfThePacket

      }
      is(sWriteTypeOfThePacket) {

        //
        // Adjust address to write type of packet to BRAM
        //
        regRdWrAddr := (MemoryCommunicationConfigurations.BASE_ADDRESS_OF_PL_TO_PS_COMMUNICATION + sendingPacketBuffer.Offset.typeOfThePacket).U

        //
        // Adjust data to write type of packet
        //
        val packetType: DebuggerRemotePacketType.Value = DebuggerRemotePacketType.DEBUGGEE_TO_DEBUGGER_HARDWARE_LEVEL
        regWrData := packetType.id.U

        //
        // Goes to the next section
        //
        state := sWriteRequestedActionOfThePacket

      }
      is(sWriteRequestedActionOfThePacket) {

        //
        // Adjust address to write requested action of packet to BRAM
        //
        regRdWrAddr := (MemoryCommunicationConfigurations.BASE_ADDRESS_OF_PL_TO_PS_COMMUNICATION + sendingPacketBuffer.Offset.requestedActionOfThePacket).U

        //
        // Adjust data to write requested action of packet
        //
        regWrData := io.requestedActionOfThePacket

        //
        // Goes to the next section
        //
        state := sDone // sWriteSendingDataArray

      }
      is(sDone) {

        //
        // Adjust the output bits
        //
        regSendingSignalDone := true.B

        //
        // Interrupt the PS
        //
        regPsOutInterrupt := true.B

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
  io.psOutInterrupt := regPsOutInterrupt
  io.rdWrAddr := regRdWrAddr
  io.wrEna := regWrEna
  io.wrData := regWrData
  io.sendingSignalDone := regSendingSignalDone
}

object DebuggerPacketSender {

  def apply(
      debug: Boolean = DebuggerConfigurations.ENABLE_DEBUG,
      bramAddrWidth: Int = DebuggerConfigurations.BLOCK_RAM_ADDR_WIDTH,
      bramDataWidth: Int = DebuggerConfigurations.BLOCK_RAM_DATA_WIDTH,
      lengthOfDataSendingArray: Int = DebuggerConfigurations.LENGTH_OF_DATA_SENDING_ARRAY
  )(
      en: Bool,
      beginSendingBuffer: Bool,
      requestedActionOfThePacket: UInt,
      sendingDataArray: Vec[UInt]
  ): (Bool, UInt, Bool, UInt, Bool) = {

    val debuggerPacketSender = Module(
      new DebuggerPacketSender(
        debug,
        bramAddrWidth,
        bramDataWidth,
        lengthOfDataSendingArray
      )
    )

    val psOutInterrupt = Wire(Bool())
    val rdWrAddr = Wire(UInt(bramAddrWidth.W))
    val wrEna = Wire(Bool())
    val wrData = Wire(UInt(bramDataWidth.W))
    val sendingSignalDone = Wire(Bool())

    //
    // Configure the input signals
    //
    debuggerPacketSender.io.en := en
    debuggerPacketSender.io.beginSendingBuffer := beginSendingBuffer
    debuggerPacketSender.io.requestedActionOfThePacket := requestedActionOfThePacket
    debuggerPacketSender.io.sendingDataArray := sendingDataArray

    //
    // Configure the output signals
    //
    psOutInterrupt := debuggerPacketSender.io.psOutInterrupt
    rdWrAddr := debuggerPacketSender.io.rdWrAddr
    wrEna := debuggerPacketSender.io.wrEna
    wrData := debuggerPacketSender.io.wrData

    //
    // Configure the output signals related to sending packets
    //
    sendingSignalDone := debuggerPacketSender.io.sendingSignalDone

    //
    // Return the output result
    //
    (psOutInterrupt, rdWrAddr, wrEna, wrData, sendingSignalDone)
  }
}
