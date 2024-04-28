##
# @file test_DebuggerModuleTestingBRAM.py
#
# @author Sina Karvandi (sina@hyperdbg.org)
#
# @brief Testing module for DebuggerModuleTestingBRAM
#
# @details
#
# @version 0.1
#
# @date 2024-04-21
#
# @copyright This project is released under the GNU Public License v3.
#

import random

import cocotb
from cocotb.clock import Clock
from cocotb.triggers import RisingEdge
from cocotb.types import LogicArray

@cocotb.test()
async def DebuggerModuleTestingBRAM_test(dut):
    """Test hwdbg module (with pre-defined BRAM)"""

    # Assert initial output is unknown
    assert LogicArray(dut.io_outputPin_0.value) == LogicArray("Z")
    assert LogicArray(dut.io_outputPin_1.value) == LogicArray("Z")
    assert LogicArray(dut.io_outputPin_2.value) == LogicArray("Z")
    assert LogicArray(dut.io_outputPin_3.value) == LogicArray("Z")
    assert LogicArray(dut.io_outputPin_4.value) == LogicArray("Z")
    assert LogicArray(dut.io_outputPin_5.value) == LogicArray("Z")
    assert LogicArray(dut.io_outputPin_6.value) == LogicArray("Z")
    assert LogicArray(dut.io_outputPin_7.value) == LogicArray("Z")
    assert LogicArray(dut.io_outputPin_8.value) == LogicArray("Z")
    assert LogicArray(dut.io_outputPin_9.value) == LogicArray("Z")
    assert LogicArray(dut.io_outputPin_10.value) == LogicArray("Z")
    assert LogicArray(dut.io_outputPin_11.value) == LogicArray("Z")
    assert LogicArray(dut.io_outputPin_12.value) == LogicArray("Z")
    assert LogicArray(dut.io_outputPin_13.value) == LogicArray("Z")
    assert LogicArray(dut.io_outputPin_14.value) == LogicArray("Z")
    assert LogicArray(dut.io_outputPin_15.value) == LogicArray("Z")
    assert LogicArray(dut.io_outputPin_16.value) == LogicArray("Z")
    assert LogicArray(dut.io_outputPin_17.value) == LogicArray("Z")
    assert LogicArray(dut.io_outputPin_18.value) == LogicArray("Z")
    assert LogicArray(dut.io_outputPin_19.value) == LogicArray("Z")
    assert LogicArray(dut.io_outputPin_20.value) == LogicArray("Z")
    assert LogicArray(dut.io_outputPin_21.value) == LogicArray("Z")
    assert LogicArray(dut.io_outputPin_22.value) == LogicArray("Z")
    assert LogicArray(dut.io_outputPin_23.value) == LogicArray("Z")
    assert LogicArray(dut.io_outputPin_24.value) == LogicArray("Z")
    assert LogicArray(dut.io_outputPin_25.value) == LogicArray("Z")
    assert LogicArray(dut.io_outputPin_26.value) == LogicArray("Z")
    assert LogicArray(dut.io_outputPin_27.value) == LogicArray("Z")
    assert LogicArray(dut.io_outputPin_28.value) == LogicArray("Z")
    assert LogicArray(dut.io_outputPin_29.value) == LogicArray("Z")
    assert LogicArray(dut.io_outputPin_30.value) == LogicArray("Z")
    assert LogicArray(dut.io_outputPin_31.value) == LogicArray("Z")

    clock = Clock(dut.clock, 10, units="ns")  # Create a 10ns period clock on port clock
    
    # Start the clock. Start it low to avoid issues on the first RisingEdge
    cocotb.start_soon(clock.start(start_high=False))
    
    dut._log.info("Initialize and reset module")

    # Initial values
    dut.io_en.value = 0
    dut.io_plInSignal.value = 0
   
    # Reset DUT
    dut.reset.value = 1
    for _ in range(10):
        await RisingEdge(dut.clock)
    dut.reset.value = 0

    dut._log.info("Enabling an interrupting chip to receive commands from BRAM")

    # Enable chip
    dut.io_en.value = 1

    # Tell the hwdbg to receive BRAM results
    dut.io_plInSignal.value = 1

    # Set initial input value to prevent it from floating
    dut.io_inputPin_0.value = 1
    dut.io_inputPin_1.value = 0
    dut.io_inputPin_2.value = 1
    dut.io_inputPin_3.value = 0
    dut.io_inputPin_4.value = 1
    dut.io_inputPin_5.value = 0
    dut.io_inputPin_6.value = 1
    dut.io_inputPin_7.value = 0
    dut.io_inputPin_8.value = 1
    dut.io_inputPin_9.value = 0
    dut.io_inputPin_10.value = 1
    dut.io_inputPin_11.value = 0
    dut.io_inputPin_12.value = 1
    dut.io_inputPin_13.value = 0
    dut.io_inputPin_14.value = 1
    dut.io_inputPin_15.value = 0
    dut.io_inputPin_16.value = 0
    dut.io_inputPin_17.value = 0
    dut.io_inputPin_18.value = 0
    dut.io_inputPin_19.value = 0
    dut.io_inputPin_20.value = 0
    dut.io_inputPin_21.value = 0
    dut.io_inputPin_22.value = 0
    dut.io_inputPin_23.value = 0
    dut.io_inputPin_24.value = 0
    dut.io_inputPin_25.value = 1
    dut.io_inputPin_26.value = 1
    dut.io_inputPin_27.value = 1
    dut.io_inputPin_28.value = 1
    dut.io_inputPin_29.value = 1
    dut.io_inputPin_30.value = 1
    dut.io_inputPin_31.value = 1

    # Synchronize with the clock. This will regisiter the initial `inputPinX` value
    await RisingEdge(dut.clock)
    
    expected_val = 0  # Matches initial input value
    for i in range(10):
        val = random.randint(0, 1)
        dut.io_inputPin_0.value = val  # Assign the random value val to the input port d
        await RisingEdge(dut.clock)
        #assert dut.io_inputPin_0.value == expected_val, f"output q was incorrect on the {i}th cycle"
        expected_val = val # Save random value for next RisingEdge

    # Check the final input on the next clock
    await RisingEdge(dut.clock)
