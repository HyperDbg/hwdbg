<p align="center">
   <img alt="hwdbg" title="hwdbg" src="https://github.com/HyperDbg/graphics/blob/master/Logos/hwdbg/hwdbg-high-resolution-logo-transparent.png?raw=true" width="300">
</p>

## Description
**hwdbg** is a chip-level debugger written in chisel. (This is a work in progress and not yet ready for testing!).

## Test

You should now have a working Chisel3 project.

You can run the included test with:
```sh
sbt test
```

Alternatively, if you use Mill:
```sh
mill hwdbg.test
```

You should see a whole bunch of output that ends with something like the following lines
```
[info] Tests: succeeded 1, failed 0, canceled 0, ignored 0, pending 0
[info] All tests passed.
[success] Total time: 5 s, completed Dec 16, 2020 12:18:44 PM
```
If you see the above then the test was successful.

### ModelSim

If you want to use ModelSim instead of GTKWave, you can configure the `modelsim.config` file. Please visit <a href="https://github.com/HyperDbg/hwdbg/blob/main/sim/README.md">here</a> for more information.

## Output 

The final generated codes for fuzzy controllers are available in the `generated` directory.

## License
**hwdbg** and all its submodules and repos, unless a license is otherwise specified, are licensed under **GPLv3** LICENSE.
