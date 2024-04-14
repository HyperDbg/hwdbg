# test_DebuggerModuleTestingBRAM.py

import random

import cocotb
from cocotb.clock import Clock
from cocotb.triggers import RisingEdge
from cocotb.types import LogicArray

@cocotb.test()
async def DebuggerModuleTestingBRAM_test(dut):
    """Test that d propagates to q"""

    # Assert initial output is unknown
    assert LogicArray(dut.io_outputPin_0.value) == LogicArray("X")
    assert LogicArray(dut.io_outputPin_1.value) == LogicArray("X")
    assert LogicArray(dut.io_outputPin_2.value) == LogicArray("X")
    assert LogicArray(dut.io_outputPin_3.value) == LogicArray("X")
    assert LogicArray(dut.io_outputPin_4.value) == LogicArray("X")
    assert LogicArray(dut.io_outputPin_5.value) == LogicArray("X")
    assert LogicArray(dut.io_outputPin_6.value) == LogicArray("X")
    assert LogicArray(dut.io_outputPin_7.value) == LogicArray("X")
    assert LogicArray(dut.io_outputPin_8.value) == LogicArray("X")
    assert LogicArray(dut.io_outputPin_9.value) == LogicArray("X")
    assert LogicArray(dut.io_outputPin_10.value) == LogicArray("X")
    assert LogicArray(dut.io_outputPin_11.value) == LogicArray("X")
    assert LogicArray(dut.io_outputPin_12.value) == LogicArray("X")
    assert LogicArray(dut.io_outputPin_13.value) == LogicArray("X")
    assert LogicArray(dut.io_outputPin_14.value) == LogicArray("X")
    assert LogicArray(dut.io_outputPin_15.value) == LogicArray("X")
        
    # Set initial input value to prevent it from floating
    dut.io_inputPin_0.value = 0
    dut.io_inputPin_1.value = 0
    dut.io_inputPin_2.value = 0
    dut.io_inputPin_3.value = 0
    dut.io_inputPin_4.value = 0
    dut.io_inputPin_5.value = 0
    dut.io_inputPin_6.value = 0
    dut.io_inputPin_7.value = 0
    dut.io_inputPin_8.value = 0
    dut.io_inputPin_9.value = 0
    dut.io_inputPin_10.value = 0
    dut.io_inputPin_11.value = 0
    dut.io_inputPin_12.value = 0
    dut.io_inputPin_13.value = 0
    dut.io_inputPin_14.value = 0
    dut.io_inputPin_15.value = 0

    clock = Clock(dut.clock, 10, units="ns")  # Create a 10ns period clock on port clock
    
    # Start the clock. Start it low to avoid issues on the first RisingEdge
    cocotb.start_soon(clock.start(start_high=False))

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
