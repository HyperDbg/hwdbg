/** @file
  *   top.scala
  * @author
  *   Sina Karvandi (sina@hyperdbg.org)
  * @brief
  *   hwdbg's top module
  * @details
  * @version 0.1
  * @date
  *   2024-04-03
  *
  * @copyright
  *   This project is released under the GNU Public License v3.
  */
package hwdbg

import chisel3._
import chisel3.util.Counter
import circt.stage.ChiselStage

import hwdbg.configs._

class DebuggerModule(
    debug: Boolean = DebuggerConfigurations.ENABLE_DEBUG,
    numberOfInputPins: Int = DebuggerConfigurations.NUMBER_OF_INPUT_PINS,
    numberOfOutputPins: Int = DebuggerConfigurations.NUMBER_OF_OUTPUT_PINS,
    bramAddrWidth: Int = DebuggerConfigurations.BLOCK_RAM_ADDR_WIDTH,
    bramDataWidth: Int = DebuggerConfigurations.BLOCK_RAM_DATA_WIDTH
) extends Module {
  val io = IO(new Bundle {

    //
    // Chip signals
    //
    val en = Input(Bool()) // chip enable signal

    //
    // Input/Output signals
    //
    val inputPin = Input(Vec(numberOfInputPins, UInt((1.W)))) // input pins
    val outputPin = Output(Vec(numberOfOutputPins, UInt((1.W)))) // output pins

    //
    // Interrupt signals (lines)
    //
    val plInSignal = Input(Bool()) // PS to PL signal
    val psOutInterrupt = Output(Bool()) // PL to PS interrupt

    //
    // BRAM (Block RAM) ports
    //
    val rdAddr = Input(UInt(bramAddrWidth.W)) // read address
    val rdData = Output(UInt(bramDataWidth.W)) // read data
    val wrAddr = Input(UInt(bramAddrWidth.W)) // write address
    val wrEna = Input(Bool()) // enable writing
    val wrData = Input(UInt(bramDataWidth.W)) // write data

  })

}

object Main extends App {

  //
  // Generate hwdbg verilog files
  //
  println(
    ChiselStage.emitSystemVerilog(
      new DebuggerModule(
        DebuggerConfigurations.ENABLE_DEBUG,
        DebuggerConfigurations.NUMBER_OF_INPUT_PINS,
        DebuggerConfigurations.NUMBER_OF_OUTPUT_PINS,
        DebuggerConfigurations.BLOCK_RAM_ADDR_WIDTH,
        DebuggerConfigurations.BLOCK_RAM_DATA_WIDTH
      ),
      firtoolOpts = Array(
        "-disable-all-randomization",
        "-strip-debug-info",
        "--split-verilog", // The intention for this argument (and next argument) is to separate generated files.
        "-o",
        "generated/"
      )
    )
  )
}
