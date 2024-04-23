##
# @file test_DebuggerPacketReceiver.py
#
# @author Sina Karvandi (sina@hyperdbg.org)
#
# @brief Testing module for DebuggerPacketReceiver
#
# @details
#
# @version 0.1
#
# @date 2024-04-22
#
# @copyright This project is released under the GNU Public License v3.
#

import random

import cocotb
from cocotb.clock import Clock
from cocotb.triggers import RisingEdge
from cocotb.types import LogicArray

'''
  input         clock,
                reset,
                io_en,
                io_plInSignal,
  output [12:0] io_rdWrAddr,
  input  [31:0] io_rdData,
  output [31:0] io_requestedActionOfThePacketOutput,
  output        io_requestedActionOfThePacketOutputValid,
  input         io_noNewDataReceiver,
                io_readNextData,
  output        io_dataValidOutput,
  output [31:0] io_receivingData,
  output        io_finishedReceivingBuffer
'''

@cocotb.test()
async def DebuggerPacketReceiver_test(dut):
    """Test DebuggerPacketReceiver module"""

    #
    # Assert initial output is unknown
    #
    assert LogicArray(dut.io_rdWrAddr.value) == LogicArray("XXXXXXXXXXXXX")
    assert LogicArray(dut.io_requestedActionOfThePacketOutput.value) == LogicArray("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
    assert LogicArray(dut.io_requestedActionOfThePacketOutputValid.value) == LogicArray("X")
    assert LogicArray(dut.io_dataValidOutput.value) == LogicArray("X")
    assert LogicArray(dut.io_receivingData.value) == LogicArray("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
    assert LogicArray(dut.io_finishedReceivingBuffer.value) == LogicArray("X")

    clock = Clock(dut.clock, 10, units="ns")  # Create a 10ns period clock on port clock
    
    #
    # Start the clock. Start it low to avoid issues on the first RisingEdge
    #
    cocotb.start_soon(clock.start(start_high=False))
    
    dut._log.info("Initialize and reset module")

    #
    # Initial values
    #
    dut.io_en.value = 0
    dut.io_readNextData.value = 0
    dut.io_noNewDataReceiver.value = 0
    dut.io_plInSignal.value = 0

    #
    # Reset DUT
    #
    dut.reset.value = 1
    for _ in range(10):
        await RisingEdge(dut.clock)
    dut.reset.value = 0

    dut._log.info("Enabling chip")

    #
    # Enable chip
    #
    dut.io_en.value = 1

    for test_number in range(10):

        dut._log.info("Enable receiving data on the chip (" + str(test_number) + ")")

        #
        # Tell the receiver to start receiving data (This mainly operates based on
        # a rising-edge detector, so we'll need to make it low)
        #
        dut.io_plInSignal.value = 1
        await RisingEdge(dut.clock)
        dut.io_plInSignal.value = 0

        #
        # Wait until the data is received
        #
        for _ in range(20):
            if (dut.io_dataValidOutput.value == 1):
                break
            else:
                match dut.io_rdWrAddr.value:
                    case 0x0: # checksum
                        dut.io_rdData.value = 0x00001234
                    case 0x8: # indicator
                        dut.io_rdData.value = 0x48595045 # first 32 bits of the indicator
                    case 0x10: # type
                        dut.io_rdData.value = 0x4 # debugger to hardware packet (DEBUGGER_TO_DEBUGGEE_HARDWARE_LEVEL)
                    case 0x14: # requested action
                        dut.io_rdData.value = 0x14141414
                    case _:
                        assert "invalid address in the address line"
            await RisingEdge(dut.clock)

        
        #
        # No new data needed to be received
        #
        dut.io_noNewDataReceiver.value = 1
        
        #
        # Run extra waiting clocks
        #
        for _ in range(10):
            await RisingEdge(dut.clock)

        #
        # Check the final input on the next clock
        #
        await RisingEdge(dut.clock)
