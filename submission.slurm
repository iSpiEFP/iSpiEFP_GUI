#!/bin/csh
#SBATCH -D ~/iSpiClient/Gamess/jobs/test/input/
#SBATCH -o ../output/test.err
#SBATCH -A debug
#SBATCH -N 1
#SBATCH -n 20
#SBATCH -t 00:30:00
#SBATCH --mem=1024
/depot/lslipche/apps/iSpiEFP/packages/gamess/gamess/rungms test.inp > ../output/test.out
