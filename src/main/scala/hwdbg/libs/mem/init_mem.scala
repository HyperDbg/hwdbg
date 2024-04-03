package hwdbg.libs.mem

import chisel3._
import chisel3.util.experimental.loadMemoryFromFileInline

class InitMemInline(
  debug: Boolean = DebuggerConfigurations.ENABLE_DEBUG,
  memoryFile: String = "",
  addrWidth: Int = DebuggerConfigurations.BLOCK_RAM_ADDR_WIDTH,
  width: Int = DebuggerConfigurations.BLOCK_RAM_DATA_WIDTH,
  size: Int = GeneralConfigurations.DEFAULT_CONFIGURATION_INITIALIZED_MEMORY_SIZE
  ) extends Module {

  val io = IO(new Bundle {
    val enable = Input(Bool())
    val write = Input(Bool())
    val addr = Input(UInt(10.W))
    val dataIn = Input(UInt(width.W))
    val dataOut = Output(UInt(width.W))
  })

  val mem =           SyncReadMem(size / width, UInt(width.W))  

  //
  // Initialize memory
  //
  if (memoryFile.trim().nonEmpty) {
    loadMemoryFromFileInline(mem, memoryFile)
  }

  io.dataOut := DontCare

  when(io.enable) {
    val rdwrPort = mem(io.addr)
    when (io.write) { rdwrPort := io.dataIn }
      .otherwise    { io.dataOut := rdwrPort }
  }
}