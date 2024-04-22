/**
 * @file
 *   receiver.scala
 * @author
 *   Sina Karvandi (sina@hyperdbg.org)
 * @brief
 *   Remote debugger packet receiver module
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

object DebuggerPacketReceiverEnums {
  object State extends ChiselEnum {
    val sIdle, sReadChecksum, sReadIndicator, sReadTypeOfThePacket, sReadRequestedActionOfThePacket, sRequestedActionIsValid, sReadActionBuffer,
        sDone = Value
  }
}

class DebuggerPacketReceiver(
    debug: Boolean = DebuggerConfigurations.ENABLE_DEBUG,
    bramAddrWidth: Int = DebuggerConfigurations.BLOCK_RAM_ADDR_WIDTH,
    bramDataWidth: Int = DebuggerConfigurations.BLOCK_RAM_DATA_WIDTH
) extends Module {

  //
  // Import state enum
  //
  import DebuggerPacketReceiverEnums.State
  import DebuggerPacketReceiverEnums.State._

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
    // Receiving signals
    //
    val requestedActionOfThePacketOutput = Output(UInt(new DebuggerRemotePacket().RequestedActionOfThePacket.getWidth.W)) // the requested action
    val requestedActionOfThePacketOutputValid = Output(Bool()) // whether data on the requested action is valid or not
    val noNewDataReceiver = Input(Bool()) // receive done or not?

    // this contains and edge-detection mechanism, which means reader should make it low after reading the data
    val readNextData = Input(Bool()) // whether the next data should be read or not?

    val dataValidOutput = Output(Bool()) // whether data on the receiving data line is valid or not?
    val receivingData = Output(UInt(bramDataWidth.W)) // data to be sent to the reader

    val finishedReceivingBuffer = Output(Bool()) // Receiving is done or not?

  })

  //
  // State registers
  //
  val state = RegInit(sIdle)

  //
  // Output pins
  //
  val rdWrAddr = WireInit(0.U(bramAddrWidth.W))
  val regRdWrAddr = RegInit(0.U(bramAddrWidth.W))
  val finishedReceivingBuffer = WireInit(false.B)
  val requestedActionOfThePacketOutput = WireInit(0.U(new DebuggerRemotePacket().RequestedActionOfThePacket.getWidth.W))
  val requestedActionOfThePacketOutputValid = WireInit(false.B)
  val dataValidOutput = WireInit(false.B)
  val receivingData = WireInit(0.U(bramDataWidth.W))

  //
  // Rising-edge detector for start receiving signal
  //
  val risingEdgePlInSignal = io.plInSignal & !RegNext(io.plInSignal)

  //
  // Rising-edge detector for reading next data signal
  //
  val risingEdgeReadNextData = io.readNextData & !RegNext(io.readNextData)

  //
  // Structure (as wire) of the received packet buffer
  //
  val receivedPacketBuffer = WireInit(0.U.asTypeOf(new DebuggerRemotePacket())) // here the wire is not used

  //
  // Apply the chip enable signal
  //
  when(io.en === true.B) {

    switch(state) {

      is(sIdle) {

        //
        // Create logs from communication structure offsets
        //
        LogInfo(debug)(f"The offset of Checksum is 0x${receivedPacketBuffer.Offset.checksum}%x")
        LogInfo(debug)(f"The offset of Indicator is 0x${receivedPacketBuffer.Offset.indicator}%x")
        LogInfo(debug)(f"The offset of TypeOfThePacket is 0x${receivedPacketBuffer.Offset.typeOfThePacket}%x")
        LogInfo(debug)(f"The offset of requestedActionOfThePacketOutput is 0x${receivedPacketBuffer.Offset.requestedActionOfThePacket}%x")

        //
        // Check whether the interrupt from the PS is received or not
        //
        when(risingEdgePlInSignal === true.B) {
          state := sReadChecksum
        }

        //
        // Configure the output pins in case of sIdle
        //
        rdWrAddr := 0.U
        requestedActionOfThePacketOutput := 0.U
        requestedActionOfThePacketOutputValid := false.B
        dataValidOutput := false.B
        receivingData := 0.U
        finishedReceivingBuffer := false.B

      }
      is(sReadChecksum) {

        //
        // Adjust address to read Checksum from BRAM (Not Used)
        //
        rdWrAddr := (MemoryCommunicationConfigurations.BASE_ADDRESS_OF_PS_TO_PL_COMMUNICATION + receivedPacketBuffer.Offset.checksum).U

        //
        // Goes to the next section
        //
        state := sReadIndicator
      }
      is(sReadIndicator) {

        //
        // Adjust address to read Indicator from BRAM
        //
        rdWrAddr := (MemoryCommunicationConfigurations.BASE_ADDRESS_OF_PS_TO_PL_COMMUNICATION + receivedPacketBuffer.Offset.indicator).U

        //
        // Goes to the next section
        //
        state := sReadTypeOfThePacket
      }
      is(sReadTypeOfThePacket) {

        //
        // Adjust address to read TypeOfThePacket from BRAM
        //
        rdWrAddr := (MemoryCommunicationConfigurations.BASE_ADDRESS_OF_PS_TO_PL_COMMUNICATION + receivedPacketBuffer.Offset.typeOfThePacket).U

        //
        // Check whether the indicator is valid or not
        //
        LogInfo(debug)(
          f"Comparing first 0x${BitwiseFunction.printFirstNBits(HyperDbgSharedConstants.INDICATOR_OF_HYPERDBG_PACKET, bramDataWidth)}%x number of the indicator"
        )
        when(io.rdData === BitwiseFunction.printFirstNBits(HyperDbgSharedConstants.INDICATOR_OF_HYPERDBG_PACKET, bramDataWidth).U) {

          //
          // Indicator of packet is valid
          // (Goes to the next section)
          //
          state := sReadRequestedActionOfThePacket

        }.otherwise {

          //
          // Indicator of packet is not valid
          // (Receiving was done but not found a valid packet,
          // so, go to the idle state)
          //
          state := sDone
        }
      }
      is(sReadRequestedActionOfThePacket) {

        //
        // Adjust address to read RequestedActionOfThePacket from BRAM
        //
        rdWrAddr := (MemoryCommunicationConfigurations.BASE_ADDRESS_OF_PS_TO_PL_COMMUNICATION + receivedPacketBuffer.Offset.requestedActionOfThePacket).U

        //
        // Save the address into a register
        //
        regRdWrAddr := (MemoryCommunicationConfigurations.BASE_ADDRESS_OF_PS_TO_PL_COMMUNICATION + receivedPacketBuffer.Offset.requestedActionOfThePacket + (bramDataWidth >> 3)).U

        //
        // Check whether the type of the packet is valid or not
        //
        val packetType: DebuggerRemotePacketType.Value = DebuggerRemotePacketType.DEBUGGER_TO_DEBUGGEE_HARDWARE_LEVEL
        when(io.rdData === packetType.id.U) {

          //
          // Type of packet is valid
          // (Goes to the next section)
          //
          state := sRequestedActionIsValid

        }.otherwise {

          //
          // Type of packet is not valid
          // (Receiving was done but not found a valid packet,
          // so, go to the idle state)
          //
          state := sDone
        }

      }
      is(sRequestedActionIsValid) {

        //
        // Read the RequestedActionOfThePacket
        //
        requestedActionOfThePacketOutput := io.rdData

        //
        // The RequestedActionOfThePacketOutput is valid from now
        //
        requestedActionOfThePacketOutputValid := true.B

        //
        // Check if the caller needs to read the next part of
        // the block RAM or the receiving data should be finished
        //
        when(risingEdgeReadNextData === true.B) {

          //
          // Adjust address to read next data to BRAM
          //
          rdWrAddr := regRdWrAddr
          regRdWrAddr := regRdWrAddr + (bramDataWidth >> 3).U

          //
          // Read the next offset of the buffer
          //
          state := sReadActionBuffer

        }.elsewhen(io.noNewDataReceiver === true.B && io.readNextData === false.B) {

          //
          // No new data, the receiving is done
          //
          state := sDone

        }.otherwise {

          //
          // Stay at the same state
          //
          state := sRequestedActionIsValid
        }

      }
      is(sReadActionBuffer) {

        //
        // Data outputs are now valid
        //
        dataValidOutput := true.B

        //
        // Adjust the read buffer data
        //
        receivingData := io.rdData

        //
        // Return to the previous state of action
        //
        state := sRequestedActionIsValid

      }
      is(sDone) {

        //
        // Reset the temporary address holder
        //
        regRdWrAddr := 0.U

        //
        // The receiving is done at this stage, either
        // was successful of unsucessful, we'll release the
        // sharing bram resource by indicating that the receiving
        // module is no longer using the bram line
        //
        finishedReceivingBuffer := true.B

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
  io.rdWrAddr := rdWrAddr
  io.requestedActionOfThePacketOutput := requestedActionOfThePacketOutput
  io.requestedActionOfThePacketOutputValid := requestedActionOfThePacketOutputValid
  io.dataValidOutput := dataValidOutput
  io.receivingData := receivingData
  io.finishedReceivingBuffer := finishedReceivingBuffer

}

object DebuggerPacketReceiver {

  def apply(
      debug: Boolean = DebuggerConfigurations.ENABLE_DEBUG,
      bramAddrWidth: Int = DebuggerConfigurations.BLOCK_RAM_ADDR_WIDTH,
      bramDataWidth: Int = DebuggerConfigurations.BLOCK_RAM_DATA_WIDTH
  )(
      en: Bool,
      plInSignal: Bool,
      rdData: UInt,
      noNewDataReceiver: Bool,
      readNextData: Bool
  ): (UInt, UInt, Bool, Bool, UInt, Bool) = {

    val debuggerPacketReceiver = Module(
      new DebuggerPacketReceiver(
        debug,
        bramAddrWidth,
        bramDataWidth
      )
    )

    val rdWrAddr = Wire(UInt(bramAddrWidth.W))
    val requestedActionOfThePacketOutput = Wire(UInt(new DebuggerRemotePacket().RequestedActionOfThePacket.getWidth.W))
    val requestedActionOfThePacketOutputValid = Wire(Bool())
    val dataValidOutput = Wire(Bool())
    val receivingData = Wire(UInt(bramDataWidth.W))
    val finishedReceivingBuffer = Wire(Bool())

    //
    // Configure the input signals
    //
    debuggerPacketReceiver.io.en := en
    debuggerPacketReceiver.io.plInSignal := plInSignal
    debuggerPacketReceiver.io.rdData := rdData
    debuggerPacketReceiver.io.noNewDataReceiver := noNewDataReceiver
    debuggerPacketReceiver.io.readNextData := readNextData

    //
    // Configure the output signals
    //
    rdWrAddr := debuggerPacketReceiver.io.rdWrAddr

    //
    // Configure the output signals related to received packets
    //
    requestedActionOfThePacketOutput := debuggerPacketReceiver.io.requestedActionOfThePacketOutput
    requestedActionOfThePacketOutputValid := debuggerPacketReceiver.io.requestedActionOfThePacketOutputValid
    dataValidOutput := debuggerPacketReceiver.io.dataValidOutput
    receivingData := debuggerPacketReceiver.io.receivingData
    finishedReceivingBuffer := debuggerPacketReceiver.io.finishedReceivingBuffer

    //
    // Return the output result
    //
    (
      rdWrAddr,
      requestedActionOfThePacketOutput,
      requestedActionOfThePacketOutputValid,
      dataValidOutput,
      receivingData,
      finishedReceivingBuffer
    )
  }
}

object ReceiverModule extends App {

  //
  // Generate hwdbg verilog files
  //
  println(
    ChiselStage.emitSystemVerilog(
      new DebuggerPacketReceiver(
        DebuggerConfigurations.ENABLE_DEBUG,
        DebuggerConfigurations.BLOCK_RAM_ADDR_WIDTH,
        DebuggerConfigurations.BLOCK_RAM_DATA_WIDTH
      ),
      firtoolOpts = Array(
        "-disable-all-randomization",
        "--lowering-options=disallowLocalVariables", // because icarus doesn't support 'automatic logic', this option prevents such logics
        "-strip-debug-info",
        "--split-verilog", // The intention for this argument (and next argument) is to separate generated files.
        "-o",
        "generated/"
      )
    )
  )
}
