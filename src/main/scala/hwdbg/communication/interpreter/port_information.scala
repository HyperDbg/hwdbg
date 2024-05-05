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
import hwdbg.utils._

object InterpreterPortInformationEnums {
  object State extends ChiselEnum {
    val sIdle, sSendCountOfInputPorts, sSendCountOfOutputPorts, sSendPortItems, sDone = Value
  }
}

class InterpreterPortInformation(
    debug: Boolean = DebuggerConfigurations.ENABLE_DEBUG,
    bramDataWidth: Int = DebuggerConfigurations.BLOCK_RAM_DATA_WIDTH,
    inputPortsConfiguration: Map[Int, Int] = DebuggerPorts.PORT_PINS_MAP_INPUT,
    outputPortsConfiguration: Map[Int, Int] = DebuggerPorts.PORT_PINS_MAP_OUTPUT
) extends Module {

  //
  // Import state enum
  //
  import InterpreterPortInformationEnums.State
  import InterpreterPortInformationEnums.State._

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
  // State registers
  //
  val state = RegInit(sIdle)

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

    switch(state) {

      is(sIdle) {

        //
        // Going to the next state (sending count of input ports)
        //
        state := sSendCountOfInputPorts
      }
      is(sSendCountOfInputPorts) {

        //
        // Send count of input ports
        //
        val numberOfInputPorts = inputPortsConfiguration.size
        LogInfo(debug)("Number of input ports (PORT_PINS_MAP_INPUT): " + numberOfInputPorts)

        sendingData := numberOfInputPorts.U

        //
        // Data is valid
        //
        noNewDataSender := true.B
        dataValidOutput := true.B

        //
        // Going to the next state (sending count of input ports)
        //
        state := sSendCountOfOutputPorts

      }
      is(sSendCountOfOutputPorts) {

        //
        // Send count of output ports
        //
        val numberOfOutputPorts = outputPortsConfiguration.size
        LogInfo(debug)("Number of output ports (PORT_PINS_MAP_OUTPUT): " + numberOfOutputPorts)

        sendingData := numberOfOutputPorts.U

        //
        // Data is valid
        //
        noNewDataSender := true.B
        dataValidOutput := true.B

        //
        // Next, we gonna send each ports' information ()
        //
        state := sSendPortItems

      }
      is(sSendPortItems) {

        //
        // Send input port items
        //
        LogInfo(debug)("Iterating over input pins:")

        inputPortsConfiguration.foreach { case (port, pins) =>
          LogInfo(debug)(s"Port $port has $pins pins")
        }

      }
    }

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
      bramDataWidth: Int = DebuggerConfigurations.BLOCK_RAM_DATA_WIDTH,
      inputPortsConfiguration: Map[Int, Int] = DebuggerPorts.PORT_PINS_MAP_INPUT,
      outputPortsConfiguration: Map[Int, Int] = DebuggerPorts.PORT_PINS_MAP_OUTPUT
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
