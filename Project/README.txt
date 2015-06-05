Escape Analysis for Multi-Threaded Javali
-----------------------------------------

# Compiling

In order to use POSIX threads, `libjavali.a` needs to be compiled. The source
is found inside the `rt/` directory. The test harness as well als the `main`
shell script are modified to link against `libjavali.a`. It is also built
as part of the Ant and Eclipse full-build target.

# Testing

Because our code is not compatible with the reference solution, we split the
test cases in javali_tests:

    javali_tests/escape_analysis

        These test cases use our built-in objects. They can be tested against
        our stack allocation regression test, but cannot be compared against
        the reference solution.

    javali_tests/exec

        These test cases are executable on the reference solution.

In order to test against the reference solution, set 
AbstractTestSamplePrograms.TEST_STACK_ALLOCATION to `false` and make sure
only the programs in `javali_tests/exec` are tested.
