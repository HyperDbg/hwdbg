/**
 * @file
 *   port_information.scala
 * @author
 *   Sina Karvandi (sina@hyperdbg.org)
 * @brief
 *   Send port information (in interpreter)
 * @details
 * @version 0.1
 * @date
 *   2024-05-04
 *
 * @copyright
 *   This project is released under the GNU Public License v3.
 */
package hwdbg.communication.interpreter

import chisel3._
import chisel3.util.{switch, is}
import circt.stage.ChiselStage

import hwdbg.version._
import hwdbg.configs._

class InterpreterPortInformation(
    debug: Boolean = DebuggerConfigurations.ENABLE_DEBUG,
    bramDataWidth: Int = DebuggerConfigurations.BLOCK_RAM_DATA_WIDTH
) extends Module {

  val io = IO(new Bundle {

    //
    // Chip signals
    //
    val en = Input(Bool()) // chip enable signal

    //
    // Sending singals
    //
    val noNewDataSender = Output(Bool()) // should sender finish sending buffers or not?
    val dataValidOutput = Output(Bool()) // should sender send next buffer or not?
    val sendingData = Output(UInt(bramDataWidth.W)) // data to be sent to the debugger

  })

  //
  // Output pins
  //
  val noNewDataSender = WireInit(false.B)
  val dataValidOutput = WireInit(false.B)
  val sendingData = WireInit(0.U(bramDataWidth.W))

  //
  // Apply the chip enable signal
  //
  when(io.en === true.B) {

    //
    // Set the version
    //
    sendingData := Version.getEncodedVersion.U

    //
    // Sending the version in one clock cycle
    //
    noNewDataSender := true.B
    dataValidOutput := true.B

  }

  // ---------------------------------------------------------------------

  //
  // Connect output pins
  //
  io.noNewDataSender := noNewDataSender
  io.dataValidOutput := dataValidOutput
  io.sendingData := sendingData

}

object InterpreterPortInformation {

  def apply(
      debug: Boolean = DebuggerConfigurations.ENABLE_DEBUG,
      bramDataWidth: Int = DebuggerConfigurations.BLOCK_RAM_DATA_WIDTH
  )(
      en: Bool
  ): (Bool, Bool, UInt) = {

    val interpreterPortInformation = Module(
      new InterpreterPortInformation(
        debug,
        bramDataWidth
      )
    )

    val noNewDataSender = Wire(Bool())
    val dataValidOutput = Wire(Bool())
    val sendingData = Wire(UInt(bramDataWidth.W))

    //
    // Configure the input signals
    //
    interpreterPortInformation.io.en := en

    //
    // Configure the output signals
    //
    noNewDataSender := interpreterPortInformation.io.noNewDataSender
    dataValidOutput := interpreterPortInformation.io.dataValidOutput
    sendingData := interpreterPortInformation.io.sendingData

    //
    // Return the output result
    //
    (
      noNewDataSender,
      dataValidOutput,
      sendingData
    )
  }
}
