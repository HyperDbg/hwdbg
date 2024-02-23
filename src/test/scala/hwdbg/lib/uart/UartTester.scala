import chisel3._
import chisel3.experimental.BundleLiterals._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

import hwdbg.lib.uart._


class UartTxTests extends AnyFreeSpec with Matchers {
  "UartTx should work" in {
    simulate(new Tx(10000, 3000)) { dut =>
      dut.clock.step(2)
      // ready/valid handshake the first character
      dut.io.channel.valid.poke(true.B)
      dut.io.channel.bits.poke('a'.toInt.U)
      while (!dut.io.channel.ready.peek().litToBoolean) {
        dut.clock.step(1)
      }
      dut.clock.step(1)
      dut.io.channel.valid.poke(false.B)
      dut.io.channel.bits.poke(0.U)

      // wait for start bit
      while (dut.io.txd.peek().litValue != 0) {
        dut.clock.step(1)
      }
      // to the first bit
      dut.clock.step(3)

      for (i <- 0 until 8) {
        dut.io.txd.expect((('a'.toInt >> i) & 0x01).U)
        dut.clock.step(3)
      }
      // stop bit
      dut.io.txd.expect(1.U)
    }
  }
}

class UartSenderTests extends AnyFreeSpec with Matchers {
  "UartSender should work" in {
    simulate(new Sender(10000, 3000)) { dut =>
      dut.clock.step(300)
    }
  }
}

class UartRxTests extends AnyFreeSpec with Matchers {
  "UartRx should work" in {
    simulate(new Rx(10000, 3000)) { dut =>
      dut.io.rxd.poke(1.U)
      dut.clock.step(10)
      // start bit
      dut.io.rxd.poke(0.U)
      dut.clock.step(3)
      // 8 data bits
      for (i <- 0 until 8) {
        dut.io.rxd.poke(((0xa5 >> i) & 0x01).U)
        dut.clock.step(3)
      }
      // stop bit
      dut.io.rxd.poke(1.U)
      while (!dut.io.channel.valid.peek().litToBoolean) {
        // wait on valid
        dut.clock.step(1)
      }
      dut.io.channel.bits.expect(0xa5.U)

      // read it out
      dut.io.channel.ready.poke(true.B)
      dut.clock.step(1)
      dut.io.channel.ready.poke(false.B)
      dut.clock.step(5)
    }
  }
}
