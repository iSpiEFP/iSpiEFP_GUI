#!/bin/csh
#SBATCH -D ~/iSpiClient/Gamess/jobs/Mon_Oct_26_11:02:28_EDT_2020/input/
#SBATCH -o ../output/Mon_Oct_26_11:02:28_EDT_2020.err
#SBATCH -A debug
#SBATCH -N 1
#SBATCH -n 20
#SBATCH -t 00:30:00
#SBATCH --mem=0
/depot/lslipche/apps/iSpiEFP/packages/gamess/gamess/rungms Mon_Oct_26_11:02:28_EDT_2020.inp > ../output/Mon_Oct_26_11:02:28_EDT_2020.out
