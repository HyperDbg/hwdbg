<p align="center">
   <img alt="hwdbg" title="hwdbg" src="https://github.com/HyperDbg/graphics/blob/master/Logos/hwdbg/hwdbg-high-resolution-logo-transparent.png?raw=true" width="300">
</p>

## Description
**hwdbg** is a chip-level debugger written in chisel. (This is a work in progress and not yet ready for testing!).

## Dependencies

#### JDK 8 or newer

We recommend LTS releases Java 8 and Java 11. You can install the JDK as your operating system recommends, or use the prebuilt binaries from [AdoptOpenJDK](https://adoptopenjdk.net/).

#### SBT or mill

SBT is the most common build tool in the Scala community. You can download it [here](https://www.scala-sbt.org/download.html).  
mill is another Scala/Java build tool without obscure DSL like SBT. You can download it [here](https://github.com/com-lihaoyi/mill/releases)

#### Verilator

The test with `svsim` needs Verilator installed.
See Verilator installation instructions [here](https://verilator.org/guide/latest/install.html).

### Test

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

## License
**hwdbg**, and all its submodules and repos, unless a license is otherwise specified, are licensed under **GPLv3** LICENSE.
