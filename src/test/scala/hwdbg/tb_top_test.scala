/** @file
  *   tb_top_test.scala
  * @author
  *   Sina Karvandi (sina@hyperdbg.org)
  * @brief
  *   Testbench for hwdbg's top module (with BRAM)
  * @details
  * @version 0.1
  * @date
  *   2024-04-12
  *
  * @copyright
  *   This project is released under the GNU Public License v3.
  */
package hwdbg

import chisel3._
import chisel3.experimental.BundleLiterals._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

import hwdbg._
import hwdbg.configs._

/** This is an example of how to run hwdbg test within sbt, use:
  * {{{
  * testOnly hwdbg.HwdbgTest
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly hwdbg.HwdbgTest'
  * }}}
  * Testing from mill:
  * {{{
  * mill hwdbg.test.testOnly hwdbg.HwdbgTest
  * }}}
  */
class HwdbgTest extends AnyFreeSpec with Matchers {

  "Data for the shared block RAM (BRAM) is provided statically" in {
    simulate(
      new DebuggerModule(
        DebuggerConfigurations.ENABLE_DEBUG,
        DebuggerConfigurations.NUMBER_OF_INPUT_PINS,
        DebuggerConfigurations.NUMBER_OF_OUTPUT_PINS,
        DebuggerConfigurations.BLOCK_RAM_ADDR_WIDTH,
        DebuggerConfigurations.BLOCK_RAM_DATA_WIDTH
      )
    ) { dut =>
      // dut.reset.poke(true.B)
      dut.clock.step()
      // dut.reset.poke(false.B)
      dut.clock.step()

    }
  }
}
