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
import circt.stage.ChiselStage

import hwdbg.configs._

class SendReceiveSynchronizer(
    debug: Boolean = DebuggerConfigurations.ENABLE_DEBUG,
    bramAddrWidth: Int = DebuggerConfigurations.BLOCK_RAM_ADDR_WIDTH,
    bramDataWidth: Int = DebuggerConfigurations.BLOCK_RAM_DATA_WIDTH
) extends Module {

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
    // Interpreter ports
    //
    val interpretationDone = Output(Bool()) // interpretation done or not?
    val foundValidPacket = Output(Bool()) // packet was valid or not
    val requestedActionOfThePacket = Output(UInt(new DebuggerRemotePacket().RequestedActionOfThePacket.getWidth.W)) // the requested action

    //
    // Sender ports
    //
    val beginSendingBuffer = Input(Bool()) // should sender start sending buffers or not?
    val noNewData = Input(Bool()) // should sender finish sending buffers or not?
    val dataValid = Input(Bool()) // should sender send next buffer or not?

    val sendWaitForBuffer = Output(Bool()) // should the external module send next buffer or not?
    val finishedSendingBuffer = Output(Bool()) // indicate that the sender finished sending buffers and ready to send next packet

    val requestedActionOfThePacket = Input(UInt(new DebuggerRemotePacket().RequestedActionOfThePacket.getWidth.W)) // the requested action
    val sendingData = Input(UInt(bramDataWidth.W)) // data to be sent to the debugger

  })

  //
  // To be implemented
  //

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
    debuggerMainModule.io.en := en
    debuggerMainModule.io.plInSignal := plInSignal
    debuggerMainModule.io.rdData := rdData

    //
    // Configure the output signals
    //
    psOutInterrupt := debuggerMainModule.io.psOutInterrupt
    rdWrAddr := debuggerMainModule.io.rdWrAddr
    wrEna := debuggerMainModule.io.wrEna
    wrData := debuggerMainModule.io.wrData

    //
    // Return the output result
    //
    (psOutInterrupt, rdWrAddr, wrEna, wrData)
  }
}
