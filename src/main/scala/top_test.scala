/** @file
  *   top_test.scala
  * @author
  *   Sina Karvandi (sina@hyperdbg.org)
  * @brief
  *   hwdbg's top module (with BRAM) for testing
  * @details
  * @version 0.1
  * @date
  *   2024-04-04
  *
  * @copyright
  *   This project is released under the GNU Public License v3.
  */
import chisel3._
import chisel3.util.Counter
import circt.stage.ChiselStage

import hwdbg._
import hwdbg.configs._
import hwdbg.libs.mem._

class DebuggerModuleTestingBRAM(
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
    // *** BRAM (Block RAM) ports are initialized from an external file ***
    //

  })

  val bramEn = WireInit(false.B)
  val bramWrite = WireInit(false.B)
  val bramAddr = WireInit(0.U(bramAddrWidth.W))
  val bramDataIn = WireInit(0.U(bramDataWidth.W))

  //
  // Instantiate the BRAM memory initializer module
  //
  val dataOut =
    InitMemInline(
      debug,
      TestingConfigurations.BRAM_INITIALIZATION_FILE_PATH,
      bramAddrWidth,
      bramDataWidth,
      GeneralConfigurations.DEFAULT_CONFIGURATION_INITIALIZED_MEMORY_SIZE
    )(
      bramEn,
      bramWrite,
      bramAddr,
      bramDataIn
    )

  //
  // Instantiate the debugger's main module
  //
  val (outputPin, psOutInterrupt, rdData) =
    DebuggerMain(
      debug,
      numberOfInputPins,
      numberOfOutputPins,
      bramAddrWidth,
      bramDataWidth
    )(
      io.en,
      io.inputPin,
      io.plInSignal,
      bramAddr,
      bramAddr,
      bramEn,
      dataOut
    )

  io.outputPin := outputPin
  io.psOutInterrupt := psOutInterrupt
  bramDataIn := rdData

}
