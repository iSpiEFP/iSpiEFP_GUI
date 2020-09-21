#!/bin/csh
#SBATCH -D iSpiClient/Gamess/jobs/meh/input/
#SBATCH -o ../output/meh.err
#SBATCH -A debug
#SBATCH -N 1
#SBATCH -n 20
#SBATCH -t 00:30:00
#SBATCH --mem=0
/depot/lslipche/apps/iSpiEFP/packages/gamess/gamess/rungms meh.inp > ../output/meh.out
