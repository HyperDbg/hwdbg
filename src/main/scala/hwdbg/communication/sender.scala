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
import hwdbg.utils._
import hwdbg.constants._

object DebuggerPacketSenderEnums {
  object State extends ChiselEnum {
    val sIdle, sInit, sDone = Value
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
    val requestedActionOfThePacket = Output(UInt(new DebuggerRemotePacket().getWidth.W)) // the requested action
    val sendingDataArray = Input(Vec(lengthOfDataSendingArray, UInt((bramDataWidth.W)))) // data to be sent to the debugger

  })

}
