#!/bin/csh
#SBATCH -o ../output/test34.err
#SBATCH -A debug
#SBATCH -N 1
#SBATCH -n 20
#SBATCH -t 00:30:00
#SBATCH --mem=1024
/depot/lslipche/apps/iSpiEFP/packages/gamess/gamess/rungms test34.inp > ../output/test34.out
