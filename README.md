# LOMS
(LO)gical (M)icro(S)imulator

## test commands
`$ java main -i full-adder.ins -d 1,1,0` Sum: 0 , Carry: 1
`$ java main -i xortest.ins -d 1,1` output: 0
**NOTE : you can replace `java main` with `./main`**

## Arguments
- `-i` input file
- `-d` data to input
- `--benchmark 1/0` Optional. view time taken to complete execution.
- `--verbose 1/0` Optional. view commands executed.
