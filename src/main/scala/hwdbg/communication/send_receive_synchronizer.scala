/**
 * @file
 *   send_receive_synchronizer.scala
 * @author
 *   Sina Karvandi (sina@hyperdbg.org)
 * @brief
 *   Send and receive synchronizer module
 * @details
 * @version 0.1
 * @date
 *   2024-04-17
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

object SendReceiveSynchronizerEnums {
  object State extends ChiselEnum {
    val sIdle, sReceiver, sSender = Value
  }
}

class SendReceiveSynchronizer(
    debug: Boolean = DebuggerConfigurations.ENABLE_DEBUG,
    bramAddrWidth: Int = DebuggerConfigurations.BLOCK_RAM_ADDR_WIDTH,
    bramDataWidth: Int = DebuggerConfigurations.BLOCK_RAM_DATA_WIDTH
) extends Module {

  //
  // Import state enum
  //
  import SendReceiveSynchronizerEnums.State
  import SendReceiveSynchronizerEnums.State._

  val io = IO(new Bundle {

    //
    // Chip signals
    //
    val en = Input(Bool()) // chip enable signal

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

    //
    // Receiver signals
    //
    val requestedActionOfThePacketOutput = Output(UInt(new DebuggerRemotePacket().RequestedActionOfThePacket.getWidth.W)) // the requested action
    val requestedActionOfThePacketOutputValid = Output(Bool()) // whether data on the requested action is valid or not

    val noNewDataReceiver = Input(Bool()) // receive done or not?
    val readNextData = Input(Bool()) // whether the next data should be read or not?

    val dataValidOutput = Output(Bool()) // whether data on the receiving data line is valid or not?
    val receivingData = Output(UInt(bramDataWidth.W)) // data to be sent to the reader

    val finishedReceivingBuffer = Output(Bool()) // Receiving is done or not?

    //
    // Sender ports
    //
    val beginSendingBuffer = Input(Bool()) // should sender start sending buffers or not?
    val noNewDataSender = Input(Bool()) // should sender finish sending buffers or not?
    val dataValidInput = Input(Bool()) // should sender send next buffer or not?

    val sendWaitForBuffer = Output(Bool()) // should the external module send next buffer or not?
    val finishedSendingBuffer = Output(Bool()) // indicate that the sender finished sending buffers and ready to send next packet

    val requestedActionOfThePacketInput = Input(UInt(new DebuggerRemotePacket().RequestedActionOfThePacket.getWidth.W)) // the requested action
    val sendingData = Input(UInt(bramDataWidth.W)) // data to be sent to the debugger

  })

  //
  // State registers
  //
  val state = RegInit(sIdle)

  //
  // Saving state of the controlling pins
  //
  val regPlInSignal = RegInit(false.B)
  val regBeginSendingBuffer = RegInit(false.B)

  //
  // Shared BRAM pins
  //
  val sharedRdWrAddr = WireInit(0.U(bramAddrWidth.W)) // read/write address
  val sharedRdData = WireInit(0.U(bramDataWidth.W)) // read data
  val sharedWrEna = WireInit(false.B) // enable writing
  val sharedWrData = WireInit(0.U(bramDataWidth.W)) // write data

  //
  // Instantiate the packet receiver module
  //
  val (
    receiverRdWrAddr,
    requestedActionOfThePacketOutput,
    requestedActionOfThePacketOutputValid,
    dataValidOutput,
    receivingData,
    finishedReceivingBuffer
  ) =
    DebuggerPacketReceiver(
      debug,
      bramAddrWidth,
      bramDataWidth
    )(
      io.en,
      regPlInSignal,
      io.rdData,
      io.noNewDataReceiver,
      io.readNextData
    )

  //
  // Instantiate the packet sender module
  //
  val (
    psOutInterrupt,
    senderRdWrAddr,
    wrEna,
    wrData,
    sendWaitForBuffer,
    finishedSendingBuffer
  ) =
    DebuggerPacketSender(
      debug,
      bramAddrWidth,
      bramDataWidth
    )(
      io.en,
      regBeginSendingBuffer,
      io.noNewDataSender,
      io.dataValidInput,
      io.requestedActionOfThePacketInput,
      io.sendingData
    )

  //
  // Apply the chip enable signal
  //
  when(io.en === true.B) {

    switch(state) {

      is(sIdle) {

        //
        // Peform the resource sepration of shared BRAM
        // and apply priority to receive over send
        //
        when(io.plInSignal === true.B) {

          //
          // Activate the receiver module
          //
          regPlInSignal := true.B

          //
          // Go to the receiver state
          //
          state := sReceiver

        }.elsewhen(io.beginSendingBuffer === true.B && io.plInSignal === false.B) {

          //
          // Activate the sender module
          //
          regBeginSendingBuffer := true.B

          //
          // Go to the sender state
          //
          state := sReceiver

        }.otherwise {

          //
          // Stay at the same state as there is no communication
          //
          state := sIdle

        }

      }
      is(sReceiver) {

        //
        // Check whether the receiving is finished
        //
        when(finishedReceivingBuffer === true.B) {

          //
          // No longer in the receiver state
          //
          regPlInSignal := false.B

          //
          // Go to the idle state
          //
          state := sIdle

        }.otherwise {

          //
          // Connect the address of BRAM reader to the receiver address
          //
          sharedRdWrAddr := receiverRdWrAddr

          //
          // On the receiver, writing is not allowed
          //
          sharedWrEna := false.B

          //
          // Stay at the same state
          //
          state := sReceiver

        }
      }
      is(sSender) {

        //
        // Check whether sending data is finished
        //
        when(finishedSendingBuffer === true.B) {

          //
          // No longer in the sender state
          //
          regBeginSendingBuffer := false.B

          //
          // Go to the idle state
          //
          state := sIdle

        }.otherwise {

          //
          // Connect shared BRAM signals to the sender
          //
          sharedRdWrAddr := senderRdWrAddr
          sharedWrEna := wrEna
          sharedWrData := wrData

          //
          // Stay at the same state
          //
          state := sSender

        }
      }
    }
  }
}

object SendReceiveSynchronizer {

  def apply(
      debug: Boolean = DebuggerConfigurations.ENABLE_DEBUG,
      bramAddrWidth: Int = DebuggerConfigurations.BLOCK_RAM_ADDR_WIDTH,
      bramDataWidth: Int = DebuggerConfigurations.BLOCK_RAM_DATA_WIDTH
  )(
      en: Bool,
      plInSignal: Bool,
      rdData: UInt
  ): (Bool, UInt, Bool, UInt) = {

    val sendReceiveSynchronizerModule = Module(
      new SendReceiveSynchronizer(
        debug,
        bramAddrWidth,
        bramDataWidth
      )
    )

    val psOutInterrupt = Wire(Bool())
    val rdWrAddr = Wire(UInt(bramAddrWidth.W))
    val wrEna = Wire(Bool())
    val wrData = Wire(UInt(bramDataWidth.W))

    //
    // Configure the input signals
    //
    sendReceiveSynchronizerModule.io.en := en
    sendReceiveSynchronizerModule.io.plInSignal := plInSignal
    sendReceiveSynchronizerModule.io.rdData := rdData

    //
    // Configure the output signals
    //
    psOutInterrupt := sendReceiveSynchronizerModule.io.psOutInterrupt
    rdWrAddr := sendReceiveSynchronizerModule.io.rdWrAddr
    wrEna := sendReceiveSynchronizerModule.io.wrEna
    wrData := sendReceiveSynchronizerModule.io.wrData

    //
    // Return the output result
    //
    (psOutInterrupt, rdWrAddr, wrEna, wrData)
  }
}
