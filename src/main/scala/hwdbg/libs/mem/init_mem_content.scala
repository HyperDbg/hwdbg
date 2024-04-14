/** @file
  *   init_mem_content.scala
  * @author
  *   Sina Karvandi (sina@hyperdbg.org)
  * @brief
  *   Initialize SRAM memory from a file (directly from the content of file)
  * @details
  * @version 0.1
  * @date
  *   2024-04-14
  *
  * @copyright
  *   This project is released under the GNU Public License v3.
  */
package hwdbg.libs.mem

import scala.collection.mutable.ArrayBuffer
import scala.io.Source

import chisel3._

import hwdbg.configs._

object Tools {
  def readmemh(path: String, width: Int): Seq[UInt] = {
    val buffer = new ArrayBuffer[UInt]
    for (line <- Source.fromFile(path).getLines()) {
      val tokens: Array[String] = line.split("(//)").map(_.trim)
      if (tokens.nonEmpty && tokens.head != "") {
        val i = Integer.parseInt(tokens.head, 16)
        buffer.append(i.U(width.W))
      }
    }
    buffer.toSeq
  }
}

class InitMemContent(
    debug: Boolean = DebuggerConfigurations.ENABLE_DEBUG,
    memoryFile: String = TestingConfigurations.BRAM_INITIALIZATION_FILE_PATH,
    addrWidth: Int = DebuggerConfigurations.BLOCK_RAM_ADDR_WIDTH,
    width: Int = DebuggerConfigurations.BLOCK_RAM_DATA_WIDTH,
    size: Int =
      GeneralConfigurations.DEFAULT_CONFIGURATION_INITIALIZED_MEMORY_SIZE
) extends Module {

  val io = IO(new Bundle {
    val enable = Input(Bool())
    val write = Input(Bool())
    val addr = Input(UInt(addrWidth.W))
    val dataIn = Input(UInt(width.W))
    val dataOut = Output(UInt(width.W))
  })

  val mem = RegInit(VecInit(Tools.readmemh(memoryFile, width)))

  when(io.enable) {
    val rdwrPort = mem(io.addr)
    io.dataOut := rdwrPort

    when(io.write) {
      mem(io.addr) := io.dataIn
    }
  }.otherwise {
    io.dataOut := 0.U
  }
}

object InitMemContent {

  def apply(
      debug: Boolean = DebuggerConfigurations.ENABLE_DEBUG,
      memoryFile: String = TestingConfigurations.BRAM_INITIALIZATION_FILE_PATH,
      addrWidth: Int = DebuggerConfigurations.BLOCK_RAM_ADDR_WIDTH,
      width: Int = DebuggerConfigurations.BLOCK_RAM_DATA_WIDTH,
      size: Int =
        GeneralConfigurations.DEFAULT_CONFIGURATION_INITIALIZED_MEMORY_SIZE
  )(
      enable: Bool,
      write: Bool,
      addr: UInt,
      dataIn: UInt
  ): UInt = {

    val initMemContentModule = Module(
      new InitMemContent(
        debug,
        memoryFile,
        addrWidth,
        width,
        size
      )
    )

    val dataOut = Wire(UInt(width.W))

    //
    // Configure the input signals
    //
    initMemContentModule.io.enable := enable
    initMemContentModule.io.write := write
    initMemContentModule.io.addr := addr
    initMemContentModule.io.dataIn := dataIn

    //
    // Configure the output signals
    //
    dataOut := initMemContentModule.io.dataOut

    //
    // Return the output result
    //
    dataOut
  }
}
